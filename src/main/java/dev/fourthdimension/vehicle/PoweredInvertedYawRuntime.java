package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.integration.RideAuthority;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import static dev.fourthdimension.vehicle.PoweredInvertedYawState.Phase.*;
import static dev.fourthdimension.vehicle.YawState.Mode.*;

/** One upright physical yaw joint per car; one fenced native Chain owner for travel. */
public final class PoweredInvertedYawRuntime {
  private static final class Motion { final DockCapture latch=new DockCapture();GenericConstraintHandle hold;long tick=Long.MIN_VALUE,resumeAt=Long.MIN_VALUE,releaseNotBefore=Long.MIN_VALUE;int stoppedTicks;boolean initialized;double speed,distance;YawSection.Interval interval; }
  private static final Map<ServerSubLevel,Motion> MOTION=new WeakHashMap<>();
  private PoweredInvertedYawRuntime(){}
  private static ServerSubLevel body(ServerLevel l,UUID id){var b=id==null?null:SubLevelContainer.getContainer(l).getSubLevel(id);return b instanceof ServerSubLevel s&&!s.isRemoved()?s:null;}
  private static double speed(ServerLevel l,ServerSubLevel b){return SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).length();}
  public static FoundationData.Rail rail(ServerLevel l,FoundationData.Vehicle v){return FoundationData.get(l).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);}
  public static PoweredInvertedYawGovernorEntity governor(ServerLevel l,FoundationData.Vehicle v){var p=v.poweredYaw.controller;return p!=null&&l.hasChunkAt(p)&&l.getBlockEntity(p) instanceof PoweredInvertedYawGovernorEntity g&&g.authorizesRide(v.chassis)?g:null;}
  private static List<FoundationData.Vehicle> cars(ServerLevel l,FoundationData.Vehicle v){return VehicleOperations.consist(l,v);}
  private static void changed(ServerLevel l){FoundationData.get(l).setDirty();}
  public static void event(ServerLevel l,FoundationData.Vehicle v,String kind,UUID event){
    var s=v.poweredYaw;String key=kind+":"+event;long now=l.getGameTime();Long before=s.eventTicks.get(key);if(before!=null&&now-before<20)return;
    s.eventTicks.put(key,now);while(s.eventTicks.size()>512)s.eventTicks.remove(s.eventTicks.keySet().iterator().next());s.sequence++;
    var g=governor(l,v);if(g!=null)g.emit(kind,v.chassis,event,v.authority.epoch,s.sequence);changed(l);
  }
  public static void fault(ServerLevel l,FoundationData.Vehicle selected,String reason){
    var ids=new HashSet<>(selected.poweredYaw.consist);ids.add(selected.chassis);var parent=body(l,selected.chassis);if(parent!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent));
    for(var id:ids){var v=FoundationData.get(l).vehicles.get(id);if(v==null||!PoweredInvertedYawVehicle.is(v))continue;var s=v.poweredYaw;
      if(s.phase!=FAULT){s.interruptedPhase=s.phase;s.interruptedMode=s.mode;event(l,v,"RIDE_FAULT",s.section);}s.phase=FAULT;s.mode=HOLD_FAULT;s.resumeRequired=true;s.holdAcknowledged=false;s.status=reason;v.stopping=true;v.restraints=true;v.fault=reason;v.carriers.forEach(c->{c.capture=true;c.latched=false;});
      var motion=MOTION.get(body(l,v.chassis));if(motion!=null)motion.resumeAt=Long.MIN_VALUE;
      RideAuthority.emergencyHold(l,v);
    }changed(l);
  }
  private static boolean dock(ServerLevel l,ServerSubLevel parent,FoundationData.Vehicle v){
    var s=v.poweredYaw;if(v.dock==null||s.dockFrom==null||!l.hasChunkAt(v.dock)||!(l.getBlockEntity(v.dock) instanceof BoardingStationEntity station)||!cars(l,v).stream().anyMatch(c->c.chassis.equals(station.vehicle)))return false;
    var r=rail(l,v);var edge=r==null?null:TrackGeometry.resolve(l,r);if(edge==null||!r.matches(s.dockFrom,s.dockTo)||r.geometryHash!=s.dockGeometry)return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));var table=TrackGeometry.table(l,edge);double at=hit==null?-1:table.distanceAt(hit.t());
    return table.length()>=28&&at>=1.75&&at<=table.length()-1.75&&center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))<=1600;
  }
  public static boolean ready(ServerLevel l,ServerSubLevel parent,FoundationData.Vehicle v){
    try{if(!ConsistRules.eligible(l,parent,v))return false;for(var car:cars(l,v)){var b=body(l,car.chassis);var c=car.carriers.getFirst();if(b==null||!dock(l,b,car)||!car.stopping||!car.fault.isEmpty()||car.poweredYaw.resumeRequired||car.alignmentRequired||!c.latched||!c.capture||Math.abs(c.angle)>.035||Math.abs(c.omega)>.05||speed(l,b)>.05||body(l,c.body)==null)return false;}return true;}catch(IllegalArgumentException e){return false;}
  }
  public static void rebindDock(ServerLevel l,FoundationData.Vehicle v,BlockPos pos){
    var cars=cars(l,v);if(cars.size()>4)throw new IllegalArgumentException("At most four powered yaw cars");
    for(var car:cars){var p=body(l,car.chassis);var r=rail(l,car);var edge=r==null?null:TrackGeometry.resolve(l,r);if(!PoweredInvertedYawVehicle.is(car)||p==null||!car.stopping||speed(l,p)>.05||edge==null||PoweredInvertedYawTrack.rejection(l,edge)!=null||TrackGeometry.table(l,edge).length()<28||new Vector3d(0,1,0).rotate(p.logicalPose().orientation()).y<.999)throw new IllegalArgumentException("Stop every car on a clear level28-block powered dock");}
    for(var car:cars){var r=rail(l,car);var s=car.poweredYaw;car.dock=pos.immutable();s.dockFrom=r.from;s.dockTo=r.to;s.dockGeometry=r.geometryHash;s.consist=cars.stream().map(a->a.chassis).toList();car.revision++;}changed(l);
  }
  public static String safety(ServerLevel l,ServerSubLevel parent,FoundationData.Vehicle v,FoundationData.Rail r){
    var edge=r==null?null:TrackGeometry.resolve(l,r);if(edge==null||!r.valid)return "Powered yaw route unavailable";String reason=PoweredInvertedYawTrack.rejection(l,edge);if(reason!=null)return reason;
    if(!FamilyControllers.find(parent).equals(Set.of(v.family)))return "Powered yaw chassis controller missing";
    if(!ConsistRules.eligible(l,parent,v))return "Powered yaw needs one to four matching cars";
    if(!v.poweredYaw.consist.isEmpty()&&!new HashSet<>(v.poweredYaw.consist).equals(new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent))))return "Powered yaw coupler/consist changed";
    if(body(l,v.carriers.getFirst().body)==null)return "Powered yaw carrier missing";
    if(speed(l,parent)>8.08)return "Powered travel exceeds8 blocks/s";var c=v.carriers.getFirst();if(Math.abs(c.angle)>YawController.LIMIT+.05||Math.abs(c.omega)>.65)return "Powered yaw native motion limit";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "Native bearing unavailable";var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));if(hit==null||ChainLiftSpanQuery.findAtGraphHit(l,hit,0)==null)return "Complete native powered Chain coverage required";
    var table=TrackGeometry.table(l,edge);double at=table.distanceAt(hit.t()),look=speed(l,parent)*speed(l,parent)/4+1.75;
    for(double d=Math.max(0,at-look);d<=Math.min(table.length(),at+look);d+=.5){double t=table.parameterAt(d);var obstacle=PoweredInvertedYawTrack.obstruction(l,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(l,edge,t),edge,t));if(obstacle!=null)return "Powered yaw sweep blocked at "+obstacle.toShortString();}
    var g=governor(l,v);if(g==null){if(!v.stopping)return "Bind a powered circuit governor";return null;}
    reason=g.systemAvailability();if(reason!=null)return reason;
    for(var other:FoundationData.get(l).vehicles.values())if(!v.poweredYaw.consist.contains(other.chassis)&&!other.chassis.equals(v.chassis)){var otherRail=rail(l,other);if(otherRail!=null&&g.routeIds().contains(otherRail.id))return "One complete train per powered circuit";}
    return null;
  }
  /** Own passive travel brake; do not brake the yaw carrier's legitimate orbital COM motion. */
  public static void brake(ServerLevel l,FoundationData.Vehicle v,double dt){
    var b=body(l,v.chassis);if(b==null)return;var m=MOTION.get(b);if(m!=null&&m.hold!=null&&m.hold.isValid())return;
    var pipeline=SubLevelContainer.getContainer(l).physicsSystem().getPipeline();var velocity=pipeline.getLinearVelocity(b,new Vector3d());double speed=velocity.length();if(speed>0)pipeline.addLinearAndAngularVelocity(b,velocity.mul(-Math.min(1,(v.fault.isEmpty()?2:6)*dt/speed)),new Vector3d());
  }
  private static void releaseHold(ServerLevel l,FoundationData.Vehicle v){var b=body(l,v.chassis);var m=b==null?null:MOTION.get(b);if(m!=null&&m.hold!=null){if(m.hold.isValid())m.hold.remove();m.hold=null;}}
  private static void hold(ServerLevel l,FoundationData.Vehicle v){
    var parent=body(l,v.chassis);if(parent==null||speed(l,parent)>.05)return;var m=MOTION.get(parent);if(m==null)return;
    if(m.hold==null||!m.hold.isValid()){var anchor=new Vector3d(parent.getMassTracker().getCenterOfMass());var world=parent.logicalPose().transformPosition(new Vector3d(anchor));m.hold=SubLevelContainer.getContainer(l).physicsSystem().getPipeline().addConstraint(null,parent,new GenericConstraintConfiguration(world,anchor,new Quaterniond(parent.logicalPose().orientation()),new Quaterniond(),EnumSet.allOf(ConstraintJointAxis.class)));}
  }
  /** Normal capture decelerates through the governor before asserting the native station hold. */
  public static void capture(ServerLevel l,FoundationData.Vehicle v){
    var pending=MOTION.get(body(l,v.chassis));if(pending!=null)pending.resumeAt=Long.MIN_VALUE;
    var g=governor(l,v);boolean stopped=cars(l,v).stream().allMatch(car->{var b=body(l,car.chassis);return b!=null&&speed(l,b)<=.05;});
    if(stopped&&g!=null&&g.systemAvailability()==null){g.acknowledge(v);for(var car:cars(l,v)){var b=body(l,car.chassis);if(safety(l,b,car,rail(l,car))==null&&!car.alignmentRequired){car.fault="";car.poweredYaw.resumeRequired=false;}}}
    for(var car:cars(l,v)){var s=car.poweredYaw;s.phase=RETURN_CAPTURE;s.status="STATION_UNWIND";s.mode=STATION_CAPTURE;s.travelRequest=0;car.stopping=stopped;car.carriers.forEach(c->{c.capture=stopped;c.latched=false;});car.revision++;}changed(l);
  }
  public static void start(ServerLevel l,FoundationData.Vehicle v){
    var g=governor(l,v);if(g==null)throw new IllegalArgumentException("Bind the complete powered governor circuit");
    for(var car:cars(l,v)){var parent=body(l,car.chassis);String reason=parent==null?"Native body missing":safety(l,parent,car,rail(l,car));if(reason!=null||!car.fault.isEmpty()||car.poweredYaw.resumeRequired)throw new IllegalArgumentException(reason==null?"Explicitly revalidate the powered ride":reason);}
    for(var r:PoweredInvertedYawGovernorEntity.route(l,v)){r.poweredYaw.validate();if(r.poweredYaw.yaw.geometry!=r.geometryHash||r.poweredYaw.yaw.intervals.isEmpty()||r.poweredYaw.speeds.isEmpty())throw new IllegalArgumentException("Author yaw and speed sections for the complete circuit");}
    g.release(v);for(var car:cars(l,v))releaseHold(l,car);
  }
  public static void dispatched(ServerLevel l,FoundationData.Vehicle v){var s=v.poweredYaw;s.phase=RUN;s.mode=BLEND;s.section=null;s.travelRequest=0;s.resumeRequired=false;s.holdAcknowledged=false;s.epoch=v.authority.epoch;s.consist=cars(l,v).stream().map(c->c.chassis).toList();}
  public static void resume(ServerLevel l,FoundationData.Vehicle v){
    boolean heldShow=v.poweredYaw.phase==HELD_DWELL;
    boolean interruptedShow=v.poweredYaw.phase==FAULT&&v.poweredYaw.interruptedPhase==HELD_DWELL;
    for(var car:cars(l,v)){var b=body(l,car.chassis);if(b==null||!car.stopping||speed(l,b)>.05||!car.restraints)throw new IllegalArgumentException("Stop and retain every car before explicit resume");}
    var g=governor(l,v);if(g==null)throw new IllegalArgumentException("Restore the bound governor");g.acknowledge(v);
    for(var car:cars(l,v)){var reason=safety(l,body(l,car.chassis),car,rail(l,car));if(reason!=null)throw new IllegalArgumentException(reason);car.fault="";car.poweredYaw.resumeRequired=false;car.maintenance=false;}
    if(heldShow||interruptedShow){for(var car:cars(l,v)){var state=car.poweredYaw;state.phase=HELD_DWELL;if(interruptedShow)state.mode=state.interruptedMode;MOTION.get(body(l,car.chassis)).releaseNotBefore=l.getGameTime()+6;state.holdAcknowledged=true;state.resumeRequired=false;car.stopping=true;hold(l,car);}changed(l);return;}
    // Recovery creates a new authority epoch. Its stop samples must be collected
    // after acknowledgment, so retain this explicit request only in live memory.
    var motion=MOTION.get(body(l,v.chassis));motion.resumeAt=l.getGameTime()+6;
    for(var car:cars(l,v)){car.poweredYaw.phase=RUN;car.poweredYaw.mode=HOLD_FAULT;car.poweredYaw.status="RESUME_WAIT_STOPPED";}changed(l);
  }
  public static void direction(ServerLevel l,FoundationData.Vehicle v,int sign){
    var pending=MOTION.get(body(l,v.chassis));if(pending!=null)pending.resumeAt=Long.MIN_VALUE;
    if(Math.abs(sign)!=1)throw new IllegalArgumentException("Choose forward or reverse");for(var car:cars(l,v)){var b=body(l,car.chassis);if(b==null||!car.stopping||speed(l,b)>.05||MOTION.get(b)==null||MOTION.get(b).stoppedTicks<5)throw new IllegalArgumentException("Hold the whole train for five stopped ticks before reversal");}
    var g=governor(l,v);if(g==null)throw new IllegalArgumentException("Restore governor");g.acknowledge(v);for(var car:cars(l,v)){car.poweredYaw.direction=sign;car.poweredYaw.pendingDirection=sign;car.poweredYaw.travelRequest=0;car.revision++;}changed(l);
  }
  private static void select(ServerLevel l,FoundationData.Vehicle v,FoundationData.Rail r,Motion m){
    var s=v.poweredYaw;var i=m.interval;if(i==null){if(!v.stopping)fault(l,v,"Yaw interval missing at this car's position");return;}
    if(r.poweredYaw.yaw.geometry!=r.geometryHash){fault(l,v,"Yaw program geometry changed");return;}
    if(Objects.equals(s.section,i.id())){if(s.sectionRevision!=r.poweredYaw.yaw.revision||s.routeGeometry!=r.geometryHash)fault(l,v,"Yaw program changed during operation");return;}
    s.section=i.id();s.sectionRevision=r.poweredYaw.yaw.revision;s.routeGeometry=r.geometryHash;s.entryAngle=v.carriers.getFirst().angle;s.entryDistance=m.distance;s.nextMode=i.mode();s.mode=i.blend()>0?BLEND:i.mode();s.yawReached=false;s.status="READY";event(l,v,"SECTION_ENTER",i.id());
  }
  private static double target(ServerLevel l,ServerSubLevel parent,ServerSubLevel child,FoundationData.Vehicle v,YawSection.Interval i){
    var s=v.poweredYaw;if(!l.dimension().location().toString().equals(i.dimension())||!l.hasChunkAt(i.markerPos())||!(l.getBlockEntity(i.markerPos()) instanceof YawMarkerEntity marker)||!marker.id.equals(i.marker())||!Objects.equals(marker.owner,s.owner)){fault(l,v,"Restore the owned scenery marker");return s.target;}
    var c=v.carriers.getFirst();var p=child.getPlot().getCenterBlock();var pivot=child.logicalPose().transformPosition(new Vector3d(p.getX()+.5+c.childPivotX,p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ));
    double theta=YawController.projected(parent.logicalPose().orientation(),pivot,new Vector3d(i.markerPos().getX()+.5,i.markerPos().getY()+.5,i.markerPos().getZ()+.5));
    if(!Double.isFinite(theta)){fault(l,v,"Scenery target projection is degenerate");return s.target;}
    double goal=s.status.equals("TARGET_VALID")?FreeRotor.unwrap(s.target,theta):YawController.resolve(theta,c.angle,i.winding());
    if(!Double.isFinite(goal)||Math.abs(goal)>YawController.LIMIT){fault(l,v,"Scenery target winding exhausted");return s.target;}s.status="TARGET_VALID";return goal;
  }
  private static boolean allStopped(ServerLevel l,List<FoundationData.Vehicle> cars){return cars.stream().allMatch(car->{var p=body(l,car.chassis);return p!=null&&speed(l,p)<=.05;});}
  private static void travel(ServerLevel l,FoundationData.Vehicle v,FoundationData.Rail r,Motion m){
    var g=governor(l,v);if(g==null||!v.chassis.equals(g.vehicle))return;var s=v.poweredYaw;var cars=cars(l,v);
    if(s.phase==FAULT||s.resumeRequired)return;
    if(m.resumeAt!=Long.MIN_VALUE){
      if(l.getGameTime()<m.resumeAt)return;
      m.resumeAt=Long.MIN_VALUE;
      try{start(l,v);for(var car:cars){car.stopping=false;car.carriers.forEach(c->{c.capture=false;c.latched=false;});dispatched(l,car);}changed(l);}
      catch(IllegalArgumentException denied){fault(l,v,denied.getMessage());}
      return;
    }
    if(s.phase==HELD_DWELL){
      if(v.authority.suspended||!allStopped(l,cars)||cars.stream().anyMatch(car->!car.restraints||!car.fault.isEmpty())||g.systemAvailability()!=null){fault(l,v,"Show hold or drive supply lost");return;}
      for(var car:cars){hold(l,car);car.poweredYaw.showTicks++;car.poweredYaw.holdAcknowledged=true;}
      if(s.dwellRemaining>0)for(var car:cars)car.poweredYaw.dwellRemaining=Math.max(0,car.poweredYaw.dwellRemaining-1);
      boolean settled=cars.stream().allMatch(car->{var c=car.carriers.getFirst();return Math.abs(c.angle-car.poweredYaw.target)<.035&&Math.abs(c.omega)<.05;});
      if(s.dwellRemaining==0&&settled&&l.getGameTime()>=m.releaseNotBefore){
        event(l,v,"DWELL_COMPLETE",s.stop);
        try{g.release(v);for(var car:cars){releaseHold(l,car);var state=car.poweredYaw;state.direction=state.pendingDirection;state.phase=RUN;state.holdAcknowledged=false;state.travelRequest=0;state.epoch=car.authority.epoch;car.stopping=false;car.carriers.forEach(c->c.capture=false);}}
        catch(IllegalArgumentException denied){fault(l,v,denied.getMessage());}
      }return;
    }
    if(v.stopping)return;
    var speed=r.poweredYaw.speed(m.distance);if(speed==null){fault(l,v,"Travel speed section missing");return;}
    int canonical=g.railDirection(r)*s.direction;if(canonical==0){fault(l,v,"Route direction binding missing");return;}
    double desired=speed.speed();s.speedIndex=r.poweredYaw.speeds.indexOf(speed);
    PoweredInvertedYawSection.Stop next=null;
    if(s.stop!=null){var current=r.poweredYaw.stops.stream().filter(a->a.id().equals(s.stop)).findFirst().orElse(null);if(current==null||s.phase==RUN&&Math.abs(m.distance-current.distance())>2)s.stop=null;}
    if(s.phase!=RETURN_CAPTURE){
      double nearest=Double.POSITIVE_INFINITY;
      if(s.phase==STOP_APPROACH&&s.nextStop>=0&&s.nextStop<r.poweredYaw.stops.size()){next=r.poweredYaw.stops.get(s.nextStop);nearest=(next.distance()-m.distance)*canonical;}
      else for(var stop:r.poweredYaw.stops){double ahead=(stop.distance()-m.distance)*canonical;if(ahead>=-.2&&ahead<nearest&&!stop.id().equals(s.stop)){next=stop;nearest=ahead;}}
      if(next!=null&&nearest<=m.speed*m.speed/4+3){if(nearest<-.75){fault(l,v,"Powered stop overshot; inspect before recovery");return;}s.phase=STOP_APPROACH;s.nextStop=r.poweredYaw.stops.indexOf(next);desired=Math.min(desired,Math.sqrt(4*Math.max(0,nearest-.08)));if(nearest<.12)desired=0;}
    }else desired=0;
    double request=s.direction*desired;s.travelRequest+=Math.clamp(request-s.travelRequest,-.1,.1);
    try{g.command(v.chassis,v.authority.epoch,s.travelRequest,2);}catch(IllegalArgumentException denied){fault(l,v,denied.getMessage());return;}
    for(var car:cars){car.poweredYaw.travelRequest=s.travelRequest;if(car!=v)car.poweredYaw.phase=s.phase;}
    if((s.phase==STOP_APPROACH||s.phase==RETURN_CAPTURE)&&Math.abs(s.travelRequest)<.0001){
      for(var car:cars)brake(l,car,.05);
      s.stoppedTicks=allStopped(l,cars)?Math.min(5,s.stoppedTicks+1):0;
      if(s.stoppedTicks>=5){
        g.acknowledge(v);
        for(var car:cars){car.stopping=true;hold(l,car);var state=car.poweredYaw;state.holdAcknowledged=true;state.travelRequest=0;
          if(s.phase==RETURN_CAPTURE){state.phase=RETURN_CAPTURE;state.mode=STATION_CAPTURE;car.carriers.forEach(c->c.capture=true);}
          else if(next!=null){state.phase=HELD_DWELL;state.stop=next.id();state.dwellRemaining=next.dwell();state.showTicks=0;state.pendingDirection=next.afterDirection()==0?s.direction:next.afterDirection();}
        }
        if(s.phase==HELD_DWELL)event(l,v,"STOP_HELD",s.stop);
      }
    }else s.stoppedTicks=0;
  }
  public static void update(ServerLevel l,ServerSubLevel parent,ServerSubLevel child,FoundationData.Vehicle v,FoundationData.Carrier c,FoundationData.Rail r,RotaryConstraintHandle joint,double dt){
    var s=v.poweredYaw;var m=MOTION.computeIfAbsent(parent,k->new Motion());
    if(!m.initialized){m.initialized=true;if(s.initialized&&s.phase!=DOCK_OPEN&&s.phase!=CAPTURED)fault(l,v,"Reload requires explicit retained recovery");if(!s.initialized){s.reference=c.angle;s.target=c.angle;s.initialized=true;}}
    if(m.tick!=l.getGameTime()){
      m.tick=l.getGameTime();m.speed=speed(l,parent);m.stoppedTicks=m.speed<=.05?Math.min(5,m.stoppedTicks+1):0;String reason=safety(l,parent,v,r);if(reason!=null)fault(l,v,reason);
      var edge=r==null?null:TrackGeometry.resolve(l,r);var center=CoasterCartResolve.cartCenterWorld(parent);var hit=edge==null||center==null?null:CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
      if(hit!=null){m.distance=TrackGeometry.distance(l,edge,r,hit.t());m.interval=r.poweredYaw.yaw.at(m.distance);s.distance=m.distance;}
      if(s.phase!=FAULT&&s.mode!=STATION_CAPTURE&&r!=null)select(l,v,r,m);
      if(r!=null)travel(l,v,r,m);
    }
    double goal=s.target;
    if(s.phase==FAULT||s.resumeRequired)s.mode=HOLD_FAULT;
    else if(s.mode==STATION_CAPTURE){if(dock(l,parent,v))goal=0;else goal=c.angle;}
    else if(m.interval!=null){var i=m.interval;
      if(i.mode()==TARGET)goal=target(l,parent,child,v,i);
      else if(i.mode()==SHOW_STOP){if(s.phase==HELD_DWELL&&s.holdAcknowledged){s.mode=SHOW_STOP;double progress=Math.min(1,(double)s.showTicks/i.showDuration());goal=i.profile().angleAt(i.profile().keys().getLast().distance()*progress);}else goal=s.entryAngle;}
      else goal=i.profile().angleAt(m.distance-i.start());
      if(s.mode==BLEND){double progress=i.blend()==0?1:Math.min(1,Math.abs(m.distance-s.entryDistance)/i.blend());goal=s.entryAngle+(goal-s.entryAngle)*progress*progress*(3-2*progress);if(progress>=1)s.mode=s.nextMode;}
    }
    if(s.mode!=HOLD_FAULT)s.target=goal;
    if(Math.abs(s.reference)+s.referenceVelocity*s.referenceVelocity/1.6>YawController.LIMIT-.002&&Math.signum(s.referenceVelocity)==Math.signum(s.reference)&&Math.abs(s.referenceVelocity)>.01)fault(l,v,"Yaw winding limit");
    double rate=m.interval==null?.45:Math.min(.45,m.interval.speed()),acceleration=m.interval==null?.45:Math.min(.45,m.interval.acceleration());
    var next=s.mode==HOLD_FAULT?YawController.brake(s.reference,s.referenceVelocity,acceleration,dt):YawController.advance(s.reference,s.referenceVelocity,s.target,rate,acceleration,dt);
    s.reference=next.angle();s.referenceVelocity=next.velocity();double inertia=SpinningRuntime.jointInertia(child,c);
    if(!Double.isFinite(inertia)||inertia<=0){fault(l,v,"Native yaw inertia unavailable");return;}
    joint.setMotor(RotaryConstraintHandle.DEFAULT_AXIS,Math.atan2(Math.sin(s.reference),Math.cos(s.reference)),4000,400,false,0);
    boolean station=v.stopping&&s.mode==STATION_CAPTURE&&v.fault.isEmpty()&&!s.resumeRequired&&dock(l,parent,v)&&m.speed<=.05;
    c.latched=m.latch.update(l.getGameTime(),station,c.angle,c.omega,m.speed);c.capture=station||s.mode==HOLD_FAULT;c.captureTarget=0;
    if(c.latched){if(s.phase!=CAPTURED&&s.phase!=DOCK_OPEN)event(l,v,"DOCK_CAPTURED",r==null?null:r.id);s.phase=v.restraints?CAPTURED:DOCK_OPEN;hold(l,v);}
    if(s.mode!=HOLD_FAULT&&Math.abs(c.angle-s.target)<.035&&Math.abs(c.omega)<.05){if(!s.yawReached){event(l,v,"YAW_REACHED",s.section);s.yawReached=true;}}else s.yawReached=false;
    if(v.stopping||v.maintenance)brake(l,v,dt);v.boardingReady=ready(l,parent,v);changed(l);
  }
}
