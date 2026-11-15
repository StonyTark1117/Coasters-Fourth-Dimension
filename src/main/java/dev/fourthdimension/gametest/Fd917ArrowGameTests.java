package dev.fourthdimension.gametest;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** Native tracking, clutch capture/persistence and intentional overtravel regression. */
@GameTestHolder("fourthdimension_fd917_arrow")
@PrefixGameTestTemplate(false)
public final class Fd917ArrowGameTests {
  @GameTest(template="empty", timeoutTicks=1600)
  public static void boundedTrackingCaptureAndOvertravel(GameTestHelper h) {
    var level=h.getLevel();
    for(int x=-2;x<=4;x++)for(int z=-2;z<=5;z++)level.setChunkForced(x,z,true);
    var vehicle=ExampleLayouts.createBankedArrow(level,new BlockPos(0,100,0));
    var container=SubLevelContainer.getContainer(level);
    int[] ticks={0},samples={0},coupled={0};
    double[] peak={0},capturePeak={0},min={Double.POSITIVE_INFINITY},max={Double.NEGATIVE_INFINITY};
    boolean[] captured={false};
    h.runAtTickTime(80,()->{
      VehicleOperations.restraints(level,vehicle,true);
      VehicleOperations.dispatch(level,vehicle);
    });
    h.onEachTick(()->{
      ticks[0]++;
      if(ticks[0]<90)return;
      var rail=FoundationData.get(level).rails.stream()
          .filter(r->r.matches(vehicle.boundFrom,vehicle.boundTo)).findFirst().orElse(null);
      var body=container.getSubLevel(vehicle.chassis);
      if(rail==null||body==null)return;
      var edge=TrackGeometry.resolve(level,rail);var center=CoasterCartResolve.cartCenterWorld(body);
      if(edge==null||center==null)return;
      var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
      if(hit==null)return;
      double input=rail.profile.angleAt(TrackGeometry.distance(level,edge,rail,hit.t()));
      for(var c:vehicle.carriers){
        double error=Math.abs(input-c.angle);
        if(c.controlCoupled)coupled[0]++;
        if(ticks[0]<900)h.assertTrue(vehicle.fault.isEmpty(),"Ordinary profile unexpectedly faulted: "+vehicle.fault);
        if(c.capture)capturePeak[0]=Math.max(capturePeak[0],error);else peak[0]=Math.max(peak[0],error);
        min[0]=Math.min(min[0],c.angle);max[0]=Math.max(max[0],c.angle);
      }
      samples[0]++;
      if(ticks[0]%20==0)System.out.println("FD917_BASELINE tick="+ticks[0]+" input="+input
          +" actual="+vehicle.carriers.stream().map(c->c.angle).toList()+" capture="+vehicle.carriers.getFirst().capture
          +" position="+center+" fault="+vehicle.fault);
      if(ticks[0]>900&&!captured[0]&&Math.abs(input)>1){
        captured[0]=true;VehicleOperations.capture(level,vehicle);
      }
    });
    h.runAtTickTime(1400,()->{
      h.assertTrue(samples[0]>100,"No actual motion measurements");
      System.out.println("FD917_BASELINE_RESULT samples="+samples[0]+" normalError="+peak[0]
          +" captureError="+capturePeak[0]+" actualRange="+min[0]+","+max[0]+" captured="+captured[0]);
      h.assertTrue(coupled[0]>100,"Ordinary travel never engaged the actual-pose clutch");
      h.assertTrue(peak[0]<dev.fourthdimension.physics.ArrowTransmission.MAX_AXLE_ERROR,"Normal program exceeded spring travel");
      h.assertTrue(vehicle.carriers.stream().allMatch(c->c.capture&&!c.controlCoupled),"Capture did not release clutch");
      var saved=FoundationData.get(level).save(new net.minecraft.nbt.CompoundTag(),level.registryAccess());
      var restored=FoundationData.load(saved,level.registryAccess()).vehicles.get(vehicle.chassis);
      h.assertTrue(restored.carriers.stream().allMatch(c->c.capture&&!c.controlCoupled),"Capture/clutch state did not persist");
      // Deliberate impossible input discontinuity after engagement, not a normal saved program.
      var rail=FoundationData.get(level).rails.stream().filter(r->r.matches(vehicle.boundFrom,vehicle.boundTo)).findFirst().orElseThrow();
      rail.profile=new dev.fourthdimension.physics.RotationProfile(rail.profile.keys().stream()
          .map(k->new dev.fourthdimension.physics.RotationProfile.Key(k.distance(),k.angle()+8,k.slope())).toList());
      vehicle.stopping=false;
      for(var c:vehicle.carriers){c.capture=false;c.controlCoupled=true;}
    });
    h.runAtTickTime(1480,()->{
      h.assertTrue(vehicle.stopping&&vehicle.carriers.stream().allMatch(c->c.capture&&!c.controlCoupled),"Overtravel did not release and capture");
      h.assertTrue(dev.fourthdimension.physics.ArrowTransmission.OVERTRAVEL.equals(vehicle.fault),"Overtravel reason missing");
      System.out.println("FD917_COUPLING_PASS normalCoupledSamples="+coupled[0]+" captured=true persisted=true overtravelReleased=true");
      h.succeed();
    });
  }
}
