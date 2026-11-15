package dev.fourthdimension.mixin;

import dev.fourthdimension.client.ImmersionEffects;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The effect source is the physical passenger child, not the native chassis. */
@Pseudo
@Mixin(targets="dev.mito.coastersimmersion.api.CoasterCart",remap=false)
public abstract class ImmersionCarrierMixin {
  @Inject(method="cartOf",at=@At("HEAD"),cancellable=true,require=0)
  private static void fourthdimension$carrier(Entity rider,CallbackInfoReturnable<Object> cir) {
    if(!ImmersionEffects.enabled())return;
    var body=ImmersionEffects.carrier(rider);
    if(body!=null)cir.setReturnValue(body);
  }
}
