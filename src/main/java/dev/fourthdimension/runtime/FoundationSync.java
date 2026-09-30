package dev.fourthdimension.runtime;

import dev.fourthdimension.FourthDimension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class FoundationSync {
  private FoundationSync() {}

  public record Snapshot(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<Snapshot> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, "foundation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeNbt(payload.tag),
            (buf) -> {
              var tag = buf.readNbt(net.minecraft.nbt.NbtAccounter.create(8 * 1024 * 1024));
              if (!(tag instanceof CompoundTag compound))
                throw new io.netty.handler.codec.DecoderException(
                    "Expected a functional snapshot compound");
              return new Snapshot(compound);
            });

    @Override
    public Type<Snapshot> type() {
      return TYPE;
    }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("2")
        .playToClient(
            Snapshot.TYPE,
            Snapshot.CODEC,
            (payload, context) ->
                context.enqueueWork(
                    () -> {
                      var level = context.player().level();
                      EligibilityHooks.CLIENT.put(
                          level, FoundationData.load(payload.tag, level.registryAccess()));
                    }));
  }

  public static void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) send(player);
  }

  public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) send(player);
  }

  public static void broadcast(ServerLevel level) {
    for (var player : level.players()) send(player);
  }

  private static void send(ServerPlayer player) {
    var level = player.serverLevel();
    PacketDistributor.sendToPlayer(
        player,
        new Snapshot(FoundationData.get(level).save(new CompoundTag(), level.registryAccess())));
  }

  public static void tick(LevelTickEvent.Post event) {
    if (event.getLevel() instanceof ServerLevel level
        && level.getGameTime() % 20 == 0
        && !level.players().isEmpty()) for (var player : level.players()) send(player);
  }
}
