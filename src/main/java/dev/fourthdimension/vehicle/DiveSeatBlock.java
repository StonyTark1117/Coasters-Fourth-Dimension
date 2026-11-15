package dev.fourthdimension.vehicle;
import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import org.joml.*;
/** Six hips at graph X=-2.5..2.5, Y=1.5. Fixed to the native chassis. */
public final class DiveSeatBlock extends CarrierSeatBlock implements BlockSubLevelCustomCenterOfMass {
  public DiveSeatBlock(Properties p){super(p,DyeColor.WHITE);}
  @Override public Vector3dc getCenterOfMass(BlockGetter l,BlockState s){return new Vector3d(0,.3125,.5);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
    return Shapes.or(Block.box(-7,8,2,7,11,14),Block.box(-7,11,13,7,29,16),Block.box(-6,4,-6,6,6,2));
  }
}
