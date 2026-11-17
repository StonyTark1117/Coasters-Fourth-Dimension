package dev.fourthdimension.gametest;
import dev.fourthdimension.tools.OperatorPanel;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import dev.fourthdimension.station.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("fourthdimension_fd11_authority")
@PrefixGameTestTemplate(false)
public final class ToolsAuthorityGameTests {
 @GameTest(template="empty",timeoutTicks=100)
 public static void panelRespectsStationAuthorityAndLoss(GameTestHelper h) {
  var l=h.getLevel();var from=new BlockPos(160,100,400);var to=from.offset(0,0,80);
  for(int x=9;x<=11;x++)for(int z=24;z<=31;z++)l.setChunkForced(x,z,true);
  for(var pos:List.of(from,to))l.setBlockAndUpdate(pos,dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=WingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var station=from.offset(6,0,20);var cars=new FoundationData.Vehicle[1];
  h.runAtTickTime(10,()->{cars[0]=WingVehicle.create(l,WingExamples.sample(l,edge,20));l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(station)).vehicle=cars[0].chassis;});
  h.runAtTickTime(60,()->{
   var v=cars[0];var p=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FD11Station"));p.setGameMode(GameType.CREATIVE);p.setPos(162,101,420);
   RideAuthority.claim(l,station,v.chassis,RideAuthority.NATIVE_CHAIN);
   h.assertTrue(!RideTelemetry.load(OperatorPanel.sample(p,v.chassis)).dispatchReady(),"Open restraints accepted by panel");
   VehicleOperations.restraints(l,v,true);
   var sample=OperatorPanel.sample(p,v.chassis);h.assertTrue(RideTelemetry.load(sample).dispatchReady(),"Bound station not ready: "+sample.getString("denial"));
   h.assertTrue(sample.getBoolean("authority")&&sample.getString("controller").contains(RideAuthority.NATIVE_CHAIN),"Authority identity missing");
   long start=System.nanoTime();for(int i=0;i<100;i++)OperatorPanel.sample(p,v.chassis);
   System.out.println("FD11_PANEL stationSamples=100 elapsedNanos="+(System.nanoTime()-start));
   l.setBlockAndUpdate(station,Blocks.AIR.defaultBlockState());var denied=OperatorPanel.sample(p,v.chassis);
   h.assertTrue(!RideTelemetry.load(denied).dispatchReady(),"Missing controller still ready");
   System.out.println("FD11_PANEL stationAuthority=true restraintGuard=true controllerLossDenied=true");h.succeed();
  });
 }
}
