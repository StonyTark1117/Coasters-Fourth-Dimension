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

/** Original Axis functional loop. Example creation is disclosed setup, not player construction. */
public final class AxisExamples {
  public record Example(List<CoasterPathEdge> edges, FoundationData.Vehicle vehicle, BlockPos station) {}
  private AxisExamples() {}

  public static void register(RegisterCommandsEvent event) {
    var demo = Commands.literal("demo");
    for (boolean car : new boolean[] {true, false})
      demo.then(Commands.literal(car ? "axis" : "axis_track").executes(ctx -> {
        try {
          create(ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12), car);
          ctx.getSource().sendSuccess(() -> Component.literal(
              "Axis loop built. Board at the level station; close restraints and Dispatch. "
                  + "The banked DRIVE section makes one full turn, then DAMP releases to FREE. Use Capture at the return dock and wait for upright alignment before opening restraints."), false);
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
    for (var pos : BlockPos.betweenClosed(origin.offset(-8, -2, -8), origin.offset(104, 7, 120)))
      if (!level.getBlockState(pos).isAir())
        throw new IllegalArgumentException("Axis example needs an empty 113 x 10 x 129 area: " + pos);
    int[][] offsets = {{0,24},{0,88},{24,112},{72,112},{96,88},{96,24},{72,0},{24,0}};
    Vec3[] tangent = {new Vec3(0,0,1),new Vec3(0,0,1),new Vec3(1,0,0),new Vec3(1,0,0),
        new Vec3(0,0,-1),new Vec3(0,0,-1),new Vec3(-1,0,0),new Vec3(-1,0,0)};
    var anchors = new ArrayList<BlockPos>();
    for (int i = 0; i < offsets.length; i++) {
      var pos = origin.offset(offsets[i][0], 0, offsets[i][1]);
      anchors.add(pos);
      level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if (i == 2 || i == 3)
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
      var rail=new FoundationData.Rail(edge.from(),edge.to(),dev.fourthdimension.contract.Family.AXIS,
          new dev.fourthdimension.physics.RotationProfile(List.of(
              new dev.fourthdimension.physics.RotationProfile.Key(0,0),
              new dev.fourthdimension.physics.RotationProfile.Key(length,0))),
          UUID.randomUUID(),AxisVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
      dev.fourthdimension.track.TrackPlacement.captureOriginalCurve(rail,edge.bezier());
      rail.liftLock=i==0;
      rail.axisMode=i==2?AxisState.Mode.DRIVE:i==3?AxisState.Mode.DAMP:AxisState.Mode.FREE;
      if(i==2)rail.profile=new dev.fourthdimension.physics.RotationProfile(List.of(
          new dev.fourthdimension.physics.RotationProfile.Key(0,0),
          new dev.fourthdimension.physics.RotationProfile.Key(length,2*Math.PI)));
      data.rails.add(rail);
    }
    data.setDirty();
    for (var edge : edges) {
      var reason = AxisTrack.rejection(level, edge);
      if (reason != null) throw new IllegalArgumentException("Axis example geometry: " + reason);
    }
    for (int i = 0; i < anchors.size(); i++) {
      // Keep power hardware away from the banked bogie sweep. One continuous
      // native span crosses both banked anchors from the upright approach.
      if(i==2||i==3)continue;
      var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(anchors.get(i));
      var edge = edges.get(i);
      var spanEdges=i==1?edges.subList(1,4):List.of(edge);
      var path = new CoasterPathGraphSpans.SpanPath(
          spanEdges.stream().map(e->new CoasterPathGraphSpans.PathSegment(e,0,1)).toList(),
          spanEdges.stream().mapToDouble(e->TrackGeometry.table(level,e).length()).sum());
      var axis = Math.abs(tangent[i].z) > .5 ? Direction.Axis.X : Direction.Axis.Z;
      anchor.commitChainLift(axis, ChainLiftEndRef.anchor(spanEdges.getLast().to()), path);
      var right=CoasterPathTrackFrame.trackBasisAtHit(level,sample(level,edge,.1)).right();
      var direction = axis == Direction.Axis.X ? (right.x>0?Direction.EAST:Direction.WEST) : (right.z>0?Direction.SOUTH:Direction.NORTH);
      var pos = anchors.get(i).relative(direction);
      level.setBlockAndUpdate(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState()
          .setValue(BlockStateProperties.FACING, direction.getOpposite()));
      ((CreativeMotorBlockEntity) level.getBlockEntity(pos)).generatedSpeed.setValue(
          48 * anchor.getChainLiftTravelSign() * ((direction == Direction.NORTH || direction == Direction.WEST) ? -1 : 1));
    }
    FoundationData.Vehicle vehicle = includeVehicle ? AxisVehicle.create(level, sample(level, edges.getFirst(), 12)) : null;
    // Capture must be reachable before lift entry, including a safely stopped
    // returning carrier that has not yet settled its spin.
    var stationPos = origin.offset(-2, 1, 36);
    level.setBlockAndUpdate(stationPos, StationContent.STATION.get().defaultBlockState());
    var station = (BoardingStationEntity) level.getBlockEntity(stationPos);
    if (vehicle != null) {station.vehicle = vehicle.chassis; vehicle.dock=stationPos;var dockRail=AxisRuntime.rail(level,vehicle);vehicle.axis.dockFrom=dockRail.from;vehicle.axis.dockTo=dockRail.to;vehicle.axis.dockGeometry=dockRail.geometryHash;}
    station.setChanged();
    // Walkway stays outside the outboard full-roll reservation; it is not a fixture mount.
    for (var p : BlockPos.betweenClosed(origin.offset(8,-1,24),origin.offset(10,-1,62)))
      level.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
    FoundationSync.broadcast(level);
    return new Example(List.copyOf(edges), vehicle, stationPos);
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
