package dev.fourthdimension.vehicle;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.shuttle.ShuttleRuntime;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import org.joml.Vector3d;
import org.slf4j.Logger;

public final class Boarding {
  private Boarding() {}

  private static final Logger LOGGER = LogUtils.getLogger();
  private static final boolean VALIDATION = Boolean.getBoolean("fourthdimension.validation");

  private static final java.util.Map<ServerLevel, java.util.Map<java.util.UUID, Exit>> EXITS =
      new java.util.WeakHashMap<>();

  private record Exit(net.minecraft.world.phys.Vec3 position, long tick) {}

  private record Restore(long deadline, boolean noGravity, float yaw, float pitch) {}

  private static final java.util.Map<java.util.UUID, Restore> RESTORE = new java.util.HashMap<>();
  private static final java.util.Set<java.util.UUID> RESTORING = new java.util.HashSet<>();
  private static final java.util.Set<java.util.UUID> REMOVAL_RECOVERY = new java.util.HashSet<>();

  public record Binding(FoundationData.Vehicle vehicle, FoundationData.Carrier carrier) {}

  public static Binding at(Level level, BlockPos seat) {
    var body = Sable.HELPER.getContaining(level, seat);
    var data = EligibilityHooks.data(level);
    if (body == null || data == null) return null;
    for (var vehicle : data.vehicles.values())
      for (var carrier : vehicle.carriers)
        if (carrier.body.equals(body.getUniqueId())) return new Binding(vehicle, carrier);
    return null;
  }

