package dev.fourthdimension.vehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;
/** Fixed support tile; Surf keeps its guide column on the parent. */
public final class StandingFrameBlock extends Block {
 public static final BooleanProperty SURF=BooleanProperty.create("surf");
 public StandingFrameBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(SURF,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(SURF);}
 @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var shape=Shapes.or(Block.box(3,4,-28,13,14,-20),Block.box(5,10,-24,11,14,4),Block.box(-14,10,0,30,14,4));for(double x:new double[]{-2.4,18.4})shape=Shapes.or(shape,Block.box(x-2,12,0,x+2,17,4),Block.box(x-.8,14,1.6,x+.8,s.getValue(SURF)?60:43,3.2));return shape;}
}
