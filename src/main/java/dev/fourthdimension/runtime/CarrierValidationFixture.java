package dev.fourthdimension.runtime;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.shuttle.ShuttleExamples;
import dev.fourthdimension.track.ExampleLayouts;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.vehicle.Boarding;
import dev.fourthdimension.vehicle.CarrierSeatBlock;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
  private static final BlockPos SPAWN = new BlockPos(0, 180, -30);
  private static List<UUID> motionChassis = List.of();
  private static int controlledCircuitCount;
  private static ShuttleExamples.Example motionPax;
  private static boolean motionPaxAssembled;
  private static int motionDelay = -1;
  private static int motionReadyDeadline;
  private static boolean motionStarted;
  private static UUID riderFixturePlayer;
  private static int riderFixtureDeadline;
  private static boolean riderFixtureBoarded;
  private static int supportLossDelay = -1;
  private static int supportLossDeadline;
  private static boolean supportLossTriggered;
  private static boolean supportLossReported;

  private CarrierValidationFixture() {}

  public static void create(ServerLevel level) {
    if (!Boolean.getBoolean("fourthdimension.carrierFixture")) return;
    var data = FoundationData.get(level);
    if (!data.vehicles.isEmpty() || !data.rails.isEmpty())
      throw new IllegalStateException(
          "FD_CARRIER_FIXTURE requires a fresh empty world; remove the previous validation save");
    preload(level, new BlockPos(-160, 160, -64), new BlockPos(160, 220, 96));
    level.setBlockAndUpdate(SPAWN.below(), Blocks.SMOOTH_STONE.defaultBlockState());
    level.setDefaultSpawnPos(SPAWN, 0);
    var controlledMotionChassis = new ArrayList<UUID>();
    motionDelay = -1;
    motionReadyDeadline = 0;
    motionStarted = false;
    motionPax = null;
    motionPaxAssembled = false;
    riderFixturePlayer = null;
    riderFixtureDeadline = 0;
    riderFixtureBoarded = false;
    supportLossDelay = -1;
    supportLossDeadline = 0;
    supportLossTriggered = false;
    supportLossReported = false;
    var circuitDesigns = riderFixtureOnly() ? List.of(TrackDesign.ARROW) : CIRCUIT_DESIGNS;
    for (int i = 0; i < circuitDesigns.size(); i++) {
      var vehicle = ExampleLayouts.create(level, CIRCUIT_ORIGINS.get(i), circuitDesigns.get(i));
      controlledMotionChassis.add(vehicle.chassis);
    }
    motionChassis = List.copyOf(controlledMotionChassis);
    controlledCircuitCount = circuitDesigns.size();
    if (!riderFixtureOnly()) motionPax = ShuttleExamples.create(level, PAX_ORIGIN, false);
    int carriers = data.vehicles.values().stream().mapToInt(v -> v.carriers.size()).sum();
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE created families={} vehicles={} carriers={} rails={}",
            circuitDesigns.size() + (riderFixtureOnly() ? 0 : 1),
            data.vehicles.size(),
            carriers,
            data.rails.size());
  }

  public static void placePlayer(
      net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
    if (!Boolean.getBoolean("fourthdimension.carrierFixture")
        || !(event.getEntity() instanceof ServerPlayer player)
        || player.serverLevel() != player.server.overworld()) return;
    player.teleportTo(SPAWN.getX() + .5, SPAWN.getY(), SPAWN.getZ() + .5);
    if (riderFixtureRequired()) {
      riderFixturePlayer = player.getUUID();
      riderFixtureDeadline = 2_000;
    } else if (Boolean.getBoolean("fourthdimension.carrierFixtureMotion") && !motionStarted) {
      // Give the joining client time to receive and statically validate every body before dispatch.
      motionDelay = 500;
      motionReadyDeadline = 2_000;
    }
  }

  /** Dispatches the configured circuit cars and optional PAX shuttle for the opt-in motion checkpoint. */
  public static void tick(ServerTickEvent.Post event) {
    var level = event.getServer().overworld();
    if (riderFixtureRequired() && riderFixturePlayer != null && !riderFixtureBoarded) {
      if (!boardFixtureRider(event.getServer(), level)) {
        if (riderFixtureDeadline-- <= 0)
          throw new IllegalStateException("FD_CARRIER_FIXTURE rider never reached an Arrow carrier seat");
        return;
      }
      riderFixtureBoarded = true;
      if (supportLossRequired()) {
        // Let the client receive the real occupied carrier before withdrawing support.
        supportLossDelay = 100;
        supportLossDeadline = 1_000;
      } else if (Boolean.getBoolean("fourthdimension.carrierFixtureMotion") && !motionStarted) {
        motionDelay = 500;
        motionReadyDeadline = 2_000;
      }
    }
    if (supportLossRequired() && riderFixtureBoarded && !supportLossTriggered) {
      if (supportLossDelay-- > 0) return;
      triggerSupportLoss(level);
      supportLossTriggered = true;
    }
    if (supportLossRequired() && supportLossTriggered) {
      if (!supportLossSettled(event.getServer(), level)) {
        if (supportLossDeadline-- <= 0)
          throw new IllegalStateException("FD_CARRIER_FIXTURE support loss did not fail closed");
        return;
      }
    }
    if (!Boolean.getBoolean("fourthdimension.carrierFixtureMotion")
        || motionStarted
        || motionDelay < 0)
      return;
    if (motionDelay-- > 0) return;
    if (!riderFixtureOnly() && motionPax == null)
      throw new IllegalStateException("FD_CARRIER_FIXTURE missing PAX motion example");
    if (!riderFixtureOnly() && !motionPaxAssembled) {
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
    var paxCars = riderFixtureOnly() ? List.<FoundationData.Vehicle>of() : motionPax.cars();
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

  /** Boards the local packaged-client player only when explicit native carrier boarding is ready. */
  private static boolean boardFixtureRider(net.minecraft.server.MinecraftServer server, ServerLevel level) {
    var player = riderFixturePlayer == null ? null : server.getPlayerList().getPlayer(riderFixturePlayer);
    if (player == null) return false;
    var arrow = motionChassis.isEmpty() ? null : FoundationData.get(level).vehicles.get(motionChassis.getFirst());
    var container = SubLevelContainer.getContainer(level);
    if (arrow == null || arrow.restraints || container == null || !Boarding.ready(level, arrow)) return false;
    for (var carrier : arrow.carriers) {
      var body = container.getSubLevel(carrier.body);
      if (body == null) continue;
      for (var offset : carrier.seats) {
        var seat = body.getPlot().getCenterBlock().offset(offset);
        if (!(level.getBlockState(seat).getBlock() instanceof CarrierSeatBlock)
            || SeatBlock.isSeatOccupied(level, seat)
            || carrier.riderSeats.containsValue(offset)) continue;
        SeatBlock.sitDown(level, seat, player);
        if (!player.isPassenger() || !carrier.riderSeats.containsKey(player.getUUID())) return false;
        LogUtils.getLogger()
            .info(
                "FD_CARRIER_FIXTURE boarded rider={} family={} carrier={} seat={}",
                player.getGameProfile().getName(),
                arrow.family,
                carrier.body,
                seat);
        return true;
      }
    }
    return false;
  }

  private static boolean riderFixtureRequired() {
    return Boolean.getBoolean("fourthdimension.carrierFixtureRider");
  }

  private static boolean supportLossRequired() {
    return riderFixtureRequired()
        && Boolean.getBoolean("fourthdimension.carrierFixtureSupportLoss");
  }

  /** Removes the actual Coasters graph connection; the runtime must fail closed without unseating. */
  private static void triggerSupportLoss(ServerLevel level) {
    var arrow = motionChassis.isEmpty() ? null : FoundationData.get(level).vehicles.get(motionChassis.getFirst());
    if (arrow == null
        || !(level.getBlockEntity(arrow.boundFrom) instanceof CoasterAnchorpointBlockEntity anchor))
      throw new IllegalStateException("FD_CARRIER_FIXTURE missing Arrow support for support-loss checkpoint");
    anchor.removeAnchorPeerCurve(level, arrow.boundTo);
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE removed Arrow support chassis={} from={} to={}",
            arrow.chassis,
            arrow.boundFrom,
            arrow.boundTo);
  }

  /** Confirms the running server's fail-closed state before the client is allowed to pass. */
  private static boolean supportLossSettled(
      net.minecraft.server.MinecraftServer server, ServerLevel level) {
    var player = riderFixturePlayer == null ? null : server.getPlayerList().getPlayer(riderFixturePlayer);
    var arrow = motionChassis.isEmpty() ? null : FoundationData.get(level).vehicles.get(motionChassis.getFirst());
    if (player == null || arrow == null) return false;
    boolean riderRetained =
        player.isPassenger()
            && arrow.carriers.stream().anyMatch(c -> c.riderSeats.containsKey(player.getUUID()));
    boolean settled =
        riderRetained
            && arrow.stopping
            && arrow.restraints
            && "Matching track support lost".equals(arrow.fault);
    if (settled && !supportLossReported) {
      supportLossReported = true;
      LogUtils.getLogger()
          .info(
              "FD_CARRIER_FIXTURE support-loss settled chassis={} rider={} retained={} fault={}",
              arrow.chassis,
              player.getGameProfile().getName(),
              riderRetained,
              arrow.fault);
    }
    return settled;
  }

  /** Limits the opt-in occupied client fixture to its one Arrow train. */
  private static boolean riderFixtureOnly() {
    return riderFixtureRequired()
        && Boolean.getBoolean("fourthdimension.carrierFixtureRiderOnly");
  }

  private static void preload(ServerLevel level, BlockPos min, BlockPos max) {
    for (int chunkX = min.getX() >> 4; chunkX <= max.getX() >> 4; chunkX++)
      for (int chunkZ = min.getZ() >> 4; chunkZ <= max.getZ() >> 4; chunkZ++)
        level.getChunk(chunkX, chunkZ);
  }
}
