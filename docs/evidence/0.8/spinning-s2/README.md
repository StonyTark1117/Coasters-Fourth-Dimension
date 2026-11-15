# S2 launched/inverting Spinning — FD1501D–F

**Current:** FD1501D is qualified, integrated and closed. FD1501E/F are qualified
by the [content and cumulative acceptance handoff](content/README.md), including
normal paid construction, operating views, restart readback and verified cleanup.
Earlier remaining-check lists below are historical checkpoints; do not replay the
accepted native governor/route/loss or occupied mechanics cases.

Active owner `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`, dispatch
HEAD `cb46ea072d2c731948e4cac07e4d5350e269dbc5`. Only pre-existing `logs/` was
untracked at dispatch. Lead owns shared hooks, central queue/ROADMAP/PLAN and Git
integration; this owner creates only `S2Spinning*` classes/tests, `s2_spinning_*`
resources, its generator/guide and this evidence. No new branch, worktree or chat.

The human's Continue 0.8 authorization supersedes historical no-0.8 notes. Full
assignment is S2 D–F, then Powered Inverted Yaw A–C/FD1901/FD1902. All accepted
0.4–0.7 cases remain closed. Powered consumes the lead's qualified FD1701A
authority handoff; preparation does not claim that prerequisite passed.

## Adopted scope and implementation

[FD1501 specification](../../../design/0.5/FD1501-spinning.md): one four-seat
local-Y carrier, native N contacts, full roll, 1–4 identical cars at eight blocks,
radius >=16, roll change <=5 degrees/block, speed <=16. Native rider mass .8 per
seat and inertia remain authoritative. Damping .20/s, passive guard above3rad/s,
fault above4. No target-facing yaw or random/animated angle substitute.

Standalone launch uses a real stress-supplied Create transmission feeding a
committed native Chain span. It commands actual source RPM with a0→12blocks/s
ramp at <=3blocks/s²; it does not misinterpret the native speed-match timestep as
requested velocity. `S2SpinningGovernorEntity` owns only its kinetic ratio and
fresh lease. Lead-owned `RideAuthority` owns one train's hold/release/epoch, while
native Coasters owns longitudinal impulse. Magnetic spans apply bounded local-Y
torque only, independent of launch speed. FD812 is optional, not a blocker.

Existing S1 native loading/joint-axis/camera conventions carry forward only where
identical. S2-specific eligibility, full inverted sweep, four-car identity,
governor stress/power/ramp, magnetic axis and interrupted launch need direct proof.
No mechanics or occupied acceptance is claimed by the initial source checkpoint.

## Ordered remaining checks

1. Coherent Java21 compilation and bounded contract checks; then native real
   source→governor→Chain ramp, actual signed travel and power cut/explicit capture.
2. Actual inverted native free dynamics with load, magnetic induction/damping,
   four independent cars, rollback and retained ramp/angle/authority restart.
3. Early representative real-client board→launch/inversion→magnetic damping→
   stopped capture→unload. Reuse shared rider/remote pose evidence unless a changed
   path actually invalidates it. Hand qualified D to lead before decorative work.
4. Normal recipes, paid placement, Chain, governor/marker controls, finite FD916
   asset review and guide/example complete E/F using cumulative D evidence.

Two inconclusive attempts require diagnosis. Each passing affected check stops;
no full-profile sweep or prior-family replay. New hashes are not new acceptance.

## Owned storage and resources

Reserved persistent root `/mnt/NVME4/fourthdimension-runs/fd1501d-s2-20261115`,
private display318, loopback ports25654/25655, units
`fd1501d-s2-20261115-{client,server,xvfb}.service`. Reservation checked idle before
use; no runtime launched at this checkpoint. Every launch rechecks ownership,
host/cgroup headroom and actual connection/window geometry. Build/world/client
directories stay below the persistent root, with worlds outside build.

Java21 executable and Gradle launcher/daemon verified. Defaults: client1536M heap
and6GiB Minecraft-only scope, separate Gradle preparation. Current storage XFS,
55GiB available and36GiB host memory available at initial review; these are not a
future launch budget. Save/stop all owned runtimes and verify PIDs inactive before
closure; preserve requested worlds/logs/screenshots first.

## Governor targeted result — 2026-11-15

Two pure field contract tests passed (`contract-02.log`): DAMP opposes relative
omega without overshoot and INDUCE keeps its canonical sign below the guard.
The first native launch exposed repeated detach/re-attach driving Create's
flicker score above128; the corrected transmission keeps its supplied input
network and smoothly updates only its straight source-owned shaft line, retaining
native stress accounting. The second reached a real route-end stop because native
`applySpeedMatch` negates projected belt velocity. The governor now maps its
vehicle-direction request through that upstream convention; it does not replace
native longitudinal impulse. These were diagnosed distinct failures, not blind
retries. `inspect/` retains the pinned upstream bytecode used for diagnosis.

