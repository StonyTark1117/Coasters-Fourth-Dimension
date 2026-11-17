package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.track.BezierConnection;
import net.createmod.catnip.data.Couple;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.track.CoasterBezierHandleEdit;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Disclosed example setup; all operation afterwards uses normal governor/station/car controls. */
@EventBusSubscriber(modid="fourthdimension")
public final class PoweredInvertedYawExamples {
  public record Example(List<CoasterPathEdge> edges,List<FoundationData.Vehicle> cars,
      BlockPos station,PoweredInvertedYawGovernorEntity governor){}
  private record Pending(Example example,long deadline){}
  private static final Map<ServerLevel,List<Pending>> PENDING=new WeakHashMap<>();
  private PoweredInvertedYawExamples(){}
  @SubscribeEvent public static void register(RegisterCommandsEvent event){
    var demo=Commands.literal("demo");
    for(int n:new int[]{0,1,4})demo.then(Commands.literal(n==0?"powered_inverted_yaw_track":n==1?"powered_inverted_yaw":"powered_inverted_yaw_train").executes(ctx->{
      try {var l=ctx.getSource().getLevel();var e=create(l,BlockPos.containing(ctx.getSource().getPosition()).offset(0,8,12),n,ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player?player.getUUID():new UUID(0,0));
        if(n>0)PENDING.computeIfAbsent(l,k->new ArrayList<>()).add(new Pending(e,l.getGameTime()+400));
        ctx.getSource().sendSuccess(()->Component.literal("Powered upright yaw circuit built. Wait for captured coupling; board, close restraints and Dispatch. It tracks scenery, performs a yaw show and reverses. Capture on return to the level dock; wait for unwind before opening restraints."),false);return 1;
      }catch(IllegalArgumentException ex){ctx.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}
    }));
    event.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2)).then(demo));
  }
  @SubscribeEvent public static void tick(LevelTickEvent.Post event){
    if(!(event.getLevel() instanceof ServerLevel l))return;var pending=PENDING.get(l);if(pending==null)return;
    for(var it=pending.iterator();it.hasNext();){var p=it.next();
      if(l.getGameTime()>p.deadline){it.remove();continue;}
      if(p.example.cars.stream().allMatch(v->v.boardingReady&&Boarding.ready(l,v))){it.remove();bind(l,p.example);}
    }
  }
  public static void bind(ServerLevel l,Example e){
    var container=SubLevelContainer.getContainer(l);
    for(int i=0;i<e.cars.size()-1;i++)if(!CoasterCartTrainLinkConstraint.linkCarts(l,
        (ServerSubLevel)container.getSubLevel(e.cars.get(i).chassis),
        (ServerSubLevel)container.getSubLevel(e.cars.get(i+1).chassis),6))
      throw new IllegalArgumentException("Wait for each empty stopped dock latch before coupling");
    PoweredInvertedYawRuntime.rebindDock(l,e.cars.getFirst(),e.station);e.governor.bind(e.cars.getFirst());
    FoundationData.get(l).setDirty();FoundationSync.broadcast(l);
  }
  public static Example create(ServerLevel l,BlockPos origin,int count,UUID owner){
    if(count!=0&&count!=1&&count!=4)throw new IllegalArgumentException("Example offers track only, one or four cars");
    for(var p:BlockPos.betweenClosed(origin.offset(-8,-8,-40),origin.offset(72,6,168)))
      if(!l.getBlockState(p).isAir())throw new IllegalArgumentException("Clear powered example area at "+p.toShortString());
    int[][] offsets={{0,0,0},{0,0,64},{0,0,128},{32,0,160},{64,0,128},{64,0,64},{64,0,0},{32,0,-32}};
    var anchors=new ArrayList<BlockPos>();var tangents=new ArrayList<Vec3>();var normals=new ArrayList<Vec3>();
    for(int i=0;i<offsets.length;i++){
      var p=origin.offset(offsets[i][0],offsets[i][1],offsets[i][2]);anchors.add(p);
      l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      var tangent=i<=2?new Vec3(0,0,1):i==3?new Vec3(1,0,0):i<=6?new Vec3(0,0,-1):new Vec3(-1,0,0);
      tangents.add(tangent);normals.add(CoasterTrackPlacement.anchorFaceNormalForAxis(l,p,tangent));
    }
    var starts=new ArrayList<Vec3>();
    for(int i=0;i<anchors.size();i++)starts.add(CoasterBezierHandleEdit.liftedWorldCenterFromCanonicalTangent(l,anchors.get(i),tangents.get(i)));
    var edges=new ArrayList<CoasterPathEdge>();var data=FoundationData.get(l);
    var markerPos=origin.offset(32,-3,96);l.setBlockAndUpdate(markerPos,YawMarkers.MARKER.get().defaultBlockState());var marker=(YawMarkerEntity)l.getBlockEntity(markerPos);marker.owner=owner;marker.setChanged();

    for(int i=0;i<anchors.size();i++){
      int next=(i+1)%anchors.size();
      var curve=new BezierConnection(Couple.create(anchors.get(i),anchors.get(next)),Couple.create(starts.get(i),starts.get(next)),
          Couple.create(tangents.get(i),tangents.get(next).scale(-1)),Couple.create(normals.get(i),normals.get(next)),true,false,CoasterTrackMaterials.COASTER);
      ((CoasterAnchorpointBlockEntity)l.getBlockEntity(anchors.get(i))).putAnchorPeerCurve(l,anchors.get(next),curve);
    }
    // Fingerprint the final native stored curve, after all incident hubs exist.
    for(int i=0;i<anchors.size();i++){
      int next=(i+1)%anchors.size();
      var curve=((CoasterAnchorpointBlockEntity)l.getBlockEntity(anchors.get(i))).getAnchorPeerCurvesView().get(anchors.get(next));
      if(!curve.isPrimary())curve=curve.secondary();
      var edge=CoasterPathEdge.curve(curve.bePositions.getFirst(),curve.bePositions.getSecond(),curve);edges.add(edge);
      double length=TrackGeometry.table(l,edge).length();var rail=new FoundationData.Rail(edge.from(),edge.to(),Family.POWERED_INVERTED,
          new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),PoweredInvertedYawVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
      var mode=i==1?YawState.Mode.TARGET:i==2?YawState.Mode.SHOW_STOP:YawState.Mode.PROFILE;
      double from=i==0?0:i==2?2*Math.PI:0,to=i==0?2*Math.PI:i==2?4*Math.PI:0;
      var interval=new YawSection.Interval(UUID.randomUUID(),i==2?"Show":"Section"+i,0,length,mode,0,0,0,new RotationProfile(List.of(new RotationProfile.Key(0,from),new RotationProfile.Key(length,to))),i==1?marker.id:null,i==1?markerPos:null,i==1?l.dimension().location().toString():"",YawState.Winding.NEAREST,.45,.45,i==2?"Turn and reverse":"",400);
      rail.poweredYaw.yaw.owner=owner;rail.poweredYaw.yaw.geometry=rail.geometryHash;rail.poweredYaw.yaw.intervals=List.of(interval);rail.poweredYaw.speeds=List.of(new PoweredInvertedYawSection.Speed(0,length,4));
      if(i==2)rail.poweredYaw.stops=List.of(new PoweredInvertedYawSection.Stop(interval.id(),"Turn and reverse",length*.5,400,-1));
      TrackPlacement.captureOriginalCurve(rail,edge.bezier());data.rails.add(rail);
    }
    for(var edge:edges){var reason=PoweredInvertedYawTrack.rejection(l,edge);if(reason!=null)throw new IllegalArgumentException("Powered Inverted Yaw example "+edge.from()+": "+reason);}
    var launch=edges.getFirst();BlockPos gp=null;
    for(int i=0;i<edges.size();i++){
      var edge=edges.get(i);var anchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(edge.from());
      var axis=Math.abs(tangents.get(i).z)>.5?Direction.Axis.X:Direction.Axis.Z;var direction=axis==Direction.Axis.X?Direction.EAST:Direction.SOUTH;
      anchor.commitChainLift(axis,ChainLiftEndRef.anchor(edge.to()),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),TrackGeometry.table(l,edge).length()));
      for(int n=1;n<=3;n++)l.setBlockAndUpdate(edge.from().relative(direction,n),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,axis));
      var governor=edge.from().relative(direction,4);l.setBlockAndUpdate(governor,PoweredInvertedYawContent.GOVERNOR.get().defaultBlockState().setValue(PoweredInvertedYawGovernorBlock.FACING,direction.getOpposite()));if(i==0)gp=governor;
      var motor=edge.from().relative(direction,5);l.setBlockAndUpdate(motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,direction.getOpposite()));((CreativeMotorBlockEntity)l.getBlockEntity(motor)).generatedSpeed.setValue(128);
    }
    var cars=new ArrayList<FoundationData.Vehicle>();var docks=new ArrayList<BlockPos>();
    for(int i=0;i<count;i++){
      var v=PoweredInvertedYawVehicle.create(l,FlyingExamples.sample(l,launch,8+6*i));cars.add(v);
      var pos=origin.offset(-3,-5,24+i*2);docks.add(pos);l.setBlockAndUpdate(pos,StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)l.getBlockEntity(pos)).vehicle=v.chassis;PoweredInvertedYawRuntime.rebindDock(l,v,pos);
    }
    if(count==0){var station=origin.offset(-3,-5,20);docks.add(station);l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());}
    for(int side:new int[]{-1,1})for(var p:BlockPos.betweenClosed(origin.offset(side*3,-6,4),origin.offset(side*6,-6,60)))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
    data.setDirty();FoundationSync.broadcast(l);return new Example(List.copyOf(edges),List.copyOf(cars),docks.getFirst(),(PoweredInvertedYawGovernorEntity)l.getBlockEntity(gp));
  }
}
