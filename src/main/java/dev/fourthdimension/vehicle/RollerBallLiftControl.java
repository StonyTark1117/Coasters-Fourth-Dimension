package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** A finite, normal held control authors lift/release/return sections on empty track. */
public final class RollerBallLiftControl extends Item {
  public RollerBallLiftControl(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext ctx){
    if(ctx.getLevel().isClientSide)return InteractionResult.SUCCESS;
    var player=ctx.getPlayer();if(player==null)return InteractionResult.PASS;
    var l=(ServerLevel)ctx.getLevel();
    try{
      if(!player.mayBuild()||!l.mayInteract(player,ctx.getClickedPos())||player.distanceToSqr(ctx.getClickLocation())>64)throw new IllegalArgumentException("Section is out of reach");
      var hit=CoasterPathTrackFrame.nearestGraphHit(l,CoasterPathGraphManager.get(l),ctx.getClickLocation(),4);
      var data=FoundationData.get(l);var rail=hit==null?null:data.rails.stream().filter(r->r.matches(hit.edge().from(),hit.edge().to())).findFirst().orElse(null);
      if(rail==null||!RollerBallVehicle.VARIANT.equals(rail.variant))throw new IllegalArgumentException("Click a support within two blocks of Roller Ball track");
      if(!rail.valid||rail.geometryHash!=TrackGeometry.fingerprint(l,hit.edge()))throw new IllegalArgumentException("Rebind changed geometry before setting lift section");
      if(TrackPlacement.occupied(data,rail))throw new IllegalArgumentException("Remove the car before editing its section");
      var modes=RollerBallState.Section.values();rail.rollerSection=modes[(rail.rollerSection.ordinal()+(player.isShiftKeyDown()?modes.length-1:1))%modes.length];rail.revision++;
      data.setDirty();FoundationSync.broadcast(l);player.displayClientMessage(Component.literal("Roller Ball section: "+rail.rollerSection),true);return InteractionResult.SUCCESS;
    }catch(IllegalArgumentException e){player.displayClientMessage(Component.literal(e.getMessage()),true);return InteractionResult.FAIL;}
  }
}
