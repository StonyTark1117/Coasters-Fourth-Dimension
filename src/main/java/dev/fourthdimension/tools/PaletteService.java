package dev.fourthdimension.tools;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import net.minecraft.server.level.ServerPlayer;

public final class PaletteService {
  private PaletteService() {}
  public static void apply(ServerPlayer player,FoundationData.Vehicle vehicle,String id){
    var palette=VehiclePalette.require(id);
    for(var car:VehicleOperations.consist(player.serverLevel(),vehicle))
      if(!Boarding.ready(player.serverLevel(),car)||car.carriers.stream().anyMatch(c->!c.occupants.isEmpty()||!c.riderSeats.isEmpty()))
        throw new IllegalArgumentException("Select colors only on an empty, stopped, captured train");
    vehicle.palette=palette;vehicle.revision++;FoundationData.get(player.serverLevel()).setDirty();
  }
}
