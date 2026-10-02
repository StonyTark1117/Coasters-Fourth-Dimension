# FD402 dense sampled native recovery clearance

Dev.26 samples the occupied banked-recovery fixtures every ten ticks, replacing
the prior twenty-tick cadence. The trace begins with a declared interval and is
written through a single buffered JSONL stream. This captures actual native
chassis, lower-bearing, wheel, rider and carrier-rack poses without altering
motion, forces or saved state.

## Method

`scripts/audit-recovery-mesh-trace.py` pins the renderer, route design, trace
recorder and both game-test producers. It rejects a trace without the ten-tick
header, missing or out-of-order samples, fewer than 500 samples, a substituted
body/carrier, absent native rider/rack volume or incomplete five-section route.
The verifier reconstructs default block-model volumes against original track
tubes and neighboring-car volumes. The two running rails remain excluded only
for intended wheel contact.

## Local result

The combined Track Styles/Engineered command below passed all 37 required
GameTests, then the independent auditor passed every route.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer --no-daemon -PfdIntegrations=styles,engineered
python3 scripts/audit-recovery-mesh-trace.py <base-jar> <minecraft-client-jar> \
  build/runs/gametest/recovery-bank-native-frames.json \
  build/runs/gametest/recovery-bank-mesh-trace.jsonl
python3 scripts/audit-recovery-mesh-trace.py <base-jar> <minecraft-client-jar> \
  build/runs/gametest/recovery-bank-train-native-frames.json \
  build/runs/gametest/recovery-bank-train-mesh-trace.jsonl
```

| Route | Cars | Pose samples | Body/track lower bound | Rider/track lower bound | Rider/rack lower bound |
| --- | ---: | ---: | ---: | ---: | ---: |
| Arrow | 1 | 577 | 0.05083 | 0.08 | 0.06250 |
| Legacy | 1 | 585 | 0.04602 | 0.08 | 0.06250 |
| Free Spin | 1 | 588 | 0.04631 | 0.08 | 0.06250 |
| Revised | 1 | 587 | 0.04625 | 0.08 | 0.06250 |
| PAX | 1 | 577 | 0.04693 | 0.08 | 0.06250 |
| Arrow train | 2 | 654 | 0.05116 | 0.08 | 0.06250 |
| PAX train | 4 | 697 | 0.04646 | 0.08 | 0.06250 |

The verifier SHA-256 is
`7cba686ce5db150471c4981286d36074af01356a706f8f98256e798ac8ce8ed0`.

## Limits

This is ten-tick sampled native Pig/default-model evidence. The separate
[linkage checkpoint](../linkage-clearance/acceptance.md) covers the exported
parent struts and carrier-axle blocks. It does not certify continuous swept
clearance, player meshes or cosmetics, client rendering, intentional seat
contact, linkage not represented by those native blocks, optional
decoration/resource overrides, real-client operator behavior, general strength
or the recovery lifecycle matrix. FD402, FD404 and the 0.9 release gate remain
open.
