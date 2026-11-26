package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Native N contacts with the straddle occupied envelope and bounded curvature. */
public final class MotorbikeTrack {
  private static final Map<CoasterPathEdge,Boolean> SHAPES=new WeakHashMap<>();
  private MotorbikeTrack(){}
  public static String rejection(Level level,CoasterPathEdge edge){
    if(CoasterPathGraphManager.edgeHostedOnSubLevel(level,edge))return "Motorbike requires fixed world track";
    if(edge.bezier()==null||!edge.bezier().getMaterial().id.equals(dev.silvergold.simulatedcoasters.CoasterTrackMaterials.COASTER.id))return "Motorbike requires native Coasters N-contact material";
    var data=EligibilityHooks.data(level);
    var rail=data==null?null:dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElse(null);
    if(rail==null||!rail.valid||rail.family!=Family.MOTORBIKE||!MotorbikeVehicle.VARIANT.equals(rail.variant))
      return "Motorbike requires matching Motorbike Track (motorbike/straddle_v1)";
    if(!TrackDesign.MOTORBIKE.nativeFit().accepts(MotorbikeVehicle.FIT))return "Motorbike contact/mount/envelope mismatch";
    return SHAPES.computeIfAbsent(edge,e->shapeFits(level,e))?null:"Motorbike needs radius >=16 and frame change <=0.05 rad/block, grade <=30 and bank <=45 degrees";
  }
  public static CoasterPathEdge support(Level level,FoundationData.Vehicle v){
    var data=EligibilityHooks.data(level);if(data==null)return null;
    var rail=dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
    var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    return edge!=null&&rejection(level,edge)==null?edge:null;
  }
  private static boolean shapeFits(Level level,CoasterPathEdge edge){
    Vec3 last=null,tangent=null,up=null;
    for(int i=0;i<=256;i++){
      double t=i/256.0;var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var b=CoasterPathTrackFrame.trackBasisAtHit(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(Math.abs(b.forward().y)>.50001||b.up().y<Math.cos(Math.toRadians(45))-.0001)return false;
      if(last!=null){
        double ds=last.distanceTo(point);if(ds<1e-7)return false;
        double bend=Math.acos(Math.clamp(tangent.dot(b.forward()),-1,1));
        if(bend/ds>1.0/16+1e-4)return false;
        // Parallel-transport previous up by the tangent change, then measure only local roll.
        var a=new org.joml.Vector3d(tangent.x,tangent.y,tangent.z);
        var z=new org.joml.Vector3d(b.forward().x,b.forward().y,b.forward().z);
        var q=new org.joml.Quaterniond().rotationTo(a,z);
        var transported=new org.joml.Vector3d(up.x,up.y,up.z).rotate(q);
        if(Math.acos(Math.clamp(transported.dot(b.up().x,b.up().y,b.up().z),-1,1))/ds>0.05+1e-4)return false;
      }
      last=point;tangent=b.forward();up=b.up();
    }return true;
  }
  public static BlockPos obstruction(Level level,CoasterPathTrackFrame.GraphHit hit){
    var b=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    // Full reserved volume, except declared native guide/drive-port contacts.
    return FlyingClearance.firstObstruction(level,hit.point().add(b.up().scale(1.55)),
        b.right(),b.up(),b.forward(),1.85,1.10,1.50,hit);
  }
}
