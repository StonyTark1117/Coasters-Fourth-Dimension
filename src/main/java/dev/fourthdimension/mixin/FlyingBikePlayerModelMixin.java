package dev.fourthdimension.mixin;

import dev.fourthdimension.vehicle.FlyingBikeRiderPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Straight prone legs and retained arms; counter-rotate the neck so mouse look remains face-forward. */
@Mixin(HumanoidModel.class)
public abstract class FlyingBikePlayerModelMixin {
  @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("RETURN"))
  private void fourthdimension$proneLimbs(LivingEntity rider,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){
    if(!(rider instanceof Player)||FlyingBikeRiderPose.binding(rider)==null)return;
    var m=(HumanoidModel<?>)(Object)this;
    float lookYaw=net.minecraft.util.Mth.wrapDegrees(net.minecraft.util.Mth.rotLerp(age-rider.tickCount,rider.yHeadRotO,rider.yHeadRot)-180)*(float)(Math.PI/180);
    var angles=new Quaternionf().rotationX(-(float)Math.PI/2).rotateY(lookYaw).rotateX(m.head.xRot).getEulerAnglesZYX(new Vector3f());
    m.head.xRot=angles.x;m.head.yRot=angles.y;m.head.zRot=angles.z;m.hat.copyFrom(m.head);
    m.body.xRot=0;m.body.yRot=0;m.body.zRot=0;
    m.leftLeg.xRot=0;m.rightLeg.xRot=0;m.leftLeg.yRot=0;m.rightLeg.yRot=0;m.leftLeg.zRot=0;m.rightLeg.zRot=0;
    m.leftArm.xRot=-2.8f;m.rightArm.xRot=-2.8f;m.leftArm.yRot=0;m.rightArm.yRot=0;m.leftArm.zRot=0;m.rightArm.zRot=0;
  }
}
