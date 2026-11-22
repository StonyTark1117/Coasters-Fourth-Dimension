# S1/S2 stationary mounting sleeve — affected client check

**P2 affected checks passed and runtime cleaned up, 2026-11-22.** Owner `/root/fd13_spinning_check`, primary `main`
from `b5e78f31`; structure implementation owner `/root/fd13_structure_reconcile`.
The changed renderer is pinned in [inputs.sha256](inputs.sha256).

P2 adds only the parent-owned annular sleeve connecting the S1/S2 controller to
the fixed race. The native Y pivot, child turntable, seats, restraints and
physics are unchanged. [Geometry audit](geometry.txt) establishes controller/race
overlap and .01-block radial clearance from the .17-radius receiver.

Private host run: `/mnt/NVME4/fourthdimension-runs/fd13-spinning-20261122`,
owner `fd13-spinning-check`, Xvfb `:343`, loopback ports `25733/25734`,
world `SpinningMount`. Java 21.0.13.1, client 1536M heap in a 6GiB unit; server
2048M heap in a separate 4GiB unit. Gradle preparation ran outside those units,
offline with a fresh daemon. Host headroom was 43GiB available; target NVMe
XFS had 163GiB free. No other agent runtime was controlled.

The original demo commands supplied S1 at source `(100,100,100)` and S2 at
`(350,100,100)`. These are disclosed setup, not player construction.
A camera turn and reachable vanilla lever right-click were verified in the
same connected client before mod input. Default bindings/raw input remained.
Native-body coordinate aiming used disclosed player teleports with Sable
`centered_in_sub_level` and `out_sub_level`, followed by an F3 seat hit check.

## S1

Car `bebcd31b-b7c6-46ff-8910-54e50b2d87bd`, child
`591931e1-cbff-48a2-a774-a4eae880c3f1`. Normal empty-hand use mounted the
near-side seat; the normal Rotation Tool editor closed restraints and dispatched.
The live state reported one rider, no fault and released lift lock, with native
relative yaw 0.502195 and 0.646201 radians. Changed fixed mounting geometry
remained joined and clear in the inspected empty and occupied views.

- [Empty](s1-empty.png)
- [Occupied open](s1-occupied-open.png)
- [Occupied free yaw](s1-occupied-yaw.png)

This is the affected mount/rotation check only; cumulative prior ride and
construction acceptance is retained, not rerun or replaced.

S1 returned to its documented lift-entry hold. Normal Capture cleared that hold,
aligned the carrier, then normal Open and Shift unloaded it (zero riders).


## S2

Car `8a806bbd-00e5-4318-bb4d-cd78360798ff`, child
`165fb464-2c9d-4184-bc01-57f4f8dd63d8`. The demo governor had not bound while
its chunks were unloaded; after loading, normal reachable empty-hand use bound
it to the empty captured car. This repaired setup, not the production renderer.
Normal empty-hand seat use mounted one real player. Normal Rotation Tool Capture
acknowledged the governor hold, then Close and Dispatch launched the car.
Live inspection reported relative yaw 0.152874 on the launch and -2.232231
radians during track roll, one rider, restraints closed and no fault.

- [Empty](s2-empty.png)
- [Occupied open](s2-occupied-open.png)
- [Occupied launch yaw](s2-occupied-yaw.png)
- [Occupied yaw during roll](s2-occupied-roll.png)

The stationary mounting sleeve remains visually continuous with the chassis and
fixed race in these views, while the seat body rotates independently. No new
gross interference was observed. The exact .01 radial clearance is established
by the source geometry audit, not inferred from pixel measurements. Parent
transform ownership is explicit in the renderer and the changed sleeve is
annular about the unchanged native Y axis. No physics or seat transform was
modified for this check.


## Evidence interpretation

S1/S2 use the same changed sleeve branch. Each was inspected because both are
explicitly affected; S2 retains its additional rotor ring and roll capability.
Screenshots, native angle samples and unchanged parent/child ownership combine
with the audit to cover P2. Screenshots do not certify exact subpixel clearance.
No broad family, profile, or physics requalification is claimed.

The first S1 aim used a relative offset as an absolute Sable plot coordinate and
therefore performed no teleport. The corrected `centered_in_sub_level` command
resolved the live plot; a far-side aim hit the controller, then one calculated
near-side correction hit the proper seat. No failed interaction was retried.
S2 used that proven targeting procedure directly.


## Completion and cleanup

S2 returned, was captured within the level dock, became latched, then normal
Open/Shift unloaded it. A bounded telemetry watcher sent exactly one normal
Capture click at observed center `(350.5,109.1,130.6)`; the first return had
already passed while evidence was inspected. This extra circulation was cleanup,
not additional acceptance. [Return observations](s2-return-capture.jsonl) preserve
the triggering state. Final S1/S2 observations show zero riders and open restraints.

The client disconnected normally and Quit Game exited it. The server flushed
and saved all dimensions, then exited from `stop`. Only this run's Xvfb unit
was stopped afterward. [Cleanup](cleanup.txt) confirms all three units inactive
with MainPID 0. The saved world and all raw screenshots remain at the persistent
run root. No world, source, historical worktree or other owner's runtime was
removed. Observed peak memory during the run was 2,637,291,520 bytes client and
2,029,166,592 bytes server, below their respective 6GiB/4GiB limits.

[Native state samples](state-samples.json), [server log](server.log),
[client log](client.log), [normal input record](input.jsonl),
[disclosed setup and queries](console.jsonl), and [Java21 build](build.log) retain
the exact check. The final source SHA remained
`bc47271faefa6376e184c76d55f7e657086fe176c5f959ab96a2228cfd85b49a`.

Only this evidence directory was written by the verification owner. Shared
production changes and central status remain with their assigned owners.
