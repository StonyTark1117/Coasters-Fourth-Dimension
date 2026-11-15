package dev.fourthdimension.track;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.vehicle.VehicleFactory;
import dev.silvergold.simulatedcoasters.*;
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
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

/**
 * Reproducible original example circuits powered by native Coasters chain drives and Create motors.
 */
public final class ExampleLayouts {
  private ExampleLayouts() {}

  public static void register(RegisterCommandsEvent event) {
    var root = Commands.literal("fd").requires(s -> s.hasPermission(2));
    var demo = Commands.literal("demo");
    for (var design : TrackDesign.values()) {
      if (design == TrackDesign.PIPELINE || design == TrackDesign.S2_SPINNING || design == TrackDesign.ROLLER_BALL || design == TrackDesign.NEW_GENERATION_FLYING || design == TrackDesign.AXIS || design == TrackDesign.FLYING || design == TrackDesign.PAX || design == TrackDesign.SPINNING || design == TrackDesign.SUSPENDED || design == TrackDesign.FIXED_INVERTED) continue;
      demo.then(
          Commands.literal(
                  switch (design) {
                    case ARROW -> "arrow";
                    case LEGACY -> "legacy";
                    case FREE_SPIN -> "free_spin";
                    case REVISED -> "revised";
                    case PAX -> "pax";
                    case PIPELINE -> "pipeline";
                    case S2_SPINNING -> "s2_spinning";
                    case SPINNING -> "spinning";
                    case SUSPENDED -> "suspended";
                    case FIXED_INVERTED -> "fixed_inverted";
                    case AXIS -> "axis";
                    case FLYING -> "flying";
                    case ROLLER_BALL -> "roller_ball";
                    case NEW_GENERATION_FLYING -> "new_generation_flying";
                  })
              .executes(
                  ctx -> {
                    try {
                      create(
                          ctx.getSource().getLevel(),
                          BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12),
                          design);
                      ctx.getSource()
                          .sendSuccess(
                              () ->
                                  Component.literal(
                                      "Example circuit built. Use a rotation tool on the car to"
                                          + " close restraints and dispatch. The station can"
                                          + " capture the returning car."),
                              false);
                      return 1;
                    } catch (IllegalArgumentException failure) {
                      ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                      return 0;
                    }
                  }));
    }
    demo.then(
        Commands.literal("arrow_bank")
            .executes(
                ctx -> {
                  try {
                    createBankedArrow(
                        ctx.getSource().getLevel(),
                        BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12));
                    ctx.getSource()
                        .sendSuccess(
                            () ->
                                Component.literal(
                                    "Banked Arrow example built with native 30-degree banks. The"
                                        + " station is level; the upper banked straight and right"
                                        + " straight each have a full controlled turn. Track"
                                        + " styling is optional and selected with the rotation"
                                        + " tool."),
                            false);
                    return 1;
                  } catch (IllegalArgumentException failure) {
                    ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                    return 0;
                  }
                }));
    root.then(demo);
    event.getDispatcher().register(root);
  }

  public static FoundationData.Vehicle create(
      ServerLevel level, BlockPos origin, TrackDesign design) {
    return create(level, origin, design, false);
  }

  public static FoundationData.Vehicle createBankedArrow(ServerLevel level, BlockPos origin) {
    return create(level, origin, TrackDesign.ARROW, true);
  }

  /** A validation-only original circuit ready for client-installed optional propulsion. */
  public static void createBareArrow(ServerLevel level, BlockPos origin) {
    create(level, origin, TrackDesign.ARROW, false, false, false);
  }

  /** Validation-only original circuit with no drive or preallocated car. */
  public static void createBare(ServerLevel level, BlockPos origin, TrackDesign design) {
    create(level, origin, design, false, false, false);
  }

  /** Adds the car after a connected client has fitted the optional motor to a bare circuit. */
  public static FoundationData.Vehicle addBareArrowCar(ServerLevel level, BlockPos origin) {
    var start = origin.offset(0, 0, 8);
    var end = origin.offset(0, 0, 44);
    var stationPos = origin.offset(-6, 0, 20);
    if (!(level.getBlockEntity(stationPos) instanceof BoardingStationEntity station)
        || station.vehicle != null)
      throw new IllegalArgumentException("Find an empty bare Arrow loop station first");
    var rail =
        FoundationData.get(level).rails.stream()
            .filter(candidate -> candidate.matches(start, end))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Bare Arrow loop track is missing"));
    var edge = TrackGeometry.resolve(level, rail);
    if (edge == null) throw new IllegalArgumentException("Bare Arrow loop track is unresolved");
    double t = TrackGeometry.table(level, edge).parameterAt(12);
    var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
    var vehicle =
        VehicleFactory.create(
            level, TrackDesign.ARROW, new CoasterPathTrackFrame.GraphHit(point, edge, t));
    station.vehicle = vehicle.chassis;
    station.setChanged();
    return vehicle;
  }

  private static FoundationData.Vehicle create(
      ServerLevel level, BlockPos origin, TrackDesign design, boolean banked) {
    return create(level, origin, design, banked, true, true);
  }

  private static FoundationData.Vehicle create(
      ServerLevel level,
      BlockPos origin,
      TrackDesign design,
      boolean banked,
      boolean nativeDrives,
      boolean includeVehicle) {
    if (design == TrackDesign.PAX)
      throw new IllegalArgumentException(
          "Use /fd demo pax or pax_tall for a physical shuttle example");
    if (Math.abs((long) origin.getX()) > 950 || Math.abs((long) origin.getZ()) > 950)
      throw new IllegalArgumentException("Example circuits must stay near origin");
    for (var pos : BlockPos.betweenClosed(origin.offset(-7, -3, -8), origin.offset(39, 13, 60)))
      if (!level.getBlockState(pos).isAir())
        throw new IllegalArgumentException("Example requires an empty 47 x 17 x 69 area: " + pos);
    var data = FoundationData.get(level);
    data.checkProfileCapacity(banked ? 18 : design.family.driven() ? 17 : 16);
    if (data.rails.size() > 248 || data.vehicles.size() >= 64)
      throw new IllegalArgumentException("Example capacity exceeded");
    int[][] offsets = {{0, 8}, {0, 44}, {8, 52}, {24, 52}, {32, 44}, {32, 8}, {24, 0}, {8, 0}};
    Vec3[] tangents = {
      new Vec3(0, 0, 1),
      new Vec3(0, 0, 1),
      new Vec3(1, 0, 0),
      new Vec3(1, 0, 0),
      new Vec3(0, 0, -1),
      new Vec3(0, 0, -1),
      new Vec3(-1, 0, 0),
      new Vec3(-1, 0, 0)
    };
    var anchors = new ArrayList<BlockPos>();
    for (int index = 0; index < offsets.length; index++) {
      var offset = offsets[index];
      int height = design != TrackDesign.ARROW && (index == 2 || index == 3) ? 6 : 0;
      var pos = origin.offset(offset[0], height, offset[1]);
      anchors.add(pos);
      level.setBlockAndUpdate(
          pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if (banked && (index == 2 || index == 3))
        ((CoasterAnchorpointBlockEntity) level.getBlockEntity(pos))
            .setJointBankRadians((float) Math.toRadians(30));
    }
    // A face normal belongs to the joint, independent of which curve approaches it.
    // Resolve once before adding incident curves so bank-side selection stays consistent.
    var normals = new ArrayList<Vec3>();
    for (int i = 0; i < anchors.size(); i++)
      normals.add(
          CoasterTrackPlacement.anchorFaceNormalForAxis(level, anchors.get(i), tangents[i]));
    var centers = new ArrayList<Vec3>();
    for (int i = 0; i < anchors.size(); i++) {
      var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(anchors.get(i));
      centers.add(
          CoasterAnchorpointBlockEntity.worldCenterBase(level, anchors.get(i))
              .add(normals.get(i).scale(anchor.getLiftBlocks())));
    }
    var edges = new ArrayList<CoasterPathEdge>();
    for (int i = 0; i < 8; i++) {
      int next = (i + 1) % 8;
      var from = anchors.get(i);
      var to = anchors.get(next);
      var curve =
          new BezierConnection(
              Couple.create(from, to),
              Couple.create(centers.get(i), centers.get(next)),
              Couple.create(tangents[i], tangents[next].scale(-1)),
              Couple.create(normals.get(i), normals.get(next)),
              true,
              false,
              CoasterTrackMaterials.COASTER);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from))
          .putAnchorPeerCurve(level, to, curve);
      var edge = CoasterPathEdge.curve(from, to, curve);
      edges.add(edge);
      double length = TrackGeometry.table(level, edge).length();
      var profile =
          design.family.driven() && (i == 4 || (banked && i == 2))
              ? new RotationProfile(
                  List.of(
                      new RotationProfile.Key(0, 0),
                      new RotationProfile.Key(length / 2, 2 * Math.PI),
                      new RotationProfile.Key(length, 0)))
              : new RotationProfile(
                  List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(length, 0)));
      var rail =
          new FoundationData.Rail(
              from,
              to,
              design.family,
              profile,
              UUID.randomUUID(),
              design.variant,
              TrackGeometry.fingerprint(level, edge),
              0,
              true);
      TrackPlacement.captureOriginalCurve(rail, curve);
      data.rails.add(rail);
    }
    if (nativeDrives) {
      for (int i = 0; i < 8; i++) {
        var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(anchors.get(i));
        var edge = edges.get(i);
        var path =
            new CoasterPathGraphSpans.SpanPath(
                List.of(new CoasterPathGraphSpans.PathSegment(edge, 0, 1)),
                TrackGeometry.table(level, edge).length());
        var axis = Math.abs(tangents[i].z) > .5 ? Direction.Axis.X : Direction.Axis.Z;
        anchor.commitChainLift(axis, ChainLiftEndRef.anchor(edge.to()), path);
        // Keep the motor on the high side of the banked joint; a low-side full
        // block intersects the tilted native bearing as it approaches the anchor.
        var direction =
            axis == Direction.Axis.X
                ? Direction.EAST
                : banked && (i == 2 || i == 3) ? Direction.NORTH : Direction.SOUTH;
        var motorPos = anchors.get(i).relative(direction);
        level.setBlockAndUpdate(
            motorPos,
            AllBlocks.CREATIVE_MOTOR
                .getDefaultState()
                .setValue(BlockStateProperties.FACING, direction.getOpposite()));
        var motor = (CreativeMotorBlockEntity) level.getBlockEntity(motorPos);
        // Native speed is 16 blocks/s at 256 RPM; choose a slow 2 blocks/s demo.
        motor.generatedSpeed.setValue(
            32 * anchor.getChainLiftTravelSign() * (direction == Direction.NORTH ? -1 : 1));
      }
    }
    FoundationData.Vehicle vehicle = null;
    if (includeVehicle) {
      var edge = edges.getFirst();
      double t = TrackGeometry.table(level, edge).parameterAt(12);
      var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
      vehicle =
          VehicleFactory.create(level, design, new CoasterPathTrackFrame.GraphHit(point, edge, t));
    }
    // Finalize bindings after every native connection/handle and component orientation exists.
    TrackGeometry.invalidate(level);
    for (var rail : data.rails)
      if (anchors.contains(rail.from) && anchors.contains(rail.to)) {
        var stored = TrackGeometry.resolve(level, rail);
        rail.profile = rail.profile.scaledTo(TrackGeometry.table(level, stored).length());
        rail.geometryHash = TrackGeometry.fingerprint(level, stored);
      }
    if (design == TrackDesign.FREE_SPIN) {
      var magneticRail =
          data.rails.stream()
              .filter(r -> r.matches(anchors.get(4), anchors.get(5)))
              .findFirst()
              .orElseThrow();
      magneticRail.magnets =
          List.of(
              new dev.fourthdimension.physics.MagneticSpan(
                  3, 9, dev.fourthdimension.physics.MagneticSpan.Mode.INDUCE, 1, 4, .2, 1),
              new dev.fourthdimension.physics.MagneticSpan(
                  12, 18, dev.fourthdimension.physics.MagneticSpan.Mode.INDUCE, -1, 4, .2, 8),
              new dev.fourthdimension.physics.MagneticSpan(
                  24, 32, dev.fourthdimension.physics.MagneticSpan.Mode.DAMP, 1, 5, .2, 15));
      magneticRail.revision++;
    }
    var stationPos = origin.offset(-6, 0, 20);
    level.setBlockAndUpdate(stationPos, StationContent.STATION.get().defaultBlockState());
    var station = (BoardingStationEntity) level.getBlockEntity(stationPos);
    if (vehicle != null) station.vehicle = vehicle.chassis;
    station.setChanged();
    for (int side : new int[] {-1, 1})
      for (var pos :
          BlockPos.betweenClosed(origin.offset(side * 6, -1, 8), origin.offset(side * 7, -1, 26)))
        level.setBlockAndUpdate(pos, Blocks.SMOOTH_STONE.defaultBlockState());
    data.setDirty();
    FoundationSync.broadcast(level);
    return vehicle;
  }
}
