package dev.fourthdimension.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Saved route intent, never a live transmission lease. */
public final class FlyingBikeState {
  public BlockPos governor, dockFrom, dockTo, evacuationStation;
  public long dockGeometry, routeHash;
  public String phase="DOCK";
  public boolean resumeRequired;
  public transient boolean initialized;
  public transient int evacuationStoppedTicks;
  public int launch;
  public double previousProgress=-1;
  public CompoundTag save(){
    var n=new CompoundTag();
    if(evacuationStation!=null)n.putLong("evacuation_station",evacuationStation.asLong());
    if(governor!=null)n.putLong("governor",governor.asLong());
    if(dockFrom!=null){n.putLong("dock_from",dockFrom.asLong());n.putLong("dock_to",dockTo.asLong());n.putLong("dock_geometry",dockGeometry);}
    n.putLong("route_hash",routeHash);n.putString("phase",phase);n.putBoolean("resume_required",resumeRequired);n.putInt("launch",launch);
    return n;
  }
  public static FlyingBikeState read(CompoundTag n){
    var s=new FlyingBikeState();
    if(n.contains("evacuation_station"))s.evacuationStation=BlockPos.of(n.getLong("evacuation_station"));
    if(n.contains("governor"))s.governor=BlockPos.of(n.getLong("governor"));
    if(n.contains("dock_from")){s.dockFrom=BlockPos.of(n.getLong("dock_from"));s.dockTo=BlockPos.of(n.getLong("dock_to"));s.dockGeometry=n.getLong("dock_geometry");}
    s.routeHash=n.getLong("route_hash");s.phase=n.contains("phase")?n.getString("phase"):"DOCK";
    s.resumeRequired=n.getBoolean("resume_required");s.launch=n.getInt("launch");
    if(!java.util.Set.of("DOCK","LAUNCH_1","COAST_1","LAUNCH_2","COAST_2","BRAKING","FAULT","RECOVERY").contains(s.phase)||s.launch<0||s.launch>2){s.phase="FAULT";s.resumeRequired=true;s.launch=0;}
    return s;
  }
}
