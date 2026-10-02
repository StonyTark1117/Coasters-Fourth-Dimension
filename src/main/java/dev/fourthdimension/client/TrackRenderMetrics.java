package dev.fourthdimension.client;

/** Client-render sample counters. Values are observational and reset when the client level changes. */
public final class TrackRenderMetrics {
  public record Snapshot(
      long meshBuilds,
      long meshBuildNanos,
      long renderStages,
      long renderNanos,
      long inRangeRails,
      long inRangeQuads,
      long inRangeDecorationPieces) {
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
  private static long inRangeRails;
  private static long inRangeQuads;
  private static long inRangeDecorationPieces;

  private TrackRenderMetrics() {}

  public static void reset() {
    meshBuilds = 0;
    meshBuildNanos = 0;
    renderStages = 0;
    renderNanos = 0;
    inRangeRails = 0;
    inRangeQuads = 0;
    inRangeDecorationPieces = 0;
  }

  public static void meshBuilt(long nanos) {
    meshBuilds++;
    meshBuildNanos += nanos;
  }

  public static void rendered(long nanos, int rails, int quads, int decorationPieces) {
    renderStages++;
    renderNanos += nanos;
    inRangeRails += rails;
    inRangeQuads += quads;
    inRangeDecorationPieces += decorationPieces;
  }

  public static Snapshot snapshot() {
    return new Snapshot(
        meshBuilds,
        meshBuildNanos,
        renderStages,
        renderNanos,
        inRangeRails,
        inRangeQuads,
        inRangeDecorationPieces);
  }
}
