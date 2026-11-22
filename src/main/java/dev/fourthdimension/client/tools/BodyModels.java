package dev.fourthdimension.client.tools;

import dev.fourthdimension.client.fd917.ProductionModels;
import dev.fourthdimension.integration.SkinAdapter;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.tools.VehicleBody;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.*;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import org.joml.Vector3f;

/** Fixed body substitution sits below the existing restraint and material wrappers. */
public final class BodyModels extends BakedModelWrapper<BakedModel> {
  private static final ModelProperty<VehicleBody> BODY = new ModelProperty<>();
  private record Applied(VehicleBody body, int generation) {}
  private static final Map<ClientSubLevel, Applied> APPLIED = new WeakHashMap<>();
  private static final Map<BakedModel, Boolean> VALID = new WeakHashMap<>();
  private static UUID previewId;
  private static VehicleBody preview;
  private final VehicleBody alternative;
  private final List<BakedQuad> alternateQuads;

  public BodyModels(BakedModel original, BlockState state, Map<ModelResourceLocation, BakedModel> models) {
    super(original);
    String name;
    if (state.getBlock() instanceof WingSeatBlock) {
      alternative = VehicleBody.WING_COMPACT;
      name = "body_wing_compact_" + state.getValue(WingSeatBlock.COLOR).getName();
    } else if (state.getBlock() instanceof FixedInvertedSeatBlock) {
      alternative = VehicleBody.FIXED_INVERTED_OPEN_FRAME;
      name = "body_fixed_inverted_open_frame" + (state.getValue(FixedInvertedSeatBlock.OUTER) ? "_outer" : "");
    } else {
      alternative = VehicleBody.BASE;
      alternateQuads = null;
      return;
    }
    var model = models.get(ModelResourceLocation.standalone(id(name)));
    if (!modelsAvailable(alternative, resource -> models.get(ModelResourceLocation.standalone(resource)))) {
      alternateQuads = null; return;
    }
    var transform = new Matrix4f();
    if (state.getBlock() instanceof WingSeatBlock) {
      float degrees = switch (state.getValue(CarrierSeatBlock.FACING)) {
        case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
      };
      transform.translate(.5f, .5f, .5f).rotateY((float)Math.toRadians(degrees)).translate(-.5f, -.5f, -.5f);
    }
    var quads = new ArrayList<BakedQuad>();
    var random = RandomSource.create(2102);
    for (var quad : model.getQuads(state, null, random)) quads.add(rotate(quad, transform));
    for (var side : Direction.values())
      for (var quad : model.getQuads(state, side, random)) quads.add(rotate(quad, transform));
    alternateQuads = List.copyOf(quads);
  }

  private static ResourceLocation id(String name) {
    return ResourceLocation.fromNamespaceAndPath("fourthdimension", "block/fd917/" + name);
  }

  private static boolean modelsAvailable(VehicleBody body, java.util.function.Function<ResourceLocation, BakedModel> lookup) {
    if (body == VehicleBody.WING_COMPACT) {
      for (var dye : net.minecraft.world.item.DyeColor.values())
        if (!available(lookup.apply(id("body_wing_compact_" + dye.getName())))) return false;
    } else if (body == VehicleBody.FIXED_INVERTED_OPEN_FRAME) {
      for (var suffix : List.of("", "_outer"))
        if (!available(lookup.apply(id("body_fixed_inverted_open_frame" + suffix)))) return false;
    }
    return true;
  }

  private static boolean available(BakedModel model) {
    if (model == null) return false;
    synchronized (VALID) {
      return VALID.computeIfAbsent(model, baked -> {
        var missing = ResourceLocation.withDefaultNamespace("missingno");
        if (baked.getParticleIcon(ModelData.EMPTY).contents().name().equals(missing)) return false;
        var quads = new ArrayList<>(baked.getQuads(null, null, RandomSource.create(2102)));
        for (var side : Direction.values()) quads.addAll(baked.getQuads(null, side, RandomSource.create(2102)));
        return !quads.isEmpty() && quads.stream().noneMatch(q -> q.getSprite().contents().name().equals(missing));
      });
    }
  }

