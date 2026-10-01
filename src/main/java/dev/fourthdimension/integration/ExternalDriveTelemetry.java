package dev.fourthdimension.integration;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;

/** Optional readouts must still see a car when a drive interlock cancels propulsion. */
public final class ExternalDriveTelemetry {
  private ExternalDriveTelemetry() {}

  public static void held(
      ServerSubLevel body,
      ServerLevel level,
      SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit) {
    if (OptionalIntegrations.supported("coastersengineered", Set.of("1.1.1")))
      dev.fourthdimension.integration.engineered.EngineeredPresence.sample(
          body, level, physics, hit);
  }
}
