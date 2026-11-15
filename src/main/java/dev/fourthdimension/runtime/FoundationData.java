package dev.fourthdimension.runtime;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** World-owned authoritative bindings. Native Sable bodies persist poses and velocities. */
public final class FoundationData extends SavedData {
  public static final int SCHEMA = 7;
  // Keep full functional snapshots below Minecraft's one-megabyte custom-payload limit.
  public static final int MAX_PROFILE_KEYS = 8192;
  public static final String VARIANT = "foundation_v1";
  public static final String PROVIDER = "fourthdimension:prototype";
  public final Map<UUID, Vehicle> vehicles = new LinkedHashMap<>();
  public final List<Rail> rails = new ArrayList<>();
  private ServerLevel owner;

  public void checkProfileCapacity(int additionalKeys) {
    if (rails.stream().mapToInt(r -> r.profile.keys().size()).sum() + additionalKeys
        > MAX_PROFILE_KEYS)
      throw new IllegalArgumentException("Dimension rotation-profile capacity reached (8192 keys)");
  }

  public static FoundationData get(ServerLevel level) {
    var data =
        level
            .getDataStorage()
            .computeIfAbsent(
                new Factory<>(FoundationData::new, FoundationData::load),
                "fourthdimension_foundation");
    data.owner = level;
    return data;
  }

  public static final class Rail {
    public final BlockPos from, to;
    public final Family family;
    public RotationProfile profile;
    public final UUID id;
    public final String variant;
    public long geometryHash, revision;
    public boolean valid = true;
    public boolean liftLock;
    /**
     * Serialized native curve at the time this rail was authored. It is optional for legacy
     * saves; when present it lets an occupied support repair put back the exact former curve
     * instead of asking upstream's placement optimizer to guess its handles.
     */
    public CompoundTag originalCurve;
    public List<dev.fourthdimension.physics.MagneticSpan> magnets = List.of();
    public dev.fourthdimension.integration.TrackAppearance appearance =
        dev.fourthdimension.integration.TrackAppearance.DEFAULT;

    public Rail(BlockPos from, BlockPos to, Family family, RotationProfile profile) {
      this(from, to, family, profile, UUID.randomUUID(), VARIANT, 0, 0, true);
    }

    public Rail(
        BlockPos from,
        BlockPos to,
        Family family,
        RotationProfile profile,
        UUID id,
        String variant,
        long geometryHash,
        long revision,
        boolean valid) {
      this.from = from.immutable();
      this.to = to.immutable();
      this.family = family;
      this.profile = profile;
      this.id = id;
      this.variant = variant;
      this.geometryHash = geometryHash;
      this.revision = revision;
      this.valid = valid;
      if (!VARIANT.equals(variant)
          && dev.fourthdimension.track.TrackDesign.byVariant(variant).family != family)
        throw new IllegalArgumentException("Track family and variant disagree");
    }

    public BlockPos from() {
      return from;
    }

    public BlockPos to() {
      return to;
    }

    public Family family() {
      return family;
    }

    public RotationProfile profile() {
      return profile;
    }

    public boolean original() {
      return !VARIANT.equals(variant);
    }

    public boolean matches(BlockPos a, BlockPos b) {
      return from.equals(a) && to.equals(b) || from.equals(b) && to.equals(a);
    }

    public boolean adjacent(Rail other) {
      return from.equals(other.from)
          || from.equals(other.to)
          || to.equals(other.from)
          || to.equals(other.to);
    }
  }

  public static final class Vehicle {
    public final UUID chassis;
    public final Family family;
    public String variant = VARIANT;
    public boolean restraints, boardingReady;
    public boolean stopping, maintenance, alignmentRequired;
    /** Set only by explicit shuttle handover; survives unloading a remote stranded car. */
    public boolean shuttleDetached;
    public UUID recoveryCart;
    public boolean recoveryCartRemoved;
    public String fault = "";
    public long revision;
    public double damping = 5, ballast;
    public final List<Carrier> carriers = new ArrayList<>();
    public BlockPos boundFrom, boundTo;
    public BlockPos dock;

    public Vehicle(UUID chassis, Family family) {
      this.chassis = chassis;
      this.family = family;
    }
  }

