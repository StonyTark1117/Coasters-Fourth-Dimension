package dev.fourthdimension.track;

import dev.fourthdimension.runtime.FoundationData;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Exclude inactive 4D sections from physics sampling without changing junction topology. */
public final class TrackEndpointAvailability {
  private TrackEndpointAvailability() {}

  @FunctionalInterface
  public interface Sampler {
    List<CoasterPathTraceEndpoints.OpenEndRef> sample(
        Level level, CoasterPathGraph graph, Set<Integer> indices);
  }

  public static List<CoasterPathTraceEndpoints.OpenEndRef> sample(
      Level level, CoasterPathGraph graph, Set<Integer> indices, Sampler original) {
    if (!(level instanceof ServerLevel server)) return original.sample(level, graph, indices);
    var unavailable = new HashMap<BlockPos, Set<BlockPos>>();
    for (var rail : FoundationData.get(server).rails)
      if (!TrackGeometry.ticking(server, rail.from) || !TrackGeometry.ticking(server, rail.to)) {
        unavailable.computeIfAbsent(rail.from, p -> new HashSet<>()).add(rail.to);
        unavailable.computeIfAbsent(rail.to, p -> new HashSet<>()).add(rail.from);
      }
    if (unavailable.isEmpty()) return original.sample(level, graph, indices);
    var edges = graph.edges();
    var active = new HashSet<Integer>();
    var degree = new HashMap<BlockPos, Integer>();
    for (int index : indices) {
      if (index < 0 || index >= edges.size()) continue;
      var edge = edges.get(index);
      if (!edge.from().equals(edge.to())) {
        degree.merge(edge.from(), 1, Integer::sum);
        degree.merge(edge.to(), 1, Integer::sum);
      }
      if (!unavailable.getOrDefault(edge.from(), Set.of()).contains(edge.to())) active.add(index);
    }
    if (active.size() == indices.size()) return original.sample(level, graph, indices);
    return original.sample(level, graph, active).stream()
        .filter(
            ref -> {
              var edge = edges.get(ref.edgeIndex());
              // Filtering availability must not turn an internal junction into a physical open end.
              return edge.from().equals(edge.to())
                  || degree.getOrDefault(ref.atFrom() ? edge.from() : edge.to(), 0) == 1;
            })
        .toList();
  }
}
