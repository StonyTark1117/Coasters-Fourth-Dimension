package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Finite pinned ordinary-track envelope; appearances never grant compatibility. */
public final class WingTrack {
  public static final double MIN_RADIUS = 16;
  public static final double MAX_FRAME_RATE = .05;
  private static final Map<CoasterPathEdge, Boolean> SHAPES = new WeakHashMap<>();
  private WingTrack() {}

  public static String rejection(Level level, CoasterPathEdge edge) {
    if (CoasterPathGraphManager.edgeHostedOnSubLevel(level, edge))
      return "Wing requires fixed world track";
    if (edge.bezier() == null || !dev.fourthdimension.integration.CtnbTracks.nativeContacts(dev.fourthdimension.integration.CtnbTracks.material(level,edge)))
      return "Wing requires ordinary Coasters or certified CTNB N-contact track";
    var data = EligibilityHooks.data(level);
    if (data == null || data.rails.stream().anyMatch(r -> r.matches(edge.from(), edge.to())))
      return "Wing cannot use a functional 4D section";
    if (!SHAPES.computeIfAbsent(edge, e -> shapeFits(level, e)))
      return "Wing needs radius >=16 blocks and bank change <=0.05 radians per block";
    return null;
  }

  private static boolean shapeFits(Level level, CoasterPathEdge edge) {
    Vec3 previous = null, previousTangent = null, previousUp = null;
    for (int i = 0; i <= 64; i++) {
      double t = i / 64.0;
      var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
      var basis = CoasterPathTrackFrame.trackBasisAtHit(level,
          new CoasterPathTrackFrame.GraphHit(point, edge, t));
      if (previous != null) {
        double ds = point.distanceTo(previous);
        if (ds < 1e-6) return false;
        double turn = Math.acos(Math.clamp(previousTangent.dot(basis.forward()), -1, 1));
        // Removing tangent change leaves bank twist. Up-vector change is conservative on grades.
        double bank = Math.acos(Math.clamp(previousUp.dot(basis.up()), -1, 1));
        if (turn / ds > 1 / MIN_RADIUS + 1e-4 || bank / ds > MAX_FRAME_RATE + 1e-4)
          return false;
      }
      previous = point;
      previousTangent = basis.forward();
      previousUp = basis.up();
    }
    return true;
  }

  public static CoasterPathEdge support(Level level, FoundationData.Vehicle vehicle) {
    if (vehicle.boundFrom == null || !level.hasChunkAt(vehicle.boundFrom)
        || !level.hasChunkAt(vehicle.boundTo)) return null;
    var moving = dev.fourthdimension.moving.MovingRouteContext.liveEdge(level,vehicle.boundFrom,vehicle.boundTo);
    if (moving != null) return rejection(level,moving) == null ? moving : null;
    var anchor = level.getBlockEntity(vehicle.boundFrom);
    if (!(anchor instanceof dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity a))
      return null;
    var curve = a.getAnchorPeerCurvesView().get(vehicle.boundTo);
    if (curve == null) return null;
    if (!curve.isPrimary()) curve = curve.secondary();
    var edge = CoasterPathEdge.curve(curve.bePositions.getFirst(), curve.bePositions.getSecond(), curve);
    return rejection(level, edge) == null ? edge : null;
  }

  public static BlockPos obstruction(Level level, CoasterPathTrackFrame.GraphHit hit) {
    var basis = CoasterPathTrackFrame.trackBasisAtHit(level, hit);
    // Separate outboard rider volumes leave native rail/contact/anchor space untouched.
    for (double side : new double[] {-3.5, 3.5}) {
      var blocked = SweptClearance.firstObstruction(level,
          hit.point().add(basis.right().scale(side)).add(basis.up().scale(1.9)),
          basis.right(), basis.up(), basis.forward(), 1.3, 1.7, 1.5);
      if (blocked != null) return blocked;
    }
    return SweptClearance.firstObstruction(level, hit.point().add(basis.up().scale(2)),
        basis.right(), basis.up(), basis.forward(), 2.2, .7, .6);
  }
}
