package dev.fourthdimension.gametest;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
/** Actual fixed powered circuit, real Chain on a locked service bridge, normal governor bind. */
@GameTestHolder("fourthdimension_fd2001_powered_route")
@PrefixGameTestTemplate(false)
public final class MovingPoweredRouteGameTests {
 @GameTest(template="empty",timeoutTicks=230)
 public static void poweredCandidateRetainsRealGovernedCircuit(GameTestHelper h){
  var l=h.getLevel();var o=new BlockPos(200,150,300);MovingRouteGameTests.force(l,o,8,14);
  var owner=UUID.randomUUID();var e=PoweredInvertedYawExamples.create(l,o,1,owner);var v=e.cars().getFirst();var old=e.edges().getFirst();var split=o.offset(0,0,16);
  l.setBlockAndUpdate(split,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  ((CoasterAnchorpointBlockEntity)l.getBlockEntity(old.from())).removeAnchorPeerCurve(l,old.to());
  var data=FoundationData.get(l);data.rails.removeIf(r->r.matches(old.from(),old.to()));
  var start=FlyingExamples.connect(l,old.from(),split,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var gap=FlyingExamples.connect(l,split,old.to(),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  author(l,start,owner);author(l,gap,owner);v.boundFrom=start.from();v.boundTo=start.to();
  // The new fixed 16-block section and 48-block bridge each retain a distinct real governor.
  for(int n=1;n<=3;n++)l.setBlockAndUpdate(split.east(n),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  l.setBlockAndUpdate(split.east(4),PoweredInvertedYawContent.GOVERNOR.get().defaultBlockState().setValue(PoweredInvertedYawGovernorBlock.FACING,Direction.WEST));
  l.setBlockAndUpdate(split.east(5),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
  ((CreativeMotorBlockEntity)l.getBlockEntity(split.east(5))).generatedSpeed.setValue(128);
  var s=new MovingAssemblyState(UUID.randomUUID(),o.west(12),"transfer",v.variant,48);s.configuration="powered_inverted_yaw";
  l.setBlockAndUpdate(s.controller,MovingContent.CONTROLLERS.get(MovingMechanism.TRANSFER).get().defaultBlockState());
  var first=gap.bezier().starts.getFirst();var last=gap.bezier().starts.getSecond();
  var pose=new MovingAssemblyState.Pose((first.x+last.x)/2,(first.y+last.y)/2+.25,(first.z+last.z)/2,0,0,0,1);
  var dock=new MovingAssemblyState.Dock("a",UUID.randomUUID(),split,old.to(),pose);s.docks.put("a",dock);s.source="a";s.destination="a";
  var parts=new ArrayList<MovingNativeAdapter.BlockPlacement>();for(int z=-24;z<=24;z++)for(int x=-1;x<=1;x++)parts.add(new MovingNativeAdapter.BlockPlacement(new BlockPos(x,1,z),MovingContent.PARTS.get(MovingPartBlock.Kind.OVERHEAD_GANTRY).get().defaultBlockState()));
  MovingNativeAdapter.createBed(l,s,pose,parts);data.moving.assemblies.put(s.id,s);
  h.runAtTickTime(40,()->{
   ((CoasterAnchorpointBlockEntity)l.getBlockEntity(gap.from())).removeAnchorPeerCurve(l,gap.to());data.rails.removeIf(r->r.matches(gap.from(),gap.to()));TrackGeometry.invalidate(l);
   // Author final fixed geometry after all native junction propagation. No accepted ride is edited.
   for(var r:data.rails)if(r.family==v.family){var edge=TrackGeometry.resolve(l,r);if(edge==null)throw new IllegalArgumentException("Fixed powered destination edge missing");r.geometryHash=TrackGeometry.fingerprint(l,edge);r.poweredYaw.yaw.geometry=r.geometryHash;r.valid=true;TrackPlacement.captureOriginalCurve(r,edge.bezier());}
   MovingNativeAdapter.dock(l,s,dock);s.phase=MovingAssemblyState.Phase.DOCKED;
   var live=MovingNativeAdapter.liveDockEdge(l,s);h.assertTrue(live!=null,"Real locked powered service bridge missing");
   commit(l,start);commit(l,live);
  });
  h.runAtTickTime(85,()->{
   e.governor().bind(v);h.assertTrue(PoweredInvertedYawRuntime.governor(l,v)==e.governor(),"Normal powered governor authority absent");
   var before=List.copyOf(data.rails);MovingRouteAdapters.validateExit(l,s,v,dock);
   var target=before.stream().filter(r->r.family==v.family).flatMap(r->r.poweredYaw.yaw.intervals.stream()).filter(i->i.mode()==YawState.Mode.TARGET).findFirst().orElseThrow();
   var marker=(YawMarkerEntity)l.getBlockEntity(target.markerPos());var savedOwner=marker.owner;marker.owner=UUID.randomUUID();
   MovingRouteGameTests.expect(h,"owned yaw target",()->MovingRouteAdapters.validateExit(l,s,v,dock));marker.owner=savedOwner;
   MovingRouteAdapters.validateExit(l,s,v,dock);h.assertTrue(data.rails.equals(before)&&MovingRouteContext.start(l,v)==null,"Powered route view mutated or leaked");
   System.out.println("FD2001_ROUTE_PASS POWERED_YAW normalGovernorBind=true actualFixedAndBridgeChain=true ownedTargetRefusal=true");h.succeed();
  });
 }
 private static void commit(net.minecraft.server.level.ServerLevel l,CoasterPathEdge e){var a=(CoasterAnchorpointBlockEntity)l.getBlockEntity(e.from());a.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(e.to()),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(e,0,1)),TrackGeometry.table(l,e).length()));}
 static void author(net.minecraft.server.level.ServerLevel l,CoasterPathEdge e,UUID owner){double length=TrackGeometry.table(l,e).length();var profile=new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0)));var r=new FoundationData.Rail(e.from(),e.to(),TrackDesign.POWERED_INVERTED_YAW.family,profile,UUID.randomUUID(),TrackDesign.POWERED_INVERTED_YAW.variant,TrackGeometry.fingerprint(l,e),0,true);r.poweredYaw.yaw.owner=owner;r.poweredYaw.yaw.geometry=r.geometryHash;r.poweredYaw.yaw.intervals=List.of(new YawSection.Interval(UUID.randomUUID(),"Service approach",0,length,YawState.Mode.PROFILE,0,0,0,profile,null,null,"",YawState.Winding.NEAREST,.45,.45,"",0));r.poweredYaw.speeds=List.of(new PoweredInvertedYawSection.Speed(0,length,2));TrackPlacement.captureOriginalCurve(r,e.bezier());FoundationData.get(l).rails.add(r);}
}
