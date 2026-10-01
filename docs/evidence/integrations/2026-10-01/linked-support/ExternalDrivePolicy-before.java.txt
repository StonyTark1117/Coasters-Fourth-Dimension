package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** External propulsion cannot release a held consist or compete with the PAX catch. */
public final class ExternalDrivePolicy {
  private ExternalDrivePolicy() {}

  public static boolean allows(ServerLevel level, UUID chassis) {
    var v = FoundationData.get(level).vehicles.get(chassis);
    if (v == null || FoundationData.VARIANT.equals(v.variant)) return true;
    try {
      return VehicleOperations.consist(level, v).stream()
          .allMatch(
              car ->
                  car.family != Family.PAX
                      && !car.stopping
                      && !car.maintenance
                      && car.restraints
                      && car.fault.isBlank());
    } catch (IllegalArgumentException missing) {
      return false;
    }
  }
}
