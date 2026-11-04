# FD401 observed client-extrapolation clearance

This bundle closes the measured client-pointer gap left by the earlier
[snapshot-lag ride](../fd401-styles-client-snapshot-lag-2c5d9a7/README.md).
It does not claim arbitrary network-delay clearance. It extends the retained
coupled Arrow clearance sweep only through the largest fast-motion lead actually
observed there: `0.0054829494915793475` tick.

## Independent full-pose client lap

One Styles-only development client ran on its own X display (`:177`) with an
isolated game directory, build directory and project cache. No existing client
or another agent's display was inspected or controlled. Production geometry
sources still matched the frozen `2c5d9a7` candidate, whose packaged JAR SHA-256
is `4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`.
The opt-in diagnostic was newer than that JAR, so this is an instrumented
development-classpath run rather than another exact-whole-JAR claim.

The client used the exact-candidate saved `FD401StopCycle` copy, default Sable
1.5-tick interpolation delay, Track Styles 1.2.0, render distance 2 and
simulation distance 5. Dev boarded the saved UI-styled Arrow car, closed
restraints, dispatched, traversed the banked and flat full-turn sections, and
returned to powered station capture. The normal vehicle UI then displayed
`arrow_controlled | Ready | Restraints closed`; **Open restraints** released
the rider, Shift dismounted, and the final validation line reported
`assignments=0`. The world saved through the menu and Gradle exited
`BUILD SUCCESSFUL`.

[`ClientSnapshotTrace.java`](ClientSnapshotTrace.java.txt) records every ready
previous/latest snapshot pair for the occupied chassis and both carriers. The
[complete trace](client-snapshot-full-pose.jsonl.gz) contains 5,410 mounted
client samples and a completion marker. It yields 1,628 distinct snapshot ticks
and 1,626 consecutive intervals across the moving lap. The screenshots retain
the captured UI, unloaded endpoint and zero-assignment result; the
[client log](client-console.log.gz) retains the complete lifecycle.

## Clearance result

[`audit-client-route-clearance.py`](audit-client-route-clearance.py.txt) first
measures the conservative extra point motion that the observed lead could add
to the full-pose client lap. It then applies the same pinned Sable/JOML nlerp
formula directly to every retained coupled Arrow recovery-route snapshot pair.
This adds only the motion beyond the latest snapshot; the earlier interpolation
sweep already bounds motion up to that snapshot. Intervals that approach the
global margin are rechecked against the local style-specific track mesh.

The [report](report.json) passes the existing `0.02`-block requirement for
uniform single spine, uniform double spine, and the saved Original/single/double
transition plan:

| Envelope | Minimum separating lower bound |
|---|---:|
| Vehicle body | 0.0200000597 |
| Full-spin wheel | 0.0225438620 |
| Native rider | 0.0509397226 |
| Conservative vanilla player skin | 0.0504336757 |

The vehicle result includes 533 exact local interval checks per plan. The
player envelope covers classic/slim vanilla skins and outer layers in every
Arrow seat location, with all head/arm rotations enclosed as in the original
banked-player certificate. The raw trace, original observed-lag report,
retained single/two-car native traces, route frames, dependency JARs, auditor
and diagnostic source are all hashed by the report or `SHA256SUMS`.

Reproduce the archived report from the repository root with:

```text
python3 scripts/audit-client-route-clearance.py \
  docs/evidence/0.9/fd401-styles-client-extrapolation-clearance-2c5d9a7/client-snapshot-full-pose.jsonl.gz \
  docs/evidence/0.9/fd401-styles-client-extrapolation-clearance-2c5d9a7/client-console.log.gz \
  docs/evidence/0.9/fd401-styles-client-extrapolation-clearance-2c5d9a7/observed-lag-report.json \
  docs/evidence/0.9/fd402-isolated-base-ff7c831/native-exports/recovery-bank-mesh-trace.jsonl.gz
```

This certifies the retained default-model banked vehicle/rider geometry through
the observed client lead. It does not cover a larger packet delay, continuous
solver motion inside a recorded physics substep, armor, held items, capes,
elytra, modded cosmetics, arbitrary resource replacements, or the deliberately
separate exact-whole-JAR acceptance claims.
