package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.*;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd2001_native")
@PrefixGameTestTemplate(false)
public final class MovingPersistenceGameTests {
  private static MovingAssemblyState state() {
    var s=new MovingAssemblyState(UUID.randomUUID(),BlockPos.ZERO,"transfer","ordinary_n_v1",16);
    s.bed=UUID.randomUUID();var car=UUID.randomUUID();s.cars=List.of(car);
    s.bodies.put(car,new MovingAssemblyState.Pose(0,1,0,0,0,0,1));
    s.docks.put("A",new MovingAssemblyState.Dock("A",UUID.randomUUID(),new BlockPos(1,2,3),new BlockPos(17,2,3),MovingAssemblyState.Pose.IDENTITY));
    s.source="A";s.destination="A";s.phase=MovingAssemblyState.Phase.MOVING;
    s.actualPose=new MovingAssemblyState.Pose(1.25,2,3,0,0,0,1);s.programSeconds=3.25;return s;
  }
  private static void restartPreservesActualPoseAndBodiesButRevokesReleaseAndReservations() {
    var s=state();var w=new MovingWorldState();w.assemblies.put(s.id,s);
    var track=UUID.randomUUID();w.reserve(s,List.of(track));var old=s.token();
    var restored=new MovingWorldState();restored.read(w.save(true));var r=restored.assemblies.get(s.id);
    assertEquals(s.actualPose,r.actualPose);assertEquals(s.bodies,r.bodies);assertEquals(s.programSeconds,r.programSeconds);
    assertEquals(MovingAssemblyState.Phase.FAULT_HOLD,r.phase);assertTrue(r.restored);assertFalse(r.locksEngaged);
    assertThrows(IllegalArgumentException.class,()->r.check(old));assertFalse(restored.owns(r.id,List.of(track)));
  }
  private static void clientSnapshotKeepsLivePhaseAndTokenWithoutPretendingToRestore() {
    var s=state();var snapshot=MovingAssemblyState.read(s.save(false));
    assertEquals(s.phase,snapshot.phase);assertEquals(s.token(),snapshot.token());assertFalse(snapshot.restored);
  }
  private static void competingReservationIsAtomicAndWholeBodyCannotHaveTwoOwners() {
    var first=state();var second=new MovingAssemblyState(UUID.randomUUID(),new BlockPos(20,0,0),"transfer","ordinary_n_v1",16);
    second.bed=UUID.randomUUID();var w=new MovingWorldState();
    w.assemblies.put(first.id,first);w.assemblies.put(second.id,second);
    var shared=UUID.randomUUID();var extra=UUID.randomUUID();w.reserve(first,List.of(shared));
    assertThrows(IllegalArgumentException.class,()->w.reserve(second,List.of(extra,shared)));
    assertFalse(w.owns(second.id,List.of(extra)));assertTrue(w.owns(first.id,List.of(shared)));
    second.bodies.put(first.cars.getFirst(),MovingAssemblyState.Pose.IDENTITY);
    second.cars=first.cars;
    var list=new ListTag();list.add(first.save(true));list.add(second.save(true));
    assertThrows(IllegalArgumentException.class,()->new MovingWorldState().read(list));
  }
  private static void restartAfterPhysicalReleaseKeepsClearanceIdentityAndExactPaidMaterials() {
    var s=state();s.bodies.clear();s.released=true;s.phase=MovingAssemblyState.Phase.RELEASING;
    s.receivingLength=12;s.materials.put("fourthdimension:moving_bed",48);
    var restored=MovingAssemblyState.read(s.save(true));
    assertTrue(restored.released);assertFalse(restored.retained());assertEquals(s.cars,restored.cars);
    assertEquals(12.0,restored.receivingLength);assertEquals(s.materials,restored.materials);
    assertEquals(MovingAssemblyState.Phase.FAULT_HOLD,restored.phase);
    var bad=s.save(true);bad.getCompound("materials").putInt("fourthdimension:moving_bed",-1);
    assertThrows(IllegalArgumentException.class,()->MovingAssemblyState.read(bad));
  }
  private static void corruptPoseOrMissingRetainedChassisCannotBecomeReleasePermission() {
    var s=state();var tag=s.save(true);tag.getCompound("pose").putDouble("qw",0);
    assertThrows(IllegalArgumentException.class,()->MovingAssemblyState.read(tag));
    s.bodies.clear();assertThrows(IllegalArgumentException.class,()->s.save(true));
  }

  @GameTest(template="empty",timeoutTicks=20) public static void persistence1(GameTestHelper h){restartPreservesActualPoseAndBodiesButRevokesReleaseAndReservations();h.succeed();}
  @GameTest(template="empty",timeoutTicks=20) public static void persistence2(GameTestHelper h){clientSnapshotKeepsLivePhaseAndTokenWithoutPretendingToRestore();h.succeed();}
  @GameTest(template="empty",timeoutTicks=20) public static void persistence3(GameTestHelper h){competingReservationIsAtomicAndWholeBodyCannotHaveTwoOwners();h.succeed();}
  @GameTest(template="empty",timeoutTicks=20) public static void persistence4(GameTestHelper h){restartAfterPhysicalReleaseKeepsClearanceIdentityAndExactPaidMaterials();h.succeed();}
  @GameTest(template="empty",timeoutTicks=20) public static void persistence5(GameTestHelper h){corruptPoseOrMissingRetainedChassisCannotBecomeReleasePermission();h.succeed();}
  public static void releasedServiceFramesSurviveButDiskDispatchDoesNot(GameTestHelper h) {
    var s=state();var car=s.cars.getFirst();var child=UUID.randomUUID();
    s.bodies.clear();s.released=true;s.phase=MovingAssemblyState.Phase.RELEASING;s.service.dock="A";
    var serviceCar=new MovingServiceState.Car(car,List.of(new MovingServiceState.Hold(car,child,
        new MovingAssemblyState.Pose(1,2,3,0,0,0,1),MovingAssemblyState.Pose.IDENTITY)));
    serviceCar.dispatched=true;s.service.cars.put(car,serviceCar);
    s.receivingEnvelope=new MovingProgram.Bounds(-2,-4,-3,2,1,3);
    var snapshot=MovingAssemblyState.read(s.save(false));var disk=MovingAssemblyState.read(s.save(true));
    assertTrue(snapshot.service.cars.get(car).dispatched);assertFalse(disk.service.cars.get(car).dispatched);
    assertEquals(serviceCar.holds,disk.service.cars.get(car).holds);assertEquals(s.receivingEnvelope,disk.receivingEnvelope);
    assertEquals(MovingAssemblyState.Phase.FAULT_HOLD,disk.phase);
    s.service.cars.put(car,new MovingServiceState.Car(car,List.of(new MovingServiceState.Hold(UUID.randomUUID(),child,
        MovingAssemblyState.Pose.IDENTITY,MovingAssemblyState.Pose.IDENTITY))));
    assertThrows(IllegalArgumentException.class,()->s.save(true));h.succeed();
  }
  private static void assertTrue(boolean value){if(!value)throw new AssertionError("Expected true");}
  private static void assertFalse(boolean value){assertTrue(!value);}
  private static void assertEquals(Object expected,Object actual){if(!Objects.equals(expected,actual))throw new AssertionError("Expected "+expected+" but got "+actual);}
  private static void assertThrows(Class<? extends Throwable> type,Runnable operation){
    try{operation.run();}catch(Throwable failure){if(type.isInstance(failure))return;throw new AssertionError("Unexpected exception",failure);}
    throw new AssertionError("Expected "+type.getName());
  }
}
