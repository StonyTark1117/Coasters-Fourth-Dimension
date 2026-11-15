package dev.fourthdimension;

import dev.fourthdimension.track.TrackItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.*;

public final class ContentTabs {
  private ContentTabs() {}

  public static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FourthDimension.ID);
  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
      TABS.register(
          "main",
          () ->
              CreativeModeTab.builder()
                  .title(Component.literal("Coasters: Fourth Dimension"))
                  .icon(() -> TrackItems.ARROW.get().getDefaultInstance())
                  .displayItems(
                      (parameters, output) -> {
                        TrackItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                        dev.fourthdimension.vehicle.DiveContent.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                        dev.fourthdimension.vehicle.PoweredInvertedYawContent.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                        dev.fourthdimension.vehicle.S2SpinningContent.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                        output.accept(dev.fourthdimension.vehicle.YawMarkers.ITEM.get());
                      })
                  .build());
}
