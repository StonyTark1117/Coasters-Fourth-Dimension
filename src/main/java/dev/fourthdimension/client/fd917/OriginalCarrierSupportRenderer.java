package dev.fourthdimension.client.fd917;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3d;

/** The original non-Arrow rotary carriers' stationary yokes and rotating axle cranks. */
@EventBusSubscriber(modid="fourthdimension", value=Dist.CLIENT)
public final class OriginalCarrierSupportRenderer {
  private OriginalCarrierSupportRenderer() {}

  @SubscribeEvent
  public static void render(RenderLevelStageEvent event) {
    if (event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
    var mc=Minecraft.getInstance();
    if (mc.level==null) return;
    var data=EligibilityHooks.data(mc.level);
    var container=SubLevelContainer.getContainer(mc.level);
    if (data==null || container==null) return;
    var camera=event.getCamera().getPosition();
    var stack=event.getPoseStack();
    stack.pushPose();
    stack.translate(-camera.x,-camera.y,-camera.z);
    var mesh=new ProductionMesh(stack.last());
    for (var vehicle:data.vehicles.values()) {
      if (!supported(vehicle.family) || FoundationData.VARIANT.equals(vehicle.variant)
          || !(container.getSubLevel(vehicle.chassis) instanceof ClientSubLevel parent)) continue;
      for (var carrier:vehicle.carriers) {
        if (!(container.getSubLevel(carrier.body) instanceof ClientSubLevel child)) continue;
        var hub=at(child,carrier.childPivotX,carrier.childPivotOffset,carrier.childPivotZ);
        if (hub.distanceSquared(camera.x,camera.y,camera.z)>128*128
            || !ProductionMesh.visible(event,hub,3)) continue;
        int light=LevelRenderer.getLightColor(mc.level,BlockPos.containing(hub.x,hub.y,hub.z));
        double sign=Math.signum(carrier.side);
        var parentAxis=new Vector3d(1,0,0).rotate(parent.renderPose().orientation());
        var parentUp=new Vector3d(0,1,0).rotate(parent.renderPose().orientation());
        var childAxis=new Vector3d(1,0,0).rotate(child.renderPose().orientation());
        var childUp=new Vector3d(0,1,0).rotate(child.renderPose().orientation());
        // The rotation axis passes through the inner seat block. Put its
        // visible bearing 0.80 blocks inboard along that SAME axis, outside
        // the seat envelope, rather than showing a race in the rider's lap.
        double bearingX=carrier.side-sign*.80;
        double journalX=carrier.childPivotX-sign*.80;
        var bearing=at(parent,bearingX,carrier.pivotHeight,carrier.longitudinal);
        var journal=at(child,journalX,carrier.childPivotOffset,carrier.childPivotZ);
        // at() adds the plot center's half-block offset. A local Y of 1.78
        // lands at model Y=2.28, inside the ordinary lower flange and the
        // Revised row's lowered upper flange. The hinge sits off that plane,
        // so the yoke must reach the beam before it reaches the bearing.
        // Bury the inner end in the second row cell, so its visible edge
        // cannot stop short of the baked transverse beam in a front view.
        var strutMount=at(parent,carrier.side-sign*1.75,1.78,carrier.longitudinal);
        var bearingShoulder=at(parent,bearingX,1.78,carrier.longitudinal);
        mesh.beam(strutMount,bearingShoulder,parentUp,.19,.16,"structural",light);
        mesh.beam(bearingShoulder,bearing,parentAxis,.19,.16,"structural",light);
        mesh.ring(bearing,parentAxis,parentUp,.08,.19,.18,"structural",light);
        mesh.ring(journal,childAxis,childUp,.045,.11,.18,"machined",light);
        // The baked axle is eccentric to the physical hinge. A rotating crank
        // joins the real hub to that axle in the child frame on the inboard side.
        double axleZ=vehicle.family==Family.ZACSPIN ? 11.75/16-.5
            : vehicle.family==Family.FREE_SPIN || vehicle.family==Family.ZACSPIN_REVISED
                ? -6.0/16-.5 : -5.5/16-.5;
        double crankX=carrier.childPivotX-sign*.60;
        var crankRoot=at(child,crankX,carrier.childPivotOffset,carrier.childPivotZ);
        // Revised lowers its real hinge by half a block; its baked axle remains
        // at the ordinary block Y=.5. The riser is essential for that family.
        var axleRoot=at(child,crankX,0,carrier.childPivotZ);
        var axleForward=at(child,crankX,0,carrier.childPivotZ+axleZ);
        var axleContact=at(child,carrier.childPivotX-sign*.4375,0,carrier.childPivotZ+axleZ);
        mesh.beam(journal,crankRoot,childUp,.10,.10,"machined",light);
        mesh.beam(crankRoot,axleRoot,childAxis,.10,.10,"structural",light);
        mesh.beam(axleRoot,axleForward,childAxis,.10,.10,"structural",light);
        mesh.beam(axleForward,axleContact,childUp,.10,.10,"structural",light);
      }
    }
    stack.popPose();
    mc.renderBuffers().bufferSource().endBatch(Sheets.cutoutBlockSheet());
  }

  private static boolean supported(Family family) {
    return family==Family.ZACSPIN || family==Family.FREE_SPIN
        || family==Family.ZACSPIN_REVISED || family==Family.PAX;
  }

  private static Vector3d at(ClientSubLevel body,double x,double y,double z) {
    var p=body.getPlot().getCenterBlock();
    return body.renderPose().transformPosition(new Vector3d(
        p.getX()+.5+x,p.getY()+.5+y,p.getZ()+.5+z));
  }
}
