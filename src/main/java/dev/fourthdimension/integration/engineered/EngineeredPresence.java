package dev.fourthdimension.integration.engineered;

import dev.jwaterfall.coastersengineered.CartSpeed;
import dev.jwaterfall.coastersengineered.controller.LinearMotorControllerIndex;
import dev.jwaterfall.coastersengineered.observer.CoasterObservers;
import dev.jwaterfall.coastersengineered.span.TrackSpanIndex;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.server.level.ServerLevel;

/** Pinned 1.1.1 presence sampling, isolated from every propulsion/parking path. */
public final class EngineeredPresence {
  private EngineeredPresence() {}

  public static void sample(
      ServerSubLevel body,
      ServerLevel level,
      SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit) {
    if (physics == null || hit == null || hit.edge() == null || hit.edge().bezier() == null) return;
    // Cancelling the whole native drive also cancels its span/observer reports.
    // A captured car must remain present, otherwise an engaged sequence advances
    // as though it departed. These native reports are idempotent within a tick.
    var edge = hit.edge();
    var run = TrackSpanIndex.runAt(level, edge, hit.t());
    if (run != null && run.hasSpan()) {
      var controller = LinearMotorControllerIndex.boundTo(level, run.pos());
      int direction = run.directionAt(edge, hit.t());
      var tangent = CartSpeed.tangent(level, hit);
      if (controller != null && direction != 0 && tangent != null)
        controller.reportCart(
            CartSpeed.velocity(physics, body).dot(tangent.mul(edge.tangentSign() * direction)));
    }
    CoasterObservers.sample(body, level, physics, hit);
  }
}
