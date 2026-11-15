package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.SkinAdapter;
import dev.ryanhcode.sable.Sable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="medev.coaster.skins.item.ScrewDriverItem",remap=false)
public abstract class SkinScrewdriverGuardMixin {
  @Inject(method="onItemUseFirst",at=@At("HEAD"),cancellable=true)
  private void explain(ItemStack stack,UseOnContext context,CallbackInfoReturnable<InteractionResult> ci) {
    var body=Sable.HELPER.getContaining(context.getLevel(),context.getClickedPos());
    if(SkinAdapter.owner(body)==null)return;
    if(!context.getLevel().isClientSide()&&context.getPlayer()!=null)
      context.getPlayer().displayClientMessage(Component.literal("Use the Rotation Tool > Appearance. Full Skin bodies and seat/physics styles cannot replace this vehicle."),false);
    ci.setReturnValue(InteractionResult.sidedSuccess(context.getLevel().isClientSide()));
  }
}
