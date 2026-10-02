package dev.fourthdimension.gametest;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.runtime.CarrierRuntime;
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
  @GameTest(template = "empty", timeoutTicks = 240)
  public static void idleWinchPreservesStateWhileNativeTrainRestores(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 55; x <= 57; x++)
      for (int z = -22; z <= -13; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, new BlockPos(900, 180, -300), false);
    var winch = example.winch();
    var container = SubLevelContainer.getContainer(level);
    helper.runAtTickTime(80, () -> ShuttleExamples.assemble(level, example));
    helper.runAtTickTime(
        100,
        () -> {
          helper.assertTrue(
              winch.carriage == null && winch.phase == ShuttleWinchEntity.Phase.BOARDING,
              "Fixture must be bound and idle before its first lift");
          var revisions = example.cars().stream().map(v -> v.revision).toList();
          var lead =
              (dev.ryanhcode.sable.sublevel.ServerSubLevel) container.getSubLevel(winch.vehicle);
          // Native holding preserves the train and restores it when physics chunks are ready.
          // Observe the real gap before Sable's next normal holding-map tick.
          container
              .getHoldingChunkMap()
              .moveToUnloaded(
                  lead,
                  new net.minecraft.world.level.ChunkPos(
                      BlockPos.containing(
                          lead.logicalPose().position().x,
                          lead.logicalPose().position().y,
                          lead.logicalPose().position().z)));
          helper.assertTrue(
              container.getSubLevel(winch.vehicle) == null,
              "Native holding did not create an availability gap");
          helper.assertTrue(
              container.getHoldingChunkMap().getHoldingSubLevel(winch.vehicle) != null,
              "Fixture deleted the native lead instead of retaining it in holding");
          ShuttleRuntime.tick(level, .05);
          System.out.println(
              "FD_SHUTTLE_IDLE observedPhase="
                  + winch.phase
                  + " fault="
                  + winch.fault
                  + " leadLoaded="
                  + (container.getSubLevel(winch.vehicle) != null)
                  + " leadHeld="
                  + (container.getHoldingChunkMap().getHoldingSubLevel(winch.vehicle) != null)
                  + " restraints="
                  + example.cars().stream().map(v -> v.restraints).toList());
          helper.assertTrue(
              winch.phase == ShuttleWinchEntity.Phase.BOARDING
                  && winch.fault.isEmpty()
                  && example.cars().stream().allMatch(v -> !v.restraints && v.fault.isEmpty())
                  && example.cars().stream().map(v -> v.revision).toList().equals(revisions),
              "Idle native restoration changed phase, faults, restraints or revisions");
          try {
            winch.start();
            helper.fail("Missing lead allowed shuttle dispatch");
          } catch (IllegalArgumentException expected) {
            helper.assertTrue(
                expected.getMessage().equals("Shuttle lead car unavailable"),
                "Unavailable dispatch failed for another reason: " + expected.getMessage());
          }
          helper.assertTrue(
              winch.phase == ShuttleWinchEntity.Phase.BOARDING && winch.carriage == null,
              "Rejected dispatch allocated a carriage or advanced the shuttle");
          System.out.println(
              "FD_SHUTTLE_IDLE gapPreserved=true missingLeadDispatchRejected=true lead="
                  + winch.vehicle);
        });
    helper.runAtTickTime(
        160,
        () -> {
          helper.assertTrue(
              example.cars().stream()
                  .allMatch(
                      v ->
                          container.getSubLevel(v.chassis) != null
                              && v.carriers.stream()
                                  .allMatch(c -> container.getSubLevel(c.body) != null)
                              && Boarding.ready(level, v)
                              && !v.restraints
                              && v.fault.isEmpty()),
              "Native holding failed to restore captured, unrestrained matching cars");
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                          container.getSubLevel(winch.vehicle))
                      .size()
                  == 4,
              "Native restoration lost the consist");
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
          // Idle waiting must not turn a lost route into an eligible dispatch.
          level.setBlockAndUpdate(
              winch.routeStart, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
          try {
            winch.start();
            helper.fail("Deleted shuttle support allowed dispatch");
          } catch (IllegalArgumentException expected) {
            helper.assertTrue(
                winch.phase == ShuttleWinchEntity.Phase.BOARDING && winch.carriage == null,
                "Invalid route dispatch partially advanced the shuttle");
          }
          System.out.println(
              "FD_SHUTTLE_IDLE restoredCars=4 restoredCarriers=8 links=4"
                  + " missingSupportDispatchRejected=true");
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 320)
  public static void caughtWinchPreservesStateWhileNativeTrainRestores(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 55; x <= 57; x++)
      for (int z = -45; z <= -35; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, new BlockPos(900, 180, -650), false);
    ShuttleWinchEntity[] winch = {example.winch()};
    var container = SubLevelContainer.getContainer(level);
    UUID[] carriage = {null};
    double[] station = {0};
    int[] cycles = {0};
    helper.runAtTickTime(80, () -> ShuttleExamples.assemble(level, example));
    helper.runAtTickTime(
        100,
        () -> {
          winch[0].fail("Interrupted boarding fixture: inspect and recover");
          winch[0].recover();
        });
    helper.runAtTickTime(
        160,
        () -> {
          helper.assertTrue(
              winch[0].phase == ShuttleWinchEntity.Phase.BOARDING
                  && winch[0].carriage != null
                  && example.cars().stream().allMatch(v -> Boarding.ready(level, v)),
              "Normal recovery did not physically catch the boarding train");
          VehicleOperations.restraints(level, example.cars().getFirst(), false);
          carriage[0] = winch[0].carriage;
          station[0] = winch[0].carriageDistance;
          cycles[0] = winch[0].cycles;
          var revisions = example.cars().stream().map(v -> v.revision).toList();
          var pos = winch[0].getBlockPos();
          var state = level.getBlockState(pos);
          var saved = winch[0].saveWithFullMetadata(level.registryAccess());
          level.removeBlockEntity(pos);
          var restored =
              (ShuttleWinchEntity)
                  net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                      pos, state, saved, level.registryAccess());
          helper.assertTrue(
              restored != null && restored.carriage.equals(carriage[0]),
              "Reload lost the existing carriage identity");
          level.setBlockEntity(restored);
          ShuttleRuntime.register(level, restored);
          winch[0] = restored;
          ShuttleRuntime.tick(level, .05);
          var lead =
              (dev.ryanhcode.sable.sublevel.ServerSubLevel) container.getSubLevel(restored.vehicle);
          container
              .getHoldingChunkMap()
              .moveToUnloaded(
                  lead,
                  new net.minecraft.world.level.ChunkPos(
                      BlockPos.containing(
                          lead.logicalPose().position().x,
                          lead.logicalPose().position().y,
                          lead.logicalPose().position().z)));
          helper.assertTrue(
              container.getSubLevel(restored.vehicle) == null
                  && container.getSubLevel(carriage[0]) == null
                  && container.getHoldingChunkMap().getHoldingSubLevel(restored.vehicle) != null
                  && container.getHoldingChunkMap().getHoldingSubLevel(carriage[0]) != null,
              "Native holding did not retain both the caught train and carriage");
          ShuttleRuntime.tick(level, .05);
          System.out.println(
              "FD_SHUTTLE_CAUGHT observedPhase="
                  + restored.phase
                  + " fault="
                  + restored.fault
                  + " leadLoaded=false carriageLoaded=false"
                  + " leadHeld=true carriageHeld=true restraints="
                  + example.cars().stream().map(v -> v.restraints).toList());
          helper.assertTrue(
              restored.phase == ShuttleWinchEntity.Phase.BOARDING
                  && restored.fault.isEmpty()
                  && restored.carriage.equals(carriage[0])
                  && restored.carriageDistance == station[0]
                  && restored.cycles == cycles[0]
                  && example.cars().stream().allMatch(v -> !v.restraints && v.fault.isEmpty())
                  && example.cars().stream().map(v -> v.revision).toList().equals(revisions),
              "Caught native restoration changed phase, faults, restraints, coordinate or"
                  + " revisions");
          try {
            restored.start();
            helper.fail("Missing caught train allowed dispatch");
          } catch (IllegalArgumentException expected) {
            helper.assertTrue(
                expected.getMessage().equals("Shuttle lead car unavailable"),
                "Unavailable dispatch failed for another reason: " + expected.getMessage());
          }
          helper.assertTrue(
              restored.phase == ShuttleWinchEntity.Phase.BOARDING,
              "Rejected caught-train dispatch advanced the phase");
        });
    helper.runAtTickTime(
        220,
        () -> {
          helper.assertTrue(
              winch[0].phase == ShuttleWinchEntity.Phase.BOARDING
                  && winch[0].fault.isEmpty()
                  && winch[0].carriage.equals(carriage[0])
                  && container.getSubLevel(carriage[0]) != null
                  && example.cars().stream()
                      .allMatch(v -> Boarding.ready(level, v) && !v.restraints && v.fault.isEmpty())
                  && CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                              container.getSubLevel(winch[0].vehicle))
                          .size()
                      == 4,
              "Native restoration lost the physical boarding catch or ready consist");
          var carriageBody = container.getSubLevel(carriage[0]);
          var lead = container.getSubLevel(winch[0].vehicle);
          helper.assertTrue(
              carriageBody.logicalPose().position().distance(lead.logicalPose().position()) < 3.2,
              "Restored physical carriage drifted away from its boarding train");
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
          level.setBlockAndUpdate(
              winch[0].routeStart, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
          try {
            winch[0].start();
            helper.fail("Deleted remote route anchor allowed caught-train dispatch");
          } catch (IllegalArgumentException expected) {
            helper.assertTrue(
                winch[0].phase == ShuttleWinchEntity.Phase.BOARDING,
                "Deleted-route dispatch partially advanced the caught train");
          }
          System.out.println(
              "FD_SHUTTLE_CAUGHT restoredCars=4 restoredCarriers=8 links=4 sameCarriage=true"
                  + " missingLeadDispatchRejected=true missingSupportDispatchRejected=true");
        });
    helper.runAtTickTime(
        260,
        () -> {
          container.removeSubLevel(
              container.getSubLevel(carriage[0]),
              dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
          helper.assertTrue(
              winch[0].phase == ShuttleWinchEntity.Phase.FAULT
                  && example.cars().stream().allMatch(v -> v.restraints && !v.fault.isEmpty()),
              "Permanent carriage destruction was treated as temporary native holding");
          try {
            winch[0].recover();
            helper.fail("Missing physical carriage allowed recovery to advance");
          } catch (IllegalArgumentException expected) {
            helper.assertTrue(
                expected
                    .getMessage()
                    .equals("Native lift carriage unavailable; recover the shuttle"),
                "Deleted-carriage recovery failed for another reason: " + expected.getMessage());
          }
          helper.assertTrue(
              winch[0].phase == ShuttleWinchEntity.Phase.FAULT
                  && winch[0].carriage.equals(carriage[0])
                  && container.getSubLevel(carriage[0]) == null,
              "Rejected recovery silently replaced the destroyed carriage");
          System.out.println(
              "FD_SHUTTLE_CAUGHT permanentCarriageRemovalFaulted=true"
                  + " destroyedCarriageRecoveryRejected=true replacementAllocated=false");
          helper.succeed();
        });
  }

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

  @GameTest(template = "empty", batch = "performanceShuttle", timeoutTicks = 2600)
  public static void sixteenRidersPhysicalCarriagePowerHoldGravityReturn(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 36; x <= 38; x++) for (int z = 33; z <= 43; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, new BlockPos(600, 180, 600), false);
    var winch = example.winch();
    var container = SubLevelContainer.getContainer(level);
    var riders = new ArrayList<Pig>();
    var phases = EnumSet.noneOf(ShuttleWinchEntity.Phase.class);
    double[] heights = {0, 0};
    boolean[] measuring = {false};
    CarrierRuntime.PerformanceSnapshot[] performance = {null};
    Runnable[] motorProbe = {() -> {}};
    int[] parking = {0, 0, 0}; // elapsed ticks, consecutive ready ticks, rejected openings
    boolean[] disturbed = {false};
    helper.onEachTick(
        () -> {
          if (!measuring[0]) return;
          motorProbe[0].run();
          phases.add(winch.phase);
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
            // A completed cycle records the pre-physics capture. Native constraints can
            // still settle afterward; operator actions must use current readiness, not cycles.
            parking[0]++;
            helper.assertTrue(
                winch.phase == ShuttleWinchEntity.Phase.BOARDING,
                "Completed shuttle left its boarding phase");
            helper.assertTrue(
                parking[0] <= 80,
                "Captured train did not settle within four seconds: "
                    + boardingSnapshot(level, example.cars()));
            boolean ready = example.cars().stream().allMatch(v -> Boarding.ready(level, v));
            if (parking[0] == 1)
              System.out.println(
                  "FD_SHUTTLE_BOARDING first=" + boardingSnapshot(level, example.cars()));
            if (!ready) {
              assertOpeningRejected(helper, example.cars());
              parking[2]++;
              parking[1] = 0;
              return;
            }
            if (++parking[1] < 10) return;
            if (!disturbed[0]) {
              // Reproduce post-capture motion deterministically through the real pipeline.
              // Do not change saved readiness or loosen the normal operator interlock.
              var car = example.cars().getFirst();
              var bodies = new ArrayList<UUID>();
              bodies.add(car.chassis);
              car.carriers.forEach(c -> bodies.add(c.body));
              for (var id : bodies)
                container
                    .physicsSystem()
                    .getPipeline()
                    .addLinearAndAngularVelocity(
                        (dev.ryanhcode.sable.sublevel.ServerSubLevel) container.getSubLevel(id),
                        new org.joml.Vector3d(.2, 0, 0),
                        new org.joml.Vector3d());
              helper.assertTrue(
                  !Boarding.ready(level, car), "Native disturbance missed readiness guard");
              assertOpeningRejected(helper, example.cars());
              parking[2]++;
              parking[1] = 0;
              disturbed[0] = true;
              return;
            }
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
            performance[0] = CarrierRuntime.performanceSnapshot();
            helper.assertTrue(
                performance[0].carrierUpdates() >= 1_600,
                "PAX performance baseline did not cover enough live carrier updates");
            System.out.println(
                "FD_SHUTTLE_BOARDING settledTicks="
                    + parking[0]
                    + " consecutiveReady="
                    + parking[1]
                    + " rejectedOpenings="
                    + parking[2]
                    + " disturbed="
                    + disturbed[0]
                    + " final="
                    + boardingSnapshot(level, example.cars()));
            LogUtils.getLogger()
                .info(
                    "FD_PERFORMANCE_SHUTTLE riders=16 runtimeInvocations={} runtimeNanos={} averageNanosPerInvocation={} carrierUpdates={} carrierNanos={} averageNanosPerCarrier={} constraintsRebuilt={}",
                    performance[0].steps(),
                    performance[0].nanos(),
                    performance[0].averageNanos(),
                    performance[0].carrierUpdates(),
                    performance[0].carrierNanos(),
                    performance[0].averageCarrierNanos(),
                    performance[0].constraintsRebuilt());
            VehicleOperations.restraints(level, example.cars().getFirst(), false);
            for (var p : riders) {
              p.stopRiding();
              p.discard();
            }
            helper.succeed();
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          for (var v : example.cars())
            for (var c : v.carriers) {
              var body = container.getSubLevel(c.body);
              for (var offset : c.seats) {
                var pig = EntityType.PIG.create(level);
                pig.setNoAi(true);
                pig.setPos(600, 183, 600);
                level.addFreshEntity(pig);
                SeatBlock.sitDown(level, body.getPlot().getCenterBlock().offset(offset), pig);
                helper.assertTrue(pig.isPassenger(), "PAX boarding failed");
                riders.add(pig);
              }
            }
          helper.assertTrue(riders.size() == 16, "PAX train must carry sixteen riders");
          motorProbe[0] = EngineeredGameTests.addOpposingShuttleMotors(helper, winch);
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
          CarrierRuntime.resetPerformanceSnapshot();
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
          heights[1] = CoasterCartResolve.cartCenterWorld(container.getSubLevel(winch.vehicle)).y;
          try {
            VehicleOperations.restraints(level, example.cars().getFirst(), false);
            helper.fail("Interrupted shuttle opened restraints");
          } catch (IllegalArgumentException expected) {
          }
        });
    helper.runAtTickTime(
        240,
        () -> {
          var height = CoasterCartResolve.cartCenterWorld(container.getSubLevel(winch.vehicle)).y;
          helper.assertTrue(
              Math.abs(height - heights[1]) < .3, "Caught train rolled back during power loss");
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
    Runnable[] motorProbe = {() -> {}};
    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          motorProbe[0] = EngineeredGameTests.addOpposingShuttleMotors(helper, winch);
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
          VehicleOperations.dispatch(level, example.cars().getFirst());
          running[0] = true;
        });
    helper.onEachTick(
        () -> {
          if (!running[0]) return;
          motorProbe[0].run();
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

  private static String boardingSnapshot(
      net.minecraft.server.level.ServerLevel level,
      List<dev.fourthdimension.runtime.FoundationData.Vehicle> cars) {
    var container = SubLevelContainer.getContainer(level);
    return cars.stream()
        .map(
            v ->
                "ready="
                    + Boarding.ready(level, v)
                    + " speed="
                    + container
                        .physicsSystem()
                        .getPipeline()
                        .getLinearVelocity(
                            (dev.ryanhcode.sable.sublevel.ServerSubLevel)
                                container.getSubLevel(v.chassis),
                            new org.joml.Vector3d())
                        .length()
                    + " carriers="
                    + v.carriers.stream()
                        .map(
                            c -> "capture=" + c.capture + ",angle=" + c.angle + ",omega=" + c.omega)
                        .toList())
        .toList()
        .toString();
  }

  private static void assertOpeningRejected(
      GameTestHelper helper, List<dev.fourthdimension.runtime.FoundationData.Vehicle> cars) {
    var revisions = cars.stream().map(v -> v.revision).toList();
    try {
      VehicleOperations.restraints(helper.getLevel(), cars.getFirst(), false);
      helper.fail("Unready captured shuttle opened restraints");
    } catch (IllegalArgumentException expected) {
      helper.assertTrue(
          expected.getMessage().equals("Stop and align every carrier before operating restraints"),
          "Unexpected post-capture opening rejection: " + expected.getMessage());
    }
    helper.assertTrue(
        cars.stream().allMatch(v -> v.restraints)
            && cars.stream().map(v -> v.revision).toList().equals(revisions),
        "Rejected opening partially mutated the consist");
  }
}
