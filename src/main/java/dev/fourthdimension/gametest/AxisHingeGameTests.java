package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Creation/COM regression only; accepted roll/power dynamics are retained separately. */
@GameTestHolder("fourthdimension_fd1302_hinge")
@PrefixGameTestTemplate(false)
public final class AxisHingeGameTests {
 @GameTest(template="empty",timeoutTicks=180)
 public static void nativeHingeCoincidesEmptyAndLoaded(GameTestHelper h){
  var l=h.getLevel();var from=new BlockPos(-600,100,-600);var to=from.offset(0,0,64);
  for(int x=-39;x<=-36;x++)for(int z=-39;z<=-33;z++)l.setChunkForced(x,z,true);
  for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=SpinningExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var data=FoundationData.get(l);double length=TrackGeometry.table(l,edge).length();
  var rail=new FoundationData.Rail(from,to,Family.AXIS,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),AxisVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
  data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
  var car=new FoundationData.Vehicle[1];
  h.runAtTickTime(10,()->car[0]=AxisVehicle.create(l,SpinningExamples.sample(l,edge,12)));
  h.runAtTickTime(50,()->{
   hinge(h,car[0],"empty");
   var c=car[0].carriers.getFirst();var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
   var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,b.getPlot().getCenterBlock().offset(c.seats.getFirst()),pig);
  });
  h.runAtTickTime(120,()->{hinge(h,car[0],"loaded");h.succeed();});
 }
 private static void hinge(GameTestHelper h,FoundationData.Vehicle v,String phase){
  var container=SubLevelContainer.getContainer(h.getLevel());var c=v.carriers.getFirst();
  var parent=(ServerSubLevel)container.getSubLevel(v.chassis);var child=(ServerSubLevel)container.getSubLevel(c.body);
  var p=parent.getPlot().getCenterBlock();var q=child.getPlot().getCenterBlock();
  var a=parent.logicalPose().transformPosition(new Vector3d(p.getX()+.5+c.side,p.getY()+.5+c.pivotHeight,p.getZ()+.5+c.longitudinal));
  var b=child.logicalPose().transformPosition(new Vector3d(q.getX()+.5+c.childPivotX,q.getY()+.5+c.childPivotOffset,q.getZ()+.5+c.childPivotZ));
  double error=a.distance(b);
  System.out.println("FD_AXIS_HINGE phase="+phase+" error="+error+" parent="+a+" child="+b);
  h.assertTrue(error<.03,"Axis "+phase+" physical hinge mismatch: "+error);
 }
}
