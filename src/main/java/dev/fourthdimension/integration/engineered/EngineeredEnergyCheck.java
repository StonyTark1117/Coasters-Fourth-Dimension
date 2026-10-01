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
  private static Double previousRegen;
  private static Integer previousCapacity;

  private EngineeredEnergyCheck() {}

  public static void configure(boolean begin) {
    if (begin) {
      previous = CoastersEngineeredConfig.get(CoastersEngineeredConfig.REQUIRE_ENERGY);
      previousRegen = CoastersEngineeredConfig.get(CoastersEngineeredConfig.REGEN_EFFICIENCY);
      previousCapacity = CoastersEngineeredConfig.get(CoastersEngineeredConfig.ENERGY_BUFFER);
      CoastersEngineeredConfig.REQUIRE_ENERGY.set(true);
    } else if (previous != null) {
      CoastersEngineeredConfig.REGEN_EFFICIENCY.set(previousRegen);
      CoastersEngineeredConfig.ENERGY_BUFFER.set(previousCapacity);
      System.out.println(
          "FD_ENGINEERED_ENERGY_TUNING_RESTORED buffer="
              + previousCapacity
              + " regen="
              + previousRegen);
      CoastersEngineeredConfig.REQUIRE_ENERGY.set(previous);
      System.out.println("FD_ENGINEERED_ENERGY_RESTORED requireEnergy=" + previous);
      previous = null;
      previousRegen = null;
      previousCapacity = null;
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
    var pulsePositions = new HashMap<UUID, Vector3d>();
    var pulseSpeeds = new HashMap<UUID, Double>();
    var coastSpeeds = new HashMap<UUID, Double>();
    var trickleSpeeds = new HashMap<UUID, Double>();
    var trickleReceived = new HashMap<TrackDesign, Integer>();
    int[] trickleTicks = {0};
    long startTick = level.getGameTime();
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
          checkIntact(helper, fixtures, riders);
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(Boarding.ready(level, v), "Energy car failed initial recapture");
            f.motor.setSteps(
                List.of(new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.FORWARD, 12)));
            energy(helper, f).extractEnergy(Integer.MAX_VALUE, false);
            VehicleOperations.dispatch(level, f.cars.getFirst());
          }
        });
    helper.runAtTickTime(
        1110,
        () -> {
          for (var f : fixtures) {
            for (var v : f.cars) {
              helper.assertTrue(
                  Math.abs(speed(level, v)) < .1, "Empty buffer moved recaptured car");
              pulsePositions.put(v.chassis, center(level, v));
            }
            helper.assertTrue(
                energy(helper, f).receiveEnergy(100, false) == 100, "Pulse input rejected");
          }
        });
    helper.runAtTickTime(
        1150,
        () -> {
          checkIntact(helper, fixtures, riders);
          for (var f : fixtures) {
            checkStarved(helper, f);
            for (var v : f.cars) {
              double velocity = speed(level, v);
              double distance = center(level, v).distance(pulsePositions.get(v.chassis));
              helper.assertTrue(
                  velocity > .02 && velocity < 2 && distance > .05 && distance < 3,
                  "Limited charge did not produce a bounded launch: " + f.design);
              pulseSpeeds.put(v.chassis, velocity);
            }
            report(helper, f, "PULSE");
          }
        });
    helper.runAtTickTime(
        1190,
        () -> {
          for (var f : fixtures) {
            checkStarved(helper, f);
            for (var v : f.cars) {
              double velocity = speed(level, v);
              helper.assertTrue(
                  Double.isFinite(velocity)
                      && Math.abs(velocity) <= pulseSpeeds.get(v.chassis) + .25,
                  "Empty buffer continued accelerating after the pulse");
              coastSpeeds.put(v.chassis, velocity);
            }
            report(helper, f, "COAST");
          }
        });
    helper.onEachTick(
        () -> {
          long tick = level.getGameTime() - startTick;
          if (tick < 1191 || tick >= 1231) return;
          trickleTicks[0]++;
          for (var f : fixtures) {
            int accepted = energy(helper, f).receiveEnergy(5, false);
            helper.assertTrue(accepted == 5, "Trickle input rejected");
            trickleReceived.merge(f.design, accepted, Integer::sum);
          }
        });
    helper.runAtTickTime(
        1240,
        () -> {
          checkIntact(helper, fixtures, riders);
          helper.assertTrue(trickleTicks[0] == 40, "Trickle duration changed");
          for (var f : fixtures) {
            checkStarved(helper, f);
            helper.assertTrue(trickleReceived.get(f.design) == 200, "Trickle charge total changed");
            for (var v : f.cars) {
              double velocity = speed(level, v);
              helper.assertTrue(
                  velocity > coastSpeeds.get(v.chassis) + .1 && velocity < 3,
                  "Trickle input did not resume bounded acceleration");
              trickleSpeeds.put(v.chassis, velocity);
            }
            System.out.println(
                "FD_ENGINEERED_ENERGY_INPUT family="
                    + f.design.family
                    + " pulse=100 trickle=200 ticks="
                    + trickleTicks[0]);
            report(helper, f, "TRICKLE");
            energy(helper, f).receiveEnergy(Integer.MAX_VALUE, false);
          }
        });
    helper.runAtTickTime(
        1280,
        () -> {
          checkIntact(helper, fixtures, riders);
          for (var f : fixtures) {
            helper.assertTrue(
                energy(helper, f).getEnergyStored() > 0 && !starving(level, f),
                "Refilled motor remained starved");
            for (var v : f.cars)
              helper.assertTrue(
                  speed(level, v) > trickleSpeeds.get(v.chassis) + 1,
                  "Full refill did not recover launch");
            report(helper, f, "REFILLED");
            energy(helper, f).extractEnergy(Integer.MAX_VALUE, false);
            f.motor.setSteps(
                List.of(new MotorStep(MotorStep.Kind.TRIM, MotorStep.Heading.FORWARD, 1)));
          }
          CoastersEngineeredConfig.REGEN_EFFICIENCY.set(0.0);
        });
    helper.runAtTickTime(
        1320,
        () -> {
          checkIntact(helper, fixtures, riders);
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(
                  Math.abs(speed(level, v)) < 1.3, "Unpowered non-regenerative trim failed");
            helper.assertTrue(
                energy(helper, f).getEnergyStored() == 0, "Disabled regeneration produced FE");
            report(helper, f, "REGEN_DISABLED");
            System.out.println(
                "FD_ENGINEERED_ENERGY_RELAUNCH_POSITION family="
                    + f.design.family
                    + " distances="
                    + f.cars.stream().map(v -> center(level, v).z - f.rail.from.getZ()).toList());
            energy(helper, f).receiveEnergy(Integer.MAX_VALUE, false);
            VehicleOperations.capture(level, f.cars.getFirst());
          }
        });
    helper.runAtTickTime(
        1450,
        () -> {
          checkIntact(helper, fixtures, riders);
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(Boarding.ready(level, v), "Recovery setup did not recapture");
            // The previous passes finish near the far end; use the available reverse run.
            f.motor.setSteps(
                List.of(new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.BACKWARD, 12)));
            VehicleOperations.dispatch(level, f.cars.getFirst());
          }
        });
    helper.runAtTickTime(
        1530,
        () -> {
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(
                  speed(level, v) < -3,
                  "Recovery setup did not relaunch: "
                      + f.design
                      + " speed="
                      + speed(level, v)
                      + " position="
                      + center(level, v)
                      + " fault="
                      + v.fault);
            report(helper, f, "REGEN_SETUP_REVERSE");
            // Empty at the old capacity first: do not hide stored energy behind a smaller capacity.
            energy(helper, f).extractEnergy(Integer.MAX_VALUE, false);
            f.motor.setSteps(
                List.of(new MotorStep(MotorStep.Kind.TRIM, MotorStep.Heading.FORWARD, 1)));
          }
          CoastersEngineeredConfig.ENERGY_BUFFER.set(1000);
          CoastersEngineeredConfig.REGEN_EFFICIENCY.set(1.0);
        });
    helper.runAtTickTime(
        1570,
        () -> {
          checkIntact(helper, fixtures, riders);
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(Math.abs(speed(level, v)) < 1.3, "Full-regeneration trim failed");
            var energy = energy(helper, f);
            helper.assertTrue(
                energy.getMaxEnergyStored() == 1000 && energy.getEnergyStored() == 1000,
                "Regeneration did not saturate the minimum buffer exactly");
            helper.assertTrue(
                energy.receiveEnergy(1, false) == 0, "Full buffer accepted excess FE");
            report(helper, f, "REGEN_SATURATED");
            VehicleOperations.capture(level, f.cars.getFirst());
          }
        });
    helper.runAtTickTime(
        1700,
        () -> {
          checkIntact(helper, fixtures, riders);
          for (var f : fixtures) {
            for (var v : f.cars)
              helper.assertTrue(
                  Boarding.ready(level, v), "Limited-power car failed final recapture");
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

  private static boolean starving(ServerLevel level, Fixture f) {
    return f.motor
        .saveWithFullMetadata(level.registryAccess())
        .getBoolean(CoastersEngineered.storageKey("Starving"));
  }

  private static void checkStarved(GameTestHelper helper, Fixture f) {
    helper.assertTrue(
        energy(helper, f).getEnergyStored() == 0
            && f.motor.isDriving()
            && starving(helper.getLevel(), f),
        "Limited-power motor lost presence or starvation state: "
            + f.design
            + " stored="
            + energy(helper, f).getEnergyStored()
            + " driving="
            + f.motor.isDriving()
            + " starving="
            + starving(helper.getLevel(), f));
  }

  private static void checkIntact(GameTestHelper helper, List<Fixture> fixtures, List<Pig> riders) {
    for (var f : fixtures) {
      helper.assertTrue(
          VehicleOperations.consist(helper.getLevel(), f.cars.getFirst()).size() == f.cars.size(),
          "Energy run lost a coupling");
      for (var v : f.cars)
        helper.assertTrue(
            v.fault.isBlank() && v.restraints,
            "Energy run lost an interlock: " + f.design + " fault=" + v.fault);
    }
    for (var rider : riders) helper.assertTrue(rider.isPassenger(), "Energy run lost a passenger");
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
