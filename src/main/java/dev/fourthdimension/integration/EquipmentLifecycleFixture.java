package dev.fourthdimension.integration;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.integration.magnetized.MagnetizedLifecycleAccess;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit validation-only parked fixture, including readback with the optional addon absent. */
public final class EquipmentLifecycleFixture {
  private EquipmentLifecycleFixture() {}

  private static boolean installed() {
    return OptionalIntegrations.supported("coastersmagnetized", Set.of("1.1.0"));
  }

  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var root = Commands.literal("magnetized_restart");
    for (var action : List.of("create", "cars", "board", "unload"))
      root.then(
          Commands.literal(action)
              .executes(
                  ctx -> {
                    try {
                      act(ctx.getSource().getLevel(), action);
                      ctx.getSource()
                          .sendSuccess(
                              () -> Component.literal("Equipment lifecycle fixture: " + action),
                              false);
                      return 1;
                    } catch (IllegalArgumentException e) {
                      ctx.getSource().sendFailure(Component.literal(e.getMessage()));
                      return 0;
                    }
                  }));
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
                                  () -> Component.literal("Equipment lifecycle snapshot recorded"),
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

  private static BlockPos from(TrackDesign design) {
    return new BlockPos(-900 + design.ordinal() * 32, 160, -200);
  }

  private static void act(ServerLevel level, String action) {
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    if (action.equals("create")) {
      if (!installed() || !data.rails.isEmpty() || !data.vehicles.isEmpty())
        throw new IllegalArgumentException("Use a fresh world with Magnetized 1.1.0");
      var player =
          FakePlayerFactory.get(
              level,
              new com.mojang.authlib.GameProfile(
                  UUID.fromString("fdfdfdfd-0000-0000-0000-000000000410"), "FDEquipmentLifecycle"));
      player.setGameMode(GameType.CREATIVE);
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      for (var design : TrackDesign.values()) {
        var from = from(design);
        var to = from.offset(0, 0, 60);
        for (int x = (from.getX() - 16) >> 4; x <= (from.getX() + 16) >> 4; x++)
          for (int z = (from.getZ() - 16) >> 4; z <= (to.getZ() + 16) >> 4; z++)
            level.setChunkForced(x, z, true);
        for (var pos : List.of(from, to)) {
          level.setBlockAndUpdate(
              pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
          level.setBlockAndUpdate(pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        }
        player.setPos(to.getCenter().add(2, 1, 0));
        var rail =
            TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
        if (design == TrackDesign.FREE_SPIN)
          rail.magnets =
              List.of(
                  new dev.fourthdimension.physics.MagneticSpan(
                      0, 60, dev.fourthdimension.physics.MagneticSpan.Mode.INDUCE, 1, 2, .1, 1));
        MagnetizedLifecycleAccess.configure(level, from, false);
        MagnetizedLifecycleAccess.configure(level, to, true);
      }
      data.setDirty();
      return;
    }
    if (data.rails.size() != 5)
      throw new IllegalArgumentException("Create the five-family fixture first");
    if (action.equals("cars")) {
      if (!data.vehicles.isEmpty())
        throw new IllegalArgumentException("Fixture cars already exist");
      for (var rail : data.rails) {
        var design =
            Arrays.stream(TrackDesign.values())
                .filter(d -> d.family == rail.family())
                .findFirst()
                .orElseThrow();
        var edge = TrackGeometry.resolve(level, rail);
        VehicleFactory.create(
            level,
            design,
            new CoasterPathTrackFrame.GraphHit(
                CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5));
      }
      return;
    }
    if (data.vehicles.size() != 5)
      throw new IllegalArgumentException("Assemble the fixture cars first");
    for (var v : data.vehicles.values()) {
      if (!Boarding.ready(level, v))
        throw new IllegalArgumentException("Fixture car not ready: " + v.family);
      if (action.equals("board")) {
        if (v.restraints || v.carriers.stream().anyMatch(c -> !c.occupants.isEmpty()))
          throw new IllegalArgumentException("Fixture must be empty with open restraints");
        var c = v.carriers.getFirst();
        var pig = EntityType.PIG.create(level);
        pig.setNoAi(true);
        pig.addTag("fd_equipment_lifecycle");
        pig.setPos(v.boundFrom.getCenter().add(0, 3, 30));
        level.addFreshEntity(pig);
        SeatBlock.sitDown(
            level,
            container.getSubLevel(c.body).getPlot().getCenterBlock().offset(c.seats.getFirst()),
            pig);
        if (!pig.isPassenger()) throw new IllegalArgumentException("Fixture rider did not board");
        VehicleOperations.restraints(level, v, true);
      } else {
        VehicleOperations.restraints(level, v, false);
        for (var c : v.carriers)
          for (var id : List.copyOf(c.occupants)) {
            var rider = level.getEntity(id);
            if (rider != null && rider.getTags().contains("fd_equipment_lifecycle"))
              rider.stopRiding();
          }
      }
    }
  }

  private static void report(ServerLevel level, String label) {
    var result = new JsonObject();
    result.addProperty("label", label);
    result.addProperty("tick", level.getGameTime());
    result.addProperty("magnetized", installed());
    var data = FoundationData.get(level);
    var anchors = new JsonArray();
    for (var rail : data.rails)
      for (var pos : List.of(rail.from, rail.to)) {
        var anchor = new JsonObject();
        anchor.addProperty("family", rail.family().name());
        anchor.addProperty("pos", pos.toShortString());
        anchor.addProperty("powered", level.hasNeighborSignal(pos));
        var be = level.getBlockEntity(pos);
        anchor.addProperty(
            "nbt", be == null ? "" : be.saveWithFullMetadata(level.registryAccess()).toString());
        if (installed()) anchor.add("equipment", MagnetizedLifecycleAccess.inspect(level, pos));
        anchors.add(anchor);
      }
    result.add("anchors", anchors);
    var cars = new JsonArray();
    var container = SubLevelContainer.getContainer(level);
    for (var v : data.vehicles.values()) {
      var car = new JsonObject();
      car.addProperty("chassis", v.chassis.toString());
      car.addProperty("family", v.family.name());
      car.addProperty("ready", Boarding.ready(level, v));
      car.addProperty("restraints", v.restraints);
      car.addProperty("stopping", v.stopping);
      car.addProperty("fault", v.fault);
      car.addProperty("externalDriveAllowed", ExternalDrivePolicy.allows(level, v.chassis));
      var carriers = new JsonArray();
      for (var c : v.carriers) {
        var carrier = new JsonObject();
        carrier.addProperty("body", c.body.toString());
        carrier.addProperty("loaded", container.getSubLevel(c.body) != null);
        carrier.addProperty("capture", c.capture);
        var riders = new JsonArray();
        for (var id : c.occupants) {
          var rider = new JsonObject();
          var entity = level.getEntity(id);
          var parent =
              entity == null || entity.getVehicle() == null
                  ? null
                  : Sable.HELPER.getContaining(entity.getVehicle());
          rider.addProperty("id", id.toString());
          rider.addProperty("alive", entity != null && entity.isAlive());
          rider.addProperty("mounted", entity != null && entity.isPassenger());
          rider.addProperty("carrier", parent == null ? "" : parent.getUniqueId().toString());
          riders.add(rider);
        }
        carrier.add("riders", riders);
        carriers.add(carrier);
      }
      car.add("carriers", carriers);
      cars.add(car);
    }
    result.add("cars", cars);
    LogUtils.getLogger().info("FD_EQUIPMENT_LIFECYCLE {}", result);
  }
}
