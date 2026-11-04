# FD402 native physics-substep clearance

This checkpoint closes a bounded part of the FD402 continuous-clearance gate:
every **paired post-physics Rapier half/full step in the sampled interval** for
the occupied, coupled two-car Arrow recovery route clears the current native
track, body, linkage, wheel, rider, vanilla-player, carrier-rack, and
neighboring-car geometry by at least `0.02` blocks.

It excludes 319 sequential startup/teardown trace steps that have no paired tick
sample, does not claim a trajectory that Rapier does not expose inside one
discrete `0.025 s` substep, and originally did not extend substep coverage to
the other four vehicle families. The later
[occupied PAX checkpoint](../pax-native-physics-substeps/acceptance.md) adds the
four-car PAX train; Legacy ZacSpin, Free Spin, and Revised ZacSpin remain open.

## Method

The preserved native run supplies 6,548 one-tick Arrow train poses and a 13,414
step sequential Rapier trace. Exactly 13,095 poses—the 6,548 matching full steps
and 6,547 intervening half steps—fall within the paired sampled interval. The current mesh auditor was rerun against the
preserved Arrow/PAX route frames and tick trace, including the corrected Arrow
carrier axle and part-level vanilla-player/rack check.

[`audit-recovery-native-substeps.py`](audit-recovery-native-substeps.py.txt)
then:

1. requires every full physics step to equal its paired native tick pose bit for
   bit;
2. computes a conservative rigid-body motion bound from each intervening
   half-step to its nearest paired tick pose using the current rendered-resource
   radii;
3. deducts that motion from each sampled track-separation bound and twice the
   maximum motion from the neighboring-car bound; and
4. carries rider/rack and player/rack separation unchanged because both members
   of each pair share the same carrier-local rigid transform.

## Result

| Check | Recorded/sample bound | Substep lower bound | Required |
| --- | ---: | ---: | ---: |
| Body/linkage to native track | 0.04878235 | 0.03598368 | 0.02 |
| Full-spin wheel envelope to non-contact track | 0.04878235 | 0.03598362 | 0.02 |
| Native rider to track | 0.08000000 | 0.06725936 | 0.02 |
| Vanilla player mesh to track | 0.08000000 | 0.06722962 | 0.02 |
| Native rider to carrier rack | 0.08000325 | 0.08000325 (rigid invariant) | 0.02 |
| Vanilla player parts to carrier rack | 0.06532928 | 0.06532928 (rigid invariant) | 0.02 |
| Different cars / ordinary recovery body | 1.27641656 | 1.25081911 | 0.02 |

All 6,548 sampled ticks matched a full physics step, and all 6,547 intervening
half steps were audited. The largest motion from the nearest sampled pose was
`0.01279873` blocks. [`verify.py`](verify.py) validates
the preserved reports, auditor snapshots, source/resource hashes, referenced
trace and frame hashes, arithmetic deductions, route counters, and acceptance
thresholds without launching Minecraft or taking a display.
