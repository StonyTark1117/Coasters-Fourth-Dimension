package dev.fourthdimension.vehicle;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

/** Saved operational state; measured native angle/rate remain in the physical carrier record. */
public final class FlyingState {
  public enum Phase { SEATED_OPEN, SEATED_SECURED, TO_PRONE, PRONE_LOCKED, RUNNING,
    DOCK_PRONE, TO_SEATED, SEATED_LOCKED, INTERRUPTED_TRANSITION }
  public enum Lock { NONE, SEATED, PRONE }
  public Phase phase=Phase.SEATED_OPEN;
  public Lock lock=Lock.NONE;
  public boolean body, legs;
  public double target, progress, holdAngle;
  public boolean referenceInitialized;
  public double referenceAngle, referenceVelocity;
  public String interruption="";
  public BlockPos dockFrom,dockTo;
  public long dockGeometry;
  public List<UUID> consist=List.of();
  public boolean transitioning(){return phase==Phase.TO_PRONE||phase==Phase.TO_SEATED;}
  public boolean retained(){return body&&legs;}
  public CompoundTag save(){
    var t=new CompoundTag();t.putInt("version",1);t.putString("phase",phase.name());t.putString("lock",lock.name());
    t.putBoolean("body",body);t.putBoolean("legs",legs);t.putDouble("target",target);
    t.putDouble("progress",progress);t.putDouble("hold_angle",holdAngle);t.putString("interruption",interruption);
    if(referenceInitialized){t.putDouble("reference_angle",referenceAngle);t.putDouble("reference_velocity",referenceVelocity);}
    if(dockFrom!=null&&dockTo!=null){t.putLong("dock_from",dockFrom.asLong());t.putLong("dock_to",dockTo.asLong());t.putLong("dock_geometry",dockGeometry);}
    var ids=new ListTag();for(var id:consist)ids.add(NbtUtils.createUUID(id));t.put("consist",ids);return t;
  }
  public static FlyingState read(CompoundTag t){
    if(t.getInt("version")!=1)throw new IllegalArgumentException("Unknown Flying posture state");
    var s=new FlyingState();s.phase=Phase.valueOf(t.getString("phase"));s.lock=Lock.valueOf(t.getString("lock"));
    s.body=t.getBoolean("body");s.legs=t.getBoolean("legs");s.target=t.getDouble("target");s.progress=t.getDouble("progress");
    s.holdAngle=t.getDouble("hold_angle");s.interruption=t.getString("interruption");
    if(t.contains("reference_angle")){s.referenceInitialized=true;s.referenceAngle=t.getDouble("reference_angle");s.referenceVelocity=t.getDouble("reference_velocity");}
    if(t.contains("dock_from")){s.dockFrom=BlockPos.of(t.getLong("dock_from"));s.dockTo=BlockPos.of(t.getLong("dock_to"));s.dockGeometry=t.getLong("dock_geometry");}
    var ids=new ArrayList<UUID>();for(Tag id:t.getList("consist",Tag.TAG_INT_ARRAY))ids.add(NbtUtils.loadUUID(id));s.consist=List.copyOf(ids);
    s.validate();return s;
  }
  public void validate(){
    if(!Double.isFinite(target)||(target!=0&&target!=Math.PI/2)||!Double.isFinite(progress)||progress<0||progress>1
        ||!Double.isFinite(holdAngle)||holdAngle<-.05||holdAngle>Math.PI/2+.05
        ||!Double.isFinite(referenceAngle)||referenceAngle< -Math.PI/2-.05||referenceAngle>.05
        ||!Double.isFinite(referenceVelocity)||Math.abs(referenceVelocity)>.25
        ||consist.size()>4||new HashSet<>(consist).size()!=consist.size()
        ||(transitioning()||phase==Phase.INTERRUPTED_TRANSITION)&&(!retained()||lock!=Lock.NONE)
        ||lock==Lock.PRONE&&!retained())throw new IllegalArgumentException("Invalid Flying posture/retention state");
  }
}
