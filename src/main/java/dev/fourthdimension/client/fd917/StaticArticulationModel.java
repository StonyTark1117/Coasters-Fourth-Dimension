package dev.fourthdimension.client.fd917;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import org.joml.Vector3f;

/** Ponder and placed seats share the exact hinged mesh; native plots use the animated owner only. */
final class StaticArticulationModel extends BakedModelWrapper<BakedModel> {
  private static final ModelProperty<Boolean> DYNAMIC = new ModelProperty<>();
  private final BlockState state;
  private final List<BakedQuad> endpoints;
  StaticArticulationModel(BakedModel original, BlockState state, List<ProductionModels.Part> parts,
                         Map<ModelResourceLocation,BakedModel> models) {
    super(original); this.state=state;
    var quads=new ArrayList<BakedQuad>();
    for(var part:parts) if(part.matches(state)) {
      var model=models.get(ModelResourceLocation.standalone(part.model()));
      if(model==null)throw new IllegalStateException("Missing articulated model "+part.model());
      var transform=transform(state,part);
      var random=RandomSource.create(917);
      for(var quad:model.getQuads(state,null,random))quads.add(rotate(quad,transform));
      for(var side:Direction.values())for(var quad:model.getQuads(state,side,random))quads.add(rotate(quad,transform));
    }
    endpoints=List.copyOf(quads);
  }
  @Override public ModelData getModelData(BlockAndTintGetter level,BlockPos pos,BlockState state,ModelData data) {
    var client=Minecraft.getInstance().level;
    boolean dynamic=client!=null && dev.ryanhcode.sable.Sable.HELPER.getContaining(client,pos)!=null;
    return super.getModelData(level,pos,state,data).derive().with(DYNAMIC,dynamic).build();
  }
  @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random) {
    return combined(originalModel.getQuads(state,side,random),side);
  }
  @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType type) {
    var base=originalModel.getQuads(state,side,random,data,type);
    return Boolean.TRUE.equals(data.get(DYNAMIC))?base:combined(base,side);
  }
  private List<BakedQuad> combined(List<BakedQuad> base,Direction side) {
    if(side!=null || endpoints.isEmpty())return base;
    var all=new ArrayList<>(base);all.addAll(endpoints);return all;
  }
  private static Matrix4f transform(BlockState state,ProductionModels.Part part) {
    var matrix=new Matrix4f();
    if(part.facing() && state.hasProperty(dev.fourthdimension.vehicle.CarrierSeatBlock.FACING)) {
      float angle=switch(state.getValue(dev.fourthdimension.vehicle.CarrierSeatBlock.FACING)) {
        case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
      };
      matrix.translate(.5f,.5f,.5f).rotateY((float)Math.toRadians(angle)).translate(-.5f,-.5f,-.5f);
    }
    matrix.translate((float)part.x(),(float)part.y(),(float)part.z());
    float angle=part.closed(state)?0:(float)Math.toRadians(part.openDegrees());
    switch(part.axis()){case "y"->matrix.rotateY(angle);case "z"->matrix.rotateZ(angle);default->matrix.rotateX(angle);}
    return matrix.translate((float)-part.x(),(float)-part.y(),(float)-part.z());
  }
  private static BakedQuad rotate(BakedQuad source,Matrix4f matrix) {
    int[] data=source.getVertices().clone();int stride=data.length/4;
    var direction=source.getDirection();
    var normal=new Matrix3f(matrix).transform(new Vector3f(direction.getStepX(),direction.getStepY(),direction.getStepZ())).normalize();
    int packed=((byte)Math.round(normal.x*127)&255)|(((byte)Math.round(normal.y*127)&255)<<8)|(((byte)Math.round(normal.z*127)&255)<<16);
    for(int i=0;i<4;i++){
      int at=i*stride;
      var p=matrix.transformPosition(new Vector3f(Float.intBitsToFloat(data[at]),Float.intBitsToFloat(data[at+1]),Float.intBitsToFloat(data[at+2])));
      data[at]=Float.floatToRawIntBits(p.x);data[at+1]=Float.floatToRawIntBits(p.y);data[at+2]=Float.floatToRawIntBits(p.z);
      data[at+7]=packed;
    }
    return new BakedQuad(data,source.getTintIndex(),Direction.getNearest(normal.x,normal.y,normal.z),source.getSprite(),source.isShade(),source.hasAmbientOcclusion());
  }
}
