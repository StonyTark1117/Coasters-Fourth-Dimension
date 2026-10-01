# Powered controlled train and decorative stroke checkpoint

This extends partial FD401 in `0.9.0-dev.4`; FD401 and 0.4/0.9 remain open. It
retains schema/network protocol 5 and all five original functional families. The
dev.3 flat-motor fixture remains documented in `operating-acceptance.md` with its
own artifact hash. New artifact/test results use the `powered-train-*` prefix.

## Native powered full turn and rollback

`EngineeredGameTests.installedMotorDrivesLinkedControlledTurnsAndRollback` builds
a 48-block original Arrow straight, installs an actual Engineered 1.1.1 controller
and span, and creates two original Arrow cars six blocks apart. Each boards one
native Pig passenger. After normal native coupling, restraint closure and
operator dispatch, the controller drives the train at target 8 blocks/s. This
fixture contains no chain drive and never writes velocity, position or carrier
angle to simulate motion. Only actual native motor forces drive its translation;
normal rotary constraints execute its original distance profile.

The program holds zero through distance 20, ramps to +360° at 26, holds that
unwrapped full turn through 36, then returns to zero by 44. Both cars traverse the
first ramp at their own axles; their measured angle difference exceeds 5.75
radians during the run. Native STOP holds both on the full-turn plateau. After
120 ticks stopped, all four real carrier angles must be within 0.08 radians of
2π with capture still disabled: an upright quaternion alone cannot pass.

The same installed controller then runs LAUNCH with BACKWARD heading. Both cars
retraverse the ramp and return to the original zero-angle plateau. A second
native STOP requires translation below 0.1 blocks/s and every carrier angle and
angular speed below 0.08, while carrier capture remains disabled. Normal
VehicleOperations capture, ready alignment, restraint opening and dismount then
unload both passengers. Coupling is cleared and native bodies/track cleaned up.

Every running tick checks native coupling, unchanged functional family/binding,
no fault, closed restraints and both passengers still mounted. The rotation
profile object remains unchanged by motor commands. The actual per-cart native
assembly mass is checked with both cars linked, ensuring that each cart's motor
membership includes its own loaded carriers and does not count the entire train.
The final combined measurement reports approximately:

- Maximum unwrapped angles: 6.2854 and 6.2830 radians (both exceed 6.2).
- Rollback minima near zero: -0.00023 and -0.00351 radians.
- Maximum axle angle difference: 5.7595 radians.
- Maximum error from six-block chassis spacing: 0.000367 blocks.

These are native fixture measurements, not exact manufacturer dynamics or a
real-player GUI ride. Exact values and assertions are preserved in the final
profile logs. Earlier development runs exercised this added fixture before the
stroke guard; they are not substituted for the final guarded build's evidence.

## Default-model control-rail stroke audit

`python scripts/audit-arrow-style-stroke.py <separately-installed-jar>` checks the
pinned Styles 1.2.0 hash, parses its default OBJ faces in memory and prints only
aggregate measurements. No asset or reconstructed mesh is saved. The report is
`research/track-styles-1.2.0-control-stroke.json`. Independent analytic fixtures
check the projection/distance primitives, including degenerate projected faces,
collinear overlap, crossing and separated stroke/triangles.

The method projects triangles into local X/Y and measures minimum distance from
each continuous vertical control-center stroke segment at X=±1.35 to those
triangles. Subtracting the original control cylinder radius 0.09 yields surface
clearance. Considering every longitudinal position of each tie is conservative
for local straight rails and covers the entire angle interval rather than a few
sampled keys. Ties use the measured local X origin 0.5; spines use origin zero.

| Default component | Minimum surface clearance over -4..+2 turns |
| --- | --- |
| Single spine | 0.9700 blocks |
| Double spine | 0.5325 blocks |
| Either Arrow tie | 0.0725 blocks |

Both ties intersect the control-cylinder envelope somewhere in the continuous
+3..+4-turn interval: minimum surface clearance is -0.0525 blocks. This is the
reason for a conservative +720° decorative limit. It is not evidence that every
angle just above +720° intersects. Monotone profile interpolation guarantees the
key extrema bound the original control stroke.

The server rejects new external selections above that limit. Existing selection
intent survives profile edits and saves; the client renderer falls back to all
Original decoration above the limit and can restore the style below it. The
appearance screen explains that Original is shown. Original retains the full
-1440°..+1440° contract; no mechanical angle is clamped or edited by the styling
adapter. Both candidate styles use the same stroke boundary.

The native appearance packet fixture tests a +1080° profile for each installed
style, checks rejection without revision mutation, preserves the high-stroke
profile/style through save/load, and restores the supported original profile
through the normal editor. Unit tests include exact +720°, just above it,
positive three/four turns and the full negative bound. These headless checks
exercise the shared render-eligibility condition and transactions; they do not
claim a new GUI observation of the fallback renderer.

## Final validation

The final build has 24 passing unit tests and 23 registered GameTests in each of
standalone, Styles-only, Engineered-only and combined profiles. With Engineered
absent, its three dedicated operating tests explicitly report not exercised:
20 core cases and three availability guards pass. Engineered-only and combined
exercise all 23 scenarios. Styles-only and combined exercise both native stroke
selection/edit/save cases; absent Styles reports zero such installed-provider
cases. See `powered-train-{profile}-native.txt`, `powered-train-unit.json` and
`powered-train-artifact.sha256` for exact final evidence.

## Remaining work

This proves the continuous local straight default-model control stroke, not
curved/banked geometry, cross-section changes along a curve, rider clearance,
resource-pack overrides or graphics backends. Complete styled player rides,
Engineered FE-enabled operation and real station/controller wiring still need
acceptance. Recovery towing, tutorials and the full lifecycle/migration/performance
gates retain their original roadmap scope. No external functional provider is
certified, and no 0.4/0.9 release readiness is claimed.
