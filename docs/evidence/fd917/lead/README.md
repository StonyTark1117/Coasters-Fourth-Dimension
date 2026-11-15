# FD917 shared track and Arrow mechanism

The shared renderer and connected Arrow assembly are implemented and have passed
bounded native and live geometry checks. **FD917 is still in progress:** Art and
Mechanisms retain their family/equipment reviews, Art has passed the new occupied Arrow cycle, and one final axle-interface view
is pending within its already assigned seat review. This record does
not close the whole package or start FD1000.

## Changed behavior

The original track now emits textured, world-lit vertices with normals and UVs.
Static geometry, normals, light positions and bounds are cached; current light is
sampled once per occupied block each frame. Valid track uses an opaque cutout
batch; invalid sections and placement previews keep their alpha blend. Tube UVs
follow cumulative rail length at 16 pixels/block, with long supports subdivided
at sprite boundaries. Frustum bounds supplement the existing distance limit.
Native/Track Styles functional ownership and the immediate Original fallback
remain intact. Art owns the affected optional-appearance view.

Arrow has paired control rollers, a sliding rack, a one-block-circumference
pinion, an 8:1 transmission, exposed bounded spring/clutch and telescopic CV shaft
to the **actual interpolated axle**. The [dimensioned contract](arrow-contract.md)
records the preserved profile mapping and the one new overtravel response.
Capture/acquisition opens the clutch; it does not hide a rail/axle mismatch behind
a target pose. Roller phase uses actual travel, while gear phase follows unwrapped
input and actual output. Old saves start with the clutch disengaged until actual
pose permits engagement. No body, seat, pivot, mass or native wheel was replaced.

Art identified the retained baked axle ahead of the actual hinge: its shaft center
is local z=-5.5/16, while the hinge is z=.5. The renderer now adds an actual-child
crank from the hinge through an inboard point x=.5-side*.36 to that shaft center.
The radial member is .07 block thick. This closes the geometric offset without
changing the pivot or seating; compilation passed, with the affected fit view
pending on Art's next necessary refresh. Prior rail-contact and occupied-cycle
evidence remains valid.

The mechanism stays within one car: its longitudinal extent is under one block
about the chassis station, versus the existing six-block linked spacing. It does
not attach to or replace native couplers. Existing native linked/capture authority
and curved train evidence remain applicable; there is no new inter-car linkage.
The native wheel-axis model reaches +/-1 block laterally; the rack's nearest face
is at 1.035 blocks. Native cart-bottom lateral bounds are +/-0.5625 block. These
bounds were read from pinned Coasters Simulated file8655742, not inferred from a
new manufacturer dimension. Final shells must retain this clearance.

## Accepted affected evidence

| Claim | Evidence and limit |
| --- | --- |
| Ordinary native tracking, capture, persistence and bounded overtravel | One Java21 targeted GameTest passed. 1,624 coupled carrier samples; ordinary peak lag .7160527 rad; capture opens the clutch and round-trips its saved state. Injected +8-radian input discontinuity after engagement releases/stops/captures with the named fault. [Summary](arrow-coupling-02-summary.txt). This injected discontinuity is not a normal program. |
| Full stroke and continuity | Candidate02 traversed the eight-edge loop forward and reverse, including curves, 30-degree banking and nonzero continuous joins. Input reached both +/-8pi and actual winding reached -25.13624 to25.13477 rad. Peak tread-contact error against the **tessellated** control-rail surface was .00115844 block, below1/64. Slow spring differential peaked at .166265 rad, below pi/8. [Machine-readable record](arrow-motion.json), [render summaries](arrow-render-summary.txt). |
| Nonzero capture and reload | The native axle settled at25.1322rad while the input remained non-neutral and the clutch stayed open. Candidate03 reopened the same saved state and F3+T retained textures/parts. [Captured bank view](views/capture.png), [current opaque/reload view](views/opaque-reload-capture.png). |
| Materials and world light | [Day](views/day.png), [midnight](views/midnight.png) and [local light](views/local-light.png) show authored surfaces responding beside native running gear. No shader or GPU-smoothness claim is made. |
| Placement preview | A disclosed item-selection/setup fixture showed textured alpha rails at the actual F3-named target anchor, without placing the connection. [Preview](views/textured-placement-preview.png). |
| Private input baseline | Same live client used default right/left bindings and Raw Input On. Relative XTest camera turn changed yaw90→94.65degrees; normal right-click visibly placed stone and produced the expected server broadcast. Later spectator teleports were explicitly view setup, not normal player-operation evidence. |
| Occupied shared contract | Art's current launch02 passed normal mount, Close, Dispatch, motion, Capture while restrained, Open and normal Shift unload. Its `art/arrow-occupied.md` record combines with the full-stroke contact observation here; only the subsequently added axle crank needs its fit view. |
| Runtime cleanup | Both lead runtime sets saved and stopped. All six client/server/Xvfb units are inactive, MainPID0. [Inventory](runtime-cleanup.txt). Art and Mechanisms remain independently owned. |

