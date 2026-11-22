package dev.fourthdimension.mixin;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.vehicle.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native hip is .5 above the saddle block origin on both logical sides. */
@Mixin(SeatEntity.class)
public abstract class MotorbikeSeatRiderMixin {
  @Inject(method="positionRider",at=@At("HEAD"),cancellable=true)
  private void fourthdimension$motorbikeHip(Entity rider,Entity.MoveFunction move,CallbackInfo ci){
    var seat=(SeatEntity)(Object)this;
    if(!(rider instanceof Player)||!seat.hasPassenger(rider)||MotorbikeRiderPose.binding(rider)==null)return;
    var pos=seat.blockPosition();move.accept(rider,pos.getX()+.5,pos.getY()-.25,pos.getZ()+.5);ci.cancel();
  }
}
