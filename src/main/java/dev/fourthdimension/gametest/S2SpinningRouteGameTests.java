package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.physics.MagneticSpan;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd1501d_route")
@PrefixGameTestTemplate(false)
public final class S2SpinningRouteGameTests {
  @GameTest(template="empty",timeoutTicks=2600)
  public static void fourIndependentCarsLaunchInvertCoastAndCapture(GameTestHelper h){
    var l=h.getLevel();var rig=new S2SpinningExamples.Example[1];var inverted=new boolean[4];var coast=new boolean[4];
    var peak=new double[4];var spread=new double[1];var stopped=new boolean[1];var phase=new int[1];var stoppedTick=new long[1];var savedAngles=new double[4];
    h.runAtTickTime(1,()->{
      for(int x=5;x<=13;x++)for(int z=3;z<=21;z++)l.setChunkForced(x,z,true);
      rig[0]=S2SpinningExamples.create(l,new BlockPos(100,100,100),4);
    });
    h.runAtTickTime(50,()->S2SpinningExamples.bind(l,rig[0]));
    h.runAtTickTime(70,()->{
      for(int i=0;i<3;i++){var v=rig[0].cars().get(i);var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body);
        int[] seats=i==1?new int[]{0,1,2,3}:new int[]{i==0?0:3};
        for(int seat:seats){var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,body.getPlot().getCenterBlock().offset(v.carriers.getFirst().seats.get(seat)),pig);}
      }
    });
    h.runAtTickTime(90,()->{VehicleOperations.restraints(l,rig[0].cars().getFirst(),true);VehicleOperations.dispatch(l,rig[0].cars().getFirst());});
    for(int t=91;t<=2500;t++){final int tick=t;h.runAtTickTime(t,()->{
      var e=rig[0];boolean allReturned=true;double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
      for(int i=0;i<4;i++){
        var v=e.cars().get(i);var c=v.carriers.getFirst();var parent=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
        h.assertTrue(v.fault.isEmpty(),"Car"+i+" route fault "+v.fault);
        double up=new Vector3d(0,1,0).rotate(parent.logicalPose().orientation()).y;inverted[i]|=up<-.8;
        var velocity=SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d());peak[i]=Math.max(peak[i],velocity.length());
        var rail=S2SpinningRuntime.rail(l,v);var edge=TrackGeometry.resolve(l,rail);var center=CoasterCartResolve.cartCenterWorld(parent);
        var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
        coast[i]|=inverted[i]&&ChainLiftSpanQuery.findAtGraphHit(l,hit,0)==null&&velocity.length()>1;
        double at=TrackGeometry.table(l,edge).distanceAt(hit.t());
        allReturned&=inverted[i]&&rail.matches(e.edges().getFirst().from(),e.edges().getFirst().to())&&at>8;
        min=Math.min(min,c.angle);max=Math.max(max,c.angle);
        if(tick%100==0)System.out.println("FD_S2_ROUTE tick="+tick+" car="+i+" up="+up+" speed="+velocity.length()+" angle="+c.angle+" omega="+c.omega+" edge="+edge.from()+" at="+at);
      }
      spread[0]=Math.max(spread[0],max-min);
      if(allReturned&&!stopped[0]){S2SpinningRuntime.capture(l,e.cars().getFirst());stopped[0]=true;stoppedTick[0]=tick;}
      if(stopped[0]&&phase[0]==0&&Boarding.ready(l,e.cars().getFirst())){
        for(int i=0;i<4;i++){h.assertTrue(inverted[i]&&coast[i],"Every car inverts under native unpowered coast");h.assertTrue(peak[i]<16.05,"S2 speed limit car"+i+"="+peak[i]);savedAngles[i]=e.cars().get(i).carriers.getFirst().angle;}
        h.assertTrue(spread[0]>.1,"Four free carriers respond independently to unequal native loads");
        double damp=e.cars().stream().mapToDouble(v->S2SpinningMagnet.audit((ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body)).damperWork).sum();
        h.assertTrue(damp<0,"DAMP removes relative spin energy");
        S2SpinningRuntime.direction(l,e.cars().getFirst(),-1);phase[0]=1;stoppedTick[0]=tick;
      }else if(phase[0]==1&&tick-stoppedTick[0]>6){VehicleOperations.dispatch(l,e.cars().getFirst());phase[0]=2;stoppedTick[0]=tick;
      }else if(phase[0]==2&&tick-stoppedTick[0]>20){
        for(int i=0;i<4;i++){var v=e.cars().get(i);var p=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
          h.assertTrue(SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).z<-.5,"Explicit stopped reversal moves backward");
          h.assertTrue(Math.abs(v.carriers.getFirst().angle-savedAngles[i])<2,"Rollback does not negate retained carrier winding");}
        S2SpinningRuntime.capture(l,e.cars().getFirst());phase[0]=3;
      }else if(phase[0]==3&&Boarding.ready(l,e.cars().getFirst())){
        VehicleOperations.restraints(l,e.cars().getFirst(),false);
        System.out.println("FD_S2_ROUTE PASS nativeInversion=4 nativeCoast=4 independentSpread="+spread[0]+" peakSpeeds="+Arrays.toString(peak)+" dampingWorkNegative=true explicitRollback=true capturedAll=true");h.succeed();
      }
    });}
  }
}
