# Packaged dev.4 styled Arrow checkpoint

This advances FD401; it does not close the integration or release-candidate gate.
The same dev.4 JAR as the powered-train checkpoint was launched with Java 21,
a 2-GiB heap cap, separately installed Track Styles 1.2.0 and Engineered 1.1.1:

```sh
./gradlew runClient -PfdPackaged -PfdIntegrations=styles,engineered \
  -PfdWorld=FD04StyledRide -PfdValidation
python scripts/verify-styled-ride.py
```

The artifact hash is in `styled-artifact.sha256`. Three actual client processes
completed normal Save and Quit / Quit Game with exit code 0. A fresh owned Xvfb
`:198` isolated these GUI interactions; it was explicitly stopped after the last
client exited. No user desktop or other running validation display was operated.

## GUI edits, fallback and persistence

The world was copied from the closed prior five-family `FD04RenderQA` fixture.
Its starting schema-5 snapshot is `styled-baseline.json`. No addon NBT was edited.
Normal Rotation Tool/appearance/editor actions changed the clear controlled
straight **(-68,123,56) → (-68,123,20)** from single to double spine, retaining
`#4A90E2`. A subsequent profile edit applied `0:0;18:1080;36:0`.

The renderer displayed our Original red spine/ties. The appearance editor kept
the double-spine ID and tint and explained the +720° limit. The full +1080° key
survived normal save, process exit, cold launch and editor inspection; it was not
clamped to fit decoration. `styled-high-save.json` records the actual saved state.
Restoring `0:0;18:360;36:0` through the normal editor automatically restored blue
double spine. A real F3+T reload rebaked the 55-entry installed catalog and retained
the restored style. See `styled-reopened-high-profile.png`,
`styled-fallback-status.png`, `styled-high-original.png` and
`styled-double-resource-reload.png`.

Normal appearance editing then selected single spine with `#9B59B6` for the flat
quarter curve **(-68,123,20) → (-76,123,12)**, length **12.568132528583506**.
`styled-curve-single.png` records exterior alignment on that native curve.
`styled-restored-save.json` was inspected after another real Save and Quit.
The verifier checks both expected edits/revisions and every unedited section,
vehicle and carrier definition. Tiny captured native angle/velocity settling is
distinct from a frozen-state equality claim.

## Real player ride and exit

Before boarding, all three old temporary operator stones and the new curve-edit
stone were removed: **(-96,122,20), (-98,122,17), (-66,122,23), (-69,122,14)**.
The player boarded by normal empty-hand seat interaction, used the mounted
Rotation Tool editor to close restraints, and dispatched. The inherited station
capture input was still powered and recaptured that first request; removing the
rear redstone block **(-106,123,33)** dispatched through the station's normal
falling-edge logic. No forced mount, addon travel command, velocity write or
angle write propelled the ride. The example's native powered chains moved it.

The occupied circuit crossed the blue double-spine controlled section and purple
single-spine curve, returned to the station, and was captured after restoring the
rear redstone input. `styled-rider-rotating.png` shows the real player and
rotating camera beside double spine. The occupied carrier log reached a target
angle **6.252640607975717 rad**, with native angle **6.200635084224253 rad**,
then returned to **-0.000264 rad**. These are sampled dynamic observations, not a
claim of exact zero-lag target following. The return editor showed ready and
closed restraints; normal opening succeeded.

A brief automated Shift press did not dismount. The read-only RootVehicle check
and `styled-occupied-save.json` exposed this: the world saved one seated player
with restraints open, rather than falsely proving exit. The third cold launch
restored that boarding state. Holding Shift for 0.5 seconds dismounted normally.
The final saved player has no RootVehicle or gravity override and position
**(-105.5,124,32.5)**. All twenty carriers are empty; the Arrow vehicle is captured,
ready, unrestrained and fault-free. `styled-final-save.json`,
`styled-final-player.json`, `styled-exit-verified.png` and the verifier record this.

`styled-client-events.txt` preserves relevant observed events, occupied-carrier
samples, the failed initial `/save-all flush` attempt (unavailable in this
integrated-server command context), and three successful client completions.
All checkpoint JSON comes from read-only inspection of actual game saves.

## Scope still open

This proves the bounded default-model straight/flat-curve GUI and ride workflow,
fallback/reload/persistence, station input behavior and final rider exit. The
installed Engineered motors were tested in separate native fixtures; this GUI
ride used native chains, not Engineered propulsion or FE consumption.

Flywheel configuration was `backend = "DEFAULT"`, distance limiting enabled,
default worker selection. No shader pack/Iris was installed. This observation
does not identify every selected backend or certify other render modes, banks,
tighter curves, all swept rider clearances, resource-pack replacements, recovery
towing or a functional external track provider. Transient Sable missing-sublevel
movement messages during loading remain a separate lifecycle investigation.
