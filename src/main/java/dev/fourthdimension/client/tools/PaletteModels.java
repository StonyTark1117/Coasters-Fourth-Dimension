package dev.fourthdimension.client.tools;

import dev.fourthdimension.tools.VehiclePalette;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.integration.SkinAdapter;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.*;

/** Material substitution preserves every vertex, normal, UV location and mechanical identifier. */
public final class PaletteModels extends BakedModelWrapper<BakedModel> {
  private static final ModelProperty<VehiclePalette> PALETTE=new ModelProperty<>();
  private static final Map<ClientSubLevel,VehiclePalette> APPLIED=new WeakHashMap<>();
  private static UUID previewId;
  private static VehiclePalette preview;
  private final Map<VehiclePalette,Map<BakedQuad,BakedQuad>> cache=new EnumMap<>(VehiclePalette.class);
  public PaletteModels(BakedModel original){super(original);}
  public static void preview(UUID id,VehiclePalette choice){previewId=id;preview=choice;refresh();}
  public static void clearPreview(){previewId=null;preview=null;refresh();}
  private static VehiclePalette choice(dev.fourthdimension.runtime.FoundationData.Vehicle v){return v.chassis.equals(previewId)&&preview!=null?preview:v.palette;}
  public static void refresh(){
    var mc=Minecraft.getInstance();if(mc.level==null){APPLIED.clear();return;}
    var container=dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(mc.level);
    var data=EligibilityHooks.data(mc.level);if(container==null||data==null)return;
    for(var v:data.vehicles.values())for(var carrier:v.carriers){
      if(!(container.getSubLevel(carrier.body) instanceof ClientSubLevel body))continue;
      var selected=choice(v);var old=APPLIED.put(body,selected);
      if(old!=selected&&body.getRenderData()!=null)body.getRenderData().rebuild();
    }
  }
  @Override public ModelData getModelData(BlockAndTintGetter level,BlockPos pos,BlockState state,ModelData data){
    var client=Minecraft.getInstance().level;
    var body=client==null?null:dev.ryanhcode.sable.Sable.HELPER.getContaining(client,pos);
    var owner=SkinAdapter.owner(body);
    return originalModel.getModelData(level,pos,state,data).derive().with(PALETTE,owner==null?VehiclePalette.ORIGINAL:choice(owner)).build();
  }
  @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType type){
    var quads=originalModel.getQuads(state,side,random,data,type);var palette=data.get(PALETTE);
    if(palette==null||palette==VehiclePalette.ORIGINAL)return quads;
    synchronized(cache){var variants=cache.computeIfAbsent(palette,k->new WeakHashMap<>());return quads.stream().map(q->variants.computeIfAbsent(q,k->recolor(k,palette))).toList();}
  }
  private static BakedQuad recolor(BakedQuad quad,VehiclePalette palette){
    var original=quad.getSprite();var id=original.contents().name();
    if(!id.getNamespace().equals("fourthdimension")||!id.getPath().startsWith("block/fd917/upholstery_"))return quad;
    var next=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.fromNamespaceAndPath("fourthdimension","block/fd917/upholstery_"+palette.material));
    if(next.contents().name().equals(ResourceLocation.withDefaultNamespace("missingno")))return quad;
    var vertices=quad.getVertices().clone();int stride=vertices.length/4;
    for(int i=0;i<4;i++){int offset=i*stride;float u=Float.intBitsToFloat(vertices[offset+4]),v=Float.intBitsToFloat(vertices[offset+5]);
      vertices[offset+4]=Float.floatToRawIntBits(next.getU((u-original.getU0())/(original.getU1()-original.getU0())));
      vertices[offset+5]=Float.floatToRawIntBits(next.getV((v-original.getV0())/(original.getV1()-original.getV0())));
    }
    return new BakedQuad(vertices,quad.getTintIndex(),quad.getDirection(),next,quad.isShade(),quad.hasAmbientOcclusion());
  }
  @net.neoforged.fml.common.EventBusSubscriber(modid="fourthdimension",value=net.neoforged.api.distmarker.Dist.CLIENT)
  public static final class Events {
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){refresh();}
    @net.neoforged.bus.api.SubscribeEvent public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){APPLIED.clear();previewId=null;preview=null;}
  }
}
