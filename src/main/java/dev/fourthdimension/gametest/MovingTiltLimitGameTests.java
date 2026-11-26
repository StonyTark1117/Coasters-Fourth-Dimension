package dev.fourthdimension.gametest;
import dev.fourthdimension.moving.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
/** Still-uncovered vertical endpoint with per-native-step measured motion/braking derivatives. */
@GameTestHolder("fourthdimension_fd2004_tilt_limit")
@PrefixGameTestTemplate(false)
public final class MovingTiltLimitGameTests {
  @GameTest(template="empty",timeoutTicks=2200)
  public static void tiltNinetyMeasured(GameTestHelper helper){
    MovingMechanismNativeGameTests.run(helper,8,new MovingProgramSpec(MovingMechanism.TILT,90,0,0,false),"tilt90");
  }
}
