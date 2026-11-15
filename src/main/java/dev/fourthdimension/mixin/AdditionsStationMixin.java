package dev.fourthdimension.mixin;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.integration.create.RideInformation;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets="dev.plushivr.coasterfins.block.StationBlockEntity",remap=false)
public abstract class AdditionsStationMixin extends SmartBlockEntity implements AdditionsStationAdapter.Bridge,IHaveGoggleInformation {
  @Unique private final AdditionsStationAdapter.State fourthdimension$state=new AdditionsStationAdapter.State();
  protected AdditionsStationMixin(BlockEntityType<?> type,BlockPos pos,BlockState state){super(type,pos,state);}
  public AdditionsStationAdapter.State fourthdimension$stationState(){return fourthdimension$state;}
  public boolean authorizesRide(UUID id){return AdditionsStationAdapter.authorizes(this,fourthdimension$state,id);}
  public RideTelemetry telemetry(){return fourthdimension$state.telemetry;}
  public boolean addToGoggleTooltip(List<Component> tooltip,boolean sneaking){return RideInformation.goggles(tooltip,telemetry());}
  @Inject(method="setOccupied(ZIZ)V",at=@At("HEAD"),cancellable=true,require=0)
  private void fourthdimension$sample(boolean occupied,int weight,boolean pre,CallbackInfo ci){
    var old=telemetry();
    if(AdditionsStationAdapter.update(this,fourthdimension$state,pre)) {
      if(!old.equals(telemetry()))sendData();
      ci.cancel();
    }
  }
  @Inject(method="setDispatch",at=@At("TAIL"),require=0)
  private void fourthdimension$configure(boolean enabled,int threshold,int delay,int cooldown,CallbackInfo ci){
    AdditionsStationAdapter.configure(fourthdimension$state,enabled);
  }
  @Inject(method="setSpan",at=@At("TAIL"),require=0)
  private void fourthdimension$spanChanged(List<?> span,CallbackInfo ci){
    fourthdimension$state.cars=Set.of();fourthdimension$state.sampled=Long.MIN_VALUE;
    AdditionsStationAdapter.configure(fourthdimension$state,false);
  }
  @Inject(method="clearSpan",at=@At("TAIL"),require=0)
  private void fourthdimension$spanCleared(CallbackInfo ci){
    fourthdimension$state.cars=Set.of();fourthdimension$state.sampled=Long.MIN_VALUE;
    AdditionsStationAdapter.configure(fourthdimension$state,false);
  }
  @Inject(method="write",at=@At("TAIL"),require=0)
  private void fourthdimension$write(CompoundTag tag,HolderLookup.Provider regs,boolean packet,CallbackInfo ci){
    tag.put("fourthdimension_station",telemetry().save());
    tag.putBoolean("fourthdimension_managed",fourthdimension$state.managed);
    if(fourthdimension$state.bound!=null)tag.putUUID("fourthdimension_bound",fourthdimension$state.bound);
  }
  @Inject(method="read",at=@At("TAIL"),require=0)
  private void fourthdimension$read(CompoundTag tag,HolderLookup.Provider regs,boolean packet,CallbackInfo ci){
    if(tag.contains("fourthdimension_station"))fourthdimension$state.telemetry=RideTelemetry.load(tag.getCompound("fourthdimension_station"));
    if(!packet){
      fourthdimension$state.managed=tag.getBoolean("fourthdimension_managed");
      fourthdimension$state.bound=tag.hasUUID("fourthdimension_bound")?tag.getUUID("fourthdimension_bound"):null;
      fourthdimension$state.cars=Set.of();fourthdimension$state.sampled=Long.MIN_VALUE;
      fourthdimension$state.armed=false;fourthdimension$state.dwell=0;
    }
  }
}
