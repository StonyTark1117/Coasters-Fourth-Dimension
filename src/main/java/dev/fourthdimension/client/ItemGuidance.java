package dev.fourthdimension.client;

import dev.fourthdimension.FourthDimension;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Localized operating guidance, including tooltips built without a player for search. */
@EventBusSubscriber(modid = FourthDimension.ID, value = Dist.CLIENT)
public final class ItemGuidance {
  private ItemGuidance() {}

  @SubscribeEvent
  public static void tooltip(ItemTooltipEvent event) {
    var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
    if (!id.getNamespace().equals(FourthDimension.ID)) return;
    String prefix = "tooltip." + FourthDimension.ID + "." + id.getPath() + ".";
    int insertion = Math.min(1, event.getToolTip().size());
    for (int line = 1; Language.getInstance().has(prefix + line); line++) {
      event
          .getToolTip()
          .add(insertion++, Component.translatable(prefix + line).withStyle(ChatFormatting.GRAY));
    }
  }
}
