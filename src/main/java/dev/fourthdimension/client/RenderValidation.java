package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opt-in completion gate for a live original-track render sample.
 *
 * <p>The check only proves that this addon's render stage built and submitted original-track
 * geometry in the active client world. It deliberately has no decoration, shader, frame-rate, or
 * whole-frame requirement.
 */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class RenderValidation {
  private static boolean completed;

  private RenderValidation() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (completed || !Boolean.getBoolean("fourthdimension.renderValidationExit")) return;
    var minecraft = Minecraft.getInstance();
    if (minecraft.level == null || minecraft.getOverlay() != null) return;
    var sample = TrackRenderMetrics.snapshot();
    int requiredStages = requiredStages();
    if (sample.renderStages() < requiredStages) return;
    if (!hasRequiredOriginalGeometry(sample, requiredStages)) {
      throw new IllegalStateException(
          "FD_RENDER_VALIDATION incomplete stages="
              + sample.renderStages()
              + " geometryStages="
              + sample.geometryStages()
              + " meshes="
              + sample.meshBuilds()
              + " rails="
              + sample.inRangeRails()
              + " quads="
              + sample.inRangeQuads());
    }
    completed = true;
    LogUtils.getLogger()
        .info(
            "FD_RENDER_VALIDATION passed stages={} target={} geometryStages={} meshes={} "
                + "avgMeshBuildNanos={} avgStageNanos={} rails={} quads={} decorationPieces={}",
            sample.renderStages(),
            requiredStages,
            sample.geometryStages(),
            sample.meshBuilds(),
            sample.averageMeshBuildNanos(),
            sample.averageRenderStageNanos(),
            sample.inRangeRails(),
            sample.inRangeQuads(),
            sample.inRangeDecorationPieces());
    minecraft.stop();
  }

  static boolean hasRequiredOriginalGeometry(TrackRenderMetrics.Snapshot sample, int requiredStages) {
    return sample.meshBuilds() >= 1
        && sample.geometryStages() >= requiredStages
        && sample.inRangeRails() >= requiredStages
        && sample.inRangeQuads() >= requiredStages;
  }

  private static int requiredStages() {
    try {
      return Math.clamp(
          Integer.parseInt(System.getProperty("fourthdimension.renderValidationStages", "500")),
          100,
          10_000);
    } catch (NumberFormatException ignored) {
      return 500;
    }
  }
}