  public static boolean ready(ServerLevel level, FoundationData.Vehicle vehicle) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null
        || !(container.getSubLevel(vehicle.chassis) instanceof ServerSubLevel parent)) return false;
    if (container.physicsSystem().getPipeline().getLinearVelocity(parent, new Vector3d()).length()
            > .08
        && !ShuttleRuntime.stationHeld(level, vehicle)) return false;
    if (WingVehicle.is(vehicle)) {
      if (!vehicle.stopping || !vehicle.fault.isEmpty() || WingTrack.support(level, vehicle) == null)
        return false;
      var up = new Vector3d(0, 1, 0).rotate(parent.logicalPose().orientation());
      return up.y > .98;
    }
    if (RollerBallVehicle.is(vehicle)) return RollerBallRuntime.ready(level,parent,vehicle);
    if (AxisVehicle.is(vehicle)) return AxisRuntime.ready(level,parent,vehicle);
    if (NewGenerationFlyingVehicle.is(vehicle)) return NewGenerationFlyingRuntime.ready(level,parent,vehicle);
    if (FlyingVehicle.is(vehicle)) return FlyingRuntime.ready(level,parent,vehicle);
    if (DiveVehicle.is(vehicle)) return DiveRuntime.ready(level,parent,vehicle);
    if (StandingVehicle.is(vehicle)) return StandingRuntime.ready(level,parent,vehicle);
    if (MotorbikeVehicle.is(vehicle)) return MotorbikeRuntime.ready(level,parent,vehicle);
    if (FlyingBikeVehicle.is(vehicle)) return FlyingBikeRuntime.ready(level,parent,vehicle);
    if (PipelineVehicle.is(vehicle)) return PipelineRuntime.ready(level,parent,vehicle);
    if (FixedInvertedVehicle.is(vehicle)) return FixedInvertedRuntime.ready(level, parent, vehicle);
    if (SuspendedVehicle.is(vehicle)) return SuspendedRuntime.ready(level, parent, vehicle);
    if (PoweredInvertedYawVehicle.is(vehicle)) return PoweredInvertedYawRuntime.ready(level,parent,vehicle);
    if (S2SpinningVehicle.is(vehicle)) return S2SpinningRuntime.ready(level,parent,vehicle);
    if (SpinningVehicle.is(vehicle)) return SpinningRuntime.ready(level, parent, vehicle);
    for (var carrier : vehicle.carriers) {
      if (!carrier.capture
          || Math.abs(carrier.omega) > .08
          || Math.abs(Math.atan2(Math.sin(carrier.angle), Math.cos(carrier.angle))) > .04
          || container.getSubLevel(carrier.body) == null) return false;
    }
    return true;
  }

  public static boolean seatOccupied(Level level, BlockPos pos) {
    var binding=at(level,pos);
    if(binding==null||!(binding.vehicle.family.specialty()||DiveVehicle.is(binding.vehicle)||PipelineVehicle.is(binding.vehicle)||FixedInvertedVehicle.is(binding.vehicle)))
      return com.simibubi.create.content.contraptions.actors.seat.SeatBlock.isSeatOccupied(level,pos);
    return level.getEntitiesOfClass(com.simibubi.create.content.contraptions.actors.seat.SeatEntity.class,
        new net.minecraft.world.phys.AABB(pos)).stream().anyMatch(s->s.blockPosition().equals(pos)&&s.isVehicle());
  }

  public static boolean canBoard(Level level, BlockPos pos) {
    var binding = at(level, pos);
    return binding != null
        && !(FlyingBikeVehicle.is(binding.vehicle) && FlyingBikeRuntime.evacuation(binding.vehicle))
        && !binding.vehicle.restraints
        && (level instanceof ServerLevel server
            ? ready(server, binding.vehicle)
            : binding.vehicle.boardingReady);
  }

  public static void mount(EntityMountEvent event) {
    Entity seat = event.getEntityBeingMounted();
    if (!(seat.level() instanceof ServerLevel level)) return;
    var binding = at(level, seat.blockPosition());
    if (binding == null || FoundationData.VARIANT.equals(binding.vehicle.variant)) return;
    if (VALIDATION && event.getEntityMounting() instanceof net.minecraft.server.level.ServerPlayer player)
      LOGGER.info(
          "FD_BOARDING_EVENT player={} action={} seat={} restraints={} restoring={}",
          player.getGameProfile().getName(),
          event.isMounting() ? "mount" : "dismount",
          seat.blockPosition(),
          binding.vehicle.restraints,
          RESTORING.contains(player.getUUID()));
    if (event.isMounting()) {
      if (!RESTORING.contains(event.getEntityMounting().getUUID())
          && !canBoard(level, seat.blockPosition())) {
        event.setCanceled(true);
        return;
      }
      dev.fourthdimension.integration.RotatingSeatIntegration.mounted(seat);
      if (DiveVehicle.is(binding.vehicle)) {
        var pos=seat.blockPosition();seat.setPos(pos.getX(),pos.getY()+.4375,pos.getZ()+.5);
      }
      if (FixedInvertedVehicle.is(binding.vehicle)) {
        var pos = seat.blockPosition();
        seat.setPos(pos.getX(), pos.getY()+.4375, pos.getZ()+.5);
      }
      if (S2SpinningVehicle.is(binding.vehicle)) {
        var pos = seat.blockPosition();
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof S2SpinningSeatBlock block) {
          var center = block.getCenterOfMass(level, state);
          seat.setPos(pos.getX()+center.x(), pos.getY()+.75, pos.getZ()+center.z());
        }
      }
      if (SpinningVehicle.is(binding.vehicle)) {
        var pos = seat.blockPosition();
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof SpinningSeatBlock block) {
          var center = block.getCenterOfMass(level, state);
          seat.setPos(pos.getX()+center.x(), pos.getY()+.75, pos.getZ()+center.z());
        }
      }
      if (event.getEntityMounting() instanceof net.minecraft.server.level.ServerPlayer player) {
        var container = SubLevelContainer.getContainer(level);
        var body = container == null ? null : container.getSubLevel(binding.carrier.body);
        if (body != null) {
          var offset = seat.blockPosition().subtract(body.getPlot().getCenterBlock());
          if (VALIDATION)
            LOGGER.info(
                "FD_BOARDING_ASSIGN player={} seat={} offset={} knownSeats={} accepted={}",
                player.getGameProfile().getName(),
                seat.blockPosition(),
                offset,
                binding.carrier.seats,
                binding.carrier.seats.contains(offset));
          if (binding.carrier.seats.contains(offset)) {
            FoundationData.get(level)
                .vehicles
                .values()
                .forEach(v -> v.carriers.forEach(c -> c.riderSeats.remove(player.getUUID())));
            binding.carrier.riderSeats.put(player.getUUID(), offset);
            FoundationData.get(level).setDirty();
          }
        }
      }
    } else if (binding.vehicle.restraints
        && !event.getEntityMounting().isRemoved()
        && event.getEntityMounting().isAlive()) {
      if (VALIDATION)
        LOGGER.info(
            "FD_BOARDING_DISMOUNT_CANCEL player={} assignedSeats={}",
            event.getEntityMounting().getName().getString(),
            binding.carrier.riderSeats.size());
      event.setCanceled(true);
    }
    else if (event.isDismounting()
        && event.getEntityMounting() instanceof net.minecraft.server.level.ServerPlayer player) {
      if (!player.connection.isAcceptingMessages()) return;
      net.minecraft.world.phys.Vec3 flyingBikeExit = null;
      if (player.isAlive() && FlyingBikeVehicle.is(binding.vehicle)
          && !REMOVAL_RECOVERY.contains(player.getUUID())) {
        flyingBikeExit = FlyingBikeRuntime.exit(level, binding.vehicle, seat.blockPosition());
        if (!ready(level, binding.vehicle) || flyingBikeExit == null) {
          event.setCanceled(true);
          return;
        }
      }
      if (VALIDATION)
        LOGGER.info(
            "FD_BOARDING_DISMOUNT_CLEAR player={} restraints={} removed={} alive={}",
            player.getGameProfile().getName(),
            binding.vehicle.restraints,
            player.isRemoved(),
            player.isAlive());
      if (binding.carrier.riderSeats.remove(player.getUUID()) != null)
        FoundationData.get(level).setDirty();
      var body = Sable.HELPER.getContaining(seat);
      if (player.isAlive()) {
        net.minecraft.world.phys.Vec3 exit = flyingBikeExit;
        if (exit == null && body != null) {
          var world =
              body.logicalPose()
                  .transformPosition(new Vector3d(seat.getX(), seat.getY(), seat.getZ()));
          exit = findExit(level, player, new net.minecraft.world.phys.Vec3(world.x, world.y, world.z));
        }
        if (exit == null) exit = level.getSharedSpawnPos().above().getCenter();
        queueExit(level, player, exit);
      }
    }
  }

  private static net.minecraft.world.phys.Vec3 findExit(
      ServerLevel level, Entity player, net.minecraft.world.phys.Vec3 origin) {
    net.minecraft.world.phys.Vec3 best = null;
    double distance = Double.POSITIVE_INFINITY;
    var start = BlockPos.containing(origin);
    for (var floor : BlockPos.betweenClosed(start.offset(-8, -6, -8), start.offset(8, 1, 8))) {
      if (!level.hasChunkAt(floor)) continue;
      var shape = level.getBlockState(floor).getCollisionShape(level, floor);
      if (shape.isEmpty()) continue;
      double top = shape.max(net.minecraft.core.Direction.Axis.Y);
      var feet =
          new net.minecraft.world.phys.Vec3(
              floor.getX() + .5, floor.getY() + top + .01, floor.getZ() + .5);
      var box =
          new net.minecraft.world.phys.AABB(
              feet.x - .31, feet.y, feet.z - .31, feet.x + .31, feet.y + 1.9, feet.z + .31);
      if (!level.getFluidState(floor.above()).isEmpty() || !level.noCollision(player, box))
        continue;
      double score = feet.distanceToSqr(origin);
      if (score < distance) {
        best = feet;
        distance = score;
      }
    }
    return best;
  }

  /** Called before permanent native-body removal, while its world transform is still available. */
  public static void recover(ServerLevel level, FoundationData.Vehicle vehicle) {
    vehicle.restraints = false;
    var container = SubLevelContainer.getContainer(level);
    for (var carrier : vehicle.carriers) {
      carrier.riderSeats.clear();
      var body = container.getSubLevel(carrier.body);
      if (body == null) continue;
      for (var offset : carrier.seats) {
        var pos = body.getPlot().getCenterBlock().offset(offset);
        for (var seat :
            level.getEntitiesOfClass(
                com.simibubi.create.content.contraptions.actors.seat.SeatEntity.class,
                new net.minecraft.world.phys.AABB(pos))) {
          var world =
              body.logicalPose()
                  .transformPosition(new Vector3d(seat.getX(), seat.getY(), seat.getZ()));
          var origin = new net.minecraft.world.phys.Vec3(world.x, world.y, world.z);
          for (var passenger : java.util.List.copyOf(seat.getPassengers())) {
            var exit = findExit(level, passenger, origin);
            if (exit == null) exit = level.getSharedSpawnPos().above().getCenter();
            // Permanent native removal uses the existing emergency relocation path;
            // ordinary Flying Bike dismount still requires its validated dock/evacuation exit.
            REMOVAL_RECOVERY.add(passenger.getUUID());
            try {
              passenger.stopRiding();
            } finally {
              REMOVAL_RECOVERY.remove(passenger.getUUID());
            }
            if (passenger instanceof net.minecraft.server.level.ServerPlayer player)
              queueExit(level, player, exit);
            else passenger.teleportTo(exit.x, exit.y, exit.z);
            passenger.fallDistance = 0;
          }
          seat.discard();
        }
      }
    }
  }

  private static final String GRAVITY_RESTORE = "fourthdimension_restore_gravity";

  public static void login(
      net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
    if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
    // Recover the original flag even after an abrupt shutdown during the short loading wait.
    if (player.getPersistentData().contains(GRAVITY_RESTORE)) {
      player.setNoGravity(player.getPersistentData().getBoolean(GRAVITY_RESTORE));
      player.getPersistentData().remove(GRAVITY_RESTORE);
    }
    long assignments =
        FoundationData.get(player.serverLevel()).vehicles.values().stream()
            .flatMap(v -> v.carriers.stream())
            .filter(c -> c.riderSeats.containsKey(player.getUUID()))
            .count();
    if (assignments > 0) {
      long deadline = player.serverLevel().getGameTime() + 200;
      RESTORE.put(
          player.getUUID(),
          new Restore(deadline, player.isNoGravity(), player.getYRot(), player.getXRot()));
      player.getPersistentData().putBoolean(GRAVITY_RESTORE, player.isNoGravity());
      player.setNoGravity(true);
      player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
      player.fallDistance = 0;
      LOGGER.info(
          "FD_BOARDING_RESTORE queued player={} assignments={} deadlineTick={}",
          player.getGameProfile().getName(),
          assignments,
          deadline);
    }
  }

  private static void finishRestore(
      net.minecraft.server.level.ServerPlayer player, String outcome) {
    var state = RESTORE.remove(player.getUUID());
    if (state != null) {
      player.setNoGravity(state.noGravity);
      if ((outcome.equals("mounted") || outcome.equals("already-mounted"))
          && player.getVehicle() != null) {
        var binding = at(player.level(), player.getVehicle().blockPosition());
        if (binding != null) {
          player.setYRot(state.yaw);
          player.setXRot(state.pitch);
          net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
              new RestoredLookSync.Look(player.level().dimension().location(),
                  binding.carrier.body, player.getVehicle().blockPosition(), state.yaw, state.pitch));
        }
      }
      LOGGER.info(
          "FD_BOARDING_RESTORE {} player={} gameTick={} deadlineTick={}",
          outcome,
          player.getGameProfile().getName(),
          player.serverLevel().getGameTime(),
          state.deadline);
    }
    player.getPersistentData().remove(GRAVITY_RESTORE);
    player.fallDistance = 0;
  }

  public static void logout(
      net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
    if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
      finishRestore(player, "cancelled-logout");
  }

  private static void restore(ServerLevel level) {
    for (var player : level.players()) {
      var deadline = RESTORE.get(player.getUUID());
      if (deadline == null) continue;
      var data = FoundationData.get(level);
      Binding found = null;
      for (var v : data.vehicles.values())
        for (var c : v.carriers)
          if (c.riderSeats.containsKey(player.getUUID())) found = new Binding(v, c);
      if (found == null) {
        finishRestore(player, "assignment-cleared");
        continue;
      }
      var binding = found;
      var body = SubLevelContainer.getContainer(level).getSubLevel(binding.carrier.body);
      var offset = binding.carrier.riderSeats.get(player.getUUID());
      if (body != null) {
        var seat = body.getPlot().getCenterBlock().offset(offset);
        if (player.getVehicle() != null && player.getVehicle().blockPosition().equals(seat)) {
          finishRestore(player, "already-mounted");
          continue;
        }
        if ((level.getBlockState(seat).getBlock() instanceof CarrierSeatBlock
                || dev.fourthdimension.integration.RotatingSeatIntegration.managed(level,seat))
            && !seatOccupied(level, seat)) {
          RESTORING.add(player.getUUID());
          try {
            com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
                level, seat, player);
          } finally {
            RESTORING.remove(player.getUUID());
          }
          if (player.isPassenger()) {
            finishRestore(player, "mounted");
            continue;
          }
        }
      }
      if (level.getGameTime() > deadline.deadline) {
        if (binding.carrier.riderSeats.remove(player.getUUID()) != null) data.setDirty();
        finishRestore(player, "timed-out");
        var exit = findExit(level, player, player.position());
        if (exit == null) exit = level.getSharedSpawnPos().above().getCenter();
        queueExit(level, player, exit);
      }
    }
  }

  private static void queueExit(
      ServerLevel level,
      net.minecraft.server.level.ServerPlayer player,
      net.minecraft.world.phys.Vec3 point) {
    // Send after the passenger-removal packet: Sable's client dismount transform otherwise
    // overwrites a same-tick teleport with the old seat position.
    EXITS
        .computeIfAbsent(level, k -> new java.util.HashMap<>())
        .put(player.getUUID(), new Exit(point, level.getGameTime() + 2));
  }

  public static void exits(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    restore(level);
    var pending = EXITS.get(level);
    if (pending == null) return;
    for (var iterator = pending.entrySet().iterator(); iterator.hasNext(); ) {
      var entry = iterator.next();
      if (level.getGameTime() < entry.getValue().tick) continue;
      iterator.remove();
      var player = level.getServer().getPlayerList().getPlayer(entry.getKey());
      if (player != null
          && player.serverLevel() == level
          && !player.isPassenger()
          && player.isAlive()) {
        var p = entry.getValue().position;
        player.teleportTo(p.x, p.y, p.z);
        player.fallDistance = 0;
      }
    }
  }
}
