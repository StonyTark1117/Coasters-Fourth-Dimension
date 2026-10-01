# Immersion 0.2.3 carrier-camera development evidence

Baseline: Minecraft 1.21.1, Java 21, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Coasters 0.1.5 and Sable 2.0.3. `dependency.json`
identifies Immersion 0.2.3 and its requirements. The profile additionally loads
Aeronautics bundled 1.3.2 (Aeronautics, Simulated and Offroad), with the exact
artifact/hash recorded in the sibling Additions evidence and dependency audit.

## Native checks

Use `JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew
-Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer` with either
`-PfdIntegrations=immersion`,
`-PfdIntegrations=engineered,styles,additions,magnetized,immersion`, or no profile.
Selected logs identify full-log hashes and exact commands. Optional fixture skips
are explicitly distinguished from executed interoperability tests. The camera
adapter is client-only; native tests prove dedicated startup/regressions and the
other adapters' interactions, not camera behavior.

## Recorded results

| Profile | Native result |
| --- | --- |
| Immersion alone | 24 registered tests passed; Engineered, Additions and Magnetized explicitly skipped. |
| All five addons | All 24 tests executed and passed. |
| Standalone | 24 registered tests passed; those three optional fixtures explicitly skipped. |

All 17 unit tests passed. The combined and standalone native runs preceded a
client-diagnostics-only correction to normalize quaternion comparisons; adapter
behavior is unchanged. The final individual Immersion run and packaged checks use
the corrected diagnostic. `source-files.sha256` records final inputs.

The final packaged verifier passed **700 mounted samples**, including ten near-fully
inverted samples, 263 samples with effects disabled and one normal dismount transition.
Maximum normalized raw-pose difference was **4.2147e-8 radians**. Captures include
[default smoothing](default-inverted.png), [zero head inertia](zero-inertia-inverted.png),
[effects disabled](disabled-inverted.png), [capture](captured.png) and [unloading](unloaded.png).
The phase-triggered observations are in `camera-sequence.json`; replay the verifier
against `camera-samples.txt` for both arguments to reproduce `camera-verification.json`.

A further packaged standalone client loaded an unoccupied copy after removing
Immersion and its Aeronautics bundle, rendered the world, saved and exited normally.
No optional camera hook ran. All saved rail records remained exactly equal and the
chassis ID set was preserved; see `removal-client.txt` and [world view](removed-world.png).
Recipe/advancement and old external-equipment warnings prevent a clean-removal claim.
No ride operation or occupied removal was tested in that standalone session.

## Packaged client procedure

The immutable JAR identified in `artifact.sha256` runs with:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runClient -PfdIntegrations=immersion -PfdPackaged -PfdPackagedJar=/absolute/path/to/snapshot.jar -PfdWorld=FDImmersionQA -PfdValidation
```

The isolated Xvfb session uses 1280×800 pixels, a windowed client, five-chunk render
and simulation distances, and a 30 FPS cap. Mouse automation uses XTest with raw
mouse input disabled in the disposable QA client's options. The inspected world
is a copy of an earlier QA world; omitted Engineered equipment generated expected
missing-registry warnings. The ride under test is a newly created original Arrow
example with native Coasters chain drives, independent of those removed blocks.
This does not certify safe removal of arbitrary external equipment.

Create the original loop with `/execute positioned -600 227 0 run fd demo arrow`.
Its initial boarding platform is around -606,230,32. Use an empty hand to board a
seat, then the Rotation Tool's normal restraint, dispatch and capture actions.
Hold Shift after opening restraints to leave the seat. No camera or body pose is
injected by the test. The track's real rotation profile drives full carrier turns.

The read-only `FD_IMMERSION` diagnostics sample the actual registered ridden
carrier, Immersion's raw orientation, rendered camera up, player mouse angles,
master/processing state, body travel and head inertia. Quaternion angle comparison
normalizes the dot product by both input norms. Early logs used an unnormalized
metric, which could report small false differences for identical near-unit poses;
those logs are not accepted by the final verifier.

Exercise ordinary mouse yaw and pitch while riding, zero head inertia, the camera
master switch disabled through a full inversion, and default 40 ms inertia after
reenabling the switch. Configuration changes use the QA client's NeoForge watched
config; runtime diagnostic state confirms reload. `camera.enabled=false` must
produce `processing=false` and zero body travel without canceling Sable's physical
seat rotation. Reset settings to their original values afterward.

The automated log verifier is `scripts/verify-immersion-camera.py RIDE_LOG
DISMOUNT_LOG`. It requires normalized measurements, finite values, a close match
to the authoritative carrier pose, inverted rendered views with effects both on
and off, zero disabled body travel, mouse-angle variation, and a real mounted to
unmounted transition within one client session. It cannot use a restart boundary
as proof of dismount. Visual captures complement those numerical checks.

An earlier occupied Arrow save/quit and cold client restart restored the real
passenger. The retained car had open restraints, remained held and allowed normal
unloading. This is one integrated-client observation, not a two-client dedicated
restart certification. Initial brief Shift presses sometimes missed the sampled
input; the accepted exit uses a held press and verifies `riding=false`.

## Remaining scope

Every other family still needs packaged camera acceptance. Third-person and Sable
camera modes, maximum comfort settings, seat-to-seat changes, dimension changes,
provider loss, multiplayer/late join, occupied moving restarts and full combined
client rendering remain open. No performance target is certified by the 30 FPS
cap. The earlier transient Sable movement-packet warnings remain an investigation.
No camera feature is claimed merely because dedicated native tests pass.

Immersion's own emotes, first-person arms, seat-aligner and overlay are retained;
this adapter does not replace carrier seats or validate every pose/seat-facing
option. No external implementation, models or textures are copied into our JAR.
Full FD901–FD904 acceptance remains open.
