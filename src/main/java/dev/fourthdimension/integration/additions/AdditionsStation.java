package dev.fourthdimension.integration.additions;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.plushivr.coasterfins.block.StationBlockEntity;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.server.level.ServerLevel;

/** Ephemeral station state. Only a current native span scan grants operation authority. */
public final class AdditionsStation {
  public interface Access {
    AdditionsStation fourthdimension$station();
  }

  public RideTelemetry telemetry = RideTelemetry.unavailable("No 4D vehicle in station span");
  private UUID bound;
  private UUID authorized;
  private long arrival;
  private int readyTicks;
  private boolean dispatched;
  private boolean preLatched;

  public boolean authorizes(UUID chassis) {
    return chassis != null && chassis.equals(authorized);
  }

  public void reset(StationBlockEntity station) {
    bound = null;
    authorized = null;
    readyTicks = 0;
    dispatched = false;
    preLatched = false;
    publish(station, RideTelemetry.unavailable("No 4D vehicle in station span"));
  }

  public boolean update(StationBlockEntity station, Set<SubLevel> members, boolean preWaiting) {
    if (!(station.getLevel() instanceof ServerLevel level)) return false;
    var data = FoundationData.get(level);
    var ids = new HashSet<UUID>();
    members.forEach(body -> ids.add(body.getUniqueId()));
    var selected =
        ids.stream()
            .map(data.vehicles::get)
            .filter(Objects::nonNull)
            .filter(v -> !FoundationData.VARIANT.equals(v.variant))
            .sorted(Comparator.comparing(v -> v.chassis))
            .findFirst()
            .orElse(null);
    if (selected == null) {
      reset(station);
      return false;
    }
    var next = RideTelemetry.sample(level, selected.chassis);
    String bindingError = "";
    try {
      var consist = VehicleOperations.consist(level, selected);
      var chassis = new HashSet<UUID>();
      consist.forEach(v -> chassis.add(v.chassis));
      if (!ids.containsAll(chassis)
          || members.stream()
              .anyMatch(
                  body ->
                      CoasterCartResolve.isCartSubLevel(body)
                          && !chassis.contains(body.getUniqueId())))
        bindingError = "Station span must contain exactly one complete 4D consist";
    } catch (IllegalArgumentException rejected) {
      bindingError = rejected.getMessage();
    }
    if (!bindingError.isEmpty()) next = RideTelemetry.unavailable(bindingError);
    if (!Objects.equals(bound, selected.chassis)) {
      bound = selected.chassis;
      arrival = level.getGameTime();
      readyTicks = 0;
      dispatched = false;
      preLatched = false;
    }
    preLatched |= preWaiting;
    if (selected.family == Family.PAX) bindingError = "Operate PAX through its bound shuttle winch";
    if (!bindingError.isEmpty()) next = next.denyDispatch(bindingError);
    if (!next.fault().isBlank()) next = next.denyDispatch(next.fault());
    int weight =
        next.capacity() <= 0
            ? 1
            : Math.max(1, Math.min(15, 1 + Math.round(14f * next.occupied() / next.capacity())));
    if (station.isDispatchEnabled() && !dispatched && bindingError.isEmpty()) {
      authorized = selected.chassis;
      try {
        if (!selected.stopping) {
          RideOperations.request(
              level, station.getBlockPos(), selected.chassis, RideOperations.Request.CAPTURE);
          next = RideTelemetry.sample(level, selected.chassis);
        }
        boolean ready =
            next.dispatchReady()
                && next.fault().isBlank()
                && weight >= station.getDispatchThreshold()
                && level.getGameTime() - arrival >= station.getDispatchCooldownSeconds() * 20L;
        readyTicks = ready ? readyTicks + 1 : 0;
        if (ready && readyTicks >= station.getDispatchDelaySeconds() * 20) {
          RideOperations.request(
              level, station.getBlockPos(), selected.chassis, RideOperations.Request.DISPATCH);
          dispatched = true;
          next = RideTelemetry.sample(level, selected.chassis);
        }
      } catch (IllegalArgumentException rejected) {
        readyTicks = 0;
        next = next.denyDispatch(rejected.getMessage());
      } finally {
        authorized = null;
      }
    } else readyTicks = 0;
    if (!next.fault().isBlank()) next = next.denyDispatch(next.fault());
    // Occupied releases only after successful dispatch. Bars reports actual closed restraints.
    station.channel(StationBlockEntity.CH_OCCUPIED).setPower(dispatched ? 0 : 15);
    station.channel(StationBlockEntity.CH_WEIGHT).setPower(weight);
    station.channel(StationBlockEntity.CH_BARS).setPower(next.restraintsClosed() ? 15 : 0);
    station.channel(StationBlockEntity.CH_PRE).setPower(preLatched ? 15 : 0);
    publish(station, next);
    return true;
  }

  private void publish(StationBlockEntity station, RideTelemetry next) {
    if (!telemetry.equals(next)) {
      telemetry = next;
      station.sendData();
    }
  }
}
