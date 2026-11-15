package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
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
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Quaterniond;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd1802_route")
@PrefixGameTestTemplate(false)
public final class NewGenerationFlyingRouteGameTests {
 record Rig(FoundationData.Vehicle vehicle,FoundationData.Rail rail,BlockPos marker,UUID markerId,UUID owner){}
 @GameTest(template="empty",timeoutTicks=2450)
 public static void targetWindingAndShow(GameTestHelper h){
  var l=h.getLevel();var rigs=new ArrayList<Rig>();double[] held={0},peak={0,0},old={0,0,0};
  h.runAtTickTime(1,()->{for(int n=0;n<3;n++)rigs.add(create(h,n));});
  h.runAtTickTime(20,()->{for(var r:rigs)r.vehicle.yaw.mode=YawState.Mode.PROFILE;});
  h.runAtTickTime(190,()->{for(var r:rigs){heading(h,r,"seated");h.assertTrue(r.vehicle.flying.lock==FlyingState.Lock.SEATED,"Seated actual lock");section(r,Math.PI/2,YawState.Mode.TARGET,0,0);}});
  h.runAtTickTime(420,()->{for(var r:rigs){heading(h,r,"prone");h.assertTrue(r.vehicle.flying.lock==FlyingState.Lock.PRONE,"Prone actual lock");l.removeBlock(r.marker,false);}});
  h.runAtTickTime(450,()->{for(var r:rigs){h.assertTrue(r.vehicle.yaw.mode==YawState.Mode.HOLD_FAULT&&r.vehicle.fault.equals("MARKER_MISSING"),"Missing marker has specific retained hold: "+r.vehicle.fault);h.assertTrue(Math.abs(r.vehicle.yaw.velocity)<.05&&r.vehicle.flying.retained(),"Marker fault brakes retained");l.setBlockAndUpdate(r.marker,YawMarkers.MARKER.get().defaultBlockState());var m=(YawMarkerEntity)l.getBlockEntity(r.marker);m.id=r.markerId;m.owner=r.owner;m.setChanged();NewGenerationFlyingRuntime.retry(l,r.vehicle);}});
  h.runAtTickTime(600,()->{for(var r:rigs)heading(h,r,"restored target");section(rigs.getFirst(),Math.PI/2,YawState.Mode.PROFILE,4*Math.PI,4*Math.PI);});
  h.runAtTickTime(1200,()->{var r=rigs.getFirst();var v=r.vehicle;h.assertTrue(Math.abs(v.yaw.angle-4*Math.PI)<Math.toRadians(2),"Native yaw retains two complete turns: "+v.yaw.angle+" / "+v.fault);held[0]=v.yaw.lastDistance;section(r,Math.PI/2,YawState.Mode.SHOW_STOP,4*Math.PI,5*Math.PI);});
  h.runAtTickTime(1205,()->{var v=rigs.getFirst().vehicle;var id=UUID.randomUUID();long epoch=v.yaw.epoch;h.assertTrue(NewGenerationFlyingRuntime.show(l,v,id,epoch,"Native show"),"Named hold acknowledged");h.assertTrue(!NewGenerationFlyingRuntime.show(l,v,id,epoch,"Native show"),"Duplicate/stale show ignored");});
  h.runAtTickTime(1660,()->{var r=rigs.getFirst();var v=r.vehicle;h.assertTrue(Math.abs(v.yaw.angle-5*Math.PI)<Math.toRadians(2),"Tick show actual yaw: "+v.yaw.angle+" / "+v.fault);h.assertTrue(Math.abs(v.yaw.lastDistance-held[0])<.01&&v.stopping&&!v.yaw.travelAuthorized,"Show holds canonical distance and drive");section(r,Math.PI/2,YawState.Mode.PROFILE,8*Math.PI,8*Math.PI);});
  for(int tick=2;tick<2400;tick++){final int t=tick;h.runAtTickTime(t,()->{for(int n=0;n<rigs.size();n++){var v=rigs.get(n).vehicle;peak[0]=Math.max(peak[0],Math.abs(v.yaw.velocity));if(t>3)peak[1]=Math.max(peak[1],Math.abs(v.yaw.velocity-old[n])*20);old[n]=v.yaw.velocity;}
   if(t%100==0)for(int n=0;n<rigs.size();n++){var v=rigs.get(n).vehicle;System.out.println("FD_NG_ROUTE tick="+t+" bank="+n*90+" psi="+v.yaw.angle+" omega="+v.yaw.velocity+" phi="+(-v.carriers.getFirst().angle)+" phiDot="+v.carriers.getFirst().omega+" lock="+v.flying.lock+" mode="+v.yaw.mode+" status="+v.yaw.status+" fault="+v.fault);}
  });}
  h.runAtTickTime(2400,()->{var v=rigs.getFirst().vehicle;h.assertTrue(v.yaw.mode==YawState.Mode.HOLD_FAULT&&v.fault.equals("WINDING_LIMIT"),"Winding exhaustion is bounded explicit fault: "+v.yaw.angle+" / "+v.fault);h.assertTrue(v.yaw.angle>7.9*Math.PI&&v.yaw.angle<=8*Math.PI+.01&&Math.abs(v.yaw.velocity)<.05,"Winding held near actual positive boundary");h.assertTrue(peak[0]<.61&&peak[1]<.82,"Banked target native rates bounded "+Arrays.toString(peak));System.out.println("FD_NG_ROUTE PASS banks=0,90,180 postures=seated,prone targetFault=missing recovery=explicit fullTurns=2 showTurns=2.5 windingLimit=4 peaks="+Arrays.toString(peak));h.succeed();});
 }
 static Rig create(GameTestHelper h,int index){
  var l=h.getLevel();var from=new BlockPos(-800+index*100,120,400);var to=from.offset(0,0,100);for(int x=(from.getX()>>4)-1;x<=(from.getX()>>4)+1;x++)for(int z=24;z<=32;z++)l.setChunkForced(x,z,true);
  for(var p:List.of(from,to)){l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());((CoasterAnchorpointBlockEntity)l.getBlockEntity(p)).setJointBankRadians((float)(index*Math.PI/2));}
  var tangent=new Vec3(0,0,1);var edge=FlyingExamples.connect(l,from,to,tangent,tangent.scale(-1),CoasterTrackPlacement.anchorFaceNormalForAxis(l,from,tangent),CoasterTrackPlacement.anchorFaceNormalForAxis(l,to,tangent));double length=TrackGeometry.table(l,edge).length();
  var rail=new FoundationData.Rail(from,to,Family.FLYING,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),NewGenerationFlyingVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);FoundationData.get(l).rails.add(rail);rail.yaw.geometry=rail.geometryHash;rail.yaw.owner=UUID.randomUUID();
  var anchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(from);anchor.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(to),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),length));
  l.setBlockAndUpdate(from.east(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));((CreativeMotorBlockEntity)l.getBlockEntity(from.east())).generatedSpeed.setValue(32*anchor.getChainLiftTravelSign());
  var v=NewGenerationFlyingVehicle.create(l,FlyingExamples.sample(l,edge,40));v.yaw.owner=rail.yaw.owner;v.flying.body=true;v.flying.legs=true;v.restraints=true;
  var chassis=body(h,v.chassis);var p=chassis.getPlot().getCenterBlock();var pivot=chassis.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+NewGenerationFlyingVehicle.PIVOT,p.getZ()+.5));
  var world=new Vector3d(12,5,24).rotate(new Quaterniond(chassis.logicalPose().orientation()).rotateY(Math.PI)).add(pivot);var pos=BlockPos.containing(world.x,world.y,world.z);l.setBlockAndUpdate(pos,YawMarkers.MARKER.get().defaultBlockState());var marker=(YawMarkerEntity)l.getBlockEntity(pos);marker.owner=v.yaw.owner;marker.setChanged();
  var dock=from.offset(4,-4,40);l.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;v.flying.dockFrom=from;v.flying.dockTo=to;v.flying.dockGeometry=rail.geometryHash;
  var rig=new Rig(v,rail,pos,marker.id,v.yaw.owner);section(rig,0,YawState.Mode.TARGET,0,0);v.yaw.mode=YawState.Mode.STATION_CAPTURE;return rig;
 }
 static void section(Rig r,double phi,YawState.Mode mode,double a,double b){double length=r.rail.profile.keys().getLast().distance();r.rail.yaw.intervals=List.of(new YawSection.Interval(UUID.randomUUID(),"Native",0,length,mode,0,phi,phi,new RotationProfile(List.of(new RotationProfile.Key(0,a),new RotationProfile.Key(length,b))),mode==YawState.Mode.TARGET?r.markerId:null,mode==YawState.Mode.TARGET?r.marker:null,mode==YawState.Mode.TARGET?"minecraft:overworld":"",YawState.Winding.NEAREST,.6,.8,mode==YawState.Mode.SHOW_STOP?"Native show":"",mode==YawState.Mode.SHOW_STOP?400:0));r.rail.yaw.revision++;r.vehicle.yaw.section=null;r.vehicle.yaw.mode=YawState.Mode.PROFILE;r.vehicle.yaw.status="READY";r.vehicle.yaw.holdAcknowledged=false;}
 private static ServerSubLevel body(GameTestHelper h,UUID id){return (ServerSubLevel)SubLevelContainer.getContainer(h.getLevel()).getSubLevel(id);}
 private static void heading(GameTestHelper h,Rig r,String phase){var v=r.vehicle;h.assertTrue(v.fault.isEmpty(),phase+" fault: "+v.fault);var chassis=body(h,v.chassis);var deck=body(h,v.yaw.deck);var p=deck.getPlot().getCenterBlock();var pivot=deck.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5));var expected=new Vector3d(r.marker.getX()+.5,r.marker.getY()+.5,r.marker.getZ()+.5).sub(pivot);var normal=new Vector3d(0,1,0).rotate(chassis.logicalPose().orientation());expected.sub(new Vector3d(normal).mul(expected.dot(normal))).normalize();var actual=new Vector3d(0,0,-1).rotate(deck.logicalPose().orientation());double dot=actual.dot(expected);System.out.println("FD_NG_TARGET phase="+phase+" dot="+dot+" phi="+(-v.carriers.getFirst().angle));h.assertTrue(dot>=Math.cos(Math.toRadians(2)),phase+" actual projected heading error "+dot);}
}
