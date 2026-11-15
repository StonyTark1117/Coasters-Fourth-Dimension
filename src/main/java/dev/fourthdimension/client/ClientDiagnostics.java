package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.backend.BackendManager;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaterniond;

/** Opt-in evidence logging. Rendering and camera transforms remain owned by Sable. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class ClientDiagnostics {
  private static int ticks;
  private static boolean renderEnvironmentLogged;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.validation") || ++ticks % 100 != 0) return;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;
    var data = EligibilityHooks.CLIENT.get(mc.level);
    var container = SubLevelContainer.getContainer(mc.level);
    if (data == null || container == null) return;
    if (Boolean.getBoolean("fourthdimension.renderValidation")) {
      if (!renderEnvironmentLogged) {
        var backend = BackendManager.currentBackend();
        var graphicsMode = mc.options.graphicsMode().get();
        var requestedGraphicsMode = System.getProperty("fourthdimension.renderValidationGraphicsMode", "");
        if (!requestedGraphicsMode.isBlank()) {
          GraphicsStatus expected;
          try {
            expected = GraphicsStatus.valueOf(requestedGraphicsMode.trim().toUpperCase(java.util.Locale.ROOT));
          } catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException(
                "FD_RENDER_VALIDATION invalid graphics mode '"
                    + requestedGraphicsMode
                    + "'; expected FAST, FANCY, or FABULOUS",
                invalid);
          }
          if (graphicsMode != expected)
            throw new IllegalStateException(
                "FD_RENDER_VALIDATION graphics mode mismatch expected="
                    + expected
                    + " actual="
                    + graphicsMode);
        }
        LogUtils.getLogger()
            .info(
                "FD_CLIENT_RENDER_ENV graphicsMode={} expectedGraphicsMode={} flywheelBackend={} "
                    + "flywheelEnabled={} renderDistance={}",
                graphicsMode,
                requestedGraphicsMode.isBlank() ? "unspecified" : requestedGraphicsMode,
                Backend.REGISTRY.getId(backend),
                BackendManager.isBackendOn(),
                mc.options.renderDistance().get());
        renderEnvironmentLogged = true;
      }
      var render = TrackRenderMetrics.snapshot();
      LogUtils.getLogger()
          .info(
              "FD_CLIENT_RENDER stages={} geometryStages={} avgStageNanos={} meshBuilds={} "
                  + "avgMeshBuildNanos={} inRangeRails={} inRangeQuads={} inRangeDecorationPieces={}",
              render.renderStages(),
              render.geometryStages(),
              render.averageRenderStageNanos(),
              render.meshBuilds(),
              render.averageMeshBuildNanos(),
              render.inRangeRails(),
              render.inRangeQuads(),
              render.inRangeDecorationPieces());
    }
    for (var v : data.vehicles.values()) {
      var parent = container.getSubLevel(v.chassis);
      if (parent == null) continue;
      for (var c : v.carriers) {
        var child = container.getSubLevel(c.body);
        if (child == null) continue;
        var relative =
            new Quaterniond(parent.logicalPose().orientation())
                .conjugate()
                .mul(child.logicalPose().orientation());
        double angle = v.family.axis().angle(c.angle, relative);
        LogUtils.getLogger()
            .info(
                "FD_CLIENT player={} body={} snapshotAngle={} nativeAngle={} occupants={} riding={}"
                    + " camera={}",
                mc.player.getName().getString(),
                c.body,
                c.angle,
                angle,
                c.occupants,
                mc.player.isPassenger(),
                mc.gameRenderer.getMainCamera().rotation());
      }
    }
  }
}
