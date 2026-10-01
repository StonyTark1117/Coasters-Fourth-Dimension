package dev.fourthdimension.integration.engineered;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.jwaterfall.coastersengineered.*;
import dev.jwaterfall.coastersengineered.controller.*;
import dev.jwaterfall.coastersengineered.span.*;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Vector3d;

/**
 * Opt-in dedicated-server evidence fixture; reconstructs all references after a process restart.
 */
public final class EngineeredRestartFixture {
  private static final List<TrackDesign> DESIGNS =
      List.of(TrackDesign.ARROW, TrackDesign.LEGACY, TrackDesign.FREE_SPIN, TrackDesign.REVISED);

  private EngineeredRestartFixture() {}

  private record Fixture(
      TrackDesign design,
      FoundationData.Rail rail,
      LinearMotorControllerBlockEntity motor,
      List<FoundationData.Vehicle> cars) {}

  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var root = Commands.literal("energy_restart");
    for (String action :
        List.of("create", "cars", "board", "charge", "dispatch", "brake", "capture", "unload"))
      root.then(
          Commands.literal(action)
              .executes(
                  ctx -> {
                    try {
                      act(ctx.getSource().getLevel(), action);
                      ctx.getSource()
                          .sendSuccess(
                              () -> Component.literal("Energy restart fixture: " + action), false);
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
                                  () -> Component.literal("Energy restart snapshot recorded"),
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

  private static BlockPos from(TrackDesign design) {
    return new BlockPos(-800 + design.ordinal() * 40, 170, -800);
  }

  private static List<Fixture> fixtures(ServerLevel level) {
    var data = FoundationData.get(level);
    return DESIGNS.stream()
        .map(
            design -> {
              var from = from(design);
              var rail =
                  data.rails.stream()
                      .filter(r -> r.from.equals(from) && r.to.equals(from.offset(0, 0, 60)))
                      .findFirst()
                      .orElseThrow(
                          () -> new IllegalArgumentException("Create the restart fixture first"));
              if (!(level.getBlockEntity(from.offset(-12, 0, 20))
                  instanceof LinearMotorControllerBlockEntity motor))
                throw new IllegalArgumentException("Restart motor unavailable");
              var cars =
                  data.vehicles.values().stream()
                      .filter(v -> rail.matches(v.boundFrom, v.boundTo))
                      .sorted(Comparator.comparing(v -> v.chassis))
                      .toList();
              return new Fixture(design, rail, motor, cars);
            })
        .toList();
  }

  private static IEnergyStorage energy(ServerLevel level, Fixture f) {
    var storage =
        level.getCapability(Capabilities.EnergyStorage.BLOCK, f.rail.from, Direction.DOWN);
    if (storage == null)
      throw new IllegalArgumentException("Enable Engineered requireEnergy before this fixture");
    return storage;
  }

  private static ServerSubLevel body(ServerLevel level, FoundationData.Vehicle v) {
    if (!(SubLevelContainer.getContainer(level).getSubLevel(v.chassis)
        instanceof ServerSubLevel body))
      throw new IllegalArgumentException("Restart chassis unavailable");
    return body;
  }

  private static void act(ServerLevel level, String action) {
    if (!TrackEnergy.required())
      throw new IllegalArgumentException("Enable Engineered requireEnergy before this fixture");
    if (action.equals("create")) {
      if (!FoundationData.get(level).rails.isEmpty()
          || !FoundationData.get(level).vehicles.isEmpty())
        throw new IllegalArgumentException("Use a fresh validation world");
      var player =
          FakePlayerFactory.get(
              level,
              new com.mojang.authlib.GameProfile(
                  UUID.fromString("fdfdfdfd-0000-0000-0000-000000000407"), "FDEnergyRestart"));
      player.setGameMode(GameType.CREATIVE);
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      for (var design : DESIGNS) {
        var from = from(design);
        var to = from.offset(0, 0, 60);
        for (int x = (from.getX() - 16) >> 4; x <= (from.getX() + 16) >> 4; x++)
          for (int z = (from.getZ() - 16) >> 4; z <= (to.getZ() + 16) >> 4; z++)
            level.setChunkForced(x, z, true);
        for (var pos : List.of(from, to))
          level.setBlockAndUpdate(
              pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        player.setPos(to.getCenter().add(2, 1, 0));
        var rail =
            TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
        var edge = TrackGeometry.resolve(level, rail);
        if (!TrackSpanBehaviour.at(level, from)
            .setSpan(
                new TrackSpan(
                    List.of(
                        new SpanSegment(
                            edge.from(),
                            edge.to(),
                            edge.from().equals(from) ? 0 : 1,
                            edge.from().equals(from) ? 1 : 0)))))
          throw new IllegalArgumentException("Restart motor span rejected");
        var pos = from.offset(-12, 0, 20);
        level.setBlockAndUpdate(
            pos, CoastersEngineeredRegistry.LINEAR_MOTOR_CONTROLLER.get().defaultBlockState());
        var motor = (LinearMotorControllerBlockEntity) level.getBlockEntity(pos);
        motor.bindTo(from);
        motor.setSteps(
            List.of(new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.FORWARD, 12)));
      }
      return;
    }
    var fixtures = fixtures(level);
    if (action.equals("cars")) {
      if (fixtures.stream().anyMatch(f -> !f.cars.isEmpty()))
        throw new IllegalArgumentException("Cars already exist");
      for (var f : fixtures) {
        var edge = TrackGeometry.resolve(level, f.rail);
        for (double distance :
            f.design == TrackDesign.ARROW ? new double[] {24, 18} : new double[] {24}) {
          double t = TrackGeometry.table(level, edge).parameterAt(distance);
          VehicleFactory.create(
              level,
              f.design,
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t));
        }
      }
      return;
    }
    for (var f : fixtures) {
      if (f.cars.size() != (f.design == TrackDesign.ARROW ? 2 : 1))
        throw new IllegalArgumentException("Expected all five restart cars");
      switch (action) {
        case "board" -> {
          if (f.cars.stream()
              .anyMatch(
                  v ->
                      !Boarding.ready(level, v)
                          || v.carriers.stream().anyMatch(c -> !c.occupants.isEmpty())))
            throw new IllegalArgumentException("Restart cars must be aligned and empty");
          if (f.cars.size() == 2
              && !CoasterCartTrainLinkConstraint.linkCarts(
                  level, body(level, f.cars.get(0)), body(level, f.cars.get(1)), 6))
            throw new IllegalArgumentException("Restart coupling failed");
          for (var v : f.cars) {
            var pig = EntityType.PIG.create(level);
            pig.setNoAi(true);
            pig.setPersistenceRequired();
            pig.addTag("fd_energy_restart");
            pig.setPos(f.rail.from.getCenter().add(0, 3, 24));
            level.addFreshEntity(pig);
            var c = v.carriers.getLast();
            var child = SubLevelContainer.getContainer(level).getSubLevel(c.body);
            SeatBlock.sitDown(
                level, child.getPlot().getCenterBlock().offset(c.seats.getLast()), pig);
            if (!pig.isPassenger())
              throw new IllegalArgumentException("Restart passenger did not board");
          }
          VehicleOperations.restraints(level, f.cars.getFirst(), true);
        }
        case "charge" -> {
          var energy = energy(level, f);
          energy.extractEnergy(Integer.MAX_VALUE, false);
          int charge = 12345 + f.design.ordinal();
          if (energy.receiveEnergy(charge, false) != charge)
            throw new IllegalArgumentException("Restart charge rejected");
        }
        case "dispatch" -> VehicleOperations.dispatch(level, f.cars.getFirst());
        case "brake" -> {
          energy(level, f).extractEnergy(Integer.MAX_VALUE, false);
          f.motor.setSteps(
              List.of(new MotorStep(MotorStep.Kind.TRIM, MotorStep.Heading.FORWARD, 1)));
        }
        case "capture" -> VehicleOperations.capture(level, f.cars.getFirst());
        case "unload" -> {
          var riders =
              f.cars.stream()
                  .flatMap(v -> v.carriers.stream())
                  .flatMap(c -> c.occupants.stream())
                  .toList();
          VehicleOperations.restraints(level, f.cars.getFirst(), false);
          for (var id : riders) {
            var rider = level.getEntity(id);
            if (rider == null || !rider.getTags().contains("fd_energy_restart"))
              throw new IllegalArgumentException("Restart passenger unavailable");
            rider.stopRiding();
          }
        }
        default -> throw new IllegalArgumentException("Unknown restart action");
      }
    }
  }

  private static void report(ServerLevel level, String label) {
    var container = SubLevelContainer.getContainer(level);
    for (var f : fixtures(level)) {
      var json = new JsonObject();
      json.addProperty("label", label);
      json.addProperty("family", f.design.family.name());
      json.addProperty("tick", level.getGameTime());
      json.addProperty("required", TrackEnergy.required());
      json.addProperty("energy", energy(level, f).getEnergyStored());
      json.addProperty(
          "anchor",
          level
              .getBlockEntity(f.rail.from)
              .saveWithFullMetadata(level.registryAccess())
              .toString());
      json.addProperty("motor", f.motor.saveWithFullMetadata(level.registryAccess()).toString());
      json.addProperty("span", TrackSpanBehaviour.at(level, f.rail.from).span().save().toString());
      var cars = new JsonArray();
      for (var v : f.cars) {
        var car = new JsonObject();
        car.addProperty("chassis", v.chassis.toString());
        car.addProperty("ready", Boarding.ready(level, v));
        car.addProperty("restraints", v.restraints);
        car.addProperty("fault", v.fault);
        car.addProperty("consist", VehicleOperations.consist(level, v).size());
        var center = CoasterCartResolve.cartCenterWorld(body(level, v));
        car.addProperty("distance", center.z() - f.rail.from.getZ());
        car.addProperty(
            "speed",
            container
                .physicsSystem()
                .getPipeline()
                .getLinearVelocity(body(level, v), new Vector3d())
                .z);
        var carriers = new JsonArray();
        for (var c : v.carriers) {
          var carrier = new JsonObject();
          carrier.addProperty("body", c.body.toString());
          carrier.addProperty("loaded", container.getSubLevel(c.body) != null);
          carrier.addProperty("capture", c.capture);
          var riders = new JsonArray();
          for (var id : c.occupants) {
            var rider = new JsonObject();
            rider.addProperty("id", id.toString());
            var entity = level.getEntity(id);
            rider.addProperty("alive", entity != null && entity.isAlive());
            rider.addProperty("mounted", entity != null && entity.isPassenger());
            var parent =
                entity == null || entity.getVehicle() == null
                    ? null
                    : Sable.HELPER.getContaining(entity.getVehicle());
            rider.addProperty("carrier", parent == null ? "" : parent.getUniqueId().toString());
            riders.add(rider);
          }
          carrier.add("riders", riders);
          carriers.add(carrier);
        }
        car.add("carriers", carriers);
        cars.add(car);
      }
      json.add("cars", cars);
      LogUtils.getLogger().info("FD_ENERGY_RESTART {}", json);
    }
  }
}
