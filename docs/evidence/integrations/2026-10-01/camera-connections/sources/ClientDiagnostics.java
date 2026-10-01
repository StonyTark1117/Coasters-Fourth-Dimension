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
  public static void camera(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")
        || event.getStage()
            != net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
    CarrierCameraDiagnostics.sample(event);
    if (dev.fourthdimension.integration.OptionalIntegrations.supported(
        "coasters_immersion", java.util.Set.of("0.2.3")))
      dev.fourthdimension.integration.immersion.ImmersionCameraDiagnostics.sample(event);
  }

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    ticks++;
    var mc = Minecraft.getInstance();
    if (ticks % 10 == 0
        && mc.screen instanceof net.createmod.ponder.foundation.ui.PonderUI ponder) {
      var scene = ponder.getActiveScene();
      if (scene != null && scene.getNamespace().equals("fourthdimension"))
        LogUtils.getLogger()
            .info(
                "FD_PONDER player={} scene={} time={} total={} keyframes={} finished={} width={}"
                    + " height={}",
                mc.player == null ? "none" : mc.player.getName().getString(),
                scene.getId(),
                scene.getCurrentTime(),
                scene.getTotalTime(),
                java.util.stream.IntStream.range(0, scene.getKeyframeCount())
                    .mapToObj(i -> Integer.toString(scene.getKeyframeTime(i)))
                    .collect(java.util.stream.Collectors.joining(",")),
                scene.isFinished(),
                ponder.width,
                ponder.height);
    }
    if (ticks % 100 != 0) return;
    if (mc.level == null || mc.player == null) return;
    if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit
        && mc.level.getBlockEntity(hit.getBlockPos())
            instanceof dev.fourthdimension.integration.create.RideInformation.Source source) {
      LogUtils.getLogger()
          .info(
              "FD_STATION player={} pos={} telemetry={}",
              mc.player.getName().getString(),
              hit.getBlockPos(),
              source.telemetry());
    }
    var data = EligibilityHooks.CLIENT.get(mc.level);
    var container = SubLevelContainer.getContainer(mc.level);
    if (data == null || container == null) return;
    for (var rail : data.rails) {
      if (!rail.appearance
          .provider()
          .equals(dev.fourthdimension.integration.AppearanceProviders.TRACK_STYLES)) continue;
      LogUtils.getLogger()
          .info(
              "FD_STYLE_VIEW player={} rail={} savedStyle={} effectiveStyle={} externalSpine={}",
              mc.player.getName().getString(),
              rail.id,
              rail.appearance.style(),
              rail.appearance.effective(rail.family).style(),
              TrackStylesVisual.available(rail));
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
