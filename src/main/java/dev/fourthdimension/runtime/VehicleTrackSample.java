package dev.fourthdimension.runtime;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Lazy geometry shared only within one vehicle's pre-physics update. Discard before any pose,
 * running-gear, graph or binding change; never store this in vehicle state or a cross-tick cache.
 */
public final class VehicleTrackSample {
  private final ServerLevel level;
  private final ServerSubLevel parent;
  private final FoundationData.Rail rail;
  private Vec3 center;
  private CoasterPathEdge edge;
  private CoasterPathTrackFrame.GraphHit hit;
  private boolean centerResolved, edgeResolved, hitResolved;

  public VehicleTrackSample(ServerLevel level, ServerSubLevel parent, FoundationData.Rail rail) {
    this.level = level;
    this.parent = parent;
    this.rail = rail;
  }

  public boolean hasRunningGear() {
    if (!centerResolved) {
      var point = CoasterCartResolve.cartCenterWorld(parent);
      center = point == null ? null : new Vec3(point.x, point.y, point.z);
      centerResolved = true;
    }
    return center != null;
  }

  public CoasterPathEdge edge() {
    if (!edgeResolved) {
      edge =
          rail == null
              ? null
              : CoasterPathGraphManager.get(level).edges().stream()
                  .filter(e -> rail.matches(e.from(), e.to()))
                  .findFirst()
                  .orElse(null);
      edgeResolved = true;
    }
    return edge;
  }

  public CoasterPathTrackFrame.GraphHit hit() {
    if (!hitResolved) {
      if (hasRunningGear() && edge() != null)
        hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, center);
      hitResolved = true;
    }
    return hit;
  }
}
