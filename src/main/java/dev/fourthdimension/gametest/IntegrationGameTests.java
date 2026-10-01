package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.integration.create.RideDisplaySource;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class IntegrationGameTests {
  @GameTest(template = "empty", timeoutTicks = 80)
  public static void unavailableRailKeepsJunctionTopologyAndOrdinaryEndpoints(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var a = new BlockPos(700, 130, -500);
    var b = a.offset(20, 0, 0);
    var c = b.offset(64, 0, 0);
    var d = c.offset(80, 0, 0);
    var e = d.offset(20, 0, 0);
    level.setChunkForced(a.getX() >> 4, a.getZ() >> 4, true);
    level.setChunkForced(b.getX() >> 4, b.getZ() >> 4, true);
    var graph = new dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraph();
    var rails = new ArrayList<FoundationData.Rail>();
    for (var pair : List.of(List.of(a, b), List.of(b, c), List.of(d, e))) {
      var from = pair.getFirst();
      var to = pair.getLast();
      graph.addEdge(
          dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge.straight(
              from, to, from.getCenter(), to.getCenter()));
      if (!from.equals(d)) {
        var rail =
            new FoundationData.Rail(
                from,
                to,
                TrackDesign.ARROW.family,
                new RotationProfile(
                    List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(64, 0))),
                UUID.randomUUID(),
                TrackDesign.ARROW.variant,
                0,
                0,
                false);
        rails.add(rail);
      }
    }
    var data = FoundationData.get(level);
    data.rails.addAll(rails);
    helper.runAtTickTime(
        20,
        () -> {
          helper.assertTrue(
              TrackGeometry.ticking(level, a)
                  && TrackGeometry.ticking(level, b)
                  && !TrackGeometry.ticking(level, c),
              "Endpoint fixture availability is wrong");
          var refs =
              TrackEndpointAvailability.sample(
                  level,
                  graph,
                  Set.of(0, 1, 2),
                  dev.silvergold.simulatedcoasters.track.graph.CoasterPathTraceEndpoints
                      ::pathOpenEndRefs);
          helper.assertTrue(
              refs.size() == 3 && refs.stream().filter(r -> r.edgeIndex() == 2).count() == 2,
              "Inactive 4D rail changed ordinary endpoints");
          helper.assertTrue(
              refs.stream()
                  .filter(r -> r.edgeIndex() != 2)
                  .allMatch(r -> r.edgeIndex() == 0 && r.atFrom()),
              "Missing section created a false junction endpoint");
          System.out.println(
              "FD_ENDPOINT_AVAILABILITY inactiveRefs=3 ordinaryRefs=2 falseJunction=false");
          level.setChunkForced(c.getX() >> 4, c.getZ() >> 4, true);
        });
    helper.runAtTickTime(
        40,
        () -> {
          helper.assertTrue(
              TrackGeometry.ticking(level, c), "Endpoint fixture did not resume ticking");
          var expected =
              dev.silvergold.simulatedcoasters.track.graph.CoasterPathTraceEndpoints
                  .pathOpenEndRefs(level, graph, Set.of(0, 1, 2));
          var actual =
              TrackEndpointAvailability.sample(
                  level,
                  graph,
                  Set.of(0, 1, 2),
                  dev.silvergold.simulatedcoasters.track.graph.CoasterPathTraceEndpoints
                      ::pathOpenEndRefs);
          helper.assertTrue(
              actual.size() == 4 && new HashSet<>(actual).equals(new HashSet<>(expected)),
              "Active endpoint sampling differs from native topology");
          data.rails.removeAll(rails);
          for (var pos : List.of(a, b, c))
            level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, false);
          System.out.println("FD_ENDPOINT_AVAILABILITY restoredRefs=4 nativeMatch=true");
          helper.succeed();
        });
  }

  @GameTest(template = "empty")
  public static void missingProvidersPreserveMechanicalData(GameTestHelper helper) {
    var data = new FoundationData();
    var rail =
        new FoundationData.Rail(
            BlockPos.ZERO,
            new BlockPos(0, 0, 36),
            TrackDesign.ARROW.family,
            new RotationProfile(
                List.of(
                    new RotationProfile.Key(0, 0, 0), new RotationProfile.Key(36, 2 * Math.PI, 0))),
            UUID.randomUUID(),
            TrackDesign.ARROW.variant,
            1234,
            7,
            true,
            "missing:functional");
    rail.appearance = new TrackAppearance("missing:appearance", "missing:style");
    data.rails.add(rail);
    var saved = data.save(new CompoundTag(), helper.getLevel().registryAccess());
    var loaded = FoundationData.load(saved, helper.getLevel().registryAccess());
    var restored = loaded.rails.getFirst();
    helper.assertTrue(
        saved.equals(loaded.save(new CompoundTag(), helper.getLevel().registryAccess())),
        "Provider removal changed persisted identity, profile, or revision");
    helper.assertTrue(
        !TrackProviders.available(restored)
            && TrackGeometry.resolve(helper.getLevel(), restored) == null,
        "Missing functional provider acquired native geometry");
    helper.assertTrue(
        restored.appearance.effective().equals(TrackAppearance.ORIGINAL),
        "Missing appearance failed to select original rendering");
    for (var component : TrackAppearance.Component.values())
      helper.assertTrue(
          TrackProviders.ORIGINAL.equals(restored.appearance.renderOwners().get(component)),
          "Fallback component lost its render owner");
    // Schema 4 has only functional provider identity. Migration must preserve its mechanics.
    var legacy = saved.copy();
    legacy.putInt("schema", 4);
    var oldRail = legacy.getList("rails", Tag.TAG_COMPOUND).getCompound(0);
    oldRail.putString("provider", TrackProviders.ORIGINAL);
    oldRail.remove("appearance_provider");
    oldRail.remove("appearance_style");
    var migrated = FoundationData.load(legacy, helper.getLevel().registryAccess()).rails.getFirst();
    helper.assertTrue(
        TrackProviders.available(migrated)
            && migrated.profile.keys().equals(rail.profile.keys())
            && migrated.id.equals(rail.id)
            && migrated.geometryHash == 1234,
        "Legacy migration changed functional profile or identity");
    var invalid = saved.copy();
    invalid.getList("rails", Tag.TAG_COMPOUND).getCompound(0).putString("provider", "bad identity");
    EditorGameTests.rejects(
        helper,
        () -> FoundationData.load(invalid, helper.getLevel().registryAccess()),
        "Malformed provider identity accepted");
    helper.succeed();
  }

  @GameTest(template = "empty")
  public static void absentOptionalModsAndTelemetryRoundTrip(GameTestHelper helper) {
    helper.assertTrue(
        OptionalIntegrations.load(
                "fourthdimension_absent_test",
                Set.of("1"),
                () -> {
                  throw new AssertionError("Absent optional adapter was resolved");
                })
            .isEmpty(),
        "Missing mod reported supported");
    helper.assertTrue(
        OptionalIntegrations.load(
                "create",
                Set.of("unsupported-test"),
                () -> {
                  throw new AssertionError("Unsupported optional adapter was resolved");
                })
            .isEmpty(),
        "Unsupported version reported supported");
    var ride =
        new RideTelemetry(
            true,
            "pax",
            4,
            2,
            16,
            true,
            false,
            true,
            false,
            false,
            "Shuttle is lifting",
            "",
            "LIFTING");
    helper.assertTrue(ride.equals(RideTelemetry.load(ride.save())), "Telemetry packet lost fields");
    helper.assertTrue(
        new RideDisplaySource(RideDisplaySource.Field.OCCUPANCY)
            .text(ride)
            .getString()
            .equals("2 / 16"),
        "Display Link occupancy differs from consist telemetry");
    helper.assertTrue(
        new RideDisplaySource(RideDisplaySource.Field.DISPATCH)
            .text(ride)
            .getString()
            .equals("Shuttle is lifting"),
        "Display Link did not report dispatch denial");
    helper.succeed();
  }
}
