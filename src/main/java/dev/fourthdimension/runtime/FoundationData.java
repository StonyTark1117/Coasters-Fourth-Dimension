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
  public static final int SCHEMA = 2;
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
    public boolean stopping, maintenance;
    public String fault = "";
    public long revision;
    public double damping = 5, ballast;
    public final List<Carrier> carriers = new ArrayList<>();
    public BlockPos boundFrom, boundTo;

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

    public Carrier(UUID body, double side) {
      this.body = body;
      this.side = side;
    }
  }

  @Override
  public CompoundTag save(CompoundTag root, HolderLookup.Provider registries) {
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
      ListTag keys = new ListTag();
      for (var key : rail.profile.keys()) {
        CompoundTag k = new CompoundTag();
        k.putDouble("distance", key.distance());
        k.putDouble("angle", key.angle());
        k.putDouble("slope", key.slope());
        keys.add(k);
      }
      tag.put("profile", keys);
      tracks.add(tag);
    }
    root.put("rails", tracks);
    ListTag carts = new ListTag();
    for (Vehicle vehicle : vehicles.values()) {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("chassis", vehicle.chassis);
      tag.putString("family", vehicle.family.id());
      tag.putString("variant", vehicle.variant);
      tag.putBoolean("restraints", vehicle.restraints);
      tag.putBoolean("stopping", vehicle.stopping);
      tag.putBoolean("maintenance", vehicle.maintenance);
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
        c.putDouble("angle", carrier.angle);
        c.putDouble("omega", carrier.omega);
        c.putBoolean("capture", carrier.capture);
        c.putDouble("pivot_height", carrier.pivotHeight);
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
    if (root.getInt("schema") != SCHEMA && root.getInt("schema") != 1)
      throw new IllegalArgumentException("Unsupported fourthdimension save schema");
    FoundationData data = new FoundationData();
    var rails = root.getList("rails", Tag.TAG_COMPOUND);
    var vehicles = root.getList("vehicles", Tag.TAG_COMPOUND);
    if (rails.size() > 256 || vehicles.size() > 64)
      throw new IllegalArgumentException("Foundation fixture capacity exceeded");
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
    }
    for (Tag raw : vehicles) {
      CompoundTag tag = (CompoundTag) raw;
      var vehicle = new Vehicle(tag.getUUID("chassis"), Family.byId(tag.getString("family")));
      validateIdentity(tag);
      vehicle.restraints = tag.getBoolean("restraints");
      vehicle.stopping = tag.getBoolean("stopping");
      vehicle.maintenance = tag.getBoolean("maintenance");
      vehicle.fault = tag.getString("fault");
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
      if (!VARIANT.equals(vehicle.variant)
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
        carrier.angle = c.getDouble("angle");
        carrier.omega = c.getDouble("omega");
        carrier.capture = c.getBoolean("capture");
        carrier.pivotHeight = c.contains("pivot_height") ? c.getDouble("pivot_height") : 2;
        if (c.contains("seats"))
          carrier.seats = Arrays.stream(c.getLongArray("seats")).mapToObj(BlockPos::of).toList();
        if (!Double.isFinite(carrier.pivotHeight)
            || carrier.pivotHeight < 0
            || carrier.pivotHeight > 4
            || carrier.seats.isEmpty()
            || carrier.seats.size() > 4
            || new HashSet<>(carrier.seats).size() != carrier.seats.size()
            || carrier.seats.stream().anyMatch(p -> p.distSqr(BlockPos.ZERO) > 16))
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
          if (!carrier.seats.contains(seat)
              || carrier.riderSeats.containsValue(seat)
              || carrier.riderSeats.put(assignment.getUUID("player"), seat) != null)
            throw new IllegalArgumentException("Invalid rider seat assignment");
        }
        vehicle.carriers.add(carrier);
      }
      if (data.vehicles.put(vehicle.chassis, vehicle) != null)
        throw new IllegalArgumentException("Duplicate vehicle identity");
    }
    data.checkProfileCapacity(0);
    return data;
  }

  private static void validateIdentity(CompoundTag tag) {
    // The first local schema-1 prototype omitted these fixed identifiers.
    if (tag.contains("variant") && !VARIANT.equals(tag.getString("variant")))
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
