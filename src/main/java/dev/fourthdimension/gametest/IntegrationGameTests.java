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
