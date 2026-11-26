package dev.fourthdimension.moving;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.mixin.MovingNativeSnapAccess;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Pinned Coasters/Sable adapter: physical rail bodies, solver retention and atomic dock edges. */
public final class MovingNativeAdapter {
  public static final double POSITION_TOLERANCE = .025;
  public static final double ANGLE_TOLERANCE = Math.toRadians(.25);
  public static final double LOCK_SPEED = .025;
  public record BlockPlacement(BlockPos local, BlockState state) {
    public BlockPlacement { local=local.immutable();Objects.requireNonNull(state); }
  }
  private static final EnumSet<ConstraintJointAxis> ALL_AXES = EnumSet.allOf(ConstraintJointAxis.class);
  private static final Map<ServerLevel, Map<UUID, Binding>> BINDINGS = new WeakHashMap<>();

  private static final class Binding {
    final MovingAssemblyState state;
    final CoasterPathEdge hosted;
    final Map<UUID, GenericConstraintHandle> retained = new LinkedHashMap<>();
    final Map<UUID, GenericConstraintHandle> rollingContacts = new LinkedHashMap<>();
    GenericConstraintHandle actuator;
    final List<GenericConstraintHandle> dockLocks = new ArrayList<>();
    CoasterPathEdge docked;
    boolean partialFaultHold;
    MovingAssemblyState.Pose commanded, holdTarget;
    Binding(MovingAssemblyState state, CoasterPathEdge hosted) { this.state=state;this.hosted=hosted; }
  }
  private MovingNativeAdapter() {}
  private static Map<UUID,Binding> bindings(ServerLevel level) {
    return BINDINGS.computeIfAbsent(level, k -> new LinkedHashMap<>());
  }
  private static ServerSubLevel body(ServerLevel level, UUID id) {
    var body = id == null ? null : SubLevelContainer.getContainer(level).getSubLevel(id);
    if (!(body instanceof ServerSubLevel server) || server.isRemoved())
      throw new IllegalArgumentException("Moving-support physical body is unavailable: " + id);
    return server;
  }
  private static Vector3d localOrigin(ServerSubLevel body) {
    var c=body.getPlot().getCenterBlock();return new Vector3d(c.getX()+.5,c.getY()+.5,c.getZ()+.5);
  }
  private static MovingAssemblyState.Pose pose(Vector3d p, Quaterniond q) {
    q.normalize();return new MovingAssemblyState.Pose(p.x,p.y,p.z,q.x,q.y,q.z,q.w);
  }
  /** Pose origin is the center of the bed's local (0,0,0) block, independent of native COM. */
  public static void createBed(ServerLevel level, MovingAssemblyState state,
      MovingAssemblyState.Pose origin, BlockState support) {
    var parts=new ArrayList<BlockPlacement>();
    for(int z=-state.deckLength/2;z<=state.deckLength/2;z++)for(int x=-1;x<=1;x++)
      parts.add(new BlockPlacement(new BlockPos(x,-1,z),support));
    createBed(level,state,origin,parts);
  }
  public static void createBed(ServerLevel level, MovingAssemblyState state,
      MovingAssemblyState.Pose origin, List<BlockPlacement> parts) {
    if(state.bed!=null)throw new IllegalArgumentException("Moving bed already exists");
    if(parts.isEmpty()||parts.size()>4096)throw new IllegalArgumentException("Invalid bounded moving structure");
    for(var part:parts)if(Math.abs(part.local().getX())>16||Math.abs(part.local().getY())>16
        ||Math.abs(part.local().getZ())>state.deckLength/2+2)throw new IllegalArgumentException("Moving structure outside declared bounds");
    validateSocketClaims(level,state);
    var container=SubLevelContainer.getContainer(level);
    var initial=new Pose3d();initial.position().set(origin.position());initial.orientation().set(origin.orientation());
    var bed=(ServerSubLevel)container.allocateNewSubLevel(initial);
    try {
      var plot=bed.getPlot();var center=plot.getCenterBlock();int half=state.deckLength/2;
      var chunks=new HashSet<ChunkPos>();
      for(var part:parts)chunks.add(new ChunkPos(center.offset(part.local())));
      chunks.add(new ChunkPos(center.offset(0,0,-half)));chunks.add(new ChunkPos(center.offset(0,0,half)));
      for(var chunk:chunks)plot.newEmptyChunk(chunk);
      var blocks=plot.getEmbeddedLevelAccessor();
      for(var part:parts)blocks.setBlock(part.local(),part.state(),3);
      for(int z:new int[]{-half,half}) blocks.setBlock(new BlockPos(0,0,z),
          SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState().setValue(CoasterAnchorpointBlock.FACING,Direction.UP),3);
      bed.updateMergedMassData(1);
      var com=bed.getMassTracker().getCenterOfMass();
      var physicalPosition=new Vector3d(com).sub(localOrigin(bed)).rotate(origin.orientation()).add(origin.position());
      bed.logicalPose().rotationPoint().set(com);
      bed.logicalPose().position().set(physicalPosition);bed.logicalPose().orientation().set(origin.orientation());
      bed.updateLastPose();container.physicsSystem().getPipeline().onStatsChanged(bed);
      container.physicsSystem().getPipeline().teleport(bed,bed.logicalPose().position(),bed.logicalPose().orientation());
      state.bed=bed.getUniqueId();state.railFrom=center.offset(0,0,-half);state.railTo=center.offset(0,0,half);
      var curve=new BezierConnection(Couple.create(state.railFrom,state.railTo),
          Couple.create(CoasterAnchorpointBlockEntity.worldCenterBase(level,state.railFrom),
              CoasterAnchorpointBlockEntity.worldCenterBase(level,state.railTo)),
          Couple.create(new Vec3(0,0,1),new Vec3(0,0,-1)),
          Couple.create(new Vec3(0,1,0),new Vec3(0,1,0)),true,false,CoasterTrackMaterials.COASTER);
      ((CoasterAnchorpointBlockEntity)level.getBlockEntity(state.railFrom)).putAnchorPeerCurve(level,state.railTo,curve);
      var binding=new Binding(state,CoasterPathEdge.curve(state.railFrom,state.railTo,curve));
      bindings(level).put(state.id,binding);bindActuator(level,binding,origin);readback(level,state);publish(level);
    } catch(RuntimeException failure) {
      var failed=bindings(level).remove(state.id);if(failed!=null)removeHandles(failed);
      container.removeSubLevel(bed,SubLevelRemovalReason.REMOVED);
      state.bed=null;state.railFrom=null;state.railTo=null;throw failure;
    }
  }
  private static Binding binding(ServerLevel level, MovingAssemblyState state) {
    var b=bindings(level).get(state.id);
    if(b==null||b.state!=state) { restore(level,state); b=bindings(level).get(state.id); }
    return b;
  }
  /** Rebuild only solver handles; saved identities and local poses remain authoritative. */
  public static void restore(ServerLevel level, MovingAssemblyState state) { restore(level,state,null); }
  /** Atomic fixed-support restoration: validate actual pose and publish the locked edge only once. */
  public static void restoreDocked(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock){
    if(state.retained())throw new IllegalArgumentException("Retained interrupted motion requires explicit recovery");
    if(!MovingNativeRestoreSupport.prepare(level,state))throw new IllegalArgumentException("Wait for every restored dock body before rebuilding its support");
    restore(level,state,Objects.requireNonNull(dock));
  }
  private static void restore(ServerLevel level, MovingAssemblyState state,MovingAssemblyState.Dock dock) {
    if(state.bed==null||state.railFrom==null)throw new IllegalArgumentException("Moving bed identity is missing");
    var bed=body(level,state.bed);
    if(!(level.getBlockEntity(state.railFrom) instanceof CoasterAnchorpointBlockEntity anchor))
      throw new IllegalArgumentException("Moving rail anchor unavailable");
    var curve=anchor.getAnchorPeerCurvesView().get(state.railTo);
    if(curve==null)throw new IllegalArgumentException("Moving rail connection unavailable");
    if(!state.bodyAnchors.keySet().equals(state.bodies.keySet())){
      restoreFaultHold(level,state);
      throw new IllegalArgumentException("Legacy retained support has no saved body anchors; service evacuation required");
    }
    // Preflight every saved body before replacing a valid set of solver handles.
    for(var id:state.bodies.keySet())try{body(level,id);}catch(IllegalArgumentException missing){
      restoreFaultHold(level,state);throw missing;
    }
    MovingVehicleHolds.validate(level,state);
    var old=bindings(level).get(state.id);boolean wasRestored=state.restored,wasLocked=state.locksEngaged;
    var b=new Binding(state,CoasterPathEdge.curve(state.railFrom,state.railTo,curve));bindings(level).put(state.id,b);
    try {
      // A restart holds the physically restored pose; it never jumps to a program target or dock.
      readback(level,state);bindActuator(level,b,state.actualPose);
      for(var entry:state.bodies.entrySet()) bindRetained(level,b,body(level,entry.getKey()),entry.getValue());
      MovingVehicleHolds.releaseAfterCapture(level,state);
      if(dock==null){if(state.restored)state.locksEngaged=false;publish(level);}
      else {state.restored=false;dock(level,state,dock);}
      if(old!=null)removeHandles(old);
    } catch(RuntimeException failure) {
      state.restored=wasRestored;state.locksEngaged=wasLocked;
      removeHandles(b);if(old!=null)bindings(level).put(state.id,old);else bindings(level).remove(state.id);throw failure;
    }
  }
  /** Rebuild surviving locks for service access; missing identities remain saved and block movement/release. */
  public static void restoreFaultHold(ServerLevel level,MovingAssemblyState state){
    body(level,state.bed);
    if(!(level.getBlockEntity(state.railFrom) instanceof CoasterAnchorpointBlockEntity anchor))
      throw new IllegalArgumentException("Moving rail anchor unavailable during fault hold");
    var curve=anchor.getAnchorPeerCurvesView().get(state.railTo);
    if(curve==null)throw new IllegalArgumentException("Moving rail connection unavailable during fault hold");
    var old=bindings(level).get(state.id);
    var b=new Binding(state,CoasterPathEdge.curve(state.railFrom,state.railTo,curve));b.partialFaultHold=true;
    bindings(level).put(state.id,b);
    try{
      readback(level,state);bindActuator(level,b,state.actualPose);b.holdTarget=state.actualPose;
      var container=SubLevelContainer.getContainer(level);
      for(var entry:state.bodies.entrySet())if(container.getSubLevel(entry.getKey()) instanceof ServerSubLevel child&&!child.isRemoved()){
        if(state.bodyAnchors.containsKey(entry.getKey()))bindRetained(level,b,child,entry.getValue());
        else bindIndependentFaultHold(level,b,child);
      }
      MovingVehicleHolds.releaseAfterCapture(level,state);
      state.locksEngaged=false;publish(level);if(old!=null)removeHandles(old);
    }catch(RuntimeException failure){removeHandles(b);if(old!=null)bindings(level).put(state.id,old);else bindings(level).remove(state.id);throw failure;}
  }
  private static void bindActuator(ServerLevel level, Binding b, MovingAssemblyState.Pose target) {
    var bed=body(level,b.state.bed);var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    b.actuator=pipeline.addConstraint(null,bed,new GenericConstraintConfiguration(
        target.position(),localOrigin(bed),target.orientation(),new Quaterniond()));
    if(b.actuator==null)throw new IllegalStateException("Native moving-bed constraint unavailable");
    tuneActuator(level,b);b.commanded=target;
  }
  private static void tuneActuator(ServerLevel level,Binding b) {
    double mass=body(level,b.state.bed).getMassTracker().getMass();
    for(var id:b.state.bodies.keySet()){
      var child=SubLevelContainer.getContainer(level).getSubLevel(id);
      if(child instanceof ServerSubLevel present&&!present.isRemoved())mass+=present.getMassTracker().getMass();
      else if(!b.partialFaultHold)throw new IllegalArgumentException("Retained body unavailable: "+id);
    }
    mass=Math.max(1,mass);
    // Finite actuator force includes gravity support; limits are measured in the prototype.
    for(var axis:ConstraintJointAxis.LINEAR)b.actuator.setMotor(axis,0,mass*4000,mass*160,true,mass*24);
    for(var axis:ConstraintJointAxis.ANGULAR)b.actuator.setMotor(axis,0,mass*40000,mass*1600,true,mass*240);
  }
  public static void capture(ServerLevel level, MovingAssemblyState state, List<UUID> bodyIds) {
    var b=binding(level,state);var bed=body(level,state.bed);
    if(b.partialFaultHold)throw new IllegalArgumentException("Repair every retained body before capture");
    if(!state.bodies.isEmpty())throw new IllegalArgumentException("Assembly already retains bodies");
    MovingVehicleHolds.validate(level,state);
    var unique=new LinkedHashSet<>(bodyIds);
    if(unique.size()!=bodyIds.size()||unique.isEmpty()||unique.contains(state.bed))
      throw new IllegalArgumentException("Invalid complete retained body list");
    var poses=new LinkedHashMap<UUID,MovingAssemblyState.Pose>();
    var anchors=new LinkedHashMap<UUID,Vector3d>();
    var physics=SubLevelContainer.getContainer(level).physicsSystem();physics.updatePose(bed);
    if(physics.getPipeline().getLinearVelocity(bed,new Vector3d()).length()>LOCK_SPEED
        ||physics.getPipeline().getAngularVelocity(bed,new Vector3d()).length()>Math.toRadians(.1))
      throw new IllegalArgumentException("Stop the bed before capturing bodies");
    for(var id:unique){var child=body(level,id);physics.updatePose(child);
      if(physics.getPipeline().getLinearVelocity(child,new Vector3d()).length()>LOCK_SPEED
          ||physics.getPipeline().getAngularVelocity(child,new Vector3d()).length()>Math.toRadians(.1))
        throw new IllegalArgumentException("Every chassis and carrier must be stopped before moving-support capture");
      var relative=new Vector3d(child.logicalPose().position()).sub(bed.logicalPose().position())
          .rotate(new Quaterniond(bed.logicalPose().orientation()).conjugate())
          .add(bed.logicalPose().rotationPoint()).sub(localOrigin(bed));
      var orientation=new Quaterniond(bed.logicalPose().orientation()).conjugate().mul(child.logicalPose().orientation());
      poses.put(id,pose(relative,orientation));
      anchors.put(id,new Vector3d(child.logicalPose().rotationPoint()));
    }
    try {
      state.bodies.putAll(poses);state.bodyAnchors.putAll(anchors);
      for(var entry:poses.entrySet())bindRetained(level,b,body(level,entry.getKey()),entry.getValue());
      tuneActuator(level,b);
      MovingVehicleHolds.releaseAfterCapture(level,state);
    }catch(RuntimeException failure){b.retained.values().forEach(PhysicsConstraintHandle::remove);b.retained.clear();state.bodies.clear();state.bodyAnchors.clear();throw failure;}
  }
  private static void bindRetained(ServerLevel level,Binding b,ServerSubLevel child,MovingAssemblyState.Pose local) {
    var anchor=b.state.bodyAnchors.get(child.getUniqueId());
    if(anchor==null)throw new IllegalArgumentException("Retained body has no saved physical anchor");
    MovingNativeSnapAccess.fourthdimension$tearDownRailCoupling(child);
    var bed=body(level,b.state.bed);var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    var handle=pipeline.<GenericConstraintHandle>addConstraint(bed,child,new GenericConstraintConfiguration(
        local.position().add(localOrigin(bed)),new Vector3d(anchor),
        local.orientation(),new Quaterniond(),ALL_AXES));
    if(handle==null)throw new IllegalStateException("Native body-retention constraint unavailable");
    handle.setContactsEnabled(false);b.retained.put(child.getUniqueId(),handle);
  }
  /** Legacy records cannot reconstruct the original bed-relative frame: preserve each actual survivor independently. */
  private static void bindIndependentFaultHold(ServerLevel level,Binding b,ServerSubLevel child){
    MovingNativeSnapAccess.fourthdimension$tearDownRailCoupling(child);
    var physics=SubLevelContainer.getContainer(level).physicsSystem();physics.updatePose(child);
    var anchor=localOrigin(child);
    var handle=physics.getPipeline().<GenericConstraintHandle>addConstraint(null,child,new GenericConstraintConfiguration(
        child.logicalPose().transformPosition(new Vector3d(anchor)),anchor,new Quaterniond(child.logicalPose().orientation()),
        new Quaterniond(),ALL_AXES));
    if(handle==null||!handle.isValid())throw new IllegalStateException("Native survivor fault hold unavailable");
    b.retained.put(child.getUniqueId(),handle);
  }
  /** Called on the physics thread. Targets come from the bounded program; never teleport live bodies. */
  public static void drive(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Pose target,double dt) {
    if(!Double.isFinite(dt)||dt<=0||dt>.1)throw new IllegalArgumentException("Invalid moving physics timestep");
    var b=binding(level,state);
    if(b.partialFaultHold)throw new IllegalArgumentException("Repair every retained body before moving");
    if(b.actuator==null||!b.actuator.isValid())throw new IllegalArgumentException("Moving actuator unavailable");
    if(b.docked!=null && (target.position().distance(b.commanded.position())>1e-8
        ||angularError(target.orientation(),b.commanded.orientation())>1e-8))
      throw new IllegalArgumentException("Disconnect the dock graph before moving");
    b.holdTarget=null;commandTarget(level,b,target);
    readback(level,state);
  }
  /** A fixed target at the interruption pose brakes through finite native motors, never teleport. */
  public static void hold(ServerLevel level,MovingAssemblyState state,double dt) {
    if(!Double.isFinite(dt)||dt<=0||dt>.1)throw new IllegalArgumentException("Invalid moving physics timestep");
    readback(level,state);var b=binding(level,state);
    if(b.holdTarget==null)b.holdTarget=state.actualPose;
    commandTarget(level,b,b.holdTarget);
  }
  /** Unchanged targets must permit native sleeping; repeatedly waking a settled solver prevents locking. */
  private static void commandTarget(ServerLevel level,Binding b,MovingAssemblyState.Pose target){
    if(target.equals(b.commanded))return;
    b.actuator.setFrame1(target.position(),target.orientation());b.commanded=target;
    SubLevelContainer.getContainer(level).physicsSystem().getPipeline().wakeUp(body(level,b.state.bed));
  }
  public static void hold(ServerLevel level,MovingAssemblyState state){hold(level,state,.05);}
  public static boolean stopped(ServerLevel level,MovingAssemblyState state){
    readback(level,state);return state.linearVelocity.length()<=LOCK_SPEED
        &&state.angularVelocity.length()<=Math.toRadians(.1);
  }
  /** Read-only solver ownership proof for an individually surviving service-evacuation body. */
  public static boolean faultHoldReady(ServerLevel level,MovingAssemblyState state,UUID bodyId){
    var b=BINDINGS.getOrDefault(level,Map.of()).get(state.id);
    if(b==null||b.state!=state||b.actuator==null||!b.actuator.isValid()||b.holdTarget==null
        ||!state.bodies.containsKey(bodyId))return false;
    var retained=b.retained.get(bodyId);
    if(retained==null||!retained.isValid())return false;
    var container=SubLevelContainer.getContainer(level);
    return container.getSubLevel(state.bed) instanceof ServerSubLevel bed&&!bed.isRemoved()
        &&container.getSubLevel(bodyId) instanceof ServerSubLevel child&&!child.isRemoved();
  }
  public static Vector3d actuatorLinearImpulse(ServerLevel level,MovingAssemblyState state){
    var linear=new Vector3d();binding(level,state).actuator.getJointImpulses(linear,new Vector3d());return linear;
  }
  public static void readback(ServerLevel level,MovingAssemblyState state) {
    var bed=body(level,state.bed);var physics=SubLevelContainer.getContainer(level).physicsSystem();physics.updatePose(bed);
    var binding=BINDINGS.getOrDefault(level,Map.of()).get(state.id);
    if(binding!=null)binding.rollingContacts.entrySet().removeIf(e->{
      var child=SubLevelContainer.getContainer(level).getSubLevel(e.getKey());
      boolean gone=child==null||child.isRemoved()||!e.getValue().isValid();
      if(gone)e.getValue().remove();return gone;
    });
    state.actualPose=pose(bed.logicalPose().transformPosition(localOrigin(bed)),new Quaterniond(bed.logicalPose().orientation()));
    physics.getPipeline().getLinearVelocity(bed,state.linearVelocity);
    physics.getPipeline().getAngularVelocity(bed,state.angularVelocity);
  }
  public static double angularError(Quaterniond a,Quaterniond b) {
    return 2*Math.acos(Math.min(1,Math.abs(a.dot(b))));
  }
  public static CoasterPathEdge hostedEdge(ServerLevel level,MovingAssemblyState state){return binding(level,state).hosted;}
  /** Existing physically locked bridge only; this method never restores handles or looks up the graph. */
  public static CoasterPathEdge liveDockEdge(ServerLevel level,MovingAssemblyState state){
    if(dev.fourthdimension.runtime.FoundationData.get(level).moving.assemblies.get(state.id)!=state
        ||!state.locksEngaged||state.restored)return null;
    if(state.phase!=MovingAssemblyState.Phase.DOCKED&&state.phase!=MovingAssemblyState.Phase.RECONNECTED
        &&state.phase!=MovingAssemblyState.Phase.RELEASING&&state.phase!=MovingAssemblyState.Phase.CAPTURED)return null;
    var b=BINDINGS.getOrDefault(level,Map.of()).get(state.id);
    if(b==null||b.state!=state||b.partialFaultHold||b.docked==null||b.actuator==null||!b.actuator.isValid()||b.dockLocks.size()!=4||b.dockLocks.stream().anyMatch(lock->!lock.isValid()))return null;
    try{
      if(!stopped(level,state))return null;
      for(var dock:state.docks.values())if(pair(b.docked,dock.from(),dock.to())
          &&state.actualPose.position().distance(dock.pose().position())<=POSITION_TOLERANCE
          &&angularError(state.actualPose.orientation(),dock.pose().orientation())<=ANGLE_TOLERANCE){
        for(boolean last:new boolean[]{false,true}){
          var actual=new Vector3d(0,-.1875,last?state.deckLength/2.0:-state.deckLength/2.0)
              .rotate(state.actualPose.orientation()).add(state.actualPose.position());
          var socket=socketFrame(state,dock,last);var expected=socket.rawPosition().add(socket.normal().scale(.0625));
          if(actual.distance(new Vector3d(expected.x,expected.y,expected.z))>POSITION_TOLERANCE)return null;
          if(!(level.getBlockEntity(last?dock.to():dock.from()) instanceof CoasterAnchorpointBlockEntity))return null;
        }
        return b.docked;
      }
    }catch(RuntimeException unavailable){return null;}
    return null;
  }
  public static void disconnect(ServerLevel level,MovingAssemblyState state) {
    var b=binding(level,state);b.docked=null;state.locksEngaged=false;publish(level);
    b.rollingContacts.values().forEach(PhysicsConstraintHandle::remove);b.rollingContacts.clear();
    b.dockLocks.forEach(PhysicsConstraintHandle::remove);b.dockLocks.clear();
  }
  public static void nativeGuideRemoved(ServerSubLevel chassis) {
    for(var bindings:BINDINGS.values())for(var b:bindings.values()) {
      var handle=b.rollingContacts.remove(chassis.getUniqueId());if(handle!=null)handle.remove();
    }
  }
  /** Called only after the native rail guide has authenticated and updated this exact edge. */
  public static void nativeGuideUpdated(ServerSubLevel chassis,ServerLevel level,CoasterPathEdge edge) {
    var data=dev.fourthdimension.runtime.FoundationData.get(level);var physics=SubLevelContainer.getContainer(level).physicsSystem();
    var car=data.vehicles.get(chassis.getUniqueId());
    for(var b:BINDINGS.getOrDefault(level,Map.of()).values()) {
      boolean allowed=false;var live=liveDockEdge(level,b.state);
      if(live!=null&&!b.state.bodies.containsKey(chassis.getUniqueId())) {
        var entry=MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.byId(b.state.configuration));
        boolean configuration=car==null?entry.configuration()==MovingCapabilityCatalog.Configuration.NATIVE_SERVICE
            :entry.family()==car.family&&entry.variant().equals(car.variant);
        boolean bridge=pair(edge,live.from(),live.to());
        boolean approach=edge.from().equals(live.from())||edge.from().equals(live.to())
            ||edge.to().equals(live.from())||edge.to().equals(live.to());
        if(configuration&&(bridge||approach)) {
          var point=dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(chassis);
          if(point!=null){var local=new Vector3d(point).sub(b.state.actualPose.position()).rotate(b.state.actualPose.orientation().conjugate());
            allowed=Math.abs(local.x)<.5&&Math.abs(local.y)<1&&Math.abs(local.z)<b.state.deckLength/2.0+2;
          }
        }
      }
      var old=b.rollingContacts.get(chassis.getUniqueId());
      if(!allowed){if(old!=null){old.remove();b.rollingContacts.remove(chassis.getUniqueId());}continue;}
      if(old!=null&&old.isValid())continue;
      // Free joint: no drive, hold or pose ownership; only the wheel/rail support pair's
      // collision filter. The unchanged native guide remains the rolling constraint.
      var handle=physics.getPipeline().<GenericConstraintHandle>addConstraint(body(level,b.state.bed),chassis,
          new GenericConstraintConfiguration(localOrigin(body(level,b.state.bed)),localOrigin(chassis),new Quaterniond(),new Quaterniond()));
      if(handle==null||!handle.isValid())throw new IllegalStateException("Native rolling rail contact filter unavailable");
      handle.setContactsEnabled(false);b.rollingContacts.put(chassis.getUniqueId(),handle);
      if(Boolean.getBoolean("fourthdimension.validation"))System.out.println("FD2001_ROLL_CONTACT id="+chassis.getUniqueId()+" edge="+edge.from()+"/"+edge.to());
    }
  }
  /** Read-only prospective route geometry. This never publishes a bridge or grants docking. */
  public static CoasterPathEdge previewDockEdge(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock) {
    if(!state.docks.containsValue(dock))throw new IllegalArgumentException("Unknown moving socket");
    validateSocketClaims(level,state);
    if(!(level.getBlockEntity(dock.from()) instanceof CoasterAnchorpointBlockEntity)
        ||!(level.getBlockEntity(dock.to()) instanceof CoasterAnchorpointBlockEntity))
      throw new IllegalArgumentException("Both fixed native dock anchors must remain present");
    if(!(level.getBlockEntity(state.railFrom) instanceof CoasterAnchorpointBlockEntity anchor))
      throw new IllegalArgumentException("Moving rail anchor unavailable");
    var hosted=anchor.getAnchorPeerCurvesView().get(state.railTo);
    if(hosted==null)throw new IllegalArgumentException("Moving rail connection unavailable");
    var first=socketFrame(state,dock,false);var last=socketFrame(state,dock,true);
    var curve=new BezierConnection(Couple.create(dock.from(),dock.to()),
        Couple.create(first.rawPosition(),last.rawPosition()),
        Couple.create(first.forward(),last.forward().scale(-1)),
        Couple.create(first.normal(),last.normal()),true,false,hosted.getMaterial());
    return CoasterPathEdge.curve(dock.from(),dock.to(),curve);
  }
  public static void dock(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock) {
    var b=binding(level,state);readback(level,state);
    if(b.partialFaultHold)throw new IllegalArgumentException("Repair every retained body before docking");
    if(state.actualPose.position().distance(dock.pose().position())>POSITION_TOLERANCE
        ||angularError(state.actualPose.orientation(),dock.pose().orientation())>ANGLE_TOLERANCE
        ||state.linearVelocity.length()>LOCK_SPEED||state.angularVelocity.length()>Math.toRadians(.1))
      throw new IllegalArgumentException("Moving bed is not physically aligned and stationary");
    if(!(level.getBlockEntity(dock.from()) instanceof CoasterAnchorpointBlockEntity)
        ||!(level.getBlockEntity(dock.to()) instanceof CoasterAnchorpointBlockEntity))
      throw new IllegalArgumentException("Both fixed native dock anchors must remain present");
    var first=CoasterPathGraphManager.sampleEdgePosition(level,b.hosted,0);
    var last=CoasterPathGraphManager.sampleEdgePosition(level,b.hosted,1);
    var normal=new Vector3d(0,1,0).rotate(dock.pose().orientation());
    var offset=new Vec3(normal.x,normal.y,normal.z).scale(.0625);
    var expectedFirst=CoasterAnchorpointBlockEntity.worldCenterBase(level,dock.from()).add(offset);
    var expectedLast=CoasterAnchorpointBlockEntity.worldCenterBase(level,dock.to()).add(offset);
    if(first.distanceTo(expectedFirst)>POSITION_TOLERANCE||last.distanceTo(expectedLast)>POSITION_TOLERANCE)
      throw new IllegalArgumentException("Dock socket endpoints do not match the physical rail span");
    var tangent=new Vector3d(0,0,1).rotate(dock.pose().orientation());
    var forward=new Vec3(tangent.x,tangent.y,tangent.z);
    var up=new Vec3(normal.x,normal.y,normal.z);
    var curve=new BezierConnection(Couple.create(dock.from(),dock.to()),
        Couple.create(first.subtract(offset),last.subtract(offset)),Couple.create(forward,forward.scale(-1)),
        Couple.create(up,up),true,false,b.hosted.bezier().getMaterial());
    // Lock both sides of each physical socket. Centerline joints leave roll loads
    // on their angular springs; lateral latch separation also restrains that axis.
    var bed=body(level,state.bed);var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    var locks=new ArrayList<GenericConstraintHandle>();
    try {
      for(boolean end:new boolean[]{false,true}) for(int side:new int[]{-1,1}) {
        var local=new Vector3d(side,-.25,end?state.deckLength/2.0:-state.deckLength/2.0);
        var world=new Vector3d(local).rotate(dock.pose().orientation()).add(dock.pose().position());
        var lock=pipeline.<GenericConstraintHandle>addConstraint(null,bed,new GenericConstraintConfiguration(
            world,local.add(localOrigin(bed)),dock.pose().orientation(),new Quaterniond(),ALL_AXES));
        if(lock==null||!lock.isValid())throw new IllegalStateException("Native physical dock lock unavailable");
        locks.add(lock);
      }
    } catch(RuntimeException failure) {locks.forEach(PhysicsConstraintHandle::remove);throw failure;}
    var previous=new ArrayList<>(b.dockLocks);var previousEdge=b.docked;boolean previousLocked=state.locksEngaged;
    b.dockLocks.clear();b.dockLocks.addAll(locks);
    b.docked=CoasterPathEdge.curve(dock.from(),dock.to(),curve);state.locksEngaged=true;
    try{publish(level);}catch(RuntimeException failure){
      b.dockLocks.clear();b.dockLocks.addAll(previous);b.docked=previousEdge;state.locksEngaged=previousLocked;
      locks.forEach(PhysicsConstraintHandle::remove);throw failure;
    }
    previous.forEach(PhysicsConstraintHandle::remove);
  }
  public static void release(ServerLevel level,MovingAssemblyState state) {
    var b=binding(level,state);
    if(b.partialFaultHold)throw new IllegalArgumentException("Repair every retained body before release");
    if(b.docked==null||!state.locksEngaged)throw new IllegalArgumentException("Cannot release an unlocked moving bed");
    b.retained.values().forEach(PhysicsConstraintHandle::remove);b.retained.clear();state.bodies.clear();state.bodyAnchors.clear();tuneActuator(level,b);
  }
  public static boolean retained(ServerLevel level,UUID id) {
    for(var b:bindings(level).values())if(b.state.bodies.containsKey(id))return true;
    return false;
  }
  private static boolean pair(CoasterPathEdge edge,BlockPos from,BlockPos to) {
    return edge.from().equals(from)&&edge.to().equals(to)||edge.from().equals(to)&&edge.to().equals(from);
  }
  /** Invoked at native graph lookup so a deferred rebuild cannot resurrect an unlocked bridge. */
  public static void reconcileGraph(ServerLevel level,CoasterPathGraph graph) {
    var entries=BINDINGS.getOrDefault(level,Map.of());
    var saved=dev.fourthdimension.runtime.FoundationData.get(level).moving.assemblies;
    if(entries.isEmpty()&&saved.isEmpty())return;
    var desired=new ArrayList<>(graph.edges());
    // Native graphs may load before solver handles. A saved lock never republishes a bridge.
    for(var state:saved.values())if(!entries.containsKey(state.id)&&state.railFrom!=null){
      CoasterPathEdge restored=null;
      if(level.hasChunkAt(state.railFrom)
          &&level.getBlockEntity(state.railFrom) instanceof CoasterAnchorpointBlockEntity anchor){
        var curve=anchor.getAnchorPeerCurvesView().get(state.railTo);
        if(curve!=null)restored=alignedEdge(desired,CoasterPathEdge.curve(state.railFrom,state.railTo,curve));
      }
      desired.removeIf(e->pair(e,state.railFrom,state.railTo)
          ||state.docks.values().stream().anyMatch(d->pair(e,d.from(),d.to())));
      if(restored!=null)desired.add(restored);
    }
    for(var b:entries.values()) {
      var replacement=alignedEdge(desired,b.docked==null?b.hosted:b.docked);
      // Native alignment owns tangent direction. Preserve it for the same physical curve;
      // otherwise each graph read would undo native alignment and invalidate every cache.
      desired.removeIf(e->pair(e,b.state.railFrom,b.state.railTo)
          ||b.state.docks.values().stream().anyMatch(d->pair(e,d.from(),d.to())));
      desired.add(replacement);
    }
    if(desired.equals(graph.edges()))return;
    var rebuilt=new CoasterPathGraph();for(var n:graph.nodes())rebuilt.upsertNode(n);
    for(var b:entries.values()) {
      var edge=b.docked==null?b.hosted:b.docked;
      rebuilt.upsertNode(new CoasterPathNode(edge.from(),new Vec3(0,1,0)));
      rebuilt.upsertNode(new CoasterPathNode(edge.to(),new Vec3(0,1,0)));
    }
    desired.forEach(rebuilt::addEdge);graph.replaceFrom(rebuilt);
    CoasterPathSavedData.get(level.getServer()).setDirty();TrackGeometry.invalidate(level);
    CoasterPathSync.scheduleBroadcastDimension(level.getServer(),level.dimension());
  }
  private static CoasterPathEdge alignedEdge(List<CoasterPathEdge> edges,CoasterPathEdge replacement){
    for(var existing:edges)if(existing.from().equals(replacement.from())
        &&existing.to().equals(replacement.to())&&existing.bezier()==replacement.bezier())
      return replacement.withTangentSign(existing.tangentSign());
    return replacement;
  }
  private static void publish(ServerLevel level) {
    reconcileGraph(level,CoasterPathGraphManager.get(level));
    CoasterPathGraphManager.invalidateServerTangentAlignment(level.dimension());
    CoasterPathSync.broadcastDimension(level.getServer(),level.dimension());
  }
  /** Empty-only construction rollback/removal; no other body's saved state is deleted. */
  public static void destroy(ServerLevel level,MovingAssemblyState state) {
    if(!state.bodies.isEmpty())throw new IllegalArgumentException("Cannot remove an occupied moving bed");
    var b=bindings(level).remove(state.id);if(b!=null)removeHandles(b);
    if(state.railFrom!=null&&level.getBlockEntity(state.railFrom) instanceof CoasterAnchorpointBlockEntity anchor)
      anchor.removeAnchorPeerCurve(level,state.railTo);
    var graph=CoasterPathGraphManager.get(level);var copy=new CoasterPathGraph();
    graph.nodes().forEach(copy::upsertNode);
    for(var edge:graph.edges())if(!(state.railFrom!=null&&pair(edge,state.railFrom,state.railTo))
        &&!(b!=null&&b.docked!=null&&pair(edge,b.docked.from(),b.docked.to())))copy.addEdge(edge);
    graph.replaceFrom(copy);
    if(state.bed!=null){var sub=SubLevelContainer.getContainer(level).getSubLevel(state.bed);
      if(sub!=null)SubLevelContainer.getContainer(level).removeSubLevel(sub,SubLevelRemovalReason.REMOVED);}
    state.bed=null;state.railFrom=null;state.railTo=null;state.locksEngaged=false;publish(level);
  }
  public record SocketFrame(Vec3 rawPosition,Vec3 normal,Vec3 forward) {}
  public static SocketFrame socketFrame(MovingAssemblyState state,MovingAssemblyState.Dock dock,boolean last) {
    var q=dock.pose().orientation();
    var p=new Vector3d(0,-.25,last?state.deckLength/2.0:-state.deckLength/2.0).rotate(q).add(dock.pose().position());
    var up=new Vector3d(0,1,0).rotate(q);var forward=new Vector3d(0,0,1).rotate(q);
    return new SocketFrame(new Vec3(p.x,p.y,p.z),new Vec3(up.x,up.y,up.z),new Vec3(forward.x,forward.y,forward.z));
  }
  public static SocketFrame socketFrame(Level level,BlockPos anchor) {
    var data=EligibilityHooks.data(level);SocketFrame found=null;
    if(data!=null)for(var state:data.moving.assemblies.values())found=matchingSocket(state,anchor,found);
    if(level instanceof ServerLevel server){var entries=BINDINGS.get(server);
      if(entries!=null)for(var binding:entries.values())found=matchingSocket(binding.state,anchor,found);}
    return found;
  }
  private static SocketFrame matchingSocket(MovingAssemblyState state,BlockPos anchor,SocketFrame found){
    for(var dock:state.docks.values())if(anchor.equals(dock.from())||anchor.equals(dock.to())){
      var frame=socketFrame(state,dock,anchor.equals(dock.to()));
      if(found!=null&&!sameSocket(found,frame))
        throw new IllegalArgumentException("Conflicting moving socket geometry at "+anchor.toShortString());
      found=frame;
    }return found;
  }
  private static boolean sameSocket(SocketFrame a,SocketFrame b){
    // A 180-degree dock uses the same physical tangent axis in the opposite travel direction.
    return a.rawPosition().distanceToSqr(b.rawPosition())<=1e-12
        &&a.normal().distanceToSqr(b.normal())<=1e-12
        &&Math.min(a.forward().distanceToSqr(b.forward()),a.forward().add(b.forward()).lengthSqr())<=1e-12;
  }
  public static void validateSocketClaims(ServerLevel level,MovingAssemblyState state){
    var owned=new HashMap<BlockPos,SocketFrame>();
    for(var dock:state.docks.values())for(boolean last:new boolean[]{false,true}){
      var anchor=last?dock.to():dock.from();var frame=socketFrame(state,dock,last);
      if(anchor.getCenter().distanceTo(frame.rawPosition())>1.25)
        throw new IllegalArgumentException("Moving socket anchor must contain its exact physical socket");
      var previous=owned.putIfAbsent(anchor,frame);
      if(previous!=null&&!sameSocket(previous,frame))throw new IllegalArgumentException("Dock socket geometry overlaps");
      var current=socketFrame(level,anchor);
      if(current!=null&&!sameSocket(current,frame))throw new IllegalArgumentException("Another moving assembly owns a different socket pose");
    }
  }
  private static void removeHandles(Binding b) {
    b.retained.values().forEach(PhysicsConstraintHandle::remove);b.retained.clear();
    b.rollingContacts.values().forEach(PhysicsConstraintHandle::remove);b.rollingContacts.clear();
    if(b.actuator!=null)b.actuator.remove();
    b.dockLocks.forEach(PhysicsConstraintHandle::remove);b.dockLocks.clear();
  }
}
