package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
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
  private static int chatTicks;
  private static boolean chatOpened;
  private static boolean exitRequested;

  private FontReloadValidation() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!FontReloadTrace.enabled()) return;
    var minecraft = Minecraft.getInstance();
    if (chatOpened && minecraft.screen instanceof ChatScreen && ++chatTicks == 20) {
      LogUtils.getLogger()
          .info("FD_FONT_CHAT_RENDERED generation={} ticks={}", FontReloadTrace.generation(), chatTicks);
      requestExit(minecraft, "chat-rendered");
    }
    int target = targetReloads();
    if (target > 0 && completed >= target && !chatRequested()) {
      requestExit(minecraft, "reloads-complete");
      return;
    }
    if (requested >= target || requested > completed) return;
    if (FontReloadTrace.generation() < 1
        || minecraft.getOverlay() != null
        || (chatRequested() && minecraft.level == null)) return;
    int request = ++requested;
    int before = FontReloadTrace.generation();
    LogUtils.getLogger()
        .info("FD_FONT_RELOAD_REQUEST request={} target={} generation={}", request, target, before);
    minecraft
        .reloadResourcePacks()
        .whenComplete(
            (ignored, failure) -> {
              if (failure == null) {
                int complete = ++completed;
                LogUtils.getLogger()
                    .info(
                        "FD_FONT_RELOAD_COMPLETE request={} completed={} target={} generation={}",
                        request,
                        complete,
                        target,
                        FontReloadTrace.generation());
                if (complete == target && chatRequested())
                  minecraft.execute(
                      () -> {
                        if (minecraft.level == null) {
                          LogUtils.getLogger().error("FD_FONT_CHAT_SKIPPED no active level");
                          return;
                        }
                        chatOpened = true;
                        minecraft.setScreen(new ChatScreen("/"));
                        LogUtils.getLogger()
                            .info("FD_FONT_CHAT_OPEN generation={}", FontReloadTrace.generation());
                      });
                else if (complete == target) requestExit(minecraft, "reloads-complete");
              } else
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

  private static boolean chatRequested() {
    return Boolean.getBoolean("fourthdimension.fontValidationChat");
  }

  private static void requestExit(Minecraft minecraft, String reason) {
    if (!Boolean.getBoolean("fourthdimension.fontValidationExit") || exitRequested) return;
    exitRequested = true;
    LogUtils.getLogger()
        .info(
            "FD_FONT_VALIDATION_EXIT reason={} completed={} target={} generation={}",
            reason,
            completed,
            targetReloads(),
            FontReloadTrace.generation());
    minecraft.stop();
  }
}
