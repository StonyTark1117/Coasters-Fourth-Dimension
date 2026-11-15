package dev.fourthdimension.gametest;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.vehicle.S2SpinningExamples;
import dev.fourthdimension.vehicle.S2SpinningGovernorEntity;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** A delayed example bind must survive controller recreation and an ordinary power guard. */
@GameTestHolder("fourthdimension_fd917_examples")
@PrefixGameTestTemplate(false)
public final class Fd917ExampleLifecycleGameTests {
  @GameTest(template="empty",timeoutTicks=220)
  public static void recreatedGovernorRetriedWithoutBypassingPower(GameTestHelper h){
    var level=h.getLevel();
    var rig=new S2SpinningGovernorGameTests.Rig[1];
    h.runAtTickTime(5,()->{
      var r=S2SpinningGovernorGameTests.create(h);rig[0]=r;
      var example=new S2SpinningExamples.Example(List.of(r.edge()),List.of(r.car()),r.station(),r.governor());
      var pos=r.governor().getBlockPos();var state=level.getBlockState(pos);
      level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
      level.setBlockAndUpdate(pos,state);
      ((CreativeMotorBlockEntity)level.getBlockEntity(r.motor())).generatedSpeed.setValue(0);
      S2SpinningExamples.scheduleBind(level,example);
    });
    h.runAtTickTime(70,()->{
      var r=rig[0];var live=(S2SpinningGovernorEntity)level.getBlockEntity(r.governor().getBlockPos());
      h.assertTrue(live!=r.governor(),"Governor was not recreated");
      h.assertTrue(r.car().chassis.equals(live.vehicle),"Pending bind never reached the live governor");
      h.assertTrue(r.car().authority.owner==null,"Unpowered pending bind bypassed the authority guard");
      ((CreativeMotorBlockEntity)level.getBlockEntity(r.motor())).generatedSpeed.setValue(192);
    });
    h.succeedWhen(()->{
      h.assertTrue(h.getTick()>80,"Wait for restored real power");
      var r=rig[0];var live=(S2SpinningGovernorEntity)level.getBlockEntity(r.governor().getBlockPos());
      h.assertTrue(r.car().authority.owner!=null&&r.car().chassis.equals(live.vehicle),"Pending bind did not recover with the live governor");
      h.assertTrue(r.car().stopping&&r.car().carriers.getFirst().occupants.isEmpty(),"Example unexpectedly launched or populated a rider");
      System.out.println("FD917_EXAMPLE_BIND PASS recreatedGovernor=true powerGuardRetained=true boundedRetry=true");
    });
  }
}
