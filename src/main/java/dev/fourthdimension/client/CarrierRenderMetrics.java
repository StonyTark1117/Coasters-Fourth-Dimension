package dev.fourthdimension.client;

import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.vanilla.VanillaChunkedSubLevelRenderData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Opt-in observations of Sable's chunked sublevel section-layer renderer. */
public final class CarrierRenderMetrics {
  public record Snapshot(long sectionLayerCalls, Set<UUID> sectionLayerBodies) {}

  private static final boolean ENABLED = Boolean.getBoolean("fourthdimension.carrierValidationRender");
  private static long sectionLayerCalls;
  private static Set<UUID> sectionLayerBodies = new HashSet<>();

  private CarrierRenderMetrics() {}

  public static void reset() {
    sectionLayerCalls = 0;
    sectionLayerBodies = new HashSet<>();
  }

  /** Called at Sable's actual chunked section-layer dispatch point on the render thread. */
  public static void sectionLayer(Iterable<ClientSubLevel> bodies) {
    if (!ENABLED) return;
    sectionLayerCalls++;
    // VanillaSubLevelRenderDispatcher calls renderChunkedSubLevel for precisely these bodies.
    for (var body : bodies)
      if (body.getRenderData() instanceof VanillaChunkedSubLevelRenderData)
        sectionLayerBodies.add(body.getUniqueId());
  }

  public static Snapshot snapshot() {
    return new Snapshot(sectionLayerCalls, Set.copyOf(sectionLayerBodies));
  }
}
