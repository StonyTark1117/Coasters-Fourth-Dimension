# FD1502A — Roller Ball mechanics handoff

Owner: lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`.
**Complete, 2026-11-15.** Native mechanics, occupied cycle, physical summit restart
and the targeted reconnect-look correction pass cumulatively. All owned runtimes
are saved and stopped; no remaining FD1502A case.

## Contract and native evidence

`roller_ball / vertical_zigzag_v1` uses one native bogie and one four-seat
carrier on an unlimited transverse X hinge. Seats at ±3.25 lateral offset share
one body. Native mass is 11 empty and 14.2 with four .8-unit riders; the centered
ballast contributes one unit. Local-X inertia is 11.34917 empty and 12.98317 full.
The original geometry, seat hitboxes, torso load points and axle COM agree.
[Corrected alignment and loading](native/transverse-alignment-pass.log).

Native Chain supplies travel on the smooth, closed YZ route. A bounded guide
keeps riders upright at lift entry and summit; an independent native latch holds
the straight up-leg. The guide has no free-descent torque. Native gravity/inertia
produce continuous unwrapped tumbling, with passive speed limiting and return
damping. [Guide entry progress and bounds](native/guide-pass.log) pass the
.35 rad/s and .5 rad/s² reference limits. The [complete native route](native/travel-feedback-pass.log)
passes lift power hold/resume, free release, descent, return and stopped capture:
9,927 ticks, speed peak 7.23750 blocks/s, angle −1.57082 through 6.28319 radians.

[Targeted recovery](native/recovery-pass.log) rejects ordinary/wrong-family track,
a second car, missing or changed geometry, and premature Resume. Exact original
route repair, native realignment and explicit Resume retain the same chassis,
carrier and passenger. Repair checks every original route fingerprint before
clearing a validity latch; it cannot silently bind a changed curve or skip an edge.

## Occupied handoff

Frozen launch-02 used Java 21, default bindings and raw input on owned private
Xvfb :316. Normal camera turn and reachable vanilla use established input before
mod interactions. Normal empty-hand seat use boarded Dev on original chassis
`b75d47e0-3265-4548-a130-b2cdcff1d38f`, carrier
`b642e36d-1b23-434f-b156-44eae2b5e2c9`. Normal Rotation Tool Close and Dispatch
received successful server acknowledgements. The four-seat assembly, native
player, harness, foot supports, axle and latch/damping cues are visible together.

The same car traversed the complete native lift, guided summit, free descent
and return without a support fault or duplicated rider. [Release boundary](occupied/release-boundary.json),
[returned upright capture](occupied/return-ready.json), and
[settled normal unload](occupied/normal-unload-settled.json) preserve identity and
show native seat/camera ownership ending on normal Open then Shift. The raw
immediate unload sample has one stale synchronized occupancy frame; the later
sample confirms it cleared, without a second intervention.

The prepared example, platforms, materials, daylight and disclosed position/aim
commands are setup. They do not claim player route-building or camera travel.
Normal crafting/construction is separately recorded in [B/C acceptance](BC-acceptance.md).
Shared native-seat networking and remote rider pose use the accepted Axis and
Conventional Flying paths; this family adds no separate rider transport.

## Persistence case

Launch-03 (`9974e5c11fbc46758134e732cb80c0f719f7600c5aab8883863d824cf8dbe68c`)
reuses the saved original car after its first returned circuit. It includes the
qualified exact-route repair and guidance fixes. The collector observes pending
release and stops only this run's server; it never writes the ride state or pose.
The server stopped at 10:59:58 UTC with pending release, command 6.448334502,
angle 6.447782376, direction +1 and closed restraints. [Saved state](occupied/restart-saved-foundation.json)
and [saved player](occupied/restart-saved-player.json) preserve the exact identities.
On reconnect the [held snapshot](occupied/restart-held.json) retained the same
edge (0,176,96)→(0,168,144), command, rider and carrier, with restartHold=true and
release still pending. No automatic departure occurred. Normal [Resume](occupied/restart-resume.json)
cleared the hold and revalidated the route; [native descent](occupied/restart-release.json)
then released normally and retained the rider through subsequent route edges.
This closes the distinct physical restart case without repeating the accepted return.

A separate look defect was observed: saved seat-local yaw 59.5294° became
−120.4712° on remount while the seat quaternion was unchanged. Sable's ordinary
mount applies an inverse seat transform, appropriate for a new world-space mount
but a second conversion for this saved local view. PLAN D16's consistent camera
composition and reconnect restoration require correcting this one case. The fix
queues the saved look for the exact restored carrier/seat and applies it once on
the client after native mounting. Ordinary mounting and subsequent mouse input
remain native. The exact stopped summit world is preserved for a focused rejoin;
physical release/retention, full circuit and sibling families remain accepted.


The targeted correction passed on frozen launch-04
`a26a61e9743c2f4b85e1a702632c4af2d7fefc5658b2a398ed034a10f74add73`.
[Restored local look](occupied/restart-look-fixed.json) is exactly
59.529404° / 23.423008°; camera orientation differs only .03174° from the pre-stop
view as the physical guide settles. One logged restore event applies the fix.
[Normal mouse look](occupied/restart-look-turn.json) changes yaw to 67.179405°;
[it remains there](occupied/restart-look-settled.json) after eleven seconds.
Same carrier, rider, route, closed restraints and pending release remain held.
The shared reconnect fix applies only to successful saved-seat restoration and
is reused by the other families; it does not reopen their physical workflows.

## Saved state and cleanup

Persistent root `/mnt/NVME4/fourthdimension-runs/fd1502-roller-ball-20261115`;
original world `server/RollerBallWorkshop`, exact pre-restart copy `summit-save`,
and post-Resume descent copy `release-resumed-save` remain on disk. The final
world is deliberately retained at the inspected summit hold; normal returned
capture/unload is the accepted earlier circuit. No extra full ride was needed.
Java 21, client 1536M heap in an owned 6 GiB unit, server 2048M in 4 GiB,
private display :316 and ports 25650/25651. Last observed client usage was 2.46 GB
under its cap. Client stopped first; RCON stop saved all dimensions at 11:06:33 UTC,
then the owned Xvfb exited. [Verification](content/cleanup.txt) records every unit
inactive with MainPID 0; all recorded client/server PIDs are absent. Compressed
input, console, native-motion and final client/server traces are under content/.
