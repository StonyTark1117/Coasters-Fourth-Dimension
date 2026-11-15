package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.SkinAdapter;
import dev.fourthdimension.runtime.EligibilityHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.UUID;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="medev.coaster.skins.client.ClientSkinState",remap=false)
public abstract class SkinClientStateGuardMixin {
  @Unique private static boolean fourthdimension$owned(ResourceKey<Level> dimension,UUID id) {
    var level=Minecraft.getInstance().level;
    if(level==null||!level.dimension().equals(dimension))return false;
    var data=EligibilityHooks.data(level);
    return data!=null && data.vehicles.values().stream().anyMatch(v->v.chassis.equals(id)||v.carriers.stream().anyMatch(c->c.body.equals(id)));
  }
  @Inject(method="get",at=@At("HEAD"),cancellable=true)
  private static void nativeState(ResourceKey<Level> dimension,UUID id,CallbackInfoReturnable<Object> ci) {
    if(fourthdimension$owned(dimension,id))ci.setReturnValue(SkinAdapter.nativeSkin());
  }
  @Inject(method="reversed",at=@At("HEAD"),cancellable=true)
  private static void originalFacing(ResourceKey<Level> dimension,UUID id,CallbackInfoReturnable<Boolean> ci) {
    if(fourthdimension$owned(dimension,id))ci.setReturnValue(false);
  }
}
