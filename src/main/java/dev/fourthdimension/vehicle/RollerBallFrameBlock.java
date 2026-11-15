package dev.fourthdimension.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;

/** The fixed axle support shows the actual physical latch and added passive damping. */
public final class RollerBallFrameBlock extends Block {
  public static final BooleanProperty LATCHED=BooleanProperty.create("latched");
  public static final BooleanProperty DAMPING=BooleanProperty.create("damping");
  public RollerBallFrameBlock(Properties properties){
    super(properties);registerDefaultState(defaultBlockState().setValue(LATCHED,false).setValue(DAMPING,false));
  }
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(LATCHED,DAMPING);}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
    return Shapes.or(box(4,0,3,12,6,13),box(3,1,5,13,5,11));
  }
}
