package dev.fourthdimension.integration;

import java.util.List;

/** Persisted cosmetic intent, including missing providers; never a functional track identity. */
public record TrackAppearance(String id, int tint) {
  public static final String ORIGINAL = TrackRenderOwnership.ORIGINAL;
  public static final TrackAppearance DEFAULT = new TrackAppearance(ORIGINAL, -1);
  public static final List<String> CANDIDATES =
      List.of(
          "coasterssimulatedextratypes:arrow_4d", "coasterssimulatedextratypes:arrow_4d_dbl_spine");

  public TrackAppearance {
    if (id == null || id.length() > 96 || !id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+"))
      throw new IllegalArgumentException("Invalid appearance identity");
    if (tint < -1 || tint > 0xffffff)
      throw new IllegalArgumentException("Appearance color must be a six-digit RGB value");
  }

  public boolean original() {
    return ORIGINAL.equals(id);
  }

  public String style() {
    return id.substring(id.indexOf(':') + 1);
  }

  public static TrackAppearance parse(String value) {
    var parts = value.split(";", -1);
    if (parts.length != 2 || !(parts[1].isEmpty() || parts[1].matches("#[0-9a-fA-F]{6}")))
      throw new IllegalArgumentException("Use appearance ID;#RRGGBB, or leave color empty");
    return new TrackAppearance(
        parts[0], parts[1].isEmpty() ? -1 : Integer.parseInt(parts[1].substring(1), 16));
  }

  public String formattedColor() {
    return tint < 0 ? "" : String.format(java.util.Locale.ROOT, "#%06X", tint);
  }
}
