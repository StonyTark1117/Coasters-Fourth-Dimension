# FD402 current-artifact one-tick recovery rider clearance

**Result:** The current combined-profile GameTest run passed all 50 required
tests. Its new five-family single-car and two linked-train traces pass the
corrected independent native/default-model clearance audit. Every original
rider is checked against non-seat rack volumes at every sampled tick. This
closes the previously open exact current-trace rider-to-rack audit; it does
not close FD402 rendered operator, client mesh, strength or lifecycle gates.

## Provenance and method

- Source code commit `a6e18c906a8e1f9f33880030dccd7b0be1b53b16`,
  Minecraft 1.21.1 / NeoForge 21.1.248; combined Track Styles 1.2.0 and
  Engineered 1.1.1 profile. Java 21 command:

  ```sh
  ./gradlew runGameTestServer --offline --no-daemon --console=plain \
    -PfdIntegrations=styles,engineered -PfdPerformanceTargets
  ```

- The successful run used a disposable GameTest directory on tmpfs to avoid
  host-disk save stalls. It took 5m 23s, reported `All 50 required tests
  passed :)`, saved all dimensions and exited normally. An earlier identical
  run on a disk-backed disposable world was interrupted during long Sable
  synchronous saves; it yielded no acceptance result.
- The [independent verifier](../../../../../scripts/audit-recovery-mesh-trace.py)
  pins renderer geometry, the native route/trace producers, and the base
  Coasters/Minecraft model resources. Its `BankedRecoveryExamples` source pin
  was updated for a later one-line `captureOriginalCurve` metadata call; the
  geometry and test-producer pins are unchanged. The base Coasters JAR SHA-256
  is `f80b4c3cdf9fa6188804967147761b0373e150a6eadbdc53626e5ac9700b2049`;
  Minecraft client resources SHA-256 are
  `499f6897d1837516680f3114072d8106e11c9adcd933fe5cf051b551089b0c99`.
  Current verifier SHA-256 is
  `1c0cab088116ffdd079400cc49b18754eb24d5917fe7b4e9e0f186cefd0f750d`.
- The verifier reconstructs original track tubes and default model volumes
  against each recorded native chassis, lower-bearing, wheel, linkage, rack
  and rider pose. Running rails are excluded only for intended wheel contact.
  It requires stable original rider identities and carrier assignments,
  records every game tick, and fails a route with a missing rider-to-rack
  check or an uncertified close pair.

| Route | Cars | One-tick samples | Rider envelopes | Rider/rack checks | Smallest model/track bound | Rider/track | Rider/rack |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Arrow | 1 | 5,768 | 23,072 | 46,144 | 0.04891 | ≥0.08 | ≥0.06250 |
| Legacy | 1 | 5,845 | 46,760 | 93,520 | 0.04395 | ≥0.08 | ≥0.06250 |
| Free Spin | 1 | 5,873 | 46,984 | 93,968 | 0.04394 | ≥0.08 | ≥0.06250 |
| Revised | 1 | 5,865 | 46,920 | 93,840 | 0.04415 | ≥0.08 | ≥0.06250 |
| PAX | 1 | 5,767 | 23,068 | 46,136 | 0.04415 | ≥0.08 | ≥0.06250 |
| Arrow train | 2 | 6,548 | 52,384 | 104,768 | 0.04878 | ≥0.08 | ≥0.06250 |
| PAX train | 4 | 6,977 | 111,632 | 223,264 | 0.04391 | ≥0.08 | ≥0.06250 |

Every route visits all five banked-recovery sections, exceeds the required
0.02-block separating bound, reports `passes=true`, and has no uncertified
pairs. The [single](single-results.json) and [linked-train](train-results.json)
reports record all source, input and resource hashes. The [compressed raw
traces](single-trace.jsonl.zst) and [train trace](train-trace.jsonl.zst), native
frames, and [GameTest log](gametest.log.zst) are preserved. `SHA256SUMS` and
[verify.py](verify.py) check the archived inputs and report counters.

This is one-tick sampled native pose and default block-model evidence. It does
not certify continuous physics-step sweep, real player meshes, cosmetic
resource overrides, client rendering, or the rendered recovery operator path.
