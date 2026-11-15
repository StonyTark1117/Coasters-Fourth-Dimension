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
    var registrar = event.registrar("8");
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
    if (request.action < 10) {
      var rail = ProfileEditor.requireEditable(player, request.id, request.revision);
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
          if (rail.family != dev.fourthdimension.contract.Family.SPINNING && rail.family != dev.fourthdimension.contract.Family.SUSPENDED)
            throw new IllegalArgumentException("Lift lock requires Spinning or Suspended track");
          rail.liftLock = !rail.liftLock;
          rail.revision++;
          FoundationData.get(player.serverLevel()).setDirty();
        }
        default -> throw new IllegalArgumentException("Unknown profile operation");
      }
    } else {
      var vehicle = VehicleOperations.accessible(player, request.id);
      if (vehicle.revision != request.revision)
        throw new IllegalArgumentException("Vehicle settings changed; review the refreshed state and try again");
      var level = player.serverLevel();
      switch (request.action) {
        case 10 -> VehicleOperations.capture(level, vehicle);
        case 11 -> VehicleOperations.restraints(level, vehicle, true);
        case 12 -> VehicleOperations.restraints(level, vehicle, false);
        case 13 -> VehicleOperations.dispatch(level, vehicle);
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
          if(dev.fourthdimension.vehicle.SuspendedVehicle.is(vehicle)) {
            dev.fourthdimension.vehicle.SuspendedRuntime.resumeToDock(level,vehicle);break;
          }
          if (!dev.fourthdimension.vehicle.SpinningVehicle.is(vehicle))
            throw new IllegalArgumentException("Resume to dock requires S1");
          dev.fourthdimension.vehicle.SpinningRuntime.resumeToDock(level, vehicle);
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
