# Powered Inverted Yaw — FD1901A–C, FD1901, FD1902

Assigned to `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da` after S2 D–F, on primary
`main` at `/home/braydon/Documents/Coasters: Fourth Dimension`. Initial HEAD
`cb46ea072d2c731948e4cac07e4d5350e269dbc5`; no new worktree/chat. Lead owns all
existing shared files, authority integration and central status/Git. New family
paths use `PoweredInvertedYaw*`, resources `powered_inverted_*`, its generator,
guide and this evidence only.

[Adopted FD1901S](../../../design/0.5/FD1901S-powered-inverted-yaw.md) requires its
own two-seat upright overhead carrier, one physical Y joint per car, 1–4 cars at
six blocks, dedicated powered overhead track, local scenery projection and
unwrapped full-turn yaw. It has no Flying posture joint or Suspended free swing.
Bounds: radius12, bank45°, slope30°, no inversion, travel8blocks/s and acceleration2,
yaw.6rad/s/.8rad/s², explicit winding/capture and dwell20–1200 ticks.

Ready inputs: accepted Fixed Inverted support/loading and FD1802A shared yaw math,
marker identities, winding/fallback, native physical pose/network and comfort.
Required input still pending at initial review: lead's qualified and integrated
FD1701A occupied authority handoff. The S2 governor provides a candidate reusable
standalone kinetic drive only after its actual native proof. No implementation
or acceptance against an unfinished authority is claimed here.

Distinct checks will cover upright per-car yaw + actual forward/reverse propulsion,
complete route drive coverage, speed/stop/dwell, yaw-changing Show, station unwind,
interrupted turn/power/controller loss and restart. The finite event list is
SECTION_ENTER, STOP_HELD, DWELL_COMPLETE, YAW_REACHED, RIDE_FAULT, DOCK_CAPTURED;
standalone bounded redstone/sound output, identities/epoch/sequence and dedup,
without scripts, external sound downloads or event-driven travel bypasses.

Normal construction/controls and finite FD916 content follow the early A occupied
handoff, reusing its evidence. Full FD1901 and FD1902 close cumulatively; Flying
is not replayed. No0.9,1.2, moving track or publication work is activated.
