package dev.fourthdimension.vehicle;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.FourthDimension;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Saved mouse angles are local; restoring them must not depend on a later UDP carrier pose. */
public final class RiderLookSync {
  private RiderLookSync() {}

  public record Restore(UUID carrier, UUID seat, float yaw, float pitch)
      implements CustomPacketPayload {
    public static final Type<Restore> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, "rider_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Restore> CODEC =
        StreamCodec.of(
            (b, p) -> {
              b.writeUUID(p.carrier);
              b.writeUUID(p.seat);
              b.writeFloat(p.yaw);
              b.writeFloat(p.pitch);
            },
            b -> new Restore(b.readUUID(), b.readUUID(), b.readFloat(), b.readFloat()));

    @Override
    public Type<Restore> type() {
      return TYPE;
    }
  }

  // Installed by client setup; keep dedicated-server class loading free of client types.
  public static BiConsumer<Restore, Player> client = (payload, player) -> {};

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("5")
        .playToClient(
            Restore.TYPE,
            Restore.CODEC,
            (p, c) -> c.enqueueWork(() -> client.accept(p, c.player())));
  }

  public static void send(ServerPlayer player, UUID carrier, float yaw, float pitch) {
    var seat = player.getVehicle();
    if (seat == null) return;
    var payload = new Restore(carrier, seat.getUUID(), yaw, pitch);
    PacketDistributor.sendToPlayer(player, payload);
    if (Boolean.getBoolean("fourthdimension.validation"))
      LogUtils.getLogger()
          .info(
              "FD_RIDER_LOOK side=server player={} carrier={} seat={} yaw={} pitch={}",
              player.getUUID(),
              carrier,
              seat.getUUID(),
              yaw,
              pitch);
  }
}
