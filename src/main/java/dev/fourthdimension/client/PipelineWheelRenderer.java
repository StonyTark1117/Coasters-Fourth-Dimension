package dev.fourthdimension.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.vehicle.PipelineVehicle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3d;
import org.joml.Matrix4f;

/** Original twelve-sided run/upstop/side rollers in the physical native chassis frame. */
@EventBusSubscriber(modid="fourthdimension",value=Dist.CLIENT)
public final class PipelineWheelRenderer {
  private static final java.util.Map<ClientSubLevel,Spin> SPINS=new java.util.WeakHashMap<>();
  private record Spin(Vector3d position,double distance){}
  private PipelineWheelRenderer(){}
  @SubscribeEvent public static void render(RenderLevelStageEvent event){
    if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)return;
    var mc=Minecraft.getInstance();if(mc.level==null)return;
    var data=EligibilityHooks.data(mc.level);var container=SubLevelContainer.getContainer(mc.level);
    if(data==null||container==null)return;
    var buffers=mc.renderBuffers().bufferSource();var out=buffers.getBuffer(RenderType.debugQuads());
    var camera=event.getCamera().getPosition();var stack=event.getPoseStack();stack.pushPose();
    stack.translate(-camera.x,-camera.y,-camera.z);var matrix=stack.last().pose();
    for(var v:data.vehicles.values()){
      if(!PipelineVehicle.is(v)||!(container.getSubLevel(v.chassis) instanceof ClientSubLevel body))continue;
      var pose=body.renderPose();var p=body.getPlot().getCenterBlock();
      var center=pose.transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5));
      if(center.distanceSquared(camera.x,camera.y,camera.z)>160*160)continue;
      // Cylindrical treads are rotationally symmetric; alternating hub facets expose travel spin.
      var old=SPINS.get(body);double travel=old==null?0:old.distance;
      if(old!=null&&old.position.distanceSquared(center)<4){
        var forward=new Vector3d(0,0,-1).rotate(pose.orientation());
        travel+=new Vector3d(center).sub(old.position).dot(forward);
      }
      SPINS.put(body,new Spin(new Vector3d(center),travel));
      double spin=travel/.20;
      for(int side:new int[]{-1,1})for(double z:new double[]{-2.3,2.3}){
        for(double y:new double[]{.60,.05})wheel(out,matrix,pose,p,side*1.5,y,z,.20,.22,false,spin*(y>.3?1:-1));
        wheel(out,matrix,pose,p,side*1.75625,.325,z,.125,.18,true,travel/.125*side);
      }
    }
    stack.popPose();buffers.endBatch(RenderType.debugQuads());
  }
  private static void wheel(VertexConsumer out,Matrix4f matrix,dev.ryanhcode.sable.companion.math.Pose3dc pose,
      net.minecraft.core.BlockPos p,double x,double y,double z,double radius,double width,boolean vertical,double spin){
    for(int i=0;i<12;i++){
      double a=i*Math.PI/6+spin,b=(i+1)*Math.PI/6+spin;
      var a0=point(x,y,z,radius,width,-1,a,vertical);var a1=point(x,y,z,radius,width,1,a,vertical);
      var b0=point(x,y,z,radius,width,-1,b,vertical);var b1=point(x,y,z,radius,width,1,b,vertical);
      quad(out,matrix,pose,p,a0,b0,b1,a1,0x30383e);
      var c0=point(x,y,z,0,width,-1,0,vertical);var c1=point(x,y,z,0,width,1,0,vertical);
      int color=i%3==0?0xe0b345:0xa4b0b7;
      quad(out,matrix,pose,p,c0,c0,b0,a0,color);quad(out,matrix,pose,p,c1,c1,a1,b1,color);
    }
  }
  private static Vector3d point(double x,double y,double z,double r,double width,int side,double angle,boolean vertical){
    return vertical?new Vector3d(x+r*Math.cos(angle),y+side*width/2,z+r*Math.sin(angle)):
        new Vector3d(x+side*width/2,y+r*Math.cos(angle),z+r*Math.sin(angle));
  }
  private static void quad(VertexConsumer out,Matrix4f matrix,dev.ryanhcode.sable.companion.math.Pose3dc pose,
      net.minecraft.core.BlockPos p,Vector3d a,Vector3d b,Vector3d c,Vector3d d,int color){
    for(var v:new Vector3d[]{a,b,c,d}){
      var world=pose.transformPosition(new Vector3d(v).add(p.getX()+.5,p.getY()+.1875,p.getZ()+.5));
      out.addVertex(matrix,(float)world.x,(float)world.y,(float)world.z).setColor(color>>16&255,color>>8&255,color&255,255);
    }
  }
}
