package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.ExternalDrivePolicy;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
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
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class ControlledTrainGameTests {
  @GameTest(template = "empty", timeoutTicks = 850)
  public static void linkedCarsSampleTheirOwnAxlesAndRollback(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -14; x <= -11; x++) for (int z = 5; z <= 12; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000102"), "FDTrainTest"));
    player.setGameMode(GameType.CREATIVE);
    var from = new BlockPos(-200, 112, 100);
    var middle = from.offset(0, 0, 36);
    var end = middle.offset(0, 0, 36);
    for (var p : List.of(from, middle, end))
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(middle.getX() + 2, middle.getY() + 1, middle.getZ());
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var first =
        TrackPlacement.connect(
            level, from, middle, TrackDesign.ARROW, player, player.getMainHandItem());
    player.setPos(end.getX() + 2, end.getY() + 1, end.getZ());
    var second =
        TrackPlacement.connect(
            level, middle, end, TrackDesign.ARROW, player, player.getMainHandItem());
    first.profile =
        new RotationProfile(
            List.of(
                new RotationProfile.Key(0, 0),
                new RotationProfile.Key(18, 2 * Math.PI),
                new RotationProfile.Key(36, 0)));
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    var moving = new AtomicBoolean(false);
    var reverse = new AtomicBoolean(false);
    var supportProbed = new AtomicBoolean(false);
    var max = new double[3];
    var container = SubLevelContainer.getContainer(level);
    helper.onEachTick(
        () -> {
          if (!moving.get()) return;
          if (!supportProbed.get()
              && second.matches(vehicles.getFirst().boundFrom, vehicles.getFirst().boundTo)
              && first.matches(vehicles.getLast().boundFrom, vehicles.getLast().boundTo)) {
            probeLinkedSupport(helper, vehicles, List.of(second, first));
            supportProbed.set(true);
          }
          for (var v : vehicles) {
            var body = (ServerSubLevel) container.getSubLevel(v.chassis);
            var pipeline = container.physicsSystem().getPipeline();
            pipeline.addLinearAndAngularVelocity(
                body,
                new Vector3d(0, 0, reverse.get() ? -2 : 2)
                    .sub(pipeline.getLinearVelocity(body, new Vector3d())),
                new Vector3d());
          }
          max[0] = Math.max(max[0], vehicles.getFirst().carriers.getFirst().angle);
          max[1] =
              Math.max(
                  max[1],
                  Math.abs(
                      vehicles.getFirst().carriers.getFirst().angle
                          - vehicles.getLast().carriers.getFirst().angle));
        });
    helper.runAtTickTime(
        15,
        () -> {
          var edge =
              CoasterPathGraphManager.get(level).edges().stream()
                  .filter(e -> first.matches(e.from(), e.to()))
                  .findFirst()
                  .orElseThrow();
          for (double distance : new double[] {10, 4}) {
            double t = distance / 36;
            var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
            vehicles.add(
                VehicleFactory.create(
                    level, TrackDesign.ARROW, new CoasterPathTrackFrame.GraphHit(point, edge, t)));
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          for (var v : vehicles)
            helper.assertTrue(Boarding.ready(level, v), "Train failed boarding alignment");
          var a = (ServerSubLevel) container.getSubLevel(vehicles.getFirst().chassis);
          var b = (ServerSubLevel) container.getSubLevel(vehicles.getLast().chassis);
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.linkCarts(level, a, b, 6),
              "Matching controlled cars failed native coupling");
          VehicleOperations.restraints(level, vehicles.getFirst(), true);
          VehicleOperations.dispatch(level, vehicles.getFirst());
          moving.set(true);
        });
    helper.runAtTickTime(
        440,
        () -> {
          helper.assertTrue(supportProbed.get(), "Train never reached split-support probe");
          System.out.println("FD_TRAIN maxTurn=" + max[0] + " maxAxleDifference=" + max[1]);
          helper.assertTrue(max[0] > 6, "Controlled production car did not complete its full turn");
          helper.assertTrue(
              max[1] > 1, "Linked cars used a train-wide angle instead of their own axle position");
          System.out.println(
              "FD_TRAIN positions="
                  + vehicles.stream()
                      .map(
                          v ->
                              CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis))
                                  + " bound="
                                  + v.boundFrom
                                  + "->"
                                  + v.boundTo
                                  + " fault="
                                  + v.fault)
                      .toList());
          helper.assertTrue(
              vehicles.stream().allMatch(v -> second.matches(v.boundFrom, v.boundTo)),
              "Train did not transition to connected matching track");
          var a = container.getSubLevel(vehicles.getFirst().chassis);
          var b = container.getSubLevel(vehicles.getLast().chassis);
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.areLinkedCarts(a, b),
              "Native coupling broke while traversing the profile");
          reverse.set(true);
          max[2] = vehicles.getFirst().carriers.getFirst().angle;
        });
    helper.runAtTickTime(
        650,
        () -> {
          helper.assertTrue(
              vehicles.stream().allMatch(v -> first.matches(v.boundFrom, v.boundTo)),
              "Rollback did not rebind the preceding segment");
          helper.assertTrue(
              vehicles.getFirst().carriers.getFirst().angle > 2,
              "Rollback did not retrace the controlled program");
          moving.set(false);
          VehicleOperations.capture(level, vehicles.getFirst());
        });
    helper.runAtTickTime(
        810,
        () -> {
          for (var v : vehicles) {
            for (var c : v.carriers)
              container.removeSubLevel(
                  container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
            FoundationData.get(level).vehicles.remove(v.chassis);
          }
          for (var r : List.of(first, second)) {
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(r.from))
                .removeAnchorPeerCurve(level, r.to);
            FoundationData.get(level).rails.remove(r);
          }
          for (var p : List.of(from, middle, end)) level.removeBlock(p, false);
          helper.succeed();
        });
  }

  private static void probeLinkedSupport(
      GameTestHelper helper,
      List<FoundationData.Vehicle> vehicles,
      List<FoundationData.Rail> supports) {
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    for (int damaged = 0; damaged < vehicles.size(); damaged++) {
      var rail = supports.get(damaged);
      int index = data.rails.indexOf(rail);
      helper.assertTrue(index >= 0, "Linked support disappeared before probe");
      for (var v : vehicles)
        helper.assertTrue(ExternalDrivePolicy.allows(level, v.chassis), "Valid train drive denied");
      var unavailable =
          new FoundationData.Rail(
              rail.from,
              rail.to,
              rail.family,
              rail.profile,
              rail.id,
              rail.variant,
              rail.geometryHash,
              rail.revision,
              rail.valid,
              "missing:functional");
      unavailable.magnets = rail.magnets;
      unavailable.appearance = rail.appearance;
      try {
        data.rails.set(index, unavailable);
        int healthy = 1 - damaged;
        var vehicle = vehicles.get(healthy);
        var body = container.getSubLevel(vehicle.chassis);
        var edge = TrackGeometry.resolve(level, supports.get(healthy));
        helper.assertTrue(edge != null, "Healthy support lost native geometry");
        var hit =
            new CoasterPathTrackFrame.GraphHit(
                CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
        boolean eligible = EligibilityHooks.allows(body, level, hit);
        boolean filtered =
            CoasterCartRailSnapEligibility.filterHitForBearing(body, level, hit) != null;
        var drive =
            vehicles.stream().map(v -> ExternalDrivePolicy.allows(level, v.chassis)).toList();
        System.out.println(
            "FD_LINKED_SUPPORT damaged="
                + damaged
                + " eligible="
                + eligible
                + " filtered="
                + filtered
                + " drive="
                + drive);
        helper.assertTrue(
            !eligible && !filtered && drive.stream().noneMatch(Boolean::booleanValue),
            "Unavailable partner provider passed linked eligibility or external drive");
      } finally {
        data.rails.set(index, rail);
      }
      for (int i = 0; i < vehicles.size(); i++) {
        var v = vehicles.get(i);
        var body = container.getSubLevel(v.chassis);
        helper.assertTrue(
            ExternalDrivePolicy.allows(level, v.chassis)
                && ConsistRules.eligible(level, body, v)
                && supports.get(i).matches(v.boundFrom, v.boundTo)
                && !v.stopping
                && !v.maintenance
                && v.restraints
                && v.fault.isBlank(),
            "Support probe changed healthy train state");
      }
      helper.assertTrue(
          CoasterCartTrainLinkConstraint.areLinkedCarts(
              container.getSubLevel(vehicles.getFirst().chassis),
              container.getSubLevel(vehicles.getLast().chassis)),
          "Support probe broke native coupling");
    }
  }
}
