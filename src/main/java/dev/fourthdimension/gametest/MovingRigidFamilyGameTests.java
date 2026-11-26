package dev.fourthdimension.gametest;
import dev.fourthdimension.track.TrackDesign;
import java.util.List;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
/** Remaining rigid-body adapter cases, separate namespace preserves accepted articulated cases. */
@GameTestHolder("fourthdimension_fd2001_rigid")
@PrefixGameTestTemplate(false)
public final class MovingRigidFamilyGameTests {
  @GameTest(template="empty",timeoutTicks=520)
  public static void rigidSpecialtyBodyGraphs(GameTestHelper h){MovingFamilyGameTests.run(h,2,List.of(TrackDesign.DIVE,TrackDesign.PIPELINE,TrackDesign.FIXED_INVERTED,TrackDesign.STANDING_STANDARD,TrackDesign.MOTORBIKE,TrackDesign.FLYING_BIKE));}
}
