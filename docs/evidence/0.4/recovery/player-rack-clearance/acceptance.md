# FD402 sampled vanilla-player carrier-rack clearance

This checkpoint closes the one-tick sampled default-player-to-rack geometry gap
for all five recovery families and the original linked Arrow/PAX trains. It is
not rendered-client or continuous-physics evidence, and it does not close the
remaining FD402 operator, lifecycle, or propulsion boundaries.

## Production correction

The previous conservative player envelope cleared original track but filled the
empty space between independently moving limbs. Applying that single volume to
the carrier rack correctly failed as uncertified. A part-level audit then
isolated the real near pair: the vanilla head/hat rotation enclosure and the
non-seat carrier axle.

Production now keeps the Arrow/Legacy axle block six model pixels behind the
Arrow seats, moves the Free Spin/PAX and Revised axle rear face seven pixels
behind its block, and gives Legacy's two opposed seat rows a dedicated centered
axle. Collision/selection shapes match the rendered elements. Existing Legacy
carrier bodies idempotently replace the former shared axle with the dedicated
block when loaded; new Legacy cars use it immediately.

## Audit

[`audit-recovery-mesh-trace.py`](audit-recovery-mesh-trace.py.txt) reuses the
current-artifact one-tick native poses and route frames. The player track check
retains the conservative vanilla 1.21.1 skin/outer-layer enclosure. The new rack
check uses separate rendered parts: exact jacket and riding legs, the full
vanilla seated arm idle-bob range, and a rotation-independent enclosure for the
head and hat. It checks every part against every non-seat rack element in the
same carrier-local frame. Armor, held items, capes, elytra, slim-arm-specific
geometry, modded cosmetics, and arbitrary resources remain outside this result.

The archived Legacy trace predates the new block identifier. The verifier makes
four explicit `carrier_axle` → `legacy_axle` substitutions for that route only;
all poses, riders, mounts, frames, and other blocks remain the recorded inputs.
The report exposes this count. The production conversion and fresh-construction
paths compile, but this checkpoint does not relabel the Legacy substitution as a
new runtime trace.

| Route | Cars | Samples | Player/track | Player/rack | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| Arrow | 1 | 5,768 | ≥0.08 | 0.06532928 | pass |
| Legacy | 1 | 5,845 | ≥0.08 | 0.28407928 | pass |
| Free Spin | 1 | 5,873 | ≥0.08 | 0.06532928 | pass |
| Revised | 1 | 5,865 | ≥0.08 | 0.06532928 | pass |
| PAX | 1 | 5,767 | ≥0.08 | 0.06532928 | pass |
| Arrow train | 2 | 6,548 | ≥0.08 | 0.06532928 | pass |
| PAX train | 4 | 6,977 | ≥0.08 | 0.06532928 | pass |

All routes retain the earlier body/track, wheel, linkage, native-rider/rack, and
neighbor checks with no uncertified pair. The train neighbor lower bound remains
above 1.276 blocks. Java compilation and all resource JSON parsing pass after the
geometry correction. [`verify.py`](verify.py) checks the archived traces and
frames, report/source/resource hashes, route counters, substitution boundary,
and every clearance requirement without rerunning the GameTest matrix.
