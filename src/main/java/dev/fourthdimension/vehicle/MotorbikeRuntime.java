package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.runtime.FamilyControllers;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Fixed-seat lifecycle uses the native chassis and retained stop, with no rotary joint. */
public final class MotorbikeRuntime {
  private static final Map<ServerSubLevel, Long> CHECKED = new WeakHashMap<>();
  private static final Map<ServerSubLevel,dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle> HOLDS=new WeakHashMap<>();
  private MotorbikeRuntime() {}
  /** Transfer only the fixed-world chassis hold after a moving bed has retained every body. */
  public static void releaseMovingWorldHold(ServerSubLevel parent) {
    var hold=HOLDS.remove(parent);if(hold!=null&&hold.isValid())hold.remove();
  }
  private static boolean vRestored(FoundationData.Vehicle v){return v.motorbike.restored;}
  private static FoundationData.Rail rail(ServerLevel l,FoundationData.Vehicle v){
    return dev.fourthdimension.moving.MovingRouteContext.rails(l).stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
  }
  /** Measured safe platform on each side; no generic world-spawn fallback qualifies a dock. */
  private static boolean platforms(ServerLevel l,ServerSubLevel p,Vec3 graph){
    var center=new Vector3d(graph.x,graph.y,graph.z);
    var right=new Vector3d(1,0,0).rotate(p.logicalPose().orientation());
    for(int side:new int[]{-1,1}){
      double x=center.x+side*3.0*right.x,z=center.z+side*3.0*right.z;
      var floor=BlockPos.containing(x,center.y+.8,z).below();
      if(!l.hasChunkAt(floor)||!l.getBlockState(floor).isFaceSturdy(l,floor,Direction.UP)
          ||!l.getFluidState(floor.above()).isEmpty())return false;
      double y=floor.getY()+1;
      if(y-center.y<.5||y-center.y>1.05||!l.noCollision(new net.minecraft.world.phys.AABB(x-.31,y,z-.31,x+.31,y+1.9,z+.31)))return false;
    }return true;
  }
  public static String dockDiagnostic(ServerLevel l,ServerSubLevel p,FoundationData.Vehicle v,BlockPos station){
    var center=CoasterCartResolve.cartCenterWorld(p);var edge=MotorbikeTrack.support(l,v);var right=new Vector3d(1,0,0).rotate(p.logicalPose().orientation());
    var detail=new StringBuilder("center="+center+" speed="+SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()+" up="+new Vector3d(0,1,0).rotate(p.logicalPose().orientation()).y+" support="+(edge!=null)+" stop="+v.stopping+" capture="+v.carriers.getFirst().capture+" fault="+v.fault+" single="+single(l,p,v));
    if(center!=null){
      detail.append(" stationDist=").append(center.distance(new Vector3d(station.getX()+.5,station.getY()+.5,station.getZ()+.5)));
      if(edge!=null){var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));detail.append(" graph=").append(hit.point()).append(" progress=").append(TrackGeometry.table(l,edge).distanceAt(hit.t())).append(" obstacle=").append(MotorbikeTrack.obstruction(l,hit));}
      for(int side:new int[]{-1,1}){double x=center.x+side*3*right.x,z=center.z+side*3*right.z;var floor=BlockPos.containing(x,center.y+.8,z).below();double y=floor.getY()+1;
        detail.append(" floor").append(side).append('=').append(floor).append('/').append(l.getBlockState(floor)).append(" dy=").append(y-center.y).append(" clear=").append(l.noCollision(new net.minecraft.world.phys.AABB(x-.31,y,z-.31,x+.31,y+1.9,z+.31)));
      }
    }return detail.toString();
  }
  private static boolean dockPose(ServerLevel l,ServerSubLevel p,FoundationData.Vehicle v,BlockPos station){
    var edge=MotorbikeTrack.support(l,v);var center=CoasterCartResolve.cartCenterWorld(p);
    if(edge==null||center==null||!single(l,p,v)||!v.stopping||!v.carriers.getFirst().capture
        ||v.alignmentRequired||v.maintenance||new Vector3d(0,1,0).rotate(p.logicalPose().orientation()).y<.999
        ||SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()>.05
        ||center.distanceSquared(new Vector3d(station.getX()+.5,station.getY()+.5,station.getZ()+.5))>144)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
    var table=TrackGeometry.table(l,edge);double at=table.distanceAt(hit.t());
    return table.length()>=12&&at>=2&&at<=table.length()-2&&MotorbikeTrack.obstruction(l,hit)==null&&platforms(l,p,hit.point());
  }
  public static boolean ready(ServerLevel l,ServerSubLevel p,FoundationData.Vehicle v){
    if(!MotorbikeVehicle.is(v)||v.dock==null||!v.fault.isEmpty()||v.fixedDockFrom==null||v.fixedDockTo==null
        ||!l.hasChunkAt(v.dock)||!(l.getBlockEntity(v.dock) instanceof BoardingStationEntity station)
        ||!v.chassis.equals(station.vehicle))return false;
    var r=rail(l,v);
    return r!=null&&r.matches(v.fixedDockFrom,v.fixedDockTo)&&r.geometryHash==v.fixedDockGeometry&&dockPose(l,p,v,v.dock);
  }
  private static boolean single(ServerLevel l,ServerSubLevel p,FoundationData.Vehicle v){
    return v.recoveryCart==null&&dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(p).stream().allMatch(id->id.equals(v.chassis));
  }
  public static void acknowledge(ServerLevel l,FoundationData.Vehicle v){
    var p=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(p instanceof ServerSubLevel body&&safety(l,body,v)==null&&!v.alignmentRequired){v.fault="";v.motorbike.running=false;}
  }
  /** Existing Chain drives may coexist with gravity spans; each accepted powered anchor stays live. */
  private static Map<BlockPos,Integer> driveDirections(ServerLevel l,FoundationData.Vehicle v){
    var result=new HashMap<BlockPos,Integer>();var visited=new HashSet<BlockPos>();var todo=new ArrayDeque<BlockPos>();
    if(v.boundFrom!=null)todo.add(v.boundFrom);
    while(!todo.isEmpty()){
      var at=todo.removeFirst();if(!visited.add(at))continue;
      if(visited.size()>64)throw new IllegalArgumentException("Motorbike route exceeds 64 anchors");
      if(!l.hasChunkAt(at))throw new IllegalArgumentException("Load the Motorbike route before Dispatch or Resume");
      if(l.getBlockEntity(at) instanceof CoasterAnchorpointBlockEntity anchor&&anchor.hasChainLiftSpan()){
        int direction=(int)Math.signum(anchor.getSpeed());
        if(direction==0||anchor.resolveChainLiftSpanPath(0)==null)throw new IllegalArgumentException("Motorbike drive power/span unavailable; restore before Dispatch or Resume");
        result.put(at.immutable(),direction);
      }
      for(var r:FoundationData.get(l).rails)if(r.valid&&r.family==v.family&&r.variant.equals(v.variant)){
        if(r.from.equals(at))todo.add(r.to);else if(r.to.equals(at))todo.add(r.from);
      }
    }return result;
  }
  public static void rememberConsist(ServerLevel l,FoundationData.Vehicle v){
    var directions=driveDirections(l,v);v.motorbike.directions.clear();v.motorbike.directions.putAll(directions);
    v.motorbike.running=true;v.motorbike.restored=false;
  }
  public static void rebindDock(ServerLevel l,FoundationData.Vehicle v,BlockPos pos){
    var p=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(!(p instanceof ServerSubLevel body)||!(l.getBlockEntity(pos) instanceof BoardingStationEntity)
        ||!dockPose(l,body,v,pos)||!v.restraints&&!v.carriers.getFirst().occupants.isEmpty())
      throw new IllegalArgumentException("Retain the upright Motorbike on a clear 12-block level dock with platforms on both sides");
    var r=rail(l,v);v.dock=pos.immutable();v.fixedDockFrom=r.from;v.fixedDockTo=r.to;v.fixedDockGeometry=r.geometryHash;
    v.revision++;FoundationData.get(l).setDirty();
  }
  public static void resumeToDock(ServerLevel l,FoundationData.Vehicle v){
    var p=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(!(p instanceof ServerSubLevel body)||!v.stopping||!v.restraints||v.maintenance||v.alignmentRequired
        ||SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.08)
      throw new IllegalArgumentException("Wait for a retained Motorbike with closed hold-down before Resume");
    var failure=safety(l,body,v);if(failure!=null)throw new IllegalArgumentException(failure);
    rememberConsist(l,v);v.fault="";v.stopping=false;v.carriers.getFirst().capture=false;v.revision++;FoundationData.get(l).setDirty();
  }

  public static void tick(ServerLevel level, ServerSubLevel parent,
      FoundationData.Vehicle vehicle, double dt) {
    if(vRestored(vehicle)){
      vehicle.stopping=true;vehicle.carriers.getFirst().capture=true;
      if(vehicle.motorbike.running){vehicle.restraints=true;vehicle.fault="Motorbike restart retained; inspect then Resume";}
      vehicle.motorbike.running=false;vehicle.motorbike.restored=false;FoundationData.get(level).setDirty();
    }
    if (!Objects.equals(CHECKED.put(parent, level.getGameTime()), level.getGameTime())) {
      String failure=safety(level,parent,vehicle);
      if(failure!=null){vehicle.stopping=true;vehicle.restraints=true;vehicle.fault=failure;vehicle.carriers.getFirst().capture=true;FoundationData.get(level).setDirty();}
    }
    boolean retained=vehicle.stopping||vehicle.maintenance;
    var hold=HOLDS.get(parent);
    if(!retained&&hold!=null){if(hold.isValid())hold.remove();HOLDS.remove(parent);}
    if(retained){
      VehicleOperations.brake(level,vehicle,dt*2);
      var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
      if((hold==null||!hold.isValid())&&pipeline.getLinearVelocity(parent,new Vector3d()).length()<=.08){
        var anchor=new Vector3d(parent.getMassTracker().getCenterOfMass());
        var world=parent.logicalPose().transformPosition(new Vector3d(anchor));
        HOLDS.put(parent,pipeline.addConstraint(null,parent,new dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration(
            world,anchor,new org.joml.Quaterniond(parent.logicalPose().orientation()),new org.joml.Quaterniond(),
            EnumSet.allOf(dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis.class))));
      }
    }
    var seats = vehicle.carriers.getFirst();
    seats.angle = 0;
    seats.omega = 0;
    NativeLoading.update(parent, vehicle, seats);
    vehicle.boardingReady = Boarding.ready(level, vehicle);
  }

  public static String safety(ServerLevel level, ServerSubLevel parent, FoundationData.Vehicle vehicle) {
    if(!vehicle.stopping&&vehicle.motorbike.running){
      for(var accepted:vehicle.motorbike.directions.entrySet()){
        if(!level.hasChunkAt(accepted.getKey())||!(level.getBlockEntity(accepted.getKey()) instanceof CoasterAnchorpointBlockEntity a)
            ||!a.hasChainLiftSpan()||a.resolveChainLiftSpanPath(0)==null||(int)Math.signum(a.getSpeed())!=accepted.getValue())
          return "Motorbike drive lost or reversed; restore and explicitly Resume";
      }
    }else{
      try{driveDirections(level,vehicle);}catch(IllegalArgumentException unavailable){return unavailable.getMessage();}
    }
    var edge = MotorbikeTrack.support(level, vehicle);
    if (edge == null) return "Motorbike matching Motorbike Track unavailable; restore track then Stop";
    if (!single(level,parent,vehicle))return "Motorbike supports one unlinked car";
    if(!FamilyControllers.find(parent).equals(Set.of(vehicle.family)))return "Motorbike controller unavailable";
    var blocks=parent.getPlot().getEmbeddedLevelAccessor();
    for(var p:MotorbikeVehicle.SEATS)if(!blocks.getBlockState(p).is(MotorbikeContent.SEAT.get()))return "Motorbike saddle unavailable";
    for(var p:MotorbikeVehicle.FRAME)if(!blocks.getBlockState(p).is(MotorbikeContent.FRAME.get()))return "Motorbike supporting frame unavailable";
    var center = CoasterCartResolve.cartCenterWorld(parent);
    if (center == null) return "Motorbike running gear unavailable";
    var hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, new Vec3(center.x, center.y, center.z));
    if (hit == null) return "Motorbike track position unavailable";
    if (MotorbikeTrack.obstruction(level, hit) != null) return "Motorbike rider envelope obstructed; clear it then Stop";
    var velocity = SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
        .getLinearVelocity(parent, new Vector3d());
    var forward = CoasterPathTrackFrame.trackBasisAtHit(level, hit).forward();
    if(velocity.length()>4.15)return "Motorbike speed exceeds 4 blocks/s profile";
    double speed = velocity.dot(forward.x, forward.y, forward.z);
    if (Math.abs(speed) < .08) return null;
    var table = TrackGeometry.table(level, edge);
    double distance = table.distanceAt(hit.t());
    double braking = speed * speed / 12 + 2.5;
    // Sample the actual swept corridor ahead, including the stopping margin.
    for (double advance = .5; advance <= braking; advance += .5) {
      double target = distance + Math.copySign(advance, speed);
      if (target < 0 || target > table.length()) break;
      double t = table.parameterAt(target);
      var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
      if (MotorbikeTrack.obstruction(level, new CoasterPathTrackFrame.GraphHit(point, edge, t)) != null)
        return "Motorbike rider envelope ahead obstructed; clear it then Stop";
    }
    double remaining = speed > 0 ? table.length() - distance : distance;
    if (remaining <= braking) {
      var endpoint = speed > 0 ? edge.to() : edge.from();
      var nextEdges = CoasterPathGraphManager.get(level).edges().stream().filter(e ->
          !(e.from().equals(edge.from()) && e.to().equals(edge.to())
              || e.from().equals(edge.to()) && e.to().equals(edge.from()))
              && (e.from().equals(endpoint) || e.to().equals(endpoint))).toList();
      if (nextEdges.size() != 1 || MotorbikeTrack.rejection(level, nextEdges.getFirst()) != null)
        return "Motorbike end or branch of compatible track; Stop and inspect route";
      var next = nextEdges.getFirst();
      var nextTable = TrackGeometry.table(level, next);
      for (double advance = 0; advance <= Math.min(braking - remaining, nextTable.length()); advance += .5) {
        double along = next.from().equals(endpoint) ? advance : nextTable.length() - advance;
        double t = nextTable.parameterAt(along);
        var point = CoasterPathGraphManager.sampleEdgePosition(level, next, t);
        if (MotorbikeTrack.obstruction(level, new CoasterPathTrackFrame.GraphHit(point, next, t)) != null)
          return "Motorbike rider envelope across connection obstructed; clear it then Stop";
      }
    }
    return null;
  }
}
