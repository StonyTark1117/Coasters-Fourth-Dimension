package dev.fourthdimension.mixin;

import dev.fourthdimension.client.CarrierRenderMetrics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observes Sable's vanilla chunked-sublevel render dispatch only when validation enables it. */
// A string target and erased iterable keep this client-only observer from resolving Sable client
// classes while a dedicated server scans the mixin configuration.
@Mixin(
    targets = "dev.ryanhcode.sable.sublevel.render.dispatcher.VanillaSubLevelRenderDispatcher",
    remap = false)
public abstract class SableSectionRenderMixin {
  @Inject(method = "renderSectionLayer", at = @At("HEAD"), remap = false)
  private void fourthdimension$observeSectionLayer(
      Iterable<?> bodies,
      RenderType layer,
      ShaderInstance shader,
      double cameraX,
      double cameraY,
      double cameraZ,
      Matrix4f modelView,
      Matrix4f projection,
      float partialTick,
      CallbackInfo ci) {
    CarrierRenderMetrics.sectionLayer(bodies);
  }
}
