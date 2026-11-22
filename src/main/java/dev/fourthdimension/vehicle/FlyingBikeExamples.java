package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.*;
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Original two-launch Flying Bike loop. Example creation is disclosed setup, not player construction. */
public final class FlyingBikeExamples {
  public record Example(List<CoasterPathEdge> edges, FoundationData.Vehicle vehicle, BlockPos station, BlockPos firstGovernor, BlockPos secondGovernor) {}
  private FlyingBikeExamples() {}

  public static void register(RegisterCommandsEvent event) {
    if(Boolean.getBoolean("fourthdimension.validation")) event.getDispatcher().register(Commands.literal("fd").requires(c->c.hasPermission(2)).then(Commands.literal("flying_bike_inspect").executes(ctx->{
      var l=ctx.getSource().getLevel();var container=dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(l);
      for(var v:FoundationData.get(l).vehicles.values())if(FlyingBikeVehicle.is(v)){
        var b=(dev.ryanhcode.sable.sublevel.ServerSubLevel)container.getSubLevel(v.chassis);if(b==null)continue;
        var plot=b.getPlot().getCenterBlock();var center=dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(b);var g=FlyingBikeRuntime.governor(l,v);
        ctx.getSource().sendSuccess(()->Component.literal("FlyingBike id="+v.chassis+" plot="+plot+" center="+center+" velocity="+container.physicsSystem().getPipeline().getLinearVelocity(b,new org.joml.Vector3d())+" phase="+v.flyingBike.phase+" launch="+v.flyingBike.launch+" stopped="+v.stopping+" restraints="+v.restraints+" ready="+Boarding.ready(l,v)+" resumeRequired="+v.flyingBike.resumeRequired+" fault="+v.fault+" occupants="+v.carriers.getFirst().occupants+" governor="+(g==null?"none":g.status())),false);
        for(var seat:FlyingBikeVehicle.SEATS){var p=plot.offset(seat);var world=b.logicalPose().transformPosition(new org.joml.Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5));ctx.getSource().sendSuccess(()->Component.literal("seat local="+p+" cushionWorld="+world),false);}
      }return 1;
    })));

