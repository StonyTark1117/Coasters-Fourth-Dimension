package dev.fourthdimension.mixin;

import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartRailSnapEligibility;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CoasterCartRailSnapEligibility.class, remap = false)
public abstract class RailEligibilityMixin {
  @Inject(method = "filterHitForBearing", at = @At("RETURN"), cancellable = true)
  private static void fourthdimension$filter(
      SubLevel sub, Level level, GraphHit hit, CallbackInfoReturnable<GraphHit> cir) {
    if (cir.getReturnValue() != null && !EligibilityHooks.allows(sub, level, cir.getReturnValue()))
      cir.setReturnValue(null);
  }
}
