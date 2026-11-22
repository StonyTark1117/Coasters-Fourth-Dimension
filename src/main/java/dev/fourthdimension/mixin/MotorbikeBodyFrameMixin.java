package dev.fourthdimension.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.fourthdimension.vehicle.MotorbikeRiderPose;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep a straddling torso aligned with native north; mouse look rotates only head/camera. */
@Mixin(LivingEntityRenderer.class)
public abstract class MotorbikeBodyFrameMixin {
  @Inject(method="setupRotations",at=@At("HEAD"),cancellable=true)
  private void fourthdimension$motorbikeTorso(LivingEntity rider,PoseStack pose,float age,float yaw,float partial,float scale,CallbackInfo ci){
    if(rider instanceof Player&&MotorbikeRiderPose.binding(rider)!=null)ci.cancel();
  }
}
