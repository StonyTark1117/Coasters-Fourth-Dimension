package dev.fourthdimension.integration.create;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.integration.TrackProviders;
import dev.fourthdimension.shuttle.ShuttleContent;
import dev.fourthdimension.station.StationContent;
import java.util.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.*;

public final class CreateIntegration {
  private static final DeferredRegister<DisplaySource> SOURCES =
      DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, FourthDimension.ID);
  private static final List<DeferredHolder<DisplaySource, RideDisplaySource>> ENTRIES =
      new ArrayList<>();

  static {
    for (var field : RideDisplaySource.Field.values())
      ENTRIES.add(
          SOURCES.register(
              "ride_" + field.name().toLowerCase(Locale.ROOT), () -> new RideDisplaySource(field)));
  }

  private CreateIntegration() {}

  public static void register(IEventBus bus) {
    SOURCES.register(bus);
    bus.addListener(CreateIntegration::setup);
  }

  private static void setup(FMLCommonSetupEvent event) {
    event.enqueueWork(
        () -> {
          for (var entry : ENTRIES) {
            DisplaySource.BY_BLOCK_ENTITY.add(StationContent.ENTITY.get(), entry.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ShuttleContent.ENTITY.get(), entry.get());
          }
          TrackProviders.freeze();
          dev.fourthdimension.integration.AppearanceProviders.setup();
        });
  }
}
