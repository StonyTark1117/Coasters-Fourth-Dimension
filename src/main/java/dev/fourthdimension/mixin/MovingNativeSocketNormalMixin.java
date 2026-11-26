package dev.fourthdimension.mixin;
import dev.fourthdimension.moving.MovingNativeAdapter;
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=CoasterTrackPlacement.class,remap=false)
public abstract class MovingNativeSocketNormalMixin {
  @Inject(method="anchorFaceNormalForAxis",at=@At("HEAD"),cancellable=true,require=1)
  private static void fourthdimension$socketNormal(Level level,BlockPos anchor,Vec3 axis,CallbackInfoReturnable<Vec3> result){
    var socket=MovingNativeAdapter.socketFrame(level,anchor);if(socket!=null)result.setReturnValue(socket.normal());
  }
}
