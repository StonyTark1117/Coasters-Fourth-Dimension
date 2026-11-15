package dev.fourthdimension.client;

import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.fourthdimension.integration.*;
import dev.silvergold.simulatedcoasters.client.track.AnchorPeerCurveHit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Coasters0.1.5 anchor hits intentionally clear the older Create selection used by CTNB1.2. */
@EventBusSubscriber(modid="fourthdimension",value=Dist.CLIENT)
public final class CtnbCurveInput {
  private CtnbCurveInput(){}
  @SubscribeEvent(priority=EventPriority.HIGHEST)
  public static void use(InputEvent.InteractionKeyMappingTriggered event){
    var mc=Minecraft.getInstance();
    if(!event.isUseItem()||mc.player==null||mc.level==null||!mc.player.isShiftKeyDown()
        ||!AnchorPeerCurveHit.isActive()||!OptionalIntegrations.installed(OptionalIntegrations.CTNB).mayInspect())return;
    var stack=mc.player.getItemInHand(event.getHand());
    if(!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(OptionalIntegrations.CTNB))return;
    try{
      var material=(TrackMaterial)OptionalCalls.call(stack.getItem(),"material");
      if(!CtnbTracks.selected(material))return;
      var curve=AnchorPeerCurveHit.curve();if(curve==null)return;
      var payload=(CustomPacketPayload)Class.forName("dev.notzyvex.coasters_extras.net.RepaintPayload")
          .getConstructor(BlockPos.class,BlockPos.class,ResourceLocation.class)
          .newInstance(curve.bePositions.getFirst(),curve.bePositions.getSecond(),material.id);
      PacketDistributor.sendToServer(payload);
      event.setCanceled(true);
      mc.player.displayClientMessage(Component.literal("Requested "+material.langName),true);
    }catch(ReflectiveOperationException|IllegalArgumentException unavailable){
      mc.player.displayClientMessage(Component.literal("CTNB material control unavailable"),true);
    }
  }
}
