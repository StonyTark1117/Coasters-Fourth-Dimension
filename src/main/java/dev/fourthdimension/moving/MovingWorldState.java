package dev.fourthdimension.moving;

import java.util.*;
import net.minecraft.nbt.*;

/** Dimension-owned assembly identities. Receiving reservations are rebuilt from live validation. */
public final class MovingWorldState {
  public final Map<UUID,MovingAssemblyState> assemblies = new LinkedHashMap<>();
  private final Map<UUID,UUID> receiving = new HashMap<>();

  public MovingAssemblyState retaining(UUID body) {
    if(body==null)return null;
    for(var s:assemblies.values())if(s.bodies.containsKey(body))return s;
    return null;
  }
  /** Call only after the runtime has validated the actual complete receiving span and occupants. */
  public void reserve(MovingAssemblyState assembly, Collection<UUID> railSegments) {
    if(assemblies.get(assembly.id)!=assembly||railSegments.isEmpty())
      throw new IllegalArgumentException("Registered assembly and receiving track required");
    for(var id:railSegments) {
      Objects.requireNonNull(id);var owner=receiving.get(id);
      if(owner!=null&&!owner.equals(assembly.id))throw new IllegalArgumentException("Exit is reserved by another moving assembly");
    }
    // Validate every segment before the first mutation; a denial cannot leave a partial claim.
    for(var id:railSegments)receiving.put(id,assembly.id);
  }
  public boolean owns(UUID assembly, Collection<UUID> railSegments) {
    return !railSegments.isEmpty()&&railSegments.stream().allMatch(id->assembly.equals(receiving.get(id)));
  }
  public Set<UUID> reservedBy(UUID assembly){var ids=new HashSet<UUID>();receiving.forEach((segment,owner)->{if(owner.equals(assembly))ids.add(segment);});return Set.copyOf(ids);}
  public UUID receivingOwner(UUID segment) { return receiving.get(segment); }
  public void releaseReservation(UUID assembly) { receiving.values().removeIf(assembly::equals); }
  public ListTag save(boolean disk) {
    var list=new ListTag();for(var s:assemblies.values())list.add(s.save(disk));return list;
  }
  public void read(ListTag list) {
    if(!assemblies.isEmpty())throw new IllegalStateException("Load moving state into an empty world state");
    if(list.size()>64)throw new IllegalArgumentException("Moving assembly capacity exceeded");
    var controllers=new HashSet<net.minecraft.core.BlockPos>();var bodies=new HashSet<UUID>();
    for(var raw:list) {
      var s=MovingAssemblyState.read((CompoundTag)raw);
      if(assemblies.putIfAbsent(s.id,s)!=null||!controllers.add(s.controller))
        throw new IllegalArgumentException("Duplicate moving assembly/controller");
      if(s.bed!=null&&!bodies.add(s.bed))throw new IllegalArgumentException("Duplicate moving bed");
      for(var id:s.bodies.keySet())if(!bodies.add(id))throw new IllegalArgumentException("A body cannot belong to two moving assemblies");
    }
    // Deliberately empty: persisted identities do not grant ownership of live receiving track.
    receiving.clear();
  }
}
