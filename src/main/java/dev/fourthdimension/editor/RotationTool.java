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
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public final class RotationTool extends Item {
  private static final boolean VALIDATION = Boolean.getBoolean("fourthdimension.validation");
  private static final Logger LOGGER = LogUtils.getLogger();
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
    double distance = player.distanceToSqr(context.getClickLocation());
    if (VALIDATION)
      LOGGER.info(
          "FD_ROTATION_USE_ON player={} hand={} item={} clicked={} hit={} distanceSq={} shift={}",
          player.getGameProfile().getName(),
          context.getHand(),
          context.getItemInHand().getItem(),
          context.getClickedPos(),
          context.getClickLocation(),
          distance,
          player.isShiftKeyDown());
    if (distance > 64) {
      if (VALIDATION) LOGGER.info("FD_ROTATION_GUARD player={} guard=reach result=FAIL", player.getName().getString());
      return InteractionResult.FAIL;
    }
    if(level.getBlockEntity(context.getClickedPos()) instanceof dev.fourthdimension.moving.MovingControllerEntity){
      dev.fourthdimension.moving.MovingNetwork.open(player,context.getClickedPos());return InteractionResult.SUCCESS;
    }
    var body = Sable.HELPER.getContaining(level, context.getClickedPos());
    var data = FoundationData.get(level);
    if (VALIDATION)
      LOGGER.info(
          "FD_ROTATION_RESOLVE player={} body={} dataVehicles={} dataRails={}",
          player.getGameProfile().getName(),
          body == null ? null : body.getUniqueId(),
          data.vehicles.size(),
          data.rails.size());
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
      if(vehicle==null)vehicle=data.vehicles.values().stream()
          .filter(v->dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.is(v)&&body.getUniqueId().equals(v.yaw.deck))
          .findFirst().orElse(null);
      if (VALIDATION)
        LOGGER.info(
            "FD_ROTATION_BIND player={} binding={} vehicle={} chassis={}",
            player.getGameProfile().getName(),
            binding == null ? null : binding.carrier().body,
            vehicle == null ? null : vehicle.variant,
            vehicle == null ? null : vehicle.chassis);
      if (vehicle != null) {
        if (VALIDATION)
          LOGGER.info("FD_ROTATION_DISPATCH player={} target={} vehicle=true", player.getGameProfile().getName(), vehicle.chassis);
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
          if (VALIDATION)
            LOGGER.info("FD_ROTATION_DISPATCH player={} target={} vehicle=false", player.getGameProfile().getName(), rail.id);
          EditorNetwork.open(player, new EditorNetwork.Target(rail.id, false));
          return InteractionResult.SUCCESS;
        }
    player.displayClientMessage(
        Component.literal("Use the rotation tool on a 4D section or car"), true);
    if (VALIDATION) LOGGER.info("FD_ROTATION_GUARD player={} guard=target result=FAIL", player.getName().getString());
    return InteractionResult.FAIL;
  }
}
