package dev.fourthdimension.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.fourthdimension.vehicle.*;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class StandingBodyFrameMixin {
 @Inject(method="setupRotations",at=@At("HEAD"),cancellable=true)
 private void fourthdimension$standingTorso(LivingEntity e,PoseStack pose,float age,float yaw,float partial,float scale,CallbackInfo ci){if(!(e instanceof Player)||e.getVehicle()==null)return;var b=Boarding.at(e.level(),e.getVehicle().blockPosition());if(b!=null&&StandingVehicle.is(b.vehicle()))ci.cancel();}
}
