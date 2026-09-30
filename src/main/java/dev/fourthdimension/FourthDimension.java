package dev.fourthdimension;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.runtime.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(FourthDimension.ID)
public final class FourthDimension {
  public static final String ID = "fourthdimension";

  public FourthDimension(IEventBus bus) {
    FamilyControllers.BLOCKS.register(bus);
    bus.addListener(FoundationSync::register);
    NeoForge.EVENT_BUS.addListener(PrototypeFixture::register);
    NeoForge.EVENT_BUS.addListener(CarrierRuntime::tick);
    NeoForge.EVENT_BUS.addListener(FoundationSync::login);
    NeoForge.EVENT_BUS.addListener(FoundationSync::dimension);
    NeoForge.EVENT_BUS.addListener(FoundationSync::tick);
    NeoForge.EVENT_BUS.addListener(
        (net.neoforged.neoforge.event.server.ServerStartedEvent event) -> {
          if (Boolean.getBoolean("fourthdimension.freezeOnStart"))
            event.getServer().tickRateManager().setFrozen(true);
        });
    LogUtils.getLogger().info("Coasters: Fourth Dimension foundation loaded");
  }
}
