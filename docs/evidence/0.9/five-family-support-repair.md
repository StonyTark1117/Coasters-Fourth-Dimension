# Five-family exact support repair — native checkpoint

**Scope:** FD902 core recovery behavior on the current original functional
tracks. This is a native server/GameTest checkpoint; it does not establish
packaged-client repair, multi-client repair, or a full post-repair physical
recovery ride.

## What is covered

`fiveFamilySupportRepairRequiresExactTrackAndExplicitResume` creates one
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
6. keeps every rider mounted and every car stopped, restrained, and captured.

The repair action does not resume a car. A chassis whose original section
vanished must be returned to the normal captured-and-aligned state through the
existing recovery workflow before dispatch can pass its normal readiness gate.

## Reproduction

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test runGameTestServer --no-daemon
```

The current run completed successfully on 2026-11-02:

```text
FD_SUPPORT_REPAIR families=[ARROW, ZACSPIN, FREE_SPIN, ZACSPIN_REVISED, PAX] riders=5 exactGeometryOnly=true noAutoResume=true realignmentRequiredBeforeDispatch=true
========= 44 GAME TESTS COMPLETE IN 4.254 min ======================
All 44 required tests passed :)
BUILD SUCCESSFUL
```

## Remaining work

The checkpoint does not show a rendered client repair, simultaneous repair by
multiple clients, or end-to-end physical return of a fallen chassis to track.
Those remain FD902 lifecycle and client acceptance work.
