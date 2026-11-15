package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.RecoveryOperations;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** Motor callbacks must respect interlocks on every car, even between controller ticks. */
public final class EngineeredInterlock {
  /** Leaves headroom below the recovery hard-stop while an optional motor callback is active. */
  public static final double RECOVERY_TARGET_SPEED = .5;
  private static final ThreadLocal<UUID> VALIDATION_PAX_MOTOR_HOLD = new ThreadLocal<>();

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
                      || car.family == Family.SPINNING || car.family == Family.SUSPENDED
                      || car.stopping
                      || car.maintenance
                      || !car.restraints
                      || !car.fault.isEmpty());
    } catch (IllegalArgumentException unavailable) {
      return true;
    }
  }

  /** Validation trace of the actual optional motor callback cancellation, on the server thread. */
  public static void notePaxMotorHold(ServerSubLevel body, ServerLevel level) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var vehicle = FoundationData.get(level).vehicles.get(body.getUniqueId());
    if (vehicle != null && vehicle.family == Family.PAX)
      VALIDATION_PAX_MOTOR_HOLD.set(body.getUniqueId());
  }

  public static boolean consumePaxMotorHold(UUID chassis) {
    var held = VALIDATION_PAX_MOTOR_HOLD.get();
    VALIDATION_PAX_MOTOR_HOLD.remove();
    return chassis.equals(held);
  }

  /**
   * Bounds an attached ordinary recovery bearing before Engineered turns its target into force.
   * Non-recovery carts retain the provider's requested target unchanged.
   */
  public static double boundedTarget(ServerSubLevel body, ServerLevel level, double requested) {
    if (!Double.isFinite(requested)) return requested;
    boolean recoveryBearing =
        FoundationData.get(level).vehicles.values().stream()
            .anyMatch(car -> body.getUniqueId().equals(car.recoveryCart));
    if (!recoveryBearing) return requested;
    return Math.clamp(
        requested, -RECOVERY_TARGET_SPEED, Math.min(RECOVERY_TARGET_SPEED, RecoveryOperations.MAX_SPEED));
  }

  private static boolean unavailable(SubLevelContainer container, UUID id) {
    // Logical vehicle records survive native unload; they cannot authorize propulsion alone.
    var body = container.getSubLevel(id);
    return body == null || body.isRemoved();
  }
}
