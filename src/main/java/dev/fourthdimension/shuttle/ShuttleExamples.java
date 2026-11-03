package dev.fourthdimension.shuttle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.*;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
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
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class ShuttleExamples {
  private ShuttleExamples() {}

  public record Example(List<FoundationData.Vehicle> cars, ShuttleWinchEntity winch) {}

  private record Pending(Example example, long tick) {}

  private static final Map<ServerLevel, List<Pending>> PENDING = new WeakHashMap<>();

  public static void register(RegisterCommandsEvent event) {
    var demo = Commands.literal("demo");
    for (boolean tall : new boolean[] {false, true})
      demo.then(
          Commands.literal(tall ? "pax_tall" : "pax")
              .executes(
                  ctx -> {
                    try {
                      var level = ctx.getSource().getLevel();
                      var example =
                          create(
                              level,
                              BlockPos.containing(ctx.getSource().getPosition()).offset(0, 5, 65),
                              tall);
                      PENDING
                          .computeIfAbsent(level, k -> new ArrayList<>())
                          .add(new Pending(example, level.getGameTime() + 80));
                      ctx.getSource()
                          .sendSuccess(
                              () ->
                                  Component.literal(
                                      "PAX shuttle built. Allow four seconds for train assembly."
                                          + " Board sixteen seats, close restraints, then use the"
                                          + " powered winch to lift and release."),
                              false);
                      return 1;
                    } catch (IllegalArgumentException failure) {
                      ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                      return 0;
                    }
                  }));
    demo.then(
        Commands.literal("pax_manual")
            .executes(
                ctx -> {
                  try {
                    var level = ctx.getSource().getLevel();
                    create(
                        level,
                        BlockPos.containing(ctx.getSource().getPosition()).offset(0, 5, 65),
                        false,
                        true);
                    ctx.getSource()
                        .sendSuccess(
                            () ->
                                Component.literal(
                                    "Unlinked PAX shuttle built. Use Chains to couple the four"
                                        + " stationary cars in order, then sneak-use the winch"
                                        + " to bind the train. Board, close restraints and"
                                        + " dispatch with the powered winch or car editor."),
                            false);
                    return 1;
                  } catch (IllegalArgumentException failure) {
                    ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                    return 0;
                  }
                }));
    event
        .getDispatcher()
        .register(Commands.literal("fd").requires(s -> s.hasPermission(2)).then(demo));
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
        p.example.winch.fail(failure.getMessage());
      }
    }
    if (pending.isEmpty()) PENDING.remove(level);
  }

  public static void assemble(ServerLevel level, Example example) {
    var container = SubLevelContainer.getContainer(level);
    for (int i = 1; i < example.cars.size(); i++)
      if (!CoasterCartTrainLinkConstraint.linkCarts(
          level,
          (ServerSubLevel) container.getSubLevel(example.cars.get(i - 1).chassis),
          (ServerSubLevel) container.getSubLevel(example.cars.get(i).chassis),
          6))
        throw new IllegalArgumentException(
            "Native PAX coupling failed; capture and inspect every car");
    example.winch.bind(example.cars.getFirst());
  }

  public static Example create(ServerLevel level, BlockPos origin, boolean tall) {
    return create(level, origin, tall, false);
  }

  private static Example create(
      ServerLevel level, BlockPos origin, boolean tall, boolean widePlatform) {
    int height = tall ? 36 : 24, spike = tall ? 64 : 48, station = tall ? 36 : 32;
    var data = FoundationData.get(level);
    if (Math.abs((long) origin.getX()) > 940
        || Math.abs((long) origin.getZ()) > 850
        || data.vehicles.size() > 60
        || data.rails.size() > 253)
      throw new IllegalArgumentException("Shuttle example exceeds near-origin or capacity limits");
    data.checkProfileCapacity(6);
    for (var pos :
        BlockPos.betweenClosed(
            origin.offset(-8, -5, -spike - 10),
            origin.offset(8, height + 18, spike + station + 10)))
      if (!level.hasChunkAt(pos) || !level.getBlockState(pos).isAir())
        throw new IllegalArgumentException(
            "Shuttle example requires a loaded empty corridor: " + pos);
    var anchors =
        List.of(
            origin.offset(0, height, -spike),
            origin,
            origin.offset(0, 0, station),
            origin.offset(0, height + 8, station + spike));
    var tangents =
        List.of(
            new Vec3(0, -.8, 1).normalize(),
            new Vec3(0, 0, 1),
            new Vec3(0, 0, 1),
            new Vec3(0, .8, 1).normalize());
    for (var pos : anchors)
      level.setBlockAndUpdate(
          pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var rails = new ArrayList<FoundationData.Rail>();
    for (int i = 0; i < 3; i++) {
      var from = anchors.get(i);
      var to = anchors.get(i + 1);
      var curve =
          new BezierConnection(
              Couple.create(from, to),
              Couple.create(
                  CoasterAnchorpointBlockEntity.worldCenter(level, from),
                  CoasterAnchorpointBlockEntity.worldCenter(level, to)),
              Couple.create(tangents.get(i), tangents.get(i + 1).scale(-1)),
              Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)),
              true,
              false,
              CoasterTrackMaterials.COASTER);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from))
          .putAnchorPeerCurve(level, to, curve);
      var edge = CoasterPathEdge.curve(from, to, curve);
      double length = TrackGeometry.table(level, edge).length();
      var rail =
          new FoundationData.Rail(
              from,
              to,
              TrackDesign.PAX.family,
              new RotationProfile(
                  List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(length, 0))),
              UUID.randomUUID(),
              TrackDesign.PAX.variant,
              TrackGeometry.fingerprint(level, edge),
              0,
              true);
      TrackPlacement.captureOriginalCurve(rail, curve);
      data.rails.add(rail);
      rails.add(rail);
    }
    TrackGeometry.invalidate(level);
    for (var rail : rails) {
      var edge = TrackGeometry.resolve(level, rail);
      rail.profile = rail.profile.scaledTo(TrackGeometry.table(level, edge).length());
      rail.geometryHash = TrackGeometry.fingerprint(level, edge);
    }
    var edge = TrackGeometry.resolve(level, rails.get(1));
    var cars = new ArrayList<FoundationData.Vehicle>();
    for (int i = 0; i < 4; i++) {
      double t = TrackGeometry.table(level, edge).parameterAt(8 + 6 * i);
      var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
      cars.add(
          VehicleFactory.create(
              level, TrackDesign.PAX, new CoasterPathTrackFrame.GraphHit(point, edge, t)));
    }
    var pos = origin.offset(-6, 0, 8);
    level.setBlockAndUpdate(
        pos,
        ShuttleContent.WINCH
            .get()
            .defaultBlockState()
            .setValue(ShuttleWinchBlock.AXIS, Direction.Axis.X));
    level.setBlockAndUpdate(
        pos.west(),
        AllBlocks.CREATIVE_MOTOR
            .getDefaultState()
            .setValue(BlockStateProperties.FACING, Direction.EAST));
    ((CreativeMotorBlockEntity) level.getBlockEntity(pos.west())).generatedSpeed.setValue(128);
    for (int side : new int[] {-1, 1})
      for (var floor :
          BlockPos.betweenClosed(
              origin.offset(side * (widePlatform ? 4 : 6), -1, 4),
              origin.offset(side * 7, -1, 30)))
        level.setBlockAndUpdate(floor, Blocks.SMOOTH_STONE.defaultBlockState());
    data.setDirty();
    FoundationSync.broadcast(level);
    return new Example(List.copyOf(cars), (ShuttleWinchEntity) level.getBlockEntity(pos));
  }
}
