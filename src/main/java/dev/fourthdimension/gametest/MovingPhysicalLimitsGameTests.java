package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

/** Remaining native derivative/braking observations, not a replay of endpoint qualification. */
@GameTestHolder("fourthdimension_fd2007_physical_limits")
@PrefixGameTestTemplate(false)
public final class MovingPhysicalLimitsGameTests {
  @GameTest(template="empty",timeoutTicks=2200)
  public static void transferLimits(GameTestHelper h){run(h,10,MovingMechanism.TRANSFER,16,0,"limits-transfer16");}
  @GameTest(template="empty",timeoutTicks=2200)
  public static void elevatorLimits(GameTestHelper h){run(h,11,MovingMechanism.ELEVATOR,32,0,"limits-elevator32");}
  @GameTest(template="empty",timeoutTicks=2200)
  public static void dropLimits(GameTestHelper h){run(h,12,MovingMechanism.DROP,12,0,"limits-drop12");}
  @GameTest(template="empty",timeoutTicks=2200)
  public static void teeterLimits(GameTestHelper h){run(h,13,MovingMechanism.TEETER,3,0,"limits-teeter3");}
  @GameTest(template="empty",timeoutTicks=2200)
  public static void sideDropLimits(GameTestHelper h){run(h,14,MovingMechanism.SIDE_DROP,8,0,"limits-side8");}
  @GameTest(template="empty",timeoutTicks=2200)
  public static void compoundLimits(GameTestHelper h){run(h,15,MovingMechanism.COMPOUND,15,180,"limits-compound180");}
  private static void run(GameTestHelper h,int index,MovingMechanism mechanism,int amount,int port,String label){
    MovingMechanismNativeGameTests.run(h,index,new MovingProgramSpec(mechanism,amount,0,port,false),label);
  }
}
