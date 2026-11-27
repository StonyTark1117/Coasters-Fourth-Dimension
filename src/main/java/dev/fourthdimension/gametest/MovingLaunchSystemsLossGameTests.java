package dev.fourthdimension.gametest;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("fourthdimension_fd2008_loss")
@PrefixGameTestTemplate(false)
public final class MovingLaunchSystemsLossGameTests {
  @GameTest(template="empty",timeoutTicks=3200)
  public static void releasedServiceDependencyLoss(GameTestHelper helper){MovingLaunchSystemsGameTests.runFailures(helper);}
}
