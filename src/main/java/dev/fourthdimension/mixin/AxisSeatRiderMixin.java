package dev.fourthdimension.mixin;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.vehicle.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Uses Create's native attachment move; the player's hip coincides with the authored cushion. */
@Mixin(SeatEntity.class)
public abstract class AxisSeatRiderMixin {
 @Inject(method="positionRider",at=@At("HEAD"),cancellable=true)
 private void fourthdimension$axisHip(Entity rider,Entity.MoveFunction move,CallbackInfo ci){
  var seat=(SeatEntity)(Object)this;
  if(!(rider instanceof Player)||!seat.hasPassenger(rider))return;
  var binding=Boarding.at(seat.level(),seat.blockPosition());
  if(binding==null||!AxisVehicle.is(binding.vehicle()))return;
  move.accept(rider,seat.getX(),seat.blockPosition().getY()-.25,seat.getZ());ci.cancel();
 }
}
