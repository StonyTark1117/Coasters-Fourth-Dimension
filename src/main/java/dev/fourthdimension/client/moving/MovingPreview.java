package dev.fourthdimension.client.moving;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
/** Wire ghosts come only from a validated server plan, and remain nearby while the player places the required socket anchors. */
@EventBusSubscriber(modid="fourthdimension",value=Dist.CLIENT)
public final class MovingPreview {
  private record Wire(AABB box,int color){}
  private static final List<Wire> WIRES=new ArrayList<>();private static BlockPos controller;private static CompoundTag current=new CompoundTag();private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
  public static void clear(){WIRES.clear();controller=null;dimension=null;current=new CompoundTag();}
  public static CompoundTag current(BlockPos owner){return owner.equals(controller)?current.copy():new CompoundTag();}
  public static void show(BlockPos owner,CompoundTag plan){clear();controller=owner;current=plan.copy();dimension=Minecraft.getInstance().level==null?null:Minecraft.getInstance().level.dimension();
    for(var raw:plan.getList("sweep",Tag.TAG_COMPOUND)){var b=(CompoundTag)raw;WIRES.add(new Wire(new AABB(b.getDouble("min_x"),b.getDouble("min_y"),b.getDouble("min_z"),b.getDouble("max_x"),b.getDouble("max_y"),b.getDouble("max_z")),plan.getBoolean("valid")?0x44ddec:0xff8855));}
    for(var raw:plan.getList("fixed",Tag.TAG_COMPOUND)){var p=BlockPos.of(((CompoundTag)raw).getLong("pos"));WIRES.add(new Wire(new AABB(p),0xffcc55));}
    for(var raw:plan.getCompound("assembly").getList("docks",Tag.TAG_COMPOUND)){var t=(CompoundTag)raw;WIRES.add(new Wire(new AABB(BlockPos.of(t.getLong("from"))).inflate(.08),0x66ff99));WIRES.add(new Wire(new AABB(BlockPos.of(t.getLong("to"))).inflate(.08),0x66ff99));}
  }
  @SubscribeEvent public static void render(RenderLevelStageEvent event){
    if(controller==null||event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.level.dimension()!=dimension||mc.player.distanceToSqr(Vec3.atCenterOf(controller))>96*96){clear();return;}
    var pose=event.getPoseStack();var camera=event.getCamera().getPosition();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);var buffers=mc.renderBuffers().bufferSource();var out=buffers.getBuffer(RenderType.lines());
    for(var wire:WIRES){var b=wire.box;Vec3[] points={new Vec3(b.minX,b.minY,b.minZ),new Vec3(b.maxX,b.minY,b.minZ),new Vec3(b.maxX,b.minY,b.maxZ),new Vec3(b.minX,b.minY,b.maxZ),new Vec3(b.minX,b.maxY,b.minZ),new Vec3(b.maxX,b.maxY,b.minZ),new Vec3(b.maxX,b.maxY,b.maxZ),new Vec3(b.minX,b.maxY,b.maxZ)};
      for(int[] pair:new int[][]{{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}}){var n=points[pair[1]].subtract(points[pair[0]]).normalize();for(int i:pair){var p=points[i];out.addVertex(pose.last().pose(),(float)p.x,(float)p.y,(float)p.z).setColor((wire.color>>16)&255,(wire.color>>8)&255,wire.color&255,140).setNormal(pose.last(),(float)n.x,(float)n.y,(float)n.z);}}
    }buffers.endBatch(RenderType.lines());pose.popPose();
  }
}
