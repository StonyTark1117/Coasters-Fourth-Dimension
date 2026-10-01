package dev.fourthdimension.integration.engineered;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.jwaterfall.coastersengineered.*;
import dev.jwaterfall.coastersengineered.controller.*;
import dev.jwaterfall.coastersengineered.span.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.joml.Vector3d;

/** A separate native batch enables FE without changing other tests' motor configuration. */
public final class EngineeredEnergyCheck {
  private static Boolean previous;

  private EngineeredEnergyCheck() {}

  public static void configure(boolean begin) {
    if (begin) {
      previous = CoastersEngineeredConfig.get(CoastersEngineeredConfig.REQUIRE_ENERGY);
      CoastersEngineeredConfig.REQUIRE_ENERGY.set(true);
    } else if (previous != null) {
      CoastersEngineeredConfig.REQUIRE_ENERGY.set(previous);
      System.out.println("FD_ENGINEERED_ENERGY_RESTORED requireEnergy=" + previous);
      previous = null;
    }
  }

  private record Fixture(
      TrackDesign design,
      FoundationData.Rail rail,
      LinearMotorControllerBlockEntity motor,
      List<FoundationData.Vehicle> cars) {}

  public static void run(GameTestHelper helper) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    helper.assertTrue(
        TrackEnergy.required(), "Energy batch did not enable upstream FE requirement");
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000405"), "FDEngineeredEnergy"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var fixtures = new ArrayList<Fixture>();
    var riders = new ArrayList<Pig>();
    var held = new HashMap<UUID, Vector3d>();
    var filled = new HashMap<TrackDesign, Integer>();
    var anchors = new HashMap<TrackDesign, net.minecraft.world.level.block.entity.BlockEntity>();
    var savedMotors = new HashMap<TrackDesign, net.minecraft.nbt.CompoundTag>();
    var savedSpans = new HashMap<TrackDesign, TrackSpan>();
    for (var design :
        List.of(
            TrackDesign.ARROW, TrackDesign.LEGACY, TrackDesign.FREE_SPIN, TrackDesign.REVISED)) {
      var from = new BlockPos(-960 + design.ordinal() * 32, 170, -1000);
      var to = from.offset(0, 0, 60);
      force(level, from, to, true);
      for (var pos : List.of(from, to))
        level.setBlockAndUpdate(
            pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      var rail = TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
      var edge = TrackGeometry.resolve(level, rail);
      helper.assertTrue(
          TrackSpanBehaviour.at(level, from)
              .setSpan(
                  new TrackSpan(
                      List.of(
                          new SpanSegment(
                              edge.from(),
                              edge.to(),
                              edge.from().equals(from) ? 0 : 1,
                              edge.from().equals(from) ? 1 : 0)))),
          "Energy motor span rejected");
      var pos = from.offset(-12, 0, 20);
      level.setBlockAndUpdate(
          pos, CoastersEngineeredRegistry.LINEAR_MOTOR_CONTROLLER.get().defaultBlockState());
      var motor = (LinearMotorControllerBlockEntity) level.getBlockEntity(pos);
      motor.bindTo(from);
      motor.setSteps(List.of(new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.FORWARD, 12)));
      fixtures.add(new Fixture(design, rail, motor, new ArrayList<>()));
    }
    helper.runAtTickTime(
        20,
        () -> {
          for (var f : fixtures) {
            var energy = energy(helper, f);
            int stored = 12345 + f.design.ordinal();
            helper.assertTrue(
                energy.receiveEnergy(stored, false) == stored, "Initial FE input failed");
            anchors.put(f.design, level.getBlockEntity(f.rail.from));
            savedSpans.put(f.design, TrackSpanBehaviour.at(level, f.rail.from).span());
            savedMotors.put(f.design, f.motor.saveWithFullMetadata(level.registryAccess()));
            System.out.println(
                "FD_ENGINEERED_ENERGY_UNLOAD family=" + f.design.family + " stored=" + stored);
          }
          for (var f : fixtures) force(level, f.rail.from, f.rail.to, false);
        });
    helper.runAtTickTime(
        450,
        () -> {
          // getChunkNow does not request loading; null proves eviction, not just stopped ticking.
          for (var f : fixtures) {
            for (var pos : List.of(f.rail.from, f.rail.to, f.motor.getBlockPos()))
              helper.assertTrue(
                  level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null,
                  "Energy equipment chunk did not unload: " + pos);
            helper.assertTrue(
                anchors.get(f.design).isRemoved() && f.motor.isRemoved(),
                "Old energy block entities remained active after chunk eviction");
            System.out.println("FD_ENGINEERED_ENERGY_EVICTED family=" + f.design.family);
          }
          for (var f : fixtures) {
            force(level, f.rail.from, f.rail.to, true);
            level.getChunkAt(f.rail.from);
            level.getChunkAt(f.rail.to);
            level.getChunkAt(f.motor.getBlockPos());
          }
        });
    helper.runAtTickTime(
        490,
        () -> {
          for (int i = 0; i < fixtures.size(); i++) {
            var f = fixtures.get(i);
            var anchor = level.getBlockEntity(f.rail.from);
            var motor =
                (LinearMotorControllerBlockEntity) level.getBlockEntity(f.motor.getBlockPos());
            helper.assertTrue(
                anchor != anchors.get(f.design) && motor != f.motor,
                "Chunk reload reused removed equipment instances");
            helper.assertTrue(
                TrackSpanBehaviour.at(level, f.rail.from).span().equals(savedSpans.get(f.design)),
                "Reload lost native motor span");
            helper.assertTrue(
                motor
                    .saveWithFullMetadata(level.registryAccess())
                    .equals(savedMotors.get(f.design)),
                "Reload changed controller binding or sequence");
            helper.assertTrue(
                TrackGeometry.resolve(level, f.rail) != null, "Reload lost functional track");
            var restored = new Fixture(f.design, f.rail, motor, f.cars);
            fixtures.set(i, restored);
            var energy = energy(helper, restored);
            int expected = 12345 + f.design.ordinal();
            helper.assertTrue(
                energy.getEnergyStored() == expected, "Chunk reload changed FE buffer");
            helper.assertTrue(
                energy.extractEnergy(Integer.MAX_VALUE, true) == expected
                    && energy.getEnergyStored() == expected,
                "Simulated extraction changed restored FE");
            System.out.println(
                "FD_ENGINEERED_ENERGY_RELOADED family="
                    + f.design.family
                    + " stored="
                    + energy.getEnergyStored()
                    + " span=true controller=true fresh=true");
            helper.assertTrue(
                energy.extractEnergy(Integer.MAX_VALUE, false) == expected,
                "Restored capability did not supply saved FE");
          }
        });
    helper.runAtTickTime(
        520,
        () -> {
          for (var f : fixtures) {
            var edge = TrackGeometry.resolve(level, f.rail);
            for (double distance :
                f.design == TrackDesign.ARROW ? new double[] {24, 18} : new double[] {24}) {
              double t = TrackGeometry.table(level, edge).parameterAt(distance);
              f.cars.add(
                  VehicleFactory.create(
                      level,
                      f.design,
                      new CoasterPathTrackFrame.GraphHit(
                          CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
            }
          }
        });
    helper.runAtTickTime(
        590,
        () -> {
          for (var f : fixtures) {
            if (f.cars.size() == 2)
              helper.assertTrue(
                  CoasterCartTrainLinkConstraint.linkCarts(
                      level, body(level, f.cars.get(0)), body(level, f.cars.get(1)), 6),
                  "Energy train link failed");
            var energy = energy(helper, f);
            helper.assertTrue(
                energy.getEnergyStored() == 0 && energy.canReceive() && energy.canExtract(),
                "Anchor capability was not an empty bidirectional FE buffer");
            int amount = energy.receiveEnergy(Integer.MAX_VALUE, true);
            helper.assertTrue(
                amount == energy.getMaxEnergyStored() && energy.getEnergyStored() == 0,
                "Simulated FE input changed the buffer");
            filled.put(f.design, energy.receiveEnergy(Integer.MAX_VALUE, false));
            for (var v : f.cars) {
              helper.assertTrue(Boarding.ready(level, v), "Energy car not boarding ready");
              held.put(v.chassis, center(level, v));
              var pig = EntityType.PIG.create(level);
              pig.setNoAi(true);
              pig.setPos(f.rail.from.getX(), 173, f.rail.from.getZ());
              level.addFreshEntity(pig);
              var c = v.carriers.getLast();
              SeatBlock.sitDown(
                  level,
                  container
                      .getSubLevel(c.body)
                      .getPlot()
                      .getCenterBlock()
                      .offset(c.seats.getLast()),
                  pig);
              helper.assertTrue(pig.isPassenger(), "Energy test rider did not board");
              riders.add(pig);
            }
          }
        });
    helper.runAtTickTime(
        630,
        () -> {
          for (var f : fixtures) {
            var energy = energy(helper, f);
            helper.assertTrue(
                energy.getEnergyStored() == filled.get(f.design), "Held car consumed FE");
            for (var v : f.cars)
              helper.assertTrue(
                  center(level, v).distance(held.get(v.chassis)) < .15,
                  "Charged motor pulled against captured carriers");
            System.out.println(
                "FD_ENGINEERED_ENERGY_HELD family="
                    + f.design.family
                    + " cars="
                    + f.cars.size()
                    + " stored="
                    + energy.getEnergyStored());
            VehicleOperations.restraints(level, f.cars.getFirst(), true);
            VehicleOperations.dispatch(level, f.cars.getFirst());
            energy.extractEnergy(Integer.MAX_VALUE, false);
          }
        });
    helper.runAtTickTime(
        670,
        () -> {
          for (var f : fixtures) {
            helper.assertTrue(f.motor.isDriving(), "Energy-starved cart lost native presence");
            helper.assertTrue(
                f.motor
                    .saveWithFullMetadata(level.registryAccess())
                    .getBoolean(CoastersEngineered.storageKey("Starving")),
                "Empty buffer did not report native controller starvation");
            for (var v : f.cars)
              helper.assertTrue(
                  center(level, v).distance(held.get(v.chassis)) < .2 && speed(level, v) < .1,
                  "Empty FE buffer still launched " + f.design);
            helper.assertTrue(
                energy(helper, f).getEnergyStored() == 0, "Unpowered launch generated FE");
            System.out.println(
                "FD_ENGINEERED_ENERGY_STARVED family="
                    + f.design.family
                    + " cars="
                    + f.cars.size());
            energy(helper, f).receiveEnergy(Integer.MAX_VALUE, false);
          }
        });
    helper.runAtTickTime(
        750,
        () -> {
          for (var f : fixtures) {
            var energy = energy(helper, f);
            for (var v : f.cars)
              helper.assertTrue(
                  speed(level, v) > 1 && center(level, v).distance(held.get(v.chassis)) > 2,
                  "Charged FE launch did not move " + f.design);
            helper.assertTrue(
                energy.getEnergyStored() < filled.get(f.design),
                "Native launch did not consume FE");
            report(helper, f, "FORWARD");
            energy.extractEnergy(Integer.MAX_VALUE, false);
            f.motor.setSteps(
                List.of(new MotorStep(MotorStep.Kind.TRIM, MotorStep.Heading.FORWARD, 1)));
          }
        });
    helper.runAtTickTime(
        790,
        () -> {
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(Math.abs(speed(level, v)) < 1.3, "Empty-buffer trim did not brake");
            helper.assertTrue(
                energy(helper, f).getEnergyStored() > 0, "Braking did not regenerate FE");
            report(helper, f, "BRAKE");
            f.motor.setSteps(
                List.of(new MotorStep(MotorStep.Kind.STOP, MotorStep.Heading.FORWARD, 0)));
          }
        });
    helper.runAtTickTime(
        840,
        () -> {
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(
                  Math.abs(speed(level, v)) < .1, "Motor STOP did not settle before reverse");
            energy(helper, f).receiveEnergy(Integer.MAX_VALUE, false);
            f.motor.setSteps(
                List.of(new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.BACKWARD, 8)));
          }
        });
    helper.runAtTickTime(
        900,
        () -> {
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(
                  speed(level, v) < -1, "FE reverse launch did not reverse " + f.design);
            helper.assertTrue(
                energy(helper, f).getEnergyStored() < filled.get(f.design),
                "Reverse launch did not consume FE");
            report(helper, f, "REVERSE");
            energy(helper, f).extractEnergy(Integer.MAX_VALUE, false);
            f.motor.setSteps(
                List.of(new MotorStep(MotorStep.Kind.TRIM, MotorStep.Heading.FORWARD, 1)));
          }
        });
    helper.runAtTickTime(
        940,
        () -> {
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(Math.abs(speed(level, v)) < 1.3, "Reverse trim did not brake");
            helper.assertTrue(
                energy(helper, f).getEnergyStored() > 0, "Reverse brake did not regenerate FE");
            report(helper, f, "REVERSE_BRAKE");
            VehicleOperations.capture(level, f.cars.getFirst());
          }
        });
    helper.runAtTickTime(
        1070,
        () -> {
          for (var f : fixtures) {
            helper.assertTrue(
                VehicleOperations.consist(level, f.cars.getFirst()).size() == f.cars.size(),
                "Energy run lost a coupling");
            for (var v : f.cars)
              helper.assertTrue(
                  v.fault.isBlank() && v.restraints && Boarding.ready(level, v),
                  "Energy car failed recapture");
            VehicleOperations.restraints(level, f.cars.getFirst(), false);
          }
          for (var rider : riders) {
            helper.assertTrue(rider.isPassenger(), "Energy run lost passenger");
            rider.stopRiding();
            rider.discard();
          }
          for (var f : fixtures)
            for (var v : f.cars)
              container.removeSubLevel(body(level, v), SubLevelRemovalReason.REMOVED);
          System.out.println("FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5");
          helper.succeed();
        });
  }

  private static void force(ServerLevel level, BlockPos from, BlockPos to, boolean forced) {
    for (int x = (from.getX() - 16) >> 4; x <= (from.getX() + 16) >> 4; x++)
      for (int z = (from.getZ() - 16) >> 4; z <= (to.getZ() + 16) >> 4; z++)
        level.setChunkForced(x, z, forced);
  }

  private static IEnergyStorage energy(GameTestHelper h, Fixture f) {
    var e =
        h.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, f.rail.from, Direction.DOWN);
    h.assertTrue(e != null, "Engineered anchor did not expose the native FE capability");
    return e;
  }

  private static ServerSubLevel body(ServerLevel level, FoundationData.Vehicle v) {
    return (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
  }

  private static Vector3d center(ServerLevel level, FoundationData.Vehicle v) {
    return new Vector3d(CoasterCartResolve.cartCenterWorld(body(level, v)));
  }

  private static double speed(ServerLevel level, FoundationData.Vehicle v) {
    return SubLevelContainer.getContainer(level)
        .physicsSystem()
        .getPipeline()
        .getLinearVelocity(body(level, v), new Vector3d())
        .z;
  }

  private static void report(GameTestHelper h, Fixture f, String phase) {
    System.out.println(
        "FD_ENGINEERED_ENERGY_"
            + phase
            + " family="
            + f.design.family
            + " stored="
            + energy(h, f).getEnergyStored()
            + " speeds="
            + f.cars.stream().map(v -> speed(h.getLevel(), v)).toList());
  }
}
