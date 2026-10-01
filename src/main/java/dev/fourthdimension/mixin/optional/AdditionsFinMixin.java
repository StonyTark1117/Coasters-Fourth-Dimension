package dev.fourthdimension.mixin.optional;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.fourthdimension.integration.ExternalDrivePolicy;
import dev.fourthdimension.integration.additions.AdditionsForces;
import dev.ryanhcode.sable.api.physics.*;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import java.util.Map;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.plushivr.coasterfins.event.FinForceSpanHandler", remap = false)
public abstract class AdditionsFinMixin {
  @Shadow @Final private static Map<ServerSubLevel, ?> HOLD;

  @WrapOperation(
      method = "apply",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/plushivr/coasterfins/logic/CartEngagement;isEngaged(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;)Z"))
  private static boolean fourthdimension$interlocks(
      ServerSubLevel body, Operation<Boolean> original) {
    if (!ExternalDrivePolicy.allows(body.getLevel(), body.getUniqueId())) {
      HOLD.remove(body);
      return false;
    }
    return original.call(body);
  }

  @Inject(method = "applyAssemblyVelocity", at = @At("HEAD"), cancellable = true)
  private static void fourthdimension$ownedBodies(
      SubLevelPhysicsSystem physics,
      ServerSubLevelContainer container,
      ServerSubLevel cart,
      Vector3d center,
      Vector3d velocity,
      Vector3d delta,
      CallbackInfo ci) {
    if (AdditionsForces.apply(physics, container, cart, delta)) ci.cancel();
  }

  @WrapOperation(
      method = "applyAssemblyVelocity",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/ryanhcode/sable/api/physics/PhysicsPipeline;addLinearAndAngularVelocity(Ldev/ryanhcode/sable/api/physics/PhysicsPipelineBody;Lorg/joml/Vector3dc;Lorg/joml/Vector3dc;)V"))
  private static void fourthdimension$excludeNeighbors(
      PhysicsPipeline pipeline,
      PhysicsPipelineBody body,
      Vector3dc linear,
      Vector3dc angular,
      Operation<Void> original) {
    if (!(body instanceof ServerSubLevel sub)
        || !AdditionsForces.owns(sub.getLevel(), sub.getUniqueId()))
      original.call(pipeline, body, linear, angular);
  }
}
