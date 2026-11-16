package dev.fourthdimension.mixin;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.runtime.CurveEdgeKey;
import dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CoasterPeerCurveHandleLengths.class, remap = false)
public abstract class PeerCurveKeyMixin {
  @Inject(method = "edgeKey", at = @At("HEAD"), cancellable = true)
  private static void fourthdimension$reuseEndpointKey(
      BezierConnection curve, CallbackInfoReturnable<String> cir) {
    if (curve instanceof CurveEdgeKey cached) cir.setReturnValue(cached.fourthdimension$edgeKey());
  }
}
