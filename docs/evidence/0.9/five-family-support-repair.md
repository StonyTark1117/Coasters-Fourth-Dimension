# Five-family exact support repair and physical realignment — native checkpoint

**Scope:** FD902 core recovery behavior on the current original functional
tracks. This is a native server/GameTest checkpoint; it does not establish
packaged-client repair, multi-client repair, or a full post-repair lifecycle.

## What is covered

`fiveFamilySupportRepairRequiresExactTrackAndRealignment` creates one
occupied native car for each original family: Arrow, legacy ZacSpin, Free Spin,
revised ZacSpin, and PAX. It then:

1. removes each car's actual matching Coasters curve while a native pig rider
   remains mounted;
2. requires the car to stop with restraints closed, all carrier bodies
   captured, and the `Matching track support lost` fault recorded;
3. rejects dispatch before explicit repair;
4. rebuilds the original matching section through the normal track-placement
   path, which is deliberately permitted only for this captured support-loss
   state;
5. accepts **Repair matching track** only when the rebuilt curve matches the
   saved geometry fingerprint; and
6. keeps every rider mounted and every car stopped, restrained, and captured;
7. requires **Realign matching track** as a separate explicit action, which
   runs Coasters Simulated's native physical snap against that exact repaired
   edge and clears its disengaged-bearing marker only after an eligibility-
   approved snap; and
8. verifies the bearing's resulting physical position on the repaired rail,
   retained mounted riders, and normal circuit-family dispatch. PAX passes the
   same matching-track dispatch gates and then stops at its required powered
   shuttle-winch gate, which this focused fixture does not construct.

Neither action resumes a car. Repair clears the fault but marks the vehicle as
requiring alignment; dispatch fails closed until the explicit physical
realignment has returned the original native chassis to the repaired matching
section and its captured carrier joints have settled.

## Reproduction

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test runGameTestServer --no-daemon
```

The current run completed successfully on 2026-11-02:

```text
FD_SUPPORT_REPAIR families=[ARROW, ZACSPIN, FREE_SPIN, ZACSPIN_REVISED, PAX] riders=5 exactGeometryOnly=true noAutoResume=true realignmentRequiredBeforeDispatch=true nativeReengagement=true normalDispatch=true
========= 44 GAME TESTS COMPLETE IN 4.549 min ======================
All 44 required tests passed :)
BUILD SUCCESSFUL
```

## Remaining work

The checkpoint does not show a rendered client realignment, simultaneous repair
by multiple clients, PAX lift dispatch after realignment, or unload/reload
after repair. Those remain FD902 lifecycle and client acceptance work.
