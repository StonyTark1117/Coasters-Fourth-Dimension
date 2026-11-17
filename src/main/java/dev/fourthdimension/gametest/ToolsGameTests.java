package dev.fourthdimension.gametest;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.tools.*;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd11")
@PrefixGameTestTemplate(false)
public final class ToolsGameTests {
  private static void near(ServerPlayer player,FoundationData.Vehicle v){var body=SubLevelContainer.getContainer(player.level()).getSubLevel(v.chassis);var p=CoasterCartResolve.cartCenterWorld(body);player.setPos(p.x+2,p.y,p.z);}
  private static void force(net.minecraft.server.level.ServerLevel l,int x){for(int a=(x-16)/16;a<=(x+16)/16;a++)for(int z=9;z<=14;z++)l.setChunkForced(a,z,true);}
  @GameTest(template="empty",timeoutTicks=180)
  public static void palettePersistenceIsolationAndAuthoritativePanel(GameTestHelper h){
    var l=h.getLevel();force(l,160);var fixture=RecoveryExamples.create(l,new BlockPos(160,100,160),TrackDesign.ARROW);var v=fixture.car();
    var p=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FD11Palette"));p.setGameMode(GameType.CREATIVE);
    h.runAtTickTime(100,()->{near(p,v);var data=FoundationData.get(l);h.assertTrue(Boarding.ready(l,v),"Fixture not captured");
      var before=data.save(new CompoundTag(),l.registryAccess());
      for(var choice:VehiclePalette.values()){EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,81,choice.name()));h.assertTrue(FoundationData.load(data.save(new CompoundTag(),l.registryAccess()),l.registryAccess()).vehicles.get(v.chassis).palette==choice,"Palette failed reopen");}
      var after=data.save(new CompoundTag(),l.registryAccess());var a=before.getList("vehicles",10);var b=after.getList("vehicles",10);
      for(int i=0;i<a.size();i++){var old=a.getCompound(i);var next=b.getCompound(i);old.remove("palette");next.remove("palette");old.remove("revision");next.remove("revision");h.assertTrue(old.equals(next),"Palette changed mechanics or rider state");}
      EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision-1,81,"OCEAN")),"Stale palette accepted");
      v.carriers.getFirst().occupants=List.of(UUID.randomUUID());
      EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,81,"OCEAN")),"Occupied palette accepted");
      var sample=OperatorPanel.sample(p,v.chassis);h.assertTrue(sample.getInt("occupied")==1,"Panel lost whole-consist occupancy");v.carriers.getFirst().occupants=List.of();
      p.setGameMode(GameType.ADVENTURE);EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,81,"OCEAN")),"No-build palette accepted");p.setGameMode(GameType.CREATIVE);
      var telemetry=RideTelemetry.load(OperatorPanel.sample(p,v.chassis));h.assertTrue(telemetry.equals(RideTelemetry.sample(l,v.chassis)),"Panel disagrees with source telemetry");
      EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,11,""));h.assertTrue(v.restraints,"Panel command failed");
      v.fault="Matching track support lost";h.assertTrue(!OperatorPanel.sample(p,v.chassis).getBoolean("dispatch"),"Fault panel advertises dispatch");
      EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,13,"")),"Fault dispatch bypass");
      System.out.println("FD11_NATIVE paletteChoices=5 persistence=true mechanicsUnchanged=true staleOccupiedPermissionRejected=true panelSourceAgreement=true dispatchGuard=true");h.succeed();
    });
  }
  @GameTest(template="empty",timeoutTicks=180)
  public static void presetTransactionsRejectWithoutMutation(GameTestHelper h){
    var l=h.getLevel();force(l,320);var fixture=RecoveryExamples.create(l,new BlockPos(320,100,160),TrackDesign.FREE_SPIN);var v=fixture.car();
    var p=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FD11Preset"));p.setGameMode(GameType.CREATIVE);
    h.runAtTickTime(100,()->{near(p,v);var data=FoundationData.get(l);var saved=PresetService.capture(data,v.chassis,true,"Free spin",0);var changed=new SettingPreset(saved.name(),saved.family(),saved.variant(),saved.kind(),"20,80");
      EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,90,"BIND_SELECTED\n\n"+changed.exportText()));h.assertTrue(v.damping==20&&v.ballast==80,"Tuning preset not applied");
      for(String invalid:List.of("CONFIRM\n\n"+saved.exportText(),"BIND_SELECTED\n\n"+new SettingPreset(saved.name(),saved.family(),saved.variant(),saved.kind(),"NaN,80").exportText(),"BIND_SELECTED\n\n"+new SettingPreset(saved.name(),"zacspin_legacy",saved.variant(),saved.kind(),"20,80").exportText())){
        var before=data.save(new CompoundTag(),l.registryAccess());EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,90,invalid)),"Invalid preset accepted");h.assertTrue(before.equals(data.save(new CompoundTag(),l.registryAccess())),"Rejected preset mutated state");
      }
      EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision-1,90,"BIND_SELECTED\n\n"+saved.exportText())),"Stale preset accepted");
      v.carriers.getFirst().occupants=List.of(UUID.randomUUID());EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,90,"BIND_SELECTED\n\n"+saved.exportText())),"Occupied preset accepted");v.carriers.getFirst().occupants=List.of();
      p.setGameMode(GameType.ADVENTURE);EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,90,"BIND_SELECTED\n\n"+saved.exportText())),"No-build preset accepted");p.setGameMode(GameType.CREATIVE);
      EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,90,"BIND_SELECTED\n\n"+saved.exportText()));h.assertTrue(v.damping==Double.parseDouble(saved.settings().split(",")[0]),"Round trip did not restore tuning");
      System.out.println("FD11_NATIVE tuningRoundTrip=true explicitBinding=true incompatibleNaNStaleOccupiedPermissionRejected=true atomicRejection=true");h.succeed();
    });
  }
}
