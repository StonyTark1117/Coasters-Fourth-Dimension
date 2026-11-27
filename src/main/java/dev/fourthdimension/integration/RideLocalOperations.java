package dev.fourthdimension.integration;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

/**
 * Bounded ride-local block controller for the first multi-train route.  It deliberately owns
 * only reservations and admission; native propulsion and station braking remain in their
 * existing owners.
 */
public final class RideLocalOperations {
  private static final Logger LOGGER = LogUtils.getLogger();
  public static final String ROUTE_ID = "arrow_fixed_circuit_v1";
  public static final int SEGMENTS = 8;
  /** Six blocks let three trains each hold a disjoint current/next pair. */
  public static final int BLOCKS = 6;
  public static final int MAX_TRAINS = 3;
  public static final Family FAMILY = Family.ARROW;

  public record RouteStatus(boolean managed, boolean permitted, UUID owner, int currentBlock,
      int nextBlock, String reason, boolean freshRequestRequired) {}
  /** Observed route shape/progression only. Native RideLocalState remains the sole reservation owner.
   * No saved observation can grant travel after reload; explicit stopped recovery reconstructs it. */
  private record ExternalObservation(FoundationData.RideLocalTrain train, List<UUID> rails,
      List<Long> geometry, int railIndex) {}
  private static final Map<ServerLevel, Map<UUID, ExternalObservation>> EXTERNAL = new WeakHashMap<>();
  private record RailShape(UUID id, net.minecraft.core.BlockPos from, net.minecraft.core.BlockPos to,
      long declaredGeometry, long revision) {}
  private record GeometryCache(int graphRevision,long handleRevision,List<RailShape> shape,List<Long> geometry) {}
  private static final Map<ServerLevel,GeometryCache> GEOMETRY = new WeakHashMap<>();

  private RideLocalOperations() {}

