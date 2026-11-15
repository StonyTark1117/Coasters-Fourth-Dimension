package dev.fourthdimension.integration;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.CarrierSeatBlock;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Independent fallback record survives removal of the optional block entity's mod. */
@EventBusSubscriber(modid="fourthdimension")
public final class RotatingSeatData extends SavedData {
  public record Key(UUID body,BlockPos offset) {}
  public final Map<Key,BlockState> originals=new LinkedHashMap<>();
  public static RotatingSeatData get(ServerLevel level) {
    return level.getDataStorage().computeIfAbsent(new Factory<>(RotatingSeatData::new,RotatingSeatData::load),"fourthdimension_rotating_seats");
  }
  public static RotatingSeatData load(CompoundTag root,HolderLookup.Provider registries) {
    var data=new RotatingSeatData();
    for(var entry:root.getList("seats",Tag.TAG_COMPOUND)) {
      var tag=(CompoundTag)entry;
      if(!tag.hasUUID("body"))continue;
      var state=NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),tag.getCompound("original"));
      if(state.getBlock().getClass()!=CarrierSeatBlock.class)continue;
      data.originals.put(new Key(tag.getUUID("body"),BlockPos.of(tag.getLong("offset"))),state);
    }
    return data;
  }
  @Override public CompoundTag save(CompoundTag root,HolderLookup.Provider registries) {
    var seats=new ListTag();
    originals.forEach((key,state)->{
      var tag=new CompoundTag();tag.putUUID("body",key.body());tag.putLong("offset",key.offset().asLong());
      tag.put("original",NbtUtils.writeBlockState(state));seats.add(tag);
    });
    root.put("seats",seats);return root;
  }
  @SubscribeEvent public static void fallback(LevelTickEvent.Post event) {
    if(!(event.getLevel() instanceof ServerLevel level))return;
    var data=get(level);if(data.originals.isEmpty())return;
    var container=SubLevelContainer.getContainer(level);if(container==null)return;
    var foundation=FoundationData.get(level);
    for(var iterator=data.originals.entrySet().iterator();iterator.hasNext();) {
      var entry=iterator.next();var key=entry.getKey();
      var owner=foundation.vehicles.values().stream().filter(v->v.carriers.stream().anyMatch(c->c.body.equals(key.body()))).findFirst().orElse(null);
      if(owner==null){iterator.remove();data.setDirty();continue;}
      var body=container.getSubLevel(key.body());if(body==null||body.isRemoved())continue;
      var pos=body.getPlot().getCenterBlock().offset(key.offset());
      var current=level.getBlockState(pos);
      if(RotatingSeatIntegration.enabled()&&RotatingSeatIntegration.optional(current))continue;
      // Never overwrite a different player-placed block. A missing addon resolves its block to air.
      if(current.isAir()||RotatingSeatIntegration.optional(current)) {
        level.setBlock(pos,entry.getValue().setValue(CarrierSeatBlock.RESTRAINED,owner.restraints),3);
        com.mojang.logging.LogUtils.getLogger().info("FD_ROTATING_SEAT fallback body={} offset={}",key.body(),key.offset());
      }
      iterator.remove();data.setDirty();
    }
  }
}
