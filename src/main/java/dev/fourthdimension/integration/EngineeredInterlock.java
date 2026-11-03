package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** Motor callbacks must respect interlocks on every car, even between controller ticks. */
public final class EngineeredInterlock {
  private EngineeredInterlock() {}

  public static boolean holds(ServerSubLevel body, ServerLevel level) {
    // Preserve ordinary recovery-cart propulsion and its dedicated brake/fault policy.
    if (PropulsionInterlock.holds(body, level)) return true;
    var vehicle = FoundationData.get(level).vehicles.get(body.getUniqueId());
    if (vehicle == null || FoundationData.VARIANT.equals(vehicle.variant)) return false;
    var container = SubLevelContainer.getContainer(level);
    if (container == null || body.isRemoved()) return true;
    try {
      return VehicleOperations.consist(level, vehicle).stream()
          .anyMatch(
              car ->
                  unavailable(container, car.chassis)
                      || car.carriers.stream().anyMatch(c -> unavailable(container, c.body))
                      || car.family == Family.PAX
                      || car.stopping
                      || car.maintenance
                      || !car.restraints
                      || !car.fault.isEmpty());
    } catch (IllegalArgumentException unavailable) {
      return true;
    }
  }

  private static boolean unavailable(SubLevelContainer container, UUID id) {
    // Logical vehicle records survive native unload; they cannot authorize propulsion alone.
    var body = container.getSubLevel(id);
    return body == null || body.isRemoved();
  }
}
