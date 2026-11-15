package dev.fourthdimension.gametest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("fourthdimension_fd1901_train")
@PrefixGameTestTemplate(false)
public final class PoweredInvertedYawTrainGameTests {
  @GameTest(template="empty",timeoutTicks=4500)
  public static void fourIndependentUprightYawCarsShowReverseCapture(GameTestHelper h){PoweredInvertedYawGameTests.cycle(h,4,new BlockPos(100,100,100));}
}
