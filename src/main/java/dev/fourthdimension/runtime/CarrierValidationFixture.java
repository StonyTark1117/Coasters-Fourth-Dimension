package dev.fourthdimension.runtime;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.shuttle.ShuttleExamples;
import dev.fourthdimension.track.ExampleLayouts;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.Boarding;
import dev.fourthdimension.vehicle.CarrierSeatBlock;
import dev.fourthdimension.vehicle.RecoveryExamples;
import dev.fourthdimension.vehicle.RecoveryOperations;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.joml.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Creates an opt-in empty-world scene for packaged client transform validation. */
public final class CarrierValidationFixture {
  private static final List<BlockPos> CIRCUIT_ORIGINS =
      List.of(
          new BlockPos(-150, 180, 0),
          new BlockPos(-75, 180, 0),
          new BlockPos(0, 180, 0),
          new BlockPos(75, 180, 0));
  private static final List<TrackDesign> CIRCUIT_DESIGNS =
      List.of(
          TrackDesign.ARROW,
          TrackDesign.LEGACY,
          TrackDesign.FREE_SPIN,
          TrackDesign.REVISED);
  private static final BlockPos PAX_ORIGIN = new BlockPos(145, 180, 0);
  private static final BlockPos RECOVERY_ORIGIN = new BlockPos(0, 180, 0);
  private static final BlockPos SPAWN = new BlockPos(0, 180, -30);
  private static List<UUID> motionChassis = List.of();
  private static int controlledCircuitCount;
  private static ShuttleExamples.Example motionPax;
  private static boolean motionPaxAssembled;
  private static int motionDelay = -1;
  private static int motionReadyDeadline;
  private static boolean motionStarted;
  private static final List<UUID> riderFixturePlayers = new ArrayList<>();
  private static int riderFixtureDeadline;
  private static boolean riderFixtureBoarded;
  private static int supportLossDelay = -1;
  private static int supportLossDeadline;
  private static int supportLossObservationDelay;
  private static boolean supportLossTriggered;
  private static boolean supportLossReported;
  private static Vector3d supportLossMotionStart;
  private static int supportRepairDelay = -1;
  private static int supportRepairDeadline;
  private static boolean supportRepairRebuilt;
  private static boolean supportRepairReported;
  private static boolean supportRealignmentTriggered;
  private static boolean supportRealignmentReported;
  private static int supportRealignmentDeadline;
  private static BezierConnection supportLossCurve;
  private static RecoveryExamples.Example recoveryExample;
  private static int recoverySpawnDelay = -1;
  private static int recoveryPreparationDelay = -1;
  private static boolean recoveryAttached;
  private static int recoveryTowDelay = -1;
  private static int recoveryTowDeadline;
  private static boolean recoveryTowStarted;

  private CarrierValidationFixture() {}

