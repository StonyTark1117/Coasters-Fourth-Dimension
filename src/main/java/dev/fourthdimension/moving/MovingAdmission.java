package dev.fourthdimension.moving;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.contract.NativeTrackFit;
import dev.fourthdimension.integration.CtnbTracks;
import dev.fourthdimension.integration.RideAuthority;
import dev.fourthdimension.moving.MovingCapabilityCatalog.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Server-measured complete-train admission. Saved identities/client selections never certify bodies. */
public final class MovingAdmission {
  public record Capture(List<UUID> cars,List<UUID> bodies,Configuration configuration,MovingProgram.Bounds bounds,
                        Set<Hold> held,Insert bed,Insert source,Insert destination,Train train,Readiness readiness) {
    public Capture { cars=List.copyOf(cars);bodies=List.copyOf(bodies);held=Set.copyOf(held); }
    /** The root runtime supplies its live exclusive receiving reservation and world collision result. */
    public Request request(MovingAssemblyState state,MovingProgram program,boolean receivingClear,boolean sweepClear){
      var r=readiness;
      return new Request(configuration,program,state.deckLength,bed,source,destination,train,
          new Readiness(held,r.restraintsClosed(),r.stopped(),r.routeValid(),receivingClear,sweepClear,
              r.propulsionReleased(),r.managedCircuit(),r.launchSystemsOwner()));
    }
  }
  /** A route owner must validate actual controller/section topology, including the selected fixed exit. */
  public interface FamilyRouteAdapter {
    void validateCapture(ServerLevel level,MovingAssemblyState state,FoundationData.Vehicle car);
    void validateRelease(ServerLevel level,MovingAssemblyState state,FoundationData.Vehicle car,MovingAssemblyState.Dock dock);
  }
  private static final Map<Configuration,FamilyRouteAdapter> ROUTES=new EnumMap<>(Configuration.class);
  private static final Set<Configuration> ROUTE_HANDOFF=Set.of(Configuration.PAX,Configuration.ROLLER_BALL,
      Configuration.POWERED_INVERTED_YAW,Configuration.NEW_GENERATION_FLYING,Configuration.FLYING_BIKE);
  private MovingAdmission(){}
  public static void registerRouteAdapter(Configuration configuration,FamilyRouteAdapter adapter){
    if(!ROUTE_HANDOFF.contains(configuration)||ROUTES.putIfAbsent(configuration,Objects.requireNonNull(adapter))!=null)
      throw new IllegalArgumentException("Invalid or duplicate moving route adapter");
  }
  public static Set<Configuration> missingRouteAdapters(){
    var missing=EnumSet.copyOf(ROUTE_HANDOFF);missing.removeAll(ROUTES.keySet());return Set.copyOf(missing);
  }

  /** Empty return is a physical claim, never inferred from cleared saved retention lists. */
  public static void requirePhysicallyEmpty(ServerLevel level,MovingAssemblyState state){
    serverThread(level);MovingNativeAdapter.readback(level,state);
    if(state.retained()||!state.cars.isEmpty()||!state.service.cars.isEmpty())
      throw new IllegalArgumentException("Release and clear every car before empty return");
    var config=Configuration.byId(state.configuration);
    var envelope=config==Configuration.WING?new NativeTrackFit.Envelope(5,3.7,.95,1.75)
        :config==Configuration.NATIVE_SERVICE?new NativeTrackFit.Envelope(2,2,2,2)
        :TrackDesign.byVariant(MovingCapabilityCatalog.entry(config).variant()).nativeFit().envelope();
    var clearance=new MovingProgram.Bounds(-envelope.halfWidth()-.5,-envelope.belowO()-.5,-state.deckLength/2.0-.25,
        envelope.halfWidth()+.5,envelope.aboveO()+.5,state.deckLength/2.0+.25);
    for(var candidate:SubLevelContainer.getContainer(level).getAllSubLevels()){
      if(candidate.isRemoved()||candidate.getUniqueId().equals(state.bed)||candidate.getPlot().getBoundingBox()==null)continue;
      var occupied=bodyBounds(state,candidate);var car=FoundationData.get(level).vehicles.get(candidate.getUniqueId());
      if(car!=null)occupied=occupied.union(declaredBounds(level,state,candidate,car));
      for(var seat:level.getEntitiesOfClass(SeatEntity.class,plotBox(candidate)))for(var rider:seat.getIndirectPassengers())
        occupied=occupied.union(riderBounds(state,rider));
      if(overlaps(clearance,occupied))throw new IllegalArgumentException("Clear the uncaptured native body/train from the moving bed before empty return: "+candidate.getUniqueId());
    }
  }
  private static boolean overlaps(MovingProgram.Bounds a,MovingProgram.Bounds b){
    return a.minX()<b.maxX()&&a.maxX()>b.minX()&&a.minY()<b.maxY()&&a.maxY()>b.minY()&&a.minZ()<b.maxZ()&&a.maxZ()>b.minZ();
  }

