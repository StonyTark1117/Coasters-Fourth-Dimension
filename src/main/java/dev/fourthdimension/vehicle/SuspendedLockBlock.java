package dev.fourthdimension.vehicle;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
public final class SuspendedLockBlock extends Block implements dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass {
  @Override public org.joml.Vector3dc getCenterOfMass(net.minecraft.world.level.BlockGetter level,BlockState state){return new org.joml.Vector3d(0,1.25,.5);}
  public static final BooleanProperty LOCKED=BooleanProperty.create("locked");
  public SuspendedLockBlock(Properties properties){super(properties);registerDefaultState(defaultBlockState().setValue(LOCKED,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LOCKED);}
  @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return Block.box(-2,18,0,2,22,16);}
}
