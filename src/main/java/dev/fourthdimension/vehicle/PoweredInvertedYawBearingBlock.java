package dev.fourthdimension.vehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public final class PoweredInvertedYawBearingBlock extends Block {
  public PoweredInvertedYawBearingBlock(Properties p){super(p);}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Block.box(-14,4,0,14,10,16);}
}
