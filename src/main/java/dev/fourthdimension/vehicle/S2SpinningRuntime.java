package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** S2 full-roll free joint, independent magnetic torque and retained train-wide capture. */
public final class S2SpinningRuntime {
  private static final class Motion {
    final DockCapture latch=new DockCapture();RotationServo servo;
    boolean initialized;long checked=Long.MIN_VALUE;
  }
  private static final Map<ServerSubLevel,Motion> MOTION=new WeakHashMap<>();
  private S2SpinningRuntime(){}
  public static FoundationData.Rail rail(ServerLevel level,FoundationData.Vehicle v){
    return dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
  }
  public static void fault(ServerLevel level,FoundationData.Vehicle selected,String reason){
    var ids=new HashSet<>(selected.s2.consist);ids.add(selected.chassis);
    var parent=SubLevelContainer.getContainer(level).getSubLevel(selected.chassis);
    if(parent!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent));
    for(var id:ids){var v=FoundationData.get(level).vehicles.get(id);if(v==null||!S2SpinningVehicle.is(v))continue;
      v.stopping=true;v.restraints=true;v.fault=reason;v.s2.resumeRequired=true;v.s2.phase="FAULT";
      for(var c:v.carriers){c.capture=true;c.latched=false;}
    }
  }
  public static void rememberConsist(ServerLevel level,FoundationData.Vehicle v){
    v.s2.consist=List.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(SubLevelContainer.getContainer(level).getSubLevel(v.chassis)));
  }
  private static boolean dock(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    if(v.dock==null||v.s2.dockFrom==null||!level.hasChunkAt(v.dock)
        ||!(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station))return false;
    if(VehicleOperations.consist(level,v).stream().noneMatch(car->car.chassis.equals(station.vehicle)))return false;
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.matches(v.s2.dockFrom,v.s2.dockTo)||rail.geometryHash!=v.s2.dockGeometry
        ||TrackGeometry.table(level,edge).length()<36)return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);
    if(center==null||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return false;
    double at=TrackGeometry.table(level,edge).distanceAt(hit.t());
    return at>=2.25&&at<=TrackGeometry.table(level,edge).length()-2.25
        &&center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))<=1600;
  }
  public static boolean ready(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    try{
      var container=SubLevelContainer.getContainer(level);
      if(!ConsistRules.eligible(level,parent,v))return false;
      for(var car:VehicleOperations.consist(level,v)){
        var c=car.carriers.getFirst();
        if(!car.stopping||!car.fault.isEmpty()||car.s2.resumeRequired||car.alignmentRequired
            ||!(container.getSubLevel(car.chassis) instanceof ServerSubLevel p)||!dock(level,p,car)
            ||!c.capture||!c.latched||c.liftHeld||Math.abs(c.omega)>.05
            ||Math.abs(c.angle-c.captureTarget)>DockCapture.ANGLE_TOLERANCE
            ||container.getSubLevel(c.body)==null
            ||container.physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()>.05)return false;
      }return true;
    }catch(IllegalArgumentException unavailable){return false;}
  }
  public static void rebindDock(ServerLevel level,FoundationData.Vehicle selected,BlockPos pos){
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    if(cars.size()>4)throw new IllegalArgumentException("Launched Inverting Spinning permits one to four cars");
    for(var v:cars){var r=rail(level,v);var edge=r==null?null:TrackGeometry.resolve(level,r);
      if(!S2SpinningVehicle.is(v)||!v.stopping||v.alignmentRequired||v.maintenance
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel p)||edge==null
          ||S2SpinningTrack.rejection(level,edge)!=null||TrackGeometry.table(level,edge).length()<36
          ||container.physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()>.05
          ||new Vector3d(0,1,0).rotate(p.logicalPose().orientation()).y<.999)
        throw new IllegalArgumentException("Stop every Launched Inverting Spinning car on a clear level 36-block dock before binding");
    }
    for(var v:cars){var r=rail(level,v);v.dock=pos.immutable();v.s2.dockFrom=r.from;v.s2.dockTo=r.to;
      v.s2.dockGeometry=r.geometryHash;v.s2.consist=List.of();
      if(v.fault.startsWith("Launched Inverting Spinning coupler/consist changed")
          ||v.fault.startsWith("S2 coupler/consist changed")){v.fault="";v.s2.resumeRequired=false;}
      v.revision++;
    }FoundationData.get(level).setDirty();
  }
  public static String safety(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v,FoundationData.Rail rail){
    var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.valid)return "Launched Inverting Spinning matching track unavailable";
    var reason=S2SpinningTrack.rejection(level,edge);if(reason!=null)return reason;
    if(!FamilyControllers.find(parent).equals(Set.of(v.family)))return "Launched Inverting Spinning controller unavailable";
    if(!ConsistRules.eligible(level,parent,v))return "Launched Inverting Spinning requires one to four identical supported cars";
    if(!v.s2.consist.isEmpty()&&!new HashSet<>(v.s2.consist).equals(new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent))))
      return "Launched Inverting Spinning coupler/consist changed; stop and rebind at dock";
    var container=SubLevelContainer.getContainer(level);
    if(container.getSubLevel(v.carriers.getFirst().body)==null)return "Launched Inverting Spinning carrier unavailable";
    if(v.dock!=null&&(!level.hasChunkAt(v.dock)||!(level.getBlockEntity(v.dock) instanceof BoardingStationEntity)))
      return "Launched Inverting Spinning dock unavailable";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "Launched Inverting Spinning native bearing unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return "Launched Inverting Spinning track position unavailable";
    var velocity=container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d());
    double speed=velocity.length();if(speed>16.05)return "Launched Inverting Spinning travel exceeds 16 blocks/s";
    if(Math.abs(v.carriers.getFirst().omega)>4)return "Launched Inverting Spinning spin exceeds 4 rad/s";
    if(optional(v)){
      if(!dev.fourthdimension.integration.OptionalIntegrations.installed(dev.fourthdimension.integration.OptionalIntegrations.MAGNETIZED).mayInspect())return "Launched Inverting Spinning Magnetized unavailable";
    }else if(v.s2.governor!=null){
      var governor=governor(level,v);
      if(governor==null)return "Launched Inverting Spinning launch governor unavailable";
      var unavailable=governor.availability();if(unavailable!=null)return "Launched Inverting Spinning "+unavailable;
    }else if(!v.stopping)return "Launched Inverting Spinning launch governor is not bound";
    var table=TrackGeometry.table(level,edge);double at=table.distanceAt(hit.t()),look=speed*speed/12+2.25;
    for(double s=Math.max(0,at-look);s<=Math.min(table.length(),at+look);s+=.5){
      double t=table.parameterAt(s);var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var obstruction=S2SpinningTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(obstruction!=null)return "Launched Inverting Spinning rotating sweep obstructed at "+obstruction.toShortString();
    }
    var activeChain=ChainLiftSpanQuery.findAtGraphHit(level,hit,0);
    if(rail.liftLock&&activeChain==null)return "Launched Inverting Spinning lift lock requires a governed native Chain span";
    if(!optional(v)&&!v.stopping&&activeChain!=null&&Math.abs(activeChain.anchor().getSpeed())<.001){
      var controller=governor(level,v);
      // The governed source starts at zero during its bounded launch ramp.
      if(controller==null||!activeChain.anchor().getBlockPos().equals(controller.anchor))return "Launched Inverting Spinning return Chain power lost; retained travel hold";
    }
    var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    double signed=velocity.dot(basis.forward().x,basis.forward().y,basis.forward().z);
    if(Math.abs(signed)>.01&&(signed>=0?table.length()-at:at)<=look){
      var endpoint=signed>=0?edge.to():edge.from();
      var next=dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r!=rail&&(r.from.equals(endpoint)||r.to.equals(endpoint))).toList();
      if(next.size()!=1)return "Launched Inverting Spinning end or ambiguous route; stop and inspect";
      var following=TrackGeometry.resolve(level,next.getFirst());
      if(following==null||S2SpinningTrack.rejection(level,following)!=null)return "Launched Inverting Spinning incompatible route ahead";
    }
    return null;
  }
  public static void capture(ServerLevel level,FoundationData.Vehicle selected){
    if(optional(selected))dev.fourthdimension.integration.RideAuthority.emergencyHold(level,selected);
    var container=SubLevelContainer.getContainer(level);
    for(var v:VehicleOperations.consist(level,selected)){v.stopping=true;v.carriers.forEach(c->c.capture=true);}
    var governor=governor(level,selected);
    if(!optional(selected)&&governor!=null&&governor.availability()==null)governor.acknowledge(selected);
    for(var v:VehicleOperations.consist(level,selected)){
      if(container.getSubLevel(v.chassis) instanceof ServerSubLevel p
          &&safety(level,p,v,rail(level,v))==null&&!v.alignmentRequired){v.fault="";v.s2.resumeRequired=false;}
      v.stopping=true;v.carriers.forEach(c->c.capture=true);v.s2.phase="DAMP";v.revision++;
    }FoundationData.get(level).setDirty();
  }
  public static void resume(ServerLevel level,FoundationData.Vehicle selected){
    var container=SubLevelContainer.getContainer(level);var cars=VehicleOperations.consist(level,selected);
    for(var v:cars){
      if(!v.stopping||!v.restraints||v.maintenance||v.alignmentRequired
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel p)
          ||container.physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()>.05)
        throw new IllegalArgumentException("Stop and retain all Launched Inverting Spinning riders before explicit resume");
      var reason=safety(level,p,v,rail(level,v));if(reason!=null)throw new IllegalArgumentException(reason);
      if(dock(level,p,v))throw new IllegalArgumentException("At the Launched Inverting Spinning dock use Capture then Dispatch");
    }
    start(level,selected);
    for(var v:cars){v.fault="";v.s2.resumeRequired=false;v.s2.rampSpeed=0;
      v.stopping=false;v.s2.phase="FREE";rememberConsist(level,v);v.carriers.forEach(c->{c.capture=false;c.latched=false;});v.revision++;}
    FoundationData.get(level).setDirty();
  }
  public static void dispatched(ServerLevel level,FoundationData.Vehicle v){
    rememberConsist(level,v);v.s2.rampSpeed=0;v.s2.resumeRequired=false;v.s2.phase="DISPATCH";
  }
  public static S2SpinningGovernorEntity governor(ServerLevel level,FoundationData.Vehicle v){
    return v.s2.governor!=null&&level.hasChunkAt(v.s2.governor)
        &&level.getBlockEntity(v.s2.governor) instanceof S2SpinningGovernorEntity g&&g.authorizesRide(v.chassis)?g:null;
  }
  /** Runs before the common dispatch mutates stopping/capture state. */
  public static boolean optional(FoundationData.Vehicle v){return dev.fourthdimension.integration.RideAuthority.MAGNETIZED.equals(v.authority.provider);}
  public static void start(ServerLevel level,FoundationData.Vehicle v){
    if(optional(v)){
      dev.fourthdimension.integration.OptionalDrive.validateRelease(level,v);
      if(dev.fourthdimension.integration.RideAuthority.holds(level,v))throw new IllegalArgumentException("Release through the selected Magnetized authority");
      return;
    }
    var governor=governor(level,v);if(governor==null)throw new IllegalArgumentException("Bind a powered Launched Inverting Spinning kinetic launch governor");
    governor.release(v);
  }
  public static void direction(ServerLevel level,FoundationData.Vehicle v,int sign){
    if(Math.abs(sign)!=1)throw new IllegalArgumentException("Launched Inverting Spinning direction must be forward or reverse");
    var container=SubLevelContainer.getContainer(level);
    for(var car:VehicleOperations.consist(level,v)){
      var raw=container.getSubLevel(car.chassis);
      if(!car.stopping||!(raw instanceof ServerSubLevel parent)||container.physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05)
        throw new IllegalArgumentException("Stop the entire Launched Inverting Spinning train before authorizing direction");
    }
    capture(level,v);
    if(optional(v))dev.fourthdimension.integration.RideAuthority.direction(level,v,sign);
    for(var car:VehicleOperations.consist(level,v))car.s2.direction=sign;
  }
  public static void update(ServerLevel level,ServerSubLevel parent,ServerSubLevel child,
      FoundationData.Vehicle v,FoundationData.Carrier c,FoundationData.Rail rail,RotaryConstraintHandle joint,double dt){
    var m=MOTION.computeIfAbsent(child,k->new Motion());
    if(!m.initialized){m.initialized=true;c.latched=false;if(!v.stopping)fault(level,v,"Launched Inverting Spinning reload hold; inspect and explicitly resume");}
    if(m.checked!=level.getGameTime()){m.checked=level.getGameTime();var reason=safety(level,parent,v,rail);if(reason!=null)fault(level,v,reason);}
    if(!optional(v)&&!v.stopping&&v.fault.isEmpty()){
      var governor=governor(level,v);
      try{if(governor==null)throw new IllegalArgumentException("Launched Inverting Spinning launch governor unavailable");
        governor.command(v.chassis,v.s2.epoch,12*v.s2.direction,3);v.s2.rampSpeed=Math.abs(governor.actualBps());
      }catch(IllegalArgumentException denied){fault(level,v,denied.getMessage());}
    }
    var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    double travel=pipeline.getLinearVelocity(parent,new Vector3d()).length();
    if(rail!=null&&rail.liftLock&&!v.stopping){
      if(!c.liftHeld){
        if(Math.abs(c.omega)>.05)fault(level,v,"Launched Inverting Spinning lift entry requires a settled captured carrier");
        else {c.liftHeld=true;c.captureTarget=c.angle;}
      }
      c.liftFrom=rail.from;c.liftTo=rail.to;
    }else if(c.liftHeld&&!v.stopping&&rail!=null&&!rail.matches(c.liftFrom,c.liftTo)){
      var edge=TrackGeometry.resolve(level,rail);var center=CoasterCartResolve.cartCenterWorld(parent);
      if(edge!=null&&center!=null){
        var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
        var table=TrackGeometry.table(level,edge);double at=table.distanceAt(hit.t());
        boolean from=edge.from().equals(c.liftFrom)||edge.from().equals(c.liftTo);
        if((from?at:table.length()-at)>=S2SpinningVehicle.FIT.envelope().halfLength()){
          c.liftHeld=false;c.liftFrom=null;c.liftTo=null;
        }
      }
    }
    boolean aligning=v.stopping&&c.capture&&v.fault.isEmpty()&&dock(level,parent,v)&&travel<=.05&&Math.abs(c.omega)<=.35;
    if(aligning&&m.servo==null){c.captureTarget=Math.rint(c.angle/(2*Math.PI))*2*Math.PI;m.servo=new RotationServo(c.angle,DockCapture.MAX_SPEED,DockCapture.MAX_ACCELERATION);}
    if(!aligning)m.servo=null;
    double goal=aligning?m.servo.advance(c.captureTarget,c.angle,dt):c.liftHeld?c.captureTarget:c.angle;
    boolean drive=aligning||c.liftHeld;
    double inertia=SpinningRuntime.jointInertia(child,c);
    if(!Double.isFinite(inertia)||inertia<=0){fault(level,v,"Launched Inverting Spinning native inertia unavailable");return;}
    double decay=.20+(Math.abs(c.omega)>3?2:0)+(v.stopping?2:0);
    joint.setMotor(RotaryConstraintHandle.DEFAULT_AXIS,Math.atan2(Math.sin(goal),Math.cos(goal)),
        drive?inertia*8:0,drive?inertia*6:inertia*decay,false,drive?inertia*.5:inertia*10);
    S2SpinningMagnet.update(level,parent,child,v,c,rail,dt);
    c.latched=m.latch.update(level.getGameTime(),aligning,c.angle-c.captureTarget,c.omega,travel);
    if(v.fault.isEmpty())v.s2.phase=c.latched?(v.restraints?"CAPTURED":"DOCK_OPEN"):aligning?"STOPPED_ALIGN":v.stopping?"DAMP":"FREE";
  }
}
