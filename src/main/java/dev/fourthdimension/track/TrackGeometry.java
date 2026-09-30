package dev.fourthdimension.track;

import dev.fourthdimension.physics.ArcLengthTable;
import dev.fourthdimension.runtime.FoundationData;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class TrackGeometry {
  private TrackGeometry() {}

  private static final Map<Level, Map<CoasterPathEdge, ArcLengthTable>> CACHE = new WeakHashMap<>();

  public static ArcLengthTable table(Level level, CoasterPathEdge edge) {
    var cache = CACHE.computeIfAbsent(level, k -> new WeakHashMap<>());
    return cache.computeIfAbsent(
        edge,
        e ->
            new ArcLengthTable(
                t -> {
                  Vec3 p = CoasterPathGraphManager.sampleEdgePositionPlotLocal(level, e, t);
                  return new ArcLengthTable.Point(p.x, p.y, p.z);
                },
                1024));
  }

  public static void invalidate(Level level) {
    CACHE.remove(level);
  }

  public static CoasterPathEdge resolve(Level level, FoundationData.Rail rail) {
    return dev.fourthdimension.integration.TrackProviders.resolve(level, rail);
  }

  public static CoasterPathEdge resolveNative(Level level, FoundationData.Rail rail) {
    if (!level.hasChunkAt(rail.from) || !level.hasChunkAt(rail.to)) return null;
    if (!(level.getBlockEntity(rail.from) instanceof CoasterAnchorpointBlockEntity anchor))
      return null;
    var curve = anchor.getAnchorPeerCurvesView().get(rail.to);
    if (curve == null) return null;
    if (!curve.isPrimary()) curve = curve.secondary();
    return CoasterPathEdge.curve(
        curve.bePositions.getFirst(), curve.bePositions.getSecond(), curve);
  }

  public static long fingerprint(Level level, CoasterPathEdge edge) {
    long hash = 0xcbf29ce484222325L;
    for (int i = 0; i <= 16; i++) {
      Vec3 p = CoasterPathGraphManager.sampleEdgePositionPlotLocal(level, edge, i / 16.0);
      var basis =
          CoasterPathTrackFrame.trackBasisPlotLocalAtHit(
              level, new CoasterPathTrackFrame.GraphHit(p, edge, i / 16.0));
      for (double value : new double[] {p.x, p.y, p.z, basis.up().x, basis.up().y, basis.up().z})
        hash = (hash ^ Math.round(value * 1e6)) * 0x100000001b3L;
    }
    return hash;
  }

  public static double distance(
      Level level, CoasterPathEdge edge, FoundationData.Rail rail, double t) {
    var table = table(level, edge);
    double forward = table.distanceAt(t);
    return edge.from().equals(rail.from) ? forward : table.length() - forward;
  }
}
