package dev.fourthdimension.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.fourthdimension.client.ImmersionEffects;
import dev.fourthdimension.vehicle.FlyingRiderPose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Flying's restrained torso remains on its physical child; mouse look belongs to the head. */
@Pseudo
@Mixin(targets="dev.mito.coastersimmersion.client.PassengerFacing",remap=false)
public abstract class ImmersionPassengerFacingMixin {
  @Inject(method="apply",at=@At("HEAD"),cancellable=true,require=0)
  private static void fourthdimension$retainedTorso(Player rider,PoseStack stack,float partial,CallbackInfoReturnable<Boolean> cir) {
    if(ImmersionEffects.enabled()&&FlyingRiderPose.binding(rider)!=null)cir.setReturnValue(false);
  }
}
