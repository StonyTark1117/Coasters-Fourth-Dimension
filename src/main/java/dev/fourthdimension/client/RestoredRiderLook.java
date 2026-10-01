package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.vehicle.RiderLookSync;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Applies one saved local look after the matching native mount has transformed world angles. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class RestoredRiderLook {
  private record Pending(
      RiderLookSync.Restore look, Player player, ClientPacketListener connection, int deadline) {}

  private static Pending pending;
  private static int ticks;

  private static void receive(RiderLookSync.Restore look, Player recipient) {
    var mc = Minecraft.getInstance();
    if (recipient != mc.player
        || mc.getConnection() == null
        || !Float.isFinite(look.yaw())
        || !Float.isFinite(look.pitch())
        || Math.abs(look.pitch()) > 90) return;
    pending = new Pending(look, recipient, mc.getConnection(), ticks + 200);
  }

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    ticks++;
    var p = pending;
    if (p == null) return;
    var mc = Minecraft.getInstance();
    if (mc.player != p.player
        || mc.getConnection() != p.connection
        || !p.connection.getConnection().isConnected()
        || ticks > p.deadline) {
      pending = null;
      return;
    }
    var seat = p.player.getVehicle();
    if (seat == null || !seat.getUUID().equals(p.look.seat())) return;
    var body = Sable.HELPER.getContaining(seat);
    if (body == null || !body.getUniqueId().equals(p.look.carrier())) return;
    // Sable's native mount hook has already run. Use the saved local angles directly,
    // rather than inverting a server pose with a different client interpolation pose.
    p.player.setYRot(p.look.yaw());
    p.player.setXRot(p.look.pitch());
    p.player.yRotO = p.look.yaw();
    p.player.xRotO = p.look.pitch();
    pending = null;
    if (Boolean.getBoolean("fourthdimension.validation"))
      LogUtils.getLogger()
          .info(
              "FD_RIDER_LOOK side=client player={} carrier={} seat={} yaw={} pitch={}",
              p.player.getUUID(),
              p.look.carrier(),
              p.look.seat(),
              p.look.yaw(),
              p.look.pitch());
  }

  @EventBusSubscriber(
      modid = "fourthdimension",
      value = Dist.CLIENT,
      bus = EventBusSubscriber.Bus.MOD)
  public static final class Registration {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> RiderLookSync.client = RestoredRiderLook::receive);
    }
  }
}
