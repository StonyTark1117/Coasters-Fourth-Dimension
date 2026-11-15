package dev.fourthdimension.gametest;

import dev.fourthdimension.vehicle.*;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

/** Only the still-open show acknowledgment and physical winding-boundary cases. */
@GameTestHolder("fourthdimension_fd1802_show")
@PrefixGameTestTemplate(false)
public final class NewGenerationFlyingShowGameTests {
 @GameTest(template="empty",timeoutTicks=1750)
 public static void acknowledgedShowAndWinding(GameTestHelper h){
  NewGenerationFlyingRouteGameTests.Rig[] rig={null};double[] held={0},peaks={0,0},old={0};
  h.runAtTickTime(1,()->rig[0]=NewGenerationFlyingRouteGameTests.create(h,0));
  h.runAtTickTime(25,()->NewGenerationFlyingRouteGameTests.section(rig[0],0,YawState.Mode.SHOW_STOP,0,Math.PI));
  h.runAtTickTime(30,()->{var v=rig[0].vehicle();held[0]=v.yaw.lastDistance;var id=UUID.randomUUID();long epoch=v.yaw.epoch;h.assertTrue(NewGenerationFlyingRuntime.show(h.getLevel(),v,id,epoch,"Native show"),"Valid named hold acknowledged");h.assertTrue(!NewGenerationFlyingRuntime.show(h.getLevel(),v,id,epoch,"Native show"),"Duplicate/stale command does not reset show");});
  h.runAtTickTime(500,()->{var v=rig[0].vehicle();h.assertTrue(Math.abs(v.yaw.angle-Math.PI)<Math.toRadians(2),"Actual show heading reached: "+v.yaw.angle+" / "+v.yaw.status+" / "+v.fault);h.assertTrue(Math.abs(v.yaw.lastDistance-held[0])<.01&&v.stopping&&!v.yaw.travelAuthorized,"Show holds distance without driving");System.out.println("FD_NG_SHOW PASS angle="+v.yaw.angle+" distanceBefore="+held[0]+" distanceAfter="+v.yaw.lastDistance+" ticks="+v.yaw.showTicks);NewGenerationFlyingRouteGameTests.section(rig[0],0,YawState.Mode.PROFILE,8*Math.PI,8*Math.PI);});
  for(int t=31;t<1700;t++){final int tick=t;h.runAtTickTime(t,()->{var v=rig[0].vehicle();peaks[0]=Math.max(peaks[0],Math.abs(v.yaw.velocity));peaks[1]=Math.max(peaks[1],Math.abs(v.yaw.velocity-old[0])*20);old[0]=v.yaw.velocity;if(tick%100==0)System.out.println("FD_NG_SHOW_STEP tick="+tick+" psi="+v.yaw.angle+" omega="+v.yaw.velocity+" mode="+v.yaw.mode+" status="+v.yaw.status+" fault="+v.fault);});}
  h.runAtTickTime(1700,()->{var v=rig[0].vehicle();h.assertTrue(v.yaw.mode==YawState.Mode.HOLD_FAULT&&v.fault.equals("WINDING_LIMIT"),"Bounded winding fault: "+v.yaw.angle+" / "+v.fault);h.assertTrue(v.yaw.angle>7.9*Math.PI&&v.yaw.angle<=8*Math.PI+.01&&Math.abs(v.yaw.velocity)<.05,"Actual winding retained near +4turns");h.assertTrue(peaks[0]<.61&&peaks[1]<.82,"Actual winding speed/acceleration bounded "+Arrays.toString(peaks));System.out.println("FD_NG_WINDING PASS angle="+v.yaw.angle+" speed="+v.yaw.velocity+" peaks="+Arrays.toString(peaks));h.succeed();});
 }
}
