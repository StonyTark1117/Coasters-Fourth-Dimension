package dev.fourthdimension.gametest;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.station.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd1501d_loss")
@PrefixGameTestTemplate(false)
public final class S2SpinningLossGameTests {
  @GameTest(template="empty",timeoutTicks=160)
  public static void bodyAndCouplerLossRetainWholeTrain(GameTestHelper h){
    var l=h.getLevel();var rigs=new ArrayList<S2SpinningGovernorGameTests.Rig>();var tails=new ArrayList<FoundationData.Vehicle>();var angles=new double[2];
    h.runAtTickTime(1,()->{for(int i=0;i<2;i++){
      var r=S2SpinningGovernorGameTests.create(h,new BlockPos(400+200*i,100,400));rigs.add(r);
      var tail=S2SpinningVehicle.create(l,FlyingExamples.sample(l,r.edge(),16));tails.add(tail);
      var station=r.station().north(2);l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)l.getBlockEntity(station)).vehicle=tail.chassis;S2SpinningRuntime.rebindDock(l,tail,station);
    }});
    h.runAtTickTime(50,()->{for(int i=0;i<2;i++){
      var r=rigs.get(i);var container=SubLevelContainer.getContainer(l);
      h.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(l,(ServerSubLevel)container.getSubLevel(r.car().chassis),(ServerSubLevel)container.getSubLevel(tails.get(i).chassis),8),"Native captured coupling");
      S2SpinningRuntime.rebindDock(l,r.car(),r.station());r.governor().bind(r.car());
    }});
    h.runAtTickTime(70,()->{for(var r:rigs){VehicleOperations.restraints(l,r.car(),true);VehicleOperations.dispatch(l,r.car());}});
    h.runAtTickTime(95,()->{
      var container=SubLevelContainer.getContainer(l);for(int i=0;i<2;i++)angles[i]=rigs.get(i).car().carriers.getFirst().angle;
      container.removeSubLevel(container.getSubLevel(tails.getFirst().carriers.getFirst().body),SubLevelRemovalReason.REMOVED);
      h.assertTrue(CoasterCartTrainLinkConstraint.clearAllLinks(l,(ServerSubLevel)container.getSubLevel(tails.get(1).chassis)),"Native coupler removed");
    });
    h.runAtTickTime(105,()->{for(int i=0;i<2;i++)for(var v:List.of(rigs.get(i).car(),tails.get(i))){
      h.assertTrue(v.stopping&&v.restraints&&!v.fault.isEmpty()&&v.carriers.getFirst().capture,"All retained after "+(i==0?"body":"coupler")+" loss: "+v.fault);
      h.assertTrue(!Boarding.ready(l,v),"No failed train may unload");
    }});
    h.runAtTickTime(140,()->{
      for(int i=0;i<2;i++){var r=rigs.get(i);h.assertTrue(Math.abs(r.car().carriers.getFirst().angle-angles[i])<1,"Surviving winding retained");
        for(var v:List.of(r.car(),tails.get(i))){var p=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
          if(p==null){h.assertTrue(i==0&&v==tails.getFirst(),"Only the deliberately removed native carrier hierarchy may disappear");continue;}
          h.assertTrue(SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length()<.05,"Every surviving native chassis physically stopped");}}
      System.out.println("FD_S2_LOSS PASS bodyLossTrainWide=true couplerLossTrainWide=true nativeDeletionCascade=true survivingNativeStopped=true windingRetained=true unloadDenied=true");h.succeed();
    });
  }
}
