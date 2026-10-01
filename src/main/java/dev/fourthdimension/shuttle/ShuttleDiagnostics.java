package dev.fourthdimension.shuttle;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.system.ticket.PhysicsChunkTicketManager;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;

/** Opt-in observations of native availability; never loads bodies or changes operating state. */
final class ShuttleDiagnostics {
  private static final Map<ShuttleWinchEntity, String> LAST = new WeakHashMap<>();

  private ShuttleDiagnostics() {}

  static void observe(ServerLevel level, ShuttleWinchEntity winch) {
    if (!Boolean.getBoolean("fourthdimension.shuttleValidation")) return;
    var container = SubLevelContainer.getContainer(level);
    String snapshot =
        "phase="
            + winch.phase
            + " interrupted="
            + winch.interrupted
            + " leadLoaded="
            + (container.getSubLevel(winch.vehicle) != null)
            + " carsLoaded="
            + winch.cars.stream().filter(id -> container.getSubLevel(id) != null).count()
            + " carriage="
            + winch.carriage
            + " carriageLoaded="
            + (winch.carriage != null && container.getSubLevel(winch.carriage) != null)
            + " winchAvailable="
            + PhysicsChunkTicketManager.isChunkLoadedEnough(
                level, winch.getBlockPos().getX() >> 4, winch.getBlockPos().getZ() >> 4)
            + " fault="
            + winch.fault;
    if (!snapshot.equals(LAST.put(winch, snapshot)))
      LogUtils.getLogger()
          .info(
              "FD_SHUTTLE_LIFECYCLE tick={} winch={} lead={} {}",
              level.getGameTime(),
              winch.getBlockPos(),
              winch.vehicle,
              snapshot);
  }
}
