package dev.fourthdimension.gametest;

import static dev.fourthdimension.gametest.EngineeredGameTests.*;

import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
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

/** Direct native motor calls isolate adapter decisions from the following physics tick. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredInterlockGameTests {
  @GameTest(template = "empty", batch = "fourthdimension_engineered_interlock", timeoutTicks = 160)
  public static void nativeMotorHonorsEveryLinkedCarInterlock(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_INTERLOCK absent/uninspected: scenario not exercised");
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var from = new BlockPos(-1000, 120, -800);
    var to = from.offset(0, 0, 48);
    for (int x = -64; x <= -61; x++)
      for (int z = -51; z <= -46; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000415"), "FDInterlock"));
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
          for (int source = 0; source < 2; source++) {
            probe(helper, cars, source, "healthy-before", false);
            for (int affected = 0; affected < 2; affected++) {
              var car = cars.get(affected);
              for (String state : List.of("stopping", "maintenance", "restraints", "fault")) {
                switch (state) {
                  case "stopping" -> car.stopping = true;
                  case "maintenance" -> car.maintenance = true;
                  case "restraints" -> car.restraints = false;
                  case "fault" -> car.fault = "Injected motor interlock fault";
                }
                try {
                  probe(helper, cars, source, state + "-car" + affected, true);
                } finally {
                  car.stopping = false;
                  car.maintenance = false;
                  car.restraints = true;
                  car.fault = "";
                }
              }
            }
            var peer = cars.get(1 - source);
            FoundationData.get(level).vehicles.remove(peer.chassis);
            try {
              probe(helper, cars, source, "missing-peer", true);
            } finally {
              FoundationData.get(level).vehicles.put(peer.chassis, peer);
            }
            probe(helper, cars, source, "healthy-after", false);
          }
          for (var car : cars) {
            car.stopping = true;
            for (var carrier : car.carriers)
              container.removeSubLevel(
                  container.getSubLevel(carrier.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(car.chassis), SubLevelRemovalReason.REMOVED);
            FoundationData.get(level).vehicles.remove(car.chassis);
          }
          FoundationData.get(level).rails.remove(rail);
          for (var pos : List.of(from, to, controllerPos)) level.removeBlock(pos, false);
          for (int x = -64; x <= -61; x++)
            for (int z = -51; z <= -46; z++) level.setChunkForced(x, z, false);
          System.out.println(
              "FD_ENGINEERED_INTERLOCK complete blocked=18 positive=4 nativeCalls=22");
          helper.succeed();
        });
  }

  private static void probe(
      GameTestHelper helper,
      List<FoundationData.Vehicle> cars,
      int source,
      String state,
      boolean blocked) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var physics = container.physicsSystem();

    var bodies = new ArrayList<ServerSubLevel>();
    for (var car : cars) {
      bodies.add((ServerSubLevel) container.getSubLevel(car.chassis));
      for (var carrier : car.carriers)
        bodies.add((ServerSubLevel) container.getSubLevel(carrier.body));
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
    invoke(
        type("MotorDrive"),
        "apply",
        body,
        level,
        physics,
        hit,
        CoasterPathTrackFrame.trackBasisAtHit(level, hit),
        .05d);
    double maximum = 0;
    for (int i = 0; i < bodies.size(); i++) {
      var total = totals.get(i);
      maximum = Math.max(maximum, total.getLocalForce().distance(before.get(i)));
      total.getLocalForce().set(before.get(i));
      total.getLocalTorque().set(torques.get(i));
    }
    System.out.println(
        "FD_ENGINEERED_INTERLOCK source="
            + source
            + " state="
            + state
            + " blocked="
            + blocked
            + " maxImpulse="
            + maximum);
    helper.assertTrue(
        blocked ? maximum < 1e-9 : maximum > .001,
        "Native motor interlock mismatch: " + source + "/" + state + " impulse=" + maximum);
  }
}
