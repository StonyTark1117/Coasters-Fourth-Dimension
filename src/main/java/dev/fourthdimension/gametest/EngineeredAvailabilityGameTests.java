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
  @GameTest(template = "empty", batch = "fourthdimension_engineered_availability", timeoutTicks = 240)
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
    var cars = new ArrayList<FoundationData.Vehicle>();
    helper.runAtTickTime(
        15,
        () -> {
          configureMotor(helper, rail, controllerPos, 10, "FORWARD");
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
        });
    helper.runAtTickTime(
        100,
        () -> {
          for (var car : cars)
            helper.assertTrue(Boarding.ready(level, car), "Interlock fixture not ready");
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.linkCarts(
                  level,
                  (ServerSubLevel) container.getSubLevel(cars.getFirst().chassis),
                  (ServerSubLevel) container.getSubLevel(cars.getLast().chassis),
                  6),
              "Native coupling failed");
          VehicleOperations.restraints(level, cars.getFirst(), true);
          VehicleOperations.dispatch(level, cars.getFirst());
          var ids = new ArrayList<UUID>();
          for (var car : cars) {
            car.carriers.forEach(c -> ids.add(c.body));
            ids.add(car.chassis);
          }
          var failures = new ArrayList<String>();
          for (int source = 0; source < 2; source++)
            helper.assertTrue(probe(helper, cars, source, "healthy-before") > .001,
                "Positive motor control failed before native unload");
          exerciseUnavailable(helper, cars, ids, 0, failures, level.getBlockEntity(controllerPos), () -> {
            for (var car : cars) {
              car.stopping = true;
              for (var carrier : car.carriers) {
                var body = container.getSubLevel(carrier.body);
                if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
              }
              var body = container.getSubLevel(car.chassis);
              if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
              FoundationData.get(level).vehicles.remove(car.chassis);
            }
            FoundationData.get(level).rails.remove(rail);
            for (var pos : List.of(from, to, controllerPos)) level.removeBlock(pos, false);
            for (int x = -62; x <= -58; x++)
              for (int z = -62; z <= -56; z++) level.setChunkForced(x, z, false);
            System.out.println("FD_ENGINEERED_AVAILABILITY complete nativeUnloads=" + ids.size()
                + " failures=" + failures);
            helper.assertTrue(failures.isEmpty(), "Native unavailable-body motor impulses: " + failures);
            helper.succeed();
          });
        });
  }

  private static void exerciseUnavailable(
      GameTestHelper helper, List<FoundationData.Vehicle> cars, List<UUID> ids,
      int index, List<String> failures, Object controller, Runnable complete) {
    if (index == ids.size()) {
      complete.run();
      return;
    }
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var id = ids.get(index);
    var body = (ServerSubLevel) container.getSubLevel(id);
    helper.assertTrue(body != null && !body.isRemoved(), "Unload fixture body unavailable");
    // Use Sable's serializer and real UNLOADED path, not deletion of logical state or UUID edits.
    // Removing one body isolates the callback window; it does not simulate a whole chunk cycle.
    var saved = SubLevelSerializer.toData(body, ids);
    System.out.println(
        "FD_ENGINEERED_AVAILABILITY unload case=" + index + " body=" + id
            + " role=" + (cars.stream().anyMatch(c -> c.chassis.equals(id)) ? "chassis" : "carrier")
            + " reason=UNLOADED");
    container.removeSubLevel(body, SubLevelRemovalReason.UNLOADED);
    try {
      helper.assertTrue(container.getSubLevel(id) == null && body.isRemoved(),
          "Native UNLOADED path did not remove the body from the container");
      for (var car : cars) {
        helper.assertTrue(FoundationData.get(level).vehicles.get(car.chassis) == car,
            "Transient unload removed the logical vehicle");
        helper.assertTrue(!car.stopping && !car.maintenance && car.restraints && car.fault.isEmpty(),
            "Transient unload changed an interlock flag; test would not isolate availability");
      }
      for (int source = 0; source < cars.size(); source++) {
        if (container.getSubLevel(cars.get(source).chassis) == null) continue;
        helper.assertTrue(VehicleOperations.consist(level, cars.get(source)).size() == 2,
            "Native unload lost the peer identity instead of retaining an unavailable consist");
        helper.assertTrue(!RideTelemetry.sample(level, cars.get(source).chassis).available(),
            "Telemetry did not expose the missing physical body");
        double impulse = probe(helper, cars, source, "unloaded-" + index);
        if (!(impulse < 1e-9)) failures.add(index + "/source" + source + "=" + impulse);
      }
    } finally {
      var restored = SubLevelSerializer.fullyLoad(level, saved);
      helper.assertTrue(restored != null && !restored.isRemoved()
              && restored.getUniqueId().equals(id) && container.getSubLevel(id) == restored,
          "Native serializer failed to restore the same body identity");
      System.out.println("FD_ENGINEERED_AVAILABILITY restored case=" + index + " body=" + id);
    }
    // Initialize restored chunks/physics without accelerating to the target between probes.
    // Only the fixture controller changes mode; vehicle readiness flags remain untouched.
    setMode(controller, "COAST", 0);
    helper.runAfterDelay(3, () -> {
      setMode(controller, "LAUNCH", 10);
      for (int source = 0; source < cars.size(); source++) {
        helper.assertTrue(RideTelemetry.sample(level, cars.get(source).chassis).available(),
            "Restored native consist is still unavailable");
        helper.assertTrue(probe(helper, cars, source, "restored-" + index) > .001,
            "Healthy propulsion did not resume after restoration");
      }
      exerciseUnavailable(helper, cars, ids, index + 1, failures, controller, complete);
    });
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
