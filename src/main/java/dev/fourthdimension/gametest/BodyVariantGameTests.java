package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.runtime.FoundationSync;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.station.StationContent;
import dev.fourthdimension.tools.VehicleBody;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.track.TrackPlacement;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

/** Real native vehicles and the production editor transaction; no client/render claims. */
@GameTestHolder("fourthdimension_fd13")
@PrefixGameTestTemplate(false)
public final class BodyVariantGameTests {
  @GameTest(template="empty", timeoutTicks=240)
  public static void wingBodyTransactionsAndPersistence(GameTestHelper h) {
    checkNative(h, false);
  }

  @GameTest(template="empty", timeoutTicks=240)
  public static void fixedInvertedBodyTransactionsAndPersistence(GameTestHelper h) {
    checkNative(h, true);
  }

  private static void checkNative(GameTestHelper h, boolean inverted) {
    var level=h.getLevel();
    var from=new BlockPos(inverted ? -600 : 600,100,600);
    var to=from.offset(0,0,80);
    for(int x=(from.getX()>>4)-1;x<=(from.getX()>>4)+1;x++)
      for(int z=(from.getZ()>>4)-1;z<=(to.getZ()>>4)+1;z++) level.setChunkForced(x,z,true);
    for(var pos:List.of(from,to))
      level.setBlockAndUpdate(pos,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=WingExamples.connect(level,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var data=FoundationData.get(level);
    if(inverted) {
      var length=TrackGeometry.table(level,edge).length();
      var rail=new FoundationData.Rail(from,to,Family.FIXED_INVERTED,
          new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),
          UUID.randomUUID(),FixedInvertedVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
      data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
    }
    var car=new FoundationData.Vehicle[1];
    var rider=new Pig[1];
    var initial=new Vector3d();
    var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),inverted?"FD13Fixed":"FD13Wing"));
    player.setGameMode(GameType.CREATIVE);
    var choice=inverted?VehicleBody.FIXED_INVERTED_OPEN_FRAME:VehicleBody.WING_COMPACT;
    var wrong=inverted?VehicleBody.WING_COMPACT:VehicleBody.FIXED_INVERTED_OPEN_FRAME;

