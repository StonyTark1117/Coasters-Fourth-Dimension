package dev.fourthdimension.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fourthdimension_fd2001_service_persistence")
@PrefixGameTestTemplate(false)
public final class MovingServicePersistenceGameTests {
  @GameTest(template="empty",timeoutTicks=20)
  public static void releasedServiceFramesSurviveButDiskDispatchDoesNot(GameTestHelper h) {
    MovingPersistenceGameTests.releasedServiceFramesSurviveButDiskDispatchDoesNot(h);
  }
}
