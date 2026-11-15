package dev.fourthdimension.vehicle;

import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.core.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Fractional seat/torso centers retain Create's native seat entity and Sable rider ownership. */
public final class SpinningSeatBlock extends CarrierSeatBlock implements BlockSubLevelCustomCenterOfMass {
  public static final BooleanProperty OUTER = BooleanProperty.create("outer");
  public SpinningSeatBlock(Properties properties) {
    super(properties, DyeColor.ORANGE);
    registerDefaultState(defaultBlockState().setValue(OUTER, false));
  }
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
    super.createBlockStateDefinition(b); b.add(OUTER);
  }
  @Override public Vector3dc getCenterOfMass(BlockGetter level, BlockState state) {
    return new Vector3d(state.getValue(OUTER) ? .6 : .4, .5,
        state.getValue(FACING) == Direction.SOUTH ? .75 : .25);
  }
  @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,
      BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
    var center=getCenterOfMass(level,state);
    return Block.box((center.x()-.375)*16,3,(center.z()-.375)*16,
        (center.x()+.375)*16,12,(center.z()+.375)*16);
  }
  public static BlockState state(BlockPos seat) {
    return VehicleParts.SPINNING_SEAT.get().defaultBlockState()
        .setValue(OUTER, seat.getX() >= 0)
        .setValue(FACING, seat.getZ() >= 0 ? Direction.SOUTH : Direction.NORTH);
  }
}
