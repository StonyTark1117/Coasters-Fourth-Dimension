package dev.fourthdimension.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.fourthdimension.vehicle.FlyingRiderPose;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The retained torso faces native north; mouse look belongs to the neck/camera, not a swivel seat. */
@Mixin(LivingEntityRenderer.class)
public abstract class FlyingBodyFrameMixin {
  @Inject(method="setupRotations",at=@At("HEAD"),cancellable=true)
  private void fourthdimension$retainedTorso(LivingEntity rider,PoseStack pose,float age,float yaw,
      float partial,float scale,CallbackInfo ci){
    if(rider instanceof Player&&FlyingRiderPose.binding(rider)!=null)ci.cancel();
  }
}
