package dev.fourthdimension.gametest;

import static dev.fourthdimension.gametest.EngineeredGameTests.*;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.integration.EngineeredInterlock;
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

  /** Probe every chassis in a four-car PAX consist while a rear carrier seat is occupied. */
  @GameTest(template = "empty", batch = "fourthdimension_engineered_interlock", timeoutTicks = 160)
  public static void nativeMotorRejectsEachOccupiedPaxCar(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_PAX_INTERLOCK absent/uninspected: scenario not exercised");
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var from = new BlockPos(-760, 120, -560);
    var to = from.offset(0, 0, 48);
    for (int x = -49; x <= -46; x++)
      for (int z = -36; z <= -31; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000416"), "FDPaxInterlock"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.PAX.get().getDefaultInstance());
    for (var pos : List.of(from, to))
      level.setBlockAndUpdate(
          pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    var rail =
        TrackPlacement.connect(level, from, to, TrackDesign.PAX, player, player.getMainHandItem());
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
          for (double distance : new double[] {32, 26, 20, 14}) {
            double t = edge.from().equals(from) ? distance / 48 : 1 - distance / 48;
            cars.add(
                VehicleFactory.create(
                    level,
                    TrackDesign.PAX,
                    new CoasterPathTrackFrame.GraphHit(
                        CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
          }
        });
    helper.runAtTickTime(
        100,
        () -> {
          for (var car : cars)
            helper.assertTrue(Boarding.ready(level, car), "PAX interlock fixture not ready");
          for (int i = 1; i < cars.size(); i++)
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.linkCarts(
                    level,
                    (ServerSubLevel) container.getSubLevel(cars.get(i - 1).chassis),
                    (ServerSubLevel) container.getSubLevel(cars.get(i).chassis),
                    6),
                "PAX interlock native coupling failed at pair " + (i - 1) + "/" + i);
          helper.assertTrue(
              VehicleOperations.consist(level, cars.getFirst()).size() == 4,
              "PAX interlock fixture did not form a four-car consist");
          var carrier = cars.getLast().carriers.getFirst();
          var body = container.getSubLevel(carrier.body);
          var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
          pig.setNoAi(true);
          var position = container.getSubLevel(cars.getLast().chassis).logicalPose().position();
          pig.setPos(position.x(), position.y(), position.z());
          level.addFreshEntity(pig);
          SeatBlock.sitDown(
              level, body.getPlot().getCenterBlock().offset(carrier.seats.getFirst()), pig);
          helper.assertTrue(pig.isPassenger(), "PAX interlock seat was not occupied");
          for (var car : cars) {
            car.stopping = false;
            car.maintenance = false;
            car.restraints = true;
            car.fault = "";
          }
          for (int source = 0; source < cars.size(); source++) {
            var selected = cars.get(source);
            EngineeredInterlock.consumePaxMotorHold(selected.chassis);
            probe(helper, cars, source, "occupied-pax-car", true);
            helper.assertTrue(
                EngineeredInterlock.consumePaxMotorHold(selected.chassis),
                "PAX car did not reach the native motor guard callback");
          }
          helper.assertTrue(pig.isPassenger(), "Native motor probe dropped the PAX rider");
          pig.discard();
          for (var car : cars) {
            car.stopping = true;
            for (var c : car.carriers)
              container.removeSubLevel(
                  container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(car.chassis), SubLevelRemovalReason.REMOVED);
            FoundationData.get(level).vehicles.remove(car.chassis);
          }
          FoundationData.get(level).rails.remove(rail);
          for (var pos : List.of(from, to, controllerPos)) level.removeBlock(pos, false);
          for (int x = -49; x <= -46; x++)
            for (int z = -36; z <= -31; z++) level.setChunkForced(x, z, false);
          System.out.println("FD_ENGINEERED_PAX_INTERLOCK complete cars=4 blocked=4 traced=4 nativeCalls=4");
          helper.succeed();
        });
  }

  /**
   * Recovery keeps the ordinary bearing under its own native coupling policy: an installed
   * Engineered controller must not bypass a brake, but may supply force once the operator has
   * explicitly started the retained consist.
   */
  @GameTest(template = "empty", batch = "fourthdimension_engineered_interlock", timeoutTicks = 220)
  public static void engineeredMotorRespectsRecoveryBrakeAndExplicitTow(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_RECOVERY absent/uninspected: scenario not exercised");
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var origin = new BlockPos(-900, 120, -700);
    for (int x = -58; x <= -55; x++)
      for (int z = -45; z <= -40; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000416"), "FDRecoveryEngineered"));
    player.setGameMode(GameType.CREATIVE);
    var recovery = RecoveryExamples.create(level, origin, TrackDesign.ARROW);
    Vector3d[] brakedStart = {null};
    Vector3d[] towingStart = {null};
    double[] brakedDistance = {0};
    double[] brakedImpulse = {0};
    double[] activeImpulse = {0};
    var rail =
        FoundationData.get(level).rails.stream()
            .filter(candidate -> candidate.matches(recovery.car().boundFrom, recovery.car().boundTo))
            .findFirst()
            .orElseThrow();
    var controllerPos = origin.west(6);
    level.setBlockAndUpdate(
        controllerPos,
        BuiltInRegistries.BLOCK
            .get(ResourceLocation.parse("coastersengineered:linear_motor_controller"))
            .defaultBlockState());
    helper.runAtTickTime(
        100,
        () -> {
          helper.assertTrue(
              Boarding.ready(level, recovery.car()), "Recovery fixture failed initial alignment");
          VehicleOperations.restraints(level, recovery.car(), true);
          VehicleOperations.retention(level, recovery.car(), true);
          var cartCenter = CoasterCartResolve.cartCenterWorld(recovery.bearing());
          var chassisCenter =
              CoasterCartResolve.cartCenterWorld(
                  (ServerSubLevel) container.getSubLevel(recovery.car().chassis));
          double spacing = cartCenter.distance(chassisCenter);
          helper.assertTrue(
              Math.abs(spacing - TrackDesign.ARROW.axleSpacing()) < .25,
              "Recovery fixture bearing spacing before Engineered configuration was " + spacing);
          player.setPos(cartCenter.x, cartCenter.y, cartCenter.z);
          RecoveryOperations.select(player, recovery.bearing());
          RecoveryOperations.attach(player, recovery.car());
          helper.assertTrue(
              recovery.car().stopping
                  && recovery.car().recoveryCart.equals(recovery.bearing().getUniqueId())
                  && CoasterCartTrainLinkConstraint.areLinkedCarts(
                      (ServerSubLevel) container.getSubLevel(recovery.car().chassis), recovery.bearing()),
              "Recovery fixture did not form a retained native group");
        });
    helper.runAtTickTime(
        130,
        () -> {
          // Do not energize a free ordinary cart: normal recovery coupling intentionally requires
          // the bearing to be stationary at the certified axle spacing. Give the new native link
          // time to settle before registering the installed controller.
          configureMotor(helper, rail, controllerPos, 10, "FORWARD");
          brakedStart[0] = new Vector3d(CoasterCartResolve.cartCenterWorld(recovery.bearing()));
        });
    helper.runAtTickTime(
        150,
        () -> {
          brakedDistance[0] =
              CoasterCartResolve.cartCenterWorld(recovery.bearing()).distance(brakedStart[0]);
          helper.assertTrue(
              brakedDistance[0] < .01,
              "Installed Engineered controller moved the recovery bearing before Start tow: "
                  + brakedDistance[0]);
          brakedImpulse[0] = motorImpulse(level, recovery.bearing());
          helper.assertTrue(
              brakedImpulse[0] < 1e-9,
              "Engineered motor bypassed the explicit recovery brake: " + brakedImpulse[0]);
          RecoveryOperations.start(level, recovery.car());
          activeImpulse[0] = motorImpulse(level, recovery.bearing());
          helper.assertTrue(
              activeImpulse[0] > .001,
              "Engineered motor did not supply the explicitly started recovery bearing: "
                  + activeImpulse[0]);
          towingStart[0] = new Vector3d(CoasterCartResolve.cartCenterWorld(recovery.bearing()));
        });
    helper.runAtTickTime(
        170,
        () -> {
          double towingDistance =
              CoasterCartResolve.cartCenterWorld(recovery.bearing()).distance(towingStart[0]);
          helper.assertTrue(
              towingDistance > brakedDistance[0] + .01,
              "Installed Engineered controller did not initiate recovery movement after Start tow: "
                  + towingDistance
                  + " (braked baseline "
                  + brakedDistance[0]
                  + ")");
          RecoveryOperations.brake(level, recovery.car());
          double rebrakedImpulse = motorImpulse(level, recovery.bearing());
          helper.assertTrue(
              rebrakedImpulse < 1e-9,
              "Engineered motor bypassed recovery braking after towing: " + rebrakedImpulse);
          System.out.println(
              "FD_ENGINEERED_RECOVERY brakedImpulse="
                  + brakedImpulse[0]
                  + " activeImpulse="
                  + activeImpulse[0]
                  + " brakedDistance="
                  + brakedDistance[0]
                  + " towingDistance="
                  + towingDistance
                  + " rebrakedImpulse="
                  + rebrakedImpulse
                  + " ordinaryBearing=true explicitStart=true");
          helper.succeed();
        });
  }

  private static double motorImpulse(net.minecraft.server.level.ServerLevel level, ServerSubLevel body) {
    var container = SubLevelContainer.getContainer(level);
    var center = CoasterCartResolve.cartCenterWorld(body);
    var hit =
        CoasterPathTrackFrame.nearestGraphHit(
            level,
            CoasterPathGraphManager.get(level),
            new net.minecraft.world.phys.Vec3(center.x, center.y, center.z),
            2);
    if (hit == null) throw new IllegalStateException("Recovery bearing lost matching track hit");
    var total =
        body.getOrCreateQueuedForceGroup(
                dev.ryanhcode.sable.api.physics.force.ForceGroups.PROPULSION.get())
            .getForceTotal();
    var beforeForce = new Vector3d(total.getLocalForce());
    var beforeTorque = new Vector3d(total.getLocalTorque());
    invoke(
        type("MotorDrive"),
        "apply",
        body,
        level,
        container.physicsSystem(),
        hit,
        CoasterPathTrackFrame.trackBasisAtHit(level, hit),
        .05d);
    double impulse = total.getLocalForce().distance(beforeForce);
    total.getLocalForce().set(beforeForce);
    total.getLocalTorque().set(beforeTorque);
    return impulse;
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
