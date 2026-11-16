package dev.fourthdimension.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import net.minecraft.world.item.ItemStack;

/** Native recipe-manager selection catches ambiguous expanded recipes in the installed profile. */
@GameTestHolder("fourthdimension_fd901_recipes")
@PrefixGameTestTemplate(false)
public final class Fd901RecipeGameTests {
  @GameTest(template="empty", timeoutTicks=80)
  public static void everyAuthoredRecipeSelectsItsOwnOutput(GameTestHelper helper) {
    var level=helper.getLevel(); var manager=level.getRecipeManager(); int checked=0;
    for(var holder:manager.getAllRecipesFor(RecipeType.CRAFTING)) {
      if(!holder.id().getNamespace().equals("fourthdimension")) continue;
      var recipe=holder.value(); var stacks=new ArrayList<ItemStack>();
      for(var ingredient:recipe.getIngredients()) {
        var choices=ingredient.getItems();
        stacks.add(choices.length==0?ItemStack.EMPTY:choices[0].copy());
      }
      int width, height;
      if(recipe instanceof ShapedRecipe shaped) { width=shaped.getWidth(); height=shaped.getHeight(); }
      else {
        helper.assertTrue(recipe instanceof net.minecraft.world.item.crafting.ShapelessRecipe,
            "Declare custom crafting coverage: "+holder.id());
        width=3; height=3;
        while(stacks.size()<9) stacks.add(ItemStack.EMPTY);
      }
      var input=CraftingInput.of(width,height,stacks);
      var selected=manager.getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow();
      helper.assertTrue(selected.id().equals(holder.id()),
          "Ambiguous recipe "+holder.id()+" selects "+selected.id());
      var expected=recipe.getResultItem(level.registryAccess());
      var actual=selected.value().assemble(input,level.registryAccess());
      helper.assertTrue(ItemStack.isSameItemSameComponents(expected,actual)&&expected.getCount()==actual.getCount(),
          "Wrong authored output "+holder.id());
      checked++;
    }
    helper.assertTrue(checked>0,"No authored recipes loaded");
    System.out.println("FD901_RECIPE_SELECTION checked="+checked+" unique=true");helper.succeed();
  }
}
