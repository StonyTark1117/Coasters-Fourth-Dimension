package dev.fourthdimension.contract;

/** Versioned contact orientation and complete reservation in the graph-sample design frame. */
public record NativeTrackFit(String contacts, String mount, Envelope envelope) {
  public static final String N_CONTACTS = "coasters_n_contacts_v1";
  public static final String ABOVE = "above_rail_v1";
  public NativeTrackFit {
    if (contacts == null || contacts.isBlank() || mount == null || mount.isBlank() || envelope == null)
      throw new IllegalArgumentException("Missing native track fit");
  }
  public boolean accepts(NativeTrackFit car) {
    return contacts.equals(car.contacts) && mount.equals(car.mount) && envelope.contains(car.envelope);
  }
  public record Envelope(double halfWidth, double aboveO, double belowO, double halfLength) {
    public Envelope {
      for (double v : new double[]{halfWidth, aboveO, belowO, halfLength})
        if (!Double.isFinite(v) || v <= 0) throw new IllegalArgumentException("Invalid envelope");
    }
    public boolean contains(Envelope other) {
      return halfWidth >= other.halfWidth && aboveO >= other.aboveO
          && belowO >= other.belowO && halfLength >= other.halfLength;
    }
  }
}
