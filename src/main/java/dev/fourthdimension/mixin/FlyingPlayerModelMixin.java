package dev.fourthdimension.mixin;

import dev.fourthdimension.vehicle.FlyingRiderPose;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native body orientation supplies posture once; only legs and the neutral neck pose are authored. */
@Mixin(HumanoidModel.class)
public abstract class FlyingPlayerModelMixin {
  @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("RETURN"))
  private void fourthdimension$flyingPose(LivingEntity entity,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){
    if(!(entity instanceof Player))return;
    double phi=FlyingRiderPose.phi(entity,s->((ClientSubLevel)s).renderPose());if(!Double.isFinite(phi))return;
    var model=(HumanoidModel<?>)(Object)this;
    float lookYaw=net.minecraft.util.Mth.wrapDegrees(net.minecraft.util.Mth.rotLerp(
        age-entity.tickCount,entity.yHeadRotO,entity.yHeadRot)-180)*(float)(Math.PI/180);
    var euler=new Quaternionf().rotationX((float)-phi).rotateY(lookYaw).rotateX(model.head.xRot).getEulerAnglesZYX(new Vector3f());
    model.head.xRot=euler.x;model.head.yRot=euler.y;model.head.zRot=euler.z;model.hat.copyFrom(model.head);
    float bend=(float)(1-phi/(Math.PI/2));
    model.leftLeg.xRot*=bend;model.rightLeg.xRot*=bend;
    model.leftLeg.yRot*=bend;model.rightLeg.yRot*=bend;model.leftLeg.zRot*=bend;model.rightLeg.zRot*=bend;
    model.leftArm.xRot=0;model.rightArm.xRot=0;model.leftArm.zRot=0;model.rightArm.zRot=0;
  }
}
