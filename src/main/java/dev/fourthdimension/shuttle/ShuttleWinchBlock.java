package dev.fourthdimension.shuttle;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3d;

public final class ShuttleWinchBlock extends RotatedPillarKineticBlock
    implements IBE<ShuttleWinchEntity> {
  public ShuttleWinchBlock(Properties properties) {
    super(properties);
  }

  @Override
  public Direction.Axis getRotationAxis(BlockState state) {
    return state.getValue(AXIS);
  }

  @Override
  public boolean hasShaftTowards(
      LevelReader level, BlockPos pos, BlockState state, Direction face) {
    return face.getAxis() == getRotationAxis(state);
  }

  @Override
  public Class<ShuttleWinchEntity> getBlockEntityClass() {
    return ShuttleWinchEntity.class;
  }

  @Override
  public BlockEntityType<? extends ShuttleWinchEntity> getBlockEntityType() {
    return ShuttleContent.ENTITY.get();
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level raw, BlockPos pos, Player player, BlockHitResult hit) {
    if (!(raw instanceof ServerLevel level)) return InteractionResult.SUCCESS;
    if (!player.mayBuild() || !level.mayInteract(player, pos)) return InteractionResult.FAIL;
    var winch = getBlockEntity(level, pos);
    try {
      if (player.isShiftKeyDown() && winch.phase == ShuttleWinchEntity.Phase.FAULT) winch.recover();
      else if (winch.vehicle == null || player.isShiftKeyDown()) {
        var container = SubLevelContainer.getContainer(level);
        FoundationData.Vehicle closest = null;
        double distance = 12 * 12;
        for (var v : FoundationData.get(level).vehicles.values())
          if (v.family == Family.PAX) {
            var body = container.getSubLevel(v.chassis);
            var center = body == null ? null : CoasterCartResolve.cartCenterWorld(body);
            double d =
                center == null
                    ? Double.POSITIVE_INFINITY
                    : center.distanceSquared(
                        new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5));
            if (d < distance) {
              closest = v;
              distance = d;
            }
          }
        if (closest == null)
          throw new IllegalArgumentException("Place a stopped PAX train within twelve blocks");
        winch.bind(closest);
      } else if (winch.phase == ShuttleWinchEntity.Phase.FAULT) winch.resume();
      else if (winch.phase == ShuttleWinchEntity.Phase.BOARDING) winch.start();
      player.displayClientMessage(
          Component.literal(
              "Shuttle: "
                  + winch.phase
                  + ". "
                  + winch.fault
                  + " Close restraints before starting; supply 16..128 RPM. Sneak-use binds the"
                  + " lift end car, or recovers an interrupted train."),
          false);
      return InteractionResult.SUCCESS;
    } catch (IllegalArgumentException failure) {
      player.displayClientMessage(Component.literal(failure.getMessage()), true);
      return InteractionResult.FAIL;
    }
  }
}
