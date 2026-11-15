package dev.fourthdimension.gametest;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** One native cosmetic contract check; player interaction and render fit have separate evidence. */
@GameTestHolder("fourthdimension_fd910")
@PrefixGameTestTemplate(false)
public final class SkinAppearanceGameTests {
  @GameTest(template="empty",timeoutTicks=180)
  public static void guardedChoicesPersistenceAndNativeIsolation(GameTestHelper h) {
    var level=h.getLevel();var origin=new BlockPos(160,100,160);
    for(int x=9;x<=11;x++)for(int z=9;z<=14;z++)level.setChunkForced(x,z,true);
    var fixture=RecoveryExamples.create(level,origin,TrackDesign.ARROW);
    var vehicle=fixture.car();
    var player=FakePlayerFactory.getMinecraft(level);player.setGameMode(GameType.CREATIVE);
    h.runAtTickTime(100,()->{
      var body=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(vehicle.chassis);
      var center=CoasterCartResolve.cartCenterWorld(body);player.setPos(center.x+2,center.y,center.z);
      h.assertTrue(Boarding.ready(level,vehicle),"Cosmetic fixture must actually be stopped and captured");
      var data=FoundationData.get(level);var before=data.save(new CompoundTag(),level.registryAccess());
      var nativeTags=body.getUserDataTag()==null?null:body.getUserDataTag().copy();
      boolean provider=OptionalIntegrations.installed("create_coaster_skins").mayInspect();
      for(var choice:SkinAppearance.values()) {
        var edit=new EditorNetwork.Edit(vehicle.chassis,vehicle.revision,80,choice.id);
        if(provider||choice==SkinAppearance.ORIGINAL)EditorNetwork.apply(player,edit);
        else EditorGameTests.rejects(h,()->EditorNetwork.apply(player,edit),"Missing provider accepted appearance");
      }
      var saved=data.save(new CompoundTag(),level.registryAccess());
      var restored=FoundationData.load(saved,level.registryAccess()).vehicles.get(vehicle.chassis);
      h.assertTrue(restored.appearance==vehicle.appearance,"Saved appearance lost");
      h.assertTrue(restored.family==vehicle.family&&restored.variant.equals(vehicle.variant),"Mechanical identity changed");
      h.assertTrue(restored.carriers.stream().map(c->c.body).toList().equals(vehicle.carriers.stream().map(c->c.body).toList()),"Carrier ownership changed");
      for(int i=0;i<vehicle.carriers.size();i++)h.assertTrue(restored.carriers.get(i).seats.equals(vehicle.carriers.get(i).seats),"Seat geometry changed");
      h.assertTrue(Objects.equals(nativeTags,body.getUserDataTag()),"Appearance wrote provider physics tags");
      EditorGameTests.rejects(h,()->EditorNetwork.apply(player,new EditorNetwork.Edit(vehicle.chassis,vehicle.revision-1,80,"original")),"Stale appearance edit accepted");
      EditorGameTests.rejects(h,()->EditorNetwork.apply(player,new EditorNetwork.Edit(vehicle.chassis,vehicle.revision,80,"wildspin")),"Unsupported body/seat style accepted");
      var occupant=UUID.randomUUID();vehicle.carriers.getFirst().occupants=List.of(occupant);
      EditorGameTests.rejects(h,()->EditorNetwork.apply(player,new EditorNetwork.Edit(vehicle.chassis,vehicle.revision,80,"original")),"Occupied appearance edit accepted");
      vehicle.carriers.getFirst().occupants=List.of();
      if(provider) {
        h.assertTrue(!Boolean.TRUE.equals(OptionalCalls.staticCall("medev.coaster.skins.skin.CartSkinTargets","isCart",body)),"Native selector accepts owned chassis");
        h.assertTrue(Boolean.TRUE.equals(OptionalCalls.staticCall("medev.coaster.skins.skin.CartSkinTargets","isCart",fixture.bearing())),"Ordinary native cart was disabled");
        h.assertTrue(!Boolean.TRUE.equals(OptionalCalls.staticCall("medev.coaster.skins.skin.LinkedCartSkins","copy",fixture.bearing(),body)),"Native link mutated owned cart");
        var stale=new CompoundTag();stale.putString("create_coaster_skins:skin","create_coaster_skins:classic_coaster");stale.putString("create_coaster_skins:classic_style","wildspin");body.setUserDataTag(stale);
        h.assertTrue(OptionalCalls.staticCall("medev.coaster.skins.skin.CartSkinData","get",body)==SkinAdapter.nativeSkin(),"Stale native style activated owned physics");
        body.setUserDataTag(nativeTags);
      }
      // In the same tick, saved data differs only in the selected appearance and edit revision.
      var beforeCart=before.getList("vehicles",10);
      var afterCart=data.save(new CompoundTag(),level.registryAccess()).getList("vehicles",10);
      for(int i=0;i<beforeCart.size();i++) {var a=beforeCart.getCompound(i);var b=afterCart.getCompound(i);a.remove("appearance");b.remove("appearance");a.remove("revision");b.remove("revision");h.assertTrue(a.equals(b),"Appearance modified non-cosmetic saved state");}
      System.out.println("FD_SKIN_NATIVE PASS provider="+provider+" choices="+SkinAppearance.values().length+" identitySeatsRestraints=true staleOccupiedUnsupportedRejected=true persistence=true nativeTagsUnchanged=true");
      h.succeed();
    });
  }
}
