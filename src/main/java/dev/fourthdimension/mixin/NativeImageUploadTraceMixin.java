package dev.fourthdimension.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.fourthdimension.client.FontReloadTrace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Captures the exact closed NativeImage just before Minecraft throws its existing allocation error. */
@Mixin(NativeImage.class)
public abstract class NativeImageUploadTraceMixin {
  @Shadow private long pixels;
  @Shadow private int width;
  @Shadow private int height;

  @Inject(method = "upload(IIIIIIIZZ)V", at = @At("HEAD"))
  private void fourthdimension$beforeUpload(
      int level,
      int xOffset,
      int yOffset,
      int x,
      int y,
      int uploadWidth,
      int uploadHeight,
      boolean blur,
      boolean clamp,
      CallbackInfo ci) {
    if (pixels == 0L) FontReloadTrace.unallocatedImage(this, width, height);
  }
}
