package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.OptionalIntegrations;
import java.util.Set;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class AdditionsIntegrationGameTests {
  @GameTest(template = "empty", timeoutTicks = 850)
  public static void additionsRequiresOneCompleteConsistAndContinuousReadiness(
      GameTestHelper helper) {
    if (!OptionalIntegrations.supported("coasterfins", Set.of("0.2.0"))) {
      System.out.println(
          "FD_ADDITIONS_CONSIST_SKIPPED: optional 0.2.0 artifact absent; no compatibility"
              + " evidence");
      helper.succeed();
      return;
    }
    dev.fourthdimension.integration.additions.AdditionsConsistCheck.run(helper);
  }

  @GameTest(template = "empty", timeoutTicks = 500)
  public static void additionsStationsAndFinsRespectCarrierInterlocks(GameTestHelper helper) {
    if (!OptionalIntegrations.supported("coasterfins", Set.of("0.2.0"))) {
      System.out.println(
          "FD_ADDITIONS_SKIPPED: optional 0.2.0 artifact absent; no compatibility evidence");
      helper.succeed();
      return;
    }
    dev.fourthdimension.integration.additions.AdditionsStationCheck.run(helper);
  }
}
