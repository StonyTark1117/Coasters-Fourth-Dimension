package dev.fourthdimension.editor;

import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class EditorNetwork {
  private EditorNetwork() {}

  public static final String OPERATOR_REQUEST_PREFIX = "ls-operator:";

  private static final boolean VALIDATION = Boolean.getBoolean("fourthdimension.validation");
  private static final Logger LOGGER = LogUtils.getLogger();

  public static Consumer<Target> openClient = target -> {};
  public static Consumer<Result> resultClient = result -> {};

  public record Target(UUID id, boolean vehicle) implements CustomPacketPayload {
    public static final Type<Target> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, "editor_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Target> CODEC =
        StreamCodec.of(
            (b, p) -> {
              b.writeUUID(p.id);
              b.writeBoolean(p.vehicle);
            },
            b -> new Target(b.readUUID(), b.readBoolean()));

    @Override
    public Type<Target> type() {
      return TYPE;
    }
  }

  public record Edit(UUID id, long revision, int action, String value)
      implements CustomPacketPayload {
    public static final Type<Edit> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, "editor_edit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Edit> CODEC =
        StreamCodec.of(
            (b, p) -> {
              b.writeUUID(p.id);
              b.writeLong(p.revision);
              b.writeVarInt(p.action);
              b.writeUtf(p.value, 16384);
            },
            b -> new Edit(b.readUUID(), b.readLong(), b.readVarInt(), b.readUtf(16384)));

    @Override
    public Type<Edit> type() {
      return TYPE;
    }
  }

  /** Acknowledges one edit after the authoritative snapshot has been sent. */
  public record Result(UUID id, long requestedRevision, int action, boolean success, String message)
      implements CustomPacketPayload {
    public static final Type<Result> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, "editor_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Result> CODEC =
        StreamCodec.of(
            (b, p) -> {
              b.writeUUID(p.id);
              b.writeLong(p.requestedRevision);
              b.writeVarInt(p.action);
              b.writeBoolean(p.success);
              b.writeUtf(p.message, 4096);
            },
            b -> new Result(b.readUUID(), b.readLong(), b.readVarInt(), b.readBoolean(), b.readUtf(4096)));

    @Override
    public Type<Result> type() {
      return TYPE;
    }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    var registrar = event.registrar("9");
    dev.fourthdimension.tools.ToolsNetwork.register(registrar);
    registrar.playToClient(
        Target.TYPE, Target.CODEC, (p, c) -> c.enqueueWork(() -> openClient.accept(p)));
    registrar.playToClient(
        Result.TYPE, Result.CODEC, (p, c) -> c.enqueueWork(() -> resultClient.accept(p)));
    registrar.playToServer(
        Edit.TYPE,
        Edit.CODEC,
        (p, c) ->
            c.enqueueWork(
                () -> {
                  if (!(c.player() instanceof ServerPlayer player)) return;
                  if(p.action==91){dev.fourthdimension.tools.ToolsNetwork.send(player,p.id);return;}
                  boolean success = false;
                  String message;
                  try {
                    apply(player, p);
                    success = true;
                    message = "4D settings updated";
                  } catch (IllegalArgumentException | IllegalStateException failure) {
                    message = Objects.requireNonNullElse(failure.getMessage(), "Unable to apply settings");
                  }
                  // Also refresh on rejection, including a stale revision. Never retry an edit
                  // automatically: the operator must review the current state first.
                  FoundationSync.send(player);
                  if (message.length() > 4096) message = message.substring(0, 4096);
                  PacketDistributor.sendToPlayer(
                      player, new Result(p.id, p.revision, p.action, success, message));
                  player.displayClientMessage(Component.literal(message), success);
                  if (VALIDATION)
                    LOGGER.info(
                        "FD_EDITOR_RESULT player={} target={} action={} success={} message={}",
                        player.getGameProfile().getName(), p.id, p.action, success, message);
                }));
  }

  public static RotationProfile parse(String text) {
    if (text.length() > 16384) throw new IllegalArgumentException("Profile payload too large");
    var parts = text.split(";", -1);
    if (parts.length < 2 || parts.length > 1024)
      throw new IllegalArgumentException("Expected 2..1024 profile keys");
    var keys = new ArrayList<RotationProfile.Key>();
    try {
      for (var part : parts) {
        var values = part.trim().split(":", -1);
        if (values.length < 2 || values.length > 3)
          throw new IllegalArgumentException(
              "Use distance:degrees[:slope] separated by semicolons");
        keys.add(
            new RotationProfile.Key(
                Double.parseDouble(values[0]),
                Math.toRadians(Double.parseDouble(values[1])),
                values.length == 3 ? Math.toRadians(Double.parseDouble(values[2])) : 0));
      }
    } catch (NumberFormatException failure) {
      throw new IllegalArgumentException("Invalid profile number");
    }
    return new RotationProfile(keys);
  }

  public static void apply(ServerPlayer player, Edit request) {
    if (request.value.length() > 16384)
      throw new IllegalArgumentException("Editor payload too large");
    var moving=FoundationData.get(player.serverLevel()).moving.retaining(request.id);
    if(moving!=null&&request.action!=10&&request.action!=11&&request.action!=12
        &&(request.action<34||request.action>37))
      throw new IllegalArgumentException("Moving support owns this car: use its controller; normal restraint controls require validated service access");
    var serviceCar=FoundationData.get(player.serverLevel()).vehicles.get(request.id);
    if(serviceCar!=null&&dev.fourthdimension.moving.MovingServiceRuntime.applies(player.serverLevel(),serviceCar)
        &&request.action!=10&&request.action!=11&&request.action!=12&&request.action!=13&&request.action!=61
        &&(request.action<34||request.action>37))
      throw new IllegalArgumentException("Released service handoff owns this car: Stop, validated restraints and explicit Dispatch remain available");
    if(request.action==92||request.action==93){dev.fourthdimension.tools.OperatorPanel.shuttle(player,request.id,request.revision,request.action==93);return;}
    if(request.action==90){dev.fourthdimension.tools.PresetService.apply(player,request.id,request.revision,request.value);return;}
    if(request.action==70){PoweredInvertedYawEditor.apply(player,request.id,request.revision,request.value);return;}
    if(request.action==8){NewGenerationFlyingEditor.apply(player,NewGenerationFlyingEditor.editable(player,request.id,request.revision),request.value);return;}
    if (request.action < 10) {
      var rail = ProfileEditor.requireEditable(player, request.id, request.revision);
      if(rail.variant.equals(NewGenerationFlyingVehicle.VARIANT)&&rail.yaw.owner!=null&&!player.hasPermissions(2)&&!player.getUUID().equals(rail.yaw.owner))throw new IllegalArgumentException("Only the route owner/operator may edit it");
      switch (request.action) {
        case 0 ->
            ProfileEditor.apply(player, request.id, request.revision, parse(request.value), false);
        case 1 -> ProfileEditor.rebind(player, request.id, request.revision);
        case 2 ->
            ProfileEditor.apply(
                player, request.id, request.revision, rail.profile.mirrored(), false);
        case 3 ->
            ProfileEditor.apply(
                player, request.id, request.revision, rail.profile.reversed(), false);
        case 4 ->
            dev.fourthdimension.magnetic.MagneticData.apply(
                player,
                request.id,
                request.revision,
                dev.fourthdimension.magnetic.MagneticData.parse(request.value));
        case 5 ->
            dev.fourthdimension.integration.AppearanceEditor.apply(
                player,
                request.id,
                request.revision,
                dev.fourthdimension.integration.TrackAppearance.parse(request.value));
        case 6 -> {
          if (rail.family != dev.fourthdimension.contract.Family.SPINNING && rail.family != dev.fourthdimension.contract.Family.SUSPENDED && rail.family != dev.fourthdimension.contract.Family.AXIS)
            throw new IllegalArgumentException("Lift lock requires Spinning, Suspended or Axis track");
          rail.liftLock = !rail.liftLock;
          rail.revision++;
          FoundationData.get(player.serverLevel()).setDirty();
        }
        case 7 -> {
          if (rail.family != dev.fourthdimension.contract.Family.AXIS) throw new IllegalArgumentException("Roll modes require Axis track");
          var modes=dev.fourthdimension.vehicle.AxisState.Mode.values();
          var old=rail.axisMode;rail.axisMode=modes[(old.ordinal()+1)%modes.length];
          if(rail.axisMode==dev.fourthdimension.vehicle.AxisState.Mode.DRIVE) {
            try { ProfileEditor.apply(player,request.id,request.revision,rail.profile,false); }
            catch(RuntimeException failure) { rail.axisMode=old;throw failure; }
          } else {rail.revision++;FoundationData.get(player.serverLevel()).setDirty();}
        }
        default -> throw new IllegalArgumentException("Unknown profile operation");
      }
    } else {
      var vehicle = VehicleOperations.accessible(player, request.id);
      if (vehicle.revision != request.revision)
        throw new IllegalArgumentException("Vehicle settings changed; review the refreshed state and try again");
      var level = player.serverLevel();
      if(NewGenerationFlyingVehicle.is(vehicle)) NewGenerationFlyingEditor.operator(player,vehicle);
      if(PoweredInvertedYawVehicle.is(vehicle))PoweredInvertedYawEditor.operator(player,vehicle);
      if(request.action>=71&&request.action<=74&&!PoweredInvertedYawVehicle.is(vehicle))throw new IllegalArgumentException("Powered controls require their matching vehicle");
      if(request.action>=40&&request.action<=42&&!NewGenerationFlyingVehicle.is(vehicle))throw new IllegalArgumentException("Compound Flying control requires its matching car");
      if(moving==null && dev.fourthdimension.integration.RideAuthority.externalSelected(vehicle)
          && ((request.action==10&&request.value.startsWith(OPERATOR_REQUEST_PREFIX))||request.action==13||request.action==61)) {
        var pos=vehicle.authority.controller;
        if(pos==null||!player.mayBuild()||!level.mayInteract(player,pos)
            ||player.distanceToSqr(pos.getCenter())>144)
          throw new IllegalArgumentException("Stand within twelve blocks of the bound controller");
        final long requestRevision;
        try {
          if(!request.value.startsWith(OPERATOR_REQUEST_PREFIX))throw new NumberFormatException();
          requestRevision=Long.parseLong(request.value.substring(OPERATOR_REQUEST_PREFIX.length()));
        }
        catch(NumberFormatException invalid){throw new IllegalArgumentException("Refresh the operator panel before an equipment request");}
        dev.fourthdimension.integration.RideAuthority.externalOperatorAction(level,vehicle,request.action,requestRevision);
        return;
      }
      switch (request.action) {
        case 81 -> dev.fourthdimension.tools.PaletteService.apply(player,vehicle,request.value);
        case 82 -> dev.fourthdimension.tools.BodyService.apply(player,vehicle,request.value);
        case 80 -> dev.fourthdimension.integration.SkinAdapter.select(player,vehicle,request.value);
        case 10 -> VehicleOperations.capture(level, vehicle);
        case 11 -> VehicleOperations.restraints(level, vehicle, true);
        case 12 -> VehicleOperations.restraints(level, vehicle, false);
        case 13 -> {
          if(PoweredInvertedYawVehicle.is(vehicle)){VehicleOperations.dispatch(level,vehicle);break;}
          if(dev.fourthdimension.vehicle.S2SpinningVehicle.is(vehicle)&&!dev.fourthdimension.vehicle.S2SpinningRuntime.optional(vehicle)) { VehicleOperations.dispatch(level,vehicle); break; }
          if(vehicle.authority.owner==null) VehicleOperations.dispatch(level,vehicle);
          else dev.fourthdimension.integration.RideAuthority.dispatch(level,vehicle.authority.controller,vehicle.chassis,dev.fourthdimension.integration.RideAuthority.token(vehicle));
        }
        case 94 -> dev.fourthdimension.integration.RideLocalOperations.admit(level, vehicle);
        case 95 -> dev.fourthdimension.integration.RideLocalOperations.resume(level, vehicle.chassis);
        case 71 -> PoweredInvertedYawRuntime.resume(level,vehicle);
        case 72,73 -> PoweredInvertedYawRuntime.direction(level,vehicle,request.action==72?1:-1);
        case 74 -> PoweredInvertedYawEditor.sound(player,vehicle);
        case 63,64 -> {
          if(!dev.fourthdimension.vehicle.S2SpinningVehicle.is(vehicle))throw new IllegalArgumentException("Direction controls require Launched Inverting Spinning");
          dev.fourthdimension.vehicle.S2SpinningRuntime.direction(level,vehicle,request.action==63?1:-1);
        }
        case 60,61,62,65,66,67,68 -> dev.fourthdimension.integration.RideAuthority.operate(player,vehicle,request.action);
        case 14 -> VehicleOperations.retention(level, vehicle, true);
        case 15 -> VehicleOperations.retention(level, vehicle, false);
        case 16 -> {
          var parts = request.value.split(",", -1);
          if (parts.length != 2) throw new IllegalArgumentException("Use damping,ballast");
          try {
            VehicleOperations.tune(
                level, vehicle, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
          } catch (NumberFormatException failure) {
            throw new IllegalArgumentException("Invalid tuning number");
          }
        }
        case 20 -> RecoveryOperations.attach(player, vehicle);
        case 21 -> RecoveryOperations.start(level, vehicle);
        case 22 -> RecoveryOperations.brake(level, vehicle);
        case 23 -> RecoveryOperations.detach(level, vehicle);
        case 24 -> dev.fourthdimension.shuttle.ShuttleRuntime.detachForRecovery(player, vehicle);
        case 25 -> VehicleOperations.repairMatchingSupport(level, vehicle);
        case 26 -> VehicleOperations.realignMatchingSupport(level, vehicle);
        case 27 -> VehicleOperations.retireDetachedPax(level, vehicle);
        case 28 -> {
          if(dev.fourthdimension.vehicle.DiveVehicle.is(vehicle)) { dev.fourthdimension.vehicle.DiveRuntime.resumeToDock(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.PipelineVehicle.is(vehicle)) { dev.fourthdimension.vehicle.PipelineRuntime.resumeToDock(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.StandingVehicle.is(vehicle)) { dev.fourthdimension.vehicle.StandingRuntime.resumeToDock(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.MotorbikeVehicle.is(vehicle)) { dev.fourthdimension.vehicle.MotorbikeRuntime.resumeToDock(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.FlyingBikeVehicle.is(vehicle)) { dev.fourthdimension.vehicle.FlyingBikeRuntime.resumeToDock(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.S2SpinningVehicle.is(vehicle)) { dev.fourthdimension.vehicle.S2SpinningRuntime.resume(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.RollerBallVehicle.is(vehicle)) { dev.fourthdimension.vehicle.RollerBallRuntime.resume(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.AxisVehicle.is(vehicle)) { dev.fourthdimension.vehicle.AxisRuntime.resumeToDock(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.FlyingVehicle.is(vehicle)) { dev.fourthdimension.vehicle.FlyingRuntime.resumeToDock(level,vehicle); break; }
          if(dev.fourthdimension.vehicle.FixedInvertedVehicle.is(vehicle)) {
            dev.fourthdimension.vehicle.FixedInvertedRuntime.resumeToDock(level,vehicle);break;
          }
          if(dev.fourthdimension.vehicle.SuspendedVehicle.is(vehicle)) {
            dev.fourthdimension.vehicle.SuspendedRuntime.resumeToDock(level,vehicle);break;
          }
          if (!dev.fourthdimension.vehicle.SpinningVehicle.is(vehicle))
            throw new IllegalArgumentException("Resume to dock requires Classic Spinning");
          dev.fourthdimension.vehicle.SpinningRuntime.resumeToDock(level, vehicle);
        }
        case 40 -> NewGenerationFlyingRuntime.unwind(level,vehicle);
        case 41 -> NewGenerationFlyingRuntime.retry(level,vehicle);
        case 42 -> {
          var fields=request.value.split("\\|",-1);if(fields.length!=3)throw new IllegalArgumentException("Invalid show request");
          NewGenerationFlyingRuntime.show(level,vehicle,UUID.fromString(fields[0]),Long.parseLong(fields[1]),fields[2]);
        }
        case 31, 32, 33, 34, 35, 36, 37 -> {
          if(NewGenerationFlyingVehicle.is(vehicle)&&request.action>=34){NewGenerationFlyingRuntime.restraints(level,vehicle,request.action<=35,request.action==34||request.action==36);break;}
          if (!dev.fourthdimension.vehicle.FlyingVehicle.is(vehicle)) throw new IllegalArgumentException("Flying controls require a Flying car");
          if (request.action <= 33) dev.fourthdimension.vehicle.FlyingRuntime.requestPosture(level,vehicle,request.action==31,request.action==33);
          else dev.fourthdimension.vehicle.FlyingRuntime.restraints(level,vehicle,request.action<=35,request.action==34||request.action==36);
        }
        case 17, 18, 19 -> VehicleOperations.loadingPreset(level, vehicle, request.action - 17);
        default -> throw new IllegalArgumentException("Unknown vehicle operation");
      }
      FoundationData.get(level).setDirty();
      FoundationSync.broadcast(level);
    }
  }

  public static void open(ServerPlayer player, Target target) {
    if (VALIDATION)
      LOGGER.info(
          "FD_EDITOR_OPEN player={} target={} vehicle={}",
          player.getGameProfile().getName(),
          target.id(),
          target.vehicle());
    FoundationSync.broadcast(player.serverLevel());
    PacketDistributor.sendToPlayer(player, target);
  }
}
