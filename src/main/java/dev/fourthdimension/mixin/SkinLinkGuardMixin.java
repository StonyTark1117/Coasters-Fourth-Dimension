package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.SkinAdapter;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="medev.coaster.skins.skin.LinkedCartSkins",remap=false)
public abstract class SkinLinkGuardMixin {
  @Inject(method="copy",at=@At("HEAD"),cancellable=true)
  private static void owned(ServerSubLevel source,ServerSubLevel target,CallbackInfoReturnable<Boolean> ci) {
    if(SkinAdapter.owner(source)!=null||SkinAdapter.owner(target)!=null)ci.setReturnValue(false);
  }
}
