package dev.fourthdimension.gametest;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("fourthdimension_fd501e_free")
@PrefixGameTestTemplate(false)
public final class BaselineFreeUpgradeGameTests {
  @GameTest(template="empty",timeoutTicks=300)
  public static void freeJointLoadingAndSave(GameTestHelper h){
    FoundationGameTests.nativeFreeGroupsOrdinaryAccessAndSave(h);
  }
}
