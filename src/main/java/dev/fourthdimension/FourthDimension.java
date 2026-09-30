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
    dev.fourthdimension.shuttle.ShuttleContent.registerItem();
    dev.fourthdimension.station.StationContent.registerItem();
    dev.fourthdimension.station.StationContent.BLOCKS.register(bus);
    dev.fourthdimension.station.StationContent.ENTITIES.register(bus);
    FamilyControllers.BLOCKS.register(bus);
    ContentTabs.TABS.register(bus);
    dev.fourthdimension.vehicle.VehicleParts.BLOCKS.register(bus);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.vehicle.Boarding::mount);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.vehicle.Boarding::exits);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.vehicle.Boarding::login);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.vehicle.Boarding::logout);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.vehicle.VehicleLifecycle::ready);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.vehicle.VehicleLifecycle::tick);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.vehicle.VehicleVisualState::tick);
    dev.fourthdimension.track.TrackItems.ITEMS.register(bus);
    dev.fourthdimension.track.TrackItems.COMPONENTS.register(bus);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.track.TrackPlacement::tick);
    bus.addListener(FoundationSync::register);
    bus.addListener(dev.fourthdimension.editor.EditorNetwork::register);
    NeoForge.EVENT_BUS.addListener(PrototypeFixture::register);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.track.ExampleLayouts::register);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.shuttle.ShuttleExamples::register);
    NeoForge.EVENT_BUS.addListener(dev.fourthdimension.shuttle.ShuttleExamples::tick);
    NeoForge.EVENT_BUS.addListener(CarrierRuntime::tick);
    NeoForge.EVENT_BUS.addListener(FoundationSync::login);
    NeoForge.EVENT_BUS.addListener(FoundationSync::dimension);
    NeoForge.EVENT_BUS.addListener(FoundationSync::tick);
    NeoForge.EVENT_BUS.addListener(
        (net.neoforged.neoforge.event.server.ServerStartedEvent event) -> {
          if (Boolean.getBoolean("fourthdimension.freezeOnStart"))
            event.getServer().tickRateManager().setFrozen(true);
        });
    LogUtils.getLogger()
        .info(
            "Coasters: Fourth Dimension loaded from {}",
            FourthDimension.class.getProtectionDomain().getCodeSource().getLocation());
  }
}
