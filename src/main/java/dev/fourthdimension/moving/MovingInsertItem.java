package dev.fourthdimension.moving;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
/** Material identity, not a client authorization token. Native geometry must still match. */
public final class MovingInsertItem extends Item {
  public final MovingCapabilityCatalog.Configuration configuration;
  public MovingInsertItem(Properties properties,MovingCapabilityCatalog.Configuration configuration){super(properties);this.configuration=configuration;}
  @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
    super.appendHoverText(stack,context,lines,flag);
    var entry=MovingCapabilityCatalog.entry(configuration);
    lines.add(Component.translatable("moving.fourthdimension.insert",Component.translatable("moving.fourthdimension.configuration."+configuration.id())));
    lines.add(Component.translatable("moving.fourthdimension.insert_pairing",Component.translatable("moving.fourthdimension.contact."+entry.insert().contacts()),Component.translatable("moving.fourthdimension.mount."+entry.insert().mount())));
  }
}
