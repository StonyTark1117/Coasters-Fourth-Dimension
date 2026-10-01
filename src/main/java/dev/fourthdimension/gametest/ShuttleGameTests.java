package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class ShuttleGameTests {
  @GameTest(template = "empty", timeoutTicks = 1800)
  public static void routeChunkStopsTickingWinchReloadAndPhysicalRecovery(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 51; x <= 53; x++) for (int z = 33; z <= 43; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, new BlockPos(840, 180, 600), false);
    ShuttleWinchEntity[] winch = {example.winch()};
    var container = SubLevelContainer.getContainer(level);
    boolean[] recovering = {false};
    double[] stoppedHeight = {0};
    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
          VehicleOperations.dispatch(level, example.cars().getFirst());
        });
    helper.runAtTickTime(
        90,
        () -> {
          // Leave the boarding, first spike and loaded bodies available; unload the remote spike.
          for (int x = 51; x <= 53; x++)
            for (int z = 40; z <= 43; z++) level.setChunkForced(x, z, false);
        });
    helper.runAtTickTime(
        500,
        () -> {
          helper.assertTrue(
              !level
                  .getChunkSource()
                  .chunkMap
                  .getDistanceManager()
                  .inBlockTickingRange(
                      net.minecraft.world.level.ChunkPos.asLong(
                          winch[0].routeEnd.getX() >> 4, winch[0].routeEnd.getZ() >> 4)),
              "Remote route chunk remained available for native physics");
          helper.assertTrue(
              winch[0].phase == ShuttleWinchEntity.Phase.FAULT,
              "Missing route chunk did not fault the shuttle");
          stoppedHeight[0] =
              CoasterCartResolve.cartCenterWorld(container.getSubLevel(winch[0].vehicle)).y;
          var pos = winch[0].getBlockPos();
          var state = level.getBlockState(pos);
          var saved = winch[0].saveWithFullMetadata(level.registryAccess());
          var previousBody = winch[0].carriage;
          level.removeBlockEntity(pos);
          var restored =
              (ShuttleWinchEntity)
                  net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                      pos, state, saved, level.registryAccess());
          helper.assertTrue(
              restored != null
                  && restored.carriage.equals(previousBody)
                  && restored.cars.size() == 4
                  && restored.phase == ShuttleWinchEntity.Phase.FAULT,
              "Winch save lost native carriage, consist, or interrupted state");
          level.setBlockEntity(restored);
          ShuttleRuntime.register(level, restored);
          winch[0] = restored;
        });
    helper.runAtTickTime(
        600,
        () -> {
          var height =
              CoasterCartResolve.cartCenterWorld(container.getSubLevel(winch[0].vehicle)).y;
          helper.assertTrue(
              Math.abs(height - stoppedHeight[0]) < .3,
              "Reloaded caught train rolled back while route was unavailable");
          for (int x = 51; x <= 53; x++)
            for (int z = 40; z <= 43; z++) level.setChunkForced(x, z, true);
          level.getChunkAt(winch[0].routeEnd);
          winch[0].recover();
          recovering[0] = true;
        });
    helper.onEachTick(
        () -> {
          if (recovering[0] && winch[0].phase == ShuttleWinchEntity.Phase.BOARDING) {
            helper.assertTrue(
                example.cars().stream().allMatch(v -> Boarding.ready(level, v)),
                "Recovered train did not align every carrier");
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                            container.getSubLevel(winch[0].vehicle))
                        .size()
                    == 4,
                "Chunk/reload recovery broke the consist");
            System.out.println(
                "FD_SHUTTLE_CHUNK_RELOAD recovered=true carriage=" + winch[0].carriage);
            helper.succeed();
          }
        });
    helper.runAtTickTime(
        1790,
        () ->
            helper.fail(
                "Chunk/reload recovery did not finish: " + winch[0].phase + " " + winch[0].fault));
  }

  @GameTest(template = "empty", timeoutTicks = 2600)
  public static void sixteenRidersPhysicalCarriagePowerHoldGravityReturn(GameTestHelper helper) {
    occupiedPowerCycle(helper, 600, false);
  }

  @BeforeBatch(batch = "fourthdimension_engineered_pax_fe")
  public static void enablePaxEnergy(net.minecraft.server.level.ServerLevel level) {
    if (dev.fourthdimension.integration.OptionalIntegrations.supported(
        "coastersengineered", Set.of("1.1.1")))
      dev.fourthdimension.integration.engineered.EngineeredShuttleCheck.configureEnergy(true);
  }

  @AfterBatch(batch = "fourthdimension_engineered_pax_fe")
  public static void restorePaxEnergy(net.minecraft.server.level.ServerLevel level) {
    if (dev.fourthdimension.integration.OptionalIntegrations.supported(
        "coastersengineered", Set.of("1.1.1")))
      dev.fourthdimension.integration.engineered.EngineeredShuttleCheck.configureEnergy(false);
  }

  @GameTest(template = "empty", batch = "fourthdimension_engineered_pax_fe", timeoutTicks = 2600)
  public static void energyPoweredMotorsRespectOccupiedPaxPowerHold(GameTestHelper helper) {
    if (!dev.fourthdimension.integration.OptionalIntegrations.supported(
        "coastersengineered", Set.of("1.1.1"))) {
      System.out.println("FD_ENGINEERED_PAX_FE_SKIPPED: optional 1.1.1 artifact absent");
      helper.succeed();
      return;
    }
    occupiedPowerCycle(helper, 480, true);
  }

  private static void occupiedPowerCycle(GameTestHelper helper, int xOrigin, boolean energy) {
    var level = helper.getLevel();
    for (int x = (xOrigin - 24) >> 4; x <= (xOrigin + 16) >> 4; x++)
      for (int z = 33; z <= 43; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, new BlockPos(xOrigin, 180, 600), false);
    var winch = example.winch();
    var container = SubLevelContainer.getContainer(level);
    var riders = new ArrayList<Pig>();
    var assignments = new HashMap<UUID, UUID>();
    var phases = EnumSet.noneOf(ShuttleWinchEntity.Phase.class);
    double[] heights = {0};
    var holdPosition = new org.joml.Vector3d();
    long[] completedAt = {-1};
    boolean[] measuring = {false};
    var externalDriveChecks = new ArrayList<Runnable>();
    helper.onEachTick(
        () -> {
          if (!measuring[0]) return;
          phases.add(winch.phase);
          externalDriveChecks.forEach(Runnable::run);
          if (dev.fourthdimension.integration.OptionalIntegrations.supported(
              "coasterfins", java.util.Set.of("0.2.0")))
            dev.fourthdimension.integration.additions.AdditionsShuttleCheck.verify(helper, winch);
          helper.assertTrue(
              !dev.fourthdimension.integration.ExternalDrivePolicy.allows(level, winch.vehicle),
              "External propulsion gained control of a PAX shuttle");
          var telemetry =
              dev.fourthdimension.integration.RideTelemetry.sample(level, winch.vehicle);
          helper.assertTrue(
              telemetry.cars() == 4
                  && telemetry.capacity() == 16
                  && telemetry.occupied() == 16
                  && telemetry.restraintsClosed()
                  && telemetry.phase().equals(winch.phase.name()),
              "Shuttle telemetry lost occupants, restraints, consist or phase: " + telemetry);
          if (winch.phase != ShuttleWinchEntity.Phase.BOARDING)
            helper.assertTrue(!telemetry.dispatchReady(), "Active shuttle reported dispatch ready");
          for (var rider : riders) {
            var parent =
                rider.getVehicle() == null
                    ? null
                    : dev.ryanhcode.sable.Sable.HELPER.getContaining(rider.getVehicle());
            helper.assertTrue(
                rider.isAlive()
                    && parent != null
                    && assignments.get(rider.getUUID()).equals(parent.getUniqueId()),
                "PAX cycle lost an original rider/carrier attachment");
          }
          var lead = container.getSubLevel(winch.vehicle);
          var center = CoasterCartResolve.cartCenterWorld(lead);
          heights[0] = Math.max(heights[0], center.y);
          if (level.getGameTime() % 40 == 0)
            System.out.println(
                "FD_SHUTTLE phase="
                    + winch.phase
                    + " distance="
                    + winch.carriageDistance
                    + " center="
                    + center
                    + " max="
                    + winch.maximumDistance
                    + " fault="
                    + winch.fault);
          if (winch.phase != ShuttleWinchEntity.Phase.FAULT)
            for (var v : example.cars())
              helper.assertTrue(v.restraints, "Shuttle opened restraints during its cycle");
          for (var v : example.cars()) {
            var body = container.getSubLevel(v.chassis);
            var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(body);
            helper.assertTrue(
                bearing != null
                    && level.getBlockState(bearing).getValue(CoasterCartBlock.SNAP_ENGAGED),
                "Shuttle cycle lost native track engagement");
          }
          if (winch.cycles == 1) {
            if (completedAt[0] < 0) completedAt[0] = level.getGameTime();
            // Observe a settled occupied boarding hold before unloading, with optional drives live.
            if (level.getGameTime() - completedAt[0] < 20) return;
            helper.assertTrue(
                phases.containsAll(
                    EnumSet.of(
                        ShuttleWinchEntity.Phase.LIFTING,
                        ShuttleWinchEntity.Phase.OUTBOUND,
                        ShuttleWinchEntity.Phase.RETURNING,
                        ShuttleWinchEntity.Phase.CAPTURING)),
                "Shuttle skipped a physical cycle stage");
            helper.assertTrue(heights[0] > 195, "Native carriage did not lift the actual train");
            helper.assertTrue(
                winch.maximumDistance > winch.stationDistance + 30,
                "Gravity run did not traverse the station");
            helper.assertTrue(
                riders.stream().allMatch(Pig::isPassenger), "Shuttle lost a restrained rider");
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(lead).size() == 4,
                "Loaded PAX coupling broke");
            VehicleOperations.restraints(level, example.cars().getFirst(), false);
            for (var p : riders) {
              p.stopRiding();
              helper.assertTrue(p.isAlive() && !p.isPassenger(), "PAX unloading failed");
            }
            System.out.println(
                "FD_PAX_POWER_COMPLETE energyRequired="
                    + energy
                    + " cars=4 riders=16 alive=true unloaded=true mounts=true telemetry=true"
                    + " cycles="
                    + winch.cycles);
            riders.forEach(Pig::discard);
            for (int x = (xOrigin - 24) >> 4; x <= (xOrigin + 16) >> 4; x++)
              for (int z = 33; z <= 43; z++) level.setChunkForced(x, z, false);
            helper.succeed();
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          if (dev.fourthdimension.integration.OptionalIntegrations.supported(
              "coasterfins", java.util.Set.of("0.2.0")))
            dev.fourthdimension.integration.additions.AdditionsShuttleCheck.attach(helper, example);
          if (dev.fourthdimension.integration.OptionalIntegrations.supported(
              "coastersmagnetized", java.util.Set.of("1.1.0")))
            externalDriveChecks.add(
                dev.fourthdimension.integration.magnetized.MagnetizedShuttleCheck.attach(
                    helper, example));
          if (dev.fourthdimension.integration.OptionalIntegrations.supported(
              "coastersengineered", java.util.Set.of("1.1.1")))
            externalDriveChecks.add(
                energy
                    ? dev.fourthdimension.integration.engineered.EngineeredShuttleCheck
                        .attachEnergy(helper, example)
                    : dev.fourthdimension.integration.engineered.EngineeredShuttleCheck.attach(
                        helper, example));
          for (var v : example.cars())
            for (var c : v.carriers) {
              var body = container.getSubLevel(c.body);
              for (var offset : c.seats) {
                var pig = EntityType.PIG.create(level);
                pig.setNoAi(true);
                pig.setPos(xOrigin, 183, 600);
                level.addFreshEntity(pig);
                SeatBlock.sitDown(level, body.getPlot().getCenterBlock().offset(offset), pig);
                helper.assertTrue(pig.isPassenger(), "PAX boarding failed");
                riders.add(pig);
                assignments.put(pig.getUUID(), c.body);
              }
            }
          helper.assertTrue(riders.size() == 16, "PAX train must carry sixteen riders");
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
        });
    helper.runAtTickTime(
        81,
        () -> {
          // Occupancy is sampled by native physics. Let boarding reach that boundary before
          // dispatch and the continuous occupied telemetry assertions begin.
          var telemetry =
              dev.fourthdimension.integration.RideTelemetry.sample(level, winch.vehicle);
          helper.assertTrue(
              telemetry.occupied() == 16,
              "Boarding occupancy did not reach telemetry after one tick: " + telemetry);
          VehicleOperations.dispatch(level, example.cars().getFirst());
          measuring[0] = true;
        });
    helper.runAtTickTime(
        170,
        () -> {
          helper.assertTrue(winch.carriage != null, "Lift used no native carriage body");
          ((CreativeMotorBlockEntity) level.getBlockEntity(winch.getBlockPos().west()))
              .generatedSpeed.setValue(0);
        });
    helper.runAtTickTime(
        200,
        () -> {
          helper.assertTrue(
              winch.phase == ShuttleWinchEntity.Phase.FAULT,
              "Power interruption did not hold the train");
          holdPosition.set(
              CoasterCartResolve.cartCenterWorld(container.getSubLevel(winch.vehicle)));
          try {
            VehicleOperations.restraints(level, example.cars().getFirst(), false);
            helper.fail("Interrupted shuttle opened restraints");
          } catch (IllegalArgumentException expected) {
          }
        });
    helper.runAtTickTime(
        240,
        () -> {
          var drift =
              CoasterCartResolve.cartCenterWorld(container.getSubLevel(winch.vehicle))
                  .distance(holdPosition);
          helper.assertTrue(drift < .3, "Caught train moved during power loss: " + drift);
          System.out.println(
              "FD_PAX_POWER_HOLD energyRequired="
                  + energy
                  + " ticks=40 drift="
                  + drift
                  + " phase="
                  + winch.phase);
          ((CreativeMotorBlockEntity) level.getBlockEntity(winch.getBlockPos().west()))
              .generatedSpeed.setValue(128);
        });
    helper.runAtTickTime(260, winch::resume);
    helper.runAtTickTime(
        2590, () -> helper.fail("Shuttle did not finish: " + winch.phase + " " + winch.fault));
  }

  @GameTest(template = "empty", timeoutTicks = 3000)
  public static void tallerShuttleTwoCompleteCyclesAndStationLock(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 44; x <= 46; x++) for (int z = 32; z <= 45; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, new BlockPos(720, 180, 600), true);
    var winch = example.winch();
    var container = SubLevelContainer.getContainer(level);
    boolean[] running = {false}, second = {false}, rejected = {false};
    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
          VehicleOperations.dispatch(level, example.cars().getFirst());
          running[0] = true;
        });
    helper.onEachTick(
        () -> {
          if (!running[0]) return;
          if (winch.phase == ShuttleWinchEntity.Phase.OUTBOUND && !rejected[0]) {
            rejected[0] = true;
            try {
              VehicleOperations.restraints(level, example.cars().getFirst(), false);
              helper.fail("Outbound station pass unlocked restraints");
            } catch (IllegalArgumentException expected) {
            }
          }
          if (winch.cycles == 1 && !second[0]) {
            second[0] = true;
            VehicleOperations.dispatch(level, example.cars().getFirst());
          }
          if (winch.cycles == 2) {
            helper.assertTrue(rejected[0], "Tall shuttle never completed an outbound pass");
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                            container.getSubLevel(winch.vehicle))
                        .size()
                    == 4,
                "Tall repeated shuttle lost a native coupling");
            helper.assertTrue(
                example.cars().stream().allMatch(v -> Boarding.ready(level, v)),
                "Tall train did not capture all carriers");
            System.out.println(
                "FD_SHUTTLE_TALL cycles="
                    + winch.cycles
                    + " maximum="
                    + winch.maximumDistance
                    + " station="
                    + winch.stationDistance);
            helper.succeed();
          }
        });
    helper.runAtTickTime(
        2990,
        () -> helper.fail("Tall shuttle did not finish twice: " + winch.phase + " " + winch.fault));
  }
}
