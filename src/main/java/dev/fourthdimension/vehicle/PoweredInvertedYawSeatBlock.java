package dev.fourthdimension.vehicle;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
/** Upright seats use the accepted ordinary native seat/rider transform. */
public final class PoweredInvertedYawSeatBlock extends CarrierSeatBlock {
  public PoweredInvertedYawSeatBlock(Properties properties){super(properties,DyeColor.CYAN);}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Block.box(2,2,2,14,10,14);}
}
