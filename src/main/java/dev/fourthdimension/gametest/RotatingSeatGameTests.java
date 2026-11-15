package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** Changed optional attachment/lock/removal path only; normal real-player sync is separate. */
@GameTestHolder("fourthdimension_fd911")
@PrefixGameTestTemplate(false)
public final class RotatingSeatGameTests {
  @GameTest(template="empty",timeoutTicks=220)
  public static void nativeOptionalSeatRetainsOneOwner(GameTestHelper h) {
    var l=h.getLevel();var from=new BlockPos(600,120,900);var to=from.offset(0,0,40);
    for(int x=36;x<=39;x++)for(int z=55;z<=59;z++)l.setChunkForced(x,z,true);
    h.assertTrue(RotatingSeatIntegration.enabled(),"Inspected Additions seat release");
    var player=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.fromString("fdfdfdfd-0000-0000-0000-000000000911"),"FDSeatFixture"));
    player.setGameMode(GameType.CREATIVE);player.setPos(to.getX()+2,to.getY()+1,to.getZ());
    player.setItemInHand(InteractionHand.MAIN_HAND,TrackItems.ARROW.get().getDefaultInstance());
    for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var rail=TrackPlacement.connect(l,from,to,TrackDesign.ARROW,player,player.getMainHandItem());
    var vehicle=new FoundationData.Vehicle[1];var child=new ServerSubLevel[1];var pos=new BlockPos[1];var seatBe=new BlockEntity[1];var rider=new Pig[1];
    h.runAtTickTime(20,()->{
      var edge=TrackGeometry.resolve(l,rail);
      vehicle[0]=VehicleFactory.create(l,TrackDesign.ARROW,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(l,edge,.5),edge,.5));
      var c=vehicle[0].carriers.getFirst();child[0]=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
      pos[0]=child[0].getPlot().getCenterBlock().offset(c.seats.getFirst());
      var original=l.getBlockState(pos[0]);
      var block=BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("coasterfins","red_coaster_seat"));
      l.setBlock(pos[0],block.defaultBlockState(),3);seatBe[0]=l.getBlockEntity(pos[0]);
      h.assertTrue(seatBe[0] instanceof RotatingSeatIntegration.Bridge,"Actual optional BE transformed");
      ((RotatingSeatIntegration.Bridge)seatBe[0]).fourthdimension$originalSeat(original);
      var saved=RotatingSeatData.get(l);saved.originals.put(new RotatingSeatData.Key(c.body,c.seats.getFirst()),original);saved.setDirty();
      RotatingSeatIntegration.project(l,pos[0],false);
    });
    h.runAtTickTime(50,()->{
      h.assertTrue(Boarding.ready(l,vehicle[0]),"Native carrier stationary and aligned");
      rider[0]=EntityType.PIG.create(l);rider[0].setNoAi(true);l.addFreshEntity(rider[0]);
      SeatBlock.sitDown(l,pos[0],rider[0]);
      h.assertTrue(rider[0].isPassenger()&&RotatingSeatIntegration.ownsSeat(rider[0].getVehicle()),"One native mount and rebuilt optional attachment");
      VehicleOperations.restraints(l,vehicle[0],true);
      RotatingSeatIntegration.project(l,pos[0],true);
    });
    h.runAtTickTime(65,()->{
      h.assertTrue(vehicle[0].carriers.getFirst().occupants.equals(List.of(rider[0].getUUID())),"Rider counted once");
      h.assertTrue(Boolean.TRUE.equals(OptionalCalls.call(seatBe[0],"isBarDown")),"Actual optional bar follows authoritative restraint");
      rider[0].stopRiding();h.assertTrue(rider[0].isPassenger(),"Locked ordinary dismount denied by FD");
      var saved=seatBe[0].saveWithoutMetadata(l.registryAccess());seatBe[0].loadWithComponents(saved,l.registryAccess());
      h.assertTrue(RotatingSeatIntegration.managed(l,pos[0]),"Original native state survives actual BE serialization");
      var original=RotatingSeatData.get(l).save(new CompoundTag(),l.registryAccess());
      h.assertTrue(RotatingSeatData.load(original,l.registryAccess()).originals.size()==1,"Fallback remains independent of optional BE");
      // The native Additions veto must not keep this locked passenger attached during recovery.
      Boarding.recover(l,vehicle[0]);
      h.assertTrue(!rider[0].isPassenger(),"Forced recovery clears locked Additions mount");
      h.assertTrue(vehicle[0].carriers.getFirst().riderSeats.isEmpty(),"Recovery clears rider assignments");
    });
    h.runAtTickTime(80,()->{
      h.assertTrue(vehicle[0].carriers.getFirst().occupants.isEmpty(),"Native loading releases rider mass/count");
      l.setBlock(pos[0],net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
    });
    h.runAtTickTime(95,()->{
      h.assertTrue(l.getBlockState(pos[0]).getBlock()==VehicleParts.ARROW_SEAT.get(),"Missing optional block restores exact native seat");
      h.assertTrue(RotatingSeatData.get(l).originals.isEmpty(),"Consumed fallback does not overwrite subsequent edits");
      System.out.println("FD911_NATIVE PASS actualOptionalSeat=true attachment=true countOnce=true lockDenied=true serialization=true forcedRecovery=true missingBlockFallback=true");
      h.succeed();
    });
  }
}
