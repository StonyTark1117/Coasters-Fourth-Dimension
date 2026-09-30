package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.ShuttleRuntime;
import dev.fourthdimension.shuttle.ShuttleWinchEntity;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** Server-local operations from physically bound controllers. Never expose as unchecked packets. */
public final class RideOperations {
  public enum Request {
    CAPTURE,
    DISPATCH
  }

  private RideOperations() {}

  public static boolean nearby(ServerLevel level, BlockPos controller, UUID id) {
    var container = SubLevelContainer.getContainer(level);
    var body = container == null || id == null ? null : container.getSubLevel(id);
    var center = body == null ? null : CoasterCartResolve.cartCenterWorld(body);
    return center != null
        && center.distanceSquared(
                new Vector3d(
                    controller.getX() + .5, controller.getY() + .5, controller.getZ() + .5))
            < 64;
  }

  public static void request(ServerLevel level, BlockPos controller, UUID id, Request request) {
    if (!level.getServer().isSameThread())
      throw new IllegalStateException("Request ride operations on the server thread");
    if (!level.hasChunkAt(controller)) throw new IllegalArgumentException("Controller unavailable");
    var v = id == null ? null : FoundationData.get(level).vehicles.get(id);
    if (v == null) throw new IllegalArgumentException("No bound vehicle");
    var block = level.getBlockEntity(controller);
    if (block instanceof BoardingStationEntity station && id.equals(station.vehicle)) {
      if (v.family == Family.PAX)
        throw new IllegalArgumentException("Operate PAX through its bound shuttle winch");
      if (!nearby(level, controller, id))
        throw new IllegalArgumentException("Vehicle is outside station range");
    } else if (block instanceof ShuttleWinchEntity winch
        && id.equals(winch.vehicle)
        && ShuttleRuntime.owner(level, id) == winch) {
      // The winch validates its entire route and consist, including station position for dispatch.
    } else throw new IllegalArgumentException("Controller is not bound to this vehicle");
    switch (request) {
      case CAPTURE -> VehicleOperations.capture(level, v);
      case DISPATCH -> VehicleOperations.dispatch(level, v);
    }
  }
}
