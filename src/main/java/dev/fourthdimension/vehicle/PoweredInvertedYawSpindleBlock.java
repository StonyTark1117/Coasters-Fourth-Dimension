package dev.fourthdimension.vehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public final class PoweredInvertedYawSpindleBlock extends Block {
  public PoweredInvertedYawSpindleBlock(Properties p){super(p);}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Block.box(7,0,7,9,16,9);}
}
