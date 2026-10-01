package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.OptionalIntegrations;
import java.util.Set;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class MagnetizedIntegrationGameTests {
  @GameTest(template = "empty", timeoutTicks = 500)
  public static void magnetizedForwardReverseBrakesAndCarrierMagnets(GameTestHelper helper) {
    if (!OptionalIntegrations.supported("coastersmagnetized", Set.of("1.1.0"))) {
      System.out.println(
          "FD_MAGNETIZED_SKIPPED: optional 1.1.0 artifact absent; no compatibility evidence");
      helper.succeed();
      return;
    }
    dev.fourthdimension.integration.magnetized.MagnetizedRideCheck.run(helper);
  }

  @GameTest(template = "empty", timeoutTicks = 500)
  public static void magnetizedLinkedArrowAndTrailingInterlocks(GameTestHelper helper) {
    if (!OptionalIntegrations.supported("coastersmagnetized", Set.of("1.1.0"))) {
      System.out.println("FD_MAGNETIZED_LINKED_SKIPPED: optional 1.1.0 artifact absent");
      helper.succeed();
      return;
    }
    dev.fourthdimension.integration.magnetized.MagnetizedRideCheck.runLinked(helper);
  }
}
