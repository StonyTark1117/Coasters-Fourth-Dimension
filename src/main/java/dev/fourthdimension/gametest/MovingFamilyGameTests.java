package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.*;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Actual original-body adapter checks. Fixture braking is not normal player admission. */
@GameTestHolder("fourthdimension_fd2001_families")
@PrefixGameTestTemplate(false)
public final class MovingFamilyGameTests {
  private record Observed(Vector3d position,Quaterniond orientation,double mass){}
  private static final class Rig {
    final TrackDesign design;MovingAssemblyState state;MovingAssemblyState.Dock start,end;
    FoundationData.Vehicle car;final Map<UUID,Observed> initial=new LinkedHashMap<>();
    Rig(TrackDesign design){this.design=design;}
  }
  @GameTest(template="empty",timeoutTicks=520)
  public static void originalRotorBodyGraphs(GameTestHelper h){run(h,0,List.of(TrackDesign.ARROW,TrackDesign.LEGACY,TrackDesign.FREE_SPIN,TrackDesign.REVISED,TrackDesign.PAX));}
  @GameTest(template="empty",timeoutTicks=520)
  public static void articulatedBodyGraphs(GameTestHelper h){run(h,1,List.of(TrackDesign.SPINNING,TrackDesign.S2_SPINNING,TrackDesign.AXIS,TrackDesign.SUSPENDED,TrackDesign.FLYING,TrackDesign.NEW_GENERATION_FLYING,TrackDesign.POWERED_INVERTED_YAW,TrackDesign.STANDING_SURF,TrackDesign.ROLLER_BALL));}

