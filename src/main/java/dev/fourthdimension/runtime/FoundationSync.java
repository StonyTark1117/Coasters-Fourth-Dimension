package dev.fourthdimension.runtime;

import dev.fourthdimension.FourthDimension;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkSync;
import java.util.HashMap;
import java.util.UUID;
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
  private static final HashMap<UUID, Long> PENDING_NATIVE_LINK_RESYNCS = new HashMap<>();

  private static final UUID SESSION=UUID.randomUUID();
  private static long sequence;
  private record Received(UUID session,long sequence){}
  private static final java.util.Map<net.minecraft.world.level.Level,Received> RECEIVED=new java.util.WeakHashMap<>();
  private FoundationSync() {}

  /** A full dimension-scoped state replacement; stale snapshots are ignored client-side. */
  public record Snapshot(ResourceLocation dimension, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<Snapshot> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, "foundation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC =
        StreamCodec.of(
            (buf, payload) -> {
              buf.writeResourceLocation(payload.dimension);
              buf.writeNbt(payload.tag);
            },
            (buf) -> {
              var dimension = buf.readResourceLocation();
              var tag = buf.readNbt(net.minecraft.nbt.NbtAccounter.create(8 * 1024 * 1024));
              if (!(tag instanceof CompoundTag compound))
                throw new io.netty.handler.codec.DecoderException(
                    "Expected a functional snapshot compound");
              return new Snapshot(dimension, compound);
            });

    @Override
    public Type<Snapshot> type() {
      return TYPE;
    }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("7")
        .playToClient(
            Snapshot.TYPE,
            Snapshot.CODEC,
            (payload, context) ->
                context.enqueueWork(
                    () -> {
                      var level = context.player().level();
                      if (!accepts(level, payload)) return;
                      var old=RECEIVED.get(level);
                      if(payload.tag.hasUUID("sync_session")){
                        var session=payload.tag.getUUID("sync_session");var seq=payload.tag.getLong("sync_sequence");
                        if(old!=null&&(!old.session.equals(session)||seq<=old.sequence))return;
                        var restored=FoundationData.load(payload.tag,level.registryAccess());
                        EligibilityHooks.CLIENT.put(level,restored);RECEIVED.put(level,new Received(session,seq));return;
                      }
                      EligibilityHooks.CLIENT.put(
                          level, FoundationData.load(payload.tag, level.registryAccess()));
                    }));
  }

  public static void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) {
      send(player);
      queueNativeLinkResync(player);
    }
  }

  public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) {
      send(player);
      queueNativeLinkResync(player);
    }
  }

  public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
    PENDING_NATIVE_LINK_RESYNCS.remove(event.getEntity().getUUID());
  }

  public static void broadcast(ServerLevel level) {
    for (var player : level.players()) send(player);
  }

  public static void send(ServerPlayer player) {
    var level = player.serverLevel();
    PacketDistributor.sendToPlayer(
        player,
        new Snapshot(
            level.dimension().location(),
            snapshot(level)));
  }

  private static CompoundTag snapshot(ServerLevel level){
    var tag=FoundationData.get(level).snapshot(new CompoundTag(),level.registryAccess());
    tag.putUUID("sync_session",SESSION);tag.putLong("sync_sequence",++sequence);tag.putLong("sync_tick",level.getGameTime());return tag;
  }

  public static boolean accepts(net.minecraft.world.level.Level level, Snapshot snapshot) {
    return level != null && level.dimension().location().equals(snapshot.dimension);
  }

  public static void tick(LevelTickEvent.Post event) {
    if (event.getLevel() instanceof ServerLevel level
        && !level.players().isEmpty()) {
      for (var player : level.players()) {
        if (level.getGameTime() % 20 == 0) send(player);
        var resyncAt = PENDING_NATIVE_LINK_RESYNCS.get(player.getUUID());
        if (resyncAt != null && level.getGameTime() >= resyncAt) {
          // The dependency's login sync can precede restoration of persisted cart links. Replay
          // it one second later so a joining client receives the link data that now exists.
          CoasterCartTrainLinkSync.sendFullSyncTo(player);
          PENDING_NATIVE_LINK_RESYNCS.remove(player.getUUID());
        }
      }
    }
  }

  private static void queueNativeLinkResync(ServerPlayer player) {
    PENDING_NATIVE_LINK_RESYNCS.put(player.getUUID(), player.serverLevel().getGameTime() + 20);
  }
}
