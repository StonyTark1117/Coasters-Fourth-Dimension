package dev.fourthdimension.integration;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
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

  private RideLocalOperations() {}

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
