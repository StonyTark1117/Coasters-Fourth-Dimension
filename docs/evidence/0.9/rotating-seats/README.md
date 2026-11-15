# FD911 — Additions carrier seats

Owner: Seats / Immersion, chat `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`.
FD911 complete, 2026-11-15. Source integrated on `main` in `fd8f334a`.

## Actual integration and scope

Selected Additions file 9243346 reports `coasterfins` 0.2.2. Its exact inspected
seat classes are recorded in [artifact-inspection.json](artifact-inspection.json).
FD501B's Aeronautics/Interiors prerequisite closure carries forward. FD810's
station telemetry remains closed and is not a prerequisite replay.

The actual CoasterSeatBlock inherits Interiors DirectionalSeatBlock, which
inherits Create SeatBlock without changing the passenger height. The adapter
therefore supports FD's unmodified CarrierSeatBlock footprint: Arrow, Legacy,
Free Spin, Revised, PAX and Suspended. Specialized seat subclasses retain their
native geometry. The existing FD carrier supplies rotation; adding another
Linked Swivel Bearing or a second native body is not part of this seat option.

Sneak-use an actual Additions seat item on a stopped, aligned, open, empty FD
seat to fit it. Building permissions and the player's interaction range are
checked in world coordinates. The original seat is returned in Survival.
Sneak-use that original seat item to restore its saved block state. Additions'
sixteen colors and four restraint appearances use the upstream renderer and
Restraint Selector. Neither appearance changes carrier identity or mechanics.

FD remains the restraint owner. Additions displays the resulting bar state and
its native synchronized seat attachments. Link signal edges submit the normal
validated whole-consist restraint operation; an unsafe request cannot open the
bar. A steady signal does not continually undo the operator's editor control.
Reload samples the current link state without replaying an operation.

Mounting and login restoration rebuild Additions' nonpersistent attachments.
The Additions server dismount veto yields to FD for these managed mounts,
including permanent-body recovery. Native SeatEntity, carrier rider assignment,
mass sampling and synchronization remain the existing shared paths. Original
seat state is also saved independently of the optional block entity so a missing
optional block can fall back to the original, with current restraints preserved.

## Targeted native result

[native-01.txt](native-01.txt) reports **all 1 required test passed** on the
qualified NeoForge 21.1.256 / Sable 2.0.6 baseline with the real Additions closure.
The test uses an actual Arrow child, actual optional seat BE, native SeatEntity
and a pig passenger; it is not evidence of real-player synchronization.

It proves transformed optional target/attachments, one rider count, ordinary
locked dismount denial, locked forced recovery, BE serialization, independent
fallback persistence and restoration of the exact native seat after the optional
block disappears. The server saved and exited cleanly. Persistent run root:
`/mnt/NVME4/fourthdimension-runs/fd09-seats-immersion-20261115`, native world
`native-01-world`, native build `native-01`.

The fallback test simulates the optional block's disappearance in a native server;
it is distinct from the actual occupied process restart below. No client run with
Additions uninstalled is claimed.

## Connected qualification and evidence reuse

The same saved Arrow example supplied the optional-seat and Immersion checks in
the expanded `styles,engineered,additions,ctnb,magnetized,skin,immersion,waterparked,createcoasters`
profile. Immersion was installed on both real clients and absent from the
dedicated server. The Java 21 packaged artifact is recorded in
[package-05.json](../immersion/package-05.json). Command-built circuits, supplied
inventory and disclosed approach teleports are setup, not construction acceptance.

| Required behavior | Accepted observation |
| --- | --- |
| Normal selection and configuration | Survival sneak-use fitted the actual red seat, consumed one item and saved the original Arrow state. [Selection](normal-survival-selection.png). Native Restraint Selector chose Lap Bar; native Wrench link GUI changed minimum range 8→9. [Menu](native-link-menu.png), [server readbacks](normal-controls-readback.json). |
| One authoritative mount/count and restraints | Dev boarded normally; server and both clients reported one assigned rider. Closing the FD restraints set the actual BE `Powered:0`; normal Shift was denied. Opening set `Powered:1` and normal Shift unloaded. [Lifecycle log](server-lifecycle-evidence.txt). The native test separately proves forced locked recovery/removal without the addon's veto. |
| Real synchronization and pose | Real Observer saw the selected lap bar, complete rider, original carrier and Classic chassis together. [Combined view](classic-optional-seat-rider.png). Moving, banked and inverted first/third-person and remote records are reconciled in [FD912](../immersion/README.md). |
| Occupied disconnect/rejoin | Normal Disconnect while locked retained one assignment. Rejoining the same endpoint queued restoration and mounted Dev in the same offset on the next server tick (08:44:03 host time). |
| Full process restart | Both clients, dedicated server and displays stopped cleanly; the saved assignment, Classic choice and locked state persisted. [Saved vehicle](saved-occupied-05.json), [independent original-seat fallback](saved-seat-05.json). Restart restored Dev immediately at 08:45:20. BE still reported Lap Bar, range 9, original Arrow state and locked bar. |
| Final unload and reset | Normal Capture, Open restraints and Shift after restart cleared the rider assignment. [Unload](restart-normal-unload.png), [final saved state](final-saved-state.json). No rider remains in either saved vehicle. |
| Supported fit | The replacement consumes the identical native SeatBlock mount height and existing CarrierSeatBlock offsets. Arrow's actual occupied view represents that common footprint; Legacy/Free Spin/Revised/PAX/Suspended retain their accepted seat-offset/physical-family contracts. Specialized subclasses are excluded. FD916 reviewed the actual red lap-seat and combined Classic views; no new family mechanism or full family sweep is claimed. |

The link adapter routes native link edges to the already-validated whole-consist
restraint operation and projects its result; the native GUI/configuration and
authoritative bar projection were exercised here. Existing operation guards
and family recovery/removal evidence remain applicable. The new native case
proves the changed addon dismount/attachment/fallback seam; real players supply
the changed mount, synchronization, reconnect, restart and unload observations.

## Fixture corrections and cleanup

The first fitting click occurred while flying: Shift descended away from the
target. Solid footing and Survival fixed that specific precondition; the next
single click passed. Default mouse bindings and Raw Input remained fixed.
The original fit click was preceded by an observed camera turn and reachable
vanilla stone placement in the same live client.

All five owned client/server/Xvfb units are inactive with MainPID 0. The server
saved all dimensions at 08:54:03. World and full logs remain at the persistent run
root above. [Cleanup and resource record](../immersion/cleanup.json). No process
or display was handed off, and no further FD911 case remains assigned.
