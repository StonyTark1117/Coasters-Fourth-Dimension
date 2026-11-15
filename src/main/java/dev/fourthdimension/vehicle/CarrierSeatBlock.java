package dev.fourthdimension.vehicle;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Own seat/restraint geometry, with Create's passenger entity and Sable's native rider transform.
 */
public class CarrierSeatBlock extends SeatBlock {
  private static final boolean VALIDATION = Boolean.getBoolean("fourthdimension.validation");
  private static final Logger LOGGER = LogUtils.getLogger();
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final BooleanProperty RESTRAINED = BooleanProperty.create("restrained");

  public CarrierSeatBlock(Properties properties, DyeColor color) {
    super(properties, color);
    registerDefaultState(
        defaultBlockState().setValue(FACING, Direction.NORTH).setValue(RESTRAINED, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(FACING, RESTRAINED);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    return board(level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
  }

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (stack.getItem() instanceof dev.fourthdimension.editor.RotationTool) {
        if (VALIDATION)
          LOGGER.info(
              "FD_ROTATION_BLOCK_USE player={} pos={} hand={} result=SKIP_DEFAULT",
              player.getName().getString(), pos, hand);
      return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }
    return board(level, pos, player) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
  }

  private boolean board(Level level, BlockPos pos, Player player) {
    if (!Boarding.canBoard(level, pos) || SeatBlock.isSeatOccupied(level, pos)) return false;
    var binding = Boarding.at(level, pos);
    var body = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, pos);
    var offset = pos.subtract(body.getPlot().getCenterBlock());
    if (binding.carrier().riderSeats.entrySet().stream()
        .anyMatch(e -> e.getValue().equals(offset) && !e.getKey().equals(player.getUUID())))
      return false;
    if (!level.isClientSide) SeatBlock.sitDown(level, pos, player);
    return true;
  }

  @Override
  public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
    /* Boarding is explicit. */
  }
}
