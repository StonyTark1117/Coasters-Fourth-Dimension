package dev.fourthdimension.mixin;
import dev.fourthdimension.moving.MovingNativeAdapter;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Exact fractional socket location; ordinary anchors keep the original endpoint behavior. */
@Mixin(value=CoasterAnchorpointBlockEntity.class,remap=false)
public abstract class MovingNativeSocketMixin {
  @Inject(method="worldCenterBase",at=@At("HEAD"),cancellable=true,require=1)
  private static void fourthdimension$socketPosition(Level level,BlockPos anchor,CallbackInfoReturnable<Vec3> result){
    var socket=MovingNativeAdapter.socketFrame(level,anchor);if(socket!=null)result.setReturnValue(socket.rawPosition());
  }
}
