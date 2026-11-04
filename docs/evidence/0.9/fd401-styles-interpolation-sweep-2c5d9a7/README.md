# FD401 swept Styles clearance between native snapshots

The [certificate](report.json) was produced by
[`audit-recovery-swept-styles.py`](../../../../scripts/audit-recovery-swept-styles.py)
from the retained single- and two-car native Arrow traces and the five
[sampled Styles reports](../fd401-banked-player-clearance-a5df9b8/README.md).
It checks both default spine choices for each train size and the saved
Original → single → double → Original → single whole-section plan. It did not
start a new GameTest or client. The script SHA-256 for this result is
`c07b584b9fc1137596cc67e99d05dfd154ab4ede9d5009dd82679455184ed6d4`.

The pinned Sable 2.0.3 snapshot interpolator calls `Pose3dc.lerp`, whose
implementation linearly interpolates position and uses quaternion `nlerp` for
orientation. The inspected factory/native spawner set each body rotation point
to its plot center, and default pose scale is one. The exported native pose
origin is that transformed center. For a rigid local point at radius `r`, the
script bounds all motion between adjacent snapshots by endpoint translation
plus `2r sin(angular_change/2)`. Subtracting this from a sampled separating
lower bound is conservative for the intervening interpolation. It checks every
reported tick interval; each occupied fixture eye remains fixed in its carrier
coordinates to within `2.7e-13` blocks.

| Configuration | Poses | Largest wheel motion | Wheel swept lower bound | Carrier intervals checked against local track mesh | Local swept lower bound |
|---|---:|---:|---:|---:|---:|
| Single spine, one car | 5,768 | 0.026413 | 0.022497 | 160 | at least 0.03 |
| Double spine, one car | 5,768 | 0.026413 | 0.022497 | 160 | at least 0.03 |
| Single spine, two cars | 6,548 | 0.026095 | 0.022687 | 307 | at least 0.03 |
| Double spine, two cars | 6,548 | 0.026095 | 0.022687 | 307 | at least 0.03 |
| Whole-section transition, two cars | 6,548 | 0.026095 | 0.022687 | 307 | at least 0.03 |

All are above the required 0.02-block gap. The sampled global minimum is set
by the native full-spin wheel envelope beside an Original running support. The
remaining body, native-rider and conservative vanilla-player envelopes use the
same rigid sweep bound. Intervals whose coarse whole-body bound could exhaust
the global sampled margin are checked directly against the style-specific
nearby track mesh. Their `0.03` result is a capped lower bound: no triangle
enters that search distance after accounting for motion. The rider/rack gap is
invariant under their common carrier pose; neighboring cars have more than a
block of sampled separation. The JSON report pins the archived compressed and
decompressed inputs, five sampled reports' source hashes, and the inspected
Sable/companion JAR hashes.

This certifies continuous **between-snapshot rendered interpolation** over the
retained default-model route and seat envelopes. The retained trace has only
one pose per game tick. It cannot certify a different physics-substep path,
Sable's bounded client extrapolation past the latest snapshot, armor, held
items, capes, elytra, modded cosmetics, arbitrary resource replacements, or
track-to-decoration self-clearance. It also does not replace the separate
client-visible ride evidence.
