package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Explicit operator recovery, using native couplers and ordinary longitudinal propulsion. */
public final class RecoveryOperations {
  private RecoveryOperations() {}

  private record Selection(
      net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, UUID id) {}

  private static final Map<ServerPlayer, Selection> SELECTED = new WeakHashMap<>();

  private record ChainProgress(Vector3d start, double seconds) {}

  private static final Map<ServerLevel, Map<UUID, ChainProgress>> CHAIN_PROGRESS =
      new WeakHashMap<>();
  public static final double MAX_SPEED = .75;
  /** Largest original consist covered by the occupied native recovery strength envelope. */
  public static final int MAX_RECOVERY_CARS = 4;
  public static final String RIDER_CLEARANCE_FAULT =
      "Rider equipment exceeds certified recovery clearance: stow held items and use only vanilla"
          + " armor or elytra before resuming";

  private static void clearChainProgress(ServerLevel level, UUID cart) {
    var progress = CHAIN_PROGRESS.get(level);
    if (progress != null) progress.remove(cart);
  }

  public static void select(ServerPlayer player, SubLevel body) {
    requireOrdinary(player, body);
    SELECTED.put(player, new Selection(player.serverLevel().dimension(), body.getUniqueId()));
  }

  private static void requireOrdinary(ServerPlayer player, SubLevel body) {
    var center = body == null ? null : CoasterCartResolve.cartCenterWorld(body);
    var operator = operatorWorldPosition(player);
    var close = center != null && center.distanceSquared(operator) <= 16 * 16;
    var canInteract =
        center != null
            && player
                .serverLevel()
                .mayInteract(
                    player, net.minecraft.core.BlockPos.containing(center.x, center.y, center.z));
    var certified = ordinary(player.serverLevel(), body);
    if (!close || !player.mayBuild() || !canInteract || !certified) {
      var vehicle = player.getVehicle();
      LogUtils.getLogger()
          .warn(
              "Recovery ordinary selection rejected center={} operator={} close={} mayBuild={} mayInteract={} certified={} mounted={} seatBinding={}",
              center,
              operator,
              close,
              player.mayBuild(),
              canInteract,
              certified,
              vehicle != null,
              vehicle != null && Boarding.at(player.serverLevel(), vehicle.blockPosition()) != null);
      throw new IllegalArgumentException(
          "Select a nearby certified bare ordinary bearing with sneak-use of the Rotation Tool");
    }
  }

  /** Resolve a mounted operator's seat coordinates into the overworld for proximity checks. */
  public static Vector3d operatorWorldPosition(ServerPlayer player) {
    var vehicle = player.getVehicle();
    var binding = vehicle == null ? null : Boarding.at(player.serverLevel(), vehicle.blockPosition());
    var container = SubLevelContainer.getContainer(player.serverLevel());
    var body = binding == null ? null : container.getSubLevel(binding.carrier().body);
    if (body != null) {
      var world =
          body.logicalPose().transformPosition(new Vector3d(vehicle.getX(), vehicle.getY(), vehicle.getZ()));
      if (Double.isFinite(world.x) && Double.isFinite(world.y) && Double.isFinite(world.z)) return world;
    }
    return new Vector3d(player.getX(), player.getY(), player.getZ());
  }

  public static boolean ordinary(net.minecraft.world.level.Level level, SubLevel body) {
    var data = EligibilityHooks.data(level);
    return body != null
        && !body.isRemoved()
        && data != null
        && !data.vehicles.containsKey(body.getUniqueId())
        && FamilyControllers.find(body).isEmpty()
        && OrdinaryGearCertificate.fits(body);
  }

  public static List<FoundationData.Vehicle> group(ServerLevel level, UUID cart) {
    return FoundationData.get(level).vehicles.values().stream()
        .filter(v -> cart.equals(v.recoveryCart))
        .toList();
  }

  public static boolean retained(FoundationData.Vehicle v) {
    return v.maintenance
        && v.restraints
        && !FoundationData.VARIANT.equals(v.variant)
        && !v.carriers.isEmpty()
        && v.carriers.stream().allMatch(c -> c.capture);
  }

