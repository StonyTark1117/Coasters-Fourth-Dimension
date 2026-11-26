package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** One ride-local longitudinal owner. No route reservation or multi-train scheduling. */
public final class RideAuthority {
  public static final int EXTERNAL_API = 1;
  /** Optional integrations register a real controller adapter, never borrow another provider's identity. */
  public interface ExternalProvider {
    Controller controller(ServerLevel level, BlockPos position);
    boolean supports(ServerLevel level, FoundationData.Vehicle vehicle);
  }
  private static final Map<String, ExternalProvider> EXTERNAL = new java.util.concurrent.ConcurrentHashMap<>();
  public static void registerExternal(String id, int api, ExternalProvider provider) {
    if (api != EXTERNAL_API || id == null || id.length()>80 || !id.matches("[a-z0-9_]+:[a-z0-9_/]+") || provider == null
        || EXTERNAL.putIfAbsent(id, provider) != null)
      throw new IllegalArgumentException("Invalid or duplicate external longitudinal provider");
  }
  public static final String NATIVE_CHAIN = "native_chain", ENGINEERED = "engineered", MAGNETIZED = "magnetized", CTNB = "ctnb";
  public record Token(UUID owner, long epoch, long sequence) {}
  public interface Controller extends RideOperations.SpanController {
    UUID authorityId();
    boolean authorityAvailable(UUID chassis);
    /** Recheck this consumer's route/exit/power/physical-hold guards on the server thread. */
    void validateRelease(ServerLevel level, FoundationData.Vehicle vehicle);
  }
  private RideAuthority() {}

  public static Token token(FoundationData.Vehicle v) {
    return new Token(v.authority.owner, v.authority.epoch, v.authority.sequence);
  }

  public static Token claim(ServerLevel level, BlockPos pos, UUID id, String provider) {
    thread(level);
    if (provider==null || !Set.of(NATIVE_CHAIN, ENGINEERED, MAGNETIZED, CTNB).contains(provider) && !EXTERNAL.containsKey(provider))
      throw new IllegalArgumentException("Unsupported longitudinal provider");
    var v = vehicle(level,id); var owner = controller(level,pos,id,provider); var cars = complete(level,v);
    stopped(level,cars);
    validateExternal(level,cars,provider);
    if(MAGNETIZED.equals(provider)){
      if(!OptionalIntegrations.installed(OptionalIntegrations.MAGNETIZED).mayInspect())throw new IllegalArgumentException("Supported Magnetized installation required");
      if(cars.stream().anyMatch(c->!dev.fourthdimension.vehicle.WingVehicle.is(c)&&!dev.fourthdimension.vehicle.S2SpinningVehicle.is(c)))
        throw new IllegalArgumentException("Magnetized is qualified for Wing and Launched Inverting Spinning only");
    }
    if(CTNB.equals(provider) && (!OptionalIntegrations.installed(OptionalIntegrations.CTNB).mayInspect() || cars.stream().anyMatch(c->!CtnbTracks.supports(c))))
      throw new IllegalArgumentException("CTNB supports certified Wing and Arrow cars only");
    for (var car : cars) if (car.authority.owner != null)
      throw new IllegalArgumentException("Ride already has an owner; explicitly transfer or resume it");
    assign(level,cars,pos,owner.authorityId(),provider,1);
    return token(v);
  }

  public static Token transfer(ServerLevel level, BlockPos pos, UUID id, Token token, BlockPos next) {
    thread(level); var v=vehicle(level,id); refresh(level,v);
    if(!token(v).equals(token)||!Objects.equals(pos,v.authority.controller))
      throw new IllegalArgumentException("Stale authority transfer");
    var cars=complete(level,v);
    validateExternal(level,cars,v.authority.provider);
    if (!v.authority.held) throw new IllegalArgumentException("Hold before transferring authority");
    stopped(level,cars); var owner = controller(level,next,id,v.authority.provider);
    int direction=v.authority.direction;
    assign(level,cars,next,owner.authorityId(),v.authority.provider,v.authority.epoch+1);
    for(var car:cars)car.authority.direction=direction;
    return token(v);
  }

