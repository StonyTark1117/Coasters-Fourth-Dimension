package dev.fourthdimension.vehicle;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
public final class FlyingLockBlock extends Block {
  public static final BooleanProperty LOCKED=BooleanProperty.create("locked");
  public FlyingLockBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(LOCKED,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(LOCKED);}
  @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,
      net.minecraft.core.BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return Block.box(-8,20,16,8,24,18);}
}
