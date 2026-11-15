package dev.fourthdimension.mixin;

import dev.fourthdimension.client.ImmersionEffects;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="dev.mito.coastersimmersion.client.ride.RideScope",remap=false)
public abstract class ImmersionComfortScopeMixin {
  @Inject(method="allows",at=@At("HEAD"),cancellable=true,require=0)
  private static void fourthdimension$comfort(@Coerce Object effect,Entity rider,CallbackInfoReturnable<Boolean> cir) {
    if(ImmersionEffects.enabled()&&ImmersionEffects.comfort(rider)
        &&(effect.toString().equals("CAMERA")||effect.toString().equals("INERTIA")))cir.setReturnValue(false);
  }
}
