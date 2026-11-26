package dev.fourthdimension.moving;

import dev.fourthdimension.moving.MovingCapabilityCatalog.Configuration;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;

/** Family route handoffs reuse production route checks against one authenticated candidate gap. */
public final class MovingRouteAdapters {
  private MovingRouteAdapters(){}
  public static void register(){
    for(var configuration:List.of(Configuration.PAX,Configuration.ROLLER_BALL,Configuration.POWERED_INVERTED_YAW,
        Configuration.NEW_GENERATION_FLYING,Configuration.FLYING_BIKE))
      MovingAdmission.registerRouteAdapter(configuration,new MovingAdmission.FamilyRouteAdapter(){
        public void validateCapture(ServerLevel level,MovingAssemblyState state,FoundationData.Vehicle car){
          if(configuration==Configuration.PAX)MovingPaxRecovery.validateMovingCapture(level,state,car);else retainedState(level,configuration,car,true);
          for(var dock:List.of(requiredDock(state,state.source),requiredDock(state,state.destination)))
            validate(level,state,car,dock,configuration);
        }
        public void validateRelease(ServerLevel level,MovingAssemblyState state,FoundationData.Vehicle car,MovingAssemblyState.Dock dock){
          if(configuration!=Configuration.PAX)retainedState(level,configuration,car,false);validate(level,state,car,dock,configuration);
        }
      });
  }
  private static MovingAssemblyState.Dock requiredDock(MovingAssemblyState state,String name){
    var dock=state.docks.get(name);if(dock==null)throw new IllegalArgumentException("Select real moving route sockets");return dock;
  }
  public static void validateExit(ServerLevel l,MovingAssemblyState s,FoundationData.Vehicle v,MovingAssemblyState.Dock dock){
    if(dock==null)throw new IllegalArgumentException("Service destination is missing");
    var config=MovingCapabilityCatalog.Configuration.byId(s.configuration);
    MovingAdmission.inspectDock(l,MovingCapabilityCatalog.entry(config),dock,"Service departure");
    if(List.of(Configuration.PAX,Configuration.ROLLER_BALL,Configuration.POWERED_INVERTED_YAW,
        Configuration.NEW_GENERATION_FLYING,Configuration.FLYING_BIKE).contains(config))validate(l,s,v,dock,config);
  }
  public static void prepareRelease(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock){
    if(MovingCapabilityCatalog.Configuration.byId(state.configuration)!=Configuration.POWERED_INVERTED_YAW)return;
    var car=FoundationData.get(level).vehicles.get(state.cars.getFirst());
    var governor=car==null?null:PoweredInvertedYawRuntime.governor(level,car);
    if(governor==null)throw new IllegalArgumentException("Restore the original Powered Yaw governor");
    governor.adoptMovingCircuit(state,dock,car);
  }
  private static void retainedState(ServerLevel level,Configuration c,FoundationData.Vehicle car,boolean capture){
    switch(c){
      case PAX->{
        var winch=ShuttleRuntime.owner(level,car.chassis);
        if(winch==null||winch.phase!=ShuttleWinchEntity.Phase.BOARDING||!winch.fault.isEmpty()
            ||!level.hasChunkAt(winch.getBlockPos())||level.getBlockEntity(winch.getBlockPos())!=winch)
          throw new IllegalArgumentException("PAX requires its stopped, repaired station winch before moving support");
        var ids=VehicleOperations.consist(level,car).stream().map(v->v.chassis).collect(java.util.stream.Collectors.toSet());
        if(!ids.equals(new HashSet<>(winch.cars)))throw new IllegalArgumentException("Capture every car owned by the PAX winch");
        if(capture&&!ShuttleRuntime.stationHeld(level,car))throw new IllegalArgumentException("PAX station supports are not physically engaged");
      }
      case ROLLER_BALL->{if(car.roller.restartHold||car.roller.released||car.roller.pendingRelease
          ||!(car.roller.phase==RollerBallState.Phase.SECURED||car.roller.phase==RollerBallState.Phase.DOCK_CAPTURE))
        throw new IllegalArgumentException("Capture Roller Ball at its secured dock before moving support");}
      case POWERED_INVERTED_YAW->{var g=PoweredInvertedYawRuntime.governor(level,car);
        if(g==null||car.poweredYaw.resumeRequired||!car.authority.held||!g.authorityId().equals(car.authority.owner))
          throw new IllegalArgumentException("Restore and acknowledge the complete Powered Yaw governor ownership");
        var reason=capture?g.systemAvailability():null;if(reason!=null)throw new IllegalArgumentException(reason);
      }
      case NEW_GENERATION_FLYING->{if(car.yaw.resumeRequired||car.yaw.travelAuthorized||!car.flying.retained())
        throw new IllegalArgumentException("Retain and acknowledge NG Flying posture/yaw before table motion");}
      case FLYING_BIKE->{var g=FlyingBikeRuntime.governor(level,car);
        if(g==null||!car.authority.held||!g.authorityId().equals(car.authority.owner))
          throw new IllegalArgumentException("Retain the Flying Bike's original two-launch governor ownership");
        var reason=g.availability();if(reason!=null)throw new IllegalArgumentException(reason);
      }
      default->throw new IllegalArgumentException("No complex route handoff");
    }
  }
  private static void validate(ServerLevel level,MovingAssemblyState state,FoundationData.Vehicle car,
                               MovingAssemblyState.Dock dock,Configuration configuration){
    if(configuration==Configuration.PAX){MovingPaxRecovery.validateMovingRoute(level,state,dock,car);return;}
    MovingRouteContext.validate(level,state,dock,()->{
      switch(configuration){
        case PAX->{var winch=ShuttleRuntime.owner(level,car.chassis);
          var route=ShuttleRoute.resolve(level,winch.routeStart,winch.routeEnd);
          if(route.segments.stream().noneMatch(s->MovingRouteContext.neutralBridge(level,s.rail())))
            throw new IllegalArgumentException("Selected PAX destination is outside its bound winch route");}
        case ROLLER_BALL->RollerBallTrack.validateRoute(level,car);
        case FLYING_BIKE->{var g=FlyingBikeRuntime.governor(level,car);if(g==null||g.availability()!=null)throw new IllegalArgumentException("Restore the original two-launch Flying Bike governor");FlyingBikeRuntime.validateRoute(level,car,g);}
        case POWERED_INVERTED_YAW->{
          var governor=PoweredInvertedYawRuntime.governor(level,car);
          if(governor==null)throw new IllegalArgumentException("Restore the original Powered Yaw governor");
          var route=PoweredInvertedYawGovernorEntity.route(level,car);
          var fixed=new HashSet<UUID>();
          for(var rail:route){if(MovingRouteContext.neutralBridge(level,rail))continue;
            fixed.add(rail.id);var edge=TrackGeometry.resolve(level,rail);
            if(edge==null||PoweredInvertedYawTrack.rejection(level,edge)!=null||TrackGeometry.fingerprint(level,edge)!=rail.geometryHash)
              throw new IllegalArgumentException("Restore the complete destination powered route geometry");
            rail.poweredYaw.validate();
            if(rail.poweredYaw.yaw.geometry!=rail.geometryHash||rail.poweredYaw.yaw.intervals.isEmpty()||rail.poweredYaw.speeds.isEmpty())
              throw new IllegalArgumentException("Author destination powered yaw and speed sections");
            validateYaw(level,car,rail.poweredYaw.yaw,car.poweredYaw.owner);
          }
          governor.validateMovingCircuit(car);
        }
        case NEW_GENERATION_FLYING->validateFlying(level,car);
        default->throw new IllegalArgumentException("Unknown moving route handoff");
      }
    });
  }
  private static void validateFlying(ServerLevel level,FoundationData.Vehicle car){
    var first=MovingRouteContext.start(level,car);var todo=new ArrayDeque<FoundationData.Rail>();todo.add(first);
    var seen=new HashSet<UUID>();
    while(!todo.isEmpty()){
      var current=todo.removeFirst();if(!seen.add(current.id))continue;
      if(!MovingRouteContext.neutralBridge(level,current)){
        var edge=TrackGeometry.resolve(level,current);
        if(edge==null||NewGenerationFlyingTrack.rejection(level,edge)!=null||TrackGeometry.fingerprint(level,edge)!=current.geometryHash)
          throw new IllegalArgumentException("Restore matching destination NG Flying route geometry");
        if(current.yaw.geometry!=current.geometryHash||current.yaw.intervals.isEmpty())throw new IllegalArgumentException("Author destination NG Flying yaw/posture intervals");
        validateYaw(level,car,current.yaw,car.yaw.owner);
      }
      for(var endpoint:List.of(current.from,current.to)){
        var adjacent=MovingRouteContext.rails(level).stream().filter(r->r.from.equals(endpoint)||r.to.equals(endpoint)).toList();
        if(adjacent.size()>2)throw new IllegalArgumentException("Resolve ambiguous branches before NG Flying route handoff");
        for(var rail:adjacent)if(!seen.contains(rail.id))todo.add(rail);
      }
    }
  }
  private static void validateYaw(ServerLevel level,FoundationData.Vehicle car,YawSection section,UUID owner){
    section.validate();
    if(section.owner!=null&&!Objects.equals(section.owner,owner))throw new IllegalArgumentException("Destination yaw program has another owner");
    for(var i:section.intervals)if(i.mode()==YawState.Mode.TARGET){
      if(!level.dimension().location().toString().equals(i.dimension())||!level.hasChunkAt(i.markerPos())
          ||!(level.getBlockEntity(i.markerPos()) instanceof YawMarkerEntity marker)
          ||!marker.id.equals(i.marker())||!Objects.equals(marker.owner,owner))
        throw new IllegalArgumentException("Restore the destination route's owned yaw target marker");
    }
  }
}
