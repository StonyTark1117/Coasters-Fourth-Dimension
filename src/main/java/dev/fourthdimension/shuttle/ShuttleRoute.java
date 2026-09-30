package dev.fourthdimension.shuttle;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** An ordered, unbranched, fixed-world PAX route with two guarded elevated terminals. */
public final class ShuttleRoute {
  public record Segment(
      FoundationData.Rail rail,
      CoasterPathEdge edge,
      boolean reversed,
      double start,
      double length) {}

  public record Sample(CoasterPathTrackFrame.GraphHit hit, Vec3 tangent, double distance) {}

  private final ServerLevel level;
  public final List<Segment> segments;
  public final double length;
  public final BlockPos start, end;

  private ShuttleRoute(ServerLevel level, List<Segment> segments, BlockPos start, BlockPos end) {
    this.level = level;
    this.segments = List.copyOf(segments);
    this.start = start;
    this.end = end;
    this.length = segments.getLast().start + segments.getLast().length;
  }

  public static ShuttleRoute resolve(ServerLevel level, BlockPos start, BlockPos end) {
    var data = FoundationData.get(level);
    var ordered = new ArrayList<Segment>();
    var used = new HashSet<UUID>();
    BlockPos node = start;
    double distance = 0;
    for (int count = 0; count < 256; count++) {
      var at = node;
      if (!dev.ryanhcode.sable.sublevel.system.ticket.PhysicsChunkTicketManager.isChunkLoadedEnough(
          level, node.getX() >> 4, node.getZ() >> 4))
        throw new IllegalArgumentException(
            "Shuttle route chunk is unavailable for physics; load the complete route");
      var legs = data.rails.stream().filter(r -> r.from.equals(at) || r.to.equals(at)).toList();
      int expected = node.equals(start) || node.equals(end) ? 1 : 2;
      if (legs.size() != expected)
        throw new IllegalArgumentException(
            "Shuttle route must have exactly two open terminals and no branches");
      for (var r : legs)
        if (!r.valid || !r.variant.equals(TrackDesign.PAX.variant))
          throw new IllegalArgumentException(
              "Every shuttle segment requires valid matching PAX track");
      if (node.equals(end)) {
        if (ordered.size() < 3)
          throw new IllegalArgumentException("Shuttle needs a station and two spikes");
        var result = new ShuttleRoute(level, ordered, start, end);
        if (distance < 50 || distance > 500)
          throw new IllegalArgumentException("Shuttle route length must be 50..500 blocks");
        return result;
      }
      var rail =
          legs.stream()
              .filter(r -> !used.contains(r.id))
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Shuttle route cannot contain a closed circuit"));
      var edge = TrackGeometry.resolve(level, rail);
      if (edge == null
          || CoasterPathGraphManager.edgeHostedOnSubLevel(level, edge)
          || TrackGeometry.fingerprint(level, edge) != rail.geometryHash)
        throw new IllegalArgumentException(
            "Load the unchanged fixed-world shuttle route before operating");
      used.add(rail.id);
      double length = TrackGeometry.table(level, edge).length();
      for (double s = 0; s <= length; s += 8) {
        double t = TrackGeometry.table(level, edge).parameterAt(s);
        var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
        if (!dev.ryanhcode.sable.sublevel.system.ticket.PhysicsChunkTicketManager
            .isChunkLoadedEnough(
                level,
                net.minecraft.util.Mth.floor(point.x) >> 4,
                net.minecraft.util.Mth.floor(point.z) >> 4))
          throw new IllegalArgumentException(
              "Shuttle route chunk is unavailable for physics; load the complete route");
      }
      ordered.add(new Segment(rail, edge, !edge.from().equals(node), distance, length));
      distance += length;
      node = rail.from.equals(node) ? rail.to : rail.from;
    }
    throw new IllegalArgumentException("Shuttle route exceeds capacity");
  }

  public Sample sample(double distance) {
    distance = Math.max(0, Math.min(length, distance));
    Segment selected = segments.getLast();
    for (var segment : segments)
      if (distance <= segment.start + segment.length) {
        selected = segment;
        break;
      }
    double local = distance - selected.start;
    double t =
        TrackGeometry.table(level, selected.edge)
            .parameterAt(selected.reversed ? selected.length - local : local);
    var point = CoasterPathGraphManager.sampleEdgePosition(level, selected.edge, t);
    var hit = new CoasterPathTrackFrame.GraphHit(point, selected.edge, t);
    var tangent = CoasterPathTrackFrame.trackBasisAtHit(level, hit).forward();
    if (selected.reversed) tangent = tangent.scale(-1);
    return new Sample(hit, tangent, distance);
  }

  public Sample locate(SubLevel body) {
    var center = CoasterCartResolve.cartCenterWorld(body);
    if (center == null) throw new IllegalArgumentException("Shuttle running gear unavailable");
    Sample best = null;
    double error = Double.POSITIVE_INFINITY;
    var point = new Vec3(center.x, center.y, center.z);
    for (var segment : segments) {
      var hit = CoasterPathTrackFrame.closestHitOnEdge(level, segment.edge, point);
      if (hit == null) continue;
      double e = hit.point().distanceToSqr(point);
      if (e < error) {
        error = e;
        double local = TrackGeometry.table(level, segment.edge).distanceAt(hit.t());
        best = sample(segment.start + (segment.reversed ? segment.length - local : local));
      }
    }
    if (best == null || error > 1)
      throw new IllegalArgumentException("Shuttle car has lost route engagement");
    return best;
  }
}
