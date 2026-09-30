package dev.fourthdimension.station;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.VehicleOperations;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BoardingStationEntity extends BlockEntity {
  public UUID vehicle;
  public String fault = "";
  private boolean powered;

  public BoardingStationEntity(BlockPos pos, BlockState state) {
    super(StationContent.ENTITY.get(), pos, state);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
    super.saveAdditional(tag, provider);
    if (vehicle != null) tag.putUUID("vehicle", vehicle);
    tag.putBoolean("powered", powered);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
    super.loadAdditional(tag, provider);
    vehicle = tag.hasUUID("vehicle") ? tag.getUUID("vehicle") : null;
    powered = tag.getBoolean("powered");
  }

  public static void tick(
      Level raw, BlockPos pos, BlockState state, BoardingStationEntity station) {
    if (!(raw instanceof ServerLevel level) || level.getGameTime() % 5 != 0) return;
    var v =
        station.vehicle == null ? null : FoundationData.get(level).vehicles.get(station.vehicle);
    var rear = state.getValue(BoardingStation.FACING).getOpposite();
    boolean signal = level.getSignal(pos.relative(rear), rear) > 0;
    boolean nearby = false;
    if (v != null) {
      var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level);
      var body = container == null ? null : container.getSubLevel(v.chassis);
      var center =
          body == null
              ? null
              : dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(
                  body);
      nearby =
          center != null
              && center.distanceSquared(
                      new org.joml.Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5))
                  < 8 * 8;
    }
    if (v != null
        && v.family != dev.fourthdimension.contract.Family.PAX
        && ((signal && nearby && !v.stopping) || (!signal && station.powered && nearby))) {
      try {
        if (signal) VehicleOperations.capture(level, v);
        else VehicleOperations.dispatch(level, v);
        station.fault = "";
      } catch (IllegalArgumentException failure) {
        station.fault = failure.getMessage();
      }
    }
    station.powered = signal;
    station.setChanged();
    boolean ready = nearby && v != null && v.boardingReady,
        locked = nearby && v != null && v.restraints;
    if (state.getValue(BoardingStation.READY) != ready
        || state.getValue(BoardingStation.LOCKED) != locked)
      level.setBlock(
          pos,
          state.setValue(BoardingStation.READY, ready).setValue(BoardingStation.LOCKED, locked),
          3);
  }
}
