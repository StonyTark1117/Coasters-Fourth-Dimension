package dev.fourthdimension.client.moving;
import dev.fourthdimension.client.fd917.ProductionMesh;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3d;
/** Real native bed pose drives the carriage stays; a completed timer cannot draw a locked dock. */
@EventBusSubscriber(modid="fourthdimension",value=Dist.CLIENT)
public final class MovingMachineryRenderer {
  @SubscribeEvent public static void render(RenderLevelStageEvent event){
    if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)return;var mc=Minecraft.getInstance();if(mc.level==null)return;var data=EligibilityHooks.data(mc.level);if(data==null)return;
    var container=SubLevelContainer.getContainer(mc.level);var camera=event.getCamera().getPosition();var stack=event.getPoseStack();stack.pushPose();stack.translate(-camera.x,-camera.y,-camera.z);var mesh=new ProductionMesh(stack.last());
    for(var state:data.moving.assemblies.values()){
      if(state.program==null||!(container.getSubLevel(state.bed) instanceof ClientSubLevel bed))continue;
      var nativePose=bed.renderPose();var c=bed.getPlot().getCenterBlock();var p=nativePose.transformPosition(new Vector3d(c.getX()+.5,c.getY()+.5,c.getZ()+.5));var q=nativePose.orientation();
      if(p.distanceSquared(camera.x,camera.y,camera.z)>192*192||!ProductionMesh.visible(event,p,state.deckLength+40))continue;
      var actual=new MovingAssemblyState.Pose(p.x,p.y,p.z,q.x(),q.y(),q.z(),q.w());var geometry=MovingMachineryGeometry.geometry(state,actual);int light=LevelRenderer.getLightColor(mc.level,BlockPos.containing(p.x,p.y,p.z));
      for(var beam:geometry.fixed())mesh.beam(beam.from(),beam.to(),new Vector3d(0,1,0),beam.width(),beam.width(),beam.material(),light);
      for(var beam:geometry.moving())mesh.beam(beam.from(),beam.to(),new Vector3d(0,1,0),beam.width(),beam.width(),beam.material(),light);
      for(var ring:geometry.races())mesh.ring(ring.center(),ring.axis(),ring.radial(),ring.inner(),ring.outer(),ring.width(),"machined",light);
    }
    stack.popPose();mc.renderBuffers().bufferSource().endBatch(Sheets.cutoutBlockSheet());
  }
}