  private static boolean components(ServerLevel level, FoundationData.Vehicle v) {
    var container = SubLevelContainer.getContainer(level);
    return container.getSubLevel(v.chassis) != null
        && FamilyControllers.find(container.getSubLevel(v.chassis)).equals(Set.of(v.family))
        && v.carriers.stream().allMatch(c -> container.getSubLevel(c.body) != null);
  }

  private static boolean support(ServerLevel level, FoundationData.Vehicle v) {
    var rail =
        FoundationData.get(level).rails.stream()
            .filter(r -> r.matches(v.boundFrom, v.boundTo))
            .findFirst()
            .orElse(null);
    return rail != null
        && rail.valid
        && rail.variant.equals(v.variant)
        && TrackGeometry.resolve(level, rail) != null;
  }

  /** Independent current main-rail check; never derives a 4D family from ordinary gear. */
  public static boolean ordinarySupport(
      net.minecraft.world.level.Level level, SubLevel cart, FoundationData.Vehicle v) {
    var center = CoasterCartResolve.cartCenterWorld(cart);
    if (center == null) return false;
    if (!(level instanceof ServerLevel server)) {
      var snapshot = EligibilityHooks.data(level);
      return snapshot != null
          && snapshot.rails.stream()
              .anyMatch(
                  r -> r.valid && r.variant.equals(v.variant) && r.matches(v.boundFrom, v.boundTo));
    }
    var hit =
        CoasterPathTrackFrame.nearestGraphHit(
            server, CoasterPathGraphManager.get(server), new Vec3(center.x, center.y, center.z), 2);
    var data = EligibilityHooks.data(level);
    return hit != null
        && data != null
        && data.rails.stream()
            .anyMatch(
                r ->
                    r.valid
                        && r.variant.equals(v.variant)
                        && r.matches(hit.edge().from(), hit.edge().to()));
  }

