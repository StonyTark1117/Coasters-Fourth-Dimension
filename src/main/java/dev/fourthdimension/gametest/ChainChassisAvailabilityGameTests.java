package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.RideTelemetry;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelSerializer;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

/** Native chain speed matching must reject unavailable members of a logical 4D consist. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class ChainChassisAvailabilityGameTests {
  @GameTest(template = "empty", batch = "fourthdimension_chain_chassis_availability", timeoutTicks = 900)
  public static void nativeChainRejectsUnavailableConsistBodies(GameTestHelper helper) {
    var steps = new ChainAvailabilityProbe.Steps(helper);
    runCase(helper, steps, 0, new ArrayList<>(), new ProbeCounts());
  }

  private static void runCase(GameTestHelper helper, ChainAvailabilityProbe.Steps steps, int index,
      List<String> failures, ProbeCounts counts) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    if (index == 6) {
      helper.assertTrue(counts.unloads == 6 && counts.unavailable == 10
              && counts.positive == 20 && counts.held == 12,
          "Chain chassis scenario skipped a required unload or callback");
      System.out.println("FD_CHAIN_CHASSIS_AVAILABILITY complete nativeUnloads=" + counts.unloads
          + " unavailableCalls=" + counts.unavailable + " positiveCalls=" + counts.positive
          + " heldCalls=" + counts.held + " failures=" + failures);
      helper.assertTrue(failures.isEmpty(), "Unavailable-body chain velocity changes: " + failures);
      helper.succeed();
      return;
    }
    var origin = new BlockPos(-800 + index * 50, 120, -1000);
    for (int x = (origin.getX() - 8) >> 4; x <= (origin.getX() + 8) >> 4; x++)
      for (int z = (origin.getZ() - 4) >> 4; z <= (origin.getZ() + 52) >> 4; z++)
        level.setChunkForced(x, z, true);
    var fixture = RecoveryExamples.create(level, origin, TrackDesign.ARROW);
    // This group uses two 4D chassis, not the example's ordinary recovery bearing.
    var unusedBearingId = fixture.bearing().getUniqueId();
    container.removeSubLevel(fixture.bearing(), SubLevelRemovalReason.REMOVED);
    var rail = FoundationData.get(level).rails.stream()
        .filter(r -> r.matches(fixture.car().boundFrom, fixture.car().boundTo))
        .findFirst().orElseThrow();
    var cars = new ArrayList<FoundationData.Vehicle>();
    cars.add(fixture.car());
    steps.after(3, () -> {
      helper.assertTrue(container.getSubLevel(unusedBearingId) == null,
          "Unused ordinary bearing survived removal");
      var edge = TrackGeometry.resolve(level, rail);
      double t = TrackGeometry.table(level, edge).parameterAt(18);
      cars.add(VehicleFactory.create(level, TrackDesign.ARROW,
          new CoasterPathTrackFrame.GraphHit(
              CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
      ChainAvailabilityProbe.power(helper, fixture, origin);
    });
    steps.after(
        100,
        () -> {
          try {
            for (var car : cars) {
              helper.assertTrue(Boarding.ready(level, car), "Fresh availability fixture not ready");
              helper.assertTrue(car.carriers.size() == 2, "Expected two Arrow carriers per car");
            }
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.linkCarts(
                    level,
                    (ServerSubLevel) container.getSubLevel(cars.getFirst().chassis),
                    (ServerSubLevel) container.getSubLevel(cars.getLast().chassis),
                    6),
                "Fresh native coupling failed");
            helper.assertTrue(cars.size() == 2, "Expected both fresh Arrow chassis");
            for (int source = 0; source < cars.size(); source++) {
              helper.assertTrue(cars.get(source).stopping, "Held control lost its station brake");
              double held = probe(helper, cars, source, "held-before-" + index);
              counts.held++;
              if (!(held < 1e-9)) failures.add(index + "/held-source" + source + "=" + held);
            }
            VehicleOperations.restraints(level, cars.getFirst(), true);
            VehicleOperations.dispatch(level, cars.getFirst());
            for (int source = 0; source < cars.size(); source++) {
              helper.assertTrue(VehicleOperations.consist(level, cars.get(source)).size() == 2,
                  "Fresh fixture is not a two-car native consist");
              helper.assertTrue(probe(helper, cars, source, "healthy-before-" + index) > .001,
                  "Positive chain control failed before native unload");
              counts.positive++;
            }

            var affected = cars.get(index / 3);
            boolean chassisCase = index % 3 == 2;
            var id = chassisCase ? affected.chassis : affected.carriers.get(index % 3).body;
            var ids = new ArrayList<UUID>();
            for (var car : cars) {
              ids.add(car.chassis);
              car.carriers.forEach(c -> ids.add(c.body));
            }
            var body = (ServerSubLevel) container.getSubLevel(id);
            helper.assertTrue(body != null && !body.isRemoved(), "Unload fixture body unavailable");
            // This isolates an actual native unload callback window, not a natural chunk cycle.
            var saved = SubLevelSerializer.toData(body, ids);
            System.out.println(
                "FD_CHAIN_CHASSIS_AVAILABILITY unload case=" + index + " body=" + id
                    + " role=" + (chassisCase ? "chassis" : "carrier") + " reason=UNLOADED");
            container.removeSubLevel(body, SubLevelRemovalReason.UNLOADED);
            try {
              helper.assertTrue(container.getSubLevel(id) == null && body.isRemoved(),
                  "Native UNLOADED path did not remove the body from the container");
              counts.unloads++;
              for (var survivingId : ids)
                if (!survivingId.equals(id)) {
                  var surviving = container.getSubLevel(survivingId);
                  helper.assertTrue(surviving != null && !surviving.isRemoved(),
                      "An extra missing body invalidates the single-component chain probe");
                }
              for (var car : cars) {
                helper.assertTrue(FoundationData.get(level).vehicles.get(car.chassis) == car,
                    "Transient unload removed the logical vehicle");
                helper.assertTrue(
                    !car.stopping && !car.maintenance && car.restraints && car.fault.isEmpty(),
                    "Transient unload changed an interlock flag instead of only availability");
              }
              int caseCalls = 0;
              for (int source = 0; source < cars.size(); source++) {
                if (container.getSubLevel(cars.get(source).chassis) == null) {
                  helper.assertTrue(chassisCase && cars.get(source).chassis.equals(id),
                      "Unexpected absent chain source would skip a required probe");
                  continue;
                }
                helper.assertTrue(VehicleOperations.consist(level, cars.get(source)).size() == 2,
                    "Native unload lost the peer identity before the chain probe");
                helper.assertTrue(!RideTelemetry.sample(level, cars.get(source).chassis).available(),
                    "Telemetry did not expose the missing physical body");
                double impulse = probe(helper, cars, source, "unloaded-" + index);
                caseCalls++;
                counts.unavailable++;
                if (!(impulse < 1e-9)) failures.add(index + "/source" + source + "=" + impulse);
              }
              helper.assertTrue(caseCalls == (chassisCase ? 1 : 2),
                  "Availability case did not execute every required surviving chain source");
            } finally {
              var restored = SubLevelSerializer.fullyLoad(level, saved);
              helper.assertTrue(
                  restored != null && !restored.isRemoved() && restored.getUniqueId().equals(id)
                      && container.getSubLevel(id) == restored,
                  "Native serializer failed to restore the same body identity");
              System.out.println("FD_CHAIN_CHASSIS_AVAILABILITY restored case=" + index + " body=" + id);
            }
            // Let native restoration initialize. The direct probe normalizes/restores velocity,
            // so a healthy source already at the actual chain target remains a valid control.
            steps.after(
                3,
                () -> {
                  try {
                    for (var bodyId : ids) {
                      var restored = container.getSubLevel(bodyId);
                      helper.assertTrue(restored != null && !restored.isRemoved(),
                          "A restored native body is still unavailable");
                    }
                    var linkSizes = cars.stream()
                        .map(car -> VehicleOperations.consist(level, car).size()).toList();
                    System.out.println("FD_CHAIN_CHASSIS_AVAILABILITY restoredLinks case=" + index
                        + " sizes=" + linkSizes + " partialChassisRestore=" + chassisCase);
                    if (!chassisCase)
                      helper.assertTrue(linkSizes.stream().allMatch(size -> size == 2),
                          "Carrier restore lost native train-link continuity");
                    for (var car : cars) {
                      helper.assertTrue(FoundationData.get(level).vehicles.get(car.chassis) == car
                              && !car.stopping && !car.maintenance && car.restraints
                              && car.fault.isEmpty(),
                          "Restoration changed logical vehicle identity or interlock flags");
                      helper.assertTrue(RideTelemetry.sample(level, car.chassis).available(),
                          "Restored native vehicle is still unavailable");
                    }
                    // A single-chassis serializer restore does not prove train-link recovery.
                    // Its case ends with body identity/availability verified, then full cleanup.
                    // Only carrier restores preserve the two-car positive-control condition.
                    if (!chassisCase) {
                      for (int source = 0; source < cars.size(); source++) {
                        helper.assertTrue(probe(helper, cars, source, "restored-" + index) > .001,
                            "Healthy chain velocity did not resume after carrier restoration");
                        counts.positive++;
                      }
                    }
                  } finally {
                    cleanupCars(helper, cars, fixture, rail, origin);
                  }
                  steps.after(3, () -> {
                    for (var bodyId : ids)
                      helper.assertTrue(container.getSubLevel(bodyId) == null,
                          "Previous native body survived fixture cleanup");
                    for (var car : cars)
                      helper.assertTrue(!FoundationData.get(level).vehicles.containsKey(car.chassis),
                          "Previous logical vehicle survived fixture cleanup");
                    runCase(helper, steps, index + 1, failures, counts);
                  });
                });
          } catch (RuntimeException | Error failure) {
            cleanupCars(helper, cars, fixture, rail, origin);
            throw failure;
          }
        });
  }

  private static void cleanupCars(GameTestHelper helper, List<FoundationData.Vehicle> cars,
      RecoveryExamples.Example fixture, FoundationData.Rail rail, BlockPos origin) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    ChainAvailabilityProbe.stop(helper, fixture);
    for (var car : cars) {
      car.stopping = true;
      var ids = new ArrayList<UUID>();
      ids.add(car.chassis);
      car.carriers.forEach(c -> ids.add(c.body));
      for (var id : ids) {
        var body = container.getSubLevel(id);
        if (body != null && !body.isRemoved())
          container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      }
      FoundationData.get(level).vehicles.remove(car.chassis);
    }
    FoundationData.get(level).rails.remove(rail);
    for (var pos : BlockPos.betweenClosed(origin.offset(-8, -1, -4), origin.offset(8, 12, 52)))
      if (!level.getBlockState(pos).isAir()) level.removeBlock(pos, false);
    for (int x = (origin.getX() - 8) >> 4; x <= (origin.getX() + 8) >> 4; x++)
      for (int z = (origin.getZ() - 4) >> 4; z <= (origin.getZ() + 52) >> 4; z++)
        level.setChunkForced(x, z, false);
  }

  private static final class ProbeCounts {
    int unloads;
    int unavailable;
    int positive;
    int held;
  }

  private static double probe(GameTestHelper helper, List<FoundationData.Vehicle> cars,
      int source, String state) {
    var container = SubLevelContainer.getContainer(helper.getLevel());
    var ids = new ArrayList<UUID>();
    for (var car : cars) {
      ids.add(car.chassis);
      car.carriers.forEach(c -> ids.add(c.body));
    }
    int index = Integer.parseInt(state.substring(state.lastIndexOf('-') + 1));
    return ChainAvailabilityProbe.measure(helper,
        (ServerSubLevel) container.getSubLevel(cars.get(source).chassis), ids,
        "chassis", index, Integer.toString(source), state);
  }
}
