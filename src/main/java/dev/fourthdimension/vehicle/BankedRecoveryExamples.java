package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/** Native quarter-bend, sustained bank and level runout for occupied recovery acceptance. */
public final class BankedRecoveryExamples {
  private BankedRecoveryExamples() {}

  public record Example(
      RecoveryExamples.Example recovery, List<FoundationData.Rail> rails, List<BlockPos> anchors) {}

  public static Example create(ServerLevel level, BlockPos origin, TrackDesign design) {
    var minimum = origin.offset(-8, -1, -4);
    var maximum = origin.offset(120, 12, 72);
    if (Math.abs((long) minimum.getX()) > 1024
        || Math.abs((long) maximum.getX()) > 1024
        || Math.abs((long) minimum.getZ()) > 1024
        || Math.abs((long) maximum.getZ()) > 1024
        || minimum.getY() < level.getMinBuildHeight()
        || maximum.getY() >= level.getMaxBuildHeight())
      throw new IllegalArgumentException(
          "Choose a loaded recovery corridor inside the supported bounds");
    var data = FoundationData.get(level);
    if (data.vehicles.size() >= 64 || data.rails.size() > 251)
      throw new IllegalArgumentException("Example capacity reached");
    data.checkProfileCapacity(design.family.driven() ? 11 : 10);
    for (var p : BlockPos.betweenClosed(minimum, maximum))
      if (!level.hasChunkAt(p) || !level.getBlockState(p).isAir())
        throw new IllegalArgumentException("Load and clear a 129 by 14 by 77 recovery corridor");
    int[][] offsets = {{0, 0}, {0, 32}, {32, 64}, {64, 64}, {88, 64}, {112, 64}};
    var anchors = new ArrayList<BlockPos>();
    var tangents = new ArrayList<Vec3>();
    for (int i = 0; i < offsets.length; i++) {
      var p = origin.offset(offsets[i][0], 0, offsets[i][1]);
      anchors.add(p);
      tangents.add(i < 2 ? new Vec3(0, 0, 1) : new Vec3(1, 0, 0));
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if (i == 2 || i == 3)
        ((CoasterAnchorpointBlockEntity) level.getBlockEntity(p))
            .setJointBankRadians((float) Math.toRadians(30));
    }
    // Resolve each joint's bank once, before adding its incident curves.
    var normals = new ArrayList<Vec3>();
    var centers = new ArrayList<Vec3>();
    for (int i = 0; i < anchors.size(); i++) {
      var normal =
          CoasterTrackPlacement.anchorFaceNormalForAxis(level, anchors.get(i), tangents.get(i));
      normals.add(normal);
      var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(anchors.get(i));
      centers.add(
          CoasterAnchorpointBlockEntity.worldCenterBase(level, anchors.get(i))
              .add(normal.scale(anchor.getLiftBlocks())));
    }
    var rails = new ArrayList<FoundationData.Rail>();
    for (int i = 0; i < anchors.size() - 1; i++) {
      var from = anchors.get(i);
      var to = anchors.get(i + 1);
      var curve =
          new BezierConnection(
              Couple.create(from, to),
              Couple.create(centers.get(i), centers.get(i + 1)),
              Couple.create(tangents.get(i), tangents.get(i + 1).scale(-1)),
              Couple.create(normals.get(i), normals.get(i + 1)),
              true,
              false,
              CoasterTrackMaterials.COASTER);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from))
          .putAnchorPeerCurve(level, to, curve);
      var edge = CoasterPathEdge.curve(from, to, curve);
      double length = TrackGeometry.table(level, edge).length();
      var keys =
          i == 2 && design.family.driven()
              ? List.of(
                  new RotationProfile.Key(0, 0),
                  new RotationProfile.Key(length / 2, 2 * Math.PI),
                  new RotationProfile.Key(length, 0))
              : List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(length, 0));
      var rail =
          new FoundationData.Rail(
              from,
              to,
              design.family,
              new RotationProfile(keys),
              UUID.randomUUID(),
              design.variant,
              TrackGeometry.fingerprint(level, edge),
              0,
              true);
      rails.add(rail);
      data.rails.add(rail);
    }
    TrackGeometry.invalidate(level);
    var edges = new ArrayList<CoasterPathEdge>();
    for (var rail : rails) {
      var edge = TrackGeometry.resolve(level, rail);
      edges.add(edge);
      rail.profile = rail.profile.scaledTo(TrackGeometry.table(level, edge).length());
      rail.geometryHash = TrackGeometry.fingerprint(level, edge);
    }
    var edge = edges.getFirst();
    double t = TrackGeometry.table(level, edge).parameterAt(12);
    var car =
        VehicleFactory.create(
            level,
            design,
            new CoasterPathTrackFrame.GraphHit(
                CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t));
    t = TrackGeometry.table(level, edge).parameterAt(12 + design.axleSpacing());
    var hit =
        new CoasterPathTrackFrame.GraphHit(
            CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
    var q =
        CoasterPathTrackFrame.quaternionFromTrackBasis(
            CoasterPathTrackFrame.trackBasisAtHit(level, hit));
    var ordinary =
        CoasterCartSpawner.spawnMinimalContraption(
            level,
            CoasterCartLinkPlacement.plotOriginForBearingCenter(
                CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), q),
            q,
            hit,
            true);
    if (ordinary == null)
      throw new IllegalStateException("Native recovery bearing allocation failed");
    var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(origin);
    anchor.commitChainLift(
        Direction.Axis.X,
        ChainLiftEndRef.anchor(anchors.getLast()),
        new CoasterPathGraphSpans.SpanPath(
            edges.stream().map(e -> new CoasterPathGraphSpans.PathSegment(e, 0, 1)).toList(),
            edges.stream().mapToDouble(e -> TrackGeometry.table(level, e).length()).sum()));
    var clutch = origin.east();
    level.setBlockAndUpdate(
        clutch,
        AllBlocks.CLUTCH
            .getDefaultState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.X)
            .setValue(BlockStateProperties.POWERED, true));
    var lever = clutch.above();
    level.setBlockAndUpdate(
        lever,
        Blocks.LEVER
            .defaultBlockState()
            .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.POWERED, true));
    var motorPos = origin.east(2);
    level.setBlockAndUpdate(
        motorPos,
        AllBlocks.CREATIVE_MOTOR
            .getDefaultState()
            .setValue(BlockStateProperties.FACING, Direction.WEST));
    var motor = (CreativeMotorBlockEntity) level.getBlockEntity(motorPos);
    motor.generatedSpeed.setValue(0);
    // Boarding and unloading platforms are outside the swept corridor and on level track.
    for (int side : new int[] {-6, 6}) {
      for (var p :
          BlockPos.betweenClosed(
              origin.offset(side, -1, 8), origin.offset(side + (side < 0 ? -1 : 1), -1, 28)))
        level.setBlockAndUpdate(p, Blocks.SMOOTH_STONE.defaultBlockState());
      for (var p :
          BlockPos.betweenClosed(
              origin.offset(92, -1, 64 + side),
              origin.offset(108, -1, 64 + side + (side < 0 ? -1 : 1))))
        level.setBlockAndUpdate(p, Blocks.SMOOTH_STONE.defaultBlockState());
    }
    data.setDirty();
    FoundationSync.broadcast(level);
    return new Example(
        new RecoveryExamples.Example(car, ordinary, motor, lever),
        List.copyOf(rails),
        List.copyOf(anchors));
  }
}
