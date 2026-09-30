package dev.fourthdimension.gametest;

import dev.fourthdimension.magnetic.*;
import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
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
public final class MagneticTraversalGameTests {
  @GameTest(template = "empty", timeoutTicks = 330)
  public static void fastForwardReverseNarrowSpanNoReplayOrOtherTargets(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -38; x <= -35; x++)
      for (int z = -2; z <= 10; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000202"), "FDMagneticSweep"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(
        InteractionHand.MAIN_HAND, TrackItems.FREE_SPIN.get().getDefaultInstance());
    var points =
        List.of(
            new BlockPos(-580, 140, 0),
            new BlockPos(-580, 140, 32),
            new BlockPos(-580, 140, 96),
            new BlockPos(-580, 140, 128));
    for (var p : points)
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var rails = new ArrayList<FoundationData.Rail>();
    for (int i = 0; i < 3; i++) {
      var to = points.get(i + 1);
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      rails.add(
          TrackPlacement.connect(
              level, points.get(i), to, TrackDesign.FREE_SPIN, player, player.getMainHandItem()));
    }
    var rail = rails.get(1);
    rail.magnets = List.of(new MagneticSpan(31, 31.05, MagneticSpan.Mode.INDUCE, 1, 20, .25, 1));
    var container = SubLevelContainer.getContainer(level);
    var physics = container.physicsSystem().getPipeline();
    FoundationData.Vehicle[] vehicle = {null};
    int[] phase = {0};
    helper.runAtTickTime(
        15,
        () -> {
          var edge = TrackGeometry.resolve(level, rail);
          double t = TrackGeometry.table(level, edge).parameterAt(20);
          vehicle[0] =
              VehicleFactory.create(
                  level,
                  TrackDesign.FREE_SPIN,
                  new CoasterPathTrackFrame.GraphHit(
                      CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t));
        });
    helper.runAtTickTime(
        85,
        () -> {
          var v = vehicle[0];
          helper.assertTrue(Boarding.ready(level, v), "Sweep car did not capture");
          for (var c : v.carriers)
            helper.assertTrue(
                MagneticRuntime.audit((ServerSubLevel) container.getSubLevel(c.body)).impulses == 0,
                "Stationary magnets added motion");
          VehicleOperations.restraints(level, v, true);
          VehicleOperations.dispatch(level, v);
          var parent = (ServerSubLevel) container.getSubLevel(v.chassis);
          physics.addLinearAndAngularVelocity(
              parent,
              new Vector3d(0, 0, 40).sub(physics.getLinearVelocity(parent, new Vector3d())),
              new Vector3d());
          phase[0] = 1;
        });
    helper.onEachTick(
        () -> {
          if (phase[0] < 1 || phase[0] > 2) return;
          var v = vehicle[0];
          var parent = (ServerSubLevel) container.getSubLevel(v.chassis);
          var point = CoasterCartResolve.cartCenterWorld(parent);
          if (phase[0] == 1 && point.z > 78) {
            physics.addLinearAndAngularVelocity(
                parent,
                new Vector3d(0, 0, -40).sub(physics.getLinearVelocity(parent, new Vector3d())),
                new Vector3d());
            phase[0] = 2;
          } else if (phase[0] == 2 && point.z < 50) {
            VehicleOperations.capture(level, v);
            phase[0] = 3;
          }
        });
    helper.runAtTickTime(
        310,
        () -> {
          var v = vehicle[0];
          helper.assertTrue(phase[0] == 3, "Native sweep did not pass forward and backward");
          for (int i = 0; i < 4; i++) {
            var audit =
                MagneticRuntime.audit(
                    (ServerSubLevel) container.getSubLevel(v.carriers.get(i).body));
            System.out.println(
                "FD02_SWEEP group="
                    + i
                    + " distance="
                    + audit.weightedDistance
                    + " impulses="
                    + audit.impulses
                    + " energy="
                    + audit.energyChange);
            helper.assertTrue(
                Math.abs(audit.weightedDistance - (i == 0 ? .075 : 0)) < 1e-6,
                "Narrow span was skipped or replayed");
            helper.assertTrue(
                i == 0 ? audit.impulses >= 2 : audit.impulses == 0, "Carrier target mask failed");
          }
          container.removeSubLevel(container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
          helper.succeed();
        });
  }
}
