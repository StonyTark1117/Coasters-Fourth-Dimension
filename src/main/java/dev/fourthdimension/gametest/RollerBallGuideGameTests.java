package dev.fourthdimension.gametest;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
/** Only the unresolved actual guide acceleration, reusing the accepted route fixture. */
@GameTestHolder("fourthdimension_fd1502_guide")
@PrefixGameTestTemplate(false)
public final class RollerBallGuideGameTests {
 @GameTest(template="empty",timeoutTicks=2500)
 public static void actualGuideBounds(GameTestHelper h){RollerBallRouteGameTests.run(h,true);}
}
