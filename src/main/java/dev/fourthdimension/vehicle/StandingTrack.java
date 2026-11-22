package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Above-rail original standing envelope; no inversion or excessive bank/pitch. */
public final class StandingTrack {
  private static final Map<CoasterPathEdge,Boolean> SHAPES=new WeakHashMap<>();
  private StandingTrack(){}
  public static String rejection(Level level,CoasterPathEdge edge){
    if(CoasterPathGraphManager.edgeHostedOnSubLevel(level,edge))return "Standing requires fixed world track";
    var data=EligibilityHooks.data(level);
    var rail=data==null?null:data.rails.stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElse(null);
    if(rail==null||!rail.valid||rail.family!=Family.STANDING||!(StandingVehicle.STANDARD.equals(rail.variant)||StandingVehicle.SURF.equals(rail.variant)))
      return "Standing requires matching standing track and dock capability";
    if(!(StandingVehicle.SURF.equals(rail.variant)?TrackDesign.STANDING_SURF:TrackDesign.STANDING_STANDARD).nativeFit().accepts(StandingVehicle.FIT))return "Standing contact/mount/envelope mismatch";
    return SHAPES.computeIfAbsent(edge,e->shapeFits(level,e))?null:"Standing needs radius >=16, pitch/bank <=45 degrees and roll change <=5 degrees/block";
  }
  public static String rejection(Level level,CoasterPathEdge edge,TrackDesign design){
    String result=rejection(level,edge);if(result!=null)return result;var data=EligibilityHooks.data(level);var rail=data.rails.stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElseThrow();
    return rail.variant.equals(design==TrackDesign.STANDING_SURF?StandingVehicle.SURF:StandingVehicle.STANDARD)?null:"Standing configuration requires its exact matching track variant";
  }
  private static boolean shapeFits(Level level,CoasterPathEdge edge){
    Vec3 last=null,tangent=null,up=null;
    for(int i=0;i<=256;i++){
      double t=i/256.0;var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var b=CoasterPathTrackFrame.trackBasisAtHit(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(Math.abs(b.forward().y)>Math.sin(Math.toRadians(45))+1e-6||b.up().y<.499999)return false;
      var levelUp=new Vec3(0,1,0).subtract(b.forward().scale(b.forward().y)).normalize();
      if(levelUp.dot(b.up())<Math.cos(Math.toRadians(45))-1e-6)return false;
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
    return FlyingClearance.firstObstruction(level,hit.point().add(b.up().scale(1.525)),
        b.right(),b.up(),b.forward(),1.75,2.475,1.75,hit);
  }
}
