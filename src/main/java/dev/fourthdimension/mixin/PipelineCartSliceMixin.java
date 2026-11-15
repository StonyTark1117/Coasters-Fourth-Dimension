package dev.fourthdimension.mixin;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.vehicle.PipelineVehicle;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.silvergold.simulatedcoasters.client.cart.CoasterCartClientFrameCache.RenderFrame;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** The original P center socket replaces the native N lower bogie mesh. Physics is unchanged. */
@Mixin(targets="dev.silvergold.simulatedcoasters.client.cart.CoasterCartBottomSliceRenderer",remap=false)
public abstract class PipelineCartSliceMixin {
  @Inject(method="renderCell",at=@At("HEAD"),cancellable=true)
  private static void fourthdimension$pipelineSlice(PoseStack pose,ModelBlockRenderer renderer,
      BufferSource buffers,BlockState state,float r,float g,float b,RandomSource random,Level level,
      RenderFrame frame,double x,double y,double z,BlockPos.MutableBlockPos cell,CallbackInfo ci){
    if(!state.is(dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks.COASTER_CART_BLOCK.get()))return;
    var body=dev.ryanhcode.sable.Sable.HELPER.getContaining(level,cell);
    var data=EligibilityHooks.data(level);var v=body==null||data==null?null:data.vehicles.get(body.getUniqueId());
    if(v!=null&&PipelineVehicle.is(v))ci.cancel();
  }
}
