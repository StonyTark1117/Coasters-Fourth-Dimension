package dev.fourthdimension.gametest;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class FoundationGameTests {
  @GameTest(template = "empty", timeoutTicks = 400)
  public static void nativeCarriersAndEligibility(GameTestHelper helper) {
    // Vanilla scatters GameTests up to 15 million blocks from spawn. Pin this
    // native-physics fixture near origin and test far-coordinate behavior separately.
    var level = helper.getLevel();
    var from = new BlockPos(16, 80, 16);
    for (int x = 0; x <= 2; x++) for (int z = 0; z <= 4; z++) level.setChunkForced(x, z, true);
    var vehicle = PrototypeFixture.create(level, from, Family.ARROW);
    var container = SubLevelContainer.getContainer(level);
    var driving = new java.util.concurrent.atomic.AtomicBoolean(false);
    helper.onEachTick(
        () -> {
          if (driving.get()) {
            var chassis = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
            var pipeline = container.physicsSystem().getPipeline();
            pipeline.addLinearAndAngularVelocity(
                chassis,
                new Vector3d(0, 0, 3).sub(pipeline.getLinearVelocity(chassis, new Vector3d())),
                new Vector3d());
            for (var c : vehicle.carriers) {
              var child = (ServerSubLevel) container.getSubLevel(c.body);
              pipeline.addLinearAndAngularVelocity(
                  child,
                  new Vector3d(0, 0, 3).sub(pipeline.getLinearVelocity(child, new Vector3d())),
                  new Vector3d());
            }
          }
        });
    helper.runAtTickTime(
        30,
        () -> {
          var data = FoundationData.get(level);
          var edge =
              CoasterPathGraphManager.get(level).edges().stream()
                  .filter(
                      e ->
                          (e.from().equals(vehicle.boundFrom) && e.to().equals(vehicle.boundTo)
                              || e.to().equals(vehicle.boundFrom)
                                  && e.from().equals(vehicle.boundTo)))
                  .findFirst()
                  .orElseThrow();
          var chassis = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
          var hit =
              CoasterPathTrackFrame.closestHitOnEdge(level, edge, from.getCenter().add(0, 0, 5));
          helper.assertTrue(
              CoasterCartRailSnapEligibility.filterHitForBearing(chassis, level, hit) != null,
              "Matching family rejected");
          var wrong = new FoundationData.Vehicle(vehicle.chassis, Family.ZACSPIN);
          data.vehicles.put(vehicle.chassis, wrong);
          helper.assertTrue(
              CoasterCartRailSnapEligibility.filterHitForBearing(chassis, level, hit) == null,
              "Wrong-family bypass");
          CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis, level, hit);
          helper.assertTrue(
              CoasterCartTrackSnap.isBearingDisengaged(chassis), "Initial snap bypass");
          data.vehicles.put(vehicle.chassis, vehicle);
          var copy =
              FoundationData.load(
                  data.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
          helper.assertTrue(
              copy.vehicles.get(vehicle.chassis).carriers.size() == 2, "Carrier persistence lost");
          driving.set(true);
        });
    helper.runAtTickTime(
        230,
        () -> {
          driving.set(false);
          var debugBody = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
          System.out.println(
              "FD_NATIVE at230 position="
                  + debugBody.logicalPose().position()
                  + " from="
                  + from
                  + " angles="
                  + vehicle.carriers.stream().map(c -> c.angle).toList()
                  + " velocity="
                  + container
                      .physicsSystem()
                      .getPipeline()
                      .getLinearVelocity(debugBody, new Vector3d())
                  + " disengaged="
                  + CoasterCartTrackSnap.isBearingDisengaged(debugBody));
          helper.assertTrue(
              vehicle.carriers.stream().allMatch(c -> c.angle > 2 * Math.PI),
              "Controlled full turn was erased or motor failed");
          vehicle.carriers.forEach(c -> c.capture = true);
          var chassis = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
          var pipeline = container.physicsSystem().getPipeline();
          pipeline.addLinearAndAngularVelocity(
              chassis,
              pipeline.getLinearVelocity(chassis, new Vector3d()).negate(),
              new Vector3d());
        });
    helper.runAtTickTime(
        350,
        () -> {
          for (var c : vehicle.carriers) {
            helper.assertTrue(
                Math.abs(Math.sin(c.angle)) < .05 && Math.abs(c.omega) < .1,
                "Carrier failed smooth capture");
            var body = (ServerSubLevel) container.getSubLevel(c.body);
            helper.assertTrue(body != null && !body.isRemoved(), "Carrier body disappeared");
            container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
          }
          container.removeSubLevel(
              container.getSubLevel(vehicle.chassis), SubLevelRemovalReason.REMOVED);
          FoundationData.get(level).vehicles.remove(vehicle.chassis);
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 300)
  public static void nativeFreeGroupsOrdinaryAccessAndSave(GameTestHelper helper) {
    var level = helper.getLevel();
    var from = new BlockPos(64, 80, 16);
    for (int x = 3; x <= 5; x++) for (int z = 0; z <= 4; z++) level.setChunkForced(x, z, true);
    var vehicle = PrototypeFixture.create(level, from, Family.ZACSPIN);
    var container = SubLevelContainer.getContainer(level);
    var pipeline = container.physicsSystem().getPipeline();
    var initial = new double[2];
    var maximumSeparation = new double[1];
    helper.onEachTick(
        () ->
            maximumSeparation[0] =
                Math.max(
                    maximumSeparation[0],
                    Math.abs(
                        vehicle.carriers.getFirst().angle - vehicle.carriers.getLast().angle)));
    var ordinaryId = new java.util.concurrent.atomic.AtomicReference<java.util.UUID>();
    var ordinaryStart = new Vector3d();
    helper.runAtTickTime(
        30,
        () -> {
          var data = FoundationData.get(level);
          var chassis = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
          var edge =
              CoasterPathGraphManager.get(level).edges().stream()
                  .filter(
                      e ->
                          data.rails.stream()
                              .anyMatch(
                                  r -> r.family() == Family.ZACSPIN && r.matches(e.from(), e.to())))
                  .findFirst()
                  .orElseThrow();
          var hit =
              CoasterPathTrackFrame.closestHitOnEdge(level, edge, from.getCenter().add(0, 0, 5));
          data.vehicles.remove(vehicle.chassis);
          helper.assertTrue(
              CoasterCartRailSnapEligibility.filterHitForBearing(chassis, level, hit) == null,
              "Unbound assembly downgraded to ordinary");
          var ordinary =
              CoasterCartSpawner.spawnMinimalContraption(
                  level,
                  from.getCenter().add(0, .3125, 20),
                  new org.joml.Quaterniond(),
                  null,
                  false);
          ordinaryId.set(ordinary.getUniqueId());
          ordinaryStart.set(ordinary.logicalPose().position());
          helper.assertTrue(
              CoasterCartRailSnapEligibility.filterHitForBearing(ordinary, level, hit) != null,
              "Fitting ordinary bearing rejected");
          pipeline.addLinearAndAngularVelocity(ordinary, new Vector3d(0, 0, 3), new Vector3d());
          data.vehicles.put(vehicle.chassis, vehicle);
          for (int i = 0; i < 2; i++) {
            var c = vehicle.carriers.get(i);
            initial[i] = c.angle;
            pipeline.addLinearAndAngularVelocity(
                (ServerSubLevel) container.getSubLevel(c.body),
                new Vector3d(),
                new Vector3d(i == 0 ? 3 : -2, 0, 0));
          }
        });
    helper.runAtTickTime(
        55,
        () -> {
          var ordinary = (ServerSubLevel) container.getSubLevel(ordinaryId.get());
          helper.assertTrue(
              ordinary.logicalPose().position().distance(ordinaryStart) > .5,
              "Ordinary bearing failed to traverse");
          helper.assertTrue(
              !FoundationData.get(level).vehicles.containsKey(ordinaryId.get()),
              "Ordinary cart received carrier behavior");
          container.removeSubLevel(ordinary, SubLevelRemovalReason.REMOVED);
          helper.assertTrue(
              Math.abs(vehicle.carriers.getFirst().angle - initial[0]) > .1,
              "Free carrier did not move");
          System.out.println(
              "FD_FREE angles="
                  + vehicle.carriers.stream().map(c -> c.angle).toList()
                  + " maximum separation="
                  + maximumSeparation[0]);
          helper.assertTrue(maximumSeparation[0] > .1, "Independent carriers coupled together");
          var data = FoundationData.get(level);
          var tag = data.save(new CompoundTag(), level.registryAccess());
          var copy = FoundationData.load(tag, level.registryAccess());
          helper.assertTrue(
              copy.save(new CompoundTag(), level.registryAccess()).equals(tag),
              "Snapshot/save round trip changed state");
          var bad = tag.copy();
          bad.putInt("schema", 99);
          boolean rejected = false;
          try {
            FoundationData.load(bad, level.registryAccess());
          } catch (IllegalArgumentException expected) {
            rejected = true;
          }
          helper.assertTrue(rejected, "Unknown schema silently downgraded");
          vehicle.carriers.forEach(c -> c.capture = true);
        });
    helper.runAtTickTime(
        250,
        () -> {
          for (var c : vehicle.carriers) {
            helper.assertTrue(
                Math.abs(Math.sin(c.angle)) < .05 && Math.abs(c.omega) < .1,
                "Free group capture failed");
            container.removeSubLevel(container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
          }
          container.removeSubLevel(
              container.getSubLevel(vehicle.chassis), SubLevelRemovalReason.REMOVED);
          FoundationData.get(level).vehicles.remove(vehicle.chassis);
          helper.succeed();
        });
  }
}
