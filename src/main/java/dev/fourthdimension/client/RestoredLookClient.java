package dev.fourthdimension.client;

import dev.fourthdimension.runtime.RestoredLookSync;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Wait for the matching native mount packet, restore once, then leave mouse look alone. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class RestoredLookClient {
  private record Pending(RestoredLookSync.Look look, ClientLevel level, int deadline) {}
  private static Pending pending;
  private RestoredLookClient() {}

  public static void receive(RestoredLookSync.Look look) {
    var mc = Minecraft.getInstance();
    if (mc.player == null || mc.level == null || !Float.isFinite(look.yaw())
        || !Float.isFinite(look.pitch()) || !mc.level.dimension().location().equals(look.dimension())) return;
    pending = new Pending(look, mc.level, mc.player.tickCount + 200);
  }

  @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
    if (pending == null) return;
    var mc = Minecraft.getInstance();
    var player = mc.player;
    if (player == null || mc.level != pending.level || player.tickCount > pending.deadline) {
      pending = null;
      return;
    }
    var look = pending.look;
    var seat = player.getVehicle();
    if (seat == null || !seat.blockPosition().equals(look.seat())) return;
    var body = Sable.HELPER.getContaining(mc.level, seat.blockPosition());
    if (body == null || !body.getUniqueId().equals(look.carrier())) return;
    player.setYRot(look.yaw());
    player.setXRot(look.pitch());
    player.yRotO = player.yHeadRot = player.yHeadRotO = player.yBodyRot = player.yBodyRotO = look.yaw();
    player.xRotO = look.pitch();
    pending = null;
    if (Boolean.getBoolean("fourthdimension.validation"))
      com.mojang.logging.LogUtils.getLogger().info(
          "FD_RESTORED_LOOK carrier={} seat={} yaw={} pitch={}", look.carrier(), look.seat(), look.yaw(), look.pitch());
  }
}
