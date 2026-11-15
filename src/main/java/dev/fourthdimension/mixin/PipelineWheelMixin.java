package dev.fourthdimension.mixin;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.vehicle.PipelineVehicle;
import dev.ryanhcode.sable.sublevel.SubLevel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** P owns its wheels; native N wheels remain unchanged for every other family. */
@Mixin(targets="dev.silvergold.simulatedcoasters.client.cart.CoasterCartWheelAxisRenderer",remap=false)
public abstract class PipelineWheelMixin {
  @Inject(method="renderCell",at=@At("HEAD"),cancellable=true)
  private static void fourthdimension$pipelineWheels(PoseStack pose,ModelBlockRenderer renderer,
      MultiBufferSource buffers,BlockState state,float r,float g,float b,RandomSource random,
      Level level,SubLevel body,@Coerce Object wheelState,BlockPos.MutableBlockPos cell,CallbackInfo ci){
    var data=EligibilityHooks.data(level);var v=data==null?null:data.vehicles.get(body.getUniqueId());
    if(v!=null&&PipelineVehicle.is(v))ci.cancel();
  }
}
