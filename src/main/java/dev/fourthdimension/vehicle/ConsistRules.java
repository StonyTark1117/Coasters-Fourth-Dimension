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
    if (vehicle.family == Family.PAX
        && CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent).size() > 4) return false;
    for (var id : CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent)) {
      if (id.equals(vehicle.chassis)) continue;
      var other = data.vehicles.get(id);
      if (other == null
          || other.family != vehicle.family
          || !(vehicle.family == Family.ARROW || vehicle.family == Family.PAX)
          || !other.variant.equals(vehicle.variant)
          || container == null
          || container.getSubLevel(id) == null) return false;
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