  public static Token hold(ServerLevel level, BlockPos pos, UUID id, Token token) {
    var v = checked(level,pos,id,token,false);
    change(level,v,true,false); return token(v);
  }

  public static Token resume(ServerLevel level, BlockPos pos, UUID id, Token token) {
    var v = checked(level,pos,id,token,true); var cars = complete(level,v); stopped(level,cars);
    validateExternal(level,cars,v.authority.provider);
    controller(level,pos,id,v.authority.provider).validateRelease(level,v);
    for (var car : cars) {
      car.authority.epoch++; car.authority.sequence++; car.authority.held=true;
      car.authority.suspended=false; car.authority.reason=""; car.authority.restored=false;
      car.authority.stoppedTicks=0; car.authority.sampledTick=Long.MIN_VALUE; car.revision++;
    }
    FoundationData.get(level).setDirty(); return token(v);
  }

  public static Token release(ServerLevel level, BlockPos pos, UUID id, Token token) {
    var v = checked(level,pos,id,token,false);
    if (!v.authority.held) throw new IllegalArgumentException("Ride hold is already released");
    var cars=complete(level,v); safeRelease(level,cars);
    for(var car:cars) if(car.authority.stoppedTicks<5)
      throw new IllegalArgumentException("Wait for five stopped server ticks");
    controller(level,pos,id,v.authority.provider).validateRelease(level,v);
    if(MAGNETIZED.equals(v.authority.provider))OptionalDrive.validateRelease(level,v);
    if(CTNB.equals(v.authority.provider))CtnbDrive.validateRelease(level,v);
    change(level,v,false,false);
    if(CTNB.equals(v.authority.provider))CtnbDrive.released(level,v);
    return token(v);
  }

  /** Atomic validated station request; unchanged vehicle code owns capture and dispatch mechanics. */
  public static void dispatch(ServerLevel level, BlockPos pos, UUID id, Token token) {
    var v=checked(level,pos,id,token,false);
    VehicleOperations.validateDispatch(level,v);
    release(level,pos,id,token);
    try { VehicleOperations.dispatch(level,v); }
    catch(RuntimeException failure) { suspend(level,v,"Dispatch failed: "+failure.getMessage()); throw failure; }
  }

  /** Native capture under the same fresh owner token; seats, restraints and rotor alignment stay vehicle-owned. */
  public static Token capture(ServerLevel level, BlockPos pos, UUID id, Token token) {
    var v=checked(level,pos,id,token,false);
    stopped(level,complete(level,v));
    VehicleOperations.capture(level,v);
    return token(v);
  }

  /** Sequence matters as well as epoch: a held/released request cannot reuse an earlier permit. */
  public static boolean permits(ServerLevel level, UUID id, Token expected, String provider) {
    var v=FoundationData.get(level).vehicles.get(id);
    return v!=null && expected!=null && expected.owner()!=null && Objects.equals(provider,v.authority.provider)
        && !holds(level,v) && expected.equals(token(v));
  }

  /** Emergency stop is always allowed; it cannot clear a suspended authority. */
  public static void emergencyHold(ServerLevel level, FoundationData.Vehicle v) {
    if(v.authority.owner==null)return;
    for(var id:v.authority.cars){var car=FoundationData.get(level).vehicles.get(id);if(car==null)continue;
      if(!car.authority.held){car.authority.held=true;car.authority.sequence++;car.revision++;}}
    FoundationData.get(level).setDirty();
  }

