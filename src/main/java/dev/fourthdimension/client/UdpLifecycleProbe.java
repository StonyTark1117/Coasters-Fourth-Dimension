package dev.fourthdimension.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.SableClient;
import dev.ryanhcode.sable.mixinterface.udp.ConnectionExtension;
import dev.ryanhcode.sable.network.packets.udp.SableUDPClientboundKeepAlivePacket;
import dev.ryanhcode.sable.network.udp.*;
import dev.ryanhcode.sable.network.udp.handler.SableUDPChannelHandlerClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Explicitly opted-in native queue probe; deliberately disconnects the test client once. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class UdpLifecycleProbe {
  private static int stage, stableTicks;
  private static Connection oldConnection;
  private static Channel oldChannel;
  private static ChannelHandlerContext oldContext;
  private static SableUDPChannelHandlerClient oldHandler;
  private static final JsonObject report = new JsonObject();

  private UdpLifecycleProbe() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.validation")
        || !Boolean.getBoolean("fourthdimension.validation.udp")
        || stage >= 2) return;
    var mc = Minecraft.getInstance();
    var listener = mc.getConnection();
    if (mc.level == null || mc.player == null || listener == null) {
      stableTicks = 0;
      return;
    }
    var connection = listener.getConnection();
    var channel = ((ConnectionExtension) connection).sable$getUDPChannel();
    if (!connection.isConnected()
        || channel == null
        || !channel.isActive()
        || (stage == 1 && connection == oldConnection)) {
      stableTicks = 0;
      return;
    }
    if (++stableTicks < 80) return;
    try {
      var context = channel.pipeline().context(SableUDPChannelHandlerClient.class);
      if (context == null) throw new IllegalStateException("Native Sable handler is missing");
      var handler = (SableUDPChannelHandlerClient) context.handler();
      report.addProperty("player", mc.player.getName().getString());
      if (stage == 0) {
        oldConnection = connection;
        oldChannel = channel;
        oldContext = context;
        oldHandler = handler;
        report.addProperty("oldChannel", channel.id().asLongText());
        var active = enqueue(handler, context);
        SableClient.NETWORK_EVENT_LOOP.runAllTasks();
        report.addProperty("initialActiveCallbacks", active.get());
        if (active.get() != 1)
          throw new IllegalStateException("Live native queue failed positive control");
        var closed = enqueue(handler, context);
        LogUtils.getLogger().info("FD_UDP_PROBE phase=CLOSED_BEGIN");
        connection.disconnect(Component.literal("Fourth Dimension UDP lifecycle validation"));
        report.addProperty(
            "closedChannelMissing",
            ((ConnectionExtension) connection).sable$getUDPChannel() == null);
        report.addProperty("closedConnection", !connection.isConnected());
        SableClient.NETWORK_EVENT_LOOP.runAllTasks();
        report.addProperty("closedCallbacks", closed.get());
        report.addProperty("phase", "awaiting_reconnect");
        stage = 1;
        stableTicks = 0;
      } else {
        LogUtils.getLogger().info("FD_UDP_PROBE phase=REPLACED_BEGIN");
        report.addProperty("newChannel", channel.id().asLongText());
        report.addProperty("newConnection", connection != oldConnection);
        report.addProperty("oldChannelInactive", !oldChannel.isActive());
        var stale = enqueue(oldHandler, oldContext);
        var active = enqueue(handler, context);
        SableClient.NETWORK_EVENT_LOOP.runAllTasks();
        report.addProperty("replacedCallbacks", stale.get());
        report.addProperty("reconnectedActiveCallbacks", active.get());
        if (active.get() != 1)
          throw new IllegalStateException("Reconnected native queue failed positive control");
        report.addProperty("phase", "complete");
        stage = 2;
        oldConnection = null;
        oldChannel = null;
        oldContext = null;
        oldHandler = null;
      }
      save(mc);
    } catch (Exception failure) {
      report.addProperty("phase", "fixture_failed");
      report.addProperty("error", failure.toString());
      stage = 2;
      LogUtils.getLogger().error("FD_UDP_PROBE fixture failed", failure);
      try {
        save(mc);
      } catch (Exception writeFailure) {
        LogUtils.getLogger().error("FD_UDP_PROBE report failed", writeFailure);
      }
    }
  }

  private static AtomicInteger enqueue(
      SableUDPChannelHandlerClient handler, ChannelHandlerContext context) throws Exception {
    var calls = new AtomicInteger();
    // Enter the actual inherited Netty inbound method, retaining this handler's connection
    // identity.
    handler.channelRead(
        context,
        new AddressedSableUDPPacket(
            new SableUDPPacket() {
              public SableUDPPacketType getType() {
                return SableUDPPacketType.KEEP_ALIVE_CLIENTBOUND;
              }

              public void handleClient(Level level) {
                calls.incrementAndGet();
              }
            },
            null));
    handler.channelRead(
        context, new AddressedSableUDPPacket(new SableUDPClientboundKeepAlivePacket(), null));
    return calls;
  }

  private static void save(Minecraft mc) throws Exception {
    Files.writeString(
        mc.gameDirectory.toPath().resolve("fd-udp-lifecycle.json"),
        new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n");
    LogUtils.getLogger().info("FD_UDP_PROBE {}", report);
  }
}
