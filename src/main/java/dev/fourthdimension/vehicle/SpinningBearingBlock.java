package dev.fourthdimension.vehicle;

import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Turntable below the cushions; its explicit COM avoids native clipping to the unit voxel. */
public final class SpinningBearingBlock extends Block implements BlockSubLevelCustomCenterOfMass {
  public SpinningBearingBlock(Properties properties) {super(properties);}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
    return Block.box(0,-8,0,16,-6,16);
  }
  @Override public Vector3dc getCenterOfMass(BlockGetter level,BlockState state) {
    return new Vector3d(.5,-.4375,.5);
  }
}
