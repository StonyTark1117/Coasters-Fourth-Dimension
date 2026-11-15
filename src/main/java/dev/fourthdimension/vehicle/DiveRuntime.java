package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.physics.constraint.*;
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
import org.joml.Quaterniond;

/** Rigid native seats: train-wide hold, explicit level-dock capture and pose-preserving recovery. */
public final class DiveRuntime {
  private static final Map<ServerSubLevel,Long> CHECKED=new WeakHashMap<>();
  private static final Map<ServerSubLevel,GenericConstraintHandle> HOLDS=new WeakHashMap<>();
  private DiveRuntime(){}
  public static FoundationData.Rail rail(ServerLevel level,FoundationData.Vehicle v){
    return FoundationData.get(level).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
  }
  public static void fault(ServerLevel level,FoundationData.Vehicle v,String reason){
    var ids=new HashSet<>(v.fixedConsist);ids.add(v.chassis);
    var parent=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
    if(parent!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent));
    for(var id:ids){var car=FoundationData.get(level).vehicles.get(id);if(car==null||!DiveVehicle.is(car))continue;
      car.stopping=true;car.restraints=true;car.fault=reason;car.carriers.forEach(c->c.capture=true);
    }
  }
  public static void rememberConsist(ServerLevel level,FoundationData.Vehicle v){
    v.fixedConsist=List.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(SubLevelContainer.getContainer(level).getSubLevel(v.chassis)));
    v.dive.phase=DiveState.Phase.RUNNING;v.dive.needsResume=false;v.dive.restored=false;
  }
  private static boolean localDock(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    if(v.dock==null||v.fixedDockFrom==null||v.fixedDockTo==null||!v.stopping||v.alignmentRequired||!v.fault.isEmpty()
        ||!level.hasChunkAt(v.dock)||!(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station))return false;
    var cars=VehicleOperations.consist(level,v);
    if(cars.stream().noneMatch(car->car.chassis.equals(station.vehicle)))return false;
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.matches(v.fixedDockFrom,v.fixedDockTo)||rail.geometryHash!=v.fixedDockGeometry
        ||DiveTrack.rejection(level,edge)!=null||TrackGeometry.table(level,edge).length()<28)return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);
    if(center==null||center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))>1600
        ||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999
        ||SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    double at=TrackGeometry.table(level,edge).distanceAt(hit.t());
    return at>=1.75&&at<=TrackGeometry.table(level,edge).length()-1.75&&DiveTrack.obstruction(level,hit)==null;
  }
  public static boolean ready(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    try{
      if(!ConsistRules.eligible(level,parent,v))return false;
      var container=SubLevelContainer.getContainer(level);
      for(var car:VehicleOperations.consist(level,v)){
        if(!DiveVehicle.is(car)||!(container.getSubLevel(car.chassis) instanceof ServerSubLevel body)
            ||!car.carriers.getFirst().capture||!localDock(level,body,car))return false;
      }return true;
    }catch(IllegalArgumentException unavailable){return false;}
  }
  public static void rebindDock(ServerLevel level,FoundationData.Vehicle selected,BlockPos pos){
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    if(cars.size()>3)throw new IllegalArgumentException("Dive supports one to three cars");
    for(var v:cars){
      var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
      if(!DiveVehicle.is(v)||!v.stopping||v.alignmentRequired||v.maintenance
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body)||edge==null
          ||DiveTrack.rejection(level,edge)!=null||TrackGeometry.table(level,edge).length()<28
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05
          ||new Vector3d(0,1,0).rotate(body.logicalPose().orientation()).y<.999
          ||!v.restraints&&!v.carriers.getFirst().occupants.isEmpty())
        throw new IllegalArgumentException("Stop and retain every car on the level 28-block dock before binding");
      var center=CoasterCartResolve.cartCenterWorld(body);
      if(center==null||center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>1600)
        throw new IllegalArgumentException("Bring every car to the declared dock");
    }
    for(var v:cars){var rail=rail(level,v);v.dock=pos.immutable();v.fixedDockFrom=rail.from;v.fixedDockTo=rail.to;
      v.fixedDockGeometry=rail.geometryHash;v.fixedConsist=List.of();
      if(v.fault.startsWith("Dive coupler/consist changed"))v.fault="";v.revision++;
    }FoundationData.get(level).setDirty();
  }
  public static String safety(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.valid)return "Dive matching wide-row support unavailable";
    var rejection=DiveTrack.rejection(level,edge);if(rejection!=null)return rejection;
    if(!FamilyControllers.find(parent).equals(Set.of(v.family)))return "Dive controller unavailable";
    if(!ConsistRules.eligible(level,parent,v))return "Dive requires one to three identical supported cars";
    if(!v.fixedConsist.isEmpty()&&!new HashSet<>(v.fixedConsist).equals(new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent))))
      return "Dive coupler/consist changed; stop and rebind at dock";
    var blocks=parent.getPlot().getEmbeddedLevelAccessor();
    for(var seat:DiveVehicle.SEATS)if(!blocks.getBlockState(seat).is(DiveContent.SEAT.get()))
      return "Dive seat/frame unavailable";
    for(var part:DiveVehicle.FRAME)if(!blocks.getBlockState(part).is(DiveContent.FRAME.get()))
      return "Dive seat/frame unavailable";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "Dive running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return "Dive track position unavailable";
    var velocity=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d());
    double speed=velocity.length();if(speed>16.05)return "Dive speed exceeds 16 blocks/s";
    var table=TrackGeometry.table(level,edge);double at=table.distanceAt(hit.t()),look=speed*speed/12+1.75;
    for(double s=Math.max(0,at-look);s<=Math.min(table.length(),at+look);s+=.5){
      double t=table.parameterAt(s);var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var obstacle=DiveTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(obstacle!=null)
        return "Dive occupied sweep obstructed at "+obstacle.toShortString()+" ("+level.getBlockState(obstacle).getBlock().getDescriptionId()+")";
    }
    var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    double signed=velocity.dot(basis.forward().x,basis.forward().y,basis.forward().z);
    double remaining=signed>=0?table.length()-at:at;
    if(Math.abs(signed)>.01&&remaining<=look){
      var endpoint=signed>0?edge.to():edge.from();
      var next=FoundationData.get(level).rails.stream().filter(r->r!=rail&&(r.from.equals(endpoint)||r.to.equals(endpoint))).toList();
      if(next.size()!=1)return "Dive end or ambiguous route; stop and inspect";
      var following=TrackGeometry.resolve(level,next.getFirst());
      if(following==null||DiveTrack.rejection(level,following)!=null)return "Dive incompatible route ahead";
      var nextTable=TrackGeometry.table(level,following);
      for(double s=0;s<=Math.min(look-remaining,nextTable.length());s+=.5){
        double t=nextTable.parameterAt(following.from().equals(endpoint)?s:nextTable.length()-s);
        if(DiveTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,following,t),following,t))!=null)
          return "Dive occupied sweep across connection obstructed";
      }
    }
    return null;
  }
  public static void acknowledge(ServerLevel level,FoundationData.Vehicle selected){
    var container=SubLevelContainer.getContainer(level);
    for(var v:VehicleOperations.consist(level,selected)){
      if(v.dive.phase!=DiveState.Phase.DOCK_OPEN&&v.dive.phase!=DiveState.Phase.OPEN&&v.dive.phase!=DiveState.Phase.DOCK_CLOSED)
        v.dive.needsResume=true;
      if(container.getSubLevel(v.chassis) instanceof ServerSubLevel body&&safety(level,body,v)==null&&!v.alignmentRequired)v.fault="";
    }
  }
  public static void resumeToDock(ServerLevel level,FoundationData.Vehicle selected){resume(level,selected);}
  public static void tick(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v,double dt){
    if(!Objects.equals(CHECKED.put(parent,level.getGameTime()),level.getGameTime())){
      var support=rail(level,v);
      if(support==null||!support.valid||TrackGeometry.resolve(level,support)==null){
        fault(level,v,VehicleOperations.MATCHING_SUPPORT_LOST);
        for(var id:v.fixedConsist){var car=FoundationData.get(level).vehicles.get(id);if(car!=null)car.alignmentRequired=true;}
        v.alignmentRequired=true;
      }else{var reason=safety(level,parent,v);if(reason!=null)fault(level,v,reason);}
    }
    boolean retained=v.stopping||v.maintenance||(v.authority.owner!=null&&(v.authority.held||v.authority.suspended));
    var hold=HOLDS.get(parent);
    if(!retained&&hold!=null){if(hold.isValid())hold.remove();HOLDS.remove(parent);hold=null;}
    if(retained){
      VehicleOperations.brake(level,v,dt*2);
      var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
      // The shared brake decelerates travel. Once slow, a native stationary
      // support carries gravity; damping alone creeps on slopes and prevents
      // the <=.05 Resume guard from ever becoming true. Anchor the measured
      // pose, never a station target, and reconstruct it from that pose on load.
      if(v.restraints&&(hold==null||!hold.isValid())
          &&pipeline.getLinearVelocity(parent,new Vector3d()).length()<=.15){
        var anchor=new Vector3d(parent.getMassTracker().getCenterOfMass());
        var world=parent.logicalPose().transformPosition(new Vector3d(anchor));
        HOLDS.put(parent,pipeline.addConstraint(null,parent,new GenericConstraintConfiguration(
            world,anchor,new Quaterniond(parent.logicalPose().orientation()),new Quaterniond(),
            EnumSet.allOf(ConstraintJointAxis.class))));
      }
    }
    if(!retained&&(v.dive.phase==DiveState.Phase.DROP||v.dive.phase==DiveState.Phase.BRAKING)
        &&SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>15)
      VehicleOperations.brake(level,v,dt*2);
    NativeLoading.update(parent,v,v.carriers.getFirst());v.boardingReady=ready(level,parent,v);
  }
  private static List<FoundationData.Vehicle> cars(ServerLevel l,FoundationData.Vehicle v){return VehicleOperations.consist(l,v);}
  private static boolean acquired(ServerLevel l,List<FoundationData.Vehicle> cars){
    var c=SubLevelContainer.getContainer(l);
    for(var v:cars){var b=c.getSubLevel(v.chassis);var h=HOLDS.get(b);
      if(!(b instanceof ServerSubLevel body)||h==null||!h.isValid()||c.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05)return false;
    }return true;
  }
  public static boolean blocksChain(ServerLevel l,FoundationData.Vehicle v){
    if(v.dive.phase!=DiveState.Phase.DROP&&v.dive.phase!=DiveState.Phase.BRAKING)return false;
    try{return cars(l,v).stream().anyMatch(c->c.dive.from!=null&&Objects.equals(c.boundFrom,c.dive.from)&&Objects.equals(c.boundTo,c.dive.to));}
    catch(IllegalArgumentException missing){return true;}
  }
  private static DiveHoldEntity controller(ServerLevel l,FoundationData.Vehicle v){
    return v.dive.controller!=null&&l.hasChunkAt(v.dive.controller)&&l.getBlockEntity(v.dive.controller) instanceof DiveHoldEntity c?c:null;
  }
  public static void validateRelease(ServerLevel l,FoundationData.Vehicle v,DiveHoldEntity controller){
    var edge=controller.edge();
    if(!controller.powered()||edge==null||!DiveHoldEntity.holdShape(l,edge)||DiveTrack.rejection(l,edge)!=null)
      throw new IllegalArgumentException("Restore enabled original Dive hold section");
    if(v.dive.from!=null&&(!Objects.equals(v.dive.from,controller.from)||!Objects.equals(v.dive.to,controller.to)||v.dive.geometry!=controller.geometry))
      throw new IllegalArgumentException("Restore the original bound Dive hold geometry");
    var train=cars(l,v);if(train.size()>3||!acquired(l,train))throw new IllegalArgumentException("Acquire the complete Dive train physically before release");
    for(var car:train){
      var body=SubLevelContainer.getContainer(l).getSubLevel(car.chassis);
      if(!(body instanceof ServerSubLevel parent)||!car.restraints||car.maintenance||car.alignmentRequired)
        throw new IllegalArgumentException("Closed supported complete Dive train required");
      var reason=safety(l,parent,car);if(reason!=null)throw new IllegalArgumentException(reason);
      if(car.dive.phase!=DiveState.Phase.DROP&&car.dive.phase!=DiveState.Phase.BRAKING
          &&!Objects.equals(car.boundFrom,controller.from))throw new IllegalArgumentException("Every Dive car must fit on the hold section");
    }
    var next=FoundationData.get(l).rails.stream().filter(r->!r.matches(edge.from(),edge.to())&&(r.from.equals(edge.to())||r.to.equals(edge.to()))).toList();
    if(next.size()!=1)throw new IllegalArgumentException("Dive exit unavailable or ambiguous");
    var exit=TrackGeometry.resolve(l,next.getFirst());if(exit==null||DiveTrack.rejection(l,exit)!=null)throw new IllegalArgumentException("Dive exit must be matching supported track");
    // Finite local hold/exit clearance; this is not a park-wide route reservation.
    for(var e:List.of(edge,exit)){
      var table=TrackGeometry.table(l,e);
      for(double d=0;d<=table.length();d+=.5){double t=table.parameterAt(d);
        if(DiveTrack.obstruction(l,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(l,e,t),e,t))!=null)
          throw new IllegalArgumentException("Dive hold or exit envelope obstructed");
      }
    }
  }
  private static void suspend(ServerLevel l,FoundationData.Vehicle v,String reason){
    fault(l,v,reason);
    for(var car:cars(l,v)){car.dive.needsResume=true;car.dive.stoppedTicks=0;}
    dev.fourthdimension.integration.RideAuthority.emergencyHold(l,v);FoundationData.get(l).setDirty();
  }
  public static void resume(ServerLevel l,FoundationData.Vehicle v){
    var train=cars(l,v);var c=controller(l,v);
    if(!acquired(l,train))throw new IllegalArgumentException("Wait for the complete stopped physical hold before Resume");
    for(var car:train){var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(car.chassis);
      var reason=safety(l,b,car);if(reason!=null)throw new IllegalArgumentException(reason);
      if(!car.restraints||car.alignmentRequired||car.maintenance)throw new IllegalArgumentException("Retain and repair every Dive car before Resume");}
    if(c!=null){
      validateRelease(l,v,c);
      if(v.authority.owner!=null){dev.fourthdimension.integration.RideAuthority.holds(l,v);
        if(!c.authorityId().equals(v.authority.owner))dev.fourthdimension.integration.RideAuthority.transfer(l,v.authority.controller,v.chassis,dev.fourthdimension.integration.RideAuthority.token(v),c.getBlockPos());
        dev.fourthdimension.integration.RideAuthority.resume(l,c.getBlockPos(),v.chassis,dev.fourthdimension.integration.RideAuthority.token(v));}
    }
    for(var car:train){car.fault="";car.dive.needsResume=false;car.dive.restored=false;car.dive.stoppedTicks=0;}
    boolean held=v.dive.phase==DiveState.Phase.HELD||v.dive.phase==DiveState.Phase.RELEASE_ARMED||v.dive.phase==DiveState.Phase.HOLD_ACQUIRING;
    if(!held){
      // Post-release recovery retains DROP and waits five fresh stopped ticks before
      // releasing its emergency authority. No second dwell or release commit.
      if(v.authority.owner==null)for(var car:train){car.stopping=false;car.carriers.getFirst().capture=false;}
    }
    FoundationData.get(l).setDirty();
  }
  public static void serverTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event){
    if(!(event.getLevel() instanceof ServerLevel l))return;
    var done=new HashSet<UUID>();
    for(var v:List.copyOf(FoundationData.get(l).vehicles.values())){
      if(!DiveVehicle.is(v)||done.contains(v.chassis))continue;
      List<FoundationData.Vehicle> train;
      try{train=cars(l,v);}catch(IllegalArgumentException missing){fault(l,v,missing.getMessage());continue;}
      train.forEach(c->done.add(c.chassis));
      try{advance(l,v,train);}catch(IllegalArgumentException failure){suspend(l,v,failure.getMessage());}
    }
  }
  private static void advance(ServerLevel l,FoundationData.Vehicle v,List<FoundationData.Vehicle> train){
    var s=v.dive;var c=controller(l,v);
    if(train.stream().anyMatch(car->car.dive.restored)){
      train.forEach(car->car.dive.restored=false);
      if(s.phase!=DiveState.Phase.DOCK_OPEN&&s.phase!=DiveState.Phase.OPEN&&s.phase!=DiveState.Phase.DOCK_CLOSED){suspend(l,v,"Dive restore requires explicit Resume");return;}
    }
    if((s.phase==DiveState.Phase.DROP||s.phase==DiveState.Phase.BRAKING)&&train.stream().allMatch(car->car.boardingReady)){
      for(var car:train){car.dive.phase=DiveState.Phase.DOCK_CLOSED;car.dive.needsResume=false;}FoundationData.get(l).setDirty();return;
    }
    if(s.needsResume)return;
    if(s.phase==DiveState.Phase.RUNNING||s.phase==DiveState.Phase.APPROACH){
      if(c==null||!Objects.equals(v.boundFrom,c.from)||!Objects.equals(v.boundTo,c.to)){
        c=null;
        // A controller is discovered only on the currently occupied dedicated section.
        var rail=rail(l,v);if(rail!=null){var edge=TrackGeometry.resolve(l,rail);
          if(edge!=null&&DiveHoldEntity.holdShape(l,edge)){
            for(var p:BlockPos.betweenClosed(rail.from.offset(-12,-12,-12),rail.from.offset(12,12,12)))
              if(l.hasChunkAt(p)&&l.getBlockEntity(p) instanceof DiveHoldEntity candidate&&Objects.equals(candidate.from,rail.from)&&Objects.equals(candidate.to,rail.to)){c=candidate;break;}
            if(c==null)throw new IllegalArgumentException("Dive descending hold requires its bound controller");
          }
        }
      }
      if(c==null)return;
      var edge=c.edge();if(edge==null||!c.powered())throw new IllegalArgumentException("Dive hold controller or power lost");
      double front=0;var container=SubLevelContainer.getContainer(l);
      for(var car:train){
        car.dive.controller=c.getBlockPos();car.dive.from=c.from;car.dive.to=c.to;car.dive.geometry=c.geometry;car.dive.phase=DiveState.Phase.APPROACH;
        var b=container.getSubLevel(car.chassis);if(!(b instanceof ServerSubLevel body))throw new IllegalArgumentException("Dive body unavailable");
        if(container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>2.05)throw new IllegalArgumentException("Dive hold approach exceeds 2 blocks/s");
        if(Objects.equals(car.boundFrom,c.from)&&Objects.equals(car.boundTo,c.to)){
          var center=CoasterCartResolve.cartCenterWorld(body);var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
          front=Math.max(front,TrackGeometry.table(l,edge).distanceAt(hit.t()));
        }
      }
      if(front>=20){for(var car:train){car.stopping=true;car.carriers.getFirst().capture=true;car.dive.phase=DiveState.Phase.HOLD_ACQUIRING;car.dive.remaining=c.dwell;}FoundationData.get(l).setDirty();}
      return;
    }
    if(s.phase==DiveState.Phase.HOLD_ACQUIRING||s.phase==DiveState.Phase.HELD||s.phase==DiveState.Phase.RELEASE_ARMED){
      if(c==null||!c.powered())throw new IllegalArgumentException("Dive held power/controller loss; Resume required");
      if(!acquired(l,train)){train.forEach(car->car.dive.stoppedTicks=0);return;}
      for(var car:train)car.dive.stoppedTicks=Math.min(5,car.dive.stoppedTicks+1);
      if(s.stoppedTicks<5)return;
      validateRelease(l,v,c);
      if(s.phase==DiveState.Phase.HOLD_ACQUIRING){
        if(v.authority.owner==null)dev.fourthdimension.integration.RideAuthority.claim(l,c.getBlockPos(),v.chassis,dev.fourthdimension.integration.RideAuthority.NATIVE_CHAIN);
        else if(!c.authorityId().equals(v.authority.owner)){
          dev.fourthdimension.integration.RideAuthority.hold(l,v.authority.controller,v.chassis,dev.fourthdimension.integration.RideAuthority.token(v));
          dev.fourthdimension.integration.RideAuthority.transfer(l,v.authority.controller,v.chassis,dev.fourthdimension.integration.RideAuthority.token(v),c.getBlockPos());
        }else dev.fourthdimension.integration.RideAuthority.hold(l,c.getBlockPos(),v.chassis,dev.fourthdimension.integration.RideAuthority.token(v));
        train.forEach(car->car.dive.phase=DiveState.Phase.HELD);FoundationData.get(l).setDirty();return;
      }
      if(v.authority.suspended)throw new IllegalArgumentException("Dive authority suspended; Resume required");
      if(s.remaining>0){train.forEach(car->car.dive.remaining--);FoundationData.get(l).setDirty();}
      if(l.getGameTime()%10==0)for(var car:train)for(var seat:car.carriers)for(var id:seat.occupants){
        var player=l.getServer().getPlayerList().getPlayer(id);if(player!=null)player.displayClientMessage(net.minecraft.network.chat.Component.literal("Dive hold: "+s.remaining+" ticks"),true);
      }
      if(s.remaining==0){
        train.forEach(car->car.dive.phase=DiveState.Phase.RELEASE_ARMED);
        dev.fourthdimension.integration.RideAuthority.release(l,c.getBlockPos(),v.chassis,dev.fourthdimension.integration.RideAuthority.token(v));
        for(var car:train){car.dive.phase=DiveState.Phase.DROP;car.dive.releaseEpoch=car.authority.epoch;car.stopping=false;car.carriers.getFirst().capture=false;car.revision++;}
        FoundationData.get(l).setDirty(); // Commit DROP before the next physics callback removes the native constraints.
      }
      return;
    }
    if((s.phase==DiveState.Phase.DROP||s.phase==DiveState.Phase.BRAKING)&&v.authority.held&&!v.authority.suspended&&acquired(l,train)){
      if(++s.stoppedTicks>=5&&c!=null){dev.fourthdimension.integration.RideAuthority.release(l,c.getBlockPos(),v.chassis,dev.fourthdimension.integration.RideAuthority.token(v));
        for(var car:train){car.stopping=false;car.carriers.getFirst().capture=false;}}
    }
  }
}
