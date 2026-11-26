package dev.fourthdimension.gametest;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
/** Independently selectable accepted Roller Ball destination-order case. */
@GameTestHolder("fourthdimension_fd2001_roller_route")
@PrefixGameTestTemplate(false)
public final class MovingRollerRouteGameTests {
 @GameTest(template="empty",timeoutTicks=220)
 public static void rollerOrderedRoute(GameTestHelper h){MovingRouteGameTests.rollerCandidateKeepsSectionOrder(h);}
}
