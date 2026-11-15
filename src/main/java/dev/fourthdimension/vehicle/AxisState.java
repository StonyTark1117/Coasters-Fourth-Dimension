package dev.fourthdimension.vehicle;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

/** Persisted route-mode handoff and bounded motor target; native bodies own actual roll. */
public final class AxisState extends dev.fourthdimension.physics.AxisDrive {
  public enum Mode { FREE, DAMP, DRIVE }
  public Mode mode=Mode.FREE;
  public double blend, windingOffset;
  public boolean activeProgram;
  public UUID profile;
  public long profileRevision;
  public BlockPos dockFrom,dockTo;
  public long dockGeometry;
  public List<UUID> consist=List.of();

  public CompoundTag save(){
    var t=new CompoundTag();t.putString("mode",mode.name());t.putDouble("command",command);
    t.putDouble("rate",commandRate);t.putDouble("blend",blend);t.putDouble("winding_offset",windingOffset);t.putBoolean("initialized",initialized);t.putBoolean("active_program",activeProgram);
    if(profile!=null)t.putUUID("profile",profile);t.putLong("profile_revision",profileRevision);
    if(dockFrom!=null){t.putLong("dock_from",dockFrom.asLong());t.putLong("dock_to",dockTo.asLong());t.putLong("dock_geometry",dockGeometry);}
    var ids=new ListTag();for(var id:consist)ids.add(NbtUtils.createUUID(id));t.put("consist",ids);return t;
  }
  public static AxisState read(CompoundTag t){
    var s=new AxisState();if(t.contains("mode"))s.mode=Mode.valueOf(t.getString("mode"));
    s.command=t.getDouble("command");s.commandRate=t.getDouble("rate");s.blend=t.getDouble("blend");s.windingOffset=t.getDouble("winding_offset");s.initialized=t.getBoolean("initialized");s.activeProgram=t.getBoolean("active_program");
    if(t.hasUUID("profile"))s.profile=t.getUUID("profile");s.profileRevision=t.getLong("profile_revision");
    if(t.contains("dock_from")){s.dockFrom=BlockPos.of(t.getLong("dock_from"));s.dockTo=BlockPos.of(t.getLong("dock_to"));s.dockGeometry=t.getLong("dock_geometry");}
    var ids=new ArrayList<UUID>();for(var id:t.getList("consist",Tag.TAG_INT_ARRAY))ids.add(NbtUtils.loadUUID(id));s.consist=List.copyOf(ids);
    if(!Double.isFinite(s.windingOffset)||!Double.isFinite(s.command)||!Double.isFinite(s.commandRate)||Math.abs(s.commandRate)>1.0001
        ||!Double.isFinite(s.blend)||s.blend<0||s.blend>1||ids.size()>2||new HashSet<>(ids).size()!=ids.size())
      throw new IllegalArgumentException("Invalid Axis drive/dock state");
    return s;
  }
}
