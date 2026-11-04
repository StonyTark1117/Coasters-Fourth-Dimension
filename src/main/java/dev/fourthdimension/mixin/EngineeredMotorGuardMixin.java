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
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Optional target; uses only base-stack types and independently authored hold policy. */
@Pseudo
@Mixin(targets = "dev.jwaterfall.coastersengineered.MotorDrive", remap = false)
public abstract class EngineeredMotorGuardMixin {
  @Inject(
      method =
          "apply(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/system/SubLevelPhysicsSystem;Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathTrackFrame$GraphHit;Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathTrackFrame$TrackBasis;D)V",
      at = @At("HEAD"),
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

  @ModifyArgs(
      method = "launch",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/jwaterfall/coastersengineered/MotorForce;accelerate(Ldev/jwaterfall/coastersengineered/span/TrackSpanBehaviour;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Ldev/ryanhcode/sable/sublevel/system/SubLevelPhysicsSystem;DLorg/joml/Vector3d;DDD)Ldev/jwaterfall/coastersengineered/MotorForce$Outcome;"),
      require = 0)
  private static void fourthdimension$boundRecoveryLaunchTarget(Args args) {
    fourthdimension$boundRecoveryTarget(args);
  }

  @ModifyArgs(
      method = "park",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/jwaterfall/coastersengineered/MotorForce;accelerate(Ldev/jwaterfall/coastersengineered/span/TrackSpanBehaviour;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Ldev/ryanhcode/sable/sublevel/system/SubLevelPhysicsSystem;DLorg/joml/Vector3d;DDD)Ldev/jwaterfall/coastersengineered/MotorForce$Outcome;"),
      require = 0)
  private static void fourthdimension$boundRecoveryParkTarget(Args args) {
    fourthdimension$boundRecoveryTarget(args);
  }

  private static void fourthdimension$boundRecoveryTarget(Args args) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()
        || !(args.get(1) instanceof ServerSubLevel body)
        || !(body.getLevel() instanceof ServerLevel level)
        || !(args.get(6) instanceof Double requested)) return;
    args.set(6, EngineeredInterlock.boundedTarget(body, level, requested));
  }
}
