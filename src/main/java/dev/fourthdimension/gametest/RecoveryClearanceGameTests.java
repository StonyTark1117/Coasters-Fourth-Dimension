package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.vehicle.Boarding;
import dev.fourthdimension.vehicle.RecoveryExamples;
import dev.fourthdimension.vehicle.RecoveryOperations;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Runtime proof that recovery motion cannot leave the certified rider-mesh envelope. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class RecoveryClearanceGameTests {
  private RecoveryClearanceGameTests() {}

  @GameTest(
      template = "empty",
      templateNamespace = "fourthdimension_fd402",
      timeoutTicks = 180)
  public static void riderEquipmentInterlocksRecoveryMotion(GameTestHelper helper) {
    var level = helper.getLevel();
    var origin = new BlockPos(-700, 180, 300);
    var forcedChunks = new ArrayList<ChunkPos>();
    for (int x = (origin.getX() - 8) >> 4; x <= (origin.getX() + 8) >> 4; x++)
      for (int z = (origin.getZ() - 4) >> 4; z <= (origin.getZ() + 52) >> 4; z++) {
        var chunk = new ChunkPos(x, z);
        if (!level.getForcedChunks().contains(chunk.toLong())) {
          level.setChunkForced(x, z, true);
          forcedChunks.add(chunk);
        }
      }
    var example = RecoveryExamples.create(level, origin, TrackDesign.ARROW);
    var car = example.car();
    var container = SubLevelContainer.getContainer(level);
    var operator =
        player(
            level,
            "fdfdfdfd-0000-0000-0000-000000000410",
            "FDClearanceOperator");
    var rider =
        player(level, "fdfdfdfd-0000-0000-0000-000000000411", "FDClearanceRider");

    helper.runAtTickTime(
        80,
        () -> {
          helper.assertTrue(Boarding.ready(level, car), "Clearance fixture did not align");
          var carrier = car.carriers.getFirst();
          var body = container.getSubLevel(carrier.body);
          var seat = body.getPlot().getCenterBlock().offset(carrier.seats.getFirst());
          rider.setPos(seat.getX(), seat.getY(), seat.getZ());
          SeatBlock.sitDown(level, seat, rider);
          helper.assertTrue(rider.isPassenger(), "Clearance rider did not mount the native seat");
          VehicleOperations.restraints(level, car, true);
          VehicleOperations.retention(level, car, true);

          var center = CoasterCartResolve.cartCenterWorld(example.bearing());
          operator.setPos(center.x + 1, center.y + 1, center.z);
          RecoveryOperations.select(operator, example.bearing());
          RecoveryOperations.attach(operator, car);
        });

    helper.runAtTickTime(
        110,
        () -> {
          helper.assertTrue(
              car.carriers.stream().anyMatch(c -> c.occupants.contains(rider.getUUID())),
              "Clearance interlock did not observe the live mounted player");

          rider.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TRIDENT));
          rejectClearance(helper, level, car, "Held trident did not reject recovery start");

          rider.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
          rider.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
          RecoveryOperations.start(level, car);
          helper.assertTrue(!car.stopping && car.fault.isEmpty(), "Vanilla armor was not admitted");

          rider.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DRAGON_HEAD));
          RecoveryOperations.tick(level, .05);
          helper.assertTrue(
              car.stopping && RecoveryOperations.RIDER_CLEARANCE_FAULT.equals(car.fault),
              "Unsafe head item did not retain and brake active recovery");

          rider.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
          RecoveryOperations.start(level, car);
          helper.assertTrue(
              !car.stopping && car.fault.isEmpty(),
              "Explicit resume did not clear the corrected rider equipment fault");
          RecoveryOperations.brake(level, car);
          RecoveryOperations.detach(level, car);
          rider.stopRiding();
          level.removePlayerImmediately(
              rider, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
          level.removePlayerImmediately(
              operator, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
          for (var chunk : forcedChunks) level.setChunkForced(chunk.x, chunk.z, false);
          helper.succeed();
        });
  }

  private static ServerPlayer player(
      net.minecraft.server.level.ServerLevel level, String id, String name) {
    var profile = new GameProfile(UUID.fromString(id), name);
    var player =
        new ServerPlayer(
            level.getServer(),
            level,
            profile,
            net.minecraft.server.level.ClientInformation.createDefault());
    player.connection = FakePlayerFactory.get(level, profile).connection;
    player.setGameMode(GameType.CREATIVE);
    level.addNewPlayer(player);
    return player;
  }

  private static void rejectClearance(
      GameTestHelper helper, net.minecraft.server.level.ServerLevel level, FoundationData.Vehicle car,
      String message) {
    try {
      RecoveryOperations.start(level, car);
      helper.assertTrue(false, message);
    } catch (IllegalArgumentException expected) {
      helper.assertTrue(
          RecoveryOperations.RIDER_CLEARANCE_FAULT.equals(expected.getMessage()),
          "Recovery start rejected for the wrong reason: " + expected.getMessage());
    }
  }
}
