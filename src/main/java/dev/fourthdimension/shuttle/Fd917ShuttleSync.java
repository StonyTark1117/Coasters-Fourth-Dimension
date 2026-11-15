package dev.fourthdimension.shuttle;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Presentation only: the far-spike rider must not depend on watching the winch chunk. */
@EventBusSubscriber(modid="fourthdimension")
public final class Fd917ShuttleSync {
  public record State(BlockPos winch, UUID lead, UUID carriage, BlockPos start, BlockPos end,
                      boolean caught, String phase, boolean fault, boolean ready, float rpm) {}
  public static final int MAX_STATES=64; // The existing per-dimension managed-vehicle capacity.
  private static final Map<Level,List<State>> CLIENT = new WeakHashMap<>();
  private Fd917ShuttleSync() {}
  public static List<State> states(Level level) { return CLIENT.getOrDefault(level,List.of()); }

  public record Snapshot(ResourceLocation dimension, CompoundTag state) implements CustomPacketPayload {
    public static final Type<Snapshot> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("fourthdimension","fd917_shuttle"));
    public static final StreamCodec<RegistryFriendlyByteBuf,Snapshot> CODEC=StreamCodec.of(
        (buffer,payload)->{buffer.writeResourceLocation(payload.dimension);buffer.writeNbt(payload.state);},
        buffer->{var dimension=buffer.readResourceLocation();var tag=buffer.readNbt(NbtAccounter.create(128*1024));
          if(!(tag instanceof CompoundTag compound))throw new io.netty.handler.codec.DecoderException("Expected shuttle presentation compound");
          return new Snapshot(dimension,compound);});
    @Override public Type<Snapshot> type(){return TYPE;}
  }
  @EventBusSubscriber(modid="fourthdimension",bus=EventBusSubscriber.Bus.MOD)
  public static final class Registration {
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
      event.registrar("1").playToClient(Snapshot.TYPE,Snapshot.CODEC,(snapshot,context)->context.enqueueWork(()->{
        var level=context.player().level();if(!level.dimension().location().equals(snapshot.dimension))return;
        var states=new ArrayList<State>();
        for(var entry:snapshot.state.getList("winches",Tag.TAG_COMPOUND)){
          var tag=(CompoundTag)entry;
          states.add(new State(BlockPos.of(tag.getLong("pos")),tag.hasUUID("lead")?tag.getUUID("lead"):null,
              tag.hasUUID("carriage")?tag.getUUID("carriage"):null,
              tag.contains("start")?BlockPos.of(tag.getLong("start")):null,
              tag.contains("end")?BlockPos.of(tag.getLong("end")):null,tag.getBoolean("caught"),
              tag.getString("phase"),tag.getBoolean("fault"),tag.getBoolean("ready"),tag.getFloat("rpm")));
        }
        CLIENT.put(level,List.copyOf(states));
      }));
    }
  }
  @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
    if(event.getLevel() instanceof ServerLevel level && level.getGameTime()%20==0 && !level.players().isEmpty()) broadcast(level);
  }
  public static void broadcast(ServerLevel level) {
    if(level.players().isEmpty())return;
    var states=new ArrayList<State>();var seen=new HashSet<UUID>();
    var vehicles=dev.fourthdimension.runtime.FoundationData.get(level).vehicles;
    for(var winch:ShuttleRuntime.winches(level)){
      if(winch.isRemoved()||!level.hasChunkAt(winch.getBlockPos())||level.getBlockEntity(winch.getBlockPos())!=winch)continue;
      // Unbound hardware uses normal chunk BE synchronization. Dimension-wide packets
      // carry only unique, existing managed leads, so arbitrary placed winches cannot grow them.
      if(winch.vehicle==null||!vehicles.containsKey(winch.vehicle)||!seen.add(winch.vehicle))continue;
      if(states.size()==MAX_STATES)break;
      states.add(state(winch));
    }
    PacketDistributor.sendToPlayersInDimension(level,new Snapshot(level.dimension().location(),encode(states)));
  }
  public static State state(ShuttleWinchEntity w) {
    return new State(w.getBlockPos(),w.vehicle,w.carriage,w.routeStart,w.routeEnd,w.catchEngaged,w.phase.name(),
        !w.fault.isBlank(),w.telemetry().dispatchReady(),w.getSpeed());
  }
  public static CompoundTag encode(List<State> states) {
    if(states.size()>MAX_STATES)throw new IllegalArgumentException("Shuttle presentation exceeds vehicle capacity");
    var list=new ListTag();
    for(var s:states) {
      var tag=new CompoundTag();tag.putLong("pos",s.winch.asLong());
      if(s.lead!=null)tag.putUUID("lead",s.lead);
      if(s.carriage!=null)tag.putUUID("carriage",s.carriage);
      if(s.start!=null)tag.putLong("start",s.start.asLong());
      if(s.end!=null)tag.putLong("end",s.end.asLong());
      tag.putBoolean("caught",s.caught);tag.putString("phase",s.phase);
      tag.putBoolean("fault",s.fault);tag.putBoolean("ready",s.ready);tag.putFloat("rpm",s.rpm);list.add(tag);
    }
    var root=new CompoundTag();root.put("winches",list);return root;
  }
}
