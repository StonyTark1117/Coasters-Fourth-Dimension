package dev.fourthdimension.mixin;

import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CoasterCartTrackSnap.class, remap = false)
public abstract class InitialSnapMixin {
  @Inject(method = "applyInitialSnapAfterSpawn", at = @At("HEAD"), cancellable = true)
  private static void fourthdimension$initial(
      ServerSubLevel sub, ServerLevel level, GraphHit hit, CallbackInfo ci) {
    if (!EligibilityHooks.allows(sub, level, hit)) {
      CoasterCartTrackSnap.markSpawnedDisengaged(sub);
      ci.cancel();
    }
  }
}
