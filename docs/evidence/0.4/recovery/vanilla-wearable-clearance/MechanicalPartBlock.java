package dev.fourthdimension.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

public final class MechanicalPartBlock extends Block {
  private final VoxelShape shape;

  public MechanicalPartBlock(Properties properties, boolean axle) {
    this(properties, axle ? Block.box(0, 6, -7, 16, 10, -4) : Block.box(0, 4, 4, 16, 12, 12));
  }

  public MechanicalPartBlock(Properties properties, VoxelShape shape) {
    super(properties);
    this.shape = shape;
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return shape;
  }
}
