package dev.fourthdimension.mixin;

import dev.fourthdimension.client.ImmersionEffects;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="dev.mito.coastersimmersion.client.camera.BodyTravel",remap=false)
public abstract class ImmersionComfortTravelMixin {
  @Inject(method="offset",at=@At("HEAD"),cancellable=true,require=0)
  private static void fourthdimension$comfort(CallbackInfoReturnable<Double> cir) {
    if(ImmersionEffects.enabled()&&ImmersionEffects.localComfort())cir.setReturnValue(0.0);
  }
}