  public static final class Carrier {
    public final UUID body;
    public final double side;
    public double angle, omega;
    public boolean capture;
    public List<UUID> occupants = List.of();
    public final Map<UUID, BlockPos> riderSeats = new HashMap<>();
    public List<BlockPos> seats = List.of(new BlockPos(0, -1, 0));
    public double pivotHeight = 2;
    public double longitudinal;
    public double childPivotOffset;
    public double childPivotX, childPivotZ;
    public boolean latched, liftHeld;
    public double captureTarget;
    public BlockPos liftFrom, liftTo;
    public double ballast;

    public Carrier(UUID body, double side) {
      this.body = body;
      this.side = side;
    }
  }

  @Override
  public CompoundTag save(CompoundTag root, HolderLookup.Provider registries) {
    return write(root, registries, true);
  }

  /** Server-only functional state sent to clients without restoration provenance. */
  public CompoundTag snapshot(CompoundTag root, HolderLookup.Provider registries) {
    return write(root, registries, false);
  }

  private CompoundTag write(
      CompoundTag root, HolderLookup.Provider registries, boolean includeOriginalCurves) {
    if (owner != null) CarrierRuntime.refreshForSave(this, owner);
    root.putInt("schema", SCHEMA);
    ListTag tracks = new ListTag();
    for (Rail rail : rails) {
      CompoundTag tag = new CompoundTag();
      tag.putLong("from", rail.from.asLong());
      tag.putLong("to", rail.to.asLong());
      tag.putString("family", rail.family.id());
      tag.putString("variant", rail.variant);
      tag.putString("provider", rail.original() ? "fourthdimension:original" : PROVIDER);
      tag.putUUID("id", rail.id);
      tag.putLong("geometry_hash", rail.geometryHash);
      tag.putLong("revision", rail.revision);
      tag.putBoolean("valid", rail.valid);
      tag.putBoolean("lift_lock", rail.liftLock);
      if (includeOriginalCurves && rail.originalCurve != null)
        tag.put("original_curve", rail.originalCurve.copy());
      ListTag keys = new ListTag();
      for (var key : rail.profile.keys()) {
        CompoundTag k = new CompoundTag();
        k.putDouble("distance", key.distance());
        k.putDouble("angle", key.angle());
        k.putDouble("slope", key.slope());
        keys.add(k);
      }
      tag.put("profile", keys);
      tag.put("magnets", dev.fourthdimension.magnetic.MagneticData.save(rail.magnets));
      CompoundTag appearance = new CompoundTag();
      appearance.putString("id", rail.appearance.id());
      appearance.putInt("tint", rail.appearance.tint());
      tag.put("appearance", appearance);
      tracks.add(tag);
    }
    root.put("rails", tracks);
    ListTag carts = new ListTag();
    for (Vehicle vehicle : vehicles.values()) {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("chassis", vehicle.chassis);
      tag.putString("family", vehicle.family.id());
      tag.putString("variant", vehicle.variant);
      if (vehicle.dock != null) tag.putLong("dock", vehicle.dock.asLong());
      if (dev.fourthdimension.vehicle.SpinningVehicle.is(vehicle)) {
        tag.putString("contact_layout", dev.fourthdimension.contract.NativeTrackFit.N_CONTACTS);
        tag.putString("mount_frame", dev.fourthdimension.contract.NativeTrackFit.ABOVE);
      }
      tag.putBoolean("restraints", vehicle.restraints);
      tag.putBoolean("stopping", vehicle.stopping);
      tag.putBoolean("maintenance", vehicle.maintenance);
      tag.putBoolean("alignment_required", vehicle.alignmentRequired);
      tag.putBoolean("shuttle_detached", vehicle.shuttleDetached);
      if (vehicle.recoveryCart != null) {
        tag.putUUID("recovery_cart", vehicle.recoveryCart);
        tag.putBoolean("recovery_cart_removed", vehicle.recoveryCartRemoved);
      }
      tag.putString("fault", vehicle.fault);
      tag.putLong("revision", vehicle.revision);
      tag.putBoolean("boarding_ready", vehicle.boardingReady);
      tag.putDouble("damping", vehicle.damping);
      tag.putDouble("ballast", vehicle.ballast);
      if (vehicle.boundFrom != null) {
        tag.putLong("boundFrom", vehicle.boundFrom.asLong());
        tag.putLong("boundTo", vehicle.boundTo.asLong());
      }
      ListTag groups = new ListTag();
      for (Carrier carrier : vehicle.carriers) {
        CompoundTag c = new CompoundTag();
        c.putUUID("body", carrier.body);
        c.putDouble("side", carrier.side);
        c.putDouble("longitudinal", carrier.longitudinal);
        c.putDouble("ballast", carrier.ballast);
        c.putDouble("angle", carrier.angle);
        c.putDouble("omega", carrier.omega);
        c.putBoolean("capture", carrier.capture);
        c.putDouble("pivot_height", carrier.pivotHeight);
        c.putDouble("child_pivot_offset", carrier.childPivotOffset);
        c.putDouble("child_pivot_x", carrier.childPivotX);
        c.putDouble("child_pivot_z", carrier.childPivotZ);
        c.putString("axis", vehicle.family.axis().name());
        c.putBoolean("latched", carrier.latched);
        c.putBoolean("lift_held", carrier.liftHeld);
        c.putDouble("capture_target", carrier.captureTarget);
        if (carrier.liftFrom != null) {
          c.putLong("lift_from", carrier.liftFrom.asLong());
          c.putLong("lift_to", carrier.liftTo.asLong());
        }
        c.putLongArray("seats", carrier.seats.stream().mapToLong(BlockPos::asLong).toArray());
        ListTag occupancy = new ListTag();
        for (UUID occupant : carrier.occupants) occupancy.add(NbtUtils.createUUID(occupant));
        c.put("occupants", occupancy);
        ListTag assignments = new ListTag();
        carrier.riderSeats.forEach(
            (id, seat) -> {
              CompoundTag assignment = new CompoundTag();
              assignment.putUUID("player", id);
              assignment.putLong("seat", seat.asLong());
              assignments.add(assignment);
            });
        c.put("rider_seats", assignments);
        groups.add(c);
      }
      tag.put("carriers", groups);
      carts.add(tag);
    }
    root.put("vehicles", carts);
    return root;
  }

