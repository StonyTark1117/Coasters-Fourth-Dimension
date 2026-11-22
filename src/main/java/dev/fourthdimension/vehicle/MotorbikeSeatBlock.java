package dev.fourthdimension.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** Narrow straddle saddle, reachable side step and rear hold-down; no back through rider. */
public final class MotorbikeSeatBlock extends CarrierSeatBlock implements dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass {
  public static final net.minecraft.world.level.block.state.properties.EnumProperty<DyeColor> COLOR=net.minecraft.world.level.block.state.properties.EnumProperty.create("color",DyeColor.class);
  public MotorbikeSeatBlock(Properties properties){super(properties,DyeColor.ORANGE);registerDefaultState(defaultBlockState().setValue(COLOR,DyeColor.ORANGE));}
  @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(COLOR);}
  @Override public org.joml.Vector3dc getCenterOfMass(BlockGetter l,BlockState s){return new org.joml.Vector3d(.5,.125,.5);}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext ctx){
    return Shapes.or(Block.box(6,4,2,10,7.8,14),Block.box(5,3,-4,11,12,3),
        Block.box(0,-3,-2,4,-1,4),Block.box(12,-3,-2,16,-1,4),
        state.getValue(RESTRAINED)?Block.box(4,10,11,12,13,14):Block.box(2,12,14,14,19,20));
  }
}
