# FD1302A — qualified Axis mechanics handoff

Owner: lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`.
The integration commit containing this record is the Axis A checkpoint. Conventional
Flying implementation required by the shared registrations is included as an
in-progress implementation, without claiming its acceptance.

## Accepted contract

Variant `longitudinal_roll_v1` uses a native negative-Z rotary constraint to
implement design-positive longitudinal roll. One native two-seat child sits at
outboard pivot `(4,1.5,0)`, with fixed keel mass1 and .8 per rider. The original
fork clears the rotating disk; N-contact running gear and native Chain own travel.
One or two same-family cars use native eight-block links. FREE, DAMP and bounded
unwrapped DRIVE preserve phase, profile identity and actual load/inertia. Native
power loss stops/retains/damps; explicit reversal retraces the canonical profile.
Lift Lock requires settled upright entry and persists until the complete pod exits.
Capture is bounded and unloading requires the whole train stopped, aligned and
inside its declared level dock. Matching support, topology, full-sweep clearance,
controller, restraint and consist guards remain active.

## Cumulative evidence

- [Native mechanics record](README.md#native-mechanics-checkpoint--2026-11-15-0830-utc)
  establishes mass/inertia, continuous roll, bounded drive, power-loss retention,
  serialization, explicit reverse and returned capture/open. Pure axis/command
  tests pass; [physical hinge check](native/native-hinge.log) passes empty/loaded.
- Real player input placed both cars and bound their stations. Two normal Chain
  clicks selected and linked the bearings at eight blocks, confirmed by cleared
  item selection, rendered native link and [client train IDs](occupied/before-drive-repair.json).
- [Normal seat use](occupied/normal-board.png) boarded Dev, increasing native mass
  from4.0 to4.8. Normal Rotation Tool Close/Dispatch actions retained the rider and
  released both cars. The [closed occupied view](occupied/occupied-closed.png)
  shows the native player, cushion, harness, journal and fork together.
- The two-car train crossed the [30-degree bank](occupied/occupied-bank.png),
  continuously rolled through the programmed section, then entered DAMP/FREE.
  [Measured motion](occupied/motion-summary.json) and [received DRIVE state](occupied/occupied-drive.json)
  retain the asymmetric rider and native/snapshot longitudinal angle. Native
  physical pose plus ordinary player look remain the single camera/body composition.
  No new transport or remote rider ownership was introduced; accepted Wing/native
  remote-rider coverage and S1 axis-aware synchronization carry forward. This is
  one real client, not a claim of a new two-client run.
- [Occupied save/reopen](occupied/occupied-reload.json) retained both native link
  members, seat/rider assignment, restraint state and actual pose. The repaired
  example's continuous span is reported by the client. Explicit Resume passed the
  former banked-motor stall and completed the programmed turn/return.
- Return Lift Lock safely stopped the unsettled loaded pod. The precise recovery
  deadlock was fixed: retained alignment may occur at the declared level dock edge,
  but Ready/open still require both cars inside the two-block inset. [Open was
  refused at the edge](occupied/edge-open-rejected.png). Normal Resume advanced
  the aligned train under Lift Lock; normal Capture then reached
  [Ready](occupied/return-ready.json). Normal Open and Shift unloaded Dev at
  `(8.5015,83,64.5)` on the clear platform. [Final client state](occupied/normal-unload.json)
  has passenger=false, no assigned vehicle and nativeSeatOrientation=null; native
  occupancy cleared and mass returned to4.0.

The two example/recovery defects and their targeted passing replacements are
recorded in [the working evidence](README.md). No broader qualification reset is
required. Original .4/.5/.6 behavior remains accepted; B/C reuse this occupied
cycle and cover only missing recipe/track construction/content review.

## Runtime ownership and consumer handoff

A's native processes have saved and exited, including old root-device processes
829672/838261. Their worlds remain preserved. The active private graphical run is
explicitly retained by **the same named lead owner for FD1302B/C**, which is now
performing recipe/normal-track construction and finite asset review, not another
ride. Exact resources: `/mnt/NVME4/fourthdimension-runs/fd1302-axis-20261115`,
client/server game directories and saved world beneath that root, display`:316`,
ports25650/25651, services `fd1302-axis-20261115-{client,server,xvfb}.service`.
Current clientPID875781/serverPID875389/XvfbPID852429 belong to that run; verify live
PIDs before use. Client heap1536M/cap6GiB; server heap2048M/cap4GiB, Java21.
B/C explicitly accepts these exact resources and must save/stop/verify them before
its completion; this is active continuation, not an idle unowned runtime.

Roller Ball may consume this integrated axis-aware native rotary/load/capture,
consist and occupied ownership handoff where its own contract is compatible.
It must retain its distinct route, running gear, motion and clearance rules.