  /** Normal editor controls; player permission/range is checked independently of the ride token. */
  public static void operate(net.minecraft.server.level.ServerPlayer player, FoundationData.Vehicle v, int action) {
    var l=player.serverLevel();
    if(action==67||action==68){
      direction(l,v,action==67?1:-1);return;
    }
    if(action==61) {
      refresh(l,v);var pos=v.authority.controller;
      if(pos==null||!l.mayInteract(player,pos)||!player.mayBuild()
          ||player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>144)
        throw new IllegalArgumentException("Approach the bound controller to resume authority");
      resume(l,pos,v.chassis,token(v));return;
    }
    BlockPos selected=null;double nearest=144;
    for(var pos:BlockPos.betweenClosed(player.blockPosition().offset(-8,-4,-8),player.blockPosition().offset(8,4,8))) {
      if(!l.hasChunkAt(pos)||!(l.getBlockEntity(pos) instanceof dev.fourthdimension.station.BoardingStationEntity station)
          ||!station.authorizesRide(v.chassis)||!l.mayInteract(player,pos))continue;
      if(action==62&&Objects.equals(station.authorityId(),v.authority.owner))continue;
      double distance=player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);
      if(distance<nearest){nearest=distance;selected=pos.immutable();}
    }
    if(selected==null)throw new IllegalArgumentException("Bind and approach the receiving Boarding Station first");
    if(action==66)selectCtnb(l,selected,v);
    else if(action==65)selectMagnetized(l,selected,v);
    else if(action==60)claim(l,selected,v.chassis,NATIVE_CHAIN);
    else {refresh(l,v);transfer(l,v.authority.controller,v.chassis,token(v),selected);}
  }

  public static void selectMagnetized(ServerLevel l,BlockPos selected,FoundationData.Vehicle v){
    thread(l);
    if(!OptionalIntegrations.installed(OptionalIntegrations.MAGNETIZED).mayInspect())throw new IllegalArgumentException("Supported Magnetized installation required");
    if(!dev.fourthdimension.vehicle.WingVehicle.is(v)&&!dev.fourthdimension.vehicle.S2SpinningVehicle.is(v))throw new IllegalArgumentException("Magnetized supports Wing and Launched Inverting Spinning");
    if(v.authority.owner==null)claim(l,selected,v.chassis,MAGNETIZED);
    else{
      refresh(l,v);if(!v.authority.held)throw new IllegalArgumentException("Hold before selecting a different drive");
      transfer(l,v.authority.controller,v.chassis,token(v),selected);
      for(var c:complete(l,v)){c.authority.provider=MAGNETIZED;c.authority.epoch++;c.authority.sequence++;c.revision++;}
      FoundationData.get(l).setDirty();
    }
  }

  public static void selectCtnb(ServerLevel l,BlockPos selected,FoundationData.Vehicle v){
    thread(l);
    if(!OptionalIntegrations.installed(OptionalIntegrations.CTNB).mayInspect() || !CtnbTracks.supports(v))
      throw new IllegalArgumentException("CTNB supports certified Wing and Arrow cars only");
    if(v.authority.owner==null)claim(l,selected,v.chassis,CTNB);
    else{
      refresh(l,v);if(!v.authority.held)throw new IllegalArgumentException("Hold before selecting a different drive");
      transfer(l,v.authority.controller,v.chassis,token(v),selected);
      for(var c:complete(l,v)){c.authority.provider=CTNB;c.authority.epoch++;c.authority.sequence++;c.revision++;}
      FoundationData.get(l).setDirty();
    }
  }

  /** Fresh drive permission; also called at each native propulsion callback. */
  public static boolean permits(ServerLevel level, UUID id, UUID owner, long epoch, String provider) {
    var v=FoundationData.get(level).vehicles.get(id);
    return v!=null && owner!=null && owner.equals(v.authority.owner) && epoch==v.authority.epoch
        && provider.equals(v.authority.provider) && !holds(level,v);
  }
  public static void direction(ServerLevel l,FoundationData.Vehicle v,int sign){
    thread(l);refresh(l,v);stopped(l,complete(l,v));
    if(Math.abs(sign)!=1||!(MAGNETIZED.equals(v.authority.provider)||CTNB.equals(v.authority.provider))||!v.authority.held)
      throw new IllegalArgumentException("Hold optional drive authority before selecting direction");
    for(var c:complete(l,v)){c.authority.direction=sign;c.revision++;}
    FoundationData.get(l).setDirty();
  }
  public static boolean deniesProvider(ServerLevel level, FoundationData.Vehicle v, String provider) {
    if (FoundationData.get(level).moving.retaining(v.chassis) != null) return true;
    if (v.family.specialty() && !NATIVE_CHAIN.equals(provider)) return true;
    return v.authority.owner!=null && (!provider.equals(v.authority.provider)||holds(level,v));
  }
  public static boolean holds(ServerLevel level, FoundationData.Vehicle v) {
    if (FoundationData.get(level).moving.retaining(v.chassis) != null) return true;
    if(v.authority.owner==null)return false;
    refresh(level,v);
    return v.authority.held||v.authority.suspended;
  }

  /** Before family physics: lost owners retain braking without changing seats or dwell. */
  public static void tick(ServerLevel level, FoundationData.Vehicle v, double dt) {
    if(v.authority.owner==null)return;
    refresh(level,v);
    var s=v.authority;
    if(s.sampledTick!=level.getGameTime()) {
      s.sampledTick=level.getGameTime();
      try { stopped(level,complete(level,v)); s.stoppedTicks=Math.min(5,s.stoppedTicks+1); }
      catch(IllegalArgumentException moving) {s.stoppedTicks=0;}
    }
    if(s.held||s.suspended) VehicleOperations.brake(level,v,dt);
  }

  private static void refresh(ServerLevel level, FoundationData.Vehicle v) {
    if(v.authority.restored) { suspend(level,v,"Restored authority requires explicit Resume"); return; }
    if(v.authority.suspended)return;
    try {
      var c=controller(level,v.authority.controller,v.chassis,v.authority.provider);
      if(!c.authorityId().equals(v.authority.owner))throw new IllegalArgumentException("Controller identity changed");
      var cars=complete(level,v);
      validateExternal(level,cars,v.authority.provider);
      for(var car:cars) if(!Objects.equals(car.authority.owner,v.authority.owner)
          ||car.authority.epoch!=v.authority.epoch||!car.authority.provider.equals(v.authority.provider))
        throw new IllegalArgumentException("Consist authority changed");
    } catch(IllegalArgumentException unavailable) {suspend(level,v,unavailable.getMessage());}
  }
  public static void suspend(ServerLevel level, FoundationData.Vehicle v, String reason) {
    for(var id:v.authority.cars) {
      var car=FoundationData.get(level).vehicles.get(id); if(car==null)continue;
      var s=car.authority;
      if(!s.suspended||s.restored){s.epoch++;s.sequence++;car.revision++;}
      s.restored=false;s.suspended=true;s.held=true;s.reason=reason;s.stoppedTicks=0;
    }
    FoundationData.get(level).setDirty();
  }
  private static FoundationData.Vehicle checked(ServerLevel level,BlockPos pos,UUID id,Token expected,boolean resume) {
    thread(level);var v=vehicle(level,id);refresh(level,v);
    if(!token(v).equals(expected)||!Objects.equals(pos,v.authority.controller))
      throw new IllegalArgumentException("Stale or foreign ride-authority request");
    var c=controller(level,pos,id,v.authority.provider);
    if(!Objects.equals(c.authorityId(),v.authority.owner))throw new IllegalArgumentException("Controller identity changed");
    if(!resume&&v.authority.suspended)throw new IllegalArgumentException(v.authority.reason);
    complete(level,v);return v;
  }
  private static Controller controller(ServerLevel l,BlockPos pos,UUID id,String providerId) {
    if(pos==null||!l.hasChunkAt(pos))throw new IllegalArgumentException("Bound authority controller unavailable");
    var provider=EXTERNAL.get(providerId);
    Controller result=provider==null
        ? (l.getBlockEntity(pos) instanceof Controller nativeController ? nativeController : null)
        : provider.controller(l,pos);
    if(result==null||!result.authorizesRide(id)||!result.authorityAvailable(id))
      throw new IllegalArgumentException("Bound authority controller unavailable");
    return result;
  }
  private static void validateExternal(ServerLevel l,List<FoundationData.Vehicle> cars,String id) {
    if (!NATIVE_CHAIN.equals(id) && cars.stream().anyMatch(car -> car.family.specialty()))
      throw new IllegalArgumentException("Specialty vehicles require native Chain propulsion");
    var provider=EXTERNAL.get(id);
    if(provider==null) {
      if(!Set.of(NATIVE_CHAIN,ENGINEERED,MAGNETIZED,CTNB).contains(id))
        throw new IllegalArgumentException("Saved longitudinal provider is unavailable");
      return;
    }
    for(var car:cars)if(!provider.supports(l,car)||RideLocalOperations.isManaged(l,car))
      throw new IllegalArgumentException("External provider does not support this vehicle or managed route");
  }
  private static FoundationData.Vehicle vehicle(ServerLevel l,UUID id) {
    var v=FoundationData.get(l).vehicles.get(id);
    if (FoundationData.get(l).moving.retaining(id) != null)
      throw new IllegalArgumentException("Moving assembly owns this train; operate its controller");
    if(v==null||FoundationData.VARIANT.equals(v.variant)||v.family==Family.PAX||v.recoveryCart!=null)
      throw new IllegalArgumentException("Ride-local authority requires a supported non-recovery vehicle");
    return v;
  }
  private static List<FoundationData.Vehicle> complete(ServerLevel l,FoundationData.Vehicle v) {
    var cars=VehicleOperations.consist(l,v);var nativeBodies=SubLevelContainer.getContainer(l);
    if(v.authority.owner!=null&&!new HashSet<>(v.authority.cars).equals(new HashSet<>(cars.stream().map(c->c.chassis).toList())))
      throw new IllegalArgumentException("Authority consist changed; retained for explicit recovery");
    for(var car:cars) {
      var ids=new ArrayList<UUID>();ids.add(car.chassis);car.carriers.forEach(c->ids.add(c.body));
      if(dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.is(car))ids.add(car.yaw.deck);
      for(var id:ids)if(id==null||!(nativeBodies.getSubLevel(id) instanceof ServerSubLevel b)||b.isRemoved())
        throw new IllegalArgumentException("Complete native ride bodies required");
    }
    return cars;
  }
  private static void safeRelease(ServerLevel l,List<FoundationData.Vehicle> cars) {
    stopped(l,cars);
    for(var car:cars) {
      if(!car.restraints||car.maintenance||car.alignmentRequired||car.recoveryCart!=null||!car.fault.isEmpty())
        throw new IllegalArgumentException("Release requires closed restraints, supported ride and no fault/recovery");
      if(!dev.fourthdimension.vehicle.WingVehicle.is(car)) {
        var rail=dev.fourthdimension.moving.MovingRouteContext.rail(l,car.boundFrom,car.boundTo);
        if(rail==null||!rail.valid||!rail.variant.equals(car.variant)||TrackGeometry.resolve(l,rail)==null)
          throw new IllegalArgumentException("Matching support unavailable");
      }
    }
  }
  private static void stopped(ServerLevel l,List<FoundationData.Vehicle> cars) {
    var c=SubLevelContainer.getContainer(l);
    for(var car:cars)if(!(c.getSubLevel(car.chassis) instanceof ServerSubLevel b)
        ||c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).length()>.05)
      throw new IllegalArgumentException("Stop the complete train before authority handoff");
  }
  private static void assign(ServerLevel l,List<FoundationData.Vehicle> cars,BlockPos pos,UUID owner,String provider,long epoch) {
    var ids=cars.stream().map(c->c.chassis).toList();
    for(var car:cars){var s=new RideAuthorityState();s.owner=owner;s.controller=pos.immutable();s.provider=provider;s.epoch=epoch;s.cars=ids;car.authority=s;car.revision++;}
    FoundationData.get(l).setDirty();
  }
  private static void change(ServerLevel l,FoundationData.Vehicle v,boolean held,boolean suspended) {
    for(var car:complete(l,v)){car.authority.held=held;car.authority.suspended=suspended;car.authority.sequence++;car.revision++;}
    FoundationData.get(l).setDirty();
  }
  private static void thread(ServerLevel l) {
    if(!l.getServer().isSameThread())throw new IllegalStateException("Ride authority requires the server thread");
  }
}
