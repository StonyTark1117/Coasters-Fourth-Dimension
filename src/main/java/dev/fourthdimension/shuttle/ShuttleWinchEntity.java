package dev.fourthdimension.shuttle;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Persisted operating state; native constraint handles live only in ShuttleRuntime. */
public final class ShuttleWinchEntity extends KineticBlockEntity {
  public enum Phase {
    BOARDING,
    LIFTING,
    OUTBOUND,
    RETURNING,
    CAPTURING,
    FAULT
  }

  public UUID vehicle, carriage;
  public List<UUID> cars = List.of();
  public BlockPos routeStart, routeEnd;
  public double stationDistance, carriageDistance;
  public Phase phase = Phase.BOARDING, interrupted = Phase.BOARDING;
  public String fault = "";
  public double maximumDistance;
  public int cycles;

  public ShuttleWinchEntity(BlockPos pos, BlockState state) {
    super(ShuttleContent.ENTITY.get(), pos, state);
  }

  @Override
  public void tick() {
    super.tick();
    if (level instanceof ServerLevel server) ShuttleRuntime.register(server, this);
  }

  @Override
  public float calculateStressApplied() {
    return lastStressApplied = 8;
  }

  public void bind(FoundationData.Vehicle selected) {
    var server = (ServerLevel) level;
    if (phase != Phase.BOARDING || carriage != null)
      throw new IllegalArgumentException("Finish or recover the existing shuttle before rebinding");
    var consist = VehicleOperations.consist(server, selected);
    if (consist.isEmpty()
        || consist.size() > 4
        || consist.stream().anyMatch(v -> v.family != Family.PAX || !Boarding.ready(server, v)))
      throw new IllegalArgumentException("Bind one to four captured matching PAX cars");
    var data = FoundationData.get(server);
    var visited = new HashSet<BlockPos>();
    var queue = new ArrayDeque<BlockPos>();
    queue.add(selected.boundFrom);
    var terminals = new ArrayList<BlockPos>();
    while (!queue.isEmpty()) {
      var node = queue.remove();
      if (!visited.add(node)) continue;
      if (visited.size() > 257)
        throw new IllegalArgumentException("Shuttle route exceeds capacity");
      var legs = data.rails.stream().filter(r -> r.from.equals(node) || r.to.equals(node)).toList();
      if (legs.size() == 1) terminals.add(node);
      if (legs.size() > 2 || legs.stream().anyMatch(r -> r.family != Family.PAX))
        throw new IllegalArgumentException(
            "Shuttle track must be an unbranched matching PAX route");
      for (var r : legs) queue.add(r.from.equals(node) ? r.to : r.from);
    }
    if (terminals.size() != 2)
      throw new IllegalArgumentException("Build two open elevated shuttle spikes");
    var container = SubLevelContainer.getContainer(server);
    var route = ShuttleRoute.resolve(server, terminals.getFirst(), terminals.getLast());
    var point = route.locate(container.getSubLevel(selected.chassis));
    if (point.distance() > route.length / 2)
      route = ShuttleRoute.resolve(server, terminals.getLast(), terminals.getFirst());
    final var orderedRoute = route;
    var sorted =
        consist.stream()
            .sorted(
                Comparator.comparingDouble(
                    v -> orderedRoute.locate(container.getSubLevel(v.chassis)).distance()))
            .toList();
    var lead = sorted.getFirst();
    double station = route.locate(container.getSubLevel(lead.chassis)).distance();
    double height = route.sample(station).hit().point().y;
    if (station < 25
        || station + 6 * (sorted.size() - 1) + 20 > route.length
        || route.sample(3).hit().point().y < height + 10
        || route.sample(route.length - 3).hit().point().y < height + 10
        || Math.abs(route.sample(station).tangent().y) > .1)
      throw new IllegalArgumentException(
          "Board on level station track between two spikes at least ten blocks high");
    for (var other : ShuttleRuntime.winches(server))
      if (other != this
          && other.cars.stream()
              .anyMatch(id -> sorted.stream().anyMatch(v -> v.chassis.equals(id))))
        throw new IllegalArgumentException("Train already belongs to another shuttle winch");
    vehicle = lead.chassis;
    cars = sorted.stream().map(v -> v.chassis).toList();
    routeStart = route.start;
    routeEnd = route.end;
    stationDistance = carriageDistance = station;
    fault = "";
    ShuttleRuntime.register(server, this);
    changed();
  }

