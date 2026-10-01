package dev.fourthdimension.integration.magnetized;

import dev.fourthdimension.shuttle.*;
import net.antopfr.coastersmagnetized.magnet.*;
import net.minecraft.gametest.framework.GameTestHelper;

public final class MagnetizedShuttleCheck {
  private MagnetizedShuttleCheck() {}

  public static Runnable attach(GameTestHelper helper, ShuttleExamples.Example example) {
    var level = helper.getLevel();
    var winch = example.winch();
    var route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
    for (var segment : route.segments)
      MagnetizedRideCheck.setEquipment(level, segment.rail(), AnchorMode.MAGNET, 100);
    System.out.println(
        "FD_MAGNETIZED_PAX_PROBE powered magnets at 100% across all "
            + route.segments.size()
            + " route segments");
    return () -> {
      for (var segment : route.segments)
        helper.assertTrue(
            MagnetBoost.accelerationFor(level, segment.edge()) > 0,
            "PAX Magnetized equipment lost power");
    };
  }
}
