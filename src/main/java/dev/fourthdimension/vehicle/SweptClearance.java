package dev.fourthdimension.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Conservative whole-wing envelope used by placement and visual previews. */
public final class SweptClearance {
  private SweptClearance() {}

  public static boolean obstructed(
      net.minecraft.world.level.Level level,
      Vec3 center,
      Vec3 right,
      Vec3 up,
      Vec3 forward,
      double x,
      double y,
      double z) {
    return firstObstruction(level, center, right, up, forward, x, y, z) != null;
  }

  /** Returns the first blocked or unavailable world position in the swept envelope. */
  public static BlockPos firstObstruction(
      net.minecraft.world.level.Level level,
      Vec3 center,
      Vec3 right,
      Vec3 up,
      Vec3 forward,
      double x,
      double y,
      double z) {
    Vec3 extent =
        new Vec3(
            Math.abs(right.x) * x + Math.abs(up.x) * y + Math.abs(forward.x) * z,
            Math.abs(right.y) * x + Math.abs(up.y) * y + Math.abs(forward.y) * z,
            Math.abs(right.z) * x + Math.abs(up.z) * y + Math.abs(forward.z) * z);
    for (var pos :
        BlockPos.betweenClosed(
            BlockPos.containing(center.subtract(extent)),
            BlockPos.containing(center.add(extent)))) {
      if (!level.hasChunkAt(pos)) return pos.immutable();
      var shape = level.getBlockState(pos).getCollisionShape(level, pos);
      if (shape.isEmpty()) continue;
      for (var local : shape.toAabbs()) {
        var block = local.move(pos);
        double[] min = {
          Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
        };
        double[] max = {
          Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY
        };
        for (int i = 0; i < 8; i++) {
          var p =
              new Vec3(
                      (i & 1) == 0 ? block.minX : block.maxX,
                      (i & 2) == 0 ? block.minY : block.maxY,
                      (i & 4) == 0 ? block.minZ : block.maxZ)
                  .subtract(center);
          double[] projection = {p.dot(right), p.dot(up), p.dot(forward)};
          for (int axis = 0; axis < 3; axis++) {
            min[axis] = Math.min(min[axis], projection[axis]);
            max[axis] = Math.max(max[axis], projection[axis]);
          }
        }
        // Conservative oriented-box overlap: false positives at corners are preferable to a missed
        // swept obstacle.
        if (min[0] < x && max[0] > -x && min[1] < y && max[1] > -y && min[2] < z && max[2] > -z)
          return pos.immutable();
      }
    }
    return null;
  }
}