  public void start() {
    if (phase != Phase.BOARDING || vehicle == null)
      throw new IllegalArgumentException("Bind and capture the shuttle before starting");
    validateTrain(true);
    if (Math.abs(getSpeed()) < 16)
      throw new IllegalArgumentException("Power the shuttle winch at 16..128 RPM");
    var route = ShuttleRoute.resolve((ServerLevel) level, routeStart, routeEnd);
    var container = SubLevelContainer.getContainer(level);
    if (Math.abs(route.locate(container.getSubLevel(vehicle)).distance() - stationDistance) > .5)
      throw new IllegalArgumentException("Return the train to its bound station before starting");
    phase = Phase.LIFTING;
    maximumDistance = stationDistance;
    ShuttleRuntime.releaseBrakes((ServerLevel) level, this, true);
    changed();
  }

  public void resume() {
    if (phase != Phase.FAULT) return;
    validateTrain(false);
    ShuttleRoute.resolve((ServerLevel) level, routeStart, routeEnd);
    if (Math.abs(getSpeed()) < 16)
      throw new IllegalArgumentException("Restore winch power before resuming");
    if (interrupted != Phase.LIFTING && interrupted != Phase.CAPTURING)
      throw new IllegalArgumentException(
          "Interrupted gravity run requires manual recovery to the station");
    phase = interrupted;
    fault = "";
    ShuttleRuntime.releaseBrakes((ServerLevel) level, this, true);
    changed();
  }

  /** Inspect a stopped interrupted train, then use the physical catch to return it to boarding. */
  public void recover() {
    if (phase != Phase.FAULT)
      throw new IllegalArgumentException("Recovery requires an interrupted shuttle");
    validateTrain(false);
    var server = (ServerLevel) level;
    ShuttleRoute.resolve(server, routeStart, routeEnd);
    if (Math.abs(getSpeed()) < 16)
      throw new IllegalArgumentException("Restore winch power before recovery");
    for (var id : cars)
      if (!Boarding.ready(server, FoundationData.get(server).vehicles.get(id)))
        throw new IllegalArgumentException(
            "Stop and capture every carrier before shuttle recovery");
    phase = Phase.CAPTURING;
    fault = "";
    for (var id : cars) FoundationData.get(server).vehicles.get(id).stopping = true;
    changed();
  }

  public void validateTrain(boolean boarding) {
    var server = (ServerLevel) level;
    var data = FoundationData.get(server);
    var container = SubLevelContainer.getContainer(server);
    var v = data.vehicles.get(vehicle);
    if (v == null || container.getSubLevel(vehicle) == null)
      throw new IllegalArgumentException("Shuttle lead car unavailable");
    if (carriage != null && container.getSubLevel(carriage) == null)
      throw new IllegalArgumentException("Native lift carriage unavailable; recover the shuttle");
    var actual =
        VehicleOperations.consist(server, v).stream()
            .map(c -> c.chassis)
            .collect(java.util.stream.Collectors.toSet());
    if (!actual.equals(new HashSet<>(cars)))
      throw new IllegalArgumentException(
          "Shuttle consist changed; recover and rebind before operating");
    for (var id : cars) {
      var car = data.vehicles.get(id);
      if (car == null
          || car.family != Family.PAX
          || !car.variant.equals(dev.fourthdimension.track.TrackDesign.PAX.variant)
          || !car.restraints
          || car.maintenance
          || container.getSubLevel(id) == null
          || car.carriers.stream().anyMatch(c -> container.getSubLevel(c.body) == null)
          || (boarding && !Boarding.ready(server, car)))
        throw new IllegalArgumentException(
            "Every shuttle car must be available, retained on matching track, and restrained");
      var rail =
          data.rails.stream()
              .filter(r -> r.matches(car.boundFrom, car.boundTo))
              .findFirst()
              .orElse(null);
      if (rail == null
          || !rail.valid
          || !rail.variant.equals(car.variant)
          || dev.fourthdimension.track.TrackGeometry.resolve(server, rail) == null)
        throw new IllegalArgumentException(
            "Every shuttle car requires its matching unchanged track support");
    }
  }

