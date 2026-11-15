package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd811_joins")
@PrefixGameTestTemplate(false)
public final class CtnbJoinGameTests {
  @GameTest(template="empty",timeoutTicks=420)
  public static void nativeJoinsAndCrossingIsolation(GameTestHelper h){
    var l=h.getLevel();var a=new BlockPos(1800,100,600);var b=a.offset(0,0,20);var c=b.offset(0,0,24);var d=c.offset(0,0,48);
    var x=a.offset(-20,0,36);var y=a.offset(20,0,36);
    for(int cx=110;cx<=114;cx++)for(int cz=36;cz<=44;cz++)l.setChunkForced(cx,cz,true);
    for(var p:List.of(a,b,c,d,x,y))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var up=new Vec3(0,1,0);var forward=new Vec3(0,0,1);var reverse=new Vec3(0,0,-1);
    var first=WingExamples.connect(l,a,b,forward,reverse,up,up);
    var nativeJoin=WingExamples.connect(l,b,c,forward,reverse,up,up);
    var last=WingExamples.connect(l,c,d,forward,reverse,up,up);
    var crossing=WingExamples.connect(l,x,y,new Vec3(1,0,0),new Vec3(-1,0,0),up,up);
    var cars=new FoundationData.Vehicle[1];var station=a.offset(6,0,12);
    h.runAtTickTime(10,()->{
      CtnbGameTests.paint(l,first,"BOOST");CtnbGameTests.paint(l,last,"SENSOR");CtnbGameTests.paint(l,crossing,"BOOST");
      l.setBlockAndUpdate(a.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
      cars[0]=WingVehicle.create(l,WingExamples.sample(l,first,12));l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(station)).vehicle=cars[0].chassis;
    });
    h.runAtTickTime(40,()->{
      var v=cars[0];RideAuthority.claim(l,station,v.chassis,RideAuthority.CTNB);VehicleOperations.restraints(l,v,true);
      var body=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
      h.assertTrue(EligibilityHooks.allows(body,l,WingExamples.sample(l,nativeJoin,1)),"Connected native provider join accepted");
      h.assertTrue(!EligibilityHooks.allows(body,l,WingExamples.sample(l,crossing,20)),"Unconnected geometric crossing rejected");
    });
    h.runAtTickTime(50,()->RideAuthority.dispatch(l,station,cars[0].chassis,RideAuthority.token(cars[0])));
    h.runAtTickTime(380,()->{
      var v=cars[0];var body=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);var pos=CoasterCartResolve.cartCenterWorld(body);
      h.assertTrue(v.fault.isEmpty()&&v.boundFrom.equals(c)&&v.boundTo.equals(d),"Actual travel crosses CTNB -> native -> CTNB without taking crossing: "+pos+" "+v.fault+" "+v.boundFrom);
      h.assertTrue((boolean)OptionalCalls.staticCall("dev.notzyvex.coasters_extras.net.SensorTracker","occupied",c,d,l.getGameTime()),"Real native travel activates downstream sensor");
      VehicleOperations.capture(l,v);System.out.println("FD811_JOINS PASS actualProviderTransitions=true crossingIsolation=true sensorArrival=true position="+pos);h.succeed();
    });
  }
}
