package dev.fourthdimension.mixin;

import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Our physics joints have no block-entity actor for Sable's native orbit-camera traversal. */
@Mixin(value = SubLevelHelper.class, remap = false)
public abstract class CarrierConnectionMixin {
  @Inject(method = "getConnectedChain", at = @At("RETURN"), cancellable = true)
  private static void fourthdimension$carrierConnections(
      SubLevel sub, CallbackInfoReturnable<Collection<SubLevel>> cir) {
    var level = sub.getLevel();
    if (level == null || !level.isClientSide) return;
    var data = EligibilityHooks.CLIENT.get(level);
    var container = SubLevelContainer.getContainer(level);
    if (data == null || container == null || sub.isRemoved()) return;
    var id = sub.getUniqueId();
    var owner =
        data.vehicles.values().stream()
            .filter(v -> !FoundationData.VARIANT.equals(v.variant))
            .filter(
                v -> v.chassis.equals(id) || v.carriers.stream().anyMatch(c -> c.body.equals(id)))
            .findFirst()
            .orElse(null);
    if (owner == null) return;
    var result = new HashSet<>(cir.getReturnValue());
    var chassisIds = new HashSet<UUID>();
    chassisIds.add(owner.chassis);
    var parent = container.getSubLevel(owner.chassis);
    if (parent != null && !parent.isRemoved())
      chassisIds.addAll(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent));
    for (var chassisId : chassisIds) {
      var chassis = container.getSubLevel(chassisId);
      if (chassis != null && !chassis.isRemoved()) result.add(chassis);
      var vehicle = data.vehicles.get(chassisId);
      if (vehicle == null || FoundationData.VARIANT.equals(vehicle.variant)) continue;
      for (var carrier : vehicle.carriers) {
        var body = container.getSubLevel(carrier.body);
        if (body != null && !body.isRemoved()) result.add(body);
      }
    }
    cir.setReturnValue(result);
  }
}
