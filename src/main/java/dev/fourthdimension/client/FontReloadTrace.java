package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceLocation;

/**
 * Opt-in lifecycle trace for the upstream bitmap-font reload failure. It only observes the
 * font/image state immediately before Minecraft's existing upload path; it never resets or keeps
 * a font, provider, or native image alive.
 */
public final class FontReloadTrace {
  private record GlyphRequest(int generation, ResourceLocation font, int codepoint) {}

  private static final AtomicInteger GENERATION = new AtomicInteger();
  private static final ThreadLocal<GlyphRequest> GLYPH = new ThreadLocal<>();

  private FontReloadTrace() {}

  public static boolean enabled() {
    return Boolean.getBoolean("fourthdimension.fontValidation");
  }

  public static int generation() {
    return GENERATION.get();
  }

  public static void applyStart(int fontSets) {
    if (!enabled()) return;
    LogUtils.getLogger()
        .info(
            "FD_FONT_APPLY_START generation={} oldFontSets={} thread={}",
            GENERATION.incrementAndGet(),
            fontSets,
            Thread.currentThread().getName());
  }

  public static void applyEnd(int fontSets) {
    if (!enabled()) return;
    LogUtils.getLogger()
        .info(
            "FD_FONT_APPLY_END generation={} newFontSets={} thread={}",
            GENERATION.get(),
            fontSets,
            Thread.currentThread().getName());
  }

  public static void fontSetClosed(ResourceLocation font, int identity) {
    if (!enabled()) return;
    LogUtils.getLogger()
        .info(
            "FD_FONT_SET_CLOSE generation={} font={} identity={}",
            GENERATION.get(),
            font,
            identity);
  }

  public static void glyphStart(ResourceLocation font, int codepoint, boolean closed) {
    if (!enabled()) return;
    int generation = GENERATION.get();
    if (generation < 2) return;
    GLYPH.set(new GlyphRequest(generation, font, codepoint));
    if (closed)
      LogUtils.getLogger()
          .error(
              "FD_FONT_CLOSED_SET_LOOKUP generation={} font={} codepoint=U+{}",
              generation,
              font,
              String.format("%04X", codepoint));
  }

  public static void glyphEnd() {
    GLYPH.remove();
  }

  public static void unallocatedImage(Object image, int width, int height) {
    if (!enabled()) return;
    var glyph = GLYPH.get();
    LogUtils.getLogger()
        .error(
            "FD_FONT_IMAGE_UNALLOCATED generation={} font={} codepoint={} image={} size={}x{} thread={}",
            glyph == null ? GENERATION.get() : glyph.generation(),
            glyph == null ? "unknown" : glyph.font(),
            glyph == null ? "unknown" : String.format("U+%04X", glyph.codepoint()),
            System.identityHashCode(image),
            width,
            height,
            Thread.currentThread().getName());
  }
}
