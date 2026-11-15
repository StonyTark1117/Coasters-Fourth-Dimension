# Conventional Flying acceptance

Owner `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`, 2026-11-15. Primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`.
Production first integrated by the lead in `2b805fcb`; the final offline-origin
correction is supplied with this handoff. Lead alone stages and commits.

**FD1801A mechanics complete.** FD1801B/C construction/content reconciliation
continues independently. No New-generation behavior is claimed. The [player
guide](../../../FLYING.md) and [specification](../../../design/0.5/FD1801S-conventional-flying.md)
define the supported vehicle. No older family or release profile was reopened.

## Mechanics and native evidence

| Required behavior | Evidence and result |
| --- | --- |
| Dedicated overhead N running gear, posture capability, identity rejection | `native/contract.xml`: FlyingContractTest 1/1 pass; dedicated family/variant/capabilities and full envelope. |
| Physical four-seat X hinge, equal native load, four identical cars at spacing8 | `native/hinge-coordinate-pass.log`: empty mass4, one rider +.8, four riders +3.2; native transformed hinge gap below .00028, graph-relative hip Y=-2.50001. Original frame remains fixed to chassis, moving body owns seats/torso loads. |
| Bounded changeover and mechanical endpoint locks | `native/posture-pass.log` and hinge pass: actual rate peak .20356 rad/s, acceleration .22498 rad/s², below .25/.35 limits. Five-tick endpoint guard, separate body/leg closure, all-car prone dispatch guard, seated return/open. |
| Full fixed-frame and moving-row clearance | `native/whole-clearance-pass.log`: upper running gear and fork obstacles rejected, adjacent motor rejected, setback motor/two native shafts accepted. Declared whole-frame envelope is half-width2.25, above1.1, below4.5, half-length2.5. Geometry calculation in `preparation/geometry.json`; original overhead brace no longer emits the old low underbrace. |
| Interrupted power and explicit recovery | Native posture pass cuts actual Chain drive mid-transition, retains intermediate pose/restraints, roundtrips state at that instant, holds then explicitly resumes. In-route posture change and prone restraint release reject. |
| Actual restart while occupied and transitioning | [Restart state](restart/saved-foundation.json), [interrupted editor](restart/interrupted-editor.png), [recovered lock](restart/resumed-prone-lock.png), [normal unload](restart/normal-unload.png), [server acknowledgements](restart/server-excerpt.txt). Details below. |

Native logs are intentionally retained despite the repository's general `*.log`
ignore; include those specific files in the handoff commit. Their earlier failed
counterparts remain under the persistent run roots, documented in CHECKPOINT.md.
No failed result is relabeled as a pass.

## Real occupied input and presentation

[Artifact identity](occupied/artifact.json) describes the first occupied cycle.
The example track and four-car links were disclosed command setup; this is not
crafting or normal Chain evidence. After a verified normal camera turn and vanilla
placement in the same private client, ordinary right-click boarded the reachable
outer seat. Native Dev occupancy increased row mass4→4.8. Rotation Tool air-use
opened the normal editor. Separate actions34/36 closed body/legs;31 changed prone;
13 dispatched;10 stopped in the level dock;32 returned seated;35/37 opened each
restraint; normal Shift unloaded onto the safe platform.

Views cover [seated](occupied/seated-front.png), [transition](occupied/changeover.png),
[physical prone rider](occupied/prone-body.png), [neutral forward gaze](occupied/prone-neutral-view.png),
[bank](occupied/occupied-banked.png), [full inversion](occupied/occupied-inverted.png),
[roll exit](occupied/occupied-roll-exit.png), [seated lock](occupied/returned-seated-lock.png)
and [unloading](occupied/shift-unloaded.png). Paired JSON records are actual native
body observations. Parent-upY reached -.99999997 while posture remained prone
locked near1.5507rad with no fault. Native and snapshot angles agree; the physical
body rotates once, neutral head counterpitch and independent look do not rotate
the torso twice. After unloading, occupants clear, mass returns4 and client
`riding=false` with the normal upright look frame.

The native Create/Sable seat entity, hierarchy tracking, FoundationSync delivery
and remote pose transport are unchanged. Existing [Wing multiplayer proof](../../0.5/wing/README.md)
and [S1 carry-forward](../../0.6/spinning/README.md) apply to that transport.
This run supplies the new Flying body/head/eye transform and late-join local rider
proof; it does not claim a second observer run. No new transport path was added.

## Actual interrupted save and narrow correction

Normal action31 began the occupied transition. A read-only watcher issued server
`stop` at intermediate phi; shutdown saved all dimensions. Saved actual phi=.385209,
rate=.172465, target=π/2, body/legs closed, exact seat assignment and motor reference
angle=-.40725/velocity=-.18 persisted. Saved actuator reference avoids dropping the
motor tracking lead on restart. Reopened runtime physically brakes to about.5105
instead of inventing an endpoint. Dev rejoins through existing seat restoration;
normal Resume posture33 reaches PRONE lock, Set seated32 returns SEATED, then
35/37 and Shift unload normally. Observed rate .203241 and occupied sampled
acceleration .248125 remain bounded. Empty rows are sampled every20 ticks; the
native test supplies their complete per-tick bound evidence.

That run exposed one distinct offline-origin failure: Sable rebuilt empty native
mass4 but retained the saved occupied logical rotationPoint, giving a .300484
hinge gap until Dev rejoined. Pinned MergedMassTracker skips upload when native
mass/COM/inertia are unchanged. NativeLoading now refreshes its first sample and,
only for a no-delta stale origin, rebases the native COM coordinate once while
preserving every block's world position and unchanged angle/velocity. This occurs
before the first solver integration, not as a live pose correction. All subsequent
tick behavior and declared loads are unchanged. Axis's initial keel delta already
executes the native refresh path and retains its accepted evidence.

The untouched same-instant checkpoint was reopened once for this affected case.
[First-frame/offline summary](restart/origin-fix-summary.json), [offline hold](restart/origin-fixed-offline.json)
and [online restoration](restart/origin-fixed-online.json) prove the correction:
offline hinge gaps approximately.00010–.00012, COM/origin agreement, retained
intermediate state and both restraints; rejoined Dev has mass4.8 and hinge gap
.000174. [Final correction artifact](restart/artifact.json) is preserved. The
earlier complete normal recovery and occupied roll are carried forward; no loop
or whole native suite was repeated for this initialization-only change.

## Consumer contract and runtime cleanup

FD1802A may consume the integrated four-seat Flying hinge, exact native COM/pivot
convention, body/leg retention state, body/head/eye composition, read-only diagnostics,
and whole-frame clearance. Conventional transitions remain station-only; route
posture/yaw are owned by New-generation. Motors use acceleration gains4000/400,
bounded reference .18rad/s and .15rad/s² plus gravity compensation; endpoint
mechanical limits capture measured settled angle and retain separate motor preload.

Owned persistent root `/mnt/NVME4/fourthdimension-runs/fd1801-flying-20261115`;
world `server/FlyingWorkshop`; display317; game/RCON25652/25653; units
`fd1801-flying-20261115-{client,server,xvfb}.service`. Java21, client1536M heap/6GiB
process cap, server2048M/4GiB. No host desktop input. Mid-transition checkpoint,
recovered/unloaded world, raw traces and JARs are preserved outside build directories.
Original failed native runs on the root filesystem also exited cleanly.

At mechanics handoff all three owned units are inactive with MainPID0, all server
dimensions saved. Later B/C runs must explicitly restart these owned resources;
there is no idle live-runtime handoff.
