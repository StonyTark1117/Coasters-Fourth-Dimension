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
public final class StandingRuntime {
  private static final Map<ServerSubLevel,Long> CHECKED=new WeakHashMap<>();
  private static final Map<ServerSubLevel,GenericConstraintHandle> HOLDS=new WeakHashMap<>();
  private StandingRuntime(){}
  public static FoundationData.Rail rail(ServerLevel level,FoundationData.Vehicle v){
    return FoundationData.get(level).rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
  }
  public static void fault(ServerLevel level,FoundationData.Vehicle v,String reason){
    var ids=new HashSet<>(v.fixedConsist);ids.add(v.chassis);
    var parent=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
    if(parent!=null)ids.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent));
    for(var id:ids){var car=FoundationData.get(level).vehicles.get(id);if(car==null||!StandingVehicle.is(car))continue;
      if(!car.standing.interrupted)car.standing.heldTravel=Math.clamp(car.standing.travel,0,.4);car.standing.interrupted=true;car.stopping=true;car.restraints=true;car.fault=reason;car.carriers.forEach(c->c.capture=true);
    }
  }
  private static java.util.Map<BlockPos,Integer> driveDirections(ServerLevel level,FoundationData.Vehicle v){
    var result=new java.util.HashMap<BlockPos,Integer>();var visited=new java.util.HashSet<BlockPos>();
    var queue=new java.util.ArrayDeque<BlockPos>();if(v.boundFrom!=null)queue.add(v.boundFrom);
    var rails=FoundationData.get(level).rails;
    while(!queue.isEmpty()){
      var p=queue.remove();if(!visited.add(p))continue;
      if(!level.hasChunkAt(p))throw new IllegalArgumentException("Load the complete Standing route before direction validation");
      if(level.getBlockEntity(p) instanceof dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity a&&a.hasChainLiftSpan()){
        int sign=(int)Math.signum(a.getSpeed());
        if(sign==0)throw new IllegalArgumentException("Restore Standing Chain power before validating direction");
        result.put(p.immutable(),sign);
      }
      for(var r:rails)if(r.family==dev.fourthdimension.contract.Family.STANDING&&r.valid){
        if(r.from.equals(p))queue.add(r.to);else if(r.to.equals(p))queue.add(r.from);
      }
    }
    return result;
  }
  public static void rememberConsist(ServerLevel level,FoundationData.Vehicle v){
    var directions=driveDirections(level,v);v.standing.directions.clear();v.standing.directions.putAll(directions);v.standing.interrupted=false;
    v.fixedConsist=List.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(SubLevelContainer.getContainer(level).getSubLevel(v.chassis)));
  }
  /** A clear dry standing exit on each side, outside the complete vehicle reservation. */
  private static boolean platforms(ServerLevel l,ServerSubLevel p){
    var center=CoasterCartResolve.cartCenterWorld(p);if(center==null)return false;var right=new Vector3d(1,0,0).rotate(p.logicalPose().orientation());
    for(int side:new int[]{-1,1}){double x=center.x+side*3.5*right.x,z=center.z+side*3.5*right.z;boolean safe=false;
      for(int dy=-1;dy<=1;dy++){var floor=BlockPos.containing(x,center.y+dy,z);double y=floor.getY()+1;if(y-center.y<.45||y-center.y>1.5)continue;
        if(l.hasChunkAt(floor)&&l.getBlockState(floor).isFaceSturdy(l,floor,net.minecraft.core.Direction.UP)&&l.getFluidState(floor.above()).isEmpty()&&l.getFluidState(floor.above(2)).isEmpty()&&l.noCollision(new net.minecraft.world.phys.AABB(x-.31,y,z-.31,x+.31,y+1.9,z+.31)))safe=true;
      }if(!safe)return false;
    }return true;
  }
  private static boolean localDock(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    if(v.dock==null||v.fixedDockFrom==null||v.fixedDockTo==null||!v.stopping||v.alignmentRequired||!v.fault.isEmpty()
        ||!level.hasChunkAt(v.dock)||!(level.getBlockEntity(v.dock) instanceof BoardingStationEntity station))return false;
    var cars=VehicleOperations.consist(level,v);
    if(cars.stream().noneMatch(car->car.chassis.equals(station.vehicle)))return false;
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.matches(v.fixedDockFrom,v.fixedDockTo)||rail.geometryHash!=v.fixedDockGeometry
        ||StandingTrack.rejection(level,edge,StandingVehicle.design(v))!=null||TrackGeometry.table(level,edge).length()<24)return false;
    var center=CoasterCartResolve.cartCenterWorld(parent);
    if(center==null||center.distanceSquared(new Vector3d(v.dock.getX()+.5,v.dock.getY()+.5,v.dock.getZ()+.5))>1600
        ||new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y<.999
        ||SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.05)return false;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    double at=TrackGeometry.table(level,edge).distanceAt(hit.t());
    return at>=1.75&&at<=TrackGeometry.table(level,edge).length()-1.75&&StandingTrack.obstruction(level,hit)==null&&platforms(level,parent);
  }
  public static boolean ready(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    try{
      if(!ConsistRules.eligible(level,parent,v))return false;
      var container=SubLevelContainer.getContainer(level);
      for(var car:VehicleOperations.consist(level,v)){
        if(!StandingVehicle.is(car)||!(container.getSubLevel(car.chassis) instanceof ServerSubLevel body)
            ||!car.carriers.getFirst().capture||!car.carriers.getFirst().latched||Math.abs(car.standing.travel)>.015||Math.abs(car.standing.velocity)>.03||car.standing.interrupted||!localDock(level,body,car))return false;
      }return true;
    }catch(IllegalArgumentException unavailable){return false;}
  }
  public static void rebindDock(ServerLevel level,FoundationData.Vehicle selected,BlockPos pos){
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    if(cars.size()!=1)throw new IllegalArgumentException("Standing supports exactly one cars");
    for(var v:cars){
      var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
      if(!StandingVehicle.is(v)||!v.stopping||v.alignmentRequired||v.maintenance
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body)||edge==null
          ||StandingTrack.rejection(level,edge,StandingVehicle.design(v))!=null||TrackGeometry.table(level,edge).length()<24
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05
          ||new Vector3d(0,1,0).rotate(body.logicalPose().orientation()).y<.999
          ||!v.restraints&&!v.carriers.getFirst().occupants.isEmpty())
        throw new IllegalArgumentException("Stop and retain every car on the level 24-block dock before binding");
      var center=CoasterCartResolve.cartCenterWorld(body);
      if(center==null||center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5))>1600)
        throw new IllegalArgumentException("Bring every car to the declared dock");
      if(!platforms(level,body))throw new IllegalArgumentException("Provide dry clear standing platforms on both sides of the dock");
    }
    for(var v:cars){var rail=rail(level,v);v.dock=pos.immutable();v.fixedDockFrom=rail.from;v.fixedDockTo=rail.to;
      v.fixedDockGeometry=rail.geometryHash;v.fixedConsist=List.of();
      if(v.fault.startsWith("Standing coupler/consist changed"))v.fault="";v.revision++;
    }FoundationData.get(level).setDirty();
  }
  public static String safety(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v){
    if(!v.stopping){
      for(var accepted:v.standing.directions.entrySet()){
        if(!level.hasChunkAt(accepted.getKey())||!(level.getBlockEntity(accepted.getKey()) instanceof dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity a)
            ||!a.hasChainLiftSpan()||(int)Math.signum(a.getSpeed())!=accepted.getValue())
          return "Standing drive lost or direction changed; stop and explicitly Resume after validation";
      }
    }
    var rail=rail(level,v);var edge=rail==null?null:TrackGeometry.resolve(level,rail);
    if(edge==null||!rail.valid)return "Standing matching standing support unavailable";
    var rejection=StandingTrack.rejection(level,edge,StandingVehicle.design(v));if(rejection!=null)return rejection;
    if(!FamilyControllers.find(parent).equals(Set.of(v.family)))return "Standing controller unavailable";
    if(!ConsistRules.eligible(level,parent,v))return "Standing requires exactly one identical supported cars";
    if(!v.fixedConsist.isEmpty()&&!new HashSet<>(v.fixedConsist).equals(new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent))))
      return "Standing coupler/consist changed; stop and rebind at dock";
    var raw=SubLevelContainer.getContainer(level).getSubLevel(v.carriers.getFirst().body);
    if(raw==null)return "Standing carriage unavailable";
    var blocks=raw.getPlot().getEmbeddedLevelAccessor();
    if(StandingVehicle.surf(v)&&!blocks.getBlockState(StandingVehicle.CARRIAGE).is(StandingContent.CARRIAGE.get()))return "Surf carriage crossmember unavailable";
    for(var seat:StandingVehicle.SEATS)if(!blocks.getBlockState(seat).is(StandingContent.SEAT.get()))
      return "Standing seat/frame unavailable";
    for(var part:StandingVehicle.FRAME)if(!parent.getPlot().getEmbeddedLevelAccessor().getBlockState(part).is(StandingContent.FRAME.get()))
      return "Standing seat/frame unavailable";
    var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)return "Standing running gear unavailable";
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return "Standing track position unavailable";
    var velocity=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d());
    double speed=velocity.length();if(speed>12.05)return "Standing speed exceeds 12 blocks/s";
    var table=TrackGeometry.table(level,edge);double at=table.distanceAt(hit.t()),look=speed*speed/12+1.75;
    for(double s=Math.max(0,at-look);s<=Math.min(table.length(),at+look);s+=.5){
      double t=table.parameterAt(s);var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var obstacle=StandingTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(obstacle!=null)
        return "Standing occupied sweep obstructed at "+obstacle.toShortString()+" ("+level.getBlockState(obstacle).getBlock().getDescriptionId()+")";
    }
    var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    double signed=velocity.dot(basis.forward().x,basis.forward().y,basis.forward().z);
    double remaining=signed>=0?table.length()-at:at;
    if(Math.abs(signed)>.01&&remaining<=look){
      var endpoint=signed>0?edge.to():edge.from();
      var next=FoundationData.get(level).rails.stream().filter(r->r!=rail&&(r.from.equals(endpoint)||r.to.equals(endpoint))).toList();
      if(next.size()!=1)return "Standing end or ambiguous route; stop and inspect";
      var following=TrackGeometry.resolve(level,next.getFirst());
      if(following==null||StandingTrack.rejection(level,following,StandingVehicle.design(v))!=null)return "Standing incompatible route ahead";
      var nextTable=TrackGeometry.table(level,following);
      for(double s=0;s<=Math.min(look-remaining,nextTable.length());s+=.5){
        double t=nextTable.parameterAt(following.from().equals(endpoint)?s:nextTable.length()-s);
        if(StandingTrack.obstruction(level,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,following,t),following,t))!=null)
          return "Standing occupied sweep across connection obstructed";
      }
    }
    var span=ChainLiftSpanQuery.findAtGraphHit(level,hit,0);
    if(span!=null&&Math.abs(ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16))<.001)
      return "Standing Chain power lost; restore and explicitly Resume to dock";
    return null;
  }
  public static void acknowledge(ServerLevel level,FoundationData.Vehicle selected){
    var container=SubLevelContainer.getContainer(level);
    for(var v:VehicleOperations.consist(level,selected)){
      if(container.getSubLevel(v.chassis) instanceof ServerSubLevel body&&safety(level,body,v)==null&&!v.alignmentRequired){v.fault="";if(localDock(level,body,v)){v.standing.interrupted=false;var m=MOTION.get(body);if(m!=null){remove(m.latch);m.latch=null;}}}
    }
  }
  public static void resumeToDock(ServerLevel level,FoundationData.Vehicle selected){
    var cars=VehicleOperations.consist(level,selected);var container=SubLevelContainer.getContainer(level);
    for(var v:cars){
      if(!v.stopping||!v.restraints||v.maintenance||v.alignmentRequired
          ||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body)
          ||container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>.05)
        throw new IllegalArgumentException("Stop and retain every Standing car before Resume");
      var reason=safety(level,body,v);if(reason!=null)throw new IllegalArgumentException(reason);
    }
    for(var v:cars){rememberConsist(level,v);v.fault="";v.standing.interrupted=false;v.stopping=false;v.carriers.getFirst().capture=false;v.carriers.getFirst().latched=false;v.revision++;}
    FoundationData.get(level).setDirty();
  }
  /** Native guide owns child-relative motion; a travel brake must not erase the Surf return velocity. */
  private static void brakeParent(ServerLevel level,ServerSubLevel parent,double dt){
    var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    var velocity=pipeline.getLinearVelocity(parent,new Vector3d());double speed=velocity.length();
    if(speed>0)pipeline.addLinearAndAngularVelocity(parent,velocity.mul(-Math.min(1,6*dt/speed)),new Vector3d());
  }
  public static String captureDiagnostic(ServerLevel level,FoundationData.Vehicle v){
    var p=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis);var m=MOTION.get(p);
    return "home="+localDock(level,p,v)+" parentSpeed="+SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()+" q="+v.standing.travel+" qdot="+v.standing.velocity+" latch="+(m!=null&&m.latch!=null&&m.latch.isValid())+" latchAt="+(m==null?0:m.latchAt)+" nativeQdot="+(m==null?0:m.nativeSpeed)+" sampleDt="+(m==null?0:m.sampleDt)+" acceleration="+(m==null?0:m.acceleration)+" gravity="+(m==null?0:m.gravity);
  }
  public static String dockDiagnostic(ServerLevel level,FoundationData.Vehicle v){
    var body=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);if(!(body instanceof ServerSubLevel parent))return "unloaded";
    var r=rail(level,v);var edge=r==null?null:TrackGeometry.resolve(level,r);var center=CoasterCartResolve.cartCenterWorld(parent);
    var hit=edge==null||center==null?null:CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    return captureDiagnostic(level,v)+" callbacks="+dev.fourthdimension.runtime.CarrierRuntime.vehicleUpdates
      +" binding="+(r!=null&&r.matches(v.fixedDockFrom,v.fixedDockTo))+" savedHash="+v.fixedDockGeometry+" railHash="+(r==null?0:r.geometryHash)
      +" station="+(v.dock==null?"none":level.getBlockEntity(v.dock) instanceof BoardingStationEntity station?station.vehicle:"missing")
      +" platforms="+platforms(level,parent)+" up="+new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y
      +" rejection="+(edge==null?"edge missing":StandingTrack.rejection(level,edge,StandingVehicle.design(v)))
      +" obstruction="+(hit==null?"hit missing":StandingTrack.obstruction(level,hit))+" at="+(hit==null?0:TrackGeometry.table(level,edge).distanceAt(hit.t()));
  }
  private static final class Motion { GenericConstraintHandle guide,latch; boolean initialized,retained,measured; double latchAt,acceleration,gravity,previousQ,measuredSpeed,nativeSpeed,sampleDt; }
  private static final Map<ServerSubLevel,Motion> MOTION=new WeakHashMap<>();
  private static Vector3d anchor(ServerSubLevel body){var p=body.getPlot().getCenterBlock();return new Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5);}
  private static GenericConstraintHandle joint(ServerSubLevel p,ServerSubLevel child,boolean latch,double q){
    var a=anchor(p);a.y+=q;var axes=EnumSet.allOf(ConstraintJointAxis.class);if(!latch)axes.remove(ConstraintJointAxis.LINEAR_Y);
    var j=SubLevelContainer.getContainer(p.getLevel()).physicsSystem().getPipeline().addConstraint(p,child,new GenericConstraintConfiguration(a,anchor(child),new Quaterniond(),new Quaterniond(),axes));
    if(j==null)throw new IllegalStateException("Standing native joint unavailable");if(!latch)j.setLimit(ConstraintJointAxis.LINEAR_Y,0,.4);j.setContactsEnabled(false);return j;
  }
  /** Snapshot measurements from the native body poses, including the final save instant. */
  public static void refresh(ServerLevel level,FoundationData.Vehicle v){
    if(!StandingVehicle.surf(v)){v.standing.travel=0;v.standing.velocity=0;return;}
    var container=SubLevelContainer.getContainer(level);if(!(container.getSubLevel(v.chassis) instanceof ServerSubLevel p)||!(container.getSubLevel(v.carriers.getFirst().body) instanceof ServerSubLevel c))return;
    var physics=container.physicsSystem();physics.updatePose(p);physics.updatePose(c);var pipeline=physics.getPipeline();var axis=new Vector3d(0,1,0).rotate(p.logicalPose().orientation());var pa=p.logicalPose().transformPosition(anchor(p));var ca=c.logicalPose().transformPosition(anchor(c));
    double q=new Vector3d(ca).sub(pa).dot(axis);
    var pv=pipeline.getAngularVelocity(p,new Vector3d()).cross(new Vector3d(pa).sub(p.logicalPose().position())).add(pipeline.getLinearVelocity(p,new Vector3d()));
    var cv=pipeline.getAngularVelocity(c,new Vector3d()).cross(new Vector3d(ca).sub(c.logicalPose().position())).add(pipeline.getLinearVelocity(c,new Vector3d()));
    double speed=cv.sub(pv).dot(axis);
    if(!Double.isFinite(q)||!Double.isFinite(speed)||q<-.04||q>.44||Math.abs(speed)>2)fault(level,v,"Surf physical carriage limit exceeded; retain and inspect");
    v.standing.travel=Double.isFinite(q)?Math.clamp(q,-.04,.44):v.standing.travel;
    var m=MOTION.get(p);if(m!=null)m.nativeSpeed=speed;
    v.standing.velocity=m!=null&&m.measured?m.measuredSpeed:Double.isFinite(speed)?Math.clamp(speed,-4,4):0;
  }
  private static void remove(GenericConstraintHandle j){if(j!=null&&j.isValid())j.remove();}
  public static void tick(ServerLevel level,ServerSubLevel parent,FoundationData.Vehicle v,double dt){
    if(!Double.isFinite(dt)||dt<=0||dt>.25)return;
    var container=SubLevelContainer.getContainer(level);var pipeline=container.physicsSystem().getPipeline();var c=v.carriers.getFirst();
    var raw=container.getSubLevel(c.body);if(!(raw instanceof ServerSubLevel child)){fault(level,v,"Standing carriage unavailable");brakeParent(level,parent,dt);return;}
    var m=MOTION.computeIfAbsent(parent,k->new Motion());
    if(!m.initialized){m.initialized=true;if(!v.stopping)fault(level,v,"Standing restored retained; inspect and explicitly Resume");}
    if(StandingVehicle.surf(v)){
      refresh(level,v);
      // Pre-physics point velocity includes the gravity phase of the previous solve.
      // Damping and capture use actual anchor displacement over the completed step.
      double measured=m.measured?(v.standing.travel-m.previousQ)/dt:0;
      if(!Double.isFinite(measured)||Math.abs(measured)>2)fault(level,v,"Surf physical carriage velocity exceeded; retain and inspect");
      m.measuredSpeed=Double.isFinite(measured)?Math.clamp(measured,-4,4):0;
      m.previousQ=v.standing.travel;m.sampleDt=dt;m.measured=true;v.standing.velocity=m.measuredSpeed;
      if(m.guide!=null&&!m.guide.isValid())fault(level,v,"Surf native guide lost; inspect and explicitly Resume");
      if(m.guide==null||!m.guide.isValid()){remove(m.latch);m.latch=null;m.guide=joint(parent,child,false,0);}
    }else{v.standing.travel=0;v.standing.velocity=0;}
    if(!Objects.equals(CHECKED.put(parent,level.getGameTime()),level.getGameTime())){
      var support=rail(level,v);if(support==null||!support.valid||TrackGeometry.resolve(level,support)==null){fault(level,v,VehicleOperations.MATCHING_SUPPORT_LOST);v.alignmentRequired=true;}else{var reason=safety(level,parent,v);if(reason!=null)fault(level,v,reason);}
    }
    boolean retained=v.stopping||v.maintenance;
    var hold=HOLDS.get(parent);if(!retained&&hold!=null){remove(hold);HOLDS.remove(parent);hold=null;}
    if(retained){brakeParent(level,parent,dt);if(v.restraints&&(hold==null||!hold.isValid())&&pipeline.getLinearVelocity(parent,new Vector3d()).length()<=.15){var a=new Vector3d(parent.getMassTracker().getCenterOfMass());HOLDS.put(parent,pipeline.addConstraint(null,parent,new GenericConstraintConfiguration(parent.logicalPose().transformPosition(new Vector3d(a)),a,new Quaterniond(parent.logicalPose().orientation()),new Quaterniond(),EnumSet.allOf(ConstraintJointAxis.class))));}}
    if(StandingVehicle.surf(v)){
      boolean home=retained&&!v.standing.interrupted&&localDock(level,parent,v);double q=v.standing.travel,qd=v.standing.velocity;
      // Once acquired, retain the native home latch while it resolves its small position error.
      // Readiness still requires settled measured travel/velocity; do not release on correction motion.
      boolean acquiredHome=m.latch!=null&&m.latch.isValid()&&Math.abs(m.latchAt)<.015;
      boolean latch=retained&&(!home||acquiredHome||Math.abs(q)<=.015&&Math.abs(qd)<=.03);
      if(latch){double target=home?0:Math.clamp(q,0,.4);if(m.latch==null||!m.latch.isValid()){m.latch=joint(parent,child,true,target);m.latchAt=target;v.standing.heldTravel=target;}c.latched=home&&Math.abs(m.latchAt)<.015;}
      else{remove(m.latch);m.latch=null;c.latched=false;var axis=new Vector3d(0,1,0).rotate(parent.logicalPose().orientation());double mass=child.getMassTracker().getMass();double gravity=dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(level).dot(axis);
        double acceleration=home?Math.clamp((Math.clamp(-q*3,-.25,.25)-qd)*10,-1.5,1.5):Math.clamp(64*(.20-q)-10*qd,-6,6);
        m.acceleration=acceleration;m.gravity=gravity;var impulse=axis.mul(mass*(acceleration-gravity)*dt);pipeline.applyLinearAndAngularImpulse(child,impulse,new Vector3d(),true);pipeline.applyLinearAndAngularImpulse(parent,new Vector3d(impulse).negate(),new Vector3d(),true);
      }
    }else c.latched=retained;
    m.retained=retained;NativeLoading.update(child,v,c);v.boardingReady=ready(level,parent,v);
  }
}
