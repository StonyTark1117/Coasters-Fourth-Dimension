package dev.fourthdimension.station;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.integration.create.RideInformation;
import dev.fourthdimension.runtime.FoundationData;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BoardingStationEntity extends BlockEntity
    implements IHaveGoggleInformation, RideInformation.Source {
  public UUID vehicle;
  public String fault = "";
  private boolean powered;
  private RideTelemetry telemetry = RideTelemetry.unavailable("Waiting for station state");

  @Override
  public RideTelemetry telemetry() {
    return telemetry;
  }

  @Override
  public boolean addToGoggleTooltip(
      java.util.List<net.minecraft.network.chat.Component> tooltip, boolean sneaking) {
    return RideInformation.goggles(tooltip, telemetry);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    var tag = saveWithoutMetadata(registries);
    tag.put("ride_telemetry", telemetry.save());
    return tag;
  }

  @Override
  public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
    return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
  }

  private void updateTelemetry(ServerLevel server) {
    var next = RideTelemetry.atController(server, worldPosition, vehicle);
    if (!next.equals(telemetry)) {
      telemetry = next;
      server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }
  }

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
    if (tag.contains("ride_telemetry"))
      telemetry = RideTelemetry.load(tag.getCompound("ride_telemetry"));
  }

  public static void tick(
      Level raw, BlockPos pos, BlockState state, BoardingStationEntity station) {
    if (!(raw instanceof ServerLevel level) || level.getGameTime() % 5 != 0) return;
    var v =
        station.vehicle == null ? null : FoundationData.get(level).vehicles.get(station.vehicle);
    var rear = state.getValue(BoardingStation.FACING).getOpposite();
    boolean signal = level.getSignal(pos.relative(rear), rear) > 0;
    boolean nearby = RideOperations.nearby(level, pos, station.vehicle);
    if (v != null
        && v.family != dev.fourthdimension.contract.Family.PAX
        && ((signal && nearby && !v.stopping) || (!signal && station.powered && nearby
            && !dev.fourthdimension.vehicle.SpinningVehicle.is(v)
            && !dev.fourthdimension.vehicle.SuspendedVehicle.is(v)
            && !dev.fourthdimension.vehicle.AxisVehicle.is(v)
            && !dev.fourthdimension.vehicle.FlyingVehicle.is(v)
            && !dev.fourthdimension.vehicle.FixedInvertedVehicle.is(v)))) {
      try {
        RideOperations.request(
            level,
            pos,
            v.chassis,
            signal ? RideOperations.Request.CAPTURE : RideOperations.Request.DISPATCH);
        station.fault = "";
      } catch (IllegalArgumentException failure) {
        station.fault = failure.getMessage();
      }
    }
    if (station.powered != signal) {
      station.powered = signal;
      station.setChanged();
    }
    station.updateTelemetry(level);
    boolean ready = nearby && station.telemetry.boardingReady(),
        locked = nearby && station.telemetry.restraintsClosed();
    if (state.getValue(BoardingStation.READY) != ready
        || state.getValue(BoardingStation.LOCKED) != locked)
      level.setBlock(
          pos,
          state.setValue(BoardingStation.READY, ready).setValue(BoardingStation.LOCKED, locked),
          3);
  }
}
