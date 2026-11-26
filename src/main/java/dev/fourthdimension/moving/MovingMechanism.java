package dev.fourthdimension.moving;

/** Persist IDs, never enum ordinals. All eight mechanisms share one retained-body contract. */
public enum MovingMechanism {
  TRANSFER("transfer"), TURNTABLE("turntable"), DROP("drop"), TILT("tilt"),
  ELEVATOR("elevator"), TEETER("teeter"), SIDE_DROP("side_drop"), COMPOUND("compound");

  private final String id;
  MovingMechanism(String id) { this.id = id; }
  public String id() { return id; }
  public boolean levelService() { return this == TRANSFER || this == TURNTABLE || this == ELEVATOR; }
  public static MovingMechanism byId(String id) {
    for (var value : values()) if (value.id.equals(id)) return value;
    throw new IllegalArgumentException("Unknown moving mechanism: " + id);
  }
}
