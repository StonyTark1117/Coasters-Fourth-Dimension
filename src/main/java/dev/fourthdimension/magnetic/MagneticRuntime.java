package dev.fourthdimension.magnetic;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Passive impulses evaluated once per native physics step against swept, connected sections. */
public final class MagneticRuntime {
  private MagneticRuntime() {}

  private record Sample(
      FoundationData.Rail rail, double distance, Vec3 point, Vec3 tangent, long revision) {}

  private record Piece(FoundationData.Rail rail, double from, double to) {
    double length() {
      return Math.abs(to - from);
    }
  }

  private static final Map<ServerSubLevel, Sample> PREVIOUS = new WeakHashMap<>();

  public static final class Audit {
    public long impulses;
    public double weightedDistance, energyChange;
    public long damperImpulses;
    public double damperEnergyChange, damperAngularSpeedReduction;
  }

  private static final Map<ServerSubLevel, Audit> AUDIT = new WeakHashMap<>();

  public static Audit audit(ServerSubLevel body) {
    return AUDIT.computeIfAbsent(body, k -> new Audit());
  }

  public static long applications;
  public static double maxEnergyIncrease, dissipatedEnergy, largestSpinGain;

  public static void tick(
      ServerLevel level, FoundationData.Vehicle vehicle, ServerSubLevel parent, double dt) {
    if (vehicle.family != Family.FREE_SPIN
        || FoundationData.VARIANT.equals(vehicle.variant)
        || !FamilyControllers.find(parent).equals(java.util.Set.of(Family.FREE_SPIN))) return;
    var container = SubLevelContainer.getContainer(level);
    var data = FoundationData.get(level);
    var bound =
        data.rails.stream()
            .filter(r -> r.matches(vehicle.boundFrom, vehicle.boundTo))
            .findFirst()
            .orElse(null);
    if (bound == null || !bound.valid) return;
    var bodies = new ArrayList<ServerSubLevel>();
    bodies.add(parent);
    for (var c : vehicle.carriers)
      if (container.getSubLevel(c.body) instanceof ServerSubLevel b) bodies.add(b);
    if (bodies.size() != vehicle.carriers.size() + 1) return;
    for (int index = 0; index < vehicle.carriers.size(); index++) {
      var carrier = vehicle.carriers.get(index);
      var child = bodies.get(index + 1);
      var center = parent.getPlot().getCenterBlock();
      var pivot =
          parent
              .logicalPose()
              .transformPosition(
                  new Vector3d(
                      center.getX() + .5 + carrier.side,
                      center.getY() + .5 + carrier.pivotHeight,
                      center.getZ() + .5 + carrier.longitudinal));
      // Project the axle centerline, excluding its lateral wing offset, onto bound/adjacent edges.
      var pathPoint =
          new Vector3d(pivot)
              .sub(
                  new Vector3d(carrier.side, carrier.pivotHeight + .0, 0)
                      .rotate(parent.logicalPose().orientation()));
      var current = sample(level, data, bound, new Vec3(pathPoint.x, pathPoint.y, pathPoint.z));
      if (current == null) {
        PREVIOUS.remove(child);
        continue;
      }
      var previous = PREVIOUS.put(child, current);
      if (previous == null
          || previous.revision != previous.rail.revision
          || carrier.capture
          || vehicle.stopping
          || vehicle.maintenance) continue;
      var sweep = sweep(data, previous, current);
      double speed =
          container
              .physicsSystem()
              .getPipeline()
              .getLinearVelocity(parent, new Vector3d())
              .length();
      double distance =
          sweep == null
              ? Double.POSITIVE_INFINITY
              : sweep.stream().mapToDouble(Piece::length).sum();
      if (sweep == null
          || distance
              > Math.max(2, speed * dt * 2 + previous.point.distanceTo(current.point) * 2)) {
        vehicle.stopping = true;
        vehicle.restraints = true;
        vehicle.fault = "Magnetic path changed unexpectedly; capture and inspect the route";
        continue;
      }
      var axis = new Vector3d(1, 0, 0).rotate(parent.logicalPose().orientation());
      for (var piece : sweep)
        for (var span : piece.rail.magnets) {
          if (!span.targets(index)) continue;
          audit(child).weightedDistance += span.weightedLength(piece.from, piece.to);
          double exposure =
              distance > 1e-8
                  ? dt * span.weightedLength(piece.from, piece.to) / distance
                  : span.mode() == MagneticSpan.Mode.DAMP ? dt * span.weight(current.distance) : 0;
          if (exposure <= 0) continue;
          // The equipment direction is canonical to this section, including reverse traversal.
          var edge = TrackGeometry.resolve(level, piece.rail);
          if (edge == null || !piece.rail.valid) continue;
          double midpoint = (piece.from + piece.to) * .5;
          var table = TrackGeometry.table(level, edge);
          double t =
              table.parameterAt(
                  edge.from().equals(piece.rail.from) ? midpoint : table.length() - midpoint);
          var tangent = tangent(level, edge, t, piece.rail);
          apply(
              level,
              bodies,
              child,
              pivot,
              axis,
              new Vector3d(tangent.x, tangent.y, tangent.z),
              span,
              exposure);
        }
    }
  }

