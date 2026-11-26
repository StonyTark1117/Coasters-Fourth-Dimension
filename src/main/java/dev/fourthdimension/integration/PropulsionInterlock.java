package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** Shared propulsion holds, including native availability between controller ticks. */
public final class PropulsionInterlock {
  private PropulsionInterlock() {}

  public static boolean holds(ServerSubLevel body, ServerLevel level) {
    var data = FoundationData.get(level);
    if (data.moving.retaining(body.getUniqueId()) != null) return true;
    var vehicle = data.vehicles.get(body.getUniqueId());
    if (vehicle == null) {
      var recoveryCars =
          data.vehicles.values().stream()
              .filter(v -> body.getUniqueId().equals(v.recoveryCart))
              .toList();
      // Ordinary carts without a Fourth Dimension tow retain upstream behavior.
      if (recoveryCars.isEmpty()) return false;
      var container = SubLevelContainer.getContainer(level);
      if (!liveSource(container, body)) return true;
      // Maintenance retention is required for towing; it must not itself block the bearing.
      return recoveryCars.stream()
          .anyMatch(
              car ->
                  car.recoveryCartRemoved
                      || car.stopping
                      || !car.fault.isEmpty()
                      || componentsUnavailable(container, car)
                      || !CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                              container.getSubLevel(car.chassis))
                          .contains(body.getUniqueId()));
    }
    if (FoundationData.VARIANT.equals(vehicle.variant)) return false;
    if (RideAuthority.holds(level, vehicle)) return true;
    if (vehicle.stopping || vehicle.maintenance || vehicle.family == Family.PAX) return true;
    var container = SubLevelContainer.getContainer(level);
    if (!liveSource(container, body)) return true;
    try {
      return VehicleOperations.consist(level, vehicle).stream()
          .anyMatch(car -> componentsUnavailable(container, car));
    } catch (IllegalArgumentException unavailable) {
      return true;
    }
  }

  private static boolean liveSource(SubLevelContainer container, ServerSubLevel body) {
    return container != null
        && !body.isRemoved()
        && container.getSubLevel(body.getUniqueId()) == body;
  }

  private static boolean componentsUnavailable(
      SubLevelContainer container, FoundationData.Vehicle car) {
    return unavailable(container, car.chassis)
        || dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.is(car)
            && (car.yaw.deck == null || unavailable(container, car.yaw.deck))
        || car.carriers.stream().anyMatch(carrier -> unavailable(container, carrier.body));
  }

  private static boolean unavailable(SubLevelContainer container, UUID id) {
    var body = container.getSubLevel(id);
    return body == null || body.isRemoved();
  }
}
