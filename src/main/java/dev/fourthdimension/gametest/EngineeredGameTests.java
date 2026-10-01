package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.ShuttleRoute;
import dev.fourthdimension.shuttle.ShuttleWinchEntity;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
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

/**
 * Real installed motor/controller physics; no optional types are needed for standalone discovery.
 */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredGameTests {
  private static final String API = "dev.jwaterfall.coastersengineered.";

  @GameTest(template = "empty", timeoutTicks = 650)
  public static void installedMotorsRespectCaptureAndDriveDispatchedFamilies(
      GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED absent/uninspected: operating scenario not exercised");
      helper.succeed();
      return;
    }
    helper.assertTrue(
        Arrays.stream(type("MotorDrive").getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("fourthdimension$holdMotor")),
        "Pinned Engineered motor interlock was not injected");
    helper.assertTrue(
        Arrays.stream(type("CartAssembly").getDeclaredMethods())
            .anyMatch(method -> method.getName().contains("fourthdimension$includeCarriers")),
        "Pinned Engineered rotary membership adapter was not injected");
    var level = helper.getLevel();
    for (int x = -39; x <= -30; x++)
      for (int z = -51; z <= -46; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000411"), "FDEngineered"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var rails = new ArrayList<FoundationData.Rail>();
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    var controllers = new ArrayList<Object>();
    var controllerPositions = new ArrayList<BlockPos>();
    var starts = new ArrayList<Vector3d>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    var container = SubLevelContainer.getContainer(level);
    var block =
        BuiltInRegistries.BLOCK.get(
            ResourceLocation.parse("coastersengineered:linear_motor_controller"));
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(-600 + design.ordinal() * 24, 120, -800);
      var to = from.offset(0, 0, 48);
      for (var p : List.of(from, to))
        level.setBlockAndUpdate(
            p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      rails.add(TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem()));
      var pos = from.offset(-6, 0, 0);
      controllerPositions.add(pos);
      level.setBlockAndUpdate(pos, block.defaultBlockState());
    }
    helper.runAtTickTime(
        15,
        () -> {
          for (int i = 0; i < rails.size(); i++) {
            var rail = rails.get(i);
            var edge = TrackGeometry.resolve(level, rail);
            var controller =
                configureMotor(helper, rail, controllerPositions.get(i), 10, "FORWARD");
            controllers.add(controller);
            var t = edge.from().equals(rail.from) ? .25 : .75;
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
            var vehicle = VehicleFactory.create(level, TrackDesign.values()[i], hit);
            vehicles.add(vehicle);
            starts.add(
                new Vector3d(container.getSubLevel(vehicle.chassis).logicalPose().position()));
          }
        });
    helper.runAtTickTime(
        60,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var position = container.getSubLevel(vehicles.get(i).chassis).logicalPose().position();
            System.out.println(
                "FD_ENGINEERED initialSettle "
                    + vehicles.get(i).family
                    + " displacement="
                    + new Vector3d(position).sub(starts.get(i)));
            starts.set(i, new Vector3d(position));
          }
        });
    helper.runAtTickTime(
        100,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            var center = new Vector3d(container.getSubLevel(v.chassis).logicalPose().position());
            double drift = Math.abs(center.z - starts.get(i).z);
            System.out.println(
                "FD_ENGINEERED held "
                    + v.family
                    + " longitudinalDrift="
                    + drift
                    + " settle="
                    + new Vector3d(center).sub(starts.get(i)));
            helper.assertTrue(drift < .1, "Motor bypassed initial capture");
            helper.assertTrue(Boarding.ready(level, v), "Motor prevented boarding alignment");
            helper.assertTrue(
                (boolean) invoke(controllers.get(i), "isLinked"), "Motor controller unlinked");
            assertMass(helper, v);
            var c = v.carriers.getFirst();
            var body = container.getSubLevel(c.body);
            var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
            pig.setNoAi(true);
            pig.setPos(center.x, center.y, center.z);
            level.addFreshEntity(pig);
            SeatBlock.sitDown(
                level, body.getPlot().getCenterBlock().offset(c.seats.getFirst()), pig);
            helper.assertTrue(pig.isPassenger(), "Installed motor fixture could not board");
            riders.add(pig);
            VehicleOperations.restraints(level, v, true);
            if (v.family != Family.PAX) VehicleOperations.dispatch(level, v);
            else VehicleOperations.retention(level, v, true);
          }
        });
    helper.runAtTickTime(
        200,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            var body = (ServerSubLevel) container.getSubLevel(v.chassis);
            var center = new Vector3d(body.logicalPose().position());
            double distance = Math.abs(center.z - starts.get(i).z);
            double speed =
                container
                    .physicsSystem()
                    .getPipeline()
                    .getLinearVelocity(body, new Vector3d())
                    .length();
            System.out.println(
                "FD_ENGINEERED launch " + v.family + " distance=" + distance + " speed=" + speed);
            if (v.family == Family.PAX) {
              helper.assertTrue(
                  distance < .1 && v.maintenance, "Motor overrode PAX ownership/retention");
            } else {
              helper.assertTrue(
                  distance > 3 && speed > 1, "Installed motor did not drive dispatched vehicle");
              helper.assertTrue(
                  !v.stopping && v.restraints, "Motor changed restraint/capture ownership");
              EditorGameTests.rejects(
                  helper,
                  () -> VehicleOperations.restraints(level, v, false),
                  "Motor movement permitted unloading");
              setMode(controllers.get(i), "STOP", 0);
            }
            helper.assertTrue(
                v.family == rails.get(i).family && rails.get(i).matches(v.boundFrom, v.boundTo),
                "Motor changed functional family/binding");
            helper.assertTrue(riders.get(i).isPassenger(), "Motor dropped passenger");
            assertMass(helper, v);
          }
        });
    helper.runAtTickTime(
        330,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            var body = (ServerSubLevel) container.getSubLevel(v.chassis);
            double speed =
                container
                    .physicsSystem()
                    .getPipeline()
                    .getLinearVelocity(body, new Vector3d())
                    .length();
            System.out.println("FD_ENGINEERED stop " + v.family + " speed=" + speed);
            helper.assertTrue(speed < .1, "Installed STOP controller did not stop vehicle");
            if (v.family != Family.PAX) {
              helper.assertTrue(
                  !v.stopping && v.restraints,
                  "Longitudinal STOP unexpectedly captured/unloaded carriers");
              VehicleOperations.capture(level, v);
            }
          }
        });
    helper.runAtTickTime(
        480,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            helper.assertTrue(Boarding.ready(level, v), "Carrier capture failed after motor stop");
            VehicleOperations.restraints(level, v, false);
            riders.get(i).stopRiding();
            riders.get(i).discard();
            VehicleOperations.retention(level, v, true);
            setMode(controllers.get(i), "LAUNCH", 20);
            starts.set(i, new Vector3d(container.getSubLevel(v.chassis).logicalPose().position()));
          }
        });
    helper.runAtTickTime(
        590,
        () -> {
          var data = FoundationData.get(level);
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            double drift =
                Math.abs(
                    container.getSubLevel(v.chassis).logicalPose().position().z()
                        - starts.get(i).z);
            System.out.println("FD_ENGINEERED retained " + v.family + " drift=" + drift);
            helper.assertTrue(
                drift < .1 && v.maintenance && v.stopping, "Motor bypassed maintenance retention");
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
            for (var c : v.carriers) {
              var body = container.getSubLevel(c.body);
              if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
            }
            data.vehicles.remove(v.chassis);
            var rail = rails.get(i);
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
            data.rails.remove(rail);
            for (var p : List.of(rail.from, rail.to, controllerPositions.get(i)))
              level.removeBlock(p, false);
          }
          System.out.println(
              "FD_ENGINEERED installed=1.1.1 fourFamilies=launch/stop/capture/retention PAX=held");
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 240)
  public static void ordinaryBearingUsesMotorWithoutRotaryMembership(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED ordinary: absent/uninspected, not exercised");
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    for (int x = -30; x <= -28; x++)
      for (int z = -51; z <= -47; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000412"), "FDEngineeredOrdinary"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var from = new BlockPos(-456, 120, -800);
    var to = from.offset(0, 0, 48);
    for (var p : List.of(from, to))
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    var rail =
        TrackPlacement.connect(
            level, from, to, TrackDesign.ARROW, player, player.getMainHandItem());
    var controllerPos = from.offset(-6, 0, 0);
    var controllerBlock =
        BuiltInRegistries.BLOCK.get(
            ResourceLocation.parse("coastersengineered:linear_motor_controller"));
    level.setBlockAndUpdate(controllerPos, controllerBlock.defaultBlockState());
    var container = SubLevelContainer.getContainer(level);
    ServerSubLevel[] body = {null};
    Object[] controller = {null};
    double[] initialZ = {0};
    helper.runAtTickTime(
        15,
        () -> {
          controller[0] = configureMotor(helper, rail, controllerPos, 10, "FORWARD");
          var edge = TrackGeometry.resolve(level, rail);
          double t = edge.from().equals(from) ? .125 : .875;
          var hit =
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
          var q =
              CoasterPathTrackFrame.quaternionFromTrackBasis(
                  CoasterPathTrackFrame.trackBasisAtHit(level, hit));
          var origin =
              CoasterCartLinkPlacement.plotOriginForBearingCenter(
                  CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), q);
          body[0] = CoasterCartSpawner.spawnMinimalContraption(level, origin, q, hit, true);
          helper.assertTrue(body[0] != null, "Ordinary motor fixture did not assemble");
          initialZ[0] = body[0].logicalPose().position().z();
        });
    helper.runAtTickTime(
        100,
        () -> {
          double distance = Math.abs(body[0].logicalPose().position().z() - initialZ[0]);
          helper.assertTrue(distance > 3, "Installed motor failed ordinary bearing traversal");
          var members = (List<?>) invoke(type("CartAssembly"), "bodies", level, body[0]);
          double mass = (double) invoke(type("MotorForce"), "mass", level, body[0]);
          helper.assertTrue(
              members.size() == 1 && members.getFirst() == body[0],
              "Ordinary bearing gained rotary membership");
          helper.assertTrue(
              Math.abs(mass - body[0].getMassTracker().getMass()) < 1e-6, "Ordinary mass changed");
          helper.assertTrue(
              !FoundationData.get(level).vehicles.containsKey(body[0].getUniqueId()),
              "Ordinary cart gained 4D state");
          helper.assertTrue(
              Math.abs(body[0].logicalPose().orientation().x()) < .05,
              "Ordinary bearing received seat rotation");
          setMode(controller[0], "STOP", 0);
          System.out.println(
              "FD_ENGINEERED ordinary distance="
                  + distance
                  + " bodies=1 mass="
                  + mass
                  + " no4D=true");
        });
    helper.runAtTickTime(
        210,
        () -> {
          double speed =
              container
                  .physicsSystem()
                  .getPipeline()
                  .getLinearVelocity(body[0], new Vector3d())
                  .length();
          helper.assertTrue(speed < .1, "Installed motor failed ordinary STOP");
          container.removeSubLevel(body[0], SubLevelRemovalReason.REMOVED);
          ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from))
              .removeAnchorPeerCurve(level, to);
          FoundationData.get(level).rails.remove(rail);
          for (var p : List.of(from, to, controllerPos)) level.removeBlock(p, false);
          System.out.println("FD_ENGINEERED ordinary STOP speed=" + speed);
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 1000)
  public static void installedMotorDrivesLinkedControlledTurnsAndRollback(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED controlled train: absent/uninspected, not exercised");
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    for (int x = -24; x <= -21; x++)
      for (int z = -51; z <= -46; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000413"), "FDEngineeredTrain"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var from = new BlockPos(-360, 120, -800);
    var to = from.offset(0, 0, 48);
    for (var pos : List.of(from, to))
      level.setBlockAndUpdate(
          pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    var rail =
        TrackPlacement.connect(
            level, from, to, TrackDesign.ARROW, player, player.getMainHandItem());
    rail.profile =
        new dev.fourthdimension.physics.RotationProfile(
            List.of(
                new dev.fourthdimension.physics.RotationProfile.Key(0, 0),
                new dev.fourthdimension.physics.RotationProfile.Key(20, 0),
                new dev.fourthdimension.physics.RotationProfile.Key(26, 2 * Math.PI),
                new dev.fourthdimension.physics.RotationProfile.Key(36, 2 * Math.PI),
                new dev.fourthdimension.physics.RotationProfile.Key(44, 0),
                new dev.fourthdimension.physics.RotationProfile.Key(48, 0)));
    var originalProfile = rail.profile;
    var controllerPos = from.offset(-6, 0, 0);
    level.setBlockAndUpdate(
        controllerPos,
        BuiltInRegistries.BLOCK
            .get(ResourceLocation.parse("coastersengineered:linear_motor_controller"))
            .defaultBlockState());
    var container = SubLevelContainer.getContainer(level);
    var cars = new ArrayList<FoundationData.Vehicle>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    Object[] controller = {null};
    int[] phase = {0}, ticks = {0}, phaseStart = {0};
    double[] maxAngle = new double[2],
        rollbackMin = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    double[] maxAxleDifference = {0}, maxSpacingError = {0};
    helper.runAtTickTime(
        15,
        () -> {
          controller[0] = configureMotor(helper, rail, controllerPos, 8, "FORWARD");
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
        80,
        () -> {
          for (var car : cars)
            helper.assertTrue(Boarding.ready(level, car), "Powered train not ready to board");
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.linkCarts(
                  level,
                  (ServerSubLevel) container.getSubLevel(cars.getFirst().chassis),
                  (ServerSubLevel) container.getSubLevel(cars.getLast().chassis),
                  6),
              "Powered train native coupling rejected");
          for (var car : cars) {
            var carrier = car.carriers.getFirst();
            var body = container.getSubLevel(carrier.body);
            var position = container.getSubLevel(car.chassis).logicalPose().position();
            var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
            pig.setNoAi(true);
            pig.setPos(position.x(), position.y(), position.z());
            level.addFreshEntity(pig);
            SeatBlock.sitDown(
                level, body.getPlot().getCenterBlock().offset(carrier.seats.getFirst()), pig);
            helper.assertTrue(pig.isPassenger(), "Powered train boarding failed");
            riders.add(pig);
          }
          VehicleOperations.restraints(level, cars.getFirst(), true);
          VehicleOperations.dispatch(level, cars.getFirst());
          phase[0] = 1;
          phaseStart[0] = ticks[0];
        });
    helper.onEachTick(
        () -> {
          ticks[0]++;
          if (phase[0] == 0 || phase[0] == 6) return;
          var a = (ServerSubLevel) container.getSubLevel(cars.getFirst().chassis);
          var b = (ServerSubLevel) container.getSubLevel(cars.getLast().chassis);
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.areLinkedCarts(a, b),
              "Motor-driven native coupling broke");
          double front = a.logicalPose().position().z() - from.getZ() - .5;
          double rear = b.logicalPose().position().z() - from.getZ() - .5;
          maxSpacingError[0] = Math.max(maxSpacingError[0], Math.abs(front - rear - 6));
          double difference =
              Math.abs(
                  cars.getFirst().carriers.getFirst().angle
                      - cars.getLast().carriers.getFirst().angle);
          maxAxleDifference[0] = Math.max(maxAxleDifference[0], difference);
          double speed =
              Math.max(
                  container
                      .physicsSystem()
                      .getPipeline()
                      .getLinearVelocity(a, new Vector3d())
                      .length(),
                  container
                      .physicsSystem()
                      .getPipeline()
                      .getLinearVelocity(b, new Vector3d())
                      .length());
          helper.assertTrue(
              rail.profile == originalProfile, "Motor controller changed the rotation profile");
          for (int i = 0; i < cars.size(); i++) {
            var car = cars.get(i);
            helper.assertTrue(
                car.restraints, "Powered train opened restraints before normal unloading");
            helper.assertTrue(
                car.family == Family.ARROW
                    && rail.matches(car.boundFrom, car.boundTo)
                    && car.fault.isEmpty(),
                "Powered train lost its matching functional binding: " + car.fault);
            helper.assertTrue(riders.get(i).isPassenger(), "Powered train lost passenger");
            for (var carrier : car.carriers) {
              maxAngle[i] = Math.max(maxAngle[i], carrier.angle);
              if (phase[0] >= 3) rollbackMin[i] = Math.min(rollbackMin[i], carrier.angle);
            }
          }
          int elapsed = ticks[0] - phaseStart[0];
          if (phase[0] == 1 && front >= 32) {
            setMode(controller[0], "STOP", 0);
            phase[0] = 2;
            phaseStart[0] = ticks[0];
            System.out.println(
                "FD_ENGINEERED_TRAIN forwardStop front="
                    + front
                    + " rear="
                    + rear
                    + " speed="
                    + speed);
          } else if (phase[0] == 2 && elapsed >= 120) {
            helper.assertTrue(
                speed < .1 && rear > 26 && front < 36,
                "Native motor STOP missed the full-turn plateau");
            for (var car : cars)
              for (var carrier : car.carriers)
                helper.assertTrue(
                    !carrier.capture && Math.abs(carrier.angle - 2 * Math.PI) < .08,
                    "Native motor did not complete/hold the unwrapped full turn");
            helper.assertTrue(
                maxAngle[0] > 6.2 && maxAngle[1] > 6.2 && maxAxleDifference[0] > 1,
                "Motor-driven train skipped turns or used a shared axle target");
            for (var car : cars) assertMass(helper, car);
            setMode(controller[0], "LAUNCH", "BACKWARD", 8);
            phase[0] = 3;
            phaseStart[0] = ticks[0];
          } else if (phase[0] == 3 && front <= 18) {
            setMode(controller[0], "STOP", 0);
            phase[0] = 4;
            phaseStart[0] = ticks[0];
            System.out.println(
                "FD_ENGINEERED_TRAIN rollbackStop front="
                    + front
                    + " rear="
                    + rear
                    + " speed="
                    + speed);
          } else if (phase[0] == 4 && elapsed >= 120) {
            helper.assertTrue(
                speed < .1 && rear > 8 && front < 20,
                "Native reverse STOP missed the original zero-angle plateau");
            for (var car : cars)
              for (var carrier : car.carriers)
                helper.assertTrue(
                    !carrier.capture
                        && Math.abs(carrier.angle) < .08
                        && Math.abs(carrier.omega) < .08,
                    "Native rollback failed to retrace the angle program");
            helper.assertTrue(
                rollbackMin[0] < .08 && rollbackMin[1] < .08,
                "Rollback lost original full-turn index");
            VehicleOperations.capture(level, cars.getFirst());
            phase[0] = 5;
            phaseStart[0] = ticks[0];
          } else if (phase[0] == 5 && elapsed >= 100) {
            for (var car : cars)
              helper.assertTrue(
                  Boarding.ready(level, car), "Powered rollback train failed normal recapture");
            VehicleOperations.restraints(level, cars.getFirst(), false);
            for (var pig : riders) {
              pig.stopRiding();
              helper.assertTrue(!pig.isPassenger(), "Powered train could not unload");
              pig.discard();
            }
            helper.assertTrue(
                maxSpacingError[0] < .25, "Motor forces overstretched native coupler");
            CoasterCartTrainLinkConstraint.clearAllLinks(level, a);
            for (var car : cars) {
              for (var carrier : car.carriers)
                container.removeSubLevel(
                    container.getSubLevel(carrier.body), SubLevelRemovalReason.REMOVED);
              container.removeSubLevel(
                  container.getSubLevel(car.chassis), SubLevelRemovalReason.REMOVED);
              FoundationData.get(level).vehicles.remove(car.chassis);
            }
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from))
                .removeAnchorPeerCurve(level, to);
            FoundationData.get(level).rails.remove(rail);
            for (var pos : List.of(from, to, controllerPos)) level.removeBlock(pos, false);
            phase[0] = 6;
            System.out.println(
                "FD_ENGINEERED_TRAIN fullTurns="
                    + Arrays.toString(maxAngle)
                    + " rollback="
                    + Arrays.toString(rollbackMin)
                    + " perAxleDifference="
                    + maxAxleDifference[0]
                    + " spacingError="
                    + maxSpacingError[0]
                    + " unloaded=true");
            helper.succeed();
          }
        });
  }

  public static Runnable addOpposingShuttleMotors(GameTestHelper helper, ShuttleWinchEntity winch) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect())
      return () -> {};
    var level = helper.getLevel();
    var route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
    var controllerBlock =
        BuiltInRegistries.BLOCK.get(
            ResourceLocation.parse("coastersengineered:linear_motor_controller"));
    for (var segment : route.segments) {
      var pos = segment.rail().from.offset(-8, 0, 0);
      level.setBlockAndUpdate(pos, controllerBlock.defaultBlockState());
      configureMotor(helper, segment.rail(), pos, 20, "BACKWARD");
    }
    Runnable probe =
        () -> {
          var container = SubLevelContainer.getContainer(level);
          for (var id : winch.cars) {
            if (!(container.getSubLevel(id) instanceof ServerSubLevel body)) continue;
            var hit = route.locate(body).hit();
            // Also exercise the optional call directly, independent of the chain-drive hook.
            invoke(
                type("MotorDrive"),
                "apply",
                body,
                level,
                container.physicsSystem(),
                hit,
                CoasterPathTrackFrame.trackBasisAtHit(level, hit),
                .05d);
          }
        };
    System.out.println(
        "FD_ENGINEERED PAX opposingSpans=" + route.segments.size() + " directMotorCalls=true");
    return probe;
  }

  private static Object configureMotor(
      GameTestHelper helper, FoundationData.Rail rail, BlockPos pos, int speed, String heading) {
    var level = helper.getLevel();
    var segment = construct("span.SpanSegment", rail.from, rail.to, 0d, 1d);
    var span = construct("span.TrackSpan", List.of(segment));
    var behaviour = invoke(type("span.TrackSpanBehaviour"), "at", level, rail.from);
    helper.assertTrue(behaviour != null, "Installed motor behaviour missing on anchor");
    helper.assertTrue((boolean) invoke(behaviour, "setSpan", span), "Motor span not set");
    var controller = level.getBlockEntity(pos);
    helper.assertTrue(controller != null, "Motor controller not registered");
    invoke(controller, "bindTo", rail.from);
    var step =
        construct(
            "controller.MotorStep",
            enumeration("controller.MotorStep$Kind", "LAUNCH"),
            enumeration("controller.MotorStep$Heading", heading),
            speed);
    invoke(controller, "setSteps", List.of(step));
    return controller;
  }

  private static void setMode(Object controller, String kind, int value) {
    setMode(controller, kind, "FORWARD", value);
  }

  private static void setMode(Object controller, String kind, String heading, int value) {
    var step =
        construct(
            "controller.MotorStep",
            enumeration("controller.MotorStep$Kind", kind),
            enumeration("controller.MotorStep$Heading", heading),
            value);
    invoke(controller, "setSteps", List.of(step));
  }

  private static void assertMass(GameTestHelper helper, FoundationData.Vehicle vehicle) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var chassis = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
    var bodies = (List<?>) invoke(type("CartAssembly"), "bodies", level, chassis);
    helper.assertTrue(
        bodies.size() == vehicle.carriers.size() + 1, "Rotary mass omitted/duplicated bodies");
    double expected = chassis.getMassTracker().getMass();
    for (var carrier : vehicle.carriers)
      expected += ((ServerSubLevel) container.getSubLevel(carrier.body)).getMassTracker().getMass();
    double actual = (double) invoke(type("MotorForce"), "mass", level, chassis);
    System.out.println(
        "FD_ENGINEERED mass "
            + vehicle.family
            + " assembly="
            + actual
            + " expected="
            + expected
            + " bodies="
            + bodies.size());
    helper.assertTrue(
        Math.abs(actual - expected) < 1e-6, "Engineered mass differs from native assembly mass");
  }

  private static Class<?> type(String name) {
    try {
      return Class.forName(API + name);
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Inspected Engineered API unavailable", failure);
    }
  }

  private static Object enumeration(String type, String name) {
    try {
      return type(type).getField(name).get(null);
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException(failure);
    }
  }

  private static Object construct(String name, Object... args) {
    try {
      for (var constructor : type(name).getConstructors())
        if (matches(constructor.getParameterTypes(), args)) return constructor.newInstance(args);
      throw new NoSuchMethodException(name);
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException(failure);
    }
  }

  private static Object invoke(Object target, String name, Object... args) {
    try {
      var type = target instanceof Class<?> c ? c : target.getClass();
      for (var method : type.getMethods())
        if (method.getName().equals(name) && matches(method.getParameterTypes(), args))
          return method.invoke(target instanceof Class<?> ? null : target, args);
      throw new NoSuchMethodException(name);
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException(failure);
    }
  }

  private static boolean matches(Class<?>[] parameters, Object[] args) {
    if (parameters.length != args.length) return false;
    for (int i = 0; i < args.length; i++)
      if (!parameters[i].isInstance(args[i])
          && !(parameters[i] == double.class && args[i] instanceof Double)
          && !(parameters[i] == int.class && args[i] instanceof Integer)) return false;
    return true;
  }
}
