package dev.fourthdimension.vehicle;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
/** Versioned travel observations; native child pose remains authoritative. */
public final class StandingState {
  public final Map<BlockPos,Integer> directions=new HashMap<>();
  public double travel,velocity,heldTravel;
  public boolean interrupted;
  public void validate(){
    if(!Double.isFinite(travel)||travel<-.04||travel>.44||!Double.isFinite(velocity)||Math.abs(velocity)>4
        ||!Double.isFinite(heldTravel)||heldTravel<0||heldTravel>.40)throw new IllegalArgumentException("Invalid standing travel state");
    if(directions.size()>4096||directions.values().stream().anyMatch(s->s!=1&&s!=-1))throw new IllegalArgumentException("Invalid standing drive directions");
  }
  public CompoundTag save(){validate();var t=new CompoundTag();t.putInt("version",1);t.putDouble("travel",travel);t.putDouble("velocity",velocity);t.putDouble("heldTravel",heldTravel);t.putBoolean("interrupted",interrupted);var list=new ListTag();directions.forEach((p,s)->{var e=new CompoundTag();e.putLong("anchor",p.asLong());e.putInt("sign",s);list.add(e);});t.put("directions",list);return t;}
  public static StandingState read(CompoundTag t){if(t.getInt("version")!=1)throw new IllegalArgumentException("Unsupported standing state version");var s=new StandingState();s.travel=t.getDouble("travel");s.velocity=t.getDouble("velocity");s.heldTravel=t.getDouble("heldTravel");s.interrupted=t.getBoolean("interrupted");for(var raw:t.getList("directions",Tag.TAG_COMPOUND)){var e=(CompoundTag)raw;s.directions.put(BlockPos.of(e.getLong("anchor")),e.getInt("sign"));}s.validate();return s;}
}
