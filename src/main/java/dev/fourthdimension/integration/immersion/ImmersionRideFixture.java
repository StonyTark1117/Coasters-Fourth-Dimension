package dev.fourthdimension.integration.immersion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Vector3d;

/** Opt-in camera acceptance circuit. No mounted camera or carrier pose is assigned. */
public final class ImmersionRideFixture {
  private static final BlockPos ORIGIN = new BlockPos(450, 140, 480);

  private ImmersionRideFixture() {}

  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var root = Commands.literal("camera_ride");
    for (var action : new String[] {"create", "close", "dispatch", "capture", "open"})
      root.then(
          Commands.literal(action)
              .executes(
                  ctx -> {
                    try {
                      act(ctx.getSource().getLevel(), action);
                      ctx.getSource()
                          .sendSuccess(() -> Component.literal("Camera ride: " + action), false);
                      return 1;
                    } catch (IllegalArgumentException failure) {
                      ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                      return 0;
                    }
                  }));
    root.then(
        Commands.literal("board")
            .then(
                Commands.argument("carrier", IntegerArgumentType.integer(0, 3))
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                ctx -> {
                                  var player = EntityArgument.getPlayer(ctx, "player");
                                  var level = player.serverLevel();
                                  var car = car(level);
                                  var carrier =
                                      car.carriers.get(
                                          IntegerArgumentType.getInteger(ctx, "carrier"));
                                  var body =
                                      SubLevelContainer.getContainer(level)
                                          .getSubLevel(carrier.body);
                                  if (player.isPassenger() || body == null)
                                    throw new IllegalArgumentException(
                                        "Player or carrier unavailable");
                                  var seat =
                                      body.getPlot()
                                          .getCenterBlock()
                                          .offset(carrier.seats.getFirst());
                                  if (!Boarding.canBoard(level, seat)
                                      || SeatBlock.isSeatOccupied(level, seat))
                                    throw new IllegalArgumentException("Seat not ready");
                                  // Move an unmounted operator to the real station before native
                                  // seat boarding.
                                  player.teleportTo(level, 443.5, 140, 500.5, -90, 10);
                                  SeatBlock.sitDown(level, seat, player);
                                  if (!player.isPassenger())
                                    throw new IllegalArgumentException("Native boarding denied");
                                  ctx.getSource()
                                      .sendSuccess(
                                          () -> Component.literal("Camera ride: boarded"), false);
                                  return 1;
                                }))));
    root.then(
        Commands.literal("report")
            .then(
                Commands.argument("label", StringArgumentType.word())
                    .executes(
                        ctx -> {
                          report(
                              ctx.getSource().getLevel(),
                              StringArgumentType.getString(ctx, "label"));
                          ctx.getSource()
                              .sendSuccess(
                                  () -> Component.literal("Camera snapshot recorded"), false);
                          return 1;
                        })));
    event
        .getDispatcher()
        .register(
            Commands.literal("fd")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("validation").then(root)));
  }

  private static FoundationData.Vehicle car(ServerLevel level) {
    var data = FoundationData.get(level);
    if (data.vehicles.size() != 1)
      throw new IllegalArgumentException("Use the single-car camera fixture");
    var car = data.vehicles.values().iterator().next();
    if (!car.variant.equals(TrackDesign.REVISED.variant))
      throw new IllegalArgumentException("Expected Revised car");
    return car;
  }

  private static void act(ServerLevel level, String action) {
    if (action.equals("create")) {
      if (!FoundationData.get(level).vehicles.isEmpty()
          || !FoundationData.get(level).rails.isEmpty())
        throw new IllegalArgumentException("Use a fresh camera fixture world");
      for (int x = 27; x <= 31; x++)
        for (int z = 29; z <= 34; z++) level.setChunkForced(x, z, true);
      ExampleLayouts.create(level, ORIGIN, TrackDesign.REVISED);
      return;
    }
    var car = car(level);
    switch (action) {
      case "close" -> VehicleOperations.restraints(level, car, true);
      case "open" -> VehicleOperations.restraints(level, car, false);
      case "capture" -> VehicleOperations.capture(level, car);
      case "dispatch" -> {
        level.setBlockAndUpdate(ORIGIN.offset(-6, 0, 21), Blocks.AIR.defaultBlockState());
        VehicleOperations.dispatch(level, car);
      }
      default -> throw new IllegalArgumentException("Unknown camera action");
    }
    FoundationSync.broadcast(level);
  }

  private static void report(ServerLevel level, String label) {
    var car = car(level);
    var container = SubLevelContainer.getContainer(level);
    var body = (ServerSubLevel) container.getSubLevel(car.chassis);
    var center = CoasterCartResolve.cartCenterWorld(body);
    var result = new JsonObject();
    result.addProperty("label", label);
    result.addProperty("tick", level.getGameTime());
    result.addProperty("family", car.family.name());
    result.addProperty("chassis", car.chassis.toString());
    result.addProperty("ready", Boarding.ready(level, car));
    result.addProperty("restraints", car.restraints);
    result.addProperty("stopping", car.stopping);
    result.addProperty("fault", car.fault);
    result.addProperty("x", center.x);
    result.addProperty("y", center.y);
    result.addProperty("z", center.z);
    result.addProperty(
        "speed",
        container.physicsSystem().getPipeline().getLinearVelocity(body, new Vector3d()).length());
    var carriers = new JsonArray();
    for (var carrier : car.carriers) {
      var row = new JsonObject();
      row.addProperty("body", carrier.body.toString());
      row.addProperty("angle", carrier.angle);
      row.addProperty("capture", carrier.capture);
      var riders = new JsonArray();
      for (var id : carrier.occupants) riders.add(id.toString());
      row.add("riders", riders);
      carriers.add(row);
    }
    result.add("carriers", carriers);
    LogUtils.getLogger().info("FD_CAMERA_RIDE {}", result);
  }
}
