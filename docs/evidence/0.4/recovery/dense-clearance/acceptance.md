# FD402 one-tick sampled native recovery clearance

Dev.29 records the occupied banked-recovery fixtures every game tick, replacing
the prior ten-tick cadence. The trace is read-only and records actual native
chassis, lower-bearing, wheel, rider and carrier-rack poses without changing
motion, forces or saved state.

## Method

scripts/audit-recovery-mesh-trace.py pins the renderer, route design, trace
recorder and both GameTest producers. It rejects any trace whose declared
interval is not one tick, any missing or out-of-order sample, fewer than 5,000
samples per route, substituted body/carrier, absent native rider/rack volume or
incomplete five-section route.

The verifier reconstructs default block-model volumes against original track
tubes and neighboring-car volumes. It combines the ten wheel-model elements
into four conservative full-spin union envelopes, one per row and axle, before
each recorded pose is checked. Those unions contain every rotated wheel element;
the two running rails remain excluded only for intended wheel contact.

## Combined target-gated result

The command below passed all **41 required GameTests** in **4.287 minutes**,
including the FD904 performance target gate. The independent single-car and
original-train audits then passed in 5m49s and 11m03s, respectively.

~~~sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon   -PfdIntegrations=styles,engineered -PfdPerformanceTargets
python3 scripts/audit-recovery-mesh-trace.py BASE_COASTERS_JAR MINECRAFT_CLIENT_JAR   build/runs/gametest/recovery-bank-native-frames.json   build/runs/gametest/recovery-bank-mesh-trace.jsonl
python3 scripts/audit-recovery-mesh-trace.py BASE_COASTERS_JAR MINECRAFT_CLIENT_JAR   build/runs/gametest/recovery-bank-train-native-frames.json   build/runs/gametest/recovery-bank-train-mesh-trace.jsonl
~~~

| Route | Cars | Pose samples | Body/track lower bound | Rider/track lower bound | Rider/rack lower bound |
| --- | ---: | ---: | ---: | ---: | ---: |
| Arrow | 1 | 5,763 | 0.04879 | 0.08 | 0.06250 |
| Legacy | 1 | 5,844 | 0.04401 | 0.08 | 0.06250 |
| Free Spin | 1 | 5,873 | 0.04394 | 0.08 | 0.06250 |
| Revised | 1 | 5,865 | 0.04411 | 0.08 | 0.06250 |
| PAX | 1 | 5,767 | 0.04409 | 0.08 | 0.06250 |
| Arrow train | 2 | 6,548 | 0.04884 | 0.08 | 0.06250 |
| PAX train | 4 | 6,974 | 0.04397 | 0.08 | 0.06250 |

The source-pinned verifier SHA-256 is
b7b704b5d820f615fad163521b52e14edff98deff5323854142b29466340c8eb.
The complete reports are [single-car results](one-tick-single-results.json) and
[train results](one-tick-train-results.json).

## Limits

This is one-game-tick sampled native Pig/default-model evidence. The separate
[linkage checkpoint](../linkage-clearance/acceptance.md) covers the exported
parent struts and carrier-axle blocks. It does not certify continuous
physics-step swept clearance, player meshes or cosmetics, client rendering,
intentional seat contact, linkage not represented by exported native blocks,
optional decoration/resource overrides, real-client operator behavior, general
strength or the recovery lifecycle matrix. FD402, FD404 and the 0.9 release
gate remain open.
