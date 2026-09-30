package dev.fourthdimension.mixin;

import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Binding changes only when the guide constraint accepts an engaged frame, not during candidate
 * filtering.
 */
@Mixin(
    targets = "dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackHitCache",
    remap = false)
public abstract class EngagedTrackMixin {
  @Inject(method = "engagedTickFrame", at = @At("HEAD"))
  private static void fourthdimension$engaged(
      ServerLevel level,
      CoasterPathGraph graph,
      ServerSubLevel cart,
      CoasterPathTrackFrame.GraphHit hit,
      boolean negate,
      boolean sublevel,
      CallbackInfoReturnable<Object> cir) {
    EligibilityHooks.bindAccepted(cart, level, hit);
  }
}
