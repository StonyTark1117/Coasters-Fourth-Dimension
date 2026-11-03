package dev.fourthdimension.gametest;

import dev.fourthdimension.vehicle.RecoveryExamples;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3d;

/** Actual chain callbacks change native velocity, not the Engineered force accumulator. */
final class ChainAvailabilityProbe {
  private ChainAvailabilityProbe() {}

  /** Register once; later stages never mutate GameTestInfo's scheduled-callback map. */
  static final class Steps {
    private record Step(int due, long order, Runnable action) {}

    private final PriorityQueue<Step> pending = new PriorityQueue<>(
        Comparator.comparingInt(Step::due).thenComparingLong(Step::order));
    private int tick;
    private long order;

    Steps(GameTestHelper helper) {
      helper.onEachTick(() -> {
        tick++;
        while (!pending.isEmpty() && pending.peek().due() <= tick)
          pending.remove().action().run();
      });
    }

    void after(int ticks, Runnable action) {
      if (ticks <= 0) throw new IllegalArgumentException("A chain probe stage needs a future tick");
      pending.add(new Step(tick + ticks, order++, action));
    }
  }

  static void power(GameTestHelper helper, RecoveryExamples.Example fixture, BlockPos origin) {
    var level = helper.getLevel();
    var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(origin);
    fixture.motor().generatedSpeed.setValue(8 * anchor.getChainLiftTravelSign());
    level.setBlock(fixture.powerLever(), level.getBlockState(fixture.powerLever())
        .setValue(BlockStateProperties.POWERED, false), 3);
  }

  static void stop(GameTestHelper helper, RecoveryExamples.Example fixture) {
    fixture.motor().generatedSpeed.setValue(0);
    var level = helper.getLevel();
    level.setBlock(fixture.powerLever(), level.getBlockState(fixture.powerLever())
        .setValue(BlockStateProperties.POWERED, true), 3);
  }

  static double measure(GameTestHelper helper, ServerSubLevel source, List<UUID> ids,
      String group, int index, String sourceLabel, String state) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var physics = container.physicsSystem();
    var pipeline = physics.getPipeline();
    helper.assertTrue(source != null && !source.isRemoved()
            && container.getSubLevel(source.getUniqueId()) == source,
        "Chain source must be the same live native body");
    var bodies = ids.stream().distinct().map(container::getSubLevel)
        .filter(Objects::nonNull).map(body -> (ServerSubLevel) body).toList();
    helper.assertTrue(bodies.contains(source), "Chain source omitted from velocity snapshots");
    var center = CoasterCartResolve.cartCenterWorld(source);
    var hit = CoasterPathTrackFrame.nearestGraphHit(level, CoasterPathGraphManager.get(level),
        new net.minecraft.world.phys.Vec3(center.x, center.y, center.z), 2);
    helper.assertTrue(hit != null, "Chain probe source has no real graph hit");
    var span = ChainLiftSpanQuery.findAtGraphHit(level, hit, 0);
    helper.assertTrue(span != null, "Chain callback would return without a real native span");
    double target = ChainLiftDriveDirection.signedChainBlocksPerSecond(
        span.anchor(), span.path(), 0, 256, 16);
    helper.assertTrue(Double.isFinite(target) && Math.abs(target) > .001,
        "Chain callback requires an actually powered native span");
    var beforeLinear = bodies.stream()
        .map(body -> pipeline.getLinearVelocity(body, new Vector3d())).toList();
    var beforeAngular = bodies.stream()
        .map(body -> pipeline.getAngularVelocity(body, new Vector3d())).toList();
    System.out.println("FD_CHAIN_AVAILABILITY_INPUT group=" + group + " case=" + index
        + " source=" + sourceLabel + " state=" + state
        + " bodies=" + bodies.stream().map(ServerSubLevel::getUniqueId).toList()
        + " originalLinear=" + beforeLinear.stream().map(v -> List.of(v.x, v.y, v.z)).toList()
        + " originalAngular=" + beforeAngular.stream().map(v -> List.of(v.x, v.y, v.z)).toList());
    double linearDelta = 0;
    double angularDelta = 0;
    try {
      // A healthy source already at target speed would legitimately receive no speed match.
      // Use the same explicit at-rest input for all probes; restore original motion afterward.
      for (int i = 0; i < bodies.size(); i++)
        pipeline.addLinearAndAngularVelocity(bodies.get(i),
            new Vector3d(beforeLinear.get(i)).negate(),
            new Vector3d(beforeAngular.get(i)).negate());
      var baselineLinear = bodies.stream()
          .map(body -> pipeline.getLinearVelocity(body, new Vector3d())).toList();
      var baselineAngular = bodies.stream()
          .map(body -> pipeline.getAngularVelocity(body, new Vector3d())).toList();
      helper.assertTrue(baselineLinear.stream().allMatch(v -> v.length() < 1e-7)
              && baselineAngular.stream().allMatch(v -> v.length() < 1e-7),
          "Native velocity getters did not observe the at-rest probe baseline");
      CoasterCartChainLiftDrive.applySpeedMatch(source, level, physics, hit,
          CoasterPathTrackFrame.trackBasisAtHit(level, hit), .05);
      for (int i = 0; i < bodies.size(); i++) {
        linearDelta = Math.max(linearDelta,
            pipeline.getLinearVelocity(bodies.get(i), new Vector3d()).distance(baselineLinear.get(i)));
        angularDelta = Math.max(angularDelta,
            pipeline.getAngularVelocity(bodies.get(i), new Vector3d()).distance(baselineAngular.get(i)));
      }
    } finally {
      for (int i = 0; i < bodies.size(); i++) {
        var body = bodies.get(i);
        pipeline.addLinearAndAngularVelocity(body,
            new Vector3d(beforeLinear.get(i)).sub(pipeline.getLinearVelocity(body, new Vector3d())),
            new Vector3d(beforeAngular.get(i)).sub(pipeline.getAngularVelocity(body, new Vector3d())));
        helper.assertTrue(pipeline.getLinearVelocity(body, new Vector3d())
                    .distance(beforeLinear.get(i)) < 1e-7
                && pipeline.getAngularVelocity(body, new Vector3d())
                    .distance(beforeAngular.get(i)) < 1e-7,
            "Native callback probe did not restore original velocity");
      }
    }
    System.out.println("FD_CHAIN_AVAILABILITY group=" + group + " case=" + index
        + " source=" + sourceLabel + " state=" + state + " targetSpeed=" + target
        + " maxLinearDelta=" + linearDelta + " maxAngularDelta=" + angularDelta
        + " zeroVelocityBaseline=true velocityRestored=true");
    return Math.max(linearDelta, angularDelta);
  }
}
