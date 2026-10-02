# FD402 sampled chassis/carrier linkage clearance

Dev.27 separates the actual linkage block geometry from the general
default-model recovery result. `VehicleFactory` builds the parent wing struts
and every independently posed carrier axle as native blocks; they are neither
decorative track parts nor assumed geometry. The dense recovery verifier now
requires at least one of these elements, tracks every checked element against
the original running/control/spine/support mesh, and rejects an export that
omits all linkage geometry.

This does not invent a generic Arrow rack model. The covered parts are the
specific `wing_strut`, `revised_strut`, `carrier_axle`, `free_spin_axle`, and
`revised_axle` blocks exported from the live parent and carrier sublevels.
Native lower-bearing and spinning-wheel geometry remain separately checked;
their intended wheel-to-running-rail contact is unchanged.

## Combined optional-profile result

The current combined Track Styles/Engineered recovery run produced the live
ten-tick traces below. The verifier's conservative 0.08 result means every
checked linkage volume was farther than four times the required 0.02-block
margin from a candidate tube, so it reports the capped lower bound rather than
an exact closest surface.

| Route | Cars | Linkage volume checks | Certified lower bound |
| --- | ---: | ---: | ---: |
| Arrow | 1 | 5,193 | at least 0.08 blocks |
| Legacy ZacSpin | 1 | 5,256 | at least 0.08 blocks |
| Free Spin | 1 | 15,876 | at least 0.08 blocks |
| Revised ZacSpin | 1 | 17,023 | at least 0.08 blocks |
| PAX shuttle | 1 | 5,193 | at least 0.08 blocks |
| Arrow train | 2 | 11,790 | at least 0.08 blocks |
| PAX train | 4 | 25,128 | at least 0.08 blocks |

The updated verifier SHA-256 is
`03c17b196be5cd3c8cfc51f69f6cf8ebd731a3e51d1ad527fdfe39107a92d196`.
It repins `OriginalTrackRenderer` after its later diagnostic-only change, so
the audit remains fail-closed against the current renderer source.

Reproduce after a Java 21 combined GameTest run:

```sh
python3 scripts/audit-recovery-mesh-trace.py BASE_COASTERS_JAR MINECRAFT_CLIENT_JAR \
  build/runs/gametest/recovery-bank-native-frames.json \
  build/runs/gametest/recovery-bank-mesh-trace.jsonl
python3 scripts/audit-recovery-mesh-trace.py BASE_COASTERS_JAR MINECRAFT_CLIENT_JAR \
  build/runs/gametest/recovery-bank-train-native-frames.json \
  build/runs/gametest/recovery-bank-train-mesh-trace.jsonl
```

## Limits

This remains a ten-tick sampled, source-pinned native block-model audit. It
does not prove continuous swept clearance, player meshes/cosmetics, a client
renderer, optional decoration/resource overrides, or any linkage that is not
represented by the exported native block geometry. FD402, FD404 and the 0.9
release gate remain open.