`native-governor-03.log` **PASS**: actual forward native speed11.86937blocks/s,
measured source-ramp acceleration3.000011blocks/s² (float tolerance), one actual
.8-unit off-center rider, local-Y magnetic motion, exact retained winding/ramp NBT,
actual motor removal→retained travel/authority hold, restoration without automatic
launch, explicit Capture and actual dock latch before restraint opening. This
closes that bounded native behavior, not the entire D card. Native world copies
`native-governor-{01,02,03}-world` and logs are outside the build directory.
All three GameTest Java processes exited; no client/display/server service was
launched by these command-line checks.

The next native case uses four independently loaded carriers on the original S2
example: real launch, unpowered downhill full roll, DAMP, return capture and
explicit stopped reversal. Its namespace is `fourthdimension_fd1501d_route`.

## Four-car inversion/return/rollback result

`native-route-03.log` **PASS**: four independently loaded native carriers completed
full inversion on unpowered coast; their angle histories diverged by8.0533rad.
Peak native travel speeds were11.8762/11.8758/11.8906/11.8887blocks/s. Magnetic
DAMP performed negative relative spin work. All four returned, stopped and
physically captured; explicit stopped reverse dispatch produced backward native
travel without negating retained winding, followed by capture/restraint opening.
The saved `native-route-03-world` and log remain outside the build directory.

The first example failure was final native-curve fingerprint mismatch. The example
now fingerprints stored curves after constructing every hub and uses native
banked lifted endpoints, reusing the accepted NG endpoint convention. The second
run proved inversion but timed out because rolling drag exhausted coast momentum
before the far return. The corrected example keeps its downhill inversion
unpowered and adds normal Create-supplied native Chain on the return at8blocks/s;
the same authority gates that provider. Neither correction weakens a route,
speed, restraint or physics guard. Earlier worlds01/02 remain preserved.

Code inspection then found the shared missing-child branch lacked S2's train-wide
fault hook; requested the lead-owned correction. That specific failure check and
real connected-client occupied/content workflow remain open; the passed route
is accepted cumulative native evidence and does not need repeating.

## Occupied mechanics handoff — FD1501D

Packaged client01 artifact SHA-256
`85dd164767616ed0f0ed1cc92b13dcda4bd81a2d27334b5440b7342abdcb5417`, Java21,
1536MiB game heap/6GiB service, server2048MiB/4GiB, private display318 and
loopback25654/25655. The real connected player Dev used normal seat right-click,
Rotation Tool, Close restraints and Dispatch; the route then completed launch,
unpowered full roll and powered return with one real occupant. Independent
seated look remained available; Shift with closed restraints kept that occupant.
The normal seat hit is the cushion shape, below its decorative backrest. An early
miss was a player standing off the platform and falling; it earned no input credit.
Default Raw Input and mouse bindings were unchanged after the initial same-client
camera/vanilla-placement baseline.

A normal Capture button click on return stopped the car and aligned it at retained
winding -4π. The dedicated server continued ticking while the editor was open.
The exact dock click was a one-shot GUI action conditioned on read-only player
position, not an injected ride command; raw record is
[occupied-return-click.log](client/occupied-return-click.log). Open restraints and
normal Shift then unloaded the player onto the platform: status occupants0,
capturetrue, angle-12.5663706143, negligible omega, no fault. See
[normal-unloaded.json](client/normal-unloaded.json) and the selected client views.
Commands supplied the disclosed example, tool and safe initial viewpoint, not
boarding, dispatch, capture, restraint changes or unloading.

[Native loss03](native/native-loss-03.log) passes actual body deletion and a
separate native coupler removal. Upstream deletion removes the affected native
hierarchy/chassis too; all surviving cars retain winding, restraints and a fault,
physically stop and deny unloading. The fixture was corrected to avoid querying
velocity on the deliberately deleted chassis. The lead supplied the shared
missing-carrier hook; no earlier passed route needs repeating.

The shared S1 rider/native hierarchy and accepted remote sequence behavior remain
unchanged; S2 uses the same fractional-seat mount hook and complete server
snapshot. Dedicated S2 evidence above supplies its launch, inverse support,
magnetic, four-car, interruption and retained-winding differences. Combined
native and connected-client evidence qualifies **FD1501D**. E/F still own normal
new-item construction/governor controls, the finite asset views (including a clear
inversion operating view), guide reconciliation and actual process-restart
readback. These are continuing assigned work, not a reset of D's passed cases.

At12:24UTC the occupied client exited, the dedicated server saved every dimension
and exited, then owned Xvfb318 exited. All three units report `inactive`, MainPID0.
The saved `server/S2SpinningNative` and `occupied-unloaded-01-world`, screenshots,
raw input, logs and captured/unloaded state remain preserved. No idle runtime was
left running. Continue E/F from this stopped world with the latest shared UI.
