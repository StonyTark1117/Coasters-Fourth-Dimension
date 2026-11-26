package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Suspended N contacts plus its finite route and full-turn passenger reservation. */
public final class SuspendedTrack {
  private static final Map<CoasterPathEdge,Boolean> SHAPES=new WeakHashMap<>();
  private SuspendedTrack() {}
  public static String rejection(Level level,CoasterPathEdge edge) {
    if(CoasterPathGraphManager.edgeHostedOnSubLevel(level,edge))return "Suspended requires fixed world track";
    var data=EligibilityHooks.data(level);
    var rail=data==null?null:dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElse(null);
    if(rail==null || !rail.valid || !SuspendedVehicle.VARIANT.equals(rail.variant)
        || rail.family!=dev.fourthdimension.contract.Family.SUSPENDED)
      return "Suspended requires matching conventional Suspended track and capture capability";
    if(!TrackDesign.SUSPENDED.nativeFit().accepts(SuspendedVehicle.FIT))return "Suspended contact/mount/envelope mismatch";
    return SHAPES.computeIfAbsent(edge,e->shapeFits(level,e))?null:
        "Suspended needs radius >=16, bank <=30 and slope <=60 degrees and no inversions";
  }
  private static boolean shapeFits(Level level,CoasterPathEdge edge) {
    Vec3 previous=null,tangent=null;
    for(int i=0;i<=128;i++) {
      double t=i/128.0;var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var b=CoasterPathTrackFrame.trackBasisAtHit(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      double horizontal=Math.sqrt(b.forward().x*b.forward().x+b.forward().z*b.forward().z);
      if(horizontal<.5-1e-6 || b.up().y/horizontal<Math.cos(Math.PI/6)-1e-6)return false;
      if(previous!=null) {
        double ds=previous.distanceTo(point);
        if(ds<1e-6 || Math.acos(Math.clamp(tangent.dot(b.forward()),-1,1))/ds>1.0/16+1e-4)return false;
      }
      previous=point;tangent=b.forward();
    }
    return true;
  }
  public static BlockPos obstruction(Level level,CoasterPathTrackFrame.GraphHit hit) {
    var b=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    // Exclude native running contacts; reserve the entire below-rail cabin sweep.
    return SweptClearance.firstObstruction(level,hit.point().add(b.up().scale(-2.8)),
        b.right(),b.up(),b.forward(),3,1.9,1.75);
  }
}
