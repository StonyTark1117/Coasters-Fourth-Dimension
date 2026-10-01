package dev.fourthdimension.client;

import dev.fourthdimension.FourthDimension;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Short operation hints accompany the existing hold-to-Ponder entry points. */
@EventBusSubscriber(modid = FourthDimension.ID, value = Dist.CLIENT)
public final class ItemDescriptions {
  private ItemDescriptions() {}

  @SubscribeEvent
  public static void describe(ItemTooltipEvent event) {
    var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
    if (!id.getNamespace().equals(FourthDimension.ID)) return;
    for (int line = 1; line <= 2; line++) {
      String key = "fourthdimension.tooltip." + id.getPath() + "." + line;
      if (I18n.exists(key))
        event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }
  }
}
