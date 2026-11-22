package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.physics.constraint.*;
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
import org.joml.Quaterniond;
import static dev.fourthdimension.vehicle.FlyingState.Phase.*;
import static dev.fourthdimension.vehicle.FlyingState.Lock.*;

/** Station-only physical posture authority. No teleport or force-mount path exists here. */
public final class FlyingRuntime {
  public static final double MAX_SPEED=.25,MAX_ACCELERATION=.35;
  private static final class Motion {
    final DockCapture endpoint=new DockCapture();
    RotationServo servo;
    boolean initialized;
    boolean braking;
    double brakeVelocity,brakeGoal,lastGoal,lastVelocity,motorGoal;
    long checked=Long.MIN_VALUE,started;
  }
  private static final Map<ServerSubLevel,Motion> MOTION=new WeakHashMap<>();
  private FlyingRuntime(){}
  public static Map<String,Double> motorObservation(ServerSubLevel child){
    var m=MOTION.get(child);return m==null?Map.of():Map.of("reference",m.lastGoal,"referenceVelocity",m.lastVelocity,"motorTarget",m.motorGoal);
  }
  public static FoundationData.Rail rail(ServerLevel l,FoundationData.Vehicle v){return FoundationData.get(l).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);}
  public static GenericConstraintHandle createJoint(ServerSubLevel parent,ServerSubLevel child,FoundationData.Carrier c){
    var p=parent.getPlot().getCenterBlock();var b=child.getPlot().getCenterBlock();
    var joint=SubLevelContainer.getContainer(parent.getLevel()).physicsSystem().getPipeline().addConstraint(parent,child,
        new GenericConstraintConfiguration(new Vector3d(p.getX()+.5+c.side,p.getY()+.5+c.pivotHeight,p.getZ()+.5+c.longitudinal),
          new Vector3d(b.getX()+.5+c.childPivotX,b.getY()+.5+c.childPivotOffset,b.getZ()+.5+c.childPivotZ),
          new Quaterniond(),new Quaterniond(),EnumSet.of(ConstraintJointAxis.LINEAR_X,ConstraintJointAxis.LINEAR_Y,
            ConstraintJointAxis.LINEAR_Z,ConstraintJointAxis.ANGULAR_Y,ConstraintJointAxis.ANGULAR_Z)));
    joint.setLimit(ConstraintJointAxis.ANGULAR_X,-Math.PI/2,0);joint.setContactsEnabled(false);return joint;
  }
  public static void fault(ServerLevel l,FoundationData.Vehicle selected,String reason){
    var ids=new HashSet<>(selected.flying.consist);ids.add(selected.chassis);
    var parent=SubLevelContainer.getContainer(l).getSubLevel(selected.chassis);
    if(parent!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent));
    for(var id:ids){var v=FoundationData.get(l).vehicles.get(id);if(v==null||!FlyingVehicle.is(v))continue;
      var s=v.flying;var c=v.carriers.getFirst();
      if(s.transitioning()||s.lock==NONE&&Math.abs(c.angle)>.035){
        if(s.phase!=INTERRUPTED_TRANSITION)s.holdAngle=Math.clamp(-c.angle,0,Math.PI/2);
        s.phase=INTERRUPTED_TRANSITION;s.lock=NONE;s.interruption=reason;
      }
      v.stopping=true;v.restraints=true;s.body=true;s.legs=true;v.fault=reason;c.capture=true;
    }FoundationData.get(l).setDirty();
  }
  private static boolean localDock(ServerLevel l,ServerSubLevel parent,FoundationData.Vehicle v){
    var s=v.flying;
    if(v.dock==null||s.dockFrom==null||s.dockTo==null||!v.stopping||v.alignmentRequired||v.maintenance
        ||!l.hasChunkAt(v.dock)||!(l.getBlockEntity(v.dock) instanceof BoardingStationEntity station))return false;
    if(VehicleOperations.consist(l,v).stream().noneMatch(car->car.chassis.equals(station.vehicle)))return false;
    var r=rail(l,v);var edge=r==null?null:TrackGeometry.resolve(l,r);
    var pipeline=SubLevelContainer.getContainer(l).physicsSystem().getPipeline();
    if(edge==null||!r.matches(s.dockFrom,s.dockTo)||r.geometryHash!=s.dockGeometry||FlyingTrack.rejection(l,edge)!=null
        ||TrackGeometry.table(l,edge).length()<36||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999
        ||pipeline.getLinearVelocity(parent,new Vector3d()).length()>.05)return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);
    if(center==null||center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))>1600)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return false;
    var table=TrackGeometry.table(l,edge);double at=table.distanceAt(hit.t());
    return at>=2.5&&at<=table.length()-2.5&&FlyingTrack.obstruction(l,hit)==null;
  }
  public static boolean dock(ServerLevel l,FoundationData.Vehicle selected){
    try{
      var container=SubLevelContainer.getContainer(l);
      for(var v:VehicleOperations.consist(l,selected))
        if(!FlyingVehicle.is(v)||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel parent)
            ||container.getSubLevel(v.carriers.getFirst().body)==null||!localDock(l,parent,v))return false;
      return true;
    }catch(IllegalArgumentException unavailable){return false;}
  }
  public static boolean ready(ServerLevel l,ServerSubLevel parent,FoundationData.Vehicle v){
    if(!v.fault.isEmpty()||!dock(l,v))return false;
    for(var car:VehicleOperations.consist(l,v)){
      var c=car.carriers.getFirst();if(car.flying.lock!=SEATED||Math.abs(c.angle)>DockCapture.ANGLE_TOLERANCE||Math.abs(c.omega)>.05)return false;
    }return true;
  }
  public static void rebindDock(ServerLevel l,FoundationData.Vehicle selected,BlockPos pos){
    var cars=VehicleOperations.consist(l,selected);var container=SubLevelContainer.getContainer(l);
    if(cars.isEmpty()||cars.size()>4)throw new IllegalArgumentException("Flying supports one to four identical cars");
    for(var v:cars){var r=rail(l,v);var edge=r==null?null:TrackGeometry.resolve(l,r);var c=v.carriers.getFirst();
      if(!FlyingVehicle.is(v)||!v.stopping||v.maintenance||v.alignmentRequired||v.flying.transitioning()
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel parent)||container.getSubLevel(c.body)==null
          ||edge==null||FlyingTrack.rejection(l,edge)!=null||TrackGeometry.table(l,edge).length()<36
          ||container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05
          ||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999
          ||!v.flying.retained()&&!c.occupants.isEmpty())
        throw new IllegalArgumentException("Stop and retain every car at the level 36-block Flying dock");
      var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null||center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>1600)
        throw new IllegalArgumentException("Bring the entire train to the dock");
    }
    var ids=cars.stream().map(v->v.chassis).toList();
    for(var v:cars){var r=rail(l,v);v.dock=pos.immutable();v.flying.dockFrom=r.from;v.flying.dockTo=r.to;
      v.flying.dockGeometry=r.geometryHash;v.flying.consist=ids;
      if(v.fault.startsWith("Flying consist changed"))v.fault="";v.revision++;
    }changed(l);
  }
  public static boolean powered(ServerLevel l,FoundationData.Vehicle v){
    if(dev.fourthdimension.integration.RideAuthority.externalSelected(v))
      return dev.fourthdimension.integration.RideAuthority.externalAccessoryPower(l,v);
    var r=rail(l,v);var edge=r==null?null:TrackGeometry.resolve(l,r);var body=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);if(edge==null||center==null)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));if(hit==null)return false;
    var span=ChainLiftSpanQuery.findAtGraphHit(l,hit,0);
    return span!=null&&Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16))>.001;
  }
  public static String safety(ServerLevel l,ServerSubLevel parent,FoundationData.Vehicle v){
    var r=rail(l,v);var edge=r==null?null:TrackGeometry.resolve(l,r);
    if(edge==null||!r.valid)return "Flying overhead support unavailable";
    String reason=FlyingTrack.rejection(l,edge);if(reason!=null)return reason;
    if(!FamilyControllers.find(parent).equals(Set.of(v.family)))return "Flying controller unavailable";
    if(!ConsistRules.eligible(l,parent,v))return "Flying requires one to four identical supported cars";
    if(!v.flying.consist.isEmpty()&&!new HashSet<>(v.flying.consist).equals(new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent))))
      return "Flying consist changed; stop and rebind at dock";
    var container=SubLevelContainer.getContainer(l);var c=v.carriers.getFirst();
    if(!(container.getSubLevel(c.body) instanceof ServerSubLevel child))return "Flying posture body unavailable";
    for(var p:FlyingVehicle.SEATS)if(!child.getPlot().getEmbeddedLevelAccessor().getBlockState(p).is(VehicleParts.FLYING_SEAT.get()))return "Flying seat unavailable";
    for(var p:FlyingFrameBlock.PARTS)if(!parent.getPlot().getEmbeddedLevelAccessor().getBlockState(p).is(VehicleParts.FLYING_FRAME.get()))return "Flying fixed fork unavailable";
    double speed=container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length();
    if(speed>16.05)return "Flying travel exceeds 16 blocks/s";
    if(c.angle>.04||c.angle< -Math.PI/2-.04)return "Flying posture range exceeded";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "Flying running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));if(hit==null)return "Flying route position unavailable";
    var table=TrackGeometry.table(l,edge);double at=table.distanceAt(hit.t()),look=speed*speed/12+2.5;
    for(double s=Math.max(0,at-look);s<=Math.min(table.length(),at+look);s+=.5){double t=table.parameterAt(s);
      if(FlyingTrack.obstruction(l,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(l,edge,t),edge,t))!=null)
        return "Flying occupied posture/frame sweep obstructed";
    }
    if((!v.stopping||v.flying.transitioning())&&!powered(l,v))return "Flying power lost; retained, explicitly resume after repair";
    return null;
  }
  private static void requireDock(ServerLevel l,FoundationData.Vehicle v,boolean power){
    if(!dock(l,v))throw new IllegalArgumentException("Hold the entire Flying train at its clear level dock");
    var container=SubLevelContainer.getContainer(l);
    for(var car:VehicleOperations.consist(l,v)){
      var reason=safety(l,(ServerSubLevel)container.getSubLevel(car.chassis),car);if(reason!=null)throw new IllegalArgumentException(reason);
      if(power&&!powered(l,car))throw new IllegalArgumentException("Restore dock Chain power before posture changeover");
    }
  }
  public static void restraints(ServerLevel l,FoundationData.Vehicle selected,boolean body,boolean closed){
    requireDock(l,selected,false);
    for(var v:VehicleOperations.consist(l,selected))if(v.flying.lock!=SEATED||v.flying.transitioning()||!v.fault.isEmpty())
      throw new IllegalArgumentException("Only the settled seated endpoint permits body/leg restraint operation");
    for(var v:VehicleOperations.consist(l,selected)){var s=v.flying;if(body)s.body=closed;else s.legs=closed;
      v.restraints=s.body||s.legs;s.phase=s.retained()?SEATED_SECURED:SEATED_OPEN;v.revision++;
    }changed(l);
  }
  public static void requestPosture(ServerLevel l,FoundationData.Vehicle selected,boolean prone,boolean resume){
    requireDock(l,selected,true);var cars=VehicleOperations.consist(l,selected);
    for(var v:cars){var s=v.flying;
      if(!s.retained()||s.transitioning()||resume&&s.phase!=INTERRUPTED_TRANSITION)
        throw new IllegalArgumentException("Close body and leg restraints; resume applies only to interrupted changeover");
      if(Math.abs(v.carriers.getFirst().omega)>.05)
        throw new IllegalArgumentException("Wait for the interrupted posture brake to settle before recovery");
      if(!resume&&prone&&s.lock!=SEATED)throw new IllegalArgumentException("Begin prone changeover from the seated lock");
      if(!prone&&s.lock!=PRONE&&s.phase!=INTERRUPTED_TRANSITION)throw new IllegalArgumentException("Return seated from prone or an interrupted transition");
    }
    for(var v:cars){var s=v.flying;double target=resume?s.target:prone?Math.PI/2:0;
      s.target=target;s.phase=target==0?TO_SEATED:TO_PRONE;s.lock=NONE;s.interruption="";s.progress=0;
      v.fault="";v.stopping=true;v.restraints=true;v.carriers.getFirst().latched=false;v.revision++;
      var child=SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body);var motion=MOTION.get(child);
      if(motion!=null){motion.servo=null;motion.started=l.getGameTime();}
    }changed(l);
  }
  public static void validateDispatch(ServerLevel l,FoundationData.Vehicle selected){
    requireDock(l,selected,true);
    for(var v:VehicleOperations.consist(l,selected))if(!v.fault.isEmpty()||!v.flying.retained()||v.flying.lock!=PRONE||v.flying.transitioning())
      throw new IllegalArgumentException("Every Flying car needs body/leg retention and its five-tick prone lock");
  }
  public static void dispatch(ServerLevel l,FoundationData.Vehicle selected){
    validateDispatch(l,selected);for(var v:VehicleOperations.consist(l,selected)){
      v.stopping=false;v.flying.phase=RUNNING;v.carriers.getFirst().capture=false;v.revision++;
    }changed(l);
  }
  public static void capture(ServerLevel l,FoundationData.Vehicle selected){
    for(var v:VehicleOperations.consist(l,selected)){
      if(v.flying.transitioning()){fault(l,v,"Flying transition stopped by operator; explicit recovery required");continue;}
      v.stopping=true;v.carriers.getFirst().capture=true;v.revision++;
      var parent=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
      if(parent instanceof ServerSubLevel body&&safety(l,body,v)==null&&v.flying.phase!=INTERRUPTED_TRANSITION)v.fault="";
    }changed(l);
  }
  public static void resumeToDock(ServerLevel l,FoundationData.Vehicle selected){
    var cars=VehicleOperations.consist(l,selected);var container=SubLevelContainer.getContainer(l);
    for(var v:cars){
      if(!v.stopping||!v.flying.retained()||v.flying.lock!=PRONE||v.maintenance||v.alignmentRequired
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel parent)
          ||container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05||!powered(l,v))
        throw new IllegalArgumentException("Restore power and retain every stopped prone-locked car before travel resume");
      var reason=safety(l,parent,v);if(reason!=null)throw new IllegalArgumentException(reason);
    }
    for(var v:cars){v.fault="";v.stopping=false;v.flying.phase=RUNNING;v.carriers.getFirst().capture=false;v.revision++;}changed(l);
  }
  private static void changed(ServerLevel l){FoundationData.get(l).setDirty();FoundationSync.broadcast(l);}
  public static double jointInertia(ServerSubLevel child,FoundationData.Carrier c){
    var mass=child.getMassTracker();var p=child.getPlot().getCenterBlock();
    var arm=new Vector3d(mass.getCenterOfMass()).sub(p.getX()+.5+c.childPivotX,p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ);
    return mass.getInertiaTensor().m00()+mass.getMass()*(arm.y*arm.y+arm.z*arm.z);
  }
  /** Actual native gravity moment about the hinge, including current rider load and bank. */
  public static double gravityTorque(ServerSubLevel parent,ServerSubLevel child,FoundationData.Carrier c){
    var mass=child.getMassTracker();var p=child.getPlot().getCenterBlock();
    var arm=new Vector3d(mass.getCenterOfMass()).sub(p.getX()+.5+c.childPivotX,
        p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ).rotate(child.logicalPose().orientation());
    var force=dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(child.getLevel()).mul(mass.getMass());
    return arm.cross(force).dot(new Vector3d(1,0,0).rotate(parent.logicalPose().orientation()));
  }
  public static void update(ServerLevel l,ServerSubLevel parent,ServerSubLevel child,FoundationData.Vehicle v,
      FoundationData.Carrier c,FoundationData.Rail rail,GenericConstraintHandle joint,double dt){
    var m=MOTION.computeIfAbsent(child,k->new Motion());var s=v.flying;
    if(!m.initialized){m.initialized=true;m.started=l.getGameTime();m.lastGoal=s.referenceInitialized?s.referenceAngle:s.lock==NONE?c.angle:-s.holdAngle;
      m.lastVelocity=s.referenceInitialized?s.referenceVelocity:c.omega;
      if(s.transitioning())fault(l,v,"Flying restarted during transition; inspect and explicitly resume");
      else if(!v.stopping)fault(l,v,"Flying restart retains prone lock; explicitly resume travel");
    }
    if(m.checked!=l.getGameTime()){
      m.checked=l.getGameTime();var reason=safety(l,parent,v);if(reason!=null)fault(l,v,reason);
      if(s.transitioning()&&(!dock(l,v)||!s.retained()))fault(l,v,"Flying dock/retention authority lost during transition");
      if(s.transitioning()&&l.getGameTime()-m.started>600)fault(l,v,"Flying posture jam/timeout; actual pose retained");
    }
    double travel=SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length();
    boolean moving=s.transitioning()&&v.fault.isEmpty();
    if(moving&&m.servo==null){m.servo=new RotationServo(m.lastGoal,.18,.15);m.started=l.getGameTime();}
    if(!moving)m.servo=null;
    double goal=moving?m.servo.advance(s.target==0?0:-s.target+.015,c.angle,dt):s.lock!=NONE?-s.holdAngle:
      s.phase==INTERRUPTED_TRANSITION?-s.holdAngle:0;
    if(s.phase==INTERRUPTED_TRANSITION){
      if(!m.braking){m.braking=true;m.brakeVelocity=m.lastVelocity;
        // Continue the already-issued reference before decelerating it; no target discontinuity.
        m.brakeGoal=m.lastGoal;}
      m.brakeVelocity+=Math.clamp(-m.brakeVelocity,-.15*dt,.15*dt);
      m.brakeGoal=Math.clamp(m.brakeGoal+m.brakeVelocity*dt,-Math.PI/2,0);
      goal=m.brakeGoal;s.holdAngle=-goal;
    }else m.braking=false;
    boolean endpoint=(moving||s.phase!=INTERRUPTED_TRANSITION&&s.lock==NONE)&&v.fault.isEmpty()&&dock(l,v);
    double target=moving?s.target:0;
    if(m.endpoint.update(l.getGameTime(),endpoint&&Math.abs(c.omega)<.015,c.angle+target,c.omega,travel)){
      s.lock=target==0?SEATED:PRONE;s.phase=target==0?(s.retained()?SEATED_LOCKED:SEATED_OPEN):PRONE_LOCKED;
      s.progress=1;c.latched=true;c.captureTarget=c.angle;s.holdAngle=-goal;m.servo=null;
    }
    if(s.lock==PRONE&&v.stopping&&s.phase==RUNNING&&dock(l,v))s.phase=DOCK_PRONE;
    c.latched=s.lock!=NONE;c.capture=v.stopping;
    double inertia=jointInertia(child,c);if(!Double.isFinite(inertia)||inertia<=0){fault(l,v,"Flying native inertia unavailable");return;}
    joint.setLimit(ConstraintJointAxis.ANGULAR_X,s.lock!=NONE?c.captureTarget:-Math.PI/2,s.lock!=NONE?c.captureTarget:0);
    // Rapier's native motor is acceleration-based: gains are independent of body mass.
    // The 0.1-second tracking lag smooths the bounded reference; stiffness leaves enough
    // holding margin for the empty row's gravity load without an endpoint pose correction.
    double stiffness=4000,damping=400;
    double motorGoal=goal-gravityTorque(parent,child,c)/(inertia*stiffness);
    joint.setMotor(ConstraintJointAxis.ANGULAR_X,motorGoal,stiffness,damping,false,0);m.motorGoal=motorGoal;
    m.lastGoal=goal;m.lastVelocity=moving&&m.servo!=null?m.servo.velocity():m.braking?m.brakeVelocity:0;
    s.referenceInitialized=true;s.referenceAngle=m.lastGoal;s.referenceVelocity=m.lastVelocity;
    if(moving)s.progress=Math.clamp(s.target==0?1+2*c.angle/Math.PI:-2*c.angle/Math.PI,0,1);
    for(var offset:c.seats){var p=child.getPlot().getCenterBlock().offset(offset);var block=l.getBlockState(p);
      if(block.is(VehicleParts.FLYING_SEAT.get())&&(block.getValue(CarrierSeatBlock.RESTRAINED)!=s.body||block.getValue(FlyingSeatBlock.LEGS)!=s.legs))
        l.setBlock(p,block.setValue(CarrierSeatBlock.RESTRAINED,s.body).setValue(FlyingSeatBlock.LEGS,s.legs),3);
    }
    var p=child.getPlot().getCenterBlock().below();var block=l.getBlockState(p);
    if(block.is(VehicleParts.FLYING_LOCK.get())&&block.getValue(FlyingLockBlock.LOCKED)!=c.latched)l.setBlock(p,block.setValue(FlyingLockBlock.LOCKED,c.latched),3);
  }
}