  private static Vec3 tangent(
      ServerLevel level, CoasterPathEdge edge, double t, FoundationData.Rail rail) {
    var tangent =
        CoasterPathGraphManager.sampleEdgePosition(level, edge, Math.min(1, t + .001))
            .subtract(
                CoasterPathGraphManager.sampleEdgePosition(level, edge, Math.max(0, t - .001)))
            .normalize();
    return edge.from().equals(rail.from) ? tangent : tangent.scale(-1);
  }

  private static Sample sample(
      ServerLevel level, FoundationData data, FoundationData.Rail bound, Vec3 point) {
    Sample best = null;
    double score = Double.POSITIVE_INFINITY;
    for (var rail : data.rails) {
      if (!rail.valid
          || !rail.variant.equals(bound.variant)
          || !(rail == bound || rail.adjacent(bound))) continue;
      var edge = TrackGeometry.resolve(level, rail);
      if (edge == null) continue;
      var hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, point);
      if (hit == null) continue;
      double d = hit.point().distanceToSqr(point);
      if (d < 12.25 && d < score) {
        score = d;
        best =
            new Sample(
                rail,
                TrackGeometry.distance(level, edge, rail, hit.t()),
                hit.point(),
                tangent(level, edge, hit.t(), rail),
                rail.revision);
      }
    }
    return best;
  }

  private static List<Piece> sweep(FoundationData data, Sample previous, Sample current) {
    if (previous.rail == current.rail)
      return List.of(new Piece(current.rail, previous.distance, current.distance));
    List<Piece> best = null;
    for (boolean forward : new boolean[] {true, false}) {
      var pieces = new ArrayList<Piece>();
      var rail = previous.rail;
      double length = rail.profile.keys().getLast().distance();
      pieces.add(new Piece(rail, previous.distance, forward ? length : 0));
      BlockPos endpoint = forward ? rail.to : rail.from;
      var seen = new HashSet<UUID>();
      seen.add(rail.id);
      for (int n = 0; n < 256; n++) {
        FoundationData.Rail next = null;
        boolean ambiguous = false;
        for (var candidate : data.rails)
          if (!seen.contains(candidate.id)
              && candidate.valid
              && candidate.variant.equals(rail.variant)
              && (candidate.from.equals(endpoint) || candidate.to.equals(endpoint))) {
            if (next != null) {
              ambiguous = true;
              break;
            }
            next = candidate;
          }
        if (next == null || ambiguous) break;
        double start = next.from.equals(endpoint) ? 0 : next.profile.keys().getLast().distance();
        if (next == current.rail) {
          pieces.add(new Piece(next, start, current.distance));
          if (best == null
              || pieces.stream().mapToDouble(Piece::length).sum()
                  < best.stream().mapToDouble(Piece::length).sum()) best = List.copyOf(pieces);
          break;
        }
        double end = start == 0 ? next.profile.keys().getLast().distance() : 0;
        pieces.add(new Piece(next, start, end));
        seen.add(next.id);
        endpoint = start == 0 ? next.to : next.from;
        rail = next;
      }
    }
    return best;
  }

  private static Vector3d inertiaAxis(ServerSubLevel body, Vector3d axis) {
    var local =
        new Vector3d(axis)
            .rotate(new org.joml.Quaterniond(body.logicalPose().orientation()).conjugate());
    return body.getMassTracker()
        .getInertiaTensor()
        .transform(local)
        .rotate(body.logicalPose().orientation());
  }

  private static double kinetic(ServerLevel level, List<ServerSubLevel> bodies) {
    var pipeline = SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    double energy = 0;
    for (var body : bodies) {
      var v = pipeline.getLinearVelocity(body, new Vector3d());
      var w = pipeline.getAngularVelocity(body, new Vector3d());
      energy +=
          .5 * body.getMassTracker().getMass() * v.lengthSquared()
              + .5 * w.dot(inertiaAxis(body, w));
    }
    return energy;
  }

  private static void apply(
      ServerLevel level,
      List<ServerSubLevel> bodies,
      ServerSubLevel child,
      Vector3d pivot,
      Vector3d axis,
      Vector3d tangent,
      MagneticSpan span,
      double exposure) {
    var pipeline = SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    var massData = child.getMassTracker();
    double mass = 0, p = 0;
    for (var body : bodies) {
      double m = body.getMassTracker().getMass();
      mass += m;
      p += m * pipeline.getLinearVelocity(body, new Vector3d()).dot(tangent);
    }
    var r =
        child.logicalPose().transformPosition(new Vector3d(massData.getCenterOfMass())).sub(pivot);
    var orbit = new Vector3d(axis).cross(r);
    var ia = inertiaAxis(child, axis);
    double inertia = axis.dot(ia) + massData.getMass() * orbit.lengthSquared();
    double cross = massData.getMass() * tangent.dot(orbit);
    double angularMomentum =
        ia.dot(pipeline.getAngularVelocity(child, new Vector3d()))
            + massData.getMass() * pipeline.getLinearVelocity(child, new Vector3d()).dot(orbit);
    var result = PassiveCoupler.impulse(mass, inertia, cross, p, angularMomentum, span, exposure);
    if (result.translation() == 0 && result.rotation() == 0) return;
    double before = kinetic(level, bodies);
    // Rapier stores velocities as floats. Avoid impulses below its energy-resolution floor.
    if (result.energyChange() >= -Math.max(1e-7, before * 5e-7)) return;
    double relativeAngularBefore = 0;
    if (span.mode() == MagneticSpan.Mode.DAMP)
      relativeAngularBefore =
          pipeline
              .getAngularVelocity(child, new Vector3d())
              .sub(pipeline.getAngularVelocity(bodies.getFirst(), new Vector3d()))
              .dot(axis);
    for (var body : bodies)
      pipeline.addLinearAndAngularVelocity(
          body, new Vector3d(tangent).mul(result.translation()), new Vector3d());
    pipeline.addLinearAndAngularVelocity(
        child,
        new Vector3d(orbit).mul(result.rotation()),
        new Vector3d(axis).mul(result.rotation()));
    double actual = kinetic(level, bodies) - before;
    audit(child).impulses++;
    audit(child).energyChange += actual;
    if (span.mode() == MagneticSpan.Mode.DAMP) {
      double relativeAngularAfter =
          pipeline
              .getAngularVelocity(child, new Vector3d())
              .sub(pipeline.getAngularVelocity(bodies.getFirst(), new Vector3d()))
              .dot(axis);
      audit(child).damperImpulses++;
      audit(child).damperEnergyChange += actual;
      audit(child).damperAngularSpeedReduction +=
          Math.abs(relativeAngularBefore) - Math.abs(relativeAngularAfter);
    }
    maxEnergyIncrease = Math.max(maxEnergyIncrease, actual);
    dissipatedEnergy -= actual;
    largestSpinGain = Math.max(largestSpinGain, Math.abs(result.rotation()));
    applications++;
  }
}