  public static Capture inspect(ServerLevel level,MovingAssemblyState state){
    serverThread(level);MovingNativeAdapter.readback(level,state);
    if(state.retained())throw new IllegalArgumentException("This bed already retains a train");
    if(!MovingNativeAdapter.stopped(level,state))throw new IllegalArgumentException("Stop and lock the bed before train capture");
    var source=state.docks.get(state.source);var destination=state.docks.get(state.destination);
    if(source==null||destination==null)throw new IllegalArgumentException("Select real source and destination docking sockets");
    var container=SubLevelContainer.getContainer(level);var data=FoundationData.get(level);
    var candidates=new LinkedHashSet<UUID>();
    for(var body:container.getAllSubLevels()){
      if(body.getUniqueId().equals(state.bed)||!bearing(body))continue;
      var point=CoasterCartResolve.cartCenterWorld(body);if(point==null)continue;
      var local=local(state,point);
      if(Math.abs(local.x)<=.35 && Math.abs(local.y)<=.85 && Math.abs(local.z)<=state.deckLength/2.0)
        candidates.add(body.getUniqueId());
    }
    if(candidates.isEmpty())throw new IllegalArgumentException("Roll a complete stopped train fully onto the locked bed");
    var first=body(level,candidates.iterator().next());var linked=linked(first);
    if(!linked.containsAll(candidates))throw new IllegalArgumentException("Only one complete linked train may occupy the bed");
    if(linked.size()>4)throw new IllegalArgumentException("Moving bed permits at most four cars, subject to lower family limits");
    var firstCar=data.vehicles.get(first.getUniqueId());
    var config=firstCar==null?Configuration.NATIVE_SERVICE:MovingCapabilityCatalog.find(firstCar.family,firstCar.variant)
        .orElseThrow(()->new IllegalArgumentException("Unknown car variant cannot enter moving support")).configuration();
    if(!state.configuration.equals(config.id()))throw new IllegalArgumentException("Bed configured for "+state.configuration+"; train is "+config.label());
    var entry=MovingCapabilityCatalog.entry(config);
    var permission=entry.permission(MovingMechanism.byId(state.mechanism),state.program==null?0:state.program.build().maxAbsPitch());
    require(permission);
    validateBedStructure(level,state,entry);
    var bed=inspectInsert(level,entry,MovingNativeAdapter.hostedEdge(level,state),"Bed");
    var sourceInsert=inspectDock(level,entry,source,"Source dock");
    var destinationInsert=inspectDock(level,entry,destination,"Destination dock");
    var bodies=new LinkedHashSet<UUID>();var passengers=new HashSet<UUID>();
    MovingProgram.Bounds bounds=null;
    var held=EnumSet.copyOf(entry.requiredHolds());
    for(var id:linked){
      var existingOwner=data.moving.retaining(id);
      if(existingOwner!=null&&!existingOwner.id.equals(state.id))throw new IllegalArgumentException("Another moving assembly already retains this train");
      var chassis=body(level,id);if(!bearing(chassis))throw new IllegalArgumentException("Train body lost its native running gear");
      if(!linked(chassis).equals(linked))throw new IllegalArgumentException("Repair incomplete or asymmetric native train links");
      stopped(level,chassis);bodies.add(id);
      var car=data.vehicles.get(id);
      if(config==Configuration.NATIVE_SERVICE){
        if(car!=null||!OrdinaryGearCertificate.fits(chassis))throw new IllegalArgumentException("Empty service accepts only inspected unmodified Coasters chassis");
        collectPassengers(level,chassis,passengers);
      }else{
        if(car==null||car.family!=entry.family()||!car.variant.equals(entry.variant()))throw new IllegalArgumentException("Mixed family/variant trains cannot enter moving support");
        validateVehicle(level,state,car,entry);
        collectBodies(car,bodies);
        var extra=declaredBounds(level,state,chassis,car);bounds=bounds==null?extra:bounds.union(extra);
      }
    }
    for(var id:bodies){
      var b=body(level,id);stopped(level,b);collectPassengers(level,b,passengers);
      var measured=bodyBounds(state,b);bounds=bounds==null?measured:bounds.union(measured);
      // Sable normally kicks mounted passengers into world coordinates; the seat stays in its plot.
      for(var seat:level.getEntitiesOfClass(SeatEntity.class,plotBox(b)))for(var rider:seat.getIndirectPassengers())
        bounds=bounds.union(riderBounds(state,rider));
    }
    // The convex union includes the complete between-bearing coupler reservation.
    var train=new Train(linked.size(),passengers.size(),bounds,true,true,false,config==Configuration.NATIVE_SERVICE);
    var ready=new Readiness(held,true,true,true,false,false,true,false,false);
    var result=new Capture(List.copyOf(linked),List.copyOf(bodies),config,bounds.union(bodyBounds(state,body(level,state.bed))),held,bed,sourceInsert,destinationInsert,train,ready);
    // Admission owns all guards except root's live receiving reservation and world sweep query.
    require(MovingCapabilityCatalog.assess(result.request(state,state.program==null?defaultProgram(state):state.program.build(),true,true)));
    return result;
  }

