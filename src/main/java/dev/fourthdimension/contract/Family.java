package dev.fourthdimension.contract;

/** Stable persisted identifiers; never serialize enum ordinals. */
public enum Family {
  ARROW("arrow_controlled", true),
  ZACSPIN("zacspin_legacy", false),
  FREE_SPIN("sns_free_spin", false),
  ZACSPIN_REVISED("zacspin_revised", false),
  PAX("pax_shuttle", false),
  WING("wing_fixed", false),
  PIPELINE("pipeline", false),
  SPINNING("spinning", false),
  SUSPENDED("suspended", false),
  FIXED_INVERTED("fixed_inverted", false),
  AXIS("axis", true),
  FLYING("flying", false),
  ROLLER_BALL("roller_ball", false);

  private final String id;
  private final boolean driven;

  Family(String id, boolean driven) {
    this.id = id;
    this.driven = driven;
  }

  public String id() {
    return id;
  }

  public boolean driven() {
    return driven;
  }

  public boolean rotary() {
    return this != WING && this != FIXED_INVERTED && this != PIPELINE;
  }

  public dev.fourthdimension.physics.CarrierAxis axis() {
    return this == AXIS ? dev.fourthdimension.physics.CarrierAxis.NEGATIVE_Z : this == SUSPENDED ? dev.fourthdimension.physics.CarrierAxis.Z : this == SPINNING ? dev.fourthdimension.physics.CarrierAxis.Y
        : dev.fourthdimension.physics.CarrierAxis.X;
  }

  /** The original prototype fixture remains the original five-family acceptance input. */
  public boolean foundationFixture() { return this != PIPELINE && this != WING && this != SPINNING && this != SUSPENDED && this != FIXED_INVERTED && this != AXIS && this != FLYING && this != ROLLER_BALL; }

  public static Family byId(String id) {
    for (Family family : values()) if (family.id.equals(id)) return family;
    throw new IllegalArgumentException("Unknown functional family: " + id);
  }
}
