package dev.fourthdimension.integration.engineered;

import dev.fourthdimension.shuttle.*;
import dev.jwaterfall.coastersengineered.CoastersEngineeredConfig;
import dev.jwaterfall.coastersengineered.CoastersEngineeredRegistry;
import dev.jwaterfall.coastersengineered.controller.*;
import dev.jwaterfall.coastersengineered.observer.CoasterObserverBlockEntity;
import dev.jwaterfall.coastersengineered.span.*;
import java.util.*;
import net.minecraft.gametest.framework.GameTestHelper;

/** Real Engineered motor commands opposing the occupied native PAX winch cycle. */
public final class EngineeredShuttleCheck {
  private EngineeredShuttleCheck() {}

  public static Runnable attach(GameTestHelper helper, ShuttleExamples.Example example) {
    return attach(helper, example, false);
  }

  public static Runnable attachReload(GameTestHelper helper, ShuttleExamples.Example example) {
    return attach(helper, example, true);
  }

  private static Runnable attach(
      GameTestHelper helper, ShuttleExamples.Example example, boolean reload) {
    var level = helper.getLevel();
    var winch = example.winch();
    var route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
    // The upstream default requires no FE. Do not accept an unpowered fixture as evidence.
    helper.assertTrue(
        !CoastersEngineeredConfig.get(CoastersEngineeredConfig.REQUIRE_ENERGY),
        "Engineered PAX fixture requires its default energy-free motor configuration");
    var anchor = route.segments.getFirst().rail().from;
    var span = TrackSpanBehaviour.at(level, anchor);
    helper.assertTrue(
        span != null
            && span.setSpan(
                new TrackSpan(
                    route.segments.stream()
                        .map(
                            s ->
                                new SpanSegment(
                                    s.edge().from(),
                                    s.edge().to(),
                                    s.reversed() ? 1 : 0,
                                    s.reversed() ? 0 : 1))
                        .toList())),
        "Engineered rejected the complete PAX motor span");
    // Keep solid equipment outside the moving carrier and carriage envelope.
    var pos = winch.getBlockPos().offset(-6, 0, 4);
    level.setBlockAndUpdate(
        pos, CoastersEngineeredRegistry.LINEAR_MOTOR_CONTROLLER.get().defaultBlockState());
    var motor = (LinearMotorControllerBlockEntity) level.getBlockEntity(pos);
    motor.bindTo(anchor);
    motor.setSteps(List.of(command(ShuttleWinchEntity.Phase.LIFTING)));
    var observerPos = pos.above(2);
    level.setBlockAndUpdate(
        observerPos, CoastersEngineeredRegistry.COASTER_OBSERVER.get().defaultBlockState());
    var observer = (CoasterObserverBlockEntity) level.getBlockEntity(observerPos);
    var middle = route.segments.get(1);
    observer.watch(SpanSegment.point(middle.edge(), .5));
    var sightings = new EnumMap<ShuttleWinchEntity.Phase, Integer>(ShuttleWinchEntity.Phase.class);
    var changes = EnumSet.noneOf(ShuttleWinchEntity.Phase.class);
    boolean[] observerSeen = {false};
    int[] boardingTicks = {0};
    ShuttleWinchEntity.Phase[] previous = {null};
    System.out.println(
        "FD_ENGINEERED_PAX_PROBE segments="
            + route.segments.size()
            + " energyRequired=false cars="
            + example.cars().size());
    String prefix = reload ? "FD_ENGINEERED_PAX_RELOAD" : "FD_ENGINEERED_PAX";
    return () -> {
      var currentWinch = winch;
      var currentMotor = motor;
      var currentObserver = observer;
      if (reload) {
        // Never load a chunk while observing the native eviction interval.
        // Upstream isLinked() resolves the anchor. Only query it when the complete route ticks.
        if (route.segments.stream()
            .anyMatch(
                s ->
                    !dev.fourthdimension.track.TrackGeometry.ticking(level, s.rail().from)
                        || !dev.fourthdimension.track.TrackGeometry.ticking(level, s.rail().to)))
          return;
        var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        var winchChunk =
            level
                .getChunkSource()
                .getChunkNow(winch.getBlockPos().getX() >> 4, winch.getBlockPos().getZ() >> 4);
        if (chunk == null || winchChunk == null) return;
        currentWinch = (ShuttleWinchEntity) winchChunk.getBlockEntities().get(winch.getBlockPos());
        currentMotor = (LinearMotorControllerBlockEntity) chunk.getBlockEntities().get(pos);
        currentObserver = (CoasterObserverBlockEntity) chunk.getBlockEntities().get(observerPos);
        if (currentWinch == null || currentMotor == null || currentObserver == null) return;
      }
      var phase = currentWinch.phase;
      if (previous[0] != phase) {
        currentMotor.setSteps(List.of(command(phase)));
        previous[0] = phase;
        changes.add(phase);
        System.out.println(
            prefix
                + "_COMMAND phase="
                + phase
                + " mode="
                + currentMotor.commandMode()
                + " heading="
                + currentMotor.commandHeading()
                + " value="
                + currentMotor.commandValue());
      }
      helper.assertTrue(
          currentMotor.isLinked() && currentMotor.commandMode() == command(phase).kind(),
          "Engineered PAX motor lost its active command or span");
      observerSeen[0] |= currentObserver.getPower() > 0;
      if (currentMotor.isDriving()) sightings.merge(phase, 1, Integer::sum);
      if (currentWinch.cycles == 1 && ++boardingTicks[0] == 20) {
        for (var required :
            List.of(
                ShuttleWinchEntity.Phase.LIFTING,
                ShuttleWinchEntity.Phase.FAULT,
                ShuttleWinchEntity.Phase.OUTBOUND,
                ShuttleWinchEntity.Phase.RETURNING,
                ShuttleWinchEntity.Phase.CAPTURING,
                ShuttleWinchEntity.Phase.BOARDING))
          helper.assertTrue(
              sightings.getOrDefault(required, 0) > 2,
              "Engineered motor never reported a PAX train in " + required);
        helper.assertTrue(observerSeen[0], "Engineered observer missed the PAX train");
        helper.assertTrue(
            changes.contains(ShuttleWinchEntity.Phase.BOARDING),
            "Engineered PAX probe did not reach final boarding");
        System.out.println(
            prefix
                + "_COMPLETE cycles="
                + currentWinch.cycles
                + " observer=true sightings="
                + sightings);
      }
    };
  }

  private static MotorStep command(ShuttleWinchEntity.Phase phase) {
    return switch (phase) {
      // Oppose the physical carriage and final catch with reverse launch power.
      case LIFTING, CAPTURING ->
          new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.BACKWARD, 100);
      case FAULT, BOARDING -> new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.FORWARD, 100);
      case OUTBOUND -> new MotorStep(MotorStep.Kind.STOP, MotorStep.Heading.FORWARD, 0);
      case RETURNING -> new MotorStep(MotorStep.Kind.TRIM, MotorStep.Heading.ROLLING, 1);
    };
  }
}
