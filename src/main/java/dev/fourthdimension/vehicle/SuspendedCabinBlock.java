package dev.fourthdimension.vehicle;
import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.*;
import org.joml.*;
public final class SuspendedCabinBlock extends Block implements BlockSubLevelCustomCenterOfMass {
  public SuspendedCabinBlock(Properties properties){super(properties);}
  @Override public Vector3dc getCenterOfMass(BlockGetter level,BlockState state){return new Vector3d(0,.5625,.5);}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext ctx){return Block.box(-16,8,-12,16,10,28);}
}
