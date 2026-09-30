package dev.fourthdimension.runtime;

import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class EligibilityHooks {
  private EligibilityHooks() {}

  // Client copies are replaced on full snapshots and keyed by level lifetime.
  public static final Map<Level, FoundationData> CLIENT =
      Collections.synchronizedMap(new WeakHashMap<>());

  public static FoundationData data(Level level) {
    return level instanceof ServerLevel server ? FoundationData.get(server) : CLIENT.get(level);
  }

  public static boolean allows(SubLevel chassis, Level level, GraphHit hit) {
    if (hit == null) return false;
    var data = data(level);
    if (data == null) return true;
    var vehicle = data.vehicles.get(chassis.getUniqueId());
    var markers = FamilyControllers.find(chassis);
    // An unbound or mixed-family assembly is not an ordinary recovery cart.
    if (!markers.isEmpty()
        && (vehicle == null || markers.size() != 1 || !markers.contains(vehicle.family)))
      return false;
    var rail =
        data.rails.stream()
            .filter(r -> r.matches(hit.edge().from(), hit.edge().to()))
            .findFirst()
            .orElse(null);
    if (vehicle == null) {
      if (rail == null) return true;
      // Prototype certificate: the unmodified base bearing uses the exact same main-rail
      // geometry. Added bodies require a measured swept-clearance certificate (FD108).
      var bounds = chassis.getPlot().getBoundingBox();
      return bounds != null
          && bounds.maxX() - bounds.minX() <= 1
          && bounds.maxY() - bounds.minY() <= 1
          && bounds.maxZ() - bounds.minZ() <= 1;
    }
    if (rail == null || rail.family() != vehicle.family) return false;
    if (vehicle.boundFrom != null
        && !rail.matches(vehicle.boundFrom, vehicle.boundTo)
        && !rail.from().equals(vehicle.boundFrom)
        && !rail.from().equals(vehicle.boundTo)
        && !rail.to().equals(vehicle.boundFrom)
        && !rail.to().equals(vehicle.boundTo)) return false;
    if (level instanceof ServerLevel) {
      vehicle.boundFrom = rail.from();
      vehicle.boundTo = rail.to();
      data.setDirty();
    }
    return true;
  }
}
