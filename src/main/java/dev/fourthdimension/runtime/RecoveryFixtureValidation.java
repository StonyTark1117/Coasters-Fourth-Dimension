package dev.fourthdimension.runtime;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.RecoveryOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartLinkPlacement;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartSpawner;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Vector3d;

/** Native bearing preparation for the retained straight Arrow recovery validation fixture. */
@EventBusSubscriber(modid = "fourthdimension")
public final class RecoveryFixtureValidation {
  private static final Map<UUID, UUID> PREPARED = new HashMap<>();

  private RecoveryFixtureValidation() {}

  @SubscribeEvent
  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    var fixture = Commands.literal("recovery_fixture");
    for (var action : List.of("prepare", "select", "status")) {
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
      if (data.vehicles.size() != 1)
        throw new IllegalArgumentException("Fixture requires exactly one saved Arrow car");
      var car = data.vehicles.values().iterator().next();
      if (car.family != Family.ARROW)
        throw new IllegalArgumentException("Fixture requires the retained Arrow car");
      var container = SubLevelContainer.getContainer(level);
      var rawChassis = container == null ? null : container.getSubLevel(car.chassis);
      if (!(rawChassis instanceof ServerSubLevel chassis))
        throw new IllegalArgumentException("Saved chassis is not loaded");
      var center = CoasterCartResolve.cartCenterWorld(chassis);
      if (center == null) throw new IllegalArgumentException("Saved chassis is not loaded");

      if (action.equals("prepare")) {
        if (car.recoveryCart != null)
          throw new IllegalArgumentException("Recovery is already attached; use status");
        var preparedId = PREPARED.get(car.chassis);
        var existing = preparedId == null ? null : container.getSubLevel(preparedId);
        if (existing == null) {
          var rail = data.rails.stream()
              .filter(candidate -> candidate.matches(car.boundFrom, car.boundTo))
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Retained track is unavailable"));
          if (rail.from.getX() != rail.to.getX() || rail.from.getY() != rail.to.getY())
            throw new IllegalArgumentException("Fixture requires its straight level Z track");
          var edge = TrackGeometry.resolve(level, rail);
          if (edge == null) throw new IllegalArgumentException("Native track edge is unavailable");
          var desired = new Vector3d(center)
              .add(0, 0, -TrackDesign.byVariant(car.variant).axleSpacing());
          var hit = CoasterPathTrackFrame.closestHitOnEdge(
              level, edge, new Vec3(desired.x, desired.y, desired.z));
          if (hit == null) throw new IllegalArgumentException("No native bearing placement point");
          var orientation = CoasterPathTrackFrame.quaternionFromTrackBasis(
              CoasterPathTrackFrame.trackBasisAtHit(level, hit));
          var bearing = CoasterCartSpawner.spawnMinimalContraption(
              level,
              CoasterCartLinkPlacement.plotOriginForBearingCenter(
                  CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), orientation),
              orientation,
              hit,
              true);
          if (bearing == null) throw new IllegalArgumentException("Native bearing allocation failed");
          PREPARED.put(car.chassis, bearing.getUniqueId());
        }
      }

      var bearingId = car.recoveryCart != null ? car.recoveryCart : PREPARED.get(car.chassis);
      var bearing = bearingId == null ? null : container.getSubLevel(bearingId);
      if (action.equals("select")) RecoveryOperations.select(player, bearing);
      var bearingCenter = bearing == null ? null : CoasterCartResolve.cartCenterWorld(bearing);
      var velocity = container.physicsSystem().getPipeline()
          .getLinearVelocity(chassis, new Vector3d());
      var riders = car.carriers.stream().mapToInt(carrier -> carrier.riderSeats.size()).sum();
      String report = "FD402_FIXTURE tick=" + level.getGameTime()
          + " chassis=" + car.chassis + " center=" + center + " velocity=" + velocity
          + " bearing=" + bearingId + " bearingCenter=" + bearingCenter
          + " spacing=" + (bearingCenter == null ? "unavailable" : center.distance(bearingCenter))
          + " attached=" + car.recoveryCart + " stopping=" + car.stopping
          + " maintenance=" + car.maintenance + " restraints=" + car.restraints
          + " riders=" + riders + " mounted=" + player.isPassenger()
          + " nativeLinks=" + CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(chassis);
      source.sendSuccess(() -> Component.literal(report), false);
      return 1;
    } catch (IllegalArgumentException failure) {
      source.sendFailure(Component.literal(failure.getMessage()));
      return 0;
    }
  }
}
