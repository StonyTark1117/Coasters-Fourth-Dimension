package dev.fourthdimension.gametest;

import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd2008_arrow")
@PrefixGameTestTemplate(false)
public final class MovingLaunchSystemsArrowGameTests {
  @GameTest(template="empty",timeoutTicks=3200)
  public static void originalArrowConsumer(GameTestHelper helper) {
    MovingLaunchSystemsGameTests.run(helper,true,false);
  }
}
