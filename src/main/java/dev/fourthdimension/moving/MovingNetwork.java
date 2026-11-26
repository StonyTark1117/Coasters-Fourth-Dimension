package dev.fourthdimension.moving;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Requests carry intent and stale-state tokens only; all geometry and authority is server-owned. */
public final class MovingNetwork {
  public static Consumer<State> receive=state->{};
  public record State(BlockPos controller,CompoundTag tag,boolean open) implements CustomPacketPayload {
    public static final Type<State> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("fourthdimension","moving_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=StreamCodec.of((b,p)->{b.writeBlockPos(p.controller);b.writeNbt(p.tag);b.writeBoolean(p.open);},b->new State(b.readBlockPos(),b.readNbt(),b.readBoolean()));
    public Type<State> type(){return TYPE;}
  }
  public record Request(BlockPos controller,String operation,UUID assembly,long epoch,long sequence,String destination,CompoundTag construction) implements CustomPacketPayload {
    public static final Type<Request> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("fourthdimension","moving_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=StreamCodec.of((b,p)->{
      b.writeBlockPos(p.controller);b.writeUtf(p.operation,24);b.writeUUID(p.assembly);b.writeLong(p.epoch);b.writeLong(p.sequence);b.writeUtf(p.destination,40);b.writeNbt(p.construction);
    },b->new Request(b.readBlockPos(),b.readUtf(24),b.readUUID(),b.readLong(),b.readLong(),b.readUtf(40),b.readNbt()));
    public Type<Request> type(){return TYPE;}
  }
  private MovingNetwork(){}
  public static void register(PayloadRegistrar registrar){
    registrar.playToClient(State.TYPE,State.CODEC,(p,c)->c.enqueueWork(()->receive.accept(p)));
    registrar.playToServer(Request.TYPE,Request.CODEC,(p,c)->c.enqueueWork(()->{if(c.player() instanceof ServerPlayer player)handle(player,p);}));
  }
  public static void open(ServerPlayer player,BlockPos pos){send(player,pos,true,null);}
  private static void send(ServerPlayer player,BlockPos pos,boolean open,CompoundTag feedback){
    CompoundTag tag;
    try {tag=MovingRuntime.snapshot(player,pos);}catch(IllegalArgumentException|IllegalStateException e){tag=new CompoundTag();tag.putString("error",e.getMessage());}
    if(feedback!=null)tag.put("result",feedback);
    PacketDistributor.sendToPlayer(player,new State(pos,tag,open));
  }
  private static void handle(ServerPlayer player,Request request){
    CompoundTag result=new CompoundTag();
    try {
      switch(request.operation){
        case "refresh" -> {}
        case "preview","validate" -> result=MovingConstruction.preview(player,request.controller,MovingConstruction.ConfigureRequest.read(request.construction));
        case "configure" -> {var built=MovingConstruction.build(player,request.controller,MovingConstruction.ConfigureRequest.read(request.construction));result.putUUID("built",built.id);}
        default -> MovingRuntime.operate(player,request.controller,MovingRuntime.Action.valueOf(request.operation.toUpperCase(java.util.Locale.ROOT)),request.assembly,request.epoch,request.sequence,request.destination);
      }
      result.putBoolean("success",true);
    }catch(IllegalArgumentException|IllegalStateException e){result.putBoolean("success",false);result.putString("error",e.getMessage()==null?"Invalid moving-track request":e.getMessage());}
    send(player,request.controller,false,result);
  }
}
