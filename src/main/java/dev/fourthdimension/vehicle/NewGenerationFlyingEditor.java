package dev.fourthdimension.vehicle;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** Server transaction behind the finite route form; never accepts arbitrary scripting. */
public final class NewGenerationFlyingEditor {
  private NewGenerationFlyingEditor(){}
  public static void operator(ServerPlayer player,FoundationData.Vehicle v){if(!player.hasPermissions(2)&&!player.getUUID().equals(v.yaw.owner))throw new IllegalArgumentException("Only the ride owner or operator may change its controls");}
  public static FoundationData.Rail editable(ServerPlayer p,UUID id,long revision){
    var rail=FoundationData.get(p.serverLevel()).rails.stream().filter(r->r.id.equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Route no longer exists"));
    if(!p.mayBuild()||!p.serverLevel().mayInteract(p,rail.from)||!p.serverLevel().mayInteract(p,rail.to)||rail.revision!=revision)throw new IllegalArgumentException("Route changed or editing is not permitted");
    var edge=TrackGeometry.resolve(p.serverLevel(),rail);boolean near=false;
    if(edge!=null)for(int k=0;k<=256;k++)if(p.position().distanceToSqr(dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.sampleEdgePosition(p.level(),edge,k/256.0))<=64){near=true;break;}
    if(!near)throw new IllegalArgumentException("Stand within eight blocks of this route section");return rail;
  }
  public static void apply(ServerPlayer p,FoundationData.Rail rail,String text){
    if(!rail.variant.equals(NewGenerationFlyingVehicle.VARIANT))throw new IllegalArgumentException("Select compound Flying track");
    if(rail.yaw.owner!=null&&!p.hasPermissions(2)&&!p.getUUID().equals(rail.yaw.owner))throw new IllegalArgumentException("Only this route's owner/operator may edit it");
    var data=FoundationData.get(p.serverLevel());
    for(var v:data.vehicles.values())if(NewGenerationFlyingVehicle.is(v)&&Objects.equals(v.yaw.owner,rail.yaw.owner)){
      if(!v.stopping||v.carriers.stream().anyMatch(c->!c.occupants.isEmpty()||!c.riderSeats.isEmpty()))throw new IllegalArgumentException("Empty and stop this ride before authoring its route");
    }
    var edge=TrackGeometry.resolve(p.serverLevel(),rail);if(edge==null||!rail.valid||TrackGeometry.fingerprint(p.serverLevel(),edge)!=rail.geometryHash)throw new IllegalArgumentException("Rebind changed route geometry first");
    var fields=text.split("\\|",-1);if(fields.length!=14)throw new IllegalArgumentException("Complete every route form field");
    try{
      String name=fields[0].trim();double start=Double.parseDouble(fields[1]),end=Double.parseDouble(fields[2]);var mode=YawState.Mode.valueOf(fields[3]);
      double phiFrom=Math.toRadians(Double.parseDouble(fields[4])),phiTo=Math.toRadians(Double.parseDouble(fields[5]));var profile=EditorNetwork.parse(fields[6]);
      BlockPos pos=null;UUID marker=null;String dimension="";
      if(mode==YawState.Mode.TARGET){var xyz=fields[7].trim().split(" +");if(xyz.length!=3)throw new IllegalArgumentException("Marker position needs x y z");pos=new BlockPos(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));
        if(!p.serverLevel().hasChunkAt(pos))throw new IllegalArgumentException("Marker chunk is unloaded");
        if(!(p.serverLevel().getBlockEntity(pos) instanceof YawMarkerEntity m))throw new IllegalArgumentException("Place a yaw marker at that position first");
        if(!Objects.equals(m.owner,rail.yaw.owner==null?p.getUUID():rail.yaw.owner))throw new IllegalArgumentException("Bind a marker owned by this ride's creator");marker=m.id;dimension=p.level().dimension().location().toString();}
      double length=TrackGeometry.table(p.level(),edge).length();if(end>length+.001)throw new IllegalArgumentException("Interval exceeds section length "+length);
      var old=rail.yaw.intervals.stream().filter(i->i.name().equals(name)).findFirst().orElse(null);
      var interval=new YawSection.Interval(old==null?UUID.randomUUID():old.id(),name,start,end,mode,Double.parseDouble(fields[8]),phiFrom,phiTo,profile,marker,pos,dimension,YawState.Winding.valueOf(fields[9]),Double.parseDouble(fields[10]),Double.parseDouble(fields[11]),fields[12],Long.parseLong(fields[13]));
      var proposed=new YawSection();proposed.owner=rail.yaw.owner==null?p.getUUID():rail.yaw.owner;proposed.geometry=rail.geometryHash;proposed.revision=rail.yaw.revision+1;
      var list=new ArrayList<>(rail.yaw.intervals);list.removeIf(i->i.name().equals(name));list.add(interval);list.sort(Comparator.comparingDouble(YawSection.Interval::start));proposed.intervals=List.copyOf(list);proposed.validate();
      rail.yaw=proposed;rail.revision++;data.setDirty();FoundationSync.broadcast(p.serverLevel());
    }catch(NumberFormatException e){throw new IllegalArgumentException("Use finite numbers in the route form");}
  }
  public static String format(YawSection.Interval i){return String.join("|",i.name(),""+i.start(),""+i.end(),i.mode().name(),""+Math.toDegrees(i.phiFrom()),""+Math.toDegrees(i.phiTo()),i.profile().keys().stream().map(k->k.distance()+":"+Math.toDegrees(k.angle())).collect(java.util.stream.Collectors.joining(";")),i.markerPos()==null?"":i.markerPos().getX()+" "+i.markerPos().getY()+" "+i.markerPos().getZ(),""+i.blend(),i.winding().name(),""+i.speed(),""+i.acceleration(),i.stopName(),""+i.showDuration());}
}
