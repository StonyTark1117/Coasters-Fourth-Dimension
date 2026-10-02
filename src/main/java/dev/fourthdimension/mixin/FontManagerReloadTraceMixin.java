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

/** Logs only opt-in FontManager apply boundaries; it does not modify cache or provider ownership. */
@Mixin(FontManager.class)
public abstract class FontManagerReloadTraceMixin {
  @Shadow @Final private Map<ResourceLocation, FontSet> fontSets;

  @Inject(method = "apply", at = @At("HEAD"))
  private void fourthdimension$applyStart(CallbackInfo ci) {
    FontReloadTrace.applyStart(fontSets.size());
  }

  @Inject(method = "apply", at = @At("RETURN"))
  private void fourthdimension$applyEnd(CallbackInfo ci) {
    FontReloadTrace.applyEnd(fontSets.size());
  }
}
