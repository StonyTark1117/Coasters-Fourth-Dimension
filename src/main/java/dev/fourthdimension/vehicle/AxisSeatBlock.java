package dev.fourthdimension.vehicle;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
/** Cushion hip is at block center; the family attachment keeps the player mesh at that hip. */
public final class AxisSeatBlock extends CarrierSeatBlock {
  public AxisSeatBlock(Properties p){super(p,DyeColor.CYAN);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
    return box(1,5,1,15,8,15);
  }
}
