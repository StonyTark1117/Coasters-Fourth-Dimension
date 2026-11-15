package dev.fourthdimension.vehicle;

import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import org.joml.*;

/** Two-block overlapping rods centered on the same transverse pivot as both pods. */
public final class RollerBallAxleBlock extends Block implements BlockSubLevelCustomCenterOfMass {
  public RollerBallAxleBlock(Properties properties){super(properties);}
  @Override public Vector3dc getCenterOfMass(BlockGetter level,BlockState state){return new Vector3d(1,.5,.5);}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return box(0,6,6,32,10,10);}
}
