package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** A retained native rider survives exact support repair; invalid substitutes remain rejected. */
@GameTestHolder("fourthdimension_fd1502_recovery")
@PrefixGameTestTemplate(false)
public final class RollerBallRecoveryGameTests {
  @GameTest(template="empty",timeoutTicks=400)
  public static void retainedSupportRepairAndEligibility(GameTestHelper h) {
    var l=h.getLevel();var origin=new BlockPos(-300,80,-300);
    for(int x=-20;x<=-18;x++)for(int z=-22;z<=-7;z++)l.setChunkForced(x,z,true);
    var data=FoundationData.get(l);var container=SubLevelContainer.getContainer(l);
    var ex=new RollerBallExamples.Example[1];
    var player=FakePlayerFactory.get(l,new GameProfile(UUID.nameUUIDFromBytes("FDRollerRepair".getBytes()),"FDRollerRepair"));
    player.setGameMode(GameType.CREATIVE);
    h.runAtTickTime(10,()->ex[0]=RollerBallExamples.create(l,origin,true));
    h.runAtTickTime(60,()->RollerBallRuntime.rebindDock(l,ex[0].car(),ex[0].dock()));
    h.runAtTickTime(130,()->{
      var v=ex[0].car();h.assertTrue(Boarding.ready(l,v),"Initial Roller dock");
      var c=v.carriers.getFirst();var child=(ServerSubLevel)container.getSubLevel(c.body);
      var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);
      SeatBlock.sitDown(l,child.getPlot().getCenterBlock().offset(c.seats.getFirst()),pig);
      var rail=RollerBallRuntime.rail(l,v);var edge=TrackGeometry.resolve(l,rail);
      data.rails.remove(rail);
      h.assertTrue(RollerBallTrack.rejection(l,edge)!=null,"Ordinary native N track must be rejected");
      var wrong=new FoundationData.Rail(rail.from,rail.to,Family.ARROW,rail.profile,UUID.randomUUID(),TrackDesign.ARROW.variant,rail.geometryHash,0,true);
      data.rails.add(wrong);h.assertTrue(RollerBallTrack.rejection(l,edge)!=null,"Wrong family must be rejected");
      data.rails.remove(wrong);data.rails.add(rail);
      var second=RollerBallVehicle.create(l,AxisExamples.sample(l,edge,24));
      boolean ready=v.boardingReady;v.boardingReady=true;second.boardingReady=true;
      h.assertTrue(!ConsistRules.canCouple(l,container.getSubLevel(v.chassis),container.getSubLevel(second.chassis),6),"No second-car link even with both boarding-ready");
      v.boardingReady=ready;
      VehicleOperations.restraints(l,v,true);
      ((CoasterAnchorpointBlockEntity)l.getBlockEntity(rail.from)).removeAnchorPeerCurve(l,rail.to);
    });
    h.runAtTickTime(180,()->{
      var v=ex[0].car();var rail=RollerBallRuntime.rail(l,v);
      h.assertTrue(v.stopping&&v.restraints&&v.alignmentRequired&&VehicleOperations.MATCHING_SUPPORT_LOST.equals(v.fault),"Support loss retains repairable car: "+v.fault);
      h.assertTrue(v.carriers.getFirst().occupants.size()==1,"Native rider retained");
      EditorGameTests.rejects(h,()->VehicleOperations.repairMatchingSupport(l,v),"Missing curve repair");
      player.setPos(rail.to.getX()+2,rail.to.getY()+1,rail.to.getZ());player.setItemInHand(InteractionHand.MAIN_HAND,TrackItems.ROLLER_BALL.get().getDefaultInstance());
      EditorGameTests.rejects(h,()->TrackPlacement.connect(l,rail.from,rail.to,TrackDesign.ARROW,player,player.getMainHandItem()),"Occupied wrong-family restoration");
      TrackPlacement.connect(l,rail.from,rail.to,TrackDesign.ROLLER_BALL,player,player.getMainHandItem());
    });
    h.runAtTickTime(220,()->{
      var v=ex[0].car();var r=RollerBallRuntime.rail(l,v);long hash=r.geometryHash;r.geometryHash^=1;
      EditorGameTests.rejects(h,()->VehicleOperations.repairMatchingSupport(l,v),"Changed fingerprint repair");r.geometryHash=hash;
      VehicleOperations.repairMatchingSupport(l,v);
      h.assertTrue(v.alignmentRequired&&v.stopping&&v.restraints,"Repair stays retained before realignment");
      EditorGameTests.rejects(h,()->RollerBallRuntime.resume(l,v),"Resume before realignment");
      VehicleOperations.realignMatchingSupport(l,v);
    });
    h.runAtTickTime(300,()->{
      var v=ex[0].car();h.assertTrue(!v.alignmentRequired&&v.fault.isEmpty()&&v.stopping&&v.restraints&&v.carriers.getFirst().occupants.size()==1,"Exact native repair retains identity/rider: "+v.fault);
      RollerBallRuntime.resume(l,v);
    });
    h.runAtTickTime(330,()->{
      var v=ex[0].car();h.assertTrue(!v.stopping&&v.fault.isEmpty()&&v.carriers.getFirst().occupants.size()==1,"Explicit resume after original support repair: "+v.fault);
      System.out.println("FD_ROLLER_RECOVERY_PASS chassis="+v.chassis+" child="+v.carriers.getFirst().body+" rider="+v.carriers.getFirst().occupants);h.succeed();
    });
  }
}
