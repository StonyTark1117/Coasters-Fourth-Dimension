package dev.fourthdimension.integration.additions;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.integration.create.RideInformation;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.plushivr.coasterfins.block.StationBlockEntity;
import dev.plushivr.coasterfins.logic.FinSpanSegment;
import dev.plushivr.coasterfins.registry.CFBlocks;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Opt-in occupied station restart fixture. Uses native stations, seats and validated operations.
 */
public final class AdditionsLifecycleFixture {
  private AdditionsLifecycleFixture() {}

  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var root = Commands.literal("additions_restart");
    for (String action : List.of("create", "cars", "pigs", "close", "unload", "release", "capture"))
      root.then(
          Commands.literal(action)
              .executes(
                  ctx -> {
                    try {
                      act(ctx.getSource().getLevel(), action);
                      ctx.getSource()
                          .sendSuccess(
                              () -> Component.literal("Additions lifecycle fixture: " + action),
                              false);
                      return 1;
                    } catch (IllegalArgumentException failure) {
                      ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                      return 0;
                    }
                  }));
    root.then(
        Commands.literal("board_player")
            .then(
                Commands.argument(
                        "lane", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 4))
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                ctx -> {
                                  var player = EntityArgument.getPlayer(ctx, "player");
                                  int lane =
                                      com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(
                                          ctx, "lane");
                                  var car = cars(ctx.getSource().getLevel()).get(lane);
                                  if (player.isPassenger())
                                    throw new IllegalArgumentException("Player is already mounted");
                                  var carrier = car.carriers.getFirst();
                                  var body =
                                      SubLevelContainer.getContainer(player.serverLevel())
                                          .getSubLevel(carrier.body);
                                  var seat =
                                      body.getPlot()
                                          .getCenterBlock()
                                          .offset(carrier.seats.getLast());
                                  if (!Boarding.canBoard(player.serverLevel(), seat)
                                      || SeatBlock.isSeatOccupied(player.serverLevel(), seat))
                                    throw new IllegalArgumentException(
                                        "Seat is not ready for boarding");
                                  player.teleportTo(
                                      player.serverLevel(),
                                      car.boundFrom.getX() + 3.5,
                                      123,
                                      18.5,
                                      0,
                                      25);
                                  SeatBlock.sitDown(player.serverLevel(), seat, player);
                                  if (!player.isPassenger())
                                    throw new IllegalArgumentException(
                                        "Normal boarding was denied");
                                  ctx.getSource()
                                      .sendSuccess(
                                          () ->
                                              Component.literal(
                                                  "Additions lifecycle player boarded"),
                                          false);
                                  return 1;
                                }))));
    root.then(
        Commands.literal("report")
            .then(
                Commands.argument("label", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .executes(
                        ctx -> {
                          report(
                              ctx.getSource().getLevel(),
                              com.mojang.brigadier.arguments.StringArgumentType.getString(
                                  ctx, "label"));
                          ctx.getSource()
                              .sendSuccess(
                                  () -> Component.literal("Additions lifecycle snapshot recorded"),
                                  false);
                          return 1;
                        })));
    event
        .getDispatcher()
        .register(
            Commands.literal("fd")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("validation").then(root)));
  }

  private static List<FoundationData.Vehicle> cars(ServerLevel level) {
    var result =
        FoundationData.get(level).vehicles.values().stream()
            .sorted(Comparator.comparingInt(v -> v.boundFrom.getX()))
            .toList();
    if (result.size() != 5) throw new IllegalArgumentException("Create five fixture cars first");
    return result;
  }

  private static StationBlockEntity station(ServerLevel level, FoundationData.Vehicle car) {
    return (StationBlockEntity) level.getBlockEntity(car.boundFrom.offset(-8, 0, 18));
  }

  private static void act(ServerLevel level, String action) {
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    if (action.equals("create")) {
      if (!OptionalIntegrations.supported("coasterfins", Set.of("0.2.0"))
          || !data.rails.isEmpty()
          || !data.vehicles.isEmpty())
        throw new IllegalArgumentException("Use a fresh world with Additions 0.2.0");
      for (var design : TrackDesign.values()) {
        int lane = design.ordinal();
        var from = new BlockPos(-120 + lane * 32, 120, 0);
        var to = from.offset(0, 0, 60);
        for (int x = (from.getX() - 16) >> 4; x <= (from.getX() + 16) >> 4; x++)
          for (int z = -1; z <= 4; z++) level.setChunkForced(x, z, true);
        for (var p : List.of(from, to))
          level.setBlockAndUpdate(
              p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        var player = FakePlayerFactory.getMinecraft(level);
        player.setGameMode(GameType.CREATIVE);
        player.setPos(to.getCenter().add(2, 1, 0));
        player.setItemInHand(
            InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
        var rail =
            TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
        if (design == TrackDesign.FREE_SPIN)
          rail.magnets =
              List.of(
                  new dev.fourthdimension.physics.MagneticSpan(
                      36, 45, dev.fourthdimension.physics.MagneticSpan.Mode.INDUCE, 1, 2, .1, 1));
        var span = List.of(new FinSpanSegment(from, to, 0, 1, true));
        var stationPos = from.offset(-8, 0, 18);
        level.setBlockAndUpdate(stationPos, CFBlocks.STATION.get().defaultBlockState());
        var station = (StationBlockEntity) level.getBlockEntity(stationPos);
        station.setSpan(span);
        AdditionsWirelessFixture.place(level, stationPos);
        station.setDispatch(true, 12, 3, 0);
        station.setChanged();
        for (var floor :
            BlockPos.betweenClosed(stationPos.offset(-2, -2, -4), stationPos.offset(2, -2, 4)))
          level.setBlockAndUpdate(floor, Blocks.SMOOTH_STONE.defaultBlockState());
        var finPos = from.offset(-10, 0, 24);
        level.setBlockAndUpdate(
            finPos,
            BuiltInRegistries.BLOCK
                .get(ResourceLocation.parse("coasterfins:coaster_fin"))
                .defaultBlockState());
        var fin = level.getBlockEntity(finPos);
        var tag = fin.saveWithoutMetadata(level.registryAccess());
        tag.putString("SpanMode", "boost");
        var segments = new ListTag();
        span.forEach(segment -> segments.add(segment.write()));
        tag.put("SpanSegments", segments);
        tag.putInt("ScrollValue", 10);
        fin.loadWithComponents(tag, level.registryAccess());
        fin.setChanged();
        level.setBlockAndUpdate(finPos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
      }
      return;
    }
    if (data.rails.size() != 5)
      throw new IllegalArgumentException("Create five fixture rails first");
    if (action.equals("cars")) {
      if (!data.vehicles.isEmpty())
        throw new IllegalArgumentException("Fixture cars already exist");
      for (var rail : data.rails) {
        AdditionsWirelessFixture.bind(
            level,
            (StationBlockEntity) level.getBlockEntity(rail.from.offset(-8, 0, 18)),
            TrackDesign.byVariant(rail.variant));
        var fin =
            (com.simibubi.create.foundation.blockEntity.SmartBlockEntity)
                level.getBlockEntity(rail.from.offset(-10, 0, 24));
        fin.getBehaviour(
                com.simibubi
                    .create
                    .foundation
                    .blockEntity
                    .behaviour
                    .scrollValue
                    .ScrollValueBehaviour
                    .TYPE)
            .setValue(10);
        var edge = TrackGeometry.resolve(level, rail);
        VehicleFactory.create(
            level,
            TrackDesign.byVariant(rail.variant),
            new CoasterPathTrackFrame.GraphHit(
                CoasterPathGraphManager.sampleEdgePosition(level, edge, .3), edge, .3));
        for (var floor :
            BlockPos.betweenClosed(rail.from.offset(-8, -5, 0), rail.from.offset(8, -5, 32)))
          level.setBlockAndUpdate(floor, Blocks.SMOOTH_STONE.defaultBlockState());
      }
      return;
    }
    for (var car : cars(level)) {
      if (!action.equals("capture") && !Boarding.ready(level, car))
        throw new IllegalArgumentException("Fixture is not captured and aligned");
      switch (action) {
        case "pigs" -> {
          if (car.restraints || car.carriers.stream().anyMatch(c -> !c.occupants.isEmpty()))
            throw new IllegalArgumentException("Fixture must be empty and open");
          var carrier = car.carriers.getFirst();
          var pig = EntityType.PIG.create(level);
          pig.setNoAi(true);
          pig.addTag("fd_additions_lifecycle");
          pig.setPos(car.boundFrom.getCenter().add(0, 3, 18));
          level.addFreshEntity(pig);
          SeatBlock.sitDown(
              level,
              container
                  .getSubLevel(carrier.body)
                  .getPlot()
                  .getCenterBlock()
                  .offset(carrier.seats.getFirst()),
              pig);
          if (!pig.isPassenger()) throw new IllegalArgumentException("Fixture pig could not board");
        }
        case "close" -> VehicleOperations.restraints(level, car, true);
        case "release" -> {
          station(level, car).setDispatch(true, 1, 2, 0);
          station(level, car).setChanged();
        }
        case "capture" -> {
          station(level, car).setDispatch(true, 12, 3, 0);
          station(level, car).setChanged();
          level.setBlockAndUpdate(
              car.boundFrom.offset(-10, -1, 24), Blocks.AIR.defaultBlockState());
          if (car.family != dev.fourthdimension.contract.Family.PAX)
            VehicleOperations.capture(level, car);
        }
        case "unload" -> {
          VehicleOperations.restraints(level, car, false);
          for (var carrier : car.carriers)
            for (var id : List.copyOf(carrier.occupants)) {
              var entity = level.getEntity(id);
              if (entity != null) entity.stopRiding();
            }
        }
        default -> throw new IllegalArgumentException("Unknown fixture action");
      }
    }
  }

  private static void report(ServerLevel level, String label) {
    var data = FoundationData.get(level);
    var result = new JsonObject();
    result.addProperty("label", label);
    result.addProperty("tick", level.getGameTime());
    result.addProperty("additions", OptionalIntegrations.supported("coasterfins", Set.of("0.2.0")));
    result.add(
        "foundation",
        NbtOps.INSTANCE.convertTo(
            JsonOps.INSTANCE, data.save(new CompoundTag(), level.registryAccess())));
    var rails = new JsonArray();
    for (var rail : data.rails) {
      var r = new JsonObject();
      r.addProperty("id", rail.id.toString());
      r.addProperty("resolved", TrackGeometry.resolve(level, rail) != null);
      rails.add(r);
    }
    result.add("rails", rails);
    var vehicles = new JsonArray();
    var container = SubLevelContainer.getContainer(level);
    for (var car : cars(level)) {
      var row = new JsonObject();
      row.addProperty("id", car.chassis.toString());
      row.addProperty("loaded", container.getSubLevel(car.chassis) != null);
      row.addProperty("ready", Boarding.ready(level, car));
      row.addProperty("restraints", car.restraints);
      row.addProperty("fault", car.fault);
      row.addProperty("family", car.family.id());
      row.addProperty("stopping", car.stopping);
      row.addProperty("driveAllowed", ExternalDrivePolicy.allows(level, car.chassis));
      var chassis = container.getSubLevel(car.chassis);
      row.addProperty(
          "speed",
          container
              .physicsSystem()
              .getPipeline()
              .getLinearVelocity(
                  (dev.ryanhcode.sable.sublevel.ServerSubLevel) chassis, new org.joml.Vector3d())
              .length());
      var station = station(level, car);
      row.add(
          "telemetry",
          NbtOps.INSTANCE.convertTo(
              JsonOps.INSTANCE, ((RideInformation.Source) station).telemetry().save()));
      row.add(
          "stationNbt",
          NbtOps.INSTANCE.convertTo(
              JsonOps.INSTANCE, station.saveWithFullMetadata(level.registryAccess())));
      var powers = new JsonArray();
      for (int i = 0; i < 4; i++) powers.add(station.channel(i).power());
      row.add("channels", powers);
      row.add(
          "received",
          new Gson().toJsonTree(AdditionsWirelessFixture.received(level, station.getBlockPos())));
      row.add(
          "finNbt",
          NbtOps.INSTANCE.convertTo(
              JsonOps.INSTANCE,
              level
                  .getBlockEntity(car.boundFrom.offset(-10, 0, 24))
                  .saveWithFullMetadata(level.registryAccess())));
      var carriers = new JsonArray();
      for (var carrier : car.carriers) {
        var body = new JsonObject();
        body.addProperty("id", carrier.body.toString());
        body.addProperty("loaded", container.getSubLevel(carrier.body) != null);
        var riders = new JsonArray();
        for (var id : carrier.occupants) {
          var entity = level.getEntity(id);
          var seat = entity == null ? null : entity.getVehicle();
          var parent = seat == null ? null : Sable.HELPER.getContaining(seat);
          var rider = new JsonObject();
          rider.addProperty("id", id.toString());
          rider.addProperty("player", entity instanceof ServerPlayer);
          rider.addProperty("alive", entity != null && entity.isAlive());
          rider.addProperty("seat", seat == null ? "" : seat.getUUID().toString());
          rider.addProperty("carrier", parent == null ? "" : parent.getUniqueId().toString());
          riders.add(rider);
        }
        body.add("riders", riders);
        carriers.add(body);
      }
      row.add("carriers", carriers);
      vehicles.add(row);
    }
    result.add("cars", vehicles);
    LogUtils.getLogger().info("FD_ADDITIONS_LIFECYCLE {}", result);
  }
}
