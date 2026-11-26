package dev.fourthdimension.moving;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.*;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.world.phys.Vec3;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Temporary local posture locks while a normally dispatched train clears an authenticated bridge. */
public final class MovingServiceRuntime {
  private static final Map<ServerLevel,Map<UUID,List<GenericConstraintHandle>>> LIVE=new WeakHashMap<>();
  private static final Map<ServerLevel,Map<UUID,GenericConstraintHandle>> FAULT_HOLDS=new WeakHashMap<>();
  private static final Map<ServerLevel,Map<UUID,Map<UUID,GenericConstraintHandle>>> FAULT_LOCAL=new WeakHashMap<>();
  private MovingServiceRuntime(){}
  private static Map<UUID,List<GenericConstraintHandle>> live(ServerLevel l){return LIVE.computeIfAbsent(l,k->new HashMap<>());}
  public static MovingAssemblyState owner(ServerLevel level,FoundationData.Vehicle v){
    for(var state:FoundationData.get(level).moving.assemblies.values())if(!state.service.cars.isEmpty()&&state.cars.contains(v.chassis))return state;
    return null;
  }
  public static boolean applies(ServerLevel l,FoundationData.Vehicle v){return owner(l,v)!=null;}
  /** Install all local locks before releasing any bed lock. No world/body or bed/body joint is made. */
  public static void prepare(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock){
    if(!state.retained()||!state.service.cars.isEmpty())throw new IllegalArgumentException("Prepare service handoff once for a retained train");
    var next=new MovingServiceState();next.dock=dock.name();var installed=new LinkedHashMap<UUID,List<GenericConstraintHandle>>();
    try{
      for(var id:state.cars){var v=FoundationData.get(level).vehicles.get(id);if(v==null){var car=new MovingServiceState.Car(id,List.of());next.cars.put(id,car);installed.put(id,List.of());continue;}
        var holds=new ArrayList<MovingServiceState.Hold>();
        if(NewGenerationFlyingVehicle.is(v))holds.add(measure(level,id,v.yaw.deck));
        for(var c:v.carriers){var parent=NewGenerationFlyingVehicle.is(v)?v.yaw.deck:id;if(!parent.equals(c.body))holds.add(measure(level,parent,c.body));}
        var car=new MovingServiceState.Car(id,holds);next.cars.put(id,car);installed.put(id,install(level,car));
      }
      MovingRouteAdapters.prepareRelease(level,state,dock);
      installed.forEach(live(level)::put);state.service=next;
    }catch(RuntimeException error){installed.values().forEach(MovingServiceRuntime::remove);throw error;}
  }
  public static void rollback(ServerLevel level,MovingAssemblyState state){
    for(var id:state.service.cars.keySet()){remove(live(level).remove(id));removeCarFaults(level,state.service.cars.get(id));clearPartial(level,id);}state.service=new MovingServiceState();
  }
  private static MovingServiceState.Hold measure(ServerLevel l,UUID parentId,UUID childId){
    var parent=body(l,parentId);var child=body(l,childId);var physics=SubLevelContainer.getContainer(l).physicsSystem();
    physics.updatePose(parent);physics.updatePose(child);
    var localB=new Vector3d(child.getMassTracker().getCenterOfMass());
    var world=child.logicalPose().transformPosition(new Vector3d(localB));
    var localA=new Vector3d(world).sub(parent.logicalPose().position())
        .rotate(new Quaterniond(parent.logicalPose().orientation()).conjugate()).add(parent.logicalPose().rotationPoint());
    var relative=new Quaterniond(parent.logicalPose().orientation()).conjugate().mul(child.logicalPose().orientation());
    return new MovingServiceState.Hold(parentId,childId,pose(localA,relative),pose(localB,new Quaterniond()));
  }
  private static MovingAssemblyState.Pose pose(Vector3d p,Quaterniond q){return new MovingAssemblyState.Pose(p.x,p.y,p.z,q.x,q.y,q.z,q.w);}
  private static List<GenericConstraintHandle> install(ServerLevel l,MovingServiceState.Car car){
    var result=new ArrayList<GenericConstraintHandle>();try{
      for(var h:car.holds){var a=body(l,h.parent());var b=body(l,h.child());
        var aw=a.logicalPose().transformPosition(h.parentFrame().position());var bw=b.logicalPose().transformPosition(h.childFrame().position());
        var aq=new Quaterniond(a.logicalPose().orientation()).mul(h.parentFrame().orientation());
        var bq=new Quaterniond(b.logicalPose().orientation()).mul(h.childFrame().orientation());
        if(aw.distance(bw)>.025||MovingNativeAdapter.angularError(aq,bq)>Math.toRadians(.25))
          throw new IllegalArgumentException("Service posture changed from saved measured frames; retain and inspect");
        var joint=SubLevelContainer.getContainer(l).physicsSystem().getPipeline().addConstraint(a,b,
            new GenericConstraintConfiguration(h.parentFrame().position(),h.childFrame().position(),h.parentFrame().orientation(),h.childFrame().orientation(),EnumSet.allOf(ConstraintJointAxis.class)));
        joint.setContactsEnabled(false);result.add(joint);
      }return List.copyOf(result);
    }catch(RuntimeException error){remove(result);throw error;}
  }
  private static void remove(List<GenericConstraintHandle> holds){if(holds!=null)for(var h:holds)if(h.isValid())h.remove();}
  private static ServerSubLevel body(ServerLevel l,UUID id){var b=SubLevelContainer.getContainer(l).getSubLevel(id);
    if(!(b instanceof ServerSubLevel result)||result.isRemoved())throw new IllegalArgumentException("Service body unavailable: "+id);return result;}
  /** True means ordinary family control must leave the saved local posture alone this callback. */
  public static boolean tick(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v,double dt){
    var state=owner(level,v);if(state==null)return false;var car=state.service.cars.get(v.chassis);if(car==null)return false;
    try{
      if(FAULT_LOCAL.getOrDefault(level,Map.of()).containsKey(v.chassis)){
        car.dispatched=false;v.stopping=true;retainSurvivors(level,state,car,dt);v.boardingReady=false;return true;
      }
      var handles=live(level).get(v.chassis);
      if(handles==null){car.dispatched=false;v.stopping=true;handles=install(level,car);live(level).put(v.chassis,handles);}
      if(handles.stream().anyMatch(h->!h.isValid()))throw new IllegalArgumentException("Service posture lock lost");
      for(var c:v.carriers){var child=body(level,c.body);NativeLoading.update(child,v,c);}
      dev.fourthdimension.integration.RideAuthority.tick(level,v,dt);
      if(v.authority.owner!=null&&dev.fourthdimension.integration.RideAuthority.holds(level,v)){car.dispatched=false;v.stopping=true;}
      if(!v.restraints||v.maintenance||!v.fault.isEmpty()){car.dispatched=false;v.stopping=true;}
      if(MovingNativeAdapter.liveDockEdge(level,state)==null)throw new IllegalArgumentException("Released bridge is not physically locked; retain train");
      if(v.family==dev.fourthdimension.contract.Family.PAX&&car.dispatched){
        if(!MovingPaxRecovery.movingDepartureReady(level,state)){v.stopping=true;VehicleOperations.brake(level,v,dt);holdStopped(level,v);return true;}
        removeCarFaults(level,car);v.stopping=false;
      }
      if(completeExit(level,state,v)){
        var rail=MovingRouteContext.rail(level,v.boundFrom,v.boundTo);
        if(rail==null&& !WingVehicle.is(v))throw new IllegalArgumentException("Restore matching fixed departure support");
        var edge=WingVehicle.is(v)?WingTrack.support(level,v):TrackGeometry.resolve(level,rail);
        var entry=MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.byId(state.configuration));
        MovingAdmission.inspectInsert(level,entry,edge,"Actual fixed departure");
        String reason=MovingAdmission.trackRejection(level,entry.configuration(),edge);if(reason!=null)throw new IllegalArgumentException(reason);
        if(!car.dispatched){v.stopping=true;VehicleOperations.brake(level,v,dt);return true;}
        MovingRouteAdapters.validateExit(level,state,v,state.docks.get(state.service.dock));
        remove(live(level).remove(v.chassis));removeCarFaults(level,car);state.service.cars.remove(v.chassis);adoptFixed(v);FoundationData.get(level).setDirty();return false;
      }
      if(!car.dispatched||v.stopping){VehicleOperations.brake(level,v,dt);holdStopped(level,v);}
      else serviceDrive(level,v,state);
      v.boardingReady=false;return true;
    }catch(IllegalArgumentException failure){car.dispatched=false;v.stopping=true;v.fault=failure.getMessage();car.fault=v.fault;VehicleOperations.brake(level,v,dt);holdStopped(level,v);retainSurvivors(level,state,car,dt);return true;}
  }
  private static boolean completeExit(ServerLevel l,MovingAssemblyState s,FoundationData.Vehicle v){
    var dock=s.docks.get(s.service.dock);if(dock==null)throw new IllegalArgumentException("Service dock identity lost");
    if(v.boundFrom==null||v.boundTo==null||v.boundFrom.equals(dock.from())&&v.boundTo.equals(dock.to())
        ||v.boundFrom.equals(dock.to())&&v.boundTo.equals(dock.from()))return false;
    var bounds=MovingAdmission.carBounds(l,s,v);
    return bounds.minZ()>s.deckLength/2.0+.25||bounds.maxZ()<-s.deckLength/2.0-.25;
  }
  public static void validateDispatch(ServerLevel l,FoundationData.Vehicle selected){
    var s=owner(l,selected);if(s==null||!s.released||MovingNativeAdapter.liveDockEdge(l,s)==null)
      throw new IllegalArgumentException("Restore and explicitly resume the physically locked released bridge");
    var ids=VehicleOperations.consist(l,selected).stream().map(v->v.chassis).collect(java.util.stream.Collectors.toSet());
    if(!ids.equals(new HashSet<>(s.cars)))throw new IllegalArgumentException("Service departure requires the same complete train");
    for(var id:s.cars){var v=FoundationData.get(l).vehicles.get(id);var car=s.service.cars.get(id);
      if(v==null||!v.restraints||!v.stopping||v.maintenance||v.alignmentRequired||!v.fault.isEmpty()||v.recoveryCart!=null)
        throw new IllegalArgumentException("Stop, retain and repair every service car before explicit dispatch");
      if(car==null){
        if(!Boarding.ready(l,v))throw new IllegalArgumentException("Capture the already-cleared car on its actual fixed support before redispatch");
        MovingRouteAdapters.validateExit(l,s,v,s.docks.get(s.service.dock));continue;
      }
      if(FAULT_LOCAL.getOrDefault(l,Map.of()).containsKey(id))throw new IllegalArgumentException("Restore all missing service bodies and acknowledge Stop before dispatch");
      var holds=live(l).get(id);if(holds==null||holds.stream().anyMatch(h->!h.isValid()))throw new IllegalArgumentException("Service local locks are not ready");
      MovingRouteAdapters.validateExit(l,s,v,s.docks.get(s.service.dock));
      departureDrive(l,v,s);
    }
  }
  /** Uses the existing fixed approach/departure drive. This helper never adds force or creates power. */
  public static void dispatch(ServerLevel l,FoundationData.Vehicle selected){
    validateDispatch(l,selected);var s=owner(l,selected);
    for(var id:s.cars){var v=FoundationData.get(l).vehicles.get(id);
      if(v.authority.owner!=null&&dev.fourthdimension.integration.RideAuthority.holds(l,v))
        throw new IllegalArgumentException("Release service departure through the original bound propulsion authority");}
    boolean pax=selected.family==dev.fourthdimension.contract.Family.PAX;
    if(pax)MovingPaxRecovery.beginMovingRecovery(l,s,s.docks.get(s.service.dock));
    for(var id:s.cars){var hold=FAULT_HOLDS.getOrDefault(l,Map.of()).get(id);if(!pax&&hold!=null&&hold.isValid())hold.remove();
      if(!pax)FAULT_HOLDS.computeIfAbsent(l,k->new HashMap<>()).remove(id);var v=FoundationData.get(l).vehicles.get(id);var service=s.service.cars.get(id);if(service!=null){if(!pax)removeCarFaults(l,service);service.dispatched=true;}v.stopping=pax;v.revision++;}
    FoundationData.get(l).setDirty();
  }
  private static CoasterPathTrackFrame.GraphHit serviceHit(ServerLevel l,FoundationData.Vehicle v,MovingAssemblyState s){
    var edge=MovingNativeAdapter.liveDockEdge(l,s);if(edge==null)throw new IllegalArgumentException("Service bridge lock unavailable");
    var center=CoasterCartResolve.cartCenterWorld(body(l,v.chassis));
    var point=new Vec3(center.x,center.y,center.z);
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,point);
    // The chassis can reach fixed support before its trailing carrier clears the bed.
    // Refresh the existing drive on that actual adjoining edge, not the clamped bridge endpoint.
    for(var fixed:MovingAdmission.dockEdges(l,s.docks.get(s.service.dock))){
      var candidate=CoasterPathTrackFrame.closestHitOnEdge(l,fixed,point);
      if(candidate!=null&&(hit==null||candidate.point().distanceToSqr(point)<hit.point().distanceToSqr(point)))hit=candidate;
    }
    return hit;
  }
  private static void departureDrive(ServerLevel l,FoundationData.Vehicle v,MovingAssemblyState s){
    if(v.family==dev.fourthdimension.contract.Family.PAX)return; // Actual stopped carriage/catch handoff belongs to the winch.
    String provider=v.authority.provider;
    if(v.authority.owner!=null&&(provider.equals(dev.fourthdimension.integration.RideAuthority.ENGINEERED)
        ||provider.equals(dev.fourthdimension.integration.RideAuthority.MAGNETIZED)))return; // Existing authority checks its real fixed consumer.
    var hit=serviceHit(l,v,s);var span=ChainLiftSpanQuery.findAtGraphHit(l,hit,0);
    if(span==null)throw new IllegalArgumentException("Provide a real fixed-powered native Chain departure span across the locked bridge");
    if(PoweredInvertedYawVehicle.is(v)){
      var g=PoweredInvertedYawRuntime.governor(l,v);var rail=MovingRouteContext.rail(l,v.boundFrom,v.boundTo);
      if(g==null||rail==null||g.railDirection(rail)==0||g.systemAvailability()!=null)
        throw new IllegalArgumentException("Bind this locked service span to the original Powered Yaw governor and actual fixed power");
    }else if(FlyingBikeVehicle.is(v)){
      var g=FlyingBikeRuntime.governor(l,v);
      if(g==null||g.availability()!=null||g.spanAt(hit)==0)throw new IllegalArgumentException("Flying Bike service departure must use one of its two actual bound launch spans");
    }else{
      var path=span.anchor().resolveChainLiftSpanPath(0);
      if(path==null||Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),path,0,256,16))<.01)
        throw new IllegalArgumentException("Supply real fixed kinetic power for service departure");
    }
  }
  private static void serviceDrive(ServerLevel l,FoundationData.Vehicle v,MovingAssemblyState s){
    // Only refresh the already-bound native governor lease. No impulse or new drive is created.
    if(PoweredInvertedYawVehicle.is(v)){
      var g=PoweredInvertedYawRuntime.governor(l,v);var rail=MovingRouteContext.rail(l,v.boundFrom,v.boundTo);
      if(g==null||rail==null||g.railDirection(rail)==0)throw new IllegalArgumentException("Restore the original Powered governor departure circuit");
      // Initial dispatch requires a powered span. Continuing onto this same circuit's
      // unpowered fixed curve may coast while the last carrier clears the bridge.
      g.command(v.chassis,v.authority.epoch,2*v.poweredYaw.direction,1);
    }else if(FlyingBikeVehicle.is(v)){
      departureDrive(l,v,s);var g=FlyingBikeRuntime.governor(l,v);int span=g.spanAt(serviceHit(l,v,s));
      g.command(v.chassis,v.authority.epoch,span==1?2:0,1);g.second().command(v.chassis,v.authority.epoch,span==2?2:0,1);
    }
  }
  public static void capture(ServerLevel l,FoundationData.Vehicle selected){
    var state=owner(l,selected);if(state==null)throw new IllegalArgumentException("No released service handoff");
    for(var id:state.cars){var v=FoundationData.get(l).vehicles.get(id);if(v==null)continue;
      var member=state.service.cars.get(id);if(member!=null)member.dispatched=false;
      v.stopping=true;if(member==null)v.carriers.forEach(c->c.capture=true);
      if(v.authority.owner!=null)dev.fourthdimension.integration.RideAuthority.emergencyHold(l,v);
      if(member==null){v.revision++;continue;}
      holdStopped(l,v);
      var car=member;
      if(FAULT_LOCAL.getOrDefault(l,Map.of()).containsKey(id)){
        try{body(l,id);for(var frame:car.holds){body(l,frame.parent());body(l,frame.child());}
          clearPartial(l,id);remove(live(l).remove(id));live(l).put(id,install(l,car));
        }catch(IllegalArgumentException missing){retainSurvivors(l,state,car,.05);v.revision++;continue;}
      }
      var handles=live(l).get(id);
      if(stopped(l,v)&&MovingNativeAdapter.liveDockEdge(l,state)!=null&&handles!=null&&handles.stream().allMatch(h->h.isValid())){
        MovingRouteAdapters.validateExit(l,state,v,state.docks.get(state.service.dock));
        if(v.fault.equals(car.fault))v.fault="";car.fault="";
      }
      v.revision++;}
    FoundationData.get(l).setDirty();
  }
  public static boolean stopped(ServerLevel l,FoundationData.Vehicle v){
    var state=owner(l,v);var car=state==null?null:state.service.cars.get(v.chassis);
    if(car!=null){var bodies=survivors(l,car);return !bodies.isEmpty()&&bodies.stream().allMatch(id->stopped(l,id));}
    return stopped(l,v.chassis);
  }
  private static boolean stopped(ServerLevel l,UUID id){
    var body=body(l,id);var physics=SubLevelContainer.getContainer(l).physicsSystem();physics.updatePose(body);
    return physics.getPipeline().getLinearVelocity(body,new Vector3d()).length()<=MovingNativeAdapter.LOCK_SPEED
        &&physics.getPipeline().getAngularVelocity(body,new Vector3d()).length()<=Math.toRadians(.1);
  }
  private static void holdStopped(ServerLevel l,FoundationData.Vehicle v){
    try{holdStopped(l,v.chassis);}catch(IllegalArgumentException unavailable){/* Missing body stays recorded and cannot be dispatched/evacuated. */}
  }
  private static void holdStopped(ServerLevel l,UUID id){
    if(!stopped(l,id))return;var map=FAULT_HOLDS.computeIfAbsent(l,k->new HashMap<>());var existing=map.get(id);
    if(existing!=null&&existing.isValid())return;
    var b=body(l,id);var anchor=new Vector3d(b.getMassTracker().getCenterOfMass());
    var world=b.logicalPose().transformPosition(new Vector3d(anchor));
    map.put(id,SubLevelContainer.getContainer(l).physicsSystem().getPipeline().addConstraint(null,b,
        new GenericConstraintConfiguration(world,anchor,new Quaterniond(b.logicalPose().orientation()),new Quaterniond(),EnumSet.allOf(ConstraintJointAxis.class))));
  }
  /** Ordinary empty carts have no FD Vehicle, so their explicit departure belongs to the moving controller. */
  public static void dispatchNative(ServerLevel l,MovingAssemblyState s){
    if(!s.configuration.equals(MovingCapabilityCatalog.Configuration.NATIVE_SERVICE.id())||!s.released
        ||s.service.cars.isEmpty()||MovingNativeAdapter.liveDockEdge(l,s)==null)
      throw new IllegalArgumentException("Release an empty native train onto its physically locked bridge first");
    var entry=MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.NATIVE_SERVICE);
    MovingAdmission.inspectDock(l,entry,s.docks.get(s.service.dock),"Native service departure");
    for(var id:s.cars){MovingAdmission.nativeServiceBounds(l,s,id);
      if(!stopped(l,id))throw new IllegalArgumentException("Stop every native service cart before explicit dispatch");
      requireNativeDrive(l,s,id);
    }
    for(var car:s.service.cars.values()){clearPartial(l,car.chassis);removeCarFaults(l,car);car.dispatched=true;car.fault="";}
    FoundationData.get(l).setDirty();
  }
  private static void requireNativeDrive(ServerLevel l,MovingAssemblyState s,UUID id){
    var edge=MovingNativeAdapter.liveDockEdge(l,s);if(edge==null)throw new IllegalArgumentException("Restore the locked native departure bridge");
    var center=CoasterCartResolve.cartCenterWorld(body(l,id));var point=new Vec3(center.x,center.y,center.z);
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,point);
    for(var fixed:MovingAdmission.dockEdges(l,s.docks.get(s.service.dock))){
      var candidate=CoasterPathTrackFrame.closestHitOnEdge(l,fixed,point);
      if(candidate!=null&&(hit==null||candidate.point().distanceToSqr(point)<hit.point().distanceToSqr(point)))hit=candidate;
    }
    var span=ChainLiftSpanQuery.findAtGraphHit(l,hit,0);
    if(span==null)throw new IllegalArgumentException("Provide a real fixed-powered native Chain departure span");
    var path=span.anchor().resolveChainLiftSpanPath(0);
    if(path==null||Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),path,0,256,16))<.01)
      throw new IllegalArgumentException("Supply real fixed kinetic power before native service dispatch");
  }
  public static void tickNative(ServerLevel l,MovingAssemblyState s,double dt){
    if(!s.configuration.equals(MovingCapabilityCatalog.Configuration.NATIVE_SERVICE.id())||s.service.cars.isEmpty())return;
    for(var car:List.copyOf(s.service.cars.values())){
      try{
        var bounds=MovingAdmission.nativeServiceBounds(l,s,car.chassis);
        if(MovingNativeAdapter.liveDockEdge(l,s)==null){car.dispatched=false;throw new IllegalArgumentException("Restore and resume the locked native bridge");}
        if(car.dispatched&&(bounds.minZ()>s.deckLength/2.0+.25||bounds.maxZ()<-s.deckLength/2.0-.25)){
          validateNativeExit(l,s,car.chassis);
          removeFaultHold(l,car.chassis);live(l).remove(car.chassis);s.service.cars.remove(car.chassis);continue;
        }
        if(!car.dispatched){brakeNative(l,car.chassis,dt);holdStopped(l,car.chassis);}
      }catch(IllegalArgumentException failure){car.dispatched=false;car.fault=failure.getMessage();
        try{brakeNative(l,car.chassis,dt);holdStopped(l,car.chassis);}catch(IllegalArgumentException missing){/* Recorded missing chassis cannot clear service. */}
      }
    }
  }
  private static void validateNativeExit(ServerLevel l,MovingAssemblyState s,UUID id){
    var center=CoasterCartResolve.cartCenterWorld(body(l,id));var point=new Vec3(center.x,center.y,center.z);
    for(var edge:MovingAdmission.dockEdges(l,s.docks.get(s.service.dock))){
      var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,point);
      if(hit!=null&&hit.point().distanceTo(point)<.5){
        MovingAdmission.inspectInsert(l,MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.NATIVE_SERVICE),edge,"Native fixed exit");return;
      }
    }throw new IllegalArgumentException("Restore the actual fixed departure support under the native service cart");
  }
  private static void brakeNative(ServerLevel l,UUID id,double dt){
    var b=body(l,id);var p=SubLevelContainer.getContainer(l).physicsSystem().getPipeline();
    var velocity=p.getLinearVelocity(b,new Vector3d());double speed=velocity.length();
    if(speed>0)p.addLinearAndAngularVelocity(b,velocity.mul(-Math.min(1,6*dt/speed)),new Vector3d());
  }
  private static void removeCarFaults(ServerLevel l,MovingServiceState.Car car){
    removeFaultHold(l,car.chassis);for(var h:car.holds){removeFaultHold(l,h.parent());removeFaultHold(l,h.child());}
  }
  private static void removeFaultHold(ServerLevel l,UUID id){
    var h=FAULT_HOLDS.computeIfAbsent(l,k->new HashMap<>()).remove(id);if(h!=null&&h.isValid())h.remove();
  }
  public static boolean faultHoldReady(ServerLevel l,FoundationData.Vehicle v){
    var s=owner(l,v);if(s==null)return false;var car=s.service.cars.get(v.chassis);if(car==null)return false;
    var surviving=survivors(l,car);if(surviving.isEmpty())return false;
    var partial=FAULT_LOCAL.getOrDefault(l,Map.of()).get(v.chassis);
    if(partial!=null){
      for(var id:surviving){var h=FAULT_HOLDS.getOrDefault(l,Map.of()).get(id);var local=partial.get(id);
        if((h==null||!h.isValid())&&(local==null||!local.isValid()))return false;}
      return true;
    }
    var h=FAULT_HOLDS.getOrDefault(l,Map.of()).get(v.chassis);var local=live(l).get(v.chassis);
    return h!=null&&h.isValid()&&local!=null&&local.size()==car.holds.size()&&local.stream().allMatch(GenericConstraintHandle::isValid);
  }
  private static Set<UUID> survivors(ServerLevel l,MovingServiceState.Car car){
    var ids=new LinkedHashSet<UUID>();ids.add(car.chassis);for(var h:car.holds){ids.add(h.parent());ids.add(h.child());}
    ids.removeIf(id->{var b=SubLevelContainer.getContainer(l).getSubLevel(id);return !(b instanceof ServerSubLevel)||b.isRemoved();});return ids;
  }
  /** Also runs when a missing chassis prevents the usual per-vehicle physics callback. */
  public static void tickFaults(ServerLevel l,MovingAssemblyState s,double dt){
    for(var car:s.service.cars.values()){
      var expected=new HashSet<UUID>();expected.add(car.chassis);for(var h:car.holds){expected.add(h.parent());expected.add(h.child());}
      boolean unavailable=!survivors(l,car).containsAll(expected)||s.phase==MovingAssemblyState.Phase.FAULT_HOLD;
      if(unavailable||FAULT_LOCAL.getOrDefault(l,Map.of()).containsKey(car.chassis)){
        car.dispatched=false;var v=FoundationData.get(l).vehicles.get(car.chassis);if(v!=null){v.stopping=true;
          // A partial hold also follows a route or posture failure. Preserve that original repair reason.
          if(unavailable||v.fault.isEmpty())v.fault="Service body/support unavailable: restore and acknowledge Stop";car.fault=v.fault;}
        retainSurvivors(l,s,car,dt);
      }
    }
  }
  private static void retainSurvivors(ServerLevel l,MovingAssemblyState s,MovingServiceState.Car car,double dt){
    var all=FAULT_LOCAL.computeIfAbsent(l,k->new HashMap<>());var joints=all.get(car.chassis);
    if(joints==null){remove(live(l).remove(car.chassis));joints=new HashMap<>();all.put(car.chassis,joints);}
    var existing=survivors(l,car);
    for(var id:existing)try{brakeNative(l,id,dt);}catch(IllegalArgumentException unavailable){/* Preserve unavailable identity. */}
    for(var frame:car.holds){
      if(!existing.contains(frame.parent())||!existing.contains(frame.child()))continue;
      var joint=joints.get(frame.child());if(joint!=null&&joint.isValid())continue;
      try{var restored=install(l,new MovingServiceState.Car(car.chassis,List.of(frame)));joints.put(frame.child(),restored.getFirst());}
      catch(IllegalArgumentException displaced){/* Do not move a surviving child to the old pose. Hold it where it stopped. */}
    }
    for(var id:existing){var joint=joints.get(id);if(joint==null||!joint.isValid())try{holdStopped(l,id);}catch(IllegalArgumentException unavailable){/* Missing stays faulted. */}}
  }
  private static void clearPartial(ServerLevel l,UUID chassis){
    var joints=FAULT_LOCAL.computeIfAbsent(l,k->new HashMap<>()).remove(chassis);if(joints!=null)remove(new ArrayList<>(joints.values()));
  }
  private static void adoptFixed(FoundationData.Vehicle v){
    if(NewGenerationFlyingVehicle.is(v)){v.yaw.mode=YawState.Mode.BLEND;v.yaw.section=null;v.yaw.travelAuthorized=true;}
    if(PoweredInvertedYawVehicle.is(v)){v.poweredYaw.mode=YawState.Mode.BLEND;v.poweredYaw.section=null;v.poweredYaw.phase=PoweredInvertedYawState.Phase.RUN;}
    // PAX returns to its original winch's CAPTURING phase with the retained seat posture.
    // That handoff must not revoke the capture which the normal station readiness requires.
    if(v.family!=dev.fourthdimension.contract.Family.PAX)v.carriers.forEach(c->{c.capture=false;c.latched=false;});
  }
}
