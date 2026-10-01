package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class AcquisitionGameTests {
  private record Case(String name, int count, String trigger, String[] grid) {}

  @GameTest(template = "empty", timeoutTicks = 80)
  public static void craftingAndRecipeDiscovery(GameTestHelper helper) {
    var cases = new ArrayList<Case>();
    String[] families = {"arrow", "legacy_zacspin", "free_spin", "revised_zacspin", "pax"};
    String[] dyes = {"red", "yellow", "blue", "purple", "green"};
    for (int i = 0; i < families.length; i++) {
      String family = families[i];
      cases.add(
          new Case(
              family + "_track",
              16,
              "minecraft:iron_ingot",
              new String[] {
                "iron_ingot",
                "iron_ingot",
                "iron_ingot",
                "copper_ingot",
                dyes[i] + "_dye",
                "copper_ingot",
                "iron_ingot",
                "iron_ingot",
                "iron_ingot"
              }));
      cases.add(
          new Case(
              family + "_car",
              1,
              "simulatedcoasters:coaster_cart",
              new String[] {
                "create:red_seat",
                "iron_bars",
                "create:red_seat",
                "iron_ingot",
                "fourthdimension:" + family + "_track",
                "iron_ingot",
                "iron_ingot",
                "simulatedcoasters:coaster_cart",
                "iron_ingot"
              }));
    }
    cases.add(
        new Case(
            "boarding_station",
            1,
            "minecraft:comparator",
            new String[] {
              "redstone",
              "comparator",
              "redstone",
              "iron_ingot",
              "copper_block",
              "iron_ingot",
              "air",
              "air",
              "air"
            }));
    cases.add(
        new Case(
            "rotation_tool",
            1,
            "create:brass_ingot",
            new String[] {
              "compass", "create:brass_ingot", "stick", "air", "air", "air", "air", "air", "air"
            }));
    cases.add(
        new Case(
            "shuttle_winch",
            1,
            "create:shaft",
            new String[] {
              "iron_ingot",
              "redstone",
              "iron_ingot",
              "copper_ingot",
              "create:shaft",
              "copper_ingot",
              "iron_ingot",
              "fourthdimension:pax_track",
              "iron_ingot"
            }));
    for (String mode : List.of("inducer", "damper")) {
      String middle = mode.equals("inducer") ? "redstone" : "quartz";
      cases.add(
          new Case(
              "magnetic_" + mode,
              2,
              "minecraft:" + middle,
              new String[] {
                "iron_ingot",
                "copper_ingot",
                "iron_ingot",
                middle,
                "copper_ingot",
                middle,
                "iron_ingot",
                "copper_ingot",
                "iron_ingot"
              }));
    }
    var level = helper.getLevel();
    for (var row : cases) {
      var id = ResourceLocation.fromNamespaceAndPath("fourthdimension", row.name());
      var ingredients = Arrays.stream(row.grid()).map(AcquisitionGameTests::stack).toList();
      var input = CraftingInput.of(3, 3, ingredients);
      var recipe =
          level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
      var result = recipe.value().assemble(input, level.registryAccess());
      helper.assertTrue(recipe.id().equals(id), "Wrong recipe selected for " + id);
      helper.assertTrue(
          BuiltInRegistries.ITEM.getKey(result.getItem()).equals(id)
              && result.getCount() == row.count(),
          "Wrong crafting result for " + id);
      var incomplete = new ArrayList<>(ingredients);
      incomplete.set(0, ItemStack.EMPTY);
      helper.assertTrue(
          !recipe.value().matches(CraftingInput.of(3, 3, incomplete), level),
          "Missing ingredient accepted for " + id);
      // NeoForge FakePlayerAdvancements deliberately ignores loading and rewards.
      // A real ServerPlayer with a no-network handler exercises vanilla discovery.
      var player =
          new ServerPlayer(
              level.getServer(),
              level,
              new GameProfile(
                  UUID.nameUUIDFromBytes(
                      ("FDRecipe/" + row.name()).getBytes(StandardCharsets.UTF_8)),
                  "FDRecipe"),
              ClientInformation.createDefault());
      player.connection =
          new ServerGamePacketListenerImpl(
              level.getServer(),
              new Connection(PacketFlow.SERVERBOUND),
              player,
              CommonListenerCookie.createInitial(player.getGameProfile(), false)) {
            @Override
            public void send(Packet<?> packet) {}
          };
      player.setGameMode(GameType.SURVIVAL);
      var advancement =
          level
              .getServer()
              .getAdvancements()
              .get(
                  ResourceLocation.fromNamespaceAndPath(
                      "fourthdimension", "recipes/" + row.name()));
      helper.assertTrue(advancement != null, "Missing recipe discovery for " + id);
      for (var criterion : advancement.value().criteria().keySet())
        player.getAdvancements().revoke(advancement, criterion);
      player.getRecipeBook().remove(recipe);
      player.getInventory().clearContent();
      var unrelated = new ItemStack(Items.DIRT);
      player.getInventory().setItem(0, unrelated);
      CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), unrelated);
      helper.assertTrue(!player.getRecipeBook().contains(recipe), "Unrelated item unlocked " + id);
      var ingredient = stack(row.trigger());
      player.getInventory().setItem(0, ingredient);
      CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), ingredient);
      helper.assertTrue(
          player.getRecipeBook().contains(recipe)
              && player.getAdvancements().getOrStartProgress(advancement).isDone(),
          "Ingredient did not unlock " + id);
      System.out.println(
          "FD_ACQUISITION recipe="
              + id
              + " count="
              + result.getCount()
              + " trigger="
              + row.trigger()
              + " missingRejected=true unrelatedRejected=true discovered=true");
      player.getInventory().clearContent();
      player.getAdvancements().stopListening();
    }
    helper.assertTrue(cases.size() == 15, "Incomplete acquisition matrix");
    helper.succeed();
  }

  private static ItemStack stack(String name) {
    var id = ResourceLocation.parse(name);
    if (id.equals(ResourceLocation.withDefaultNamespace("air"))) return ItemStack.EMPTY;
    if (!BuiltInRegistries.ITEM.containsKey(id))
      throw new IllegalArgumentException("Missing ingredient " + name);
    return new ItemStack(BuiltInRegistries.ITEM.get(id));
  }
}
