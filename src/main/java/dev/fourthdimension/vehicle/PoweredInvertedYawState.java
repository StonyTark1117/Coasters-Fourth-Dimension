package dev.fourthdimension.vehicle;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

/** Upright single-joint yaw and finite travel/event state; no Flying posture state. */
public final class PoweredInvertedYawState {
  public enum Phase { DOCK_OPEN, CAPTURED, RUN, STOP_APPROACH, HELD_DWELL, RETURN_CAPTURE, FAULT }
  public YawState.Mode mode=YawState.Mode.STATION_CAPTURE,nextMode=YawState.Mode.PROFILE;
  public Phase phase=Phase.DOCK_OPEN,interruptedPhase=Phase.DOCK_OPEN;
  public YawState.Mode interruptedMode=YawState.Mode.STATION_CAPTURE;
  public BlockPos controller,dockFrom,dockTo;
  public UUID owner,section,stop;
  public List<UUID> consist=List.of();
  public double reference,referenceVelocity,target,entryAngle,entryDistance,distance,travelRequest;
  public int direction=1,pendingDirection=1,speedIndex=-1,nextStop=-1,stoppedTicks;
  public long dockGeometry,sectionRevision,routeGeometry,dwellRemaining,showTicks,epoch,sequence,lastTick=-1;
  public boolean initialized,resumeRequired,holdAcknowledged,yawReached;
  public String status="READY";
  public final Map<String,Long> eventTicks=new LinkedHashMap<>();
  public CompoundTag save(){
    var t=new CompoundTag();t.putInt("version",1);t.putString("axes","chassis_y_upright");t.putString("mode",mode.name());t.putString("next_mode",nextMode.name());t.putString("phase",phase.name());t.putString("interrupted_phase",interruptedPhase.name());t.putString("interrupted_mode",interruptedMode.name());
    pos(t,"controller",controller);pos(t,"dock_from",dockFrom);pos(t,"dock_to",dockTo);id(t,"owner",owner);id(t,"section",section);id(t,"stop",stop);
    var ids=new ListTag();for(var u:consist)ids.add(NbtUtils.createUUID(u));t.put("consist",ids);
    t.putDouble("reference",reference);t.putDouble("reference_velocity",referenceVelocity);t.putDouble("target",target);t.putDouble("entry_angle",entryAngle);t.putDouble("entry_distance",entryDistance);t.putDouble("distance",distance);t.putDouble("travel_request",travelRequest);
    t.putInt("direction",direction);t.putInt("pending_direction",pendingDirection);t.putInt("speed_index",speedIndex);t.putInt("next_stop",nextStop);t.putInt("stopped_ticks",stoppedTicks);
    t.putLong("dock_geometry",dockGeometry);t.putLong("section_revision",sectionRevision);t.putLong("route_geometry",routeGeometry);t.putLong("dwell_remaining",dwellRemaining);t.putLong("show_ticks",showTicks);t.putLong("epoch",epoch);t.putLong("sequence",sequence);t.putLong("last_tick",lastTick);
    t.putBoolean("initialized",initialized);t.putBoolean("resume_required",resumeRequired);t.putBoolean("hold_acknowledged",holdAcknowledged);t.putBoolean("yaw_reached",yawReached);t.putString("status",status);
    var events=new CompoundTag();eventTicks.forEach(events::putLong);t.put("event_ticks",events);return t;
  }
  private static void pos(CompoundTag t,String key,BlockPos p){if(p!=null)t.putLong(key,p.asLong());}
  private static void id(CompoundTag t,String key,UUID u){if(u!=null)t.putUUID(key,u);}
  public static PoweredInvertedYawState read(CompoundTag t){
    if(t.getInt("version")!=1||!t.getString("axes").equals("chassis_y_upright"))throw new IllegalArgumentException("Unknown upright yaw state");
    var s=new PoweredInvertedYawState();s.mode=YawState.Mode.valueOf(t.getString("mode"));s.nextMode=YawState.Mode.valueOf(t.getString("next_mode"));s.phase=Phase.valueOf(t.getString("phase"));s.interruptedPhase=t.contains("interrupted_phase")?Phase.valueOf(t.getString("interrupted_phase")):s.phase;s.interruptedMode=t.contains("interrupted_mode")?YawState.Mode.valueOf(t.getString("interrupted_mode")):s.mode;
    s.controller=t.contains("controller")?BlockPos.of(t.getLong("controller")):null;s.dockFrom=t.contains("dock_from")?BlockPos.of(t.getLong("dock_from")):null;s.dockTo=t.contains("dock_to")?BlockPos.of(t.getLong("dock_to")):null;s.owner=t.hasUUID("owner")?t.getUUID("owner"):null;s.section=t.hasUUID("section")?t.getUUID("section"):null;s.stop=t.hasUUID("stop")?t.getUUID("stop"):null;
    var ids=new ArrayList<UUID>();for(var a:t.getList("consist",Tag.TAG_INT_ARRAY))ids.add(NbtUtils.loadUUID(a));s.consist=List.copyOf(ids);
    s.reference=t.getDouble("reference");s.referenceVelocity=t.getDouble("reference_velocity");s.target=t.getDouble("target");s.entryAngle=t.getDouble("entry_angle");s.entryDistance=t.getDouble("entry_distance");s.distance=t.getDouble("distance");s.travelRequest=t.getDouble("travel_request");
    s.direction=t.getInt("direction");s.pendingDirection=t.getInt("pending_direction");s.speedIndex=t.getInt("speed_index");s.nextStop=t.getInt("next_stop");s.stoppedTicks=t.getInt("stopped_ticks");s.dockGeometry=t.getLong("dock_geometry");s.sectionRevision=t.getLong("section_revision");s.routeGeometry=t.getLong("route_geometry");s.dwellRemaining=t.getLong("dwell_remaining");s.showTicks=t.getLong("show_ticks");s.epoch=t.getLong("epoch");s.sequence=t.getLong("sequence");s.lastTick=t.getLong("last_tick");
    s.initialized=t.getBoolean("initialized");s.resumeRequired=t.getBoolean("resume_required");s.holdAcknowledged=t.getBoolean("hold_acknowledged");s.yawReached=t.getBoolean("yaw_reached");s.status=t.getString("status");var events=t.getCompound("event_ticks");for(var key:events.getAllKeys())s.eventTicks.put(key,events.getLong(key));s.validate();return s;
  }
  public void validate(){
    for(double value:new double[]{reference,referenceVelocity,target,entryAngle,entryDistance,distance,travelRequest})if(!Double.isFinite(value))throw new IllegalArgumentException("Nonfinite upright yaw state");
    if(Math.abs(reference)>8*Math.PI+.001||Math.abs(target)>8*Math.PI+.001||Math.abs(referenceVelocity)>.600001||Math.abs(travelRequest)>8.000001||Math.abs(direction)!=1||Math.abs(pendingDirection)!=1||consist.size()>4||dwellRemaining<0||dwellRemaining>1200||showTicks<0||epoch<0||sequence<0||eventTicks.size()>512||stoppedTicks<0)throw new IllegalArgumentException("Invalid upright yaw state");
  }
}
