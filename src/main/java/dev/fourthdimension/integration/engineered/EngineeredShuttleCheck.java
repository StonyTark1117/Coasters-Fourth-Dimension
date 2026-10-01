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
  private static Boolean previousEnergy;

  private EngineeredShuttleCheck() {}

  public static void configureEnergy(boolean begin) {
    if (begin) {
      previousEnergy = CoastersEngineeredConfig.get(CoastersEngineeredConfig.REQUIRE_ENERGY);
      CoastersEngineeredConfig.REQUIRE_ENERGY.set(true);
    } else if (previousEnergy != null) {
      CoastersEngineeredConfig.REQUIRE_ENERGY.set(previousEnergy);
      System.out.println("FD_ENGINEERED_PAX_FE_RESTORED requireEnergy=" + previousEnergy);
      previousEnergy = null;
    }
  }

  public static Runnable attachEnergy(GameTestHelper helper, ShuttleExamples.Example example) {
    return attach(helper, example, false, null, true);
  }

  public static Runnable attach(GameTestHelper helper, ShuttleExamples.Example example) {
    return attach(helper, example, false, null, false);
  }

  public static Runnable attachReload(GameTestHelper helper, ShuttleExamples.Example example) {
    return attachReload(helper, example, ShuttleWinchEntity.Phase.LIFTING);
  }

  public static Runnable attachReload(
      GameTestHelper helper,
      ShuttleExamples.Example example,
      ShuttleWinchEntity.Phase interruptedPhase) {
    return attachReload(helper, example, interruptedPhase, false);
  }

  public static Runnable attachReload(
      GameTestHelper helper,
      ShuttleExamples.Example example,
      ShuttleWinchEntity.Phase interruptedPhase,
      boolean energyRequired) {
    return attach(helper, example, true, interruptedPhase, energyRequired);
  }

  private static Runnable attach(
      GameTestHelper helper,
      ShuttleExamples.Example example,
      boolean reload,
      ShuttleWinchEntity.Phase interruptedPhase,
      boolean energyRequired) {
    var level = helper.getLevel();
    var winch = example.winch();
    var route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
    helper.assertTrue(
        CoastersEngineeredConfig.get(CoastersEngineeredConfig.REQUIRE_ENERGY) == energyRequired,
        "Engineered PAX fixture energy configuration mismatch");
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
    var energy =
        energyRequired
            ? level.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                anchor,
                net.minecraft.core.Direction.DOWN)
            : null;
    if (energyRequired)
      helper.assertTrue(
          energy != null && energy.canReceive() && energy.canExtract(),
          "PAX span did not expose the native FE capability");
    int[] expectedEnergy = {energyRequired && reload ? 12345 + interruptedPhase.ordinal() : 0};
    int[] energyTicks = {0}, restoredEquipment = {0};
    if (energyRequired && reload)
      helper.assertTrue(
          energy.receiveEnergy(expectedEnergy[0], false) == expectedEnergy[0],
          "Could not seed native reload FE balance");
    var checkedMotor = new LinearMotorControllerBlockEntity[] {motor};
    var checkedObserver = new CoasterObserverBlockEntity[] {observer};
    var checkedEnergy = new net.neoforged.neoforge.energy.IEnergyStorage[] {energy};
    var energyStages =
        new EnumMap<ShuttleWinchEntity.Phase, Set<Integer>>(ShuttleWinchEntity.Phase.class);
    System.out.println(
        "FD_ENGINEERED_PAX_PROBE segments="
            + route.segments.size()
            + " energyRequired="
            + energyRequired
            + " cars="
            + example.cars().size());
    String prefix =
        energyRequired
            ? reload ? "FD_ENGINEERED_PAX_FE_PHASE_" + interruptedPhase : "FD_ENGINEERED_PAX_FE"
            : reload ? "FD_ENGINEERED_PAX_PHASE_" + interruptedPhase : "FD_ENGINEERED_PAX";
    if (energyRequired && reload)
      System.out.println(
          prefix
              + "_SEEDED stored="
              + expectedEnergy[0]
              + " capacity="
              + energy.getMaxEnergyStored());
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
      var currentEnergy = energy;
      if (energyRequired && reload) {
        currentEnergy =
            level.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                anchor,
                net.minecraft.core.Direction.DOWN);
        helper.assertTrue(currentEnergy != null, "Reloaded PAX span lost its FE capability");
        if (currentMotor != checkedMotor[0]) {
          helper.assertTrue(
              checkedMotor[0].isRemoved()
                  && checkedObserver[0].isRemoved()
                  && currentObserver != checkedObserver[0]
                  && currentEnergy != checkedEnergy[0],
              "PAX equipment did not reload as fresh native instances");
          sameFields(
              helper,
              "controller",
              checkedMotor[0].saveWithFullMetadata(level.registryAccess()),
              currentMotor.saveWithFullMetadata(level.registryAccess()),
              "Anchor",
              "Sequence",
              "Step");
          sameFields(
              helper,
              "observer",
              checkedObserver[0].saveWithFullMetadata(level.registryAccess()),
              currentObserver.saveWithFullMetadata(level.registryAccess()),
              "Watching");
          helper.assertTrue(
              span.span().equals(TrackSpanBehaviour.at(level, anchor).span()),
              "Native reload changed the complete PAX span");
          helper.assertTrue(
              currentEnergy.getEnergyStored() == expectedEnergy[0],
              "Native reload changed the PAX FE balance");
          restoredEquipment[0]++;
          System.out.println(
              prefix
                  + "_RELOADED stored="
                  + currentEnergy.getEnergyStored()
                  + " freshMotor=true freshObserver=true freshEnergy=true controller=true span=true"
                  + " observer=true");
          checkedMotor[0] = currentMotor;
          checkedObserver[0] = currentObserver;
          checkedEnergy[0] = currentEnergy;
        }
      }
      if (energyRequired) {
        helper.assertTrue(
            CoastersEngineeredConfig.get(CoastersEngineeredConfig.REQUIRE_ENERGY),
            "PAX FE requirement changed during its batch");
        helper.assertTrue(
            currentEnergy.getEnergyStored() == expectedEnergy[0],
            "External PAX motor consumed or regenerated FE: expected="
                + expectedEnergy[0]
                + " actual="
                + currentEnergy.getEnergyStored());
        if (previous[0] != phase) energyTicks[0] = 0;
        int age = energyTicks[0]++;
        if (!reload && (age == 0 || age == 5 || age == 10 || age == 15)) {
          int target =
              age == 5
                  ? energy.getMaxEnergyStored()
                  : age == 15 ? Math.min(12345, energy.getMaxEnergyStored()) : 0;
          int before = energy.getEnergyStored();
          int moved =
              target > before
                  ? energy.receiveEnergy(target - before, false)
                  : energy.extractEnergy(before - target, false);
          helper.assertTrue(
              moved == Math.abs(target - before) && energy.getEnergyStored() == target,
              "Native PAX FE supply transition failed");
          expectedEnergy[0] = target;
          energyStages.computeIfAbsent(phase, p -> new HashSet<>()).add(age);
          System.out.println(
              prefix
                  + "_BUFFER phase="
                  + phase
                  + " phaseTick="
                  + age
                  + " stored="
                  + target
                  + " capacity="
                  + energy.getMaxEnergyStored());
        }
      }
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
        var requiredPhases =
            EnumSet.of(
                ShuttleWinchEntity.Phase.LIFTING,
                ShuttleWinchEntity.Phase.FAULT,
                ShuttleWinchEntity.Phase.OUTBOUND,
                ShuttleWinchEntity.Phase.RETURNING,
                ShuttleWinchEntity.Phase.CAPTURING,
                ShuttleWinchEntity.Phase.BOARDING);
        if (interruptedPhase == ShuttleWinchEntity.Phase.OUTBOUND)
          requiredPhases.remove(ShuttleWinchEntity.Phase.RETURNING);
        for (var required : requiredPhases)
          helper.assertTrue(
              sightings.getOrDefault(required, 0) > 2,
              "Engineered motor never reported a PAX train in " + required);
        if (energyRequired && reload)
          helper.assertTrue(
              restoredEquipment[0] == 1,
              "PAX FE probe did not verify one native equipment restoration");
        if (energyRequired && !reload)
          for (var required : requiredPhases)
            helper.assertTrue(
                energyStages.getOrDefault(required, Set.of()).containsAll(Set.of(0, 5, 10, 15)),
                "Missing empty/full/refill FE stages in " + required);
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

  private static void sameFields(
      GameTestHelper helper,
      String kind,
      net.minecraft.nbt.CompoundTag previous,
      net.minecraft.nbt.CompoundTag current,
      String... fields) {
    for (var field : fields) {
      var key = dev.jwaterfall.coastersengineered.CoastersEngineered.storageKey(field);
      helper.assertTrue(
          previous.contains(key)
              && current.contains(key)
              && Objects.equals(previous.get(key), current.get(key)),
          "Native PAX reload changed " + kind + " " + field);
    }
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
