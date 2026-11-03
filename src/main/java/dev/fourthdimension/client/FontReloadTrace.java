package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;

/**
 * Opt-in lifecycle trace for the upstream bitmap-font reload failure. It only observes the
 * font/image state immediately before Minecraft's existing upload path. When a reload leaves a
 * closed set reachable, it routes that invalid request to the current set for the same font.
 */
public final class FontReloadTrace {
  private record GlyphRequest(int generation, ResourceLocation font, int codepoint) {}

  private static final AtomicInteger GENERATION = new AtomicInteger();
  private static final ThreadLocal<GlyphRequest> GLYPH = new ThreadLocal<>();
  private static final Map<ResourceLocation, FontSet> LIVE_FONT_SETS = new ConcurrentHashMap<>();
  private static volatile FontSet liveMissingFontSet;
  private static final Set<String> REDIRECT_LOGS = ConcurrentHashMap.newKeySet();

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

  public static void applyEnd(Map<ResourceLocation, FontSet> fontSets, FontSet missingFontSet) {
    String probeName = System.getProperty("fourthdimension.staleRemovedFontProbe", "");
    ResourceLocation probeFont = probeName.isBlank() ? null : ResourceLocation.tryParse(probeName);
    if (!probeName.isBlank() && probeFont == null)
      throw new IllegalArgumentException("Invalid stale removed-font probe ID: " + probeName);
    FontSet staleProbe = probeFont == null ? null : LIVE_FONT_SETS.get(probeFont);
    LIVE_FONT_SETS.clear();
    LIVE_FONT_SETS.putAll(fontSets);
    // FontManager keeps this set outside fontSets and uses it for absent IDs. Do not substitute
    // a resource-pack-defined minecraft:missing entry: it is not Minecraft's missing-font set.
    liveMissingFontSet = missingFontSet;
    REDIRECT_LOGS.clear();
    if (!enabled()) return;
    LogUtils.getLogger()
        .info(
            "FD_FONT_APPLY_END generation={} newFontSets={} thread={}",
            GENERATION.get(),
            fontSets.size(),
            Thread.currentThread().getName());
    if (staleProbe != null && !fontSets.containsKey(probeFont)) {
      // This opt-in probe retains an actual set from the prior resource generation. It invokes
      // the FontSet mixin after FontManager closed that set and removed its ID from the new map.
      // A successful lookup must redirect through Minecraft's live missing-font set.
      int identity = System.identityHashCode(staleProbe);
      String redirectKey = GENERATION.get() + ":" + identity;
      BakedGlyph glyph = staleProbe.getGlyph('A');
      if (!REDIRECT_LOGS.contains(redirectKey) || glyph != missingFontSet.getGlyph('A'))
        throw new IllegalStateException(
            "FD_FONT_REMOVED_STALE_PROBE failed: closed-set lookup did not use live missing set");
      LogUtils.getLogger()
          .info(
              "FD_FONT_REMOVED_STALE_PROBE passed generation={} font={} closedIdentity={} "
                  + "missingIdentity={} codepoint=U+0041",
              GENERATION.get(),
              probeFont,
              identity,
              System.identityHashCode(missingFontSet));
    }
  }

  /**
   * Resolves an impossible lookup through the current set after resource reload closed the old one.
   * A closed set owns bitmap providers whose NativeImage storage has already been released.
   */
  public static FontSet replacementForClosedSet(ResourceLocation font, FontSet closedSet) {
    FontSet replacement = LIVE_FONT_SETS.get(font);
    boolean missingFallback = false;
    if (replacement == null) {
      // FontManager's normal absent-ID path returns the live missing set. A resource-pack reload
      // can remove a custom font ID while a pre-reload screen still holds its closed FontSet.
      replacement = liveMissingFontSet;
      missingFallback = true;
    }
    if (replacement == null || replacement == closedSet) return null;
    int generation = GENERATION.get();
    int closedIdentity = System.identityHashCode(closedSet);
    if (enabled() && REDIRECT_LOGS.add(generation + ":" + closedIdentity))
      LogUtils.getLogger()
          .warn(
              "FD_FONT_STALE_SET_REDIRECT generation={} font={} closedIdentity={} liveIdentity={} missingFallback={}",
              generation,
              font,
              closedIdentity,
              System.identityHashCode(replacement),
              missingFallback);
    return replacement;
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

  public static void glyphStart(ResourceLocation font, int identity, int codepoint, boolean closed) {
    if (!enabled()) return;
    int generation = GENERATION.get();
    if (generation < 2) return;
    GLYPH.set(new GlyphRequest(generation, font, codepoint));
    if (closed)
      LogUtils.getLogger()
          .error(
              "FD_FONT_CLOSED_SET_LOOKUP generation={} font={} identity={} codepoint=U+{}",
              generation,
              font,
              identity,
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
