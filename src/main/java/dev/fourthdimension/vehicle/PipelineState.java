package dev.fourthdimension.vehicle;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
/** Accepted native drive directions for this route; changing power cannot auto-authorize reversal. */
public final class PipelineState {
  public final Map<BlockPos,Integer> directions=new HashMap<>();
  public CompoundTag save(){
    var tag=new CompoundTag();tag.putInt("version",1);var list=new ListTag();
    directions.forEach((p,s)->{var item=new CompoundTag();item.putLong("anchor",p.asLong());item.putInt("sign",s);list.add(item);});
    tag.put("directions",list);return tag;
  }
  public static PipelineState load(CompoundTag tag){
    if(tag.getInt("version")!=1)throw new IllegalArgumentException("Unsupported Pipeline state version");
    var result=new PipelineState();
    for(var raw:tag.getList("directions",Tag.TAG_COMPOUND)){
      var item=(CompoundTag)raw;int sign=item.getInt("sign");
      if(sign!=1&&sign!=-1)throw new IllegalArgumentException("Invalid Pipeline native drive direction");
      if(result.directions.size()>=4096)throw new IllegalArgumentException("Pipeline route too large");
      result.directions.put(BlockPos.of(item.getLong("anchor")),sign);
    }
    return result;
  }
}
