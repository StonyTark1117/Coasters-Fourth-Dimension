package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opt-in completion gate for a fixture-created occupied client ride after a resource reload.
 *
 * <p>The fixture still uses the normal mounted-player and dispatch paths, but it is intentionally
 * not evidence of a player placing or operating an Engineered controller. It exists so a bounded
 * client run can prove that a mounted player remains in an active ride after the client resource
 * manager has completed its requested reload.
 */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class OccupiedRideReloadValidation {
  private static final double MINIMUM_RIDE_DISPLACEMENT = 1;
  private static final int RELOAD_TIMEOUT_TICKS = 2_400;
  private static int initialGeneration = -1;
  private static int initialCompletedReloads = -1;
  private static boolean reloadRequested;
  private static boolean reloadObserved;
  private static UUID armedPlayer;
  private static UUID armedMount;
  private static UUID armedChassis;
  private static UUID armedCarrier;
  private static Vec3 preReloadStart;
  private static double preReloadDisplacement;
  private static int preReloadTicks;
  private static long lastOccupiedGeometryStage = -1;
  private static long occupiedGeometryStages;
  private static int occupiedTicks;
  private static int waitedTicks;
  private static double maximumRideDisplacement;
  private static Vec3 rideStart;
  private static UUID riddenChassis;
  private static UUID riddenCarrier;
  private static boolean completed;
  private static Level lastLevel;

  private OccupiedRideReloadValidation() {}

  /** Prevents the font-only helper from closing the client before this stronger gate completes. */
  static boolean ownsExit() {
    return Boolean.getBoolean("fourthdimension.occupiedReloadValidation") && !completed;
  }

  /** The font helper may request its reload only during an already moving, identified FD ride. */
  static boolean readyForReload() {
    if (!ownsExit()) return true;
    if (reloadRequested) return true;
    var minecraft = Minecraft.getInstance();
    if (minecraft.level == null || minecraft.player == null) return false;
    var ride = currentRide(minecraft);
    if (ride == null) {
      resetPreReloadRide();
      return false;
    }
    if (!sameArmedRide(ride)) {
      resetPreReloadRide();
      armedPlayer = ride.player();
      armedMount = ride.mount();
      armedChassis = ride.chassis();
      armedCarrier = ride.carrier();
      preReloadStart = ride.chassisPosition();
    }
    preReloadTicks++;
    preReloadDisplacement =
        Math.max(preReloadDisplacement, ride.chassisPosition().distanceTo(preReloadStart));
    return preReloadTicks >= 20 && preReloadDisplacement >= MINIMUM_RIDE_DISPLACEMENT;
  }

  /** Locks the pre-reload identity before Minecraft starts changing resources. */
  static void reloadRequested() {
    if (!ownsExit() || reloadRequested) return;
    if (preReloadTicks < 20
        || preReloadDisplacement < MINIMUM_RIDE_DISPLACEMENT
        || !sameArmedRide(currentRide(Minecraft.getInstance())))
      throw new IllegalStateException("FD_OCCUPIED_RELOAD requested without an active FD ride");
    var minecraft = Minecraft.getInstance();
    lastLevel = minecraft.level;
    initialGeneration = FontReloadTrace.generation();
    initialCompletedReloads = FontReloadValidation.completedReloads();
    reloadRequested = true;
    LogUtils.getLogger()
        .info(
            "FD_OCCUPIED_RELOAD armed player={} mount={} chassis={} carrier={} "
                + "preReloadTicks={} preReloadDisplacement={} generation={} completedReloads={}",
            armedPlayer,
            armedMount,
            armedChassis,
            armedCarrier,
            preReloadTicks,
            preReloadDisplacement,
            initialGeneration,
            initialCompletedReloads);
  }

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!ownsExit()) return;
    var minecraft = Minecraft.getInstance();
    if (minecraft.level != lastLevel) {
      if (reloadRequested)
        throw new IllegalStateException("FD_OCCUPIED_RELOAD level changed during reload");
      reset(minecraft.level);
    }
    if (reloadRequested) {
      var ride = currentRide(minecraft);
      if (ride == null || !sameArmedRide(ride))
        throw new IllegalStateException("FD_OCCUPIED_RELOAD original player or FD mount lost during reload");
    }
    if (minecraft.level == null || minecraft.player == null || minecraft.getOverlay() != null) return;
    if (++waitedTicks > RELOAD_TIMEOUT_TICKS)
      throw new IllegalStateException(
          "FD_OCCUPIED_RELOAD timed out waiting for a successful reload and active FD carrier ride");

    if (!reloadRequested) return;
    int generation = FontReloadTrace.generation();
    if (!reloadObserved) {
      if (generation <= initialGeneration
          || FontReloadValidation.completedReloads() <= initialCompletedReloads) return;
      reloadObserved = true;
      LogUtils.getLogger()
          .info(
              "FD_OCCUPIED_RELOAD observedReload generation={} completedReloads={}",
              generation,
              FontReloadValidation.completedReloads());
    }
    var ride = currentRide(minecraft);
    // The original mount was checked above, including while a reload overlay was displayed.
    if (!ride.chassis().equals(riddenChassis) || !ride.carrier().equals(riddenCarrier)) {
      resetOccupiedRide();
      riddenChassis = ride.chassis();
      riddenCarrier = ride.carrier();
    }

    var render = TrackRenderMetrics.snapshot();
    if (rideStart == null) {
      rideStart = ride.chassisPosition();
      lastOccupiedGeometryStage = render.geometryStages();
      occupiedTicks = 1;
      return;
    }
    occupiedTicks++;
    if (lastOccupiedGeometryStage >= 0)
      occupiedGeometryStages += Math.max(0, render.geometryStages() - lastOccupiedGeometryStage);
    lastOccupiedGeometryStage = render.geometryStages();
    maximumRideDisplacement =
        Math.max(maximumRideDisplacement, ride.chassisPosition().distanceTo(rideStart));
    if (occupiedTicks < 20
        || occupiedGeometryStages < requiredStages()
        || maximumRideDisplacement < MINIMUM_RIDE_DISPLACEMENT) return;

    completed = true;
    LogUtils.getLogger()
        .info(
            "FD_OCCUPIED_RELOAD passed generation={} occupiedTicks={} occupiedGeometryStages={} "
                + "rideDisplacement={} "
                + "meshes={} rails={} quads={} decorationPieces={}",
            generation,
            occupiedTicks,
            occupiedGeometryStages,
            maximumRideDisplacement,
            render.meshBuilds(),
            render.inRangeRails(),
            render.inRangeQuads(),
            render.inRangeDecorationPieces());
    if (Boolean.getBoolean("fourthdimension.occupiedReloadValidationExit")) minecraft.stop();
  }

  static int requiredStages() {
    try {
      return Math.clamp(
          Integer.parseInt(System.getProperty("fourthdimension.occupiedReloadValidationStages", "100")),
          1,
          10_000);
    } catch (NumberFormatException ignored) {
      return 100;
    }
  }

  private static Ride currentRide(Minecraft minecraft) {
    var player = minecraft.player;
    if (minecraft.level == null || player == null || !player.isPassenger() || player.getVehicle() == null)
      return null;
    var binding =
        dev.fourthdimension.vehicle.Boarding.at(
            minecraft.level, player.getVehicle().blockPosition());
    if (binding == null || !binding.carrier().riderSeats.containsKey(player.getUUID())) return null;
    var container = SubLevelContainer.getContainer(minecraft.level);
    var chassis = container == null ? null : container.getSubLevel(binding.vehicle().chassis);
    if (chassis == null) return null;
    var center = chassis.getPlot().getCenterBlock();
    var position =
        chassis
            .logicalPose()
            .transformPosition(
                new org.joml.Vector3d(center.getX() + .5, center.getY() + .5, center.getZ() + .5));
    return new Ride(
        player.getUUID(),
        player.getVehicle().getUUID(),
        binding.vehicle().chassis,
        binding.carrier().body,
        new Vec3(position.x, position.y, position.z));
  }

  private static boolean sameArmedRide(Ride ride) {
    return ride != null
        && ride.player().equals(armedPlayer)
        && ride.mount().equals(armedMount)
        && ride.chassis().equals(armedChassis)
        && ride.carrier().equals(armedCarrier);
  }

  private static void resetPreReloadRide() {
    armedPlayer = null;
    armedMount = null;
    armedChassis = null;
    armedCarrier = null;
    preReloadStart = null;
    preReloadDisplacement = 0;
    preReloadTicks = 0;
  }

  private static void resetOccupiedRide() {
    lastOccupiedGeometryStage = -1;
    occupiedGeometryStages = 0;
    occupiedTicks = 0;
    maximumRideDisplacement = 0;
    rideStart = null;
    riddenChassis = null;
    riddenCarrier = null;
  }

  private static void reset(Level level) {
    initialGeneration = -1;
    initialCompletedReloads = -1;
    reloadRequested = false;
    reloadObserved = false;
    resetPreReloadRide();
    resetOccupiedRide();
    waitedTicks = 0;
    completed = false;
    lastLevel = level;
  }

  private record Ride(UUID player, UUID mount, UUID chassis, UUID carrier, Vec3 chassisPosition) {}
}
