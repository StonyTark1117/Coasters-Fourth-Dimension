# Powered Inverted Yaw — qualified

**FD1901A–C, FD1901 and FD1902 are complete, 2026-11-15.** Native one/four-car
composition and actual drive/controller loss combine with the normal crafted,
occupied workflow and interrupted-show process restart below. All owned runtimes
are stopped. Lead owns central status and integration; no0.9/1.2 work is activated.

Owner `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`, primary `main` checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`. No new branch/worktree.
Lead integrated initial Powered classes/shared hooks at `a3d6a2ef`; the final
family-only recovery guard is covered by `native-loss-03` and launch02 below.

## Scope and carried-forward contracts

The [adopted specification](../../../design/0.5/FD1901S-powered-inverted-yaw.md)
is implemented as `powered_inverted/upright_yaw_v1`: two upright seats, one
independent native Y carrier per car, pivot -3.3125 in the native coordinate
convention, one to four cars at six-block spacing. Dedicated overhead N rail
requires a complete, unbranched, powered circuit. It has no posture joint or
free swing. Actual Chain force reads a supplied Create governor on every section;
one FD1701A master controls their fresh leases. No optional provider is required.

Fixed Inverted support/loading, FD1802A yaw projection/marker identity/winding,
native body/seat synchronization and rider comfort, FD1701A ownership and S2's
real stress-supplied transmission are accepted inputs. Their unchanged shared
math, networking, fallback and restraint interfaces carry forward. The distinct
upright composition is proven here with one real client; no new synchronization
path requires another two-client replay. Closed siblings and earlier milestones
stay closed.

## Native mechanics

Each targeted namespace reports **one required test passed**. Tests use actual
native bodies, alternating one/two pig masses, real stress-supplied governors,
and complete eight-section Chain coverage, without an optional drive addon.

| Case | Evidence and result |
| --- | --- |
| Single-car full cycle | [native-single-01](native-single-01.log): physical full-turn PROFILE, own-pivot TARGET, held yaw-changing SHOW, actual reverse and zero-winding station capture/Open. Peak travel3.99225b/s, yaw.53435rad/s, actual source ramp2.000003b/s² with floating-point tolerance; monotonic controller sequence13. |
| Four-car full cycle | [native-train-01](native-train-01.log): each independent carrier completes the same operations. Peak travel3.99370b/s, yaw.53913rad/s; independent target spread.54833rad; source ramp2.000003b/s²; sequence46. Shared lead distance does not substitute for trailing pivot position. |
| Power/controller loss and recovery | [native-loss-03](native-loss-03.log): actual motor removal during yaw/travel stops the car and retains nonzero winding/restraints/authority. Power restoration neither launches nor replays scene events. Explicit recovery resumes after fresh stopped samples. Later held recovery ignores completed-show history, honors selected direction and releases maintenance; actual controller removal stops/retains again. |

The first loss test caught immediate release after authority acknowledgment reset
its five stopped samples. Runtime now keeps an explicit pending release only in
live memory and waits six ticks before guarded release; new fault/capture cancels
it and reload cannot preserve launch intent. Loss02 passed that fix. Later
inspection of an off-dock stop found stale completed-show metadata could be reused
by ordinary Resume. Runtime now restores a show only from current HELD_DWELL or
an actual interrupted-show fault, releases maintenance after revalidation, and
also observes fresh stopped samples when a resumed show's remaining dwell is zero.
Loss03 includes the reproduced stale-history precondition and passes. These
recovery-only changes do not invalidate single/four-car normal cycle evidence.
Failed01 and all successful worlds/logs remain preserved; no full sweep was run.

## Normal construction, controls and occupied operation

The run supplied upstream ingredients/unlocks, Rotation Tool, blank native anchors,
utility platforms/viewpoints, and the track-only example with supplied Create
motors. Commands did not craft the three new outputs, place/bind the crafted car
or governor, author the demonstrated form, board, dispatch, resume, capture or
unload. One nearby fixture station was repositioned within its normal twelve-block
binding radius before normal use; this was disclosed setup, not a binding bypass.

| Required workflow | Evidence |
| --- | --- |
| Survival recipes | [Track](track-recipe.png), [car](car-recipe.png), [governor](governor-recipe.png), [inventory](crafted-inventory.png). Fourteen iron, four redstone, cyan dye, two Cyan Seats, Bearing, native Cart, two Shafts and Clutch were consumed into car1/governor1/track15; the car used one of sixteen crafted tracks. |
| Paid track | Two normal endpoint clicks connected supplied blank anchors eight blocks apart, consuming two matching tracks, 15→13. [Connection](paid-track.png), [construction save](constructed.json). |
| Car/station | [Normal placement](normal-car-placement.png) consumed the crafted car. [Normal station use](normal-station-binding.png) bound its stopped, empty native body. |
| Supplied governor | [Normal placement](normal-governor-placement.png) consumed the crafted governor, facing WEST into the shaft. [Empty-hand binding](normal-governor-binding.png) discovered all eight route governors. NBT reports real input-128RPM, stress1024 and output0 while held. |
| Program authoring | Rotation Tool on the actual anchor opened the sixteen-field form. [Normal Save](saved-program.png) set section7's blend to2, preserved bounded yaw/speed fields and advanced its revision; action70 succeeded. |
| Board/operate | [Normal seat use](normal-board.png) mounted Dev. Close restraints and Dispatch succeeded through the UI. Actual travelling yaw exceeded one turn, tracked scenery, stopped at the show and resumed/reversed through normal controls. [Show view](occupied-show.png), [reverse view](occupied-reverse.png), [occupied trace](occupied-trace.jsonl), [resumed trace](resumed-trace.jsonl). |
| Capture/unload | [Normal Capture](normal-capture.png) on the qualified dock decelerated and unwound. [Open](open-restraints.png) used the refreshed post-capture revision; [Shift](normal-unload.png) unloaded to `(103.5,95,127.5)` on the dock platform. [Final save](final-empty.json): same vehicle/carrier, empty rider lists, restraints open, DOCK_OPEN, ready, fault empty, held authority, angle≈.000012rad. |

The first manual return Capture arrived after the dock and safely stopped outside
it without offering unload. This was timing, not failed mouse input. The same
saved car was recovered forward using normal Set forward and Explicit resume.
For the final approach, the already-open Capture menu received one mouse click
when read-only saved state confirmed the dock edge at distance19.63. The
[click record](final-capture-click.log) and [return trace](final-return-trace.jsonl)
separate that timing automation from the real normal input/handler result. No
command teleported or operated the vehicle. Accepted show/travel was not replayed.

## Interrupted show and finite event output

During the actual occupied show, the dedicated server saved and exited, then a
new Java process loaded that same world. [Stopped state](stopped-show.json) records
show tick124, remaining dwell276 and unwrapped angle7.256rad. On reload, bounded
yaw braking settled at7.532rad rather than erasing the winding. [Rejoined state](rejoined-held-show.json)
and [normal UI](restored-show-controls.png) show the same car/carrier and rider,
FAULT/HOLD_FAULT, suspended held authority, zero travel/output, and exactly276
dwell ticks still pending. The rider returned through the accepted native seat
restoration path. Normal Explicit resume continued those saved show ticks to400,
reached the physical target and reversed. It did not restart or skip the show.

[Server events](server-events.log) retain ride/car/event identities, tick, epoch,
monotonic controller/car sequences and the finite SECTION_ENTER, STOP_HELD,
DWELL_COMPLETE, YAW_REACHED, RIDE_FAULT and DOCK_CAPTURED transitions. Restart
emits the new recovery fault, not past show/audio events. A real adjacent redstone
lamp changed off→on→off at final capture: [pulse readback](scene-pulse.jsonl).
The governor's four-tick output can extend on overlapping fresh transitions;
the lamp adds its vanilla off delay. It is not a latched or replayed signal.
[Normal empty-ride chime toggle](chime-control.png) succeeded and the bound master
saved `sound:1b`; the implementation uses the registered note-block chime and
contains no script/command/external-audio execution or travel/restraint consumer.
The chime toggle was verified by normal input and authoritative state, not an
auditory assertion. Shared registered sound dispatch is unchanged.

## Finite content review

The original cyan/metal/black/amber resources cover track/car/governor inventory
models, dedicated overhead rail presentation, compact controller collar, slender
spindle, two seat backs/cushions, raised/open and closed restraints, foot supports,
yaw bearing and governor output cue. Recipe/inventory, placed-world and operating
views above show readable textures, no missing-texture surfaces, upright rider
alignment, cleared spindle/track and stable full-turn geometry. The occupied
reverse image exposes both seats, harnesses, rider and overhead assembly.
Generated element bounds are checked by the generator; accepted native clearance
and seat/pivot geometry did not change during content work.

The [player guide](../../../POWERED-INVERTED-YAW.md) covers ingredients, paid
anchors, complete drive coverage, stress supply, ownership/binding, four-car
spacing, sixteen-field yaw/speed/stop/dwell authoring, held scenes, direction,
recovery, capture/unload and finite outputs. The supplied examples expose
track-only/one/four-car configurations. These accepted assets/views feed later
FD916 reconciliation without activating that milestone or a second art campaign.

## Provenance and cleanup

Persistent XFS run root:
`/mnt/NVME4/fourthdimension-runs/fd1901-powered-yaw-20261115`.
Owned display318, game/RCON25654/25655, unit prefix`fd1901-powered-yaw-20261115`.
Java/Gradle21; client heap1536M, Minecraft-only6GiB service; server heap2048M/4GiB.
Host34GiB available before final launch; selected filesystem54GiB free.
[Measured final memory](final-memory.txt) stays within the owned limits.

Launch01 JAR SHA256 `f2c76c2c4d4cd778966a83af72e24202090cd85073c33ecd23448de5e7ea5514`
proves crafting/construction, occupied yaw/show/reverse and actual interrupted-show
restart. Launch02 `39049422d1ff2fe6d1000e73c0f9bd58a5358b5af31dcf9a4cc93579fda509b1`
adds the narrowly tested recovery guard and proves the same-car return/unload.
New shared CTNB code is limited to certified Wing/Arrow; Powered remains native
Chain. Commit/JAR identity alone does not invalidate those earlier observations.

Full raw logs/screens/input remain at the run root. Included [input](input.jsonl)
and [console](console.jsonl) records distinguish normal interactions from setup.
Saved worlds outside build directories: `native-single-01-world`,
`native-train-01-world`, `native-loss-01-world` through`native-loss-03-world`,
`interrupted-show-world`, `offdock-held-world` and`final-empty-world`.
The server's live directory also retains the final empty world. At13:55:55UTC,
client exited, server saved all dimensions and exited, then owned Xvfb stopped.
[Cleanup](cleanup.txt): all three units inactive, MainPID0. No runtime handoff or
unstopped process remains. FD1901A/B/C and both aggregate gates are qualified for
lead integration; no required Powered case remains open.
