package dev.fourthdimension.vehicle;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
/** Finite original standing course: broad bends, gentle crest, one native Chain travel owner. */
public final class StandingExamples {
 public record Example(List<CoasterPathEdge> edges,FoundationData.Vehicle vehicle,BlockPos station){}
 private StandingExamples(){}
 public static void register(RegisterCommandsEvent event){var demo=Commands.literal("demo");for(var d:List.of(TrackDesign.STANDING_STANDARD,TrackDesign.STANDING_SURF))for(boolean car:new boolean[]{true,false})demo.then(Commands.literal((d==TrackDesign.STANDING_SURF?"standing_surf":"standing_standard")+(car?"":"_track")).executes(ctx->{try{create(ctx.getSource().getLevel(),BlockPos.containing(ctx.getSource().getPosition()).offset(0,3,12),d,car);ctx.getSource().sendSuccess(()->Component.literal("Standing loop built. Board at level dock, close restraints, Dispatch. Stop the returning car on the long dock; Surf must settle to home before Open and Shift. Power or support faults require repair and explicit Resume."),false);return 1;}catch(IllegalArgumentException e){ctx.getSource().sendFailure(Component.literal(e.getMessage()));return 0;}}));event.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2)).then(demo));}
 public static Example create(ServerLevel l,BlockPos origin,TrackDesign design,boolean car){StandingVehicle.fit(design);if(Math.abs((long)origin.getX())>750||Math.abs((long)origin.getZ())>750)throw new IllegalArgumentException("Choose standing example near world origin");for(var p:BlockPos.betweenClosed(origin.offset(-8,-3,-40),origin.offset(104,12,136)))if(!l.getBlockState(p).isAir())throw new IllegalArgumentException("Standing example needs an empty 113 x 16 x 177 area: "+p);
  int[][] offsets={{0,0,0},{0,0,48},{0,0,96},{32,4,128},{64,4,128},{96,0,96},{96,0,48},{96,0,0},{64,0,-32},{32,0,-32}};var anchors=new ArrayList<BlockPos>();var tangents=new ArrayList<Vec3>();var edges=new ArrayList<CoasterPathEdge>();var data=FoundationData.get(l);data.checkProfileCapacity(20);if(data.rails.size()>246||data.vehicles.size()>=64)throw new IllegalArgumentException("Standing example capacity exceeded");
  for(int i=0;i<10;i++){var o=offsets[i];var p=origin.offset(o[0],o[1],o[2]);anchors.add(p);tangents.add(i<3?new Vec3(0,0,1):i<5?new Vec3(1,0,0):i<8?new Vec3(0,0,-1):new Vec3(-1,0,0));l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());}
  for(int i=0;i<10;i++){int next=(i+1)%10;var e=PipelineExamples.connect(l,anchors.get(i),anchors.get(next),tangents.get(i),tangents.get(next).scale(-1),new Vec3(0,1,0),new Vec3(0,1,0));edges.add(e);var length=TrackGeometry.table(l,e).length();var r=new FoundationData.Rail(e.from(),e.to(),Family.STANDING,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),design.variant,TrackGeometry.fingerprint(l,e),0,true);TrackPlacement.captureOriginalCurve(r,e.bezier());data.rails.add(r);}
  TrackGeometry.invalidate(l);for(var r:data.rails)if(anchors.contains(r.from)){var e=TrackGeometry.resolve(l,r);r.geometryHash=TrackGeometry.fingerprint(l,e);r.profile=r.profile.scaledTo(TrackGeometry.table(l,e).length());}
  for(var e:edges){String reason=StandingTrack.rejection(l,e,design);if(reason!=null)throw new IllegalArgumentException("Standing example: "+reason);}
  for(int i:new int[]{1,6}){var a=(CoasterAnchorpointBlockEntity)l.getBlockEntity(anchors.get(i));var e=edges.get(i);var axis=Math.abs(tangents.get(i).z)>.5?Direction.Axis.X:Direction.Axis.Z;var direction=axis==Direction.Axis.X?Direction.EAST:Direction.SOUTH;var powered=new ArrayList<CoasterPathEdge>();for(int part=0;part<5;part++)powered.add(edges.get((i+part)%edges.size()));var path=new CoasterPathGraphSpans.SpanPath(powered.stream().map(edge->new CoasterPathGraphSpans.PathSegment(edge,0,1)).toList(),powered.stream().mapToDouble(edge->TrackGeometry.table(l,edge).length()).sum());a.commitChainLift(axis,ChainLiftEndRef.anchor(powered.getLast().to()),path);for(int step=1;step<=2;step++)l.setBlockAndUpdate(anchors.get(i).relative(direction,step),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,axis));var motorPos=anchors.get(i).relative(direction,3);l.setBlockAndUpdate(motorPos,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,direction.getOpposite()));((CreativeMotorBlockEntity)l.getBlockEntity(motorPos)).generatedSpeed.setValue(64*a.getChainLiftTravelSign());}
  var v=car?StandingVehicle.create(l,PipelineExamples.sample(l,edges.getFirst(),12),design):null;var dock=origin.offset(-4,1,18);l.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());var station=(BoardingStationEntity)l.getBlockEntity(dock);if(v!=null){station.vehicle=v.chassis;v.dock=dock;v.fixedDockFrom=edges.getFirst().from();v.fixedDockTo=edges.getFirst().to();v.fixedDockGeometry=StandingRuntime.rail(l,v).geometryHash;}station.setChanged();
  for(int side:new int[]{-1,1})for(var p:BlockPos.betweenClosed(origin.offset(side*3,1,-4),origin.offset(side*6,1,96)))if(!p.equals(dock))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());data.setDirty();FoundationSync.broadcast(l);return new Example(List.copyOf(edges),v,dock);
 }
}
