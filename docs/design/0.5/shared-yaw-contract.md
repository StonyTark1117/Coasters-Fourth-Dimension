# Physical yaw shared by New-generation Flying and Powered Inverted

**Normative version 1, 2026-11-14 America/Phoenix.** FD1802S specifies this contract;
FD1802A implements/proves it; FD1901A consumes that integrated handoff. This is a
specification, not an additional package or present runtime feature. See
[conventions](specification-conventions.md), [Flying](FD1802S-new-generation-flying.md)
and [Powered Inverted](FD1901S-powered-inverted-yaw.md).

## Physical hierarchy and target meaning

Use `B * T(P) * Ry(psi) * Rx(phi) * T(seatOffset)`. B is the converted graph-origin design
frame (`Qnative*Ry(π)`), not the raw native quaternion; P is the specified pivot; **outer yaw precedes inner posture**. Matrix order
means the seat offset is pitched first, then yawed, then mounted at P. There are
real native yaw-deck and posture-carrier bodies/joints. Powered Inverted fixes
phi=0 and has no posture body. Never commute Rx/Ry or rotate a mounted rider again
in a renderer. Position, collision, mass, rider and camera origin use this hierarchy.

Yaw axis is always chassis +Y, even at bank/inversion. It is not world-up yaw and
is not the posture-rotated +Y axis (which would be longitudinal roll when prone).
Target-facing means physical **presentation heading**: seated front is carrier +Z;
in prone flight the head-first body axis is the pitched carrier +Y, also +Z in the
yaw deck. An authored posture-relative reference vector
`h(phi)=(0,sin(phi),cos(phi))` yields `Rx(phi)*h(phi)=(0,0,1)` through the transition.
This defines the same controllable heading continuously, including fully prone,
without the degenerate attempt to yaw a face-down normal about itself. Prone
rider model's neutral head frame counter-pitches `-phi` relative to the torso;
independent look follows it. The body still pitches physically, and yaw rotates
its head-first heading. This explicit original pose convention must be represented
in the occupied proof; camera-only aiming would fail.

For scenery target M, pivot world W, transform `d=B⁻¹(M-W)`, discard d.y and
compute `theta=atan2(d.x,d.z)`. If projected length<.05 blocks, projection is
**DEGENERATE**. Target height outside this plane is ignored, not secretly handled
by extra pitch or forced look. This rule works for seated/prone/banked/inverted
states. It aims the physical presentation heading at the projected target; no
claim of unrestricted 3D eye-ray tracking is made. Target visualization must show
the projection and the actual bounded heading so an operator sees this limitation.

## Finite modes, ranges and interpolation

Modes: `STATION_CAPTURE`, `PROFILE`, `TARGET`, `SHOW_STOP`, `BLEND`, `HOLD_FAULT`.
A program is a finite ordered set of named route intervals with revision, canonical
start/end distance, mode, entry/exit blend length, target binding or angle keys,
winding policy and bounds. Configuration only when empty, stopped and authorized;
a validated program may drive occupied transitions. Route geometry/fingerprint
changes invalidate dependent intervals before motion.

- Physical joint has full-turn capability; allowed stored yaw window is **[-8π,+8π]**
  (four turns either way). Full 360° movement is available within that window;
  winding count is real state, not discarded modulo2π. This multi-turn window is
  enforced by unwrapped-state supervision and bounded braking, not a wrapped native
  joint limit. Reaching a limit decelerates
  and reports `WINDING_LIMIT`; do not snap or silently unwind riders.
- Default max |omega|=.6 rad/s and |alpha|=.8 rad/s²; route/editor may lower but not
  exceed those bounds. Use the actual native angle and an acceleration-limited
  servo target; max request is not proof of physical tracking accuracy.
- Authored PROFILE keys store unwrapped radians at increasing canonical distances;
  a monotone-time interpolation evaluates distance without inferring turn count
  from a wrapped angle. Use cubic smoothstep between keys with zero endpoint slope,
  then enforce time-domain velocity/acceleration limits. If travel is too fast to
  meet a required downstream alignment, slow/hold travel before that boundary.
