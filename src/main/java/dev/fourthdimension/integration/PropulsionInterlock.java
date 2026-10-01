package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;

/** Shared station/maintenance hold and exclusive powered-winch ownership. */
public final class PropulsionInterlock {
  private PropulsionInterlock() {}

  public static boolean holds(ServerSubLevel body, ServerLevel level) {
    var vehicle = FoundationData.get(level).vehicles.get(body.getUniqueId());
    if (vehicle == null)
      return FoundationData.get(level).vehicles.values().stream()
          .anyMatch(
              v -> body.getUniqueId().equals(v.recoveryCart) && (v.stopping || !v.fault.isEmpty()));
    return vehicle != null
        && !FoundationData.VARIANT.equals(vehicle.variant)
        && (vehicle.stopping || vehicle.maintenance || vehicle.family == Family.PAX);
  }
}
