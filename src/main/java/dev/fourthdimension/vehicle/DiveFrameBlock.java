package dev.fourthdimension.vehicle;
import net.minecraft.world.level.block.Block;
/** Narrow crossbeam tiles keep the full row within the adopted 3.25 half-width. */
public final class DiveFrameBlock extends Block {
  public DiveFrameBlock(Properties p){super(p);}
  @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return Block.box(-8,12,3,8,16,13);}
}