    var demo = Commands.literal("demo");
    for (boolean car : new boolean[] {true, false})
      demo.then(Commands.literal(car ? "flying_bike" : "flying_bike_track").executes(ctx -> {
        try {
          create(ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12), car);
          ctx.getSource().sendSuccess(() -> Component.literal(
              "Flying Bike loop built. Board at the level station; close restraints and Dispatch. "
                  + "Bind the first governor normally, then sneak-use first and second to link. The returning car captures automatically. Open only at the settled dock."), false);
          return 1;
        } catch (IllegalArgumentException failure) {
          ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
          return 0;
        }
      }));
    event.getDispatcher().register(Commands.literal("fd").requires(s -> s.hasPermission(2)).then(demo));
  }

  public static Example create(ServerLevel level, BlockPos origin, boolean includeVehicle) {
    if (Math.abs((long) origin.getX()) > 850 || Math.abs((long) origin.getZ()) > 850)
      throw new IllegalArgumentException("Choose an example area near world origin");
    for (var pos : BlockPos.betweenClosed(origin.offset(-8, -6, -8), origin.offset(104, 7, 120)))
      if (!level.getBlockState(pos).isAir())
        throw new IllegalArgumentException("Flying Bike example needs an empty 113 x 14 x 129 area: " + pos);
    int[][] offsets = {{0,24},{0,32},{0,88},{24,112},{72,112},{96,88},{96,80},{96,24},{72,0},{24,0}};
    Vec3[] tangent = {new Vec3(0,0,1),new Vec3(0,0,1),new Vec3(0,0,1),new Vec3(1,0,0),new Vec3(1,0,0),
        new Vec3(0,0,-1),new Vec3(0,0,-1),new Vec3(0,0,-1),new Vec3(-1,0,0),new Vec3(-1,0,0)};
    var anchors = new ArrayList<BlockPos>();
    for (int i = 0; i < offsets.length; i++) {
      var pos = origin.offset(offsets[i][0], 0, offsets[i][1]);
      anchors.add(pos);
      level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if (i == 3 || i == 4)
        ((CoasterAnchorpointBlockEntity) level.getBlockEntity(pos)).setJointBankRadians((float) Math.toRadians(30));
    }
    var normals = new ArrayList<Vec3>();
    for (int i = 0; i < anchors.size(); i++)
      normals.add(CoasterTrackPlacement.anchorFaceNormalForAxis(level, anchors.get(i), tangent[i]));
    var edges = new ArrayList<CoasterPathEdge>();
    for (int i = 0; i < anchors.size(); i++) {
      int next = (i + 1) % anchors.size();
      edges.add(connect(level, anchors.get(i), anchors.get(next), tangent[i], tangent[next].scale(-1),
          normals.get(i), normals.get(next)));
    }
    var data = FoundationData.get(level);
    for (int i=0;i<edges.size();i++) {
      var edge=edges.get(i);
      var length=TrackGeometry.table(level,edge).length();
      var rail=new FoundationData.Rail(edge.from(),edge.to(),dev.fourthdimension.contract.Family.FLYING_BIKE,
          new dev.fourthdimension.physics.RotationProfile(List.of(
              new dev.fourthdimension.physics.RotationProfile.Key(0,0),
              new dev.fourthdimension.physics.RotationProfile.Key(length,0))),
          UUID.randomUUID(),FlyingBikeVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
      dev.fourthdimension.track.TrackPlacement.captureOriginalCurve(rail,edge.bezier());
      rail.liftLock=i==1||i==6;
      data.rails.add(rail);
    }
    data.setDirty();
    for (var edge : edges) {
      var reason = FlyingBikeTrack.rejection(level, edge);
      if (reason != null) throw new IllegalArgumentException("Flying Bike example geometry: " + reason);
    }
    var governors=new ArrayList<BlockPos>();
    for(int i:new int[]{1,6}){
      var anchor=(CoasterAnchorpointBlockEntity)level.getBlockEntity(anchors.get(i));var edge=edges.get(i);
      var path=new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),TrackGeometry.table(level,edge).length());
      anchor.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(edge.to()),path);
      var outward=i==1?Direction.EAST:Direction.WEST;
      for(int step=1;step<=3;step++)level.setBlockAndUpdate(anchors.get(i).relative(outward,step),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
      var gp=anchors.get(i).relative(outward,4);governors.add(gp);
      level.setBlockAndUpdate(gp,FlyingBikeContent.GOVERNOR.get().defaultBlockState().setValue(FlyingBikeGovernorBlock.FACING,outward.getOpposite()));
      var motor=anchors.get(i).relative(outward,5);
      level.setBlockAndUpdate(motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,outward.getOpposite()));
      ((CreativeMotorBlockEntity)level.getBlockEntity(motor)).generatedSpeed.setValue(192);
    }
    FoundationData.Vehicle vehicle = includeVehicle ? FlyingBikeVehicle.create(level, sample(level, edges.get(1), 6)) : null;
    // Capture must be reachable before lift entry, including a safely stopped
    // returning carrier that has not yet settled its spin.
    var stationPos = origin.offset(-3, -2, 38);
    level.setBlockAndUpdate(stationPos, StationContent.STATION.get().defaultBlockState());
    var station = (BoardingStationEntity) level.getBlockEntity(stationPos);
    if (vehicle != null) {station.vehicle = vehicle.chassis; vehicle.dock=stationPos;var r=FlyingBikeRuntime.rail(level,vehicle);vehicle.flyingBike.dockFrom=r.from;vehicle.flyingBike.dockTo=r.to;vehicle.flyingBike.dockGeometry=r.geometryHash;}
    station.setChanged();
    for(int side:new int[]{-1,1})for(var p:BlockPos.betweenClosed(origin.offset(side*3,-3,34),origin.offset(side*5,-3,90)))
      level.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
    FoundationSync.broadcast(level);
    return new Example(List.copyOf(edges), vehicle, stationPos,governors.get(0),governors.get(1));
  }

  public static CoasterPathEdge connect(ServerLevel level, BlockPos from, BlockPos to,
      Vec3 firstTangent, Vec3 secondTangent, Vec3 firstNormal, Vec3 secondNormal) {
    var curve = new BezierConnection(Couple.create(from,to), Couple.create(
        CoasterAnchorpointBlockEntity.worldCenterBase(level,from),
        CoasterAnchorpointBlockEntity.worldCenterBase(level,to)),
        Couple.create(firstTangent,secondTangent), Couple.create(firstNormal,secondNormal),
        true,false,CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level,to,curve);
    return CoasterPathEdge.curve(from,to,curve);
  }

  public static CoasterPathTrackFrame.GraphHit sample(ServerLevel level, CoasterPathEdge edge, double distance) {
    double t = TrackGeometry.table(level,edge).parameterAt(distance);
    return new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,edge,t),edge,t);
  }
}
