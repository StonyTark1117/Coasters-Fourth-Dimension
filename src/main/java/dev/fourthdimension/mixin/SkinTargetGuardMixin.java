package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.SkinAdapter;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="medev.coaster.skins.skin.CartSkinTargets",remap=false)
public abstract class SkinTargetGuardMixin {
  @Inject(method="isCart",at=@At("HEAD"),cancellable=true)
  private static void owned(SubLevel body,CallbackInfoReturnable<Boolean> ci) {
    if(SkinAdapter.owner(body)!=null)ci.setReturnValue(false);
  }
}
