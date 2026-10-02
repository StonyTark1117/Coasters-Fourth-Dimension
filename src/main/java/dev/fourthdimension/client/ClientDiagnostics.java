package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
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

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.validation") || ++ticks % 100 != 0) return;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;
    var data = EligibilityHooks.CLIENT.get(mc.level);
    var container = SubLevelContainer.getContainer(mc.level);
    if (data == null || container == null) return;
    if (Boolean.getBoolean("fourthdimension.renderValidation")) {
      var render = TrackRenderMetrics.snapshot();
      LogUtils.getLogger()
          .info(
              "FD_CLIENT_RENDER stages={} avgStageNanos={} meshBuilds={} avgMeshBuildNanos={}"
                  + " inRangeRails={} inRangeQuads={} inRangeDecorationPieces={}",
              render.renderStages(),
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
        double angle = 2 * Math.atan2(relative.x, relative.w);
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
