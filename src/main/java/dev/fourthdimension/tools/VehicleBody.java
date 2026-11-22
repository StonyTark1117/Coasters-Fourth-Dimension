package dev.fourthdimension.tools;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.FixedInvertedVehicle;
import dev.fourthdimension.vehicle.WingVehicle;
import java.util.List;

/** Original cosmetic geometry choices; independent of mechanical variant and palette. */
public enum VehicleBody {
  BASE("base", "Original"),
  WING_COMPACT("wing_compact", "Compact Wing"),
  FIXED_INVERTED_OPEN_FRAME("fixed_inverted_open_frame", "Open-frame Fixed Inverted");

  public final String id, label;
  VehicleBody(String id, String label) { this.id = id; this.label = label; }

  public static boolean supported(FoundationData.Vehicle vehicle) {
    return vehicle != null && (WingVehicle.is(vehicle) || FixedInvertedVehicle.is(vehicle));
  }

  public static List<VehicleBody> choices(FoundationData.Vehicle vehicle) {
    if (vehicle != null && WingVehicle.is(vehicle)) return List.of(BASE, WING_COMPACT);
    if (vehicle != null && FixedInvertedVehicle.is(vehicle)) return List.of(BASE, FIXED_INVERTED_OPEN_FRAME);
    return List.of(BASE);
  }

  public static VehicleBody require(FoundationData.Vehicle vehicle, String id) {
    if (supported(vehicle))
      for (var choice : choices(vehicle)) if (choice.id.equals(id)) return choice;
    throw new IllegalArgumentException("Choose an available body for this vehicle");
  }

  /** Keep an unavailable saved ID intact while displaying the original body. */
  public static VehicleBody resolve(FoundationData.Vehicle vehicle) {
    if (vehicle != null)
      for (var choice : choices(vehicle)) if (choice.id.equals(vehicle.bodyId)) return choice;
    return BASE;
  }
}
