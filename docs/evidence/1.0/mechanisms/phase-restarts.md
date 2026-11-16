# FD902C — active-phase restart evidence

Executed2026-11-15 America/Phoenix (2026-11-16 UTC), owner
`01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`, primary `main`.
The [frozen input](../lead/freeze.md) remains immutable. The bounded repair
[candidate](overlay04.json) has SHA256
`8e441483af8a794ff0a4aa671d45d99f75c3c0de1138620aa7b3aaf8e82453e4`.
Only `ShuttleRuntime.class` and `ShuttleWinchEntity.class` differ from frozen
production; `Fd902LifecycleFixture.class` is an added opt-in observation/setup
helper. All other original JAR entries are byte-identical. Java21 compile06
passed. Lead owns integration and its final artifact identity.

## Root cause and bounded repair

Actual OUTBOUND shutdown saved the existing four-car/eight-carrier train and
native carriage at distance3.05. Frozen production reopened in FAULT but rebuilt
the carriage guide only inside the caught-phase condition. The independent empty
carriage fell until Sable removed it. Ordinary braking also allowed gravity creep
on the spike. [Failed state](outbound/failure-state-final.json) and its stopped
world are preserved, separate from the original active checkpoint.

The repair reconstructs the empty carriage guide independently of catching,
holds interrupted gravity/capture chassis, and leaves carrier capture free to
settle. Explicit Recover still validates identity, route, alignment and power.
A valid native chassis hold plus existing capture/angle/omega/body checks admits
solver velocity residue, like the established station hold. Holds persist while
the empty carriage approaches, then release once the physical catch exists.
CAPTURING reload cannot automatically attach a distant carriage.

An independently observed BOARDING native-holding gap now re-arms the existing
station posture restoration when the original bodies return. Lead's exact idle
and caught restoration checks passed. The caught fixture explicitly places both
original bodies in native holding: pinned Sable2.0.6 traverses intersecting
bounds and declared loading dependencies, not arbitrary physics catch joints.
The previous assumption that unloading the lead necessarily unloaded the
carriage was disproved by readback and upstream bytecode inspection.

## Actual saved phases and explicit recovery

| Case | Actual stopped save | Reopen and completion |
| --- | --- | --- |
| OUTBOUND | [Stopped winch](outbound/saved-winch.json), distance3.05, cycle0. | Original4/8/carriage/native links held through1339ticks with no phase/distance/cycle advance. [Comparison](outbound/repair03-comparison.json): successive maximum chassis drift0.00049 and0.00092blocks. First held candidate exposed the solver-readiness issue; final candidate rejected Resume, accepted explicit Recover, physically returned to [BOARDING cycle1](outbound/recovered-boarding.json), all4ready. |
| RETURNING | [Stopped winch](returning/saved-winch.json), distance20.45, cycle1, actual player aboard. | Reopened FAULT interruptedRETURNING. Original player automatically restored to the same fourth-car logical seat; all bodies/native links retained. [Identity comparison](returning/identity-comparison.json). No automatic recovery. Explicit Recover entered CAPTURING, where the next real process stop occurred. |
| CAPTURING | [Stopped winch](capturing/saved-winch.json), distance104.34410112355589, cycle1, same actual player. This is real recovery before the distant empty carriage reaches the train. | Original rider/body/seat and native graph restored. [760tick held comparison](capturing/hold-comparison.json): maximum chassis drift0.0000492blocks, carriage drift0. Explicit Resume moved the empty carriage, caught the train and reached [occupied BOARDING cycle2](capturing/recovered-boarding.json), all4ready and no faults. |

LIFTING actual cold restart and the4800tick support-loss case retain their
[accepted evidence](README.md#finite-cases-and-evidence-reuse). No phase was
injected into NBT or runtime state. The helper watches actual phases and calls
the dedicated server's normal halt/save path. Each stopped phase is decoded from
the actual world after process exit. Fresh Java server invocations reopen it.

Player `FD902`, UUID`48516062-50c2-36f2-9ebc-5be485c307ae`, uses the original
fourth-car carrier`bb450abd-5136-4272-9c97-aaa13c03aca1`, logical offset(0,-1,0).
Server events show accepted automatic remount at tick7237 for RETURNING and8179
for CAPTURING, each one tick after the queue and before its200tick deadline.
Native Create seat UUID recreation is allowed; logical player/carrier/seat is
the stable identity. The actual client views and network logs corroborate mounts.

## Evidence boundary

The first OUTBOUND fixture had16 native pig passengers. Their seats and nested
pigs are present in the stopped entity save; Create discards their seat entities
on cold load while pigs remain saved. That synthetic path is not credited as
player restoration. The shared changed gravity/capture path is exercised with
one actual player in RETURNING/CAPTURING, combined with the native OUTBOUND
identity assertions and accepted two-real-client PAX controls/synchronization.
Art's [current two-player cold backup restore](../art/migration.md) supplies the
separate shared restored-seat path for FD902A.

This run uses commands for initial fixture construction/native player mounting
and existing operator handlers for phase operations. It proves native lifecycle
and actual player restoration, not new normal click/crafting acceptance. Normal
controls, construction and two-client operation retain their earlier evidence.
No FPS/tick-budget claim is made during concurrent owners' work.

Persistent worlds, logs and launch inputs are under
`/mnt/NVME4/fourthdimension-runs/fd10-mechanisms-20261115`, outside build outputs.
Original checkpoints are `checkpoints/outbound`, `returning`, `capturing`;
failed original reopen is `outbound-failure`. Each has a complete stopped world.
The [OUTBOUND support-loss recovery](outbound-support/README.md), normal unload
and [verified cleanup](cleanup.json) are complete; the [package index](README.md)
reconciles the full FD902 outcome.
