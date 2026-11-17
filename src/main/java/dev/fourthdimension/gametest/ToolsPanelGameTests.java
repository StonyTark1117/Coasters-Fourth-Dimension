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

@GameTestHolder("fourthdimension_fd11_panel")
@PrefixGameTestTemplate(false)
public final class ToolsPanelGameTests {
  @GameTest(template="empty",timeoutTicks=220)
  public static void shuttlePanelUsesCompleteBoundTrain(GameTestHelper h){
    var l=h.getLevel();for(int x=-2;x<=3;x++)for(int z=12;z<=24;z++)l.setChunkForced(x,z,true);
    var example=ShuttleExamples.create(l,new BlockPos(0,100,256),false);
    h.runAtTickTime(80,()->ShuttleExamples.assemble(l,example));
    h.runAtTickTime(120,()->{
      var p=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FD11Pax"));p.setGameMode(GameType.CREATIVE);var v=example.cars().getFirst();
      var body=dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(l).getSubLevel(v.chassis);var center=dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(body);p.setPos(center.x+2,center.y,center.z);
      var sample=OperatorPanel.sample(p,v.chassis);var expected=RideTelemetry.atController(l,example.winch().getBlockPos(),v.chassis);
      h.assertTrue(RideTelemetry.load(sample).equals(expected),"PAX panel differs from winch source");h.assertTrue(sample.getInt("cars")==4&&sample.getInt("capacity")==16,"PAX panel lost linked cars/seats");h.assertTrue(sample.getString("power").contains("RPM"),"PAX power missing");
      EditorGameTests.rejects(h,()->OperatorPanel.shuttle(p,v.chassis,v.revision-1,false),"Stale shuttle panel accepted");
      System.out.println("FD11_PANEL paxSourceAgreement=true cars=4 seats=16 power=true staleRequestRejected=true");h.succeed();
    });
  }
}
