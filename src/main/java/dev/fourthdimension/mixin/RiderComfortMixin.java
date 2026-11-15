package dev.fourthdimension.mixin;

import dev.fourthdimension.client.RiderComfort;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import org.joml.Quaterniond;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sable composes this CAMERA frame with normal mouse look exactly once. */
// RETURN callbacks are inserted before earlier return injections. Apply after
// Immersion's priority-1000 mixin so our managed-seat return precedes its filter.
@Mixin(value = EntitySubLevelRotationHelper.class, remap = false, priority = 900)
public abstract class RiderComfortMixin {
  @Inject(method = "getEntityOrientation", at = @At("RETURN"), cancellable = true)
  private static void fourthdimension$comfort(Entity entity,
      Function<SubLevel, Pose3dc> pose, float partialTick,
      EntitySubLevelRotationHelper.Type type, CallbackInfoReturnable<Quaterniond> callback) {
    if(type!=EntitySubLevelRotationHelper.Type.CAMERA
        ||dev.fourthdimension.client.ImmersionEffects.carrier(entity)==null)return;
    var neutral=dev.fourthdimension.vehicle.FlyingRiderPose.orientation(entity,pose,type,callback.getReturnValue());
    neutral=dev.fourthdimension.client.ImmersionEffects.compose(entity,neutral);
    callback.setReturnValue(RiderComfort.orientation(entity, type, neutral));
  }
}
