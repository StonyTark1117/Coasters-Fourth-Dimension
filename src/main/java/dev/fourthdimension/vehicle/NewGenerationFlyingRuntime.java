package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import static dev.fourthdimension.vehicle.YawState.Mode.*;

/** Native compound controller. Graph travel, yaw and posture each retain explicit authority. */
public final class NewGenerationFlyingRuntime {
  private static final class Motion {
    GenericConstraintHandle yaw,posture,showHold;
    final DockCapture endpoint=new DockCapture();
    long checked=Long.MIN_VALUE;
    double distance,travel;
    boolean exitHold;
    YawSection.Interval interval;
  }
  private static final Map<ServerSubLevel,Motion> MOTION=new WeakHashMap<>();
  private NewGenerationFlyingRuntime(){}
  public static FoundationData.Rail rail(ServerLevel l,FoundationData.Vehicle v){return FoundationData.get(l).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);}
  private static ServerSubLevel body(ServerLevel l,UUID id){if(id==null)return null;var b=SubLevelContainer.getContainer(l).getSubLevel(id);return b instanceof ServerSubLevel s&&!s.isRemoved()?s:null;}
  private static double speed(ServerSubLevel b){return SubLevelContainer.getContainer(b.getLevel()).physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).length();}
  public static void refresh(ServerLevel l,FoundationData.Vehicle v){
    var chassis=body(l,v.chassis);var deck=body(l,v.yaw.deck);var inner=body(l,v.carriers.getFirst().body);if(chassis==null||deck==null||inner==null)return;
    var m=MOTION.get(chassis);if(m==null||m.yaw==null||!m.yaw.isValid()||!m.posture.isValid())return;
    var physics=SubLevelContainer.getContainer(l).physicsSystem();var pipeline=physics.getPipeline();physics.updatePose(chassis);physics.updatePose(deck);physics.updatePose(inner);
    var s=v.yaw;var c=v.carriers.getFirst();
    s.angle=CarrierAxis.Y.angle(s.angle,new Quaterniond(chassis.logicalPose().orientation()).conjugate().mul(deck.logicalPose().orientation()));
    s.velocity=pipeline.getAngularVelocity(deck,new Vector3d()).sub(pipeline.getAngularVelocity(chassis,new Vector3d())).dot(new Vector3d(0,1,0).rotate(chassis.logicalPose().orientation()));
    c.angle=CarrierAxis.X.angle(c.angle,new Quaterniond(deck.logicalPose().orientation()).conjugate().mul(inner.logicalPose().orientation()));
    c.omega=pipeline.getAngularVelocity(inner,new Vector3d()).sub(pipeline.getAngularVelocity(deck,new Vector3d())).dot(new Vector3d(1,0,0).rotate(deck.logicalPose().orientation()));
    s.tick=l.getGameTime();s.sequence++;
  }
  private static GenericConstraintHandle yawJoint(ServerSubLevel chassis,ServerSubLevel deck){
    var p=chassis.getPlot().getCenterBlock();var d=deck.getPlot().getCenterBlock();
    var j=SubLevelContainer.getContainer(chassis.getLevel()).physicsSystem().getPipeline().addConstraint(chassis,deck,new GenericConstraintConfiguration(
      new Vector3d(p.getX()+.5,p.getY()+.5+NewGenerationFlyingVehicle.PIVOT,p.getZ()+.5),new Vector3d(d.getX()+.5,d.getY()+.5,d.getZ()+.5),new Quaterniond(),new Quaterniond(),EnumSet.of(ConstraintJointAxis.LINEAR_X,ConstraintJointAxis.LINEAR_Y,ConstraintJointAxis.LINEAR_Z,ConstraintJointAxis.ANGULAR_X,ConstraintJointAxis.ANGULAR_Z)));
    j.setContactsEnabled(false);return j;
  }
  public static void fault(ServerLevel l,FoundationData.Vehicle v,String reason){
    List<FoundationData.Vehicle> cars;try{cars=VehicleOperations.consist(l,v);}catch(IllegalArgumentException e){cars=List.of(v);}
    for(var car:cars){if(!NewGenerationFlyingVehicle.is(car))continue;var s=car.yaw;
      if(s.mode!=HOLD_FAULT){s.mode=HOLD_FAULT;s.resumeRequired=true;s.epoch++;}
      s.status=reason;s.travelAuthorized=false;s.holdAcknowledged=false;car.stopping=true;car.restraints=true;car.flying.body=true;car.flying.legs=true;car.fault=reason;
    }FoundationData.get(l).setDirty();
  }
  public static void tick(ServerLevel l,ServerSubLevel chassis,FoundationData.Vehicle v,double dt){
    var s=v.yaw;var c=v.carriers.getFirst();var deck=body(l,s.deck);var inner=body(l,c.body);
    if(deck==null||inner==null){fault(l,v,"HIERARCHY_UNAVAILABLE");VehicleOperations.brake(l,v,dt);return;}
    var m=MOTION.get(chassis);
    if(m==null){m=new Motion();MOTION.put(chassis,m);if(s.initialized)fault(l,v,"RESTART_REVALIDATION");else{s.reference=s.angle;s.postureReference=c.angle;s.initialized=true;}}
    if(!v.stopping&&m.showHold!=null){if(m.showHold.isValid())m.showHold.remove();m.showHold=null;}
    NativeLoading.update(inner,v,c);
    if(m.yaw==null||!m.yaw.isValid())m.yaw=yawJoint(chassis,deck);
    if(m.posture==null||!m.posture.isValid())m.posture=FlyingRuntime.createJoint(deck,inner,c);
    refresh(l,v);
    if(m.checked!=l.getGameTime()){
      m.checked=l.getGameTime();m.travel=speed(chassis);var r=rail(l,v);String reason=safety(l,chassis,v);
      if(reason!=null)fault(l,v,reason);
      if(r!=null){BoundaryGuard.check(l,chassis,v,r);if(!v.fault.isEmpty()&&s.mode!=HOLD_FAULT)fault(l,v,v.fault);
        var edge=TrackGeometry.resolve(l,r);var center=CoasterCartResolve.cartCenterWorld(chassis);
        var hit=edge==null||center==null?null:CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
        if(hit!=null){double distance=TrackGeometry.distance(l,edge,r,hit.t());
          if(Math.abs(distance-s.lastDistance)>.001&&m.travel>.05&&!v.stopping&&s.travelAuthorized&&r.geometryHash==s.routeFingerprint){int direction=distance>s.lastDistance?1:-1;if(direction!=s.direction&&s.mode==BLEND){s.entryAngle=s.angle;s.entryVelocity=s.velocity;s.entryDistance=distance;s.blendProgress=0;}s.direction=direction;}
          m.distance=distance;m.interval=r.yaw.at(m.distance);}
      }
      if(s.mode!=HOLD_FAULT&&s.mode!=STATION_CAPTURE)select(l,v,m,r);
    }
    double yawGoal=s.lastTarget,phiGoal=v.flying.target;
    if(s.mode==STATION_CAPTURE){yawGoal=0;phiGoal=0;}
    else if(s.mode!=HOLD_FAULT&&m.interval!=null&&!s.status.equals("AWAIT_SHOW_ACK")){
      var i=m.interval;phiGoal=i.posture(m.distance);
      if(s.mode==SHOW_STOP){phiGoal=i.phiFrom()==i.phiTo()?i.phiFrom():-s.postureReference;if(!s.holdAcknowledged||m.showHold==null||!m.showHold.isValid()||m.travel>.05)fault(l,v,"SHOW_HOLD_LOST");
        else{if(s.lastShowTick!=l.getGameTime()){s.lastShowTick=l.getGameTime();s.showTicks++;}yawGoal=i.profile().angleAt(i.profile().keys().getLast().distance()*Math.min(1,(double)s.showTicks/i.showDuration()));}}
      else if(i.mode()==SHOW_STOP&&s.holdAcknowledged){phiGoal=i.phiFrom()==i.phiTo()?i.phiFrom():-s.postureReference;yawGoal=i.profile().keys().getLast().angle();}
      else if(i.mode()==TARGET){yawGoal=target(l,chassis,deck,v,i);}
      else yawGoal=i.profile().angleAt(m.distance-i.start());
      if(s.mode==BLEND){double progress=i.blend()==0?1:Math.min(1,Math.abs(m.distance-s.entryDistance)/i.blend());s.blendProgress=progress;double t=progress*progress*(3-2*progress);yawGoal=s.entryAngle+(yawGoal-s.entryAngle)*t;if(progress>=1)s.mode=s.nextMode;}
      if(s.mode!=HOLD_FAULT){s.lastTarget=yawGoal;v.flying.target=phiGoal==0?0:Math.PI/2;}
      if((Math.abs(i.phiTo()-i.phiFrom())>1e-9||Math.abs(c.angle+phiGoal)>.035)&&m.travel>2.05)fault(l,v,"POSTURE_TRAVEL_LIMIT");
      // Stop before the remaining canonical interval is exhausted when endpoint capture is pending.
      double remaining=s.direction>0?i.end()-m.distance:m.distance-i.start();
      double exitPhi=s.direction>0?i.phiTo():i.phiFrom();
      boolean postureExit=Math.abs(i.phiTo()-i.phiFrom())>1e-9
          &&(exitPhi==0||exitPhi==Math.PI/2)
          &&v.flying.lock!=(exitPhi==0?FlyingState.Lock.SEATED:FlyingState.Lock.PRONE);
      boolean pending=postureExit||Math.abs(c.angle+phiGoal)>Math.toRadians(2)
          ||Math.abs(c.omega)>.05||Math.abs(s.angle-yawGoal)>Math.toRadians(2);
      if(remaining<=m.travel*m.travel/12+.75&&pending)m.exitHold=true;
      // A guard-owned hold releases after its measured condition clears. An operator Stop,
      // fault or Show removes travel authorization and can never be released by this path.
      if(m.exitHold){
        m.exitHold=pending;
        // Every powered chassis in a native consist must honor the same hold;
        // otherwise a following car can push the pending carrier across its exit.
        var cars=VehicleOperations.consist(l,v);
        boolean held=cars.stream().anyMatch(car->{var motion=MOTION.get(body(l,car.chassis));return motion!=null&&motion.exitHold;});
        for(var car:cars)car.stopping=held||!car.yaw.travelAuthorized;
      }
      s.lastDistance=m.distance;
    }
    if(Math.abs(s.reference)+s.referenceVelocity*s.referenceVelocity/1.6>YawController.LIMIT-.002&&Math.signum(s.referenceVelocity)==Math.signum(s.reference)&&Math.abs(s.referenceVelocity)>.01)fault(l,v,"WINDING_LIMIT");
    double rate=m.interval==null?.45:Math.min(.45,m.interval.speed()),accel=m.interval==null?.45:Math.min(.45,m.interval.acceleration());
    var yaw=s.mode==HOLD_FAULT?YawController.brake(s.reference,s.referenceVelocity,accel,dt):YawController.advance(s.reference,s.referenceVelocity,yawGoal,rate,accel,dt);
    var posture=s.mode==HOLD_FAULT?YawController.brake(s.postureReference,s.postureVelocity,.15,dt):YawController.advance(s.postureReference,s.postureVelocity,phiGoal==Math.PI/2?-phiGoal+.015:-phiGoal,.18,.15,dt);
    s.reference=yaw.angle();s.referenceVelocity=yaw.velocity();s.postureReference=posture.angle();s.postureVelocity=posture.velocity();
    // Native angular motor uses wrapped target; supervisory state and target path retain every turn.
    double wrapped=Math.atan2(Math.sin(s.reference),Math.cos(s.reference));
    m.yaw.setMotor(ConstraintJointAxis.ANGULAR_Y,wrapped,4000,400,false,0);
    double captureGoal=phiGoal;
    boolean endpoint=phiGoal==0||phiGoal==Math.PI/2;
    if(!endpoint&&m.interval!=null&&Math.abs(m.interval.phiTo()-m.interval.phiFrom())>1e-9){
      double exitPhi=s.direction>0?m.interval.phiTo():m.interval.phiFrom();
      // Qualify the actual endpoint within the specified two-degree window before leaving
      // the transition; stopping short of the boundary must not deadlock an exact-key test.
      if((exitPhi==0||exitPhi==Math.PI/2)&&Math.abs(phiGoal-exitPhi)<=Math.toRadians(2)){
        endpoint=true;captureGoal=exitPhi;
      }
    }
    endpoint&=s.mode!=HOLD_FAULT;
    if(m.endpoint.update(l.getGameTime(),endpoint&&Math.abs(c.omega)<.0175,c.angle+captureGoal,c.omega,0)){
      v.flying.lock=captureGoal==0?FlyingState.Lock.SEATED:FlyingState.Lock.PRONE;c.latched=true;c.captureTarget=c.angle;
    }else if(!endpoint||Math.abs(c.angle+captureGoal)>.035){v.flying.lock=FlyingState.Lock.NONE;c.latched=false;}
    m.posture.setLimit(ConstraintJointAxis.ANGULAR_X,c.latched?c.captureTarget:-Math.PI/2,c.latched?c.captureTarget:0);
    double inertia=FlyingRuntime.jointInertia(inner,c);double motor=s.postureReference-FlyingRuntime.gravityTorque(deck,inner,c)/(inertia*4000);
    m.posture.setMotor(ConstraintJointAxis.ANGULAR_X,motor,4000,400,false,0);
    v.flying.phase=!v.flying.retained()?FlyingState.Phase.SEATED_OPEN:s.mode==HOLD_FAULT?FlyingState.Phase.INTERRUPTED_TRANSITION:v.flying.lock==FlyingState.Lock.PRONE?FlyingState.Phase.PRONE_LOCKED:v.flying.lock==FlyingState.Lock.SEATED?FlyingState.Phase.SEATED_LOCKED:phiGoal>-c.angle?FlyingState.Phase.TO_PRONE:FlyingState.Phase.TO_SEATED;
    if(s.mode==HOLD_FAULT){v.flying.lock=FlyingState.Lock.NONE;c.latched=false;v.flying.holdAngle=Math.clamp(-c.angle,0,Math.PI/2);}
    v.flying.referenceInitialized=true;v.flying.referenceAngle=s.postureReference;v.flying.referenceVelocity=s.postureVelocity;
    for(var offset:c.seats){var p=inner.getPlot().getCenterBlock().offset(offset);var state=l.getBlockState(p);if(state.is(VehicleParts.NEW_GENERATION_FLYING_SEAT.get())&&(state.getValue(CarrierSeatBlock.RESTRAINED)!=v.flying.body||state.getValue(NewGenerationFlyingSeatBlock.LEGS)!=v.flying.legs))l.setBlock(p,state.setValue(CarrierSeatBlock.RESTRAINED,v.flying.body).setValue(NewGenerationFlyingSeatBlock.LEGS,v.flying.legs),3);}
    // A Show fixes its already-stopped chassis with a native constraint. Braking
    // the inner body's linear velocity would oppose its legitimate orbital COM
    // motion under yaw, transferring asymmetric rider impulses back into travel.
    if((m.showHold==null||!m.showHold.isValid())&&(v.stopping||v.maintenance||!s.travelAuthorized))VehicleOperations.brake(l,v,dt);
    v.boardingReady=ready(l,chassis,v);
  }
  private static void select(ServerLevel l,FoundationData.Vehicle v,Motion m,FoundationData.Rail r){
    var s=v.yaw;if(m.interval==null){fault(l,v,"ROUTE_INTERVAL_MISSING");return;}
    if(r.yaw.geometry!=r.geometryHash){fault(l,v,"ROUTE_REVISION_INVALID");return;}
    var i=m.interval;
    if(Objects.equals(s.section,i.id())){if(s.sectionRevision!=r.yaw.revision||s.routeFingerprint!=r.geometryHash)fault(l,v,"ROUTE_REVISION_INVALID");return;}
    s.previousSection=s.section;s.section=i.id();s.sectionRevision=r.yaw.revision;s.routeFingerprint=r.geometryHash;s.entryAngle=s.angle;s.entryVelocity=s.velocity;s.entryDistance=m.distance;s.lastDistance=m.distance;s.blendProgress=0;s.marker=i.marker();s.markerDimension=i.dimension();s.nextMode=i.mode();
    if(i.mode()==SHOW_STOP){capture(l,v);s.holdAcknowledged=false;s.status="AWAIT_SHOW_ACK";s.mode=BLEND;s.nextMode=SHOW_STOP;}
    else{s.mode=i.blend()>0?BLEND:i.mode();s.status="READY";}
  }
  private static double target(ServerLevel l,ServerSubLevel chassis,ServerSubLevel deck,FoundationData.Vehicle v,YawSection.Interval i){
    var s=v.yaw;String reason=null;YawMarkerEntity marker=null;
    if(!l.dimension().location().toString().equals(i.dimension()))reason="MARKER_WRONG_DIMENSION";
    else if(!l.hasChunkAt(i.markerPos()))reason="MARKER_UNLOADED";
    else if(!(l.getBlockEntity(i.markerPos()) instanceof YawMarkerEntity found))reason="MARKER_MISSING";
    else{marker=found;if(!marker.id.equals(i.marker()))reason="MARKER_BINDING_INVALID";else if(!Objects.equals(marker.owner,v.yaw.owner))reason="MARKER_UNAUTHORIZED";}
    if(reason!=null){fault(l,v,reason);return s.lastTarget;}
    var p=deck.getPlot().getCenterBlock();var pivot=deck.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5));
    double theta=YawController.projected(chassis.logicalPose().orientation(),pivot,new Vector3d(i.markerPos().getX()+.5,i.markerPos().getY()+.5,i.markerPos().getZ()+.5));
    if(!Double.isFinite(theta)){fault(l,v,"TARGET_DEGENERATE");return s.lastTarget;}
    // Continue a previously selected branch through the ±π bearing seam.
    double target=s.status.equals("TARGET_VALID")?FreeRotor.unwrap(s.lastTarget,theta):YawController.resolve(theta,s.angle,i.winding());
    if(Math.abs(target)>YawController.LIMIT)target=Double.NaN;
    if(!Double.isFinite(target)){fault(l,v,"WINDING_UNREACHABLE");return s.lastTarget;}
    s.branch=Math.rint((target-theta)/(2*Math.PI));s.status="TARGET_VALID";return target;
  }
  public static String safety(ServerLevel l,ServerSubLevel chassis,FoundationData.Vehicle v){
    var r=rail(l,v);var edge=r==null?null:TrackGeometry.resolve(l,r);if(edge==null||!r.valid)return "ROUTE_UNAVAILABLE";
    var reason=NewGenerationFlyingTrack.rejection(l,edge);if(reason!=null)return reason;
    if(!ConsistRules.eligible(l,chassis,v))return "CONSIST_INVALID";
    if(!FamilyControllers.find(chassis).equals(Set.of(v.family)))return "AUTHORITY_UNAVAILABLE";
    if(body(l,v.yaw.deck)==null||body(l,v.carriers.getFirst().body)==null)return "HIERARCHY_UNAVAILABLE";
    if(speed(chassis)>16.05)return "TRAVEL_LIMIT";
    var c=v.carriers.getFirst();if(c.angle< -Math.PI/2-.05||c.angle>.05)return "POSTURE_LIMIT";
    var center=CoasterCartResolve.cartCenterWorld(chassis);if(center==null)return "RUNNING_GEAR_UNAVAILABLE";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));if(hit==null)return "ROUTE_POSITION_UNAVAILABLE";
    var table=TrackGeometry.table(l,edge);double at=table.distanceAt(hit.t()),look=speed(chassis)*speed(chassis)/12+2.75;
    for(double d=Math.max(0,at-look);d<=Math.min(table.length(),at+look);d+=.5){double t=table.parameterAt(d);var obstruction=NewGenerationFlyingTrack.obstruction(l,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(l,edge,t),edge,t));if(obstruction!=null)return "COMBINED_SWEEP_OBSTRUCTED at "+obstruction.toShortString();}
    if((v.yaw.travelAuthorized||Math.abs(v.yaw.referenceVelocity)>.001||Math.abs(v.yaw.postureVelocity)>.001)&&!FlyingRuntime.powered(l,v))return "POWER_LOST";
    return null;
  }
  public static boolean dock(ServerLevel l,FoundationData.Vehicle selected){
    try{for(var v:VehicleOperations.consist(l,selected)){var b=body(l,v.chassis);var r=rail(l,v);var s=v.flying;
      if(!NewGenerationFlyingVehicle.is(v)||b==null||!v.stopping||v.dock==null||s.dockFrom==null||r==null||!r.matches(s.dockFrom,s.dockTo)||r.geometryHash!=s.dockGeometry||!l.hasChunkAt(v.dock)||!(l.getBlockEntity(v.dock) instanceof BoardingStationEntity station)||!VehicleOperations.consist(l,v).stream().anyMatch(car->car.chassis.equals(station.vehicle))||speed(b)>.05||new Vector3d(0,1,0).rotate(b.logicalPose().orientation()).y<.999)return false;
      var edge=TrackGeometry.resolve(l,r);var center=CoasterCartResolve.cartCenterWorld(b);if(edge==null||center==null||TrackGeometry.table(l,edge).length()<36||center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))>1600)return false;
      var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));if(hit==null||NewGenerationFlyingTrack.obstruction(l,hit)!=null)return false;
    }return true;}catch(IllegalArgumentException e){return false;}
  }
  public static boolean ready(ServerLevel l,ServerSubLevel parent,FoundationData.Vehicle v){
    if(!v.fault.isEmpty()||v.yaw.resumeRequired||!dock(l,v))return false;
    for(var car:VehicleOperations.consist(l,v)){var c=car.carriers.getFirst();if(car.flying.lock!=FlyingState.Lock.SEATED||Math.abs(c.angle)>Math.toRadians(2)||Math.abs(c.omega)>.05||Math.abs(car.yaw.angle)>Math.toRadians(2)||Math.abs(car.yaw.velocity)>.05)return false;}
    return true;
  }
  public static void rebindDock(ServerLevel l,FoundationData.Vehicle v,BlockPos pos){
    var cars=VehicleOperations.consist(l,v);if(cars.size()>4)throw new IllegalArgumentException("At most four matching compound cars");
    for(var car:cars){var b=body(l,car.chassis);var r=rail(l,car);if(!NewGenerationFlyingVehicle.is(car)||b==null||!car.stopping||speed(b)>.05||r==null||TrackGeometry.resolve(l,r)==null||TrackGeometry.table(l,TrackGeometry.resolve(l,r)).length()<36||new Vector3d(0,1,0).rotate(b.logicalPose().orientation()).y<.999||CoasterCartResolve.cartCenterWorld(b).distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>1600)throw new IllegalArgumentException("Stop every car at a clear level 36-block dock");}
    for(var car:cars){var r=rail(l,car);car.dock=pos.immutable();car.flying.dockFrom=r.from;car.flying.dockTo=r.to;car.flying.dockGeometry=r.geometryHash;car.flying.consist=cars.stream().map(a->a.chassis).toList();car.revision++;}changed(l);
  }
  public static void capture(ServerLevel l,FoundationData.Vehicle v){for(var car:VehicleOperations.consist(l,v)){car.stopping=true;car.yaw.travelAuthorized=false;car.revision++;}changed(l);}
  public static void validateDispatch(ServerLevel l,FoundationData.Vehicle v){
    for(var car:VehicleOperations.consist(l,v)){var b=body(l,car.chassis);var active=b==null?null:MOTION.get(b);if(car.yaw.mode==SHOW_STOP&&active!=null&&active.interval!=null&&(car.yaw.showTicks<active.interval.showDuration()||Math.abs(car.yaw.angle-active.interval.profile().keys().getLast().angle())>Math.toRadians(2)||Math.abs(car.yaw.velocity)>.05))throw new IllegalArgumentException("Wait for the acknowledged show to finish and settle before travel authorization");if(!car.flying.retained()||car.yaw.resumeRequired||!car.fault.isEmpty()||b==null||safety(l,b,car)!=null||!FlyingRuntime.powered(l,car))throw new IllegalArgumentException("Secure body/legs, restore power and explicitly resolve yaw faults before dispatch");var r=rail(l,car);if(r==null||r.yaw.intervals.isEmpty()||r.yaw.geometry!=r.geometryHash)throw new IllegalArgumentException("Author and validate the route yaw/posture intervals first");}
  }
  public static void dispatch(ServerLevel l,FoundationData.Vehicle v){validateDispatch(l,v);for(var car:VehicleOperations.consist(l,v)){
    boolean completedShow=car.yaw.mode==SHOW_STOP&&car.yaw.holdAcknowledged;
    car.stopping=false;car.yaw.travelAuthorized=true;car.yaw.mode=completedShow?PROFILE:BLEND;
    if(!completedShow){car.yaw.section=null;car.yaw.holdAcknowledged=false;}
    car.yaw.epoch++;car.revision++;
  }changed(l);}
  public static void restraints(ServerLevel l,FoundationData.Vehicle v,boolean body,boolean closed){
    var parent=body(l,v.chassis);if(parent==null||!ready(l,parent,v))throw new IllegalArgumentException("Settle seated and unwind to actual zero at the clear dock first");
    for(var car:VehicleOperations.consist(l,v)){if(body)car.flying.body=closed;else car.flying.legs=closed;car.restraints=car.flying.body||car.flying.legs;car.revision++;}changed(l);
  }
  public static void unwind(ServerLevel l,FoundationData.Vehicle v){
    if(!dock(l,v))throw new IllegalArgumentException("Stop at the clear bound dock before unwinding");
    for(var car:VehicleOperations.consist(l,v))if(!car.flying.retained()||!car.fault.isEmpty()||car.yaw.resumeRequired||!FlyingRuntime.powered(l,car))throw new IllegalArgumentException("Secure both restraints, restore power and resolve faults before unwind");
    for(var car:VehicleOperations.consist(l,v)){car.yaw.mode=STATION_CAPTURE;car.yaw.travelAuthorized=false;car.yaw.lastTarget=0;car.flying.target=0;car.revision++;car.yaw.epoch++;}changed(l);
  }
  public static void retry(ServerLevel l,FoundationData.Vehicle v){
    for(var car:VehicleOperations.consist(l,v)){var b=body(l,car.chassis);if(b==null||speed(b)>.05||!car.stopping||!car.flying.retained()||!FlyingRuntime.powered(l,car))throw new IllegalArgumentException("Stop, retain both restraints and restore power before retry");var reason=safety(l,b,car);if(reason!=null)throw new IllegalArgumentException(reason);}
    for(var car:VehicleOperations.consist(l,v)){var s=car.yaw;s.mode=BLEND;s.section=null;s.resumeRequired=false;s.status="READY";s.epoch++;car.fault="";car.revision++;}changed(l);
  }
  /** Idempotent named operation; duplicate/stale command IDs cannot restart a show. */
  public static boolean show(ServerLevel l,FoundationData.Vehicle v,UUID command,long epoch,String name){
    var s=v.yaw;if(epoch!=s.epoch||command.equals(s.command))return false;
    var b=body(l,v.chassis);var m=b==null?null:MOTION.get(b);if(m==null||m.interval==null||m.interval.mode()!=SHOW_STOP||!m.interval.stopName().equals(name)||!v.stopping||speed(b)>.05||!v.flying.retained()||s.resumeRequired||!v.fault.isEmpty())throw new IllegalArgumentException("Named show requires a stopped, retained, valid interval");
    var cars=VehicleOperations.consist(l,v);
    for(var car:cars){var parent=body(l,car.chassis);if(parent==null||MOTION.get(parent)==null||!car.stopping||speed(parent)>.05||!car.flying.retained()||!car.fault.isEmpty())throw new IllegalArgumentException("Stop and retain every car before acknowledging the Show");}
    for(var car:cars){var parent=body(l,car.chassis);var motion=MOTION.get(parent);
      if(motion.showHold==null||!motion.showHold.isValid()){
        var anchor=new Vector3d(parent.getMassTracker().getCenterOfMass());
        var world=parent.logicalPose().transformPosition(new Vector3d(anchor));
        motion.showHold=SubLevelContainer.getContainer(l).physicsSystem().getPipeline().addConstraint(null,parent,
            new GenericConstraintConfiguration(world,anchor,new Quaterniond(parent.logicalPose().orientation()),new Quaterniond(),EnumSet.allOf(ConstraintJointAxis.class)));
      }
    }
    s.command=command;s.stopName=name;s.status="SHOW_RUNNING";s.holdAcknowledged=true;s.showTicks=0;s.lastShowTick=-1;s.mode=SHOW_STOP;s.travelAuthorized=false;s.epoch++;v.revision++;changed(l);return true;
  }
  private static void changed(ServerLevel l){FoundationData.get(l).setDirty();FoundationSync.broadcast(l);}
}
