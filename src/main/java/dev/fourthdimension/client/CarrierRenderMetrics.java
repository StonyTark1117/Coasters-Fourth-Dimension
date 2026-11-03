package dev.fourthdimension.client;

import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.render.vanilla.VanillaChunkedSubLevelRenderData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Opt-in observations of Sable's chunked sublevel section-layer renderer. */
public final class CarrierRenderMetrics {
  public record Snapshot(
      long sectionLayerCalls, Set<UUID> sectionLayerBodies, Set<UUID> coasterCartBodies) {}

  private static final boolean ENABLED = Boolean.getBoolean("fourthdimension.carrierValidationRender");
  private static long sectionLayerCalls;
  private static Set<UUID> sectionLayerBodies = new HashSet<>();
  private static Set<UUID> coasterCartBodies = new HashSet<>();

  private CarrierRenderMetrics() {}

  public static void reset() {
    sectionLayerCalls = 0;
    sectionLayerBodies = new HashSet<>();
    coasterCartBodies = new HashSet<>();
  }

  /** Called from Coasters' native cart per-cell render submission after its camera cull. */
  public static void coasterCartCell(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos cell) {
    if (!ENABLED) return;
    var body = Sable.HELPER.getContaining(level, cell);
    if (body != null) coasterCartBodies.add(body.getUniqueId());
  }

  /** Called at Sable's actual chunked section-layer dispatch point on the render thread. */
  public static void sectionLayer(Iterable<?> bodies) {
    if (!ENABLED) return;
    sectionLayerCalls++;
    // VanillaSubLevelRenderDispatcher calls renderChunkedSubLevel for precisely these bodies.
    for (var value : bodies)
      if (value instanceof ClientSubLevel body
          && body.getRenderData() instanceof VanillaChunkedSubLevelRenderData)
        sectionLayerBodies.add(body.getUniqueId());
  }

  public static Snapshot snapshot() {
    return new Snapshot(
        sectionLayerCalls, Set.copyOf(sectionLayerBodies), Set.copyOf(coasterCartBodies));
  }
}
