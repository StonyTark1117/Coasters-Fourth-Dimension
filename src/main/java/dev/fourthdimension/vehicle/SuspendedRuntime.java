package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** One native constrained pendulum, with train-wide retention and separately guarded locks. */
public final class SuspendedRuntime {
  public static final double LIMIT=Math.PI/4, SOFT=Math.toRadians(35);
  private static final class State {
    final DockCapture dock=new DockCapture(), entry=new DockCapture();
    RotationServo servo;
    boolean initialized;
    long checked=Long.MIN_VALUE;
  }
  private static final Map<ServerSubLevel,State> STATES=new WeakHashMap<>();
  private SuspendedRuntime() {}
  public static void fault(ServerLevel level,FoundationData.Vehicle v,String reason) {
    var ids=new HashSet<>(v.suspendedConsist);ids.add(v.chassis);
    var body=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
    if(body!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body));
    for(var id:ids) {
      var car=FoundationData.get(level).vehicles.get(id);if(car==null || !SuspendedVehicle.is(car))continue;
      car.stopping=true;car.restraints=true;car.fault=reason;
      for(var c:car.carriers){c.capture=true;c.latched=false;}
    }
  }
  public static boolean dock(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v) {
    if(v.dock==null || !level.hasChunkAt(v.dock)
        || !(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station))return false;
    var cars=VehicleOperations.consist(level,v);
    if(cars.stream().noneMatch(car->car.chassis.equals(station.vehicle)))return false;
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null || TrackGeometry.table(level,edge).length()<36)return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);
    return center!=null && center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))<=1600
        && new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y>.999;
  }
  public static boolean ready(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v) {
    if(!v.stopping || !v.fault.isEmpty() || !dock(level,parent,v))return false;
    var container=SubLevelContainer.getContainer(level);var c=v.carriers.getFirst();
    return c.capture && c.latched && !c.liftHeld && container.getSubLevel(c.body)!=null
        && Math.abs(c.omega)<=.05 && Math.abs(c.angle)<=DockCapture.ANGLE_TOLERANCE
        && container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()<=.05;
  }
  public static FoundationData.Rail rail(ServerLevel level,FoundationData.Vehicle v) {
    return dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
  }
  /** Deliberate stationary dock rebind accepts a changed train without masking an in-motion loss. */
  public static void rebindDock(ServerLevel level,FoundationData.Vehicle selected,net.minecraft.core.BlockPos pos) {
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    if(cars.isEmpty()||cars.size()>4)throw new IllegalArgumentException("Suspended dock supports one to four cars");
    var ids=cars.stream().map(v->v.chassis).toList();
    for(var v:cars) {
      if(!SuspendedVehicle.is(v)||!v.stopping||v.alignmentRequired||v.maintenance)
        throw new IllegalArgumentException("Stop and repair every Suspended car before dock rebind");
      var c=v.carriers.getFirst();var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
      if(!(container.getSubLevel(v.chassis) instanceof ServerSubLevel parent)||container.getSubLevel(c.body)==null
          ||edge==null||SuspendedTrack.rejection(level,edge)!=null||TrackGeometry.table(level,edge).length()<36
          ||container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05
          ||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999
          ||!c.capture||Math.abs(c.angle)>DockCapture.ANGLE_TOLERANCE||Math.abs(c.omega)>.05
          ||!v.restraints&&!c.occupants.isEmpty())
        throw new IllegalArgumentException("Align and retain every car at the level 36-block dock before rebind");
      var center=CoasterCartResolve.cartCenterWorld(parent);
      if(center==null||center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>1600)
        throw new IllegalArgumentException("Bring the whole train to this dock before rebind");
    }
    for(var v:cars) {
      v.dock=pos.immutable();v.suspendedConsist=List.copyOf(ids);
      if(v.fault.startsWith("Suspended coupler/consist changed"))v.fault="";
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }
  /** A latched station may retain its cabin through the next contiguous lift approach. */
  public static FoundationData.Rail departureLift(ServerLevel level,FoundationData.Vehicle vehicle) {
    var current=rail(level,vehicle);if(current==null)return null;if(current.liftLock)return current;
    var candidates=dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r!=current && r.liftLock && r.valid
        && r.family==vehicle.family && r.variant.equals(vehicle.variant)
        && (r.from.equals(current.from)||r.to.equals(current.from)||r.from.equals(current.to)||r.to.equals(current.to))).toList();
    return candidates.size()==1?candidates.getFirst():null;
  }
  public static String safety(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v,FoundationData.Rail rail) {
    if(rail==null || !rail.valid)return "Suspended matching overhead track unavailable";
    var edge=TrackGeometry.resolve(level,rail);if(edge==null)return "Suspended route unavailable";
    var reason=SuspendedTrack.rejection(level,edge);if(reason!=null)return reason;
    if(!ConsistRules.eligible(level,parent,v))return "Suspended requires one to four identical cars";
    if(!v.suspendedConsist.isEmpty() && !new HashSet<>(v.suspendedConsist).equals(
        new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent))))
      return "Suspended coupler/consist changed; stop and rebind at dock";
    var container=SubLevelContainer.getContainer(level);var c=v.carriers.getFirst();
    if(container.getSubLevel(c.body)==null)return "Suspended cabin body unavailable";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "Suspended running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return "Suspended track position unavailable";
    double speed=container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length();
    if(speed>12.05)return "Suspended speed exceeds 12 blocks/s";
    if(Math.abs(c.omega)>1.5)return "Suspended swing speed exceeds 1.5 rad/s";
    if(Math.abs(c.angle)>LIMIT+.025)return "Suspended physical swing limit exceeded";
    var table=TrackGeometry.table(level,edge);double distance=table.distanceAt(hit.t()),look=speed*speed/12+1.75;
    for(double s=Math.max(0,distance-look);s<=Math.min(table.length(),distance+look);s+=.5) {
      double t=table.parameterAt(s);var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      if(SuspendedTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(point,edge,t))!=null)
        return "Suspended cabin sweep obstructed";
    }
    var span=ChainLiftSpanQuery.findAtGraphHit(level,hit,0);
    double drive=span==null?0:Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16));
    if(span!=null && drive<.001)return "Suspended Chain power lost; lock retained, restore and explicitly resume";
    if(rail.liftLock && (span==null || drive>2.01))return "Suspended lift needs powered Chain at no more than 2 blocks/s";
    if(c.liftHeld && c.liftFrom!=null) {
      var saved=dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(c.liftFrom,c.liftTo)).findFirst().orElse(null);
      if(saved==null || !saved.valid || saved.geometryHash!=c.liftGeometry || TrackGeometry.resolve(level,saved)==null)
        return "Suspended locked lift identity changed";
    }
    return null;
  }
  public static void resumeToDock(ServerLevel level,FoundationData.Vehicle v) {
    var container=SubLevelContainer.getContainer(level);
    for(var car:VehicleOperations.consist(level,v)) {
      if(!car.stopping || !car.restraints || car.maintenance || car.alignmentRequired
          || !(container.getSubLevel(car.chassis) instanceof ServerSubLevel body)
          || container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05)
        throw new IllegalArgumentException("Stop and retain every Suspended car before resume");
      String reason=safety(level,body,car,rail(level,car));if(reason!=null)throw new IllegalArgumentException(reason);
      if(rail(level,car).liftLock && !car.carriers.getFirst().liftHeld)
        throw new IllegalArgumentException("Wait for the guarded lift lock before resume");
    }
    for(var car:VehicleOperations.consist(level,v)) {
      car.fault="";car.stopping=false;car.carriers.forEach(c->{c.capture=false;c.latched=false;});car.revision++;
    }
    FoundationData.get(level).setDirty();
  }
  public static double jointInertia(ServerSubLevel child,FoundationData.Carrier c) {
    var mass=child.getMassTracker();var p=child.getPlot().getCenterBlock();
    var arm=new Vector3d(mass.getCenterOfMass()).sub(p.getX()+.5+c.childPivotX,p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ);
    return mass.getInertiaTensor().m22()+mass.getMass()*(arm.x*arm.x+arm.y*arm.y);
  }
  private static boolean exitClear(ServerLevel level,FoundationData.Vehicle v) {
    var container=SubLevelContainer.getContainer(level);
    for(var car:VehicleOperations.consist(level,v)) {
      var rail=rail(level,car);var c=car.carriers.getFirst();
      if(car.stopping || !car.restraints || c.capture || !c.liftEntered || rail==null || rail.liftLock || !rail.valid)return false;
      var edge=TrackGeometry.resolve(level,rail);var body=container.getSubLevel(car.chassis);
      var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
      if(edge==null || center==null)return false;
      if(c.liftFrom!=null && (edge.from().equals(c.liftFrom)||edge.from().equals(c.liftTo)
          ||edge.to().equals(c.liftFrom)||edge.to().equals(c.liftTo))) {
        var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
        var table=TrackGeometry.table(level,edge);double s=table.distanceAt(hit.t());
        boolean from=edge.from().equals(c.liftFrom)||edge.from().equals(c.liftTo);
        if((from?s:table.length()-s)<1.75)return false;
      }
    }
    return true;
  }
  /** Look ahead through the braking distance, including rollback, before crossing lift entry. */
  private static FoundationData.Rail requestedLift(ServerLevel level,ServerSubLevel parent,FoundationData.Rail rail) {
    if(rail==null)return null;if(rail.liftLock)return rail;
    var edge=TrackGeometry.resolve(level,rail);var center=CoasterCartResolve.cartCenterWorld(parent);
    if(edge==null || center==null)return null;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    var table=TrackGeometry.table(level,edge);double s=table.distanceAt(hit.t());
    var velocity=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d());
    var tangent=dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.sampleEdgePosition(level,edge,Math.min(1,hit.t()+.001))
        .subtract(dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.sampleEdgePosition(level,edge,Math.max(0,hit.t()-.001))).normalize();
    double signed=velocity.dot(tangent.x,tangent.y,tangent.z),speed=velocity.length();
    double look=speed*speed/12+2.5;
    return dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(next->next!=rail && next.liftLock && next.valid
        && ((s<=look && signed<=.01 && (next.from.equals(edge.from())||next.to.equals(edge.from())))
          || (table.length()-s<=look && signed>=-.01 && (next.from.equals(edge.to())||next.to.equals(edge.to()))))).findFirst().orElse(null);
  }
  public static void update(ServerLevel level,ServerSubLevel parent,ServerSubLevel child,
      FoundationData.Vehicle v,FoundationData.Carrier c,FoundationData.Rail rail,GenericConstraintHandle joint,double dt) {
    var state=STATES.computeIfAbsent(child,k->new State());
    if(!state.initialized) {
      state.initialized=true;c.latched=false;
      if(!v.stopping)fault(level,v,"Suspended reload holds travel; inspect and explicitly resume");
    }
    var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    double travel=pipeline.getLinearVelocity(parent,new Vector3d()).length();
    if(state.checked!=level.getGameTime()) {
      state.checked=level.getGameTime();String reason=safety(level,parent,v,rail);
      if(reason!=null)fault(level,v,reason);
    }
    boolean atDock=dock(level,parent,v) && v.fault.isEmpty();
    boolean aligning=v.stopping && c.capture && atDock && travel<=.05 && (c.latched || Math.abs(c.omega)<=.35);
    var entryRail=requestedLift(level,parent,rail);
    if(entryRail!=null && !c.liftHeld && v.restraints && !v.maintenance) {
      c.liftPending=true;
      if(!v.stopping && !c.latched)fault(level,v,"Suspended lift entry not latched; stopped with restraints retained");
      // Reuse the five-tick guard, scaling its speed input to the specified .5 entry threshold.
      if(state.entry.update(level.getGameTime(),true,c.angle,c.omega,travel/10)) {
        c.liftHeld=true;c.liftPending=false;c.captureTarget=0;
        c.liftFrom=entryRail.from;c.liftTo=entryRail.to;c.liftGeometry=entryRail.geometryHash;
      }
    } else if(!c.liftHeld) { c.liftPending=false;state.entry.update(level.getGameTime(),false,0,0,0); }
    if(c.liftHeld && rail!=null && rail.liftLock) {
      c.liftEntered=true;
      c.liftFrom=rail.from;c.liftTo=rail.to;c.liftGeometry=rail.geometryHash;
    }
    if(c.liftHeld && entryRail==null && exitClear(level,v)) {
      // Release atomically after the rear of every car clears the last marked segment.
      for(var car:VehicleOperations.consist(level,v))for(var other:car.carriers) {
        other.liftHeld=false;other.liftEntered=false;other.liftFrom=null;other.liftTo=null;other.liftGeometry=0;
      }
    }
    if(aligning && state.servo==null)state.servo=new RotationServo(c.angle,DockCapture.MAX_SPEED,DockCapture.MAX_ACCELERATION);
    if(!aligning)state.servo=null;
    double goal=aligning?state.servo.advance(0,c.angle,dt):0;
    double inertia=jointInertia(child,c);
    if(!Double.isFinite(inertia)||inertia<=0){fault(level,v,"Suspended inertia unavailable");return;}
    c.latched=(c.latched && aligning) || state.dock.update(level.getGameTime(),aligning,c.angle,c.omega,travel);
    if(c.latched && v.stopping && atDock){c.liftHeld=false;c.liftEntered=false;}
    boolean locked=c.liftHeld || c.latched;
    joint.setLimit(ConstraintJointAxis.ANGULAR_Z,locked?0:-LIMIT,locked?0:LIMIT);
    double damping=.65+(Math.abs(c.angle)>SOFT?4:0)+(v.stopping?2:0);
    joint.setMotor(ConstraintJointAxis.ANGULAR_Z,goal,aligning?inertia*8:0,
        aligning?inertia*6:inertia*damping,false,aligning?inertia*.5:inertia*10);
    var pos=child.getPlot().getCenterBlock().below();var block=level.getBlockState(pos);
    if(block.is(VehicleParts.SUSPENDED_LOCK.get()) && block.getValue(SuspendedLockBlock.LOCKED)!=locked)
      level.setBlock(pos,block.setValue(SuspendedLockBlock.LOCKED,locked),3);
  }
}
