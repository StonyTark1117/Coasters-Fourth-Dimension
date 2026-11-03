# Historical curve-provenance backfill — bounded PASS

Exact checkout `307ec35ee4039c9669515cfb3ae4901868b1a000`, packaged dev.27 JAR
SHA-256 `5de077e3d76e5e81184c6af316d4c7a6083f99bf5367d5319fd697a96b3fb598`.
Executed November 2, 2026 (America/Phoenix; November 3 UTC), standalone Minecraft
1.21.1 / NeoForge 21.1.248, schema 6 / protocol 7. Product source includes
`b6c08dc` and its presentation-only restoration-message follow-up `807ff5f`.
The coordinator separately owns the 49-test native result on b6c08dc and compile
check for the message follow-up. This bundle is a real historical-world upgrade
check on the exact combined packaged artifact, not another native-suite claim.

Two historical worlds that genuinely predate `original_curve` were copied in
full, booted separately, saved twice and stopped normally. Both original worlds
and pristine input copies remain untouched. No provenance fields were deleted
or injected to fabricate legacy inputs.

| Input | Before | After | Outcome |
| --- | --- | --- | --- |
| Intact world after the a1aeb1a active-lift restart checkpoint | Six rails: three valid PAX, two valid Arrow, one already invalid Arrow; zero provenance snapshots | All five valid rails acquire snapshots exactly equal to their input native Bezier representations. Existing invalid Arrow remains without one | PASS; every other saved rail field unchanged |
| Historical 311078e failed PAX support-restoration world | Six rails: three invalid PAX, two valid Arrow, one invalid Arrow; zero snapshots; one saved fourth-car rider reservation | Only the two valid Arrow rails acquire snapshots. All invalid rails stay invalid and without snapshots; PAX support faults and logical reservation persist | PASS; no adoption of the changed PAX support |

The second world has a **physically rebuilt, changed station curve**, with the
original stored fingerprint and invalid status. It is not a missing-support
simulation. Its two PAX spike rails were also already invalid. The unrelated
invalid Arrow section includes an absent anchor; its unavailable block query is
preserved rather than treated as a successfully decoded block entity.

## Reproduction

1. Use the full historical source worlds named in each `*-baseline.json`.
   Verify their foundation hashes match the pristine copies. They contain six
   original chassis/twelve carriers total, including the original four-car PAX
   train. All six rails lack `original_curve` in both inputs.
2. Build the exact checkout with Java 21 and `./gradlew build --offline
   --no-daemon --console=plain`. Copy each pristine world into a separate server
   world folder: `legacy-backfill-intact-307ec35` and
   `legacy-backfill-changed-307ec35`.
3. Run one dedicated JVM at a time with `runServer -PfdPackaged -PfdValidation`.
   No clients, GUI, Xvfb display or optional mods are launched. Explicitly
   forceload the nine historical anchor chunks using the included per-case
   command list; allow normal server ticks. These are load requests, not geometry
   or mechanical-state edits.
4. Read availability, time, online-player list, winch and each anchor's block
   data, then `save-all flush`. Capture the saved foundation state. Repeat after
   additional normal ticks, then `stop` and wait for process exit 0. Preserve the
   complete stopped world before proceeding to the other input.
5. Compare the stopped data with the pristine input. New snapshots must equal
   the actual serialized native curves from the input anchors. Every other rail
   field—including ID, endpoints, family/variant, validity, revision, profile,
   geometry hash, magnetic settings and appearance—must remain unchanged. Verify
   complete chassis/carrier identity, seat layout, mechanical parameters,
   restraints/capture, fault/alignment state, logical seat reservation and winch
   binding/route/cycle fields.

Runtime commands are limited to forceload, read-only diagnostics, save/flush and
stop. There is no repair, rebind, dispatch, rider mount, profile change, geometry
write, state injection, or original-world modification. Source and backup paths
are recorded in `scope.json`. Input/output foundation NBT, decoded JSON, native
winch chunks, player state, raw observations, decoded anchor observations and
complete compressed logs are included.

The positive case retained all other rail fields exactly. Added snapshots match
the input native curve data field for field; they are not reconstructed from
endpoints or guessed tangents. Production performs its loaded-curve fingerprint
comparison before capture, as separately covered by the native regression. This
record does not claim an independent offline reimplementation of that sampler.
The negative case stays excluded by its pre-existing invalid status.

## Occupancy and limits

Both servers report six loaded chassis and twelve loaded carriers throughout.
No players are online. The changed-support input retains the original logical
player-to-carrier-to-seat reservation, while its live `occupants` list clears
because the player has not connected. That is not a remount failure or proof of
successful reconnect. On-disk RootVehicle data alone is not a live mount.
Native carrier angles/angular velocities can settle during ordinary server ticks;
the unchanged-state claim excludes those dynamic values and live occupant lists.

Winch bindings, original train IDs, profiles, guards, faults and logical
reservations persist. The older 311078e input lacks `shuttle_pending_retention`;
the new save writes its default false value. The verifier permits only that
explicit default addition when comparing persisted shuttle fields. Both server processes exit 0, with complete saves and
normal shutdown in their logs. Existing upstream mixin/dist-cleaner/offline/LAN
warnings remain; this is not a warning-free or performance result.

This covers forward provenance capture on intact loaded legacy tracks and refusal
to adopt pre-invalid changed support in two actual old saves. It does **not** prove
reconstruction of already-missing geometry, repair of the failed historical PAX
world, normal occupied operation after migration, client reconnect, schema-7/
protocol-8 provider migration, optional addons, every historical save version or
the complete FD903/0.9 gate. The earlier fresh normal-restoration lifecycle and
active-lift restart checkpoints retain their own exact-artifact limits.

Run `python3 verify.py` in this directory. It checks the before/after invariants,
full native snapshot equality, two stable live saves per case, offline-reservation
semantics, shutdown records/logs and complete SHA-256 manifest.
