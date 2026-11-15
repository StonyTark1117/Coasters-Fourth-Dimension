package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Qualified overhead contacts with rigid below-track reservation, including native full roll. */
public final class PipelineTrack {
  private static final Map<CoasterPathEdge,Boolean> SHAPES=new WeakHashMap<>();
  private PipelineTrack(){}
  public static String rejection(Level level,CoasterPathEdge edge){
    if(CoasterPathGraphManager.edgeHostedOnSubLevel(level,edge))return "Pipeline requires fixed world track";
    var data=EligibilityHooks.data(level);
    var rail=data==null?null:data.rails.stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElse(null);
    if(rail==null||!rail.valid||rail.family!=Family.PIPELINE||!PipelineVehicle.VARIANT.equals(rail.variant))
      return "Pipeline requires matching side-rail track and dock capability";
    if(!TrackDesign.PIPELINE.nativeFit().accepts(PipelineVehicle.FIT))return "Pipeline contact/mount/envelope mismatch";
    return SHAPES.computeIfAbsent(edge,e->shapeFits(level,e))?null:"Pipeline needs radius >=16 and roll change <=5 degrees/block";
  }
  private static boolean shapeFits(Level level,CoasterPathEdge edge){
    Vec3 last=null,tangent=null,up=null;
    for(int i=0;i<=256;i++){
      double t=i/256.0;var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var b=CoasterPathTrackFrame.trackBasisAtHit(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(last!=null){
        double ds=last.distanceTo(point);if(ds<1e-7)return false;
        double bend=Math.acos(Math.clamp(tangent.dot(b.forward()),-1,1));
        if(bend/ds>1.0/16+1e-4)return false;
        // Parallel-transport previous up by the tangent change, then measure only local roll.
        var a=new org.joml.Vector3d(tangent.x,tangent.y,tangent.z);
        var z=new org.joml.Vector3d(b.forward().x,b.forward().y,b.forward().z);
        var q=new org.joml.Quaterniond().rotationTo(a,z);
        var transported=new org.joml.Vector3d(up.x,up.y,up.z).rotate(q);
        if(Math.acos(Math.clamp(transported.dot(b.up().x,b.up().y,b.up().z),-1,1))/ds>Math.toRadians(5)+1e-4)return false;
      }
      last=point;tangent=b.forward();up=b.up();
    }return true;
  }
  public static BlockPos obstruction(Level level,CoasterPathTrackFrame.GraphHit hit){
    var b=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    // Full reserved volume, except declared native guide/drive-port contacts.
    return FlyingClearance.firstObstruction(level,hit.point().add(b.up().scale(-.75)),
        b.right(),b.up(),b.forward(),2.05,1.75,2.75,hit);
  }
}
