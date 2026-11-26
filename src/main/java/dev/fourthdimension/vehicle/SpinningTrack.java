package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** S1 N contacts plus its finite route and full-turn passenger reservation. */
public final class SpinningTrack {
  private static final Map<CoasterPathEdge,Boolean> SHAPES=new WeakHashMap<>();
  private SpinningTrack() {}
  public static String rejection(Level level,CoasterPathEdge edge) {
    if(CoasterPathGraphManager.edgeHostedOnSubLevel(level,edge))return "Classic Spinning requires fixed world track";
    var data=EligibilityHooks.data(level);
    var rail=data==null?null:dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElse(null);
    if(rail==null || !rail.valid || !SpinningVehicle.VARIANT.equals(rail.variant)
        || rail.family!=dev.fourthdimension.contract.Family.SPINNING)
      return "Classic Spinning requires matching track and capture capability";
    if(!TrackDesign.SPINNING.nativeFit().accepts(SpinningVehicle.FIT))return "Classic Spinning contact/mount/envelope mismatch";
    return SHAPES.computeIfAbsent(edge,e->shapeFits(level,e))?null:
        "Classic Spinning needs radius >=12, bank/slope <=60 degrees and no inversions";
  }
  private static boolean shapeFits(Level level,CoasterPathEdge edge) {
    Vec3 previous=null,tangent=null;
    for(int i=0;i<=128;i++) {
      double t=i/128.0;var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var b=CoasterPathTrackFrame.trackBasisAtHit(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      double horizontal=Math.sqrt(b.forward().x*b.forward().x+b.forward().z*b.forward().z);
      if(horizontal<.5-1e-6 || b.up().y/horizontal<.5-1e-6)return false;
      if(previous!=null) {
        double ds=previous.distanceTo(point);
        if(ds<1e-6 || Math.acos(Math.clamp(tangent.dot(b.forward()),-1,1))/ds>1.0/12+1e-4)return false;
      }
      previous=point;tangent=b.forward();
    }
    return true;
  }
  public static BlockPos obstruction(Level level,CoasterPathTrackFrame.GraphHit hit) {
    var b=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    // Native rail, anchor and bearing contacts are below the rotating passenger volume.
    return SweptClearance.firstObstruction(level,hit.point().add(b.up().scale(2.375)),
        b.right(),b.up(),b.forward(),2.25,1.275,2.25);
  }
}
