package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.RotatingSeatIntegration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets="dev.plushivr.coasterfins.block.CoasterSeatBlockEntity",remap=false)
public abstract class RotatingSeatBlockEntityMixin implements RotatingSeatIntegration.Bridge {
  @Unique private BlockState fourthdimension$original;
  @Unique private Boolean fourthdimension$link;
  public BlockState fourthdimension$originalSeat(){return fourthdimension$original;}
  public void fourthdimension$originalSeat(BlockState state){fourthdimension$original=state;}
  public Boolean fourthdimension$linkState(){return fourthdimension$link;}
  public void fourthdimension$linkState(boolean value){fourthdimension$link=value;}
  @Inject(method="write",at=@At("TAIL"),require=0)
  private void fourthdimension$save(CompoundTag tag,HolderLookup.Provider registries,boolean packet,CallbackInfo ci) {
    if(fourthdimension$original!=null)tag.put("fourthdimension_original_seat",NbtUtils.writeBlockState(fourthdimension$original));
  }
  @Inject(method="read",at=@At("TAIL"),require=0)
  private void fourthdimension$load(CompoundTag tag,HolderLookup.Provider registries,boolean packet,CallbackInfo ci) {
    fourthdimension$original=tag.contains("fourthdimension_original_seat")
        ?NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),tag.getCompound("fourthdimension_original_seat")):null;
    fourthdimension$link=null;
  }
  @Redirect(method="tick",at=@At(value="INVOKE",target="Ldev/plushivr/coasterfins/block/CoasterSeatBlockEntity;setPowered(Z)V"),require=0)
  private void fourthdimension$link(@Coerce Object seat,boolean powered) {
    RotatingSeatIntegration.link((BlockEntity)seat,powered);
  }
}
