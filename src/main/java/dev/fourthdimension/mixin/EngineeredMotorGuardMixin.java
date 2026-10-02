package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.EngineeredInterlock;
import dev.fourthdimension.integration.OptionalIntegrations;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Optional target; uses only base-stack types and independently authored hold policy. */
@Pseudo
@Mixin(targets = "dev.jwaterfall.coastersengineered.MotorDrive", remap = false)
public abstract class EngineeredMotorGuardMixin {
  @Inject(
      method =
          "apply(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/system/SubLevelPhysicsSystem;Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathTrackFrame$GraphHit;Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathTrackFrame$TrackBasis;D)V",
      // The native prefix only resolves the span and reports cart speed/presence.
      // Keep those reports alive while denying all following propulsion/parking work.
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/jwaterfall/coastersengineered/controller/LinearMotorControllerBlockEntity;commandMode()Ldev/jwaterfall/coastersengineered/controller/MotorStep$Kind;"),
      cancellable = true,
      require = 0)
  private static void fourthdimension$holdMotor(
      ServerSubLevel body,
      ServerLevel level,
      SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit,
      CoasterPathTrackFrame.TrackBasis basis,
      double dt,
      CallbackInfo callback) {
    if (OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()
        && EngineeredInterlock.holds(body, level)) callback.cancel();
  }
}
