package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.DockCapture;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Native continuous longitudinal roll; route program drives only the carrier, never travel. */
public final class AxisRuntime {
  private static final class Live { final DockCapture latch=new DockCapture(); long checked=Long.MIN_VALUE; boolean started, aligning; }
  private static final Map<ServerSubLevel,Live> LIVE=new WeakHashMap<>();
  private AxisRuntime(){}
  public static FoundationData.Rail rail(ServerLevel level,FoundationData.Vehicle v){return FoundationData.get(level).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);}
  public static void fault(ServerLevel level,FoundationData.Vehicle v,String reason){
    var ids=new HashSet<>(v.axis.consist);ids.add(v.chassis);
    var body=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
    if(body!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body));
    for(var id:ids){var car=FoundationData.get(level).vehicles.get(id);if(car==null||!AxisVehicle.is(car))continue;
      car.stopping=true;car.restraints=true;car.fault=reason;car.axis.mode=AxisState.Mode.DAMP;car.axis.blend=0;
      for(var c:car.carriers){c.capture=false;c.latched=false;}
    }
  }
  public static boolean dock(ServerLevel level,ServerSubLevel body,FoundationData.Vehicle v){
    return dock(level,body,v,true);
  }
  private static boolean dock(ServerLevel level,ServerSubLevel body,FoundationData.Vehicle v,boolean unloading){
    if(v.dock==null||v.axis.dockFrom==null||!level.hasChunkAt(v.dock)
        ||!(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station)
        ||VehicleOperations.consist(level,v).stream().noneMatch(c->c.chassis.equals(station.vehicle)))return false;
    var r=rail(level,v);var edge=r==null?null:TrackGeometry.resolve(level,r);
    if(edge==null||!r.matches(v.axis.dockFrom,v.axis.dockTo)||r.geometryHash!=v.axis.dockGeometry||AxisTrack.rejection(level,edge)!=null
        ||TrackGeometry.table(level,edge).length()<16)return false;
    var center=CoasterCartResolve.cartCenterWorld(body);
    if(center==null||center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))>1024
        ||new Vector3d(0,1,0).rotate(body.logicalPose().orientation()).y<.999)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    var table=TrackGeometry.table(level,edge);double at=table.distanceAt(hit.t());
    // A lift-entry stop can leave the bearing at the declared dock edge. Permit
    // retained alignment there; Ready/unloading still requires the complete inset.
    return (unloading?at>=2&&at<=table.length()-2:v.restraints||at>=2&&at<=table.length()-2)
        &&AxisTrack.obstruction(level,hit)==null;
  }
  public static boolean ready(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    if(!ConsistRules.eligible(level,parent,v))return false;
    var container=SubLevelContainer.getContainer(level);
    for(var car:VehicleOperations.consist(level,v)){
      if(!AxisVehicle.is(car)||!car.stopping||car.alignmentRequired||!car.fault.isEmpty()
          ||!(container.getSubLevel(car.chassis) instanceof ServerSubLevel body)||!dock(level,body,car))return false;
      var c=car.carriers.getFirst();
      if(!c.capture||!c.latched||container.getSubLevel(c.body)==null||Math.abs(c.omega)>.05
          ||Math.abs(c.angle-c.captureTarget)>DockCapture.ANGLE_TOLERANCE
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05)return false;
    }return true;
  }
  public static void rebindDock(ServerLevel level,FoundationData.Vehicle selected,BlockPos pos){
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    if(cars.size()>2)throw new IllegalArgumentException("Axis supports one or two identical cars");
    for(var v:cars){
      var r=rail(level,v);var edge=r==null?null:TrackGeometry.resolve(level,r);
      if(!AxisVehicle.is(v)||!v.stopping||v.maintenance||v.alignmentRequired||edge==null||AxisTrack.rejection(level,edge)!=null
          ||TrackGeometry.table(level,edge).length()<16||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body)
          ||new Vector3d(0,1,0).rotate(body.logicalPose().orientation()).y<.999
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05
          ||!v.restraints&&!v.carriers.getFirst().occupants.isEmpty())throw new IllegalArgumentException("Stop and retain both Axis cars on a level 16-block dock");
      var center=CoasterCartResolve.cartCenterWorld(body);
      if(center==null||center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>1024)throw new IllegalArgumentException("Bring both Axis cars to the declared dock");
    }
    for(var v:cars){var r=rail(level,v);v.dock=pos.immutable();v.axis.dockFrom=r.from;v.axis.dockTo=r.to;v.axis.dockGeometry=r.geometryHash;v.axis.consist=List.of();
      if(v.fault.startsWith("Axis coupler/consist changed"))v.fault="";v.carriers.getFirst().capture=true;v.revision++;
    }FoundationData.get(level).setDirty();
  }
  public static String safety(ServerLevel level,ServerSubLevel body,FoundationData.Vehicle v,FoundationData.Rail r){
    var edge=r==null?null:TrackGeometry.resolve(level,r);
    if(edge==null||!r.valid)return "Axis matching support unavailable";
    var reason=AxisTrack.rejection(level,edge);if(reason!=null)return reason;
    if(!FamilyControllers.find(body).equals(Set.of(v.family)))return "Axis controller unavailable";
    if(!v.stopping&&!v.restraints)return "Axis travel requires closed restraints";
    if(!ConsistRules.eligible(level,body,v))return "Axis permits at most two identical cars";
    if(!v.axis.consist.isEmpty()&&!new HashSet<>(v.axis.consist).equals(new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body))))return "Axis coupler/consist changed; stop and rebind at dock";
    var child=SubLevelContainer.getContainer(level).getSubLevel(v.carriers.getFirst().body);
    if(child==null)return "Axis rotating carrier unavailable";
    var blocks=body.getPlot().getEmbeddedLevelAccessor();
    for(var pos:AxisVehicle.FRAME)if(!blocks.getBlockState(pos).is(VehicleParts.AXIS_FRAME.get()))return "Axis outrigger frame unavailable";
    for(var pos:AxisVehicle.SEATS)if(!child.getPlot().getEmbeddedLevelAccessor().getBlockState(pos).is(VehicleParts.AXIS_SEAT.get()))return "Axis seat unavailable";
    if(Math.abs(v.carriers.getFirst().omega)>3)return "Axis roll exceeds 3 rad/s";
    var center=CoasterCartResolve.cartCenterWorld(body);if(center==null)return "Axis running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));if(hit==null)return "Axis route position unavailable";
    var velocity=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d());
    double speed=velocity.length();if(speed>12.05)return "Axis travel exceeds 12 blocks/s";
    var table=TrackGeometry.table(level,edge);double at=table.distanceAt(hit.t()),look=speed*speed/12+2;
    for(double s=Math.max(0,at-look);s<=Math.min(table.length(),at+look);s+=.5){double t=table.parameterAt(s);
      var obstacle=AxisTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,edge,t),edge,t));
      if(obstacle!=null)return "Axis sweep obstructed at "+obstacle.toShortString();}
    var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);double signed=velocity.dot(basis.forward().x,basis.forward().y,basis.forward().z);
    double remaining=signed>=0?table.length()-at:at;
    if(Math.abs(signed)>.01&&remaining<=look){
      var endpoint=signed>0?edge.to():edge.from();var following=FoundationData.get(level).rails.stream().filter(n->n!=r&&(n.from.equals(endpoint)||n.to.equals(endpoint))).toList();
      if(following.size()!=1)return "Axis route end or ambiguous connection";
      var next=TrackGeometry.resolve(level,following.getFirst());if(next==null||AxisTrack.rejection(level,next)!=null)return "Axis incompatible route ahead";
      var nt=TrackGeometry.table(level,next);for(double s=0;s<=Math.min(look-remaining,nt.length());s+=.5){double t=nt.parameterAt(next.from().equals(endpoint)?s:nt.length()-s);
        var obstacle=AxisTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,next,t),next,t));
        if(obstacle!=null)return "Axis connection sweep obstructed at "+obstacle.toShortString();}
    }
    var span=ChainLiftSpanQuery.findAtGraphHit(level,hit,0);
    if((r.axisMode==AxisState.Mode.DRIVE||r.liftLock)&&span==null)return "Axis DRIVE/lift requires native Chain power";
    if(span!=null&&(!v.stopping||r.axisMode==AxisState.Mode.DRIVE||r.liftLock)&&Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16))<.001)return "Axis Chain power lost; DAMP and travel hold";
    return null;
  }
  public static void acknowledge(ServerLevel level,FoundationData.Vehicle v){
    var container=SubLevelContainer.getContainer(level);
    for(var car:VehicleOperations.consist(level,v))if(container.getSubLevel(car.chassis) instanceof ServerSubLevel body
        &&!car.alignmentRequired&&safety(level,body,car,rail(level,car))==null)car.fault="";
  }
  public static void rememberConsist(ServerLevel level,FoundationData.Vehicle v){v.axis.consist=List.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(SubLevelContainer.getContainer(level).getSubLevel(v.chassis)));}
  public static void resumeToDock(ServerLevel level,FoundationData.Vehicle v){
    var cars=VehicleOperations.consist(level,v);var container=SubLevelContainer.getContainer(level);
    for(var car:cars){
      if(!car.stopping||!car.restraints||car.alignmentRequired||car.maintenance||!(container.getSubLevel(car.chassis) instanceof ServerSubLevel body)
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05)throw new IllegalArgumentException("Stop, retain and repair both Axis cars before Resume");
      var reason=safety(level,body,car,rail(level,car));if(reason!=null)throw new IllegalArgumentException(reason);
    }
    for(var car:cars){rememberConsist(level,car);car.fault="";car.stopping=false;car.carriers.getFirst().capture=false;car.carriers.getFirst().latched=false;car.revision++;}FoundationData.get(level).setDirty();
  }
  public static double inertia(ServerSubLevel child,FoundationData.Carrier c){
    var mass=child.getMassTracker();var p=child.getPlot().getCenterBlock();
    var arm=new Vector3d(mass.getCenterOfMass()).sub(p.getX()+.5+c.childPivotX,p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ);
    return mass.getInertiaTensor().m22()+mass.getMass()*(arm.x*arm.x+arm.y*arm.y);
  }
  private static double gravityTorque(ServerSubLevel parent,ServerSubLevel child,FoundationData.Carrier c) {
    var mass=child.getMassTracker();var p=child.getPlot().getCenterBlock();
    var arm=new Vector3d(mass.getCenterOfMass()).sub(p.getX()+.5+c.childPivotX,p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ)
        .rotate(child.logicalPose().orientation());
    var gravity=dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(child.getLevel()).mul(mass.getMass());
    return arm.cross(gravity).dot(new Vector3d(0,0,-1).rotate(parent.logicalPose().orientation()));
  }
  public static void update(ServerLevel level,ServerSubLevel parent,ServerSubLevel child,FoundationData.Vehicle v,FoundationData.Carrier c,
      FoundationData.Rail r,RotaryConstraintHandle joint,double dt){
    var live=LIVE.computeIfAbsent(child,k->new Live());var state=v.axis;
    if(!live.started){live.started=true;c.latched=false;if(!v.stopping){v.stopping=true;v.restraints=true;c.capture=false;v.fault="Axis restored pose retained; inspect then explicitly Resume";}}
    if(live.checked!=level.getGameTime()){live.checked=level.getGameTime();var reason=safety(level,parent,v,r);if(reason!=null)fault(level,v,reason);}
    var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();double travel=pipeline.getLinearVelocity(parent,new Vector3d()).length();
    if(v.stopping||v.maintenance)VehicleOperations.brake(level,v,dt);
    boolean captureRequested=v.stopping&&c.capture&&v.fault.isEmpty()&&dock(level,parent,v,false)&&travel<=.05;
    live.aligning=captureRequested&&(live.aligning||Math.abs(c.omega)<=.35);
    boolean capture=live.aligning;
    if(!v.stopping&&r!=null&&r.liftLock){
      if(!c.liftHeld){
        if(Math.abs(c.omega)>.05||Math.abs(c.angle-Math.rint(c.angle/(2*Math.PI))*2*Math.PI)>DockCapture.ANGLE_TOLERANCE)fault(level,v,"Axis lift entry requires settled upright capture");
        else{c.liftHeld=true;c.captureTarget=c.angle;}
      }
      c.liftFrom=r.from;c.liftTo=r.to;c.liftGeometry=r.geometryHash;
    }else if(c.liftHeld&&!v.stopping&&r!=null&&!r.matches(c.liftFrom,c.liftTo)){
      var edge=TrackGeometry.resolve(level,r);var center=CoasterCartResolve.cartCenterWorld(parent);
      var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));var table=TrackGeometry.table(level,edge);
      double at=table.distanceAt(hit.t());boolean from=edge.from().equals(c.liftFrom)||edge.from().equals(c.liftTo);
      if((from?at:table.length()-at)>=2){c.liftHeld=false;c.liftFrom=null;c.liftTo=null;}
    }
    double I=inertia(child,c);if(!Double.isFinite(I)||I<=0){fault(level,v,"Axis native inertia unavailable");return;}
    var mode=r==null?AxisState.Mode.DAMP:r.axisMode;
    if(v.stopping)mode=AxisState.Mode.DAMP;
    boolean programmed=!v.stopping&&mode==AxisState.Mode.DRIVE;
    double target=c.angle;
    if(programmed){
      var edge=TrackGeometry.resolve(level,r);var center=CoasterCartResolve.cartCenterWorld(parent);var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
      target=r.profile.angleAt(TrackGeometry.distance(level,edge,r,hit.t()));
      if(!state.activeProgram||!Objects.equals(state.profile,r.id)||state.profileRevision!=r.revision){state.initialized=false;state.profile=r.id;state.profileRevision=r.revision;state.windingOffset=Math.rint((c.angle-target)/(2*Math.PI))*2*Math.PI;}
      target+=state.windingOffset;state.activeProgram=true;
    } else if(!v.stopping) state.activeProgram=false;
    if(capture){if(!state.initialized||state.mode!=AxisState.Mode.DAMP)c.captureTarget=Math.rint(c.angle/(2*Math.PI))*2*Math.PI;target=c.captureTarget;}
    if(c.liftHeld)target=c.captureTarget;
    boolean driven=capture||c.liftHeld||programmed;
    if(driven){if(state.mode!=mode)state.initialized=false;target=state.advance(target,c.angle,dt,programmed?1:.35,programmed?.75:.5);}
    else{state.command=c.angle;state.commandRate=0;state.initialized=false;}
    state.blend=Math.clamp(state.blend+(programmed?dt:-dt),0,1);
    state.mode=mode;
    double actuatorBlend=(capture||c.liftHeld)?1:state.blend;
    double decay=.4+(mode==AxisState.Mode.DAMP?2:0)+(Math.abs(c.omega)>2?3:0);
    // Pinned Rapier motors use acceleration-based gains. The boolean enables the
    // force limit; it does not select a motor model. Compensate measured gravity
    // through the same native actuator, fading its contribution on release.
    double gravity=gravityTorque(parent,child,c);
    double motorTarget=target-(actuatorBlend>0?gravity/(I*400):0);
    joint.setMotor(RotaryConstraintHandle.DEFAULT_AXIS,Math.atan2(Math.sin(motorTarget),Math.cos(motorTarget)),
        400*actuatorBlend,decay+(40-decay)*actuatorBlend,true,I*(actuatorBlend>0?100:12)+Math.abs(gravity)*actuatorBlend);
    c.latched=live.latch.update(level.getGameTime(),capture,c.angle-c.captureTarget,c.omega,travel);
    if(v.stopping&&c.latched)c.liftHeld=false;
  }
}
