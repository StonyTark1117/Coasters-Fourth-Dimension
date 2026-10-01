package dev.fourthdimension.mixin.optional;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.mixinterface.udp.ConnectionExtension;
import dev.ryanhcode.sable.network.udp.handler.SableUDPChannelHandlerClient;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

/** A queued UDP packet belongs to the connection/channel that received it, even after reconnect. */
@Mixin(value = SableUDPChannelHandlerClient.class, remap = false)
public abstract class SableClientQueueMixin {
  @Shadow @Final private Connection connection;
  @Shadow private Channel channel;

  @ModifyArg(
      method =
          "channelRead0(Lio/netty/channel/ChannelHandlerContext;Ldev/ryanhcode/sable/network/udp/AddressedSableUDPPacket;)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/ryanhcode/sable/network/client/SableClientNetworkEventLoop;tell(Ljava/lang/Runnable;)V"),
      index = 0)
  private Runnable fourthdimension$keepReceivingConnection(Runnable packet) {
    var owner = connection;
    var receivedOn = channel;
    return () -> {
      var listener = Minecraft.getInstance().getConnection();
      boolean currentConnection = listener != null && listener.getConnection() == owner;
      boolean connected = owner.isConnected();
      boolean currentChannel =
          receivedOn != null && ((ConnectionExtension) owner).sable$getUDPChannel() == receivedOn;
      boolean activeChannel = receivedOn != null && receivedOn.isActive();
      if (!currentConnection || !connected || !currentChannel || !activeChannel) {
        if (Boolean.getBoolean("fourthdimension.validation"))
          LogUtils.getLogger()
              .info(
                  "FD_UDP_DROP currentConnection={} connected={} currentChannel={}"
                      + " activeChannel={}",
                  currentConnection,
                  connected,
                  currentChannel,
                  activeChannel);
        return;
      }
      packet.run();
    };
  }
}
