package dev.fourthdimension.mixin;

import dev.fourthdimension.client.CarrierRenderMetrics;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.silvergold.simulatedcoasters.client.cart.CoasterCartClientFrameCache.RenderFrame;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records native Coasters cart cell submission separately from Sable section rendering. */
@Mixin(targets = "dev.silvergold.simulatedcoasters.client.cart.CoasterCartBottomSliceRenderer", remap = false)
public abstract class CoasterCartRenderMixin {
  @Inject(
      method = "renderCell",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/silvergold/simulatedcoasters/client/cart/CoasterCartClientFrameCache$RenderFrame;visualPose()Ldev/ryanhcode/sable/companion/math/Pose3d;",
              shift = At.Shift.AFTER),
      remap = false)
  private static void fourthdimension$observeNativeCartRender(
      PoseStack poseStack,
      ModelBlockRenderer modelRenderer,
      BufferSource buffers,
      BlockState state,
      float red,
      float green,
      float blue,
      RandomSource random,
      Level level,
      RenderFrame frame,
      double cameraX,
      double cameraY,
      double cameraZ,
      BlockPos.MutableBlockPos cell,
      CallbackInfo callback) {
    CarrierRenderMetrics.coasterCartCell(level, cell);
  }
}
