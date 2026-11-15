package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.RideAuthority;
import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd1501d_governor")
@PrefixGameTestTemplate(false)
public final class S2SpinningGovernorGameTests {
  public record Rig(CoasterPathEdge edge,FoundationData.Rail rail,FoundationData.Vehicle car,
      S2SpinningGovernorEntity governor,BlockPos motor,BlockPos station){}
  public static Rig create(GameTestHelper h){
    return create(h,new BlockPos(400,100,400));
  }
  public static Rig create(GameTestHelper h,BlockPos from){
    var l=h.getLevel();var to=from.offset(0,0,100);
    for(int x=(from.getX()>>4)-1;x<=(from.getX()>>4)+1;x++)for(int z=(from.getZ()>>4)-1;z<=(to.getZ()>>4)+1;z++)l.setChunkForced(x,z,true);
    for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=FlyingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var rail=new FoundationData.Rail(edge.from(),edge.to(),Family.SPINNING,
        new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(100,0))),UUID.randomUUID(),
        S2SpinningVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
    rail.magnets=List.of(new MagneticSpan(5,85,MagneticSpan.Mode.INDUCE,1,4,.1,1));
    TrackPlacement.captureOriginalCurve(rail,edge.bezier());FoundationData.get(l).rails.add(rail);
    var anchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(from);
    anchor.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(to),new CoasterPathGraphSpans.SpanPath(
        List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),TrackGeometry.table(l,edge).length()));
    for(int i=1;i<=3;i++)l.setBlockAndUpdate(from.east(i),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
    var governorPos=from.east(4);l.setBlockAndUpdate(governorPos,S2SpinningContent.GOVERNOR.get().defaultBlockState().setValue(S2SpinningGovernorBlock.FACING,Direction.WEST));
    var motor=from.east(5);l.setBlockAndUpdate(motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
    ((CreativeMotorBlockEntity)l.getBlockEntity(motor)).generatedSpeed.setValue(192);
    var v=S2SpinningVehicle.create(l,FlyingExamples.sample(l,edge,8));
    var station=from.offset(-4,1,24);l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());
    ((BoardingStationEntity)l.getBlockEntity(station)).vehicle=v.chassis;
    S2SpinningRuntime.rebindDock(l,v,station);
    return new Rig(edge,rail,v,(S2SpinningGovernorEntity)l.getBlockEntity(governorPos),motor,station);
  }
  @GameTest(template="empty",timeoutTicks=1400)
  public static void nativeStressSuppliedLaunchAndPowerLoss(GameTestHelper h){
    var l=h.getLevel();var rig=new Rig[1];double[] measured={0,0,0,0,0};
    h.runAtTickTime(5,()->rig[0]=create(h));
    h.runAtTickTime(30,()->{
      var r=rig[0];h.assertTrue(r.governor.availability()==null||r.governor.anchor==null,"Input power/shaft availability");
      r.governor.bind(r.car);h.assertTrue(Math.abs(r.governor.inputRpm())>=192,"Real input RPM supplied");
      h.assertTrue(r.governor.getGeneratedSpeed()==0&&r.governor.calculateStressApplied()==8,"Governor consumes stress and generates no capacity");
    });
    h.runAtTickTime(50,()->{
      var r=rig[0];h.assertTrue(Boarding.ready(l,r.car),"Initial actual dock latch: "+r.car.fault);
      var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(r.car.carriers.getFirst().body);
      measured[3]=body.getMassTracker().getMass();var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);
      SeatBlock.sitDown(l,body.getPlot().getCenterBlock().offset(r.car.carriers.getFirst().seats.getFirst()),pig);
    });
    h.runAtTickTime(65,()->{
      var r=rig[0];var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(r.car.carriers.getFirst().body);
      h.assertTrue(Math.abs(body.getMassTracker().getMass()-measured[3]-.8)<1e-5,"Off-center native rider mass exactly .8");
      VehicleOperations.restraints(l,r.car,true);VehicleOperations.dispatch(l,r.car);
      h.assertTrue(!RideAuthority.holds(l,r.car),"Normal dispatch releases governor authority");
    });
    for(int t=66;t<=158;t++){final int tick=t;h.runAtTickTime(t,()->{
      var r=rig[0];var parent=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(r.car.chassis);
      double speed=SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).z;
      double source=r.governor.actualBps();
      measured[0]=Math.max(measured[0],speed);
      if(tick>67)measured[1]=Math.max(measured[1],Math.abs(source-measured[2])*20);
      measured[2]=source;
      if(tick%10==0)System.out.println("FD_S2_LAUNCH tick="+tick+" nativeZ="+speed+" "+r.governor.status()+" fault="+r.car.fault+" psi="+r.car.carriers.getFirst().angle+" live="+(l.getBlockEntity(r.governor.getBlockPos())==r.governor)+" flicker="+r.governor.getFlickerScore());
    });}
    h.runAtTickTime(159,()->{
      var r=rig[0];h.assertTrue(r.car.fault.isEmpty(),"Valid launch remains authorized: "+r.car.fault);
      h.assertTrue(Math.abs(r.governor.actualBps())>11.8,"Actual Chain source reaches12 b/s: "+r.governor.status());
      h.assertTrue(measured[0]>10&&measured[0]<12.8,"Real native longitudinal launch: "+measured[0]);
      h.assertTrue(measured[1]<=3.05,"Actual source acceleration bounded: "+measured[1]);
      var c=r.car.carriers.getFirst();measured[4]=c.angle;
      h.assertTrue(Math.abs(c.angle)>.01&&S2SpinningMagnet.audit((ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body)).impulses>0,"Native Y spin responds to independent magnet");
      var saved=FoundationData.get(l).save(new CompoundTag(),l.registryAccess());var restored=FoundationData.load(saved,l.registryAccess()).vehicles.get(r.car.chassis);
      h.assertTrue(restored.s2.rampSpeed>10&&restored.s2.governor.equals(r.governor.getBlockPos())&&Math.abs(restored.carriers.getFirst().angle-c.angle)<1e-9,"Pending launch and exact winding persist");
      l.removeBlock(r.motor,false);
    });
    h.runAtTickTime(185,()->{
      var r=rig[0];h.assertTrue(r.car.stopping&&r.car.restraints&&!r.car.fault.isEmpty()&&RideAuthority.holds(l,r.car),"Power loss retains train and drive hold");
      h.assertTrue(Math.abs(r.governor.actualBps())<.001,"Power loss produces no magic Chain speed");
      h.assertTrue(!Boarding.ready(l,r.car),"Interrupted rotating train cannot unload");
      l.setBlockAndUpdate(r.motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
      ((CreativeMotorBlockEntity)l.getBlockEntity(r.motor)).generatedSpeed.setValue(192);
    });
    h.runAtTickTime(220,()->{
      var r=rig[0];h.assertTrue(r.car.stopping&&RideAuthority.holds(l,r.car),"Power restoration never auto launches");
      S2SpinningRuntime.capture(l,r.car);
    });
    h.succeedWhen(()->{
      h.assertTrue(h.getTick()>230,"Wait for power-loss recovery");var r=rig[0];
      h.assertTrue(Boarding.ready(l,r.car),"Bounded captured return: "+r.car.fault+" angle="+r.car.carriers.getFirst().angle);
      VehicleOperations.restraints(l,r.car,false);
      System.out.println("FD_S2_GOVERNOR PASS actualNativeSpeed="+measured[0]+" actualSourceAcceleration="+measured[1]+" oneNativeRider=true magneticY=true powerLossHold=true noAutoRestart=true explicitCapture=true");
    });
  }
}
