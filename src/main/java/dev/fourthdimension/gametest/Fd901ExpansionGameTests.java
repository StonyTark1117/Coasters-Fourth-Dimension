package dev.fourthdimension.gametest;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

/** Remaining variant boundaries; positive family mechanisms retain their own accepted suites. */
@GameTestHolder("fourthdimension_fd901_expansion")
@PrefixGameTestTemplate(false)
public final class Fd901ExpansionGameTests {
  @GameTest(template="empty",timeoutTicks=240)
  public static void distinctVariantsRejectNativeMixedConsistsAndPipelineRejectsBareBearing(GameTestHelper h){
    var l=h.getLevel();var c=SubLevelContainer.getContainer(l);
    var s1=SpinningExamples.create(l,new BlockPos(-700,110,-500),true).vehicle();
    var s2example=S2SpinningExamples.create(l,new BlockPos(-350,110,-500),1);var s2=s2example.cars().getFirst();
    var flying=FlyingExamples.create(l,new BlockPos(0,110,-500),true).vehicle();
    var ng=NewGenerationFlyingExamples.create(l,new BlockPos(350,110,-500),new UUID(0,901)).vehicle();
    var pipeline=PipelineExamples.create(l,new BlockPos(650,110,-500),true);
    var arrow=ExampleLayouts.create(l,new BlockPos(-700,110,200),TrackDesign.ARROW);
    for(var edge:CoasterPathGraphManager.get(l).edges())for(int i=0;i<=64;i++){
      var p=CoasterPathGraphManager.sampleEdgePosition(l,edge,i/64d);int x=((int)Math.floor(p.x))>>4,z=((int)Math.floor(p.z))>>4;
      for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)l.setChunkForced(x+dx,z+dz,true);
    }
    h.runAtTickTime(120,()->{
      S2SpinningExamples.bind(l,s2example);
      int count=0;
      for(var pair:List.of(List.of(s1,s2),List.of(flying,ng),List.of(pipeline.vehicle(),arrow))){
        h.assertTrue(pair.stream().allMatch(v->Boarding.ready(l,v)),"Boundary test requires actual captured ready endpoints");
        for(int direction=0;direction<2;direction++){
          var a=pair.get(direction);var b=pair.get(1-direction);var ab=(ServerSubLevel)c.getSubLevel(a.chassis);var bb=(ServerSubLevel)c.getSubLevel(b.chassis);
          var af=a.boundFrom;var at=a.boundTo;var bf=b.boundFrom;var bt=b.boundTo;
          var al=Set.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(ab));var bl=Set.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(bb));
          double distance=a==arrow?6:8;
          h.assertTrue(!ConsistRules.canCouple(l,ab,bb,distance),"Mixed variant accepted by shared coupling policy");
          h.assertTrue(!CoasterCartTrainLinkConstraint.linkCarts(l,ab,bb,distance),"Mixed variant accepted by native coupling");
          h.assertTrue(Objects.equals(a.boundFrom,af)&&Objects.equals(a.boundTo,at)&&Objects.equals(b.boundFrom,bf)&&Objects.equals(b.boundTo,bt)
              && al.equals(Set.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(ab)))&&bl.equals(Set.copyOf(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(bb))),"Rejected coupling changed bindings or consist");count++;
        }
      }
      var edge=pipeline.edges().getFirst();var hit=new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(l,edge,.75),edge,.75);
      var q=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(l,hit));
      var origin=CoasterCartLinkPlacement.plotOriginForBearingCenter(CoasterPathTrackFrame.pathAnchorForSnappedBlock(l,hit),q);
      var ordinary=CoasterCartSpawner.spawnMinimalContraption(l,origin,q,hit,true);
      h.assertTrue(ordinary!=null&&!EligibilityHooks.allows(ordinary,l,hit)&&CoasterCartRailSnapEligibility.filterHitForBearing(ordinary,l,hit)==null,"Pipeline accepted an ordinary bare bearing");
      h.assertTrue(!FoundationData.get(l).vehicles.containsKey(ordinary.getUniqueId()),"Rejected bare bearing acquired addon state");
      c.removeSubLevel(ordinary,SubLevelRemovalReason.REMOVED);
      System.out.println("FD901_EXPANSION directedVariantAndFamilyRejects="+count+" bindingsAndConsistsUnchanged=true pipelineBareDenied=true");h.succeed();
    });
  }
}
