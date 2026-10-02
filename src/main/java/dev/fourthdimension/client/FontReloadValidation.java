package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opt-in client resource-reload trigger for lifecycle tracing in environments where input cannot
 * reliably reach a Minecraft window. It is inactive unless both font trace properties are set.
 */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class FontReloadValidation {
  private static int requested;
  private static int completed;

  private FontReloadValidation() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    int target = targetReloads();
    if (!FontReloadTrace.enabled() || requested >= target || requested > completed) return;
    var minecraft = Minecraft.getInstance();
    if (FontReloadTrace.generation() < 1 || minecraft.getOverlay() != null) return;
    int request = ++requested;
    int before = FontReloadTrace.generation();
    LogUtils.getLogger()
        .info("FD_FONT_RELOAD_REQUEST request={} target={} generation={}", request, target, before);
    minecraft
        .reloadResourcePacks()
        .whenComplete(
            (ignored, failure) -> {
              if (failure == null)
                LogUtils.getLogger()
                    .info(
                        "FD_FONT_RELOAD_COMPLETE request={} completed={} target={} generation={}",
                        request,
                        ++completed,
                        target,
                        FontReloadTrace.generation());
              else
                LogUtils.getLogger()
                    .error(
                        "FD_FONT_RELOAD_FAILED request={} target={} generation={}",
                        request,
                        target,
                        before,
                        failure);
            });
  }

  private static int targetReloads() {
    String configured = System.getProperty("fourthdimension.fontValidationReloads", "0");
    try {
      return Math.clamp(Integer.parseInt(configured), 0, 64);
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }
}
