package dev.fourthdimension.gametest;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
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
import org.joml.Vector3d;
/** Real different-circuit transfer. Setup creates track/car and brakes once; departure uses native Chain. */
@GameTestHolder("fourthdimension_fd2001_powered_handoff")
@PrefixGameTestTemplate(false)
public final class MovingPoweredHandoffGameTests {
 private record Circuit(PoweredInvertedYawExamples.Example example,CoasterPathEdge fixed,CoasterPathEdge gap,boolean reverse){}
 @GameTest(template="empty",timeoutTicks=680)
 public static void differentCircuitKeepsAuthorityAndActuallyDeparts(GameTestHelper h){
  var l=h.getLevel();var o=new BlockPos(200,150,300);MovingRouteGameTests.force(l,o,10,14);var owner=UUID.randomUUID();
  var source=split(l,PoweredInvertedYawExamples.create(l,o.west(80),0,owner),5,owner,true);
  var destination=split(l,PoweredInvertedYawExamples.create(l,o,0,owner),0,owner,false);
  var vehicles=new FoundationData.Vehicle[1];var data=FoundationData.get(l);
  var s=new MovingAssemblyState(UUID.randomUUID(),o.west(30),"transfer",TrackDesign.POWERED_INVERTED_YAW.variant,48);s.configuration="powered_inverted_yaw";s.program=new MovingProgramSpec(MovingMechanism.TRANSFER,16,0,0,false);
  l.setBlockAndUpdate(s.controller,MovingContent.CONTROLLERS.get(MovingMechanism.TRANSFER).get().defaultBlockState());
  var a=dock("a",source.gap);var b=dock("b",destination.gap);s.docks.put("a",a);s.docks.put("b",b);s.source="a";s.destination="b";
  var parts=new ArrayList<MovingNativeAdapter.BlockPlacement>();for(int z=-24;z<=24;z++)for(int x=-1;x<=1;x++)parts.add(new MovingNativeAdapter.BlockPlacement(new BlockPos(x,1,z),MovingContent.PARTS.get(MovingPartBlock.Kind.OVERHEAD_GANTRY).get().defaultBlockState()));
  MovingNativeAdapter.createBed(l,s,a.pose(),parts);data.moving.assemblies.put(s.id,s);
  var chassis=new Vector3d[1];
  h.runAtTickTime(40,()->{
   for(var c:List.of(source,destination)){cut(l,c.gap);commit(l,c.fixed,c.reverse);commit(l,c.gap,c.reverse);}
   TrackGeometry.invalidate(l);
   for(var r:data.rails)if(r.family==TrackDesign.POWERED_INVERTED_YAW.family){var e=TrackGeometry.resolve(l,r);if(e==null)throw new IllegalArgumentException("Missing authored fixed edge");r.geometryHash=TrackGeometry.fingerprint(l,e);r.poweredYaw.yaw.geometry=r.geometryHash;TrackPlacement.captureOriginalCurve(r,e.bezier());}
   MovingNativeAdapter.dock(l,s,a);s.phase=MovingAssemblyState.Phase.DOCKED;commit(l,MovingNativeAdapter.liveDockEdge(l,s),source.reverse);
  });
  h.runAtTickTime(50,()->{
   // Creation fixture supplies the yaw owner metadata normally obtained on the fixed approach.
   // Remove it immediately: the authenticated moving bridge remains the runtime route authority.
   var edge=MovingNativeAdapter.liveDockEdge(l,s);MovingPoweredRouteGameTests.author(l,edge,owner);
   try{vehicles[0]=PoweredInvertedYawVehicle.create(l,FlyingExamples.sample(l,edge,40));}
   finally{data.rails.removeIf(r->r.matches(edge.from(),edge.to()));}
  });
  h.runAtTickTime(85,()->{var v=vehicles[0];
   var physics=SubLevelContainer.getContainer(l).physicsSystem();
   for(var id:List.of(v.chassis,v.carriers.getFirst().body,s.bed)){var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(id);physics.updatePose(body);physics.getPipeline().resetVelocity(body);}
   var g=source.example.governor();g.bind(v);v.restraints=true;v.fault="";v.poweredYaw.resumeRequired=false;
   MovingRouteAdapters.validateExit(l,s,v,b);
   // Bound source is physically present; destination is only a prospective real fixed circuit.
   var ids=List.of(v.chassis,v.carriers.getFirst().body);s.cars=List.of(v.chassis);
   MovingNativeAdapter.capture(l,s,ids);s.phase=MovingAssemblyState.Phase.CAPTURED;MovingNativeAdapter.disconnect(l,s);s.phase=MovingAssemblyState.Phase.DISCONNECTED;
   chassis[0]=new Vector3d(((ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis)).logicalPose().position());
   System.out.println("FD2001_POWERED_CAPTURE source="+a.from()+" destination="+b.from()+" sameOwner="+v.authority.owner+" actualBodies="+s.bodies.keySet());
  });
  for(int tick=86;tick<=285;tick++){final int t=tick;h.runAtTickTime(tick,()->{double u=(t-85)/200.0;double p=u*u*u*u*(35+u*(-84+u*(70-20*u)));MovingNativeAdapter.drive(l,s,new MovingAssemblyState.Pose(a.pose().x()+16*p,a.pose().y(),a.pose().z(),0,0,0,1),.05);});}
  for(int tick=286;tick<=345;tick++)h.runAtTickTime(tick,()->MovingNativeAdapter.drive(l,s,b.pose(),.05));
  h.runAtTickTime(350,()->{var v=vehicles[0];
   MovingNativeAdapter.readback(l,s);System.out.println("FD2001_POWERED_PREDOCK pose="+s.actualPose+" target="+b.pose()+" linear="+s.linearVelocity+" angular="+s.angularVelocity+" phase="+s.phase+" fault="+s.fault);
   FoundationData.get(l).moving.reserve(s,MovingReservations.validate(l,s,b,Math.max(4,s.receivingLength)));
   MovingNativeAdapter.dock(l,s,b);s.phase=MovingAssemblyState.Phase.RECONNECTED;
   h.assertTrue(((ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis)).logicalPose().position().distance(new Vector3d(chassis[0]).add(16,0,0))<.08,"Powered bodies did not reach distinct destination");
   MovingAdmission.validateRelease(l,s,b);MovingServiceRuntime.prepare(l,s,b);MovingNativeAdapter.release(l,s);MovingAdmission.released(l,s,b);s.released=true;s.phase=MovingAssemblyState.Phase.RELEASING;
   h.assertTrue(source.example.governor().authorityId().equals(v.authority.owner),"Destination changed propulsion authority identity");
   h.assertTrue(source.example.governor().systemAvailability()==null,"Destination real governor circuit unavailable: "+source.example.governor().systemAvailability());
   System.out.println("FD2001_POWERED_DESTINATION adopted="+source.example.governor().routeIds()+" sourceOutput="+source.example.governor().outputRpm()+" authority="+v.authority.owner);
  });
  for(int tick=351;tick<=361;tick+=5){final int t=tick;h.runAtTickTime(tick,()->{MovingNativeAdapter.readback(l,s);System.out.println("FD2001_POWERED_RELEASE_STATE tick="+t+" pose="+s.actualPose+" linear="+s.linearVelocity+" angular="+s.angularVelocity+" phase="+s.phase+" fault="+s.fault+" bridge="+(MovingNativeAdapter.liveDockEdge(l,s)!=null));});}
  h.runAtTickTime(365,()->{var v=vehicles[0];
   var g=source.example.governor();System.out.println("FD2001_POWERED_PRE_DISPATCH availability="+g.systemAvailability()+" fault="+v.fault+" reason="+v.authority.reason);g.acknowledge(v);g.release(v);VehicleOperations.dispatch(l,v);
   h.assertTrue(!v.authority.held&&s.service.cars.get(v.chassis).dispatched,"Explicit normal governor dispatch did not release service");
  });
  for(int tick=385;tick<=645;tick+=20){final int t=tick;h.runAtTickTime(tick,()->{var v=vehicles[0];
   var p=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);System.out.println("FD2001_POWERED_DEPART tick="+t+" position="+p.logicalPose().position()+" fault="+v.fault+" authority="+v.authority.reason+" held="+v.authority.held+" service="+MovingServiceRuntime.applies(l,v)+" assemblyPhase="+s.phase+" assemblyFault="+s.fault+" bounds="+MovingAdmission.carBounds(l,s,v));
  });}
  h.runAtTickTime(650,()->{var v=vehicles[0];
   var p=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
   h.assertTrue(Math.abs(p.logicalPose().position().z-chassis[0].z)>8,"Actual native Chain did not move the released Powered train");
   h.assertTrue(!MovingServiceRuntime.applies(l,v),"Complete Powered car and carrier did not clear destination bridge");
   h.assertTrue(v.fault.isEmpty(),"Destination departure fault: "+v.fault);
   h.assertTrue(source.example.governor().authorityId().equals(v.authority.owner),"Authority changed during native departure");
   System.out.println("FD2001_POWERED_HANDOFF_PASS transfer=16 realNativeChainDeparture=true completeCarrierExit=true sameAuthority=true");h.succeed();
  });
 }
 private static Circuit split(net.minecraft.server.level.ServerLevel l,PoweredInvertedYawExamples.Example e,int index,UUID owner,boolean reverse){
  var old=e.edges().get(index);var low=old.from().getZ()<old.to().getZ()?old.from():old.to();var high=old.from().getZ()>old.to().getZ()?old.from():old.to();var split=low.offset(0,0,16);
  l.setBlockAndUpdate(split,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());cut(l,old);
  var fixed=FlyingExamples.connect(l,low,split,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var gap=FlyingExamples.connect(l,split,high,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  MovingPoweredRouteGameTests.author(l,fixed,owner);MovingPoweredRouteGameTests.author(l,gap,owner);
  for(int n=1;n<=3;n++)l.setBlockAndUpdate(split.east(n),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  l.setBlockAndUpdate(split.east(4),PoweredInvertedYawContent.GOVERNOR.get().defaultBlockState().setValue(PoweredInvertedYawGovernorBlock.FACING,Direction.WEST));
  l.setBlockAndUpdate(split.east(5),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));((CreativeMotorBlockEntity)l.getBlockEntity(split.east(5))).generatedSpeed.setValue(128);
  return new Circuit(e,fixed,gap,reverse);
 }
 private static MovingAssemblyState.Dock dock(String name,CoasterPathEdge gap){var a=gap.bezier().starts.getFirst();var b=gap.bezier().starts.getSecond();return new MovingAssemblyState.Dock(name,UUID.randomUUID(),gap.from(),gap.to(),new MovingAssemblyState.Pose((a.x+b.x)/2,(a.y+b.y)/2+.25,(a.z+b.z)/2,0,0,0,1));}
 private static void cut(net.minecraft.server.level.ServerLevel l,CoasterPathEdge e){((CoasterAnchorpointBlockEntity)l.getBlockEntity(e.from())).removeAnchorPeerCurve(l,e.to());FoundationData.get(l).rails.removeIf(r->r.matches(e.from(),e.to()));}
 private static void commit(net.minecraft.server.level.ServerLevel l,CoasterPathEdge e,boolean reverse){var from=reverse?e.to():e.from();var to=reverse?e.from():e.to();var a=(CoasterAnchorpointBlockEntity)l.getBlockEntity(from);a.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(to),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(e,reverse?1:0,reverse?0:1)),TrackGeometry.table(l,e).length()));}
}