  public static boolean canCouple(ServerLevel level, SubLevel a, SubLevel b, double distance) {
    var data = FoundationData.get(level);
    var first = data.vehicles.get(a.getUniqueId());
    var second = data.vehicles.get(b.getUniqueId());
    if ((first == null) == (second == null)) return false;
    var v = first == null ? second : first;
    var cart = first == null ? a : b;
    if (v.recoveryCartRemoved
        || !retained(v)
        || !cart.getUniqueId().equals(v.recoveryCart)
        || !ordinary(level, cart)
        || !Double.isFinite(distance)
        || Math.abs(distance - WingVehicle.spacing(v)) > .01
        || !ordinarySupport(level, cart, v)
        || speed(level, cart) > .08) return false;
    var ids = CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(cart);
    if (ids.stream().anyMatch(id -> !id.equals(cart.getUniqueId()))) return false;
    var cars = group(level, v.recoveryCart);
    var linked = CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(first == null ? b : a);
    return !cars.isEmpty()
        && cars.size() <= MAX_RECOVERY_CARS
        && cars.stream()
            .allMatch(
                c ->
                    c.variant.equals(v.variant)
                        && retained(c)
                        && components(level, c)
                        && support(level, c)
                        && Boarding.ready(level, c)
                        && dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, c.chassis)
                            == null)
        && cars.stream()
            .map(c -> c.chassis)
            .collect(java.util.stream.Collectors.toSet())
            .equals(linked);
  }

  public static void attach(ServerPlayer player, FoundationData.Vehicle selected) {
    if (SpinningVehicle.is(selected))
      throw new IllegalArgumentException("S1 requires its matching dock; ordinary recovery is unsupported");
    if (WingVehicle.is(selected))
      throw new IllegalArgumentException("Wing uses ordinary track; restore its route and Stop before unloading");
    var level = player.serverLevel();
    var container = SubLevelContainer.getContainer(level);
    var selection = SELECTED.get(player);
    var id =
        selection != null && selection.dimension().equals(level.dimension())
            ? selection.id()
            : null;
    var raw = id == null ? null : container.getSubLevel(id);
    requireOrdinary(player, raw);
    var cart = (ServerSubLevel) raw;
    var cars = VehicleOperations.consist(level, selected);
    if (cars.size() > MAX_RECOVERY_CARS)
      throw new IllegalArgumentException(
          "Recovery towing is rated for at most " + MAX_RECOVERY_CARS + " original cars");
    if (cars.stream()
        .anyMatch(
            v ->
                v.recoveryCart != null
                    || !retained(v)
                    || !Boarding.ready(level, v)
                    || !components(level, v)
                    || !support(level, v)
                    || dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, v.chassis) != null))
      throw new IllegalArgumentException(
          "Capture and retain every car, close restraints, and detach any shuttle winch before"
              + " coupling");
    var parent = (ServerSubLevel) container.getSubLevel(selected.chassis);
    double spacing = WingVehicle.spacing(selected);
    var center = CoasterCartResolve.cartCenterWorld(parent);
    var ordinaryCenter = CoasterCartResolve.cartCenterWorld(cart);
    if (center == null
        || ordinaryCenter == null
        || Math.abs(center.distance(ordinaryCenter) - spacing) > .25)
      throw new IllegalArgumentException(
          "Place the recovery bearing " + spacing + " blocks from the end car axle");
    if (!ordinarySupport(level, cart, selected) || speed(level, cart) > .08)
      throw new IllegalArgumentException("Stop the ordinary bearing on matching functional track");
    cars.forEach(v -> v.recoveryCart = id);
    boolean linked = false;
    try {
      linked = CoasterCartTrainLinkConstraint.linkCarts(level, parent, cart, spacing);
      if (!linked)
        throw new IllegalArgumentException(
            "Recovery coupling rejected: select a free bearing and an end car");
    } finally {
      if (!linked) cars.forEach(v -> v.recoveryCart = null);
    }
    for (var v : cars) {
      v.stopping = true;
      v.revision++;
    }
    SELECTED.remove(player);
    FoundationData.get(level).setDirty();
  }

  public static void start(ServerLevel level, FoundationData.Vehicle selected) {
    var cars = requireGroup(level, selected);
    var cart = SubLevelContainer.getContainer(level).getSubLevel(selected.recoveryCart);
    if (cars.size() > MAX_RECOVERY_CARS)
      throw new IllegalArgumentException(
          "Recovery towing is rated for at most " + MAX_RECOVERY_CARS + " original cars");
    if (selected.recoveryCartRemoved
        || !ordinary(level, cart)
        || speed(level, cart) > .08
        || cars.stream()
            .anyMatch(
                v ->
                    !retained(v)
                        || !components(level, v)
                        || !support(level, v)
                        || dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, v.chassis)
                            != null
                        || !Boarding.ready(level, v)
                        || !ordinarySupport(level, cart, v)
                        || !CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                                SubLevelContainer.getContainer(level).getSubLevel(v.chassis))
                            .contains(v.recoveryCart)
                        || !ConsistRules.eligible(
                            level,
                            SubLevelContainer.getContainer(level).getSubLevel(v.chassis),
                            v)))
      throw new IllegalArgumentException(
          "Restore the complete matching recovery consist, stop and align every carrier before"
              + " towing");
    if (!ridersWithinCertifiedClearance(level, cars))
      throw new IllegalArgumentException(RIDER_CLEARANCE_FAULT);
    for (var v : cars) {
      v.stopping = false;
      v.fault = "";
      v.revision++;
    }
    clearChainProgress(level, selected.recoveryCart);
    FoundationData.get(level).setDirty();
  }

  public static void brake(ServerLevel level, FoundationData.Vehicle selected) {
    for (var v : requireGroup(level, selected)) {
      v.stopping = true;
      v.revision++;
    }
    clearChainProgress(level, selected.recoveryCart);
    FoundationData.get(level).setDirty();
  }

  public static void detach(ServerLevel level, FoundationData.Vehicle selected) {
    var cars = requireGroup(level, selected);
    var cart = SubLevelContainer.getContainer(level).getSubLevel(selected.recoveryCart);
    if (cart == null && !selected.recoveryCartRemoved)
      throw new IllegalArgumentException(
          "Restore the unavailable recovery bearing before disconnecting; permanent removal must be"
              + " observed");
    if ((cart != null && speed(level, cart) > .08)
        || cars.stream().anyMatch(v -> !components(level, v) || !Boarding.ready(level, v)))
      throw new IllegalArgumentException("Brake and align every car before disconnecting recovery");
    for (var v : cars)
      dev.fourthdimension.mixin.RecoveryLinkAccess.fourthdimension$unlink(
          level, v.chassis, selected.recoveryCart);
    clearChainProgress(level, selected.recoveryCart);
    for (var v : cars) {
      v.recoveryCart = null;
      v.recoveryCartRemoved = false;
      v.stopping = true;
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  private static List<FoundationData.Vehicle> requireGroup(
      ServerLevel level, FoundationData.Vehicle v) {
    if (v.recoveryCart == null)
      throw new IllegalArgumentException("Attach a selected recovery bearing first");
    return group(level, v.recoveryCart);
  }

  private static double speed(ServerLevel level, SubLevel body) {
    return body instanceof ServerSubLevel server
        ? SubLevelContainer.getContainer(level)
            .physicsSystem()
            .getPipeline()
            .getLinearVelocity(server, new Vector3d())
            .length()
        : Double.POSITIVE_INFINITY;
  }

  /** Missing/broken native equipment retains mode and requires explicit operator resume. */
  public static void tick(ServerLevel level, double dt) {
    var container = SubLevelContainer.getContainer(level);
    var data = FoundationData.get(level);
    var ids =
        data.vehicles.values().stream()
            .map(v -> v.recoveryCart)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    var progress = CHAIN_PROGRESS.computeIfAbsent(level, key -> new HashMap<>());
    progress.keySet().retainAll(ids);
    for (var id : ids) {
      var cars = group(level, id);
      var cart = container.getSubLevel(id);
      String fault = "";
      if (cars.size() > MAX_RECOVERY_CARS)
        fault =
            "Recovery consist exceeds the "
                + MAX_RECOVERY_CARS
                + "-car strength rating: brake and split the train before resuming";
      else if (!ordinary(level, cart)
          || cars.stream()
              .anyMatch(
                  v ->
                      v.recoveryCartRemoved
                          || !retained(v)
                          || !components(level, v)
                          || !support(level, v)
                          || dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, v.chassis)
                              != null
                          || !ordinarySupport(level, cart, v)
                          || !CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                                  container.getSubLevel(v.chassis))
                              .contains(id)
                          || !ConsistRules.eligible(level, container.getSubLevel(v.chassis), v)))
        fault =
            "Recovery equipment, coupling or matching track unavailable: brake and inspect before"
                + " resume";
      else if (!ridersWithinCertifiedClearance(level, cars)) fault = RIDER_CLEARANCE_FAULT;
      else if (speed(level, cart) > MAX_SPEED
          || cars.stream()
              .anyMatch(v -> speed(level, container.getSubLevel(v.chassis)) > MAX_SPEED))
        fault = "Recovery speed exceeded 0.75 blocks/s: brake and inspect propulsion";
      if (fault.isEmpty() && cart instanceof ServerSubLevel lead) {
        container.physicsSystem().updatePose(lead);
        var center = CoasterCartResolve.cartCenterWorld(lead);
        var hit =
            CoasterPathTrackFrame.nearestGraphHit(
                level,
                CoasterPathGraphManager.get(level),
                new Vec3(center.x, center.y, center.z),
                2);
        var leadingRail =
            data.rails.stream()
                .filter(r -> r.matches(hit.edge().from(), hit.edge().to()))
                .findFirst()
                .orElse(null);
        // Check the ordinary leading axle before it reaches a wrong-family or open end.
        // This only requests braking; it gives the ordinary bearing no 4D mechanism.
        BoundaryGuard.check(level, lead, cars.getFirst(), leadingRail);
        var chain =
            dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery.findAtGraphHit(
                level, hit, 0);
        double target =
            chain == null
                ? 0
                : Math.abs(
                    dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftDriveDirection
                        .signedChainBlocksPerSecond(chain.anchor(), chain.path(), 0, 256, 16));
        if (target > .1
            && cars.stream().noneMatch(v -> v.stopping || !v.fault.isEmpty())
            && Double.isFinite(dt)
            && dt > 0) {
          var previous = progress.get(id);
          // Observe physical displacement, not pre-step velocity: native chain speed matching
          // can apply a velocity before the loaded couplers solve it back to a standstill.
          if (previous == null || previous.start.distance(center) > .08) {
            progress.put(id, new ChainProgress(new Vector3d(center), 0));
          } else {
            double seconds = previous.seconds + dt;
            progress.put(id, new ChainProgress(previous.start, seconds));
            if (seconds >= 4)
              fault = "Native chain drive stalled: brake and inspect propulsion before resuming";
          }
        } else progress.remove(id);
      }
      if (!fault.isEmpty()) progress.remove(id);
      if (!fault.isEmpty())
        for (var v : cars) {
          if (!v.stopping || !v.fault.equals(fault)) v.revision++;
          v.stopping = true;
          v.restraints = true;
          v.carriers.forEach(c -> c.capture = true);
          v.fault = fault;
        }
      if (cars.stream().anyMatch(v -> v.stopping || !v.fault.isEmpty())) {
        for (var v : cars)
          if (!v.stopping) {
            v.stopping = true;
            v.revision++;
          }
        if (cart instanceof ServerSubLevel body) {
          var pipeline = container.physicsSystem().getPipeline();
          var velocity = pipeline.getLinearVelocity(body, new Vector3d());
          if (velocity.length() > 0)
            pipeline.addLinearAndAngularVelocity(
                body, velocity.mul(-Math.min(1, 6 * dt / velocity.length())), new Vector3d());
        }
      }
    }
  }

  /**
   * Keep moving recovery scenes inside the player meshes certified by the FD402 clearance audit.
   * Riders may change equipment after the operator starts towing, so both {@link #start} and
   * {@link #tick} apply this policy. Non-player passengers retain their existing behavior; the
   * advertised clearance claim is specifically for mounted vanilla players.
   */
  private static boolean ridersWithinCertifiedClearance(
      ServerLevel level, List<FoundationData.Vehicle> cars) {
    return cars.stream()
        .flatMap(v -> v.carriers.stream())
        // riderSeats is updated by the mount event before the next physics callback;
        // occupants also covers live native riders after the mass/seat sample.
        .flatMap(
            c ->
                java.util.stream.Stream.concat(
                    c.riderSeats.keySet().stream(), c.occupants.stream()))
        .distinct()
        .map(level::getEntity)
        .filter(ServerPlayer.class::isInstance)
        .map(ServerPlayer.class::cast)
        .allMatch(
            player ->
                certifiedPlayerEquipment(
                    player.getMainHandItem(),
                    player.getOffhandItem(),
                    player.getItemBySlot(EquipmentSlot.HEAD),
                    player.getItemBySlot(EquipmentSlot.CHEST),
                    player.getItemBySlot(EquipmentSlot.LEGS),
                    player.getItemBySlot(EquipmentSlot.FEET)));
  }

  static boolean certifiedPlayerEquipment(
      ItemStack main,
      ItemStack off,
      ItemStack head,
      ItemStack chest,
      ItemStack legs,
      ItemStack feet) {
    return RecoveryClearancePolicy.certifiedEquipment(
        main.isEmpty(),
        off.isEmpty(),
        head.isEmpty() || vanillaArmorFor(head, EquipmentSlot.HEAD),
        chest.isEmpty()
            || chest.is(Items.ELYTRA)
            || vanillaArmorFor(chest, EquipmentSlot.CHEST),
        legs.isEmpty() || vanillaArmorFor(legs, EquipmentSlot.LEGS),
        feet.isEmpty() || vanillaArmorFor(feet, EquipmentSlot.FEET));
  }

  private static boolean vanillaArmorFor(ItemStack stack, EquipmentSlot slot) {
    if (!(stack.getItem() instanceof ArmorItem armor) || armor.getEquipmentSlot() != slot)
      return false;
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft");
  }
}
