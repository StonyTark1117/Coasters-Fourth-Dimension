package dev.fourthdimension.moving;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.NewGenerationFlyingVehicle;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** First-physics-step world supports for bodies on a restored dock before graph authority exists. */
public final class MovingNativeRestoreSupport {
  private record Hold(ServerSubLevel body,GenericConstraintHandle handle){}
  private static final class Pending {
    final Set<UUID> expected=new LinkedHashSet<>();
    final Map<UUID,Hold> holds=new LinkedHashMap<>();
  }
  private static final Map<ServerLevel,Map<UUID,Pending>> PENDING=new WeakHashMap<>();
  private MovingNativeRestoreSupport(){}
  /** Call only from a native pre-physics callback, before ordinary guide/family callbacks. */
  public static void prepareAll(ServerLevel level){
    for(var state:FoundationData.get(level).moving.assemblies.values())
      if(state.restored||PENDING.getOrDefault(level,Map.of()).containsKey(state.id))prepare(level,state);
  }
  public static boolean prepare(ServerLevel level,MovingAssemblyState state){
    if(state.retained())return true; // Saved retained bodies use the normal bed/partial-fault restoration path.
    var pending=PENDING.computeIfAbsent(level,k->new HashMap<>()).computeIfAbsent(state.id,k->new Pending());
    if(state.bed!=null)pending.expected.add(state.bed);
    pending.expected.addAll(state.bodies.keySet());
    var data=FoundationData.get(level);var container=SubLevelContainer.getContainer(level);
    for(var car:data.vehicles.values()){
      boolean savedDock=state.docks.values().stream().anyMatch(d->car.boundFrom!=null&&car.boundTo!=null
          &&(car.boundFrom.equals(d.from())&&car.boundTo.equals(d.to())||car.boundFrom.equals(d.to())&&car.boundTo.equals(d.from())));
      var parent=container.getSubLevel(car.chassis);
      if(!savedDock&&(parent==null||!overBed(state,parent.logicalPose().position())))continue;
      // A stale last-edge identity does not seize an already departed car.
      if(parent!=null&&!overBed(state,parent.logicalPose().position())&&!state.bodies.containsKey(car.chassis))continue;
      pending.expected.add(car.chassis);car.carriers.forEach(c->pending.expected.add(c.body));
      if(NewGenerationFlyingVehicle.is(car)&&car.yaw.deck!=null)pending.expected.add(car.yaw.deck);
    }
    // Empty native service carts have no FoundationData vehicle record.
    for(var body:container.getAllSubLevels())if(!body.getUniqueId().equals(state.bed)
        &&CoasterCartResolve.cartCenterWorld(body)!=null&&overBed(state,body.logicalPose().position()))pending.expected.add(body.getUniqueId());
    for(var id:pending.expected){
      if(!(container.getSubLevel(id) instanceof ServerSubLevel body)||body.isRemoved())continue;
      var old=pending.holds.get(id);if(old!=null&&old.body==body&&old.handle.isValid())continue;
      var physics=container.physicsSystem();physics.updatePose(body);
      var hold=physics.getPipeline().addConstraint(null,body,new GenericConstraintConfiguration(
          new Vector3d(body.logicalPose().position()),new Vector3d(body.logicalPose().rotationPoint()),
          new Quaterniond(body.logicalPose().orientation()),new Quaterniond(),EnumSet.allOf(ConstraintJointAxis.class)));
      if(hold==null)throw new IllegalArgumentException("Native restored-dock support unavailable: "+id);
      pending.holds.put(id,new Hold(body,hold));if(old!=null)old.handle.remove();
    }
    return pending.expected.stream().allMatch(id->{var h=pending.holds.get(id);return h!=null&&h.handle.isValid();});
  }
  private static boolean overBed(MovingAssemblyState state,org.joml.Vector3dc point){
    var poses=List.of(state.actualPose);
    for(var pose:poses){var local=new Vector3d(point).sub(pose.position()).rotate(pose.orientation().conjugate());
      if(Math.abs(local.x)<8&&Math.abs(local.y)<8&&Math.abs(local.z)<state.deckLength/2.0+2)return true;}
    return false;
  }
  public static boolean held(ServerLevel level,UUID body){
    for(var pending:PENDING.getOrDefault(level,Map.of()).values()){
      var h=pending.holds.get(body);if(h!=null&&h.handle.isValid())return true;
    }return false;
  }
  public static Set<UUID> heldBodies(ServerLevel level,MovingAssemblyState state){
    var pending=PENDING.getOrDefault(level,Map.of()).get(state.id);return pending==null?Set.of():Set.copyOf(pending.holds.keySet());
  }
  /** Explicit empty recovery only; the caller has already validated its path and reservation. */
  public static void releaseForEmptyMotion(ServerLevel level,MovingAssemblyState state){
    if(state.retained()||state.released||!state.service.cars.isEmpty())
      throw new IllegalArgumentException("Retained or released vehicles cannot use empty recovery");
    MovingAdmission.requirePhysicallyEmpty(level,state);
    var pending=PENDING.getOrDefault(level,Map.of()).get(state.id);
    if(pending!=null&&pending.expected.stream().anyMatch(id->!id.equals(state.bed)))
      throw new IllegalArgumentException("Resolve restored vehicle support before empty recovery");
    remove(level,state);
  }
  private static void remove(ServerLevel level,MovingAssemblyState state){
    var map=PENDING.get(level);if(map==null)return;var pending=map.remove(state.id);
    if(pending!=null)pending.holds.values().forEach(h->h.handle.remove());
  }
  /** Drop bootstrap holds only after the actual complete dock bridge has been authenticated. */
  public static void releaseAfterDock(ServerLevel level,MovingAssemblyState state){
    if(MovingNativeAdapter.liveDockEdge(level,state)==null)throw new IllegalArgumentException("Restore measured dock support before releasing bootstrap holds");
    remove(level,state);
  }
}