  public static void run(GameTestHelper h,int group,List<TrackDesign> designs){
    var l=h.getLevel();var data=FoundationData.get(l);var rigs=designs.stream().map(Rig::new).toList();
    for(int i=0;i<rigs.size();i++){
      var r=rigs.get(i);var origin=new BlockPos(-1200-i*64,150,-1200-group*128);
      for(int x=(origin.getX()>>4)-1;x<=(origin.getX()>>4)+1;x++)for(int z=(origin.getZ()>>4)-3;z<=(origin.getZ()>>4)+3;z++)l.setChunkForced(x,z,true);
      var config=MovingCapabilityCatalog.find(r.design.family,r.design.variant).orElseThrow().configuration();
      var s=new MovingAssemblyState(UUID.randomUUID(),origin.west(12),"transfer",r.design.variant,16);s.configuration=config.id();r.state=s;
      var a=new MovingAssemblyState.Pose(origin.getX()+.5,origin.getY()+.5,origin.getZ()+.5,0,0,0,1);
      var b=new MovingAssemblyState.Pose(a.x()+8,a.y(),a.z(),0,0,0,1);
      r.start=new MovingAssemblyState.Dock("a",UUID.randomUUID(),origin.offset(0,0,-8),origin.offset(0,0,8),a);
      r.end=new MovingAssemblyState.Dock("b",UUID.randomUUID(),origin.offset(8,0,-8),origin.offset(8,0,8),b);
      s.docks.put("a",r.start);s.docks.put("b",r.end);s.source="a";s.destination="b";
      for(var dock:s.docks.values())for(var p:List.of(dock.from(),dock.to()))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      var parts=new ArrayList<MovingNativeAdapter.BlockPlacement>();
      for(int z=-8;z<=8;z++)for(int x=-1;x<=1;x++)parts.add(new MovingNativeAdapter.BlockPlacement(new BlockPos(x,r.design.overhead()?1:r.design==TrackDesign.PIPELINE?-4:-1,z),
          group==2?MovingContent.PARTS.get(r.design.overhead()?MovingPartBlock.Kind.OVERHEAD_GANTRY:r.design==TrackDesign.PIPELINE?MovingPartBlock.Kind.PIPELINE_BED:MovingPartBlock.Kind.BED).get().defaultBlockState():Blocks.IRON_BLOCK.defaultBlockState()));
      if(r.design==TrackDesign.PIPELINE)parts.addAll(MovingConstruction.pipelineEndPortals(s.deckLength));
      MovingNativeAdapter.createBed(l,s,a,parts);
    }
    h.runAtTickTime(45,()->{
      for(var r:rigs){MovingNativeAdapter.dock(l,r.state,r.start);
        var edge=CoasterPathGraphManager.get(l).edges().stream().filter(e->e.from().equals(r.start.from())&&e.to().equals(r.start.to())).findFirst().orElseThrow();
        double length=TrackGeometry.table(l,edge).length();
        // Explicit creation fixture record for a locked physical bridge, not admission certification.
        var rail=new FoundationData.Rail(edge.from(),edge.to(),r.design.family,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),r.design.variant,TrackGeometry.fingerprint(l,edge),0,true);
        data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
        r.car=VehicleFactory.create(l,r.design,MotorbikeExamples.sample(l,edge,length/2));r.car.restraints=true;
      }
    });
    // Let original family callbacks create their actual native child and stationary holding joints.
    // Fixture velocity reset before transfer isolates the new locking adapter from old station setup.
    for(int tick=85;tick<=100;tick++)h.runAtTickTime(tick,()->{
      for(var r:rigs)for(var id:ids(r.car)){
        var b=body(h,id);SubLevelContainer.getContainer(l).physicsSystem().getPipeline().resetVelocity(b);
      }
    });
    h.runAtTickTime(101,()->{
      for(var r:rigs){var ids=ids(r.car);r.state.cars=List.of(r.car.chassis);
        h.assertTrue(data.vehicles.get(r.car.chassis)==r.car,"Original family metadata lost before actual capture");
        // Setup braking must occur in the same callback: another solver step between reset and
        // capture reintroduces gravity/spring velocity. This does not certify normal admission.
        var physics=SubLevelContainer.getContainer(l).physicsSystem();
        if(group==2&&(r.design==TrackDesign.PIPELINE||r.design.overhead()))auditPipelineClearance(h,r);
        var setupBodies=new ArrayList<>(ids);setupBodies.add(r.state.bed);
        for(var id:setupBodies){var b=body(h,id);physics.updatePose(b);
          System.out.println("FD2001_FAMILY_SETUP_STOP design="+r.design+" body="+id
              +" speed="+physics.getPipeline().getLinearVelocity(b,new Vector3d()).length()
              +" angular="+physics.getPipeline().getAngularVelocity(b,new Vector3d()).length());
          physics.getPipeline().resetVelocity(b);
        }
        MovingNativeAdapter.capture(l,r.state,ids); // Invokes actual family world-hold teardown.
        h.assertTrue(r.state.bodies.keySet().equals(new LinkedHashSet<>(ids)),"Incomplete original child inventory: "+r.design);
        if(r.design==TrackDesign.NEW_GENERATION_FLYING)h.assertTrue(ids.size()==3&&ids.contains(r.car.yaw.deck),"NG intermediate native deck omitted");
        if(r.design==TrackDesign.STANDING_SURF)h.assertTrue(ids.size()==2&&!r.car.carriers.getFirst().body.equals(r.car.chassis),"Surf child is not distinct");
        if(r.design==TrackDesign.STANDING_STANDARD||r.design==TrackDesign.PIPELINE||r.design==TrackDesign.MOTORBIKE)h.assertTrue(ids.size()==1,"Fixed specialty gained an artificial child");
        for(var id:ids){var b=body(h,id);r.initial.put(id,new Observed(new Vector3d(b.logicalPose().position()),new Quaterniond(b.logicalPose().orientation()),b.getMassTracker().getMass()));}
        // No production controller exists in this adapter-only fixture. Suspend metadata only AFTER
        // capture tears down real family/world holds; retained physical bodies and joints remain live.
        data.vehicles.remove(r.car.chassis);MovingNativeAdapter.disconnect(l,r.state);
        System.out.println("FD2001_FAMILY_CAPTURE design="+r.design+" chassis="+r.car.chassis+" bodies="+r.state.bodies.keySet());
      }
    });
    for(int tick=102;tick<=201;tick++){final int t=tick;h.runAtTickTime(tick,()->{
      double x=4*smooth((t-101)/100.0);for(var r:rigs)MovingNativeAdapter.drive(l,r.state,translated(r.start.pose(),x),.05);
    });}
    for(int tick=202;tick<=250;tick++)h.runAtTickTime(tick,()->{for(var r:rigs)MovingNativeAdapter.hold(l,r.state,.05);});
    h.runAtTickTime(250,()->{
      for(var r:rigs){assertBodies(h,r,4,.10);h.assertTrue(MovingNativeAdapter.stopped(l,r.state),"Family bed failed stationary interruption: "+r.design);
        var saved=MovingAssemblyState.read(r.state.save(true));h.assertTrue(saved.bodies.equals(r.state.bodies)&&saved.cars.equals(r.state.cars),"Original graph changed in saved retention: "+r.design);
        MovingNativeAdapter.restore(l,saved);r.state=saved;
      }
    });
    for(int tick=251;tick<=350;tick++){final int t=tick;h.runAtTickTime(tick,()->{
      double x=4+4*smooth((t-250)/100.0);for(var r:rigs)MovingNativeAdapter.drive(l,r.state,translated(r.start.pose(),x),.05);
    });}
    for(int tick=351;tick<=410;tick++)h.runAtTickTime(tick,()->{for(var r:rigs)MovingNativeAdapter.drive(l,r.state,r.end.pose(),.05);});
    h.runAtTickTime(415,()->{
      for(var r:rigs){assertBodies(h,r,8,.08);MovingNativeAdapter.dock(l,r.state,r.end);
        h.assertTrue(MovingNativeAdapter.stopped(l,r.state),"Native family destination did not stop: "+r.design);
        MovingNativeAdapter.release(l,r.state);
        h.assertTrue(!r.state.retained(),"Native locks did not release original body graph: "+r.design);
        System.out.println("FD2001_FAMILY_PASS design="+r.design+" originalBodies="+r.initial.keySet()+" translation=8 interruptionRestore=true");
      }h.succeed();
    });
  }
  private static List<UUID> ids(FoundationData.Vehicle v){var ids=new LinkedHashSet<UUID>();ids.add(v.chassis);if(NewGenerationFlyingVehicle.is(v))ids.add(v.yaw.deck);v.carriers.forEach(c->ids.add(c.body));return List.copyOf(ids);}
  private static ServerSubLevel body(GameTestHelper h,UUID id){var b=SubLevelContainer.getContainer(h.getLevel()).getSubLevel(id);h.assertTrue(b instanceof ServerSubLevel&&!b.isRemoved(),"Original native body unavailable: "+id);return (ServerSubLevel)b;}
  private static void assertBodies(GameTestHelper h,Rig r,double distance,double tolerance){
    for(var e:r.initial.entrySet()){var b=body(h,e.getKey());var expected=new Vector3d(e.getValue().position()).add(distance,0,0);
      h.assertTrue(b.logicalPose().position().distance(expected)<tolerance,"Retained family body did not follow bed: "+r.design+" "+e.getKey()+" error="+b.logicalPose().position().distance(expected));
      h.assertTrue(MovingNativeAdapter.angularError(new Quaterniond(b.logicalPose().orientation()),e.getValue().orientation())<Math.toRadians(.5),"Original local posture changed: "+r.design);
      h.assertTrue(Math.abs(b.getMassTracker().getMass()-e.getValue().mass())<1e-6,"Body mass changed during retained movement: "+r.design);
    }
  }
  private static void auditPipelineClearance(GameTestHelper h,Rig r){
    var bed=body(h,r.state.bed);var car=body(h,r.car.chassis);var l=h.getLevel();
    SubLevelContainer.getContainer(l).physicsSystem().updatePose(bed);SubLevelContainer.getContainer(l).physicsSystem().updatePose(car);
    var bp=bed.getPlot().getBoundingBox();var cp=car.getPlot().getBoundingBox();int pairs=0;double worst=0;String offender="";
    for(var a:BlockPos.betweenClosed(new BlockPos(bp.minX(),bp.minY(),bp.minZ()),new BlockPos(bp.maxX(),bp.maxY(),bp.maxZ())))
      for(var sa:l.getBlockState(a).getCollisionShape(l,a).toAabbs()){
        var ab=worldBox(bed,sa.move(a));
        for(var b:BlockPos.betweenClosed(new BlockPos(cp.minX(),cp.minY(),cp.minZ()),new BlockPos(cp.maxX(),cp.maxY(),cp.maxZ())))
          for(var sb:l.getBlockState(b).getCollisionShape(l,b).toAabbs()){
            pairs++;var bb=worldBox(car,sb.move(b));
            if(ab.intersects(bb)){double depth=Math.min(Math.min(Math.min(ab.maxX,bb.maxX)-Math.max(ab.minX,bb.minX),Math.min(ab.maxY,bb.maxY)-Math.max(ab.minY,bb.minY)),Math.min(ab.maxZ,bb.maxZ)-Math.max(ab.minZ,bb.minZ));
              if(depth>worst){worst=depth;offender="bed="+a+" "+ab+" car="+b+" "+bb;}}
          }
      }
    System.out.println("FD2001_BED_CLEARANCE design="+r.design+" pairs="+pairs+" maxOverlap="+worst+" "+offender);
    h.assertTrue(worst<.001,"Production bed overlaps original native car "+r.design+": "+worst+" "+offender);
  }
  private static net.minecraft.world.phys.AABB worldBox(ServerSubLevel body,net.minecraft.world.phys.AABB box){
    var min=new Vector3d(Double.POSITIVE_INFINITY);var max=new Vector3d(Double.NEGATIVE_INFINITY);
    for(double x:new double[]{box.minX,box.maxX})for(double y:new double[]{box.minY,box.maxY})for(double z:new double[]{box.minZ,box.maxZ}){var p=body.logicalPose().transformPosition(new Vector3d(x,y,z));min.min(p);max.max(p);}
    return new net.minecraft.world.phys.AABB(min.x,min.y,min.z,max.x,max.y,max.z);
  }
  private static MovingAssemblyState.Pose translated(MovingAssemblyState.Pose p,double x){return new MovingAssemblyState.Pose(p.x()+x,p.y(),p.z(),p.qx(),p.qy(),p.qz(),p.qw());}
  private static double smooth(double t){return t*t*t*t*(35+t*(-84+t*(70-20*t)));}
}
