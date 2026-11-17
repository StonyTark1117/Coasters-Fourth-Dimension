package dev.fourthdimension.tools;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ToolsNetwork {
  public static Consumer<State> receive=state->{};
  private ToolsNetwork(){}
  public record State(UUID id,CompoundTag tag) implements CustomPacketPayload {
    public static final Type<State> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("fourthdimension","operator_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=StreamCodec.of((b,p)->{b.writeUUID(p.id);b.writeNbt(p.tag);},b->new State(b.readUUID(),b.readNbt()));
    @Override public Type<State> type(){return TYPE;}
  }
  public static void register(PayloadRegistrar registrar){registrar.playToClient(State.TYPE,State.CODEC,(p,c)->c.enqueueWork(()->receive.accept(p)));}
  public static void send(ServerPlayer player,UUID id){
    CompoundTag tag;
    try{tag=OperatorPanel.sample(player,id);}catch(IllegalArgumentException|IllegalStateException e){tag=dev.fourthdimension.integration.RideTelemetry.unavailable(e.getMessage()).save();}
    PacketDistributor.sendToPlayer(player,new State(id,tag));
  }
}
