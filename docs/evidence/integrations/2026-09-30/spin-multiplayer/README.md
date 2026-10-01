# Legacy / Free Spin combined-pack station and lifecycle checks

Bounded local evidence on September 30, 2026. This extends the Arrow session; it
is not the full FD901–FD904 support matrix. Two real packaged clients used normal
seat clicks, the Rotation Tool, and native Engineered/Create blocks on a packaged
dedicated server. All five optional adapters were installed, with the same pinned
Minecraft, NeoForge, Create, Sable, Coasters, Aeronautics and Interiors versions as
[the previous combined-pack session](../combined-two-client/README.md).

## Initial session and failures retained

`original-artifact.sha256` identifies the `fb34901` JAR. Dev occupied a legacy
ZacSpin outer seat; Observer occupied the outer seat on Free Spin's last carrier.
Each vehicle had one real rider. Both clients received the same 16 rails and two
vehicles. The verifier matches 1,412 received packets to server packets, including
677 identical snapshots received by both clients.

A native Display Link above the legacy station supplied an oak sign. Cycling its
six actual source IDs produced Boarding, `1 / 8`, open restraints, the correct
dispatch denial, Boarding phase, and an empty fault. Closing restraints changed
readiness to `Ready to dispatch`; maintenance retention changed it to the actual
retention denial. The second client's screenshots show native sign rendering.
Free Spin's sign also changed from `0 / 8` to `1 / 8`, and both returned to zero
after normal exit. Long denial text exceeds a small sign's useful display width;
its full native component is preserved in the NBT transcript.

Both occupied cars were frozen, saved, cold restarted, and saved before resuming.
The full decoded schema-5 data is identical. Reattachment retained the original
carrier/seat assignments. The camera verifier uses a 0.25-degree tolerance for
rounded quaternion diagnostics; observed cold view changes are below that limit.
Raw Immersion orientation error remained at most 4.215e-8 radians. Legacy tilted
past horizontal (minimum carrier-up -0.642); Free Spin reached -0.971. This is not
an all-family/all-camera-mode inversion certification. Normal exits restored the
camera and cleared saved rider assignments.

The original legacy circuit returned and captured, but coasted beyond the station's
operation range. Free Spin stalled on the unpowered trim section containing its
passive spin inducers. The saved state has no fault, closed restraints and the
original rider; the native controller reports near-zero speed. Changing that
section to LAUNCH allowed it to return. That intervention is **recovery evidence,
not a passing original circuit**. Keep `stalled.json`, `stall.txt`,
`rescue-configuration.txt`, and the original logs when evaluating the result.

## Layout correction

`layout-fixed-artifact.sha256` identifies the next candidate. New Free Spin demos
put the 8-block/s trim brake on the elevated straight and power the magnetic
straight. All three Engineered demos use a 4-block/s station launch and a separate
4-block/s approach brake. This preserves the passive magnetic model and keeps
normal capture within the station's eight-block operation range.

A fresh world ran the generated legacy and Free Spin circuits with the same two
real riders. Both completed the circuit, captured, and produced `Ready to dispatch`
on their native signs. An occupied cold restart at the returned position preserved
all decoded saved data and restored both ready signs. Both riders exited normally.
`fixed-*` evidence records this candidate. Old saved demonstrations are not
rewritten automatically: rebuild them or configure their native controllers.

Attempting another pulse sequence exposed a separate presence bug: cancelling
propulsion at the outer native drive entry also suppressed Engineered's presence
reports. An engaged LAUNCH/COAST sequence could interpret a held car as departed.
The first attempted fix at MotorDrive alone failed the combined native regression
because the Magnetized wrapper had already cancelled that entry. Its failed test
is retained as `build3.txt`.

The final adapter restores only native span/controller and observer sampling at
all guarded drive entries. It never calls motor, braking, parking or return-to-start
forces for a held car. Exact-version loading still isolates Engineered classes.
The regression requires the returning held car to remain in its COAST step and
within station range, in addition to occupied capture and propulsion inhibition.

## Final artifact and validation

`final-artifact.sha256` identifies the final JAR used by both clients and the
packaged server. On the existing corrected world, both operators used Engineered's
native reset button, reboarded normally, closed restraints, and dispatched. Reset
can make Engineered return to its start mark before launch; arm station capture
only after the car has actually departed. Both occupied cars completed the circuit
and captured within station range. The native controllers remained in COAST with
fresh near-zero-speed presence reports and both Display Links showed readiness.

An additional occupied cold restart with this final artifact preserved the entire
decoded save, restored the same carrier/seat assignments, and retained COAST and
station readiness. The rendered view deltas were 0.00963 degrees (Dev) and 0.01038
(Observer), with the same 0.25-degree rounded-diagnostic tolerance. All 459 received
final-run snapshots match server records. Both riders opened restraints and exited
normally; saved occupant/seat assignments are empty. Final and earlier server and
client runs all exited with status zero; the owned displays were closed.

Validation: 19 JUnit tests; all 24 combined native tests; 24 registered tests in the
Engineered-only run (two explicit optional-fixture skips); 24 registered base tests
(three explicit optional-fixture skips). The COAST regression passes for Arrow,
legacy and Free Spin both with Engineered alone and with all five addons. Per-run
mod lists, build excerpts, JAR hashes, full-log hashes and the failed intermediate
regression are retained. This does not certify all repeat-cycle/occupancy patterns.

## Reproduce

Build with Java 21 and `-PfdIntegrations=engineered,styles,additions,magnetized,immersion`.
Use an immutable copy of the JAR for `runServer`, `runClient`, and `runObserver` with
`-PfdPackaged -PfdPackagedJar=/absolute/copy.jar -PfdValidation`; clients additionally
use `-PfdConnect=127.0.0.1:25585`. The local QA server used creative/offline mode,
flat peaceful terrain, and view/simulation distance five. Clients used 30 FPS caps
and isolated 1280×800 displays; this is not a performance benchmark.

1. Create legacy at `execute positioned -600 227 0 run fd demo engineered legacy`
   and Free Spin at `execute positioned -400 227 0 run fd demo engineered free_spin`.
   Keep the full routes loaded. These sessions used force-loaded chunks, so they
   provide no chunk-interruption evidence.
2. Stations are (-606,230,32) and (-406,230,32). Put a native Display Link one block
   above each, facing up, and a sign four blocks west. Configure `TargetOffset`
   `[-4,0,0]`, `TargetLine:0`, and the selected `fourthdimension:ride_*` source.
   No sign text is injected; native Create transfers actual station telemetry.
3. Equip goggles and a Rotation Tool. Board empty-handed from the west, at legacy
   z31.5 and Free Spin z29.5. Close restraints with the normal tool UI. Pulse the
   station's south input, remove it, and reapply after departure to arm capture.
4. Wait for native readiness, not merely a stopped-looking car. Freeze/save/stop;
   restart with `-PfdFrozen`, compare saves, reconnect clients, then unfreeze.
5. Open restraints through the tool, exit with Shift, and confirm zero occupancy.

Run `python3 scripts/verify-spin-multiplayer.py docs/evidence/integrations/2026-09-30/spin-multiplayer`.

Remaining scope includes repeated controller sequences across every lifecycle,
all occupancy patterns, linked trains, external power limits, rollback, addon
removal, chunk interruption, other camera modes and measured performance. Native
optional-mod startup/resource warnings remain those documented in the earlier
artifact audits. No external mod code or assets are redistributed here.
