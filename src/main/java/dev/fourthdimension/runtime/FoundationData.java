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
  public static final int SCHEMA = 1;
  public static final String VARIANT = "foundation_v1";
  public static final String PROVIDER = "fourthdimension:prototype";
  public final Map<UUID, Vehicle> vehicles = new LinkedHashMap<>();
  public final List<Rail> rails = new ArrayList<>();
  private ServerLevel owner;

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

  public record Rail(BlockPos from, BlockPos to, Family family, RotationProfile profile) {
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
      tag.putString("variant", VARIANT);
      tag.putString("provider", PROVIDER);
      tag.putString("contact_certificate", "simulatedcoasters:unmodified_0.1.5");
      ListTag keys = new ListTag();
      for (var key : rail.profile.keys()) {
        CompoundTag k = new CompoundTag();
        k.putDouble("distance", key.distance());
        k.putDouble("angle", key.angle());
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
      tag.putString("variant", VARIANT);
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
        ListTag occupancy = new ListTag();
        for (UUID occupant : carrier.occupants) occupancy.add(NbtUtils.createUUID(occupant));
        c.put("occupants", occupancy);
        groups.add(c);
      }
      tag.put("carriers", groups);
      carts.add(tag);
    }
    root.put("vehicles", carts);
    return root;
  }

  public static FoundationData load(CompoundTag root, HolderLookup.Provider registries) {
    if (root.getInt("schema") != SCHEMA)
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
        keys.add(new RotationProfile.Key(t.getDouble("distance"), t.getDouble("angle")));
      }
      data.rails.add(
          new Rail(
              BlockPos.of(tag.getLong("from")),
              BlockPos.of(tag.getLong("to")),
              Family.byId(tag.getString("family")),
              new RotationProfile(keys)));
    }
    for (Tag raw : vehicles) {
      CompoundTag tag = (CompoundTag) raw;
      var vehicle = new Vehicle(tag.getUUID("chassis"), Family.byId(tag.getString("family")));
      validateIdentity(tag);
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
        if (!Double.isFinite(carrier.angle)
            || !Double.isFinite(carrier.omega)
            || !Double.isFinite(carrier.side))
          throw new IllegalArgumentException("Invalid carrier state");
        List<UUID> occupants = new ArrayList<>();
        for (Tag o : c.getList("occupants", Tag.TAG_INT_ARRAY)) occupants.add(NbtUtils.loadUUID(o));
        carrier.occupants = List.copyOf(occupants);
        vehicle.carriers.add(carrier);
      }
      if (data.vehicles.put(vehicle.chassis, vehicle) != null)
        throw new IllegalArgumentException("Duplicate vehicle identity");
    }
    return data;
  }

  private static void validateIdentity(CompoundTag tag) {
    // The first local schema-1 prototype omitted these fixed identifiers.
    if (tag.contains("variant") && !VARIANT.equals(tag.getString("variant")))
      throw new IllegalArgumentException("Unsupported fixture variant");
    if (tag.contains("provider") && !PROVIDER.equals(tag.getString("provider")))
      throw new IllegalArgumentException("Unregistered functional provider");
    if (tag.contains("contact_certificate")
        && !"simulatedcoasters:unmodified_0.1.5".equals(tag.getString("contact_certificate")))
      throw new IllegalArgumentException("Unknown running gear certificate");
  }
}
