package dev.fourthdimension.tools;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.magnetic.MagneticData;
import dev.fourthdimension.vehicle.*;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/** Applies one existing editor transaction, after all portable-envelope checks have passed. */
public final class PresetService {
  private PresetService() {}
  public static SettingPreset capture(FoundationData data, UUID id, boolean vehicle, String name, int section) {
    if (vehicle) {
      var v = data.vehicles.get(id);
      if (v == null || !v.family.foundationFixture() || v.family.driven() || FoundationData.VARIANT.equals(v.variant))
        throw new IllegalArgumentException("Tuning presets support Legacy, Free Spin, Revised and PAX cars");
      return new SettingPreset(name, v.family.id(), v.variant, SettingPreset.Kind.TUNING, v.damping + "," + v.ballast);
    }
    var r = data.rails.stream().filter(a -> a.id.equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Select a track section"));
    if (!r.original()) throw new IllegalArgumentException("Select an original track section");
    SettingPreset.Kind kind; String value;
    if (r.variant.equals(NewGenerationFlyingVehicle.VARIANT)) {
      kind = SettingPreset.Kind.FLYING_ROUTE;
      if (r.yaw.intervals.isEmpty()) throw new IllegalArgumentException("Author a route section first");
      value = NewGenerationFlyingEditor.format(r.yaw.intervals.get(Math.floorMod(section, r.yaw.intervals.size())));
      value = removeMarker(value, 7);
    } else if (r.variant.equals(PoweredInvertedYawVehicle.VARIANT)) {
      kind = SettingPreset.Kind.POWERED_ROUTE;
      if (r.poweredYaw.yaw.intervals.isEmpty()) throw new IllegalArgumentException("Author a route section first");
      value = PoweredInvertedYawEditor.format(r.poweredYaw, r.poweredYaw.yaw.intervals.get(Math.floorMod(section, r.poweredYaw.yaw.intervals.size())));
      value = removeMarker(value, 5);
    } else if (r.family.driven()) {
      kind = SettingPreset.Kind.PROFILE;
      value = r.profile.keys().stream().map(k -> k.distance() + ":" + Math.toDegrees(k.angle()) + ":" + Math.toDegrees(k.slope()))
          .collect(java.util.stream.Collectors.joining(";"));
    } else if (r.family == dev.fourthdimension.contract.Family.FREE_SPIN || r.variant.equals(S2SpinningVehicle.VARIANT)) {
      kind = SettingPreset.Kind.MAGNETS; value = MagneticData.format(r.magnets);
    } else throw new IllegalArgumentException("This section has no portable motion/tuning settings");
    return new SettingPreset(name, r.family.id(), r.variant, kind, value);
  }
  private static String removeMarker(String value, int field) {
    var f = value.split("\\|", -1); f[field] = ""; return String.join("|", f);
  }
  public static void apply(ServerPlayer player, UUID id, long revision, String envelope) {
    // Explicit destination consent accompanies every apply, including a new-world import.
    var parts = envelope.split("\n", -1);
    if (parts.length != 3 || !parts[0].equals("BIND_SELECTED"))
      throw new IllegalArgumentException("Explicitly bind this preset to the selected destination first");
    var preset = SettingPreset.parse(parts[2]);
    var data = FoundationData.get(player.serverLevel());
    boolean vehicle = preset.kind() == SettingPreset.Kind.TUNING;
    if (vehicle) {
      var v = VehicleOperations.accessible(player, id);
      preset.requireTarget(v.family.id(), v.variant, true);
    } else {
      var r = data.rails.stream().filter(a -> a.id.equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Track no longer exists"));
      preset.requireTarget(r.family.id(), r.variant, false);
    }
    int action = switch (preset.kind()) { case PROFILE -> 0; case MAGNETS -> 4; case TUNING -> 16; case FLYING_ROUTE -> 8; case POWERED_ROUTE -> 70; };
    String value = preset.settings();
    if (preset.kind() == SettingPreset.Kind.FLYING_ROUTE || preset.kind() == SettingPreset.Kind.POWERED_ROUTE) {
      var f = value.split("\\|", -1);
      int index = preset.kind() == SettingPreset.Kind.FLYING_ROUTE ? 7 : 5;
      int count = preset.kind() == SettingPreset.Kind.FLYING_ROUTE ? 14 : 16;
      if (f.length != count || !f[index].isEmpty()) throw new IllegalArgumentException("Portable route must omit world marker coordinates");
      if (f[3].equals("TARGET")) {
        if (parts[1].isBlank()) throw new IllegalArgumentException("Explicitly bind an owned marker in this world (x y z)");
        f[index] = parts[1];
      }
      value = String.join("|", f);
    }
    EditorNetwork.apply(player, new EditorNetwork.Edit(id, revision, action, value));
  }
}
