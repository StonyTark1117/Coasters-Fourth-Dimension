package dev.fourthdimension.mixin;

import dev.fourthdimension.client.SkinAppearanceModels;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="medev.coaster.skins.client.CoasterSkinClient",remap=false)
public abstract class SkinClientRenderMixin {
  @Inject(method="skinFor",at=@At("HEAD"),cancellable=true)
  private static void appearance(SubLevel body,CallbackInfoReturnable<Object> ci) {
    var skin=SkinAppearanceModels.skin(body);
    if(skin!=null)ci.setReturnValue(skin);
  }
}
