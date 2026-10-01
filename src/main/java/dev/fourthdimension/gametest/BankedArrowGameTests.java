package dev.fourthdimension.gametest;

import com.google.gson.Gson;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** Native bank frames and loaded controlled motion, independent of optional decorative models. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class BankedArrowGameTests {
  private record Frame(
      double distance, double[] center, double[] right, double[] up, double[] forward) {}

  private record Section(
      String id, double length, List<Frame> rails, List<Frame> spines, List<Frame> ties) {}

  private static double[] vector(Vec3 v) {
    return new double[] {v.x, v.y, v.z};
  }

  private static Frame frame(GameTestHelper helper, CoasterPathEdge edge, double distance) {
    var table = TrackGeometry.table(helper.getLevel(), edge);
    double t = table.parameterAt(distance);
    var center = CoasterPathGraphManager.sampleEdgePosition(helper.getLevel(), edge, t);
    var basis =
        CoasterPathTrackFrame.trackBasisAtHit(
            helper.getLevel(), new CoasterPathTrackFrame.GraphHit(center, edge, t));
    return new Frame(
        distance,
        vector(center),
        vector(basis.right()),
        vector(basis.up()),
        vector(basis.forward()));
  }

  @GameTest(template = "empty", timeoutTicks = 2400)
  public static void nativeBankedFramesAndLoadedFullTurns(GameTestHelper helper) {
    var level = helper.getLevel();
    var origin = new BlockPos(-350, 210, -200);
    for (int x = -23; x <= -19; x++)
      for (int z = -14; z <= -8; z++) level.setChunkForced(x, z, true);
    var data = FoundationData.get(level);
    var vehicle = ExampleLayouts.createBankedArrow(level, origin);
    var container = SubLevelContainer.getContainer(level);
    var own =
        data.rails.stream()
            .filter(
                r ->
                    r.from.getY() == origin.getY()
                        && r.from.getX() >= origin.getX()
                        && r.from.getX() <= origin.getX() + 32
                        && r.from.getZ() >= origin.getZ()
                        && r.from.getZ() <= origin.getZ() + 52)
            .toList();
    helper.assertTrue(own.size() == 8, "Bank fixture did not create exactly eight sections");
    Pig[] rider = {null};
    boolean[] dispatched = {false};
    boolean[] powered = {false};
    int[] ticks = {0};
    double[] bankedPeak = {0};
    double[] flatPeak = {0};
    helper.runAtTickTime(
        80,
        () -> {
          var sections = new ArrayList<Section>();
          double maxBank = 0;
          for (var rail : own) {
            helper.assertTrue(rail.valid, "Native bank settings invalidated the initial profile");
            var edge = TrackGeometry.resolve(level, rail);
            var table = TrackGeometry.table(level, edge);
            var rails = new ArrayList<Frame>();
            var spines = new ArrayList<Frame>();
            var ties = new ArrayList<Frame>();
            int count = Math.max(8, (int) Math.ceil(table.length() * 4));
            for (int i = 0; i <= count; i++) {
              var f = frame(helper, edge, table.length() * i / count);
              rails.add(f);
              maxBank = Math.max(maxBank, Math.acos(Math.max(-1, Math.min(1, f.up[1]))));
              helper.assertTrue(
                  Math.abs(new Vec3(f.up[0], f.up[1], f.up[2]).length() - 1) < 1e-6,
                  "Banked native up is not normalized");
            }
            int segments = Math.max(1, (int) Math.ceil(table.length() * 2));
            double step = table.length() / segments;
            for (int i = 0; i < segments; i++) spines.add(frame(helper, edge, (i + .5) * step));
            for (double d = 0; d <= table.length() + 1e-6; d += 2)
              ties.add(frame(helper, edge, Math.min(d, table.length())));
            sections.add(new Section(rail.id.toString(), table.length(), rails, spines, ties));
          }
          var geometryPath =
              net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve("bank-native-frames.json");
          try {
            Files.writeString(geometryPath, new Gson().toJson(sections));
          } catch (IOException failedExport) {
            throw new UncheckedIOException(failedExport);
          }
          System.out.println("FD_BANK geometryPath=" + geometryPath);
          // The native frame API uses one-sided finite differences (dt=2e-4) at
          // endpoints. Allow that small orientation error, but no centerline gap.
          double maxCenterError = 0, maxTangentError = 0, maxUpError = 0;
          for (int i = 0; i < sections.size(); i++) {
            var end = sections.get(i).rails.getLast();
            var start = sections.get((i + 1) % sections.size()).rails.getFirst();
            double centerError =
                new Vec3(end.center[0], end.center[1], end.center[2])
                    .distanceTo(new Vec3(start.center[0], start.center[1], start.center[2]));
            maxCenterError = Math.max(maxCenterError, centerError);
            helper.assertTrue(
                centerError < 1e-6, "Native bank centerline is discontinuous at section " + i);
            double tangentError =
                new Vec3(end.forward[0], end.forward[1], end.forward[2])
                    .distanceTo(new Vec3(start.forward[0], start.forward[1], start.forward[2]));
            helper.assertTrue(
                tangentError < 1e-3,
                "Native bank tangent is discontinuous at section " + i + ": " + tangentError);
            maxTangentError = Math.max(maxTangentError, tangentError);
            double upError =
                new Vec3(end.up[0], end.up[1], end.up[2])
                    .distanceTo(new Vec3(start.up[0], start.up[1], start.up[2]));
            maxUpError = Math.max(maxUpError, upError);
            helper.assertTrue(upError < 1e-3, "Native bank face is discontinuous at section " + i);
          }
          System.out.println(
              "FD_BANK jointErrors center="
                  + maxCenterError
                  + " tangent="
                  + maxTangentError
                  + " up="
                  + maxUpError);
          for (var f : sections.get(2).rails)
            helper.assertTrue(
                Math.acos(Math.max(-1, Math.min(1, f.up[1]))) > Math.toRadians(29),
                "Upper straight lost its bank between the joints");
          System.out.println("FD_BANK maxBankDegrees=" + Math.toDegrees(maxBank));
          helper.assertTrue(
              maxBank > Math.toRadians(29) && maxBank < Math.toRadians(31),
              "Native frame did not retain the declared 30-degree bank: "
                  + Math.toDegrees(maxBank));
          helper.assertTrue(
              Boarding.ready(level, vehicle), "Level bank-demo station was not ready");
          var carrier = vehicle.carriers.getFirst();
          var body = container.getSubLevel(carrier.body);
          rider[0] = EntityType.PIG.create(level);
          rider[0].setNoAi(true);
          level.addFreshEntity(rider[0]);
          SeatBlock.sitDown(
              level, body.getPlot().getCenterBlock().offset(carrier.seats.getFirst()), rider[0]);
          helper.assertTrue(
              rider[0].isPassenger(), "Bank fixture failed native passenger boarding");
          VehicleOperations.restraints(level, vehicle, true);
          VehicleOperations.dispatch(level, vehicle);
          dispatched[0] = true;
        });
    helper.onEachTick(
        () -> {
          ticks[0]++;
          if (!dispatched[0]) return;
          helper.assertTrue(rider[0].isPassenger(), "Banked circuit lost passenger");
          helper.assertTrue(
              vehicle.restraints && vehicle.fault.isEmpty(), "Banked circuit fault/unload");
          helper.assertTrue(
              own.stream().anyMatch(r -> r.valid && r.matches(vehicle.boundFrom, vehicle.boundTo)),
              "Banked circuit lost matching functional support");
          var bound =
              own.stream()
                  .filter(r -> r.matches(vehicle.boundFrom, vehicle.boundTo))
                  .findFirst()
                  .orElseThrow();
          if (ticks[0] % 400 == 0)
            System.out.println(
                "FD_BANK progress tick="
                    + ticks[0]
                    + " section="
                    + own.indexOf(bound)
                    + " stopping="
                    + vehicle.stopping
                    + " ready="
                    + Boarding.ready(level, vehicle)
                    + " peaks="
                    + bankedPeak[0]
                    + ","
                    + flatPeak[0]
                    + " center="
                    + dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve
                        .cartCenterWorld(container.getSubLevel(vehicle.chassis))
                    + " nativeVelocity="
                    + container
                        .physicsSystem()
                        .getPipeline()
                        .getLinearVelocity(
                            (dev.ryanhcode.sable.sublevel.ServerSubLevel)
                                container.getSubLevel(vehicle.chassis),
                            new org.joml.Vector3d()));
          boolean banked =
              bound.from.getZ() == origin.getZ() + 52 && bound.to.getZ() == origin.getZ() + 52;
          for (var carrier : vehicle.carriers) {
            if (banked) bankedPeak[0] = Math.max(bankedPeak[0], carrier.angle);
            else flatPeak[0] = Math.max(flatPeak[0], carrier.angle);
          }
          if (ticks[0] == 260) {
            level.setBlockAndUpdate(
                origin.offset(-6, 0, 21), Blocks.REDSTONE_BLOCK.defaultBlockState());
            powered[0] = true;
          }
          if (!powered[0] || !vehicle.stopping || !Boarding.ready(level, vehicle)) return;
          helper.assertTrue(
              Math.abs(bankedPeak[0] - 2 * Math.PI) < .08
                  && Math.abs(flatPeak[0] - 2 * Math.PI) < .08,
              "Banked or flat full turn missed the native 0.08-radian tolerance");
          VehicleOperations.restraints(level, vehicle, false);
          rider[0].stopRiding();
          helper.assertTrue(!rider[0].isPassenger(), "Banked circuit failed normal unloading");
          rider[0].discard();
          var station = (BoardingStationEntity) level.getBlockEntity(origin.offset(-6, 0, 20));
          helper.assertTrue(station.fault.isEmpty(), "Banked return station rejected capture");
          for (var c : vehicle.carriers)
            container.removeSubLevel(container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
          container.removeSubLevel(
              container.getSubLevel(vehicle.chassis), SubLevelRemovalReason.REMOVED);
          data.vehicles.remove(vehicle.chassis);
          data.rails.removeAll(own);
          for (var pos :
              BlockPos.betweenClosed(origin.offset(-7, -3, -8), origin.offset(39, 13, 60)))
            if (!level.getBlockState(pos).isAir()) level.removeBlock(pos, false);
          dispatched[0] = false;
          System.out.println(
              "FD_BANK loadedTurnPeaks="
                  + bankedPeak[0]
                  + ","
                  + flatPeak[0]
                  + " captured=true unloaded=true");
          helper.succeed();
        });
  }
}
