package dev.fourthdimension.mixin;

import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CoasterCartTrackSnap.class, remap = false)
public abstract class InitialSnapMixin {
  @Shadow @Final private static Map<ServerSubLevel, Boolean> BEARING_DISENGAGED;

  @Inject(method = "applyInitialSnapAfterSpawn", at = @At("HEAD"), cancellable = true)
  private static void fourthdimension$initial(
      ServerSubLevel sub, ServerLevel level, GraphHit hit, CallbackInfo ci) {
    if (!EligibilityHooks.allows(sub, level, hit)) {
      CoasterCartTrackSnap.markSpawnedDisengaged(sub);
      ci.cancel();
    } else EligibilityHooks.bindAccepted(sub, level, hit);
  }

  // Base initial snap intentionally leaves a previously disengaged bearing marked as such. For
  // an eligibility-approved explicit support repair, clear only that stale marker after the base
  // snap has rebuilt its guide so the next native physics tick can hold the renewed contact.
  @Inject(method = "applyInitialSnapAfterSpawn", at = @At("RETURN"))
  private static void fourthdimension$reengage(
      ServerSubLevel sub, ServerLevel level, GraphHit hit, CallbackInfo ci) {
    BEARING_DISENGAGED.remove(sub);
  }
}
