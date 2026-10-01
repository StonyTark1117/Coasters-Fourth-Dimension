package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class ShuttleChunkGameTests {
  @GameTest(template = "empty", batch = "fourthdimension_shuttle_chunks_lift", timeoutTicks = 12000)
  public static void occupiedLiftNativeEvictionExplicitResume(GameTestHelper helper) {
    occupiedEviction(helper, ShuttleWinchEntity.Phase.LIFTING, -840);
  }

  @GameTest(
      template = "empty",
      batch = "fourthdimension_shuttle_chunks_outbound",
      timeoutTicks = 12000)
  public static void occupiedOutboundNativeEvictionManualRecovery(GameTestHelper helper) {
    occupiedEviction(helper, ShuttleWinchEntity.Phase.OUTBOUND, -760);
  }

  @GameTest(
      template = "empty",
      batch = "fourthdimension_shuttle_chunks_returning",
      timeoutTicks = 12000)
  public static void occupiedReturningNativeEvictionManualRecovery(GameTestHelper helper) {
    occupiedEviction(helper, ShuttleWinchEntity.Phase.RETURNING, -680);
  }

  @GameTest(
      template = "empty",
      batch = "fourthdimension_shuttle_chunks_capture",
      timeoutTicks = 12000)
  public static void occupiedCaptureNativeEvictionExplicitResume(GameTestHelper helper) {
    occupiedEviction(helper, ShuttleWinchEntity.Phase.CAPTURING, -600);
  }

  private static void occupiedEviction(
      GameTestHelper helper, ShuttleWinchEntity.Phase interruptedPhase, int xOrigin) {
    var level = helper.getLevel();
    var origin = new BlockPos(xOrigin, 180, 600);
    java.util.function.Consumer<Boolean> tickets =
        force -> {
          for (int x = (xOrigin - 24) >> 4; x <= (xOrigin + 16) >> 4; x++)
            for (int z = 33; z <= 43; z++) level.setChunkForced(x, z, force);
        };
    tickets.accept(true);
    var example = ShuttleExamples.create(level, origin, false);
    var container = SubLevelContainer.getContainer(level);
    var oldWinch = example.winch();
    var anchors =
        dev.fourthdimension.runtime.FoundationData.get(level).rails.stream()
            .filter(
                r ->
                    r.family == dev.fourthdimension.contract.Family.PAX && r.from.getX() == xOrigin)
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
    long[] restoredAt = {-1}, resumedAt = {-1}, releasedAt = {-1};
    int[] targetTicks = {0};
    long[] partialAt = {-1}, partialBodyAt = {-1};
    var partialPosition = new org.joml.Vector3d();
    double[] partialTravel = {0};
    var holdPosition = new org.joml.Vector3d();
    var previousPosition = new org.joml.Vector3d();
    var restoredCarriagePosition = new org.joml.Vector3d();
    var previousCarriagePosition = new org.joml.Vector3d();
    double[] initialCarriageTravel = {0}, maxCarriageStep = {0};
    boolean[] carriageSeen = {false};
    double[] carriageDrift = {0};
    boolean[] observedBody = {false};
    double[] maxReloadStep = {0};
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
                    helper, example, interruptedPhase));
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
    helper.onEachTick(
        () -> {
          long tick = level.getGameTime() - start;
          externalChecks.forEach(Runnable::run);
          if (releasedAt[0] < 0) {
            if (oldWinch.vehicle != null) phases.add(oldWinch.phase);
            if (oldWinch.phase != interruptedPhase || oldWinch.carriage == null) return;
            // Lift far enough from the station that forcing the carriage chunk does not
            // also make the neighboring winch chunk tick.
            if (++targetTicks[0] < (interruptedPhase == ShuttleWinchEntity.Phase.LIFTING ? 240 : 5))
              return;
            for (var v : example.cars()) {
              oldBodies.put(v.chassis, container.getSubLevel(v.chassis));
              for (var c : v.carriers) oldBodies.put(c.body, container.getSubLevel(c.body));
            }
            oldBodies.put(oldWinch.carriage, container.getSubLevel(oldWinch.carriage));
            helper.assertTrue(
                oldBodies.size() == 13 && oldBodies.values().stream().allMatch(Objects::nonNull),
                "Expected four PAX chassis, eight carriers and one carriage");

            var trainIds = new HashSet<>(oldBodies.keySet());
            trainIds.remove(oldWinch.carriage);
            for (var id : trainIds) {
              var dependencies =
                  dev.ryanhcode.sable.api.SubLevelHelper.getLoadingDependencyChain(
                      (dev.ryanhcode.sable.sublevel.ServerSubLevel) oldBodies.get(id));
              var actualIds =
                  dependencies.stream()
                      .map(dev.ryanhcode.sable.sublevel.SubLevel::getUniqueId)
                      .collect(java.util.stream.Collectors.toSet());
              helper.assertTrue(
                  actualIds.containsAll(trainIds),
                  "Incomplete native train loading group: expected=12 actual="
                      + trainIds.stream().filter(actualIds::contains).count()
                      + " seed="
                      + id);
            }
            System.out.println(
                "FD_PAX_PHASE_DEPENDENCIES target="
                    + interruptedPhase
                    + " seeds=12 requiredBodies=12 complete=true");
            int replacedBeforeRelease = 0;
            for (var pos : oldBlocks.keySet()) {
              var current = level.getBlockEntity(pos);
              helper.assertTrue(current != null, "PAX block missing before release");
              if (current != oldBlocks.get(pos)) replacedBeforeRelease++;
              oldBlocks.put(pos, current);
            }
            System.out.println(
                "FD_PAX_PHASE_REFERENCES target="
                    + interruptedPhase
                    + " replacedBeforeRelease="
                    + replacedBeforeRelease);
            var lead = container.getSubLevel(oldWinch.vehicle);
            var velocity =
                container
                    .physicsSystem()
                    .getPipeline()
                    .getLinearVelocity(
                        (dev.ryanhcode.sable.sublevel.ServerSubLevel) lead,
                        new org.joml.Vector3d());
            System.out.println(
                "FD_PAX_PHASE_RELEASE target="
                    + interruptedPhase
                    + " tick="
                    + tick
                    + " speed="
                    + velocity.length()
                    + " bodies="
                    + oldBodies.size()
                    + " riders=16");
            tickets.accept(false);
            releasedAt[0] = tick;
            return;
          }
          long sinceRelease = tick - releasedAt[0];
          // Native save futures can enqueue their unload callbacks after a flush returns.
          // Accelerated GameTests have no spare-tick budget, so drain again while still waiting.
          if (partialAt[0] < 0 && sinceRelease >= 250 && (sinceRelease - 250) % 200 == 0) {
            level.getChunkSource().save(true);
            System.out.println("FD_PAX_PHASE_FLUSH target=" + interruptedPhase + " native=true");
          }
          if (sinceRelease < 250) return;
          if (partialAt[0] >= 0 && resumedAt[0] < 0) {
            long loadedTrain =
                oldBodies.keySet().stream()
                    .filter(
                        id -> !id.equals(oldWinch.carriage) && container.getSubLevel(id) != null)
                    .count();
            helper.assertTrue(
                loadedTrain == 0 || loadedTrain == 12,
                "Native reload split coupled train/carrier bodies: " + loadedTrain);
            if (interruptedPhase == ShuttleWinchEntity.Phase.OUTBOUND
                && ((restoredAt[0] < 0 && (tick - partialAt[0]) % 20 == 0)
                    || (restoredAt[0] >= 0 && tick - restoredAt[0] <= 20)))
              System.out.println(
                  "FD_PAX_PHASE_NATIVE target="
                      + interruptedPhase
                      + " stage="
                      + (restoredAt[0] < 0 ? "partial" : "full")
                      + " age="
                      + (tick - (restoredAt[0] < 0 ? partialAt[0] : restoredAt[0]))
                      + " trainBodies="
                      + loadedTrain);
          }
          if (partialAt[0] >= 0 && restoredAt[0] < 0) {
            helper.assertTrue(
                !dev.fourthdimension.track.TrackGeometry.ticking(level, oldWinch.getBlockPos()),
                "Staggered reload accidentally made the winch tick");
            var carriage = container.getSubLevel(oldWinch.carriage);
            if (carriage == null) {
              helper.assertTrue(tick - partialAt[0] < 300, "Staggered carriage did not reload");
              return;
            }
            if (partialBodyAt[0] < 0) {
              partialBodyAt[0] = tick;
              partialPosition.set(carriage.logicalPose().position());
            }
            partialTravel[0] =
                Math.max(
                    partialTravel[0], carriage.logicalPose().position().distance(partialPosition));
            helper.assertTrue(
                partialTravel[0] < 1.5,
                "Carriage fell while the winch chunk was unavailable: " + partialTravel[0]);
            if (tick - partialBodyAt[0] < 100) return;
            System.out.println(
                "FD_PAX_PHASE_STAGGERED target="
                    + interruptedPhase
                    + " winchTicking=false carriage=true ticks=100 travel="
                    + partialTravel[0]);
            tickets.accept(true);
            restoredAt[0] = tick;
            return;
          }
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
              if (sinceRelease == 750 || sinceRelease == 1150)
                System.out.println(
                    "FD_PAX_PHASE_WAIT target="
                        + interruptedPhase
                        + " blocks="
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
                                        + ":chunk="
                                        + level
                                            .getChunkSource()
                                            .chunkMap
                                            .getChunkDebugData(
                                                new net.minecraft.world.level.ChunkPos(e.getKey()))
                                            .replace("\n", ";")
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
              helper.assertTrue(sinceRelease < 8000, "PAX fixture never physically evicted");
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
                "FD_PAX_PHASE_EVICTED target="
                    + interruptedPhase
                    + " tick="
                    + tick
                    + " blocks="
                    + oldBlocks.size()
                    + " bodies="
                    + oldBodies.size()
                    + " occupants=16 phase="
                    + oldWinch.phase
                    + " interrupted="
                    + oldWinch.interrupted);
            var savedPosition = oldBodies.get(oldWinch.carriage).logicalPose().position();
            var carriagePosition =
                BlockPos.containing(savedPosition.x(), savedPosition.y(), savedPosition.z());
            level.setChunkForced(carriagePosition.getX() >> 4, carriagePosition.getZ() >> 4, true);
            partialAt[0] = tick;
            return;
          }
          var reloadedCarriage = container.getSubLevel(oldWinch.carriage);
          if (reloadedCarriage != null && resumedAt[0] < 0) {
            var position = reloadedCarriage.logicalPose().position();
            if (!carriageSeen[0]) {
              restoredCarriagePosition.set(position);
              carriageSeen[0] = true;
            } else
              maxCarriageStep[0] =
                  Math.max(maxCarriageStep[0], position.distance(previousCarriagePosition));
            previousCarriagePosition.set(position);
            helper.assertTrue(
                maxCarriageStep[0] < 1.5,
                "Interrupted carriage jumped after reload: " + maxCarriageStep[0]);
            if (tick < restoredAt[0] + 100)
              initialCarriageTravel[0] =
                  Math.max(initialCarriageTravel[0], position.distance(restoredCarriagePosition));
            else if (tick == restoredAt[0] + 100) restoredCarriagePosition.set(position);
            else {
              carriageDrift[0] =
                  Math.max(carriageDrift[0], position.distance(restoredCarriagePosition));
              helper.assertTrue(
                  carriageDrift[0] < .3,
                  "Interrupted empty carriage moved after settling: " + carriageDrift[0]);
            }
          }
          helper.assertTrue(
              initialCarriageTravel[0] < 1.5,
              "Interrupted carriage left its reload settling envelope: "
                  + initialCarriageTravel[0]);
          var reloadedLead = container.getSubLevel(oldWinch.vehicle);
          if (reloadedLead != null && resumedAt[0] < 0) {
            var position = CoasterCartResolve.cartCenterWorld(reloadedLead);
            if (observedBody[0])
              maxReloadStep[0] = Math.max(maxReloadStep[0], position.distance(previousPosition));
            previousPosition.set(position);
            observedBody[0] = true;
            helper.assertTrue(
                maxReloadStep[0] < 1.5, "Reloaded PAX jumped between ticks: " + maxReloadStep[0]);
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
                w.phase == ShuttleWinchEntity.Phase.FAULT && w.interrupted == interruptedPhase,
                "PAX reloaded lift resumed without explicit authorization: "
                    + w.phase
                    + " "
                    + w.fault);
            verifyReloadedPassengers(helper, assignments);
            verifyTelemetry(helper, w);
            if (tick == restoredAt[0] + 100) {
              hold[0] = w.carriageDistance;
              holdPosition.set(
                  CoasterCartResolve.cartCenterWorld(container.getSubLevel(w.vehicle)));
              System.out.println(
                  "FD_PAX_PHASE_RESTORED target="
                      + interruptedPhase
                      + " fresh=true riders=16 phase="
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
            boolean gravity =
                interruptedPhase == ShuttleWinchEntity.Phase.OUTBOUND
                    || interruptedPhase == ShuttleWinchEntity.Phase.RETURNING;
            if (gravity) {
              try {
                w.resume();
                helper.fail("Interrupted gravity run accepted resume");
              } catch (IllegalArgumentException expected) {
                helper.assertTrue(
                    expected.getMessage().contains("manual recovery"),
                    "Gravity resume rejected for unrelated reason: " + expected.getMessage());
              }
              helper.assertTrue(
                  w.phase == ShuttleWinchEntity.Phase.FAULT && w.carriageDistance == hold[0],
                  "Gravity fault resumed or moved its carriage coordinate");
              if (!example.cars().stream().allMatch(v -> Boarding.ready(level, v))) {
                if ((tick - restoredAt[0]) % 1000 == 0) {
                  var route = ShuttleRoute.resolve(level, w.routeStart, w.routeEnd);
                  System.out.println(
                      "FD_PAX_PHASE_BRAKING target="
                          + interruptedPhase
                          + " sinceRestore="
                          + (tick - restoredAt[0])
                          + " cars="
                          + example.cars().stream()
                              .map(
                                  v -> {
                                    var body =
                                        (dev.ryanhcode.sable.sublevel.ServerSubLevel)
                                            container.getSubLevel(v.chassis);
                                    return v.chassis
                                        + ":distance="
                                        + route.locate(body).distance()
                                        + ":speed="
                                        + container
                                            .physicsSystem()
                                            .getPipeline()
                                            .getLinearVelocity(body, new org.joml.Vector3d())
                                            .length()
                                        + ":ready="
                                        + Boarding.ready(level, v);
                                  })
                              .toList());
                }
                helper.assertTrue(
                    tick < restoredAt[0] + 6000,
                    "Interrupted gravity train never became ready for manual recovery");
                return;
              }
            } else if (tick < restoredAt[0] + 160) return;
            double drift =
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(w.vehicle))
                    .distance(holdPosition);
            helper.assertTrue(
                (gravity || drift < .3) && w.carriageDistance == hold[0],
                "Reloaded PAX moved while held: " + drift);
            if (gravity) {
              w.recover();
            } else w.resume();
            resumedAt[0] = tick;
            System.out.println(
                "FD_PAX_PHASE_RECOVERY target="
                    + interruptedPhase
                    + " action="
                    + (gravity ? "recover" : "resume")
                    + " waitTicks="
                    + (tick - restoredAt[0] - 100)
                    + " drift="
                    + drift
                    + " initialCarriageTravel="
                    + initialCarriageTravel[0]
                    + " maxCarriageStep="
                    + maxCarriageStep[0]
                    + " carriageDrift="
                    + carriageDrift[0]
                    + " maxReloadStep="
                    + maxReloadStep[0]
                    + " phase="
                    + w.phase);
            return;
          }
          var w = live[0];
          phases.add(w.phase);
          verifyReloadedPassengers(helper, assignments);
          verifyTelemetry(helper, w);
          helper.assertTrue(
              !dev.fourthdimension.integration.ExternalDrivePolicy.allows(level, w.vehicle),
              "External drive gained PAX ownership after reload");
          helper.assertTrue(
              w.phase != ShuttleWinchEntity.Phase.FAULT, "Resumed PAX faulted: " + w.fault);
          if (w.cycles == 1 && w.phase == ShuttleWinchEntity.Phase.BOARDING) {
            if (completedAt[0] < 0) completedAt[0] = tick;
            if (tick - completedAt[0] < 20) return;
            helper.assertTrue(
                phases.contains(interruptedPhase)
                    && phases.contains(ShuttleWinchEntity.Phase.CAPTURING)
                    && phases.contains(ShuttleWinchEntity.Phase.BOARDING),
                "Reloaded PAX skipped required recovery phases");
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
                "FD_PAX_PHASE_COMPLETE target="
                    + interruptedPhase
                    + " cycles="
                    + w.cycles
                    + " cars=4 riders=16 alive=true unloaded=true telemetry=true phases="
                    + phases);
            tickets.accept(false);
            helper.succeed();
          }
        });
  }

  private static void verifyTelemetry(GameTestHelper helper, ShuttleWinchEntity winch) {
    var snapshot =
        dev.fourthdimension.integration.RideTelemetry.sample(helper.getLevel(), winch.vehicle);
    helper.assertTrue(
        snapshot.available()
            && snapshot.cars() == 4
            && snapshot.capacity() == 16
            && snapshot.occupied() == 16
            && snapshot.restraintsClosed()
            && snapshot.phase().equals(winch.phase.name()),
        "Reloaded PAX telemetry lost passengers, restraints or phase");
    if (winch.phase != ShuttleWinchEntity.Phase.BOARDING)
      helper.assertTrue(!snapshot.dispatchReady(), "Interrupted PAX telemetry granted dispatch");
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
}
