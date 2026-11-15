package dev.fourthdimension.integration;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** Fenced optional longitudinal forces. Ordinary carts retain the addon implementation. */
public final class OptionalDrive {
  private OptionalDrive(){}
  private static FoundationData.Vehicle vehicle(ServerLevel l,ServerSubLevel body){
    return FoundationData.get(l).vehicles.get(body.getUniqueId());
  }
  private static boolean managed(ServerLevel l,ServerSubLevel body){
    return vehicle(l,body)!=null||FoundationData.get(l).vehicles.values().stream().anyMatch(v->body.getUniqueId().equals(v.recoveryCart));
  }
  public static boolean allowed(ServerLevel l,ServerSubLevel body,String provider){
    var v=vehicle(l,body);
    if(v==null||v.authority.owner==null||!provider.equals(v.authority.provider)||PropulsionInterlock.holds(body,l))return false;
    try{return VehicleOperations.consist(l,v).stream().allMatch(c->c.restraints&&!c.stopping&&!c.maintenance
        &&!c.alignmentRequired&&c.fault.isEmpty()&&c.recoveryCart==null);}
    catch(IllegalArgumentException unavailable){return false;}
  }
  public static void validateRelease(ServerLevel l,FoundationData.Vehicle v){
    if(!OptionalIntegrations.installed(OptionalIntegrations.MAGNETIZED).mayInspect())throw new IllegalArgumentException("Supported Magnetized installation required");
    var edge=dev.fourthdimension.vehicle.WingVehicle.is(v)?dev.fourthdimension.vehicle.WingTrack.support(l,v)
        :FoundationData.get(l).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).map(r->dev.fourthdimension.track.TrackGeometry.resolve(l,r)).filter(java.util.Objects::nonNull).findFirst().orElse(null);
    if(edge==null||(double)OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.MagnetBoost","accelerationFor",l,edge)<=0)
      throw new IllegalArgumentException("Dispatch requires powered Magnetized launch equipment on the current section");
  }
  public static boolean fenceMagnetized(ServerSubLevel body,ServerLevel l,SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit,CoasterPathTrackFrame.TrackBasis basis,double dt){return managed(l,body);}
  public static boolean magnetized(ServerSubLevel body,ServerLevel l,SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit,CoasterPathTrackFrame.TrackBasis basis,double dt){
    if(!managed(l,body))return false;
    if(!OptionalIntegrations.installed(OptionalIntegrations.MAGNETIZED).mayInspect()
        ||!allowed(l,body,RideAuthority.MAGNETIZED)||hit==null||basis==null||!Double.isFinite(dt)||dt<=0)return true;
    var v=vehicle(l,body);
    try{
      boolean equipment=false,powered=false;
      for(var pos:java.util.List.of(hit.edge().from(),hit.edge().to())){
        var mode=OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.MagnetizedAnchors","modeOf",l,pos);
        if((boolean)OptionalCalls.call(mode,"isActive")){equipment=true;powered|=l.hasNeighborSignal(pos);}
      }
      if(!equipment)return true; // Coasting between equipped sections is intentional.
      if(!powered){RideAuthority.suspend(l,v,"Magnetized power lost; stop and explicitly resume at station");return true;}
      double acceleration=(double)OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.MagnetBoost","accelerationFor",l,hit.edge());
      double max=(double)OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.MagnetBoost","maxSpeed");
      if(!Double.isFinite(acceleration)||!Double.isFinite(max)||max<=0)throw new IllegalArgumentException("Invalid Magnetized settings");
      var tangent=new Vector3d(basis.forward().x,basis.forward().y,basis.forward().z).normalize();
      var pipeline=physics.getPipeline();double speed=pipeline.getLinearVelocity(body,new Vector3d()).dot(tangent);
      // Direction is explicitly selected while held, including a stationary start and rollback.
      double target=acceleration<0?0:v.authority.direction*Math.min(12,max);
      double cap=dev.fourthdimension.vehicle.S2SpinningVehicle.is(v)?3:12;
      double delta=Math.clamp(target-speed,-Math.min(cap,Math.abs(acceleration))*Math.min(dt,.1),Math.min(cap,Math.abs(acceleration))*Math.min(dt,.1));
      if(dev.fourthdimension.vehicle.S2SpinningVehicle.is(v))v.s2.rampSpeed=Math.abs(speed+delta);
      pipeline.addLinearAndAngularVelocity(body,tangent.mul(delta),new Vector3d());
    }catch(IllegalArgumentException unavailable){RideAuthority.suspend(l,v,unavailable.getMessage());}
    return true;
  }
  public static boolean ctnb(ServerSubLevel body,ServerLevel l,SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit,CoasterPathTrackFrame.TrackBasis basis,double dt){
    // The validated adapter runs once at the Chain seam; suppress duplicate addon and driver forces.
    return managed(l,body);
  }
}
