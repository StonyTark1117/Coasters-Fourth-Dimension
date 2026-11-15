package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** S1 native Y joint: passive relative damping, bounded dock capture and chassis-relative lift lock. */
public final class SpinningRuntime {
  private static final class State {
    final DockCapture latch=new DockCapture();
    RotationServo servo;
    boolean initialized;
    long checked=Long.MIN_VALUE;
  }
  private static final Map<ServerSubLevel,State> STATES=new WeakHashMap<>();
  private SpinningRuntime() {}
  public static void fault(FoundationData.Vehicle v,String reason) {
    v.stopping=true;v.restraints=true;v.fault=reason;
    for(var c:v.carriers) {c.capture=true;c.latched=false;}
  }
  public static boolean dock(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v) {
    if(v.dock==null || !level.hasChunkAt(v.dock)
        || !(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station)
        || !v.chassis.equals(station.vehicle))return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);
    return center!=null && center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))<=144
        && new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y>.98;
  }
  public static boolean ready(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v) {
    if(!v.stopping || !v.fault.isEmpty() || !dock(level,parent,v))return false;
    var container=SubLevelContainer.getContainer(level);
    var c=v.carriers.getFirst();
    return c.capture && c.latched && !c.liftHeld && container.getSubLevel(c.body)!=null
        && Math.abs(c.omega)<=.05 && Math.abs(c.angle-c.captureTarget)<=DockCapture.ANGLE_TOLERANCE
        && container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()<=.05;
  }
  public static void resumeToDock(ServerLevel level,FoundationData.Vehicle v) {
    var container=SubLevelContainer.getContainer(level);
    if(!v.stopping || !v.restraints || v.maintenance || v.alignmentRequired
        || !(container.getSubLevel(v.chassis) instanceof ServerSubLevel parent)
        || container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05)
      throw new IllegalArgumentException("Stop, retain riders and repair support before S1 resume");
    var rail=FoundationData.get(level).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
    var reason=safety(level,parent,v,rail);
    if(reason!=null)throw new IllegalArgumentException(reason);
    if(dock(level,parent,v))throw new IllegalArgumentException("At the dock use Capture then Dispatch");
    if(rail.liftLock && !v.carriers.getFirst().liftHeld)
      throw new IllegalArgumentException("Restore the captured lift state before resuming");
    v.fault="";v.stopping=false;
    for(var c:v.carriers){c.capture=false;c.latched=false;}
    v.revision++;FoundationData.get(level).setDirty();
  }
  public static double jointInertia(ServerSubLevel child,FoundationData.Carrier c) {
    var mass=child.getMassTracker();var center=child.getPlot().getCenterBlock();
    var arm=new Vector3d(mass.getCenterOfMass()).sub(center.getX()+.5+c.childPivotX,
        center.getY()+.5+c.childPivotOffset,center.getZ()+.5+c.childPivotZ);
    return mass.getInertiaTensor().m11()+mass.getMass()*(arm.x*arm.x+arm.z*arm.z);
  }
  public static String safety(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v,FoundationData.Rail rail) {
    if(rail==null || !rail.valid)return "S1 matching track unavailable";
    if(v.dock!=null && (!level.hasChunkAt(v.dock)
        || !(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station)
        || !v.chassis.equals(station.vehicle))) return "S1 designated dock unavailable; restore then Capture";
    var edge=TrackGeometry.resolve(level,rail);
    if(edge==null)return "S1 route unavailable";
    var reason=SpinningTrack.rejection(level,edge);if(reason!=null)return reason;
    if(!ConsistRules.eligible(level,parent,v))return "S1 permits one car only";
    var container=SubLevelContainer.getContainer(level);
    if(container.getSubLevel(v.carriers.getFirst().body)==null)return "S1 carrier body unavailable";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "S1 running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return "S1 track position unavailable";
    var speed=container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length();
    if(speed>10.05)return "S1 travel speed exceeds 10 blocks/s";
    if(Math.abs(v.carriers.getFirst().omega)>3)return "S1 spin exceeds 3 rad/s; stopped for inspection";
    var table=TrackGeometry.table(level,edge);double distance=table.distanceAt(hit.t());
    // Look in both directions through the braking distance; no assumption about rollback sign.
    double look=speed*speed/12+2.25;
    for(double s=Math.max(0,distance-look);s<=Math.min(table.length(),distance+look);s+=.5) {
      double t=table.parameterAt(s);var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      if(SpinningTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(point,edge,t))!=null)
        return "S1 rider sweep obstructed; clear it then Capture";
    }
    var span=ChainLiftSpanQuery.findAtGraphHit(level,hit,0);
    if(span!=null && Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16))<.001)
      return "S1 Chain power unavailable; restore then Capture and Dispatch";
    if(rail.liftLock && span==null)return "S1 lift requires a powered native Chain span";
    return null;
  }
  public static void update(ServerLevel level,ServerSubLevel parent,ServerSubLevel child,
      FoundationData.Vehicle v,FoundationData.Carrier c,FoundationData.Rail rail,
      RotaryConstraintHandle handle,double dt) {
    var state=STATES.computeIfAbsent(child,k->new State());
    if(!state.initialized) {
      state.initialized=true;c.latched=false;
      if(!v.stopping)fault(v,"S1 reload holds travel; inspect then Capture and Dispatch");
    }
    var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    double travel=pipeline.getLinearVelocity(parent,new Vector3d()).length();
    if(state.checked!=level.getGameTime()) {
      state.checked=level.getGameTime();
      var reason=safety(level,parent,v,rail);if(reason!=null)fault(v,reason);
    }
    if(v.stopping || v.maintenance)VehicleOperations.brake(level,v,dt);
    if(rail!=null && rail.liftLock && !v.stopping) {
      if(!c.liftHeld) {
        if(Math.abs(c.omega)>.05)fault(v,"S1 lift entry requires a settled captured carrier");
        else {c.liftHeld=true;c.captureTarget=c.angle;}
      }
      c.liftFrom=rail.from;c.liftTo=rail.to;
    } else if(c.liftHeld && !v.stopping && rail!=null && !rail.matches(c.liftFrom,c.liftTo)) {
      var edge=TrackGeometry.resolve(level,rail);var center=CoasterCartResolve.cartCenterWorld(parent);
      if(edge!=null && center!=null) {
        var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
        var table=TrackGeometry.table(level,edge);
        double s=table.distanceAt(hit.t());
        boolean from=edge.from().equals(c.liftFrom)||edge.from().equals(c.liftTo);
        if((from?s:table.length()-s)>=SpinningVehicle.FIT.envelope().halfLength()) {
          c.liftHeld=false;c.liftFrom=null;c.liftTo=null;
        }
      }
    }
    boolean atDock=dock(level,parent,v) && v.fault.isEmpty();
    boolean aligning=v.stopping && c.capture && atDock && travel<=.05 && Math.abs(c.omega)<=.35;
    boolean drive=aligning || c.liftHeld;
    double inertia=jointInertia(child,c);
    if(!Double.isFinite(inertia) || inertia<=0) {fault(v,"S1 native inertia unavailable");return;}
    if(aligning) {
      if(state.servo==null) {
        c.captureTarget=Math.rint(c.angle/(2*Math.PI))*2*Math.PI;
        state.servo=new RotationServo(c.angle,DockCapture.MAX_SPEED,DockCapture.MAX_ACCELERATION);
      }
    } else state.servo=null;
    double goal=aligning?state.servo.advance(c.captureTarget,c.angle,dt):c.captureTarget;
    double decay=.35+(Math.abs(c.omega)>2?2:0)+(v.stopping?2:0);
    // Force-based motor: coefficient c = I * decay, never a raw rate on unequal bodies.
    handle.setMotor(RotaryConstraintHandle.DEFAULT_AXIS,Math.atan2(Math.sin(goal),Math.cos(goal)),
        drive?inertia*8:0,drive?inertia*6:inertia*decay,false,
        drive?inertia*.5:inertia*10);
    c.latched=state.latch.update(level.getGameTime(),aligning,c.angle-c.captureTarget,c.omega,travel);
    if(v.stopping && c.latched)c.liftHeld=false;
  }
}