  /** Null explicitly signals that the picker should show the original miniature. */
  public static BakedModel miniature(VehicleBody body, FoundationData.Vehicle vehicle) {
    if (!VehicleBody.supported(vehicle) || !VehicleBody.choices(vehicle).contains(body)) return null;
    var manager = Minecraft.getInstance().getModelManager();
    if (!modelsAvailable(body, resource -> manager.getModel(ModelResourceLocation.standalone(resource)))) return null;
    String name = switch (body) {
      case WING_COMPACT -> "body_wing_compact_item";
      case FIXED_INVERTED_OPEN_FRAME -> "body_fixed_inverted_open_frame_item";
      case BASE -> WingVehicle.is(vehicle) ? "body_wing_base_item" : "body_fixed_inverted_base_item";
    };
    var model = manager.getModel(ModelResourceLocation.standalone(id(name)));
    return available(model) ? model : null;
  }

  private static VehicleBody choice(FoundationData.Vehicle vehicle) {
    return vehicle.chassis.equals(previewId) && preview != null && VehicleBody.choices(vehicle).contains(preview)
        ? preview : VehicleBody.resolve(vehicle);
  }
  public static void preview(UUID id, VehicleBody body) { previewId = id; preview = body; refresh(); }
  public static void clearPreview() { previewId = null; preview = null; refresh(); }
  public static void resourceReloaded() {
    APPLIED.clear();
    synchronized (VALID) { VALID.clear(); }
  }
  public static void refresh() {
    var level = Minecraft.getInstance().level;
    if (level == null) { APPLIED.clear(); return; }
    var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level);
    var data = EligibilityHooks.data(level);
    if (container == null || data == null) return;
    for (var vehicle : data.vehicles.values()) {
      if (!VehicleBody.supported(vehicle)) continue;
      var selected = new Applied(choice(vehicle), ProductionModels.generation());
      for (var carrier : vehicle.carriers) {
        if (!(container.getSubLevel(carrier.body) instanceof ClientSubLevel body)) continue;
        if (!selected.equals(APPLIED.put(body, selected)) && body.getRenderData() != null)
          body.getRenderData().rebuild();
      }
    }
  }

  @Override public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
    var client = Minecraft.getInstance().level;
    var body = client == null ? null : dev.ryanhcode.sable.Sable.HELPER.getContaining(client, pos);
    var owner = SkinAdapter.owner(body);
    return originalModel.getModelData(level, pos, state, data).derive()
        .with(BODY, owner == null ? VehicleBody.BASE : choice(owner)).build();
  }
  @Override public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData data, RenderType type) {
    if (alternateQuads != null && data.get(BODY) == alternative)
      return side == null ? alternateQuads : List.of();
    return originalModel.getQuads(state, side, random, data, type);
  }

  private static BakedQuad rotate(BakedQuad source, Matrix4f matrix) {
    int[] data = source.getVertices().clone();
    int stride = data.length / 4;
    var d = source.getDirection();
    var normal = new Matrix3f(matrix).transform(new Vector3f(d.getStepX(), d.getStepY(), d.getStepZ())).normalize();
    int packed = ((byte)Math.round(normal.x * 127) & 255)
        | (((byte)Math.round(normal.y * 127) & 255) << 8) | (((byte)Math.round(normal.z * 127) & 255) << 16);
    for (int i = 0; i < 4; i++) {
      int at = i * stride;
      var p = matrix.transformPosition(new Vector3f(Float.intBitsToFloat(data[at]), Float.intBitsToFloat(data[at + 1]), Float.intBitsToFloat(data[at + 2])));
      data[at] = Float.floatToRawIntBits(p.x); data[at + 1] = Float.floatToRawIntBits(p.y); data[at + 2] = Float.floatToRawIntBits(p.z);
      data[at + 7] = packed;
    }
    return new BakedQuad(data, source.getTintIndex(), Direction.getNearest(normal.x, normal.y, normal.z),
        source.getSprite(), source.isShade(), source.hasAmbientOcclusion());
  }

  @net.neoforged.fml.common.EventBusSubscriber(modid="fourthdimension", value=net.neoforged.api.distmarker.Dist.CLIENT)
  public static final class Events {
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) { refresh(); }
    @net.neoforged.bus.api.SubscribeEvent public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
      APPLIED.clear(); previewId = null; preview = null;
      synchronized (VALID) { VALID.clear(); }
    }
  }
}
