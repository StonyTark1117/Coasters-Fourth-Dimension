package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.integration.RideTelemetry;
import dev.fourthdimension.integration.create.RideInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in received-snapshot evidence; never samples server state or changes display output. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class TelemetryClientDiagnostics {
  private static ClientLevel observedLevel;
  private static BlockPos observedPosition;
  private static RideTelemetry observedSnapshot;
  private static int ticks;

  private TelemetryClientDiagnostics() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.validation") || ++ticks % 10 != 0) return;
    var mc = Minecraft.getInstance();
    if (mc.level != observedLevel) {
      observedLevel = mc.level;
      observedPosition = null;
      observedSnapshot = null;
    }
    if (mc.level == null
        || !(mc.hitResult instanceof BlockHitResult hit)
        || !(mc.level.getBlockEntity(hit.getBlockPos()) instanceof RideInformation.Source source)) {
      observedPosition = null;
      observedSnapshot = null;
      return;
    }
    var snapshot = source.telemetry();
    if (hit.getBlockPos().equals(observedPosition) && snapshot.equals(observedSnapshot)) return;
    observedPosition = hit.getBlockPos().immutable();
    observedSnapshot = snapshot;
    LogUtils.getLogger()
        .info(
            "FD_CLIENT_TELEMETRY dimension={} position={} snapshot={}",
            mc.level.dimension().location(),
            observedPosition,
            snapshot.save());
  }
}
