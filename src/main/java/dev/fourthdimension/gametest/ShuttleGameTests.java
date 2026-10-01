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
  @GameTest(template = "empty", batch = "fourthdimension_shuttle_chunks", timeoutTicks = 12000)
  public static void occupiedLiftNativeEvictionExplicitResume(GameTestHelper helper) {
    var level = helper.getLevel();
    var origin = new BlockPos(-840, 180, 600);
    java.util.function.Consumer<Boolean> tickets =
        force -> {
          for (int x = -54; x <= -52; x++)
            for (int z = 33; z <= 43; z++) level.setChunkForced(x, z, force);
        };
    tickets.accept(true);
    var example = ShuttleExamples.create(level, origin, false);
    var container = SubLevelContainer.getContainer(level);
    var oldWinch = example.winch();
    var anchors =
        dev.fourthdimension.runtime.FoundationData.get(level).rails.stream()
            .filter(
                r -> r.family == dev.fourthdimension.contract.Family.PAX && r.from.getX() == -840)
            .flatMap(r -> java.util.stream.Stream.of(r.from, r.to))
            .distinct()
            .toList();
    var oldBlocks = new HashMap<BlockPos, net.minecraft.world.level.block.entity.BlockEntity>();
    anchors.forEach(p -> oldBlocks.put(p, level.getBlockEntity(p)));
    oldBlocks.put(oldWinch.getBlockPos(), oldWinch);
    oldBlocks.put(
        oldWinch.getBlockPos().west(), level.getBlockEntity(oldWinch.getBlockPos().west()));
    var oldBodies = new HashMap<UUID, dev.ryanhcode.sable.sublevel.SubLevel>();
    var assignments = new HashMap<UUID, UUID>();
    ShuttleWinchEntity[] live = {oldWinch};
    long start = level.getGameTime();
    long[] restoredAt = {-1}, resumedAt = {-1};
    double[] hold = {0};
    long[] completedAt = {-1};
    var externalChecks = new ArrayList<Runnable>();
    var phases = EnumSet.noneOf(ShuttleWinchEntity.Phase.class);
    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          if (dev.fourthdimension.integration.OptionalIntegrations.supported(
              "coastersengineered", Set.of("1.1.1"))) {
            externalChecks.add(
                dev.fourthdimension.integration.engineered.EngineeredShuttleCheck.attachReload(
                    helper, example));
            var motorPos = oldWinch.getBlockPos().offset(-6, 0, 4);
            oldBlocks.put(motorPos, level.getBlockEntity(motorPos));
            oldBlocks.put(motorPos.above(2), level.getBlockEntity(motorPos.above(2)));
          }

          for (var v : example.cars())
            for (var c : v.carriers)
              for (var offset : c.seats) {
                var pig = EntityType.PIG.create(level);
                pig.setNoAi(true);
                pig.setPersistenceRequired();
                pig.setPos(origin.getCenter().add(0, 3, 0));
                level.addFreshEntity(pig);
                SeatBlock.sitDown(
                    level,
                    container.getSubLevel(c.body).getPlot().getCenterBlock().offset(offset),
                    pig);
                helper.assertTrue(pig.isPassenger(), "PAX interruption boarding failed");
                assignments.put(pig.getUUID(), c.body);
              }
          helper.assertTrue(assignments.size() == 16, "PAX interruption requires sixteen riders");
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
          VehicleOperations.dispatch(level, example.cars().getFirst());
        });
    helper.runAtTickTime(
        150,
        () -> {
          helper.assertTrue(
              oldWinch.phase == ShuttleWinchEntity.Phase.LIFTING && oldWinch.carriage != null,
              "PAX interruption did not start during an actual carriage lift");
          for (var v : example.cars()) {
            oldBodies.put(v.chassis, container.getSubLevel(v.chassis));
            for (var c : v.carriers) oldBodies.put(c.body, container.getSubLevel(c.body));
          }
          oldBodies.put(oldWinch.carriage, container.getSubLevel(oldWinch.carriage));
          helper.assertTrue(
              oldBodies.size() == 13 && oldBodies.values().stream().allMatch(Objects::nonNull),
              "Expected four PAX chassis, eight carriers and one carriage");
          tickets.accept(false);
          System.out.println(
              "FD_PAX_EVICTION_RELEASE phase="
                  + oldWinch.phase
                  + " bodies="
                  + oldBodies.size()
                  + " riders="
                  + assignments.size());
        });
    helper.runAtTickTime(
        400,
        () -> {
          level.getChunkSource().save(true);
          System.out.println("FD_PAX_EVICTION_FLUSH native=true");
        });
    helper.onEachTick(
        () -> {
          long tick = level.getGameTime() - start;
          externalChecks.forEach(Runnable::run);
          if (tick < 400) return;
          if (restoredAt[0] < 0) {
            boolean absent =
                oldBlocks.entrySet().stream()
                        .allMatch(
                            e ->
                                e.getValue().isRemoved()
                                    && level
                                            .getChunkSource()
                                            .getChunkNow(
                                                e.getKey().getX() >> 4, e.getKey().getZ() >> 4)
                                        == null)
                    && oldBodies.entrySet().stream()
                        .allMatch(
                            e ->
                                e.getValue().isRemoved()
                                    && container.getSubLevel(e.getKey()) == null);
            if (!absent) {
              if (tick == 900 || tick == 1300)
                System.out.println(
                    "FD_PAX_EVICTION_WAIT blocks="
                        + oldBlocks.entrySet().stream()
                            .map(
                                e ->
                                    e.getKey()
                                        + ":"
                                        + e.getValue().isRemoved()
                                        + ":resident="
                                        + (level
                                                .getChunkSource()
                                                .getChunkNow(
                                                    e.getKey().getX() >> 4, e.getKey().getZ() >> 4)
                                            != null)
                                        + ":ticking="
                                        + dev.fourthdimension.track.TrackGeometry.ticking(
                                            level, e.getKey()))
                            .toList()
                        + " bodies="
                        + oldBodies.entrySet().stream()
                            .map(
                                e ->
                                    e.getKey()
                                        + ":"
                                        + e.getValue().isRemoved()
                                        + ":"
                                        + (container.getSubLevel(e.getKey()) != null))
                            .toList());
              if (tick == 1000) {
                level.getChunkSource().save(true);
                System.out.println("FD_PAX_EVICTION_FLUSH retry=true");
              }
              helper.assertTrue(tick < 8000, "PAX fixture never physically evicted");
              return;
            }
            helper.assertTrue(
                example.cars().stream()
                    .allMatch(
                        v ->
                            v.restraints
                                && v.carriers.stream().allMatch(c -> c.occupants.size() == 2)),
                "Unloaded PAX lost restrained saved occupancy");
            System.out.println(
                "FD_PAX_EVICTED tick="
                    + tick
                    + " blocks="
                    + oldBlocks.size()
                    + " bodies="
                    + oldBodies.size()
                    + " occupants=16 phase="
                    + oldWinch.phase
                    + " interrupted="
                    + oldWinch.interrupted);
            tickets.accept(true);
            restoredAt[0] = tick;
            return;
          }
          if (tick < restoredAt[0] + 100) return;
          if (resumedAt[0] < 0) {
            live[0] = (ShuttleWinchEntity) level.getBlockEntity(oldWinch.getBlockPos());
            var w = live[0];
            helper.assertTrue(
                w != null
                    && w != oldWinch
                    && w.carriage.equals(oldWinch.carriage)
                    && w.cars.equals(oldWinch.cars),
                "PAX native reload lost winch or train/carriage identity");
            helper.assertTrue(
                oldBlocks.entrySet().stream()
                    .allMatch(
                        e ->
                            level.getBlockEntity(e.getKey()) != null
                                && level.getBlockEntity(e.getKey()) != e.getValue()),
                "PAX native reload reused old block entities");
            helper.assertTrue(
                oldBodies.entrySet().stream()
                    .allMatch(
                        e ->
                            container.getSubLevel(e.getKey()) != null
                                && container.getSubLevel(e.getKey()) != e.getValue()),
                "PAX native reload lost bodies or reused old instances");
            helper.assertTrue(
                w.phase == ShuttleWinchEntity.Phase.FAULT
                    && w.interrupted == ShuttleWinchEntity.Phase.LIFTING,
                "PAX reloaded lift resumed without explicit authorization: "
                    + w.phase
                    + " "
                    + w.fault);
            verifyReloadedPassengers(helper, assignments);
            if (tick == restoredAt[0] + 100) {
              hold[0] = CoasterCartResolve.cartCenterWorld(container.getSubLevel(w.vehicle)).y;
              System.out.println(
                  "FD_PAX_RESTORED fresh=true riders=16 phase="
                      + w.phase
                      + " interrupted="
                      + w.interrupted
                      + " fault="
                      + w.fault);
            }
            try {
              VehicleOperations.restraints(level, example.cars().getFirst(), false);
              helper.fail("Reloaded interrupted PAX opened restraints");
            } catch (IllegalArgumentException expected) {
            }
            if (tick < restoredAt[0] + 160) return;
            double drift =
                Math.abs(
                    CoasterCartResolve.cartCenterWorld(container.getSubLevel(w.vehicle)).y
                        - hold[0]);
            helper.assertTrue(drift < .3, "Reloaded PAX mechanical catch rolled back: " + drift);
            w.resume();
            resumedAt[0] = tick;
            System.out.println("FD_PAX_RESUMED holdTicks=60 drift=" + drift + " phase=" + w.phase);
            return;
          }
          var w = live[0];
          phases.add(w.phase);
          verifyReloadedPassengers(helper, assignments);
          helper.assertTrue(
              !dev.fourthdimension.integration.ExternalDrivePolicy.allows(level, w.vehicle),
              "External drive gained PAX ownership after reload");
          helper.assertTrue(
              w.phase != ShuttleWinchEntity.Phase.FAULT, "Resumed PAX faulted: " + w.fault);
          if (w.cycles == 1 && w.phase == ShuttleWinchEntity.Phase.BOARDING) {
            if (completedAt[0] < 0) completedAt[0] = tick;
            if (tick - completedAt[0] < 20) return;
            helper.assertTrue(
                phases.containsAll(
                    EnumSet.of(
                        ShuttleWinchEntity.Phase.LIFTING,
                        ShuttleWinchEntity.Phase.OUTBOUND,
                        ShuttleWinchEntity.Phase.RETURNING,
                        ShuttleWinchEntity.Phase.CAPTURING,
                        ShuttleWinchEntity.Phase.BOARDING)),
                "Reloaded PAX skipped a cycle phase");
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                            container.getSubLevel(w.vehicle))
                        .size()
                    == 4,
                "PAX reload broke the four-car consist");
            helper.assertTrue(
                example.cars().stream().allMatch(v -> Boarding.ready(level, v)),
                "Reloaded PAX did not return ready");
            VehicleOperations.restraints(level, example.cars().getFirst(), false);
            for (var id : assignments.keySet()) {
              var pig = level.getEntity(id);
              pig.stopRiding();
              helper.assertTrue(
                  pig.isAlive() && !pig.isPassenger(), "Reloaded PAX failed normal unloading");
            }
            System.out.println(
                "FD_PAX_EVICTION_COMPLETE cycles="
                    + w.cycles
                    + " cars=4 riders=16 alive=true unloaded=true phases="
                    + phases);
            helper.succeed();
          }
        });
  }

  private static void verifyReloadedPassengers(GameTestHelper helper, Map<UUID, UUID> assignments) {
    var level = helper.getLevel();
    for (var entry : assignments.entrySet()) {
      var rider = level.getEntity(entry.getKey());
      var parent =
          rider == null || rider.getVehicle() == null
              ? null
              : dev.ryanhcode.sable.Sable.HELPER.getContaining(rider.getVehicle());
      helper.assertTrue(
          rider != null
              && rider.isAlive()
              && parent != null
              && entry.getValue().equals(parent.getUniqueId()),
          "Native PAX reload lost original rider/carrier attachment: " + entry.getKey());
    }
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

  @GameTest(template = "empty", timeoutTicks = 2600)
  public static void sixteenRidersPhysicalCarriagePowerHoldGravityReturn(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 36; x <= 38; x++) for (int z = 33; z <= 43; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, new BlockPos(600, 180, 600), false);
    var winch = example.winch();
    var container = SubLevelContainer.getContainer(level);
    var riders = new ArrayList<Pig>();
    var phases = EnumSet.noneOf(ShuttleWinchEntity.Phase.class);
    double[] heights = {0, 0};
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
                  && telemetry.phase().equals(winch.phase.name()),
              "Shuttle telemetry lost consist or phase");
          if (winch.phase != ShuttleWinchEntity.Phase.BOARDING)
            helper.assertTrue(!telemetry.dispatchReady(), "Active shuttle reported dispatch ready");
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
              p.discard();
            }
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
                dev.fourthdimension.integration.engineered.EngineeredShuttleCheck.attach(
                    helper, example));
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
          VehicleOperations.restraints(level, example.cars().getFirst(), true);
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
