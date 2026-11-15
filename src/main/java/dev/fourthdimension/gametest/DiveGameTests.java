package dev.fourthdimension.gametest;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;
@GameTestHolder("fourthdimension_fd1702")
@PrefixGameTestTemplate(false)
public final class DiveGameTests {
  @GameTest(template="empty",timeoutTicks=5000)
  public static void diveHoldReleaseContract(GameTestHelper h){
    var l=h.getLevel();for(int x=-1;x<=7;x++)for(int z=-3;z<=25;z++)l.setChunkForced(x,z,true);
    var example=DiveExamples.train(l,new BlockPos(0,80,0));var v=example.vehicle();var step=new int[]{0};var time=new long[]{0};var dwell=new int[]{0};
    h.onEachTick(()->{
      var tick=l.getGameTime();var data=FoundationData.get(l);var container=SubLevelContainer.getContainer(l);
      var train=VehicleOperations.consist(l,v);
      if(tick%100==0)System.out.println("FD_DIVE_NATIVE phase="+v.dive.phase+" step="+step[0]+" cars="+train.size()+" fault="+v.fault+" center="+dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis)));
      if(step[0]==0&&train.size()==3&&train.stream().allMatch(c->c.boardingReady)){
        for(var car:train){DiveVehicle.validate(car);h.assertTrue(car.carriers.getFirst().seats.size()==6,"Six seats required");}
        VehicleOperations.restraints(l,v,true);VehicleOperations.dispatch(l,v);step[0]=1;
      }else if(step[0]==1&&v.dive.phase==DiveState.Phase.HELD&&v.dive.remaining<=45){
        var c=(DiveHoldEntity)l.getBlockEntity(v.dive.controller);c.validateRelease(l,v);
        v.restraints=false;reject(h,()->c.validateRelease(l,v),"open restraints");v.restraints=true;
        var exit=data.rails.stream().filter(r->!r.matches(c.from,c.to)&&(r.from.equals(c.to)||r.to.equals(c.to))).findFirst().orElseThrow();
        exit.valid=false;reject(h,()->c.validateRelease(l,v),"invalid exit");exit.valid=true;
        var support=DiveRuntime.rail(l,v);support.valid=false;reject(h,()->c.validateRelease(l,v),"invalid hold support");support.valid=true;
        l.setBlockAndUpdate(c.getBlockPos().below(),Blocks.AIR.defaultBlockState());dwell[0]=v.dive.remaining;time[0]=tick;step[0]=2;
      }else if(step[0]==2&&tick-time[0]>=50){
        h.assertTrue(v.dive.remaining==dwell[0]&&v.dive.needsResume,"Lost power advanced dwell/release");
        for(var car:train){var body=(ServerSubLevel)container.getSubLevel(car.chassis);h.assertTrue(container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()<.05,"Whole train hold moved");}
        var copy=FoundationData.load(data.snapshot(new CompoundTag(),l.registryAccess()),l.registryAccess());
        for(var car:train){var saved=copy.vehicles.get(car.chassis);h.assertTrue(saved.dive.phase==DiveState.Phase.HELD&&saved.dive.remaining==dwell[0],"Held persistence changed phase/timer");car.dive=saved.dive;car.authority=saved.authority;}
        l.setBlockAndUpdate(v.dive.controller.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());time[0]=tick;step[0]=3;
      }else if(step[0]==3&&tick-time[0]>=10){
        h.assertTrue(v.dive.remaining==dwell[0]&&v.dive.needsResume,"Restored power auto-released");
        DiveRuntime.resumeToDock(l,v);time[0]=tick;step[0]=4;
      }else if(step[0]==4&&v.dive.phase==DiveState.Phase.DROP){
        h.assertTrue(v.dive.remaining==0&&v.dive.releaseEpoch>0,"Missing release commit");
        h.assertTrue(tick-time[0]>=dwell[0],"Dwell expired before simulation tick budget");
        h.assertTrue(DiveRuntime.blocksChain(l,v),"Chain powered released hold");time[0]=tick;step[0]=5;
      }else if(step[0]==5&&tick-time[0]>=15){
        var body=(ServerSubLevel)container.getSubLevel(v.chassis);var velocity=container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d());
        h.assertTrue(velocity.length()>.1&&velocity.y<0,"Gravity did not start drop");
        var saved=FoundationData.load(data.snapshot(new CompoundTag(),l.registryAccess()),l.registryAccess()).vehicles.get(v.chassis);
        h.assertTrue(saved.dive.phase==DiveState.Phase.DROP&&saved.dive.releaseEpoch==v.dive.releaseEpoch,"Released commit did not persist");
        System.out.println("FD_DIVE_NATIVE PASS threeCars8 sixSeats18 physicalHold powerFreeze explicitResume closedExitGuards heldAndDropPersistence gravityRelease velocity="+velocity);
        h.succeed();
      }
      if(step[0]==1&&!v.fault.isEmpty())throw new GameTestAssertException("Dive route fault: "+v.fault);
    });
  }
  private static void reject(GameTestHelper h,Runnable r,String label){boolean rejected=false;try{r.run();}catch(IllegalArgumentException expected){rejected=true;}h.assertTrue(rejected,"Failed denial: "+label);}
}
