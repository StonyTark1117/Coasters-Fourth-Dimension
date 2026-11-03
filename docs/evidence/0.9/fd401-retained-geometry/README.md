# FD401 retained-input geometry audit

Offline audit performed 2026-11-02 using unchanged scripts at `494d89f`.
No Minecraft/Java process was launched, no production file changed, and no
clearance margin or supported stroke was relaxed.

| Existing auditor | Result |
| --- | --- |
| `audit-arrow-style-geometry.py` | Exit 0 in 13.004 s; eight recorded banked circuit sections, 1,244 control segments, continuous stroke interval −4 to +2 turns. Both installed Arrow styles' ties/spines pass the 0.01-block margin; smallest conservative gap 0.040983780691590255. |
| `audit-arrow-wheel-clearance.py` | Exit 0 in 1.875 s; twenty full-spin native-wheel envelopes on a level straight. Revised decoration minimum 0.1175277397884602 blocks; native support minimum 0.06000000000000005, both above 0.01. The legacy tie placements still report zero gap as negative controls. |

The pinned Coasters 0.1.5 and Track Styles 1.2.0 JAR hashes were checked before
execution. Neither JAR nor provider mesh is distributed here. Reports pin the
actual decoration-placement and track-design source hashes. Exact commands,
exit codes and elapsed times are in [results.json](results.json); input/script
identities are in [provenance.json](provenance.json).

## Retained frame provenance

The frame file was copied from the owned build directory after the last Styles
native run at `abc4a9a`. The original Styles console log records that export path;
the file timestamp is inside that run's recorded start/end interval. The
[native matrix](../profile-matrix-abc4a9a/README.md) records source/JAR equivalence,
49 required tests and a normal shutdown. Source geometry and auditor files are
unchanged between that revision and this audit.

**Limitation:** the original runner did not capture a frame hash at export time.
This audit hashes and preserves the retained bytes now; timestamp/path evidence
is not a cryptographic guarantee that they remained unchanged since export.
Treat this as a reproducible offline result for this recorded geometry, not a
fresh runtime or an extension of the original native manifest. Future candidate
runs must preserve and hash their frame files before another run overwrites them.

## Scope and reproduction

The banked audit compares default provider decoration to continuous-stroke
control-rail volumes. It excludes wheels, chassis, carriers, riders, linked cars
and resource overrides. The wheel audit includes continuous wheel spin and
longitudinal travel **only on a level straight**. Neither report proves visible
rendering, player clearance, resource-reload stability or arbitrary track geometry.
FD401 remains open; see the [release handoff](../../../FD401-RELEASE-HANDOFF.md).

From the repository root, use the original scripts and separately installed
JARs matching `provenance.json`:

```sh
python3 scripts/audit-arrow-style-geometry.py /absolute/track-styles.jar docs/evidence/0.9/fd401-retained-geometry/bank-native-frames.json
python3 scripts/audit-arrow-wheel-clearance.py /absolute/coasters-simulated.jar /absolute/track-styles.jar
```

Use `python3 docs/evidence/0.9/fd401-retained-geometry/verify.py` to verify recorded
payload hashes, report identities, the original 0.01 margins and asserted result
scope. This verifies saved evidence; it does not launch or certify a game runtime.
