package dev.fourthdimension.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Client-render sample counters. Values are observational and reset when the client level changes. */
public final class TrackRenderMetrics {
  public record Snapshot(
      long meshBuilds,
      long meshBuildNanos,
      long renderStages,
      long renderNanos,
      long geometryStages,
      long inRangeRails,
      long inRangeQuads,
      long inRangeDecorationPieces,
      Set<UUID> nonzeroProfileRails) {
    public long averageMeshBuildNanos() {
      return meshBuilds == 0 ? 0 : meshBuildNanos / meshBuilds;
    }

    public long averageRenderStageNanos() {
      return renderStages == 0 ? 0 : renderNanos / renderStages;
    }
  }

  private static long meshBuilds;
  private static long meshBuildNanos;
  private static long renderStages;
  private static long renderNanos;
  private static long geometryStages;
  private static long inRangeRails;
  private static long inRangeQuads;
  private static long inRangeDecorationPieces;
  private static Set<UUID> nonzeroProfileRails = new HashSet<>();

  private TrackRenderMetrics() {}

  public static void reset() {
    meshBuilds = 0;
    meshBuildNanos = 0;
    renderStages = 0;
    renderNanos = 0;
    geometryStages = 0;
    inRangeRails = 0;
    inRangeQuads = 0;
    inRangeDecorationPieces = 0;
    nonzeroProfileRails = new HashSet<>();
  }

  public static void meshBuilt(long nanos) {
    meshBuilds++;
    meshBuildNanos += nanos;
  }

  public static void rendered(long nanos, int rails, int quads, int decorationPieces) {
    renderStages++;
    renderNanos += nanos;
    if (rails > 0 && quads > 0) geometryStages++;
    inRangeRails += rails;
    inRangeQuads += quads;
    inRangeDecorationPieces += decorationPieces;
  }

  /** Called after an in-range original mesh with a nonzero profile was submitted to the renderer. */
  public static void renderedNonzeroProfile(UUID rail) {
    nonzeroProfileRails.add(rail);
  }

  public static Snapshot snapshot() {
    return new Snapshot(
        meshBuilds,
        meshBuildNanos,
        renderStages,
        renderNanos,
        geometryStages,
        inRangeRails,
        inRangeQuads,
        inRangeDecorationPieces,
        Set.copyOf(nonzeroProfileRails));
  }
}
