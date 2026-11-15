package dev.fourthdimension.vehicle;

import dev.fourthdimension.track.TrackDesign;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class VehicleItem extends Item {
  public final TrackDesign design;

  public VehicleItem(Properties properties, TrackDesign design) {
    super(properties);
    this.design = design;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level raw, Player player, InteractionHand hand) {
    var stack = player.getItemInHand(hand);
    if (raw.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
    var level = (ServerLevel) raw;
    try {
      if (!player.mayBuild()) throw new IllegalArgumentException("Cannot place a vehicle here");
      var hit = aimedTrack(level, player);
      if (hit == null)
        throw new IllegalArgumentException(design == null ? "Aim at ordinary Coasters track" : "Aim at matching 4D track to place this car");
      if (!level.mayInteract(player, BlockPos.containing(hit.point())))
        throw new IllegalArgumentException("Cannot place a vehicle here");
      place(level, player, stack, hit);
      return InteractionResultHolder.success(stack);
    } catch (IllegalArgumentException | IllegalStateException failure) {
      if (Boolean.getBoolean("fourthdimension.validation"))
        System.out.println("FD_VEHICLE_AIR_PLACEMENT rejected=" + failure.getMessage());
      player.displayClientMessage(Component.literal(failure.getMessage()), true);
      return InteractionResultHolder.fail(stack);
    }
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
        throw new IllegalArgumentException(design == null ? "Click ordinary Coasters track" : "Click matching 4D track to place this car");
      place(level, player, context.getItemInHand(), hit);
      return InteractionResult.SUCCESS;
    } catch (IllegalArgumentException | IllegalStateException failure) {
      player.displayClientMessage(Component.literal(failure.getMessage()), true);
      return InteractionResult.FAIL;
    }
  }

  private void place(
      ServerLevel level,
      Player player,
      ItemStack stack,
      CoasterPathTrackFrame.GraphHit hit) {
    if (design == null) WingVehicle.create(level, hit);
    else VehicleFactory.create(level, design, hit);
    if (!player.isCreative()) stack.shrink(1);
    player.displayClientMessage(
        Component.literal(design == null ? "Wing stopped. Board, close restraints, then dispatch."
            : "Carrier capture engaged. Wait for alignment before boarding."), true);
  }

  /** The rail is a rendered curve, so an interior placement need not click a support block. */
  private static CoasterPathTrackFrame.GraphHit aimedTrack(ServerLevel level, Player player) {
    Vec3 eye = player.getEyePosition();
    Vec3 direction = player.getViewVector(1);
    var graph = CoasterPathGraphManager.get(level);
    for (double distance = .5; distance <= 6; distance += .25) {
      Vec3 point = eye.add(direction.scale(distance));
      var hit = CoasterPathTrackFrame.nearestGraphHit(level, graph, point, .6);
      if (hit != null && hit.point().distanceToSqr(point) <= .45 * .45) return hit;
    }
    return null;
  }
}
