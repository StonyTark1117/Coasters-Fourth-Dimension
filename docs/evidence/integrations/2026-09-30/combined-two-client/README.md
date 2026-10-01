# Combined-pack Arrow: dedicated server and two clients

Bounded FD902/FD903 development evidence, recorded September 30, 2026 (America/Phoenix).
This is not full integration certification or a completed FD901–FD904 gate.

## Exact stack

Minecraft 1.21.1; Java 21; NeoForge 21.1.248; Create 6.0.10-280;
Ponder 1.0.82+mc1.21.1; Flywheel 1.0.6; Coasters 0.1.5; Sable 2.0.3.
Optional artifacts: Engineered 1.1.1 (8863559), Track Styles 1.2.0 (9020601),
Additions 0.2.0 (8772429), Magnetized 1.1.0 (8668703), Immersion 0.2.3
(8765632), Aeronautics bundle 1.3.2 (8763471) and Interiors 0.6.1 v3 (8974633).
The Aeronautics bundle also supplies Simulated and Offroad 1.3.2.
Coordinates are pinned in `build.gradle`; sibling integration evidence records
upstream artifact hashes. `artifact.sha256` identifies the immutable mod JAR used
by all three processes. Runtime adapters are based on `affd049`; this slice adds
validation-only packet/station logging. `source-files.sha256` identifies changed Java inputs.

The isolated local offline server used port 25585, a fresh flat `FDCombinedQA`
world, creative/peaceful mode, view/simulation distance 5 and two real Java clients
named Dev and Observer. Each client had a separate 1280×800 Xvfb display, an
854×480 game window, a 30 FPS cap and a 2 GiB heap cap. The server also had a
2 GiB cap. Both clients and both server runs exited normally with status 0.
No shaders were loaded. These are functional checks, not performance measurements.

## Recorded results

- All **24 native tests executed and passed** with all five integration profiles;
  **17 unit tests** passed with no failures/skips.
- **490 Dev + 401 Observer** received snapshots match their server packet hashes.
  **395** tick/payload pairs were received identically by both clients. Hashing covers
  the full serialized mod snapshot, with a separate rail hash. It does not prove
  that every rendered frame or native physics packet matches.
- Real goggles and typed client telemetry show empty/open/denied, one rider/open/denied,
  closed/ready and retained/denied states. Both clients observed the empty, ready and
  retained transitions. The second client saw occupancy change to 1/4 after Dev boarded.
- Both players boarded through ordinary right-click seat interaction, one on each
  independent carrier. Closed restraints and both seat assignments were saved.
- Frozen pre-stop and post-cold-start SavedData JSON are **exactly equal**. After
  simulation resumed, both players reattached to the same native carriers. All rails,
  vehicle fields and carrier fields except settling angle/angular velocity remained
  equal; both rider-to-seat mappings remained intact. Debug tick metadata was not saved.
- The station rear pulse advanced Engineered's STOP → LAUNCH sequence and requested
  dispatch through the normal station validation. Reapplying rear power armed capture
  when the car returned. Both riders remained aboard through the circuit and inversion.
  Capture reached boarding readiness; normal tool interaction opened restraints;
  ordinary held Shift dismounted both players and cleared both seat assignments.
- Immersion logged **792/722 mounted samples** and **12/10 near-fully inverted samples**
  for Dev/Observer. Both views inverted, raw carrier-pose error stayed at or below
  **4.2147e-8 radians**, and normal dismount restored ordinary mouse-pitch camera up
  with zero residual body travel. Default enabled camera/head inertia 0.04 only.
- A style edit beside the occupied approach was rejected. A distant curved rail accepted
  the single spine. That rail survived the occupied restart unchanged. While Observer
  was disconnected, Dev changed it to the double spine; Observer's next connection
  received and rendered that choice. F3+T retained it. The only saved rail differences
  were `appearance_style` and its revision; family, provider, profile, magnets and
  geometry remained identical.

Run the read-only verifier from the repository root:

```sh
python3 scripts/verify-combined-multiplayer.py docs/evidence/integrations/2026-09-30/combined-two-client
```

`verification.json` is its output. SavedData JSON files, selected raw runtime events,
unit/native summaries and screenshots are committed beside this document. Runtime
logs retain observed upstream warnings rather than presenting a warning-free run.

## Reproduction

Build with Java 21 and `build runGameTestServer`, passing
`-PfdIntegrations=engineered,styles,additions,magnetized,immersion`. Copy the resulting
JAR to an immutable path before starting packaged processes. For all three runs use
that profile plus `-PfdPackaged -PfdPackagedJar=/absolute/path/to/snapshot.jar
-PfdValidation`. Use `runServer`, `runClient` and `runObserver`; clients additionally
use `-PfdConnect=127.0.0.1:25585`. Separate X displays/client directories are required.

1. On a fresh server, execute `execute positioned -600 227 0 run fd demo engineered arrow`.
   Origin is (-600,230,12); the station is (-606,230,32), controller (-606,230,34).
2. Equip the rotation tool and goggles. A small viewing pad at (-604,229,20) allows
   the rejected near-approach style edit. A pad at (-565,229,56) allows the permitted
   curved-rail edit with the tool's Change appearance button.
3. Let Observer join the existing scene. View the station from the platform, then
   board Dev from (-606.5,230,32.5), facing east/pitch -5, with an empty hand.
   Observer boards from (-593.5,230,32.5), facing west/pitch -5. Close restraints.
4. Freeze ticks, `save-all flush`, inspect the foundation `.dat` with
   `scripts/inspect-foundation-save.py`, and stop the server while both riders remain
   mounted. Start a new process with the same arguments plus `-PfdFrozen`.
   Reconnect both clients, save/inspect before unfreezing, then resume simulation.
5. Pulse a redstone block at (-606,230,33) for one second, then remove it. Reapply
   after departure to capture the return. After readiness, open restraints through
   the normal editor and hold Shift to exit. Inspect occupancy before teleporting
   players back to the viewing platform.
6. With both clients viewing station goggles, close restraints, retain the car, and
   check the corresponding ready/denied transitions. A falling rear signal while
   retained must not dispatch. Release retention and reopen restraints.
7. Disconnect Observer, change the far curved rail to double spine, reconnect,
   view it and reload resources. Save, stop and quit both clients normally.

## Limits and observations

The combined runtime loaded all addons, but this physical ride used Engineered
propulsion, Track Styles cosmetics and Immersion camera effects. It did not exercise
Additions/Magnetized equipment with human riders; their combined native fixtures
passed separately. No all-family, linked-train, chunk interruption, support-loss,
optional-removal, dimension transition or shader/performance acceptance is claimed.
Create Display Link output and Ponder animations were not exercised in this session.

Exact view-heading preservation across reattachment is **not certified**: the logged
player yaw changes by approximately 180° when Sable reattaches each rider after the
cold restart. Carrier orientation input and post-ride dismount restoration pass;
the local/world camera-heading conversion still needs a separate investigation.

Known upstream dedicated client-class mixin warnings and client resource warnings
remain (including unused Track Styles models, Coasters track variants and Magnetized
end-piece textures). Resource reload completed and the selected spine rendered.
This capped session had no exit 137 and no logged movement-packet warnings, but that
does not resolve the earlier uncapped FD305 incident. No release readiness is inferred.
