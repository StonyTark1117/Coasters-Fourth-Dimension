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
public final class FixedInvertedRuntime {
  private static final Map<ServerSubLevel,Long> CHECKED=new WeakHashMap<>();
  private FixedInvertedRuntime(){}
  public static FoundationData.Rail rail(ServerLevel level,FoundationData.Vehicle v){
    return FoundationData.get(level).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
  }
  public static void fault(ServerLevel level,FoundationData.Vehicle v,String reason){
    var ids=new HashSet<>(v.fixedConsist);ids.add(v.chassis);
    var parent=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
    if(parent!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent));
    for(var id:ids){var car=FoundationData.get(level).vehicles.get(id);if(car==null||!FixedInvertedVehicle.is(car))continue;
      car.stopping=true;car.restraints=true;car.fault=reason;car.carriers.forEach(c->c.capture=true);
    }
  }
  public static void rememberConsist(ServerLevel level,FoundationData.Vehicle v){
    v.fixedConsist=List.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(SubLevelContainer.getContainer(level).getSubLevel(v.chassis)));
  }
  private static boolean localDock(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    if(v.dock==null||v.fixedDockFrom==null||v.fixedDockTo==null||!v.stopping||v.alignmentRequired||!v.fault.isEmpty()
        ||!level.hasChunkAt(v.dock)||!(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station))return false;
    var cars=VehicleOperations.consist(level,v);
    if(cars.stream().noneMatch(car->car.chassis.equals(station.vehicle)))return false;
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.matches(v.fixedDockFrom,v.fixedDockTo)||rail.geometryHash!=v.fixedDockGeometry
        ||FixedInvertedTrack.rejection(level,edge)!=null||TrackGeometry.table(level,edge).length()<36)return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);
    if(center==null||center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))>1600
        ||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999
        ||SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    double at=TrackGeometry.table(level,edge).distanceAt(hit.t());
    return at>=1.75&&at<=TrackGeometry.table(level,edge).length()-1.75&&FixedInvertedTrack.obstruction(level,hit)==null;
  }
  public static boolean ready(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    try{
      if(!ConsistRules.eligible(level,parent,v))return false;
      var container=SubLevelContainer.getContainer(level);
      for(var car:VehicleOperations.consist(level,v)){
        if(!FixedInvertedVehicle.is(car)||!(container.getSubLevel(car.chassis) instanceof ServerSubLevel body)
            ||!car.carriers.getFirst().capture||!localDock(level,body,car))return false;
      }return true;
    }catch(IllegalArgumentException unavailable){return false;}
  }
  public static void rebindDock(ServerLevel level,FoundationData.Vehicle selected,BlockPos pos){
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    if(cars.size()>4)throw new IllegalArgumentException("Fixed Inverted supports one to four cars");
    for(var v:cars){
      var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
      if(!FixedInvertedVehicle.is(v)||!v.stopping||v.alignmentRequired||v.maintenance
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body)||edge==null
          ||FixedInvertedTrack.rejection(level,edge)!=null||TrackGeometry.table(level,edge).length()<36
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05
          ||new Vector3d(0,1,0).rotate(body.logicalPose().orientation()).y<.999
          ||!v.restraints&&!v.carriers.getFirst().occupants.isEmpty())
        throw new IllegalArgumentException("Stop and retain every car on the level 36-block dock before binding");
      var center=CoasterCartResolve.cartCenterWorld(body);
      if(center==null||center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>1600)
        throw new IllegalArgumentException("Bring every car to the declared dock");
    }
    for(var v:cars){var rail=rail(level,v);v.dock=pos.immutable();v.fixedDockFrom=rail.from;v.fixedDockTo=rail.to;
      v.fixedDockGeometry=rail.geometryHash;v.fixedConsist=List.of();
      if(v.fault.startsWith("Fixed Inverted coupler/consist changed"))v.fault="";v.revision++;
    }FoundationData.get(level).setDirty();
  }
  public static String safety(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.valid)return "Fixed Inverted matching overhead support unavailable";
    var rejection=FixedInvertedTrack.rejection(level,edge);if(rejection!=null)return rejection;
    if(!FamilyControllers.find(parent).equals(Set.of(v.family)))return "Fixed Inverted controller unavailable";
    if(!ConsistRules.eligible(level,parent,v))return "Fixed Inverted requires one to four identical supported cars";
    if(!v.fixedConsist.isEmpty()&&!new HashSet<>(v.fixedConsist).equals(new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent))))
      return "Fixed Inverted coupler/consist changed; stop and rebind at dock";
    var blocks=parent.getPlot().getEmbeddedLevelAccessor();
    for(var seat:FixedInvertedVehicle.SEATS)if(!blocks.getBlockState(seat).is(VehicleParts.FIXED_INVERTED_SEAT.get()))
      return "Fixed Inverted seat/frame unavailable";
    for(var part:FixedInvertedVehicle.FRAME)if(!blocks.getBlockState(part).is(VehicleParts.FIXED_INVERTED_FRAME.get()))
      return "Fixed Inverted seat/frame unavailable";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "Fixed Inverted running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return "Fixed Inverted track position unavailable";
    var velocity=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d());
    double speed=velocity.length();if(speed>16.05)return "Fixed Inverted speed exceeds 16 blocks/s";
    var table=TrackGeometry.table(level,edge);double at=table.distanceAt(hit.t()),look=speed*speed/12+1.75;
    for(double s=Math.max(0,at-look);s<=Math.min(table.length(),at+look);s+=.5){
      double t=table.parameterAt(s);var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var obstacle=FixedInvertedTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(obstacle!=null)
        return "Fixed Inverted occupied sweep obstructed at "+obstacle.toShortString()+" ("+level.getBlockState(obstacle).getBlock().getDescriptionId()+")";
    }
    var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    double signed=velocity.dot(basis.forward().x,basis.forward().y,basis.forward().z);
    double remaining=signed>=0?table.length()-at:at;
    if(Math.abs(signed)>.01&&remaining<=look){
      var endpoint=signed>0?edge.to():edge.from();
      var next=FoundationData.get(level).rails.stream().filter(r->r!=rail&&(r.from.equals(endpoint)||r.to.equals(endpoint))).toList();
      if(next.size()!=1)return "Fixed Inverted end or ambiguous route; stop and inspect";
      var following=TrackGeometry.resolve(level,next.getFirst());
      if(following==null||FixedInvertedTrack.rejection(level,following)!=null)return "Fixed Inverted incompatible route ahead";
      var nextTable=TrackGeometry.table(level,following);
      for(double s=0;s<=Math.min(look-remaining,nextTable.length());s+=.5){
        double t=nextTable.parameterAt(following.from().equals(endpoint)?s:nextTable.length()-s);
        if(FixedInvertedTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,following,t),following,t))!=null)
          return "Fixed Inverted occupied sweep across connection obstructed";
      }
    }
    var span=ChainLiftSpanQuery.findAtGraphHit(level,hit,0);
    if(span!=null&&Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16))<.001)
      return "Fixed Inverted Chain power lost; restore and explicitly Resume to dock";
    return null;
  }
  public static void acknowledge(ServerLevel level,FoundationData.Vehicle selected){
    var container=SubLevelContainer.getContainer(level);
    for(var v:VehicleOperations.consist(level,selected)){
      if(container.getSubLevel(v.chassis) instanceof ServerSubLevel body&&safety(level,body,v)==null&&!v.alignmentRequired)v.fault="";
    }
  }
  public static void resumeToDock(ServerLevel level,FoundationData.Vehicle selected){
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    for(var v:cars){
      if(!v.stopping||!v.restraints||v.maintenance||v.alignmentRequired
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body)
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05)
        throw new IllegalArgumentException("Stop and retain every Fixed Inverted car before Resume");
      var reason=safety(level,body,v);if(reason!=null)throw new IllegalArgumentException(reason);
    }
    for(var v:cars){rememberConsist(level,v);v.fault="";v.stopping=false;v.carriers.getFirst().capture=false;v.revision++;}
    FoundationData.get(level).setDirty();
  }
  public static void tick(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v,double dt){
    if(!Objects.equals(CHECKED.put(parent,level.getGameTime()),level.getGameTime())){
      var support=rail(level,v);
      if(support==null||!support.valid||TrackGeometry.resolve(level,support)==null){
        fault(level,v,VehicleOperations.MATCHING_SUPPORT_LOST);
        for(var id:v.fixedConsist){var car=FoundationData.get(level).vehicles.get(id);if(car!=null)car.alignmentRequired=true;}
        v.alignmentRequired=true;
      }else{var reason=safety(level,parent,v);if(reason!=null)fault(level,v,reason);}
    }
    if(v.stopping||v.maintenance)VehicleOperations.brake(level,v,dt);
    NativeLoading.update(parent,v,v.carriers.getFirst());v.boardingReady=ready(level,parent,v);
  }
}
