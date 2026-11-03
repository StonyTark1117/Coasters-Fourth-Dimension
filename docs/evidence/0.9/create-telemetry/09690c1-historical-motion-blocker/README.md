# Historical repair replay: physical-stop precondition remains unmet

**Failed/incomplete operator acceptance.** The fixed candidate gets past the old
historical-boundary-fault rejection, but the preserved occupied Arrow chassis does
not satisfy the physical-stop precondition for normal realignment. Normal Capture,
a twenty-second wait with ten read-only motion samples, and two later normal
Realign attempts do not complete the operation. No source edit, direct vehicle-state
write, velocity reset, threshold bypass, or edited replay NBT was used.

Source: `09690c1a45d09a088438fa40e1ba3af35c9731d4`.
Packaged `coasters-fourth-dimension-0.9.0-dev.27.jar` SHA-256:
`bc98cd6cc7b8e2ff6d8d647c8e6914f2b618aac1258fbeef8da070a82ee777e4`.
Standalone dependencies and exact hashes are in `manifest.json`. The build succeeds
and executes the unit-test task; compilation may reuse cache. It is not a fresh
native GameTest suite result. The later release commit `eca36ca` is not included.
Schema 6 / protocol 7; isolated Xvfb :121 / llvmpipe, 854×480.

## Untouched input and normal actions

The full stopped world from the [de667f7 failed case](../de667f7-arrow-repair-blocker/README.md)
was copied to `telemetry-09690c1`, without editing its data. The original backup
remains separate. `input-world.json` records equal original/copied foundation SHA-256
`7b30ddbf3abf72f1002a3935992d6331d1b89ee2beed15f7ccc100d9098ca821`.
The server directory name differs from the old embedded level display name; logs
can therefore display `telemetry-de667f7` while loading this copied world.

1. Start the exact packaged candidate on the copied dedicated-server world.
   Dev and Observer connect normally. Dev's original rider/carrier/seat assignment
   and native passenger restoration survive the restart; the historical boundary
   fault, closed restraints and pending realignment remain present.
2. Dev uses the Rotation Tool, then normal **Realign matching track**. The message
   changes from the old **Repair matching track before realignment** rejection to
   **Wait for every linked chassis to stop physically before realignment**.
   No realignment occurs. Observer goggles show 1/4 riders, alignment denied,
   dispatch denied and the retained end-of-track fault; the same fault Display Link
   still shows that fault. The sign's visible text is cropped; saved NBT is complete.
3. Dev invokes normal **Capture**. Observe native motion read-only with
   `/fd validation motion Dev` ten times at roughly two-second intervals, over
   about eighteen seconds between first/last samples plus the final wait. Chassis
   position stays near `(31.39,80.64,38.55)`, below the original rail height 83.75,
   while sampled speed magnitudes remain approximately **0.262–0.287**. The
   realignment precondition is at most **0.1**. These rounded command readings
   prove sampled residual motion; they are not a continuous velocity trace.
4. Dev reopens the editor and normally retries Realign after the wait, then again
   later. Both reject with the same physical-stop message. All five retained state
   observations preserve the chassis, both carriers and original player-to-seat
   assignment, with closed restraints, pending realignment and the boundary fault.
   Fault clear, readiness, normal unloading and fresh recurrence are not achieved.

## Runtime interruption and evidence limits

Cross-chat app calls began failing, first with a tool error and then a refused
app socket. On the following continuation, RCON was refused, original server/client
exec handles were unknown, and all owned server/client/Xvfb processes were absent.
Logs end without a normal shutdown sequence. The timing suggests an environment
interruption, but its cause is **not established**. No clean-exit or crash-recovery
claim is made, and no exit codes are available. A separate native test process was
live, so this runtime was not restarted over it.

The full interrupted replay is preserved locally under
`build/telemetry-local-acceptance/09690c1/interrupted-historical-replay-world`.
`last-saved-foundation.dat` is the last on-disk state, not a certified clean shutdown
snapshot. Original PNGs, all five observations, ten motion samples, build output,
and available server/client logs are retained. The fresh support-loss → restore →
Repair → delayed Realign → unload case was **not started** and remains required.
The second client observed the original Display Link and goggles; no optional
profile, hardware performance, or complete rendered-clearance result is implied.

## Code observation for investigation

At this source, `VehicleOperations.brake` uses native `resetVelocity` only when
`fault` is exactly `Matching track support lost`. The retained historical boundary
fault instead receives bounded counter-velocity braking, despite
`alignmentRequired` remaining true. The same distinction applies after repair clears
the fault. This is a plausible source of residual solver motion, not proof of the
root cause; temporary restoration scaffolding may also be relevant to this scene.
Neither source nor scaffolding was changed during this replay. The core chat was
notified after messaging recovered. Any fix needs its own exact-artifact historical
and fresh operator retests while preserving the physical-stop and geometry guards.

Run `python3 verify.py` here to verify recorded identity/state, sampled velocity
and file hashes. This does not launch Minecraft or establish a successful recovery.
