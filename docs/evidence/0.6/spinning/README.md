# S1 Spinning — FD1501A–C

**FD1501A mechanics and occupied handoff complete, 2026-11-15 UTC.**
FD1501B/C remain in progress: normal construction, the corrected example approach,
remaining content views and the distinct power-loss/resume case are next. Owner:
`01a1dd1d-1468-7f81-8f31-5532ad7a178d`. Primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`; input HEAD
`d73d43b820b725fc9648c6a2daa9348dead16b22`. Preserve unrelated `logs/`.
The current queue records the integrated commit and shared-file ownership transfer.

## Integrated mechanics

One native local-Y joint, four exact fractional seats, native .8 rider masses,
measured-inertia passive damping, bounded capture and five distinct server-tick
latch guards implement `spinning / conventional_v1`. Track eligibility checks the
N contact/mount version, full-turn reservation, radius/bank/slope limits and one-car
restriction. Powered native Chain supplies travel. Lift locks remain relative to
the chassis and release after the full rear reservation clears the section.
Faults brake travel and retain restraints; restart and resume require explicit
operator action. Optional Engineered and ordinary recovery are not certified for S1.

Shared seams: `Family.axis()` / `CarrierAxis` own joint creation, native angle and
omega measurement and restore. Schema7 preserves the axis, fractional child pivots,
seat offsets, winding, capture/lift state and dock identity, while accepting existing
schema1–6 saves. Default X behavior and the original servo remain unchanged;
`foundationFixture()` preserves original five-family fixture scope. `NativeTrackFit`
separates contact/mount identity from appearance and includes longitudinal extent.

## Native and unit evidence

- [First native run](native/first-native.log): one 850-tick GameTest passed. Empty
  mass8.0; two equal riders add1.6 exactly. Loaded Y pivot inertia8.59533. The same
  chassis acceleration produces balanced yaw−0.00007569 versus asymmetric
  yaw−0.10425042 radians. Native free Y rotation, moving-board denial,
  stop/capture/open, missing-dock unload denial and schema7 round-trip pass.
- [Targeted unit run](native/unit.log): twelve math tests pass, covering X/Y twist
  and winding, original X/default behavior, bounded capture speed/acceleration and
  deceleration, distinct server-tick latching and functional fit dimensions.
- The first native winding probe deliberately changed the stored unwrapped branch;
  the current fixture confines that probe to its NBT copy. No natural rotation claim
  depends on the injected winding. That test-only correction does not reopen native
  dynamics. Explicit quaternion components avoid the cached JOML `rotationX`
  constructor's incorrect reference output in the unit fixture.
- The below-cushion plate correction preserves mass and X/Z mass distribution,
  hence Y inertia/loading evidence. Its affected geometry is shown in the corrected
  occupied view. [Handoff compilation](occupied/handoff-build.log) passes.

The native run at
`/home/braydon/.local/state/fourthdimension-runs/fd1501-native-20261114` completed its
slow save at04:58 UTC; all dimensions saved, BUILD SUCCESSFUL, PID642557 exited.
Its world remains in `build/runs/gametest/world`. No display was used.

## Connected occupied handoff

The Java21 standalone packaged run uses Create6.0.10-280, Coasters0.1.5 file8655742,
Sable2.0.3 and NeoForge21.1.248, unchanged from accepted0.5. Corrected occupied JAR
SHA256 `22698997bb085de0c39ba4117becf3f3e5b20155149e976dc7a11c40f7cadcb4`.
Setup `/fd demo spinning` at `(0,90,0)` supplied the disclosed track/car fixture;
it is not construction proof. Chassis `e0a60cf1-78b9-4761-97a0-9358c6b0f937`,
carrier `d19268d0-09ee-4f31-acc4-9884d6fe62a9`, player Dev. No helper mounted the player.

1. Retained raw input/default bindings passed camera turning and vanilla lever use
   in the same client. After the geometry correction, the actual ray hit the
   reachable unrestrained Spinning seat. One normal right-click mounted Dev through
   Create's seat path at05:05:43 UTC; [state](occupied/normal-board.json),
   [view](occupied/normal-board.png), [server log](occupied/server.log).
2. The normal Rotation Tool opened the editor. Close restraints then Dispatch
   changed revisions1/2, retained exactly one occupant and disabled boarding.
   [Lift state](occupied/dispatched-lift.json) shows liftHeld=true with native travel.
3. After leaving that section, [banked-curve state](occupied/occupied-curve.json)
   shows liftHeld=false and free yaw−.509 radians. The subsequent
   [free state](occupied/occupied-free.json) records yaw−2.824 and omega.206rad/s.
   [Occupied motion](occupied/occupied-motion.png) shows rider, restraints and the
   physical rotating assembly together. Client native/snapshot Y angles agree in
   the retained [client log](occupied/client.log).
4. Returning to the lift with unsettled spin invoked the safe lift-entry stop;
   [state](occupied/lift-entry-safe-stop.json) retains the occupant/restraints and
   denies unload. This exposed an example layout error: its dock was18 blocks past
   entry. The source now puts the dock at the entrance and starts the car6 blocks
   into the straight, with reachable approach platforms outside the reservation.
   In the retained run, a disclosed setup command placed that corrected dock at
   `(-3,94,36)`. A measured actual block ray and normal empty-hand right-click bound
   it to the existing car. This fixture correction earns no normal-placement credit.
5. Normal Capture cleared the settled stop and aligned from approximately−2 radians
   to the nearest dock orientation. [Capture states](occupied/occupied-capture.json)
   end Ready/latched at angle.0047 and omega−.0093, with restraints retained;
   [Ready view](occupied/captured-ready.png). Normal Open restraints succeeded.
6. Normal Shift at05:12:22 UTC unloaded Dev. [Final state](occupied/normal-unload.json)
   has passenger=false, no assigned vehicle and nativeSeatOrientation=null;
   [view](occupied/normal-unload.png). The server records one mount and one dismount.

The native Create/Sable seat entity, tracking packets, FoundationSync and remote
pose ownership are unchanged; accepted [Wing occupied/multiplayer evidence](../../0.5/wing/README.md)
and original native rider synchronization carry forward. This handoff supplies the
new axis/fractional-seat local rider/camera proof; it does not claim another observer run.
Original0.4/0.5 cases remain closed.

## Runtime preservation and cleanup

Owned root `/home/braydon/.local/state/fourthdimension-runs/fd1501-spinning-20261114`;
world `server/SpinningWorkshop`, display`:311`, ports25640/25641, units
`fd1501-spinning-20261114-{client,server,xvfb}.service`. Client1536M heap/6GiB unit;
server2048M/4GiB. Before restart: host33GiB available, app.slice unlimited with
39.6GB current usage, persistent XFS263GB free. No host-desktop input.

The first shutdown saved all dimensions before an RCON deadlock. The helper queued
its normal empty response-end marker after `stop`; that command waited on the
stopped server event loop while shutdown joined its RCON thread. The
[completed-save log](occupied/first-server-completed-save.log) and
[thread dump](occupied/post-save-rcon-threads.txt) preserve this diagnosis. Only that
saved, owned process was terminated after SIGTERM did not exit. The helper now
reads stop's single acknowledgement without the trailing command; Python compilation
passes and the final shutdown verifies the correction. Buffered chunk writes on the
restart removed the per-write disk stall without bypassing clean world saving.

Final client stopped05:13:38 UTC; server saved all dimensions05:13:40 and stopped its
RCON listener05:13:41. Client, server and Xvfb units all inactive/MainPID0 verified.
World, original/updated JARs and full input records/screenshots remain under the root.
No live runtime is handed off. The corrected example-source layout is compiled;
its normal construction/content check remains B/C, without repeating accepted A.
