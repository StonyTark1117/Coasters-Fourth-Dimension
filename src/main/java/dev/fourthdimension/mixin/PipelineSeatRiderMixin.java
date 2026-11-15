package dev.fourthdimension.mixin;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.vehicle.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SeatEntity.class)
public abstract class PipelineSeatRiderMixin {
  @Inject(method="positionRider",at=@At("HEAD"),cancellable=true)
  private void fourthdimension$pipelineHip(Entity rider,Entity.MoveFunction move,CallbackInfo ci){
    var seat=(SeatEntity)(Object)this;if(!(rider instanceof Player)||!seat.hasPassenger(rider))return;
    var b=Boarding.at(seat.level(),seat.blockPosition());if(b==null||!PipelineVehicle.is(b.vehicle()))return;
    var p=seat.blockPosition();var s=seat.level().getBlockState(p);
    // Player model hip is .75 above its feet. Plot block Y origin is graph Y-.1875.
    move.accept(rider,p.getX()+PipelineSeatBlock.hipX(s),p.getY()-.2625,p.getZ()+PipelineSeatBlock.hipZ(s));ci.cancel();
  }
}
