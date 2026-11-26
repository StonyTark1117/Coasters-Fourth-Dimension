package dev.fourthdimension.gametest;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.track.OrdinaryGearCertificate;
import dev.fourthdimension.vehicle.MotorbikeExamples;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;
/** Maximum four-car native empty service train and an actual fifth linked bearing refusal. */
@GameTestHolder("fourthdimension_fd2001_train")
@PrefixGameTestTemplate(false)
public final class MovingTrainLimitGameTests {
 @GameTest(template="empty",timeoutTicks=450)
 public static void maximumNativeTrainAndOneBeyond(GameTestHelper h){
  var l=h.getLevel();var o=new BlockPos(-1500,140,400);MovingRouteGameTests.force(l,o,4,5);
  var s=new MovingAssemblyState(UUID.randomUUID(),o.west(12),"transfer",MovingCapabilityCatalog.ORDINARY_VARIANT,48);s.configuration="native_service";s.program=new MovingProgramSpec(MovingMechanism.TRANSFER,4,0,0,false);
  var pose=new MovingAssemblyState.Pose(o.getX()+.5,o.getY()+.5,o.getZ()+.5,0,0,0,1);
  var target=new MovingAssemblyState.Pose(pose.x()+4,pose.y(),pose.z(),0,0,0,1);
  var a=new MovingAssemblyState.Dock("a",UUID.randomUUID(),o.offset(0,0,-24),o.offset(0,0,24),pose);
  var b=new MovingAssemblyState.Dock("b",UUID.randomUUID(),o.offset(4,0,-24),o.offset(4,0,24),target);s.docks.put("a",a);s.docks.put("b",b);s.source="a";s.destination="b";
  for(var d:s.docks.values())for(var p:List.of(d.from(),d.to(),d.from().offset(0,0,-24),d.to().offset(0,0,24)))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var parts=new ArrayList<MovingNativeAdapter.BlockPlacement>();for(int z=-24;z<=24;z++)for(int x=-1;x<=1;x++)parts.add(new MovingNativeAdapter.BlockPlacement(new BlockPos(x,-1,z),MovingContent.PARTS.get(MovingPartBlock.Kind.BED).get().defaultBlockState()));
  MovingNativeAdapter.createBed(l,s,pose,parts);
  for(var d:s.docks.values()){
   MotorbikeExamples.connect(l,d.from().offset(0,0,-24),d.from(),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
   MotorbikeExamples.connect(l,d.to(),d.to().offset(0,0,24),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  }
  var cars=new ArrayList<ServerSubLevel>();var starts=new HashMap<UUID,Vector3d>();
  h.runAtTickTime(40,()->{MovingNativeAdapter.dock(l,s,a);var e=MovingNativeAdapter.liveDockEdge(l,s);if(e==null)e=MovingNativeAdapter.previewDockEdge(l,s,a);for(int i=0;i<4;i++)cars.add(spawn(h,e,9+6*i));});
  h.runAtTickTime(85,()->{
   for(int i=0;i<3;i++)h.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(l,cars.get(i),cars.get(i+1),6),"Actual native coupling failed");
   stop(h,s,cars);var capture=MovingAdmission.inspect(l,s);h.assertTrue(capture.cars().size()==4&&capture.bodies().size()==4&&capture.train().passengers()==0,"Maximum complete native train was not admitted");
   s.cars=capture.cars();MovingNativeAdapter.capture(l,s,capture.bodies());cars.forEach(c->starts.put(c.getUniqueId(),new Vector3d(c.logicalPose().position())));MovingNativeAdapter.disconnect(l,s);
   System.out.println("FD2001_MAXTRAIN_CAPTURE cars="+capture.cars()+" bounds="+capture.train().occupiedBounds());
  });
  for(int tick=86;tick<=185;tick++){final int t=tick;h.runAtTickTime(tick,()->{double x=(t-85)/100.0;double p=x*x*x*x*(35+x*(-84+x*(70-20*x)));MovingNativeAdapter.drive(l,s,new MovingAssemblyState.Pose(pose.x()+4*p,pose.y(),pose.z(),0,0,0,1),.05);});}
  for(int tick=186;tick<=245;tick++)h.runAtTickTime(tick,()->MovingNativeAdapter.drive(l,s,target,.05));
  h.runAtTickTime(250,()->{
   for(var c:cars)h.assertTrue(c.logicalPose().position().distance(new Vector3d(starts.get(c.getUniqueId())).add(4,0,0))<.08,"Maximum train body did not follow physical bed");
   MovingNativeAdapter.dock(l,s,b);MovingNativeAdapter.release(l,s);s.source="b";
   var e=MovingNativeAdapter.previewDockEdge(l,s,b);cars.add(spawn(h,e,33));
  });
  h.runAtTickTime(285,()->{
   h.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(l,cars.get(3),cars.get(4),6),"Native fifth setup coupling failed before moving admission");stop(h,s,cars);
   var ids=CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(cars.getFirst());h.assertTrue(ids.size()==5,"Fifth native identity absent");
   MovingRouteGameTests.expect(h,"at most four",()->MovingAdmission.inspect(l,s));
   h.assertTrue(!s.retained()&&CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(cars.getFirst()).equals(ids),"Denial changed links or retained train");
   System.out.println("FD2001_MAXTRAIN_PASS movedCars=4 distance=4 fifthNativeLinked=true refusalNoMutation=true");h.succeed();
  });
 }
 private static ServerSubLevel spawn(GameTestHelper h,CoasterPathEdge edge,double distance){
  var l=h.getLevel();var hit=MotorbikeExamples.sample(l,edge,distance);var q=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(l,hit));
  var origin=CoasterCartLinkPlacement.plotOriginForBearingCenter(CoasterPathTrackFrame.pathAnchorForSnappedBlock(l,hit),q);
  var body=CoasterCartSpawner.spawnMinimalContraption(l,origin,q,hit,true);h.assertTrue(body!=null&&OrdinaryGearCertificate.fits(body),"Unmodified native bearing certificate failed");return body;
 }
 private static void stop(GameTestHelper h,MovingAssemblyState s,List<ServerSubLevel> cars){var physics=SubLevelContainer.getContainer(h.getLevel()).physicsSystem();cars.forEach(c->{physics.updatePose(c);physics.getPipeline().resetVelocity(c);});physics.getPipeline().resetVelocity((ServerSubLevel)SubLevelContainer.getContainer(h.getLevel()).getSubLevel(s.bed));}
}
