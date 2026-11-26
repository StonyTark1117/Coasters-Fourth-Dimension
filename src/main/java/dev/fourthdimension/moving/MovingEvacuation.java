package dev.fourthdimension.moving;

import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Service access only: the operator enables ordinary restraints and voluntary safe dismount. */
public final class MovingEvacuation {
  private static final Map<ServerLevel,Map<UUID,Long>> ACCESS=new WeakHashMap<>();
  private MovingEvacuation() {}
  public static void open(ServerPlayer operator,MovingAssemblyState s) {
    var level=operator.serverLevel();validate(level,s);
    for(var player:level.players()) {
      var seat=player.getVehicle();if(seat==null)continue;
      var body=Sable.HELPER.getContaining(seat);
      if(body!=null&&ownedBody(s,body.getUniqueId())&&findExit(level,player,seat)==null)
        throw new IllegalArgumentException("Build a clear fixed service platform within two blocks of every occupied seat");
    }
    ACCESS.computeIfAbsent(level,k->new HashMap<>()).put(s.id,s.epoch);
    // Preserve the underlying fault; access is separately revalidated against the current epoch.
  }
  public static boolean authorized(ServerLevel level,FoundationData.Vehicle car) {
    var s=FoundationData.get(level).moving.retaining(car.chassis);
    if(s==null)s=MovingServiceRuntime.owner(level,car);if(s==null)return false;
    if(!Objects.equals(ACCESS.getOrDefault(level,Map.of()).get(s.id),s.epoch))return false;
    try{validate(level,s);}catch(RuntimeException unavailable){return false;}
    for(var player:level.players()) {
      var seat=player.getVehicle();if(seat==null)continue;var body=Sable.HELPER.getContaining(seat);
      if(body!=null&&ownedBody(s,body.getUniqueId())&&findExit(level,player,seat)==null)return false;
    }
    return true;
  }
  public static Vec3 exit(ServerLevel level,FoundationData.Vehicle car,ServerPlayer player,Entity seat) {
    return authorized(level,car)?findExit(level,player,seat):null;
  }
  private static boolean ownedBody(MovingAssemblyState s,UUID id) {
    return s.bodies.containsKey(id)||s.service.cars.containsKey(id)
        ||s.service.cars.values().stream().anyMatch(c->c.holds.stream().anyMatch(h->h.child().equals(id)));
  }
  private static void validate(ServerLevel level,MovingAssemblyState s) {
    if(s.released) {
      if(s.phase!=MovingAssemblyState.Phase.FAULT_HOLD&&s.phase!=MovingAssemblyState.Phase.RELEASING)
        throw new IllegalArgumentException("Stop the released service train before evacuation");
      if(s.service.cars.isEmpty())throw new IllegalArgumentException("No retained service train");
      for(var id:s.service.cars.keySet()) {
        var car=FoundationData.get(level).vehicles.get(id);
        if(car==null||!MovingServiceRuntime.faultHoldReady(level,car))
          throw new IllegalArgumentException("Stop and secure every surviving service car before evacuation");
      }
    } else if(s.phase!=MovingAssemblyState.Phase.FAULT_HOLD||!s.retained()||!MovingNativeAdapter.stopped(level,s))
      throw new IllegalArgumentException("Stop and retain the assembly before service evacuation");
    var container=SubLevelContainer.getContainer(level);var pipeline=container.physicsSystem().getPipeline();
    var ids=new HashSet<UUID>(s.bodies.keySet());
    for(var c:s.service.cars.values()){ids.add(c.chassis);for(var h:c.holds)ids.add(h.child());}
    for(var id:ids){
      var raw=container.getSubLevel(id);
      // A missing saved body remains a motion/release fault. It must not trap riders on secured survivors.
      if(raw==null||raw.isRemoved())continue;
      if(!(raw instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel body)
          ||!s.released&&!MovingNativeAdapter.faultHoldReady(level,s,id)
          ||pipeline.getLinearVelocity(body,new Vector3d()).length()>.025
          ||pipeline.getAngularVelocity(body,new Vector3d()).length()>.01)
        throw new IllegalArgumentException("Every surviving car and carrier must be physically held and stationary for evacuation");
    }
  }
  private static Vec3 findExit(ServerLevel level,ServerPlayer player,Entity seat) {
    var body=Sable.HELPER.getContaining(seat);if(body==null)return null;
    var point=body.logicalPose().transformPosition(new Vector3d(seat.getX(),seat.getY(),seat.getZ()));
    var origin=new Vec3(point.x,point.y,point.z);var start=BlockPos.containing(origin);
    Vec3 best=null;double distance=Double.POSITIVE_INFINITY;
    for(var floor:BlockPos.betweenClosed(start.offset(-2,-2,-2),start.offset(2,0,2))) {
      if(!level.hasChunkAt(floor)||Sable.HELPER.getContaining(level,floor)!=null
          ||!level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP))continue;
      var shape=level.getBlockState(floor).getCollisionShape(level,floor);if(shape.isEmpty())continue;
      var feet=new Vec3(floor.getX()+.5,floor.getY()+shape.max(Direction.Axis.Y)+.01,floor.getZ()+.5);
      if(Math.abs(feet.y-origin.y)>1.25||feet.distanceToSqr(origin)>6.25)continue;
      var box=new AABB(feet.x-.31,feet.y,feet.z-.31,feet.x+.31,feet.y+1.9,feet.z+.31);
      if(!level.getFluidState(floor.above()).isEmpty()||!level.getFluidState(floor.above(2)).isEmpty()
          ||!level.noCollision(player,box))continue;
      var ray=level.clip(new net.minecraft.world.level.ClipContext(origin.add(0,.7,0),feet.add(0,.7,0),
          net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.ANY,player));
      if(ray.getType()!=net.minecraft.world.phys.HitResult.Type.MISS)continue;
      double score=feet.distanceToSqr(origin);if(score<distance){best=feet;distance=score;}
    }
    return best;
  }
}
