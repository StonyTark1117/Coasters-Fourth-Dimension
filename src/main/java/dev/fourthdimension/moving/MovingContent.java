package dev.fourthdimension.moving;

import dev.fourthdimension.FourthDimension;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Original finite machinery catalog. Functional eligibility comes from MovingCapabilityCatalog. */
public final class MovingContent {
  public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(FourthDimension.ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,FourthDimension.ID);
  public static final Map<MovingMechanism,DeferredBlock<MovingControllerBlock>> CONTROLLERS=new EnumMap<>(MovingMechanism.class);
  public static final Map<MovingPartBlock.Kind,DeferredBlock<MovingPartBlock>> PARTS=new EnumMap<>(MovingPartBlock.Kind.class);
  public static final Map<MovingCapabilityCatalog.Configuration,DeferredItem<MovingInsertItem>> INSERTS=new EnumMap<>(MovingCapabilityCatalog.Configuration.class);
  public static final List<DeferredItem<? extends Item>> ALL_ITEMS=new ArrayList<>();
  static {
    for(var configuration:MovingCapabilityCatalog.Configuration.values()){
      var item=ITEMS.register("moving_insert_"+configuration.id(),()->new MovingInsertItem(new Item.Properties(),configuration));
      INSERTS.put(configuration,item);ALL_ITEMS.add(item);
    }
    for(var mechanism:MovingMechanism.values()){
      String id="moving_"+mechanism.id();
      var block=BLOCKS.register(id,()->new MovingControllerBlock(properties(),mechanism));CONTROLLERS.put(mechanism,block);
      ALL_ITEMS.add(ITEMS.register(id,()->new BlockItem(block.get(),new Item.Properties()){
        @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag){
          super.appendHoverText(stack,context,lines,flag);
          lines.add(Component.translatable("moving.fourthdimension.power"));
          lines.add(Component.translatable("moving.fourthdimension.compatibility_hint"));
        }
      }));
    }
    for(var part:MovingPartBlock.Kind.values()){
      String id="moving_"+part.name().toLowerCase(Locale.ROOT);
      var block=BLOCKS.register(id,()->new MovingPartBlock(properties(),part));PARTS.put(part,block);
      ALL_ITEMS.add(ITEMS.register(id,()->new BlockItem(block.get(),new Item.Properties()){
        @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag){
          super.appendHoverText(stack,context,lines,flag);lines.add(Component.translatable("moving.fourthdimension.part"));
        }
      }));
    }
  }
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<MovingControllerEntity>> CONTROLLER_ENTITY=
      ENTITIES.register("moving_controller",()->BlockEntityType.Builder.of(MovingControllerEntity::new,
          CONTROLLERS.values().stream().map(DeferredBlock::get).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
  private MovingContent(){}
  private static BlockBehaviour.Properties properties(){return BlockBehaviour.Properties.of().strength(4,12).noOcclusion().pushReaction(PushReaction.BLOCK);}
  public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);}
}
