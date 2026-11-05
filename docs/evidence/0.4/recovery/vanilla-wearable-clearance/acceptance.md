# FD402 sampled vanilla wearable clearance

**Result:** The five single-car banked recovery routes and the original linked
Arrow/PAX routes pass a new per-tick audit for vanilla armor, cape and seated
elytra geometry. The carrier axle models and matching selection shapes were
corrected where the audit found real rack conflicts. This closes the bounded
vanilla wearable layer item; it does not claim held-item, arbitrary modded
cosmetic, resource-pack, rendered-client or continuous-trajectory coverage.

## Production correction

The first audit retained the existing 0.02-block requirement and found two
carrier-local problems while every wearable remained at least 0.08 blocks from
the original track:

- Arrow, Free Spin, Revised and PAX axle faces left only
  `0.0168638784` blocks around the rotation-independent outer-helmet envelope.
- Legacy's centered beam occupied the space reached by the clamped cape and
  seated elytra alternatives.

The Arrow axle moved one model pixel rearward. Free Spin/PAX and Revised moved
one pixel rearward while retaining their four-pixel depth. Legacy's beam moved
into the measured gap between its asymmetric live rider envelopes and narrowed
from three to one-and-a-half model pixels. `MechanicalPartBlock` and
`VehicleParts` use the same selection shapes as the JSON elements, and
`generate-content.py` now reproduces all four corrected models instead of its
stale pre-clearance coordinates.

## Independent audit

[`audit-recovery-player-wearables.py`](audit-recovery-player-wearables.py.txt)
extends the exact default-player verifier archived by the earlier
[player/rack checkpoint](../player-rack-clearance/acceptance.md). It refuses a
different baseline verifier, route frames or decompressed trace. The audit
streams the preserved current-artifact traces, requires stable original rider
identities and carrier assignments, and places eleven named envelopes at every
recorded occupied seat:

- outer helmet, chest, seated idle-bob arms and boot geometry;
- inner seated leggings;
- both non-fall-flying elytra wings over the complete folded-to-crouched client
  interpolation; and
- the cape over its complete vanilla clamp ranges, including walk bob and both
  empty/armored chest pivots.

The formulas come from the pinned Minecraft 1.21.1 client
`LayerDefinitions`, `HumanoidArmorModel`, `CapeLayer` and `ElytraModel` code.
Mutually exclusive layers are checked independently rather than combined into a
synthetic player. Each named envelope is tested against reconstructed original
track triangles and every non-seat rack element in the live carrier frame. The
current rack elements are also checked against track at every pose, so the
production model changes do not inherit the previous resource hashes.

| Route | Cars | Ticks | Seats/tick | Wearable/track checks | Wearable/rack checks | Wearable/track | Wearable/rack | Rack/track |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Arrow | 1 | 5,768 | 4 | 253,792 | 507,584 | ≥0.08 | 0.07936388 | ≥0.08 |
| Legacy | 1 | 5,845 | 8 | 514,360 | 1,028,720 | ≥0.08 | 0.04811388 | ≥0.08 |
| Free Spin | 1 | 5,873 | 8 | 516,824 | 1,033,648 | ≥0.08 | 0.07936388 | ≥0.08 |
| Revised | 1 | 5,865 | 8 | 516,120 | 1,032,240 | ≥0.08 | 0.07936388 | ≥0.08 |
| PAX | 1 | 5,767 | 4 | 253,748 | 507,496 | ≥0.08 | 0.07936388 | ≥0.08 |
| Arrow train | 2 | 6,548 | 8 | 576,224 | 1,152,448 | ≥0.08 | 0.07936388 | ≥0.08 |
| PAX train | 4 | 6,977 | 16 | 1,227,952 | 2,455,904 | ≥0.08 | 0.07936388 | ≥0.08 |

All seven routes report `passes=true`, no uncertified pair, and exact counts of
`ticks × occupied seats × 11 wearable parts`. The [single](single-results.json)
and [train](train-results.json) reports pin the compressed and decompressed
traces, frames, passing baseline reports, verifier snapshots, external JARs and
every model resource read. The reports were produced without starting Gradle,
Minecraft, Java or an X display.

After archiving the reports, one isolated offline
`compileJava processResources` run used
`-PfdBuildDirectory=/tmp/fd402-wearable-build`. It completed successfully in
eight seconds. No GameTest, server, client or display was started.

## Reproduction

Install `scripts/requirements-geometry.txt` plus `zstandard`, then run the
following with the inspected Coasters 0.1.5 and official Minecraft 1.21.1
client-resource JARs:

```sh
python3 scripts/audit-recovery-player-wearables.py BASE_COASTERS_JAR MINECRAFT_CLIENT_JAR \
  docs/evidence/0.4/recovery/current-tick-rider-clearance/single-frames.json \
  docs/evidence/0.4/recovery/current-tick-rider-clearance/single-trace.jsonl.zst \
  docs/evidence/0.4/recovery/player-rack-clearance/single-results.json

python3 scripts/audit-recovery-player-wearables.py BASE_COASTERS_JAR MINECRAFT_CLIENT_JAR \
  docs/evidence/0.4/recovery/current-tick-rider-clearance/train-frames.json \
  docs/evidence/0.4/recovery/current-tick-rider-clearance/train-trace.jsonl.zst \
  docs/evidence/0.4/recovery/player-rack-clearance/train-results.json
```

`verify.py` checks the archived files, current production snapshots, raw trace
identities, route counters and all three clearance bounds.

## Remaining scope

Held items and their item-use arm poses, non-armor head-slot renderers, arbitrary
modded render layers and resource overrides remain outside this geometry result.
Production recovery now refuses motion for the held and non-armor equipment cases;
see the [certified rider-equipment interlock](../certified-rider-equipment/acceptance.md).
Observed client rendering, unanchored startup/teardown, the unexposed trajectory
inside one Rapier substep and broader recovery lifecycle/operator acceptance also
remain open. FD402 remains in progress.
