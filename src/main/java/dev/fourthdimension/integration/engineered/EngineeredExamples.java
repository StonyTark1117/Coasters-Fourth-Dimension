package dev.fourthdimension.integration.engineered;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.jwaterfall.coastersengineered.CoastersEngineeredRegistry;
import dev.jwaterfall.coastersengineered.controller.*;
import dev.jwaterfall.coastersengineered.observer.CoasterObserverBlockEntity;
import dev.jwaterfall.coastersengineered.span.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Loaded only with the inspected Engineered 1.1.1. Original layout, upstream equipment. */
public final class EngineeredExamples {
  public record Example(
      FoundationData.Vehicle vehicle,
      BlockPos station,
      LinearMotorControllerBlockEntity launch,
      CoasterObserverBlockEntity observer) {}

  private EngineeredExamples() {}

  public static void register(RegisterCommandsEvent event) {
    var demo = Commands.literal("engineered");
    for (var design : List.of(TrackDesign.ARROW, TrackDesign.LEGACY, TrackDesign.FREE_SPIN))
      demo.then(
          Commands.literal(design.name().toLowerCase(Locale.ROOT))
              .executes(
                  ctx -> {
                    try {
                      create(
                          ctx.getSource().getLevel(),
                          BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12),
                          design);
                      ctx.getSource()
                          .sendSuccess(
                              () ->
                                  Component.literal(
                                      "Engineered demonstration built. Close restraints, then pulse"
                                          + " the station rear input. That pulse advances the"
                                          + " nearby motor controller; its falling edge requests"
                                          + " validated dispatch. Power the rear input again after"
                                          + " departure to capture the return. Reset the first"
                                          + " controller before another cycle."),
                              false);
                      return 1;
                    } catch (IllegalArgumentException failure) {
                      ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                      return 0;
                    }
                  }));
    event
        .getDispatcher()
        .register(
            Commands.literal("fd")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("demo").then(demo)));
  }

  public static Example create(ServerLevel level, BlockPos origin, TrackDesign design) {
    if (design != TrackDesign.ARROW
        && design != TrackDesign.LEGACY
        && design != TrackDesign.FREE_SPIN)
      throw new IllegalArgumentException(
          "This demonstration supports Arrow, legacy, and Free Spin");
    var before = Set.copyOf(FoundationData.get(level).rails);
    var v = ExampleLayouts.create(level, origin, design, false);
    var rails = FoundationData.get(level).rails.stream().filter(r -> !before.contains(r)).toList();
    var station = origin.offset(-6, 0, 20);
    int launchSpeed = design == TrackDesign.ARROW ? 4 : 12;
    int trimSpeed = design == TrackDesign.ARROW ? 2 : 8;
    LinearMotorControllerBlockEntity launch = null;
    for (int i = 0; i < rails.size(); i++) {
      var rail = rails.get(i);
      var edge = TrackGeometry.resolve(level, rail);
      var span = TrackSpanBehaviour.at(level, rail.from);
      if (span == null
          || !span.setSpan(
              new TrackSpan(
                  List.of(
                      new SpanSegment(
                          edge.from(),
                          edge.to(),
                          edge.from().equals(rail.from) ? 0 : 1,
                          edge.from().equals(rail.from) ? 1 : 0)))))
        throw new IllegalArgumentException("Engineered rejected the demonstration motor span");
      var pos = i == 0 ? station.offset(0, 0, 2) : rail.from.offset(0, -2, 0);
      level.setBlockAndUpdate(
          pos, CoastersEngineeredRegistry.LINEAR_MOTOR_CONTROLLER.get().defaultBlockState());
      var controller = (LinearMotorControllerBlockEntity) level.getBlockEntity(pos);
      controller.bindTo(rail.from);
      if (i == 0) {
        controller.setSteps(
            List.of(
                new MotorStep(MotorStep.Kind.STOP, MotorStep.Heading.FORWARD, 12),
                new MotorStep(MotorStep.Kind.LAUNCH, MotorStep.Heading.FORWARD, launchSpeed),
                new MotorStep(MotorStep.Kind.COAST)));
        launch = controller;
      } else
        controller.setSteps(
            List.of(
                new MotorStep(
                    i == 4 ? MotorStep.Kind.TRIM : MotorStep.Kind.LAUNCH,
                    MotorStep.Heading.FORWARD,
                    i == 4 ? trimSpeed : launchSpeed)));
    }
    var observerPos = station.offset(0, 0, -2);
    level.setBlockAndUpdate(
        observerPos, CoastersEngineeredRegistry.COASTER_OBSERVER.get().defaultBlockState());
    var observer = (CoasterObserverBlockEntity) level.getBlockEntity(observerPos);
    var first = TrackGeometry.resolve(level, rails.getFirst());
    observer.watch(SpanSegment.point(first, .7));
    return new Example(v, station, launch, observer);
  }
}
