package dev.fourthdimension.station;

import com.mojang.serialization.MapCodec;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3d;

public final class BoardingStation extends BaseEntityBlock {
  public static final MapCodec<BoardingStation> CODEC = simpleCodec(BoardingStation::new);
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final BooleanProperty READY = BooleanProperty.create("ready"),
      LOCKED = BooleanProperty.create("locked");

  public BoardingStation(Properties properties) {
    super(properties);
    registerDefaultState(
        defaultBlockState()
            .setValue(FACING, Direction.NORTH)
            .setValue(READY, false)
            .setValue(LOCKED, false));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, READY, LOCKED);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BoardingStationEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return level.isClientSide
        ? null
        : createTickerHelper(type, StationContent.ENTITY.get(), BoardingStationEntity::tick);
  }

  @Override
  public boolean isSignalSource(BlockState state) {
    return true;
  }

  @Override
  public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
    return side == state.getValue(FACING).getOpposite()
        ? (state.getValue(READY) ? 15 : 0)
        : side == state.getValue(FACING).getClockWise().getOpposite()
            ? (state.getValue(LOCKED) ? 15 : 0)
            : 0;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!(player instanceof ServerPlayer server)
        || !(level.getBlockEntity(pos) instanceof BoardingStationEntity station))
      return InteractionResult.SUCCESS;
    if (!player.mayBuild() || !level.mayInteract(player, pos)) return InteractionResult.FAIL;
    var data = FoundationData.get(server.serverLevel());
    var v = station.vehicle == null ? null : data.vehicles.get(station.vehicle);
    if (v != null && v.family == Family.PAX) {
      station.vehicle = null;
      station.setChanged();
      v = null;
    }
    boolean rebinding = v == null || player.isShiftKeyDown();
    if (rebinding) {
      var container = SubLevelContainer.getContainer(level);
      double closest = 12 * 12;
      v = null;
      for (var candidate : data.vehicles.values())
        if (!FoundationData.VARIANT.equals(candidate.variant) && candidate.family != Family.PAX) {
          var body = container.getSubLevel(candidate.chassis);
          var point = body == null ? null : CoasterCartResolve.cartCenterWorld(body);
          if (point != null) {
            double distance =
                point.distanceSquared(
                    new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5));
            if (distance < closest) {
              closest = distance;
              v = candidate;
            }
          }
        }
      if (v == null) {
        player.displayClientMessage(
            Component.literal(
                "Place a non-PAX 4D car within twelve blocks. PAX requires its shuttle winch."),
            true);
        return InteractionResult.FAIL;
      }
    }
    if (rebinding && dev.fourthdimension.vehicle.DiveVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.DiveRuntime.rebindDock(server.serverLevel(),v,pos); }
      catch (IllegalArgumentException denied) { server.displayClientMessage(net.minecraft.network.chat.Component.literal(denied.getMessage()), true); return net.minecraft.world.InteractionResult.FAIL; }
    }
    if (rebinding && dev.fourthdimension.vehicle.PipelineVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.PipelineRuntime.rebindDock(server.serverLevel(),v,pos); }
      catch (IllegalArgumentException rejected) { player.displayClientMessage(Component.literal(rejected.getMessage()),true); return InteractionResult.FAIL; }
    } else if (rebinding && dev.fourthdimension.vehicle.PoweredInvertedYawVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.PoweredInvertedYawRuntime.rebindDock(server.serverLevel(),v,pos); }
      catch(IllegalArgumentException unavailable){server.displayClientMessage(Component.literal(unavailable.getMessage()),true);return InteractionResult.FAIL;}
    } else if (rebinding && dev.fourthdimension.vehicle.S2SpinningVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.S2SpinningRuntime.rebindDock(server.serverLevel(),v,pos); }
      catch (IllegalArgumentException rejected) { player.displayClientMessage(Component.literal(rejected.getMessage()),true); return InteractionResult.FAIL; }
    } else if (rebinding && dev.fourthdimension.vehicle.RollerBallVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.RollerBallRuntime.rebindDock(server.serverLevel(),v,pos); }
      catch (IllegalArgumentException rejected) { player.displayClientMessage(Component.literal(rejected.getMessage()),true); return InteractionResult.FAIL; }
    } else if (rebinding && dev.fourthdimension.vehicle.AxisVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.AxisRuntime.rebindDock(server.serverLevel(),v,pos); }
      catch (IllegalArgumentException rejected) { player.displayClientMessage(Component.literal(rejected.getMessage()),true); return InteractionResult.FAIL; }
    } else if (rebinding && dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.NewGenerationFlyingRuntime.rebindDock(server.serverLevel(),v,pos); }
      catch (IllegalArgumentException rejected) { player.displayClientMessage(Component.literal(rejected.getMessage()),true); return InteractionResult.FAIL; }
    } else if (rebinding && dev.fourthdimension.vehicle.FlyingVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.FlyingRuntime.rebindDock(server.serverLevel(),v,pos); }
      catch (IllegalArgumentException rejected) { player.displayClientMessage(Component.literal(rejected.getMessage()),true); return InteractionResult.FAIL; }
    } else if (rebinding && dev.fourthdimension.vehicle.FixedInvertedVehicle.is(v)) {
      try { dev.fourthdimension.vehicle.FixedInvertedRuntime.rebindDock(server.serverLevel(), v, pos); }
      catch (IllegalArgumentException rejected) { player.displayClientMessage(Component.literal(rejected.getMessage()), true); return InteractionResult.FAIL; }
    } else if (rebinding && dev.fourthdimension.vehicle.SuspendedVehicle.is(v)) {
      try {
        dev.fourthdimension.vehicle.SuspendedRuntime.rebindDock(server.serverLevel(), v, pos);
      } catch (IllegalArgumentException rejected) {
        player.displayClientMessage(Component.literal(rejected.getMessage()), true);
        return InteractionResult.FAIL;
      }
    } else if (dev.fourthdimension.vehicle.SpinningVehicle.is(v)) {
      v.dock = pos.immutable();
      data.setDirty();
    }
    if (rebinding) {
      station.vehicle = v.chassis;
      station.setChanged();
    }
    player.displayClientMessage(
        Component.literal(
            ((dev.fourthdimension.vehicle.RollerBallVehicle.is(v) || dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.is(v) || dev.fourthdimension.vehicle.SpinningVehicle.is(v) || dev.fourthdimension.vehicle.SuspendedVehicle.is(v) || dev.fourthdimension.vehicle.FixedInvertedVehicle.is(v) || dev.fourthdimension.vehicle.AxisVehicle.is(v) || dev.fourthdimension.vehicle.FlyingVehicle.is(v))
                ? "Bound dock: power requests capture; use Dispatch explicitly. Front: ready. Right side:"
                : "Rear redstone input: capture on power; dispatch on release. Front: ready. Right side:")
                + " restraints closed. "
                + station.fault),
        false);
    EditorNetwork.open(server, new EditorNetwork.Target(v.chassis, true));
    return InteractionResult.SUCCESS;
  }
}
