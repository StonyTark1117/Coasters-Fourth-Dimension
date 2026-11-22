package dev.fourthdimension.vehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
/** Child-owned metal crossmember joins both Surf stance blocks into one native body. */
public final class StandingCarriageBlock extends Block {
 public StandingCarriageBlock(Properties p){super(p);}
 @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(-3.8,9,14,19.8,11,16);}
}
