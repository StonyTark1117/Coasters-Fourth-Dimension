package dev.fourthdimension.tools;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Portable settings only. No entity ids, positions, owners, geometry hashes or authority tokens. */
public record SettingPreset(String name, String family, String variant, Kind kind, String settings) {
  public enum Kind { PROFILE, MAGNETS, TUNING, FLYING_ROUTE, POWERED_ROUTE }
  public static final int LIMIT = 12000;
  public SettingPreset {
    if (name == null || name.isBlank() || name.length() > 64 || name.chars().anyMatch(Character::isISOControl))
      throw new IllegalArgumentException("Preset name must contain 1–64 printable characters");
    if (family == null || !family.matches("[a-z_]{1,48}") || variant == null
        || !variant.matches("[a-z0-9_:.-]{1,128}") || kind == null || settings == null
        || settings.length() > 8000)
      throw new IllegalArgumentException("Invalid preset identity or settings size");
  }
  public String exportText() {
    String text = String.join("|", "FD_PRESET", "1", encode(name), family, variant, kind.name(), encode(settings));
    if (text.length() > LIMIT) throw new IllegalArgumentException("Preset exceeds sharing limit");
    return text;
  }
  public static SettingPreset parse(String text) {
    if (text == null || text.length() > LIMIT) throw new IllegalArgumentException("Preset too large");
    var f = text.strip().split("\\|", -1);
    if (f.length != 7 || !f[0].equals("FD_PRESET") || !f[1].equals("1"))
      throw new IllegalArgumentException("Expected Fourth Dimension preset format version 1");
    try { return new SettingPreset(decode(f[2]), f[3], f[4], Kind.valueOf(f[5]), decode(f[6])); }
    catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid preset: " + ex.getMessage()); }
  }
  public void requireTarget(String targetFamily, String targetVariant, boolean vehicle) {
    if (!family.equals(targetFamily) || !variant.equals(targetVariant) || vehicle != (kind == Kind.TUNING))
      throw new IllegalArgumentException("Preset requires its exact family, variant and target type");
  }
  private static String encode(String text) { return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8)); }
  private static String decode(String text) {
    byte[] raw = Base64.getUrlDecoder().decode(text);
    String result = new String(raw, StandardCharsets.UTF_8);
    if (!java.util.Arrays.equals(raw, result.getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException("Invalid UTF-8");
    return result;
  }
}
