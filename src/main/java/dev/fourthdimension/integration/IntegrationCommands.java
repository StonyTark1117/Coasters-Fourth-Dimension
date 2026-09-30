package dev.fourthdimension.integration;

import java.util.Set;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class IntegrationCommands {
  private IntegrationCommands() {}

  public static void register(RegisterCommandsEvent event) {
    if (OptionalIntegrations.supported("coastersengineered", Set.of("1.1.1")))
      dev.fourthdimension.integration.engineered.EngineeredExamples.register(event);
  }
}
