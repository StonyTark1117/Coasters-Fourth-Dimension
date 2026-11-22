package dev.fourthdimension.tools;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.Boarding;
import dev.fourthdimension.vehicle.VehicleOperations;
import net.minecraft.server.level.ServerPlayer;

public final class BodyService {
  private BodyService() {}

  public static void apply(ServerPlayer player, FoundationData.Vehicle vehicle, String id) {
    var body = VehicleBody.require(vehicle, id);
    for (var car : VehicleOperations.consist(player.serverLevel(), vehicle))
      if (!Boarding.ready(player.serverLevel(), car)
          || car.carriers.stream().anyMatch(c -> !c.occupants.isEmpty() || !c.riderSeats.isEmpty()))
        throw new IllegalArgumentException("Select a body only on an empty, stopped, captured train");
    vehicle.bodyId = body.id;
    vehicle.revision++;
    FoundationData.get(player.serverLevel()).setDirty();
  }
}
