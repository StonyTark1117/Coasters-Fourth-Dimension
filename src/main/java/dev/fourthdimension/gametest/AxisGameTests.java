package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Native physics probe; entity mounting and mode changes here are disclosed fixture inputs. */
@GameTestHolder("fourthdimension_fd1302")
@PrefixGameTestTemplate(false)
public final class AxisGameTests {
 @GameTest(template="empty",timeoutTicks=1450)
 public static void nativeRollLoadAndPowerRecovery(GameTestHelper h){
  var l=h.getLevel();var from=new BlockPos(-600,100,-600);var to=from.offset(0,0,128);
  for(int x=-39;x<=-36;x++)for(int z=-39;z<=-28;z++)l.setChunkForced(x,z,true);
  for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=SpinningExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var data=FoundationData.get(l);double length=TrackGeometry.table(l,edge).length();
  var rail=new FoundationData.Rail(from,to,Family.AXIS,new RotationProfile(List.of(
      new RotationProfile.Key(0,0),new RotationProfile.Key(16,0),new RotationProfile.Key(80,4*Math.PI),new RotationProfile.Key(length,4*Math.PI))),
      UUID.randomUUID(),AxisVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
  data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
  var anchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(from);
  anchor.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(to),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),length));
  var motorPos=from.east();l.setBlockAndUpdate(motorPos,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
  var motor=(CreativeMotorBlockEntity)l.getBlockEntity(motorPos);motor.generatedSpeed.setValue(32*anchor.getChainLiftTravelSign());
  var car=new FoundationData.Vehicle[1];double[] empty={0},peak={0},interrupted={0},maxRate={0},last={0};
  h.runAtTickTime(10,()->{
    car[0]=AxisVehicle.create(l,SpinningExamples.sample(l,edge,12));
    var dock=from.offset(-3,1,12);l.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
    ((BoardingStationEntity)l.getBlockEntity(dock)).vehicle=car[0].chassis;
    car[0].dock=dock;
  });
  h.runAtTickTime(50,()->AxisRuntime.rebindDock(l,car[0],car[0].dock));
  for(int tick=60;tick<=160;tick+=20)h.runAtTickTime(tick,()->{var v=car[0];var c=v.carriers.getFirst();System.out.println("FD_AXIS_ALIGN tick="+l.getGameTime()+" angle="+c.angle+" omega="+c.omega+" goal="+v.axis.command+" rate="+v.axis.commandRate+" fault="+v.fault);});
  h.runAtTickTime(180,()->{
    var v=car[0];var c=v.carriers.getFirst();var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
    h.assertTrue(Boarding.ready(l,v),"Axis initial dock: "+v.fault+" angle="+c.angle+" omega="+c.omega);
    empty[0]=b.getMassTracker().getMass();h.assertTrue(Math.abs(empty[0]-4)<1e-6,"Axis connected pod plus keel mass: "+empty[0]);
    var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,b.getPlot().getCenterBlock().offset(c.seats.getFirst()),pig);
    System.out.println("FD_AXIS_EMPTY mass="+empty[0]+" inertia="+AxisRuntime.inertia(b,c)+" com="+b.getMassTracker().getCenterOfMass());
  });
  h.runAtTickTime(240,()->{
    var v=car[0];var c=v.carriers.getFirst();var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
    h.assertTrue(Math.abs(b.getMassTracker().getMass()-empty[0]-.8)<1e-6,"Axis asymmetric rider is not native .8 mass");
    System.out.println("FD_AXIS_LOADED inertia="+AxisRuntime.inertia(b,c)+" com="+b.getMassTracker().getCenterOfMass()+" angle="+c.angle);
    VehicleOperations.restraints(l,v,true);rail.axisMode=AxisState.Mode.DRIVE;VehicleOperations.dispatch(l,v);
  });
  for(int tick=241;tick<650;tick++)h.runAtTickTime(tick,()->{
    var v=car[0];var c=v.carriers.getFirst();peak[0]=Math.max(peak[0],c.angle);maxRate[0]=Math.max(maxRate[0],Math.abs(c.omega));
    h.assertTrue(v.fault.isEmpty(),"Axis drive fault: "+v.fault+" angle="+c.angle);
    h.assertTrue(Math.abs(v.axis.commandRate)<=1.0001,"Axis target rate exceeded");
    h.assertTrue(Math.abs(v.axis.commandRate-last[0])<=.75*.051+.001,"Axis target acceleration exceeded "+last[0]+" -> "+v.axis.commandRate);last[0]=v.axis.commandRate;
  });
  h.runAtTickTime(650,()->{
    var v=car[0];var c=v.carriers.getFirst();
    System.out.println("FD_AXIS_DRIVE peak="+peak[0]+" maxOmega="+maxRate[0]+" angle="+c.angle+" target="+v.axis.command);
    h.assertTrue(peak[0]>2*Math.PI+.1,"Axis did not make a continuous unwrapped turn: "+peak[0]);
    interrupted[0]=c.angle;motor.generatedSpeed.setValue(0);
  });
  h.runAtTickTime(690,()->{
    var v=car[0];var c=v.carriers.getFirst();
    h.assertTrue(v.stopping&&v.restraints&&!v.fault.isEmpty()&&!c.capture,"Power loss must brake, retain, DAMP without capture");
    h.assertTrue(Math.abs(c.angle-interrupted[0])<Math.PI,"Power interruption jumped pose");
    var round=FoundationData.load(data.save(new CompoundTag(),l.registryAccess()),l.registryAccess());var copy=round.vehicles.get(v.chassis);
    h.assertTrue(copy.axis.mode==v.axis.mode&&copy.carriers.getFirst().angle==c.angle&&copy.carriers.getFirst().omega==c.omega,"Axis unwrapped mode/pose roundtrip lost");
    motor.generatedSpeed.setValue(-32*anchor.getChainLiftTravelSign());
  });
  h.runAtTickTime(720,()->AxisRuntime.resumeToDock(l,car[0]));
  h.runAtTickTime(1140,()->{rail.axisMode=AxisState.Mode.FREE;VehicleOperations.capture(l,car[0]);motor.generatedSpeed.setValue(0);});
  h.runAtTickTime(1180,()->{
    var v=car[0];System.out.println("FD_AXIS_REVERSE angle="+v.carriers.getFirst().angle+" fault="+v.fault);
    h.assertTrue(v.carriers.getFirst().angle<peak[0]-Math.PI,"Reversal did not retrace canonical profile");
    h.assertTrue(v.restraints,"Reversal released rider");
  });
  h.runAtTickTime(1300,()->{
    var v=car[0];var c=v.carriers.getFirst();
    h.assertTrue(Boarding.ready(l,v),"Axis return capture not ready: "+v.fault+" a="+c.angle+" w="+c.omega);
    VehicleOperations.restraints(l,v,false);
    h.succeed();
  });
 }
}