  /** Current native body/model/rider reservation in stable bed coordinates, including the bed. */
  public static MovingProgram.Bounds bounds(ServerLevel level,MovingAssemblyState state){
    MovingNativeAdapter.readback(level,state);
    MovingProgram.Bounds result=bodyBounds(state,body(level,state.bed));
    for(var id:state.bodies.keySet()){
      var b=body(level,id);result=result.union(bodyBounds(state,b));
      for(var seat:level.getEntitiesOfClass(SeatEntity.class,plotBox(b)))for(var rider:seat.getIndirectPassengers())
        result=result.union(riderBounds(state,rider));
    }
    for(var id:state.cars){var car=FoundationData.get(level).vehicles.get(id);
      if(car!=null)result=result.union(declaredBounds(level,state,body(level,id),car));}
    return result;
  }

  public static void validateRetained(ServerLevel level,MovingAssemblyState state){
    serverThread(level);var data=FoundationData.get(level);var expected=new LinkedHashSet<UUID>();
    var entry=MovingCapabilityCatalog.entry(Configuration.byId(state.configuration));
    if(state.cars.isEmpty())throw new IllegalArgumentException("Retained train identity is missing");
    for(var id:state.cars){
      var b=body(level,id);if(!bearing(b))throw new IllegalArgumentException("Retained chassis lost native running gear");
      expected.add(id);var car=data.vehicles.get(id);
      if(entry.configuration()==Configuration.NATIVE_SERVICE){
        if(car!=null||!OrdinaryGearCertificate.fits(b))throw new IllegalArgumentException("Retained native service chassis was modified");
        var riders=new HashSet<UUID>();collectPassengers(level,b,riders);if(!riders.isEmpty())throw new IllegalArgumentException("Native service cart must remain empty");
      }else{
        if(car==null||car.family!=entry.family()||!car.variant.equals(entry.variant()))throw new IllegalArgumentException("Retained car identity changed");
        if(!car.restraints||car.recoveryCart!=null)throw new IllegalArgumentException("Retained car lost restraint/recovery isolation");
        collectBodies(car,expected);
      }
      if(!linked(b).equals(new LinkedHashSet<>(state.cars)))throw new IllegalArgumentException("Retained complete-train links changed");
    }
    if(!expected.equals(state.bodies.keySet()))throw new IllegalArgumentException("Retained body inventory changed; repair chassis/carrier hierarchy");
    expected.forEach(id->body(level,id));
  }

