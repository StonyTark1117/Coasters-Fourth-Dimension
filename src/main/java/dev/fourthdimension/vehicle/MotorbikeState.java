package dev.fourthdimension.vehicle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import java.util.HashMap;
import java.util.Map;

/** Persisted motion intent; a fresh process must never replay a previous release. */
public final class MotorbikeState {
  public boolean running, restored;
  public final Map<BlockPos,Integer> directions=new HashMap<>();
  public CompoundTag save() {
    var t=new CompoundTag();t.putInt("version",1);t.putBoolean("running",running);
    var list=new ListTag();directions.forEach((p,s)->{var d=new CompoundTag();d.putLong("pos",p.asLong());d.putInt("sign",s);list.add(d);});t.put("directions",list);return t;
  }
  public static MotorbikeState read(CompoundTag t) {
    if(t.getInt("version")!=1)throw new IllegalArgumentException("Unknown Motorbike state version");
    var s=new MotorbikeState();s.running=t.getBoolean("running");s.restored=true;
    var list=t.getList("directions",10);if(list.size()>64)throw new IllegalArgumentException("Motorbike drive capacity");
    for(int i=0;i<list.size();i++){var d=list.getCompound(i);int sign=d.getInt("sign");
      if(Math.abs(sign)!=1||s.directions.put(BlockPos.of(d.getLong("pos")),sign)!=null)throw new IllegalArgumentException("Invalid Motorbike drive intent");}
    return s;
  }
}
