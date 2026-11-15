package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.vehicle.*;
import dev.fourthdimension.station.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd812_s2")
@PrefixGameTestTemplate(false)
public final class MagnetizedS2GameTests {
  @GameTest(template="empty",timeoutTicks=300)
  public static void optionalS2ProviderHandoff(GameTestHelper h){
    var l=h.getLevel();var rig=new S2SpinningGovernorGameTests.Rig[1];var station=new BlockPos[1];
    h.runAtTickTime(5,()->{
      rig[0]=S2SpinningGovernorGameTests.create(h,new BlockPos(800,100,600));var r=rig[0];
      station[0]=r.edge().from().offset(-4,1,8);l.setBlockAndUpdate(station[0],StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)l.getBlockEntity(station[0])).vehicle=r.car().chassis;S2SpinningRuntime.rebindDock(l,r.car(),station[0]);
      MagnetizedDriveGameTests.mode(l,r.edge().from(),"MAGNET");l.setBlockAndUpdate(r.edge().from().below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
    });
    h.runAtTickTime(40,()->rig[0].governor().bind(rig[0].car()));
    h.runAtTickTime(60,()->{
      var r=rig[0];h.assertTrue(Boarding.ready(l,r.car()),"S2 native latch before optional handoff: "+r.car().fault);
      RideAuthority.selectMagnetized(l,station[0],r.car());VehicleOperations.restraints(l,r.car(),true);
    });
    h.runAtTickTime(72,()->{
      var r=rig[0];RideAuthority.dispatch(l,station[0],r.car().chassis,RideAuthority.token(r.car()));
      var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(r.car().chassis);
      var hit=FlyingExamples.sample(l,r.edge(),8);var basis=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.trackBasisAtHit(l,hit);
      var before=c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d());
      CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,basis,.05);
      var after=c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d());
      h.assertTrue(after.z>before.z&&after.z-before.z<=.150001,"S2 actual optional ramp <=3 b/s²");
      h.assertTrue(r.governor().outputRpm()==0,"Previous governor lease cannot drive after handoff");
    });
    h.runAtTickTime(92,()->{
      var r=rig[0];var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(r.car().chassis);
      double speed=c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).length();
      System.out.println("FD812_S2 actualSpeedAfter20Ticks="+speed);
      h.assertTrue(speed>.2&&r.car().fault.isEmpty(),"Actual S2 optional travel, without standalone governor: "+speed+" "+r.car().fault);
      l.setBlockAndUpdate(r.edge().from().below(),Blocks.AIR.defaultBlockState());
    });
    h.runAtTickTime(95,()->{
      var r=rig[0];h.assertTrue(r.car().authority.held&&r.car().authority.suspended,"S2 power loss holds selected authority");
      l.setBlockAndUpdate(r.edge().from().below(),Blocks.REDSTONE_BLOCK.defaultBlockState());VehicleOperations.capture(l,r.car());
    });
    h.runAtTickTime(180,()->{
      var r=rig[0];h.assertTrue(Boarding.ready(l,r.car()),"Captured S2 aligned at same dock: "+r.car().fault);
      h.assertTrue(r.car().authority.suspended&&r.governor().outputRpm()==0,"Power restore retains explicit resume latch");
      RideAuthority.resume(l,station[0],r.car().chassis,RideAuthority.token(r.car()));
    });
    h.runAtTickTime(190,()->{
      var r=rig[0];RideAuthority.dispatch(l,station[0],r.car().chassis,RideAuthority.token(r.car()));
      h.assertTrue(!r.car().stopping&&!r.car().authority.held,"Explicit resume then validated dispatch restarts S2");
      VehicleOperations.capture(l,r.car());
      System.out.println("FD812_S2 PASS actualOptionalTravel=true boundedRamp=true governorLeaseZero=true powerLossHeld=true restoredPowerNoStart=true explicitResumeDispatch=true unchangedSpinAndDock=true");h.succeed();
    });
  }
}
