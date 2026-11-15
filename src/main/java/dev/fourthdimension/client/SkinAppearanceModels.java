package dev.fourthdimension.client;

import dev.fourthdimension.integration.*;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Reload-safe: resolve model availability each time, without retaining baked models across reloads. */
public final class SkinAppearanceModels {
  private static final java.util.Map<SkinAppearance,Object> DEFINITIONS=new java.util.EnumMap<>(SkinAppearance.class);
  private static final java.util.Map<net.minecraft.client.resources.model.BakedModel,Boolean> VALID=new java.util.WeakHashMap<>();
  private SkinAppearanceModels() {}
  public static Object skin(SubLevel body) {
    var vehicle=SkinAdapter.owner(body);
    if(vehicle==null)return null;
    var choice=vehicle.appearance;
    if(!vehicle.chassis.equals(body.getUniqueId()) || !SkinAdapter.supported(vehicle)
        || choice==SkinAppearance.ORIGINAL || !OptionalIntegrations.installed("create_coaster_skins").mayInspect()
        || !available(choice.underbody) || !available(choice.wheel))return SkinAdapter.nativeSkin();
    try {
      var cached=DEFINITIONS.get(choice);
      if(cached!=null)return cached;
      var definition=Class.forName("medev.coaster.skins.skin.SkinDefinition")
          .getConstructor(ResourceLocation.class,Component.class,ResourceLocation.class,ResourceLocation.class,ResourceLocation.class)
          .newInstance(ResourceLocation.fromNamespaceAndPath("fourthdimension",choice.id),Component.literal(choice.label),null,choice.underbody,choice.wheel);
      DEFINITIONS.put(choice,definition);
      return definition;
    } catch(ReflectiveOperationException failure){return SkinAdapter.nativeSkin();}
  }
  private static boolean available(ResourceLocation id) {
    if(id==null)return true;
    var models=Minecraft.getInstance().getModelManager();
    var model=models.getModel(ModelResourceLocation.standalone(id));
    if(model==models.getMissingModel())return false;
    return VALID.computeIfAbsent(model,baked->{
      var missing=ResourceLocation.withDefaultNamespace("missingno");
      if(baked.getParticleIcon(net.neoforged.neoforge.client.model.data.ModelData.EMPTY).contents().name().equals(missing))return false;
      var random=net.minecraft.util.RandomSource.create(917L);
      var quads=new java.util.ArrayList<>(baked.getQuads(null,null,random));
      for(var direction:net.minecraft.core.Direction.values())quads.addAll(baked.getQuads(null,direction,random));
      return !quads.isEmpty()&&quads.stream().noneMatch(q->q.getSprite().contents().name().equals(missing));
    }) && Boolean.TRUE.equals(OptionalCalls.staticCall("medev.coaster.skins.client.CoasterSkinClient","hasModel",id));
  }
}
