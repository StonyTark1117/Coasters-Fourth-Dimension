package dev.fourthdimension.vehicle;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
public final class SuspendedHangerBlock extends Block {
 public static final BooleanProperty FRONT=BooleanProperty.create("front");
 public SuspendedHangerBlock(Properties properties){super(properties);registerDefaultState(defaultBlockState().setValue(FRONT,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FRONT);}
 @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
  return s.getValue(FRONT)?Block.box(7,10,-2,9,32,0):Block.box(7,10,16,9,32,18);
 }
}
