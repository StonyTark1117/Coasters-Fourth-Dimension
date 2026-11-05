package dev.fourthdimension.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.fourthdimension.client.CarrierRenderMetrics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records an opt-in local-rider entity draw after vanilla has decided to render it. */
@Mixin(EntityRenderDispatcher.class)
public abstract class LocalRiderRenderMixin {
  @Inject(method = "render", at = @At("HEAD"))
  private <E extends Entity> void fourthdimension$observeLocalRider(
      E entity,
      double x,
      double y,
      double z,
      float rotationYaw,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource buffers,
      int packedLight,
      CallbackInfo callback) {
    CarrierRenderMetrics.entityRendered(entity);
  }
}
