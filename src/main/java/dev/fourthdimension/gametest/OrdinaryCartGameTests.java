package dev.fourthdimension.gametest;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
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
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class OrdinaryCartGameTests {
  @GameTest(template = "empty", timeoutTicks = 180)
  public static void bareBearingsFitAllTracksWithoutReceivingRotation(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 36; x <= 41; x++) for (int z = 6; z <= 10; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000108"), "FDGearTest"));
    player.setGameMode(GameType.CREATIVE);
    var rails = new ArrayList<FoundationData.Rail>();
    var bodies = new ArrayList<ServerSubLevel>();
    var starts = new ArrayList<Vector3d>();
    var container = SubLevelContainer.getContainer(level);
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(600 + design.ordinal() * 24, 116, 110);
      var to = from.offset(0, 0, 32);
      for (var p : List.of(from, to))
        level.setBlockAndUpdate(
            p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      rails.add(TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem()));
    }
    rails.stream()
            .filter(r -> r.family == dev.fourthdimension.contract.Family.FREE_SPIN)
            .findFirst()
            .orElseThrow()
            .magnets =
        List.of(
            new dev.fourthdimension.physics.MagneticSpan(
                1, 30, dev.fourthdimension.physics.MagneticSpan.Mode.INDUCE, 1, 20, .1, 15));
    helper.runAtTickTime(
        15,
        () -> {
          for (var rail : rails) {
            var edge = TrackGeometry.resolve(level, rail);
            var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, .25);
            var hit = new CoasterPathTrackFrame.GraphHit(point, edge, .25);
            var q =
                CoasterPathTrackFrame.quaternionFromTrackBasis(
                    CoasterPathTrackFrame.trackBasisAtHit(level, hit));
            var origin =
                CoasterCartLinkPlacement.plotOriginForBearingCenter(
                    CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), q);
            var body = CoasterCartSpawner.spawnMinimalContraption(level, origin, q, hit, true);
            helper.assertTrue(
                body != null && EligibilityHooks.allows(body, level, hit),
                "Bare bearing was rejected");
            bodies.add(body);
            starts.add(new Vector3d(body.logicalPose().position()));
            var embedded = body.getPlot().getEmbeddedLevelAccessor();
            embedded.setBlock(new BlockPos(2, 0, 0), Blocks.IRON_BLOCK.defaultBlockState(), 3);
            helper.assertTrue(
                !EligibilityHooks.allows(body, level, hit), "Uncertified wide body was accepted");
            embedded.setBlock(new BlockPos(2, 0, 0), Blocks.AIR.defaultBlockState(), 3);
            helper.assertTrue(
                EligibilityHooks.allows(body, level, hit), "Restored bare bearing rejected");
            container
                .physicsSystem()
                .getPipeline()
                .addLinearAndAngularVelocity(body, new Vector3d(0, 0, 3), new Vector3d());
          }
        });
    helper.runAtTickTime(
        100,
        () -> {
          var data = FoundationData.get(level);
          for (int i = 0; i < bodies.size(); i++) {
            var body = bodies.get(i);
            helper.assertTrue(
                body.logicalPose().position().distance(starts.get(i)) > 1,
                "Ordinary bearing failed native traversal");
            helper.assertTrue(
                !data.vehicles.containsKey(body.getUniqueId()), "Ordinary bearing gained 4D state");
            helper.assertTrue(
                Math.abs(body.logicalPose().orientation().x()) < .05,
                "Ordinary bearing received seat rotation");
            helper.assertTrue(
                dev.fourthdimension.magnetic.MagneticRuntime.audit(body).impulses == 0,
                "Ordinary bearing received magnetic impulses");
            container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
          }
          for (var rail : rails) {
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
            data.rails.remove(rail);
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
          }
          helper.succeed();
        });
  }
}
