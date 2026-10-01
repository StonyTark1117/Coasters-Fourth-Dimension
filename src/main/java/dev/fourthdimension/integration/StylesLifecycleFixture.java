package dev.fourthdimension.integration;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Opt-in occupied appearance lifecycle fixture. Every mutation uses normal ride validation. */
public final class StylesLifecycleFixture {
  private StylesLifecycleFixture() {}

  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var root = Commands.literal("styles_restart");
    for (String action : List.of("create", "cars", "pigs", "close", "unload", "reject_edit"))
      root.then(
          Commands.literal(action)
              .executes(
                  ctx -> {
                    try {
                      act(ctx.getSource().getLevel(), action);
                      ctx.getSource()
                          .sendSuccess(
                              () -> Component.literal("Styles lifecycle fixture: " + action),
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
                        "lane", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 1))
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
                                      6.5,
                                      0,
                                      25);
                                  SeatBlock.sitDown(player.serverLevel(), seat, player);
                                  if (!player.isPassenger())
                                    throw new IllegalArgumentException(
                                        "Normal boarding was denied");
                                  ctx.getSource()
                                      .sendSuccess(
                                          () ->
                                              Component.literal("Styles lifecycle player boarded"),
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
                                  () -> Component.literal("Styles lifecycle snapshot recorded"),
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
    if (result.size() != 3) throw new IllegalArgumentException("Create three fixture cars first");
    return result;
  }

  private static ServerPlayer editor(ServerLevel level, FoundationData.Rail rail) {
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000411"), "FDStylesLifecycle"));
    player.setGameMode(GameType.CREATIVE);
    player.setPos(rail.from.getCenter().add(2, 1, 0));
    return player;
  }

  private static void act(ServerLevel level, String action) {
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    if (action.equals("create")) {
      if (!OptionalIntegrations.supported("coasterssimulatedextratypes", Set.of("1.2.0"))
          || !data.rails.isEmpty()
          || !data.vehicles.isEmpty())
        throw new IllegalArgumentException("Use a fresh world with Track Styles 1.2.0");
      for (int lane = 0; lane < 3; lane++) {
        var from = new BlockPos(-120 + lane * 20, 120, 0);
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
            TrackPlacement.connect(
                level, from, to, TrackDesign.ARROW, player, player.getMainHandItem());
        var edit = editor(level, rail);
        ProfileEditor.apply(
            edit,
            rail.id,
            rail.revision,
            new RotationProfile(
                List.of(
                    new RotationProfile.Key(0, 0, 0), new RotationProfile.Key(12, 0, 0),
                    new RotationProfile.Key(36, 2 * Math.PI, 0),
                        new RotationProfile.Key(60, 0, 0))),
            false);
        String style =
            lane == 0
                ? "coasterssimulatedextratypes:arrow_4d"
                : lane == 1
                    ? "coasterssimulatedextratypes:arrow_4d_dbl_spine"
                    : TrackAppearance.ORIGINAL.style();
        AppearanceProviders.apply(edit, rail.id, rail.revision, style);
      }
      return;
    }
    if (data.rails.size() != 3)
      throw new IllegalArgumentException("Create three fixture rails first");
    if (action.equals("cars")) {
      if (!data.vehicles.isEmpty())
        throw new IllegalArgumentException("Fixture cars already exist");
      for (var rail : data.rails) {
        var edge = TrackGeometry.resolve(level, rail);
        VehicleFactory.create(
            level,
            TrackDesign.ARROW,
            new CoasterPathTrackFrame.GraphHit(
                CoasterPathGraphManager.sampleEdgePosition(level, edge, .1), edge, .1));
        for (var floor :
            BlockPos.betweenClosed(rail.from.offset(-8, -5, 0), rail.from.offset(8, -5, 16)))
          level.setBlockAndUpdate(floor, Blocks.SMOOTH_STONE.defaultBlockState());
      }
      return;
    }
    if (action.equals("reject_edit")) {
      for (var rail : data.rails) {
        String before = data.save(new CompoundTag(), level.registryAccess()).toString();
        try {
          AppearanceProviders.apply(
              editor(level, rail), rail.id, rail.revision, TrackAppearance.ORIGINAL.style());
          throw new IllegalStateException("Occupied appearance edit was accepted");
        } catch (IllegalArgumentException failure) {
          if (!failure.getMessage().contains("occupied")) throw failure;
        }
        if (!before.equals(data.save(new CompoundTag(), level.registryAccess()).toString()))
          throw new IllegalStateException("Rejected edit changed ride data");
      }
      return;
    }
    for (var car : cars(level)) {
      if (!Boarding.ready(level, car))
        throw new IllegalArgumentException("Fixture is not captured and aligned");
      switch (action) {
        case "pigs" -> {
          if (car.restraints || car.carriers.stream().anyMatch(c -> !c.occupants.isEmpty()))
            throw new IllegalArgumentException("Fixture must be empty and open");
          var carrier = car.carriers.getFirst();
          var pig = EntityType.PIG.create(level);
          pig.setNoAi(true);
          pig.addTag("fd_styles_lifecycle");
          pig.setPos(car.boundFrom.getCenter().add(0, 3, 6));
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
    result.addProperty(
        "styles", OptionalIntegrations.supported("coasterssimulatedextratypes", Set.of("1.2.0")));
    result.add(
        "foundation",
        NbtOps.INSTANCE.convertTo(
            JsonOps.INSTANCE, data.save(new CompoundTag(), level.registryAccess())));
    var rails = new JsonArray();
    for (var rail : data.rails) {
      var r = new JsonObject();
      r.addProperty("id", rail.id.toString());
      r.addProperty("resolved", TrackGeometry.resolve(level, rail) != null);
      r.addProperty("effectiveStyle", rail.appearance.effective(rail.family).style());
      r.add("owners", new Gson().toJsonTree(rail.appearance.effective(rail.family).renderOwners()));
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
    LogUtils.getLogger().info("FD_STYLES_LIFECYCLE {}", result);
  }
}
