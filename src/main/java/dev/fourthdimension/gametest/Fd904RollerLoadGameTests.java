package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Isolated FD904B full-capacity motion check; synthetic boarding is disclosed setup. */
@GameTestHolder("fourthdimension_fd904_roller_load")
@PrefixGameTestTemplate(false)
public final class Fd904RollerLoadGameTests {
  @GameTest(template="empty", timeoutTicks=22000)
  public static void fullLoadMotion(GameTestHelper h) {
    boolean diagnose="diagnose".equals(System.getenv("FD904_ROLLER_MODE"));
    var l=h.getLevel();var origin=new BlockPos(-750,110,110);
    // Own the route and neighboring envelope chunks, independently of the park world.
    for(int x=-48;x<=-46;x++)for(int z=3;z<=18;z++)l.setChunkForced(x,z,true);
    var ex=new RollerBallExamples.Example[1];var pigs=new ArrayList<Pig>();
    var seen=EnumSet.noneOf(RollerBallState.Phase.class);
    long[] stationary={0},unloaded={-1},cut={-1};boolean[] resumed={false},released={false},entryPassed={false};
    Vector3d[] previous={null},stall={null},hold={null};
    double[] maxOmega={0},maxAccel={0},maxAngleRate={0},maxAngleAccel={0},lastOmega={0},lastAngle={0},lastRate={0};
    boolean[] lastGuided={false},lastRateValid={false};
    h.runAtTickTime(10,()->ex[0]=RollerBallExamples.create(l,origin,true));
    h.runAtTickTime(50,()->RollerBallRuntime.rebindDock(l,ex[0].car(),ex[0].dock()));
    h.runAtTickTime(150,()->{
      var v=ex[0].car();h.assertTrue(Boarding.ready(l,v),"Initial dock ready: "+v.fault);
      var c=v.carriers.getFirst();var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
      for(var seat:c.seats){var pig=EntityType.PIG.create(l);pig.setNoAi(true);pig.setPersistenceRequired();l.addFreshEntity(pig);SeatBlock.sitDown(l,child.getPlot().getCenterBlock().offset(seat),pig);pigs.add(pig);}
    });
    h.runAtTickTime(190,()->{
      var v=ex[0].car();var c=v.carriers.getFirst();var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
      h.assertTrue(new HashSet<>(c.occupants).size()==4&&c.occupants.size()==4,"Four unique native riders");
      h.assertTrue(Math.abs(child.getMassTracker().getMass()-14.2)<1e-6,"Four riders give native carrier mass14.2");
      System.out.println("FD904_ROLLER_START mode="+(diagnose?"diagnose":"qualify")+" chassis="+v.chassis+" carrier="+c.body+" occupants="+c.occupants);
      VehicleOperations.restraints(l,v,true);VehicleOperations.dispatch(l,v);
    });
    h.onEachTick(()->{
      long tick=h.getTick();if(tick<191)return;
      var v=ex[0].car();var c=v.carriers.getFirst();var container=SubLevelContainer.getContainer(l);
      var p=(ServerSubLevel)container.getSubLevel(v.chassis);var child=(ServerSubLevel)container.getSubLevel(c.body);
      var center=CoasterCartResolve.cartCenterWorld(p);var pipeline=container.physicsSystem().getPipeline();
      var velocity=pipeline.getLinearVelocity(p,new Vector3d());var childVelocity=pipeline.getLinearVelocity(child,new Vector3d());
      var rail=RollerBallRuntime.rail(l,v);var edge=TrackGeometry.resolve(l,rail);
      var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,new Vec3(center.x,center.y,center.z));
      boolean guiding=v.roller.phase==RollerBallState.Phase.LIFT_GUIDED||v.roller.phase==RollerBallState.Phase.LIFT_LOCKED;
      if(guiding){maxOmega[0]=Math.max(maxOmega[0],Math.abs(c.omega));if(lastGuided[0]){
        maxAccel[0]=Math.max(maxAccel[0],Math.abs(c.omega-lastOmega[0])*20);
        double rate=(c.angle-lastAngle[0])*20;maxAngleRate[0]=Math.max(maxAngleRate[0],Math.abs(rate));
        if(lastRateValid[0]){
          double acceleration=Math.abs(rate-lastRate[0])*20;
          if(acceleration>maxAngleAccel[0]&&acceleration>.5)System.out.println("FD904_ROLLER_ANGLE_ACCEL tick="+tick+" value="+acceleration+" center="+center+" phase="+v.roller.phase+" entryPassed="+entryPassed[0]);
          maxAngleAccel[0]=Math.max(maxAngleAccel[0],acceleration);
        }
        lastRate[0]=rate;lastRateValid[0]=true;
      }else lastRateValid[0]=false;}else lastRateValid[0]=false;
      lastGuided[0]=guiding;lastOmega[0]=c.omega;lastAngle[0]=c.angle;
      seen.add(v.roller.phase);released[0]|=v.roller.released;
      if(tick%100==0||unloaded[0]>=0&&tick<=unloaded[0]+100)
        System.out.printf(Locale.ROOT,"FD904_ROLLER_SAMPLE tick=%d xyz=%.9f,%.9f,%.9f velocity=%s childVelocity=%s from=%s to=%s t=%.12f mass=%.9f occupants=%d unique=%d phase=%s angle=%.9f omega=%.9f fault=%s%n",tick,center.x,center.y,center.z,velocity,childVelocity,rail.from,rail.to,hit.t(),child.getMassTracker().getMass(),c.occupants.size(),new HashSet<>(c.occupants).size(),v.roller.phase,c.angle,c.omega,v.fault);
      h.assertTrue(velocity.length()<8.05,"Bounded native speed "+velocity);
      h.assertTrue(Math.abs(c.omega)<=3.05,"Bounded tumble "+c.omega);
      if(diagnose){
        h.assertTrue(v.fault.isEmpty()&&!v.stopping,"No fault/hold explains stalled motion: "+v.fault);
        if(unloaded[0]<0){
          h.assertTrue(c.occupants.size()==4&&new HashSet<>(c.occupants).size()==4&&Math.abs(child.getMassTracker().getMass()-14.2)<1e-6,"Retain exact full native load");
          boolean nearJoin=rail.from.equals(origin.offset(0,0,32))&&hit.t()>.95;
          stationary[0]=nearJoin&&previous[0]!=null&&center.distance(previous[0])<.0001?stationary[0]+1:0;
          if(stationary[0]>=150){
            unloaded[0]=tick;stall[0]=new Vector3d(center);
            // Deliberate fixture-entity removal, not normal unload: closed restraints
            // correctly reject stopRiding(), and must remain unchanged for this probe.
            for(int i=1;i<4;i++)pigs.get(i).discard();
            System.out.println("FD904_ROLLER_UNLOAD tick="+tick+" center="+center+" sameProcess=true reset=false chassis="+v.chassis+" carrier="+c.body);
          }
          h.assertTrue(tick<5000,"Full-load stall not reproduced in bounded window");
        }else if(tick==unloaded[0]+100){
          h.assertTrue(c.occupants.size()==1&&Math.abs(child.getMassTracker().getMass()-11.8)<1e-6,"Three removed: native mass11.8");
          System.out.println("FD904_ROLLER_DIAG_PASS displacement="+center.distance(stall[0])+" deltaY="+(center.y-stall[0].y)+" nativeBodiesUnchanged=true");
          h.assertTrue(center.y-stall[0].y>.1,"Removing three riders permits progress without constraint reset");h.succeed();
        }
      }else{
        h.assertTrue(c.occupants.size()==4&&new HashSet<>(c.occupants).size()==4&&Math.abs(child.getMassTracker().getMass()-14.2)<1e-6,"Retain full native load throughout cycle");
        // The accepted guide-only assertion covers the initial rise past the entry
        // curve. Later cycle peaks are recorded, as in RollerBallRouteGameTests.
        if(!entryPassed[0]&&center.y>origin.getY()+20){
          System.out.println("FD904_ROLLER_ENTRY tick="+tick+" guideRate="+maxOmega[0]+" guideAccel="+maxAccel[0]+" angleRate="+maxAngleRate[0]+" angleAccel="+maxAngleAccel[0]);
          h.assertTrue(maxOmega[0]<=.3501&&maxAccel[0]<=.5001&&maxAngleRate[0]<=.3501&&maxAngleAccel[0]<=.5001,"Existing entry guide rate/acceleration bounds");entryPassed[0]=true;
        }
        if(cut[0]<0&&v.roller.phase==RollerBallState.Phase.LIFT_LOCKED&&center.y>origin.getY()+45){
          cut[0]=tick;hold[0]=new Vector3d(center);
          for(var e:ex[0].edges())if(l.getBlockEntity(e.from().east()) instanceof CreativeMotorBlockEntity motor)motor.generatedSpeed.setValue(0);
          System.out.println("FD904_ROLLER_POWER_CUT tick="+tick+" center="+center);
        }
        if(cut[0]>=0&&!resumed[0]&&tick==cut[0]+80){
          h.assertTrue(v.stopping&&v.restraints&&!v.fault.isEmpty()&&velocity.length()<.1&&center.distance(hold[0])<1,"No-power retained stop");
          System.out.println("FD904_ROLLER_POWER_HOLD drift="+center.distance(hold[0])+" velocity="+velocity+" fault="+v.fault);
          for(var e:ex[0].edges())if(l.getBlockEntity(e.from().east()) instanceof CreativeMotorBlockEntity motor){var anchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(e.from());motor.generatedSpeed.setValue(32*anchor.getChainLiftTravelSign());}
        }
        if(cut[0]>=0&&!resumed[0]&&tick==cut[0]+100){h.assertTrue(v.stopping,"Power restore cannot auto-resume");RollerBallRuntime.resume(l,v);resumed[0]=true;}
        if(cut[0]<0||resumed[0])h.assertTrue(v.fault.isEmpty(),"Fault-free full-load cycle: "+v.fault);
        if(tick==3500)h.assertTrue(entryPassed[0],"Full load progresses beyond first curve");
        if(released[0]&&v.stopping&&Boarding.ready(l,v)){
          h.assertTrue(resumed[0]&&seen.contains(RollerBallState.Phase.FREE_DESCENT)&&seen.contains(RollerBallState.Phase.RETURN_DAMP),"Full lift/free/return cycle with power hold");
          System.out.println("FD904_ROLLER_CYCLE_PASS tick="+tick+" guideRate="+maxOmega[0]+" guideAccel="+maxAccel[0]+" angleRate="+maxAngleRate[0]+" angleAccel="+maxAngleAccel[0]+" phases="+seen);
          h.assertTrue(entryPassed[0],"Entry guide bounds passed");h.succeed();
        }
      }
      previous[0]=new Vector3d(center);
    });
  }
}
