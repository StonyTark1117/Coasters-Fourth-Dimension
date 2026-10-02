package dev.fourthdimension.runtime;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.AppearanceEditor;
import dev.fourthdimension.integration.TrackAppearance;
import dev.fourthdimension.track.TrackPlacement;
import dev.fourthdimension.vehicle.Boarding;
import dev.fourthdimension.vehicle.CarrierSeatBlock;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
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
                Commands.literal("dispatch")
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context ->
                                    dispatch(
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
    var vehicle =
        FoundationData.get(level).vehicles.values().stream()
            .filter(candidate -> candidate.carriers.stream().anyMatch(c -> c.riderSeats.containsKey(player.getUUID())))
            .findFirst()
            .orElse(null);
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

  private static int failure(net.minecraft.commands.CommandSourceStack source, String message) {
    source.sendFailure(Component.literal(message));
    return 0;
  }
}
