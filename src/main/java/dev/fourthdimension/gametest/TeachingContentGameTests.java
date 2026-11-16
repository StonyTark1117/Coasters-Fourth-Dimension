package dev.fourthdimension.gametest;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class TeachingContentGameTests {
  record Craft(
      String name,
      List<String> pattern,
      Map<Character, String> ingredients,
      int count,
      String discoveryItem) {}

  static List<Craft> crafts() {
    var result = new ArrayList<Craft>();
    String[] families = {"arrow", "legacy_zacspin", "free_spin", "revised_zacspin", "pax"};
    String[] dyes = {"red", "yellow", "blue", "purple", "green"};
    for (int i = 0; i < families.length; i++) {
      String track = families[i] + "_track";
      String dye = "minecraft:" + dyes[i] + "_dye";
      result.add(
          new Craft(
              track,
              List.of("III", "CDC", "III"),
              Map.of('I', "minecraft:iron_ingot", 'C', "minecraft:copper_ingot", 'D', dye),
              16,
              dye));
      result.add(
          new Craft(
              families[i] + "_car",
              List.of("SAS", "ITI", "ICI"),
              Map.of(
                  'S',
                  "create:red_seat",
                  'A',
                  "minecraft:iron_bars",
                  'I',
                  "minecraft:iron_ingot",
                  'T',
                  "fourthdimension:" + track,
                  'C',
                  "simulatedcoasters:coaster_cart"),
              1,
              "fourthdimension:" + track));
    }
    result.add(
        new Craft(
            "rotation_tool",
            List.of("CBS"),
            Map.of('C', "minecraft:compass", 'B', "create:brass_ingot", 'S', "minecraft:stick"),
            1,
            "minecraft:compass"));
    result.add(
        new Craft(
            "boarding_station",
            List.of("RCR", "IBI"),
            Map.of(
                'R',
                "minecraft:redstone",
                'C',
                "minecraft:comparator",
                'I',
                "minecraft:iron_ingot",
                'B',
                "minecraft:copper_block"),
            1,
            "minecraft:comparator"));
    for (String mode : List.of("inducer", "damper")) {
      String core = mode.equals("inducer") ? "minecraft:redstone" : "minecraft:quartz";
      result.add(
          new Craft(
              "magnetic_" + mode,
              List.of("ICI", "RCR", "ICI"),
              Map.of('I', "minecraft:iron_ingot", 'C', "minecraft:copper_ingot", 'R', core),
              2,
              core));
    }
    result.add(
        new Craft(
            "shuttle_winch",
            List.of("IRI", "CSC", "ITI"),
            Map.of(
                'I',
                "minecraft:iron_ingot",
                'R',
                "minecraft:redstone",
                'C',
                "minecraft:copper_ingot",
                'S',
                "create:shaft",
                'T',
                "fourthdimension:pax_track"),
            1,
            "fourthdimension:pax_track"));
    return result;
  }

  private static ItemStack stack(GameTestHelper helper, String id) {
    var key = ResourceLocation.parse(id);
    helper.assertTrue(BuiltInRegistries.ITEM.containsKey(key), "Missing ingredient " + id);
    return new ItemStack(BuiltInRegistries.ITEM.get(key));
  }

  @GameTest(template = "empty", timeoutTicks = 80)
  public static void nativeCraftingAndRecipeDiscovery(GameTestHelper helper) {
    var level = helper.getLevel();
    var manager = level.getRecipeManager();
    var cases = crafts();
    // This inventory proves original-family recipes. Expanded-family content has
    // independent native/player checks and must not invalidate this finite list.
    var owned = manager.getRecipeIds().filter(id -> id.getNamespace().equals("fourthdimension"))
        .collect(java.util.stream.Collectors.toSet());
    helper.assertTrue(cases.stream().allMatch(c -> owned.contains(
        ResourceLocation.fromNamespaceAndPath("fourthdimension", c.name()))),
        "Missing original player recipe");
    // Use real advancements/recipe-book state without registering a client that
    // has not negotiated the installed mods' custom payloads. Only network output
    // is discarded by NeoForge's fake connection; recipe discovery runs normally.
    var profile = new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "FDRecipeTest");
    var connection = FakePlayerFactory.get(level, profile).connection;
    var player =
        new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
    player.connection = connection;
    try {
      for (var craft : cases) {
        var input = new ArrayList<ItemStack>();
        for (String row : craft.pattern()) {
          for (char symbol : row.toCharArray())
            input.add(stack(helper, craft.ingredients().get(symbol)));
        }
        var grid =
            CraftingInput.of(craft.pattern().getFirst().length(), craft.pattern().size(), input);
        var recipe =
            manager
                .getRecipeFor(RecipeType.CRAFTING, grid, level)
                .orElseThrow(() -> new IllegalStateException("Uncraftable " + craft.name()));
        var id = ResourceLocation.fromNamespaceAndPath("fourthdimension", craft.name());
        helper.assertTrue(recipe.id().equals(id), "Recipe collision for " + craft.name());
        var output = recipe.value().assemble(grid, level.registryAccess());
        helper.assertTrue(
            output.is(stack(helper, id.toString()).getItem()) && output.getCount() == craft.count(),
            "Wrong craft output " + craft.name());
        var missing = new ArrayList<>(input);
        missing.set(0, ItemStack.EMPTY);
        helper.assertTrue(
            !recipe
                .value()
                .matches(
                    CraftingInput.of(
                        craft.pattern().getFirst().length(), craft.pattern().size(), missing),
                    level),
            "Craft succeeded with a missing ingredient: " + craft.name());

        var advancement =
            level
                .getServer()
                .getAdvancements()
                .get(
                    ResourceLocation.fromNamespaceAndPath(
                        "fourthdimension", "recipes/" + craft.name()));
        helper.assertTrue(advancement != null, "Missing recipe discovery " + craft.name());
        player.getRecipeBook().remove(recipe);
        for (String criterion : advancement.value().criteria().keySet()) {
          player.getAdvancements().revoke(advancement, criterion);
        }
        player.getInventory().clearContent();
        var ingredient = stack(helper, craft.discoveryItem());
        player.getInventory().setItem(0, ingredient);
        CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), ingredient);
        helper.assertTrue(
            player.getRecipeBook().contains(id),
            "Inventory acquisition failed to discover " + craft.name());
        helper.assertTrue(
            player.getAdvancements().getOrStartProgress(advancement).isDone(),
            "Recipe advancement incomplete " + craft.name());
      }
      LogUtils.getLogger()
          .info(
              "FD_TEACHING nativeCrafts={} missingIngredientRejections={} inventoryDiscoveries={}",
              cases.size(),
              cases.size(),
              cases.size());
    } finally {
      player.getAdvancements().stopListening();
    }
    helper.succeed();
  }
}
