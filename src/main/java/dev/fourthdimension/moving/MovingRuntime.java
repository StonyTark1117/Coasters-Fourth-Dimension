package dev.fourthdimension.moving;

import dev.fourthdimension.runtime.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import static dev.fourthdimension.moving.MovingAssemblyState.Phase.*;

/** Server authority for retained track. Native readback, never elapsed program time, opens a dock. */
public final class MovingRuntime {
  public enum Action { CONFIGURE, CAPTURE, MOVE, RESUME, RETURN, RELEASE, DISPATCH_SERVICE, EVACUATE, DISMANTLE }
  private record Run(MovingProgram.Recovery path,double elapsed,long checkedTick) {}
  private static final Map<ServerLevel,Map<UUID,Run>> RUNS=new WeakHashMap<>();
  private MovingRuntime() {}
  private static Map<UUID,Run> runs(ServerLevel level){return RUNS.computeIfAbsent(level,k->new HashMap<>());}
  public static boolean retained(Level level,UUID body){var d=EligibilityHooks.data(level);return d!=null&&d.moving.retaining(body)!=null;}
  public static MovingAssemblyState at(ServerLevel level,BlockPos controller) {
    return FoundationData.get(level).moving.assemblies.values().stream().filter(s->s.controller.equals(controller)).findFirst().orElse(null);
  }
  public static boolean protects(ServerLevel level,BlockPos pos) {
    for(var s:FoundationData.get(level).moving.assemblies.values())
      if(s.controller.equals(pos)||s.fixedParts.containsKey(pos)||pos.equals(s.railFrom)||pos.equals(s.railTo)
          ||s.docks.values().stream().anyMatch(d->d.from().equals(pos)||d.to().equals(pos)))return true;
    return false;
  }
  public static void register(ServerLevel level,MovingAssemblyState state) {
    thread(level);var d=FoundationData.get(level);
    if(at(level,state.controller)!=null||d.moving.assemblies.containsKey(state.id)||d.moving.assemblies.size()>=64)
      throw new IllegalArgumentException("Controller already owns an assembly or dimension assembly capacity reached");
    if(state.program==null||!state.program.mechanism().id().equals(state.mechanism))
      throw new IllegalArgumentException("Choose the mechanism's finite motion program");
    MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.byId(state.configuration));
    if(state.bed==null)throw new IllegalArgumentException("Create the actual native rail bed before registration");
    d.moving.assemblies.put(state.id,state);d.setDirty();
  }
  public static CompoundTag snapshot(ServerPlayer player,BlockPos controller) {
    var entity=controller(player,controller);var result=new CompoundTag();result.putString("mechanism",entity.mechanismId());
    result.putDouble("rpm",entity.inputRpm());result.putBoolean("powered",entity.powered());var s=at(player.serverLevel(),controller);if(s==null)return result;
    result.put("assembly",s.save(false));result.putLong("epoch",s.epoch);result.putLong("sequence",s.sequence);
    result.putString("configuration",s.configuration);result.putInt("length",s.deckLength);
    result.putString("phase",s.phase.name());result.putString("fault",s.fault);result.putBoolean("secured",s.retained());
    result.putBoolean("locks",s.locksEngaged);result.putString("destination",s.destination);
    var names=new ListTag();for(var name:s.docks.keySet())names.add(StringTag.valueOf(name));result.put("destinations",names);
    result.put("state",s.save(false));return result;
  }
  public static void operate(ServerPlayer player,BlockPos pos,Action action,UUID assembly,long epoch,long sequence,String destination) {
    var entity=controller(player,pos);var level=player.serverLevel();var s=at(level,pos);
    if(s==null)throw new IllegalArgumentException("Build and validate this moving assembly first");
    s.check(new MovingAssemblyState.Token(assembly,epoch,sequence));
    if(!entity.mechanismId().equals(s.mechanism))throw new IllegalArgumentException("Controller mechanism changed");
    switch(action) {
      case CONFIGURE -> throw new IllegalArgumentException("Use the construction preview and Build request");
      case CAPTURE -> capture(level,s,destination);
      case MOVE -> {
        if(s.phase!=CAPTURED&&s.phase!=RESERVED)throw new IllegalArgumentException("Capture the complete stopped train first");
        start(level,s,false);
      }
      case RESUME -> {
        if(s.phase!=FAULT_HOLD)throw new IllegalArgumentException("This assembly does not need recovery");
        if(s.released)resumeRelease(level,s);else start(level,s,s.returning);
      }
      case RETURN -> {
        if(s.phase!=FAULT_HOLD&&s.phase!=CAPTURED&&s.phase!=RECONNECTED&&s.phase!=DOCKED)
          throw new IllegalArgumentException("Stop and secure the assembly before a controlled return");
        start(level,s,true);
      }
      case RELEASE -> release(level,s);
      case DISPATCH_SERVICE -> MovingServiceRuntime.dispatchNative(level,s);
      case EVACUATE -> MovingEvacuation.open(player,s);
      case DISMANTLE -> MovingConstruction.dismantle(player,s);
    }
    s.sequence++;FoundationData.get(level).setDirty();FoundationSync.send(player);
  }
  private static void capture(ServerLevel level,MovingAssemblyState s,String destination) {
    if(s.phase!=DOCKED&&s.phase!=RECONNECTED||s.retained())throw new IllegalArgumentException("Use an empty locked bed for admission");
    if(s.released||!s.cars.isEmpty())throw new IllegalArgumentException("Wait until the previous train clears its reserved receiving span");
    if(s.programSeconds>.0001)throw new IllegalArgumentException("Return the empty bed to its start dock before the next admission");
    if(!s.locksEngaged)throw new IllegalArgumentException("Wait for measured docking and end locks");
    var end=worldPose(s,s.program.build().end());
    var selected=s.docks.get(destination);
    if(selected!=null&&(selected.pose().position().distance(end.position())>.001
        ||MovingNativeAdapter.angularError(selected.pose().orientation(),end.orientation())>.001))
      throw new IllegalArgumentException("Choose the configured program endpoint");
    if(!s.docks.containsKey(destination))throw new IllegalArgumentException("Select a configured named destination");
    var prior=s.destination;s.destination=destination;
    try {
      var c=MovingAdmission.inspect(level,s);s.cars=List.copyOf(c.cars());s.receivingEnvelope=c.train().occupiedBounds();
      var program=s.program.build();
      var keys=MovingReservations.validate(level,s,s.docks.get(destination),trainLength(c.train().occupiedBounds()));
      MovingConstruction.validateSweep(level,s,c.bounds(),program,c.bodies());
      var base=c.readiness();var ready=new MovingCapabilityCatalog.Readiness(c.held(),base.restraintsClosed(),base.stopped(),base.routeValid(),
          true,true,base.propulsionReleased(),base.managedCircuit(),base.launchSystemsOwner());
      var decision=MovingCapabilityCatalog.assess(new MovingCapabilityCatalog.Request(c.configuration(),program,s.deckLength,
          c.bed(),c.source(),c.destination(),c.train(),ready));
      if(!decision.allowed())throw new IllegalArgumentException(decision.message());
      FoundationData.get(level).moving.reserve(s,keys);
      try{MovingNativeAdapter.capture(level,s,c.bodies());}
      catch(RuntimeException failure){FoundationData.get(level).moving.releaseReservation(s.id);throw failure;}
      for(var id:s.cars){var car=FoundationData.get(level).vehicles.get(id);
        if(car!=null&&dev.fourthdimension.integration.RideAuthority.externalSelected(car))
          dev.fourthdimension.integration.RideAuthority.suspend(level,car,"Moving capture: release at a locked dock and explicitly arm the receiving controller");}
      MovingPaxRecovery.captured(level,s);
      s.receivingLength=trainLength(c.train().occupiedBounds());s.released=false;
      s.phase=CAPTURED;s.fault="";s.programSeconds=0;s.returning=false;
    }catch(RuntimeException failure){if(!s.retained()){s.cars=List.of();s.destination=prior;}throw failure;}
  }
  private static void start(ServerLevel level,MovingAssemblyState s,boolean returning) {
    if(s.released)throw new IllegalArgumentException("Wait until the released train clears before returning the bed");
    requirePower(level,s);validateParts(level,s);
    if(s.program==null)throw new IllegalArgumentException("Program unavailable");
    if(s.retained())MovingAdmission.validateRetained(level,s);
    else if(!returning)throw new IllegalArgumentException("Secure a complete train before moving");
    else MovingAdmission.requirePhysicallyEmpty(level,s);
    if(s.phase==FAULT_HOLD)MovingNativeAdapter.restore(level,s);
    var base=s.program.build();
    if(!MovingNativeAdapter.stopped(level,s))throw new IllegalArgumentException("Wait until the retained physical assembly has stopped");
    var inverse=s.origin.orientation().conjugate();
    var measured=s.actualPose.position().sub(s.origin.position()).rotate(inverse);
    var orientation=new Quaterniond(inverse).mul(s.actualPose.orientation()).normalize();
    var projection=base.projectStoppedPose(s.programSeconds,new MovingProgram.Point(measured.x,measured.y,measured.z),
        new double[]{orientation.x,orientation.y,orientation.z,orientation.w},2,Math.toRadians(15),
        MovingNativeAdapter.POSITION_TOLERANCE,MovingNativeAdapter.ANGLE_TOLERANCE);
    var recovery=base.recoveryFrom(projection.originalSeconds(),returning);
    var target=s.docks.get(returning?s.source:s.destination);
    if(target==null)throw new IllegalArgumentException("Repair the selected dock before Resume");
    var bounds=MovingAdmission.bounds(level,s);
    var keys=MovingReservations.validate(level,s,target,Math.max(4,s.receivingLength));
    MovingConstruction.validateSweep(level,s,bounds,recovery.program());
    FoundationData.get(level).moving.reserve(s,keys);
    if(!s.retained())MovingNativeRestoreSupport.releaseForEmptyMotion(level,s);
    MovingNativeAdapter.disconnect(level,s);s.phase=DISCONNECTED;s.returning=returning;s.restored=false;s.fault="";
    s.programSeconds=projection.originalSeconds();runs(level).put(s.id,new Run(recovery,0,Long.MIN_VALUE));s.phase=MOVING;s.epoch++;
  }
  private static void release(ServerLevel level,MovingAssemblyState s) {
    if(s.phase!=RECONNECTED||!s.locksEngaged||!s.retained())throw new IllegalArgumentException("Wait for a physically locked dock and retained whole train");
    var dock=s.docks.get(s.returning?s.source:s.destination);validateParts(level,s);
    var keys=MovingReservations.validate(level,s,dock,Math.max(4,s.receivingLength));
    if(!FoundationData.get(level).moving.owns(s.id,keys))throw new IllegalArgumentException("Revalidate the receiving reservation before release");
    MovingAdmission.validateRelease(level,s,dock);MovingNativeAdapter.dock(level,s,dock);
    MovingServiceRuntime.prepare(level,s,dock);
    try{MovingNativeAdapter.release(level,s);}catch(RuntimeException failure){MovingServiceRuntime.rollback(level,s);throw failure;}
    MovingAdmission.released(level,s,dock);s.released=true;s.phase=RELEASING;
    // Reservations stay until the released consist has fully cleared the bed and receiving span.
    s.fault="";s.epoch++;
  }
  public static void emergencyHold(ServerLevel level,MovingAssemblyState s,String reason){
    thread(level);s.fault(reason);runs(level).remove(s.id);
    try{MovingNativeAdapter.disconnect(level,s);MovingNativeAdapter.hold(level,s,.05);}
    finally{FoundationData.get(level).setDirty();}
  }
  public static void tick(ServerLevel level,double dt) {
    if(!Double.isFinite(dt)||dt<=0||dt>.1)return;
    var data=FoundationData.get(level);if(data.moving.assemblies.isEmpty())return;
    for(var s:data.moving.assemblies.values()) {
      if(s.bed==null)continue;
      try {
        if(s.restored){
          if(!s.restoreDock.isEmpty()){
            validateParts(level,s);
            var dock=s.docks.get(s.restoreDock);
            if(dock==null)throw new IllegalArgumentException("Repair the original stationary dock before restoring support");
            MovingNativeAdapter.restoreDocked(level,s,dock);
            s.phase=s.restoreStationaryPhase;
            MovingNativeRestoreSupport.releaseAfterDock(level,s);
            s.fault="";s.restoreDock="";s.restoreStationaryPhase=null;
          }else{
            MovingNativeAdapter.restore(level,s);MovingNativeAdapter.disconnect(level,s);s.restored=false;
            s.fault("Restored moving assembly: revalidate and explicitly Resume");
          }
        }
        MovingServiceRuntime.tickFaults(level,s,dt);
        MovingServiceRuntime.tickNative(level,s,dt);
        validateParts(level,s);
        if(s.phase==FAULT_HOLD){MovingNativeAdapter.hold(level,s,dt);continue;}
        if(s.phase==DOCKED&&!s.locksEngaged){
          var dock=s.docks.get(s.source);MovingNativeAdapter.drive(level,s,dock.pose(),dt);
          if(MovingNativeAdapter.stopped(level,s)&&s.actualPose.position().distance(dock.pose().position())<=MovingNativeAdapter.POSITION_TOLERANCE
              &&MovingNativeAdapter.angularError(s.actualPose.orientation(),dock.pose().orientation())<=MovingNativeAdapter.ANGLE_TOLERANCE)MovingNativeAdapter.dock(level,s,dock);continue;}
        if(s.phase==RELEASING){finishRelease(level,s);continue;}
        if(s.phase!=MOVING&&s.phase!=ALIGNING)continue;
        requirePower(level,s);if(s.retained())MovingAdmission.validateRetained(level,s);
        var run=runs(level).get(s.id);if(run==null)throw new IllegalArgumentException("Motion cursor lost: explicit recovery required");
        var elapsed=run.elapsed+dt;var sample=run.path.sample(elapsed);
        long checkedTick=run.checkedTick;
        if(checkedTick!=level.getGameTime()) {
          MovingNativeAdapter.readback(level,s);
          var lookahead=run.path.sample(elapsed+.05);
          MovingConstruction.validateMotionStep(level,s,MovingAdmission.bounds(level,s),s.actualPose,
              worldPose(s,lookahead.motion().pose()));
          checkedTick=level.getGameTime();
        }
        MovingNativeAdapter.drive(level,s,worldPose(s,sample.motion().pose()),dt);
        s.programSeconds=sample.originalSeconds();runs(level).put(s.id,new Run(run.path,elapsed,checkedTick));
        if(sample.motion().targetFinished()){
          s.phase=ALIGNING;var dock=s.docks.get(s.returning?s.source:s.destination);
          if(MovingNativeAdapter.stopped(level,s)&&s.actualPose.position().distance(dock.pose().position())<=MovingNativeAdapter.POSITION_TOLERANCE
              &&MovingNativeAdapter.angularError(s.actualPose.orientation(),dock.pose().orientation())<=MovingNativeAdapter.ANGLE_TOLERANCE){
            var keys=MovingReservations.validate(level,s,dock,Math.max(4,s.receivingLength));
            if(!data.moving.owns(s.id,keys))throw new IllegalArgumentException("Receiving reservation changed");
            s.phase=LOCKED;MovingNativeAdapter.dock(level,s,dock);s.phase=RECONNECTED;runs(level).remove(s.id);s.sequence++;
            if(!s.retained()&&s.cars.isEmpty())data.moving.releaseReservation(s.id);
          }
        }
      }catch(RuntimeException failure){
        if(!s.restoreDock.isEmpty())s.restored=true;
        s.fault(failure.getMessage()==null?failure.getClass().getSimpleName():failure.getMessage());runs(level).remove(s.id);
        if(s.restored&&s.bodies.keySet().stream().anyMatch(id->MovingNativeAdapter.faultHoldReady(level,s,id)))s.restored=false;
        try{MovingNativeAdapter.disconnect(level,s);MovingNativeAdapter.hold(level,s,dt);}catch(RuntimeException unavailable){/* Saved identities remain held; never release missing bodies. */}
      }finally{
        for(var pos:s.fixedParts.keySet()){var block=level.getBlockState(pos);
          if(block.getBlock() instanceof MovingPartBlock part&&part.kind()==MovingPartBlock.Kind.LOCK
              &&block.getValue(MovingPartBlock.LOCKED)!=s.locksEngaged)level.setBlock(pos,block.setValue(MovingPartBlock.LOCKED,s.locksEngaged),3);}
      }
    }
    data.setDirty();
  }
  private static void resumeRelease(ServerLevel level,MovingAssemblyState s) {
    validateParts(level,s);var dock=s.docks.get(s.returning?s.source:s.destination);
    var keys=MovingReservations.validate(level,s,dock,Math.max(4,s.receivingLength));
    FoundationData.get(level).moving.reserve(s,keys);MovingNativeAdapter.dock(level,s,dock);s.phase=RELEASING;
    MovingNativeRestoreSupport.releaseAfterDock(level,s);s.fault="";
  }
  private static void finishRelease(ServerLevel level,MovingAssemblyState s) {
    // The old train alone may use its reservation while leaving; new admissions cannot steal it.
    var data=FoundationData.get(level);
    if(!s.service.cars.isEmpty()||level.getGameTime()%10!=0||!MovingReservations.cleared(level,s))return;
    data.moving.releaseReservation(s.id);s.cars=List.of();s.released=false;s.phase=RECONNECTED;s.sequence++;
  }
  public static MovingAssemblyState.Pose worldPose(MovingAssemblyState s,MovingProgram.Pose relative) {
    var q=relative.quaternion();var rotation=s.origin.orientation().mul(new Quaterniond(q[0],q[1],q[2],q[3])).normalize();
    var position=new Vector3d(relative.x(),relative.y(),relative.z()).rotate(s.origin.orientation()).add(s.origin.position());
    return new MovingAssemblyState.Pose(position.x,position.y,position.z,rotation.x,rotation.y,rotation.z,rotation.w);
  }
  private static double trainLength(MovingProgram.Bounds bounds){return Math.max(4,bounds.maxZ()-bounds.minZ()+4);}
  private static void validateParts(ServerLevel level,MovingAssemblyState s) {
    if(!level.hasChunkAt(s.controller)||!(level.getBlockEntity(s.controller) instanceof MovingControllerEntity c)||!c.mechanismId().equals(s.mechanism))
      throw new IllegalArgumentException("Repair the original moving-track controller");
    for(var part:s.fixedParts.entrySet())if(!level.hasChunkAt(part.getKey())||!BuiltInRegistries.BLOCK.getKey(level.getBlockState(part.getKey()).getBlock()).toString().equals(part.getValue()))
      throw new IllegalArgumentException("Repair moving-track guide, socket or end lock at "+part.getKey().toShortString());
    for(var dock:s.docks.values())for(var endpoint:List.of(dock.from(),dock.to()))
      if(!level.hasChunkAt(endpoint)||!(level.getBlockEntity(endpoint) instanceof dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity))
        throw new IllegalArgumentException("Repair fixed moving-track endpoint at "+endpoint.toShortString());
  }
  private static void requirePower(ServerLevel level,MovingAssemblyState s) {
    if(!(level.getBlockEntity(s.controller) instanceof MovingControllerEntity c)||!c.powered())
      throw new IllegalArgumentException("Restore stationary Create drive power, then explicitly Resume");
  }
  private static MovingControllerEntity controller(ServerPlayer player,BlockPos pos) {
    var level=player.serverLevel();thread(level);
    if(!player.mayBuild()||!level.hasChunkAt(pos)||!level.mayInteract(player,pos)
        ||player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>64
        ||!(level.getBlockEntity(pos) instanceof MovingControllerEntity))throw new IllegalArgumentException("Approach and access the moving-track controller");
    return (MovingControllerEntity)level.getBlockEntity(pos);
  }
  private static void thread(ServerLevel level){if(!level.getServer().isSameThread())throw new IllegalStateException("Moving-track requests require the server thread");}
}
