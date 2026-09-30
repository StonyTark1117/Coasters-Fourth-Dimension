package dev.fourthdimension.vehicle;

import dev.fourthdimension.track.TrackDesign;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

public final class VehicleItem extends Item {
  public final TrackDesign design;

  public VehicleItem(Properties properties, TrackDesign design) {
    super(properties);
    this.design = design;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var player = context.getPlayer();
    if (player == null) return InteractionResult.PASS;
    if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
    var level = (ServerLevel) context.getLevel();
    try {
      if (!player.mayBuild()
          || !level.mayInteract(player, context.getClickedPos())
          || player.distanceToSqr(context.getClickLocation()) > 64)
        throw new IllegalArgumentException("Cannot place a vehicle here");
      var hit =
          CoasterPathTrackFrame.nearestGraphHit(
              level, CoasterPathGraphManager.get(level), context.getClickLocation(), 4);
      if (hit == null)
        throw new IllegalArgumentException("Click matching 4D track to place this car");
      VehicleFactory.create(level, design, hit);
      if (!player.isCreative()) context.getItemInHand().shrink(1);
      player.displayClientMessage(
          Component.literal("Carrier capture engaged. Wait for alignment before boarding."), true);
      return InteractionResult.SUCCESS;
    } catch (IllegalArgumentException | IllegalStateException failure) {
      player.displayClientMessage(Component.literal(failure.getMessage()), true);
      return InteractionResult.FAIL;
    }
  }
}
