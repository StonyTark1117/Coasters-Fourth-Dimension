package dev.fourthdimension.vehicle;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Dye changes the original fixed-seat cushions without changing the seat contract. */
public final class WingSeatBlock extends CarrierSeatBlock {
  public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

  public WingSeatBlock(Properties properties) {
    super(properties, DyeColor.CYAN);
    registerDefaultState(defaultBlockState().setValue(COLOR, DyeColor.CYAN));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(COLOR);
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
      BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (!(stack.getItem() instanceof DyeItem dye))
      return super.useItemOn(stack, state, level, pos, player, hand, hit);
    if (level.isClientSide) return ItemInteractionResult.SUCCESS;
    var binding = Boarding.at(level, pos);
    if (binding == null || !WingVehicle.is(binding.vehicle()) || !player.mayBuild()
        || !Boarding.ready((ServerLevel) level, binding.vehicle())
        || binding.vehicle().restraints || !binding.carrier().occupants.isEmpty()) {
      player.displayClientMessage(Component.literal("Stop and empty the Wing, then open restraints to dye it"), true);
      return ItemInteractionResult.FAIL;
    }
    var body = SubLevelContainer.getContainer(level).getSubLevel(binding.vehicle().chassis);
    for (var offset : WingVehicle.SEATS) {
      var seat = body.getPlot().getCenterBlock().offset(offset);
      var old = level.getBlockState(seat);
      if (old.getBlock() == this) level.setBlock(seat, old.setValue(COLOR, dye.getDyeColor()), 3);
    }
    if (!player.isCreative()) stack.shrink(1);
    return ItemInteractionResult.SUCCESS;
  }
}
