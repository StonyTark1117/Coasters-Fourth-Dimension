package dev.fourthdimension.gametest;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import java.io.IOException;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Regression for the recorded schema-3 integrated world, not a synthetic legacy tag. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class HistoricalMigrationGameTests {
  private static final String SCHEMA3_FIXTURE =
      "/data/fourthdimension/fixtures/schema3-foundation.nbt.gz.b64";

  @GameTest(template = "empty", timeoutTicks = 80)
  public static void recordedSchema3IntegratedWorldLoadsAndUpgrades(GameTestHelper helper) {
    var source = readHistoricalFixture();
    helper.assertTrue(source.getInt("schema") == 3, "Historical fixture schema changed");
    var sourceRails = source.getList("rails", Tag.TAG_COMPOUND);
    var sourceVehicles = source.getList("vehicles", Tag.TAG_COMPOUND);
    helper.assertTrue(
        sourceRails.size() == 24 && sourceVehicles.size() == 3,
        "Historical fixture no longer has its recorded 24 rails and 3 chassis");
    var sourceChassis =
        sourceVehicles.stream().map(tag -> ((CompoundTag) tag).getUUID("chassis")).collect(Collectors.toSet());
    var sourceRailIds =
        sourceRails.stream().map(tag -> ((CompoundTag) tag).getUUID("id")).collect(Collectors.toSet());
    var sourceProfiles =
        sourceRails.stream()
            .collect(
                Collectors.toMap(
                    tag -> ((CompoundTag) tag).getUUID("id"),
                    tag -> readProfile((CompoundTag) tag)));

    var migrated = FoundationData.load(source, helper.getLevel().registryAccess());
    helper.assertTrue(
        migrated.vehicles.keySet().equals(sourceChassis)
            && migrated.rails.stream().map(rail -> rail.id).collect(Collectors.toSet()).equals(sourceRailIds),
        "Schema-3 migration changed historic chassis or rail identities");
    helper.assertTrue(
        migrated.rails.stream()
            .collect(Collectors.toMap(rail -> rail.id, rail -> rail.profile))
            .equals(sourceProfiles),
        "Schema-3 migration changed a historic rotation profile");
    helper.assertTrue(
        migrated.vehicles.values().stream()
            .allMatch(vehicle -> vehicle.recoveryCart == null && !vehicle.recoveryCartRemoved),
        "Schema-3 migration invented recovery state");
    helper.assertTrue(
        migrated.vehicles.values().stream().map(vehicle -> vehicle.family).collect(Collectors.toSet())
            .equals(Set.of(Family.ARROW, Family.ZACSPIN, Family.FREE_SPIN)),
        "Schema-3 migration changed historic functional families");

    var upgraded = migrated.save(new CompoundTag(), helper.getLevel().registryAccess());
    helper.assertTrue(
        upgraded.getInt("schema") == FoundationData.SCHEMA,
        "Migrated historical fixture did not write the current schema");
    var reopened = FoundationData.load(upgraded, helper.getLevel().registryAccess());
    helper.assertTrue(
        reopened.vehicles.keySet().equals(sourceChassis)
            && reopened.rails.stream().map(rail -> rail.id).collect(Collectors.toSet()).equals(sourceRailIds)
            && reopened.rails.stream()
                .collect(Collectors.toMap(rail -> rail.id, rail -> rail.profile))
                .equals(sourceProfiles)
            && reopened.vehicles.values().stream()
                .allMatch(vehicle -> vehicle.recoveryCart == null && !vehicle.recoveryCartRemoved),
        "Current-schema reopen changed historic migration state or rotation profile");

    // A historical Fourth Dimension record may be upgraded, but an external functional
    // track provider cannot be guessed from an Arrow-looking identifier during migration.
    var foreignProvider = source.copy();
    foreignProvider
        .getList("rails", Tag.TAG_COMPOUND)
        .getCompound(0)
        .putString("provider", "coasterssimulatedextratypes:arrow_4d");
    EditorGameTests.rejects(
        helper,
        () -> FoundationData.load(foreignProvider, helper.getLevel().registryAccess()),
        "Foreign functional provider was silently converted during migration");
    System.out.println(
        "FD903_HISTORICAL schema3Rails=24 chassis=3 providerConversionRejected=true");
    helper.succeed();
  }

  private static RotationProfile readProfile(CompoundTag rail) {
    return new RotationProfile(
        rail.getList("profile", Tag.TAG_COMPOUND).stream()
            .map(
                raw -> {
                  var key = (CompoundTag) raw;
                  return new RotationProfile.Key(
                      key.getDouble("distance"), key.getDouble("angle"), key.getDouble("slope"));
                })
            .toList());
  }

  private static CompoundTag readHistoricalFixture() {
    try (var encoded = HistoricalMigrationGameTests.class.getResourceAsStream(SCHEMA3_FIXTURE)) {
      if (encoded == null) throw new IllegalStateException("Missing schema-3 historical fixture resource");
      try (var decoded = Base64.getMimeDecoder().wrap(encoded)) {
        return NbtIo.readCompressed(decoded, NbtAccounter.create(1_048_576)).getCompound("data");
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Could not read schema-3 historical fixture", exception);
    }
  }

  private HistoricalMigrationGameTests() {}
}
