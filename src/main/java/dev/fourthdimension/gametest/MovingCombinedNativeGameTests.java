package dev.fourthdimension.gametest;
import dev.fourthdimension.moving.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
/** Separate affected namespace preserves already accepted single-axis siblings. */
@GameTestHolder("fourthdimension_fd2006_combined")
@PrefixGameTestTemplate(false)
public final class MovingCombinedNativeGameTests {
  @GameTest(template="empty",timeoutTicks=2200) public static void sideDrop(GameTestHelper h){MovingMechanismNativeGameTests.run(h,5,new MovingProgramSpec(MovingMechanism.SIDE_DROP,4,0,0,false));}
  @GameTest(template="empty",timeoutTicks=2200) public static void compound(GameTestHelper h){MovingMechanismNativeGameTests.run(h,6,new MovingProgramSpec(MovingMechanism.COMPOUND,15,0,90,false));}
}
