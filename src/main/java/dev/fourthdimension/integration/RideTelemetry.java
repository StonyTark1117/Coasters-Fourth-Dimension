package dev.fourthdimension.integration;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.ShuttleRuntime;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/** Immutable consist snapshot, sampled on the server thread. It never grants command authority. */
public record RideTelemetry(
    boolean available,
    String family,
    int cars,
    int occupied,
    int capacity,
    boolean captured,
    boolean boardingReady,
    boolean restraintsClosed,
    boolean retained,
    boolean dispatchReady,
    String dispatchDenial,
    String fault,
    String phase) {
  public static RideTelemetry unavailable(String reason) {
    return new RideTelemetry(
        false, "", 0, 0, 0, false, false, false, false, false, reason, reason, "");
  }

  public static RideTelemetry sample(ServerLevel level, UUID id) {
    if (!level.getServer().isSameThread())
      throw new IllegalStateException("Sample ride telemetry on the server thread");
    var selected = id == null ? null : FoundationData.get(level).vehicles.get(id);
    if (selected == null) return unavailable("No bound vehicle");
    final List<FoundationData.Vehicle> cars;
    try {
      cars = VehicleOperations.consist(level, selected);
    } catch (IllegalArgumentException missing) {
      return unavailable(missing.getMessage());
    }
    var container = SubLevelContainer.getContainer(level);
    if (container == null
        || cars.stream()
            .anyMatch(
                v ->
                    container.getSubLevel(v.chassis) == null
                        || v.carriers.stream()
                            .anyMatch(c -> container.getSubLevel(c.body) == null)))
      return unavailable("Vehicle or carrier unavailable");
    int seats = cars.stream().flatMap(v -> v.carriers.stream()).mapToInt(c -> c.seats.size()).sum();
    int occupants =
        (int)
            cars.stream()
                .flatMap(v -> v.carriers.stream())
                .flatMap(c -> c.occupants.stream())
                .distinct()
                .count();
    boolean captured = cars.stream().flatMap(v -> v.carriers.stream()).allMatch(c -> c.capture);
    boolean ready = cars.stream().allMatch(v -> Boarding.ready(level, v));
    String denial = "";
    try {
      VehicleOperations.validateDispatch(level, selected);
    } catch (IllegalArgumentException rejected) {
      denial = rejected.getMessage();
    }
    var winch = ShuttleRuntime.owner(level, id);
    String fault = cars.stream().map(v -> v.fault).filter(f -> !f.isBlank()).findFirst().orElse("");
    if (winch != null && !winch.fault.isBlank()) fault = winch.fault;
    return new RideTelemetry(
        true,
        selected.family.id(),
        cars.size(),
        occupants,
        seats,
        captured,
        ready,
        cars.stream().allMatch(v -> v.restraints),
        cars.stream().anyMatch(v -> v.maintenance),
        denial.isEmpty(),
        denial,
        fault,
        winch == null
            ? (ready ? "BOARDING" : captured ? "CAPTURING" : "RUNNING")
            : winch.phase.name());
  }

  public RideTelemetry denyDispatch(String reason) {
    return new RideTelemetry(
        available,
        family,
        cars,
        occupied,
        capacity,
        captured,
        boardingReady,
        restraintsClosed,
        retained,
        false,
        reason,
        fault,
        phase);
  }

  public CompoundTag save() {
    var tag = new CompoundTag();
    tag.putBoolean("available", available);
    tag.putString("family", family);
    tag.putInt("cars", cars);
    tag.putInt("occupied", occupied);
    tag.putInt("capacity", capacity);
    tag.putBoolean("captured", captured);
    tag.putBoolean("boarding", boardingReady);
    tag.putBoolean("restraints", restraintsClosed);
    tag.putBoolean("retained", retained);
    tag.putBoolean("dispatch", dispatchReady);
    tag.putString("denial", dispatchDenial);
    tag.putString("fault", fault);
    tag.putString("phase", phase);
    return tag;
  }

  public static RideTelemetry load(CompoundTag tag) {
    return new RideTelemetry(
        tag.getBoolean("available"),
        tag.getString("family"),
        tag.getInt("cars"),
        tag.getInt("occupied"),
        tag.getInt("capacity"),
        tag.getBoolean("captured"),
        tag.getBoolean("boarding"),
        tag.getBoolean("restraints"),
        tag.getBoolean("retained"),
        tag.getBoolean("dispatch"),
        tag.getString("denial"),
        tag.getString("fault"),
        tag.getString("phase"));
  }
}
