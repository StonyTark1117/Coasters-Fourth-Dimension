package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Adapter proof only: fixture owns its Wing metadata while the ordinary family runtime is suspended. */
@GameTestHolder("fourthdimension_fd2001_native")
@PrefixGameTestTemplate(false)
public final class MovingNativeGameTests {
  @GameTest(template="empty",timeoutTicks=560)
  public static void physicalBedDockRetainTranslateAndRestore(GameTestHelper h) {
    var level=h.getLevel();var data=FoundationData.get(level);
    var origin=new BlockPos(-320,130,-320);int half=8;
    for(int x=-22;x<=-18;x++)for(int z=-22;z<=-18;z++)level.setChunkForced(x,z,true);
    var s=new MovingAssemblyState(UUID.randomUUID(),origin.west(10),"transfer",WingVehicle.VARIANT,16);
    var initial=new MovingAssemblyState.Pose(origin.getX()+.5,origin.getY()+.5,origin.getZ()+.5,0,0,0,1);
    var end=new MovingAssemblyState.Pose(initial.x()+8,initial.y(),initial.z(),0,0,0,1);
    var a=new MovingAssemblyState.Dock("a",UUID.randomUUID(),origin.offset(0,0,-half),origin.offset(0,0,half),initial);
    var b=new MovingAssemblyState.Dock("b",UUID.randomUUID(),origin.offset(8,0,-half),origin.offset(8,0,half),end);
    s.docks.put("a",a);s.docks.put("b",b);s.source="a";s.destination="b";
    for(var dock:List.of(a,b)) {
      for(var p:List.of(dock.from(),dock.to(),dock.from().offset(0,0,-24),dock.to().offset(0,0,24)))
        level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      MotorbikeExamples.connect(level,dock.from().offset(0,0,-24),dock.from(),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
      MotorbikeExamples.connect(level,dock.to(),dock.to().offset(0,0,24),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    }
    MovingNativeAdapter.createBed(level,s,initial,Blocks.IRON_BLOCK.defaultBlockState());
    MovingAssemblyState[] state={s};FoundationData.Vehicle[] car={null};CoasterPathEdge[] dockEdge={null};
    Vector3d[] retainedPosition={null};int[] peakGraph={0};
    UUID missingBody=UUID.randomUUID();Vector3d[] faultPosition={null};
    h.runAtTickTime(45,()->{
      MovingNativeAdapter.readback(level,s);
      System.out.println("FD2001_BED_SETTLED pose="+s.actualPose+" v="+s.linearVelocity+" w="+s.angularVelocity);
      MovingNativeAdapter.dock(level,s,a);dockEdge[0]=dockEdge(level,a);
      h.assertTrue(!CoasterPathGraphManager.edgeHostedOnSubLevel(level,dockEdge[0]),"Locked dock must expose world graph edge");
      var graph=CoasterPathGraphManager.get(level);int index=graph.indexOfEdge(dockEdge[0]);
      h.assertTrue(graph.edges().stream().filter(e->e!=dockEdge[0]&&(e.to().equals(a.from())||e.from().equals(a.to())))
          .allMatch(e->graph.edgesAreSameOrDirectNeighbors(index,graph.indexOfEdge(e))),"Locked span must be native neighbor of both fixed approaches");
      peakGraph[0]=graph.edges().size();
      car[0]=WingVehicle.create(level,MotorbikeExamples.sample(level,dockEdge[0],8));
      // Disclosed fixture retention: this isolated adapter has no production controller registration.
      car[0].restraints=true;data.vehicles.remove(car[0].chassis);
    });
    h.runAtTickTime(95,()->{
      var parent=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(car[0].chassis);
      System.out.println("FD2001_WING_SETTLED fixtureRetained="+car[0].restraints+" fault="+car[0].fault
          +" pose="+parent.logicalPose()+" velocity="+SubLevelContainer.getContainer(level).physicsSystem()
              .getPipeline().getLinearVelocity(parent,new Vector3d()));
    });
    h.runAtTickTime(100,()->{
      s.cars=List.of(car[0].chassis);
      var count=SubLevelContainer.getContainer(level).getAllSubLevels().size();
      MovingNativeAdapter.capture(level,s,List.of(car[0].chassis));
      // Isolate the native adapter from the production family/authority coordinator under development.
      // This proves real Wing bodies/rail/constraints, not normal controls or coordinator acceptance.
      data.vehicles.remove(car[0].chassis);
      retainedPosition[0]=new Vector3d(SubLevelContainer.getContainer(level).getSubLevel(car[0].chassis).logicalPose().position());
      MovingNativeAdapter.disconnect(level,s);assertDisconnected(h,s,a,b);
      CoasterPathGraphManager.get(level).addEdge(dockEdge[0]); // Simulated deferred native rebuild tries to resurrect the old bridge.
      assertDisconnected(h,s,a,b);
      h.assertTrue(SubLevelContainer.getContainer(level).getAllSubLevels().size()==count,"Capture duplicated native body");
      System.out.println("FD2001_CAPTURE actual="+s.actualPose+" chassis="+car[0].chassis+" local="+s.bodies.get(car[0].chassis));
    });
    for(int tick=101;tick<=200;tick++) {
      final int time=tick;h.runAtTickTime(tick,()->{
        double u=(time-100)/100.0;double x=4*smooth(u);
        MovingNativeAdapter.drive(level,state[0],new MovingAssemblyState.Pose(initial.x()+x,initial.y(),initial.z(),0,0,0,1),.05);
        assertDisconnected(h,state[0],a,b);
      });
    }
    for(int tick=201;tick<=250;tick++)h.runAtTickTime(tick,()->MovingNativeAdapter.hold(level,state[0],.05));
    h.runAtTickTime(250,()->{
      var current=state[0];MovingNativeAdapter.readback(level,current);
      h.assertTrue(current.actualPose.x()>initial.x()+3.5,"Physical bed did not move laterally");
      h.assertTrue(MovingNativeAdapter.stopped(level,current),"Interrupted bed did not physically stop");
      var chassis=SubLevelContainer.getContainer(level).getSubLevel(car[0].chassis);
      h.assertTrue(chassis.logicalPose().position().x>retainedPosition[0].x+3.5,"Retained physical Wing stayed behind");
      var saved=current.save(true);var loaded=MovingAssemblyState.read(saved);
      h.assertTrue(loaded.restored&&loaded.phase==MovingAssemblyState.Phase.FAULT_HOLD&&loaded.bed.equals(current.bed)
          &&loaded.bodies.equals(current.bodies)&&loaded.cars.equals(current.cars),"Saved body/bed retention identities changed");
      MovingNativeAdapter.restore(level,loaded);state[0]=loaded;
      System.out.println("FD2001_RESTORE retained="+loaded.bodies+" actual="+loaded.actualPose+" v="+loaded.linearVelocity);
    });
    for(int tick=251;tick<=280;tick++)h.runAtTickTime(tick,()->MovingNativeAdapter.hold(level,state[0],.05));
    h.runAtTickTime(260,()->{
      var current=state[0];current.bodies.put(missingBody,current.bodies.get(car[0].chassis));
      current.bodyAnchors.put(missingBody,new Vector3d(current.bodyAnchors.get(car[0].chassis)));
      boolean denied=false;
      try{MovingNativeAdapter.restore(level,current);}catch(IllegalArgumentException expected){denied=true;}
      h.assertTrue(denied,"Missing retained identity must fail full restoration");
      denied=false;
      try{MovingNativeAdapter.drive(level,current,end,.05);}catch(IllegalArgumentException expected){denied=true;}
      h.assertTrue(denied&&current.bodies.containsKey(missingBody),"Fault restore allowed travel or erased missing identity");
      faultPosition[0]=new Vector3d(SubLevelContainer.getContainer(level).getSubLevel(car[0].chassis).logicalPose().position());
      MovingNativeAdapter.hold(level,current,.05);
    });
    h.runAtTickTime(280,()->{
      var current=state[0];var survivor=SubLevelContainer.getContainer(level).getSubLevel(car[0].chassis);
      h.assertTrue(MovingNativeAdapter.stopped(level,current)&&survivor.logicalPose().position().distance(faultPosition[0])<.05,
          "Surviving physical body lost stationary support during missing-body fault");
      h.assertTrue(current.bodies.containsKey(missingBody)&&current.bodies.containsKey(car[0].chassis),"Fault hold altered retention inventory");
      // Disclosed failure injection is removed by the fixture; production cannot discard missing identities.
      current.bodies.remove(missingBody);current.bodyAnchors.remove(missingBody);MovingNativeAdapter.restore(level,current);
      System.out.println("FD2001_MISSING_BODY_HELD survivor="+car[0].chassis+" actual="+current.actualPose);
    });
    for(int tick=281;tick<=380;tick++) {
      final int time=tick;h.runAtTickTime(tick,()->{
        double u=(time-280)/100.0;double x=4+4*smooth(u);
        MovingNativeAdapter.drive(level,state[0],new MovingAssemblyState.Pose(initial.x()+x,initial.y(),initial.z(),0,0,0,1),.05);
      });
    }
    for(int tick=381;tick<=430;tick++)h.runAtTickTime(tick,()->MovingNativeAdapter.drive(level,state[0],end,.05));
    h.runAtTickTime(435,()->{
      var current=state[0];MovingNativeAdapter.readback(level,current);
      System.out.println("FD2001_END pose="+current.actualPose+" v="+current.linearVelocity+" impulse="+MovingNativeAdapter.actuatorLinearImpulse(level,current));
      MovingNativeAdapter.dock(level,current,b);var edge=dockEdge(level,b);
      h.assertTrue(CoasterPathGraphManager.get(level).edges().size()==peakGraph[0],"Dock graph leaked/duplicated edges");
      h.assertTrue(!CoasterPathGraphManager.edgeHostedOnSubLevel(level,edge),"Destination not a real fixed graph connection");
      h.assertTrue(car[0].carriers.getFirst().body.equals(car[0].chassis),"Wing fixed seat/native body identity changed");
      MovingNativeAdapter.release(level,current);car[0].boundFrom=b.from();car[0].boundTo=b.to();
      data.vehicles.put(car[0].chassis,car[0]);
    });
    h.runAtTickTime(470,()->{
      var chassis=SubLevelContainer.getContainer(level).getSubLevel(car[0].chassis);
      h.assertTrue(chassis.logicalPose().position().distance(retainedPosition[0])>7.5,"Final Wing body did not travel with moving rail");
      h.assertTrue(!dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap.isBearingDisengaged((dev.ryanhcode.sable.sublevel.ServerSubLevel)chassis),"Native guide did not resume at locked dock");
      System.out.println("FD2001_NATIVE_PASS bed="+state[0].bed+" chassis="+car[0].chassis+" fixed="+b.from()+"/"+b.to());h.succeed();
    });
  }
  private static double smooth(double t){return t*t*t*t*(35+t*(-84+t*(70-20*t)));}
  private static CoasterPathEdge dockEdge(net.minecraft.server.level.ServerLevel level,MovingAssemblyState.Dock d){
    return CoasterPathGraphManager.get(level).edges().stream().filter(e->e.from().equals(d.from())&&e.to().equals(d.to())).findFirst().orElseThrow();
  }
  private static void assertDisconnected(GameTestHelper h,MovingAssemblyState s,MovingAssemblyState.Dock a,MovingAssemblyState.Dock b){
    var graph=CoasterPathGraphManager.get(h.getLevel());
    h.assertTrue(graph.edges().stream().noneMatch(e->e.from().equals(a.from())&&e.to().equals(a.to())||e.from().equals(b.from())&&e.to().equals(b.to())),"Unlocked fixed dock edge remained traversable");
    h.assertTrue(graph.edges().stream().anyMatch(e->e.from().equals(s.railFrom)&&e.to().equals(s.railTo)),"Physical hosted rail disappeared");
  }
}
