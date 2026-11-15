package dev.fourthdimension.gametest;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FamilyControllers;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

/** Distinct marker/authority fallback guards on the actual compound native body. */
@GameTestHolder("fourthdimension_fd1802_failure")
@PrefixGameTestTemplate(false)
public final class NewGenerationFlyingFailureGameTests {
 @GameTest(template="empty",timeoutTicks=1100)
 public static void distinctFallbacks(GameTestHelper h){
  var l=h.getLevel();NewGenerationFlyingRouteGameTests.Rig[] r={null};YawSection.Interval[] original={null};BlockPos[] vertical={null};double[] before={0};
  h.runAtTickTime(1,()->r[0]=NewGenerationFlyingRouteGameTests.create(h,0));
  h.runAtTickTime(20,()->r[0].vehicle().yaw.mode=YawState.Mode.PROFILE);
  h.runAtTickTime(190,()->{original[0]=r[0].rail().yaw.intervals.getFirst();var far=new BlockPos(12000,140,12000);h.assertTrue(!l.hasChunkAt(far),"Fixture marker chunk is unloaded");replace(r[0],original[0],far,YawState.Winding.NEAREST);});
  h.runAtTickTime(230,()->{held(h,r[0],"MARKER_UNLOADED");restore(h,r[0],original[0]);});
  h.runAtTickTime(270,()->{((YawMarkerEntity)l.getBlockEntity(r[0].marker())).owner=UUID.randomUUID();});
  h.runAtTickTime(310,()->{held(h,r[0],"MARKER_UNAUTHORIZED");((YawMarkerEntity)l.getBlockEntity(r[0].marker())).owner=r[0].owner();NewGenerationFlyingRuntime.retry(l,r[0].vehicle());});
  h.runAtTickTime(350,()->{vertical[0]=r[0].rail().from.offset(0,20,40);l.setBlockAndUpdate(vertical[0],YawMarkers.MARKER.get().defaultBlockState());var m=(YawMarkerEntity)l.getBlockEntity(vertical[0]);m.id=r[0].markerId();m.owner=r[0].owner();replace(r[0],original[0],vertical[0],YawState.Winding.NEAREST);});
  h.runAtTickTime(390,()->{held(h,r[0],"TARGET_DEGENERATE");l.removeBlock(vertical[0],false);restore(h,r[0],original[0]);});
  h.runAtTickTime(430,()->{before[0]=r[0].vehicle().yaw.angle;var b=body(h,r[0].vehicle().chassis);b.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(0,-1,0),Blocks.AIR.defaultBlockState(),3);});
  h.runAtTickTime(435,()->{h.assertTrue(r[0].vehicle().fault.equals("AUTHORITY_UNAVAILABLE"),"Lost authority identified: "+r[0].vehicle().fault);var b=body(h,r[0].vehicle().chassis);b.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(0,-1,0),FamilyControllers.CONTROLLERS.get(Family.FLYING).get().defaultBlockState(),3);});
  h.runAtTickTime(470,()->{held(h,r[0],"AUTHORITY_UNAVAILABLE");h.assertTrue(Math.abs(r[0].vehicle().yaw.angle-before[0])<.035,"Lost authority retains actual yaw");NewGenerationFlyingRuntime.retry(l,r[0].vehicle());NewGenerationFlyingRouteGameTests.section(r[0],0,YawState.Mode.PROFILE,-.5,-.5);});
  h.runAtTickTime(700,()->{var v=r[0].vehicle();h.assertTrue(Math.abs(v.yaw.angle+.5)<.035,"Settled negative bearing before winding-branch exhaustion");v.yaw.angle+=8*Math.PI;v.yaw.reference+=8*Math.PI;replace(r[0],original[0],r[0].marker(),YawState.Winding.POSITIVE);});
  h.runAtTickTime(740,()->{held(h,r[0],"WINDING_UNREACHABLE");h.assertTrue(r[0].vehicle().yaw.angle<YawController.LIMIT,"Unreachable positive target retains permitted winding");System.out.println("FD_NG_FAILURE PASS unloaded=true unauthorized=true degenerate=true authorityLost=true windingUnreachable=true boundedRetention=true explicitRetry=true");h.succeed();});
 }
 private static void replace(NewGenerationFlyingRouteGameTests.Rig r,YawSection.Interval i,BlockPos pos,YawState.Winding winding){r.rail().yaw.intervals=List.of(new YawSection.Interval(UUID.randomUUID(),i.name(),i.start(),i.end(),i.mode(),i.blend(),i.phiFrom(),i.phiTo(),i.profile(),i.marker(),pos,i.dimension(),winding,i.speed(),i.acceleration(),i.stopName(),i.showDuration()));r.rail().yaw.revision++;r.vehicle().yaw.section=null;r.vehicle().yaw.status="READY";}
 private static void restore(GameTestHelper h,NewGenerationFlyingRouteGameTests.Rig r,YawSection.Interval i){r.rail().yaw.intervals=List.of(i);r.rail().yaw.revision++;r.vehicle().yaw.section=null;NewGenerationFlyingRuntime.retry(h.getLevel(),r.vehicle());}
 private static void held(GameTestHelper h,NewGenerationFlyingRouteGameTests.Rig r,String reason){var v=r.vehicle();h.assertTrue(v.yaw.mode==YawState.Mode.HOLD_FAULT&&v.fault.equals(reason),"Specific retained fault expected "+reason+", got "+v.fault);h.assertTrue(v.stopping&&!v.yaw.travelAuthorized&&v.yaw.resumeRequired&&v.flying.retained(),"Failure keeps restraints/drive interlock");h.assertTrue(Math.abs(v.yaw.velocity)<.05&&Math.abs(v.carriers.getFirst().omega)<.05,"Failure settles actual native motion");System.out.println("FD_NG_FAILURE reason="+reason+" angle="+v.yaw.angle+" rate="+v.yaw.velocity+" phi="+(-v.carriers.getFirst().angle));}
 private static ServerSubLevel body(GameTestHelper h,UUID id){return (ServerSubLevel)SubLevelContainer.getContainer(h.getLevel()).getSubLevel(id);}
}
