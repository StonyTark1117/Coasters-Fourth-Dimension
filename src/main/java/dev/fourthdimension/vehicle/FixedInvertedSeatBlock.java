package dev.fourthdimension.vehicle;

import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import org.joml.*;

/** Exact native row centers; the same local offset serves the mesh, mount and rider loading. */
public final class FixedInvertedSeatBlock extends CarrierSeatBlock implements BlockSubLevelCustomCenterOfMass {
  public static final net.minecraft.world.level.block.state.properties.BooleanProperty OUTER = net.minecraft.world.level.block.state.properties.BooleanProperty.create("outer");
  public FixedInvertedSeatBlock(Properties properties) { super(properties, DyeColor.PURPLE); registerDefaultState(defaultBlockState().setValue(OUTER,false)); }
  @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(OUTER);}
  @Override public Vector3dc getCenterOfMass(BlockGetter level, BlockState state) {
    return new Vector3d(0,.9375,.5);
  }
  @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return Block.box(-7,12,2,7,15,14);
  }
}
