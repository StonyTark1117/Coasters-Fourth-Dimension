# FD403A Arrow circuit handoff

Use the [current queue](CURRENT-QUALIFICATION.md#fd403-construction-and-teaching-cases)
for assignments. FD502 repaired and saved a usable **preparation copy** on
2026-11-13. Its route legs were traversed cumulatively while correcting two
propulsion gaps; this is not FD403A's completed player-workflow acceptance.
The [executable handoff](evidence/0.4/fd502-execution-repair-20261113/README.md)
contains the input proof, resources and evidence.

## Accepted claims and exact remaining work

| Claim | Disposition / next action |
| --- | --- |
| Normal Arrow crafting, paid connection, car placement and Chain installation | Carry forward the [50 m client record](evidence/0.4/survival-client-arrow-powered/acceptance.md). No recrafting or generic input replay. |
| Occupied controlled full turn, capture, unload and cold reopen on that straight | Accepted in the same record. Reuse unchanged mechanics and identities; observe only their still-unproven interaction with the completed circuit. |
| Full native circuit mechanics | [Native eight-section loop](evidence/0.4/survival-client-arrow-loop-native/README.md) remains accepted at its synthetic-player scope. It does not certify the connected-client circuit. |
| Camera and ordinary item use | FD502 verified both in one connected client. Use the checked-in helper; do not repeat mouse-method discovery. |
| This loop's return connections and propulsion | All nine graph edges already exist. FD502 verified motion over their combined route, repaired F–G with normal Chain input and corrected C–D coverage/direction as disclosed setup. Do not rebuild the other connections. If using this copy for normal-construction acceptance, replace/verify **only C–D's command-extended Chain span** through normal client input. Its command edit earns no placement credit. |
| Complete occupied client circuit | Still open: record one completed circuit with the intended car, expected route order, controlled-profile behavior, normal final capture/open/unload and no fault. Reuse earlier component evidence; no second client is required. |
| Final circuit state after reopen | Still open: save/reopen the completed circuit and check its relevant route/profile and vehicle/carrier identities and captured/unloaded usability. Do not repeat unrelated persistence or recovery cases. |
| Arrow teaching | Reconcile those observations with the [accepted common/changed-caption review](evidence/0.4/teaching-current/acceptance.md). Share the result with FD403F; no second walkthrough run. |

## Saved world and intended vehicle

Persistent root:
`/home/braydon/.local/state/fourthdimension-runs/fd502-execution-repair-20261113`.
World: `server/FD403ArrowRetained`. Source: the previously stopped
`fd403a-root-parallel-20261113/server-game/FD403ArrowRootParallel`, left untouched.
The active FD403 copies on `:248` and `:250` are different worlds.

- Intended chassis: `819ce77e-8cd5-4eaf-831a-c45de09908cb`.
- Carrier identities: `62191fac-a0ae-441f-b94a-b1c9f7dd1109` and
  `b86810c6-bd7e-4efc-8e20-46bed47ecba2`.
- Final chassis position: approximately `(230.5,-58.88,34.52)` on A–B.
  Captured, `boarding_ready=1`, `alignment_required=0`, empty fault,
  restraints open, both occupant/rider lists empty. `RootVehicle` was absent.
- Bound rail: `35e41c5e-5d06-4908-8ea3-fd063040eefb`, A–B,
  profile `0:0:0;15:360:0;50:0:0`. The carrier's `longitudinal=0` is an offset,
  not a claim that the chassis is at the route origin.
- The saved player is at `(237.5,-60,34.209)`, yaw 90°, pitch −6.63°,
  empty hand selected (hotbar 2), looking at the accessible open Arrow seat.
  F3 identified local seat `(20481033,127,20581384)`; these are Sable-local
  coordinates, not a world teleport destination.

Two obstructing return cars were removed **from this copy only** through Sable's
native removal path after exact UUID checks:
`7a1295c5-2e6e-48e5-9925-8e4068d1de76` and
`b83842c8-ef61-4b5f-b732-884c2ea322e2`. Their originals remain in the source.
The final save contains 22 rails and 13 vehicles. This cleanup earns no player
removal or recovery acceptance.

## Route and working propulsion

All anchor coordinates below have y=−60. Verified travel order is
**A → I → H → G → F → E → D → C → B → A**, starting midway along A–B.
Use these observed directions, not an assumed clockwise route or a sign inferred
from another world. Chain direction also depends on its stored path orientation.

| Anchor | (x,z) | Final drive / route observation |
| --- | --- | --- |
| A | (230,20) | Full 50 m A–B Chain; anchor speed −16, travel sign −1, source `(231,-60,20)`. Observed travel toward decreasing z. |
| B | (230,70) | B–C curve coasts into A–B; no new span needed. |
| C | (238,78) | C–D extended from 14 to 16 m by disclosed block-data setup. Source `(239,-60,78)`, west-facing motor `ScrollValue=-32`, produces anchor speed **+32**, sign +1. Observed travel toward **decreasing x**, D→C. The old −16 source opposed this route. |
| D | (254,78) | D–E bend traversed; two obstruction cars were cleared. |
| E | (262,70) | Existing 12.0859 m span toward D, speed +16, sign −1, source `(261,-60,70)`. Observed F→E→D traversal. |
| F | (262,45) | New normal-client Chain span: entire G–F plus a short adjoining H–G portion, total 26.3467 m. Source `(263,-60,45)`, west-facing motor `ScrollValue=-32`, anchor speed **+32**, sign +1. Observed increasing z toward F and onward toward E. |
| G | (262,20) | Initial run stalled near `(264.1,22.9)` on unpowered G–F. F's new span resolves that gap. |
| H | (254,12) | H–G bend traversed from the existing I drive. |
| I | (238,12) | Existing 14.0841 m span, speed −16, sign +1, source `(238,-60,13)`. Observed increasing x toward H; A–I coasts into it. |

The initial snapshots intentionally retain the **old** coverage and power values.
The console/input logs and corrected motion records establish the final changes.
A partial span does not automatically need replacement: retain one when actual
travel proves the adjoining coast. A speed field alone does not prove coverage
or direction.

## Boarding and operation from the retained stop

1. Obtain a recorded runtime handoff, or make an independently owned copy of
   this **stopped** save. Use the [scoped launch procedure](FD403-RUN-PREPARATION.md),
   one client and the existing standalone profile. Confirm live connection,
   current screenshot and window geometry.
2. At the saved pose, confirm the open seat is reachable and empty hand is
   selected; one normal right-click boards it. Earlier preparation boarding of
   this same seat succeeded. Do not use a stale chassis coordinate as its seat.
3. Select the retained Rotation Tool (hotbar 8), right-click, confirm **Ready**,
   choose **Close restraints**, then reopen the editor and confirm closed state.
   At the verified 854×480 geometry, Close is `(425,215)` and Dispatch `(310,241)`;
   re-read the screen after a resize. The tool was supplied for setup and earns
   no crafting credit; accepted crafting evidence is separate.
4. Dispatch once and read native motion plus the client view. Expect decreasing
   z on A–B, followed by the route order above. Keep the profile bound to A–B;
   an arbitrary replacement car on C–D would not exercise that profile.
5. On return, normal Capture `(310,215)` stops and aligns the car. Reopen the
   editor to confirm **Ready**, open restraints `(540,215)`, then use Shift to
   dismount. The helper holds keys for 180 ms; the former 12 ms default tap was
   missed. Confirm the actual state after each operation.
6. Record only the remaining acceptance claims above, perform their relevant
   reopen check, reconcile teaching, and stop. FD502's diagnostics do not become
   a new test sequence for FD403 agents.

## Preparation proof and limitations

The first occupied preparation traversal crossed A–I–H–G and isolated the G–F
power gap. After its normal Chain repair, the same occupied car crossed F–E–D
and reached C–D. Extending C–D exposed the opposing drive; correcting its source
produced approximately −1.99 m/s in x, then B–C traversal and −1.00 m/s in z on
A–B. Normal Capture/Open/Shift left the final state described above. These are
cumulative route-readiness observations with stops and repairs, **not one
uninterrupted acceptance circuit**. No generic input retest, new native suite
or second client is needed to consume them.

The world was saved and the server/client exited cleanly. The saved identities,
profile and final vehicle state were decoded; no new cold-reopen acceptance is
claimed. Source worlds and other agents' runtimes were not changed.
