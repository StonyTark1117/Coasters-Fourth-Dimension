package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.integration.RideAuthority;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd1901_single")
@PrefixGameTestTemplate(false)
public final class PoweredInvertedYawGameTests {
  @GameTest(template="empty",timeoutTicks=4500)
  public static void uprightPoweredYawShowReverseAndCapture(GameTestHelper h){cycle(h,1,new BlockPos(100,100,100));}
  public static void cycle(GameTestHelper h,int count,BlockPos origin){
    var l=h.getLevel();var rig=new PoweredInvertedYawExamples.Example[1];var fullTurn=new boolean[count];var tracked=new boolean[count];var showTurn=new boolean[count];var reversed=new boolean[1];var captured=new boolean[1];var requestedCapture=new boolean[1];
    var peakSpeed=new double[count];var peakYaw=new double[count];var trackingSpread=new double[1];var peakRamp=new double[1];var lastSource=new double[1];var heldAngle=new double[count];var seenHold=new boolean[1];var eventSequence=new long[1];
    h.runAtTickTime(1,()->{for(int x=(origin.getX()>>4)-1;x<=(origin.getX()>>4)+5;x++)for(int z=(origin.getZ()>>4)-3;z<=(origin.getZ()>>4)+11;z++)l.setChunkForced(x,z,true);rig[0]=PoweredInvertedYawExamples.create(l,origin,count,UUID.randomUUID());});
    h.runAtTickTime(50,()->PoweredInvertedYawExamples.bind(l,rig[0]));
    h.runAtTickTime(65,()->{for(int i=0;i<count;i++){var v=rig[0].cars().get(i);var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body);for(int seat=0;seat<(i%2==0?1:2);seat++){var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,child.getPlot().getCenterBlock().offset(v.carriers.getFirst().seats.get(seat)),pig);}}});
    h.runAtTickTime(90,()->{var e=rig[0];h.assertTrue(Boarding.ready(l,e.cars().getFirst()),"Actual upright dock capture before dispatch");h.assertTrue(e.governor().getGeneratedSpeed()==0&&e.governor().calculateStressApplied()>0,"Real supplied transmission consumes stress");VehicleOperations.restraints(l,e.cars().getFirst(),true);VehicleOperations.dispatch(l,e.cars().getFirst());});
    for(int t=91;t<=4400;t++){final int tick=t;h.runAtTickTime(t,()->{
      var e=rig[0];var master=e.cars().getFirst();boolean allDock=true;double minTarget=Double.POSITIVE_INFINITY,maxTarget=Double.NEGATIVE_INFINITY;int trackingCars=0;double leadAt=0;
      for(int i=0;i<count;i++){
        var v=e.cars().get(i);var c=v.carriers.getFirst();var s=v.poweredYaw;var parent=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
        h.assertTrue(v.fault.isEmpty(),"Powered car"+i+" fault="+v.fault);var velocity=SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d());peakSpeed[i]=Math.max(peakSpeed[i],velocity.length());peakYaw[i]=Math.max(peakYaw[i],Math.abs(c.omega));fullTurn[i]|=Math.abs(c.angle)>2*Math.PI-.08;
        var rail=PoweredInvertedYawRuntime.rail(l,v);var edge=TrackGeometry.resolve(l,rail);var center=CoasterCartResolve.cartCenterWorld(parent);var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));double at=TrackGeometry.table(l,edge).distanceAt(hit.t());if(i==0)leadAt=at;
        if(s.mode==YawState.Mode.TARGET&&s.status.equals("TARGET_VALID")){tracked[i]=true;trackingCars++;minTarget=Math.min(minTarget,s.target);maxTarget=Math.max(maxTarget,s.target);}
        if(s.phase==PoweredInvertedYawState.Phase.HELD_DWELL){if(!seenHold[0])heldAngle[i]=c.angle;showTurn[i]|=Math.abs(c.angle-heldAngle[i])>Math.PI;h.assertTrue(velocity.length()<=.055,"Show holds native travel while yaw turns");}
        reversed[0]|=s.direction<0&&velocity.length()>.5;
        allDock&=rail.matches(e.edges().getFirst().from(),e.edges().getFirst().to())&&at>5&&at<58;
        if(tick%100==0)System.out.println("FD_POWERED_NATIVE tick="+tick+" car="+i+" phase="+s.phase+" mode="+s.mode+" distance="+at+" actualSpeed="+velocity.length()+" direction="+s.direction+" psi="+c.angle+" omega="+c.omega+" target="+s.target+" dwell="+s.dwellRemaining+" authority="+v.authority.held);
      }
      if(master.poweredYaw.phase==PoweredInvertedYawState.Phase.HELD_DWELL)seenHold[0]=true;
      if(trackingCars==count)trackingSpread[0]=Math.max(trackingSpread[0],maxTarget-minTarget);
      double source=e.governor().actualBps();if(tick>92&&!requestedCapture[0])peakRamp[0]=Math.max(peakRamp[0],Math.abs(source-lastSource[0])*20);lastSource[0]=source;
      h.assertTrue(e.governor().eventSequence>=eventSequence[0],"Controller event sequence monotonic");eventSequence[0]=e.governor().eventSequence;
      if(reversed[0]&&allDock&&leadAt<40&&!requestedCapture[0]){PoweredInvertedYawRuntime.capture(l,master);requestedCapture[0]=true;}
      if(requestedCapture[0]&&Boarding.ready(l,master)){
        captured[0]=true;for(int i=0;i<count;i++){h.assertTrue(fullTurn[i]&&tracked[i]&&showTurn[i],"Car"+i+" full turn, own tracking and yaw-changing show");h.assertTrue(peakSpeed[i]<=8.08&&peakYaw[i]<=.65,"Native travel/yaw bounds");h.assertTrue(Math.abs(e.cars().get(i).carriers.getFirst().angle)<.035,"Station unwinds actual turns to zero");var saved=PoweredInvertedYawState.read(e.cars().get(i).poweredYaw.save());h.assertTrue(saved.sequence==e.cars().get(i).poweredYaw.sequence&&saved.direction==-1,"Travel/event/direction persistence");}
        if(count==4)h.assertTrue(trackingSpread[0]>.03,"Trailing cars independently project scenery from their own native pivot");h.assertTrue(peakRamp[0]<=2.001,"Actual native source acceleration/deceleration <=2: "+peakRamp[0]);h.assertTrue(seenHold[0]&&reversed[0]&&eventSequence[0]>0,"Finite show, reverse and event output");VehicleOperations.restraints(l,master,false);
        System.out.println("FD_POWERED_NATIVE PASS cars="+count+" peakNativeSpeeds="+Arrays.toString(peakSpeed)+" peakYaw="+Arrays.toString(peakYaw)+" actualSourceRamp="+peakRamp[0]+" independentTargetSpread="+trackingSpread[0]+" fullTurn=true show=true reverse=true capture=true eventSequence="+eventSequence[0]);h.succeed();
      }
    });}
  }
}
