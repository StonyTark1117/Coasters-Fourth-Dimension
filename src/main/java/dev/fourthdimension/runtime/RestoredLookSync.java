package dev.fourthdimension.runtime;

import dev.fourthdimension.FourthDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.UUID;

/** A saved occupied look is already seat-local; ordinary native mounts are not. */
public final class RestoredLookSync {
  private RestoredLookSync() {}

  public record Look(ResourceLocation dimension, UUID carrier, BlockPos seat,
      float yaw, float pitch) implements CustomPacketPayload {
    public static final Type<Look> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, "restored_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Look> CODEC = StreamCodec.of(
        (buf, value) -> {
          buf.writeResourceLocation(value.dimension);
          buf.writeUUID(value.carrier);
          buf.writeBlockPos(value.seat);
          buf.writeFloat(value.yaw);
          buf.writeFloat(value.pitch);
        }, buf -> new Look(buf.readResourceLocation(), buf.readUUID(), buf.readBlockPos(),
            buf.readFloat(), buf.readFloat()));
    @Override public Type<Look> type() { return TYPE; }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event.registrar("7").playToClient(Look.TYPE, Look.CODEC,
        (payload, context) -> context.enqueueWork(
            () -> dev.fourthdimension.client.RestoredLookClient.receive(payload)));
  }
}
