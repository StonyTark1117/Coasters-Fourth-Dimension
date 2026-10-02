package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;

/** Motor callbacks must respect interlocks on every car, even between controller ticks. */
public final class EngineeredInterlock {
  private EngineeredInterlock() {}

  public static boolean holds(ServerSubLevel body, ServerLevel level) {
    // Preserve ordinary recovery-cart propulsion and its dedicated brake/fault policy.
    if (PropulsionInterlock.holds(body, level)) return true;
    var vehicle = FoundationData.get(level).vehicles.get(body.getUniqueId());
    if (vehicle == null || FoundationData.VARIANT.equals(vehicle.variant)) return false;
    try {
      return VehicleOperations.consist(level, vehicle).stream()
          .anyMatch(
              car ->
                  car.family == Family.PAX
                      || car.stopping
                      || car.maintenance
                      || !car.restraints
                      || !car.fault.isEmpty());
    } catch (IllegalArgumentException unavailable) {
      return true;
    }
  }
}
