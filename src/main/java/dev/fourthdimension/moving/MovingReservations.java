package dev.fourthdimension.moving;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

/** Whole receiving spans, reserved by stable physical endpoint identities in this dimension. */
public final class MovingReservations {
  private MovingReservations() {}
  public static UUID key(BlockPos a,BlockPos b) {
    long first=a.asLong(),second=b.asLong();
    return UUID.nameUUIDFromBytes((Math.min(first,second)+":"+Math.max(first,second)).getBytes(StandardCharsets.UTF_8));
  }
  public static Set<UUID> validate(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock,double requiredLength) {
    if(!Double.isFinite(requiredLength)||requiredLength<=0||requiredLength>state.deckLength)
      throw new IllegalArgumentException("Invalid complete receiving train length");
    var graph=CoasterPathGraphManager.get(level);var keys=new LinkedHashSet<UUID>();
    keys.add(key(dock.from(),dock.to()));
    // Both ends are protected: a second train cannot enter the retained bed from the approach.
    for(var start:List.of(dock.from(),dock.to())) {
      var at=start;var previous=start.equals(dock.from())?dock.to():dock.from();double covered=0;
      var visited=new HashSet<BlockPos>();
      while(covered<requiredLength) {
        if(!visited.add(at))throw new IllegalArgumentException("Receiving track loops back inside the reserved train length");
        if(!level.hasChunkAt(at))throw new IllegalArgumentException("Load the complete receiving track before moving");
        final var node=at;final var prior=previous;
        var next=graph.edges().stream().filter(e->(e.from().equals(node)||e.to().equals(node))
            &&!e.from().equals(prior)&&!e.to().equals(prior)).toList();
        if(next.size()!=1)throw new IllegalArgumentException("Each dock needs one unambiguous fixed receiving span at both ends");
        var edge=next.getFirst();var other=edge.from().equals(at)?edge.to():edge.from();
        if(!level.hasChunkAt(other)||Sable.HELPER.getContaining(level,edge.from())!=null
            ||Sable.HELPER.getContaining(level,edge.to())!=null)
          throw new IllegalArgumentException("Receiving track must be loaded and fixed in the world");
        var length=TrackGeometry.table(level,edge).length();
        if(!Double.isFinite(length)||length<.01)throw new IllegalArgumentException("Invalid receiving track geometry");
        // Dense world occupancy boxes include the full receiving rail, not merely a destination point.
        int count=(int)Math.ceil(length/.5);
        for(int i=0;i<=count;i++) {
          var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,i/(double)count);
          if(!level.hasChunkAt(BlockPos.containing(point)))throw new IllegalArgumentException("Receiving span crosses an unloaded chunk");
          var clearance=clearance(level,state,edge,i/(double)count);
          for(var body:SubLevelContainer.getContainer(level).getAllSubLevels()) {
            var id=body.getUniqueId();if(id.equals(state.bed)||state.bodies.containsKey(id)||state.cars.contains(id)||ownChild(level,state,id))continue;
            var b=body.boundingBox();
            if(clearance.intersects(new AABB(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ())))
              throw new IllegalArgumentException("Exit "+dock.name()+" occupied; clear its entire receiving track");
          }
        }
        keys.add(key(edge.from(),edge.to()));covered+=length;previous=at;at=other;
        if(visited.size()>64)throw new IllegalArgumentException("Receiving route exceeds the bounded moving-track scan");
      }
    }
    return Set.copyOf(keys);
  }
  private static AABB clearance(ServerLevel level,MovingAssemblyState state,CoasterPathEdge edge,double t) {
    var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,point);
    if(hit==null)throw new IllegalArgumentException("Receiving track frame unavailable");
    var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    var forward=new org.joml.Vector3d(basis.forward().x,basis.forward().y,basis.forward().z);
    var up=new org.joml.Vector3d(basis.up().x,basis.up().y,basis.up().z);
    var right=new org.joml.Vector3d(basis.right().x,basis.right().y,basis.right().z);
    var e=state.receivingEnvelope;AABB result=null;
    for(double x:new double[]{e.minX(),e.maxX()})for(double y:new double[]{e.minY(),e.maxY()})for(double z:new double[]{-.5,.5}){
      var v=new org.joml.Vector3d(right).mul(x).fma(y,up).fma(z,forward).add(point.x,point.y,point.z);
      var corner=new net.minecraft.world.phys.Vec3(v.x,v.y,v.z);var box=new AABB(corner,corner);
      result=result==null?box:result.minmax(box);
    }
    return result.inflate(.25);
  }
  public static boolean cleared(ServerLevel level,MovingAssemblyState state) {
    var data=FoundationData.get(level);var claimed=data.moving.reservedBy(state.id);
    if(claimed.isEmpty())throw new IllegalArgumentException("Receiving claims unavailable; explicitly revalidate");
    var container=SubLevelContainer.getContainer(level);var bodies=new ArrayList<dev.ryanhcode.sable.sublevel.SubLevel>();
    for(var id:state.cars){var body=container.getSubLevel(id);if(body==null)throw new IllegalArgumentException("Released chassis unavailable; retain exit reservation");bodies.add(body);
      var car=data.vehicles.get(id);
      if(car!=null&&dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.is(car)){
        var deck=container.getSubLevel(car.yaw.deck);if(deck==null)throw new IllegalArgumentException("Released yaw deck unavailable; retain exit reservation");bodies.add(deck);
      }
      if(car!=null)for(var carrier:car.carriers){var child=container.getSubLevel(carrier.body);if(child==null)throw new IllegalArgumentException("Released carrier unavailable; retain exit reservation");bodies.add(child);}}
    var seen=new HashSet<UUID>();
    for(var edge:CoasterPathGraphManager.get(level).edges())if(claimed.contains(key(edge.from(),edge.to()))){
      seen.add(key(edge.from(),edge.to()));int count=Math.max(1,(int)Math.ceil(TrackGeometry.table(level,edge).length()/.5));
      for(int i=0;i<=count;i++){
        var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,i/(double)count);
        if(!level.hasChunkAt(BlockPos.containing(point)))throw new IllegalArgumentException("Load the complete receiving span before releasing its reservation");
        var box=clearance(level,state,edge,i/(double)count);
        for(var body:bodies){var b=body.boundingBox();if(box.intersects(new AABB(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ())))return false;}}
    }
    if(!seen.containsAll(claimed))throw new IllegalArgumentException("Reserved receiving span changed; retain claims");
    return true;
  }
  private static boolean ownChild(ServerLevel level,MovingAssemblyState state,UUID body){
    var data=FoundationData.get(level);for(var id:state.cars){var car=data.vehicles.get(id);if(car==null)continue;
      if(car.carriers.stream().anyMatch(c->c.body.equals(body))||body.equals(car.yaw.deck))return true;}return false;
  }
  public static boolean permits(FoundationData data,UUID body,CoasterPathEdge edge) {
    var owner=data.moving.receivingOwner(key(edge.from(),edge.to()));
    if(owner==null)return true;
    var assembly=data.moving.assemblies.get(owner);
    return assembly!=null&&assembly.cars.contains(body);
  }
}
