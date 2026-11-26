package dev.fourthdimension.moving;

import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Server-owned finite construction. Preview and validation never spend or mutate world state. */
public final class MovingConstruction {
  public record ConfigureRequest(String configuration,int length,BlockPos origin,Direction facing,MovingProgramSpec program){
    public ConfigureRequest {
      MovingCapabilityCatalog.Configuration.byId(configuration);
      if(length!=16&&length!=32&&length!=48)throw new IllegalArgumentException("Choose a 16, 32 or 48 block bed");
      origin=origin.immutable();Objects.requireNonNull(program);
      if(facing.getAxis().isVertical())throw new IllegalArgumentException("Choose a horizontal construction direction");
    }
    public CompoundTag save(){var t=new CompoundTag();t.putInt("version",1);t.putString("configuration",configuration);t.putInt("length",length);t.putLong("origin",origin.asLong());t.putString("facing",facing.getName());t.putString("mechanism",program.mechanism().id());t.putInt("amount",program.amount());t.putInt("from",program.fromPort());t.putInt("to",program.toPort());t.putBoolean("show",program.fullShowTurn());return t;}
    public static ConfigureRequest read(CompoundTag t){
      if(t==null||t.getInt("version")!=1)throw new IllegalArgumentException("Unknown moving construction version; refresh the panel");
      var facing=Direction.byName(t.getString("facing"));if(facing==null)throw new IllegalArgumentException("Choose a cardinal direction");
      return new ConfigureRequest(t.getString("configuration"),t.getInt("length"),BlockPos.of(t.getLong("origin")),facing,
        new MovingProgramSpec(MovingMechanism.byId(t.getString("mechanism")),t.getInt("amount"),t.getInt("from"),t.getInt("to"),t.getBoolean("show")));
    }
  }
  public record FixedPart(BlockPos pos,BlockState state){}
  private record Plan(MovingAssemblyState state,List<MovingNativeAdapter.BlockPlacement> moving,List<FixedPart> fixed,Map<Item,Integer> costs,List<AABB> sweep){}
  private MovingConstruction(){}
  public static ConfigureRequest defaults(BlockPos controller,MovingMechanism mechanism){
    int amount=switch(mechanism){case TRANSFER,DROP,SIDE_DROP,ELEVATOR->4;case TILT->30;case TEETER->1;case COMPOUND->15;case TURNTABLE->0;};
    return new ConfigureRequest("wing",16,controller.east(mechanism==MovingMechanism.TURNTABLE||mechanism==MovingMechanism.COMPOUND?20:14),Direction.SOUTH,new MovingProgramSpec(mechanism,amount,0,mechanism==MovingMechanism.TURNTABLE||mechanism==MovingMechanism.COMPOUND?90:0,false));
  }
  public static void access(ServerPlayer player,BlockPos controller){
    var level=player.serverLevel();
    if(!player.mayBuild()||!level.mayInteract(player,controller)||!level.hasChunkAt(controller)||player.distanceToSqr(Vec3.atCenterOf(controller))>64)
      throw new IllegalArgumentException("Stand within eight blocks of an editable loaded controller");
    if(!(level.getBlockState(controller).getBlock() instanceof MovingControllerBlock))throw new IllegalArgumentException("Moving-track controller is missing");
  }
  private static MovingAssemblyState.Pose compose(MovingAssemblyState.Pose origin,MovingProgram.Pose local){
    var qv=local.quaternion();var q=origin.orientation().mul(new Quaterniond(qv[0],qv[1],qv[2],qv[3]));
    var p=new Vector3d(local.x(),local.y(),local.z()).rotate(origin.orientation()).add(origin.position());
    return new MovingAssemblyState.Pose(p.x,p.y,p.z,q.x,q.y,q.z,q.w);
  }
  private static Vec3 transform(MovingAssemblyState.Pose pose,double x,double y,double z){var p=new Vector3d(x,y,z).rotate(pose.orientation()).add(pose.position());return new Vec3(p.x,p.y,p.z);}
  private static AABB transformBox(MovingAssemblyState.Pose pose,MovingProgram.Bounds b){
    AABB result=null;for(var p:b.corners()){var v=transform(pose,p.x(),p.y(),p.z());var box=new AABB(v,v);result=result==null?box:result.minmax(box);}return result;
  }
  private static BlockPos socket(MovingAssemblyState.Pose pose,int half){return BlockPos.containing(transform(pose,0,-.25,half));}
  private static TrackDesign design(MovingCapabilityCatalog.Configuration configuration){
    return configuration==MovingCapabilityCatalog.Configuration.WING||configuration==MovingCapabilityCatalog.Configuration.NATIVE_SERVICE?null:TrackDesign.valueOf(configuration.name());
  }
  private static BlockState part(MovingPartBlock.Kind kind){return MovingContent.PARTS.get(kind).get().defaultBlockState();}
  private static Plan plan(ServerPlayer player,BlockPos controller,ConfigureRequest request){
    access(player,controller);var level=player.serverLevel();
    if(controller.distSqr(request.origin)>64*64)throw new IllegalArgumentException("Place the bed origin within 64 blocks of this controller");
    var mechanism=((MovingControllerBlock)level.getBlockState(controller).getBlock()).mechanism();
    if(mechanism!=request.program.mechanism())throw new IllegalArgumentException("The selected program does not match this controller");
    var configuration=MovingCapabilityCatalog.Configuration.byId(request.configuration);var entry=MovingCapabilityCatalog.entry(configuration);var program=request.program.build();
    var permission=entry.permission(mechanism,program.maxAbsPitch());if(!permission.allowed())throw new IllegalArgumentException(permission.message());
    var world=FoundationData.get(level).moving;
    if(world.assemblies.size()>=64)throw new IllegalArgumentException("Dimension moving-assembly capacity reached");
    if(world.assemblies.values().stream().anyMatch(s->s.controller.equals(controller)))throw new IllegalArgumentException("This controller already owns an assembly; occupied or reserved geometry cannot be edited");
    var state=new MovingAssemblyState(UUID.randomUUID(),controller,mechanism.id(),entry.variant(),request.length);
    state.configuration=request.configuration;state.program=request.program;
    double yaw=Math.toRadians(switch(request.facing){case SOUTH->0;case EAST->90;case NORTH->180;case WEST->270;default->throw new IllegalArgumentException("Horizontal direction required");});
    var q=new Quaterniond().rotateY(yaw);var p=Vec3.atCenterOf(request.origin);
    state.origin=new MovingAssemblyState.Pose(p.x,p.y,p.z,q.x,q.y,q.z,q.w);state.actualPose=compose(state.origin,program.start());
    var start=state.actualPose;var end=compose(state.origin,program.end());int half=request.length/2;
    state.docks.put("start",new MovingAssemblyState.Dock("start",UUID.randomUUID(),socket(start,-half),socket(start,half),start));
    if(start.position().distance(end.position())>.001||MovingNativeAdapter.angularError(start.orientation(),end.orientation())>.001)
      state.docks.put("end",new MovingAssemblyState.Dock("end",UUID.randomUUID(),socket(end,-half),socket(end,half),end));
    state.source="start";state.destination=state.docks.containsKey("end")?"end":"start";
    var moving=new ArrayList<MovingNativeAdapter.BlockPlacement>();var d=design(configuration);
    boolean overhead=d!=null&&d.overhead(),pipeline=d==TrackDesign.PIPELINE;
    var bedKind=overhead?MovingPartBlock.Kind.OVERHEAD_GANTRY:pipeline?MovingPartBlock.Kind.PIPELINE_BED:MovingPartBlock.Kind.BED;
    int bedY=overhead?1:pipeline?-4:-1;
    for(int z=-half;z<=half;z++)for(int x=-1;x<=1;x++)moving.add(new MovingNativeAdapter.BlockPlacement(new BlockPos(x,bedY,z),part(bedKind)));
    if(pipeline)moving.addAll(pipelineEndPortals(request.length));
    var fixedMap=new LinkedHashMap<BlockPos,BlockState>();
    MovingMachineryGeometry.geometry(state,state.actualPose).feet().forEach((pos,kind)->fixedMap.put(pos,part(kind)));
    var localBounds=new MovingProgram.Bounds(pipeline?-3.5:-1.5,bedY-.5,-half-.5,pipeline?3.5:1.5,overhead||pipeline?1.5:0,half+.5).union(MovingMachineryGeometry.movingBounds(state));
    // Moving stays and guide sleeves are part of the same conservative continuous reservation.
    var sweeps=program.sweptVolumes(localBounds,.125).stream().map(b->transformBox(state.origin,b)).toList();
    var fixed=fixedMap.entrySet().stream().map(e->new FixedPart(e.getKey(),e.getValue())).toList();
    for(var block:fixed)state.fixedParts.put(block.pos,net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block.state.getBlock()).toString());
    var costs=new LinkedHashMap<Item,Integer>();for(var block:moving)costs.merge(block.state().getBlock().asItem(),1,Integer::sum);for(var block:fixed)costs.merge(block.state.getBlock().asItem(),1,Integer::sum);
    costs.put(MovingContent.INSERTS.get(configuration).get(),request.length);
    return new Plan(state,List.copyOf(moving),fixed,costs,sweeps);
  }
  /** Voxel-connected end portals keep both hosted anchors in the same native body.
   * Uprights stay outside the Pipeline cabin; the overhead header clears the native cart. */
  public static List<MovingNativeAdapter.BlockPlacement> pipelineEndPortals(int length){
    if(length!=16&&length!=32&&length!=48)throw new IllegalArgumentException("Unsupported Pipeline bed length");
    var parts=new LinkedHashMap<BlockPos,BlockState>();
    var brace=part(MovingPartBlock.Kind.PIPELINE_BRACE);
    for(int z:new int[]{-length/2,length/2}){
      for(int x:new int[]{-3,3})for(int y=-4;y<=1;y++)parts.put(new BlockPos(x,y,z),brace.setValue(MovingPartBlock.FACING,y==-4||y==1?Direction.EAST:Direction.UP).setValue(MovingPartBlock.LOCKED,y==-4||y==1));
      for(int x:new int[]{-2,2})parts.put(new BlockPos(x,-4,z),brace.setValue(MovingPartBlock.FACING,Direction.EAST));
      for(int x=-2;x<=2;x++)parts.put(new BlockPos(x,1,z),brace.setValue(MovingPartBlock.FACING,Direction.EAST));
    }
    return parts.entrySet().stream().map(e->new MovingNativeAdapter.BlockPlacement(e.getKey(),e.getValue())).toList();
  }
  private static void validate(ServerPlayer player,Plan plan){
    var level=player.serverLevel();var state=plan.state;var data=FoundationData.get(level);var entry=MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.byId(state.configuration));
    MovingNativeAdapter.validateSocketClaims(level,state);
    var anchors=new HashSet<BlockPos>();
    for(var dock:state.docks.values())for(var pos:List.of(dock.from(),dock.to())){
      if(!anchors.add(pos))continue;
      if(!level.hasChunkAt(pos)||!level.mayInteract(player,pos)||!(level.getBlockEntity(pos) instanceof CoasterAnchorpointBlockEntity))throw new IllegalArgumentException("Build a loaded, editable native anchor at socket "+pos.toShortString()+" before assembly");
      var anchor=(CoasterAnchorpointBlockEntity)level.getBlockEntity(pos);
      var frame=MovingNativeAdapter.socketFrame(state,dock,pos.equals(dock.to()));
      if(!anchor.getAnchorPeerCurvesView().isEmpty()&&(frame.rawPosition().distanceTo(CoasterAnchorpointBlockEntity.worldCenterBase(level,pos))>.001||frame.normal().distanceTo(new Vec3(0,1,0))>.001))
        throw new IllegalArgumentException("Remove attached curves at angled socket "+pos.toShortString()+", build the assembly, then join matching fixed approaches normally");
      var rails=data.rails.stream().filter(r->r.from.equals(pos)||r.to.equals(pos)).toList();
      boolean ordinary=entry.insert().provider().equals(MovingCapabilityCatalog.ORDINARY_PROVIDER);
      if(ordinary&&!rails.isEmpty()||!ordinary&&rails.stream().anyMatch(r->!r.valid||r.family!=entry.family()||!r.variant.equals(entry.variant())))throw new IllegalArgumentException("Socket "+pos.toShortString()+" needs the selected matching original track interface");
      for(var other:data.moving.assemblies.values())for(var used:other.docks.values())if(used.from().equals(pos)||used.to().equals(pos))throw new IllegalArgumentException("A socket is already owned by another moving assembly");
    }
    for(var fixed:plan.fixed){if(fixed.pos.equals(state.controller)||anchors.contains(fixed.pos)||!level.hasChunkAt(fixed.pos)||!level.mayInteract(player,fixed.pos)||!level.getBlockState(fixed.pos).canBeReplaced()||!level.getFluidState(fixed.pos).isEmpty())throw new IllegalArgumentException("Clear the fixed machinery position "+fixed.pos.toShortString());}
    validateBoxes(level,state,plan.sweep,player,Set.of());
    validateFixedMachinery(level,state,player);
  }
  public static void validateSweep(ServerLevel level,MovingAssemblyState state,MovingProgram.Bounds occupiedBounds,MovingProgram program){
    validateSweep(level,state,occupiedBounds,program,state.bodies.keySet());
  }
  /** Capture passes the body identities just authenticated by MovingAdmission, before retention is installed. */
  public static void validateSweep(ServerLevel level,MovingAssemblyState state,MovingProgram.Bounds occupiedBounds,MovingProgram program,Collection<UUID> admittedBodies){
    var boxes=program.sweptVolumes(occupiedBounds.union(MovingMachineryGeometry.movingBounds(state)),.125).stream().map(b->transformBox(state.origin,b)).toList();
    validateBoxes(level,state,boxes,null,admittedBodies);
    validateFixedMachinery(level,state,null);
  }
  /** Endpoint hull plus a conservative rotational arc bound and one extra game tick of travel. */
  public static AABB motionEnvelope(MovingProgram.Bounds bounds,MovingAssemblyState.Pose actual,MovingAssemblyState.Pose next,double linearSpeed,double angularSpeed){
    double angle=MovingNativeAdapter.angularError(actual.orientation(),next.orientation());
    double distance=actual.position().distance(next.position());
    if(!Double.isFinite(linearSpeed)||!Double.isFinite(angularSpeed)||!Double.isFinite(angle)||!Double.isFinite(distance))throw new IllegalArgumentException("Repair non-finite moving-body motion before continuing");
    double radius=bounds.radius();
    // The target is at most one game tick ahead. Use the larger measured or requested speed.
    double lookahead=Math.max(Math.abs(linearSpeed)*.05,distance)+radius*Math.max(Math.abs(angularSpeed)*.05,angle);
    return transformBox(actual,bounds).minmax(transformBox(next,bounds)).inflate(radius*angle*.5+lookahead+1e-6);
  }
  public static void validateMotionStep(ServerLevel level,MovingAssemblyState state,MovingProgram.Bounds bounds,MovingAssemblyState.Pose actualPose,MovingAssemblyState.Pose nextTarget){
    var envelope=motionEnvelope(bounds.union(MovingMachineryGeometry.movingBounds(state)),actualPose,nextTarget,state.linearVelocity.length(),state.angularVelocity.length());
    validateBoxes(level,state,List.of(envelope),null,state.bodies.keySet());
    validateFixedMachinery(level,state,null);
  }
  private static void validateFixedMachinery(ServerLevel level,MovingAssemblyState state,ServerPlayer builder){
    for(var box:MovingMachineryGeometry.fixedBounds(state))for(var mutable:BlockPos.betweenClosed((int)Math.floor(box.minX),(int)Math.floor(box.minY),(int)Math.floor(box.minZ),(int)Math.floor(box.maxX),(int)Math.floor(box.maxY),(int)Math.floor(box.maxZ))){
      var pos=mutable.immutable();if(!level.hasChunkAt(pos)||(builder!=null&&!level.mayInteract(builder,pos)))throw new IllegalArgumentException("Load and obtain access to the complete actuator and guide structure");
      if(state.fixedParts.containsKey(pos))continue; // Only the declared guide feet are designed bearing contact.
      if(!level.getFluidState(pos).isEmpty())throw new IllegalArgumentException("Drain actuator clearance at "+pos.toShortString());
      for(var collision:level.getBlockState(pos).getCollisionShape(level,pos).toAabbs())if(collision.move(pos).intersects(box))throw new IllegalArgumentException("Clear actuator or guide structure at "+pos.toShortString());
    }
  }
  private static void validateBoxes(ServerLevel level,MovingAssemblyState state,List<AABB> boxes,ServerPlayer builder,Collection<UUID> admittedBodies){
    var anchors=new HashSet<BlockPos>();for(var dock:state.docks.values()){anchors.add(dock.from());anchors.add(dock.to());}
    var visited=new HashSet<BlockPos>();
    for(var box:boxes){
      if(box.minY<level.getMinBuildHeight()||box.maxY>=level.getMaxBuildHeight())throw new IllegalArgumentException("The moving sweep exceeds build height");
      for(var mutable:BlockPos.betweenClosed((int)Math.floor(box.minX),(int)Math.floor(box.minY),(int)Math.floor(box.minZ),(int)Math.floor(box.maxX),(int)Math.floor(box.maxY),(int)Math.floor(box.maxZ))){
        var pos=mutable.immutable();visited.add(pos);
        if(visited.size()>200000)throw new IllegalArgumentException("The bounded sweep exceeds construction capacity");
        if(!level.hasChunkAt(pos)||(builder!=null&&!level.mayInteract(builder,pos)))throw new IllegalArgumentException("Load and obtain build access to the complete moving sweep");
        if(anchors.contains(pos))continue; // Native rail endpoint contact is the intended docking interface.
        if(!level.getFluidState(pos).isEmpty())throw new IllegalArgumentException("Drain the moving sweep at "+pos.toShortString());
        for(var shape:level.getBlockState(pos).getCollisionShape(level,pos).toAabbs())
          if(shape.move(pos).intersects(box))throw new IllegalArgumentException("Clear the moving sweep at "+pos.toShortString());
      }
    }
    validateDynamicObstacles(level,state,boxes,admittedBodies);
  }
  private static AABB nativeBox(dev.ryanhcode.sable.sublevel.ServerSubLevel body,AABB local){
    AABB result=null;
    for(double x:new double[]{local.minX,local.maxX})for(double y:new double[]{local.minY,local.maxY})for(double z:new double[]{local.minZ,local.maxZ}){
      var p=body.logicalPose().transformPosition(new Vector3d(x,y,z));var v=new Vec3(p.x,p.y,p.z);var point=new AABB(v,v);result=result==null?point:result.minmax(point);
    }return result;
  }
  private static void validateDynamicObstacles(ServerLevel level,MovingAssemblyState state,List<AABB> boxes,Collection<UUID> admittedBodies){
    var owned=new HashSet<UUID>(admittedBodies);owned.addAll(state.bodies.keySet());if(state.bed!=null)owned.add(state.bed);
    var ownEntities=new HashSet<UUID>();var obstacles=new ArrayList<AABB>();
    for(var nativeBody:dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level).getAllSubLevels()){
      if(!(nativeBody instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel body)||body.isRemoved())continue;
      var bounds=body.getPlot().getBoundingBox();if(bounds==null)continue;
      var plot=new AABB(bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX()+1,bounds.maxY()+1,bounds.maxZ()+1);
      boolean own=owned.contains(body.getUniqueId());
      if(!own)obstacles.add(nativeBox(body,plot));
      for(var entity:level.getEntities((net.minecraft.world.entity.Entity)null,plot,e->!e.isSpectator())){
        if(own&&entity.getRootVehicle() instanceof com.simibubi.create.content.contraptions.actors.seat.SeatEntity){ownEntities.add(entity.getUUID());for(var rider:entity.getIndirectPassengers())ownEntities.add(rider.getUUID());}
        else {obstacles.add(nativeBox(body,entity.getBoundingBox()));for(var rider:entity.getIndirectPassengers()){var containing=dev.ryanhcode.sable.Sable.HELPER.getContaining(level,rider.position());obstacles.add(containing instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel riderBody?nativeBox(riderBody,rider.getBoundingBox()):rider.getBoundingBox());}}
      }
    }
    for(var box:boxes){
      for(var obstacle:obstacles)if(obstacle.intersects(box))throw new IllegalArgumentException("Move other native bodies and their occupants outside the moving sweep");
      if(!level.getEntities((net.minecraft.world.entity.Entity)null,box,e->!e.isSpectator()&&!ownEntities.contains(e.getUUID())).isEmpty())throw new IllegalArgumentException("Move players and entities outside the moving sweep");
    }
  }

  public static CompoundTag preview(ServerPlayer player,BlockPos controller,ConfigureRequest request){
    var plan=plan(player,controller,request);var tag=new CompoundTag();tag.put("request",request.save());tag.put("assembly",plan.state.save(false));
    var costs=new ListTag();for(var cost:plan.costs.entrySet()){var t=new CompoundTag();t.putString("item",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(cost.getKey()).toString());t.putInt("count",cost.getValue());costs.add(t);}tag.put("costs",costs);
    var boxes=new ListTag();for(var box:plan.sweep){var t=new CompoundTag();t.putDouble("min_x",box.minX);t.putDouble("min_y",box.minY);t.putDouble("min_z",box.minZ);t.putDouble("max_x",box.maxX);t.putDouble("max_y",box.maxY);t.putDouble("max_z",box.maxZ);boxes.add(t);}tag.put("sweep",boxes);
    var parts=new ListTag();for(var fixed:plan.fixed){var t=new CompoundTag();t.putLong("pos",fixed.pos.asLong());t.putString("block",net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(fixed.state.getBlock()).toString());parts.add(t);}tag.put("fixed",parts);
    try{validate(player,plan);tag.putBoolean("valid",true);}catch(IllegalArgumentException e){tag.putBoolean("valid",false);tag.putString("error",e.getMessage());}
    return tag;
  }
  public static MovingAssemblyState build(ServerPlayer player,BlockPos controller,ConfigureRequest request){
    var plan=plan(player,controller,request);validate(player,plan);var inventory=player.getInventory();
    if(!player.isCreative())for(var cost:plan.costs.entrySet()){int available=inventory.items.stream().filter(s->s.is(cost.getKey())).mapToInt(ItemStack::getCount).sum();if(available<cost.getValue())throw new IllegalArgumentException("Requires "+cost.getValue()+" × "+cost.getKey().getDescription().getString());}
    var before=inventory.items.stream().map(ItemStack::copy).toList();var originals=new LinkedHashMap<BlockPos,BlockState>();var level=player.serverLevel();var data=FoundationData.get(level);FoundationData.Rail rail=null;
    try{
      MovingNativeAdapter.createBed(level,plan.state,plan.state.actualPose,plan.moving);
      var config=MovingCapabilityCatalog.Configuration.byId(request.configuration);var d=design(config);
      if(d!=null){var edge=MovingNativeAdapter.hostedEdge(level,plan.state);rail=new FoundationData.Rail(edge.from(),edge.to(),d.family,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(request.length,0))),UUID.randomUUID(),d.variant,TrackGeometry.fingerprint(level,edge),0,true);
        var anchor=(CoasterAnchorpointBlockEntity)level.getBlockEntity(edge.from());TrackPlacement.captureOriginalCurve(rail,anchor.getAnchorPeerCurvesView().get(edge.to()));data.rails.add(rail);}
      for(var part:plan.fixed){originals.put(part.pos,level.getBlockState(part.pos));if(!level.setBlock(part.pos,part.state,3))throw new IllegalStateException("Could not place the validated machinery; construction rolled back");}
      if(!player.isCreative())for(var cost:plan.costs.entrySet()){int remaining=cost.getValue();for(var stack:inventory.items)if(stack.is(cost.getKey())){int take=Math.min(stack.getCount(),remaining);stack.shrink(take);remaining-=take;if(remaining==0)break;}if(remaining!=0)throw new IllegalStateException("Inventory changed during construction");}
      if(!player.isCreative())for(var cost:plan.costs.entrySet())plan.state.materials.put(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(cost.getKey()).toString(),cost.getValue());
      MovingRuntime.register(level,plan.state);inventory.setChanged();data.setDirty();dev.fourthdimension.runtime.FoundationSync.broadcast(level);return plan.state;
    }catch(RuntimeException failure){
      if(rail!=null)data.rails.remove(rail);data.moving.assemblies.remove(plan.state.id);originals.forEach((pos,state)->level.setBlock(pos,state,3));for(int i=0;i<before.size();i++)inventory.items.set(i,before.get(i));inventory.setChanged();
      if(plan.state.bed!=null)MovingNativeAdapter.destroy(level,plan.state);data.setDirty();throw failure;
    }
  }
  public static void dismantle(ServerPlayer player,MovingAssemblyState state){
    access(player,state.controller);var level=player.serverLevel();var data=FoundationData.get(level);
    if(data.moving.assemblies.get(state.id)!=state||state.retained()||!state.cars.isEmpty()
        ||state.phase!=MovingAssemblyState.Phase.DOCKED&&state.phase!=MovingAssemblyState.Phase.RECONNECTED&&state.phase!=MovingAssemblyState.Phase.FAULT_HOLD)
      throw new IllegalArgumentException("Release every train and stop the empty assembly before dismantling");
    MovingNativeAdapter.readback(level,state);if(!MovingNativeAdapter.stopped(level,state))throw new IllegalArgumentException("Wait for the bed to stop before dismantling");
    for(var body:dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level).getAllSubLevels()){
      if(body.getUniqueId().equals(state.bed))continue;
      var center=dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(body);if(center==null)continue;
      var local=new Vector3d(center).sub(state.actualPose.position()).rotate(state.actualPose.orientation().conjugate());
      if(Math.abs(local.x)<8&&Math.abs(local.y)<8&&Math.abs(local.z)<state.deckLength/2.0+2)
        throw new IllegalArgumentException("Move all native cars clear of the bed before dismantling");
    }
    for(var part:state.fixedParts.entrySet())if(!level.hasChunkAt(part.getKey())||!level.mayInteract(player,part.getKey())||!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(part.getKey()).getBlock()).toString().equals(part.getValue()))throw new IllegalArgumentException("Restore and obtain access to owned machinery before dismantling");
    var from=state.railFrom;var to=state.railTo;MovingNativeAdapter.destroy(level,state);
    data.rails.removeIf(rail->rail.matches(from,to));state.fixedParts.keySet().forEach(pos->level.removeBlock(pos,false));
    data.moving.releaseReservation(state.id);data.moving.assemblies.remove(state.id);data.setDirty();
    for(var material:state.materials.entrySet()){
      var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(material.getKey()));int remaining=material.getValue();
      while(remaining>0){var stack=new ItemStack(item,Math.min(64,remaining));remaining-=stack.getCount();if(!player.getInventory().add(stack))player.drop(stack,false);}
    }
    state.materials.clear();player.getInventory().setChanged();dev.fourthdimension.runtime.FoundationSync.broadcast(level);
  }

}