  public static FoundationData load(CompoundTag root, HolderLookup.Provider registries) {
    if (root.getInt("schema") != SCHEMA
        && root.getInt("schema") != 6
        && root.getInt("schema") != 5
        && root.getInt("schema") != 4
        && root.getInt("schema") != 3
        && root.getInt("schema") != 2
        && root.getInt("schema") != 1)
      throw new IllegalArgumentException("Unsupported fourthdimension save schema");
    FoundationData data = new FoundationData();
    var rails = root.getList("rails", Tag.TAG_COMPOUND);
    var vehicles = root.getList("vehicles", Tag.TAG_COMPOUND);
    if (rails.size() > 256 || vehicles.size() > 64)
      throw new IllegalArgumentException("Foundation fixture capacity exceeded");
    Set<UUID> railIds = new HashSet<>();
    for (Tag raw : rails) {
      CompoundTag tag = (CompoundTag) raw;
      List<RotationProfile.Key> keys = new ArrayList<>();
      validateIdentity(tag);
      for (Tag k : tag.getList("profile", Tag.TAG_COMPOUND)) {
        var t = (CompoundTag) k;
        keys.add(
            new RotationProfile.Key(
                t.getDouble("distance"), t.getDouble("angle"), t.getDouble("slope")));
      }
      data.rails.add(
          new Rail(
              BlockPos.of(tag.getLong("from")),
              BlockPos.of(tag.getLong("to")),
              Family.byId(tag.getString("family")),
              new RotationProfile(keys),
              tag.hasUUID("id")
                  ? tag.getUUID("id")
                  : UUID.nameUUIDFromBytes(
                      (tag.getLong("from") + ":" + tag.getLong("to"))
                          .getBytes(java.nio.charset.StandardCharsets.UTF_8)),
              tag.contains("variant") ? tag.getString("variant") : VARIANT,
              tag.getLong("geometry_hash"),
              tag.getLong("revision"),
              !tag.contains("valid") || tag.getBoolean("valid")));
      var loadedRail = data.rails.getLast();
      loadedRail.liftLock = tag.getBoolean("lift_lock");
      if (tag.contains("original_curve", Tag.TAG_COMPOUND))
        loadedRail.originalCurve = tag.getCompound("original_curve").copy();
      if (!railIds.add(loadedRail.id))
        throw new IllegalArgumentException("Duplicate rail identity");
      if (tag.contains("appearance", Tag.TAG_COMPOUND)) {
        var appearance = tag.getCompound("appearance");
        loadedRail.appearance =
            new dev.fourthdimension.integration.TrackAppearance(
                appearance.getString("id"),
                appearance.contains("tint") ? appearance.getInt("tint") : -1);
      }
      loadedRail.magnets =
          dev.fourthdimension.magnetic.MagneticData.load(tag.getList("magnets", Tag.TAG_COMPOUND));
      dev.fourthdimension.magnetic.MagneticData.validate(loadedRail);
    }
    Set<UUID> nativeBodyIds = new HashSet<>();
    Set<UUID> assignedRiders = new HashSet<>();
    for (Tag raw : vehicles) {
      CompoundTag tag = (CompoundTag) raw;
      var vehicle = new Vehicle(tag.getUUID("chassis"), Family.byId(tag.getString("family")));
      validateIdentity(tag);
      if (data.vehicles.containsKey(vehicle.chassis))
        throw new IllegalArgumentException("Duplicate vehicle identity");
      if (!nativeBodyIds.add(vehicle.chassis))
        throw new IllegalArgumentException("Vehicle chassis reuses a carrier identity");
      vehicle.restraints = tag.getBoolean("restraints");
      vehicle.stopping = tag.getBoolean("stopping");
      vehicle.maintenance = tag.getBoolean("maintenance");
      vehicle.shuttleDetached = tag.getBoolean("shuttle_detached");
      if (tag.contains("recovery_cart") && !tag.hasUUID("recovery_cart"))
        throw new IllegalArgumentException("Invalid recovery cart identity");
      vehicle.recoveryCart = tag.hasUUID("recovery_cart") ? tag.getUUID("recovery_cart") : null;
      vehicle.recoveryCartRemoved = tag.getBoolean("recovery_cart_removed");
      if (vehicle.recoveryCart == null && vehicle.recoveryCartRemoved)
        throw new IllegalArgumentException("Removed recovery partner has no identity");
      vehicle.fault = tag.getString("fault");
      // Saves written before exact-support recovery had no separate alignment state. A still
      // faulted support-loss car must remain conservative when that field is absent.
      vehicle.alignmentRequired =
          tag.contains("alignment_required")
              ? tag.getBoolean("alignment_required")
              : dev.fourthdimension.vehicle.VehicleOperations.MATCHING_SUPPORT_LOST.equals(
                  vehicle.fault);
      vehicle.revision = tag.getLong("revision");
      vehicle.boardingReady = tag.getBoolean("boarding_ready");
      vehicle.damping = tag.contains("damping") ? tag.getDouble("damping") : 5;
      vehicle.ballast = tag.getDouble("ballast");
      if (!Double.isFinite(vehicle.damping)
          || vehicle.damping < 0
          || vehicle.damping > 250
          || !Double.isFinite(vehicle.ballast)
          || vehicle.ballast < 0
          || vehicle.ballast > 240)
        throw new IllegalArgumentException("Invalid loading/damping preset");
      vehicle.variant = tag.contains("variant") ? tag.getString("variant") : VARIANT;
      if (tag.contains("dock")) vehicle.dock = BlockPos.of(tag.getLong("dock"));
      if (dev.fourthdimension.vehicle.SpinningVehicle.is(vehicle)
          && (!dev.fourthdimension.contract.NativeTrackFit.N_CONTACTS.equals(tag.getString("contact_layout"))
              || !dev.fourthdimension.contract.NativeTrackFit.ABOVE.equals(tag.getString("mount_frame"))))
        throw new IllegalArgumentException("Unknown S1 contact/mount definition");
      if (!VARIANT.equals(vehicle.variant)
          && !dev.fourthdimension.vehicle.WingVehicle.is(vehicle)
          && dev.fourthdimension.track.TrackDesign.byVariant(vehicle.variant).family
              != vehicle.family)
        throw new IllegalArgumentException("Vehicle family and variant disagree");
      if (tag.contains("boundFrom")) {
        vehicle.boundFrom = BlockPos.of(tag.getLong("boundFrom"));
        vehicle.boundTo = BlockPos.of(tag.getLong("boundTo"));
      }
      var groups = tag.getList("carriers", Tag.TAG_COMPOUND);
      if (groups.size() > 8) throw new IllegalArgumentException("Too many carriers");
      for (Tag group : groups) {
        var c = (CompoundTag) group;
        var carrier = new Carrier(c.getUUID("body"), c.getDouble("side"));
        if (!(dev.fourthdimension.vehicle.WingVehicle.is(vehicle) && carrier.body.equals(vehicle.chassis))
            && !nativeBodyIds.add(carrier.body))
          throw new IllegalArgumentException("Duplicate native carrier identity");
        carrier.longitudinal = c.getDouble("longitudinal");
        carrier.childPivotOffset = c.getDouble("child_pivot_offset");
        carrier.childPivotX = c.getDouble("child_pivot_x");
        carrier.childPivotZ = c.getDouble("child_pivot_z");
        if (c.contains("axis") && !vehicle.family.axis().name().equals(c.getString("axis")))
          throw new IllegalArgumentException("Carrier axis disagrees with family");
        carrier.latched = c.getBoolean("latched");
        carrier.liftHeld = c.getBoolean("lift_held");
        carrier.captureTarget = c.getDouble("capture_target");
        if (c.contains("lift_from")) {
          carrier.liftFrom = BlockPos.of(c.getLong("lift_from"));
          carrier.liftTo = BlockPos.of(c.getLong("lift_to"));
        }
        if (!Double.isFinite(carrier.childPivotX) || !Double.isFinite(carrier.childPivotZ)
            || Math.abs(carrier.childPivotX) > .5 || Math.abs(carrier.childPivotZ) > .5
            || !Double.isFinite(carrier.captureTarget))
          throw new IllegalArgumentException("Invalid joint pivot/capture state");
        carrier.ballast = c.getDouble("ballast");
        if (!Double.isFinite(carrier.longitudinal)
            || Math.abs(carrier.longitudinal) > 3
            || !Double.isFinite(carrier.ballast)
            || carrier.ballast < 0
            || carrier.ballast > 240)
          throw new IllegalArgumentException("Invalid carrier offset or loading");
        carrier.angle = c.getDouble("angle");
        carrier.omega = c.getDouble("omega");
        carrier.capture = c.getBoolean("capture");
        carrier.pivotHeight = c.contains("pivot_height") ? c.getDouble("pivot_height") : 2;
        if (c.contains("seats"))
          carrier.seats = Arrays.stream(c.getLongArray("seats")).mapToObj(BlockPos::of).toList();
        if (!Double.isFinite(carrier.childPivotOffset)
            || carrier.childPivotOffset < -.5
            || carrier.childPivotOffset > 0
            || !Double.isFinite(carrier.pivotHeight)
            || carrier.pivotHeight < 0
            || carrier.pivotHeight > 4
            || carrier.seats.isEmpty()
            || carrier.seats.size() > 4
            || new HashSet<>(carrier.seats).size() != carrier.seats.size()
            || (!dev.fourthdimension.vehicle.WingVehicle.is(vehicle)
                && carrier.seats.stream().anyMatch(p -> p.distSqr(BlockPos.ZERO) > 16)))
          throw new IllegalArgumentException("Invalid carrier seating geometry");
        if (!Double.isFinite(carrier.angle)
            || !Double.isFinite(carrier.omega)
            || !Double.isFinite(carrier.side))
          throw new IllegalArgumentException("Invalid carrier state");
        List<UUID> occupants = new ArrayList<>();
        for (Tag o : c.getList("occupants", Tag.TAG_INT_ARRAY)) occupants.add(NbtUtils.loadUUID(o));
        carrier.occupants = List.copyOf(occupants);
        for (Tag rawAssignment : c.getList("rider_seats", Tag.TAG_COMPOUND)) {
          var assignment = (CompoundTag) rawAssignment;
          var seat = BlockPos.of(assignment.getLong("seat"));
          var rider = assignment.getUUID("player");
          if (!carrier.seats.contains(seat)
              || carrier.riderSeats.containsValue(seat)
              || !assignedRiders.add(rider)
              || carrier.riderSeats.put(rider, seat) != null)
            throw new IllegalArgumentException("Invalid rider seat assignment");
        }
        vehicle.carriers.add(carrier);
      }
      if (dev.fourthdimension.vehicle.WingVehicle.is(vehicle)) {
        if (vehicle.carriers.size() != 1) throw new IllegalArgumentException("Wing needs one fixed seat group");
        var fixed = vehicle.carriers.getFirst();
        if (!fixed.body.equals(vehicle.chassis)
            || !fixed.seats.equals(dev.fourthdimension.vehicle.WingVehicle.SEATS)
            || fixed.side != 0 || fixed.longitudinal != 0 || fixed.pivotHeight != 0
            || fixed.childPivotOffset != 0 || fixed.angle != 0 || fixed.omega != 0
            || vehicle.damping != 0 || vehicle.ballast != 0 || fixed.ballast != 0)
          throw new IllegalArgumentException("Invalid fixed Wing topology");
      } else if (dev.fourthdimension.vehicle.SpinningVehicle.is(vehicle)) {
        dev.fourthdimension.vehicle.SpinningVehicle.validate(vehicle);
      } else if (!VARIANT.equals(vehicle.variant)) {
        var layout = dev.fourthdimension.track.TrackDesign.byVariant(vehicle.variant);
        if (vehicle.carriers.size() != layout.groups()
            || vehicle.carriers.stream()
                .anyMatch(
                    c ->
                        c.seats.size()
                            != (layout == dev.fourthdimension.track.TrackDesign.LEGACY ? 4 : 2))
            || vehicle.carriers.stream().map(c -> c.body).distinct().count() != layout.groups())
          throw new IllegalArgumentException("Carrier topology does not match " + vehicle.variant);
        for (int i = 0; i < layout.groups(); i++) {
          var carrier = vehicle.carriers.get(i);
          if (carrier.side != (i < layout.groups() / 2 ? -3 : 3)
              || carrier.longitudinal != (layout.pairedRows() ? (i % 2 == 0 ? -3 : 3) : 0)
              || carrier.childPivotX != 0 || carrier.childPivotZ != 0
              || carrier.childPivotOffset != layout.childPivotOffset()
              || carrier.pivotHeight != 2 + layout.childPivotOffset())
            throw new IllegalArgumentException("Carrier order or pivot geometry changed");
        }
      }
      if (data.vehicles.put(vehicle.chassis, vehicle) != null)
        throw new IllegalArgumentException("Duplicate vehicle identity");
    }
    var recoveryVariants = new java.util.HashMap<UUID, String>();
    for (var vehicle : data.vehicles.values())
      if (vehicle.recoveryCart != null
          && (VARIANT.equals(vehicle.variant) || nativeBodyIds.contains(vehicle.recoveryCart)))
        throw new IllegalArgumentException("Recovery partner must be an ordinary cart identity");
    for (var vehicle : data.vehicles.values())
      if (vehicle.recoveryCart != null) {
        var previous = recoveryVariants.putIfAbsent(vehicle.recoveryCart, vehicle.variant);
        if (previous != null && !previous.equals(vehicle.variant))
          throw new IllegalArgumentException("Mixed functional families in saved recovery consist");
      }
    data.checkProfileCapacity(0);
    if (data.rails.stream().mapToInt(r -> r.magnets.size()).sum() > 512)
      throw new IllegalArgumentException("Magnetic equipment capacity exceeded");
    return data;
  }

  private static void validateIdentity(CompoundTag tag) {
    // The first local schema-1 prototype omitted these fixed identifiers.
    if (tag.contains("variant") && !VARIANT.equals(tag.getString("variant"))
        && !dev.fourthdimension.vehicle.WingVehicle.VARIANT.equals(tag.getString("variant")))
      dev.fourthdimension.track.TrackDesign.byVariant(tag.getString("variant"));
    if (tag.contains("provider")
        && !PROVIDER.equals(tag.getString("provider"))
        && !"fourthdimension:original".equals(tag.getString("provider")))
      throw new IllegalArgumentException("Unregistered functional provider");
    if (tag.contains("contact_certificate")
        && !"simulatedcoasters:unmodified_0.1.5".equals(tag.getString("contact_certificate")))
      throw new IllegalArgumentException("Unknown running gear certificate");
  }
}
