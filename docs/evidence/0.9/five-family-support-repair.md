# Five-family exact support repair and physical realignment — native checkpoint

**Scope:** FD902 core recovery behavior on the current original functional
tracks. The native server/GameTest checkpoint is supplemented by the
[packaged occupied five-family client realignment](client-occupied-five-family-support-realignment.md).
The server now exercises two independent rider identities per family. The
separate [Arrow two-client recovery checkpoint](two-client-arrow-support-recovery.md)
adds live replication of this interruption; wider family and lifecycle coverage
remain outside this scope.

## What is covered

`fiveFamilySupportRepairRequiresExactTrackAndRealignment` creates one native
car for each original family: Arrow, legacy ZacSpin, Free Spin, revised
ZacSpin, and PAX. It places two independent server-player identities in
distinct native seats of each car's first carrier, then:

1. removes each car's actual matching Coasters curve while both native riders
   remain mounted;
2. requires the car to stop with restraints closed, all carrier bodies
   captured, and the `Matching track support lost` fault recorded;
3. rejects dispatch before explicit repair;
4. rebuilds the original matching section through the normal track-placement
   path, which is deliberately permitted only for this captured support-loss
   state;
5. accepts **Repair matching track** only when the rebuilt curve matches the
   saved geometry fingerprint; and
6. keeps every rider mounted, each two-seat assignment distinct, and every car
   stopped, restrained, and captured;
7. requires **Realign matching track** as a separate explicit action, which
   runs Coasters Simulated's native physical snap against that exact repaired
   edge and clears its disengaged-bearing marker only after an eligibility-
   approved snap; and
8. verifies the bearing's resulting physical position on the repaired rail,
   both retained mounted riders, their distinct seat assignments, and normal
   circuit-family dispatch. PAX passes the same matching-track dispatch gates
   and then stops at its required powered shuttle-winch gate, which this
   focused fixture does not construct; and
9. recaptures the dispatch-proven circuit cars, serializes the repaired holding
   state, moves every chassis and carrier into Sable's native holding store, and
   verifies that every original body, both mounted riders, their seat
   assignments, restraint, capture, repaired binding, and cleared alignment
   requirement restores intact.

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
FD_SUPPORT_REPAIR families=[ARROW, ZACSPIN, FREE_SPIN, ZACSPIN_REVISED, PAX] riders=10 exactGeometryOnly=true noAutoResume=true realignmentRequiredBeforeDispatch=true nativeReengagement=true normalDispatch=true postRepairHoldingReload=true
========= 44 GAME TESTS COMPLETE IN 4.154 min ======================
All 44 required tests passed :)
BUILD SUCCESSFUL
```

## Remaining work

The checkpoint now includes a mounted packaged client for every family,
server-native two-rider recovery, and a server-native holding-store
unload/reload after repair. It does not show simultaneous recovery replicated
for Legacy, Free Spin, Revised, or PAX, PAX lift dispatch after realignment,
or a post-repair client reconnect or cold server restart. Those remain FD902
lifecycle work.
