package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.ShuttleContent;
import dev.fourthdimension.shuttle.ShuttleWinchEntity;
import dev.fourthdimension.station.BoardingStation;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.station.StationContent;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.track.TrackItems;
import dev.fourthdimension.track.TrackPlacement;
import dev.fourthdimension.vehicle.Boarding;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Supplies materials and supports, then uses native survival crafting/right-click paths.
 * Connected-client operation, a player rider and the complete shuttle route have separate gates.
 */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class SurvivalBuildingGameTests {
  private static Item item(String id) {
    return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
  }

  private static Item track(TrackDesign design) {
    return switch (design) {
      case ARROW -> TrackItems.ARROW.get();
      case LEGACY -> TrackItems.LEGACY.get();
      case FREE_SPIN -> TrackItems.FREE_SPIN.get();
      case REVISED -> TrackItems.REVISED.get();
      case PAX -> TrackItems.PAX.get();
    };
  }

  private static Item car(TrackDesign design) {
    return switch (design) {
      case ARROW -> TrackItems.ARROW_CAR.get();
      case LEGACY -> TrackItems.LEGACY_CAR.get();
      case FREE_SPIN -> TrackItems.FREE_SPIN_CAR.get();
      case REVISED -> TrackItems.REVISED_CAR.get();
      case PAX -> TrackItems.PAX_CAR.get();
    };
  }

  private static int count(ServerPlayer player, Item item) {
    return player.getInventory().items.stream()
        .filter(s -> s.is(item))
        .mapToInt(ItemStack::getCount)
        .sum();
  }

  private static void hold(GameTestHelper helper, ServerPlayer player, Item item) {
    var inventory = player.getInventory();
    for (int i = 0; i < inventory.items.size(); i++) {
      var stack = inventory.items.get(i);
      if (item == null ? stack.isEmpty() : stack.is(item)) {
        if (i >= 9) {
          inventory.items.set(i, inventory.items.get(0));
          inventory.items.set(0, stack);
          i = 0;
        }
        inventory.selected = i;
        return;
      }
    }
    helper.fail("Crafted item unavailable: " + item);
  }

  /** Walk the synthetic player into ordinary reach before sending the normal server use action. */
  private static InteractionResult click(ServerPlayer player, BlockPos pos) {
    player.setPos(pos.getX() + 2.5, pos.getY() + 1, pos.getZ() + .5);
    return player.gameMode.useItemOn(
        player,
        player.serverLevel(),
        player.getMainHandItem(),
        InteractionHand.MAIN_HAND,
        new BlockHitResult(pos.getCenter(), Direction.UP, pos, false));
  }

  private static void place(
      GameTestHelper helper, ServerPlayer player, Item item, BlockPos target) {
    var level = player.serverLevel();
    level.setBlockAndUpdate(target.below(), Blocks.STONE.defaultBlockState());
    hold(helper, player, item);
    int before = count(player, item);
    helper.assertTrue(
        click(player, target.below()).consumesAction(), "Native block placement rejected " + item);
    helper.assertTrue(
        count(player, item) == before - 1, "Block placement did not consume one " + item);
  }

  private static void craft(
      GameTestHelper helper,
      ServerPlayer player,
      BlockPos table,
      TeachingContentGameTests.Craft recipe) {
    var menu =
        new CraftingMenu(
            7, player.getInventory(), ContainerLevelAccess.create(player.serverLevel(), table));
    player.containerMenu = menu;
    for (int row = 0; row < recipe.pattern().size(); row++) {
      String symbols = recipe.pattern().get(row);
      for (int column = 0; column < symbols.length(); column++) {
        Item ingredient = item(recipe.ingredients().get(symbols.charAt(column)));
        var stock =
            player.getInventory().items.stream()
                .filter(s -> s.is(ingredient))
                .findFirst()
                .orElseThrow();
        menu.getSlot(1 + row * 3 + column).set(stock.split(1));
      }
    }
    Item output = item("fourthdimension:" + recipe.name());
    helper.assertTrue(menu.stillValid(player), "Crafting table inaccessible");
    helper.assertTrue(
        menu.getSlot(0).getItem().is(output),
        "Native crafting menu has wrong output: " + recipe.name());
    menu.clicked(0, 0, ClickType.PICKUP, player);
    var result = menu.getCarried();
    helper.assertTrue(
        result.is(output) && result.getCount() == recipe.count(),
        "Native result-slot pickup failed: " + recipe.name());
    for (int slot = 1; slot <= 9; slot++)
      helper.assertTrue(
          menu.getSlot(slot).getItem().isEmpty(),
          "Craft did not consume its grid: " + recipe.name());
    helper.assertTrue(
        player
            .getRecipeBook()
            .contains(ResourceLocation.fromNamespaceAndPath("fourthdimension", recipe.name())),
        "Taking the craft did not discover its recipe: " + recipe.name());
    menu.setCarried(ItemStack.EMPTY);
    helper.assertTrue(player.getInventory().add(result), "Craft output did not fit inventory");
    player.closeContainer();
    LogUtils.getLogger()
        .info(
            "FD_SURVIVAL craft={} count={} gridConsumed=true recipeDiscovered=true",
            recipe.name(),
            recipe.count());
  }

  @GameTest(template = "empty", batch = "fd_survival", timeoutTicks = 150)
  public static void nativeSurvivalCraftingAndWorldPlacement(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -58; x <= -48; x++)
      for (int z = -57; z <= -52; z++) level.setChunkForced(x, z, true);
    var profile = new GameProfile(UUID.randomUUID(), "FDSurvivalBuilder");
    var connection = FakePlayerFactory.get(level, profile).connection;
    var player =
        new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
    player.connection =
        connection; // discard only outgoing packets; no unnegotiated client in the player list
    player.setGameMode(GameType.SURVIVAL);
    helper.assertTrue(
        !player.isCreative() && player.mayBuild(), "Fixture is not a survival builder");
    var table = new BlockPos(-930, 132, -880);
    level.setBlockAndUpdate(table, Blocks.CRAFTING_TABLE.defaultBlockState());
    player.setPos(table.getX() + 2, table.getY(), table.getZ());
    var materials =
        Map.ofEntries(
            Map.entry("minecraft:iron_ingot", 96),
            Map.entry("minecraft:copper_ingot", 32),
            Map.entry("minecraft:red_dye", 1),
            Map.entry("minecraft:yellow_dye", 1),
            Map.entry("minecraft:blue_dye", 1),
            Map.entry("minecraft:purple_dye", 1),
            Map.entry("minecraft:green_dye", 1),
            Map.entry("create:red_seat", 10),
            Map.entry("minecraft:iron_bars", 5),
            Map.entry("simulatedcoasters:coaster_cart", 5),
            Map.entry("minecraft:compass", 1),
            Map.entry("create:brass_ingot", 1),
            Map.entry("minecraft:stick", 1),
            Map.entry("minecraft:redstone", 16),
            Map.entry("minecraft:quartz", 2),
            Map.entry("minecraft:comparator", 2),
            Map.entry("minecraft:copper_block", 2),
            Map.entry("create:shaft", 1),
            Map.entry("simulatedcoasters:coaster_anchorpoint", 10));
    materials.forEach(
        (id, quantity) -> {
          helper.assertTrue(
              item(id) != net.minecraft.world.item.Items.AIR, "Missing source material " + id);
          helper.assertTrue(
              player.getInventory().add(new ItemStack(item(id), quantity)),
              "Source materials did not fit");
        });
    for (var recipe : TeachingContentGameTests.crafts()) craft(helper, player, table, recipe);
    craft(
        helper,
        player,
        table,
        TeachingContentGameTests.crafts().stream()
            .filter(c -> c.name().equals("boarding_station"))
            .findFirst()
            .orElseThrow());
    var data = FoundationData.get(level);
    var rails = new ArrayList<FoundationData.Rail>();
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(-900 + design.ordinal() * 24, 132, -880);
      var to = from.offset(0, 0, 32);
      place(helper, player, item("simulatedcoasters:coaster_anchorpoint"), from);
      place(helper, player, item("simulatedcoasters:coaster_anchorpoint"), to);
      helper.assertTrue(
          level.getBlockState(from).is(SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get()),
          "Wrong anchor block");
      hold(helper, player, track(design));
      var stack = player.getMainHandItem();
      int before = count(player, track(design));
      var escrow = stack.split(stack.getCount() - 1);
      helper.assertTrue(
          click(player, from).consumesAction(), "First anchor click failed " + design);
      int railCount = data.rails.size();
      helper.assertTrue(
          click(player, to) == InteractionResult.FAIL,
          "Insufficient survival inventory accepted " + design);
      helper.assertTrue(
          data.rails.size() == railCount && stack.getCount() == 1,
          "Failed track placement changed inventory or rails " + design);
      helper.assertTrue(
          !((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).hasAnchorPeer(to)
              && !((CoasterAnchorpointBlockEntity) level.getBlockEntity(to)).hasAnchorPeer(from),
          "Insufficient-inventory placement left a native graph connection " + design);
      stack.grow(escrow.getCount());
      escrow.setCount(0);
      player.setShiftKeyDown(true);
      helper.assertTrue(
          click(player, from).consumesAction(), "Selection cancellation failed " + design);
      player.setShiftKeyDown(false);
      helper.assertTrue(
          !stack.has(TrackItems.SELECTION.get()), "Cancelled selection persisted " + design);
      helper.assertTrue(
          click(player, from).consumesAction() && click(player, to).consumesAction(),
          "Normal track clicks failed " + design);
      var rail = data.rails.stream().filter(r -> r.matches(from, to)).findFirst().orElseThrow();
      rails.add(rail);
      int cost = (int) Math.ceil(rail.profile.keys().getLast().distance() / 4);
      helper.assertTrue(
          count(player, track(design)) == before - cost && !stack.has(TrackItems.SELECTION.get()),
          "Wrong track cost or unfinished selection " + design);
      helper.assertTrue(
          rail.variant.equals(design.variant), "Survival track lost family " + design);
      LogUtils.getLogger()
          .info(
              "FD_SURVIVAL track={} cost={} insufficientReject=passed selectionCancel=passed",
              design,
              cost);
    }
    hold(helper, player, TrackItems.INDUCER.get());
    helper.assertTrue(
        click(player, rails.get(2).from).consumesAction(), "Normal inducer click failed");
    helper.assertTrue(
        count(player, TrackItems.INDUCER.get()) == 1 && rails.get(2).magnets.size() == 1,
        "Inducer cost incorrect");
    hold(helper, player, TrackItems.DAMPER.get());
    helper.assertTrue(
        click(player, rails.get(2).from).consumesAction(), "Normal damper click failed");
    helper.assertTrue(
        count(player, TrackItems.DAMPER.get()) == 1 && rails.get(2).magnets.size() == 2,
        "Damper cost incorrect");
    hold(helper, player, TrackItems.ROTATION_TOOL.get());
    for (var rail : rails)
      helper.assertTrue(
          click(player, rail.from).consumesAction(), "Normal rotation-tool click failed");
    helper.runAtTickTime(
        10,
        () -> {
          for (int i = 0; i < TrackDesign.values().length; i++) {
            var design = TrackDesign.values()[i];
            var rail = rails.get(i);
            for (var other : TrackDesign.values()) {
              if (other == design) continue;
              hold(helper, player, car(other));
              int badCount = count(player, car(other));
              int vehicleCount = data.vehicles.size();
              helper.assertTrue(
                  click(player, rail.from) == InteractionResult.FAIL,
                  "Wrong-family car accepted through native dispatcher " + other + " on " + design);
              helper.assertTrue(
                  data.vehicles.size() == vehicleCount && count(player, car(other)) == badCount,
                  "Rejected car placement consumed items or allocated a vehicle");
            }
          }
          for (int i = 0; i < TrackDesign.values().length; i++) {
            var design = TrackDesign.values()[i];
            var rail = rails.get(i);
            hold(helper, player, car(design));
            helper.assertTrue(
                click(player, rail.from).consumesAction(),
                "Normal car placement rejected " + design);
            helper.assertTrue(
                count(player, car(design)) == 0,
                "Car placement did not consume its craft " + design);
            var vehicle =
                data.vehicles.values().stream()
                    .filter(v -> rail.matches(v.boundFrom, v.boundTo))
                    .findFirst()
                    .orElseThrow();
            vehicles.add(vehicle);
            helper.assertTrue(
                vehicle.variant.equals(design.variant)
                    && vehicle.carriers.size() == design.groups(),
                "Survival car lost its topology " + design);
          }
          var station = rails.getFirst().from.offset(-8, 0, 3);
          place(helper, player, item("fourthdimension:boarding_station"), station);
          helper.assertTrue(
              level.getBlockState(station).is(StationContent.STATION.get()), "Wrong station block");
          var paxStation = rails.getLast().from.offset(-8, 0, -3);
          place(helper, player, item("fourthdimension:boarding_station"), paxStation);
          var winch = rails.getLast().from.offset(-8, 0, 3);
          place(helper, player, item("fourthdimension:shuttle_winch"), winch);
          helper.assertTrue(
              level.getBlockState(winch).is(ShuttleContent.WINCH.get())
                  && level.getBlockEntity(winch) instanceof ShuttleWinchEntity,
              "Winch craft did not place its mechanism");
        });
    helper.runAtTickTime(
        100,
        () -> {
          for (var v : vehicles)
            helper.assertTrue(
                Boarding.ready(level, v),
                "Survival car did not settle into initial capture " + v.variant);
          var station = rails.getFirst().from.offset(-8, 0, 3);
          hold(helper, player, null);
          helper.assertTrue(
              click(player, station).consumesAction(), "Normal empty-hand station binding failed");
          helper.assertTrue(
              ((BoardingStationEntity) level.getBlockEntity(station))
                  .vehicle.equals(vehicles.getFirst().chassis),
              "Station bound the wrong car");
          var paxStation = rails.getLast().from.offset(-8, 0, -3);
          var stationRejection = click(player, paxStation);
          LogUtils.getLogger()
              .info(
                  "FD_SURVIVAL paxStationResult={} binding={}",
                  stationRejection,
                  ((BoardingStationEntity) level.getBlockEntity(paxStation)).vehicle);
          helper.assertTrue(
              !stationRejection.consumesAction(),
              "Ordinary boarding station accepted PAX instead of requiring its winch");
          helper.assertTrue(
              ((BoardingStationEntity) level.getBlockEntity(paxStation)).vehicle == null,
              "Rejected PAX station retained an unusable binding");
          // A binding saved by older builds must not bypass the family check.
          ((BoardingStationEntity) level.getBlockEntity(paxStation)).vehicle =
              vehicles.getLast().chassis;
          helper.assertTrue(
              !click(player, paxStation).consumesAction()
                  && ((BoardingStationEntity) level.getBlockEntity(paxStation)).vehicle == null,
              "Previously bound PAX station bypassed or retained the invalid binding");
          player.setShiftKeyDown(true);
          helper.assertTrue(
              !click(player, rails.getLast().from.offset(-8, 0, 3)).consumesAction(),
              "A flat 32-block PAX section accepted an invalid shuttle route");
          player.setShiftKeyDown(false);
          var rejectedWinch =
              (ShuttleWinchEntity) level.getBlockEntity(rails.getLast().from.offset(-8, 0, 3));
          helper.assertTrue(
              rejectedWinch.vehicle == null
                  && rejectedWinch.carriage == null
                  && rejectedWinch.cars.isEmpty()
                  && rejectedWinch.phase == ShuttleWinchEntity.Phase.BOARDING,
              "Invalid route allocated or bound a shuttle mechanism");
          LogUtils.getLogger()
              .info(
                  "FD_SURVIVAL passed recipeTypes=15 craftOperations=16 trackFamilies=5"
                      + " wrongFamilyRejects=20 placedCars=5 equipment=2 stationBinding=passed"
                      + " winchPlacement=passed initialCapture=passed paxStationReject=passed"
                      + " oldPaxBindingCleared=passed invalidShuttleReject=passed");
          var container = SubLevelContainer.getContainer(level);
          for (var v : vehicles) {
            for (var c : v.carriers)
              container.removeSubLevel(
                  container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
            data.vehicles.remove(v.chassis);
          }
          for (var rail : rails) {
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
            level.removeBlock(rail.from.below(), false);
            level.removeBlock(rail.to.below(), false);
            var panel = rail.from.offset(-8, 0, 3);
            level.removeBlock(panel, false);
            level.removeBlock(panel.below(), false);
            data.rails.remove(rail);
          }
          level.removeBlock(paxStation, false);
          level.removeBlock(paxStation.below(), false);
          level.removeBlock(table, false);
          for (int x = -58; x <= -48; x++)
            for (int z = -57; z <= -52; z++) level.setChunkForced(x, z, false);
          player.getAdvancements().stopListening();
          helper.succeed();
        });
  }

  @GameTest(template = "empty", batch = "fd_survival_circuit", timeoutTicks = 6600)
  public static void craftedArrowLoopTraversesNativePoweredCircuit(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 18; x <= 21; x++)
      for (int z = 30; z <= 35; z++) level.setChunkForced(x, z, true);
    var origin = new BlockPos(300, 132, 500);
    var profile = new GameProfile(UUID.randomUUID(), "FDArrowLoopBuilder");
    var connection = FakePlayerFactory.get(level, profile).connection;
    var player =
        new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
    player.connection = connection;
    player.setGameMode(GameType.SURVIVAL);
    helper.assertTrue(!player.isCreative() && player.mayBuild(), "Loop builder is not in survival");
    var table = origin.offset(-7, 0, -7);
    level.setBlockAndUpdate(table, Blocks.CRAFTING_TABLE.defaultBlockState());
    player.setPos(table.getX() + 2, table.getY(), table.getZ());
    var materials = Map.ofEntries(Map.entry("minecraft:iron_ingot", 48),
        Map.entry("minecraft:copper_ingot", 16), Map.entry("minecraft:red_dye", 5),
        Map.entry("create:red_seat", 2), Map.entry("minecraft:iron_bars", 1),
        Map.entry("simulatedcoasters:coaster_cart", 1), Map.entry("minecraft:compass", 1),
        Map.entry("create:brass_ingot", 1), Map.entry("minecraft:stick", 1),
        Map.entry("minecraft:redstone", 2), Map.entry("minecraft:comparator", 1),
        Map.entry("minecraft:copper_block", 1),
        Map.entry("simulatedcoasters:coaster_anchorpoint", 8));
    materials.forEach(
        (id, quantity)
            -> helper.assertTrue(player.getInventory().add(new ItemStack(item(id), quantity)),
                "Loop material did not fit: " + id));
    var recipes = TeachingContentGameTests.crafts();
    var arrowTrack =
        recipes.stream().filter(r -> r.name().equals("arrow_track")).findFirst().orElseThrow();
    for (int i = 0; i < 5; i++) craft(helper, player, table, arrowTrack);
    for (var name : List.of("arrow_car", "rotation_tool", "boarding_station"))
      craft(helper, player, table,
          recipes.stream().filter(r -> r.name().equals(name)).findFirst().orElseThrow());
    helper.assertTrue(count(player, TrackItems.ARROW_CAR.get()) == 1,
        "Crafted Arrow car missing immediately after result-slot insertion");
    // A player may split crafted track. This leaves fewer items in the selected stack
    // than the first connection costs, while keeping sufficient stock in other slots.
    var inventory = player.getInventory();
    int beforeSplit = count(player, TrackItems.ARROW.get());
    var selectedTrack =
        inventory.items.stream().filter(s -> s.is(TrackItems.ARROW.get())).findFirst().orElseThrow();
    helper.assertTrue(selectedTrack.getCount() > 2, "Crafted track did not form a splittable stack");
    var reserveTrack = selectedTrack.split(selectedTrack.getCount() - 2);
    int reserveSlot = -1;
    for (int i = 0; i < inventory.items.size(); i++)
      if (inventory.items.get(i).isEmpty()) {
        reserveSlot = i;
        break;
      }
    helper.assertTrue(reserveSlot >= 0, "No slot for the split track stack");
    inventory.items.set(reserveSlot, reserveTrack);
    helper.assertTrue(
        selectedTrack.getCount() == 2 && count(player, TrackItems.ARROW.get()) == beforeSplit,
        "Splitting crafted track changed its item count");
    int[][] offsets = {{0, 8}, {0, 44}, {8, 52}, {24, 52}, {32, 44}, {32, 8}, {24, 0}, {8, 0}};
    var anchors = new ArrayList<BlockPos>();
    for (var offset : offsets) {
      var pos = origin.offset(offset[0], 0, offset[1]);
      place(helper, player, item("simulatedcoasters:coaster_anchorpoint"), pos);
      anchors.add(pos);
    }
    helper.assertTrue(count(player, TrackItems.ARROW_CAR.get()) == 1,
        "Crafted Arrow car lost while placing native anchors");
    var data = FoundationData.get(level);
    var rails = new ArrayList<FoundationData.Rail>();
    int trackBefore = count(player, TrackItems.ARROW.get());
    int totalCost = 0;
    for (int i = 0; i < anchors.size(); i++) {
      var from = anchors.get(i);
      var to = anchors.get((i + 1) % anchors.size());
      var preview = TrackPlacement.preview(level, from, to);
      var previewEdge =
          CoasterPathEdge.curve(preview.bePositions.getFirst(), preview.bePositions.getSecond(), preview);
      double previewLength = TrackGeometry.table(level, previewEdge).length();
      int sectionCost = (int) Math.ceil(previewLength / 4);
      if (i == 0)
        helper.assertTrue(sectionCost > 2, "First loop connection did not cross split stacks");
      int sectionBefore = count(player, TrackItems.ARROW.get());
      hold(helper, player, TrackItems.ARROW.get());
      helper.assertTrue(click(player, from).consumesAction() && click(player, to).consumesAction(),
          "Crafted track did not connect Arrow loop section " + i);
      var rail = data.rails.stream().filter(r -> r.matches(from, to)).findFirst().orElseThrow();
      helper.assertTrue(rail.original() && rail.valid, "Loop section lost Original provenance");
      int sectionSpent = sectionBefore - count(player, TrackItems.ARROW.get());
      LogUtils.getLogger().info(
          "FD_SURVIVAL_ARROW_SECTION index={} previewLength={} storedLength={} expectedCost={} spent={}",
          i, previewLength, rail.profile.keys().getLast().distance(), sectionCost, sectionSpent);
      helper.assertTrue(
          sectionSpent == sectionCost,
          "Loop section " + i + " spent " + sectionSpent + " but preview required " + sectionCost);
      helper.assertTrue(count(player, TrackItems.ARROW_CAR.get()) == 1,
          "Crafted Arrow car lost while connecting loop section " + i);
      totalCost += sectionCost;
      rails.add(rail);
    }
    helper.assertTrue(count(player, TrackItems.ARROW.get()) == trackBefore - totalCost,
        "Full loop did not consume its actual track length");
    int trackCost = totalCost;
    hold(helper, player, TrackItems.ROTATION_TOOL.get());
    helper.assertTrue(
        click(player, anchors.getFirst()).consumesAction(), "Loop editor was inaccessible");
    helper.assertTrue(count(player, TrackItems.ARROW_CAR.get()) == 1,
        "Crafted Arrow car lost while opening the loop editor");
    var vehicleRef = new java.util.concurrent.atomic.AtomicReference<FoundationData.Vehicle>();
    var stationPos = origin.offset(-6, 0, 20);
    var stationPowerRef = new java.util.concurrent.atomic.AtomicReference<BlockPos>();
    var riderRef = new java.util.concurrent.atomic.AtomicReference<net.minecraft.world.entity.Entity>();
    var container = SubLevelContainer.getContainer(level);
    double[] maxXZ = {0, 0}, maxAngle = {0};
    boolean[] visited = new boolean[rails.size()];
    int[] sampledTicks = {0}, currentSection = {-1};
    var running = new java.util.concurrent.atomic.AtomicBoolean(false);
    helper.onEachTick(() -> {
      if (!running.get()) return;
      var vehicle = vehicleRef.get();
      var point = CoasterCartResolve.cartCenterWorld(container.getSubLevel(vehicle.chassis));
      maxXZ[0] = Math.max(maxXZ[0], point.x - origin.getX());
      maxXZ[1] = Math.max(maxXZ[1], point.z - origin.getZ());
      maxAngle[0] = Math.max(maxAngle[0], vehicle.carriers.getFirst().angle);
      if (++sampledTicks[0] % 5 != 0) return;
      var hit = CoasterPathTrackFrame.nearestGraphHit(
          level, CoasterPathGraphManager.get(level), new Vec3(point.x, point.y, point.z), 4);
      if (hit == null) return;
      for (int i = 0; i < rails.size(); i++)
        if (rails.get(i).matches(hit.edge().from(), hit.edge().to())) {
          currentSection[0] = i;
          if (hit.t() > .15 && hit.t() < .85 && !visited[i]) {
            visited[i] = true;
            LogUtils.getLogger().info(
                "FD_SURVIVAL_ARROW_VISIT section={} t={} center={},{},{} angle={}",
                i, hit.t(), point.x, point.y, point.z, vehicle.carriers.getFirst().angle);
          }
          break;
        }
    });
    // Register every timed action before GameTest begins iterating its action map.
    helper.runAtTickTime(10, () -> {
      var foothold = origin.offset(0, -1, 20);
      level.setBlockAndUpdate(foothold, Blocks.STONE.defaultBlockState());
      helper.assertTrue(count(player, TrackItems.ARROW_CAR.get()) == 1,
          "Crafted Arrow car lost before delayed native placement");
      hold(helper, player, TrackItems.ARROW_CAR.get());
      helper.assertTrue(
          click(player, foothold).consumesAction(), "Crafted Arrow car was not placed");
      level.removeBlock(foothold, false);
      var vehicle = data.vehicles.values()
                        .stream()
                        .filter(v -> rails.getFirst().matches(v.boundFrom, v.boundTo))
                        .findFirst()
                        .orElseThrow();
      vehicleRef.set(vehicle);
      helper.assertTrue(
          count(player, TrackItems.ARROW_CAR.get()) == 0, "Car craft was not consumed");
      place(helper, player, item("fourthdimension:boarding_station"), stationPos);
      var stationRear =
          level.getBlockState(stationPos).getValue(BoardingStation.FACING).getOpposite();
      stationPowerRef.set(stationPos.relative(stationRear));
    });
    // The upstream Chain and creative motor supply power only; the builder authored the
    // functional rails, controlled profile, car and station through normal survival paths.
    helper.runAtTickTime(40, () -> {
      for (int i = 0; i < rails.size(); i++) {
        var rail = rails.get(i);
        var edge = TrackGeometry.resolve(level, rail);
        helper.assertTrue(edge != null, "Player-connected rail has no native edge: " + i);
        double length = TrackGeometry.table(level, edge).length();
        if (i == 4) {
          player.setPos(
              anchors.get(i).getX() + 2.5, anchors.get(i).getY() + 1, anchors.get(i).getZ() + .5);
          EditorNetwork.apply(player,
              new EditorNetwork.Edit(
                  rail.id, rail.revision, 0, "0:0;" + (length / 2) + ":360;" + length + ":0"));
          helper.assertTrue(rail.profile.angleAt(length / 2) > 6,
              "Survival builder did not author a controlled turn");
        }
        // Native graph insertion may store the edge opposite the order of the player's
        // two clicks. A chain span must follow the intended loop direction in either case.
        edge = TrackGeometry.resolve(level, rail);
        helper.assertTrue(edge != null, "Edited rail lost its native edge: " + i);
        length = TrackGeometry.table(level, edge).length();
        var from = anchors.get(i);
        var to = anchors.get((i + 1) % anchors.size());
        boolean forward = edge.from().equals(from) && edge.to().equals(to);
        boolean reverse = edge.from().equals(to) && edge.to().equals(from);
        helper.assertTrue(forward || reverse, "Native edge endpoints differ from loop section " + i);
        var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(anchors.get(i));
        var axis = i == 0 || i == 1 || i == 4 || i == 5 ? Direction.Axis.X : Direction.Axis.Z;
        anchor.commitChainLift(axis, ChainLiftEndRef.anchor(to),
            new CoasterPathGraphSpans.SpanPath(
                List.of(new CoasterPathGraphSpans.PathSegment(
                    edge, forward ? 0 : 1, forward ? 1 : 0)), length));
        LogUtils.getLogger().info(
            "FD_SURVIVAL_ARROW_CHAIN section={} from={} to={} edgeFrom={} edgeTo={} reverse={} sign={} length={}",
            i, from, to, edge.from(), edge.to(), reverse, anchor.getChainLiftTravelSign(), length);
        var direction = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        var motorPos = anchors.get(i).relative(direction);
        level.setBlockAndUpdate(motorPos,
            AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(
                BlockStateProperties.FACING, direction.getOpposite()));
        var motor = (CreativeMotorBlockEntity) level.getBlockEntity(motorPos);
        motor.generatedSpeed.setValue(32 * anchor.getChainLiftTravelSign());
      }
      data.setDirty();
    });
    helper.runAtTickTime(100, () -> {
      var vehicle = vehicleRef.get();
      helper.assertTrue(vehicle != null, "Crafted car assembly did not run");
      helper.assertTrue(Boarding.ready(level, vehicle), "Crafted car did not settle");
      var chassisCenter = CoasterCartResolve.cartCenterWorld(container.getSubLevel(vehicle.chassis));
      double stationDistance =
          Math.sqrt(chassisCenter.distanceSquared(
              new org.joml.Vector3d(
                  stationPos.getX() + .5, stationPos.getY() + .5, stationPos.getZ() + .5)));
      LogUtils.getLogger().info("FD_SURVIVAL_ARROW_STATION distance={}", stationDistance);
      helper.assertTrue(stationDistance < 12, "Crafted car settled outside station binding range");
      hold(helper, player, null);
      helper.assertTrue(
          click(player, stationPos).consumesAction(),
          "Player station binding failed at distance " + stationDistance);
      helper.assertTrue(
          vehicle.chassis.equals(((BoardingStationEntity) level.getBlockEntity(stationPos)).vehicle),
          "Station bound another car");
      var carrier = vehicle.carriers.getFirst();
      var body = container.getSubLevel(carrier.body);
      var rider = net.minecraft.world.entity.EntityType.PIG.create(level);
      riderRef.set(rider);
      rider.setNoAi(true);
      rider.setPos(origin.getX(), origin.getY() + 5, origin.getZ() + 20);
      level.addFreshEntity(rider);
      SeatBlock.sitDown(
          level, body.getPlot().getCenterBlock().offset(carrier.seats.getFirst()), rider);
      helper.assertTrue(rider.isPassenger(), "Crafted car seat did not mount a rider");
      player.setPos(chassisCenter.x + 2, chassisCenter.y + 1, chassisCenter.z);
      EditorNetwork.apply(
          player, new EditorNetwork.Edit(vehicle.chassis, vehicle.revision, 11, ""));
      EditorNetwork.apply(
          player, new EditorNetwork.Edit(vehicle.chassis, vehicle.revision, 13, ""));
      running.set(true);
    });
    helper.runAtTickTime(260, () -> {
      var stationPower = stationPowerRef.get();
      helper.assertTrue(stationPower != null, "Crafted station placement did not run");
      level.setBlockAndUpdate(stationPower, Blocks.REDSTONE_BLOCK.defaultBlockState());
    });
    helper.runAtTickTime(2000, () -> {
      var vehicle = vehicleRef.get();
      var center = CoasterCartResolve.cartCenterWorld(container.getSubLevel(vehicle.chassis));
      LogUtils.getLogger().info(
          "FD_SURVIVAL_ARROW_CHECKPOINT tick=2000 section={} center={},{},{} visited={} maxAngle={} ready={}",
          currentSection[0], center.x, center.y, center.z,
          java.util.Arrays.toString(visited), maxAngle[0], Boarding.ready(level, vehicle));
    });
    helper.runAtTickTime(6500, () -> {
      var vehicle = vehicleRef.get();
      var rider = riderRef.get();
      var stationPower = stationPowerRef.get();
      helper.assertTrue(vehicle != null && rider != null && stationPower != null,
          "Survival loop setup was incomplete");
      running.set(false);
      LogUtils.getLogger().info(
          "FD_SURVIVAL_ARROW_LOOP rails={} cost={} maxXZ={},{} visited={} maxAngle={} ready={} fault={}",
          rails.size(), trackCost, maxXZ[0], maxXZ[1], java.util.Arrays.toString(visited), maxAngle[0],
          Boarding.ready(level, vehicle), vehicle.fault);
      helper.assertTrue(maxXZ[0] > 30 && maxXZ[1] > 50,
          "Survival-connected Arrow loop did not complete a circuit");
      for (int i = 0; i < visited.length; i++)
        helper.assertTrue(visited[i], "Survival-connected Arrow loop missed section " + i);
      helper.assertTrue(
          maxAngle[0] > 6, "Survival-connected Arrow loop did not make its controlled turn");
      helper.assertTrue(rider.isPassenger(), "Loop lost its native rider");
      helper.assertTrue(
          Boarding.ready(level, vehicle) && vehicle.stopping && vehicle.restraints,
          "Survival-connected Arrow loop did not capture at station");
      helper.assertTrue(
          ((BoardingStationEntity) level.getBlockEntity(stationPos)).fault.isEmpty(),
          "Station rejected returning survival-built car");
      var returned = CoasterCartResolve.cartCenterWorld(container.getSubLevel(vehicle.chassis));
      player.setPos(returned.x + 2, returned.y + 1, returned.z);
      EditorNetwork.apply(
          player, new EditorNetwork.Edit(vehicle.chassis, vehicle.revision, 12, ""));
      rider.stopRiding();
      rider.discard();
      for (var carrierBody : vehicle.carriers)
        container.removeSubLevel(
            container.getSubLevel(carrierBody.body), SubLevelRemovalReason.REMOVED);
      container.removeSubLevel(
          container.getSubLevel(vehicle.chassis), SubLevelRemovalReason.REMOVED);
      data.vehicles.remove(vehicle.chassis);
      for (int i = 0; i < rails.size(); i++) {
        var rail = rails.get(i);
        ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
            .removeAnchorPeerCurve(level, rail.to);
        level.removeBlock(
            anchors.get(i).relative(
                i == 0 || i == 1 || i == 4 || i == 5 ? Direction.EAST : Direction.SOUTH),
            false);
      }
      for (var anchor : anchors) {
        level.removeBlock(anchor, false);
        level.removeBlock(anchor.below(), false);
      }
      data.rails.removeAll(rails);
      level.removeBlock(stationPos, false);
      level.removeBlock(stationPower, false);
      level.removeBlock(stationPos.below(), false);
      level.removeBlock(table, false);
      for (int x = 18; x <= 21; x++)
        for (int z = 30; z <= 35; z++) level.setChunkForced(x, z, false);
      player.getAdvancements().stopListening();
      helper.succeed();
    });
  }
}
