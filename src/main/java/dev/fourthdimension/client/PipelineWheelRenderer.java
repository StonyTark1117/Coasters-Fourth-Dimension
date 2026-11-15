package dev.fourthdimension.client;

import dev.fourthdimension.client.fd917.ProductionMesh;
import dev.fourthdimension.client.fd917.ProductionModels;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.PipelineVehicle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3d;

/** Two articulated side-rail trucks; the native chassis remains the only physical running body. */
@EventBusSubscriber(modid="fourthdimension", value=Dist.CLIENT)
public final class PipelineWheelRenderer {
  private static final Map<ClientSubLevel, Spin[]> SPINS = new WeakHashMap<>();
  private record Spin(Vector3d position, double distance) {}
  private static Object lastLevel;
  private static int generation = -1;
  private static long edgeTick = Long.MIN_VALUE;
  private static final Map<UUID, List<CoasterPathEdge>> ROUTES = new HashMap<>();
  private PipelineWheelRenderer() {}

  @SubscribeEvent public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
    SPINS.clear();ROUTES.clear();lastLevel=null;edgeTick=Long.MIN_VALUE;
  }
  @SubscribeEvent public static void render(RenderLevelStageEvent event) {
    if (event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
    var mc=Minecraft.getInstance();
    if (lastLevel!=mc.level || generation!=ProductionModels.generation()) {
      SPINS.clear(); ROUTES.clear(); edgeTick=Long.MIN_VALUE;
      lastLevel=mc.level; generation=ProductionModels.generation();
    }
    if (mc.level==null) return;
    var data=EligibilityHooks.data(mc.level); var container=SubLevelContainer.getContainer(mc.level);
    if (data==null || container==null) return;
    if (edgeTick!=mc.level.getGameTime()) {
      ROUTES.clear();
      for (var vehicle : data.vehicles.values()) if (PipelineVehicle.is(vehicle)) {
        var matching=data.rails.stream().filter(r -> r.valid && r.family==Family.PIPELINE).toList();
        var bound=matching.stream().filter(r -> r.matches(vehicle.boundFrom,vehicle.boundTo)).findFirst().orElse(null);
        if (bound==null) continue;
        var candidates=new ArrayList<dev.fourthdimension.runtime.FoundationData.Rail>();
        candidates.add(bound);
        // A bogie may cross either end of the current authoritative edge. Follow only
        // unambiguous graph continuations; nearby disconnected or branching rails cannot win.
        for (var start : List.of(bound.from,bound.to)) {
          var node=start; var previous=bound;
          double remaining=3;
          while (remaining>0 && candidates.size()<9) {
            var at=node;
            var incident=matching.stream().filter(r -> r.from.equals(at)||r.to.equals(at)).toList();
            if (incident.size()!=2) break;
            var next=incident.getFirst()==previous?incident.getLast():incident.getFirst();
            if(candidates.contains(next)) break;
            var edge=TrackGeometry.resolve(mc.level,next); if(edge==null) break;
            candidates.add(next); remaining-=TrackGeometry.table(mc.level,edge).length();
            node=next.from.equals(at)?next.to:next.from; previous=next;
          }
        }
        ROUTES.put(vehicle.chassis,candidates.stream().map(r -> TrackGeometry.resolve(mc.level,r))
            .filter(Objects::nonNull).toList());
      }
      edgeTick=mc.level.getGameTime();
    }
    long renderStarted=dev.fourthdimension.client.fd917.MechanismRenderMetrics.start();
    var camera=event.getCamera().getPosition(); var stack=event.getPoseStack();
    stack.pushPose(); stack.translate(-camera.x,-camera.y,-camera.z);
    var mesh=new ProductionMesh(stack.last());
    for (var vehicle:data.vehicles.values()) {
      if (!PipelineVehicle.is(vehicle) || !(container.getSubLevel(vehicle.chassis) instanceof ClientSubLevel body)) continue;
      var pose=body.renderPose(); var p=body.getPlot().getCenterBlock();
      var center=pose.transformPosition(new Vector3d(p.getX()+.5,p.getY()+.1875,p.getZ()+.5));
      if (center.distanceSquared(camera.x,camera.y,camera.z)>128*128) { SPINS.remove(body); continue; }
      var spins=SPINS.computeIfAbsent(body, ignored -> new Spin[2]);
      for (int end=0;end<2;end++) {
        double z=end==0?-2.3:2.3;
        var expected=pose.transformPosition(new Vector3d(p.getX()+.5,p.getY()+.1875,p.getZ()+.5+z));
        var hit=CoasterPathTrackFrame.nearestGraphHit(mc.level,ROUTES.getOrDefault(vehicle.chassis,List.of()),new Vec3(expected.x,expected.y,expected.z),1);
        if (hit==null) { spins[end]=null; continue; }
        var basis=CoasterPathTrackFrame.trackBasisAtHit(mc.level,hit);
        var origin=vec(hit.point()); var up=vec(basis.up()); var right=vec(basis.right());
        var bodyRight=new Vector3d(1,0,0).rotate(pose.orientation());
        if (right.dot(bodyRight)<0) right.negate();
        var forward=new Vector3d(up).cross(right).normalize();
        // Keep phase sign tied to the chassis forward frame even if an edge's canonical order changes.
        var old=spins[end]; double travel=0;
        if (old!=null && old.position.distanceSquared(origin)<4)
          travel=old.distance+new Vector3d(origin).sub(old.position).dot(forward);
        spins[end]=new Spin(new Vector3d(origin),travel);
        if(!ProductionMesh.visible(event,origin,4))continue;
        int light=LevelRenderer.getLightColor(mc.level,BlockPos.containing(origin.x,origin.y,origin.z));
        for(int side:new int[]{-1,1}) {
          var rail=new Vector3d(origin).fma(side*1.5,right).fma(TrackDesign.MAIN_RAIL_HEIGHT,up);
          var upper=new Vector3d(rail).fma(TrackDesign.RAIL_HALF_HEIGHT+.20,up);
          var lower=new Vector3d(rail).fma(-TrackDesign.RAIL_HALF_HEIGHT-.20,up);
          var lateral=new Vector3d(rail).fma(side*(TrackDesign.RAIL_HALF_WIDTH+.125),right);
          mesh.wheel(upper,right,up,.20,.22,-travel/.20,light);
          mesh.wheel(lower,right,up,.20,.22,travel/.20,light);
          mesh.wheel(lateral,up,right,.125,.18,side*travel/.125,light);
          // An inboard C bracket carries both vertical rollers without crossing the rail.
          var upperIn=new Vector3d(upper).fma(-side*.23,right);
          var lowerIn=new Vector3d(lower).fma(-side*.23,right);
          mesh.beam(upper,upperIn,up,.065,.065,"machined",light);
          mesh.beam(lower,lowerIn,up,.065,.065,"machined",light);
          mesh.beam(upperIn,lowerIn,forward,.09,.09,"structural",light);
          var topSide=new Vector3d(lateral).fma(.20,up);
          mesh.beam(lateral,topSide,right,.055,.055,"machined",light);
          mesh.beam(topSide,upperIn,up,.075,.075,"structural",light);
          var mount=pose.transformPosition(new Vector3d(p.getX()+.5+side*1.4,p.getY()+.1875+.55,p.getZ()+.5+z));
          mesh.beam(upperIn,mount,up,.10,.10,"structural",light);
          mesh.wheel(mount,forward,up,.10,.10,0,light);
        }
      }
    }
    stack.popPose(); mc.renderBuffers().bufferSource().endBatch(Sheets.cutoutBlockSheet());
    dev.fourthdimension.client.fd917.MechanismRenderMetrics.finish("pipeline",renderStarted);
  }
  private static Vector3d vec(Vec3 v) { return new Vector3d(v.x,v.y,v.z); }
}
