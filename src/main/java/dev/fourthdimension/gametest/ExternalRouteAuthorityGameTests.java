package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.ExampleLayouts;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

/** Actual bounded Arrow circuit, adapted from Fd12MechanismsNativeGameTests. */
@GameTestHolder("fourthdimension_ls006")
@PrefixGameTestTemplate(false)
public final class ExternalRouteAuthorityGameTests {
  @GameTest(template="empty",timeoutTicks=160)
  public static void delegatedRouteRetainsOneOwnerAndFailsClosed(GameTestHelper h) {
    var level=h.getLevel();var origin=new BlockPos(0,100,512);
    for(int x=-1;x<=3;x++)for(int z=31;z<=37;z++)level.setChunkForced(x,z,true);
    ExampleLayouts.createBareArrow(level,origin);
    var v=ExampleLayouts.addBareArrowCar(level,origin);
    var pos=origin.offset(6,0,20);level.setBlockAndUpdate(pos,Blocks.BARREL.defaultBlockState());
    var owner=UUID.randomUUID();var direction=new int[]{1};var token=new RideAuthority.Token[1];
    String allowed="launchsystems_fixture:route", legacy="launchsystems_fixture:route_legacy";
    class Adapter implements RideAuthority.ExternalProvider {
      private final boolean optIn;
      Adapter(boolean optIn){this.optIn=optIn;}
      public boolean supports(ServerLevel l,FoundationData.Vehicle vehicle){return vehicle.chassis.equals(v.chassis);}
      public boolean supportsManagedRoutes(){return optIn;}
      public int routeDirection(ServerLevel l,BlockPos p,FoundationData.Vehicle vehicle){return direction[0];}
      public RideAuthority.Controller controller(ServerLevel l,BlockPos p){
        if(!pos.equals(p)||!l.getBlockState(p).is(Blocks.BARREL))return null;
        return new RideAuthority.Controller(){
          public UUID authorityId(){return owner;}
          public boolean authorizesRide(UUID id){return v.chassis.equals(id);}
          public boolean authorityAvailable(UUID id){return true;}
          public void validateRelease(ServerLevel l,FoundationData.Vehicle vehicle){}
        };
      }
    }
    RideAuthority.registerExternal(allowed,1,new Adapter(true));
    RideAuthority.registerExternal(legacy,1,new Adapter(false));
    h.runAtTickTime(60,()->{
      h.assertTrue(level.hasChunkAt(pos)&&level.getBlockState(pos).is(Blocks.BARREL),"Fixture controller chunk/barrel disappeared before admission");
      System.out.println("LS006_STAGE tick=60 controllerLoaded="+level.hasChunkAt(pos)+" block="+level.getBlockState(pos));
      denied(h,()->RideAuthority.requestRoute(level,pos,v.chassis,legacy),"provider without route opt-in");
      denied(h,()->RideAuthority.claim(level,pos,v.chassis,legacy),"legacy managed-route claim");
      h.assertTrue(v.authority.owner==null,"Denied legacy provider acquired owner");
      var grant=RideAuthority.requestRoute(level,pos,v.chassis,allowed);
      h.assertTrue(grant.managed()&&grant.permitted()&&grant.owner().equals(v.chassis),"Real Arrow route not granted: "+grant);
      token[0]=RideAuthority.claim(level,pos,v.chassis,allowed);
      VehicleOperations.restraints(level,v,true);
      h.assertTrue(!RideAuthority.dispatchReady(level,v.chassis,token[0]),"Fresh claim advertised dispatch before five stopped ticks");
      h.assertTrue(v.authority.owner.equals(owner)&&RideAuthority.holds(level,v),"Admission changed owner or dispatched");
    });
    h.runAtTickTime(70,()->{
      System.out.println("LS006_STAGE tick=70 dispatch status="+RideAuthority.routeStatus(level,v.chassis));
      h.assertTrue(RideAuthority.dispatchReady(level,v.chassis,token[0]),"Settled native train did not advertise dispatch readiness");
      RideAuthority.dispatch(level,pos,v.chassis,token[0]);token[0]=RideAuthority.token(v);
      h.assertTrue(RideAuthority.permits(level,v.chassis,token[0],allowed),"Live delegated route did not permit drive");
      token[0]=RideAuthority.capture(level,pos,v.chassis,token[0]);
      h.assertTrue(RideAuthority.holds(level,v)&&!RideAuthority.routeStatus(level,v.chassis).permitted(),"Capture did not retain both holds");
      RideAuthority.resumeRoute(level,pos,v.chassis,token[0]);
      token[0]=RideAuthority.resume(level,pos,v.chassis,token[0]);
      h.assertTrue(RideAuthority.holds(level,v)&&RideAuthority.routeStatus(level,v.chassis).permitted(),"Explicit recovery failed or dispatched");
    });
    h.runAtTickTime(85,()->{
      System.out.println("LS006_STAGE tick=85 reservation mutations");
      var state=FoundationData.get(level).rideLocal;var train=state.trains.get(v.chassis);
      var reservations=new HashMap<>(state.reservations);var other=UUID.randomUUID();
      state.reservations.put(train.nextBlock,other);
      h.assertTrue(!RideAuthority.routeStatus(level,v.chassis).permitted(),"Changed next-block owner accepted");
      h.assertTrue(RideAuthority.holds(level,v)&&v.authority.owner.equals(owner),"Changed reservation released longitudinal owner");
      denied(h,()->RideAuthority.dispatch(level,pos,v.chassis,RideAuthority.token(v)),"conflicting dispatch");
      h.assertTrue(state.blockOwner(train.nextBlock).equals(other),"Denied dispatch stole next reservation");
      state.reservations.clear();state.reservations.putAll(reservations);
      RideAuthority.resumeRoute(level,pos,v.chassis,RideAuthority.token(v));
      token[0]=RideAuthority.resume(level,pos,v.chassis,RideAuthority.token(v));
      int block=train.block,next=train.nextBlock;
      train.block=2;train.nextBlock=3;
      state.reservations.clear();state.reserve(2,v.chassis);state.reserve(3,v.chassis);
      h.assertTrue(!RideAuthority.routeStatus(level,v.chassis).permitted(),"Coherently replaced stored pair bypassed observed rail identity");
      h.assertTrue(RideAuthority.holds(level,v)&&train.block==2&&train.nextBlock==3,"Pair denial silently rewrote route ownership");
      train.block=block;train.nextBlock=next;state.reservations.clear();state.reservations.putAll(reservations);
      RideAuthority.resumeRoute(level,pos,v.chassis,RideAuthority.token(v));
      token[0]=RideAuthority.resume(level,pos,v.chassis,RideAuthority.token(v));
      var wrong=new FoundationData.RideLocalTrain(v.chassis,List.of(UUID.randomUUID()),block);
      wrong.nextBlock=next;wrong.braking=true;state.trains.put(v.chassis,wrong);
      denied(h,()->RideAuthority.resumeRoute(level,pos,v.chassis,RideAuthority.token(v)),"changed admitted consist");
      h.assertTrue(state.reservations.equals(reservations)&&wrong.braking,"Rejected resume mutated admitted reservation/hold");
      state.trains.put(v.chassis,train);
      RideAuthority.resumeRoute(level,pos,v.chassis,RideAuthority.token(v));
      token[0]=RideAuthority.resume(level,pos,v.chassis,RideAuthority.token(v));
      var savedOwner=v.authority.owner;v.authority.owner=UUID.randomUUID();
      denied(h,()->RideAuthority.resumeRoute(level,pos,v.chassis,RideAuthority.token(v)),"replaced authority owner");
      h.assertTrue(state.reservations.equals(reservations),"Wrong authority owner mutated route reservations");
      v.authority.owner=savedOwner;
      RideAuthority.resumeRoute(level,pos,v.chassis,RideAuthority.token(v));
      token[0]=RideAuthority.resume(level,pos,v.chassis,RideAuthority.token(v));
      direction[0]=-1;
      h.assertTrue(!RideAuthority.routeStatus(level,v.chassis).permitted(),"Opposing route direction accepted");
      denied(h,()->RideAuthority.resumeRoute(level,pos,v.chassis,RideAuthority.token(v)),"opposing resume");
      h.assertTrue(RideAuthority.holds(level,v)&&state.reservations.equals(reservations),"Opposing denial lost existing reservation");
      direction[0]=1;
      RideAuthority.resumeRoute(level,pos,v.chassis,RideAuthority.token(v));
      token[0]=RideAuthority.resume(level,pos,v.chassis,RideAuthority.token(v));
    });
    h.runAtTickTime(100,()->{
      System.out.println("LS006_STAGE tick=100 unloaded body");
      var state=FoundationData.get(level).rideLocal;var reservations=new HashMap<>(state.reservations);
      var container=SubLevelContainer.getContainer(level);var body=container.getSubLevel(v.carriers.getFirst().body);
      container.removeSubLevel(body,SubLevelRemovalReason.UNLOADED);
      h.assertTrue(!RideAuthority.routeStatus(level,v.chassis).permitted(),"Unloaded articulated body retained permission");
      h.assertTrue(RideAuthority.holds(level,v)&&v.authority.owner.equals(owner)&&state.reservations.equals(reservations),"Unloaded-body denial lost hold/identity/reservations");
      System.out.println("LS006_ROUTE PASS actualArrowCircuit=true stoppedReadiness=true explicitOptIn=true oneOwner=true liveGrant=true captureResume=true changedReservationDenied=true coherentPairDenied=true savedConsistDenied=true replacedOwnerDenied=true opposingDenied=true unloadedBodyHeld=true freshRequestRequired=true");h.succeed();
    });
  }
  private static void denied(GameTestHelper h,Runnable action,String name){try{action.run();}catch(IllegalArgumentException expected){return;}h.fail("Accepted "+name);}
}
