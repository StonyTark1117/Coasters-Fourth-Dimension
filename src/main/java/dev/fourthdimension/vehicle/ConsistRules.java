package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.TrackProviders;
import dev.fourthdimension.runtime.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import net.minecraft.world.level.Level;

public final class ConsistRules {
  private ConsistRules() {}

  public static boolean canCouple(Level level, SubLevel a, SubLevel b, double distance) {
    var data = EligibilityHooks.data(level);
    if (data == null || a.getUniqueId().equals(b.getUniqueId())) return false;
    var first = data.vehicles.get(a.getUniqueId());
    var second = data.vehicles.get(b.getUniqueId());
    if (first == null && second == null)
      return FamilyControllers.find(a).isEmpty() && FamilyControllers.find(b).isEmpty();
    var linked = new java.util.HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(a));
    linked.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(b));
    linked.add(a.getUniqueId());
    linked.add(b.getUniqueId());
    return first != null
        && second != null
        && (first.family == Family.ARROW || first.family == Family.PAX)
        && second.family == first.family
        && (first.family != Family.PAX || linked.size() <= 4)
        && first.variant.equals(second.variant)
        && !FoundationData.VARIANT.equals(first.variant)
        && Double.isFinite(distance)
        && Math.abs(distance - 6) < .01
        && first.boardingReady
        && second.boardingReady;
  }

  public static boolean eligible(Level level, SubLevel parent, FoundationData.Vehicle vehicle) {
    var data = EligibilityHooks.data(level);
    var container = SubLevelContainer.getContainer(level);
    if (data == null || container == null) return false;
    if (vehicle.family == Family.PAX
        && CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent).size() > 4) return false;
    for (var id : CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent)) {
      if (id.equals(vehicle.chassis)) continue;
      var other = data.vehicles.get(id);
      if (other == null
          || other.family != vehicle.family
          || !(vehicle.family == Family.ARROW || vehicle.family == Family.PAX)
          || !other.variant.equals(vehicle.variant)
          || container.getSubLevel(id) == null) return false;
      if (!hasMatchingSupport(level, other)) return false;
    }
    return true;
  }

  /** Saved functional support contract only; native geometry/loading is validated separately. */
  public static boolean hasMatchingSupport(Level level, FoundationData.Vehicle vehicle) {
    var data = EligibilityHooks.data(level);
    if (data == null) return false;
    return data.rails.stream()
        .filter(rail -> rail.matches(vehicle.boundFrom, vehicle.boundTo))
        .findFirst()
        .filter(
            rail ->
                rail.valid
                    && rail.family() == vehicle.family
                    && rail.variant.equals(vehicle.variant)
                    && TrackProviders.available(rail))
        .isPresent();
  }
}
