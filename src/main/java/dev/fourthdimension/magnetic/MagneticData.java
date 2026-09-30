package dev.fourthdimension.magnetic;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.MagneticSpan;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MagneticData {
  private MagneticData() {}

  public static ListTag save(List<MagneticSpan> spans) {
    var list = new ListTag();
    for (var span : spans) {
      var t = new CompoundTag();
      t.putDouble("start", span.start());
      t.putDouble("end", span.end());
      t.putString("mode", span.mode().name());
      t.putInt("direction", span.direction());
      t.putDouble("strength", span.strength());
      t.putDouble("falloff", span.falloff());
      t.putInt("targets", span.targets());
      list.add(t);
    }
    return list;
  }

  public static List<MagneticSpan> load(ListTag list) {
    if (list.size() > 8) throw new IllegalArgumentException("At most eight magnets per section");
    var spans = new ArrayList<MagneticSpan>();
    for (var raw : list) {
      var t = (CompoundTag) raw;
      spans.add(
          new MagneticSpan(
              t.getDouble("start"),
              t.getDouble("end"),
              MagneticSpan.Mode.valueOf(t.getString("mode")),
              t.getInt("direction"),
              t.getDouble("strength"),
              t.getDouble("falloff"),
              t.getInt("targets")));
    }
    return List.copyOf(spans);
  }

  public static void validate(FoundationData.Rail rail) {
    validate(rail, rail.magnets);
  }

  public static void validate(FoundationData.Rail rail, List<MagneticSpan> spans) {
    if (spans.size() > 8 || (!spans.isEmpty() && rail.family != Family.FREE_SPIN))
      throw new IllegalArgumentException(
          "Only Free Spin track supports up to eight magnetic spans");
    double length = rail.profile.keys().getLast().distance();
    for (var span : spans)
      if (span.end() > length + 1e-6)
        throw new IllegalArgumentException("Magnetic span exceeds section length " + length);
  }

  public static String format(List<MagneticSpan> spans) {
    return spans.stream()
        .map(
            s ->
                s.start()
                    + ":"
                    + s.end()
                    + ":"
                    + s.mode().name().toLowerCase(Locale.ROOT)
                    + ":"
                    + s.direction()
                    + ":"
                    + s.strength()
                    + ":"
                    + s.falloff()
                    + ":"
                    + s.targets())
        .collect(java.util.stream.Collectors.joining(";"));
  }

  public static List<MagneticSpan> parse(String text) {
    if (text.length() > 4096)
      throw new IllegalArgumentException("Magnet payload exceeds 4096 characters");
    if (text.isBlank()) return List.of();
    String[] rows = text.split(";", -1);
    if (rows.length > 8) throw new IllegalArgumentException("At most eight magnets per section");
    var result = new ArrayList<MagneticSpan>();
    try {
      for (var row : rows) {
        var v = row.trim().split(":", -1);
        if (v.length != 7)
          throw new IllegalArgumentException(
              "Use start:end:induce|damp:direction:strength:falloff:targets");
        result.add(
            new MagneticSpan(
                Double.parseDouble(v[0]),
                Double.parseDouble(v[1]),
                MagneticSpan.Mode.valueOf(v[2].toUpperCase(Locale.ROOT)),
                Integer.parseInt(v[3]),
                Double.parseDouble(v[4]),
                Double.parseDouble(v[5]),
                Integer.parseInt(v[6])));
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid magnet number");
    }
    return List.copyOf(result);
  }

  public static void apply(
      ServerPlayer player, UUID id, long revision, List<MagneticSpan> proposed) {
    var rail = ProfileEditor.requireEditable(player, id, revision);
    var level = player.serverLevel();
    var edge = TrackGeometry.resolve(level, rail);
    if (!rail.valid || edge == null || TrackGeometry.fingerprint(level, edge) != rail.geometryHash)
      throw new IllegalArgumentException("Rebind changed track before installing magnets");
    validate(rail, proposed);
    var data = FoundationData.get(level);
    if (data.rails.stream().mapToInt(r -> r.magnets.size()).sum()
            - rail.magnets.size()
            + proposed.size()
        > 512) throw new IllegalArgumentException("Dimension magnetic capacity reached (512)");
    Map<Item, Integer> change = new LinkedHashMap<>();
    for (var mode : MagneticSpan.Mode.values()) {
      Item item = (mode == MagneticSpan.Mode.INDUCE ? TrackItems.INDUCER : TrackItems.DAMPER).get();
      int delta =
          (int)
              (proposed.stream().filter(s -> s.mode() == mode).count()
                  - rail.magnets.stream().filter(s -> s.mode() == mode).count());
      change.put(item, delta);
      if (!player.isCreative()
          && delta > 0
          && player.getInventory().items.stream()
                  .filter(s -> s.is(item))
                  .mapToInt(ItemStack::getCount)
                  .sum()
              < delta)
        throw new IllegalArgumentException(
            "Requires " + delta + " " + item.getDescription().getString());
    }
    if (!player.isCreative())
      change.forEach(
          (item, delta) -> {
            if (delta < 0)
              player.getInventory().placeItemBackInInventory(new ItemStack(item, -delta));
            else {
              int remaining = delta;
              for (var stack : player.getInventory().items)
                if (stack.is(item)) {
                  int n = Math.min(remaining, stack.getCount());
                  stack.shrink(n);
                  remaining -= n;
                }
            }
          });
    rail.magnets = List.copyOf(proposed);
    rail.revision++;
    data.setDirty();
    FoundationSync.broadcast(level);
  }
}
