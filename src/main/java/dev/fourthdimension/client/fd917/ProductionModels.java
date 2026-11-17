package dev.fourthdimension.client.fd917;

import com.google.gson.JsonParser;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Authored component models and block-local hinges; no server state or seat ownership. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ProductionModels {
  public record Part(ResourceLocation model, String property, double x, double y, double z,
                     String axis, float openDegrees, boolean facing, Map<String, String> when) {
    public boolean matches(BlockState state) {
      for (var entry : when.entrySet()) {
        var property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
        if (property == null || !value(state, property).equals(entry.getValue())) return false;
      }
      return true;
    }
    public boolean closed(BlockState state) {
      var key = state.getBlock().getStateDefinition().getProperty(property);
      return key != null && value(state, key).equals("true");
    }
  }
  private static Map<ResourceLocation, List<Part>> parts = Map.of();
  private static int generation;
  private ProductionModels() {}

  private static <T extends Comparable<T>> String value(
      BlockState state, net.minecraft.world.level.block.state.properties.Property<T> property) {
    return property.getName(state.getValue(property));
  }
  public static List<Part> parts(ResourceLocation block) { return parts.getOrDefault(block, List.of()); }
  public static int generation() { return generation; }

  @SubscribeEvent
  public static void staticEndpoints(ModelEvent.ModifyBakingResult event) {
    var models = event.getModels();
    for (var entry : parts.entrySet()) {
      var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(entry.getKey());
      if (!(block instanceof dev.fourthdimension.vehicle.CarrierSeatBlock)) continue;
      for (var state : block.getStateDefinition().getPossibleStates()) {
        var id = net.minecraft.client.renderer.block.BlockModelShaper.stateToModelLocation(state);
        var original = models.get(id);
        if (original != null) models.put(id, new dev.fourthdimension.client.tools.PaletteModels(new StaticArticulationModel(original, state, entry.getValue(), models)));
      }
    }
  }

  @SubscribeEvent
  public static void register(ModelEvent.RegisterAdditional event) {
    var resources = Minecraft.getInstance().getResourceManager();
    var loaded = new HashMap<ResourceLocation, List<Part>>();
    for (var entry : resources.listResources("fd917_articulation", p -> p.getPath().endsWith(".json")).entrySet()) {
      var id = entry.getKey();
      var list = new ArrayList<Part>();
      try (var reader = entry.getValue().openAsReader()) {
        var json = JsonParser.parseReader(reader).getAsJsonObject();
        for (var element : json.getAsJsonArray("parts")) {
          var part = element.getAsJsonObject();
          var pivot = part.getAsJsonArray("pivot");
          var when = new HashMap<String, String>();
          if (part.has("when")) part.getAsJsonObject("when").entrySet()
              .forEach(e -> when.put(e.getKey(), e.getValue().getAsString()));
          var model = ResourceLocation.parse(part.get("model").getAsString());
          var axis = part.get("axis").getAsString();
          if (!Set.of("x", "y", "z").contains(axis) || pivot.size() != 3)
            throw new IllegalArgumentException("Invalid hinge axis or pivot");
          list.add(new Part(model, part.get("property").getAsString(),
              pivot.get(0).getAsDouble()/16, pivot.get(1).getAsDouble()/16,
              pivot.get(2).getAsDouble()/16, axis, part.get("open_degrees").getAsFloat(),
              part.has("rotate_facing") && part.get("rotate_facing").getAsBoolean(), Map.copyOf(when)));
          event.register(ModelResourceLocation.standalone(model));
        }
        var path = id.getPath();
        loaded.put(ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
            path.substring("fd917_articulation/".length(), path.length()-5)), List.copyOf(list));
      } catch (Exception failure) {
        throw new IllegalStateException("Invalid FD917 articulation resource " + id, failure);
      }
    }
    // Other authored mechanism components are standalone models too.
    for (var id : resources.listResources("models/block/fd917", p -> p.getPath().endsWith(".json")).keySet()) {
      var path = id.getPath();
      event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
          id.getNamespace(), path.substring("models/".length(), path.length()-5))));
    }
    parts = Map.copyOf(loaded);
    generation++;
  }
}
