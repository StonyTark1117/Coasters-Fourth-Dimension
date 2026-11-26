package dev.fourthdimension.moving;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Explicit recovery through the original PAX winch and fixed station, never a new table station. */
public final class MovingPaxRecovery {
  public static final String OPERATOR_CAPTURE="Operator emergency capture: inspect the shuttle before recovery";
  private record Station(UUID rail,Vec3 point,double distance){}
  private MovingPaxRecovery(){}
  public static boolean isMovingCaptureFault(ServerLevel level,FoundationData.Vehicle car){
    var winch=ShuttleRuntime.owner(level,car.chassis);
    return car.family==Family.PAX&&winch!=null&&winch.phase==ShuttleWinchEntity.Phase.FAULT
        &&OPERATOR_CAPTURE.equals(winch.fault)&&OPERATOR_CAPTURE.equals(car.fault);
  }
  private static ShuttleWinchEntity owner(ServerLevel level,MovingAssemblyState state,FoundationData.Vehicle car){
    var winch=ShuttleRuntime.owner(level,car.chassis);
    if(winch==null||!level.hasChunkAt(winch.getBlockPos())||level.getBlockEntity(winch.getBlockPos())!=winch
        ||winch.routeStart==null||winch.routeEnd==null||winch.carriage==null)
      throw new IllegalArgumentException("Restore the original loaded PAX winch, carriage and station route");
    var ids=VehicleOperations.consist(level,car).stream().map(v->v.chassis).collect(java.util.stream.Collectors.toSet());
    if(!ids.equals(new HashSet<>(winch.cars))||!state.cars.isEmpty()&&!ids.equals(new HashSet<>(state.cars)))
      throw new IllegalArgumentException("Moving PAX recovery requires the complete original winch train");
    if(!(SubLevelContainer.getContainer(level).getSubLevel(winch.carriage) instanceof ServerSubLevel body)||body.isRemoved())
      throw new IllegalArgumentException("Restore the original native winch carriage");
    return winch;
  }
  public static void validateMovingCapture(ServerLevel level,MovingAssemblyState state,FoundationData.Vehicle car){
    var winch=owner(level,state,car);
    boolean boarding=winch.phase==ShuttleWinchEntity.Phase.BOARDING&&winch.fault.isEmpty();
    boolean interrupted=winch.phase==ShuttleWinchEntity.Phase.FAULT&&OPERATOR_CAPTURE.equals(winch.fault);
    if(!boarding&&!interrupted)throw new IllegalArgumentException("Explicitly stop the original PAX train before transferring its support");
    for(var id:winch.cars){
      var v=FoundationData.get(level).vehicles.get(id);
      if(v==null||!v.restraints||!v.stopping||v.maintenance||v.alignmentRequired||v.recoveryCart!=null
          ||!v.fault.isEmpty()&&!isMovingCaptureFault(level,v))
        throw new IllegalArgumentException("Repair and restrain every original PAX car before moving capture");
      if(!(boarding?ShuttleRuntime.stationHeld(level,v):ShuttleRuntime.readyForRecovery(level,v)))
        throw new IllegalArgumentException("PAX native stationary support and carrier capture are not ready");
      var parent=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(id);
      if(parent==null||SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(parent,new Vector3d()).length()>.025)
        throw new IllegalArgumentException("Stop every PAX car before moving capture");
    }
    validateMovingRoute(level,state,state.docks.get(state.destination),car);
  }
  /** Called outside any prospective route scope; both candidates retain the very same fixed station. */
  public static void validateMovingRoute(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock,FoundationData.Vehicle car){
    var winch=owner(level,state,car);
    if(dock==null)throw new IllegalArgumentException("Select the original PAX route's receiving socket");
    var original=station(level,state,state.docks.get(state.source),winch);
    var destination=station(level,state,dock,winch);
    if(!original.rail.equals(destination.rail)||original.point.distanceTo(destination.point)>.025
        ||Math.abs(original.distance-destination.distance)>.001)
      throw new IllegalArgumentException("Destination must preserve the original fixed PAX station and route coordinate");
  }
  private static Station station(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock,ShuttleWinchEntity winch){
    if(dock==null)throw new IllegalArgumentException("Original PAX source socket missing");
    Station[] result={null};
    MovingRouteContext.validate(level,state,dock,()->{
      var route=ShuttleRoute.resolve(level,winch.routeStart,winch.routeEnd);
      if(route.segments.stream().noneMatch(s->MovingRouteContext.neutralBridge(level,s.rail())))
        throw new IllegalArgumentException("Receiving PAX bridge is outside the original winch route");
      double coordinate=winch.stationDistance;
      if(!Double.isFinite(coordinate)||coordinate<25||coordinate+6*(winch.cars.size()-1)+20>route.length)
        throw new IllegalArgumentException("Original PAX station no longer fits its complete train");
      var segment=route.segments.stream().filter(s->coordinate>=s.start()&&coordinate<=s.start()+s.length()).findFirst().orElseThrow();
      if(MovingRouteContext.neutralBridge(level,segment.rail()))
        throw new IllegalArgumentException("A moving bed cannot replace the original fixed PAX boarding station");
      var sample=route.sample(coordinate);double height=sample.hit().point().y;
      if(Math.abs(sample.tangent().y)>.1||route.sample(3).hit().point().y<height+10
          ||route.sample(route.length-3).hit().point().y<height+10)
        throw new IllegalArgumentException("Restore the original level PAX station and both elevated spikes");
      result[0]=new Station(segment.rail().id,sample.hit().point(),coordinate);
    });return result[0];
  }
  /** A successful explicit table capture acknowledges only the named operator stop. */
  public static void captured(ServerLevel level,MovingAssemblyState state){
    for(var id:state.cars){var car=FoundationData.get(level).vehicles.get(id);
      if(car!=null&&isMovingCaptureFault(level,car)){car.fault="";car.revision++;}}
  }
  /** Keeps train world holds until the existing carriage physically approaches and catches it. */
  public static void beginMovingRecovery(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock){
    if(!state.released||MovingNativeAdapter.liveDockEdge(level,state)==null)
      throw new IllegalArgumentException("Lock and release the real PAX receiving bridge before explicit recovery");
    var car=FoundationData.get(level).vehicles.get(state.cars.getFirst());var winch=owner(level,state,car);
    validateMovingRoute(level,state,dock,car);
    if(Math.abs(winch.getSpeed())<16)throw new IllegalArgumentException("Restore original winch power before moving-track recovery");
    winch.validateTrain(false);
    for(var id:winch.cars){var v=FoundationData.get(level).vehicles.get(id);
      if(!v.fault.isEmpty()||!v.stopping||!v.restraints||v.maintenance||v.alignmentRequired)
        throw new IllegalArgumentException("Repair and retain every service car before original winch recovery");}
    // Original route endpoints, stationDistance, carriage UUID and ownership are intentionally unchanged.
    winch.phase=ShuttleWinchEntity.Phase.CAPTURING;winch.interrupted=ShuttleWinchEntity.Phase.CAPTURING;
    winch.fault="";winch.changed();
  }
  public static boolean movingDepartureReady(ServerLevel level,MovingAssemblyState state){
    if(state.cars.isEmpty())return false;
    var winch=ShuttleRuntime.owner(level,state.cars.getFirst());
    return winch!=null&&winch.fault.isEmpty()&&ShuttleRuntime.movingCatchEngaged(level,winch);
  }
}
