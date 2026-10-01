package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.OptionalIntegrations;
import java.util.Set;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredIntegrationGameTests {
  @BeforeBatch(batch = "fourthdimension_engineered_energy")
  public static void enableEnergy(net.minecraft.server.level.ServerLevel level) {
    if (OptionalIntegrations.supported("coastersengineered", Set.of("1.1.1")))
      dev.fourthdimension.integration.engineered.EngineeredEnergyCheck.configure(true);
  }

  @AfterBatch(batch = "fourthdimension_engineered_energy")
  public static void restoreEnergy(net.minecraft.server.level.ServerLevel level) {
    if (OptionalIntegrations.supported("coastersengineered", Set.of("1.1.1")))
      dev.fourthdimension.integration.engineered.EngineeredEnergyCheck.configure(false);
  }

  @GameTest(template = "empty", batch = "fourthdimension_engineered_energy", timeoutTicks = 650)
  public static void energyRequiredLinkedTrainLaunchBrakeAndReverse(GameTestHelper helper) {
    if (!OptionalIntegrations.supported("coastersengineered", Set.of("1.1.1"))) {
      System.out.println("FD_ENGINEERED_ENERGY_SKIPPED: optional 1.1.1 artifact absent");
      helper.succeed();
      return;
    }
    dev.fourthdimension.integration.engineered.EngineeredEnergyCheck.run(helper);
  }

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
