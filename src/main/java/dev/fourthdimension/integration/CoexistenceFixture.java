package dev.fourthdimension.integration;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.trains.track.*;
import dev.fourthdimension.vehicle.*;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Validation-only disclosed native slide, utility and core-ride setup; never player-build evidence. */
@EventBusSubscriber(modid="fourthdimension")
public final class CoexistenceFixture {
  public record Fixture(WingExamples.Example core,CoasterPathEdge slide,BlockPos bearing,BlockPos lock){}
  private CoexistenceFixture(){}
  public static Block block(String id){return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));}
  public static TrackMaterial slideMaterial(){
    try{return (TrackMaterial)Class.forName("net.omori_sunny.create_waterparked.content.waterslide.WaterslideTrackMaterials").getField("WATERSLIDE").get(null);}
    catch(ReflectiveOperationException failure){throw new IllegalArgumentException("Selected Waterparked is required",failure);}
  }
  public static Fixture create(ServerLevel l,BlockPos o){
    if(block("createcoasters:lock_block")==Blocks.AIR)throw new IllegalArgumentException("Selected Create Coasters is required");
    var material=slideMaterial();
    for(int x=(o.getX()-32)>>4;x<=(o.getX()+150)>>4;x++)for(int z=(o.getZ()-16)>>4;z<=(o.getZ()+128)>>4;z++)l.setChunkForced(x,z,true);
    var core=WingExamples.create(l,o,true);
    var a=o.offset(130,6,24);var b=o.offset(130,0,88);
    for(var p:List.of(a,b))l.setBlockAndUpdate(p,block("create_waterparked:waterslide_anchor").defaultBlockState());
    var direction=b.getCenter().subtract(a.getCenter()).normalize();
    var curve=new BezierConnection(Couple.create(a,b),Couple.create(CoasterAnchorpointBlockEntity.worldCenterBase(l,a),CoasterAnchorpointBlockEntity.worldCenterBase(l,b)),Couple.create(direction,direction.scale(-1)),Couple.create(new Vec3(0,1,0),new Vec3(0,1,0)),true,false,material);
    ((CoasterAnchorpointBlockEntity)l.getBlockEntity(a)).putAnchorPeerCurve(l,b,curve);
    for(var p:List.of(a,b)){
      var anchor=l.getBlockEntity(p);OptionalCalls.call(anchor,"refillWater");OptionalCalls.call(anchor,"setWaterActive",true);
      OptionalCalls.call(anchor,"setCurveWatered",p.equals(a)?b:a,true);
    }
    for(var p:BlockPos.betweenClosed(a.offset(-6,-1,-5),a.offset(-3,-1,5)))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
    var bearing=o.offset(-15,0,42);var lock=bearing.offset(-2,0,0);
    l.setBlockAndUpdate(lock,block("createcoasters:lock_block").defaultBlockState());
    l.setBlockAndUpdate(bearing,AllBlocks.MECHANICAL_BEARING.getDefaultState().setValue(BlockStateProperties.FACING,Direction.UP));
    l.setBlockAndUpdate(bearing.above(),AllBlocks.SEATS.get(net.minecraft.world.item.DyeColor.WHITE).getDefaultState());
    l.setBlockAndUpdate(bearing.below(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.UP));
    ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity)l.getBlockEntity(bearing.below())).generatedSpeed.setValue(16);
    var range=com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour.get(l,lock,com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour.TYPE);
    if(range!=null)range.setValue(3);
    ((MechanicalBearingBlockEntity)l.getBlockEntity(bearing)).assemble();
    for(var p:BlockPos.betweenClosed(bearing.offset(-4,-1,-4),bearing.offset(-2,-1,4)))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
    l.setBlockAndUpdate(lock.west(),Blocks.LEVER.defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE,net.minecraft.world.level.block.state.properties.AttachFace.FLOOR));
    l.setBlockAndUpdate(lock.west().below(),Blocks.SMOOTH_STONE.defaultBlockState());
    return new Fixture(core,CoasterPathEdge.curve(a,b,curve),bearing,lock);
  }
  @SubscribeEvent public static void register(RegisterCommandsEvent e){
    if(!Boolean.getBoolean("fourthdimension.validation"))return;
    e.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2)).then(Commands.literal("validation").then(Commands.literal("coexistence").executes(c->{
      create(c.getSource().getLevel(),new BlockPos(0,100,0));c.getSource().sendSuccess(()->Component.literal("Coexistence fixture: Wing station(-6,100,42), Create lock/bearing(-15,100,42), Waterparked slide(130,106,24) to(130,100,88). Command-authored geometry; normal controls remain manual."),false);return 1;
    }))));
  }
}
