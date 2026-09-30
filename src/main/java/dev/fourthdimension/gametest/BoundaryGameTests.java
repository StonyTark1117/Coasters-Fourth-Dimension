package dev.fourthdimension.gametest;

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
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class BoundaryGameTests {
  @GameTest(template = "empty", timeoutTicks = 240)
  public static void wrongFamilyBoundariesBrakeInBothDirections(GameTestHelper helper) {
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    for (int x = -26; x <= -22; x++)
      for (int z = 15; z <= 24; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000107"), "FDBoundaryTest"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var all = new ArrayList<FoundationData.Rail>();
    var routes = new ArrayList<FoundationData.Rail>();
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    var container = SubLevelContainer.getContainer(level);
    for (int direction = 0; direction < 2; direction++) {
      var points = new ArrayList<BlockPos>();
      for (int n = 0; n < 4; n++) {
        var p = new BlockPos(-400 + 24 * direction, 100, 260 + 40 * n);
        points.add(p);
        level.setBlockAndUpdate(
            p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      }
      for (int n = 0; n < 3; n++) {
        var to = points.get(n + 1);
        player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
        all.add(
            TrackPlacement.connect(
                level, points.get(n), to, TrackDesign.ARROW, player, player.getMainHandItem()));
      }
      routes.add(all.get(direction * 3 + 1));
    }
    // Simulate an existing mixed graph loaded from another provider. Our normal placement rejects
    // it.
    for (int i : new int[] {0, 2, 3, 5}) {
      var old = all.get(i);
      data.rails.remove(old);
      var wrong =
          new FoundationData.Rail(
              old.from,
              old.to,
              TrackDesign.LEGACY.family,
              old.profile,
              old.id,
              TrackDesign.LEGACY.variant,
              old.geometryHash,
              0,
              true);
      all.set(i, wrong);
      data.rails.add(wrong);
    }
    helper.runAtTickTime(
        15,
        () -> {
          for (int i = 0; i < 2; i++) {
            var edge = TrackGeometry.resolve(level, routes.get(i));
            double t = i == 0 ? .8 : .2;
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
            vehicles.add(VehicleFactory.create(level, TrackDesign.ARROW, hit));
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          for (int i = 0; i < 2; i++) {
            var v = vehicles.get(i);
            var body = (ServerSubLevel) container.getSubLevel(v.chassis);
            var wrong = TrackGeometry.resolve(level, all.get(i * 3 + (i == 0 ? 2 : 0)));
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, wrong, .5), wrong, .5);
            helper.assertTrue(
                !EligibilityHooks.allows(body, level, hit), "Wrong-family re-engagement accepted");
            var before = new Vector3d(body.logicalPose().position());
            CoasterCartTrackSnap.applyInitialSnapAfterSpawn(body, level, hit);
            helper.assertTrue(
                before.distance(body.logicalPose().position()) < 1e-8,
                "Initial-snap API bypassed family restriction");
            var otherEdge = TrackGeometry.resolve(level, routes.get(1 - i));
            var crossing =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, otherEdge, .5),
                    otherEdge,
                    .5);
            helper.assertTrue(
                !EligibilityHooks.allows(body, level, crossing),
                "Disconnected matching track stole binding");
            VehicleOperations.restraints(level, v, true);
            VehicleOperations.dispatch(level, v);
            container
                .physicsSystem()
                .getPipeline()
                .addLinearAndAngularVelocity(
                    body, new Vector3d(0, 0, i == 0 ? 4 : -4), new Vector3d());
          }
        });
    helper.runAtTickTime(
        210,
        () -> {
          for (int i = 0; i < 2; i++) {
            var v = vehicles.get(i);
            var body = (ServerSubLevel) container.getSubLevel(v.chassis);
            var point = CoasterCartResolve.cartCenterWorld(body);
            System.out.println(
                "FD_BOUNDARY direction=" + i + " position=" + point + " fault=" + v.fault);
            helper.assertTrue(
                v.stopping && v.restraints && !v.fault.isBlank(),
                "Boundary did not retain restraints and stop");
            helper.assertTrue(
                point.z > 301 && point.z < 340, "Vehicle crossed incompatible boundary");
            helper.assertTrue(
                container
                        .physicsSystem()
                        .getPipeline()
                        .getLinearVelocity(body, new Vector3d())
                        .length()
                    < .1,
                "Boundary braking did not stop translation");
            helper.assertTrue(
                routes.get(i).matches(v.boundFrom, v.boundTo), "Wrong family replaced bound track");
            container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
          }
          for (var rail : all) {
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
            data.rails.remove(rail);
          }
          for (var rail : all) {
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
          }
          helper.succeed();
        });
  }
}
