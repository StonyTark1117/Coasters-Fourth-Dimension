package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import org.joml.Vector3d;

public final class Boarding {
  private Boarding() {}

  private static final java.util.Map<ServerLevel, java.util.Map<java.util.UUID, Exit>> EXITS =
      new java.util.WeakHashMap<>();

  private record Exit(net.minecraft.world.phys.Vec3 position, long tick) {}

  private record Restore(long deadline, boolean noGravity, float localYaw, float localPitch) {}

  private static final java.util.Map<java.util.UUID, Restore> RESTORE = new java.util.HashMap<>();
  private static final java.util.Set<java.util.UUID> RESTORING = new java.util.HashSet<>();

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
        > .08) return false;
    for (var carrier : vehicle.carriers) {
      if (!carrier.capture
          || Math.abs(carrier.omega) > .08
          || Math.abs(Math.atan2(Math.sin(carrier.angle), Math.cos(carrier.angle))) > .04
          || container.getSubLevel(carrier.body) == null) return false;
    }
    return true;
  }

  public static boolean canBoard(Level level, BlockPos pos) {
    var binding = at(level, pos);
    return binding != null
        && !binding.vehicle.restraints
        && (level instanceof ServerLevel server
            ? ready(server, binding.vehicle)
            : binding.vehicle.boardingReady);
  }

  public static void mount(EntityMountEvent event) {
    Entity seat = event.getEntityBeingMounted();
    if (!(seat.level() instanceof ServerLevel level)) return;
    // Vanilla replays RootVehicle before the login event. Defer saved 4D riders
    // even with open restraints, so every restored seat uses the same local-view
    // conversion and waits for its carrier rather than mounting during player load.
    if (event.isMounting()
        && seat instanceof com.simibubi.create.content.contraptions.actors.seat.SeatEntity
        && !RESTORING.contains(event.getEntityMounting().getUUID())
        && event.getEntityMounting() instanceof net.minecraft.server.level.ServerPlayer player
        && hasSavedSeat(level, player.getUUID())) {
      event.setCanceled(true);
      return;
    }
    var binding = at(level, seat.blockPosition());
    if (binding == null || FoundationData.VARIANT.equals(binding.vehicle.variant)) return;
    if (event.isMounting()) {
      if (!RESTORING.contains(event.getEntityMounting().getUUID())
          && !restoringSavedPassenger(level, event.getEntityMounting(), seat, binding)
          && !canBoard(level, seat.blockPosition())) event.setCanceled(true);
    } else if (binding.vehicle.restraints
        && !event.getEntityMounting().isRemoved()
        && event.getEntityMounting().isAlive()) event.setCanceled(true);
    else if (event.isDismounting()
        && event.getEntityMounting() instanceof net.minecraft.server.level.ServerPlayer player) {
      if (!player.connection.isAcceptingMessages()) return;
      binding.carrier.riderSeats.remove(player.getUUID());
      var body = Sable.HELPER.getContaining(seat);
      if (body != null && player.isAlive()) {
        var world =
            body.logicalPose()
                .transformPosition(new Vector3d(seat.getX(), seat.getY(), seat.getZ()));
        var exit =
            findExit(level, player, new net.minecraft.world.phys.Vec3(world.x, world.y, world.z));
        if (exit != null) queueExit(level, player, exit);
      }
    }
  }

  private static boolean restoringSavedPassenger(
      ServerLevel level, Entity rider, Entity seat, Binding binding) {
    // Native entity NBT replays passengers before either entity joins the level. Keep that
    // saved attachment even with closed restraints; live boarding still uses canBoard.
    if (rider instanceof net.minecraft.world.entity.player.Player
        || rider.isAddedToLevel()
        || seat.isAddedToLevel()
        || !rider.isAlive()
        || !(seat instanceof com.simibubi.create.content.contraptions.actors.seat.SeatEntity)
        || !binding.carrier.occupants.contains(rider.getUUID())) return false;
    var body = SubLevelContainer.getContainer(level).getSubLevel(binding.carrier.body);
    return body != null
        && binding.carrier.seats.contains(
            seat.blockPosition().subtract(body.getPlot().getCenterBlock()));
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
            passenger.stopRiding();
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
    if (hasSavedSeat(player.serverLevel(), player.getUUID())) {
      RESTORE.put(
          player.getUUID(),
          new Restore(
              player.serverLevel().getGameTime() + 200,
              player.isNoGravity(),
              player.getYRot(),
              player.getXRot()));
      player.getPersistentData().putBoolean(GRAVITY_RESTORE, player.isNoGravity());
      player.setNoGravity(true);
      player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
      player.fallDistance = 0;
    }
  }

  private static boolean hasSavedSeat(ServerLevel level, java.util.UUID player) {
    return FoundationData.get(level).vehicles.values().stream()
        .flatMap(v -> v.carriers.stream())
        .anyMatch(c -> c.riderSeats.containsKey(player));
  }

  private static void finishRestore(net.minecraft.server.level.ServerPlayer player) {
    var state = RESTORE.remove(player.getUUID());
    if (state != null) player.setNoGravity(state.noGravity);
    player.getPersistentData().remove(GRAVITY_RESTORE);
    player.fallDistance = 0;
  }

  public static void logout(
      net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
    if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
      finishRestore(player);
      // Sable normally prunes disconnected UUIDs during its next simulation tick.
      // A reconnect while ticks are frozen can otherwise inherit the old connection's
      // tracking membership and never receive the native bodies' full initial sync.
      var level = player.serverLevel();
      var container = SubLevelContainer.getContainer(level);
      if (container == null) return;
      for (var vehicle : FoundationData.get(level).vehicles.values()) {
        forgetTracker(container.getSubLevel(vehicle.chassis), player.getUUID());
        for (var carrier : vehicle.carriers)
          forgetTracker(container.getSubLevel(carrier.body), player.getUUID());
      }
    }
  }

  private static void forgetTracker(
      dev.ryanhcode.sable.sublevel.SubLevel body, java.util.UUID player) {
    if (body instanceof ServerSubLevel server) server.getTrackingPlayers().remove(player);
  }

  private static void restore(ServerLevel level) {
    for (var player : level.players()) {
      var deadline = RESTORE.get(player.getUUID());
      if (deadline == null) continue;
      Binding found = null;
      for (var v : FoundationData.get(level).vehicles.values())
        for (var c : v.carriers)
          if (c.riderSeats.containsKey(player.getUUID())) found = new Binding(v, c);
      if (found == null) {
        finishRestore(player);
        continue;
      }
      var binding = found;
      var body = SubLevelContainer.getContainer(level).getSubLevel(binding.carrier.body);
      var offset = binding.carrier.riderSeats.get(player.getUUID());
      if (body != null) {
        var seat = body.getPlot().getCenterBlock().offset(offset);
        if (player.getVehicle() != null && player.getVehicle().blockPosition().equals(seat)) {
          RiderLookSync.send(player, binding.carrier.body, deadline.localYaw, deadline.localPitch);
          finishRestore(player);
          continue;
        }
        if (level.getBlockState(seat).getBlock() instanceof CarrierSeatBlock
            && !com.simibubi.create.content.contraptions.actors.seat.SeatBlock.isSeatOccupied(
                level, seat)) {
          RESTORING.add(player.getUUID());
          // Persisted rider angles are already carrier-local. Sable's client mount hook
          // expects world angles and transforms them into local space once more.
          var worldLook =
              dev.fourthdimension.physics.RiderLook.toWorld(
                  deadline.localYaw, deadline.localPitch, body.logicalPose().orientation());
          player.setYRot(worldLook.yaw());
          player.setXRot(worldLook.pitch());
          try {
            com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
                level, seat, player);
          } finally {
            // The mount teleport packet captured the world input. Server-side saved
            // rider state remains local, including when a boarding attempt is rejected.
            player.setYRot(deadline.localYaw);
            player.setXRot(deadline.localPitch);
            RESTORING.remove(player.getUUID());
          }
          if (player.isPassenger()) {
            RiderLookSync.send(
                player, binding.carrier.body, deadline.localYaw, deadline.localPitch);
            finishRestore(player);
            continue;
          }
        }
      }
      if (level.getGameTime() > deadline.deadline) {
        binding.carrier.riderSeats.remove(player.getUUID());
        finishRestore(player);
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
