package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import net.minecraft.world.level.Level;

public final class ConsistRules {
  private ConsistRules() {}

  public static boolean canCouple(Level level, SubLevel a, SubLevel b, double distance) {
    // Coasters can ask its coupling callback while a native body is still being resolved.
    // Treat an incomplete endpoint as an ineligible link instead of crashing the server tick.
    if (a == null || b == null) return false;
    var data = EligibilityHooks.data(level);
    if (data == null || a.getUniqueId().equals(b.getUniqueId())) return false;
    var first = data.vehicles.get(a.getUniqueId());
    var second = data.vehicles.get(b.getUniqueId());
    if (first == null && second == null)
      return FamilyControllers.find(a).isEmpty() && FamilyControllers.find(b).isEmpty();
    if ((first == null) != (second == null))
      return level instanceof net.minecraft.server.level.ServerLevel server
          && RecoveryOperations.canCouple(server, a, b, distance);
    if (first != null && first.recoveryCart != null
        || second != null && second.recoveryCart != null) return false;
    var linked = new java.util.HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(a));
    linked.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(b));
    linked.add(a.getUniqueId());
    linked.add(b.getUniqueId());
    return first != null
        && second != null
        && (first.family == Family.ARROW || first.family == Family.PAX || WingVehicle.is(first) || SuspendedVehicle.is(first) || FixedInvertedVehicle.is(first))
        && second.family == first.family
        && (first.family != Family.PAX || linked.size() <= 4)
        && (!WingVehicle.is(first) || linked.size() <= 2)
        && (!(SuspendedVehicle.is(first) || FixedInvertedVehicle.is(first)) || linked.size() <= 4)
        && first.variant.equals(second.variant)
        && !FoundationData.VARIANT.equals(first.variant)
        && Double.isFinite(distance)
        && Math.abs(distance - ((SuspendedVehicle.is(first) || FixedInvertedVehicle.is(first)) ? 8 : 6)) < .01
        && first.boardingReady
        && second.boardingReady;
  }

  public static boolean eligible(Level level, SubLevel parent, FoundationData.Vehicle vehicle) {
    var data = EligibilityHooks.data(level);
    if (data == null || parent == null) return false;
    var container = SubLevelContainer.getContainer(level);
    if (vehicle.family == Family.PAX
        && CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent).size()
            > (vehicle.recoveryCart == null ? 4 : 5)) return false;
    var ids = CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent);
    if (WingVehicle.is(vehicle) && ids.size() > 2) return false;
    if ((SuspendedVehicle.is(vehicle) || FixedInvertedVehicle.is(vehicle)) && ids.size() > 4) return false;
    for (var id : ids) {
      if (id.equals(vehicle.chassis)) continue;
      var other = data.vehicles.get(id);
      // Lost recovery equipment stops towing in RecoveryOperations; it must never
      // revoke this disabled car's own matching rail support and make it fall.
      if (other == null && id.equals(vehicle.recoveryCart)) continue;
      if (other == null
          || other.family != vehicle.family
          || !(vehicle.family == Family.ARROW || vehicle.family == Family.PAX || WingVehicle.is(vehicle) || SuspendedVehicle.is(vehicle) || FixedInvertedVehicle.is(vehicle))
          || !other.variant.equals(vehicle.variant)
          || !java.util.Objects.equals(other.recoveryCart, vehicle.recoveryCart)
          || other.recoveryCart != null && !RecoveryOperations.retained(other)
          || container == null
          || container.getSubLevel(id) == null) return false;
      if (WingVehicle.is(vehicle)) {
        if (WingTrack.support(level, other) == null) return false;
        continue;
      }
      var support =
          data.rails.stream()
              .filter(r -> r.matches(other.boundFrom, other.boundTo))
              .findFirst()
              .orElse(null);
      if (support == null || !support.valid || !support.variant.equals(other.variant)) return false;
    }
    return true;
  }
}
