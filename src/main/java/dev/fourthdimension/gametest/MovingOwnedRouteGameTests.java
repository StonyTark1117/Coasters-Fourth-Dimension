package dev.fourthdimension.gametest;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("fourthdimension_fd2001_owned_routes")
@PrefixGameTestTemplate(false)
public final class MovingOwnedRouteGameTests {
 public static void rollerOrderedRoute(GameTestHelper h){MovingRouteGameTests.rollerCandidateKeepsSectionOrder(h);}
 @GameTest(template="empty",timeoutTicks=220)
 public static void flyingBikeKeepsTwoLaunchOwners(GameTestHelper h){
  var l=h.getLevel();var o=new BlockPos(-500,130,300);MovingRouteGameTests.force(l,o,9,10);
  var e=FlyingBikeExamples.create(l,o,true);var v=e.vehicle();var gap=e.edges().get(8);
  // Destination setup: end side platforms before the curve's full reserved cabin sweep.
  // The exact prior obstruction was local (3,-3,90), beyond the launch straight at z88.
  for(int side:new int[]{-1,1})for(int x=3;x<=5;x++)for(int z=86;z<=90;z++)l.setBlockAndUpdate(o.offset(side*x,-3,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
  var s=MovingRouteGameTests.prepare(h,v,gap,48,new BlockPos(-1100,150,300));
  h.runAtTickTime(60,()->{
   var first=(FlyingBikeGovernorEntity)l.getBlockEntity(e.firstGovernor());var second=(FlyingBikeGovernorEntity)l.getBlockEntity(e.secondGovernor());
   first.bind(v);first.link(second);h.assertTrue(FlyingBikeRuntime.governor(l,v)==first,"Actual two-launch ownership not established");
   MovingRouteGameTests.removeGap(l,gap);var before=List.copyOf(FoundationData.get(l).rails);
   MovingRouteAdapters.validateExit(l,s,v,s.docks.get("candidate"));
   var old=v.flyingBike.governor;v.flyingBike.governor=e.secondGovernor();
   // A subordinate or missing authority must produce an actionable denial, never a null dereference.
   MovingRouteGameTests.expect(h,"governor",()->MovingRouteAdapters.validateExit(l,s,v,s.docks.get("candidate")));v.flyingBike.governor=old;
   MovingRouteAdapters.validateExit(l,s,v,s.docks.get("candidate"));
   h.assertTrue(FoundationData.get(l).rails.equals(before)&&MovingRouteContext.start(l,v)==null,"Destination validation mutated/leaked route");
   System.out.println("FD2001_ROUTE_PASS FLYING_BIKE twoRealLaunches=true wrongGovernorRefused=true candidateOnly=true");h.succeed();
  });
 }
}
