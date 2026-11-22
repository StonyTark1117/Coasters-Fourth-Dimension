package dev.fourthdimension.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.fourthdimension.vehicle.FlyingBikeRiderPose;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Rigid prone torso pivoted around the real eye; native chassis supplies track roll only once. */
@Mixin(LivingEntityRenderer.class)
public abstract class FlyingBikeBodyFrameMixin {
  @Inject(method="setupRotations",at=@At("HEAD"),cancellable=true)
  private void fourthdimension$proneTorso(LivingEntity rider,PoseStack pose,float age,float yaw,float partial,float scale,CallbackInfo ci){
    if(!(rider instanceof Player)||FlyingBikeRiderPose.binding(rider)==null)return;
    pose.translate(0,rider.getEyeHeight(),0);
    pose.mulPose(Axis.XP.rotationDegrees(-90));
    pose.translate(0,-rider.getEyeHeight(),0);ci.cancel();
  }
}
