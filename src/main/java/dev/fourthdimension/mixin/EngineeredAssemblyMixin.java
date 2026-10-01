package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.EngineeredAssembly;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Independently supplied membership: rotary children are not Engineered's native riveted bodies.
 */
@Pseudo
@Mixin(targets = "dev.jwaterfall.coastersengineered.CartAssembly", remap = false)
public abstract class EngineeredAssemblyMixin {
  @Inject(
      method =
          "bodies(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;)Ljava/util/List;",
      at = @At("RETURN"),
      cancellable = true,
      require = 0)
  private static void fourthdimension$includeCarriers(
      ServerLevel level,
      ServerSubLevel chassis,
      CallbackInfoReturnable<List<ServerSubLevel>> callback) {
    callback.setReturnValue(
        EngineeredAssembly.includeCarriers(level, chassis, callback.getReturnValue()));
  }
}
