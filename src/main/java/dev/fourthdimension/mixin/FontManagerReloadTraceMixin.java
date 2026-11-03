package dev.fourthdimension.mixin;

import dev.fourthdimension.client.FontReloadTrace;
import java.util.Map;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records FontManager apply boundaries and publishes the current set map for closed-set recovery. */
@Mixin(FontManager.class)
public abstract class FontManagerReloadTraceMixin {
  @Shadow @Final private Map<ResourceLocation, FontSet> fontSets;
  @Shadow @Final private FontSet missingFontSet;

  @Inject(method = "apply", at = @At("HEAD"))
  private void fourthdimension$applyStart(CallbackInfo ci) {
    FontReloadTrace.applyStart(fontSets.size());
  }

  @Inject(method = "apply", at = @At("RETURN"))
  private void fourthdimension$applyEnd(CallbackInfo ci) {
    FontReloadTrace.applyEnd(fontSets, missingFontSet);
  }
}
