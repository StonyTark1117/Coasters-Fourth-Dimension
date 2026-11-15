package dev.fourthdimension.integration;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** Additions reports shared state and requests operations; it never writes restraints or vehicle phases. */
public final class AdditionsStationAdapter {
  public interface Bridge extends RideOperations.SpanController,dev.fourthdimension.integration.create.RideInformation.Source {
    State fourthdimension$stationState();
  }
  public static final class State {
    public RideTelemetry telemetry=RideTelemetry.unavailable("No complete Fourth Dimension train in span");
    public UUID bound;
    public Set<UUID> cars=Set.of();
    public long sampled=Long.MIN_VALUE;
    public int dwell;
    public boolean armed,dispatched,managed;
    public String fault="";
  }
  private AdditionsStationAdapter() {}
  public static boolean authorizes(BlockEntity block,State s,UUID id) {
    return block.getLevel() instanceof ServerLevel l && !block.isRemoved()
        && s.sampled==l.getGameTime() && s.cars.contains(id) && s.telemetry.available();
  }
  public static void configure(State s,boolean enabled) {
    s.armed=enabled;s.dispatched=false;s.dwell=0;s.fault="";
  }
  public static boolean update(BlockEntity block,State s,boolean preWaiting) {
    if(!(block.getLevel() instanceof ServerLevel l))return false;
    if(s.sampled==l.getGameTime())return s.managed;
    if(!OptionalIntegrations.installed(OptionalIntegrations.ADDITIONS).mayInspect()) {
      s.telemetry=RideTelemetry.unavailable("Unsupported Additions artifact");s.armed=false;
      outputs(block,0,0,0,0);return true;
    }
    try {
      var span=(List<?>)OptionalCalls.call(block,"getSpan");
      var touched=new ArrayList<FoundationData.Vehicle>();
      for(var v:FoundationData.get(l).vehicles.values())
        if(!FoundationData.VARIANT.equals(v.variant)&&inside(l,v,span,false))touched.add(v);
      if(touched.isEmpty()&&!s.managed)return false;
      s.managed=true;
      if(touched.isEmpty()) {
        s.cars=Set.of();s.sampled=l.getGameTime();s.dwell=0;
        s.telemetry=RideTelemetry.unavailable("No complete Fourth Dimension train in span");
        // Native unload is not departure: retain identity and require explicit re-arming.
        if(s.bound!=null&&SubLevelContainer.getContainer(l).getSubLevel(s.bound)==null)s.armed=false;
        else {s.bound=null;s.dispatched=false;}
        outputs(block,0,0,0,preWaiting?15:0);return true;
      }
      var selected=touched.getFirst();var consist=VehicleOperations.consist(l,selected);
      var ids=new HashSet<>(consist.stream().map(v->v.chassis).toList());
      if(touched.stream().anyMatch(v->!ids.contains(v.chassis)))
        throw new IllegalArgumentException("Multiple trains in station span; inspect and re-arm");
      if(consist.stream().anyMatch(v->!inside(l,v,span,true))) {
        s.cars=Set.of();s.sampled=l.getGameTime();s.dwell=0;
        s.telemetry=RideTelemetry.unavailable("Complete single train must fit the station span");
        outputs(block,15,0,15,preWaiting?15:0);return true;
      }
      if(s.bound!=null&&!ids.contains(s.bound)&&!s.dispatched)
        throw new IllegalArgumentException("Station train changed; re-arm automatic dispatch");
      s.bound=selected.chassis;s.cars=Set.copyOf(ids);s.sampled=l.getGameTime();
      s.telemetry=RideTelemetry.sample(l,s.bound);
      if(!s.telemetry.available())throw new IllegalArgumentException(s.telemetry.fault());
      // The request path samples authority and whole-consist guards again immediately.
      s.telemetry=RideTelemetry.atController(l,block.getBlockPos(),s.bound);
      if(selected.authority.owner!=null&&!block.getBlockPos().equals(selected.authority.controller)) {
        s.armed=false;s.dwell=0;s.fault="Another controller owns this ride";
        s.telemetry=s.telemetry.denyDispatch(s.fault);
        outputs(block,15,Math.min(15,s.telemetry.occupied()),s.telemetry.restraintsClosed()?0:15,preWaiting?15:0);
        return true;
      }
      boolean enabled=(boolean)OptionalCalls.call(block,"isDispatchEnabled");
      if(enabled&&s.armed&&!s.dispatched) {
        if(!s.telemetry.captured()) {
          RideOperations.request(l,block.getBlockPos(),s.bound,RideOperations.Request.CAPTURE);
          s.dwell=0;
        } else if(s.telemetry.dispatchReady()&&s.telemetry.occupied()>=(int)OptionalCalls.call(block,"getDispatchThreshold")) {
          int wait=20*((int)OptionalCalls.call(block,"getDispatchDelaySeconds")+(int)OptionalCalls.call(block,"getDispatchCooldownSeconds"));
          if(++s.dwell>=Math.max(1,wait)) {
            RideOperations.request(l,block.getBlockPos(),s.bound,RideOperations.Request.DISPATCH);
            s.dispatched=true;s.dwell=0;
          }
        } else s.dwell=0;
      } else s.dwell=0;
      s.fault="";
      outputs(block,15,Math.min(15,s.telemetry.occupied()),s.telemetry.restraintsClosed()?0:15,preWaiting?15:0);
      return true;
    } catch(IllegalArgumentException unavailable) {
      s.cars=Set.of();s.sampled=l.getGameTime();s.dwell=0;s.armed=false;s.fault=unavailable.getMessage();
      s.telemetry=RideTelemetry.unavailable(s.fault);outputs(block,15,0,0,preWaiting?15:0);return true;
    }
  }
  private static boolean inside(ServerLevel l,FoundationData.Vehicle v,List<?> span,boolean complete) {
    var body=SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
    var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
    if(center==null||body.isRemoved())return false;
    var point=new Vec3(center.x,center.y,center.z);
    for(var segment:span) {
      var from=OptionalCalls.call(segment,"from");var to=OptionalCalls.call(segment,"to");
      var edge=CoasterPathGraphManager.get(l).edges().stream().filter(e->e.from().equals(from)&&e.to().equals(to)||e.from().equals(to)&&e.to().equals(from)).findFirst().orElse(null);
      if(edge==null||v.boundFrom==null||!(edge.from().equals(v.boundFrom)&&edge.to().equals(v.boundTo)
          ||edge.to().equals(v.boundFrom)&&edge.from().equals(v.boundTo)))continue;
      var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,point);
      if(hit==null||hit.point().distanceToSqr(point)>4||!(boolean)OptionalCalls.call(segment,"matches",edge,hit.t()))continue;
      if(!complete)return true;
      var table=TrackGeometry.table(l,edge);double d=table.distanceAt(hit.t());
      // Two blocks each side conservatively enclose supported native chassis wheel contacts.
      if(d<2||d+2>table.length())continue;
      if((boolean)OptionalCalls.call(segment,"matches",edge,table.parameterAt(d-2))
          &&(boolean)OptionalCalls.call(segment,"matches",edge,table.parameterAt(d+2)))return true;
    }
    return false;
  }
  private static void outputs(Object block,int occupancy,int riders,int restraints,int pre) {
    int[] values={occupancy,riders,restraints,pre};
    for(int i=0;i<4;i++)OptionalCalls.call(OptionalCalls.call(block,"channel",i),"setPower",values[i]);
  }
}
