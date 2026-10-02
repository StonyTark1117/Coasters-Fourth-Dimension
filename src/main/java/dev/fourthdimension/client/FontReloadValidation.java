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
  private static boolean requested;

  private FontReloadValidation() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (requested
        || !FontReloadTrace.enabled()
        || !Boolean.getBoolean("fourthdimension.fontValidationReload")) return;
    var minecraft = Minecraft.getInstance();
    if (FontReloadTrace.generation() < 1 || minecraft.getOverlay() != null) return;
    requested = true;
    int before = FontReloadTrace.generation();
    LogUtils.getLogger().info("FD_FONT_RELOAD_REQUEST generation={}", before);
    minecraft
        .reloadResourcePacks()
        .whenComplete(
            (ignored, failure) -> {
              if (failure == null)
                LogUtils.getLogger()
                    .info("FD_FONT_RELOAD_COMPLETE generation={}", FontReloadTrace.generation());
              else
                LogUtils.getLogger().error("FD_FONT_RELOAD_FAILED generation={}", before, failure);
            });
  }
}
