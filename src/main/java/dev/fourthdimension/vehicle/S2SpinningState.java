package dev.fourthdimension.vehicle;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

/** Persistent launch authority and complete-train capture identity; no angle is synthesized here. */
public final class S2SpinningState {
  public BlockPos dockFrom,dockTo,governor;
  public long dockGeometry,epoch;
  public List<UUID> consist=List.of();
  public double rampSpeed;
  public int direction=1;
  public boolean resumeRequired;
  public String phase="DOCK_OPEN";

  public CompoundTag save(){
    var n=new CompoundTag();n.putInt("version",1);
    if(dockFrom!=null){n.putLong("dock_from",dockFrom.asLong());n.putLong("dock_to",dockTo.asLong());n.putLong("dock_geometry",dockGeometry);}
    if(governor!=null)n.putLong("governor",governor.asLong());
    var ids=new ListTag();for(var id:consist)ids.add(NbtUtils.createUUID(id));n.put("consist",ids);
    n.putLong("epoch",epoch);n.putDouble("ramp_speed",rampSpeed);n.putInt("direction",direction);
    n.putBoolean("resume_required",resumeRequired);n.putString("phase",phase);return n;
  }
  public static S2SpinningState read(CompoundTag n){
    var s=new S2SpinningState();
    if(n.isEmpty())return s;
    if(n.getInt("version")!=1)throw new IllegalArgumentException("Unsupported S2 state");
    if(n.contains("dock_from")){s.dockFrom=BlockPos.of(n.getLong("dock_from"));s.dockTo=BlockPos.of(n.getLong("dock_to"));s.dockGeometry=n.getLong("dock_geometry");}
    if(n.contains("governor"))s.governor=BlockPos.of(n.getLong("governor"));
    var ids=new ArrayList<UUID>();for(Tag id:n.getList("consist",Tag.TAG_INT_ARRAY))ids.add(NbtUtils.loadUUID(id));
    if(ids.size()>4||new HashSet<>(ids).size()!=ids.size())throw new IllegalArgumentException("Invalid S2 consist");
    s.consist=List.copyOf(ids);s.epoch=n.getLong("epoch");s.rampSpeed=n.getDouble("ramp_speed");
    s.direction=n.getInt("direction");s.resumeRequired=n.getBoolean("resume_required");s.phase=n.getString("phase");
    if(!Double.isFinite(s.rampSpeed)||s.rampSpeed<0||s.rampSpeed>12||Math.abs(s.direction)!=1||s.epoch<0
        ||!Set.of("DOCK_OPEN","SECURED_CAPTURED","DISPATCH","FREE","DAMP","STOPPED_ALIGN","CAPTURED","FAULT").contains(s.phase))
      throw new IllegalArgumentException("Invalid S2 launch state");
    return s;
  }
}
