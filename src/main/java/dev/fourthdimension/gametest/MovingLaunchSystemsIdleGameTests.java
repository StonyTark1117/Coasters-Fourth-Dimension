package dev.fourthdimension.gametest;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("fourthdimension_fd2008_idle")
@PrefixGameTestTemplate(false)
public final class MovingLaunchSystemsIdleGameTests {
 @GameTest(template="empty",timeoutTicks=1300)
 public static void stationarySource(GameTestHelper h){MovingLaunchSystemsGameTests.run(h,false,true,true);}
}
