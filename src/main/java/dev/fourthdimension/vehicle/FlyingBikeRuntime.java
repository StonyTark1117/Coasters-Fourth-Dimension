package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Rigid native seats: train-wide hold, explicit level-dock capture and pose-preserving recovery. */
public final class FlyingBikeRuntime {
  private static final Map<ServerSubLevel,Long> CHECKED=new WeakHashMap<>();
  private FlyingBikeRuntime(){}
  public static FoundationData.Rail rail(ServerLevel level,FoundationData.Vehicle v){
    return dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
  }
  public static void fault(ServerLevel level,FoundationData.Vehicle v,String reason){
    var ids=new HashSet<>(java.util.List.<java.util.UUID>of());ids.add(v.chassis);
    var parent=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
    if(parent!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent));
    for(var id:ids){var car=FoundationData.get(level).vehicles.get(id);if(car==null||!FlyingBikeVehicle.is(car))continue;
      car.stopping=true;car.restraints=true;car.fault=reason;car.carriers.forEach(c->c.capture=true);
      car.flyingBike.resumeRequired=true;car.flyingBike.phase="FAULT";
      if(car.authority.owner!=null)dev.fourthdimension.integration.RideAuthority.emergencyHold(level,car);
      var g=governor(level,car);if(g!=null)g.cut();
    }
  }
  public static void rememberConsist(ServerLevel level,FoundationData.Vehicle v){
    if(!VehicleOperations.consist(level,v).equals(List.of(v)))throw new IllegalArgumentException("Flying Bike is a single car");
  }
  /** Dry standing exits beside both prone riders, outside the rigid vehicle reservation. */
  private static boolean platforms(ServerLevel level,ServerSubLevel parent,Vec3 graph){
    var right=new Vector3d(1,0,0).rotate(parent.logicalPose().orientation());
    for(int side:new int[]{-1,1}){
      double x=graph.x+side*4*right.x,z=graph.z+side*4*right.z;
      var floor=BlockPos.containing(x,graph.y-2,z).below();double y=floor.getY()+1;
      if(!level.hasChunkAt(floor)||!level.getBlockState(floor).isFaceSturdy(level,floor,net.minecraft.core.Direction.UP)
          ||!level.getFluidState(floor).isEmpty()||!level.getFluidState(floor.above()).isEmpty()||!level.getFluidState(floor.above(2)).isEmpty()
          ||y-graph.y< -2.8||y-graph.y> -2
          ||!level.noCollision(new net.minecraft.world.phys.AABB(x-.31,y,z-.31,x+.31,y+1.9,z+.31)))return false;
    }return true;
  }
  public static boolean evacuation(FoundationData.Vehicle v){return v.flyingBike.evacuationStation!=null;}
  /** Exact bounded world-horizontal transition beside the transformed seat, valid at any car roll. */
  public static Vec3 exit(ServerLevel level,FoundationData.Vehicle v,BlockPos nativeSeat){
    var raw=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
    if(!(raw instanceof ServerSubLevel body))return null;
    var offset=nativeSeat.subtract(body.getPlot().getCenterBlock());if(!FlyingBikeVehicle.SEATS.contains(offset))return null;
    var p=body.logicalPose().transformPosition(new Vector3d(nativeSeat.getX()+.5,nativeSeat.getY()+.7,nativeSeat.getZ()-.35));
    var origin=new Vec3(p.x,p.y,p.z);Vec3 best=null;double score=Double.MAX_VALUE;
    var start=BlockPos.containing(origin);
    for(var floor:BlockPos.betweenClosed(start.offset(-4,-3,-4),start.offset(4,1,4))){
      double y=floor.getY()+1,dx=floor.getX()+.5-origin.x,dz=floor.getZ()+.5-origin.z,dy=y-origin.y;
      if(dx*dx+dz*dz>16||dy< -1.25||dy>.6||!level.hasChunkAt(floor)
          ||!level.getBlockState(floor).isFaceSturdy(level,floor,net.minecraft.core.Direction.UP)
          ||!level.getFluidState(floor).isEmpty()||!level.getFluidState(floor.above()).isEmpty()||!level.getFluidState(floor.above(2)).isEmpty())continue;
      var feet=new Vec3(floor.getX()+.5,y+.01,floor.getZ()+.5);
      if(!level.noCollision(new net.minecraft.world.phys.AABB(feet.x-.31,feet.y,feet.z-.31,feet.x+.31,feet.y+1.9,feet.z+.31)))continue;
      double d=feet.distanceToSqr(origin);if(d<score){score=d;best=feet;}
    }return best;
  }
  private static boolean evacuationSupport(ServerLevel level,ServerSubLevel body,FoundationData.Vehicle v){
    var r=rail(level,v);var e=r==null?null:TrackGeometry.resolve(level,r);
    if(!v.stopping||v.alignmentRequired||!v.carriers.getFirst().capture||e==null||FlyingBikeTrack.rejection(level,e)!=null
        ||SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05
        ||SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getAngularVelocity(body,new Vector3d()).length()>.01
        ||v.flyingBike.evacuationStoppedTicks<5)return false;
    try{FlyingBikeVehicle.validate(v);if(!ConsistRules.eligible(level,body,v))return false;}catch(IllegalArgumentException invalid){return false;}
    var blocks=body.getPlot().getEmbeddedLevelAccessor();
    for(var seat:FlyingBikeVehicle.SEATS)if(!blocks.getBlockState(seat).is(FlyingBikeContent.SEAT.get()))return false;
    for(var part:FlyingBikeVehicle.FRAME)if(!blocks.getBlockState(part).is(FlyingBikeContent.FRAME.get()))return false;
    if(!FamilyControllers.find(body).equals(Set.of(v.family)))return false;
    for(var seat:FlyingBikeVehicle.SEATS)if(exit(level,v,body.getPlot().getCenterBlock().offset(seat))==null)return false;
    return true;
  }
  private static boolean evacuationReady(ServerLevel level,ServerSubLevel body,FoundationData.Vehicle v){
    var station=v.flyingBike.evacuationStation;
    return station!=null&&level.hasChunkAt(station)&&level.getBlockEntity(station) instanceof BoardingStationEntity s
        &&v.chassis.equals(s.vehicle)&&evacuationSupport(level,body,v);
  }
  private static boolean localDock(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    if(v.dock==null||v.flyingBike.dockFrom==null||v.flyingBike.dockTo==null||!v.stopping||v.alignmentRequired||!v.fault.isEmpty()
        ||!level.hasChunkAt(v.dock)||!(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station))return false;
    var cars=VehicleOperations.consist(level,v);
    if(cars.stream().noneMatch(car->car.chassis.equals(station.vehicle)))return false;
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.matches(v.flyingBike.dockFrom,v.flyingBike.dockTo)||rail.geometryHash!=v.flyingBike.dockGeometry
        ||FlyingBikeTrack.rejection(level,edge)!=null||TrackGeometry.table(level,edge).length()<32)return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);
    if(center==null||center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))>1600
        ||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999
        ||SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    double at=TrackGeometry.table(level,edge).distanceAt(hit.t());
    return at>=.1&&at<=TrackGeometry.table(level,edge).length()-1.75&&FlyingBikeTrack.obstruction(level,hit)==null&&platforms(level,parent,hit.point());
  }
  public static boolean ready(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    try{
      if(evacuation(v))return evacuationReady(level,parent,v);
      if(!ConsistRules.eligible(level,parent,v))return false;
      var container=SubLevelContainer.getContainer(level);
      for(var car:VehicleOperations.consist(level,v)){
        if(!FlyingBikeVehicle.is(car)||!(container.getSubLevel(car.chassis) instanceof ServerSubLevel body)
            ||!car.carriers.getFirst().capture||!localDock(level,body,car))return false;
      }return true;
    }catch(IllegalArgumentException unavailable){return false;}
  }
  public static void rebindDock(ServerLevel level,FoundationData.Vehicle selected,BlockPos pos){
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    if(!Objects.equals(pos,selected.dock)&&(selected.dock!=null||!selected.carriers.getFirst().occupants.isEmpty()||evacuation(selected))){
      var raw=container.getSubLevel(selected.chassis);
      if(!(raw instanceof ServerSubLevel body)||!selected.restraints||!evacuationSupport(level,body,selected))
        throw new IllegalArgumentException("Stop and retain the car, repair matching support and provide dry clear exits beside both transformed seats");
      var center=CoasterCartResolve.cartCenterWorld(body);
      if(center==null||center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>144)
        throw new IllegalArgumentException("Place the evacuation station within twelve blocks of the stopped car");
      selected.flyingBike.evacuationStation=pos.immutable();selected.flyingBike.resumeRequired=true;
      selected.revision++;FoundationData.get(level).setDirty();return;
    }
    if(cars.size()!=1)throw new IllegalArgumentException("Flying Bike supports exactly one rigid car");
    for(var v:cars){
      var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
      if(evacuation(v)&&(rail==null||!rail.matches(v.flyingBike.dockFrom,v.flyingBike.dockTo)||rail.geometryHash!=v.flyingBike.dockGeometry))
        throw new IllegalArgumentException("Restore the original level launch dock before leaving evacuation mode");
      if(!FlyingBikeVehicle.is(v)||!v.stopping||v.alignmentRequired||v.maintenance
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body)||edge==null
          ||FlyingBikeTrack.rejection(level,edge)!=null||TrackGeometry.table(level,edge).length()<32
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05
          ||new Vector3d(0,1,0).rotate(body.logicalPose().orientation()).y<.999
          ||!v.restraints&&!v.carriers.getFirst().occupants.isEmpty())
        throw new IllegalArgumentException("Stop and retain every car on the level 32-block dock before binding");
      var center=CoasterCartResolve.cartCenterWorld(body);
      if(center==null||center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>1600)
        throw new IllegalArgumentException("Bring every car to the declared dock");
      var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
      if(hit==null||!platforms(level,body,hit.point()))throw new IllegalArgumentException("Provide dry sturdy clear platforms on both sides of the prone dock");
    }
    for(var v:cars){v.flyingBike.evacuationStation=null;var rail=rail(level,v);v.dock=pos.immutable();v.flyingBike.dockFrom=rail.from;v.flyingBike.dockTo=rail.to;
      v.flyingBike.dockGeometry=rail.geometryHash;
      if(v.fault.startsWith("Flying Bike coupler/consist changed"))v.fault="";v.revision++;
    }FoundationData.get(level).setDirty();
  }
  public static String safety(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.valid)return "Flying Bike matching overhead support unavailable";
    var rejection=FlyingBikeTrack.rejection(level,edge);if(rejection!=null)return rejection;
    if(!FamilyControllers.find(parent).equals(Set.of(v.family)))return "Flying Bike controller unavailable";
    if(!ConsistRules.eligible(level,parent,v))return "Flying Bike requires one original unlinked car";
    var blocks=parent.getPlot().getEmbeddedLevelAccessor();
    for(var seat:FlyingBikeVehicle.SEATS)if(!blocks.getBlockState(seat).is(FlyingBikeContent.SEAT.get()))
      return "Flying Bike seat/frame unavailable";
    for(var part:FlyingBikeVehicle.FRAME)if(!blocks.getBlockState(part).is(FlyingBikeContent.FRAME.get()))
      return "Flying Bike seat/frame unavailable";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "Flying Bike running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return "Flying Bike track position unavailable";
    var velocity=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d());
    double speed=velocity.length();if(speed>12.05)return "Flying Bike speed exceeds 12 blocks/s";
    var table=TrackGeometry.table(level,edge);double at=table.distanceAt(hit.t()),look=speed*speed/12+2.50;
    for(double s=Math.max(0,at-look);s<=Math.min(table.length(),at+look);s+=.5){
      double t=table.parameterAt(s);var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var obstacle=FlyingBikeTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(obstacle!=null)
        return "Flying Bike occupied sweep obstructed at "+obstacle.toShortString()+" ("+level.getBlockState(obstacle).getBlock().getDescriptionId()+")";
    }
    var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    double signed=velocity.dot(basis.forward().x,basis.forward().y,basis.forward().z);
    double remaining=signed>=0?table.length()-at:at;
    if(Math.abs(signed)>.01&&remaining<=look){
      var endpoint=signed>0?edge.to():edge.from();
      var next=dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r!=rail&&(r.from.equals(endpoint)||r.to.equals(endpoint))).toList();
      if(next.size()!=1)return "Flying Bike end or ambiguous route; stop and inspect";
      var following=TrackGeometry.resolve(level,next.getFirst());
      if(following==null||FlyingBikeTrack.rejection(level,following)!=null)return "Flying Bike incompatible route ahead";
      var nextTable=TrackGeometry.table(level,following);
      for(double s=0;s<=Math.min(look-remaining,nextTable.length());s+=.5){
        double t=nextTable.parameterAt(following.from().equals(endpoint)?s:nextTable.length()-s);
        if(FlyingBikeTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,following,t),following,t))!=null)
          return "Flying Bike occupied sweep across connection obstructed";
      }
    }
    if(v.flyingBike.governor!=null){var g=governor(level,v);if(g==null)return "Flying Bike governor unavailable";
      var reason=g.availability();if(reason!=null)return reason;
      var span=ChainLiftSpanQuery.findAtGraphHit(level,hit,0);
      if(span!=null&&g.spanAt(hit)==0)return "Flying Bike entered an unowned Chain span";
    }else if(!v.stopping)return "Bind both Flying Bike launches before dispatch";
    return null;
  }
  public static FlyingBikeGovernorEntity governor(ServerLevel l,FoundationData.Vehicle v){
    var p=v.flyingBike.governor;return p!=null&&l.hasChunkAt(p)&&l.getBlockEntity(p) instanceof FlyingBikeGovernorEntity g&&g.authorizesRide(v.chassis)?g:null;
  }
  public static void acknowledge(ServerLevel l,FoundationData.Vehicle v){
    v.stopping=true;v.carriers.forEach(c->c.capture=true);
    var g=governor(l,v);if(g!=null){g.cut();if(g.availability()==null)g.acknowledge(v);}
    var parent=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(parent instanceof ServerSubLevel b&&safety(l,b,v)==null&&!v.alignmentRequired){v.fault="";v.flyingBike.resumeRequired=false;}
    v.flyingBike.phase="BRAKING";
  }
  public static void validateDispatch(ServerLevel l,FoundationData.Vehicle v){
    var parent=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(evacuation(v)||!(parent instanceof ServerSubLevel b)||!ready(l,b,v)||!v.restraints||v.maintenance||v.flyingBike.resumeRequired)
      throw new IllegalArgumentException("Capture at the level Flying Bike dock, close restraints and explicitly clear recovery before dispatch");
    var g=governor(l,v);if(g==null)throw new IllegalArgumentException("Bind two powered Flying Bike launch governors");
    if(!g.authorityId().equals(v.authority.owner)||!g.getBlockPos().equals(v.authority.controller)
        ||!dev.fourthdimension.integration.RideAuthority.NATIVE_CHAIN.equals(v.authority.provider))
      throw new IllegalArgumentException("Flying Bike coordinator must own this car through native Chain authority");
    g.validateRelease(l,v);
  }
  public static void start(ServerLevel l,FoundationData.Vehicle v){
    validateDispatch(l,v);var g=governor(l,v);
    if(dev.fourthdimension.integration.RideAuthority.holds(l,v))g.release(v);
    v.flyingBike.phase="LAUNCH_1";v.flyingBike.launch=0;v.flyingBike.routeHash=validateRoute(l,v,g);
    v.stopping=false;v.carriers.forEach(c->c.capture=false);v.revision++;FoundationData.get(l).setDirty();
  }
  public static void resumeToDock(ServerLevel l,FoundationData.Vehicle v){
    var parent=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    if(evacuation(v)||!(parent instanceof ServerSubLevel b)||!v.stopping||!v.restraints||v.maintenance||v.alignmentRequired
        ||SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).length()>.05)
      throw new IllegalArgumentException("Stop and retain the Flying Bike before explicit recovery");
    var reason=safety(l,b,v);if(reason!=null)throw new IllegalArgumentException(reason);
    var g=governor(l,v);if(g==null)throw new IllegalArgumentException("Restore both launch governors");
    acknowledge(l,v);
    if(!v.fault.isEmpty())throw new IllegalArgumentException(v.fault);
    g.release(v);v.flyingBike.resumeRequired=false;v.flyingBike.phase="RECOVERY";
    v.fault="";v.stopping=false;v.carriers.forEach(c->c.capture=false);v.revision++;FoundationData.get(l).setDirty();
  }
  /** Finite graph proof: one unambiguous closed original route, no hidden powered span. */
  public static long validateRoute(ServerLevel l,FoundationData.Vehicle v,FlyingBikeGovernorEntity g){
    var first=dev.fourthdimension.moving.MovingRouteContext.start(l,v);if(first==null)first=rail(l,v);if(first==null)throw new IllegalArgumentException("Flying Bike route unavailable");
    var seen=new HashSet<FoundationData.Rail>();var current=first;var entry=first.from;var start=entry;
    double total=0;long hash=1;var spans=new HashSet<Integer>();
    while(true){
      if(!seen.add(current)||seen.size()>32)throw new IllegalArgumentException("Flying Bike route must be a simple cycle of at most 32 sections");
      var edge=TrackGeometry.resolve(l,current);if(edge==null||FlyingBikeTrack.rejection(l,edge)!=null)throw new IllegalArgumentException("Flying Bike route track unavailable");
      var table=TrackGeometry.table(l,edge);total+=table.length();if(total>1024)throw new IllegalArgumentException("Flying Bike route exceeds 1024 blocks");
      hash=31*hash+current.geometryHash;
      for(double distance=0;distance<=table.length();distance+=2){
        double t=table.parameterAt(distance);var point=CoasterPathGraphManager.sampleEdgePosition(l,edge,t);
        var hit=new CoasterPathTrackFrame.GraphHit(point,edge,t);
        var obstruction=FlyingBikeTrack.obstruction(l,hit);
        if(obstruction!=null)throw new IllegalArgumentException("Flying Bike route clearance is obstructed at "+obstruction.toShortString()+" on "+current.from.toShortString()+" -> "+current.to.toShortString());
        var span=ChainLiftSpanQuery.findAtGraphHit(l,hit,0);
        if(span!=null){int which=g.spanAt(hit);if(which==0)throw new IllegalArgumentException("Only the two bound launch spans may power this route");spans.add(which);
          requireStraightApproach(l,current,entry);
          var basis=CoasterPathTrackFrame.trackBasisAtHit(l,hit);if(basis.up().y<.999||Math.abs(basis.forward().y)>.001)throw new IllegalArgumentException("Flying Bike launches must be upright and level");}
      }
      var exit=current.from.equals(entry)?current.to:current.from;
      var previous=current;var next=dev.fourthdimension.moving.MovingRouteContext.rails(l).stream().filter(r->r!=previous && (r.from.equals(exit)||r.to.equals(exit))).toList();
      if(next.size()!=1)throw new IllegalArgumentException("Flying Bike route branches or ends");
      if(next.getFirst()==first){if(!exit.equals(start))throw new IllegalArgumentException("Flying Bike route is not closed");break;}
      entry=exit;current=next.getFirst();
    }
    if(!spans.equals(Set.of(1,2)))throw new IllegalArgumentException("Route must visit both bound launch spans");
    return hash;
  }
  private static CoasterPathTrackFrame.GraphHit graphSample(ServerLevel level,CoasterPathEdge edge,double distance){
    double t=TrackGeometry.table(level,edge).parameterAt(distance);
    return new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,edge,t),edge,t);
  }
  /** Native cart collision bounds need an aligned run-in before an anchor's transverse drive shaft. */
  private static void requireStraightApproach(ServerLevel level,FoundationData.Rail launch,BlockPos entry){
    var approaches=dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r!=launch&&(r.from.equals(entry)||r.to.equals(entry))).toList();
    if(approaches.size()!=1)throw new IllegalArgumentException("Each Flying Bike launch needs one unpowered upright straight approach of at least eight blocks");
    var prior=approaches.getFirst();var edge=TrackGeometry.resolve(level,prior);
    if(edge==null)throw new IllegalArgumentException("Flying Bike approach support unavailable");
    var table=TrackGeometry.table(level,edge);
    if(table.length()<8-.001)throw new IllegalArgumentException("Flying Bike launch approach must be at least eight blocks");
    var launchEdge=TrackGeometry.resolve(level,launch);
    var launchHit=graphSample(level,launchEdge,entry.equals(launch.from)?0:TrackGeometry.table(level,launchEdge).length());
    var direction=CoasterPathTrackFrame.trackBasisAtHit(level,launchHit).forward();
    for(double d=0;d<=8;d+=1){
      double at=prior.to.equals(entry)?table.length()-d:d;
      var hit=graphSample(level,edge,at);var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
      if(basis.up().y<.999||Math.abs(basis.forward().y)>.001||Math.abs(basis.forward().dot(direction))<.9999
          ||ChainLiftSpanQuery.findAtGraphHit(level,hit,0)!=null)
        throw new IllegalArgumentException("Flying Bike launch requires eight unpowered upright straight blocks before its drive shaft");
    }
  }
  public static void tick(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v,double dt){
    if(!v.flyingBike.initialized){v.flyingBike.initialized=true;if(!v.stopping)fault(level,v,"Flying Bike reload hold; explicitly inspect and resume");}
    if(!Objects.equals(CHECKED.put(parent,level.getGameTime()),level.getGameTime())){
      if(v.stopping&&SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()<=.05
          &&SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getAngularVelocity(parent,new Vector3d()).length()<=.01)
        v.flyingBike.evacuationStoppedTicks=Math.min(5,v.flyingBike.evacuationStoppedTicks+1);
      else v.flyingBike.evacuationStoppedTicks=0;
      var support=rail(level,v);
      if(support==null||!support.valid||TrackGeometry.resolve(level,support)==null){
        fault(level,v,VehicleOperations.MATCHING_SUPPORT_LOST);
        v.alignmentRequired=true;
      }else if(evacuation(v)){if(!evacuationReady(level,parent,v))fault(level,v,"Flying Bike evacuation support or exit unavailable");}
      else{var reason=safety(level,parent,v);if(reason!=null)fault(level,v,reason);}
    }
    if(!v.stopping&&!v.maintenance&&v.fault.isEmpty()){
      try{
        var g=governor(level,v);if(g==null)throw new IllegalArgumentException("Flying Bike governor unavailable");
        var r=rail(level,v);var edge=r==null?null:TrackGeometry.resolve(level,r);var center=CoasterCartResolve.cartCenterWorld(parent);
        var hit=edge==null||center==null?null:CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
        int span=g.spanAt(hit);var s=v.flyingBike;
        if(span==1&&s.launch==2){acknowledge(level,v);s.phase="BRAKING";}
        else{
          if(span==1){s.launch=Math.max(1,s.launch);s.phase="LAUNCH_1";}
          else if(span==2){if(s.launch==0)throw new IllegalArgumentException("Flying Bike skipped its first launch");s.launch=2;s.phase="LAUNCH_2";}
          else s.phase=s.launch==2?"COAST_2":"COAST_1";
          g.drive(v,span,SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length());
        }
      }catch(IllegalArgumentException failure){fault(level,v,failure.getMessage());}
    }
    if(v.stopping||v.maintenance){var g=governor(level,v);if(g!=null)g.cut();VehicleOperations.brake(level,v,dt);}
    if(v.stopping&&!evacuation(v)&&ready(level,parent,v))v.flyingBike.phase="DOCK";
    NativeLoading.update(parent,v,v.carriers.getFirst());v.boardingReady=ready(level,parent,v);
  }
}
