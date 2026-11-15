package dev.fourthdimension.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Whole Flying envelope; only the native rail and upright drive-port contacts are intentional. */
public final class FlyingClearance {
  private FlyingClearance() {}

  /** Returns the first blocked or unavailable world position in the swept envelope. */
  public static BlockPos firstObstruction(
      net.minecraft.world.level.Level level,
      Vec3 center,
      Vec3 right,
      Vec3 up,
      Vec3 forward,
      double x,
      double y,
      double z, dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit hit) {
    return firstObstruction(level,center,right,up,forward,x,y,z,hit,2);
  }

  /** Family-specific shaft reach; ordinary blocks and banked drive ports remain obstacles. */
  public static BlockPos firstObstruction(
      net.minecraft.world.level.Level level, Vec3 center, Vec3 right, Vec3 up, Vec3 forward,
      double x, double y, double z,
      dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit hit,
      int allowedDriveShaftDistance) {
    if(allowedDriveShaftDistance<0||allowedDriveShaftDistance>3)
      throw new IllegalArgumentException("Unsupported Flying drive shaft reach");
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
      if(intentionalContact(level,pos,hit,allowedDriveShaftDistance))continue;
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
  private static boolean intentionalContact(net.minecraft.world.level.Level level,BlockPos pos,
      dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit hit,int allowedDriveShaftDistance){
    var state=level.getBlockState(pos);
    for(var anchorPos:java.util.List.of(hit.edge().from(),hit.edge().to())){
      if(!(level.getBlockEntity(anchorPos) instanceof dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity anchor))continue;
      if(pos.equals(anchorPos))return true;
      if(!anchor.hasChainLiftShaft()||!com.simibubi.create.AllBlocks.SHAFT.has(state))continue;
      var axis=anchor.getChainLiftShaftAxis();
      if(axis==net.minecraft.core.Direction.Axis.Y||state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)!=axis)continue;
      var basis=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.trackBasisAtHit(level,
          new dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit(
              dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.sampleEdgePosition(level,hit.edge(),anchorPos.equals(hit.edge().from())?0:1),hit.edge(),anchorPos.equals(hit.edge().from())?0:1));
      if(basis.up().y<.999)continue;
      var delta=pos.subtract(anchorPos);int along=axis==net.minecraft.core.Direction.Axis.X?delta.getX():delta.getZ();
      if(delta.getY()==0&&Math.abs(along)>=1&&Math.abs(along)<=allowedDriveShaftDistance
          &&(axis==net.minecraft.core.Direction.Axis.X?delta.getZ():delta.getX())==0)return true;
    }return false;
  }
}
