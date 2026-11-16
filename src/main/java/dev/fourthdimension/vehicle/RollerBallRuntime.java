package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.station.BoardingStationEntity;
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
import static dev.fourthdimension.vehicle.RollerBallState.Section.*;
import static dev.fourthdimension.vehicle.RollerBallState.Phase.*;

/** Native gravity/inertia tumble. The guide rotates the carrier; Chain alone powers travel. */
public final class RollerBallRuntime {
  private static final class Live { boolean started;long checked=Long.MIN_VALUE,lastTick=Long.MIN_VALUE,routeTick=Long.MIN_VALUE;int aligned;final DockCapture dock=new DockCapture();GenericConstraintHandle hold,activeJoint,latchJoint;boolean jointLocked;double jointTarget=Double.NaN;Vector3d travelPosition; }
  private static final Map<ServerSubLevel,Live> LIVE=new WeakHashMap<>();
  private RollerBallRuntime(){}
  public static FoundationData.Rail rail(ServerLevel l,FoundationData.Vehicle v){return FoundationData.get(l).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);}
  public static GenericConstraintHandle joint(ServerSubLevel parent,ServerSubLevel child,FoundationData.Carrier c){
    return joint(parent,child,c,false,0);
  }
  private static GenericConstraintHandle joint(ServerSubLevel parent,ServerSubLevel child,FoundationData.Carrier c,boolean locked,double latch){
    var p=parent.getPlot().getCenterBlock();var b=child.getPlot().getCenterBlock();
    var axes=locked?EnumSet.of(ConstraintJointAxis.ANGULAR_X):EnumSet.of(ConstraintJointAxis.LINEAR_X,ConstraintJointAxis.LINEAR_Y,ConstraintJointAxis.LINEAR_Z,ConstraintJointAxis.ANGULAR_Y,ConstraintJointAxis.ANGULAR_Z);
    var joint=SubLevelContainer.getContainer(parent.getLevel()).physicsSystem().getPipeline().addConstraint(parent,child,
      new GenericConstraintConfiguration(new Vector3d(p.getX()+.5,p.getY()+.5+c.pivotHeight,p.getZ()+.5),
        new Vector3d(b.getX()+.5,b.getY()+1.5,b.getZ()+.5),new Quaterniond().rotateX(latch),new Quaterniond(),axes));
    joint.setContactsEnabled(false);return joint;
  }
  public static void fault(ServerLevel l,FoundationData.Vehicle v,String reason){
    v.stopping=true;v.restraints=true;v.fault=reason;v.roller.phase=HOLD;
    for(var c:v.carriers)c.capture=false;
    FoundationData.get(l).setDirty();
  }
  private static boolean dock(ServerLevel l,ServerSubLevel p,FoundationData.Vehicle v){
    var s=v.roller;var r=rail(l,v);var edge=r==null?null:TrackGeometry.resolve(l,r);
    if(v.dock==null||s.dockFrom==null||!l.hasChunkAt(v.dock)||!(l.getBlockEntity(v.dock) instanceof BoardingStationEntity station)||!v.chassis.equals(station.vehicle)
        ||edge==null||r.rollerSection!=DOCK||!r.matches(s.dockFrom,s.dockTo)||r.geometryHash!=s.dockGeometry||TrackGeometry.table(l,edge).length()<8||RollerBallTrack.rejection(l,edge)!=null)return false;
    var center=CoasterCartResolve.cartCenterWorld(p);if(center==null||new Vector3d(0,1,0).rotate(p.logicalPose().orientation()).y<.999)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));var t=TrackGeometry.table(l,edge);double at=t.distanceAt(hit.t());
    return at>=2&&at<=t.length()-2&&center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))<=144&&RollerBallTrack.obstruction(l,hit)==null;
  }
  public static boolean ready(ServerLevel l,ServerSubLevel p,FoundationData.Vehicle v){
    var c=v.carriers.getFirst();return v.stopping&&!v.alignmentRequired&&v.fault.isEmpty()&&dock(l,p,v)&&ConsistRules.eligible(l,p,v)
      &&c.capture&&c.latched&&Math.abs(c.omega)<=.05&&Math.abs(c.angle-c.captureTarget)<=DockCapture.ANGLE_TOLERANCE
      &&SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()<=.05;
  }
  public static void rebindDock(ServerLevel l,FoundationData.Vehicle v,BlockPos pos){
    var r=rail(l,v);var e=r==null?null:TrackGeometry.resolve(l,r);var raw=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(!v.stopping||!ConsistRules.eligible(l,raw,v)||e==null||TrackGeometry.table(l,e).length()<8||!(raw instanceof ServerSubLevel p)
        ||new Vector3d(0,1,0).rotate(p.logicalPose().orientation()).y<.999||SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()>.05)
      throw new IllegalArgumentException("Stop one Roller Ball on a level eight-block dock");
    var center=CoasterCartResolve.cartCenterWorld(p);if(center==null||center.distance(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>12)throw new IllegalArgumentException("Bring Roller Ball within twelve blocks of dock");
    r.rollerSection=DOCK;v.dock=pos.immutable();v.roller.dockFrom=r.from;v.roller.dockTo=r.to;v.roller.dockGeometry=r.geometryHash;v.carriers.getFirst().capture=true;v.revision++;FoundationData.get(l).setDirty();
  }
  public static String safety(ServerLevel l,ServerSubLevel p,FoundationData.Vehicle v,FoundationData.Rail r){
    var e=r==null?null:TrackGeometry.resolve(l,r);if(e==null||!r.valid)return "Roller Ball matching support unavailable";
    var reason=RollerBallTrack.rejection(l,e);if(reason!=null)return reason;
    if(!FamilyControllers.find(p).equals(Set.of(v.family)))return "Roller Ball controller unavailable";
    if(!ConsistRules.eligible(l,p,v))return "Roller Ball supports exactly one car and no recovery bearing";
    if(!v.stopping&&!v.restraints)return "Roller Ball travel requires closed restraints";
    var child=SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body);if(child==null)return "Roller Ball carrier unavailable";
    for(var pos:RollerBallVehicle.SEATS)if(!child.getPlot().getEmbeddedLevelAccessor().getBlockState(pos).is(VehicleParts.ROLLER_BALL_SEAT.get()))return "Roller Ball seat unavailable";
    if(Math.abs(v.carriers.getFirst().omega)>3)return "Roller Ball tumble exceeds three radians/s";
    var center=CoasterCartResolve.cartCenterWorld(p);if(center==null)return "Roller Ball running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,e,new Vec3(center.x,center.y,center.z));
    double speed=SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length();
    var table=TrackGeometry.table(l,e);double at=table.distanceAt(hit.t()),look=speed*speed/48+2.75;
    for(double distance=Math.max(0,at-look);distance<=Math.min(table.length(),at+look);distance+=.5){
      var obstruction=obstruction(l,e,distance);
      if(obstruction!=null)return "Roller Ball sweep obstructed at "+obstruction.toShortString();
    }
    for(boolean ahead:new boolean[]{false,true}){
      double remaining=ahead?table.length()-at:at;
      if(remaining>=look)continue;
      var endpoint=ahead?e.to():e.from();
      var following=FoundationData.get(l).rails.stream().filter(n->n!=r&&(n.from.equals(endpoint)||n.to.equals(endpoint))).toList();
      // A stationary newly built dock need not yet have its complete ride attached.
      if(following.isEmpty()&&v.stopping)continue;
      if(following.size()!=1)return "Roller Ball route end or ambiguous connection";
      var next=TrackGeometry.resolve(l,following.getFirst());
      if(next==null||RollerBallTrack.rejection(l,next)!=null)return "Roller Ball incompatible route ahead";
      double length=TrackGeometry.table(l,next).length();
      for(double distance=0;distance<=Math.min(look-remaining,length);distance+=.5){
        var obstruction=obstruction(l,next,next.from().equals(endpoint)?distance:length-distance);
        if(obstruction!=null)return "Roller Ball connection sweep obstructed at "+obstruction.toShortString();
      }
    }
    if(r.rollerSection==LIFT||r.rollerSection==SUMMIT){var span=ChainLiftSpanQuery.findAtGraphHit(l,hit,0);
      if(span==null||Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16))<.001)return "Roller Ball lift Chain power unavailable";}
    return null;
  }
  private static BlockPos obstruction(ServerLevel l,CoasterPathEdge edge,double distance){
    double t=TrackGeometry.table(l,edge).parameterAt(distance);
    return RollerBallTrack.obstruction(l,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(l,edge,t),edge,t));
  }
  public static void capture(ServerLevel l,FoundationData.Vehicle v){
    var p=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(p instanceof ServerSubLevel parent&&!v.alignmentRequired&&safety(l,parent,v,rail(l,v))==null)v.fault="";
    v.stopping=true;v.carriers.getFirst().capture=true;v.roller.initialized=false;v.revision++;FoundationData.get(l).setDirty();
  }
  public static void resume(ServerLevel l,FoundationData.Vehicle v){
    var p=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(!v.stopping||!v.restraints||v.alignmentRequired||v.maintenance||!(p instanceof ServerSubLevel parent))throw new IllegalArgumentException("Stop, retain and repair Roller Ball before Resume");
    var reason=safety(l,parent,v,rail(l,v));if(reason!=null)throw new IllegalArgumentException(reason);
    if(SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05)throw new IllegalArgumentException("Wait for travel to stop");
    v.roller.routeHash=RollerBallTrack.validateRoute(l,v);v.stopping=false;v.fault="";v.roller.restartHold=false;v.carriers.getFirst().capture=false;v.revision++;FoundationData.get(l).setDirty();
  }
  public static void releaseHold(ServerLevel l,FoundationData.Vehicle v){
    var child=SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body);
    var live=LIVE.get(child);if(live!=null&&live.hold!=null){if(live.hold.isValid())live.hold.remove();live.hold=null;}
  }
  public static double inertia(ServerSubLevel child,FoundationData.Carrier c){
    var m=child.getMassTracker();var b=child.getPlot().getCenterBlock();var arm=new Vector3d(m.getCenterOfMass()).sub(b.getX()+.5,b.getY()+1.5,b.getZ()+.5);
    return m.getInertiaTensor().m00()+m.getMass()*(arm.y*arm.y+arm.z*arm.z);
  }
  public static GenericConstraintHandle update(ServerLevel l,ServerSubLevel p,ServerSubLevel child,FoundationData.Vehicle v,FoundationData.Carrier c,FoundationData.Rail r,GenericConstraintHandle joint,double dt){
    var live=LIVE.computeIfAbsent(child,k->new Live());var s=v.roller;var pipeline=SubLevelContainer.getContainer(l).physicsSystem().getPipeline();
    if(!live.started){live.started=true;if(!v.stopping){v.stopping=true;v.restraints=true;c.capture=false;s.restartHold=true;v.fault="Roller Ball restored at measured pose; inspect and explicitly Resume";}}
    if(Math.abs(c.omega)>3)fault(l,v,"Roller Ball tumble exceeds three radians/s");
    if(live.checked!=l.getGameTime()){live.checked=l.getGameTime();var reason=safety(l,p,v,r);if(reason!=null&&!v.alignmentRequired)fault(l,v,reason);}
    if(!v.stopping&&live.routeTick!=l.getGameTime()/20){live.routeTick=l.getGameTime()/20;
      try{if(s.routeHash!=RollerBallTrack.routeStamp(l,v))fault(l,v,"Roller Ball route binding changed; inspect and Resume");}
      catch(IllegalArgumentException invalid){fault(l,v,invalid.getMessage());}
    }
    if(!v.stopping&&live.hold!=null){if(live.hold.isValid())live.hold.remove();live.hold=null;}
    var velocity=pipeline.getLinearVelocity(p,new Vector3d());double speed=velocity.length();
    var measuredCenter=CoasterCartResolve.cartCenterWorld(p);
    var measuredVelocity=live.travelPosition==null?new Vector3d(velocity):new Vector3d(measuredCenter).sub(live.travelPosition).div(dt);
    live.travelPosition=new Vector3d(measuredCenter);
    if(v.stopping&&(live.hold==null||!live.hold.isValid())){
      // A stationary native support, not a repeated pose write: gravity cannot roll a held lift back.
      var anchor=new Vector3d(p.getMassTracker().getCenterOfMass());
      var world=p.logicalPose().transformPosition(new Vector3d(anchor));
      live.hold=pipeline.addConstraint(null,p,new GenericConstraintConfiguration(world,anchor,
          new Quaterniond(p.logicalPose().orientation()),new Quaterniond(),EnumSet.allOf(ConstraintJointAxis.class)));
    }
    var up=new Vector3d(0,1,0).rotate(p.logicalPose().orientation());var axis=new Vector3d(1,0,0).rotate(p.logicalPose().orientation());
    // Projection onto the constant world transverse axis survives native forward sign conventions.
    s.pitch=FreeRotor.unwrap(s.pitch,Math.atan2(up.z*axis.x,up.y));
    boolean guided=!v.stopping&&!s.released&&r!=null&&(r.rollerSection==DOCK||r.rollerSection==LIFT||r.rollerSection==SUMMIT||r.rollerSection==FREE);
    boolean capture=v.stopping&&c.capture&&v.fault.isEmpty()&&speed<=.05&&dock(l,p,v);
    var edge=r==null?null:TrackGeometry.resolve(l,r);
    if(!v.stopping&&edge!=null){
      var center=CoasterCartResolve.cartCenterWorld(p);var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
      var basis=CoasterPathTrackFrame.trackBasisAtHit(l,hit);double signed=velocity.dot(basis.forward().x,basis.forward().y,basis.forward().z);
      if(Math.abs(signed)>.1){if(s.direction!=0&&Math.signum(signed)!=s.direction)fault(l,v,"Roller Ball unexpected reversal; retained brake");else s.direction=Math.signum(signed);}
      boolean chainDriven=false;
      Vector3d childBeforeChain=null;
      if(!v.stopping&&r.rollerSection!=FREE){
        var span=ChainLiftSpanQuery.findAtGraphHit(l,hit,0);
        double chainSpeed=span==null?0:ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16);
        double target=span==null?0:-ChainLiftSpanQuery.chainWorldVelocityAtHit(l,span.path(),hit,chainSpeed,0).dot(basis.forward());
        var gravity=dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(l);
        double alongGravity=gravity.dot(basis.forward().x,basis.forward().y,basis.forward().z);
        if(Math.abs(chainSpeed)>=.001&&target*alongGravity< -1e-6){
          // Coasters speed-matches only the running-gear body. Our separate, loaded
          // carrier otherwise dilutes that correction through the hinge and can stall
          // before a vertical join. Share the native uphill Chain drive with its load;
          // level/downhill spans retain their existing native path, with no assistance
          // on unpowered rail or independent target/gravity cancellation.
          childBeforeChain=pipeline.getLinearVelocity(child,new Vector3d());
          CoasterCartChainLiftDrive.applySpeedMatch(child,l,SubLevelContainer.getContainer(l).physicsSystem(),hit,basis,dt);
          chainDriven=true;
        }
      }
      double limit=guided?1.75:r.rollerSection==RETURN?2.5:6.5;
      if(guided){
        var path=TrackGeometry.table(l,edge);double distance=path.distanceAt(hit.t());
        double boundary=Math.min(distance,path.length()-distance);
        // Curves are tangent-continuous but curvature can step at an anchor. Approach
        // and leave that join slowly: the native running-gear frame can still be
        // finishing the curve when the nearest center hit has entered the straight.
        limit=Math.min(limit,.1+.5*Math.max(0,boundary-1));
      }
      var tangent=new Vector3d(basis.forward().x,basis.forward().y,basis.forward().z);
      double travelSpeed=measuredVelocity.dot(tangent);
      if(chainDriven){
        // Chain writes a pre-solve velocity to each body. Predict their shared
        // translation after gravity, so a stronger loaded pull cannot outrun the
        // join limit for one substep. Keeping gravity in this estimate avoids
        // mistaking lift support for travel (and braking a loaded car to rest).
        double parentMass=p.getMassTracker().getMass(),childMass=child.getMassTracker().getMass();
        var childVelocity=pipeline.getLinearVelocity(child,new Vector3d());
        double predicted=pipeline.getLinearVelocity(p,new Vector3d()).mul(parentMass)
            .add(new Vector3d(childVelocity).mul(childMass))
            .div(parentMass+childMass).dot(tangent)
            +dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(l).dot(tangent)*dt;
        // Native Chain can accelerate at 64 blocks/s², while this guide's brake
        // is bounded to 24. Trim only the extra carrier pull which that brake
        // cannot absorb this substep; never weaken braking or invent propulsion.
        double direction=Math.signum(predicted);
        double added=Math.max(0,childVelocity.sub(childBeforeChain).dot(tangent)*direction);
        double excess=Math.max(0,Math.abs(predicted)-limit-24*dt);
        double trim=Math.min(added,excess*(parentMass+childMass)/childMass);
        if(trim>0){
          pipeline.addLinearAndAngularVelocity(child,new Vector3d(tangent).mul(-direction*trim),new Vector3d());
          predicted-=direction*trim*childMass/(parentMass+childMass);
        }
        if(Math.abs(predicted)>Math.abs(travelSpeed))travelSpeed=predicted;
      }
      if(Math.abs(travelSpeed)>limit){
        var delta=tangent.mul(-Math.copySign(Math.min(Math.abs(travelSpeed)-limit,24*dt),travelSpeed));
        // Brake both bodies equally using completed travel and, on powered rail,
        // the gravity-adjusted prediction above rather than raw support velocity.
        pipeline.addLinearAndAngularVelocity(p,new Vector3d(delta),new Vector3d());
        pipeline.addLinearAndAngularVelocity(child,new Vector3d(delta),new Vector3d());
      }
      if(!s.released&&r.rollerSection==FREE){s.pendingRelease=true;var table=TrackGeometry.table(l,edge);double at=table.distanceAt(hit.t());
        if((signed>=0?at:table.length()-at)>=2.75){s.released=true;s.pendingRelease=false;c.liftHeld=false;c.latched=false;s.phase=SUMMIT_RELEASE;guided=false;}}
    }
    if(v.stopping||v.maintenance)VehicleOperations.brake(l,v,dt);
    if(!v.stopping&&r!=null&&r.rollerSection==DOCK&&s.released&&edge!=null){
      var center=CoasterCartResolve.cartCenterWorld(p);var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));var table=TrackGeometry.table(l,edge);double at=table.distanceAt(hit.t());
      if(at>=2.75&&at<=table.length()-2.75){v.stopping=true;c.capture=true;s.phase=DOCK_CAPTURE;}
    }
    boolean retainedLift=c.liftHeld&&(v.stopping||guided);boolean brakeGuide=v.stopping&&!s.released&&s.initialized&&!capture&&!retainedLift;boolean driving=capture||guided||retainedLift||brakeGuide;
    double target=c.angle;
    if(capture){if(!s.initialized)c.captureTarget=Math.rint(c.angle/(2*Math.PI))*2*Math.PI;target=c.captureTarget;s.phase=DOCK_CAPTURE;}
    else if(guided){target=-s.pitch+Math.rint((c.angle+s.pitch)/(2*Math.PI))*2*Math.PI;s.phase=LIFT_GUIDED;
      if(Math.abs(target-c.angle)>.08)VehicleOperations.brake(l,v,dt);
    }else if(retainedLift)target=c.captureTarget;
    else if(!v.stopping)s.phase=r!=null&&r.rollerSection==RETURN?RETURN_DAMP:FREE_DESCENT;
    double I=inertia(child,c);if(!Double.isFinite(I)||I<=0){fault(l,v,"Roller Ball inertia unavailable");return joint;}
    double goal;
    if(brakeGuide){double next=s.commandRate+Math.clamp(-s.commandRate,-.5*dt,.5*dt);s.command+=(s.commandRate+next)*dt*.5;s.commandRate=next;goal=s.command;}
    else goal=driving?s.advance(target,c.angle,dt,.35,.5):c.angle;
    if(!driving){s.initialized=false;s.command=c.angle;s.commandRate=0;}
    if(guided){
      // Native snapped track orientation can change while reported body angular velocity is zero.
      // Identify a straight lift from its geometry, never that velocity sample.
      boolean straightSection=r.rollerSection==DOCK&&r.from.getY()==r.to.getY()
          ||r.rollerSection==LIFT&&r.from.getZ()==r.to.getZ();
      var center=CoasterCartResolve.cartCenterWorld(p);
      var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
      var table=TrackGeometry.table(l,edge);double distance=table.distanceAt(hit.t());
      double remaining=s.direction>=0?table.length()-distance:distance;
      // Hand over to the upright motor while the frame is still straight, before curvature begins.
      boolean straight=straightSection&&remaining>1&&Math.abs(c.angle-target)<.02&&Math.abs(c.omega)<.02;
      if(live.lastTick!=l.getGameTime()){live.lastTick=l.getGameTime();live.aligned=straight?live.aligned+1:0;}
      boolean settled=c.liftHeld&&straight||live.aligned>=5;if(settled&&!c.liftHeld)c.captureTarget=c.angle;c.liftHeld=settled;if(c.liftHeld)s.phase=LIFT_LOCKED;
    }
    c.latched=live.dock.update(l.getGameTime(),capture,c.angle-c.captureTarget,c.omega,speed);
    boolean locked=c.latched||c.liftHeld;
    // Keep the unlimited motor hinge alive while a separate native X latch is engaged.
    // Removing the latch then preserves the already-preloaded gravity compensation.
    double latch=locked?Math.atan2(Math.sin(c.captureTarget),Math.cos(c.captureTarget)):0;
    if(live.activeJoint!=joint||live.jointLocked!=locked||Double.compare(live.jointTarget,latch)!=0){
      if(live.latchJoint!=null&&live.latchJoint.isValid())live.latchJoint.remove();
      live.latchJoint=locked?joint(p,child,c,true,latch):null;
      live.activeJoint=joint;live.jointLocked=locked;live.jointTarget=latch;
    }
    var b=child.getPlot().getCenterBlock();var arm=new Vector3d(child.getMassTracker().getCenterOfMass()).sub(b.getX()+.5,b.getY()+1.5,b.getZ()+.5).rotate(child.logicalPose().orientation());
    var gravity=dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(l).mul(child.getMassTracker().getMass());double torque=arm.cross(gravity).dot(axis);
    double decay=.6+(v.stopping||r!=null&&r.rollerSection==RETURN?2:0)+(Math.abs(c.omega)>2?20:0);
    // A position-motor offset reacts only after its implicit spring solve. Counter the
    // measured gravity load as a native torque impulse at each guided substep instead,
    // with an equal opposite chassis reaction. Free descent receives no such torque.
    if(driving&&!locked){
      pipeline.applyLinearAndAngularImpulse(child,new Vector3d(),new Vector3d(-torque*dt,0,0),true);
      pipeline.applyLinearAndAngularImpulse(p,new Vector3d(),new Vector3d(torque*dt,0,0),true);
    }
    joint.setMotor(ConstraintJointAxis.ANGULAR_X,Math.atan2(Math.sin(goal),Math.cos(goal)),driving?40000:0,driving?4000:decay,false,0);
    var frame=new BlockPos(0,2,0);var blocks=p.getPlot().getEmbeddedLevelAccessor();var state=blocks.getBlockState(frame);
    boolean damping=!driving&&decay>.6;
    if(state.is(VehicleParts.ROLLER_BALL_FRAME.get())&&(state.getValue(RollerBallFrameBlock.LATCHED)!=locked||state.getValue(RollerBallFrameBlock.DAMPING)!=damping))
      blocks.setBlock(frame,state.setValue(RollerBallFrameBlock.LATCHED,locked).setValue(RollerBallFrameBlock.DAMPING,damping),3);
    if(c.latched){s.released=false;s.pendingRelease=false;s.direction=0;s.phase=v.restraints?SECURED:DOCK_OPEN;c.liftHeld=false;}
    return joint;
  }
}
