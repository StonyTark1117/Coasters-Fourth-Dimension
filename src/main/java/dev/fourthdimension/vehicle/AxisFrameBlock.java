package dev.fourthdimension.vehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.*;
/** Outrigger forks lie beyond the passenger disk's longitudinal ends. */
public final class AxisFrameBlock extends Block {
  public static final IntegerProperty PART=IntegerProperty.create("part",0,11);
  public AxisFrameBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PART,0));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
    var at=AxisVehicle.FRAME.get(s.getValue(PART));
    if(Math.abs(at.getZ())==2)return box(0,9.4,at.getZ()>0?2:10,16,12.6,at.getZ()>0?6:14);
    return box(6,9.4,0,10,12.6,16);
  }
}
