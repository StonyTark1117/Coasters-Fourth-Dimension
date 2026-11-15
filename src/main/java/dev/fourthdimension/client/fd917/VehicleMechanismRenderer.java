package dev.fourthdimension.client.fd917;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
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

/** Bearings, paired brake locks and reaction surfaces on their real native body frames. */
@EventBusSubscriber(modid="fourthdimension",value=Dist.CLIENT)
public final class VehicleMechanismRenderer {
  private VehicleMechanismRenderer() {}
  @SubscribeEvent public static void render(RenderLevelStageEvent event) {
    if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)return;
    var mc=Minecraft.getInstance();if(mc.level==null)return;
    var data=EligibilityHooks.data(mc.level);var container=SubLevelContainer.getContainer(mc.level);
    if(data==null||container==null)return;
    long renderStarted=dev.fourthdimension.client.fd917.MechanismRenderMetrics.start();
    var camera=event.getCamera().getPosition();var stack=event.getPoseStack();
    stack.pushPose();stack.translate(-camera.x,-camera.y,-camera.z);var mesh=new ProductionMesh(stack.last());
    for(var v:data.vehicles.values()) {
      if(!(container.getSubLevel(v.chassis) instanceof ClientSubLevel chassis))continue;
      boolean joints=AxisVehicle.is(v)||RollerBallVehicle.is(v)||FlyingVehicle.is(v)||NewGenerationFlyingVehicle.is(v)
          ||SpinningVehicle.is(v)||S2SpinningVehicle.is(v)||PoweredInvertedYawVehicle.is(v)||SuspendedVehicle.is(v);
      for(var c:v.carriers) {
        if(!(container.getSubLevel(c.body) instanceof ClientSubLevel child))continue;
        var parent=NewGenerationFlyingVehicle.is(v)&&container.getSubLevel(v.yaw.deck) instanceof ClientSubLevel deck?deck:chassis;
        var hub=at(child,c.childPivotX,c.childPivotOffset,c.childPivotZ);
        if(hub.distanceSquared(camera.x,camera.y,camera.z)>128*128||!ProductionMesh.visible(event,hub,4))continue;
        int light=light(hub);
        if(FlyingVehicle.is(v)||NewGenerationFlyingVehicle.is(v)) {
          flyingMount(mesh,parent,child,hub,at(parent,c.side,c.pivotHeight,c.longitudinal),
              NewGenerationFlyingVehicle.is(v)?1.37:2.10,c.latched||c.liftHeld,light);
        } else if(joints) {
          boolean y=SpinningVehicle.is(v)||S2SpinningVehicle.is(v)||PoweredInvertedYawVehicle.is(v);
          boolean z=AxisVehicle.is(v)||SuspendedVehicle.is(v);
          var localAxis=y?new Vector3d(0,1,0):z?new Vector3d(0,0,1):new Vector3d(1,0,0);
          var localRadial=y?new Vector3d(1,0,0):new Vector3d(0,1,0);
          var axis=new Vector3d(localAxis).rotate(parent.renderPose().orientation());
          var radial=new Vector3d(localRadial).rotate(parent.renderPose().orientation());
          var childAxis=new Vector3d(localAxis).rotate(child.renderPose().orientation());
          var childRadial=new Vector3d(localRadial).rotate(child.renderPose().orientation());
          var fixed=at(parent,c.side,c.pivotHeight,c.longitudinal);
          // Structural race belongs to the parent; receiver and spokes rotate with the child.
          mesh.ring(fixed,axis,radial,.18,.29,.34,"structural",light);
          mesh.ring(hub,childAxis,childRadial,.12,.17,.42,"machined",light);
          var disc=new Vector3d(hub).fma(.30,childAxis);
          mesh.ring(disc,childAxis,childRadial,.08,.32,.06,"machined",light);
          for(int i=0;i<4;i++) {
            var spoke=new Vector3d(childRadial).rotateAxis(i*Math.PI/2,childAxis.x,childAxis.y,childAxis.z);
            mesh.beam(new Vector3d(disc).fma(.08,spoke),new Vector3d(disc).fma(.28,spoke),childAxis,.045,.045,"structural",light);
          }
          boolean locked=c.latched||c.liftHeld;
          double gap=locked?.033:.11;
          var caliper=new Vector3d(fixed).fma(.30,axis).fma(.26,radial);
          for(int side:new int[]{-1,1}) {
            var pad=new Vector3d(caliper).fma(side*gap,axis);
            mesh.beam(new Vector3d(pad).fma(-.065,radial),new Vector3d(pad).fma(.065,radial),axis,.13,.045,"rubber",light);
            mesh.beam(new Vector3d(fixed).fma(side*.24+.30,axis).fma(.39,radial),pad,radial,.065,.065,"structural",light);
          }
          mesh.beam(new Vector3d(caliper).fma(.13,radial),new Vector3d(caliper).fma(.22,radial),axis,.08,.06,
              locked?"indicator_hold":"indicator_off",light);
        }
        if(v.family==Family.FREE_SPIN&&!FoundationData.VARIANT.equals(v.variant)) {
          // Coaxial annulus stays at the same radius for every rotor angle; it never rubs the span.
          var axis=new Vector3d(1,0,0).rotate(child.renderPose().orientation());
          var radial=new Vector3d(0,1,0).rotate(child.renderPose().orientation());
          double innerSide=Math.copySign(1.6,c.side)-c.side;
          var ring=new Vector3d(hub).fma(innerSide,axis);
          mesh.ring(ring,axis,radial,1.32,1.48,.08,"machined",light);
          mesh.beam(hub,ring,radial,.16,.16,"structural",light);
          for(int i=0;i<4;i++) {
            var direction=new Vector3d(radial).rotateAxis(i*Math.PI/2,axis.x,axis.y,axis.z);
            mesh.beam(ring,new Vector3d(ring).fma(1.34,direction),axis,.07,.07,"structural",light);
          }
        }
        if(S2SpinningVehicle.is(v)) {
          var axis=new Vector3d(0,1,0).rotate(child.renderPose().orientation());
          var radial=new Vector3d(1,0,0).rotate(child.renderPose().orientation());
          var ring=new Vector3d(hub).fma(-1.10,axis);
          mesh.ring(ring,axis,radial,1.29,1.40,.16,"machined",light);
          for(int i=0;i<4;i++) {
            var direction=new Vector3d(radial).rotateAxis(i*Math.PI/2,axis.x,axis.y,axis.z);
            var base=new Vector3d(ring).fma(1.32,direction);
            mesh.beam(base,new Vector3d(base).fma(.50,axis),direction,.07,.07,"structural",light);
          }
        }
      }
      // Existing server snapshots supply these remote-controller cues; no fake physical holding jaw.
      if(DiveVehicle.is(v)&&v.dive.controller!=null) {
        String cue=!v.fault.isBlank()||v.dive.needsResume?"indicator_fault":
            v.dive.phase==DiveState.Phase.HELD?"indicator_hold":
            v.dive.phase==DiveState.Phase.RELEASE_ARMED?"indicator_ready":"indicator_off";
        control(mesh,v.dive.controller,cue,camera,DiveHoldEntity.class);
      }
      if(S2SpinningVehicle.is(v)&&v.s2.governor!=null)
        control(mesh,v.s2.governor,!v.fault.isBlank()||v.s2.resumeRequired?"indicator_fault":v.stopping?"indicator_hold":"indicator_off",camera,S2SpinningGovernorEntity.class);
      if(PoweredInvertedYawVehicle.is(v)&&v.poweredYaw.controller!=null)
        control(mesh,v.poweredYaw.controller,!v.fault.isBlank()||v.poweredYaw.resumeRequired?"indicator_fault":v.poweredYaw.holdAcknowledged?"indicator_hold":"indicator_off",camera,PoweredInvertedYawGovernorEntity.class);
    }
    stack.popPose();mc.renderBuffers().bufferSource().endBatch(Sheets.cutoutBlockSheet());
    dev.fourthdimension.client.fd917.MechanismRenderMetrics.finish("joints",renderStarted);
  }
  private static void flyingMount(ProductionMesh mesh,ClientSubLevel parent,ClientSubLevel child,
      Vector3d hub,Vector3d fixed,double halfWidth,boolean locked,int light) {
    var axis=new Vector3d(1,0,0).rotate(parent.renderPose().orientation());
    var radial=new Vector3d(0,1,0).rotate(parent.renderPose().orientation());
    var childAxis=new Vector3d(1,0,0).rotate(child.renderPose().orientation());
    var childRadial=new Vector3d(0,1,0).rotate(child.renderPose().orientation());
    // Actual transverse posture shaft reaches both existing parent fork arms and every seat pan.
    mesh.beam(new Vector3d(hub).fma(-halfWidth,childAxis),new Vector3d(hub).fma(halfWidth,childAxis),childRadial,.12,.12,"machined",light);
    for(int side:new int[]{-1,1}) {
      var end=new Vector3d(hub).fma(side*halfWidth,childAxis);
      var socket=new Vector3d(fixed).fma(side*halfWidth,axis);
      if(halfWidth>2)mesh.beam(socket,new Vector3d(fixed).fma(side*2.21875,axis),radial,.12,.12,"structural",light);
      mesh.ring(socket,axis,radial,.105,.20,.15,"structural",light);
      mesh.ring(end,childAxis,childRadial,.04,.10,.18,"machined",light);
      mesh.ring(end,childAxis,childRadial,.04,.24,.025,"machined",light);
      var caliper=new Vector3d(socket).fma(.20,radial);
      for(int face:new int[]{-1,1}) {
        var pad=new Vector3d(caliper).fma(face*(locked?.025:.07),axis);
        mesh.beam(new Vector3d(pad).fma(-.04,radial),new Vector3d(pad).fma(.04,radial),axis,.075,.025,"rubber",light);
        mesh.beam(new Vector3d(socket).fma(.29,radial),pad,axis,.045,.045,"structural",light);
      }
      mesh.beam(new Vector3d(socket).fma(.27,radial),new Vector3d(socket).fma(.33,radial),axis,.06,.04,locked?"indicator_hold":"indicator_off",light);
    }
  }
  private static void control(ProductionMesh mesh,BlockPos pos,String cue,net.minecraft.world.phys.Vec3 camera,Class<?> controllerType) {
    var center=new Vector3d(pos.getX()+.5,pos.getY()+.96,pos.getZ()+.5);
    if(center.distanceSquared(camera.x,camera.y,camera.z)>64*64||!Minecraft.getInstance().level.hasChunkAt(pos)
        ||!controllerType.isInstance(Minecraft.getInstance().level.getBlockEntity(pos)))return;
    mesh.beam(new Vector3d(center).add(-.22,0,0),new Vector3d(center).add(.22,0,0),new Vector3d(0,1,0),.16,.08,cue,light(center));
  }
  private static Vector3d at(ClientSubLevel body,double x,double y,double z) {
    var p=body.getPlot().getCenterBlock();return body.renderPose().transformPosition(new Vector3d(p.getX()+.5+x,p.getY()+.5+y,p.getZ()+.5+z));
  }
  private static int light(Vector3d p) {return LevelRenderer.getLightColor(Minecraft.getInstance().level,BlockPos.containing(p.x,p.y,p.z));}
}