The selected [forward curve](views/forward-minimum.png), [forward bank](views/forward-bank.png),
[reverse bank](views/reverse-bank.png) and [reverse stroke](views/reverse-stroke.png)
views supplement the numeric contact record. Full100-second forward/reverse videos
and the short capture video remain in the persistent run root's `evidence/`, with
JSONL snapshots of actual chassis/carrier state and disclosed camera setup.

## Comparable renderer cost

Same copied world, camera `(6.5,100.5,22.5)` at yaw110/pitch0,854x480, render distance6,
pinned core dependencies, Java21, Ryzen9 7900X3D, llvmpipe LLVM23.1.4/Mesa26.2.6.
One old client/server pair used the retained 0.9 JAR; one current pair used the
opaque textured candidate. The camera position was read back and both screenshots
were inspected. After at least500 warmup geometry frames:

| Version | Window frames | Mean addon track-stage CPU time |
| --- | ---: | ---: |
| Retained 0.9, SHA787a01bf… | 4,363 | 6.5595ms |
| FD917 opaque textured, SHAcf125780… | 6,990 | 3.4069ms |

[Baseline counters](render-baseline.json), [current counters](render-current.json),
[old view](views/retained-track-baseline.png), [new view](views/production-track-current.png).
The current result is52% of the old cost in that view; frustum culling reduces
submitted rails from8 to2. This satisfies the shared-track working budget of not
exceeding its retained same-view stage cost. The initial all-translucent prototype
was corrected before acceptance; it need not be repeated.

Arrow's instrumented close/moving views averaged about1.6ms per stage with one
car/two assemblies, within a3ms component working allowance; final frustum culling
eliminates the offscreen assembly. This is a bounded component measurement, not
whole-frame, GPU or representative-park qualification. Live concurrent server
readings are not substitutes for the accepted fixed native performance workloads;
those unchanged gates remain closed. FD904B still owns its planned final scene,
percentiles, long run and whole-frame budgets.

## Reproducibility and retained state

Persistent root: `/mnt/NVME4/fourthdimension-runs/fd917-lead-20261115`.
The normal saved world is under `server/world`, outside build outputs. The old
comparison copied it to `comparison-baseline/server/world`; it never overwrote
the original. Frozen candidate directories include their exact JAR and launch
arguments. Logs preserve Java21 selection and the1536M client heap option; the
client/server scopes enforced6GiB/4GiB. Candidate03 peaks were approximately
2.57GB client and1.70GB server cgroup usage.

`gametest/Fd917VisualFixture` commands exist only with the validation JVM flag.
They set the isolated eight-edge smooth +/-8pi profile and use existing dispatch,
capture and motor operations. They do not establish player construction or normal
operation. Art's occupied view supplies that affected shared-contract observation. Final Java21
compilation passed in `compile-final-lead.log` and, after the axle crank addition,
`compile-axle-interface.log`.
No full family/profile release campaign was run or requested by this work.
