package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.OptionalIntegrations;
import java.util.Set;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredIntegrationGameTests {
  @GameTest(template = "empty", timeoutTicks = 2400)
  public static void enginePoweredRidesRespectStationInterlocks(GameTestHelper helper) {
    if (!OptionalIntegrations.supported("coastersengineered", Set.of("1.1.1"))) {
      System.out.println(
          "FD_ENGINEERED_SKIPPED: optional 1.1.1 artifact absent; no compatibility evidence");
      helper.succeed();
      return;
    }
    dev.fourthdimension.integration.engineered.EngineeredRideCheck.run(helper);
  }
}
