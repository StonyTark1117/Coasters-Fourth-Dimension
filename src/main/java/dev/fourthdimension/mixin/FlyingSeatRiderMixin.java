package dev.fourthdimension.mixin;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.vehicle.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A declared hip attachment, using the same native MoveFunction as Create's normal seat. */
@Mixin(SeatEntity.class)
public abstract class FlyingSeatRiderMixin {
  @Inject(method="positionRider",at=@At("HEAD"),cancellable=true)
  private void fourthdimension$flyingHip(Entity rider,Entity.MoveFunction move,CallbackInfo ci){
    var seat=(SeatEntity)(Object)this;
    if(!(rider instanceof Player)||!seat.hasPassenger(rider))return;
    var b=Boarding.at(seat.level(),seat.blockPosition());if(b==null||!FlyingVehicle.is(b.vehicle()))return;
    // Vanilla player model hips are .75 above its feet; this places them at the authored .5 hip.
    move.accept(rider,seat.getX(),seat.blockPosition().getY()-.25,seat.getZ());ci.cancel();
  }
}
