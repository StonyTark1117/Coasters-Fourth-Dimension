package dev.fourthdimension.gametest;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Opt-in release harness: stable original cases, bounded class batches, explicit affected filter. */
@GameTestHolder("fourthdimension_fd901")
public final class Fd901NativeSuite {
  @GameTestGenerator
  public static Collection<TestFunction> cases() {
    if(!System.getProperty("neoforge.enabledGameTestNamespaces", "").contains("fourthdimension_fd901"))return List.of();
    var types=List.of(AppearanceGameTests.class,
        BankedArrowGameTests.class,
        BankedRecoveryGameTests.class,
        BankedRecoveryTrainGameTests.class,
        BoundaryGameTests.class,
        ChainChassisAvailabilityGameTests.class,
        ChainRecoveryAvailabilityGameTests.class,
        ControlledTrainGameTests.class,
        DemoRideGameTests.class,
        EditorGameTests.class,
        EngineeredAvailabilityGameTests.class,
        EngineeredEnergyGameTests.class,
        EngineeredGameTests.class,
        EngineeredInterlockGameTests.class,
        EngineeredRecoveryAvailabilityGameTests.class,
        FamilyEligibilityGameTests.class,
        FoundationGameTests.class,
        HistoricalMigrationGameTests.class,
        MagneticEditorGameTests.class,
        MagneticGameTests.class,
        MagneticTraversalGameTests.class,
        OperationsGameTests.class,
        OrdinaryCartGameTests.class,
        PonderDiagramGameTests.class,
        RecoveryClearanceGameTests.class,
        RecoveryFamilySubstepGameTests.class,
        RecoveryGameTests.class,
        RecoveryHandoverGameTests.class,
        RecoveryStrengthGameTests.class,
        RecoveryTrainGameTests.class,
        RevisedCarrierGameTests.class,
        RideTelemetryGameTests.class,
        ShuttleBoardingHoldGameTests.class,
        ShuttleGameTests.class,
        SurvivalBuildingGameTests.class,
        TeachingContentGameTests.class,
        TrackContentGameTests.class,
        VehicleContentGameTests.class);
    String filter=System.getProperty("fourthdimension.fd901Filter", "");
    var selected=new HashSet<>(Arrays.asList(filter.toLowerCase(Locale.ROOT).split(",")));
    var result=new ArrayList<TestFunction>();
    for(var type:types)for(var method:type.getDeclaredMethods()) {
      var test=method.getAnnotation(GameTest.class);if(test==null)continue;
      String name=method.getName().toLowerCase(Locale.ROOT);
      if(!filter.isBlank()&&!selected.contains(name)&&!selected.contains(type.getSimpleName().toLowerCase(Locale.ROOT)))continue;
      // Default used to launch dozens of unrelated native fixtures against the shared64-vehicle budget.
      String batch=test.batch().equals("defaultBatch")?"fd901_"+type.getSimpleName():test.batch();
      result.add(new TestFunction(batch,name,"fourthdimension_fd901:empty",Rotation.NONE,
          test.timeoutTicks(),test.setupTicks(),test.required(),test.manualOnly(),
          test.attempts(),test.requiredSuccesses(),test.skyAccess(),helper->{
            try {method.invoke(null,helper);}
            catch(java.lang.reflect.InvocationTargetException e) {
              if(e.getCause() instanceof RuntimeException r)throw r;
              if(e.getCause() instanceof Error error)throw error;
              throw new RuntimeException(e.getCause());
            } catch(ReflectiveOperationException e) {throw new RuntimeException(e);}
          }));
    }
    if(result.isEmpty())throw new IllegalArgumentException("FD901 filter selected no cases: "+filter);
    System.out.println("FD901_SUITE cases="+result.size()+" filter="+filter+" batches="+result.stream().map(TestFunction::batchName).distinct().count());
    return result;
  }
}
