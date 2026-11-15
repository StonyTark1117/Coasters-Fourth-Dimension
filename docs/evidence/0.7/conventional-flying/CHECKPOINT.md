# Conventional Flying execution checkpoint

Owner: chat `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`. Assigned FD1801A–C and
FD1801 only. Active implementation, no acceptance claim yet. Lead alone stages,
commits, pushes and updates central roadmap/queue. Existing primary `main` at
`b8db0f06b566ed5b465c758c005db11d5c7b9837`, checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`. Preserve other owners' edits
and pre-existing untracked `logs/`.

Owned source: new vehicle/Flying*.java, FlyingGameTests, Flying*Test, flying_
resources, generate-flying.py, FLYING.md, this evidence. Lead explicitly transferred
RiderComfortMixin and new Flying* mixins; LocalRiderRenderMixin stays unchanged.
Lead integrates shared enum/registration/serialization/runtime/editor hooks from
our concrete new public interfaces. No new chat, subagent, branch or worktree.

Implemented initial source (not compiled/qualified yet): native Generic X posture
body with native angle=-design phi, hip graphY=-2.5 and native bearing correction
once, four seats, separate retained body/leg states, five-tick endpoint guard,
dock-only transition, actual intermediate interrupt hold, explicit recovery,
native Chain power authority, continuous sweep reservation, four-car spacing8.
New camera/head counterpitch uses physical interpolated parent-child orientation;
body remains attached once to the physical child. Dedicated seat attachment fixes
player hip to child seat center, torso stays native north and look stays independent.
Native and player checks will validate these choices before acceptance.

Original resources have explicit UVs and <=16-unit cuboids; all frame and seat
selection shapes match authored parts. Frame generated from FlyingFrameBlock.BOXES
is22 connected cells plus controller. Running N contacts unchanged. Existing
OriginalTrackRenderer overhead brace is graphY+.95; the historical -.70 brace is
not emitted on overhead track. New stationary center brace lowerface=-.55.
Pinned native bottom model lowest zero-thickness side-plane Y1/16 relative native
block origin graphY-.1875=>-.125; its central box lowerfaceY3/16=>graphY0.
Swept closed occupied YZ corner upper extent=-2.5+sqrt(1.5²+.6²)=-.88445;
center brace gap=.33445. Outboard hinge support innerX2.1875 gives.25 to actual
closed row seat/rider outerX1.9375; reserved external occupied bound remains2.0.
Native/visual sweep proof is still pending, including same-client posture gaze.

Next finite checks: compile coherent shared source; targeted FD1801 native check
(four cars, asymmetric/full equal loading, physical rate/acceleration, interrupted
hold+snapshot+explicit recovery, endpoint guard, seated return); private occupied
ordinary placement/board/body+leg close/changeover/dispatch/banked-inverted travel/
stop/seated/open/Shift with camera restoration. Then one actual mid-transition
save/restart, content crafting/buildability and any still-unproven rejection.
Early complete A occupied handoff goes to lead/New-generation before B/C polish.
Prior0.4–0.6 accepted evidence carries unchanged except named affected hooks.

Planned private run root:
`/home/braydon/.local/state/fourthdimension-runs/fd1801-flying-20261115`;
private display317, game/RCON ports25652/25653, units
`fd1801-flying-20261115-{server,client,xvfb}.service`. Not launched or claimed yet;
verify current ownership and headroom before launch. Defaults Java21, heap1536M,
6GiB client-only cap, sync-chunk-writes=false. No runtime currently owned.

## First native result and active correction

Java21 confirmed both launcher and Gradle9.2.1. FlyingContractTest passed1/1.
First native `build` run passed normal native contracts, four-car8 linking,
empty mass4.0, asymmetric+.8/full+3.2 and all separate-retention guards;
power interruption held phi≈.534 and roundtripped exact state; explicit resume
locked the occupied first row. Failed at tick405 because empty rows stabilized
at phi≈1.4677, short of the2° latch tolerance. Saved state proves a motor-gravity
droop, not insufficient test time. Exact native gravity moment compensation now
biases the physical motor target; emergency interruption uses a bounded braking
target preserving the PD tracking lead. Test now measures all four cars' rates
and accelerations, including interruption. No blanket or sibling rerun.

Corrected native run uses separate `build-native2`, namespace
`fourthdimension_fd1801`, `-PfdValidation`; log `evidence/native-gravity-compensation.log`.
Owned first native PID825714 is still cleanly flushing its world in `build`;
thread dump shows RegionFile.writeHeader/pwrite with O_DSYNC file descriptors,
and write_bytes continues advancing. Do not kill or clean this build/world.
Set sync-chunk-writes=false before the second run and in prepared dedicated
server properties. First shell session26493; corrected session94815.

Prepared dedicated client/server configs under the planned run root. No graphical
client or Xvfb launched. Units were inactive/PID0, display317/ports25652–3 free;
recheck immediately before launch. Last host available memory30GiB, current
calling cgroup has memory.max=max. No secrets in evidence.


## Native motor diagnosis after second attempt

Second native failed empty rows at phi1.50108, while occupied row locked. Both
failed worlds continue clean saving (PIDs825714/833948); writes advance. Root
NVMe has high synchronous-write latency; no owned process killed. New build/run
root reserved on distinct persistent XFS `/mnt/NVME4/fourthdimension-runs/fd1801-flying-20261115`
(57GiB free before run), while all earlier worlds remain preserved on root.

Pinned Sable setMotor boolean means hasForceLimit; native JointMotor defaults to
AccelerationBased, not force gains. Removed inertia multiplication from gains;
use acceleration gains4000/400 and gravity moment divided by hinge inertia.
Interruption target now continues last issued reference, then decelerates;
physical lock retains measured settled angle instead of snapping the tolerated
endpoint error. Lock also waits for abs(omega)<.002. Targeted native check now
samples acceleration through lock engagement; unchanged contract unit pass reused.


## Native mechanics pass (current)

`native/posture-pass.log`: Java21 targeted Flying namespace passed. Four physical
cars at8 spacing, equal asymmetric/full load, separate retention/dispatch guards,
power-interrupted intermediate hold+snapshot+explicit resume, prone lock/dispatch,
return seated and open all pass. Peak measured rate=.2035863930650521rad/s and
acceleration=.2020144643029198rad/s², including lock and unlock. Final motor uses
acceleration gains4000/400, bounded reference.18/.15, prone command.015rad inside
hard range; physical lock captures settled actual angle while retaining distinct
motor preload in saved holdAngle. Run clean-saved/exited; no further mechanics
rerun merely for packaging. Existing1/1 contract unit evidence remains accepted.

This followed the identified first-unlock discontinuity: locking actual angle
must not replace the actuator preload. The first preload edit accidentally also
changed the mechanical lock limit; exact line corrected and pass above includes
explicit peak car/tick/omega/state/reference diagnostics. No threshold loosened.
Pinned API sources: Sable17d29f5b99d50e3cc13b536b23f9181ade99f14f joints.rs invokes
set_motor_position; Rapier38e92f117590862481a53df6fc69a5d893e29186 generic_joint.rs
uses MotorModel::AccelerationBased by default. Native boolean controls force cap.

Private GUI configs copied to newNVME4 root (secret server properties0600), world
outsidebuild. Old failed worlds still owned on root disk awaiting clean save.
No graphical runtime launched. Next: full external clearance+example motor setback,
then coherent packaged occupied run and actual mid-transition restart proof.


## Private client and hinge-origin correction

Owned NVME4 dedicated server/client started with Java21,6G client cap/1536M heap
confirmed by jcmd on actual Java PID852668 (service MainPID852666 was shell),
4G/2048M server. Display317/window2097160/854x480; normal input baseline proved
relative XTest camera yaw0→7.7° and reachable vanilla stone placement with fixed
rawMouseInput=true/default bindings. Input/screenshots in root/evidence. No Flying
boarding yet. `execute positioned 0 70 0 run fd demo flying_train` built disclosed
four-car route at graph origin near(0,73,12); native links8 and all seated-ready.
First carUUIDb1526ad7-f14f-4cea-ba27-1b16e0fee538; seatsworldX2,1,0,−1/Z18.5.
PlayerDev staged on platform(−4.5,69,18.5),yaw−90,pitch−1.1,empty hotbar9,F3on.

First image/read-only trace found child hip displaced−.375: creation overwrote
Sable COM rotationPoint with hinge. Fixed FlyingVehicle.create to retain actual
COM origin and initial COM world=desiredHip+Q*(COM−localHip). No live pose writes.
`native/hinge-coordinate-pass.log` now proves physical parent/child transformed
hinge gap <.00028 empty/loaded/prone, graph-relative Y−2.5000107, full same bounded
cycle peak speed.2035585/acceleration.2249731. Native run saved/exited. Other closed
family evidence unchanged. Whole-clearance native pass still applies.

First graphical client was stopped, server saved all dimensions and exited,
then Xvfb stopped (all units inactive/PID0). World retained outside build at
NVME4root/server/FlyingWorkshop. Repackaged hinge correction and restarting same
server; existing Sable reload resets COM correctly, must verify hinge readback.
No occupied/restart qualification claim yet. Existing controller script/run.json
retain exact resource ownership; check current service/window/PIDs before input.


## Occupied normal cycle passed; actual transition restart next

Owned GUI run verified ordinary right-click boarding at reachable Flying seat,
.8native rider load, tool air-use editor, separate normal actions34/36(body/legs),
31(prone),13(Dispatch),10(Stop),32(seated),35/37(open),Shift unload. Four-car
continuous loop includes climb, curves and full bank/inversion with phi retained;
minimum parent-upY−.9999999698, no fault. Retained client views, read-only trace
snapshots and editor acknowledgements in `occupied/`. Example and8links were
command-built fixtures; no construction credit. Client independent look and
seated/changeover/prone body views captured. Camera views cycle through five
pinned Sable modes: first→third back→sublevel→unlocked sublevel→third front→first.
F3 yaw is mounted-local; native local180 is this route's world+Z, not local0.

Normal Stop after all four entered dock captured first bearingZ18.807. Returned
SEATED_LOCKED, separate restraints open, then normal Shift put Dev at safe platform
(−3.5,69,18.5). All masses4.0, occupants empty, readytrue; client ridingfalse and
upright look quaternion restored. `occupied/artifact.json` identifies retained
cycle JAR. A handoff still awaits actual mid-transition restart; B/C crafting,
normal placement/Chain and guide/content reconciliation remain separate.

After this cycle client/server/Xvfb stopped; saved world preserved. Original root
failed native PIDs825714/833948 were still clean-saving last checked; inspect before
cleanup, neverkill. New package now builds saved actuator referenceAngle/Velocity
in FlyingState. Identified restart risk: existing WeakHashMap reference vanished,
so restarted moving row would drop its PD lead. New fields restore before fault
and bounded braking. This is the named remaining interrupted-restart criterion;
no unrelated family replay. Next reopen sameworld, normal board/close/prone, stop
server partway through actual transition, save/read exact state, restart/rejoin,
verify interrupted hold and explicit bounded resume/return then unload/camera.


## Actual occupied restart and isolated offline-origin correction

Main advanced through lead integration to `2b805fcb` (Flying production explicitly
in progress). Actual clean stop at occupied phi=.385209/rate=.172465 saved target,
restraints, rider assignment and motor reference=-.40725/-.18. Rejoin restored Dev
normally; explicit editor33 resumed to PRONE lock,32 returned SEATED,35/37 opened,
Shift unloaded and restored camera. Observed occupied peak rate=.203241 and
sampled acceleration=.248125. Snapshots and acknowledgements retained in restart/.
All three owned units stopped/inactive after this cycle; saved worlds retained.

One distinct failed invariant was found before rejoin: offline empty native mass4
had COM=(plotX,128.125,plotZ+.5) but logical origin retained saved occupied
COM=(plotX+.25,128.291667,plotZ+.5), leaving a .300484 hinge gap. On rejoin the
normal mass delta reconciled it. Lead transferred NativeLoading.java for the
narrow initialization repair. Pinned MergedMassTracker.uploadData skips equal
native mass/COM/inertia despite stale logical origin, so refreshing alone would
not repair it. First-sample/no-delta origin rebase now preserves every world-space
block: native position += Q*(newCOM-oldOrigin), origin=newCOM. Subsequent ticks,
loading quantities and families with actual initial deltas remain unchanged.

Replaying untouched `checkpoints/occupied-mid-transition` on owned server only;
original recovered/unloaded world preserved in `checkpoints/recovered-live-original`
and `checkpoints/recovered-unloaded`. Same NVME4 root/display/ports/units as above.
No full native or occupied loop replay. Pass condition: offline hinge/COM agree,
retained bounded interrupt state, then normal Dev rejoin retains that geometry.
Own docs/tests/resources/generator uncommitted; lead remains sole committer.


## A complete; content construction remaining

Narrow offline-origin replay passed: all offline hinge gaps .00010–.00012;
rejoined Dev mass4.8, exact COM/origin agreement, gap .000174. NativeLoading returned
to lead with compile and replay evidence. README.md maps complete A; handed to
lead and New-generation owner for integration. All three owned units inactive/PID0,
server saved. Source branch main2b805fcb pending lead handoff commit; only shared
production diff NativeLoading fix, own tests/docs/evidence/generator uncommitted.
No further A run. B/C resumes saved recovered/unloaded workshop, restored from
checkpoint to server/FlyingWorkshop after preserving origin-fixed-restored world.
Remaining: craft new Flying Track/Car recipes, normal track application/car placement/
Chain8, inventory/placed view and content reconciliation. Prior occupied views and
all mechanics acceptance carry forward unchanged. These are same task-owned
resources317/25652–3, currently stopped; startup requires headroom/ownership checks.