  public static void create(ServerLevel level) {
    if (!Boolean.getBoolean("fourthdimension.carrierFixture")) return;
    var data = FoundationData.get(level);
    if (!data.vehicles.isEmpty() || !data.rails.isEmpty())
      throw new IllegalStateException(
          "FD_CARRIER_FIXTURE requires a fresh empty world; remove the previous validation save");
    preload(level, new BlockPos(-160, 160, -64), new BlockPos(160, 220, 96));
    // A new normal world can generate terrain through this reserved test corridor. The fixture is
    // explicitly opt-in and already refuses any world with addon state, so clear only the authored
    // example volumes before asking their builders for an empty area.
    boolean recoveryFixture = recoveryFixture();
    var circuitDesigns = recoveryFixture ? List.<TrackDesign>of() : riderFixtureOnly() ? selectedCircuitDesigns() : CIRCUIT_DESIGNS;
    if (recoveryFixture) clearRecoveryArea(level);
    else {
      for (int i = 0; i < circuitDesigns.size(); i++) clearCircuitArea(level, CIRCUIT_ORIGINS.get(i));
      if ((riderFixtureOnly() && riderFixtureFamily() == Family.PAX) || !riderFixtureOnly())
        clearPaxArea(level);
    }
    var spawn = fixtureSpawn();
    level.setBlockAndUpdate(spawn.below(), Blocks.SMOOTH_STONE.defaultBlockState());
    level.setDefaultSpawnPos(spawn, 0);
    var controlledMotionChassis = new ArrayList<UUID>();
    motionDelay = -1;
    motionReadyDeadline = 0;
    motionStarted = false;
    motionPax = null;
    motionPaxAssembled = false;
    riderFixturePlayers.clear();
    riderFixtureDeadline = 0;
    riderFixtureBoarded = false;
    supportLossDelay = -1;
    supportLossDeadline = 0;
    supportLossObservationDelay = 0;
    supportLossTriggered = false;
    supportLossReported = false;
    supportLossMotionStart = null;
    supportRepairDelay = -1;
    supportRepairDeadline = 0;
    supportRepairRebuilt = false;
    supportRepairReported = false;
    supportRealignmentTriggered = false;
    supportRealignmentReported = false;
    supportRealignmentDeadline = 0;
    supportLossCurve = null;
    recoveryExample = null;
    recoverySpawnDelay = -1;
    recoveryPreparationDelay = -1;
    recoveryAttached = false;
    recoveryTowDelay = -1;
    recoveryTowDeadline = 0;
    recoveryTowStarted = false;
    if (recoveryFixture) {
      // RecoveryExamples allocates a native sublevel bearing. Defer it to an active server tick;
      // level-load callbacks run before that bearing's plot can participate in the cart scan.
    } else {
      for (int i = 0; i < circuitDesigns.size(); i++) {
        var vehicle = ExampleLayouts.create(level, CIRCUIT_ORIGINS.get(i), circuitDesigns.get(i));
        controlledMotionChassis.add(vehicle.chassis);
      }
      if (riderFixtureOnly() && riderFixtureFamily() == Family.PAX) {
        motionPax = ShuttleExamples.create(level, PAX_ORIGIN, false);
      } else if (!riderFixtureOnly()) {
        motionPax = ShuttleExamples.create(level, PAX_ORIGIN, false);
      }
    }
    motionChassis = List.copyOf(controlledMotionChassis);
    controlledCircuitCount = circuitDesigns.size();
    int carriers = data.vehicles.values().stream().mapToInt(v -> v.carriers.size()).sum();
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE created families={} vehicles={} carriers={} rails={} riderFamily={} riderCount={}",
            data.vehicles.values().stream().map(vehicle -> vehicle.family).distinct().count(),
            data.vehicles.size(),
            carriers,
            data.rails.size(),
            riderFixtureRequired() ? riderFixtureFamily() : "none",
            riderFixtureRequired() ? riderFixtureCount() : 0);
  }

  public static void placePlayer(
      net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
    if (!Boolean.getBoolean("fourthdimension.carrierFixture")
        || !(event.getEntity() instanceof ServerPlayer player)
        || player.serverLevel() != player.server.overworld()) return;
    var spawn = fixtureSpawn();
    player.teleportTo(spawn.getX() + .5, spawn.getY(), spawn.getZ() + .5);
    if (riderFixtureRequired()) {
      if (!riderFixturePlayers.contains(player.getUUID())
          && riderFixturePlayers.size() < riderFixtureCount()) {
        riderFixturePlayers.add(player.getUUID());
        riderFixtureDeadline = 2_000;
        if (recoveryFixture()) recoverySpawnDelay = 20;
        LogUtils.getLogger()
            .info(
                "FD_CARRIER_FIXTURE admitted rider={} slot={}/{}",
                player.getGameProfile().getName(),
                riderFixturePlayers.size(),
                riderFixtureCount());
      }
    } else if (Boolean.getBoolean("fourthdimension.carrierFixtureMotion") && !motionStarted) {
      // Give the joining client time to receive and statically validate every body before dispatch.
      motionDelay = 500;
      motionReadyDeadline = 2_000;
    }
  }

  /** Dispatches the configured circuit cars and optional PAX shuttle for the opt-in motion checkpoint. */
  public static void tick(ServerTickEvent.Post event) {
    var level = event.getServer().overworld();
    if (recoveryFixture() && recoveryExample == null) {
      // The native uncoupled bearing is reclaimed during startup. Wait for the real local player
      // and a live container before allocation, then allow native axle alignment to settle.
      if (riderFixturePlayers.isEmpty()) return;
      if (recoverySpawnDelay-- > 0) return;
      recoveryExample = RecoveryExamples.create(level, RECOVERY_ORIGIN, TrackDesign.ARROW);
      motionChassis = List.of(recoveryExample.car().chassis);
      recoveryPreparationDelay = 100;
      logRecoveryBearingState(level, "created");
      LogUtils.getLogger().info("FD_CARRIER_FIXTURE created active-tick recovery example");
    }
    if (recoveryFixture() && recoveryPreparationDelay-- > 0) return;
    if (riderFixtureRequired() && !riderFixtureBoarded) {
      if (riderFixturePlayers.size() < riderFixtureCount()) {
        if (riderFixtureDeadline > 0 && riderFixtureDeadline-- <= 0)
          throw new IllegalStateException(
              "FD_CARRIER_FIXTURE did not receive " + riderFixtureCount() + " rider clients");
        return;
      }
      if (!boardFixtureRiders(event.getServer(), level)) {
        if (riderFixtureDeadline-- <= 0)
          throw new IllegalStateException(
              "FD_CARRIER_FIXTURE riders never reached "
                  + riderFixtureFamily()
                  + " carrier seats: "
                  + riderBoardingState(level));
        return;
      }
      riderFixtureBoarded = true;
      if (recoveryFixture()) {
        logRecoveryBearingState(level, "boarded");
        // The fixture performs the guarded production coupling after native axle alignment.
        recoveryTowDeadline = 2_000;
      } else if (supportLossRequired()) {
        // Let the client receive the real occupied carrier before withdrawing support.
        supportLossDelay = supportLossMotionRequired() ? -1 : 100;
        supportLossDeadline = 1_000;
        if (supportLossMotionRequired() && !motionStarted) {
          motionDelay = 500;
          motionReadyDeadline = 2_000;
        }
      } else if (Boolean.getBoolean("fourthdimension.carrierFixtureMotion") && !motionStarted) {
        motionDelay = 500;
        motionReadyDeadline = 2_000;
      }
    }
    if (recoveryFixture() && riderFixtureBoarded && !recoveryAttached) {
      if (recoveryExample == null || recoveryExample.bearing().isRemoved()) {
        if (recoveryTowDeadline-- <= 0) {
          var bearing = recoveryExample == null ? null : recoveryExample.bearing();
          var container = SubLevelContainer.getContainer(level);
          var centerPlotState =
              bearing == null
                  ? null
                  : bearing
                      .getPlot()
                      .getEmbeddedLevelAccessor()
                      .getBlockState(BlockPos.ZERO);
          LogUtils.getLogger()
              .error(
                  "FD_CARRIER_FIXTURE recovery bearing pose timeout id={} removed={} contained={} level={} centerPlotState={}",
                  bearing == null ? null : bearing.getUniqueId(),
                  bearing != null && bearing.isRemoved(),
                  bearing != null && container != null && container.getSubLevel(bearing.getUniqueId()) == bearing,
                  bearing == null ? null : bearing.getLevel(),
                  centerPlotState);
          throw new IllegalStateException(
              "FD_CARRIER_FIXTURE ordinary recovery bearing was removed before coupling");
        }
        return;
      }
      attachRecoveryFixture(event.getServer(), level);
      recoveryAttached = true;
      // Wait for the client to receive the occupied, linked group and collect its static render
      // samples before applying native chain power.
      recoveryTowDelay = 200;
      recoveryTowDeadline = 1_000;
    }
    if (recoveryFixture() && recoveryAttached && !recoveryTowStarted)
      progressRecoveryTow(event.getServer(), level);
    if (supportLossRequired() && riderFixtureBoarded && !supportLossTriggered) {
      if (supportLossReadyToTrigger(level)) {
        triggerSupportLoss(level);
        supportLossTriggered = true;
      }
    }
    if (supportLossRequired() && supportLossTriggered) {
      boolean repaired = supportRepairRequired() && supportRepairSettled(level);
      boolean realigned = supportRealignmentRequired() && supportRealignmentSettled(level);
      if (!repaired && !supportLossSettled(event.getServer(), level)) {
        if (supportLossDeadline-- <= 0)
          throw new IllegalStateException("FD_CARRIER_FIXTURE support loss did not fail closed");
        return;
      }
      if (supportRepairRequired() && !repaired) {
        // Keep the faulted occupied state visible long enough for the packaged client to
        // receive it before restoring the exact curve. This is still the normal server
        // failure state; the delay only makes the client acceptance probe deterministic.
        if (supportLossObservationDelay-- > 0) return;
        progressSupportRepair(level);
        return;
      }
      if (supportRealignmentRequired() && !realigned) {
        progressSupportRealignment(level);
        return;
      }
    }
    if (!(Boolean.getBoolean("fourthdimension.carrierFixtureMotion") || supportLossMotionRequired())
        || motionStarted
        || motionDelay < 0)
      return;
    if (motionDelay-- > 0) return;
    if (motionPax == null && !riderFixtureOnly())
      throw new IllegalStateException("FD_CARRIER_FIXTURE missing PAX motion example");
    if (motionPax != null && !motionPaxAssembled) {
      try {
        ShuttleExamples.assemble(level, motionPax);
        motionPaxAssembled = true;
      } catch (IllegalArgumentException failure) {
        if (motionReadyDeadline-- <= 0)
          throw new IllegalStateException("FD_CARRIER_FIXTURE PAX train never assembled", failure);
        return;
      }
    }
    var data = FoundationData.get(level);
    var circuitVehicles = motionChassis.stream().map(data.vehicles::get).toList();
    var paxCars = motionPax == null ? List.<FoundationData.Vehicle>of() : motionPax.cars();
    var vehicles = new ArrayList<FoundationData.Vehicle>(circuitVehicles.size() + paxCars.size());
    vehicles.addAll(circuitVehicles);
    vehicles.addAll(paxCars);
    if (circuitVehicles.size() != controlledCircuitCount
        || vehicles.stream().anyMatch(java.util.Objects::isNull))
      throw new IllegalStateException("FD_CARRIER_FIXTURE missing controlled motion vehicle");
    if (vehicles.stream().anyMatch(vehicle -> !dev.fourthdimension.vehicle.Boarding.ready(level, vehicle))) {
      if (motionReadyDeadline-- <= 0)
        throw new IllegalStateException("FD_CARRIER_FIXTURE controlled motion vehicles never became ready");
      return;
    }
    for (var vehicle : circuitVehicles) {
      dev.fourthdimension.vehicle.VehicleOperations.restraints(level, vehicle, true);
      dev.fourthdimension.vehicle.VehicleOperations.dispatch(level, vehicle);
    }
    if (!paxCars.isEmpty()) {
      var paxLead = paxCars.getFirst();
      dev.fourthdimension.vehicle.VehicleOperations.restraints(level, paxLead, true);
      dev.fourthdimension.vehicle.VehicleOperations.dispatch(level, paxLead);
    }
    motionStarted = true;
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE dispatched controlled motion chassis={} families={}",
            vehicles.stream().map(vehicle -> vehicle.chassis).toList(),
            vehicles.stream().map(vehicle -> vehicle.family).distinct().toList());
  }

  /** Boards every required packaged-client rider into distinct seats of the same native carrier. */
  private static boolean boardFixtureRiders(net.minecraft.server.MinecraftServer server, ServerLevel level) {
    var players = riderFixturePlayers.stream().map(server.getPlayerList()::getPlayer).toList();
    if (players.size() != riderFixtureCount() || players.stream().anyMatch(java.util.Objects::isNull))
      return false;
    var vehicle = supportLossVehicle(level);
    var container = SubLevelContainer.getContainer(level);
    if (vehicle == null || vehicle.restraints || container == null || !Boarding.ready(level, vehicle)) return false;
    var carrier = vehicle.carriers.getFirst();
    var body = container.getSubLevel(carrier.body);
    if (body == null) return false;
    for (var player : players) {
      if (player.isPassenger() && carrier.riderSeats.containsKey(player.getUUID())) continue;
      var offset =
          carrier.seats.stream()
              .filter(
                  candidate -> {
                    var seat = body.getPlot().getCenterBlock().offset(candidate);
                    return level.getBlockState(seat).getBlock() instanceof CarrierSeatBlock
                        && !SeatBlock.isSeatOccupied(level, seat)
                        && !carrier.riderSeats.containsValue(candidate);
                  })
              .findFirst()
              .orElse(null);
      if (offset == null) return false;
      var seat = body.getPlot().getCenterBlock().offset(offset);
      SeatBlock.sitDown(level, seat, player);
      if (!player.isPassenger() || !carrier.riderSeats.containsKey(player.getUUID())) return false;
      LogUtils.getLogger()
          .info(
              "FD_CARRIER_FIXTURE boarded rider={} family={} carrier={} seat={}",
              player.getGameProfile().getName(), vehicle.family, carrier.body, seat);
    }
    return players.stream()
        .allMatch(
            player -> player.isPassenger() && carrier.riderSeats.containsKey(player.getUUID()));
  }

  /** Uses the same guarded production coupling path after a real client player is seated. */
  private static void attachRecoveryFixture(
      net.minecraft.server.MinecraftServer server, ServerLevel level) {
    if (recoveryExample == null)
      throw new IllegalStateException("FD_CARRIER_FIXTURE missing recovery example");
    var car = recoveryExample.car();
    var player = server.getPlayerList().getPlayer(riderFixturePlayers.getFirst());
    if (player == null) throw new IllegalStateException("FD_CARRIER_FIXTURE missing recovery rider");
    try {
      VehicleOperations.restraints(level, car, true);
      VehicleOperations.retention(level, car, true);
      RecoveryOperations.select(player, recoveryExample.bearing());
      RecoveryOperations.attach(player, car);
    } catch (IllegalArgumentException failure) {
      throw new IllegalStateException("FD_CARRIER_FIXTURE could not attach occupied recovery", failure);
    }
    if (!recoveryExample.bearing().getUniqueId().equals(car.recoveryCart)
        || !RecoveryOperations.retained(car)
        || !car.stopping)
      throw new IllegalStateException("FD_CARRIER_FIXTURE did not form a retained recovery group");
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE attached occupied recovery chassis={} bearing={} riders={}",
            car.chassis,
            car.recoveryCart,
            car.carriers.stream().mapToInt(carrier -> carrier.riderSeats.size()).sum());
  }

  private static void logRecoveryBearingState(ServerLevel level, String stage) {
    var bearing = recoveryExample == null ? null : recoveryExample.bearing();
    var container = SubLevelContainer.getContainer(level);
    var centerPlotState =
        bearing == null
            ? null
            : bearing.getPlot().getEmbeddedLevelAccessor().getBlockState(BlockPos.ZERO);
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE recovery bearing {} id={} removed={} massInvalid={} contained={} level={} plotCenter={} cartCenter={} centerPlotState={}",
            stage,
            bearing == null ? null : bearing.getUniqueId(),
            bearing != null && bearing.isRemoved(),
            bearing != null && bearing.getMassTracker().isInvalid(),
            bearing != null && container != null && container.getSubLevel(bearing.getUniqueId()) == bearing,
            bearing == null ? null : bearing.getLevel(),
            bearing == null ? null : CoasterCartResolve.bearingPlotCenter(bearing),
            bearing == null ? null : CoasterCartResolve.cartCenterWorld(bearing),
            centerPlotState);
  }

  /** Starts the attached native chain only after the linked client-render checkpoint can settle. */
  private static void progressRecoveryTow(
      net.minecraft.server.MinecraftServer server, ServerLevel level) {
    if (recoveryTowDelay-- > 0) return;
    if (recoveryExample == null)
      throw new IllegalStateException("FD_CARRIER_FIXTURE missing recovery tow example");
    var car = recoveryExample.car();
    try {
      RecoveryOperations.start(level, car);
      var player = server.getPlayerList().getPlayer(riderFixturePlayers.getFirst());
      if (player == null) throw new IllegalStateException("FD_CARRIER_FIXTURE missing recovery rider");
      var lever = recoveryExample.powerLever();
      level
          .getBlockState(lever)
          .useWithoutItem(
              level,
              player,
              new net.minecraft.world.phys.BlockHitResult(
                  net.minecraft.world.phys.Vec3.atCenterOf(lever),
                  net.minecraft.core.Direction.UP,
                  lever,
                  false));
      var anchor =
          (CoasterAnchorpointBlockEntity) level.getBlockEntity(recoveryExample.car().boundFrom);
      recoveryExample.motor().generatedSpeed.setValue(8 * anchor.getChainLiftTravelSign());
      recoveryTowStarted = true;
      LogUtils.getLogger()
          .info(
              "FD_CARRIER_FIXTURE started occupied recovery chassis={} bearing={} nativeChain=true",
              car.chassis,
              car.recoveryCart);
    } catch (IllegalArgumentException failure) {
      if (recoveryTowDeadline-- <= 0)
        throw new IllegalStateException("FD_CARRIER_FIXTURE recovery tow did not become safe to start", failure);
      recoveryTowDelay = 20;
    }
  }

  /** Describes production boarding readiness when an opt-in rider fixture times out. */
  private static String riderBoardingState(ServerLevel level) {
    var vehicle = supportLossVehicle(level);
    var container = SubLevelContainer.getContainer(level);
    if (vehicle == null) return "missing selected vehicle";
    if (container == null) return "missing native container";
    var parent = container.getSubLevel(vehicle.chassis);
    if (parent == null) return "missing native chassis=" + vehicle.chassis;
    if (!(parent instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel nativeChassis))
      return "non-server native chassis=" + vehicle.chassis;
    var velocity =
        container
            .physicsSystem()
            .getPipeline()
            .getLinearVelocity(nativeChassis, new Vector3d())
            .length();
    var carriers =
        vehicle.carriers.stream()
            .map(
                carrier -> {
                  var body = container.getSubLevel(carrier.body);
                  int availableSeats =
                      body == null
                          ? 0
                          : (int)
                              carrier.seats.stream()
                                  .filter(
                                      offset -> {
                                        var seat = body.getPlot().getCenterBlock().offset(offset);
                                        return level.getBlockState(seat).getBlock()
                                                instanceof CarrierSeatBlock
                                            && !SeatBlock.isSeatOccupied(level, seat)
                                            && !carrier.riderSeats.containsValue(offset);
                                      })
                                  .count();
                  return "body="
                      + carrier.body
                      + ",capture="
                      + carrier.capture
                      + ",angle="
                      + carrier.angle
                      + ",omega="
                      + carrier.omega
                      + ",seats="
                      + carrier.seats.size()
                      + ",available="
                      + availableSeats;
                })
            .toList();
    return "stopping="
        + vehicle.stopping
        + ",restraints="
        + vehicle.restraints
        + ",velocity="
        + velocity
        + ",ready="
        + Boarding.ready(level, vehicle)
        + ",carriers="
        + carriers;
  }

  private static boolean riderFixtureRequired() {
    return Boolean.getBoolean("fourthdimension.carrierFixtureRider") || recoveryFixture();
  }

  private static boolean recoveryFixture() {
    return Boolean.getBoolean("fourthdimension.carrierFixtureRecovery");
  }

  /** Keeps the selected occupied fixture in the connecting players' view-distance ticket. */
  private static BlockPos fixtureSpawn() {
    if (recoveryFixture()) return RECOVERY_ORIGIN.offset(0, 0, -20);
    if (!riderFixtureOnly()) return SPAWN;
    if (riderFixtureFamily() == Family.PAX)
      // The PAX builder reserves a full x=-8..8 empty corridor; keep the spawn ticket near
      // the shuttle while leaving its authored construction volume untouched.
      return PAX_ORIGIN.offset(20, 0, -30);
    return CIRCUIT_ORIGINS.getFirst().offset(0, 0, -30);
  }

  private static int riderFixtureCount() {
    int count = Integer.getInteger("fourthdimension.carrierFixtureRiders", 1);
    if (count < 1 || count > 2)
      throw new IllegalStateException("FD_CARRIER_FIXTURE supports one or two rider clients");
    return count;
  }

  private static boolean supportLossRequired() {
    return riderFixtureRequired()
        && Boolean.getBoolean("fourthdimension.carrierFixtureSupportLoss");
  }

  /** Rebuilds only the exact interrupted section, then uses the production explicit repair action. */
  private static void progressSupportRepair(ServerLevel level) {
    var vehicle = supportLossVehicle(level);
    if (vehicle == null) throw new IllegalStateException("FD_CARRIER_FIXTURE missing support-repair vehicle");
    if (!supportRepairRebuilt) {
      if (supportLossCurve == null
          || !(level.getBlockEntity(vehicle.boundFrom) instanceof CoasterAnchorpointBlockEntity anchor))
        throw new IllegalStateException("FD_CARRIER_FIXTURE lost the exact original curve");
      // The generated circuit has authored tangents that ordinary straight placement cannot
      // reproduce. Reinsert its original Coasters curve, which still passes the production
      // occupied-support guard, before invoking the same explicit repair operation a player uses.
      anchor.putAnchorPeerCurve(level, vehicle.boundTo, supportLossCurve);
      TrackGeometry.invalidate(level);
      supportRepairRebuilt = true;
      // Coasters publishes its reinstated graph edge asynchronously. The native repair action must
      // validate the edge actually visible through the normal resolver, never a fixture shortcut.
      supportRepairDelay = 20;
      supportRepairDeadline = 400;
      LogUtils.getLogger()
          .info(
              "FD_CARRIER_FIXTURE rebuilt exact {} support chassis={} from={} to={}",
              vehicle.family,
              vehicle.chassis,
              vehicle.boundFrom,
              vehicle.boundTo);
      return;
    }
    if (supportRepairDelay-- > 0) return;
    try {
      VehicleOperations.repairMatchingSupport(level, vehicle);
    } catch (IllegalArgumentException failure) {
      if (supportRepairDeadline-- <= 0)
        throw new IllegalStateException("FD_CARRIER_FIXTURE support repair did not become valid", failure);
      return;
    }
    if (!supportRepairSettled(level))
      throw new IllegalStateException("FD_CARRIER_FIXTURE repair resumed or released the vehicle");
    supportRepairReported = true;
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE support repair settled chassis={} stopped={} restraints={} ridersRetained={}",
            vehicle.chassis,
            vehicle.stopping,
            vehicle.restraints,
            vehicle.carriers.stream().mapToInt(carrier -> carrier.riderSeats.size()).sum());
  }

  private static boolean supportRepairRequired() {
    return supportLossRequired()
        && Boolean.getBoolean("fourthdimension.carrierFixtureSupportRepair");
  }

  /** Re-engages the same repaired native chassis only after the explicit repair has completed. */
  private static void progressSupportRealignment(ServerLevel level) {
    var vehicle = supportLossVehicle(level);
    if (vehicle == null)
      throw new IllegalStateException("FD_CARRIER_FIXTURE missing support-realignment vehicle");
    if (!supportRealignmentTriggered) {
      if (supportRealignmentDeadline <= 0) supportRealignmentDeadline = 400;
      try {
        VehicleOperations.realignMatchingSupport(level, vehicle);
      } catch (IllegalArgumentException failure) {
        if (supportRealignmentDeadline-- <= 0)
          throw new IllegalStateException(
              "FD_CARRIER_FIXTURE support realignment did not become physically safe", failure);
        return;
      }
      supportRealignmentTriggered = true;
      supportRealignmentDeadline = 400;
      LogUtils.getLogger()
          .info(
              "FD_CARRIER_FIXTURE physically realigned {} support chassis={}",
              vehicle.family,
              vehicle.chassis);
      return;
    }
    if (supportRealignmentSettled(level)) return;
    if (supportRealignmentDeadline-- <= 0)
      throw new IllegalStateException(
          "FD_CARRIER_FIXTURE native carrier did not settle after physical realignment");
  }

  private static boolean supportRealignmentRequired() {
    return supportRepairRequired()
        && Boolean.getBoolean("fourthdimension.carrierFixtureSupportRealignment");
  }

  private static boolean supportRepairSettled(ServerLevel level) {
    var vehicle = supportLossVehicle(level);
    if (vehicle == null) return false;
    var rail =
        FoundationData.get(level).rails.stream()
            .filter(candidate -> candidate.matches(vehicle.boundFrom, vehicle.boundTo))
            .findFirst()
            .orElse(null);
    return supportRepairReported
        || (vehicle.stopping
            && vehicle.restraints
            && vehicle.fault.isEmpty()
            && vehicle.carriers.stream().allMatch(carrier -> carrier.capture)
            && rail != null
            && rail.valid);
  }

  private static boolean supportRealignmentSettled(ServerLevel level) {
    var vehicle = supportLossVehicle(level);
    if (vehicle == null) return false;
    var rail =
        FoundationData.get(level).rails.stream()
            .filter(candidate -> candidate.matches(vehicle.boundFrom, vehicle.boundTo))
            .findFirst()
            .orElse(null);
    boolean settled =
        vehicle.stopping
            && vehicle.restraints
            && vehicle.fault.isEmpty()
            && !vehicle.alignmentRequired
            && vehicle.carriers.stream().allMatch(carrier -> carrier.capture)
            && rail != null
            && rail.valid
            && Boarding.ready(level, vehicle);
    if (settled && !supportRealignmentReported) {
      supportRealignmentReported = true;
      LogUtils.getLogger()
          .info(
              "FD_CARRIER_FIXTURE support realignment settled chassis={} ridersRetained={}",
              vehicle.chassis,
              vehicle.carriers.stream().mapToInt(carrier -> carrier.riderSeats.size()).sum());
    }
    return settled;
  }

  /**
   * An active-route checkpoint must remove support only after the occupied chassis really moved.
   */
  private static boolean supportLossReadyToTrigger(ServerLevel level) {
    if (!supportLossMotionRequired()) return supportLossDelay-- <= 0;
    if (!motionStarted) return false;
    var vehicle = supportLossVehicle(level);
    var container = SubLevelContainer.getContainer(level);
    if (vehicle == null || container == null) return false;
    var parent = container.getSubLevel(vehicle.chassis);
    var center = parent == null ? null : CoasterCartResolve.cartCenterWorld(parent);
    if (center == null) return false;
    if (supportLossMotionStart == null) {
      supportLossMotionStart = center;
      return false;
    }
    return center.distanceSquared(supportLossMotionStart) >= 4;
  }

  private static boolean supportLossMotionRequired() {
    return supportLossRequired()
        && Boolean.getBoolean("fourthdimension.carrierFixtureSupportLossMotion");
  }

  /** Removes the actual Coasters graph connection; the runtime must fail closed without unseating. */
  private static void triggerSupportLoss(ServerLevel level) {
    var vehicle = supportLossVehicle(level);
    if (vehicle == null
        || !(level.getBlockEntity(vehicle.boundFrom) instanceof CoasterAnchorpointBlockEntity anchor))
      throw new IllegalStateException(
          "FD_CARRIER_FIXTURE missing " + riderFixtureFamily() + " support for support-loss checkpoint");
    var curve = anchor.getAnchorPeerCurvesView().get(vehicle.boundTo);
    if (curve == null)
      throw new IllegalStateException("FD_CARRIER_FIXTURE missing exact curve for support-loss checkpoint");
    supportLossCurve = curve.isPrimary() ? curve : curve.secondary();
    supportLossObservationDelay = 20;
    anchor.removeAnchorPeerCurve(level, vehicle.boundTo);
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE removed {} support chassis={} from={} to={}",
            vehicle.family,
            vehicle.chassis,
            vehicle.boundFrom,
            vehicle.boundTo);
  }

  /** Confirms the running server's fail-closed state before the client is allowed to pass. */
  private static boolean supportLossSettled(
      net.minecraft.server.MinecraftServer server, ServerLevel level) {
    var players = riderFixturePlayers.stream().map(server.getPlayerList()::getPlayer).toList();
    var vehicle = supportLossVehicle(level);
    if (players.size() != riderFixtureCount()
        || players.stream().anyMatch(java.util.Objects::isNull)
        || vehicle == null) return false;
    boolean riderRetained =
        players.stream()
            .allMatch(
                player ->
                    player.isPassenger()
                        && vehicle.carriers.stream()
                            .anyMatch(carrier -> carrier.riderSeats.containsKey(player.getUUID())));
    boolean settled =
        riderRetained
            && vehicle.stopping
            && vehicle.restraints
            && "Matching track support lost".equals(vehicle.fault);
    if (settled && !supportLossReported) {
      supportLossReported = true;
      LogUtils.getLogger()
          .info(
              "FD_CARRIER_FIXTURE support-loss settled chassis={} rider={} retained={} fault={}",
              vehicle.chassis,
              players.stream().map(player -> player.getGameProfile().getName()).toList(),
              riderRetained,
              vehicle.fault);
    }
    return settled;
  }

  /** Limits the opt-in occupied client fixture to the requested vehicle family. */
  private static boolean riderFixtureOnly() {
    return riderFixtureRequired()
        && Boolean.getBoolean("fourthdimension.carrierFixtureRiderOnly");
  }

  private static List<TrackDesign> selectedCircuitDesigns() {
    return switch (riderFixtureFamily()) {
      case ARROW -> List.of(TrackDesign.ARROW);
      case ZACSPIN -> List.of(TrackDesign.LEGACY);
      case FREE_SPIN -> List.of(TrackDesign.FREE_SPIN);
      case ZACSPIN_REVISED -> List.of(TrackDesign.REVISED);
      case PAX -> List.of();
    };
  }

  private static FoundationData.Vehicle supportLossVehicle(ServerLevel level) {
    return FoundationData.get(level).vehicles.values().stream()
        .filter(vehicle -> vehicle.family == riderFixtureFamily())
        .findFirst()
        .orElse(null);
  }

  private static Family riderFixtureFamily() {
    var requested = System.getProperty("fourthdimension.carrierFixtureRiderFamily", Family.ARROW.name());
    try {
      return Family.valueOf(requested.trim().toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException ignored) {
      return Family.byId(requested.trim());
    }
  }

  private static void preload(ServerLevel level, BlockPos min, BlockPos max) {
    for (int chunkX = min.getX() >> 4; chunkX <= max.getX() >> 4; chunkX++)
      for (int chunkZ = min.getZ() >> 4; chunkZ <= max.getZ() >> 4; chunkZ++)
        level.getChunk(chunkX, chunkZ);
  }

  private static void clearCircuitArea(ServerLevel level, BlockPos origin) {
    clear(level, origin.offset(-7, -3, -8), origin.offset(39, 13, 60));
  }

  private static void clearPaxArea(ServerLevel level) {
    // Keep this in lockstep with ShuttleExamples.create(..., false)'s occupancy contract.
    clear(level, PAX_ORIGIN.offset(-8, -5, -58), PAX_ORIGIN.offset(8, 42, 90));
  }

  private static void clearRecoveryArea(ServerLevel level) {
    clear(level, RECOVERY_ORIGIN.offset(-8, -1, -4), RECOVERY_ORIGIN.offset(8, 12, 52));
  }

  private static void clear(ServerLevel level, BlockPos min, BlockPos max) {
    for (var pos : BlockPos.betweenClosed(min, max))
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
  }
}
