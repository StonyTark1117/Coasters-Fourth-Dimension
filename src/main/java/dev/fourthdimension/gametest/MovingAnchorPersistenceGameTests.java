package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.MovingAssemblyState;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd2001_anchor_persistence")
@PrefixGameTestTemplate(false)
public final class MovingAnchorPersistenceGameTests {
  @GameTest(template="empty",timeoutTicks=20)
  public static void captureAnchorSurvivesDiskAndLegacyIdentityRemainsHeld(GameTestHelper h) {
    var s=new MovingAssemblyState(UUID.randomUUID(),BlockPos.ZERO,"transfer","ordinary_n_v1",16);
    var car=UUID.randomUUID();s.bed=UUID.randomUUID();s.cars=List.of(car);
    s.bodies.put(car,new MovingAssemblyState.Pose(-.296296, .92131,0,0,0,0,1));
    var anchor=new Vector3d(20481032.796,129.296,20483080.5);
    s.bodyAnchors.put(car,new Vector3d(anchor));
    var saved=s.save(true);var restored=MovingAssemblyState.read(saved);
    require(anchor.equals(restored.bodyAnchors.get(car)),"Exact plot-local anchor changed on disk roundtrip");
    restored.bodyAnchors.get(car).x+=1;
    require(anchor.equals(s.bodyAnchors.get(car)),"Read anchor aliases original state");
    require(anchor.equals(MovingAssemblyState.read(saved).bodyAnchors.get(car)),"Read anchor aliases saved tag");
    var legacy=saved.copy();legacy.getList("bodies",Tag.TAG_COMPOUND).getCompound(0).remove("anchor");
    var old=MovingAssemblyState.read(legacy);
    require(old.bodies.equals(s.bodies)&&old.cars.equals(s.cars),"Legacy identities/poses lost");
    require(old.bodyAnchors.isEmpty()&&old.restored&&old.phase==MovingAssemblyState.Phase.FAULT_HOLD,
        "Legacy record synthesized an anchor or granted motion");
    require(MovingAssemblyState.read(old.save(true)).bodies.equals(s.bodies),"Legacy fault record cannot be preserved");
    var malformed=saved.copy();malformed.getList("bodies",Tag.TAG_COMPOUND).getCompound(0).getCompound("anchor").remove("x");
    rejects(()->MovingAssemblyState.read(malformed));
    var nonfinite=saved.copy();nonfinite.getList("bodies",Tag.TAG_COMPOUND).getCompound(0).getCompound("anchor").putDouble("x",Double.NaN);
    rejects(()->MovingAssemblyState.read(nonfinite));
    s.bodyAnchors.put(UUID.randomUUID(),new Vector3d());rejects(()->s.save(true));
    h.succeed();
  }
  private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
  private static void rejects(Runnable action){
    try{action.run();}catch(IllegalArgumentException expected){return;}
    throw new AssertionError("Invalid anchor record accepted");
  }
}
