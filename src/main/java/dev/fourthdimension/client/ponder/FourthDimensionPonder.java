package dev.fourthdimension.client.ponder;

import dev.fourthdimension.FourthDimension;
import java.util.List;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Only the physical client loads Ponder's scene and rendering classes. */
@EventBusSubscriber(modid = FourthDimension.ID, value = Dist.CLIENT)
public final class FourthDimensionPonder implements PonderPlugin {
  static final List<String> ITEMS =
      List.of(
          "arrow_car",
          "arrow_track",
          "legacy_zacspin_car",
          "legacy_zacspin_track",
          "free_spin_car",
          "free_spin_track",
          "revised_zacspin_car",
          "revised_zacspin_track",
          "pax_car",
          "pax_track",
          "rotation_tool",
          "magnetic_inducer",
          "magnetic_damper",
          "boarding_station",
          "shuttle_winch");

  @SubscribeEvent
  public static void setup(FMLClientSetupEvent event) {
    PonderIndex.addPlugin(new FourthDimensionPonder());
  }

  @Override
  public String getModId() {
    return FourthDimension.ID;
  }

  static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, path);
  }

  @Override
  public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
    for (String item : ITEMS) {
      var scenes = helper.forComponents(id(item));
      scenes.addStoryBoard("matching", FourthDimensionScenes::matching);
      if (item.endsWith("_track") || item.endsWith("_car") || item.equals("rotation_tool"))
        scenes.addStoryBoard("construction", FourthDimensionScenes::construction);
      if (item.startsWith("arrow_") || item.equals("rotation_tool"))
        scenes.addStoryBoard("arrow", FourthDimensionScenes::arrow);
      if (item.startsWith("legacy_zacspin_"))
        scenes.addStoryBoard("legacy", FourthDimensionScenes::legacy);
      if (item.startsWith("free_spin_"))
        scenes.addStoryBoard("free_spin", FourthDimensionScenes::freeSpin);
      if (item.startsWith("revised_zacspin_"))
        scenes.addStoryBoard("revised", FourthDimensionScenes::revised);
      if (item.startsWith("pax_") || item.equals("shuttle_winch"))
        scenes.addStoryBoard("pax", FourthDimensionScenes::pax);
      if (item.startsWith("magnetic_")
          || item.startsWith("free_spin_")
          || item.equals("rotation_tool"))
        scenes.addStoryBoard("magnets", FourthDimensionScenes::magnets);
      if (item.equals("boarding_station")
          || item.equals("rotation_tool")
          || (item.endsWith("_car") || item.endsWith("_track")) && !item.startsWith("pax_"))
        scenes.addStoryBoard("boarding", FourthDimensionScenes::boarding);
    }
  }
}