  /** Pure guard: root calls released() only after native dock/lock and physical release succeed. */
  public static void validateRelease(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock){
    validateRetained(level,state);var entry=MovingCapabilityCatalog.entry(Configuration.byId(state.configuration));
    inspectDock(level,entry,dock,"Destination dock");
    var approaches=dockEdges(level,dock);
    for(var id:state.cars){
      var car=FoundationData.get(level).vehicles.get(id);
      if(car==null)continue;
      for(var edge:approaches){String rejection=trackRejection(level,entry.configuration(),edge);
        if(rejection!=null)throw new IllegalArgumentException(rejection);}
      if(ROUTE_HANDOFF.contains(entry.configuration()))route(entry.configuration()).validateRelease(level,state,car,dock);
    }
  }
  public static void released(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock){
    for(var id:state.cars){var car=FoundationData.get(level).vehicles.get(id);if(car==null)continue;
      car.boundFrom=dock.from();car.boundTo=dock.to();car.stopping=true;car.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  public static void validateBedStructure(ServerLevel l,MovingAssemblyState s,Entry entry){
    var nativeBed=body(l,s.bed);var blocks=nativeBed.getPlot().getEmbeddedLevelAccessor();
    boolean overhead=NativeTrackFit.OVERHEAD.equals(entry.insert().mount());
    var expected=overhead?MovingPartBlock.Kind.OVERHEAD_GANTRY:entry.configuration()==Configuration.PIPELINE?MovingPartBlock.Kind.PIPELINE_BED:MovingPartBlock.Kind.BED;
    for(int z=-s.deckLength/2;z<=s.deckLength/2;z++)for(int x=-1;x<=1;x++){
      var state=blocks.getBlockState(new BlockPos(x,overhead?1:entry.configuration()==Configuration.PIPELINE?-4:-1,z));
      if(!(state.getBlock() instanceof MovingPartBlock part)||part.kind()!=expected)
        throw new IllegalArgumentException("Repair the complete physical "+expected.name().toLowerCase(java.util.Locale.ROOT)+" under/over the moving insert");
    }
    if(entry.configuration()==Configuration.PIPELINE)for(var brace:MovingConstruction.pipelineEndPortals(s.deckLength))
      if(!blocks.getBlockState(brace.local()).equals(brace.state()))
        throw new IllegalArgumentException("Repair the complete Pipeline end portals joining both native anchors to the lower bed");
  }
  public static Insert inspectInsert(ServerLevel level,Entry entry,CoasterPathEdge edge,String name){
    if(edge==null||edge.bezier()==null||!level.hasChunkAt(edge.from())||!level.hasChunkAt(edge.to()))
      throw new IllegalArgumentException(name+": load and repair the actual native rail span");
    var material=CtnbTracks.material(level,edge);
    if(material==null||!material.id.equals(CoasterTrackMaterials.COASTER.id))
      throw new IllegalArgumentException(name+": native Coasters material required; cosmetic metadata cannot grant support");
    var rail=FoundationData.get(level).rails.stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElse(null);
    final Insert insert;
    if(rail==null){
      insert=new Insert(MovingCapabilityCatalog.ORDINARY_PROVIDER,1,null,MovingCapabilityCatalog.ORDINARY_VARIANT,
          NativeTrackFit.N_CONTACTS,NativeTrackFit.ABOVE,Set.of(),true);
    }else{
      if(!rail.original()||!rail.valid||TrackGeometry.resolve(level,rail)==null
          ||rail.geometryHash!=TrackGeometry.fingerprint(level,edge))
        throw new IllegalArgumentException(name+": functional rail registration is invalid");
      var design=TrackDesign.byVariant(rail.variant);var fit=design.nativeFit();var contract=design.contract();
      if(design.family!=rail.family)throw new IllegalArgumentException(name+": rail family and insert disagree");
      insert=new Insert(contract.provider(),1,rail.family,rail.variant,fit.contacts(),fit.mount(),contract.mechanisms(),true);
    }
    require(MovingCapabilityCatalog.checkInsert(entry,insert,name));return insert;
  }
  private static void validateVehicle(ServerLevel level,MovingAssemblyState state,FoundationData.Vehicle car,Entry entry){
    if(car.recoveryCart!=null)throw new IllegalArgumentException("Detach recovery carts on fixed track before admission");
    if(!car.restraints||!car.stopping||car.maintenance||car.alignmentRequired||!car.fault.isEmpty()&&!MovingPaxRecovery.isMovingCaptureFault(level,car))
      throw new IllegalArgumentException("Stop, repair and close every original car restraint before capture");
    var data=FoundationData.get(level);
    if(data.rideLocal.trains.values().stream().anyMatch(t->t.cars.contains(car.chassis)))
      throw new IllegalArgumentException("Managed 1.2 circuit trains cannot enter moving support");
    var provider=car.authority.provider;
    if(RideAuthority.externalSelected(car))RideAuthority.validateMovingCapture(level,car);
    if(car.authority.owner!=null&&!RideAuthority.externalSelected(car)&&!Set.of(RideAuthority.NATIVE_CHAIN,RideAuthority.ENGINEERED,RideAuthority.MAGNETIZED).contains(provider))
      throw new IllegalArgumentException("Unsupported longitudinal owner must release before moving support");
    if(car.authority.owner!=null&&!car.authority.held)throw new IllegalArgumentException("Hold and transfer fixed-span propulsion before capture");
    validateShape(car,entry.configuration());
    for(var carrier:car.carriers){
      body(level,carrier.body);
      if(!carrier.capture||!Double.isFinite(carrier.angle)||!Double.isFinite(carrier.omega)||Math.abs(carrier.omega)>.03)
        throw new IllegalArgumentException("Capture and stop each carrier before moving its chassis");
      if(entry.requiredHolds().contains(Hold.CARRIER)&&Math.abs(Math.atan2(Math.sin(carrier.angle-carrier.captureTarget),Math.cos(carrier.angle-carrier.captureTarget)))>.04)
        throw new IllegalArgumentException("Carrier is not aligned to its held target");
    }
    if(entry.requiredHolds().contains(Hold.POSTURE)&&(car.flying.lock==FlyingState.Lock.NONE||car.flying.transitioning()||!car.flying.retained()))
      throw new IllegalArgumentException("Lock Flying posture and both restraints before moving support");
    if(entry.requiredHolds().contains(Hold.YAW)){
      double rate=entry.configuration()==Configuration.NEW_GENERATION_FLYING?car.yaw.velocity:car.carriers.getFirst().omega;
      if(!Double.isFinite(rate)||Math.abs(rate)>.03)throw new IllegalArgumentException("Stop and hold yaw before table motion");
    }
    if(entry.requiredHolds().contains(Hold.SURF_HOME)&&(!car.carriers.getFirst().latched||Math.abs(car.standing.travel)>.015
        ||Math.abs(car.standing.velocity)>.03||car.standing.interrupted))throw new IllegalArgumentException("Return Surf travel home and engage its latch");
    if(ROUTE_HANDOFF.contains(entry.configuration()))route(entry.configuration()).validateCapture(level,state,car);
  }
  private static FamilyRouteAdapter route(Configuration c){
    var adapter=ROUTES.get(c);if(adapter==null)throw new IllegalArgumentException(c.label()+": moving route handoff adapter is not yet registered");return adapter;
  }
  private static void validateShape(FoundationData.Vehicle v,Configuration c){
    switch(c){
      case SPINNING->SpinningVehicle.validate(v);case S2_SPINNING->S2SpinningVehicle.validate(v);
      case PIPELINE->PipelineVehicle.validate(v);case DIVE->DiveVehicle.validate(v);case SUSPENDED->SuspendedVehicle.validate(v);
      case FIXED_INVERTED->FixedInvertedVehicle.validate(v);case AXIS->AxisVehicle.validate(v);case FLYING->FlyingVehicle.validate(v);
      case NEW_GENERATION_FLYING->NewGenerationFlyingVehicle.validate(v);case POWERED_INVERTED_YAW->PoweredInvertedYawVehicle.validate(v);
      case ROLLER_BALL->RollerBallVehicle.validate(v);case STANDING_STANDARD,STANDING_SURF->StandingVehicle.validate(v);
      case MOTORBIKE->MotorbikeVehicle.validate(v);case FLYING_BIKE->FlyingBikeVehicle.validate(v);
      default->{if(v.carriers.isEmpty())throw new IllegalArgumentException("Original carrier identity is missing");}
    }
  }
  public static String trackRejection(ServerLevel l,Configuration c,CoasterPathEdge e){return switch(c){
    case WING->WingTrack.rejection(l,e);case SPINNING->SpinningTrack.rejection(l,e);case S2_SPINNING->S2SpinningTrack.rejection(l,e);
    case PIPELINE->PipelineTrack.rejection(l,e);case DIVE->DiveTrack.rejection(l,e);case SUSPENDED->SuspendedTrack.rejection(l,e);
    case FIXED_INVERTED->FixedInvertedTrack.rejection(l,e);case AXIS->AxisTrack.rejection(l,e);case FLYING->FlyingTrack.rejection(l,e);
    case NEW_GENERATION_FLYING->NewGenerationFlyingTrack.rejection(l,e);case POWERED_INVERTED_YAW->PoweredInvertedYawTrack.rejection(l,e);
    case ROLLER_BALL->RollerBallTrack.rejection(l,e);case STANDING_STANDARD->StandingTrack.rejection(l,e,TrackDesign.STANDING_STANDARD);
    case STANDING_SURF->StandingTrack.rejection(l,e,TrackDesign.STANDING_SURF);case MOTORBIKE->MotorbikeTrack.rejection(l,e);
    case FLYING_BIKE->FlyingBikeTrack.rejection(l,e);default->null;
  };}
  private static void collectBodies(FoundationData.Vehicle v,Set<UUID> ids){
    ids.add(v.chassis);for(var c:v.carriers)ids.add(c.body);if(NewGenerationFlyingVehicle.is(v)){if(v.yaw.deck==null)throw new IllegalArgumentException("NG Flying yaw deck is missing");ids.add(v.yaw.deck);}
  }
  /** Complete actual car reservation in the current bed frame, including rider/model extents. */
  public static MovingProgram.Bounds carBounds(ServerLevel l,MovingAssemblyState state,FoundationData.Vehicle v){
    var ids=new LinkedHashSet<UUID>();collectBodies(v,ids);
    var result=declaredBounds(l,state,body(l,v.chassis),v);
    for(var id:ids){var b=body(l,id);result=result.union(bodyBounds(state,b));
      for(var seat:l.getEntitiesOfClass(SeatEntity.class,plotBox(b)))for(var rider:seat.getIndirectPassengers())result=result.union(riderBounds(state,rider));}
    return result;
  }
  public static MovingProgram.Bounds nativeServiceBounds(ServerLevel level,MovingAssemblyState state,UUID id){
    var b=body(level,id);
    if(FoundationData.get(level).vehicles.containsKey(id)||!OrdinaryGearCertificate.fits(b)
        ||!linked(b).equals(new LinkedHashSet<>(state.cars)))throw new IllegalArgumentException("Restore the complete unmodified native service train");
    var riders=new HashSet<UUID>();collectPassengers(level,b,riders);
    if(!riders.isEmpty())throw new IllegalArgumentException("Native service carts must remain empty");
    return bodyBounds(state,b);
  }
  private static boolean bearing(ServerSubLevel b){return b.getPlot().getEmbeddedLevelAccessor().getBlockState(BlockPos.ZERO).is(SimulatedCoastersBlocks.COASTER_CART_BLOCK.get());}
  private static LinkedHashSet<UUID> linked(ServerSubLevel body){var ids=new LinkedHashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body));ids.add(body.getUniqueId());return ids;}
  private static ServerSubLevel body(ServerLevel l,UUID id){
    var b=id==null?null:SubLevelContainer.getContainer(l).getSubLevel(id);
    if(!(b instanceof ServerSubLevel result)||result.isRemoved())throw new IllegalArgumentException("Load/repair missing retained body "+id);return result;
  }
  private static void stopped(ServerLevel l,ServerSubLevel b){
    var physics=SubLevelContainer.getContainer(l).physicsSystem();physics.updatePose(b);
    if(physics.getPipeline().getLinearVelocity(b,new Vector3d()).length()>MovingNativeAdapter.LOCK_SPEED
        ||physics.getPipeline().getAngularVelocity(b,new Vector3d()).length()>Math.toRadians(.1))
      throw new IllegalArgumentException("Stop every chassis and carrier before capture");
  }
  private static Vector3d local(MovingAssemblyState state,Vector3d world){return new Vector3d(world).sub(state.actualPose.position()).rotate(state.actualPose.orientation().conjugate());}
  private static AABB plotBox(ServerSubLevel body){var b=body.getPlot().getBoundingBox();if(b==null)throw new IllegalArgumentException("Native body is empty");return new AABB(b.minX(),b.minY(),b.minZ(),b.maxX()+1,b.maxY()+1,b.maxZ()+1);}
  private static MovingProgram.Bounds bodyBounds(MovingAssemblyState state,ServerSubLevel body){return transformedBox(state,body,plotBox(body));}
  private static MovingProgram.Bounds transformedBox(MovingAssemblyState state,ServerSubLevel body,AABB box){
    MovingProgram.Bounds result=null;
    for(double x:new double[]{box.minX,box.maxX})for(double y:new double[]{box.minY,box.maxY})for(double z:new double[]{box.minZ,box.maxZ}){
      var p=local(state,body.logicalPose().transformPosition(new Vector3d(x,y,z)));
      var point=new MovingProgram.Bounds(p.x,p.y,p.z,p.x,p.y,p.z);result=result==null?point:result.union(point);
    }return result;
  }
  /** Sable 2.0.6 positionRider/rideTick kick passengers to world space, unlike SeatEntity.
   * Inspect actual containment to also cover the brief pre-kick plot-coordinate state. */
  private static MovingProgram.Bounds riderBounds(MovingAssemblyState state,net.minecraft.world.entity.Entity rider){
    var containing=dev.ryanhcode.sable.Sable.HELPER.getContaining(rider.level(),rider.position());
    var box=rider.getBoundingBox();MovingProgram.Bounds result=null;
    for(double x:new double[]{box.minX,box.maxX})for(double y:new double[]{box.minY,box.maxY})for(double z:new double[]{box.minZ,box.maxZ}){
      var world=new Vector3d(x,y,z);if(containing!=null)containing.logicalPose().transformPosition(world);
      var p=local(state,world);var point=new MovingProgram.Bounds(p.x,p.y,p.z,p.x,p.y,p.z);
      result=result==null?point:result.union(point);
    }return result;
  }
  private static MovingProgram.Bounds declaredBounds(ServerLevel l,MovingAssemblyState state,ServerSubLevel body,FoundationData.Vehicle v){
    // Include model/rider reservation outside voxel cubes, transformed by the actual native chassis.
    NativeTrackFit.Envelope e=WingVehicle.is(v)?new NativeTrackFit.Envelope(5,3.7,.95,1.75):TrackDesign.byVariant(v.variant).nativeFit().envelope();
    var center=body.getPlot().getCenterBlock();
    return transformedBox(state,body,new AABB(center.getX()+.5-e.halfWidth(),center.getY()+.5-e.belowO()-.3125,
        center.getZ()+.5-e.halfLength(),center.getX()+.5+e.halfWidth(),center.getY()+.5+e.aboveO()-.3125,center.getZ()+.5+e.halfLength()));
  }
  private static void collectPassengers(ServerLevel l,ServerSubLevel body,Set<UUID> result){
    for(var seat:l.getEntitiesOfClass(SeatEntity.class,plotBox(body)))for(var rider:seat.getIndirectPassengers())result.add(rider.getUUID());
  }
  public static Insert inspectDock(ServerLevel level,Entry entry,MovingAssemblyState.Dock dock,String name){
    Insert result=null;
    for(var edge:dockEdges(level,dock)){
      var actual=inspectInsert(level,entry,edge,name);
      if(result!=null&&!result.equals(actual))throw new IllegalArgumentException(name+": fixed interfaces disagree");
      result=actual;
    }
    return Objects.requireNonNull(result);
  }
  /** A dock is a real gap, never a fictional fixed from-to rail. Inspect each adjacent fixed span. */
  public static List<CoasterPathEdge> dockEdges(ServerLevel level,MovingAssemblyState.Dock dock){
    var result=new ArrayList<CoasterPathEdge>();
    for(var socket:List.of(dock.from(),dock.to())){
      if(!level.hasChunkAt(socket)||!(level.getBlockEntity(socket) instanceof dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity anchor))
        throw new IllegalArgumentException("Load/repair fixed socket "+socket.toShortString());
      int count=0;
      for(var connection:anchor.getAnchorPeerCurvesView().entrySet()){
        var other=connection.getKey();
        if(other.equals(dock.from())||other.equals(dock.to()))throw new IllegalArgumentException("Remove fixed rail bridging the moving gap");
        var curve=connection.getValue();if(!curve.isPrimary())curve=curve.secondary();
        var edge=CoasterPathEdge.curve(curve.bePositions.getFirst(),curve.bePositions.getSecond(),curve);
        if(CoasterPathGraphManager.edgeHostedOnSubLevel(level,edge))throw new IllegalArgumentException("Dock approach must remain fixed world track");
        result.add(edge);count++;
      }
      if(count!=1)throw new IllegalArgumentException("Each fixed moving socket needs exactly one unbranched approach/departure span");
    }
    return List.copyOf(result);
  }
  private static MovingProgram defaultProgram(MovingAssemblyState state){throw new IllegalArgumentException("Select a finite moving program before capture");}
  private static void require(Decision d){if(!d.allowed())throw new IllegalArgumentException(d.message());}
  private static void serverThread(ServerLevel l){if(!l.getServer().isSameThread())throw new IllegalStateException("Moving admission must run on the server thread");}
}
