package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Whole smooth native route, asymmetric load, stopped power interruption, explicit recovery. */
@GameTestHolder("fourthdimension_fd1502_route")
@PrefixGameTestTemplate(false)
public final class RollerBallRouteGameTests {
 @GameTest(template="empty",timeoutTicks=18000)
 public static void liftZigzagReturnAndPowerHold(GameTestHelper h){run(h,false);}
 public static void run(GameTestHelper h,boolean guideOnly){
  var l=h.getLevel();var o=new BlockPos(-300,80,-300);for(int x=-20;x<=-18;x++)for(int z=-22;z<=-7;z++)l.setChunkForced(x,z,true);
  var ex=new RollerBallExamples.Example[1];long[] cut={-1};boolean[] resumed={false},released={false};Set<RollerBallState.Phase> seen=new HashSet<>();double[] guidePeak={0},guideAccel={0},previousOmega={0},previousAngle={0},previousRate={0},angleRatePeak={0},angleAccelPeak={0};boolean[] previousGuided={false};double[] peak={0},minAngle={Double.POSITIVE_INFINITY},maxAngle={Double.NEGATIVE_INFINITY};
  h.runAtTickTime(10,()->ex[0]=RollerBallExamples.create(l,o,true));
  h.runAtTickTime(50,()->{
    for(var rail:FoundationData.get(l).rails){
      var edge=dev.fourthdimension.track.TrackGeometry.resolve(l,rail);
      var hash=edge==null?0:dev.fourthdimension.track.TrackGeometry.fingerprint(l,edge);
      System.out.println("FD_ROLLER_BOUND edge="+rail.from+".."+rail.to+" valid="+rail.valid+" saved="+rail.geometryHash+" native="+hash);
      h.assertTrue(rail.valid&&hash==rail.geometryHash,"Native stored route geometry must remain bound at "+rail.from);
    }
    RollerBallRuntime.rebindDock(l,ex[0].car(),ex[0].dock());
  });
  h.runAtTickTime(150,()->{
    var v=ex[0].car();h.assertTrue(Boarding.ready(l,v),"Initial route dock: "+v.fault+" angle="+v.carriers.getFirst().angle+" omega="+v.carriers.getFirst().omega);var c=v.carriers.getFirst();var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
    var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,b.getPlot().getCenterBlock().offset(c.seats.getFirst()),pig);
  });
  h.runAtTickTime(190,()->{VehicleOperations.restraints(l,ex[0].car(),true);VehicleOperations.dispatch(l,ex[0].car());});
  for(int tick=191;tick<17990;tick++){final int at=tick;h.runAtTickTime(tick,()->{
    var v=ex[0].car();var c=v.carriers.getFirst();var container=SubLevelContainer.getContainer(l);var p=(ServerSubLevel)container.getSubLevel(v.chassis);var pos=CoasterCartResolve.cartCenterWorld(p);
    double speed=container.physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).length();peak[0]=Math.max(peak[0],speed);seen.add(v.roller.phase);
    boolean guiding=v.roller.phase==RollerBallState.Phase.LIFT_GUIDED||v.roller.phase==RollerBallState.Phase.LIFT_LOCKED;
    if(guiding){guidePeak[0]=Math.max(guidePeak[0],Math.abs(c.omega));if(previousGuided[0]){
      double acceleration=Math.abs(c.omega-previousOmega[0])*20;
      if(acceleration>guideAccel[0]){guideAccel[0]=acceleration;if(acceleration>.5)System.out.println("FD_ROLLER_GUIDE_ACCEL tick="+at+" acceleration="+acceleration+" omega="+c.omega+" previous="+previousOmega[0]+" angle="+c.angle+" pitch="+v.roller.pitch+" command="+v.roller.command+" rate="+v.roller.commandRate+" latch="+c.liftHeld+" speed="+speed);}
    }}
    if(guiding&&previousGuided[0]){double rate=(c.angle-previousAngle[0])*20;angleRatePeak[0]=Math.max(angleRatePeak[0],Math.abs(rate));angleAccelPeak[0]=Math.max(angleAccelPeak[0],Math.abs(rate-previousRate[0])*20);previousRate[0]=rate;}
    previousAngle[0]=c.angle;previousOmega[0]=c.omega;previousGuided[0]=guiding;
    minAngle[0]=Math.min(minAngle[0],c.angle);maxAngle[0]=Math.max(maxAngle[0],c.angle);released[0]|=v.roller.released;
    if(at%100==0)System.out.println("FD_ROLLER_ROUTE_MOTION tick="+at+" center="+pos+" speed="+speed+" phase="+v.roller.phase+" pitch="+v.roller.pitch+" angle="+c.angle+" omega="+c.omega+" command="+v.roller.command+" latched="+c.liftHeld+" fault="+v.fault);
    if(cut[0]<0&&v.roller.phase==RollerBallState.Phase.LIFT_LOCKED&&pos.y>o.getY()+45){
      cut[0]=at;for(var edge:ex[0].edges())if(l.getBlockEntity(edge.from().east()) instanceof CreativeMotorBlockEntity m)m.generatedSpeed.setValue(0);
    }
    if(cut[0]>=0&&!resumed[0]&&at==cut[0]+80){
      h.assertTrue(v.stopping&&v.restraints&&speed<.1&&!v.fault.isEmpty(),"Power loss retains and stops lift");
      var d=FoundationData.get(l);var saved=FoundationData.load(d.save(new CompoundTag(),l.registryAccess()),l.registryAccess()).vehicles.get(v.chassis);
      h.assertTrue(saved.carriers.getFirst().body.equals(c.body)&&saved.carriers.getFirst().angle==c.angle&&saved.roller.phase==v.roller.phase,"Mid-lift save retains exact state");
      for(var edge:ex[0].edges())if(l.getBlockEntity(edge.from().east()) instanceof CreativeMotorBlockEntity m){var anchor=(dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)l.getBlockEntity(edge.from());m.generatedSpeed.setValue(32*anchor.getChainLiftTravelSign());}
    }
    if(cut[0]>=0&&!resumed[0]&&at==cut[0]+100){RollerBallRuntime.resume(l,v);resumed[0]=true;}
    if(cut[0]<0||resumed[0])h.assertTrue(v.fault.isEmpty(),"Native Roller Ball route fault: "+v.fault+" at "+pos+" phase="+v.roller.phase);
    h.assertTrue(speed<8.05,"Passive descent brake speed "+speed);h.assertTrue(Math.abs(c.omega)<=3.05,"Bounded retained native tumble omega="+c.omega+" angle="+c.angle);
    h.assertTrue(c.occupants.size()==1,"Native rider remains on same carrier through route");
    if(guideOnly&&at==2300){System.out.println("FD_ROLLER_GUIDE_MEASURE rate="+guidePeak[0]+" accel="+guideAccel[0]+" angleRate="+angleRatePeak[0]+" angleAccel="+angleAccelPeak[0]);h.assertTrue(pos.y>o.getY()+20,"Guide must progress beyond entry curve");h.assertTrue(guidePeak[0]<=.3501&&guideAccel[0]<=.5001&&angleRatePeak[0]<=.3501&&angleAccelPeak[0]<=.5001,"Native guide bounds");h.succeed();}
    if(!guideOnly&&released[0]&&v.stopping&&Boarding.ready(l,v)){
      h.assertTrue(resumed[0]&&seen.contains(RollerBallState.Phase.FREE_DESCENT)&&seen.contains(RollerBallState.Phase.RETURN_DAMP),"Complete lift/free/return phases");
      h.assertTrue(maxAngle[0]-minAngle[0]>.5,"Native gravity/inertia creates relative tumble");
      System.out.println("FD_ROLLER_ROUTE_PASS ticks="+at+" speedPeak="+peak[0]+" angleRange="+minAngle[0]+".."+maxAngle[0]+" guideRate="+guidePeak[0]+" guideAccel="+guideAccel[0]+" phases="+seen);h.succeed();
    }
  });}
 }
}