    h.runAtTickTime(10,()-> {
      car[0]=inverted?FixedInvertedVehicle.create(level,WingExamples.sample(level,edge,20))
          :WingVehicle.create(level,WingExamples.sample(level,edge,20));
      if(inverted) {
        var dock=from.offset(4,-3,20);
        level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
        ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=car[0].chassis;
        car[0].dock=dock;
      }
    });
    h.runAtTickTime(55,()-> {
      if(inverted) FixedInvertedRuntime.rebindDock(level,car[0],car[0].dock);
      h.assertTrue(Boarding.ready(level,car[0]),"Native fixture did not settle: "+car[0].fault);
    });
    h.runAtTickTime(70,()-> {
      var v=car[0];near(player,v);
      h.assertTrue(v.bodyId.equals("base")&&VehicleBody.resolve(v)==VehicleBody.BASE,"New native car is not base");
      h.assertTrue(VehicleBody.choices(v).equals(List.of(VehicleBody.BASE,choice)),"Wrong finite family roster");
      for(var selected:List.of(choice,VehicleBody.BASE,choice)) {
        var before=save(level);long revision=v.revision;
        apply(player,v,v.revision,selected.id);
        h.assertTrue(v.bodyId.equals(selected.id)&&v.revision==revision+1,"Body apply/revision failed");
        var after=save(level);
        h.assertTrue(withoutBodyRevision(before).equals(withoutBodyRevision(after)),"Body selection changed unrelated serialized state");
        assertRoundTrip(h,level,v,selected);
      }
      rejectAtomic(h,level,()->apply(player,v,v.revision,"missing_future_body"),"unknown new ID");
      rejectAtomic(h,level,()->apply(player,v,v.revision,wrong.id),"wrong-family choice");
      rejectAtomic(h,level,()->apply(player,v,v.revision-1,VehicleBody.BASE.id),"stale revision");
      player.setGameMode(GameType.ADVENTURE);
      rejectAtomic(h,level,()->apply(player,v,v.revision,VehicleBody.BASE.id),"permission");
      player.setGameMode(GameType.CREATIVE);
      assertSavedFallback(h,level,v,"missing_future_body");
      assertSavedFallback(h,level,v,wrong.id);
      var legacy=save(level);vehicleTag(legacy,v.chassis).remove("body");
      var migrated=FoundationData.load(legacy,level.registryAccess()).vehicles.get(v.chassis);
      h.assertTrue(migrated.bodyId.equals("base")&&VehicleBody.resolve(migrated)==VehicleBody.BASE,"Missing field did not default to base");
      rider[0]=EntityType.PIG.create(level);rider[0].setNoAi(true);level.addFreshEntity(rider[0]);
      var body=body(level,v);
      SeatBlock.sitDown(level,body.getPlot().getCenterBlock().offset(v.carriers.getFirst().seats.getFirst()),rider[0]);
      h.assertTrue(rider[0].isPassenger(),"Actual native seat mount failed");
    });
    h.runAtTickTime(90,()-> {
      var v=car[0];near(player,v);
      h.assertTrue(v.carriers.getFirst().occupants.contains(rider[0].getUUID()),"Native rider did not reach authoritative occupancy");
      rejectAtomic(h,level,()->apply(player,v,v.revision,VehicleBody.BASE.id),"occupied native seat");
      rider[0].stopRiding();rider[0].discard();
    });
    h.runAtTickTime(110,()-> {
      var v=car[0];near(player,v);
      h.assertTrue(v.carriers.stream().allMatch(c->c.occupants.isEmpty()&&c.riderSeats.isEmpty()),"Native rider cleanup incomplete");
      h.assertTrue(Boarding.ready(level,v),"Empty fixture no longer captured");
      EditorNetwork.apply(player,new EditorNetwork.Edit(v.chassis,v.revision,11,""));
      EditorNetwork.apply(player,new EditorNetwork.Edit(v.chassis,v.revision,13,""));
      var body=body(level,v);initial.set(CoasterCartResolve.cartCenterWorld(body));
      SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
          .addLinearAndAngularVelocity(body,new Vector3d(0,0,2),new Vector3d());
    });
    h.runAtTickTime(120,()-> {
      var v=car[0];near(player,v);var body=body(level,v);
      double speed=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length();
      double moved=CoasterCartResolve.cartCenterWorld(body).distance(initial);
      h.assertTrue(speed>.08&&moved>.05,"Native moving precondition absent: speed="+speed+" moved="+moved);
      h.assertTrue(!Boarding.ready(level,v),"Moving native body incorrectly ready");
      rejectAtomic(h,level,()->apply(player,v,v.revision,VehicleBody.BASE.id),"moving native body");
      System.out.println("FD13_BODY_MOVING family="+v.family.id()+" speed="+speed+" displacement="+moved+" rejectedAtomically=true");
      VehicleOperations.capture(level,v);
    });
    h.runAtTickTime(190,()-> {
      var v=car[0];h.assertTrue(Boarding.ready(level,v),"Native fixture did not stop cleanly");
      h.assertTrue(v.bodyId.equals(choice.id),"Denied edit changed accepted body");
      System.out.println("FD13_BODY_NATIVE PASS family="+v.family.id()+" actualNative=true actualOccupied=true actualMoving=true choices=2 saveLoad=true snapshotCodec=true missingDefault=true unknownAndWrongFamilyFallback=true unrelatedNbtUnchanged=true atomicDenials=unknown,wrongFamily,stale,permission,occupied,moving");
      h.succeed();
    });
  }

  private static ServerSubLevel body(ServerLevel level,FoundationData.Vehicle v) {
    return (ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
  }
  private static void near(ServerPlayer player,FoundationData.Vehicle v) {
    var p=CoasterCartResolve.cartCenterWorld(body(player.serverLevel(),v));player.setPos(p.x+2,p.y,p.z);
  }
  private static void apply(ServerPlayer p,FoundationData.Vehicle v,long revision,String id) {
    EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,revision,82,id));
  }
  private static CompoundTag save(ServerLevel level) {
    return FoundationData.get(level).save(new CompoundTag(),level.registryAccess());
  }
  private static CompoundTag vehicleTag(CompoundTag root,UUID id) {
    for(var raw:root.getList("vehicles",Tag.TAG_COMPOUND)) {
      var tag=(CompoundTag)raw;if(tag.getUUID("chassis").equals(id))return tag;
    }
    throw new IllegalArgumentException("Missing native vehicle "+id);
  }
  private static CompoundTag withoutBodyRevision(CompoundTag tag) {
    var copy=tag.copy();for(var raw:copy.getList("vehicles",Tag.TAG_COMPOUND)) {
      var v=(CompoundTag)raw;v.remove("body");v.remove("revision");
    }return copy;
  }
  private static void rejectAtomic(GameTestHelper h,ServerLevel level,Runnable action,String reason) {
    var before=save(level);boolean rejected=false;
    try {action.run();}catch(IllegalArgumentException expected){rejected=true;}
    h.assertTrue(rejected,"Body transaction accepted "+reason);
    h.assertTrue(before.equals(save(level)),"Rejected "+reason+" mutated serialized state");
  }
  private static void assertRoundTrip(GameTestHelper h,ServerLevel level,FoundationData.Vehicle v,VehicleBody choice) {
    var saved=save(level);var loaded=FoundationData.load(saved,level.registryAccess());
    h.assertTrue(loaded.vehicles.get(v.chassis).bodyId.equals(choice.id),"Saved body ID lost");
    h.assertTrue(vehicleTag(saved,v.chassis).equals(vehicleTag(loaded.save(new CompoundTag(),level.registryAccess()),v.chassis)),"Vehicle save/load changed unrelated NBT");
    var tag=FoundationData.get(level).snapshot(new CompoundTag(),level.registryAccess());
    var payload=new FoundationSync.Snapshot(level.dimension().location(),tag);
    var buf=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
    try {
      FoundationSync.Snapshot.CODEC.encode(buf,payload);
      var decoded=FoundationSync.Snapshot.CODEC.decode(buf);
      h.assertTrue(decoded.equals(payload)&&!buf.isReadable(),"Functional snapshot codec changed state");
      var copy=FoundationData.load(decoded.tag(),level.registryAccess());
      h.assertTrue(copy.vehicles.get(v.chassis).bodyId.equals(choice.id)&&VehicleBody.resolve(copy.vehicles.get(v.chassis))==choice,"Client snapshot lost choice");
    }finally{buf.release();}
  }
  private static void assertSavedFallback(GameTestHelper h,ServerLevel level,FoundationData.Vehicle v,String unavailable) {
    var saved=save(level);vehicleTag(saved,v.chassis).putString("body",unavailable);
    var loaded=FoundationData.load(saved,level.registryAccess());var copy=loaded.vehicles.get(v.chassis);
    var before=loaded.save(new CompoundTag(),level.registryAccess());
    h.assertTrue(copy.bodyId.equals(unavailable)&&VehicleBody.resolve(copy)==VehicleBody.BASE,"Unavailable persisted choice did not retain ID/fall back");
    var after=loaded.save(new CompoundTag(),level.registryAccess());
    h.assertTrue(before.equals(after),"Resolving unavailable body mutated state");
    h.assertTrue(vehicleTag(saved,v.chassis).equals(vehicleTag(after,v.chassis)),"Unavailable body load changed mechanical identity/state");
  }
}
