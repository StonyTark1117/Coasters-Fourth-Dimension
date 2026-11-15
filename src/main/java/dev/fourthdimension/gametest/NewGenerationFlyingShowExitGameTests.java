package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.PropulsionInterlock;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd1802_show_exit")
@PrefixGameTestTemplate(false)
public final class NewGenerationFlyingShowExitGameTests {
 @GameTest(template="empty",timeoutTicks=500)
 public static void completedShowCanDepart(GameTestHelper h){
  var l=h.getLevel();NewGenerationFlyingRouteGameTests.Rig[] rig={null};double[] held={0};
  h.runAtTickTime(1,()->rig[0]=NewGenerationFlyingRouteGameTests.create(h,0));
  h.runAtTickTime(20,()->{var r=rig[0];r.rail().yaw.intervals=List.of(
   new YawSection.Interval(UUID.randomUUID(),"Show",0,45,YawState.Mode.SHOW_STOP,0,0,0,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(45,Math.PI))),null,null,"",YawState.Winding.NEAREST,.6,.8,"Departure",200),
   new YawSection.Interval(UUID.randomUUID(),"Departure",45,100,YawState.Mode.PROFILE,0,0,0,new RotationProfile(List.of(new RotationProfile.Key(0,Math.PI),new RotationProfile.Key(55,Math.PI))),null,null,"",YawState.Winding.NEAREST,.6,.8,"",0));r.rail().yaw.revision++;r.vehicle().yaw.mode=YawState.Mode.PROFILE;});
  h.runAtTickTime(30,()->{var v=rig[0].vehicle();held[0]=v.yaw.lastDistance;NewGenerationFlyingRuntime.show(l,v,UUID.randomUUID(),v.yaw.epoch,"Departure");});
  h.runAtTickTime(80,()->{try{NewGenerationFlyingRuntime.dispatch(l,rig[0].vehicle());h.fail("Incomplete show departure accepted");}catch(IllegalArgumentException expected){}});
  h.runAtTickTime(280,()->{var v=rig[0].vehicle();h.assertTrue(Math.abs(v.yaw.angle-Math.PI)<.035&&Math.abs(v.yaw.lastDistance-held[0])<.01,"Completed show physically held position");NewGenerationFlyingRuntime.dispatch(l,v);});
  h.runAtTickTime(390,()->{var v=rig[0].vehicle();h.assertTrue(v.yaw.lastDistance>49&&v.yaw.travelAuthorized&&!v.stopping&&v.fault.isEmpty(),"Completed show departs through next interval: "+v.yaw.lastDistance+" / "+v.yaw.status+" / "+v.fault);h.assertTrue(Math.abs(v.yaw.angle-Math.PI)<.035,"Departure preserves completed show heading");
   var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);h.assertTrue(!PropulsionInterlock.holds(b,l),"Valid hierarchy permits native drive");var deck=v.yaw.deck;v.yaw.deck=UUID.randomUUID();h.assertTrue(PropulsionInterlock.holds(b,l),"Missing deck immediately denies native drive before controller tick");v.yaw.deck=deck;NewGenerationFlyingRuntime.capture(l,v);
   System.out.println("FD_NG_SHOW_EXIT PASS incompleteDenied=true actualShowHeld=true explicitDispatchLeaves=true nextInterval=true missingDeckImmediateDriveHold=true distance="+v.yaw.lastDistance);h.succeed();});
 }
}
