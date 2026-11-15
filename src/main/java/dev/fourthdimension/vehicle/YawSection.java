package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.RotationProfile;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

/** Finite named canonical-distance intervals on one fingerprinted native track edge. */
public final class YawSection {
  public record Interval(UUID id,String name,double start,double end,YawState.Mode mode,double blend,
      double phiFrom,double phiTo,RotationProfile profile,UUID marker,BlockPos markerPos,String dimension,
      YawState.Winding winding,double speed,double acceleration,String stopName,long showDuration){
    public Interval {
      if(id==null||name.isBlank()||name.length()>48||!Double.isFinite(start)||!Double.isFinite(end)||start<0||end<=start||end>65536
          ||!(mode==YawState.Mode.PROFILE||mode==YawState.Mode.TARGET||mode==YawState.Mode.SHOW_STOP)
          ||!Double.isFinite(blend)||blend<0||blend>end-start||!Double.isFinite(phiFrom)||!Double.isFinite(phiTo)||phiFrom<0||phiFrom>Math.PI/2||phiTo<0||phiTo>Math.PI/2
          ||phiFrom!=phiTo&&end-start<20||!Double.isFinite(speed)||speed<=0||speed>.6||!Double.isFinite(acceleration)||acceleration<=0||acceleration>.8
          ||profile==null||profile.keys().stream().anyMatch(k->k.slope()!=0)||profile.keys().getFirst().distance()!=0||profile.keys().getLast().distance()>end-start
          ||(mode==YawState.Mode.TARGET)&&(marker==null||markerPos==null||dimension.isBlank())
          ||(mode==YawState.Mode.SHOW_STOP)&&(stopName.isBlank()||showDuration<1||showDuration>12000))throw new IllegalArgumentException("Invalid named yaw interval");
    }
    public double progress(double at){return Math.clamp((at-start)/(end-start),0,1);}
    public double posture(double at){double p=progress(at);return phiFrom+(phiTo-phiFrom)*p*p*(3-2*p);}
  }
  public long revision,geometry;
  public UUID owner;
  public List<Interval> intervals=List.of();
  public Interval at(double distance){return intervals.stream().filter(i->distance>=i.start&&distance<=i.end).findFirst().orElse(null);}
  public void validate(){
    if(intervals.size()>32||revision<0)throw new IllegalArgumentException("Too many yaw intervals");double last=-1;var ids=new HashSet<UUID>();
    for(var i:intervals){if(i.start<last||!ids.add(i.id))throw new IllegalArgumentException("Overlapping or duplicate yaw intervals");last=i.end;}
  }
  public CompoundTag save(){var t=new CompoundTag();t.putInt("version",1);t.putLong("revision",revision);t.putLong("geometry",geometry);if(owner!=null)t.putUUID("owner",owner);var list=new ListTag();
    for(var i:intervals){var a=new CompoundTag();a.putUUID("id",i.id);a.putString("name",i.name);a.putDouble("start",i.start);a.putDouble("end",i.end);a.putString("mode",i.mode.name());a.putDouble("blend",i.blend);a.putDouble("phi_from",i.phiFrom);a.putDouble("phi_to",i.phiTo);a.putString("winding",i.winding.name());a.putDouble("speed",i.speed);a.putDouble("acceleration",i.acceleration);a.putString("stop_name",i.stopName);a.putLong("show_duration",i.showDuration);
      if(i.marker!=null){a.putUUID("marker",i.marker);a.putLong("marker_pos",i.markerPos.asLong());a.putString("dimension",i.dimension);}
      var keys=new ListTag();for(var k:i.profile.keys()){var q=new CompoundTag();q.putDouble("distance",k.distance());q.putDouble("angle",k.angle());keys.add(q);}a.put("keys",keys);list.add(a);
    }t.put("intervals",list);return t;
  }
  public static YawSection read(CompoundTag t){if(t.getInt("version")!=1)throw new IllegalArgumentException("Unknown yaw program");var s=new YawSection();s.revision=t.getLong("revision");s.geometry=t.getLong("geometry");if(t.hasUUID("owner"))s.owner=t.getUUID("owner");var list=new ArrayList<Interval>();
    for(Tag raw:t.getList("intervals",Tag.TAG_COMPOUND)){var a=(CompoundTag)raw;var keys=new ArrayList<RotationProfile.Key>();for(Tag kr:a.getList("keys",Tag.TAG_COMPOUND)){var k=(CompoundTag)kr;keys.add(new RotationProfile.Key(k.getDouble("distance"),k.getDouble("angle")));}
      list.add(new Interval(a.getUUID("id"),a.getString("name"),a.getDouble("start"),a.getDouble("end"),YawState.Mode.valueOf(a.getString("mode")),a.getDouble("blend"),a.getDouble("phi_from"),a.getDouble("phi_to"),new RotationProfile(keys),a.hasUUID("marker")?a.getUUID("marker"):null,a.hasUUID("marker")?BlockPos.of(a.getLong("marker_pos")):null,a.getString("dimension"),YawState.Winding.valueOf(a.getString("winding")),a.getDouble("speed"),a.getDouble("acceleration"),a.getString("stop_name"),a.getLong("show_duration")));}
    s.intervals=List.copyOf(list);s.validate();return s;
  }
}