- Reversal retraces the same distance profile; it does not negate psi, reverse key
  order, or reset winding. TARGET recomputes from current pose in either travel
  direction. Stopped PROFILE holds its distance target and cannot advance via clock.
- TARGET resolves theta+2πk within bounds. `NEAREST` chooses minimum distance from
  current unwrapped angle; exact π ties choose positive rotation. `POSITIVE` and
  `NEGATIVE` restrict target travel direction; no legal candidate means unreachable.
  A saved winding branch is preferred while continuous; branch changes require a
  rate-bounded blend, never a modulo snap.
- BLEND entry/exit defaults to8 blocks, at least enough for configured rates and
  current speed. Blend from the measured entry angle/rate into the incoming mode;
  exiting targets blend toward the next profile or held angle. Reverse re-evaluates
  the interval's canonical side and retains continuity. Stop inside a blend holds
  distance progress; resume continues from saved measured state.
- SHOW_STOP is allowed only at a named stop whose travel hold is acknowledged by
  the operations authority. It may use a separate tick-based bounded yaw sequence
  or target tracking while distance remains constant. It cannot issue drive,
  change distance-profile progress or open restraints. Exit restores the route
  mode through a bounded blend after yaw completion and new travel authorization.
- Station unwinding is an explicit stopped, restrained, cleared-dock command to
  psi=0 within limits, followed by capture. It may make several turns at bounded
  speed. Do not call a 4π pose zero until the physical unwind completes.

## Faults, permissions and lifecycle

Missing/unloaded marker, wrong dimension, invalid marker ownership/binding,
degenerate projection, winding exhaustion or removed route each freezes the last
valid target and ramps omega to zero within .8 rad/s², preserving actual pose.
Expose a specific reason; deny dependent posture transitions, show completion,
dispatch or station release that requires alignment. After a target returns,
revalidate and blend from current pose; never catch up by jumping. A persistent
invalid target needs explicit operator retry/skip-to-declared-safe-mode. Skipping
may hold the ride; it cannot bypass a required posture/capture condition.
Loss of authority/power invokes HOLD_FAULT, zero new drive and retained restraints.

Only the ride owner/operator permission may edit profile, marker binding, bounds,
dwell or show program while empty/stopped. Server validates dimension, marker ID,
route, reachability, limits and controller epoch. Ordinary riders may change their
own look/comfort, not the ride program. Scene integrations submit validated requests
with ride ID, event/command ID, expected epoch and bound; duplicate/stale requests
are ignored. Client prediction cannot mutate persistent physical state.

Persist common identity/schema plus yaw/body hierarchy, P/axes and transform-order
version, actual unwrapped psi/omega, phi/phiDot where present, selected winding
branch, mode, previous/next interval, entry pose/rate, blend progress, revision,
marker UUID/dimension/binding, target-resolution status, show command/elapsed ticks,
stop/hold acknowledgment and authority epoch. Store tick and sequence in snapshots.
On restart rebuild native bodies/joints, restore measured state once, enter held
revalidation, then require explicit resume. Late join gets a complete snapshot;
interpolation cannot choose a shortest wrapped path and erase a requested turn.

## Required finite proof carried into existing acceptance

FD1802A proves projected target-facing through **both** seated and prone occupied
travel, with one combined transition at nonzero yaw, and a banked/inverted frame.
Measure actual body heading dot projected target≥cos(2°) once settled, hierarchy
and distinct pupil/player look input; rate/acceleration bounds apply during settling.
Show-stop yaw changes pose without changing canonical distance. Add profile
reversal/unwrapped crossing, one winding-limit fallback, one degenerate/missing
marker fallback and its smooth recovery, mid-transition restart and late join.
A second real client observes the new hierarchy/sync path once; FD1901 reuses it.

The prototype must compare body, seat/rider, renderer and camera origins, and
continuous **combined** posture+yaw sweep. Passing two independent-axis tests
alone is insufficient. Powered Inverted adds its travel/dwell/reverse authority
and finite scene-event cases without repeating Flying's common target math.
