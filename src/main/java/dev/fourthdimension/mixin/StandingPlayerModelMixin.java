package dev.fourthdimension.mixin;
import dev.fourthdimension.vehicle.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HumanoidModel.class)
public abstract class StandingPlayerModelMixin {
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("RETURN"))
 private void fourthdimension$standingLegs(LivingEntity entity,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){if(!(entity instanceof Player)||entity.getVehicle()==null)return;var b=Boarding.at(entity.level(),entity.getVehicle().blockPosition());if(b==null||!StandingVehicle.is(b.vehicle()))return;var m=(HumanoidModel<?>)(Object)this;m.leftLeg.xRot=m.rightLeg.xRot=m.leftLeg.yRot=m.rightLeg.yRot=m.leftLeg.zRot=m.rightLeg.zRot=0;m.leftArm.xRot=m.rightArm.xRot=0;m.leftArm.zRot=m.rightArm.zRot=0;}
}
