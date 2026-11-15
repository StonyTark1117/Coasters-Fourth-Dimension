package dev.fourthdimension.vehicle;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** Finite operator form; only empty/stopped owned rides can change route programs. */
public final class PoweredInvertedYawEditor {
  private PoweredInvertedYawEditor(){}
  public static void operator(ServerPlayer p,FoundationData.Vehicle v){if(!p.hasPermissions(2)&&!p.getUUID().equals(v.poweredYaw.owner))throw new IllegalArgumentException("Only the ride owner/operator may change powered controls");}
  public static void apply(ServerPlayer p,UUID id,long revision,String text){
    var rail=NewGenerationFlyingEditor.editable(p,id,revision);if(!rail.variant.equals(PoweredInvertedYawVehicle.VARIANT))throw new IllegalArgumentException("Select Powered Inverted Yaw track");
    var old=rail.poweredYaw;var owner=old.yaw.owner==null?p.getUUID():old.yaw.owner;if(!p.hasPermissions(2)&&!p.getUUID().equals(owner))throw new IllegalArgumentException("Only the route owner/operator may edit it");
    var data=FoundationData.get(p.serverLevel());for(var v:data.vehicles.values())if(PoweredInvertedYawVehicle.is(v)&&(Objects.equals(v.poweredYaw.owner,owner)||Objects.equals(v.boundFrom,rail.from)||Objects.equals(v.boundTo,rail.to)))if(!v.stopping||v.carriers.stream().anyMatch(c->!c.occupants.isEmpty()||!c.riderSeats.isEmpty()))throw new IllegalArgumentException("Empty and stop this ride before editing");
    var edge=TrackGeometry.resolve(p.serverLevel(),rail);if(edge==null||!rail.valid||TrackGeometry.fingerprint(p.serverLevel(),edge)!=rail.geometryHash)throw new IllegalArgumentException("Rebind changed geometry first");
    var f=text.split("\\|",-1);if(f.length!=16)throw new IllegalArgumentException("Complete all sixteen powered route fields");
    try{
      String name=f[0].trim();double start=Double.parseDouble(f[1]),end=Double.parseDouble(f[2]);var mode=YawState.Mode.valueOf(f[3]);var profile=EditorNetwork.parse(f[4]);double length=TrackGeometry.table(p.level(),edge).length();if(end>length+.001)throw new IllegalArgumentException("Interval exceeds track length "+length);
      BlockPos markerPos=null;UUID marker=null;String dimension="";
      if(mode==YawState.Mode.TARGET){var xyz=f[5].trim().split(" +");if(xyz.length!=3)throw new IllegalArgumentException("Marker needs x y z");markerPos=new BlockPos(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));if(!p.level().hasChunkAt(markerPos)||!(p.level().getBlockEntity(markerPos) instanceof YawMarkerEntity m)||!owner.equals(m.owner))throw new IllegalArgumentException("Place an owned yaw marker first");marker=m.id;dimension=p.level().dimension().location().toString();}
      var existing=old.yaw.intervals.stream().filter(i->i.name().equals(name)).findFirst().orElse(null);UUID key=existing==null?UUID.randomUUID():existing.id();
      var interval=new YawSection.Interval(key,name,start,end,mode,Double.parseDouble(f[6]),0,0,profile,marker,markerPos,dimension,YawState.Winding.valueOf(f[7]),Double.parseDouble(f[8]),Double.parseDouble(f[9]),f[10],Long.parseLong(f[11]));
      var next=PoweredInvertedYawSection.read(old.save());next.yaw.owner=owner;next.yaw.geometry=rail.geometryHash;next.yaw.revision++;
      var yaw=new ArrayList<>(next.yaw.intervals);yaw.removeIf(i->i.name().equals(name));yaw.add(interval);yaw.sort(Comparator.comparingDouble(YawSection.Interval::start));next.yaw.intervals=List.copyOf(yaw);
      var speeds=new ArrayList<>(next.speeds);if(existing!=null)speeds.removeIf(s->s.start()==existing.start()&&s.end()==existing.end());speeds.add(new PoweredInvertedYawSection.Speed(start,end,Double.parseDouble(f[12])));speeds.sort(Comparator.comparingDouble(PoweredInvertedYawSection.Speed::start));next.speeds=List.copyOf(speeds);
      var stops=new ArrayList<>(next.stops);stops.removeIf(s->s.id().equals(key));if(!f[13].isBlank()){double distance=Double.parseDouble(f[13]);if(distance<start||distance>end)throw new IllegalArgumentException("Put the stop inside this section");stops.add(new PoweredInvertedYawSection.Stop(key,f[10].isBlank()?name:f[10],distance,Integer.parseInt(f[14]),Integer.parseInt(f[15])));}stops.sort(Comparator.comparingDouble(PoweredInvertedYawSection.Stop::distance));next.stops=List.copyOf(stops);next.validate();
      rail.poweredYaw=next;rail.revision++;data.setDirty();FoundationSync.broadcast(p.serverLevel());
    }catch(NumberFormatException e){throw new IllegalArgumentException("Use finite numbers in the powered route form");}
  }
  public static String format(PoweredInvertedYawSection section,YawSection.Interval i){
    var speed=section.speeds.stream().filter(s->s.start()==i.start()).findFirst().orElse(new PoweredInvertedYawSection.Speed(i.start(),i.end(),4));var stop=section.stops.stream().filter(s->s.id().equals(i.id())).findFirst().orElse(null);
    return String.join("|",i.name(),""+i.start(),""+i.end(),i.mode().name(),i.profile().keys().stream().map(k->k.distance()+":"+Math.toDegrees(k.angle())).collect(java.util.stream.Collectors.joining(";")),i.markerPos()==null?"":i.markerPos().getX()+" "+i.markerPos().getY()+" "+i.markerPos().getZ(),""+i.blend(),i.winding().name(),""+i.speed(),""+i.acceleration(),i.stopName(),""+i.showDuration(),""+speed.speed(),stop==null?"":""+stop.distance(),stop==null?"200":""+stop.dwell(),stop==null?"0":""+stop.afterDirection());
  }
  public static void sound(ServerPlayer p,FoundationData.Vehicle v){operator(p,v);if(!v.stopping||v.carriers.stream().anyMatch(c->!c.occupants.isEmpty()))throw new IllegalArgumentException("Empty and stop before changing event cues");var g=PoweredInvertedYawRuntime.governor(p.serverLevel(),v);if(g==null)throw new IllegalArgumentException("Restore the bound governor");g.sound=!g.sound;g.setChanged();g.sendData();}
}
