package dev.fourthdimension.vehicle;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class YawMarkerEntity extends BlockEntity {
  public UUID id=UUID.randomUUID(),owner;
  public YawMarkerEntity(BlockPos p,BlockState s){super(YawMarkers.ENTITY.get(),p,s);}
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putUUID("marker",id);if(owner!=null)t.putUUID("owner",owner);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);id=t.hasUUID("marker")?t.getUUID("marker"):UUID.randomUUID();owner=t.hasUUID("owner")?t.getUUID("owner"):null;}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
  @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
}
