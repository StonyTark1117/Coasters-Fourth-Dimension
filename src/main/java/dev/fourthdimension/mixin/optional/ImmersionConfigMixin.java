package dev.fourthdimension.mixin.optional;

import dev.fourthdimension.integration.immersion.ImmersionCameraContext;
import dev.mito.coastersimmersion.config.ImmersionClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The camera master switch must also bypass Immersion's orientation filter on our rides. */
@Mixin(value = ImmersionClientConfig.class, remap = false)
public abstract class ImmersionConfigMixin {
  @Inject(method = "cameraProcessingActive", at = @At("HEAD"), cancellable = true)
  private static void fourthdimension$cameraMaster(CallbackInfoReturnable<Boolean> cir) {
    if (ImmersionCameraContext.carrier() != null && !ImmersionClientConfig.cinematicEnabled())
      cir.setReturnValue(false);
  }
}
