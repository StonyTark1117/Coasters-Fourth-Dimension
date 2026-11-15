package dev.fourthdimension.integration;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

/** Saved ownership, independent of a vehicle's posture, restraint or dwell state. */
public final class RideAuthorityState {
  public UUID owner;
  public BlockPos controller;
  public String provider = "", reason = "";
  public long epoch, sequence;
  public boolean held = true, suspended;
  public List<UUID> cars = List.of();
  boolean restored;
  long sampledTick = Long.MIN_VALUE;
  int stoppedTicks;

  public CompoundTag save() {
    var t = new CompoundTag();
    if (owner == null) return t;
    t.putUUID("owner", owner); t.putLong("controller", controller.asLong());
    t.putString("provider", provider); t.putLong("epoch", epoch); t.putLong("sequence", sequence);
    t.putBoolean("held", held); t.putBoolean("suspended", suspended); t.putString("reason", reason);
    var list = new ListTag(); for (var id : cars) list.add(NbtUtils.createUUID(id)); t.put("cars", list);
    return t;
  }

  public static RideAuthorityState read(CompoundTag t) {
    var s = new RideAuthorityState();
    if (!t.hasUUID("owner")) return s;
    s.owner = t.getUUID("owner"); s.controller = BlockPos.of(t.getLong("controller"));
    s.provider = t.getString("provider"); s.epoch = t.getLong("epoch"); s.sequence = t.getLong("sequence");
    s.held = t.getBoolean("held"); s.suspended = t.getBoolean("suspended"); s.reason = t.getString("reason");
    var ids = new ArrayList<UUID>(); for (var raw : t.getList("cars", Tag.TAG_INT_ARRAY)) ids.add(NbtUtils.loadUUID(raw));
    if (ids.isEmpty() || ids.size() > 64 || new HashSet<>(ids).size() != ids.size()
        || s.provider.isEmpty() || s.provider.length() > 80 || s.epoch < 1 || s.sequence < 0)
      throw new IllegalArgumentException("Invalid saved ride authority");
    s.cars = List.copyOf(ids); s.restored = true;
    return s;
  }
}
