package dev.fourthdimension.moving;

import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.RollerBallState;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Scoped prospective route validation. Never publishes native graph edges or writes saved rails. */
public final class MovingRouteContext {
  private record View(ServerLevel level,FoundationData.Rail bridge,CoasterPathEdge edge,List<FoundationData.Rail> rails) {}
  private static final ThreadLocal<View> CURRENT=new ThreadLocal<>();
  private record Live(MovingAssemblyState state,MovingAssemblyState.Dock dock,FoundationData.Rail rail,CoasterPathEdge edge) {}
  private static final Map<ServerLevel,Map<UUID,Live>> LIVE=new WeakHashMap<>();
  private MovingRouteContext(){}
  public static void validate(ServerLevel level,MovingAssemblyState state,MovingAssemblyState.Dock dock,Runnable action){
    if(!level.getServer().isSameThread()||CURRENT.get()!=null)
      throw new IllegalStateException("Moving route validation requires a non-nested server-thread scope");
    if(FoundationData.get(level).moving.assemblies.get(state.id)!=state)
      throw new IllegalArgumentException("Moving route assembly is not registered");
    var entry=MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.byId(state.configuration));
    if(entry.family()==null)throw new IllegalArgumentException("Native service carts have no original family route");
    MovingAdmission.inspectDock(level,entry,dock,"Candidate route");
    var edge=MovingNativeAdapter.previewDockEdge(level,state,dock);
    var id=UUID.nameUUIDFromBytes((state.id+":"+dock.socket()).getBytes(StandardCharsets.UTF_8));
    var bridge=new FoundationData.Rail(dock.from(),dock.to(),entry.family(),
        new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(state.deckLength,0))),
        id,entry.variant(),TrackGeometry.fingerprint(level,edge),state.epoch,true);
    bridge.rollerSection=RollerBallState.Section.DOCK;
    var rails=new ArrayList<FoundationData.Rail>();
    for(var rail:rails(level)){
      if(rail.matches(state.railFrom,state.railTo)||owner(level,rail)==state)continue; // Hosted insert is represented at the selected physical socket pose.
      if(rail.matches(dock.from(),dock.to())){
        if(neutralBridge(level,rail))continue;
        throw new IllegalArgumentException("A fixed rail illegally bridges the moving gap");
      }
      rails.add(rail);
    }
    rails.add(bridge);CURRENT.set(new View(level,bridge,edge,List.copyOf(rails)));
    try{action.run();}finally{CURRENT.remove();}
  }
  public static List<FoundationData.Rail> rails(Level level){
    var view=CURRENT.get();if(view!=null&&view.level==level)return view.rails;
    var data=EligibilityHooks.data(level);if(data==null)return List.of();
    if(!(level instanceof ServerLevel server))return data.rails;
    if(data.moving.assemblies.isEmpty())return data.rails;
    var result=new ArrayList<>(data.rails);
    for(var live:live(server)){
      result.removeIf(r->r.matches(live.state.railFrom,live.state.railTo));result.add(live.rail);
    }
    return List.copyOf(result);
  }
  public static FoundationData.Rail start(Level level,FoundationData.Vehicle car){
    var view=CURRENT.get();return view!=null&&view.level==level&&view.bridge.family==car.family
        &&view.bridge.variant.equals(car.variant)?view.bridge:null;
  }
  public static FoundationData.Rail rail(Level level,BlockPos from,BlockPos to){
    return rails(level).stream().filter(r->r.matches(from,to)).findFirst().orElse(null);
  }
  public static CoasterPathEdge resolve(Level level,FoundationData.Rail rail){
    var view=CURRENT.get();if(view!=null&&view.level==level&&view.bridge==rail)return view.edge;
    if(level instanceof ServerLevel server)for(var live:live(server))if(live.rail==rail)return live.edge;
    return null;
  }
  public static boolean neutralBridge(Level level,FoundationData.Rail rail){
    var view=CURRENT.get();if(view!=null&&view.level==level&&view.bridge==rail)return true;
    if(level instanceof ServerLevel server)for(var live:live(server))if(live.rail==rail)return true;
    return false;
  }
  /** Actual native bridge, never a saved phase alone. The native helper does not restore bindings. */
  private static List<Live> live(ServerLevel level){
    var cache=LIVE.computeIfAbsent(level,k->new HashMap<>());var result=new ArrayList<Live>();
    for(var state:FoundationData.get(level).moving.assemblies.values()){
      var edge=MovingNativeAdapter.liveDockEdge(level,state);if(edge==null)continue;
      var entry=MovingCapabilityCatalog.entry(MovingCapabilityCatalog.Configuration.byId(state.configuration));
      if(entry.family()==null||entry.configuration()==MovingCapabilityCatalog.Configuration.WING)continue;
      var dock=state.docks.values().stream().filter(d->d.from().equals(edge.from())&&d.to().equals(edge.to())
          ||d.from().equals(edge.to())&&d.to().equals(edge.from())).findFirst().orElse(null);
      if(dock==null)continue;
      var id=UUID.nameUUIDFromBytes((state.id+":"+dock.socket()).getBytes(StandardCharsets.UTF_8));
      long hash=TrackGeometry.fingerprint(level,edge);var old=cache.get(id);
      if(old==null||old.state!=state||old.rail.geometryHash!=hash||!old.rail.variant.equals(entry.variant())){
        var rail=new FoundationData.Rail(dock.from(),dock.to(),entry.family(),
            new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(state.deckLength,0))),
            id,entry.variant(),hash,0,true);rail.rollerSection=RollerBallState.Section.DOCK;
        old=new Live(state,dock,rail,edge);cache.put(id,old);
      }else if(old.edge!=edge){old=new Live(state,dock,old.rail,edge);cache.put(id,old);}
      result.add(old);
    }
    var ids=result.stream().map(l->l.rail.id).collect(java.util.stream.Collectors.toSet());cache.keySet().retainAll(ids);
    return List.copyOf(result);
  }
  /** Ordinary Wing/native bridge lookup without inventing an original-family Rail marker. */
  public static CoasterPathEdge liveEdge(Level level,BlockPos from,BlockPos to){
    if(!(level instanceof ServerLevel server)||from==null||to==null)return null;
    for(var state:FoundationData.get(server).moving.assemblies.values()){
      var edge=MovingNativeAdapter.liveDockEdge(server,state);
      if(edge!=null&&(edge.from().equals(from)&&edge.to().equals(to)||edge.from().equals(to)&&edge.to().equals(from)))return edge;
    }
    return null;
  }
  public static MovingAssemblyState owner(Level level,FoundationData.Rail rail){
    if(level instanceof ServerLevel server)for(var live:live(server))if(live.rail==rail)return live.state;
    return null;
  }
}
