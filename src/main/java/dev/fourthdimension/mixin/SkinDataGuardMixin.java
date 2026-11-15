package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.SkinAdapter;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="medev.coaster.skins.skin.CartSkinData",remap=false)
public abstract class SkinDataGuardMixin {
  @Inject(method="get",at=@At("HEAD"),cancellable=true)
  private static void nativePhysics(ServerSubLevel body,CallbackInfoReturnable<Object> ci) {
    if(SkinAdapter.owner(body)!=null)ci.setReturnValue(SkinAdapter.nativeSkin());
  }
}
