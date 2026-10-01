package dev.fourthdimension.mixin.optional;

import dev.fourthdimension.integration.immersion.ImmersionCameraContext;
import dev.mito.coastersimmersion.smooth.CameraFilter;
import org.joml.Quaterniond;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Feed effects the carrier pose even when the addon's smoothing/raw cache is disabled. */
@Mixin(value = CameraFilter.class, remap = false)
public abstract class ImmersionOrientationMixin {
  @Inject(method = "freshOrientation", at = @At("HEAD"), cancellable = true)
  private static void fourthdimension$carrierPose(CallbackInfoReturnable<Quaterniond> cir) {
    var orientation = ImmersionCameraContext.orientation();
    if (orientation != null) cir.setReturnValue(orientation);
  }
}
