package dev.fourthdimension.integration;

import com.simibubi.create.foundation.blockEntity.behaviour.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** Selected CTNB dials and sensors, with shared validated capture/release instead of upstream pushes. */
public final class CtnbDrive {
  private static final String API="dev.notzyvex.coasters_extras.";
  private CtnbDrive(){}
  public record Settings(double speed,int direction,boolean signal,int dwell,double launch){}
  private static Object behaviour(ServerLevel l,BlockPos p,String name){
    try {return BlockEntityBehaviour.get(l,p,(BehaviourType<?>)Class.forName(API+name).getField("TYPE").get(null));}
    catch(ReflectiveOperationException e){throw new IllegalArgumentException("CTNB control API unavailable",e);}
  }
  private static int field(Object o,String name){try{return o.getClass().getField(name).getInt(o);}catch(ReflectiveOperationException e){throw new IllegalArgumentException("CTNB dial unavailable",e);}}
  public static Settings settings(ServerLevel l,CoasterPathEdge e){
    double speed=4,launch=4;int direction=0,dwell=60;boolean signal=false,read=false;
    for(var p:List.of(e.from(),e.to())){
      var dial=behaviour(l,p,"StationBoostBehaviour");
      if(dial!=null){if(!read){int value=field(dial,"value");speed=Math.clamp(value,0,12);dwell=Math.clamp(value>0?value*20:60,20,1200);read=true;}launch=Math.max(launch,Math.min(12,field(dial,"launch")));}
      var send=behaviour(l,p,"SendDirectionBehaviour");if(direction==0&&send!=null)direction=(int)OptionalCalls.call(send,"sign");
      var trigger=behaviour(l,p,"LaunchTriggerBehaviour");if(trigger!=null)signal|=(boolean)OptionalCalls.call(trigger,"needsSignal");
    }
    return new Settings(speed,direction,signal,dwell,launch);
  }
  private static boolean powered(ServerLevel l,CoasterPathEdge e){return l.hasNeighborSignal(e.from())||l.hasNeighborSignal(e.to());}
  public static CoasterPathEdge support(ServerLevel l,FoundationData.Vehicle v){
    if(WingVehicle.is(v))return WingTrack.support(l,v);
    return FoundationData.get(l).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).map(r->TrackGeometry.resolve(l,r)).filter(Objects::nonNull).findFirst().orElse(null);
  }
  private static String kind(ServerLevel l,CoasterPathEdge e){var m=e==null?null:CtnbTracks.material(l,e);return CtnbTracks.selected(m)?m.id.getPath():"";}
  private static boolean allOn(ServerLevel l,FoundationData.Vehicle v,CoasterPathEdge e){return VehicleOperations.consist(l,v).stream().allMatch(c->c.boundFrom!=null&&(c.boundFrom.equals(e.from())&&c.boundTo.equals(e.to())||c.boundFrom.equals(e.to())&&c.boundTo.equals(e.from())));}
  /** Remote release is available only on a complete selected station/reverse span. */
  public static boolean operatingStop(ServerLevel l,FoundationData.Vehicle v){var e=support(l,v);var k=kind(l,e);return (k.equals("station_track")||k.equals("reverse_track"))&&allOn(l,v,e);}
  public static void validateRelease(ServerLevel l,FoundationData.Vehicle v){
    if(!OptionalIntegrations.installed(OptionalIntegrations.CTNB).mayInspect()||!CtnbTracks.supports(v))throw new IllegalArgumentException("Supported CTNB installation and car required");
    var e=support(l,v);var k=kind(l,e);if(!Set.of("boost_track","powered_boost_track","launch_track","station_track","reverse_track").contains(k))throw new IllegalArgumentException("Dispatch from selected CTNB launch/station/reverse track");
    if(!allOn(l,v,e))throw new IllegalArgumentException("Fit the entire train on the operating section");
    var settings=settings(l,e);
    if(k.equals("station_track")){
      if(powered(l,e))throw new IllegalArgumentException("CTNB station redstone hold is active");
      if(VehicleOperations.consist(l,v).stream().anyMatch(c->c.ctnb.remaining>0))throw new IllegalArgumentException("Wait for CTNB station dwell");
    }else if((k.equals("powered_boost_track")||settings.signal)&&!powered(l,e))throw new IllegalArgumentException("CTNB launch signal is absent");
    if(settings.direction!=0&&settings.direction!=v.authority.direction)throw new IllegalArgumentException("Match the stopped ride direction to the CTNB direction dial before dispatch");
  }
  public static void released(ServerLevel l,FoundationData.Vehicle v){
    var e=support(l,v);for(var c:VehicleOperations.consist(l,v)){c.ctnb.from=e.from();c.ctnb.to=e.to();c.ctnb.kind=kind(l,e);c.ctnb.departing=true;}
  }
  public static void apply(ServerSubLevel body,ServerLevel l,SubLevelPhysicsSystem physics,CoasterPathTrackFrame.GraphHit hit,CoasterPathTrackFrame.TrackBasis basis,double dt){
    var v=FoundationData.get(l).vehicles.get(body.getUniqueId());
    if(v==null||!RideAuthority.CTNB.equals(v.authority.provider)||hit==null||basis==null||!Double.isFinite(dt)||dt<=0)return;
    try{
      if(!OptionalIntegrations.installed(OptionalIntegrations.CTNB).mayInspect()||!CtnbTracks.eligible(l,v,hit.edge()))throw new IllegalArgumentException("CTNB track/provider is unavailable or unqualified");
      var k=kind(l,hit.edge());var pipeline=physics.getPipeline();var tangent=new Vector3d(basis.forward().x,basis.forward().y,basis.forward().z).normalize();double speed=pipeline.getLinearVelocity(body,new Vector3d()).dot(tangent);
      OptionalCalls.staticCall(API+"net.RideTelemetry","report",hit.edge().from(),hit.edge().to(),body.getRuntimeId(),Math.abs(speed),l.getGameTime());
      if(k.equals("sensor_track"))OptionalCalls.staticCall(API+"net.SensorTracker","report",hit.edge().from(),hit.edge().to(),l.getGameTime());
      if(k.isEmpty()){v.ctnb.departing=false;v.ctnb.from=null;v.ctnb.to=null;return;} // Native joins coast.
      var settings=settings(l,hit.edge());var s=v.ctnb;
      if(!s.matches(hit.edge().from(),hit.edge().to())){s.from=hit.edge().from();s.to=hit.edge().to();s.kind=k;s.departing=false;s.remaining=0;}
      if(k.equals("station_track")||k.equals("reverse_track")){
        if(!s.departing&&!v.stopping&&allOn(l,v,hit.edge())){
          VehicleOperations.capture(l,v);
          for(var car:VehicleOperations.consist(l,v)){car.ctnb.from=hit.edge().from();car.ctnb.to=hit.edge().to();car.ctnb.kind=k;car.ctnb.remaining=k.equals("station_track")?settings.dwell:0;car.ctnb.departing=false;}
          v.authority.reason=k.equals("station_track")?"CTNB station captured; wait for dwell and explicitly Dispatch":"CTNB reverse captured; select direction and explicitly Dispatch";
          FoundationData.get(l).setDirty();
        }
        if(s.sampled!=l.getGameTime()){s.sampled=l.getGameTime();if(v.stopping&&Math.abs(speed)<.05&&s.remaining>0){s.remaining--;FoundationData.get(l).setDirty();}}
        if(k.equals("station_track"))OptionalCalls.staticCall(API+"net.StationTracker","report",hit.edge().from(),hit.edge().to(),(byte)(v.stopping?1:2),s.remaining,settings.dwell,l.getGameTime());
      }
      if(!OptionalDrive.allowed(l,body,RideAuthority.CTNB))return;
      if((k.equals("powered_boost_track")||settings.signal&&!k.equals("station_track"))&&!powered(l,hit.edge()))throw new IllegalArgumentException("CTNB launch signal lost; explicitly resume");
      if(settings.direction!=0&&settings.direction!=v.authority.direction)throw new IllegalArgumentException("CTNB direction changed while released; stop and select direction");
      double target=v.authority.direction*(settings.speed>0?settings.speed:4),rate=8;
      if(k.equals("sensor_track"))return;
      if(k.equals("brake_track")){target=Math.copySign(Math.min(Math.abs(speed),settings.speed),speed);rate=4;}
      if(k.equals("station_track")){if(powered(l,hit.edge())){VehicleOperations.capture(l,v);return;}target=v.authority.direction*settings.launch;}
      double delta=Math.clamp(target-speed,-rate*Math.min(dt,.1),rate*Math.min(dt,.1));
      pipeline.addLinearAndAngularVelocity(body,tangent.mul(delta),new Vector3d());
    }catch(IllegalArgumentException unavailable){RideAuthority.suspend(l,v,unavailable.getMessage());}
  }
}
