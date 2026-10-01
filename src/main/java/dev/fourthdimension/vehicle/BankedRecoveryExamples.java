package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
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
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Native quarter-bend, sustained bank and level runout for occupied recovery acceptance. */
public final class BankedRecoveryExamples {
  private BankedRecoveryExamples() {}

  public record Example(
      RecoveryExamples.Example recovery,
      List<FoundationData.Rail> rails,
      List<BlockPos> anchors,
      List<FoundationData.Vehicle> cars) {}

  private record Pending(Example example, long tick) {}

  private static final Map<ServerLevel, List<Pending>> PENDING = new WeakHashMap<>();

  public static void queueAssembly(ServerLevel level, Example example) {
    PENDING
        .computeIfAbsent(level, k -> new ArrayList<>())
        .add(new Pending(example, level.getGameTime() + 80));
  }

  public static void tick(LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    var pending = PENDING.get(level);
    if (pending == null) return;
    for (var iterator = pending.iterator(); iterator.hasNext(); ) {
      var p = iterator.next();
      if (level.getGameTime() < p.tick) continue;
      iterator.remove();
      try {
        assemble(level, p.example);
      } catch (IllegalArgumentException failure) {
        for (var car : p.example.cars()) {
          car.stopping = true;
          car.fault = failure.getMessage();
        }
        FoundationData.get(level).setDirty();
        FoundationSync.broadcast(level);
      }
    }
    if (pending.isEmpty()) PENDING.remove(level);
  }

  public static void assemble(ServerLevel level, Example example) {
    var container = SubLevelContainer.getContainer(level);
    var expected =
        example.cars().stream().map(c -> c.chassis).collect(java.util.stream.Collectors.toSet());
    for (var car : example.cars()) {
      var body = container.getSubLevel(car.chassis);
      if (body == null
          || !Boarding.ready(level, car)
          || CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body).stream()
              .anyMatch(id -> !expected.contains(id)))
        throw new IllegalArgumentException(
            "Recovery example assembly failed: capture and inspect every original car");
    }
    for (int i = 1; i < example.cars().size(); i++)
      if (!CoasterCartTrainLinkConstraint.areLinkedCarts(
              container.getSubLevel(example.cars().get(i - 1).chassis),
              container.getSubLevel(example.cars().get(i).chassis))
          && !CoasterCartTrainLinkConstraint.linkCarts(
              level,
              (ServerSubLevel) container.getSubLevel(example.cars().get(i - 1).chassis),
              (ServerSubLevel) container.getSubLevel(example.cars().get(i).chassis),
              6))
        throw new IllegalArgumentException(
            "Native recovery train coupling failed: capture and inspect every original car");
  }

  public static Example create(ServerLevel level, BlockPos origin, TrackDesign design) {
    return create(level, origin, design, 1);
  }

  public static Example createTrain(ServerLevel level, BlockPos origin, TrackDesign design) {
    if (design != TrackDesign.ARROW && design != TrackDesign.PAX)
      throw new IllegalArgumentException("Only Arrow and PAX support these linked train examples");
    return create(level, origin, design, design == TrackDesign.ARROW ? 2 : 4);
  }

  private static Example create(
      ServerLevel level, BlockPos origin, TrackDesign design, int carCount) {
    int approach = carCount > 1 ? 48 : 32;
    int runout = carCount > 1 ? 48 : 24;
    int endZ = approach + 32;
    var minimum = origin.offset(-8, -1, -4);
    var maximum = origin.offset(88 + runout + 8, 12, endZ + 8);
    if (Math.abs((long) minimum.getX()) > 1024
        || Math.abs((long) maximum.getX()) > 1024
        || Math.abs((long) minimum.getZ()) > 1024
        || Math.abs((long) maximum.getZ()) > 1024
        || minimum.getY() < level.getMinBuildHeight()
        || maximum.getY() >= level.getMaxBuildHeight())
      throw new IllegalArgumentException(
          "Choose a loaded recovery corridor inside the supported bounds");
    var data = FoundationData.get(level);
    if (data.vehicles.size() + carCount > 64 || data.rails.size() > 251)
      throw new IllegalArgumentException("Example capacity reached");
    data.checkProfileCapacity(design.family.driven() ? 11 : 10);
    for (var p : BlockPos.betweenClosed(minimum, maximum))
      if (!level.hasChunkAt(p) || !level.getBlockState(p).isAir())
        throw new IllegalArgumentException(
            "Load and clear a "
                + (maximum.getX() - minimum.getX() + 1)
                + " by 14 by "
                + (maximum.getZ() - minimum.getZ() + 1)
                + " recovery corridor");
    int[][] offsets = {
      {0, 0}, {0, approach}, {32, endZ}, {64, endZ}, {88, endZ}, {88 + runout, endZ}
    };
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
    var cars = new ArrayList<FoundationData.Vehicle>();
    for (int i = 0; i < carCount; i++) {
      double t = TrackGeometry.table(level, edge).parameterAt(12 + i * design.axleSpacing());
      cars.add(
          VehicleFactory.create(
              level,
              design,
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
    }
    var car = cars.getLast();
    double t = TrackGeometry.table(level, edge).parameterAt(12 + carCount * design.axleSpacing());
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
              origin.offset(side, -1, 8),
              origin.offset(side + (side < 0 ? -1 : 1), -1, approach - 4)))
        level.setBlockAndUpdate(p, Blocks.SMOOTH_STONE.defaultBlockState());
      for (var p :
          BlockPos.betweenClosed(
              origin.offset(92, -1, endZ + side),
              origin.offset(84 + runout, -1, endZ + side + (side < 0 ? -1 : 1))))
        level.setBlockAndUpdate(p, Blocks.SMOOTH_STONE.defaultBlockState());
    }
    data.setDirty();
    FoundationSync.broadcast(level);
    return new Example(
        new RecoveryExamples.Example(car, ordinary, motor, lever),
        List.copyOf(rails),
        List.copyOf(anchors),
        List.copyOf(cars));
  }
}
