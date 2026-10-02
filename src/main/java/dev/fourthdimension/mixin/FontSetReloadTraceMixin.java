package dev.fourthdimension.mixin;

import dev.fourthdimension.client.FontReloadTrace;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Marks an observed FontSet closed so an impossible subsequent glyph lookup is attributable. */
@Mixin(FontSet.class)
public abstract class FontSetReloadTraceMixin {
  @Shadow @Final private ResourceLocation name;
  @Unique private boolean fourthdimension$closed;

  @Inject(method = "close", at = @At("HEAD"))
  private void fourthdimension$close(CallbackInfo ci) {
    fourthdimension$closed = true;
    FontReloadTrace.fontSetClosed(name, System.identityHashCode(this));
  }

  @Inject(method = "computeBakedGlyph", at = @At("HEAD"))
  private void fourthdimension$glyphStart(int codepoint, CallbackInfoReturnable<BakedGlyph> ci) {
    FontReloadTrace.glyphStart(name, codepoint, fourthdimension$closed);
  }

  @Inject(method = "computeBakedGlyph", at = @At("RETURN"))
  private void fourthdimension$glyphEnd(int codepoint, CallbackInfoReturnable<BakedGlyph> ci) {
    FontReloadTrace.glyphEnd();
  }
}
