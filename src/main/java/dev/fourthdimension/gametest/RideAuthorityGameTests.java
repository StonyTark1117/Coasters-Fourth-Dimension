package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd1701a")
@PrefixGameTestTemplate(false)
public final class RideAuthorityGameTests {
  @GameTest(template="empty",timeoutTicks=180)
  public static void ownershipTransferLossAndRestore(GameTestHelper h) {
    var l=h.getLevel();var from=new BlockPos(1600,100,400);var to=from.offset(0,0,80);
    for(int x=99;x<=101;x++)for(int z=24;z<=31;z++)l.setChunkForced(x,z,true);
    for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=WingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var first=from.offset(6,0,20);var second=from.offset(6,0,21);
    var car=new FoundationData.Vehicle[1];var token=new RideAuthority.Token[2];
    h.runAtTickTime(10,()->{
      car[0]=WingVehicle.create(l,WingExamples.sample(l,edge,20));
      for(var p:List.of(first,second)){l.setBlockAndUpdate(p,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(p)).vehicle=car[0].chassis;}
    });
    h.runAtTickTime(50,()->{
      var v=car[0];h.assertTrue(Boarding.ready(l,v),"Representative fixed car settled");
      token[0]=RideAuthority.claim(l,first,v.chassis,RideAuthority.NATIVE_CHAIN);
      denied(h,()->RideAuthority.claim(l,second,v.chassis,RideAuthority.ENGINEERED),"second owner");
      denied(h,()->RideAuthority.release(l,first,v.chassis,token[0]),"open restraints");
      VehicleOperations.restraints(l,v,true);
    });
    h.runAtTickTime(58,()->{
      var v=car[0];token[1]=RideAuthority.transfer(l,first,v.chassis,token[0],second);
      denied(h,()->RideAuthority.release(l,first,v.chassis,token[0]),"stale transfer token");
      h.assertTrue(RideAuthority.holds(l,v),"Transfer retained native braking");
    });
    h.runAtTickTime(66,()->{
      var v=car[0];RideAuthority.dispatch(l,second,v.chassis,token[1]);
      h.assertTrue(!RideAuthority.deniesProvider(l,v,RideAuthority.NATIVE_CHAIN),"Selected native drive permitted");
      h.assertTrue(RideAuthority.deniesProvider(l,v,RideAuthority.ENGINEERED),"Competing drive denied");
      denied(h,()->RideAuthority.release(l,second,v.chassis,token[1]),"replayed release");
      VehicleOperations.capture(l,v);
    });
    h.runAtTickTime(90,()->{
      var v=car[0];var old=RideAuthority.token(v);
      var saved=FoundationData.get(l).save(new CompoundTag(),l.registryAccess());
      h.assertTrue(saved.getInt("schema")==8,"Authority saves have downgrade-refusing schema");
      v.authority=FoundationData.load(saved,l.registryAccess()).vehicles.get(v.chassis).authority;
      h.assertTrue(RideAuthority.holds(l,v)&&v.authority.suspended,"Saved authority requires explicit resume");
      denied(h,()->RideAuthority.release(l,second,v.chassis,old),"pre-reload token");
      token[1]=RideAuthority.resume(l,second,v.chassis,RideAuthority.token(v));
      h.assertTrue(RideAuthority.holds(l,v),"Resume does not dispatch");
    });
    h.runAtTickTime(100,()->{
      var v=car[0];RideAuthority.dispatch(l,second,v.chassis,token[1]);
      l.setBlockAndUpdate(second,Blocks.AIR.defaultBlockState());
      var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
      h.assertTrue(PropulsionInterlock.holds(body,l)&&v.authority.suspended,"Missing controller denies force immediately");
      VehicleOperations.capture(l,v);
    });
    h.runAtTickTime(125,()->{
      var v=car[0];token[0]=RideAuthority.transfer(l,second,v.chassis,RideAuthority.token(v),first);
      h.assertTrue(RideAuthority.holds(l,v),"Explicit replacement transfer remains held");
      var raw=FoundationData.get(l).save(new CompoundTag(),l.registryAccess());raw.putInt("schema",7);
      for(var t:raw.getList("vehicles",net.minecraft.nbt.Tag.TAG_COMPOUND))((CompoundTag)t).remove("authority");
      h.assertTrue(FoundationData.load(raw,l.registryAccess()).vehicles.get(v.chassis).authority.owner==null,"Schema7 upgrades without inventing ownership");
      System.out.println("FD1701A_NATIVE PASS exclusiveOwner=true heldTransfer=true staleDenied=true replayDenied=true providerExclusive=true savedHold=true explicitResume=true immediateLossHold=true replacementTransfer=true schema7Upgrade=true");
      h.succeed();
    });
  }
  private static void denied(GameTestHelper h,Runnable action,String name){
    try{action.run();}catch(IllegalArgumentException expected){return;}h.fail("Accepted "+name);
  }
}
