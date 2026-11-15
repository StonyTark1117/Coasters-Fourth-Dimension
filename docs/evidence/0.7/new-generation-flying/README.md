# New-generation Flying — FD1802A–C / FD1802 / FD1803

## Current checkpoint — native prototype implementation

Owner `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`.
Conventional A prerequisite integrated at `5fe557f2`; latest observed main
`fe993053707c995d9614cfa97fc9783cf714e97a` also closes Conventional B/C.
The lead transferred NewGenerationFlying*/Yaw* classes/assets/tests, this evidence,
and FoundationSync, EditorNetwork, RotationEditorScreen, LocalRiderRenderMixin.
Lead retains other shared hooks and all Git integration. Conventional supplied the
additive FlyingRiderPose/FlyingSeatRiderMixin compound dispatch changes for our
checks. Unrelated `logs/` and other owners' work are preserved.

Java21 compile and five targeted `NewGenerationFlyingContractTest` checks pass.
The affected discrete-reference endpoint check also passes after correcting target
overshoot. Native compound topology/loading/motion/recovery is now accepted:
[summary](native/compound-summary.json), [native log](native/compound-pass.log).
Empty inner mass2, full two-seat mass3.6; maximum hinge gap.000223232. Actual yaw
speed/acceleration peaks.460901/.512877 and posture.167790/.152755 remain inside
required limits. This is native evidence, not occupied client acceptance.

Native01 exposed the massless no-collision standalone deck; enabling its authored
native collision fixed allocation. Native02 then exposed discrete-reference
overshoot into the seated hard endpoint; conservative discrete braking corrected
that condition. Native03 passed the affected case, saved all dimensions and exited.
Its world is retained outside build as `native-compound-pass-world`.
No client/Xvfb exists. The separate `fourthdimension_fd1802_route` namespace is
next for banked targets, winding, show and marker-fault semantics.

Persistent build/run/evidence root:
`/mnt/NVME4/fourthdimension-runs/fd1802-new-generation-20261115`.
Logs `evidence/contract-pass.log`, `evidence/native-compound-01.log`; accepted unit
XML under `build/test-results/test/`. First native namespace only
`fourthdimension_fd1802`, Java21; no previous family cases reopened.

[Preparation contract and finite verification mapping](preparation/HANDOFF.md)
records agreed Conventional pose seams, explicit transform conversion, player
controls, persistence/authority inputs and combined-clearance concerns. The
reproducible [reference calculations](preparation/reference.py) produce
[results](preparation/report.json), a simultaneous nonzero-yaw posture profile
and [envelope drawing](preparation/combined-envelope.svg). These are specification
inputs, not native or occupied acceptance.

Next: qualify actual compound native mass/hinges/dynamics, then the early real
occupied both-posture targeting and combined transition handoff. Finish route
profile/show/fault/persistence/late-join and normal construction/content requirements
cumulatively. FD1803 reconciles accepted Conventional plus New-generation without
an aggregate replay. No 0.8 work is activated.


## First occupied client checkpoint — 2026-11-15

Frozen packaged artifact `54e25052afec835c481928131320855b8bcadb577fb7cf7648ca600bf08f9aff`,
private display :318, game/RCON 25654/25655, Java 21, actual client heap 1536 MiB
and 6 GiB service, server heap 2048 MiB and 4 GiB service. Owner is
`01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`. Root:
`/mnt/NVME4/fourthdimension-runs/fd1802-new-generation-20261115`.

Normal camera turn and vanilla stone placement passed with default bindings and
Raw Input enabled. Command-built example and disclosed approach teleport do not
claim player construction. A temporary front approach pad entered the reserved
lookahead volume and correctly produced retained `COMBINED_SWEEP_OBSTRUCTED`;
it was removed. Normal Rotation Tool Retry, explicit Unwind, separate body and
leg release then succeeded. A clear diagonal approach from the existing side
platform targeted the seat pan. Normal empty-hand right-click boarded Dev;
server `/fd status` recorded one occupant. Independent rider look and seated
Rotation Tool access worked; normal body and leg closure both acknowledged and
visibly changed their separate meshes.

First Dispatch was acknowledged, but the inherited Conventional example motor
at offset three obstructed the wider compound reservation as lookahead expanded.
This is an unresolved example clearance failure, not an accepted ride. The
corrected example moves full motors to offset four and extends only the native
shaft; its narrow port allowance must match. Saved occupied fault checkpoint:
`occupied-station-checkpoint`; input/console records, screenshots and logs are
under `evidence`. All three owned services were stopped and verified inactive,
MainPID=0, after clean server save and exit. Continue the same run/world after
the targeted example/port correction; unchanged native proofs remain accepted.


### Continued occupied evidence and current native boundary diagnosis

Artifact02 `8d628b64d3c923edf318c9c6f1aebeed590f4df469abf62fd30eccbcddc42fc7`
corrected the wider drive-port geometry. All fifteen powered anchors were checked
for motor offset4 and native shaft offset3 (`client/drive-offset-verification.json`).
The occupied car then travelled seated at yaw90°, changed posture and yaw together,
and reached prone target travel through the bank/inversion. Real Observer on owned
:319 joined later and saw all three native bodies and Dev aligned to the prone
seat. Both screens agreed at yaw530.1°/1.47 turns and posture89.1°, with the same
retained target-entry blend. Retained views and native saved states are in `client/`.
The comfort toggle changed only the rider camera frame and mouse look remained
responsive; the car/body hierarchy stayed native.

