package dev.fourthdimension.vehicle;
import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.track.*;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class MotorbikeContent {
  public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(FourthDimension.ID);
  public static final DeferredBlock<MotorbikeSeatBlock> SEAT=BLOCKS.register("motorbike_seat",()->new MotorbikeSeatBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final DeferredBlock<net.minecraft.world.level.block.Block> FRAME=BLOCKS.register("motorbike_frame",()->new MechanicalPartBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion(),net.minecraft.world.level.block.Block.box(0,14,5,16,18,11)));
  public static final DeferredItem<FamilyTrackItem> TRACK=ITEMS.register("motorbike_track",()->new FamilyTrackItem(new Item.Properties(),TrackDesign.MOTORBIKE));
  public static final DeferredItem<VehicleItem> CAR=ITEMS.register("motorbike_car",()->new VehicleItem(new Item.Properties().stacksTo(1),TrackDesign.MOTORBIKE));
  public static net.minecraft.world.phys.shapes.VoxelShape controllerShape(){
    return net.minecraft.world.phys.shapes.Shapes.or(net.minecraft.world.level.block.Block.box(-8,0,5,24,2,11),
        net.minecraft.world.level.block.Block.box(5,0,5,11,8,11),net.minecraft.world.level.block.Block.box(3,7,3,13,14,13));
  }
  private MotorbikeContent(){}
  public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);}
}
