package dev.fourthdimension.mixin;

import dev.fourthdimension.vehicle.MotorbikeRiderPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A true straddle silhouette: narrow saddle between separated thighs, hands forward at grips. */
@Mixin(HumanoidModel.class)
public abstract class MotorbikePlayerModelMixin {
  @org.spongepowered.asm.mixin.Unique private boolean fourthdimension$bikeApplied;
  @org.spongepowered.asm.mixin.Unique private float fourthdimension$leftX,fourthdimension$rightX;
  @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("HEAD"))
  private void fourthdimension$restoreHipWidth(LivingEntity entity,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){
    if(fourthdimension$bikeApplied){var m=(HumanoidModel<?>)(Object)this;m.leftLeg.x=fourthdimension$leftX;m.rightLeg.x=fourthdimension$rightX;fourthdimension$bikeApplied=false;}
  }
  @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("RETURN"))
  private void fourthdimension$motorbikePose(LivingEntity entity,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){
    if(!(entity instanceof Player)||MotorbikeRiderPose.binding(entity)==null)return;
    var m=(HumanoidModel<?>)(Object)this;
    m.body.xRot=0;m.body.yRot=0;m.body.zRot=0;
    fourthdimension$leftX=m.leftLeg.x;fourthdimension$rightX=m.rightLeg.x;fourthdimension$bikeApplied=true;
    m.leftLeg.x=4;m.rightLeg.x=-4;
    m.leftLeg.xRot=-.70f;m.rightLeg.xRot=-.70f;
    m.leftLeg.yRot=.15f;m.rightLeg.yRot=-.15f;
    m.leftLeg.zRot=-.10f;m.rightLeg.zRot=.10f;
    m.leftArm.xRot=-1.35f;m.rightArm.xRot=-1.35f;
    m.leftArm.yRot=-.05f;m.rightArm.yRot=.05f;
    m.leftArm.zRot=0;m.rightArm.zRot=0;
    m.head.yRot=net.minecraft.util.Mth.wrapDegrees(net.minecraft.util.Mth.rotLerp(age-entity.tickCount,entity.yHeadRotO,entity.yHeadRot)-180)*(float)(Math.PI/180);
    m.head.zRot=0;m.hat.copyFrom(m.head);
  }
}
