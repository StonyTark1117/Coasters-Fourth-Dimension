package dev.fourthdimension.runtime;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * Operator-only, repeatable foundation fixture; not a content item or arbitrary track conversion.
 */
public final class PrototypeFixture {
  private PrototypeFixture() {}

  public static void register(RegisterCommandsEvent event) {
    var root = Commands.literal("fd").requires(s -> s.hasPermission(2));
    root.then(
        Commands.literal("clear")
            .executes(
                ctx -> {
                  var level = ctx.getSource().getLevel();
                  var data = FoundationData.get(level);
                  var container = SubLevelContainer.getContainer(level);
                  data.vehicles.values().forEach(v -> v.restraints = false);
                  var carrierIds =
                      data.vehicles.values().stream()
                          .flatMap(v -> v.carriers.stream())
                          .map(c -> c.body)
                          .collect(java.util.stream.Collectors.toSet());
                  for (var player : level.players())
                    if (player.getVehicle() != null) {
                      var ridden =
                          dev.ryanhcode.sable.Sable.HELPER.getContaining(player.getVehicle());
                      if (ridden != null && carrierIds.contains(ridden.getUniqueId()))
                        player.stopRiding();
                    }
                  for (var v : data.vehicles.values()) {
                    for (var c : v.carriers) {
                      var body = container.getSubLevel(c.body);
                      if (body != null)
                        container.removeSubLevel(
                            body,
                            dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
                    }
                    var body = container.getSubLevel(v.chassis);
                    if (body != null)
                      container.removeSubLevel(
                          body, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
                  }
                  for (var rail : data.rails) {
                    if (level.getBlockEntity(rail.from())
                        instanceof CoasterAnchorpointBlockEntity anchor)
                      anchor.removeAnchorPeerCurve(level, rail.to());
                    for (var pos : List.of(rail.from(), rail.to()))
                      if (level.getBlockEntity(pos) instanceof CoasterAnchorpointBlockEntity anchor
                          && anchor.legCount() == 0) level.removeBlock(pos, false);
                  }
                  data.vehicles.clear();
                  data.rails.clear();
                  data.setDirty();
                  return 1;
                }));
    for (Family family : Family.values())
      root.then(
          Commands.literal(family.id())
              .executes(
                  ctx -> {
                    var source = ctx.getSource();
                    var pos = BlockPos.containing(source.getPosition()).offset(0, 5, 8);
                    create(source.getLevel(), pos, family);
                    source.sendSuccess(
                        () ->
                            Component.literal(
                                "Created " + family.id() + " foundation fixture at " + pos),
                        true);
                    return 1;
                  }));
    root.then(
        Commands.literal("status")
            .executes(
                ctx -> {
                  var data = FoundationData.get(ctx.getSource().getLevel());
                  for (var v : data.vehicles.values())
                    for (var c : v.carriers)
                      ctx.getSource()
                          .sendSuccess(
                              () ->
                                  Component.literal(
                                      v.family.id()
                                          + " "
                                          + c.body
                                          + " angle="
                                          + c.angle
                                          + " omega="
                                          + c.omega
                                          + " capture="
                                          + c.capture
                                          + " occupants="
                                          + c.occupants.size()),
                              false);
                  ctx.getSource()
                      .sendSuccess(
                          () ->
                              Component.literal(
                                  "Carrier update average: "
                                      + (CarrierRuntime.steps == 0
                                          ? 0
                                          : CarrierRuntime.nanos / CarrierRuntime.steps)
                                      + " ns"),
                          false);
                  return data.vehicles.size();
                }));
    root.then(
        Commands.literal("capture")
            .executes(
                ctx -> {
                  var data = FoundationData.get(ctx.getSource().getLevel());
                  data.vehicles.values().forEach(v -> v.carriers.forEach(c -> c.capture = true));
                  data.setDirty();
                  return 1;
                }));
    root.then(
        Commands.literal("release")
            .executes(
                ctx -> {
                  var data = FoundationData.get(ctx.getSource().getLevel());
                  data.vehicles.values().forEach(v -> v.carriers.forEach(c -> c.capture = false));
                  data.setDirty();
                  return 1;
                }));
    root.then(
        Commands.literal("travel")
            .then(
                Commands.argument("speed", DoubleArgumentType.doubleArg(-5, 5))
                    .executes(
                        ctx -> {
                          var level = ctx.getSource().getLevel();
                          var container = SubLevelContainer.getContainer(level);
                          var pipeline = container.physicsSystem().getPipeline();
                          double speed = DoubleArgumentType.getDouble(ctx, "speed");
                          for (var v : FoundationData.get(level).vehicles.values()) {
                            var body = container.getSubLevel(v.chassis);
                            if (!(body instanceof ServerSubLevel chassis)) continue;
                            pipeline.addLinearAndAngularVelocity(
                                chassis,
                                new Vector3d(0, 0, speed)
                                    .sub(pipeline.getLinearVelocity(chassis, new Vector3d())),
                                new Vector3d());
                            for (var c : v.carriers) {
                              var child = container.getSubLevel(c.body);
                              if (child instanceof ServerSubLevel carrier)
                                pipeline.addLinearAndAngularVelocity(
                                    carrier,
                                    new Vector3d(0, 0, speed)
                                        .sub(pipeline.getLinearVelocity(carrier, new Vector3d())),
                                    new Vector3d());
                            }
                          }
                          return 1;
                        })));
    root.then(
        Commands.literal("kick")
            .executes(
                ctx -> {
                  var level = ctx.getSource().getLevel();
                  var container = SubLevelContainer.getContainer(level);
                  var pipeline = container.physicsSystem().getPipeline();
                  for (var v : FoundationData.get(level).vehicles.values())
                    if (!v.family.driven())
                      for (var c : v.carriers) {
                        var body = container.getSubLevel(c.body);
                        if (body instanceof ServerSubLevel carrier)
                          pipeline.addLinearAndAngularVelocity(
                              carrier,
                              new Vector3d(),
                              new Vector3d(Math.copySign(3, c.side), 0, 0));
                      }
                  return 1;
                }));
    root.then(
        Commands.literal("ride")
            .executes(
                ctx -> {
                  var player = ctx.getSource().getPlayerOrException();
                  var level = player.serverLevel();
                  var container = SubLevelContainer.getContainer(level);
                  for (var v : FoundationData.get(level).vehicles.values())
                    for (var c : v.carriers) {
                      var body = container.getSubLevel(c.body);
                      if (body == null) continue;
                      var seat = body.getPlot().getCenterBlock().below();
                      if (!SeatBlock.isSeatOccupied(level, seat)) {
                        SeatBlock.sitDown(level, seat, player);
                        return 1;
                      }
                    }
                  return 0;
                }));
    event.getDispatcher().register(root);
  }

  public static FoundationData.Vehicle create(ServerLevel level, BlockPos from, Family family) {
    var data = FoundationData.get(level);
    data.checkProfileCapacity(2);
    if (data.vehicles.size() >= 64 || data.rails.size() >= 256)
      throw new IllegalStateException("Fixture capacity reached");
    BlockPos to = from.offset(0, 0, 40);
    if (Math.abs((long) from.getX()) > 1024
        || Math.abs((long) from.getZ()) > 1024
        || Math.abs((long) to.getZ()) > 1024)
      throw new IllegalArgumentException(
          "Foundation physics fixtures must be within 1024 blocks of origin");
    // No overwriting existing builds. Reserve the entire swept prototype corridor.
    for (BlockPos p : BlockPos.betweenClosed(from.offset(-5, -4, -1), to.offset(5, 5, 1)))
      if (!level.getBlockState(p).isAir())
        throw new IllegalStateException("Fixture corridor must be empty: " + p);
    level.setBlockAndUpdate(
        from, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    level.setBlockAndUpdate(
        to, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    Vec3 a = CoasterAnchorpointBlockEntity.worldCenter(level, from),
        b = CoasterAnchorpointBlockEntity.worldCenter(level, to);
    var curve =
        new BezierConnection(
            Couple.create(from, to),
            Couple.create(a, b),
            Couple.create(new Vec3(0, 0, 1), new Vec3(0, 0, -1)),
            Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)),
            true,
            false,
            CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from))
        .putAnchorPeerCurve(level, to, curve);
    data.rails.add(
        new FoundationData.Rail(
            from,
            to,
            family,
            new RotationProfile(
                List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(40, 4 * Math.PI)))));
    var chassis =
        CoasterCartSpawner.spawnMinimalContraption(
            level, a.add(0, .3125, 5), new Quaterniond(), null, false);
    if (chassis == null) throw new IllegalStateException("Cannot spawn chassis");
    chassis
        .getPlot()
        .getEmbeddedLevelAccessor()
        .setBlock(
            new BlockPos(0, 1, 0),
            FamilyControllers.CONTROLLERS.get(family).get().defaultBlockState(),
            3);
    var vehicle = new FoundationData.Vehicle(chassis.getUniqueId(), family);
    vehicle.boundFrom = from;
    vehicle.boundTo = to;
    data.vehicles.put(vehicle.chassis, vehicle);
    var container = SubLevelContainer.getContainer(level);
    var pipeline = container.physicsSystem().getPipeline();
    for (double side : new double[] {-3, 3}) {
      Pose3d pose = new Pose3d();
      var world =
          chassis
              .logicalPose()
              .transformPosition(
                  new Vector3d(
                      chassis.getPlot().getCenterBlock().getX() + .5 + side,
                      chassis.getPlot().getCenterBlock().getY() + 2.5,
                      chassis.getPlot().getCenterBlock().getZ() + .5));
      pose.position().set(world);
      var carrier = (ServerSubLevel) container.allocateNewSubLevel(pose);
      var plot = carrier.getPlot();
      plot.newEmptyChunk(plot.getCenterChunk());
      var embedded = plot.getEmbeddedLevelAccessor();
      // Ballast behind the pivot leaves the rider's head and camera unobstructed.
      embedded.setBlock(new BlockPos(0, 0, -1), Blocks.IRON_BLOCK.defaultBlockState(), 3);
      embedded.setBlock(
          new BlockPos(0, -1, 0), AllBlocks.SEATS.get(DyeColor.CYAN).getDefaultState(), 3);
      var anchor = plot.getCenterBlock();
      carrier
          .logicalPose()
          .rotationPoint()
          .set(anchor.getX() + .5, anchor.getY() + .5, anchor.getZ() + .5);
      carrier.logicalPose().position().set(world);
      carrier.updateLastPose();
      pipeline.onStatsChanged(carrier);
      pipeline.teleport(
          carrier, carrier.logicalPose().position(), carrier.logicalPose().orientation());
      vehicle.carriers.add(new FoundationData.Carrier(carrier.getUniqueId(), side));
    }
    data.setDirty();
    return vehicle;
  }
}
