package dev.fourthdimension.vehicle;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
/** Four original rigid spar pieces joined to the running-gear crosshead. */
public final class FlyingBikeFrameBlock extends Block {
  public static final IntegerProperty PART=IntegerProperty.create("part",0,3);
  public FlyingBikeFrameBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PART,0));}
  /** Lower saddle collision; the narrow terminal bearing neck is intentional native running-gear contact. */
  public static net.minecraft.world.phys.shapes.VoxelShape controllerShape(){return Block.box(2,5,2,14,12,14);}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART);}
  @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return s.getValue(PART)<2 ? net.minecraft.world.phys.shapes.Shapes.or(Block.box(6,0,14,10,12,16), Block.box(s.getValue(PART)==0?6:-10,8,4,s.getValue(PART)==0?26:10,12,16)) : Block.box(6,0,14,10,16,16);}
}
