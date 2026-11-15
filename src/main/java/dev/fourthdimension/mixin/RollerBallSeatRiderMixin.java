package dev.fourthdimension.mixin;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.vehicle.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SeatEntity.class)
public abstract class RollerBallSeatRiderMixin {
 @Inject(method="positionRider",at=@At("HEAD"),cancellable=true)
 private void fourthdimension$rollerHip(Entity rider,Entity.MoveFunction move,CallbackInfo ci){
  var seat=(SeatEntity)(Object)this;if(!(rider instanceof Player)||!seat.hasPassenger(rider))return;
  var b=Boarding.at(seat.level(),seat.blockPosition());if(b==null||!RollerBallVehicle.is(b.vehicle()))return;
  var state=seat.level().getBlockState(seat.blockPosition());
  move.accept(rider,seat.getX()+RollerBallSeatBlock.hipX(state)-.5,seat.blockPosition().getY()-.25,seat.getZ()+.5);ci.cancel();
 }
}
