package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Fixed-seat lifecycle uses the native chassis and ordinary station stop, with no rotary joint. */
public final class WingRuntime {
  private static final Map<ServerSubLevel, Long> CHECKED = new WeakHashMap<>();
  private WingRuntime() {}

  public static void tick(ServerLevel level, ServerSubLevel parent,
      FoundationData.Vehicle vehicle, double dt) {
    if (!Objects.equals(CHECKED.put(parent, level.getGameTime()), level.getGameTime())) {
      String failure = safety(level, parent, vehicle);
      if (failure != null) {
        java.util.List<FoundationData.Vehicle> cars;
        try { cars = VehicleOperations.consist(level, vehicle); }
        catch (IllegalArgumentException missing) { cars = java.util.List.of(vehicle); }
        for (var car : cars) {
          car.stopping = true;
          car.restraints = true;
          car.fault = failure;
          car.carriers.forEach(c -> c.capture = true);
        }
      }
    }
    if (vehicle.stopping || vehicle.maintenance)
      VehicleOperations.brake(level, vehicle, dt);
    var seats = vehicle.carriers.getFirst();
    seats.angle = 0;
    seats.omega = 0;
    NativeLoading.update(parent, vehicle, seats);
    vehicle.boardingReady = Boarding.ready(level, vehicle);
  }

  public static String safety(ServerLevel level, ServerSubLevel parent, FoundationData.Vehicle vehicle) {
    var edge = WingTrack.support(level, vehicle);
    if (edge == null) return "Wing ordinary track unavailable; restore track then Stop";
    if (!ConsistRules.eligible(level, parent, vehicle)) return "Wing consist unavailable or incompatible";
    var center = CoasterCartResolve.cartCenterWorld(parent);
    if (center == null) return "Wing running gear unavailable";
    var hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, new Vec3(center.x, center.y, center.z));
    if (hit == null) return "Wing track position unavailable";
    if (WingTrack.obstruction(level, hit) != null) return "Wing rider envelope obstructed; clear it then Stop";
    var velocity = SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
        .getLinearVelocity(parent, new Vector3d());
    var forward = CoasterPathTrackFrame.trackBasisAtHit(level, hit).forward();
    double speed = velocity.dot(forward.x, forward.y, forward.z);
    if (Math.abs(speed) < .08) return null;
    var table = TrackGeometry.table(level, edge);
    double distance = table.distanceAt(hit.t());
    double braking = speed * speed / 12 + 2.5;
    // Sample the actual swept corridor ahead, including the stopping margin.
    for (double advance = .5; advance <= braking; advance += .5) {
      double target = distance + Math.copySign(advance, speed);
      if (target < 0 || target > table.length()) break;
      double t = table.parameterAt(target);
      var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
      if (WingTrack.obstruction(level, new CoasterPathTrackFrame.GraphHit(point, edge, t)) != null)
        return "Wing rider envelope ahead obstructed; clear it then Stop";
    }
    double remaining = speed > 0 ? table.length() - distance : distance;
    if (remaining <= braking) {
      var endpoint = speed > 0 ? edge.to() : edge.from();
      var nextEdges = CoasterPathGraphManager.get(level).edges().stream().filter(e ->
          !(e.from().equals(edge.from()) && e.to().equals(edge.to())
              || e.from().equals(edge.to()) && e.to().equals(edge.from()))
              && (e.from().equals(endpoint) || e.to().equals(endpoint))).toList();
      if (nextEdges.size() != 1 || WingTrack.rejection(level, nextEdges.getFirst()) != null)
        return "Wing end or branch of compatible track; Stop and inspect route";
      var next = nextEdges.getFirst();
      var nextTable = TrackGeometry.table(level, next);
      for (double advance = 0; advance <= Math.min(braking - remaining, nextTable.length()); advance += .5) {
        double along = next.from().equals(endpoint) ? advance : nextTable.length() - advance;
        double t = nextTable.parameterAt(along);
        var point = CoasterPathGraphManager.sampleEdgePosition(level, next, t);
        if (WingTrack.obstruction(level, new CoasterPathTrackFrame.GraphHit(point, next, t)) != null)
          return "Wing rider envelope across connection obstructed; clear it then Stop";
      }
    }
    return null;
  }
}
