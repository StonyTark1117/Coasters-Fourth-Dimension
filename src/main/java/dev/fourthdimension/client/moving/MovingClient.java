package dev.fourthdimension.client.moving;
import dev.fourthdimension.moving.MovingNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
@EventBusSubscriber(modid="fourthdimension",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class MovingClient {
  @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->MovingNetwork.receive=MovingScreen::accept);}
}