  public static FoundationData.RideLocalTrain routeTrain(ServerLevel l,UUID id) {
    return FoundationData.get(l).rideLocal.trains.values().stream()
        .filter(t->t.chassis.equals(id)||t.cars.contains(id)).findFirst().orElse(null);
  }
  public static boolean hasRouteInterest(ServerLevel l,UUID id) {
    var v=FoundationData.get(l).vehicles.get(id);
    return routeTrain(l,id)!=null || v!=null && isManaged(l,v);
  }
  private static List<FoundationData.Rail> externalRails(ServerLevel l) {
    var data=FoundationData.get(l); var state=data.rideLocal;
    if(!ROUTE_ID.equals(state.routeId)||state.rails.size()!=SEGMENTS
        ||new HashSet<>(state.rails).size()!=SEGMENTS||state.trains.size()>MAX_TRAINS)
      throw new IllegalArgumentException("Fixed Arrow route shape or capacity changed");
    var rails=new ArrayList<FoundationData.Rail>();
    for(var id:state.rails) {
      var rail=data.rails.stream().filter(r->r.id.equals(id)).findFirst().orElse(null);
      if(rail==null||!rail.valid||rail.family!=FAMILY||!rail.original()||TrackGeometry.resolve(l,rail)==null)
        throw new IllegalArgumentException("Fixed Arrow route support unavailable");
      rails.add(rail);
    }
    for(int i=0;i<rails.size();i++) {
      var a=rails.get(i); var previous=rails.get((i+SEGMENTS-1)%SEGMENTS); var next=rails.get((i+1)%SEGMENTS);
      boolean fromNext=next.from.equals(a.from)||next.to.equals(a.from);
      boolean toNext=next.from.equals(a.to)||next.to.equals(a.to);
      boolean fromPrevious=previous.from.equals(a.from)||previous.to.equals(a.from);
      boolean toPrevious=previous.from.equals(a.to)||previous.to.equals(a.to);
      if(fromNext==toNext||fromPrevious==toPrevious||fromNext==fromPrevious)
        throw new IllegalArgumentException("Fixed Arrow route is no longer one ordered closed loop");
    }
    return rails;
  }
  private static int railIndex(List<FoundationData.Rail> rails,FoundationData.Vehicle v) {
    for(int i=0;i<rails.size();i++) if(rails.get(i).matches(v.boundFrom,v.boundTo)) return i;
    throw new IllegalArgumentException("Train left its declared fixed route");
  }
  public static void validateExternalDirection(ServerLevel l,FoundationData.Vehicle v,int direction) {
    var rails=externalRails(l); int index=railIndex(rails,v);
    var rail=rails.get(index); var next=rails.get((index+1)%SEGMENTS);
    int expected=next.from.equals(rail.to)||next.to.equals(rail.to)?1:-1;
    if(direction!=expected) throw new IllegalArgumentException("Equipment direction opposes or leaves the native route");
    var container=SubLevelContainer.getContainer(l);
    if(container==null||!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body)||body.isRemoved())
      throw new IllegalArgumentException("Complete native route bodies required");
    var center=CoasterCartResolve.cartCenterWorld(body); var edge=TrackGeometry.resolve(l,rail);
    if(center==null||edge==null) throw new IllegalArgumentException("Native route position unavailable");
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null) throw new IllegalArgumentException("Native route contact unavailable");
    var forward=CoasterPathTrackFrame.trackBasisAtHit(l,hit).forward();
    double signed=container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d())
        .dot(forward.x,forward.y,forward.z)*(edge.from().equals(rail.from)?1:-1)*expected;
    if(signed < -.05) throw new IllegalArgumentException("Route rollback retains its stopping reservation");
  }
  private static List<Long> geometry(ServerLevel l,List<FoundationData.Rail> rails) {
    // Cache only expensive curve sampling. Live loaded/valid rails, bodies, ownership,
    // direction and velocity are still checked at every permit, including within a tick.
    int revision=dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.get(l).revision();
    long handles=dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths.revision();
    var shape=rails.stream().map(r->new RailShape(r.id,r.from,r.to,r.geometryHash,r.revision)).toList();
    var cached=GEOMETRY.get(l);
    if(cached!=null&&cached.graphRevision()==revision&&cached.handleRevision()==handles&&cached.shape().equals(shape))
      return cached.geometry();
    var value=rails.stream().map(r->TrackGeometry.fingerprint(l,TrackGeometry.resolve(l,r))).toList();
    GEOMETRY.put(l,new GeometryCache(revision,handles,shape,value)); return value;
  }
  public static void acceptExternal(ServerLevel l,FoundationData.Vehicle v) {
    var train=routeTrain(l,v.chassis); var rails=externalRails(l);
    if(train==null) throw new IllegalArgumentException("Native route admission required");
    validateExternalConsist(l,train);
    if(train.braking||train.unresolved||train.restored) throw new IllegalArgumentException("Route requires explicit recovery");
    EXTERNAL.computeIfAbsent(l,k->new HashMap<>()).put(train.chassis,
        new ExternalObservation(train,List.copyOf(FoundationData.get(l).rideLocal.rails),geometry(l,rails),railIndex(rails,v)));
  }
  static void validateExternalConsist(ServerLevel l,FoundationData.RideLocalTrain train) {
    var data=FoundationData.get(l); var lead=data.vehicles.get(train.chassis);
    if(lead==null) throw new IllegalArgumentException("Admitted native train unavailable");
    validateHomogeneous(l,lead);
    var cars=VehicleOperations.consist(l,lead);
    if(!new HashSet<>(train.cars).equals(new HashSet<>(cars.stream().map(v->v.chassis).toList())))
      throw new IllegalArgumentException("Admitted route consist changed");
    var container=SubLevelContainer.getContainer(l);
    for(var car:cars) {
      var bodies=new ArrayList<UUID>(); bodies.add(car.chassis); car.carriers.forEach(c->bodies.add(c.body));
      for(var id:bodies) if(id==null||!(container.getSubLevel(id) instanceof ServerSubLevel body)||body.isRemoved())
        throw new IllegalArgumentException("Complete native route bodies required");
    }
  }
  private static void validateExternalObservation(ServerLevel l,FoundationData.RideLocalTrain train) {
    var data=FoundationData.get(l); var state=data.rideLocal;
    var observed=EXTERNAL.getOrDefault(l,Map.of()).get(train.chassis);
    if(observed==null||observed.train()!=train) throw new IllegalArgumentException("Route observation restored; explicit recovery required");
    int expectedBlock=observed.railIndex()%BLOCKS, expectedNext=(observed.railIndex()+1)%SEGMENTS%BLOCKS;
    var owned=new HashSet<Integer>();
    state.reservations.forEach((block,owner)->{if(train.chassis.equals(owner)) owned.add(block);});
    if(train.block!=expectedBlock||train.nextBlock!=expectedNext||!owned.equals(Set.of(expectedBlock,expectedNext)))
      throw new IllegalArgumentException("Native current/next reservation pair changed");
    if(!train.chassis.equals(state.blockOwner(train.block))||!train.chassis.equals(state.blockOwner(train.nextBlock)))
      throw new IllegalArgumentException("Native current/next reservation changed");
    validateExternalConsist(l,train); var rails=externalRails(l);
    if(!observed.rails().equals(state.rails)||!observed.geometry().equals(geometry(l,rails)))
      throw new IllegalArgumentException("Native route geometry changed");
    int index=railIndex(rails,data.vehicles.get(train.chassis));
    if(index!=observed.railIndex() && index!=(observed.railIndex()+1)%SEGMENTS)
      throw new IllegalArgumentException("Native route progression changed or reversed");
  }
  public static void validateRetainedExternal(ServerLevel l,FoundationData.Vehicle v,int direction) {
    var train=routeTrain(l,v.chassis);
    if(train==null) throw new IllegalArgumentException("Native route admission required for handoff");
    validateExternalObservation(l,train);
    validateExternalDirection(l,v,direction);
  }
  /** Refresh safety and native reconciliation, without creating admission or resuming a held train. */
  public static RouteStatus externalStatus(ServerLevel l,UUID id,int direction) {
    if(!hasRouteInterest(l,id)) return new RouteStatus(false,false,null,-1,-1,"",false);
    var train=routeTrain(l,id);
    if(train==null) return new RouteStatus(true,false,null,-1,-1,"Native route admission required",true);
    try {
      var v=FoundationData.get(l).vehicles.get(id);
      if(v==null) throw new IllegalArgumentException("Admitted native vehicle unavailable");
      validateExternalObservation(l,train);
      validateExternalDirection(l,v,direction);
      if(train.restored||train.unresolved||train.braking)
        throw new IllegalArgumentException("Native route held; explicit Resume and a fresh request required");
      reconcile(l);
      validateExternalObservation(l,train);
      if(train.restored||train.unresolved||train.braking)
        throw new IllegalArgumentException("Native next block denied; stopping reservation retained");
      return new RouteStatus(true,true,train.chassis,train.block,train.nextBlock,"",false);
    } catch(IllegalArgumentException denied) {
      train.braking=true; emergencyBrakeKnown(l,train); FoundationData.get(l).setDirty();
      return new RouteStatus(true,false,train.chassis,train.block,train.nextBlock,denied.getMessage(),true);
    }
  }

  public static void configure(ServerLevel level, List<UUID> orderedRails) {
    if (orderedRails.size() != SEGMENTS || new HashSet<>(orderedRails).size() != SEGMENTS)
      throw new IllegalArgumentException("The fixed Arrow circuit requires exactly eight unique segments");
    var data = FoundationData.get(level);
    for (var id : orderedRails) {
      var rail = data.rails.stream().filter(r -> r.id.equals(id)).findFirst().orElse(null);
      if (rail == null || rail.family != FAMILY || !rail.original())
        throw new IllegalArgumentException("The fixed circuit requires original Arrow segments");
    }
    var rails = orderedRails.stream().map(id -> data.rails.stream().filter(r -> r.id.equals(id)).findFirst().orElseThrow()).toList();
    for (int i = 0; i < rails.size(); i++) {
      var a = rails.get(i); var b = rails.get((i + 1) % rails.size());
      if (!(a.to.equals(b.from) || a.to.equals(b.to) || a.from.equals(b.from) || a.from.equals(b.to)))
        throw new IllegalArgumentException("The fixed circuit segments must form one closed connected loop");
    }
    data.rideLocal.configure(ROUTE_ID, orderedRails);
    data.setDirty();
  }

  public static void ensureConfigured(ServerLevel level, FoundationData.Vehicle vehicle) {
    var state = FoundationData.get(level).rideLocal;
    if (!state.routeId.isEmpty()) {
      if (!ROUTE_ID.equals(state.routeId)) throw new IllegalArgumentException("Unknown ride-local route");
      return;
    }
    if (vehicle.family != FAMILY || !vehicle.original())
      throw new IllegalArgumentException("The bounded route supports original Arrow trains only");
    var data = FoundationData.get(level);
    var start = data.rails.stream()
        .filter(r -> r.family == FAMILY && r.original() && r.matches(vehicle.boundFrom, vehicle.boundTo))
        .findFirst().orElse(null);
    if (start == null) throw new IllegalArgumentException("Arrow train is not bound to a fixed route segment");
    var ordered = new ArrayList<UUID>();
    var current = start;
    for (int i = 0; i < SEGMENTS; i++) {
      ordered.add(current.id);
      var endpoint = current.to;
      var next = data.rails.stream().filter(r -> r.family == FAMILY && r.original() && !ordered.contains(r.id)
          && (r.from.equals(endpoint) || r.to.equals(endpoint))).findFirst().orElse(null);
      if (next == null) break;
      current = next;
    }
    configure(level, ordered);
  }

  /** Admission is separate from propulsion: it reserves the current and next block. */
  public static void admit(ServerLevel level, FoundationData.Vehicle lead) {
    validateHomogeneous(level, lead);
    ensureConfigured(level, lead);
    var state = FoundationData.get(level).rideLocal;
    var train = state.trains.get(lead.chassis);
    if (train != null) {
      if (train.unresolved || train.restored) throw new IllegalArgumentException("Train state requires explicit recovery");
      return;
    }
    if (state.trains.size() >= MAX_TRAINS) throw new IllegalArgumentException("Fixed route train limit reached (3)");
    int block = blockFor(level, lead);
    validateTrainSpan(level, lead, block);
    int next = nextBlock(level, lead);
    if (state.blockOwner(block) != null || state.blockOwner(next) != null)
      throw new IllegalArgumentException("Route block occupied; station dispatch remains braked");
    var cars = VehicleOperations.consist(level, lead).stream().map(v -> v.chassis).toList();
    var admitted = new FoundationData.RideLocalTrain(lead.chassis, cars, block); admitted.nextBlock = next;
    state.trains.put(lead.chassis, admitted);
    state.reserve(block, lead.chassis); state.reserve(next, lead.chassis);
    state.restored = false;
    FoundationData.get(level).setDirty();
  }

  public static void beforeDispatch(ServerLevel level, FoundationData.Vehicle lead) {
    if (!isManaged(level, lead)) return;
    admit(level, lead);
    var state = FoundationData.get(level).rideLocal;
    var train = state.trains.get(lead.chassis);
    if (train.unresolved || train.braking || train.restored)
      throw new IllegalArgumentException("Route reservation is braking or unresolved; dispatch denied");
  }

  public static void capture(ServerLevel level, FoundationData.Vehicle lead) {
    var state = FoundationData.get(level).rideLocal;
    var train = state.trains.get(lead.chassis);
    if (train != null) { train.braking = true; state.restored = false; FoundationData.get(level).setDirty(); }
  }

  /** Removes a route train and its reservations after the vehicle lifecycle permanently deletes it. */
  public static void onPermanentRemoval(ServerLevel level, UUID chassis) {
    var state = FoundationData.get(level).rideLocal;
    if (state.trains.entrySet().removeIf(e -> e.getValue().chassis.equals(chassis)))
      state.reservations.entrySet().removeIf(e -> e.getValue().equals(chassis));
  }

  public static void releaseAfterStop(ServerLevel level, FoundationData.Vehicle lead) {
    var state = FoundationData.get(level).rideLocal;
    var container = SubLevelContainer.getContainer(level);
    if (dataVehiclePresent(level, lead.chassis, container))
      throw new IllegalArgumentException("Stop and remove the complete native train before releasing its route blocks");
    var train = state.trains.remove(lead.chassis);
    if (train != null) { state.release(lead.chassis); FoundationData.get(level).setDirty(); }
  }

  public static void reconcile(ServerLevel level) {
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    for (var train : new ArrayList<>(data.rideLocal.trains.values())) {
      var lead = data.vehicles.get(train.chassis);
      if (lead == null || container == null || !(container.getSubLevel(train.chassis) instanceof ServerSubLevel body) || body.isRemoved()) {
        if (Boolean.getBoolean("fourthdimension.validation") && !train.braking)
          LOGGER.info("FD_RIDE_RECONCILE_BRAKE chassis={} reason=missing-body leadPresent={} containerPresent={} bodyPresent={} removed={}",
              train.chassis, lead != null, container != null,
              container != null && container.getSubLevel(train.chassis) != null,
              container != null && container.getSubLevel(train.chassis) instanceof ServerSubLevel missingBody && missingBody.isRemoved());
        train.unresolved = true; train.braking = true;
        emergencyBrakeKnown(level, train);
        continue;
      }
      try {
        validateHomogeneous(level, lead);
        if(RideAuthority.externalSelected(lead) && EXTERNAL.getOrDefault(level,Map.of()).containsKey(train.chassis))
          validateExternalObservation(level,train);
        int current = blockFor(level, lead);
        validateTrainSpan(level, lead, current);
        if (!train.braking && current != train.block) {
          UUID nextOwner = data.rideLocal.blockOwner(nextBlock(level, lead));
          if (nextOwner != null && !nextOwner.equals(train.chassis)) {
            if (Boolean.getBoolean("fourthdimension.validation"))
              LOGGER.info("FD_RIDE_RECONCILE_BRAKE chassis={} reason=occupied-next current={} next={} nextOwner={} storedBlock={} position={}",
                  train.chassis, current, nextBlock(level, lead), nextOwner, train.block, body.logicalPose().position());
            train.braking = true;
            emergencyBrakeKnown(level, train);
          } else {
            data.rideLocal.release(train.chassis);
            train.block = current; train.nextBlock = nextBlock(level, lead);
            data.rideLocal.reserve(current, train.chassis);
            data.rideLocal.reserve(nextBlock(level, lead), train.chassis);
            var observation=EXTERNAL.getOrDefault(level,Map.of()).get(train.chassis);
            if(observation!=null) EXTERNAL.get(level).put(train.chassis,new ExternalObservation(train,
                observation.rails(),observation.geometry(),railIndex(externalRails(level),lead)));
          }
        }
      } catch (IllegalArgumentException e) {
        if (Boolean.getBoolean("fourthdimension.validation") && !train.braking)
          LOGGER.info("FD_RIDE_RECONCILE_BRAKE chassis={} reason=validation current=unavailable storedBlock={} position={} message={}",
              train.chassis, train.block, body.logicalPose().position(), e.getMessage());
        train.unresolved = true; train.braking = true; emergencyBrakeKnown(level, train);
      }
    }
    data.setDirty();
  }

  public static void resume(ServerLevel level, UUID chassis) {
    var data = FoundationData.get(level); var train = data.rideLocal.trains.get(chassis);
    var lead = data.vehicles.get(chassis);
    if (train == null || lead == null) throw new IllegalArgumentException("Unknown train identity cannot resume");
    validateHomogeneous(level, lead);
    int block = blockFor(level, lead); validateTrainSpan(level, lead, block);
    UUID owner = data.rideLocal.blockOwner(block);
    int next = nextBlock(level, lead);
    UUID nextOwner = data.rideLocal.blockOwner(next);
    if ((owner != null && !owner.equals(chassis)) || (nextOwner != null && !nextOwner.equals(chassis)))
      throw new IllegalArgumentException("Route block occupied; recovery remains braked");
    data.rideLocal.release(chassis); train.block = block; train.nextBlock = next;
    data.rideLocal.reserve(block, chassis); data.rideLocal.reserve(next, chassis);
    train.restored = false; train.unresolved = false; train.braking = false;
    data.setDirty();
  }

  public static boolean isManaged(ServerLevel level, FoundationData.Vehicle vehicle) {
    var state = FoundationData.get(level).rideLocal;
    if (!ROUTE_ID.equals(state.routeId) || vehicle.family != FAMILY || !vehicle.original()) return false;
    return state.rails.stream().anyMatch(id -> FoundationData.get(level).rails.stream()
        .filter(r -> r.id.equals(id)).anyMatch(r -> r.matches(vehicle.boundFrom, vehicle.boundTo)));
  }

  private static void emergencyBrakeKnown(ServerLevel level, FoundationData.RideLocalTrain train) {
    var data = FoundationData.get(level); var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    for (var id : train.cars) {
      var vehicle = data.vehicles.get(id); if (vehicle == null) continue;
      vehicle.stopping = true; vehicle.carriers.forEach(c -> c.capture = true);
      var body = container.getSubLevel(id);
      if (body instanceof ServerSubLevel nativeBody) container.physicsSystem().getPipeline().resetVelocity(nativeBody);
    }
  }

  private static void validateTrainSpan(ServerLevel level, FoundationData.Vehicle lead, int current) {
    var blocks = new HashSet<Integer>();
    for (var car : VehicleOperations.consist(level, lead)) {
      int block = blockFor(level, car); blocks.add(block);
    }
    if (blocks.isEmpty() || blocks.size() > 2 || !blocks.contains(current))
      throw new IllegalArgumentException("Complete Arrow consist exceeds the two-block reservation span");
    int next = nextBlock(level, lead);
    for (int block : blocks)
      if (block != current && block != next)
        throw new IllegalArgumentException("Complete Arrow consist crosses an unreserved block boundary");
  }

  private static void validateHomogeneous(ServerLevel level, FoundationData.Vehicle lead) {
    if (lead.family != FAMILY || !lead.original()) throw new IllegalArgumentException("Mixed or unsupported consist");
    var cars = VehicleOperations.consist(level, lead);
    if (cars.isEmpty() || cars.stream().anyMatch(v -> v.family != FAMILY || !v.original()))
      throw new IllegalArgumentException("Every linked car must be an original Arrow car");
    var container = SubLevelContainer.getContainer(level);
    if (container == null) throw new IllegalArgumentException("Complete native train bodies required");
    for (var car : cars) {
      if (car.carriers.isEmpty() || car.carriers.stream().anyMatch(c -> c.occupants == null || c.riderSeats == null))
        throw new IllegalArgumentException("Whole-consist occupancy is unresolved");
      if (container.getSubLevel(car.chassis) == null)
        throw new IllegalArgumentException("Complete native train bodies required");
    }
  }

  private static int blockFor(ServerLevel level, FoundationData.Vehicle lead) {
    var state = FoundationData.get(level); var rail = state.rails.stream()
        .filter(r -> r.matches(lead.boundFrom, lead.boundTo)).findFirst().orElse(null);
    if (rail == null) throw new IllegalArgumentException("Train is not on a declared route segment");
    int index = state.rideLocal.rails.indexOf(rail.id); if (index < 0) throw new IllegalArgumentException("Train is outside fixed route");
    return index % BLOCKS;
  }

  private static int nextBlock(ServerLevel level, FoundationData.Vehicle lead) {
    var state = FoundationData.get(level).rideLocal;
    var rail = state.rails.indexOf(FoundationData.get(level).rails.stream()
        .filter(r -> r.matches(lead.boundFrom, lead.boundTo)).findFirst().orElseThrow().id);
    return (rail + 1) % SEGMENTS % BLOCKS;
  }

  private static boolean dataVehiclePresent(ServerLevel level, UUID chassis, SubLevelContainer container) {
    if (container == null) return false;
    var body = container.getSubLevel(chassis);
    return body != null && !body.isRemoved();
  }
}
