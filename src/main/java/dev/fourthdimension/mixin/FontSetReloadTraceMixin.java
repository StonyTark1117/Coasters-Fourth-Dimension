package dev.fourthdimension.mixin;

import com.mojang.blaze3d.font.GlyphInfo;
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

/** Records closed sets and redirects any later glyph lookup to the current reloaded set. */
@Mixin(FontSet.class)
public abstract class FontSetReloadTraceMixin {
  @Shadow @Final private ResourceLocation name;
  @Unique private boolean fourthdimension$closed;

  @Inject(method = "close", at = @At("HEAD"))
  private void fourthdimension$close(CallbackInfo ci) {
    fourthdimension$closed = true;
    FontReloadTrace.fontSetClosed(name, System.identityHashCode(this));
  }

  @Unique
  private FontSet fourthdimension$replacement() {
    if (!fourthdimension$closed) return null;
    return FontReloadTrace.replacementForClosedSet(name, (FontSet) (Object) this);
  }

  @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true)
  private void fourthdimension$redirectClosedGlyph(
      int codepoint, CallbackInfoReturnable<BakedGlyph> cir) {
    FontSet replacement = fourthdimension$replacement();
    if (replacement != null) cir.setReturnValue(replacement.getGlyph(codepoint));
  }

  @Inject(method = "getGlyphInfo", at = @At("HEAD"), cancellable = true)
  private void fourthdimension$redirectClosedGlyphInfo(
      int codepoint, boolean filterFishy, CallbackInfoReturnable<GlyphInfo> cir) {
    FontSet replacement = fourthdimension$replacement();
    if (replacement != null) cir.setReturnValue(replacement.getGlyphInfo(codepoint, filterFishy));
  }

  @Inject(method = "getRandomGlyph", at = @At("HEAD"), cancellable = true)
  private void fourthdimension$redirectClosedRandomGlyph(
      GlyphInfo glyph, CallbackInfoReturnable<BakedGlyph> cir) {
    FontSet replacement = fourthdimension$replacement();
    if (replacement != null) cir.setReturnValue(replacement.getRandomGlyph(glyph));
  }

  @Inject(method = "whiteGlyph", at = @At("HEAD"), cancellable = true)
  private void fourthdimension$redirectClosedWhiteGlyph(CallbackInfoReturnable<BakedGlyph> cir) {
    FontSet replacement = fourthdimension$replacement();
    if (replacement != null) cir.setReturnValue(replacement.whiteGlyph());
  }

  @Inject(method = "computeBakedGlyph", at = @At("HEAD"))
  private void fourthdimension$glyphStart(int codepoint, CallbackInfoReturnable<BakedGlyph> ci) {
    FontReloadTrace.glyphStart(name, System.identityHashCode(this), codepoint, fourthdimension$closed);
  }

  @Inject(method = "computeBakedGlyph", at = @At("RETURN"))
  private void fourthdimension$glyphEnd(int codepoint, CallbackInfoReturnable<BakedGlyph> ci) {
    FontReloadTrace.glyphEnd();
  }
}
