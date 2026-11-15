package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.PropulsionInterlock;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd1802_loaded_show")
@PrefixGameTestTemplate(false)
public final class NewGenerationFlyingLoadedShowGameTests {
 @GameTest(template="empty",timeoutTicks=1100)
 public static void loadedProneShowCanDepart(GameTestHelper h){
  var l=h.getLevel();NewGenerationFlyingRouteGameTests.Rig[] rig={null};double[] held={0},peaks={0,0},previous={0};
  h.runAtTickTime(1,()->rig[0]=NewGenerationFlyingRouteGameTests.create(h,0));
  h.runAtTickTime(15,()->{var v=rig[0].vehicle();var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body);var pig=net.minecraft.world.entity.EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(l,child.getPlot().getCenterBlock().offset(v.carriers.getFirst().seats.getFirst()),pig);NewGenerationFlyingRouteGameTests.section(rig[0],Math.PI/2,YawState.Mode.PROFILE,Math.PI,Math.PI);});
  h.runAtTickTime(400,()->{var r=rig[0];r.rail().yaw.intervals=List.of(
   new YawSection.Interval(UUID.randomUUID(),"Show",0,45,YawState.Mode.SHOW_STOP,0,Math.PI/2,Math.PI/2,new RotationProfile(List.of(new RotationProfile.Key(0,Math.PI),new RotationProfile.Key(45,3*Math.PI))),null,null,"",YawState.Winding.NEAREST,.6,.8,"Departure",400),
   new YawSection.Interval(UUID.randomUUID(),"Departure",45,100,YawState.Mode.PROFILE,0,Math.PI/2,Math.PI/2,new RotationProfile(List.of(new RotationProfile.Key(0,3*Math.PI),new RotationProfile.Key(55,3*Math.PI))),null,null,"",YawState.Winding.NEAREST,.6,.8,"",0));r.rail().yaw.revision++;r.vehicle().yaw.mode=YawState.Mode.PROFILE;});
  h.runAtTickTime(420,()->{var v=rig[0].vehicle();held[0]=v.yaw.lastDistance;NewGenerationFlyingRuntime.show(l,v,UUID.randomUUID(),v.yaw.epoch,"Departure");});
  for(int t=421;t<=900;t++){final int tick=t;h.runAtTickTime(t,()->{var v=rig[0].vehicle();peaks[0]=Math.max(peaks[0],Math.abs(v.yaw.velocity));if(tick>422)peaks[1]=Math.max(peaks[1],Math.abs(v.yaw.velocity-previous[0])*20);previous[0]=v.yaw.velocity;});}
  h.runAtTickTime(470,()->{try{NewGenerationFlyingRuntime.dispatch(l,rig[0].vehicle());h.fail("Incomplete show departure accepted");}catch(IllegalArgumentException expected){}});
  h.runAtTickTime(900,()->{var v=rig[0].vehicle();h.assertTrue(v.fault.isEmpty()&&Math.abs(v.yaw.angle-3*Math.PI)<.035&&Math.abs(v.yaw.lastDistance-held[0])<.01,"Completed loaded show physically held position: "+v.fault+" angle="+v.yaw.angle+" drift="+(v.yaw.lastDistance-held[0]));h.assertTrue(v.flying.lock==FlyingState.Lock.PRONE,"Prone endpoint stays captured through the loaded Show");h.assertTrue(peaks[0]<.61&&peaks[1]<.82,"Loaded physical yaw bounds: "+Arrays.toString(peaks));System.out.println("FD_NG_LOADED_SHOW drift="+(v.yaw.lastDistance-held[0])+" peaks="+Arrays.toString(peaks));NewGenerationFlyingRuntime.dispatch(l,v);});
  h.runAtTickTime(1010,()->{var v=rig[0].vehicle();h.assertTrue(v.yaw.lastDistance>49&&v.yaw.travelAuthorized&&!v.stopping&&v.fault.isEmpty(),"Completed show departs through next interval: "+v.yaw.lastDistance+" / "+v.yaw.status+" / "+v.fault);h.assertTrue(Math.abs(v.yaw.angle-3*Math.PI)<.035,"Departure preserves completed show heading");
   var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);h.assertTrue(!PropulsionInterlock.holds(b,l),"Valid hierarchy permits native drive");var deck=v.yaw.deck;v.yaw.deck=UUID.randomUUID();h.assertTrue(PropulsionInterlock.holds(b,l),"Missing deck immediately denies native drive before controller tick");v.yaw.deck=deck;NewGenerationFlyingRuntime.capture(l,v);
   System.out.println("FD_NG_LOADED_SHOW PASS oneOffCenterRider=true prone=true fullTurn=true incompleteDenied=true actualShowHeld=true explicitDispatchLeaves=true nextInterval=true missingDeckImmediateDriveHold=true distance="+v.yaw.lastDistance);h.succeed();});
 }
}
