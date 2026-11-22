package dev.fourthdimension.runtime;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.AppearanceEditor;
import dev.fourthdimension.integration.TrackAppearance;
import dev.fourthdimension.track.ExampleLayouts;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackPlacement;
import dev.fourthdimension.vehicle.Boarding;
import dev.fourthdimension.vehicle.CarrierSeatBlock;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Local-only helpers for reproducible validation; registered only with the validation JVM flag. */
public final class ValidationCommands {
  private ValidationCommands() {}

  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var validation =
        Commands.literal("validation")
            .then(
                Commands.literal("availability")
                    .executes(context -> availability(context.getSource())))
            .then(
                Commands.literal("route_state")
                    .executes(context -> routeState(context.getSource())))
            .then(
                Commands.literal("engineered_loop")
                    .executes(context -> engineeredLoop(context.getSource(), TrackDesign.ARROW))
                    .then(
                        Commands.literal("legacy")
                            .executes(
                                context ->
                                    engineeredLoop(context.getSource(), TrackDesign.LEGACY)))
                    .then(
                        Commands.literal("free_spin")
                            .executes(
                                context ->
                                    engineeredLoop(context.getSource(), TrackDesign.FREE_SPIN)))
                    .then(
                        Commands.literal("revised")
                            .executes(
                                context ->
                                    engineeredLoop(context.getSource(), TrackDesign.REVISED))))
            .then(
                Commands.literal("engineered_car")
                    .executes(context -> engineeredCar(context.getSource())))
            .then(
                Commands.literal("board")
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context ->
                                    board(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        false))))
            .then(
                Commands.literal("board_locked")
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context ->
                                    board(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        true))))
            .then(
                Commands.literal("board_arrow_locked")
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context ->
                                    boardArrowLocked(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
            .then(
                Commands.literal("dispatch")
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context ->
                                    dispatch(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
            .then(
                Commands.literal("motion")
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context ->
                                    motion(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
            .then(
                Commands.literal("arrow_trace")
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context ->
                                    ArrowTraceValidation.start(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
            .then(
                Commands.literal("style")
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context ->
                                    style(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))));
    event
        .getDispatcher()
        .register(
            Commands.literal("fd")
                .requires(source -> source.hasPermission(2))
                .then(validation));
  }

  private static int engineeredLoop(
      net.minecraft.commands.CommandSourceStack source, TrackDesign design) {
    try {
      ExampleLayouts.createBare(
          source.getLevel(), BlockPos.containing(source.getPosition()).offset(0, 3, 12), design);
      source.sendSuccess(
          () ->
              Component.literal(
                  "Bare original "
                      + design.name()
                      + " circuit built; install the optional motor and place a car before binding the station."),
          false);
      return 1;
    } catch (IllegalArgumentException failure) {
      return failure(source, failure.getMessage());
    }
  }

  private static int engineeredCar(net.minecraft.commands.CommandSourceStack source) {
    try {
      var vehicle =
          ExampleLayouts.addBareArrowCar(
              source.getLevel(), BlockPos.containing(source.getPosition()).offset(0, 3, 12));
      source.sendSuccess(
          () -> Component.literal("Bare Arrow loop car placed and station bound: " + vehicle.chassis),
          false);
      return 1;
    } catch (IllegalArgumentException | IllegalStateException failure) {
      return failure(source, failure.getMessage());
    }
  }

  private static int availability(net.minecraft.commands.CommandSourceStack source) {
    var level = source.getLevel();
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return failure(source, "No native sublevel container is available");
    int chassis = 0, chassisTotal = 0, carriers = 0, carriersTotal = 0, assignments = 0;
    for (var vehicle : FoundationData.get(level).vehicles.values()) {
      chassisTotal++;
      if (container.getSubLevel(vehicle.chassis) != null) chassis++;
      for (var carrier : vehicle.carriers) {
        carriersTotal++;
        if (container.getSubLevel(carrier.body) != null) carriers++;
        assignments += carrier.riderSeats.size();
      }
    }
    int loadedChassis = chassis;
    int loadedCarriers = carriers;
    int totalChassis = chassisTotal;
    int totalCarriers = carriersTotal;
    int totalAssignments = assignments;
    source.sendSuccess(
        () ->
            Component.literal(
                "Validation availability chassis="
                    + loadedChassis
                    + "/"
                    + totalChassis
                    + " carriers="
                    + loadedCarriers
                    + "/"
                    + totalCarriers
                    + " assignments="
                    + totalAssignments),
        false);
    return 1;
  }

  /** Read-only route telemetry for C2/C6 evidence; deliberately does not mutate route state. */
  private static int routeState(net.minecraft.commands.CommandSourceStack source) {
    var route = FoundationData.get(source.getLevel()).rideLocal;
    source.sendSuccess(
        () ->
            Component.literal(
                "Validation route_state route="
                    + route.routeId
                    + " trains="
                    + route.trains.size()
                    + " reservations="
                    + route.reservations.size()),
        false);
    route.trains.values().stream()
        .sorted(java.util.Comparator.comparing(train -> train.chassis.toString()))
        .forEach(
            train -> {
    var body = SubLevelContainer.getContainer(source.getLevel()).getSubLevel(train.chassis);
    var position = body instanceof ServerSubLevel nativeBody ? nativeBody.logicalPose().position() : null;
    var center = body == null ? null : dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(body);
    source.sendSuccess(
                    () ->
                        Component.literal(
                            "train chassis="
                                + train.chassis
                                + " cars="
                                + train.cars
                                + " block="
                                + train.block
                                + " nextBlock="
                                + train.nextBlock
                                + " pos="
                                + (position == null ? "missing" : position.x()+","+position.y()+","+position.z())
                                + " center="
                                + (center == null ? "missing" : center.x()+","+center.y()+","+center.z())
                                + " braking="
                                + train.braking
                                + " unresolved="
                                + train.unresolved
                                + " restored="
                                + train.restored),
                    false);
            });
    route.reservations.entrySet().stream()
        .sorted(java.util.Map.Entry.comparingByKey())
        .forEach(
            reservation ->
                source.sendSuccess(
                    () ->
                        Component.literal(
                            "reservation block="
                                + reservation.getKey()
                                + " chassis="
                                + reservation.getValue()),
                    false));
    return 1;
  }

  private static int board(
      net.minecraft.commands.CommandSourceStack source, ServerPlayer player, boolean closeRestraints) {
    var level = player.serverLevel();
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return failure(source, "No native sublevel container is available");
    for (var vehicle : FoundationData.get(level).vehicles.values()) {
      if (vehicle.restraints || !Boarding.ready(level, vehicle)) continue;
      for (var carrier : vehicle.carriers) {
        var body = container.getSubLevel(carrier.body);
        if (body == null) continue;
        for (var offset : carrier.seats) {
          var seat = body.getPlot().getCenterBlock().offset(offset);
          if (!(level.getBlockState(seat).getBlock() instanceof CarrierSeatBlock)
              || SeatBlock.isSeatOccupied(level, seat)
              || carrier.riderSeats.containsValue(offset)) continue;
          SeatBlock.sitDown(level, seat, player);
          if (!player.isPassenger()) return failure(source, "Seat rejected the validation mount");
          if (closeRestraints) {
            try {
              dev.fourthdimension.vehicle.VehicleOperations.restraints(level, vehicle, true);
            } catch (IllegalArgumentException exception) {
              player.stopRiding();
              return failure(source, exception.getMessage());
            }
          }
          source.sendSuccess(
              () ->
                  Component.literal(
                      "Validation boarded "
                          + player.getGameProfile().getName()
                          + (closeRestraints ? " and closed restraints" : "")),
              false);
          return 1;
        }
      }
    }
    return failure(source, "No ready unoccupied fourth-dimension carrier seat was found");
  }

  private static int boardArrowLocked(
      net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
    var level = player.serverLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return failure(source, "No native sublevel container is available");
    for (var vehicle : data.vehicles.values()) {
      if (vehicle.family != Family.ARROW || vehicle.restraints || !Boarding.ready(level, vehicle)) continue;
      var chassis = container.getSubLevel(vehicle.chassis);
      if (chassis == null) continue;
      var linked = dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint
          .getTrainLinkedCartIds(chassis);
      if (linked.size() != 2
          || linked.stream().anyMatch(
              id -> {
                var member = data.vehicles.get(id);
                return member == null || member.family != Family.ARROW;
              })) continue;
      for (var carrier : vehicle.carriers) {
        var body = container.getSubLevel(carrier.body);
        if (body == null) continue;
        for (var offset : carrier.seats) {
          var seat = body.getPlot().getCenterBlock().offset(offset);
          if (!(level.getBlockState(seat).getBlock() instanceof CarrierSeatBlock)
              || SeatBlock.isSeatOccupied(level, seat)
              || carrier.riderSeats.containsValue(offset)) continue;
          SeatBlock.sitDown(level, seat, player);
          if (!player.isPassenger()) return failure(source, "Arrow seat rejected the validation mount");
          try {
            dev.fourthdimension.vehicle.VehicleOperations.restraints(level, vehicle, true);
          } catch (IllegalArgumentException exception) {
            player.stopRiding();
            return failure(source, exception.getMessage());
          }
          source.sendSuccess(
              () -> Component.literal("Validation boarded and closed restraints on linked Arrow chassis " + vehicle.chassis),
              false);
          return 1;
        }
      }
    }
    return failure(source, "No ready two-chassis native-linked Arrow seat was found");
  }

  private static int style(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
    var data = FoundationData.get(player.serverLevel());
    var rail =
        data.rails.stream()
            .filter(
                candidate ->
                    candidate.family == Family.ARROW && candidate.appearance.original())
            .filter(
                candidate ->
                    Math.min(
                            player.distanceToSqr(candidate.from.getCenter()),
                            player.distanceToSqr(candidate.to.getCenter()))
                        <= 64)
            .filter(
                candidate ->
                    data.rails.stream()
                        .filter(other -> other == candidate || other.adjacent(candidate))
                        .noneMatch(other -> TrackPlacement.occupied(data, other)))
            .findFirst()
            .orElse(null);
    if (rail == null)
      return failure(source, "No original controlled Arrow rail is within eight blocks");
    try {
      AppearanceEditor.apply(
          player,
          rail.id,
          rail.revision,
          new TrackAppearance(TrackAppearance.CANDIDATES.getFirst(), -1));
    } catch (IllegalArgumentException exception) {
      return failure(source, exception.getMessage());
    }
    source.sendSuccess(
        () -> Component.literal("Validation selected Track Styles Arrow appearance"), false);
    return 1;
  }

  private static int dispatch(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
    var level = player.serverLevel();
    var vehicle = assigned(level, player);
    if (vehicle == null)
      return failure(source, "No assigned fourth-dimension vehicle was found for the player");
    try {
      dev.fourthdimension.vehicle.VehicleOperations.dispatch(level, vehicle);
    } catch (IllegalArgumentException exception) {
      return failure(source, exception.getMessage());
    }
    source.sendSuccess(
        () -> Component.literal("Validation dispatched " + player.getGameProfile().getName()), false);
    return 1;
  }

  private static int motion(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
    var level = player.serverLevel();
    var vehicle = assigned(level, player);
    var container = SubLevelContainer.getContainer(level);
    if (vehicle == null || container == null || container.getSubLevel(vehicle.chassis) == null)
      return failure(source, "No loaded assigned fourth-dimension vehicle was found for the player");
    var body = container.getSubLevel(vehicle.chassis);
    if (!(body instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel parent))
      return failure(source, "The assigned vehicle has no server-native chassis body");
    var pose = dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(body);
    var velocity =
        container.physicsSystem().getPipeline().getLinearVelocity(parent, new org.joml.Vector3d());
    source.sendSuccess(
        () ->
            Component.literal(
                "Validation motion chassis="
                    + vehicle.chassis
                    + " position="
                    + pose
                    + " velocity="
                    + velocity
                    + " capture="
                    + vehicle.carriers.stream().allMatch(c -> c.capture)),
        false);
    return 1;
  }

  private static FoundationData.Vehicle assigned(
      net.minecraft.server.level.ServerLevel level, ServerPlayer player) {
    return FoundationData.get(level).vehicles.values().stream()
        .filter(candidate -> candidate.carriers.stream().anyMatch(c -> c.riderSeats.containsKey(player.getUUID())))
        .findFirst()
        .orElse(null);
  }

  private static int failure(net.minecraft.commands.CommandSourceStack source, String message) {
    source.sendFailure(Component.literal(message));
    return 0;
  }
}
