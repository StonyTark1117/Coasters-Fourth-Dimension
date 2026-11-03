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

  @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true)
  private void fourthdimension$redirectClosedSet(
      int codepoint, CallbackInfoReturnable<BakedGlyph> cir) {
    if (!fourthdimension$closed) return;
    FontSet replacement = FontReloadTrace.replacementForClosedSet(name, (FontSet) (Object) this);
    if (replacement != null) cir.setReturnValue(replacement.getGlyph(codepoint));
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
