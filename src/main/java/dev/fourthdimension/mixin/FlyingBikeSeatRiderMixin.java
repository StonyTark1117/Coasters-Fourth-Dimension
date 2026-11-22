package dev.fourthdimension.mixin;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.vehicle.FlyingBikeRiderPose;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Place the actual player eye at the face-forward head; retain native seat identity and parent. */
@Mixin(SeatEntity.class)
public abstract class FlyingBikeSeatRiderMixin {
  @Inject(method="positionRider",at=@At("HEAD"),cancellable=true)
  private void fourthdimension$proneEye(Entity rider,Entity.MoveFunction move,CallbackInfo ci){
    var seat=(SeatEntity)(Object)this;
    if(!(rider instanceof Player)||!seat.hasPassenger(rider)||FlyingBikeRiderPose.binding(rider)==null)return;
    var p=seat.blockPosition();
    move.accept(rider,p.getX()+.5,p.getY()+FlyingBikeRiderPose.EYE_HEIGHT-rider.getEyeHeight(),p.getZ()+FlyingBikeRiderPose.HEAD_Z);
    ci.cancel();
  }
}
