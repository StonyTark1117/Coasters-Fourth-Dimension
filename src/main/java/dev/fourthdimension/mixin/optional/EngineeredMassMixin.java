package dev.fourthdimension.mixin.optional;

import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Engineered counts riveted bodies; our independent rotary joints need explicit accounting. */
@Pseudo
@Mixin(targets = "dev.jwaterfall.coastersengineered.MotorForce", remap = false)
public abstract class EngineeredMassMixin {
  @Inject(method = "mass", at = @At("RETURN"), cancellable = true)
  private static void fourthdimension$includeCarriers(
      ServerLevel level, ServerSubLevel chassis, CallbackInfoReturnable<Double> cir) {
    var v = FoundationData.get(level).vehicles.get(chassis.getUniqueId());
    if (v == null || FoundationData.VARIANT.equals(v.variant) || cir.getReturnValue() <= 0) return;
    var nativeBodies = dev.jwaterfall.coastersengineered.CartAssembly.bodies(level, chassis);
    var container = SubLevelContainer.getContainer(level);
    double total = cir.getReturnValue();
    for (var carrier : v.carriers) {
      if (!(container.getSubLevel(carrier.body) instanceof ServerSubLevel body)) {
        cir.setReturnValue(0.0);
        return;
      }
      if (body.getMassTracker().isInvalid()) {
        cir.setReturnValue(0.0);
        return;
      }
      double mass = body.getMassTracker().getMass();
      if (!Double.isFinite(mass) || mass <= 0) {
        cir.setReturnValue(0.0);
        return;
      }
      if (!nativeBodies.contains(body)) total += mass;
    }
    cir.setReturnValue(total);
  }
}
