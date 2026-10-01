package dev.fourthdimension.editor;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.Boarding;
import dev.ryanhcode.sable.Sable;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class RotationTool extends Item {
  public RotationTool(Properties properties) {
    super(properties);
  }

  @Override
  public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(
      net.minecraft.world.level.Level level,
      net.minecraft.world.entity.player.Player player,
      net.minecraft.world.InteractionHand hand) {
    var ridden = player.getVehicle();
    var binding = ridden == null ? null : Boarding.at(level, ridden.blockPosition());
    if (binding == null)
      return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
    if (player instanceof ServerPlayer server)
      EditorNetwork.open(server, new EditorNetwork.Target(binding.vehicle().chassis, true));
    return net.minecraft.world.InteractionResultHolder.sidedSuccess(
        player.getItemInHand(hand), level.isClientSide);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
    if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;
    var level = player.serverLevel();
    if (player.distanceToSqr(context.getClickLocation()) > 64) return InteractionResult.FAIL;
    var body = Sable.HELPER.getContaining(level, context.getClickedPos());
    var data = FoundationData.get(level);
    if (body != null) {
      if (player.isShiftKeyDown()
          && dev.fourthdimension.vehicle.RecoveryOperations.ordinary(level, body)) {
        try {
          dev.fourthdimension.vehicle.RecoveryOperations.select(player, body);
          player.displayClientMessage(
              Component.literal(
                  "Recovery bearing selected. Open the retained end car and choose Attach"
                      + " recovery."),
              false);
          return InteractionResult.SUCCESS;
        } catch (IllegalArgumentException failure) {
          player.displayClientMessage(Component.literal(failure.getMessage()), true);
          return InteractionResult.FAIL;
        }
      }
      var binding = Boarding.at(level, context.getClickedPos());
      var vehicle = binding == null ? data.vehicles.get(body.getUniqueId()) : binding.vehicle();
      if (vehicle != null) {
        EditorNetwork.open(player, new EditorNetwork.Target(vehicle.chassis, true));
        return InteractionResult.SUCCESS;
      }
    }
    var hit =
        CoasterPathTrackFrame.nearestGraphHit(
            level, CoasterPathGraphManager.get(level), context.getClickLocation(), 4);
    if (hit != null)
      for (var rail : data.rails)
        if (rail.original() && rail.matches(hit.edge().from(), hit.edge().to())) {
          EditorNetwork.open(player, new EditorNetwork.Target(rail.id, false));
          return InteractionResult.SUCCESS;
        }
    player.displayClientMessage(
        Component.literal("Use the rotation tool on a 4D section or car"), true);
    return InteractionResult.FAIL;
  }
}
