package dev.fourthdimension.mixin;

import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.fourthdimension.integration.*;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pin the selected native repaint packet to the player's actual held item, permissions and reach. */
@Pseudo
@Mixin(targets="dev.notzyvex.coasters_extras.net.ModNetwork",remap=false)
public abstract class CtnbRepaintPermissionMixin {
  @Inject(method="lambda$register$4",at=@At("HEAD"),cancellable=true)
  private static void guard(IPayloadContext context,@Coerce Object payload,CallbackInfo ci){
    var id=(ResourceLocation)OptionalCalls.call(payload,"material");
    if(!id.getNamespace().equals(OptionalIntegrations.CTNB)||!CtnbTracks.MATERIALS.contains(id.getPath()))return;
    if(!(context.player() instanceof ServerPlayer p)){ci.cancel();return;}
    var l=p.serverLevel();var a=(BlockPos)OptionalCalls.call(payload,"anchor");var b=(BlockPos)OptionalCalls.call(payload,"peer");
    boolean held=false;
    for(var stack:java.util.List.of(p.getMainHandItem(),p.getOffhandItem()))try{
      var material=(TrackMaterial)OptionalCalls.call(stack.getItem(),"material");held|=material.id.toString().equals(id.toString());
    }catch(IllegalArgumentException ignored){}
    if(!held||!p.mayBuild()||!l.hasChunkAt(a)||!l.hasChunkAt(b)||!l.mayInteract(p,a)||!l.mayInteract(p,b)
        ||!(l.getBlockEntity(a) instanceof CoasterAnchorpointBlockEntity anchor)){ci.cancel();return;}
    var curve=anchor.getAnchorPeerCurvesView().get(b);if(curve==null){ci.cancel();return;}
    var edge=CoasterPathEdge.curve(a,b,curve);
    var hit=CoasterPathTrackFrame.closestHitOnEdge(l,edge,p.getEyePosition());
    if(hit==null||hit.point().distanceToSqr(p.getEyePosition())>36)ci.cancel();
  }
}
