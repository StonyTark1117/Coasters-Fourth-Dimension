package dev.fourthdimension.gametest;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.tools.*;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.shuttle.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd11_routes")
@PrefixGameTestTemplate(false)
public final class ToolsRouteGameTests {
  @GameTest(template="empty",timeoutTicks=100)
  public static void profileExportDestinationAndRejection(GameTestHelper h){
    var l=h.getLevel();for(int x=30;x<=34;x++)for(int z=8;z<=14;z++)l.setChunkForced(x,z,true);
    var p=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FD11Route"));p.setGameMode(GameType.CREATIVE);
    var from=new BlockPos(512,100,160);var to=from.offset(0,0,32);
    for(var pos:List.of(from,to))l.setBlockAndUpdate(pos,dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    p.setPos(514,101,192);p.setItemInHand(InteractionHand.MAIN_HAND,TrackItems.ARROW.get().getDefaultInstance());
    var rail=TrackPlacement.connect(l,from,to,TrackDesign.ARROW,p,p.getMainHandItem());
    h.runAtTickTime(20,()->{var data=FoundationData.get(l);var saved=PresetService.capture(data,rail.id,false,"Arrow rotation",0);var preset=new SettingPreset(saved.name(),saved.family(),saved.variant(),saved.kind(),"0:0:0;32:360:0");
      EditorNetwork.apply(p,new EditorNetwork.Edit(rail.id,rail.revision,90,"BIND_SELECTED\n\n"+preset.exportText()));h.assertTrue(Math.abs(rail.profile.keys().getLast().angle()-2*Math.PI)<1e-8,"Profile not applied to destination");
      var before=data.save(new CompoundTag(),l.registryAccess());var bad=new SettingPreset(saved.name(),saved.family(),saved.variant(),saved.kind(),"0:0:0;33:360:0");
      EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(rail.id,rail.revision,90,"BIND_SELECTED\n\n"+bad.exportText())),"Wrong section length accepted");h.assertTrue(before.equals(data.save(new CompoundTag(),l.registryAccess())),"Rejected length mutated world");
      rail.valid=false;EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(rail.id,rail.revision,90,"BIND_SELECTED\n\n"+saved.exportText())),"Preset silently rebound changed geometry");rail.valid=true;
      EditorNetwork.apply(p,new EditorNetwork.Edit(rail.id,rail.revision,90,"BIND_SELECTED\n\n"+saved.exportText()));h.assertTrue(rail.profile.keys().getLast().angle()==0,"Profile round-trip failed");
      // Construct an authored TARGET interval only to inspect portability; no synthetic world claim.
      var route=new FoundationData.Rail(from,to,dev.fourthdimension.contract.Family.FLYING,rail.profile,UUID.randomUUID(),NewGenerationFlyingVehicle.VARIANT,0,0,true);
      route.yaw.intervals=List.of(new YawSection.Interval(UUID.randomUUID(),"View",0,32,YawState.Mode.TARGET,8,0,0,rail.profile,UUID.randomUUID(),new BlockPos(600,110,200),l.dimension().location().toString(),YawState.Winding.NEAREST,.6,.8,"Show",200));data.rails.add(route);
      var portable=PresetService.capture(data,route.id,false,"View",0);h.assertTrue(portable.settings().split("\\|",-1)[7].isEmpty(),"Export leaked world marker");
      before=data.save(new CompoundTag(),l.registryAccess());EditorGameTests.rejects(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(route.id,route.revision,90,"BIND_SELECTED\n\n"+portable.exportText())),"Missing marker binding accepted");h.assertTrue(before.equals(data.save(new CompoundTag(),l.registryAccess())),"Marker rejection mutated world");data.rails.remove(route);
      System.out.println("FD11_ROUTE profileRoundTrip=true wrongLengthAtomic=true staleGeometryRequiresRebind=true markerReferencesStripped=true explicitMarkerRequired=true");h.succeed();
    });
  }
}
