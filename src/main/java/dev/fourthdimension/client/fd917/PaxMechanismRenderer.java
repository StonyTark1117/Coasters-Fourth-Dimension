package dev.fourthdimension.client.fd917;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.shuttle.Fd917ShuttleSync;
import dev.fourthdimension.track.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3d;

/** Remote kinetic power, local rack drive, real guided carriage and native catch anchors. */
@EventBusSubscriber(modid="fourthdimension",value=Dist.CLIENT)
public final class PaxMechanismRenderer {
  private record Segment(Vector3d a,Vector3d b,Vector3d up,Vector3d right,boolean support) {}
  private record Route(long key,List<CoasterPathEdge> edges,Map<CoasterPathEdge,Integer> signs,List<Segment> rack) {}
  private record Travel(Vector3d position,double distance) {}
  private static final Map<BlockPos,Route> ROUTES=new HashMap<>();
  private static final Map<ClientSubLevel,Travel> TRAVEL=new WeakHashMap<>();
  private static Object lastLevel;
  private static long nearbyTick=Long.MIN_VALUE;
  private static BlockPos nearbyCenter;
  private static final List<dev.fourthdimension.shuttle.ShuttleWinchEntity> LOCAL=new ArrayList<>();
  private static int generation=-1;
  private PaxMechanismRenderer() {}

