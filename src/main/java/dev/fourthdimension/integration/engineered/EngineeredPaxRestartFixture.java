package dev.fourthdimension.integration.engineered;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.vehicle.*;
import dev.jwaterfall.coastersengineered.*;
import dev.jwaterfall.coastersengineered.controller.*;
import dev.jwaterfall.coastersengineered.observer.CoasterObserverBlockEntity;
import dev.jwaterfall.coastersengineered.span.*;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Vector3d;

/** Opt-in packaged-server fixture; no live object references survive between commands. */
public final class EngineeredPaxRestartFixture {
  private static final BlockPos ORIGIN = new BlockPos(-600, 180, 600);
  private static final BlockPos WINCH = ORIGIN.offset(-6, 0, 8);
  private static final BlockPos MOTOR = WINCH.offset(-6, 0, 4);

  private EngineeredPaxRestartFixture() {}

  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var root = Commands.literal("pax_energy_restart");
    for (var action :
        List.of(
            "create",
            "assemble",
            "equip",
            "board",
            "charge",
            "dispatch",
            "resume",
            "recover",
            "unload",
            "reverse",
            "forward",
            "stop_motor",
            "trim"))
      root.then(
          Commands.literal(action)
              .executes(
                  ctx -> {
                    try {
                      act(ctx.getSource().getLevel(), action);
                      ctx.getSource()
                          .sendSuccess(
                              () -> Component.literal("PAX energy restart fixture: " + action),
                              false);
                      return 1;
                    } catch (IllegalArgumentException ex) {
                      ctx.getSource().sendFailure(Component.literal(ex.getMessage()));
                      return 0;
                    }
                  }));
    root.then(
        Commands.literal("report")
            .then(
                Commands.argument("label", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .executes(
                        ctx -> {
                          report(
                              ctx.getSource().getLevel(),
                              com.mojang.brigadier.arguments.StringArgumentType.getString(
                                  ctx, "label"));
                          ctx.getSource()
                              .sendSuccess(
                                  () -> Component.literal("PAX energy restart snapshot recorded"),
                                  false);
                          return 1;
                        })));
    event
        .getDispatcher()
        .register(
            Commands.literal("fd")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("validation").then(root)));
  }

  private static ShuttleWinchEntity winch(ServerLevel level) {
    if (!(level.getBlockEntity(WINCH) instanceof ShuttleWinchEntity winch))
      throw new IllegalArgumentException("Create the PAX restart fixture first");
    return winch;
  }

  private static List<FoundationData.Vehicle> cars(ServerLevel level) {
    var data = FoundationData.get(level);
    var winch = winch(level);
    var cars =
        winch.cars.isEmpty()
            ? data.vehicles.values().stream()
                .sorted(
                    Comparator.comparingDouble(
                        v ->
                            CoasterCartResolve.cartCenterWorld(
                                    SubLevelContainer.getContainer(level).getSubLevel(v.chassis))
                                .z()))
                .toList()
            : winch.cars.stream().map(data.vehicles::get).toList();
    if (cars.size() != 4
        || cars.stream()
            .anyMatch(v -> v == null || v.family != dev.fourthdimension.contract.Family.PAX))
      throw new IllegalArgumentException("Expected four native PAX restart cars");
    return cars;
  }

  private static LinearMotorControllerBlockEntity motor(ServerLevel level) {
    if (!(level.getBlockEntity(MOTOR) instanceof LinearMotorControllerBlockEntity motor))
      throw new IllegalArgumentException("Equip the PAX restart fixture first");
    return motor;
  }

  private static IEnergyStorage energy(ServerLevel level) {
    var energy =
        level.getCapability(
            Capabilities.EnergyStorage.BLOCK, winch(level).routeStart, Direction.DOWN);
    if (energy == null) throw new IllegalArgumentException("Native PAX FE capability unavailable");
    return energy;
  }

  private static void act(ServerLevel level, String action) {
    if (!TrackEnergy.required())
      throw new IllegalArgumentException("Enable Engineered requireEnergy before this fixture");
    if (action.equals("create")) {
      var data = FoundationData.get(level);
      if (!data.rails.isEmpty() || !data.vehicles.isEmpty())
        throw new IllegalArgumentException("Use a fresh validation world");
      for (int x = (ORIGIN.getX() - 24) >> 4; x <= (ORIGIN.getX() + 16) >> 4; x++)
        for (int z = 33; z <= 43; z++) level.setChunkForced(x, z, true);
      ShuttleExamples.create(level, ORIGIN, false);
      return;
    }
    var winch = winch(level);
    var cars = cars(level);
    var container = SubLevelContainer.getContainer(level);
    switch (action) {
      case "assemble" -> ShuttleExamples.assemble(level, new ShuttleExamples.Example(cars, winch));
      case "equip" -> {
        if (level.getBlockEntity(MOTOR) != null)
          throw new IllegalArgumentException("Restart equipment already exists");
        var route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
        var span = TrackSpanBehaviour.at(level, route.start);
        if (span == null
            || !span.setSpan(
                new TrackSpan(
                    route.segments.stream()
                        .map(
                            s ->
                                new SpanSegment(
                                    s.edge().from(),
                                    s.edge().to(),
                                    s.reversed() ? 1 : 0,
                                    s.reversed() ? 0 : 1))
                        .toList())))
          throw new IllegalArgumentException("Native PAX restart span rejected");
        level.setBlockAndUpdate(
            MOTOR, CoastersEngineeredRegistry.LINEAR_MOTOR_CONTROLLER.get().defaultBlockState());
        motor(level).bindTo(route.start);
        motor(level)
            .setSteps(
                List.of(new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.BACKWARD, 100)));
        level.setBlockAndUpdate(
            MOTOR.above(2), CoastersEngineeredRegistry.COASTER_OBSERVER.get().defaultBlockState());
        ((CoasterObserverBlockEntity) level.getBlockEntity(MOTOR.above(2)))
            .watch(SpanSegment.point(route.segments.get(1).edge(), .5));
      }
      case "board" -> {
        if (cars.stream()
            .anyMatch(
                v ->
                    !Boarding.ready(level, v)
                        || v.restraints
                        || v.carriers.stream().anyMatch(c -> !c.occupants.isEmpty())))
          throw new IllegalArgumentException("PAX restart cars must be aligned, empty and open");
        for (var v : cars)
          for (var c : v.carriers)
            for (var offset : c.seats) {
              var pig = EntityType.PIG.create(level);
              pig.setNoAi(true);
              pig.setPersistenceRequired();
              pig.addTag("fd_pax_energy_restart");
              pig.setPos(ORIGIN.getCenter().add(0, 3, 0));
              level.addFreshEntity(pig);
              SeatBlock.sitDown(
                  level,
                  container.getSubLevel(c.body).getPlot().getCenterBlock().offset(offset),
                  pig);
              if (!pig.isPassenger())
                throw new IllegalArgumentException("PAX restart passenger did not board");
            }
        VehicleOperations.restraints(level, cars.getFirst(), true);
      }
      case "charge" -> {
        var energy = energy(level);
        energy.extractEnergy(Integer.MAX_VALUE, false);
        if (energy.receiveEnergy(12345, false) != 12345)
          throw new IllegalArgumentException("PAX restart charge rejected");
      }
      case "dispatch" -> VehicleOperations.dispatch(level, cars.getFirst());
      case "resume" -> winch.resume();
      case "recover" -> winch.recover();
      case "reverse", "forward", "stop_motor", "trim" ->
          motor(level)
              .setSteps(
                  List.of(
                      new MotorStep(
                          action.equals("stop_motor")
                              ? MotorStep.Kind.STOP
                              : action.equals("trim") ? MotorStep.Kind.TRIM : MotorStep.Kind.LAUNCH,
                          action.equals("reverse")
                              ? MotorStep.Heading.BACKWARD
                              : action.equals("trim")
                                  ? MotorStep.Heading.ROLLING
                                  : MotorStep.Heading.FORWARD,
                          action.equals("stop_motor") ? 0 : action.equals("trim") ? 1 : 100)));
      case "unload" -> {
        var ids =
            cars.stream()
                .flatMap(v -> v.carriers.stream())
                .flatMap(c -> c.occupants.stream())
                .toList();
        VehicleOperations.restraints(level, cars.getFirst(), false);
        for (var id : ids) {
          var rider = level.getEntity(id);
          if (rider == null || !rider.getTags().contains("fd_pax_energy_restart"))
            throw new IllegalArgumentException("PAX restart passenger unavailable");
          rider.stopRiding();
        }
      }
      default -> throw new IllegalArgumentException("Unknown PAX restart action");
    }
  }

  private static JsonObject fields(CompoundTag tag, String... fields) {
    var json = new JsonObject();
    for (var field : fields) {
      var value = tag.get(CoastersEngineered.storageKey(field));
      if (value == null) throw new IllegalArgumentException("Missing equipment field: " + field);
      json.addProperty(field, value.toString());
    }
    return json;
  }

  private static void report(ServerLevel level, String label) {
    var winch = winch(level);
    var container = SubLevelContainer.getContainer(level);
    var json = new JsonObject();
    json.addProperty("label", label);
    json.addProperty("tick", level.getGameTime());
    json.addProperty("required", TrackEnergy.required());
    json.addProperty("energy", energy(level).getEnergyStored());
    json.addProperty("phase", winch.phase.name());
    json.addProperty("interrupted", winch.interrupted.name());
    json.addProperty("fault", winch.fault);
    json.addProperty("cycles", winch.cycles);
    json.addProperty("carriage", Objects.toString(winch.carriage, ""));
    var carriage = winch.carriage == null ? null : container.getSubLevel(winch.carriage);
    json.addProperty("carriageLoaded", carriage != null);
    if (carriage != null) {
      var position = carriage.logicalPose().position();
      json.add(
          "carriagePosition",
          new Gson().toJsonTree(new double[] {position.x(), position.y(), position.z()}));
    }
    json.addProperty("winch", winch.saveWithFullMetadata(level.registryAccess()).toString());
    json.addProperty(
        "span", TrackSpanBehaviour.at(level, winch.routeStart).span().save().toString());
    json.add(
        "controller",
        fields(
            motor(level).saveWithFullMetadata(level.registryAccess()),
            "Anchor",
            "Sequence",
            "Step"));
    var observer = (CoasterObserverBlockEntity) level.getBlockEntity(MOTOR.above(2));
    json.add("observer", fields(observer.saveWithFullMetadata(level.registryAccess()), "Watching"));
    json.addProperty("observerPower", observer.getPower());
    json.addProperty("motorDriving", motor(level).isDriving());
    json.addProperty("externalDriveAllowed", ExternalDrivePolicy.allows(level, winch.vehicle));
    json.add("telemetry", new Gson().toJsonTree(RideTelemetry.sample(level, winch.vehicle)));
    var vehicleRows = new JsonArray();
    for (var v : cars(level)) {
      var car = new JsonObject();
      car.addProperty("chassis", v.chassis.toString());
      car.addProperty("ready", Boarding.ready(level, v));
      car.addProperty("restraints", v.restraints);
      car.addProperty("stopping", v.stopping);
      car.addProperty("fault", v.fault);
      car.addProperty("consist", VehicleOperations.consist(level, v).size());
      var body = (dev.ryanhcode.sable.sublevel.ServerSubLevel) container.getSubLevel(v.chassis);
      var pos = CoasterCartResolve.cartCenterWorld(body);
      car.add("position", new Gson().toJsonTree(new double[] {pos.x(), pos.y(), pos.z()}));
      car.addProperty(
          "speed",
          container.physicsSystem().getPipeline().getLinearVelocity(body, new Vector3d()).length());
      var carriers = new JsonArray();
      for (var c : v.carriers) {
        var carrier = new JsonObject();
        carrier.addProperty("body", c.body.toString());
        carrier.addProperty("loaded", container.getSubLevel(c.body) != null);
        carrier.addProperty("capture", c.capture);
        var riders = new JsonArray();
        for (var id : c.occupants) {
          var rider = new JsonObject();
          var entity = level.getEntity(id);
          var parent =
              entity == null || entity.getVehicle() == null
                  ? null
                  : Sable.HELPER.getContaining(entity.getVehicle());
          rider.addProperty("id", id.toString());
          rider.addProperty("alive", entity != null && entity.isAlive());
          rider.addProperty("mounted", entity != null && entity.isPassenger());
          rider.addProperty("carrier", parent == null ? "" : parent.getUniqueId().toString());
          riders.add(rider);
        }
        carrier.add("riders", riders);
        carriers.add(carrier);
      }
      car.add("carriers", carriers);
      vehicleRows.add(car);
    }
    json.add("cars", vehicleRows);
    LogUtils.getLogger().info("FD_PAX_ENERGY_RESTART {}", json);
  }
}
