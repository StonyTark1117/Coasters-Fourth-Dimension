package dev.fourthdimension.track;

import dev.fourthdimension.integration.RideLocalOperations;
import dev.fourthdimension.runtime.FoundationData;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/**
 * Player-facing bridge for the bounded 1.2 example. It delegates route authority to
 * RideLocalOperations; it does not create a second reservation or dispatcher.
 */
public final class Fd12ArtExample {
  public static final String ROUTE_ID = RideLocalOperations.ROUTE_ID;
  public static final int MAX_TRAINS = RideLocalOperations.MAX_TRAINS;

  private Fd12ArtExample() {}

  /** Configure the named eight-segment Arrow circuit after the example has built its rails. */
  public static void configure(ServerLevel level, List<UUID> orderedArrowRails) {
    RideLocalOperations.configure(level, orderedArrowRails);
  }

  /** Admit one complete original Arrow consist and reserve its current/next blocks. */
  public static void admit(ServerLevel level, FoundationData.Vehicle lead) {
    RideLocalOperations.admit(level, lead);
  }

  public static String playerInstructions() {
    return "arrow_fixed_circuit_v1: configure eight Arrow segments/six blocks, admit at most three "
        + "homogeneous original Arrow trains, then dispatch only when current/next blocks are free";
  }
}
