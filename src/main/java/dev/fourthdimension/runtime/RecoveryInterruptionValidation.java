package dev.fourthdimension.runtime;

import dev.fourthdimension.mixin.RecoveryLinkAccess;
import dev.fourthdimension.vehicle.RecoveryOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3d;

/** Validation-only permanent interruption controls for the retained recovery fixture. */
@EventBusSubscriber(modid = "fourthdimension")
public final class RecoveryInterruptionValidation {
  private RecoveryInterruptionValidation() {}

  @SubscribeEvent
  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var fixture = Commands.literal("recovery_interruption");
    for (var action : List.of("status", "hold", "remove_coupling", "remove_bearing")) {
      fixture.then(
          Commands.literal(action)
              .then(
                  Commands.argument("player", EntityArgument.player())
                      .executes(
                          context ->
                              execute(
                                  context.getSource(),
                                  EntityArgument.getPlayer(context, "player"),
                                  action))));
    }
    event.getDispatcher().register(
        Commands.literal("fd")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("validation").then(fixture)));
  }

  private static int execute(CommandSourceStack source, ServerPlayer player, String action) {
    try {
      var level = player.serverLevel();
      var data = FoundationData.get(level);
      var group =
          data.vehicles.values().stream()
              .filter(vehicle -> vehicle.recoveryCart != null)
              .toList();
      if (group.isEmpty())
        throw new IllegalArgumentException("No occupied recovery group is attached");
      var lead = group.getFirst();
      var container = SubLevelContainer.getContainer(level);
      var chassis = requireBody(container, lead.chassis);
      var bearingId = lead.recoveryCart;
      var bearing = container.getSubLevel(bearingId);
      var ids = new ArrayList<UUID>();
      group.forEach(
          vehicle -> {
            ids.add(vehicle.chassis);
            vehicle.carriers.forEach(carrier -> ids.add(carrier.body));
          });
      ids.add(bearingId);

      if (action.equals("hold")) {
        if (!lead.stopping) throw new IllegalArgumentException("Brake recovery before holding it");
        var center = CoasterCartResolve.cartCenterWorld(chassis);
        if (center == null) throw new IllegalArgumentException("Recovery chassis is unavailable");
        var held = new ChunkPos((int) Math.floor(center.x) >> 4, (int) Math.floor(center.z) >> 4);
        if (bearing instanceof ServerSubLevel serverBearing)
          container.getHoldingChunkMap().moveToUnloaded(serverBearing, held);
        for (var id : ids) {
          var body = container.getSubLevel(id);
          if (body instanceof ServerSubLevel serverBody)
            container.getHoldingChunkMap().moveToUnloaded(serverBody, held);
        }
        log(
            "held native recovery dependency group",
            group,
            bearingId,
            container,
            level);
      }
      if (action.equals("remove_coupling")) {
        if (!lead.stopping || bearing == null)
          throw new IllegalArgumentException("Attach and brake recovery before removing its coupling");
        RecoveryLinkAccess.fourthdimension$unlink(level, lead.chassis, bearingId);
        log("permanently removed recovery coupling", group, bearingId, container, level);
      }
      if (action.equals("remove_bearing")) {
        if (!lead.stopping || bearing == null)
          throw new IllegalArgumentException("Attach and brake recovery before removing its bearing");
        container.removeSubLevel(
            bearing, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
        log("permanently removed recovery bearing", group, bearingId, container, level);
      }
      var report = report(group, bearingId, container, level, player);
      com.mojang.logging.LogUtils.getLogger().info(report);
      source.sendSuccess(() -> Component.literal(report), false);
      return 1;
    } catch (IllegalArgumentException failure) {
      source.sendFailure(Component.literal(failure.getMessage()));
      return 0;
    }
  }

  private static ServerSubLevel requireBody(SubLevelContainer container, UUID id) {
    var body = container.getSubLevel(id);
    if (!(body instanceof ServerSubLevel server) || body.isRemoved())
      throw new IllegalArgumentException("Recovery chassis is unavailable");
    return server;
  }

  private static void log(
      String action,
      List<FoundationData.Vehicle> group,
      UUID bearingId,
      SubLevelContainer container,
      ServerLevel level) {
    var lead = group.getFirst();
    com.mojang.logging.LogUtils.getLogger()
        .info(
            "FD402_INTERRUPTION action={} chassis={} cars={} bearing={} riders={} recoveryCartRemoved={} nativeLinks={}",
            action,
            lead.chassis,
            group.size(),
            bearingId,
            riderCount(group),
            group.stream().anyMatch(vehicle -> vehicle.recoveryCartRemoved),
            linkedIds(container, level, lead.chassis));
  }

  private static String report(
      List<FoundationData.Vehicle> group,
      UUID bearingId,
      SubLevelContainer container,
      ServerLevel level,
      ServerPlayer player) {
    var lead = group.getFirst();
    return "FD402_INTERRUPTION tick="
        + level.getGameTime()
        + " chassis="
        + lead.chassis
        + " cars="
        + group.size()
        + " bearing="
        + bearingId
        + " riders="
        + riderCount(group)
        + " mounted="
        + player.isPassenger()
        + " stopping="
        + lead.stopping
        + " maintenance="
        + lead.maintenance
        + " recoveryCartRemoved="
        + group.stream().anyMatch(vehicle -> vehicle.recoveryCartRemoved)
        + " faults="
        + group.stream().map(vehicle -> vehicle.fault).filter(fault -> !fault.isEmpty()).toList()
        + " nativeLinks="
        + linkedIds(container, level, lead.chassis);
  }

  private static List<UUID> linkedIds(SubLevelContainer container, ServerLevel level, UUID chassis) {
    var body = container.getSubLevel(chassis);
    return body == null
        ? List.of()
        : CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body).stream().toList();
  }

  private static int riderCount(List<FoundationData.Vehicle> group) {
    return group.stream()
        .flatMap(vehicle -> vehicle.carriers.stream())
        .mapToInt(carrier -> carrier.riderSeats.size())
        .sum();
  }
}