  @SubscribeEvent public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
    ROUTES.clear();TRAVEL.clear();LOCAL.clear();nearbyTick=Long.MIN_VALUE;nearbyCenter=null;lastLevel=null;
  }
  @SubscribeEvent public static void render(RenderLevelStageEvent event) {
    if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)return;
    var mc=Minecraft.getInstance();
    if(lastLevel!=mc.level||generation!=ProductionModels.generation()) {
      ROUTES.clear();TRAVEL.clear();LOCAL.clear();nearbyTick=Long.MIN_VALUE;nearbyCenter=null;lastLevel=mc.level;generation=ProductionModels.generation();
    }
    if(mc.level==null)return;
    var data=EligibilityHooks.data(mc.level);var container=SubLevelContainer.getContainer(mc.level);
    if(data==null||container==null)return;
    long renderStarted=dev.fourthdimension.client.fd917.MechanismRenderMetrics.start();
    var camera=event.getCamera().getPosition();var stack=event.getPoseStack();
    stack.pushPose();stack.translate(-camera.x,-camera.y,-camera.z);var mesh=new ProductionMesh(stack.last());
    var seen=new HashSet<BlockPos>();
    for(var state:states(mc,BlockPos.containing(camera))) {
      seen.add(state.winch());
      var route=route(state,data);
      if(route!=null) for(var segment:route.rack) {
        var mid=new Vector3d(segment.a).lerp(segment.b,.5);
        if(mid.distanceSquared(camera.x,camera.y,camera.z)>128*128||!ProductionMesh.visible(event,mid,2))continue;
        int light=light(mid);
        mesh.beam(segment.a,segment.b,segment.up,.10,.10,"machined",light);
        if(segment.support) {
          // Rack is outside the native wheel envelope; its knee lands on the existing track cross support.
          var foot=new Vector3d(segment.a).fma(TrackDesign.RUNNING_UNDERBRACE_HEIGHT-.05,segment.up);
          var mount=new Vector3d(foot).fma(TrackDesign.RUNNING_OUTBOARD_HALF_GAUGE-1.35,segment.right);
          mesh.beam(mount,foot,segment.up,.065,.065,"structural",light);
          mesh.beam(foot,segment.a,new Vector3d(segment.b).sub(segment.a),.065,.065,"structural",light);
        }
      }
      double phase=0;
      if(state.carriage()!=null && container.getSubLevel(state.carriage()) instanceof ClientSubLevel carriage) {
        var pose=carriage.renderPose();var p=carriage.getPlot().getCenterBlock();
        var center=pose.transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5));
        var up=new Vector3d(0,1,0).rotate(pose.orientation());
        var right=new Vector3d(1,0,0).rotate(pose.orientation());
        var forward=new Vector3d(0,0,-1).rotate(pose.orientation());
        var old=TRAVEL.get(carriage);double distance=0;
        if(old!=null && old.position.distanceSquared(center)<4)
          distance=old.distance+new Vector3d(center).sub(old.position).dot(forward);
        TRAVEL.put(carriage,new Travel(new Vector3d(center),distance));phase=distance/.20;
        if(center.distanceSquared(camera.x,camera.y,camera.z)<128*128 && ProductionMesh.visible(event,center,4)) {
          int light=light(center);
          // Native train axles sit at z=±.7 with a .2-radius wheel. The catch
          // carriage rolls beyond that envelope instead of duplicating those wheels.
          for(double z:new double[]{-1.25,1.25}) {
            mesh.beam(at(carriage,-1.15,0,z),at(carriage,1.15,0,z),up,.12,.14,"structural",light);
            var approximate=at(carriage,0,-3,z);
            var hit=route==null?null:CoasterPathTrackFrame.nearestGraphHit(mc.level,route.edges,
                new Vec3(approximate.x,approximate.y,approximate.z),1);
            if(hit==null)continue;
            var basis=CoasterPathTrackFrame.trackBasisAtHit(mc.level,hit);
            var railUp=vec(basis.up());var railRight=vec(basis.right());
            if(railRight.dot(right)<0)railRight.negate();
            for(int side:new int[]{-1,1}) {
              var wheel=vec(hit.point()).fma(side*TrackDesign.HALF_GAUGE,railRight)
                  .fma(TrackDesign.MAIN_RAIL_HEIGHT+TrackDesign.RAIL_HALF_HEIGHT+.16,railUp);
              var outer=new Vector3d(wheel).fma(side*.34,railRight);
              mesh.wheel(wheel,railRight,railUp,.16,.18,-distance/.16,light);
              mesh.beam(wheel,outer,railUp,.065,.065,"machined",light);
              mesh.beam(outer,at(carriage,side*1.15,0,z),forward,.10,.12,"structural",light);
            }
            if(z<0) {
              // This vertical-axis pinion contacts only the dedicated rack, never the running rail.
              var rackRight=vec(basis.right()).mul(route.signs.getOrDefault(hit.edge(),1));
              var rack=vec(hit.point()).fma(1.35,rackRight).fma(.05,railUp);
              var pinion=new Vector3d(rack).fma(-.25,rackRight);
              mesh.wheel(pinion,railUp,rackRight,.20,.10,phase,light,"machined");
              mesh.beam(pinion,at(carriage,(rackRight.dot(right)>0?1:-1)*1.15,-.4,z),right,.12,.12,"structural",light);
            }
          }
          mesh.beam(at(carriage,0,0,-1.25),at(carriage,0,0,1.25),up,.28,.22,"paint_cyan",light);
          // Split catch jaws visibly withdraw when the actual native constraint is absent.
          double jaw=state.caught()?.13:.36;
          for(int side:new int[]{-1,1}) {
            mesh.beam(at(carriage,side*.35,0,0),at(carriage,side*jaw,-.13,0),forward,.10,.12,"machined",light);
          }
        }
      }
      if(state.lead()!=null && container.getSubLevel(state.lead()) instanceof ClientSubLevel lead) {
        var receiver=at(lead,0,3,0);int light=light(receiver);
        if(receiver.distanceSquared(camera.x,camera.y,camera.z)<128*128 && ProductionMesh.visible(event,receiver,3)) {
          var up=new Vector3d(0,1,0).rotate(lead.renderPose().orientation());
          var right=new Vector3d(1,0,0).rotate(lead.renderPose().orientation());
          mesh.beam(at(lead,0,1.5,0),receiver,right,.22,.22,"structural",light);
          mesh.wheel(receiver,right,up,.13,.24,0,light);
          mesh.beam(at(lead,-.4,2,0),at(lead,0,2.8,0),right,.09,.09,"structural",light);
          mesh.beam(at(lead,.4,2,0),at(lead,0,2.8,0),right,.09,.09,"structural",light);
        }
      }
      var winch=state.winch();var center=new Vector3d(winch.getX()+.5,winch.getY()+.5,winch.getZ()+.5);
      if(center.distanceSquared(camera.x,camera.y,camera.z)<64*64 && ProductionMesh.visible(event,center,1) && mc.level.hasChunkAt(winch)) {
        var block=mc.level.getBlockState(winch);
        if(!block.hasProperty(BlockStateProperties.AXIS))continue;
        var axis=switch(block.getValue(BlockStateProperties.AXIS)) {
          case X -> new Vector3d(1,0,0);case Y -> new Vector3d(0,1,0);case Z -> new Vector3d(0,0,1);
        };
        var radial=axis.y>.5?new Vector3d(1,0,0):new Vector3d(0,1,0);
        int light=light(center);
        mesh.wheel(center,axis,radial,.28,.40,phase,light,"structural");
        boolean drive=Set.of("LIFTING","CAPTURING","OUTBOUND","RETURNING").contains(state.phase())&&!state.fault();
        mesh.ring(new Vector3d(center).fma(drive?.24:.35,axis),axis,radial,.10,.22,.06,"machined",light);
        mesh.beam(new Vector3d(center).fma(-.5,axis),new Vector3d(center).fma(.5,axis),radial,.10,.10,"machined",light);
        // Input follows actual kinetic RPM even while the output is declutched.
        // The output drum above follows carriage travel, never an idle motor.
        double inputPhase=(mc.level.getGameTime()+net.createmod.catnip.animation.AnimationTickHolder.getPartialTicks(mc.level))
            *state.rpm()*Math.PI/600;
        mesh.wheel(new Vector3d(center).fma(-.38,axis),axis,radial,.14,.08,inputPhase,light,"machined");
        var indicator=new Vector3d(center).add(0,.38,0);
        mesh.beam(new Vector3d(indicator).add(-.15,0,0),new Vector3d(indicator).add(.15,0,0),new Vector3d(0,1,0),.10,.08,
            state.fault()?"indicator_fault":state.ready()?"indicator_ready":state.caught()?"indicator_hold":"indicator_off",light);
      }
    }
    ROUTES.keySet().retainAll(seen);
    stack.popPose();mc.renderBuffers().bufferSource().endBatch(Sheets.cutoutBlockSheet());
    dev.fourthdimension.client.fd917.MechanismRenderMetrics.finish("pax",renderStarted);
  }

  private static Vector3d at(ClientSubLevel body,double x,double y,double z) {
    var p=body.getPlot().getCenterBlock();
    return body.renderPose().transformPosition(new Vector3d(p.getX()+.5+x,p.getY()+.5+y,p.getZ()+.5+z));
  }
  private static int light(Vector3d p) {return LevelRenderer.getLightColor(Minecraft.getInstance().level,BlockPos.containing(p.x,p.y,p.z));}
  private static Vector3d vec(Vec3 v) {return new Vector3d(v.x,v.y,v.z);}

  private static Collection<Fd917ShuttleSync.State> states(Minecraft mc,BlockPos camera) {
    // Normal chunk packets already own nearby unbound controls. Cache the loaded BE
    // inventory once per second or camera chunk change; never load a chunk to draw it.
    if(nearbyCenter==null||mc.level.getGameTime()-nearbyTick>=20
        ||(camera.getX()>>4)!=(nearbyCenter.getX()>>4)||(camera.getZ()>>4)!=(nearbyCenter.getZ()>>4)) {
      LOCAL.clear();nearbyCenter=camera;nearbyTick=mc.level.getGameTime();
      for(int x=(camera.getX()>>4)-4;x<=(camera.getX()>>4)+4;x++)
        for(int z=(camera.getZ()>>4)-4;z<=(camera.getZ()>>4)+4;z++)
          if(mc.level.hasChunk(x,z))for(var be:mc.level.getChunk(x,z).getBlockEntities().values())
            if(be instanceof dev.fourthdimension.shuttle.ShuttleWinchEntity winch)LOCAL.add(winch);
    }
    var states=new LinkedHashMap<BlockPos,Fd917ShuttleSync.State>();
    for(var state:Fd917ShuttleSync.states(mc.level))states.put(state.winch(),state);
    for(var winch:LOCAL)if(!winch.isRemoved()&&mc.level.hasChunkAt(winch.getBlockPos())
        &&mc.level.getBlockEntity(winch.getBlockPos())==winch)states.putIfAbsent(winch.getBlockPos(),Fd917ShuttleSync.state(winch));
    return states.values();
  }
  private static Route route(Fd917ShuttleSync.State state,FoundationData data) {
    if(state.start()==null||state.end()==null)return null;
    long key=Objects.hash(state.start(),state.end());
    for(var rail:data.rails)if(rail.family==Family.PAX)key=31*key+Objects.hash(rail.id,rail.geometryHash,rail.valid);
    var existing=ROUTES.get(state.winch());if(existing!=null&&existing.key==key)return existing;
    var mc=Minecraft.getInstance();var edges=new ArrayList<CoasterPathEdge>();var signs=new HashMap<CoasterPathEdge,Integer>();var segments=new ArrayList<Segment>();
    var seen=new HashSet<UUID>();var node=state.start();
    for(int i=0;i<256 && !node.equals(state.end());i++) {
      var at=node;
      var candidates=data.rails.stream().filter(r -> r.valid&&r.family==Family.PAX&&!seen.contains(r.id)
          &&(r.from.equals(at)||r.to.equals(at))).toList();
      if(candidates.size()!=1)return null;
      var rail=candidates.getFirst();seen.add(rail.id);var edge=TrackGeometry.resolve(mc.level,rail);
      if(edge==null)return null;edges.add(edge);int sign=edge.from().equals(at)?1:-1;signs.put(edge,sign);var table=TrackGeometry.table(mc.level,edge);
      int count=Math.max(1,(int)Math.ceil(table.length()*2));Vector3d previous=null;
      for(int n=0;n<=count;n++) {
        double t=table.parameterAt(table.length()*(sign>0?n:count-n)/count);
        var point=CoasterPathGraphManager.sampleEdgePosition(mc.level,edge,t);
        var basis=CoasterPathTrackFrame.trackBasisAtHit(mc.level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
        var p=vec(point).fma(1.35*sign,vec(basis.right())).fma(.05,vec(basis.up()));
        if(previous!=null)segments.add(new Segment(previous,p,vec(basis.up()),vec(basis.right()).mul(sign),n%8==0));previous=p;
      }
      node=rail.from.equals(at)?rail.to:rail.from;
    }
    if(!node.equals(state.end()))return null;
    var result=new Route(key,List.copyOf(edges),Map.copyOf(signs),List.copyOf(segments));ROUTES.put(state.winch(),result);return result;
  }
}
