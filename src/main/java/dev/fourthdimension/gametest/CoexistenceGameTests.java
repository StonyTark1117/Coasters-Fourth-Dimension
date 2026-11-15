package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartRailSnapEligibility;
import dev.silvergold.simulatedcoasters.track.graph.*;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd913")
@PrefixGameTestTemplate(false)
public final class CoexistenceGameTests {
  @GameTest(template="empty",timeoutTicks=180)
  public static void sharedGraphAndSeparateUtilityAuthority(GameTestHelper h){
    var l=h.getLevel();var f=CoexistenceFixture.create(l,new BlockPos(0,100,0));
    var v=f.core().vehicle();var container=SubLevelContainer.getContainer(l);
    h.runAtTickTime(30,()->{
      var hit=WingExamples.sample(l,f.slide(),12);
      h.assertTrue(CoasterPathGraphManager.get(l).edges().stream().anyMatch(e->e.from().equals(f.slide().from())&&e.to().equals(f.slide().to())),"Native shared graph includes actual waterslide");
      h.assertTrue(CoasterCartRailSnapEligibility.filterHitForBearing(container.getSubLevel(v.chassis),l,hit)==null,"FD native snap refuses waterslide");
      for(var design:TrackDesign.values())try{VehicleFactory.create(l,design,hit);h.fail("Factory accepted slide for "+design);}catch(IllegalArgumentException expected){}
      try{TrackPlacement.preview(l,f.slide().from(),f.slide().from().offset(0,0,-20));h.fail("FD track accepted slide anchor");}catch(IllegalArgumentException expected){}
      var nativeContraption=((MechanicalBearingBlockEntity)l.getBlockEntity(f.bearing())).getMovedContraption();
      h.assertTrue(nativeContraption!=null,"Real Create bearing contraption assembled");
      l.setBlockAndUpdate(f.lock().east(),Blocks.REDSTONE_BLOCK.defaultBlockState());
    });
    h.runAtTickTime(45,()->{
      var e=((MechanicalBearingBlockEntity)l.getBlockEntity(f.bearing())).getMovedContraption();
      var colors=(java.util.List<?>)OptionalCalls.call(e,"getDisabledColors");h.assertTrue(colors.size()==16,"Powered native utility locks Create seats");
      h.assertTrue(v.authority.owner==null&&!v.restraints&&v.fault.isEmpty(),"Utility cannot claim or restrain Sable ride");
      l.setBlockAndUpdate(f.lock().east(),Blocks.AIR.defaultBlockState());
    });
    h.runAtTickTime(60,()->{
      var e=((MechanicalBearingBlockEntity)l.getBlockEntity(f.bearing())).getMovedContraption();
      h.assertTrue(((java.util.List<?>)OptionalCalls.call(e,"getDisabledColors")).isEmpty(),"Unpowered native utility unlocks Create seats");
      h.assertTrue(WingTrack.rejection(l,f.core().edges().getFirst())==null&&v.fault.isEmpty(),"Core ordinary graph remains independent");
      System.out.println("FD913_914 shared slide graph; native snap/factory/anchor rejection; real Create lock/unlock; native Sable authority unchanged");h.succeed();
    });
  }
}
