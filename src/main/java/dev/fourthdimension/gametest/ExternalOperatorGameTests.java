package dev.fourthdimension.gametest;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.tools.OperatorPanel;
import dev.fourthdimension.vehicle.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** Adapted from ToolsAuthorityGameTests and ExternalRideAuthorityGameTests on the selected API1 base. */
@GameTestHolder("fourthdimension_ls602")
@PrefixGameTestTemplate(false)
public final class ExternalOperatorGameTests {
  @GameTest(template="empty",timeoutTicks=100)
  public static void externalPanelSharesStatusAndFreshRequests(GameTestHelper h) {
    var l=h.getLevel(); var from=new BlockPos(2608,100,400); var to=from.offset(0,0,80);
    for(int x=162;x<=164;x++)for(int z=24;z<=31;z++)l.setChunkForced(x,z,true);
    for(var pos:List.of(from,to))l.setBlockAndUpdate(pos,dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=WingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var position=from.offset(6,0,20); var owner=UUID.randomUUID(); var cars=new FoundationData.Vehicle[1];
    var revision=new long[]{42}; var requests=new int[1]; var charged=new boolean[1];
    String provider="launchsystems_fixture:operator";
    RideAuthority.registerExternal(provider,1,new RideAuthority.ExternalProvider(){
      public boolean supports(ServerLevel level,FoundationData.Vehicle v){return true;}
      public RideAuthority.Controller controller(ServerLevel level,BlockPos pos){
        if(!position.equals(pos)||!level.getBlockState(pos).is(Blocks.BARREL))return null;
        return new RideAuthority.Controller(){
          public UUID authorityId(){return owner;}
          public boolean authorizesRide(UUID id){return cars[0]!=null&&cars[0].chassis.equals(id);}
          public boolean authorityAvailable(UUID id){return true;}
          public void validateRelease(ServerLevel level,FoundationData.Vehicle v){}
        };
      }
      public CompoundTag operatorStatus(ServerLevel level,FoundationData.Vehicle v){
        var tag=new CompoundTag();tag.putUUID("owner",owner);tag.putString("phase","READY");
        tag.putString("charge",charged[0]?"100 / 100 work":"0 / 100 work");
        tag.putString("blocked_reason",charged[0]?"":"Launch energy reserve is charging");
        tag.putString("request","Fresh request required");tag.putLong("request_revision",revision[0]);
        tag.putBoolean("dispatch_ready",charged[0]);return tag;
      }
      public void operatorAction(ServerLevel level,FoundationData.Vehicle v,int action,long expected){
        if(expected!=revision[0])throw new IllegalArgumentException("Stale external request");
        if(action==13&&!charged[0])throw new IllegalArgumentException("Launch energy reserve is charging");
        if(action!=10&&action!=13&&action!=61)throw new IllegalArgumentException("Unsupported action");
        requests[0]++;revision[0]++;
      }
    });
    h.runAtTickTime(10,()->{cars[0]=WingVehicle.create(l,WingExamples.sample(l,edge,20));l.setBlockAndUpdate(position,Blocks.BARREL.defaultBlockState());});
    h.runAtTickTime(60,()->{
      var v=cars[0]; var p=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"LS602Panel"));
      p.setGameMode(GameType.CREATIVE);p.setPos(position.getX(),position.getY(),position.getZ());
      RideAuthority.claim(l,position,v.chassis,provider);VehicleOperations.restraints(l,v,true);
      var low=OperatorPanel.sample(p,v.chassis);
      h.assertTrue(!low.getBoolean("dispatch")&&low.getString("denial").contains("charging"),"Panel lost external charge denial");
      h.assertTrue(low.getCompound("operator").getUUID("owner").equals(owner)&&low.getString("controller").contains(owner.toString()),"Panel reports different owner");
      h.assertTrue(low.getString("power").equals("0 / 100 work"),"Panel reports different charge");
      denied(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,13,"41")),"stale request");
      denied(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,13,"42")),"uncharged request");
      charged[0]=true;
      var ready=OperatorPanel.sample(p,v.chassis);
      h.assertTrue(ready.getBoolean("dispatch")&&!ready.getString("denial").contains("Restore"),"Registered adapter not ready: "+ready.getString("denial"));
      EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,13,"42"));
      h.assertTrue(requests[0]==1&&v.authority.held,"Panel bypassed queue and released native hold");
      denied(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,13,"42")),"replayed request");
      EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,10,"43"));
      EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,61,"44"));
      h.assertTrue(requests[0]==3&&v.authority.held,"Explicit service replayed dispatch");
      p.setPos(position.getX()+30,position.getY(),position.getZ());
      denied(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,13,"45")),"distant request");
      p.setPos(position.getX(),position.getY(),position.getZ());l.setBlockAndUpdate(position,Blocks.AIR.defaultBlockState());
      h.assertTrue(!OperatorPanel.sample(p,v.chassis).getBoolean("dispatch"),"Missing external controller is ready");
      denied(h,()->EditorNetwork.apply(p,new EditorNetwork.Edit(v.chassis,v.revision,13,"45")),"missing controller");
      System.out.println("LS602_OPERATOR PASS statusParity=true chargeGuard=true registeredAdapter=true staleReplayDenied=true queueOnly=true explicitService=true rangeGuard=true missingController=true");h.succeed();
    });
  }
  private static void denied(GameTestHelper h,Runnable action,String name){try{action.run();}catch(IllegalArgumentException expected){return;}h.fail("Accepted "+name);}
}
