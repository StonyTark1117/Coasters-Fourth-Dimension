package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import dev.fourthdimension.station.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd810")
@PrefixGameTestTemplate(false)
public final class AdditionsStationGameTests {
  @GameTest(template="empty",timeoutTicks=260)
  public static void wholeConsistSignalsAndValidatedAutomaticDispatch(GameTestHelper h) {
    var l=h.getLevel();var from=new BlockPos(1600,100,800);var to=from.offset(0,0,100);
    for(int x=99;x<=101;x++)for(int z=49;z<=57;z++)l.setChunkForced(x,z,true);
    h.assertTrue(OptionalIntegrations.installed(OptionalIntegrations.ADDITIONS).mayInspect(),"Exact fingerprinted Additions closure loads");
    for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=WingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var pos=from.offset(8,0,20);var cars=new FoundationData.Vehicle[2];var station=new BlockEntity[1];
    h.runAtTickTime(10,()->{
      cars[0]=WingVehicle.create(l,WingExamples.sample(l,edge,20));
      cars[1]=WingVehicle.create(l,WingExamples.sample(l,edge,26));
      var block=BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("coasterfins","station"));
      l.setBlockAndUpdate(pos,block.defaultBlockState());station[0]=l.getBlockEntity(pos);
      h.assertTrue(station[0] instanceof AdditionsStationAdapter.Bridge,"Actual optional target mixed in");
      setSpan(station[0],from,to,.1,.4);
      var frequencies=List.of(net.minecraft.world.item.Items.STONE,net.minecraft.world.item.Items.DIRT,net.minecraft.world.item.Items.IRON_INGOT,net.minecraft.world.item.Items.GOLD_INGOT);
      for(int i=0;i<4;i++)OptionalCalls.call(station[0],"setFrequency",i,0,new net.minecraft.world.item.ItemStack(frequencies.get(i)));
    });
    h.runAtTickTime(40,()->{
      h.assertTrue(dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint.linkCarts(l,
          (ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(cars[0].chassis),(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(cars[1].chassis),6),"Native coupling");
    });
    h.runAtTickTime(50,()->{
      var s=((AdditionsStationAdapter.Bridge)station[0]).fourthdimension$stationState();
      AdditionsStationAdapter.update(station[0],s,false);
      h.assertTrue(s.telemetry.available()&&s.telemetry.cars()==2&&s.telemetry.capacity()==8,"Whole native consist telemetry");
      h.assertTrue(power(station[0],0)==15&&power(station[0],2)==15,"Occupied and bars-up signals");
      var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(cars[1].chassis);
      var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);
      SeatBlock.sitDown(l,body.getPlot().getCenterBlock().offset(cars[1].carriers.getFirst().seats.getFirst()),pig);
      OptionalCalls.call(station[0],"setDispatch",true,1,0,0);
    });
    h.runAtTickTime(65,()->{
      var s=((AdditionsStationAdapter.Bridge)station[0]).fourthdimension$stationState();
      h.assertTrue(cars[0].stopping&&cars[1].stopping,"Open restraints deny automatic dispatch");
      h.assertTrue(s.telemetry.occupied()==1&&power(station[0],1)==1,"Real rider counted once across whole train");
      VehicleOperations.restraints(l,cars[0],true);
    });
    h.runAtTickTime(80,()->{
      var s=((AdditionsStationAdapter.Bridge)station[0]).fourthdimension$stationState();
      h.assertTrue(!cars[0].stopping&&!cars[1].stopping&&s.dispatched,"Actual adapter automatic request dispatches whole train");
      h.assertTrue(power(station[0],2)==0,"Closed bars signal is low");
      VehicleOperations.capture(l,cars[0]);
      setSpan(station[0],from,to,.15,.24);
    });
    h.runAtTickTime(100,()->{
      var s=((AdditionsStationAdapter.Bridge)station[0]).fourthdimension$stationState();
      h.assertTrue(!s.telemetry.available()&&!((AdditionsStationAdapter.Bridge)station[0]).authorizesRide(cars[0].chassis),"Partial consist denies ownership");
      var saved=station[0].saveWithoutMetadata(l.registryAccess());
      AdditionsStationAdapter.configure(s,true);s.managed=false;station[0].loadWithComponents(saved,l.registryAccess());
      h.assertTrue(!s.armed&&s.managed,"Reload preserves managed guard without resuming auto dispatch");
      setSpan(station[0],from,to,.1,.4);OptionalCalls.call(station[0],"setDispatch",false,1,0,0);
    });
    h.runAtTickTime(120,()->{
      var other=from.offset(-6,0,20);l.setBlockAndUpdate(other,StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)l.getBlockEntity(other)).vehicle=cars[0].chassis;
      RideAuthority.claim(l,other,cars[0].chassis,RideAuthority.NATIVE_CHAIN);
      OptionalCalls.call(station[0],"setDispatch",true,1,0,0);
    });
    h.runAtTickTime(140,()->{
      var s=((AdditionsStationAdapter.Bridge)station[0]).fourthdimension$stationState();
      h.assertTrue(cars[0].stopping&&cars[1].stopping&&!s.armed&&s.fault.contains("Another controller"),"Real simultaneous controller conflict denied");
      h.assertTrue(s.telemetry.available()&&s.telemetry.occupied()==1&&s.telemetry.restraintsClosed(),"Foreign owner does not falsify truthful read-only telemetry");
      s.sampled=l.getGameTime()-1;h.assertTrue(!((AdditionsStationAdapter.Bridge)station[0]).authorizesRide(cars[0].chassis),"Stale snapshot cannot authorize");
      AdditionsStationAdapter.configure(s,false);
      System.out.println("FD810_NATIVE PASS fingerprint=true wholeTwoCar=true riderCount=true restraintPolarity=true automaticRequest=true partialDenied=true ownerConflict=true staleDenied=true");h.succeed();
    });
  }
  public static void setSpan(Object station,BlockPos from,BlockPos to,double lo,double hi) {
    try {var type=Class.forName("dev.plushivr.coasterfins.logic.FinSpanSegment");
      var segment=type.getConstructor(BlockPos.class,BlockPos.class,double.class,double.class,boolean.class).newInstance(from,to,lo,hi,true);
      OptionalCalls.call(station,"setSpan",List.of(segment));
    } catch(ReflectiveOperationException failure){throw new IllegalArgumentException(failure);}
  }
  private static int power(Object station,int channel){return ((Number)OptionalCalls.call(OptionalCalls.call(station,"channel",channel),"power")).intValue();}
}