  public void fail(String reason) {
    if (phase != Phase.FAULT) interrupted = phase;
    phase = Phase.FAULT;
    fault = reason;
    if (level instanceof ServerLevel server)
      for (var id : cars) {
        var v = FoundationData.get(server).vehicles.get(id);
        if (v != null) {
          v.stopping = true;
          v.restraints = true;
          v.carriers.forEach(c -> c.capture = true);
          v.fault = reason;
        }
      }
    changed();
  }

  public void changed() {
    setChanged();
    sendData();
  }

  @Override
  protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean client) {
    super.write(tag, registries, client);
    if (vehicle != null) tag.putUUID("shuttle_vehicle", vehicle);
    if (carriage != null) tag.putUUID("shuttle_carriage", carriage);
    var list = new ListTag();
    cars.forEach(id -> list.add(NbtUtils.createUUID(id)));
    tag.put("shuttle_cars", list);
    if (routeStart != null) tag.putLong("shuttle_start", routeStart.asLong());
    if (routeEnd != null) tag.putLong("shuttle_end", routeEnd.asLong());
    tag.putDouble("shuttle_station", stationDistance);
    tag.putDouble("shuttle_distance", carriageDistance);
    tag.putString("shuttle_phase", phase.name());
    tag.putString("shuttle_interrupted", interrupted.name());
    tag.putString("shuttle_fault", fault);
    tag.putDouble("shuttle_maximum", maximumDistance);
    tag.putInt("shuttle_cycles", cycles);
  }

  @Override
  protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean client) {
    super.read(tag, registries, client);
    vehicle = tag.hasUUID("shuttle_vehicle") ? tag.getUUID("shuttle_vehicle") : null;
    carriage = tag.hasUUID("shuttle_carriage") ? tag.getUUID("shuttle_carriage") : null;
    var ids = new ArrayList<UUID>();
    for (var id : tag.getList("shuttle_cars", Tag.TAG_INT_ARRAY)) ids.add(NbtUtils.loadUUID(id));
    cars = List.copyOf(ids);
    routeStart = tag.contains("shuttle_start") ? BlockPos.of(tag.getLong("shuttle_start")) : null;
    routeEnd = tag.contains("shuttle_end") ? BlockPos.of(tag.getLong("shuttle_end")) : null;
    stationDistance = tag.getDouble("shuttle_station");
    carriageDistance = tag.getDouble("shuttle_distance");
    maximumDistance = tag.getDouble("shuttle_maximum");
    cycles = tag.getInt("shuttle_cycles");
    phase =
        tag.contains("shuttle_phase")
            ? Phase.valueOf(tag.getString("shuttle_phase"))
            : Phase.BOARDING;
    interrupted =
        tag.contains("shuttle_interrupted")
            ? Phase.valueOf(tag.getString("shuttle_interrupted"))
            : phase;
    fault = tag.getString("shuttle_fault");
    if (!Double.isFinite(stationDistance)
        || !Double.isFinite(carriageDistance)
        || cars.size() > 4
        || new HashSet<>(cars).size() != cars.size())
      throw new IllegalArgumentException("Invalid persisted shuttle state");
    if (!client && phase != Phase.BOARDING && phase != Phase.FAULT) {
      interrupted = phase;
      phase = Phase.FAULT;
      fault = "Shuttle reloaded: inspect and explicitly resume or recover";
    }
  }
}
