package dev.fourthdimension.vehicle;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
/** Low fixed cabin floor, clear of the side rails and four occupied bounds. */
public final class PipelineFrameBlock extends Block {
  public static final IntegerProperty PART=IntegerProperty.create("part",0,29);
  public PipelineFrameBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PART,0));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(PART);}
  @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){var part=PipelineVehicle.FRAME.get(s.getValue(PART));
    return part.getY()==-3?Block.box(0,12,0,16,15,16):part.getY()==0?Block.box(0,9,6,16,14,10):Block.box(6,0,6,10,16,10);}
}
