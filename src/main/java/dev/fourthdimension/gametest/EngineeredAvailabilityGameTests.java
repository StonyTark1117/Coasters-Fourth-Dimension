package dev.fourthdimension.gametest;

import static dev.fourthdimension.gametest.EngineeredGameTests.*;

import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.integration.RideTelemetry;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelSerializer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Real native unload/reload callbacks with logical vehicle and interlock state retained. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredAvailabilityGameTests {
  @GameTest(template = "empty", batch = "fourthdimension_engineered_availability", timeoutTicks = 800)
  public static void nativeMotorRejectsTransientlyUnavailableBodies(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_AVAILABILITY absent/uninspected: scenario not exercised");
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var from = new BlockPos(-960, 120, -960);
    var to = from.offset(0, 0, 48);
    for (int x = -62; x <= -58; x++)
      for (int z = -62; z <= -56; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000416"), "FDAvailability"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    for (var pos : List.of(from, to))
      level.setBlockAndUpdate(
          pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    var rail =
        TrackPlacement.connect(
            level, from, to, TrackDesign.ARROW, player, player.getMainHandItem());
    var controllerPos = from.offset(-6, 0, 0);
    level.setBlockAndUpdate(
        controllerPos,
        BuiltInRegistries.BLOCK
            .get(ResourceLocation.parse("coastersengineered:linear_motor_controller"))
            .defaultBlockState());
    var failures = new ArrayList<String>();
    var counts = new ProbeCounts();
    helper.runAtTickTime(
        15,
        () -> {
          configureMotor(helper, rail, controllerPos, 10, "FORWARD");
          runCase(helper, rail, from, to, controllerPos, 0, failures, counts);
        });
  }

  private static void runCase(
      GameTestHelper helper,
      FoundationData.Rail rail,
      BlockPos from,
      BlockPos to,
      BlockPos controllerPos,
      int index,
      List<String> failures,
      ProbeCounts counts) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var controller = level.getBlockEntity(controllerPos);
    if (index == 6) {
      FoundationData.get(level).rails.remove(rail);
      for (var pos : List.of(from, to, controllerPos)) level.removeBlock(pos, false);
      for (int x = -62; x <= -58; x++)
        for (int z = -62; z <= -56; z++) level.setChunkForced(x, z, false);
      helper.assertTrue(counts.unloads == 6 && counts.unavailable == 10 && counts.positive == 24,
          "Availability scenario skipped a required native unload or motor probe");
      System.out.println(
          "FD_ENGINEERED_AVAILABILITY complete nativeUnloads=" + counts.unloads
              + " unavailableCalls=" + counts.unavailable + " positiveCalls=" + counts.positive
              + " failures=" + failures);
      helper.assertTrue(failures.isEmpty(), "Native unavailable-body motor impulses: " + failures);
      helper.succeed();
      return;
    }

    // Each case starts with a newly assembled native train. Partial chassis restoration can
    // tear down an old native link on a later tick; never reuse that consist for another case.
    setMode(controller, "COAST", 0);
    var cars = new ArrayList<FoundationData.Vehicle>();
    var edge = TrackGeometry.resolve(level, rail);
    for (double distance : new double[] {20, 14}) {
      double t = edge.from().equals(from) ? distance / 48 : 1 - distance / 48;
      cars.add(
          VehicleFactory.create(
              level,
              TrackDesign.ARROW,
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
    }
    helper.runAfterDelay(
        85,
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
            VehicleOperations.restraints(level, cars.getFirst(), true);
            VehicleOperations.dispatch(level, cars.getFirst());
            setMode(controller, "LAUNCH", 10);
            for (int source = 0; source < cars.size(); source++) {
              helper.assertTrue(VehicleOperations.consist(level, cars.get(source)).size() == 2,
                  "Fresh fixture is not a two-car native consist");
              helper.assertTrue(probe(helper, cars, source, "healthy-before-" + index) > .001,
                  "Positive motor control failed before native unload");
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
                "FD_ENGINEERED_AVAILABILITY unload case=" + index + " body=" + id
                    + " role=" + (chassisCase ? "chassis" : "carrier") + " reason=UNLOADED");
            container.removeSubLevel(body, SubLevelRemovalReason.UNLOADED);
            try {
              helper.assertTrue(container.getSubLevel(id) == null && body.isRemoved(),
                  "Native UNLOADED path did not remove the body from the container");
              counts.unloads++;
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
                      "Unexpected absent motor source would skip a required probe");
                  continue;
                }
                helper.assertTrue(VehicleOperations.consist(level, cars.get(source)).size() == 2,
                    "Native unload lost the peer identity before the motor probe");
                helper.assertTrue(!RideTelemetry.sample(level, cars.get(source).chassis).available(),
                    "Telemetry did not expose the missing physical body");
                double impulse = probe(helper, cars, source, "unloaded-" + index);
                caseCalls++;
                counts.unavailable++;
                if (!(impulse < 1e-9)) failures.add(index + "/source" + source + "=" + impulse);
              }
              helper.assertTrue(caseCalls == (chassisCase ? 1 : 2),
                  "Availability case did not execute every required surviving motor source");
            } finally {
              var restored = SubLevelSerializer.fullyLoad(level, saved);
              helper.assertTrue(
                  restored != null && !restored.isRemoved() && restored.getUniqueId().equals(id)
                      && container.getSubLevel(id) == restored,
                  "Native serializer failed to restore the same body identity");
              System.out.println("FD_ENGINEERED_AVAILABILITY restored case=" + index + " body=" + id);
            }
            // Let native physics initialize without reaching target speed between direct probes.
            setMode(controller, "COAST", 0);
            helper.runAfterDelay(
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
                    System.out.println("FD_ENGINEERED_AVAILABILITY restoredLinks case=" + index
                        + " sizes=" + linkSizes + " partialChassisRestore=" + chassisCase);
                    if (chassisCase && linkSizes.stream().anyMatch(size -> size != 2)) {
                      helper.assertTrue(linkSizes.stream().allMatch(size -> size == 1),
                          "Partial chassis restore left ambiguous native link state");
                      // Explicit fixture reset, not proof of natural train-link restoration.
                      helper.assertTrue(
                          CoasterCartTrainLinkConstraint.linkCarts(
                              level,
                              (ServerSubLevel) container.getSubLevel(cars.getFirst().chassis),
                              (ServerSubLevel) container.getSubLevel(cars.getLast().chassis), 6),
                          "Could not rebuild the native link for the restored positive control");
                      System.out.println(
                          "FD_ENGINEERED_AVAILABILITY fixtureRelink case=" + index);
                    }
                    for (var car : cars) {
                      helper.assertTrue(VehicleOperations.consist(level, car).size() == 2,
                          "Restored positive control requires the original two-car membership");
                      helper.assertTrue(FoundationData.get(level).vehicles.get(car.chassis) == car
                              && !car.stopping && !car.maintenance && car.restraints
                              && car.fault.isEmpty(),
                          "Restoration changed logical vehicle identity or interlock flags");
                    }
                    setMode(controller, "LAUNCH", 10);
                    for (int source = 0; source < cars.size(); source++) {
                      helper.assertTrue(RideTelemetry.sample(level, cars.get(source).chassis).available(),
                          "Restored native vehicle is still unavailable");
                      helper.assertTrue(probe(helper, cars, source, "restored-" + index) > .001,
                          "Healthy propulsion did not resume after body restoration");
                      counts.positive++;
                    }
                  } finally {
                    setMode(controller, "COAST", 0);
                    cleanupCars(helper, cars);
                  }
                  helper.runAfterDelay(3, () -> {
                    for (var bodyId : ids)
                      helper.assertTrue(container.getSubLevel(bodyId) == null,
                          "Previous native body survived fixture cleanup");
                    for (var car : cars)
                      helper.assertTrue(!FoundationData.get(level).vehicles.containsKey(car.chassis),
                          "Previous logical vehicle survived fixture cleanup");
                    runCase(helper, rail, from, to, controllerPos, index + 1, failures, counts);
                  });
                });
          } catch (RuntimeException | Error failure) {
            cleanupCars(helper, cars);
            throw failure;
          }
        });
  }

  private static void cleanupCars(GameTestHelper helper, List<FoundationData.Vehicle> cars) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    for (var car : cars) {
      car.stopping = true;
      for (var carrier : car.carriers) {
        var child = container.getSubLevel(carrier.body);
        if (child != null && !child.isRemoved())
          container.removeSubLevel(child, SubLevelRemovalReason.REMOVED);
      }
      var chassis = container.getSubLevel(car.chassis);
      if (chassis != null && !chassis.isRemoved())
        container.removeSubLevel(chassis, SubLevelRemovalReason.REMOVED);
      FoundationData.get(level).vehicles.remove(car.chassis);
    }
  }

  private static final class ProbeCounts {
    int unloads;
    int unavailable;
    int positive;
  }

  private static double probe(
      GameTestHelper helper,
      List<FoundationData.Vehicle> cars,
      int source,
      String state) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var physics = container.physicsSystem();

    var bodies = new ArrayList<ServerSubLevel>();
    for (var car : cars) {
      var chassis = (ServerSubLevel) container.getSubLevel(car.chassis);
      if (chassis != null) bodies.add(chassis);
      for (var carrier : car.carriers) {
        var child = (ServerSubLevel) container.getSubLevel(carrier.body);
        if (child != null) bodies.add(child);
      }
    }
    var totals =
        bodies.stream()
            .map(
                b ->
                    b.getOrCreateQueuedForceGroup(
                            dev.ryanhcode.sable.api.physics.force.ForceGroups.PROPULSION.get())
                        .getForceTotal())
            .toList();
    var before = totals.stream().map(t -> new Vector3d(t.getLocalForce())).toList();
    var torques = totals.stream().map(t -> new Vector3d(t.getLocalTorque())).toList();
    var body = (ServerSubLevel) container.getSubLevel(cars.get(source).chassis);
    var center = CoasterCartResolve.cartCenterWorld(body);
    var hit =
        CoasterPathTrackFrame.nearestGraphHit(
            level,
            CoasterPathGraphManager.get(level),
            new net.minecraft.world.phys.Vec3(center.x, center.y, center.z),
            2);
    helper.assertTrue(hit != null, "Interlock fixture lost track hit");
    double maximum = 0;
    try {
      invoke(
          type("MotorDrive"), "apply", body, level, physics, hit,
          CoasterPathTrackFrame.trackBasisAtHit(level, hit), .05d);
      for (int i = 0; i < bodies.size(); i++) {
        var total = totals.get(i);
        maximum = Math.max(maximum, total.getLocalForce().distance(before.get(i)));
        maximum = Math.max(maximum, total.getLocalTorque().distance(torques.get(i)));
      }
    } finally {
      for (int i = 0; i < bodies.size(); i++) {
        totals.get(i).getLocalForce().set(before.get(i));
        totals.get(i).getLocalTorque().set(torques.get(i));
      }
    }
    System.out.println(
        "FD_ENGINEERED_AVAILABILITY source="
            + source
            + " state="
            + state
            + " maxQueuedDelta="
            + maximum);
    return maximum;
  }
}
