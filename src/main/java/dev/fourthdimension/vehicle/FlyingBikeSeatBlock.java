package dev.fourthdimension.vehicle;

import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import org.joml.*;

/** Fixed chest/belly cradle; the single restraint also retains the calves. */
public final class FlyingBikeSeatBlock extends CarrierSeatBlock implements BlockSubLevelCustomCenterOfMass {
  public static final net.minecraft.world.level.block.state.properties.BooleanProperty LOAD_MARKER=net.minecraft.world.level.block.state.properties.BooleanProperty.create("load_marker");
  public FlyingBikeSeatBlock(Properties p){super(p,DyeColor.CYAN);registerDefaultState(defaultBlockState().setValue(LOAD_MARKER,false));}
  @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(LOAD_MARKER);}
  @Override public Vector3dc getCenterOfMass(BlockGetter l,BlockState s){return new Vector3d(.5,s.getValue(LOAD_MARKER)?-.35:.55,.9);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(2,5,-5,14,10,23);}
}
