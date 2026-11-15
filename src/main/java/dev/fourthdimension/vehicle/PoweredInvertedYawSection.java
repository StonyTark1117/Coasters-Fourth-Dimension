package dev.fourthdimension.vehicle;

import java.util.*;
import net.minecraft.nbt.*;

/** One fingerprinted route edge: shared yaw intervals plus bounded travel/show stops. */
public final class PoweredInvertedYawSection {
  public record Speed(double start,double end,double speed){
    public Speed {if(!Double.isFinite(start)||!Double.isFinite(end)||!Double.isFinite(speed)||start<0||end<=start||end>65536||speed<=0||speed>8)throw new IllegalArgumentException("Travel sections require start < end, speed (0,8]");}
  }
  public record Stop(UUID id,String name,double distance,int dwell,int afterDirection){
    public Stop {if(id==null||name.isBlank()||name.length()>48||!Double.isFinite(distance)||distance<0||distance>65536||dwell<20||dwell>1200||Math.abs(afterDirection)>1)throw new IllegalArgumentException("Stop requires a name, distance, dwell20..1200 and direction -1/0/+1");}
  }
  public YawSection yaw=new YawSection();
  public List<Speed> speeds=List.of();
  public List<Stop> stops=List.of();
  public void validate(){
    yaw.validate();if(speeds.size()>32||stops.size()>32)throw new IllegalArgumentException("At most32 speed sections and stops");
    for(var i:yaw.intervals)if(i.phiFrom()!=0||i.phiTo()!=0)throw new IllegalArgumentException("Powered Inverted Yaw is permanently upright");
    double end=-1;for(var s:speeds){if(s.start()<end)throw new IllegalArgumentException("Overlapping travel speed sections");end=s.end();}
    var ids=new HashSet<UUID>();double previous=-1;for(var s:stops){if(!ids.add(s.id())||s.distance()<=previous)throw new IllegalArgumentException("Stops require unique IDs and increasing distance");previous=s.distance();}
  }
  public Speed speed(double at){return speeds.stream().filter(s->at>=s.start()&&at<=s.end()).findFirst().orElse(null);}
  public CompoundTag save(){
    var t=new CompoundTag();t.putInt("version",1);t.put("yaw",yaw.save());var speed=new ListTag();for(var s:speeds){var n=new CompoundTag();n.putDouble("start",s.start());n.putDouble("end",s.end());n.putDouble("speed",s.speed());speed.add(n);}t.put("speeds",speed);
    var stop=new ListTag();for(var s:stops){var n=new CompoundTag();n.putUUID("id",s.id());n.putString("name",s.name());n.putDouble("distance",s.distance());n.putInt("dwell",s.dwell());n.putInt("direction",s.afterDirection());stop.add(n);}t.put("stops",stop);return t;
  }
  public static PoweredInvertedYawSection read(CompoundTag t){
    if(t.getInt("version")!=1)throw new IllegalArgumentException("Unknown powered yaw section");var s=new PoweredInvertedYawSection();s.yaw=YawSection.read(t.getCompound("yaw"));
    var speed=new ArrayList<Speed>();for(var a:t.getList("speeds",Tag.TAG_COMPOUND)){var n=(CompoundTag)a;speed.add(new Speed(n.getDouble("start"),n.getDouble("end"),n.getDouble("speed")));}s.speeds=List.copyOf(speed);
    var stop=new ArrayList<Stop>();for(var a:t.getList("stops",Tag.TAG_COMPOUND)){var n=(CompoundTag)a;stop.add(new Stop(n.getUUID("id"),n.getString("name"),n.getDouble("distance"),n.getInt("dwell"),n.getInt("direction")));}s.stops=List.copyOf(stop);s.validate();return s;
  }
}
