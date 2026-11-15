package dev.fourthdimension.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Reuses the accepted native contract inputs for the changed Sable physics baseline only. */
@GameTestHolder("fourthdimension_fd501e")
@PrefixGameTestTemplate(false)
public final class BaselineUpgradeGameTests {
  @GameTest(template="empty",timeoutTicks=400)
  public static void controlledJointAndEligibility(GameTestHelper helper){
    FoundationGameTests.nativeCarriersAndEligibility(helper);
  }
}
