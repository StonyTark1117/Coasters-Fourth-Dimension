package dev.fourthdimension.moving;

import java.util.*;
import net.minecraft.nbt.*;

/** Saved measured parent-child frames for a released train still crossing its locked bridge. */
public final class MovingServiceState {
  public record Hold(UUID parent,UUID child,MovingAssemblyState.Pose parentFrame,MovingAssemblyState.Pose childFrame){
    public Hold{Objects.requireNonNull(parent);Objects.requireNonNull(child);Objects.requireNonNull(parentFrame);Objects.requireNonNull(childFrame);
      if(parent.equals(child))throw new IllegalArgumentException("Service frame must join distinct bodies");}
  }
  public static final class Car {
    public final UUID chassis;public final List<Hold> holds;public boolean dispatched;public String fault="";
    public Car(UUID chassis,List<Hold> holds){this.chassis=Objects.requireNonNull(chassis);this.holds=List.copyOf(holds);}
  }
  public final Map<UUID,Car> cars=new LinkedHashMap<>();
  public String dock="";
  public void validate(MovingAssemblyState state){
    if(cars.isEmpty())return;
    if(!state.docks.containsKey(dock)||!state.cars.containsAll(cars.keySet()))throw new IllegalArgumentException("Service dock/train identity is missing");
    for(var car:cars.values()){
      var owned=new HashSet<UUID>();owned.add(car.chassis);
      for(var hold:car.holds){if(!owned.contains(hold.parent)||!owned.add(hold.child))throw new IllegalArgumentException("Service child hierarchy is not rooted in its chassis");}
    }
  }
  public CompoundTag save(boolean disk){
    var t=new CompoundTag();t.putInt("version",1);t.putString("dock",dock);var list=new ListTag();
    for(var car:cars.values()){
      var n=new CompoundTag();n.putUUID("chassis",car.chassis);n.putBoolean("dispatched",!disk&&car.dispatched);n.putString("fault",car.fault);var holds=new ListTag();
      for(var h:car.holds){var a=new CompoundTag();a.putUUID("parent",h.parent);a.putUUID("child",h.child);a.put("a",h.parentFrame.save());a.put("b",h.childFrame.save());holds.add(a);}
      n.put("holds",holds);list.add(n);
    }t.put("cars",list);return t;
  }
  public static MovingServiceState read(CompoundTag tag){
    var s=new MovingServiceState();if(tag.isEmpty())return s;
    if(tag.getInt("version")!=1)throw new IllegalArgumentException("Unknown moving service state");s.dock=tag.getString("dock");
    var list=tag.getList("cars",Tag.TAG_COMPOUND);if(list.size()>4)throw new IllegalArgumentException("Too many service cars");
    var children=new HashSet<UUID>();
    for(var raw:list){var n=(CompoundTag)raw;var holds=new ArrayList<Hold>();
      for(var item:n.getList("holds",Tag.TAG_COMPOUND)){var a=(CompoundTag)item;var h=new Hold(a.getUUID("parent"),a.getUUID("child"),MovingAssemblyState.Pose.read(a.getCompound("a")),MovingAssemblyState.Pose.read(a.getCompound("b")));
        if(!children.add(h.child)||holds.size()>=8)throw new IllegalArgumentException("Invalid service child inventory");holds.add(h);}
      var c=new Car(n.getUUID("chassis"),holds);c.dispatched=n.getBoolean("dispatched");c.fault=n.getString("fault"); // Disk writes false; network snapshots retain live state.
      if(s.cars.putIfAbsent(c.chassis,c)!=null)throw new IllegalArgumentException("Duplicate service chassis");
    }return s;
  }
}
