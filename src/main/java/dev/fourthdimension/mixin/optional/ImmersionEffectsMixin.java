package dev.fourthdimension.mixin.optional;

import dev.fourthdimension.integration.immersion.ImmersionCameraContext;
import dev.mito.coastersimmersion.client.camera.*;
import dev.mito.coastersimmersion.client.ride.RideTelemetry;
import dev.mito.coastersimmersion.smooth.CameraFilter;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Do not carry a previous carrier's transient camera effects into the next view. */
@Mixin(value = CinematicCamera.class, remap = false)
public abstract class ImmersionEffectsMixin {
  @Shadow private static double bank;
  @Shadow private static double neckPitch;
  @Shadow private static double neckVelocity;
  @Shadow private static double gPitch;
  @Shadow private static double gRoll;
  @Shadow private static double shakeYaw;
  @Shadow private static double shakePitch;
  @Shadow private static double bobPitch;

  @Inject(method = "onRenderFrame", at = @At("HEAD"))
  private static void fourthdimension$restoreView(RenderFrameEvent.Pre event, CallbackInfo ci) {
    if (!ImmersionCameraContext.changed()) return;
    bank = neckPitch = neckVelocity = gPitch = gRoll = shakeYaw = shakePitch = bobPitch = 0;
    CameraFilter.reset();
    BodyTravel.reset();
    LaunchPulse.reset();
    SpeedTracker.reset();
    RideTelemetry.reset();
  }
}
