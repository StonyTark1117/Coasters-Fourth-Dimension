package dev.fourthdimension.client.ponder;

import com.mojang.logging.LogUtils;
import java.util.HashSet;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in actual client compilation/playback; never runs in an ordinary client. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class PonderValidation {
  private static Language checkedLanguage;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.ponderValidation")) return;
    var minecraft = Minecraft.getInstance();
    if (minecraft.level == null || minecraft.getOverlay() != null) return;
    var language = Language.getInstance();
    if (checkedLanguage == language) return;
    checkedLanguage = language;
    try {
      int registrations = 0;
      var unique = new HashSet<String>();
      for (String item : FourthDimensionPonder.ITEMS) {
        var scenes = PonderIndex.getSceneAccess().compile(FourthDimensionPonder.id(item));
        require(!scenes.isEmpty(), "No tutorial for " + item);
        for (var scene : scenes) {
          String name = scene.getId().getPath();
          var lesson = PonderLessons.ALL.get(name);
          require(lesson != null, "Unknown lesson " + scene.getId());
          require(scene.getTitle().equals(lesson.title()), "Untranslated title " + scene.getId());
          for (int i = 0; i < lesson.text().size(); i++) {
            String key = "fourthdimension.ponder." + name + ".text_" + (i + 1);
            require(I18n.get(key).equals(lesson.text().get(i)), "Wrong translation " + key);
          }
          require(
              scene.getBounds().getXSpan() >= 13 && scene.getBounds().getZSpan() >= 13,
              "Empty schematic " + scene.getId());
          int ticks = 0;
          while (!scene.isFinished() && ticks++ < 2000) scene.tick();
          require(scene.isFinished(), "Scene did not finish " + scene.getId());
          require(ticks >= lesson.text().size() * 100, "Truncated scene " + scene.getId());
          // Reset and replay the real Ponder backup/restore path as well.
          scene.begin();
          ticks = 0;
          while (!scene.isFinished() && ticks++ < 2000) scene.tick();
          require(scene.isFinished(), "Replayed scene did not finish " + scene.getId());
          unique.add(name);
          registrations++;
          LogUtils.getLogger()
              .info("FD_PONDER item={} scene={} playbackTicks={} replay=passed", item, name, ticks);
        }
      }
      require(unique.equals(PonderLessons.ALL.keySet()), "An authored scene is unreachable");
      LogUtils.getLogger()
          .info(
              "FD_PONDER passed items={} registrations={} uniqueScenes={} reloadLanguage={}",
              FourthDimensionPonder.ITEMS.size(),
              registrations,
              unique.size(),
              System.identityHashCode(language));
    } catch (RuntimeException failure) {
      LogUtils.getLogger().error("FD_PONDER validation failed", failure);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new IllegalStateException(message);
  }
}
