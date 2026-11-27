package dev.fourthdimension.gametest;

import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd2008_setup")
@PrefixGameTestTemplate(false)
public final class MovingLaunchSystemsSetupGameTests {
  @GameTest(template="empty",timeoutTicks=3200)
  public static void occupiedSetup(GameTestHelper helper) {
    MovingLaunchSystemsGameTests.run(helper,false,true);
  }
  @GameTest(template="empty",timeoutTicks=100)
  public static void receivingSection(GameTestHelper h) {
    var l=h.getLevel();var s=dev.fourthdimension.runtime.FoundationData.get(l).moving.assemblies.values().stream().filter(a->a.configuration.equals("wing")&&a.released).findFirst().orElseThrow();
    var d=s.docks.get("end");var receiver=MovingLaunchSystemsGameTests.install(l,d.from(),d.to(),new net.minecraft.core.BlockPos(-402,128,-420),true);
    var exit=MovingLaunchSystemsGameTests.install(l,d.to(),d.to().offset(0,0,64),d.to().offset(4,-2,8),false);
    MovingLaunchSystemsGameTests.call(receiver,"connectDestination",exit);
    MovingLaunchSystemsGameTests.call(exit,"setArmed",true);
    System.out.println("FD2008_OCCUPIED_RECEIVER_SETUP receiver=-402,128,-420");h.succeed();
  }
}
