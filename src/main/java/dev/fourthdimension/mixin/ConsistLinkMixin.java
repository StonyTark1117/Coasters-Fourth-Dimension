package dev.fourthdimension.mixin;

import dev.fourthdimension.vehicle.ConsistRules;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CoasterCartTrainLinkConstraint.class, remap = false)
public abstract class ConsistLinkMixin {
  @Inject(
      method =
          "linkCarts(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;D)Z",
      at = @At("HEAD"),
      cancellable = true)
  private static void fourthdimension$coupling(
      ServerLevel level,
      ServerSubLevel a,
      ServerSubLevel b,
      double distance,
      CallbackInfoReturnable<Boolean> cir) {
    if (!ConsistRules.canCouple(level, a, b, distance)) cir.setReturnValue(false);
  }
}
