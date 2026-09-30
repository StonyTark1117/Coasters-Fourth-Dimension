package dev.fourthdimension.mixin;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.client.OriginalTrackRenderer;
import dev.silvergold.simulatedcoasters.client.track.AnchorPeerTrackCurveBerRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = AnchorPeerTrackCurveBerRender.class, remap = false)
public abstract class OriginalTrackBerMixin {
  @Redirect(
      method = "render",
      at =
          @At(
              value = "INVOKE",
              target = "Lcom/simibubi/create/content/trains/track/BezierConnection;isPrimary()Z"))
  private static boolean fourthdimension$ownsMesh(BezierConnection curve) {
    return curve.isPrimary() && !OriginalTrackRenderer.owns(curve);
  }
}
