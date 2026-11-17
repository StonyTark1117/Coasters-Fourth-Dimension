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
public final class S2SpinningExamples {
  public record Example(List<CoasterPathEdge> edges,List<FoundationData.Vehicle> cars,
      BlockPos station,S2SpinningGovernorEntity governor){}
  private record Pending(Example example,long deadline){}
  private static final Map<ServerLevel,List<Pending>> PENDING=new WeakHashMap<>();
  private S2SpinningExamples(){}
  @SubscribeEvent public static void register(RegisterCommandsEvent event){
    var demo=Commands.literal("demo");
    for(int n:new int[]{0,1,4})demo.then(Commands.literal(n==0?"s2_spinning_track":n==1?"s2_spinning":"s2_spinning_train").executes(ctx->{
      try {var l=ctx.getSource().getLevel();var e=create(l,BlockPos.containing(ctx.getSource().getPosition()).offset(0,8,12),n);
        if(n>0)scheduleBind(l,e);
        ctx.getSource().sendSuccess(()->Component.literal("Launched Inverting Spinning loop built with kinetic launch and unpowered downhill roll. Wait for captured couplers, board, close restraints, Dispatch. Capture on return to the level dock, wait for alignment, then open restraints. Sneak-use the governor to bind a different empty train."),false);return 1;
      }catch(IllegalArgumentException ex){ctx.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}
    }));
    event.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2)).then(demo));
  }
  public static void scheduleBind(ServerLevel level,Example example){
    PENDING.computeIfAbsent(level,k->new ArrayList<>()).add(new Pending(example,level.getGameTime()+400));
  }
  @SubscribeEvent public static void tick(LevelTickEvent.Post event){
    if(!(event.getLevel() instanceof ServerLevel l))return;var pending=PENDING.get(l);if(pending==null)return;
    for(var it=pending.iterator();it.hasNext();){var p=it.next();
      if(l.getGameTime()>p.deadline){it.remove();continue;}
      if(p.example.cars.stream().allMatch(v->v.boardingReady&&Boarding.ready(l,v))){
        try {bind(l,p.example);it.remove();}
        catch(IllegalArgumentException notReady){
          // A chunk/controller or kinetic network may change while the native dock settles.
          // Keep the existing guards and bounded deadline; a setup retry must not crash ticks.
        }
      }
    }
    if(pending.isEmpty())PENDING.remove(l);
  }
  public static void bind(ServerLevel l,Example e){
    var pos=e.governor.getBlockPos();
    if(!l.hasChunkAt(pos)||!(l.getBlockEntity(pos) instanceof S2SpinningGovernorEntity governor))
      throw new IllegalArgumentException("Load the example governor before binding");
    var container=SubLevelContainer.getContainer(l);
    for(int i=0;i<e.cars.size()-1;i++){
      var next=e.cars.get(i+1).chassis;
      if(VehicleOperations.consist(l,e.cars.get(i)).stream().anyMatch(v->v.chassis.equals(next)))continue;
      if(!CoasterCartTrainLinkConstraint.linkCarts(l,
        (ServerSubLevel)container.getSubLevel(e.cars.get(i).chassis),
        (ServerSubLevel)container.getSubLevel(e.cars.get(i+1).chassis),8))
      throw new IllegalArgumentException("Wait for each empty stopped dock latch before coupling");
    }
    S2SpinningRuntime.rebindDock(l,e.cars.getFirst(),e.station);governor.bind(e.cars.getFirst());
    FoundationData.get(l).setDirty();FoundationSync.broadcast(l);
  }
  public static Example create(ServerLevel l,BlockPos origin,int count){
    if(count!=0&&count!=1&&count!=4)throw new IllegalArgumentException("Example offers track only, one or four cars");
    for(var p:BlockPos.betweenClosed(origin.offset(-8,-8,-40),origin.offset(104,6,232)))
      if(!l.getBlockState(p).isAir())throw new IllegalArgumentException("Clear Launched Inverting Spinning example area at "+p.toShortString());
    int[][] offsets={{0,0,0},{0,0,64},{0,-1,88},{0,-2,112},{0,-3,136},{0,-4,160},
      {0,-4,192},{32,-4,224},{64,-4,224},{96,-4,192},{96,-4,144},{96,-2,96},
      {96,0,48},{96,0,0},{64,0,-32},{32,0,-32}};
    var anchors=new ArrayList<BlockPos>();var tangents=new ArrayList<Vec3>();var normals=new ArrayList<Vec3>();
    for(int i=0;i<offsets.length;i++){
      var p=origin.offset(offsets[i][0],offsets[i][1],offsets[i][2]);anchors.add(p);
      l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if(i>=2&&i<=5)((CoasterAnchorpointBlockEntity)l.getBlockEntity(p)).setJointBankRadians((float)Math.toRadians(90*(i-1)));
      var tangent=i<=6?new Vec3(0,0,1):i<=8?new Vec3(1,0,0):i<=13?new Vec3(0,0,-1):new Vec3(-1,0,0);
      tangents.add(tangent);normals.add(CoasterTrackPlacement.anchorFaceNormalForAxis(l,p,tangent));
    }
    var starts=new ArrayList<Vec3>();
    for(int i=0;i<anchors.size();i++)starts.add(CoasterBezierHandleEdit.liftedWorldCenterFromCanonicalTangent(l,anchors.get(i),tangents.get(i)));
    var edges=new ArrayList<CoasterPathEdge>();var data=FoundationData.get(l);
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
      double length=TrackGeometry.table(l,edge).length();var rail=new FoundationData.Rail(edge.from(),edge.to(),Family.SPINNING,
          new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),S2SpinningVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
      if(i==0)rail.magnets=List.of(new MagneticSpan(16,56,MagneticSpan.Mode.INDUCE,1,6,.1,1));
      if(i==5||i==15)rail.magnets=List.of(new MagneticSpan(2,length-2,MagneticSpan.Mode.DAMP,1,12,.1,1));
      TrackPlacement.captureOriginalCurve(rail,edge.bezier());data.rails.add(rail);
    }
    for(var edge:edges){var reason=S2SpinningTrack.rejection(l,edge);if(reason!=null)throw new IllegalArgumentException("Launched Inverting Spinning example "+edge.from()+": "+reason);}
    var anchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(origin);var launch=edges.getFirst();
    anchor.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(launch.to()),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(launch,0,1)),TrackGeometry.table(l,launch).length()));
    for(int i=1;i<=3;i++)l.setBlockAndUpdate(origin.east(i),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
    var gp=origin.east(4);l.setBlockAndUpdate(gp,S2SpinningContent.GOVERNOR.get().defaultBlockState().setValue(S2SpinningGovernorBlock.FACING,Direction.WEST));
    var mp=origin.east(5);l.setBlockAndUpdate(mp,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
    ((CreativeMotorBlockEntity)l.getBlockEntity(mp)).generatedSpeed.setValue(192);
    // Native rolling drag cannot carry a four-car train around the whole loop.
    // Keep the downhill full roll unpowered, then use supplied native Chain to
    // return at eight blocks/s. The same ride authority gates every Chain impulse.
    for(int i=5;i<edges.size();i++){
      var edge=edges.get(i);var returnAnchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(edge.from());
      var axis=Math.abs(tangents.get(i).z)>.5?Direction.Axis.X:Direction.Axis.Z;
      var direction=axis==Direction.Axis.X?Direction.EAST:(i==7||i==8)?Direction.NORTH:Direction.SOUTH;
      returnAnchor.commitChainLift(axis,ChainLiftEndRef.anchor(edge.to()),new CoasterPathGraphSpans.SpanPath(
          List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),TrackGeometry.table(l,edge).length()));
      for(int step=1;step<=2;step++)l.setBlockAndUpdate(edge.from().relative(direction,step),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,axis));
      var motor=edge.from().relative(direction,3);l.setBlockAndUpdate(motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,direction.getOpposite()));
      ((CreativeMotorBlockEntity)l.getBlockEntity(motor)).generatedSpeed.setValue(128*returnAnchor.getChainLiftTravelSign()*(direction==Direction.NORTH?-1:1));
    }
    var cars=new ArrayList<FoundationData.Vehicle>();var docks=new ArrayList<BlockPos>();
    for(int i=0;i<count;i++){
      var v=S2SpinningVehicle.create(l,FlyingExamples.sample(l,launch,8+8*i));cars.add(v);
      var pos=origin.offset(-4,1,24+i*2);docks.add(pos);l.setBlockAndUpdate(pos,StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)l.getBlockEntity(pos)).vehicle=v.chassis;S2SpinningRuntime.rebindDock(l,v,pos);
    }
    if(count==0){var station=origin.offset(-4,1,20);docks.add(station);l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());}
    for(int side:new int[]{-1,1})for(var p:BlockPos.betweenClosed(origin.offset(side*4,0,4),origin.offset(side*7,0,60)))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
    data.setDirty();FoundationSync.broadcast(l);return new Example(List.copyOf(edges),List.copyOf(cars),docks.getFirst(),(S2SpinningGovernorEntity)l.getBlockEntity(gp));
  }
}