A clean occupied partial-blend restart retained the deck/inner UUIDs, marker,
section revision/fingerprint, entry distance and blend progress. Actual yaw differed
by -0.00000566 radians and posture by -0.00031655 radians; one real rider restored.
The required RESTART_REVALIDATION hold retained both restraints. Normal Retry and
Dispatch resumed. A stopped physics-position fluctuation changed stored direction;
the new direction guard only updates during authorized moving travel on the same
canonical section. This correction still needs its targeted check.

Continuation stalled near the final roll edge `(0,27,204)->(0,27,236)`, at
canonical distance31.3118/32 and native center about(.5127,27.62,235.8). An initial
inference that this was a completed alignment hold was **incorrect**: saved state
shows `stopping=false`, travel authorized, no fault, and target error about.04°.
The saved world is `native-boundary-stall-checkpoint`. Artifact03 adds read-only
`/fd ng_inspect` diagnostics: current/adjacent rails all pass rejection, safety is
null, and no deck box overlaps world collision shapes at the stopped checkpoint.
The source also corrects a separately identified guard-owned exit hold and endpoint
qualification window; those changes are not yet accepted merely by compiling.
No whole native/body/late-join replay is required. Boundary continuation and the
new guard behavior remain targeted work. Observer client/display are inactive;
main server/client/display remain owned only during this active diagnosis.

### Native remaining-case closure and fresh example correction

The four-car [consist check](native/consist-pass.log) passes native eight-block
links, fifth-car rejection, actual prone endpoint before leaving the transition,
whole-consist holds, forward distance79.215607 to reverse45.960052 retracing the
same authored yaw/posture profile, and all cars seated/unwound at the dock.
The [failure check](native/failure-pass.log) passes distinct unloaded, unauthorized,
degenerate, lost-controller-authority and unreachable-winding fallback with bounded
actual motion, retained restraints, denied travel and explicit recovery.
The [Show departure check](native/show-exit-pass.log) passes unfinished-show denial,
actual stopped Show, explicit departure preserving the completed heading, next
interval travel, and immediate native-drive denial when the yaw deck lookup fails.
All three targeted native runs passed once and saved/exited cleanly.

The boundary diagnosis was original example geometry: raw worldCenterBase puts the
upright curve at anchorY+.25, whereas native placement uses the banked lifted
endpoint (upright anchorY+.75). The stalled cart bottom intersected the upright
half-height anchor; the deck and seat body did not. Conventional supplied an
additive FlyingExamples overload using the native lifted endpoint once per hub,
before curves, fingerprints and Chain spans. Existing example defaults and all
previous accepted worlds remain unchanged. NG opts into native endpoints in a
fresh `NewGenerationNativeEndpoints` world on artifact05. The old occupied boundary
world remains saved; its force-load tickets were removed and all units stopped
before the fresh launch. The new first edge includes seated sideways and seated
scenery-target sections. Fresh occupied validation is in progress on owned :318,
25654/25655 with the same Java21/1536M heap/6G client scope and2048M/4G server.


### Occupied both-posture handoff and loaded Show correction

Artifact06 closes normal occupied seated scenery targeting (actual heading error
0.52°, seated lock), simultaneous yaw/posture changeover, prone targeting through
the declared full roll, and the original stalled boundary. At10:47:27 UTC the
native chassis was beyond anchor236 atZ252.9 with native drive authorized and
no safety fault; actual prone89.5° and target error1.43°. The original endpoint
failure is closed by that targeted check. The screenshots and saved state under
`client/` distinguish the seated target view from the later combined transition.

A fresh fixture originally used a distant shared marker for the new seated target
and correctly held MARKER_UNLOADED. The authored example now places each target
marker near its own section; it needs no remote marker chunk ticket.

The next occupied Show failure was diagnosed separately. Moving the Show eight
blocks inside its span removed anchor overlaps, but SHOW_HOLD_LOST still occurred
under one off-center real rider. The loaded native reproducer confirmed the same
failure on a clear straight: drift0.0582904 and actualyaw4.141069 before retained
fault. Shared braking subtracts inner-body linear velocity even though that COM
must translate during compound yaw. The NG Show now creates a native world-to-
chassis constraint only after a stopped, retained acknowledgement, keeps its
measured pose, allows legitimate inner COM translation, and releases the hold on
explicit departure. No broad shared brake or collision change was made.

Fresh artifact07 used disclosed `tick sprint4000` only to advance the already-
accepted approach to the corrected Show, then verified ordinary20tps before normal
Show input. It does not replace artifact06's ordinary occupied travel evidence.
After the repeated Show fault all three owned services saved/stopped and were
verified inactive/PID0; all worlds remain preserved for diagnosis.

The corrected loaded native Show passes [the final targeted test](native/loaded-show-pass.log):
zero canonical drift, full turn with one off-center prone rider, actual yaw peak
0.448125rad/s and acceleration0.137430rad/s², prone lock preserved, incomplete-show
and unsettled departure denied, explicit departure reaches the next interval.
Artifact08 then resumed the saved real occupied clear-Show state. Normal Retry,
named Start and ordinary20tps Show reached540.0° with PRONE lock, both restraints
closed, no fault, and exactly8.746704059566428 canonical distance before/after.
Normal Dispatch released the native hold and advanced along the same curve.
See `client/show-completion.json`, before/after saves and occupied Show screenshot.

The resumed return's already-accepted constant-prone section used disclosed
`tick sprint2000` for fixture positioning, with `tick query` confirming ordinary20tps
before observing the return changeover. Normal controls showed decreasing posture,
then seated0.4° endpoint lock with yaw179.8° and closed restraints. No new graphics,
profile, or earlier lifecycle matrix was launched.

Normal-content preparation found the NG purple-dye track recipe duplicated older
track identities. Only NG changes to unique magenta dye; generator/guide match.
A world datapack override copies the exact production recipe for normal crafting
without repackaging mechanics. Other families' recipes are unchanged/outside scope.
