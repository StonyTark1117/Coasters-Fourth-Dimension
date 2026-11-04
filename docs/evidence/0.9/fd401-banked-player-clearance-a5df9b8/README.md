# FD401 banked vehicle and vanilla-player clearance

This bounded audit adds the player-rendered volume that the earlier prospective
Styles reports explicitly excluded. Both supported Arrow Styles configurations
pass on the retained native banked recovery route for one occupied car and a
coupled two-car train. The retained Original → single → double → Original →
single whole-section transition plan passes with the same player envelope.

| Configuration | Cars | One-tick poses | Player envelopes | Player/track lower bound | Overall lower bound | Required |
|---|---:|---:|---:|---:|---:|---:|
| `arrow_4d` | 1 | 5,768 | 23,072 | at least 0.08 | 0.04891048 | 0.02 |
| `arrow_4d_dbl_spine` | 1 | 5,768 | 23,072 | at least 0.08 | 0.04891048 | 0.02 |
| `arrow_4d` | 2 | 6,548 | 52,384 | at least 0.08 | 0.04878235 | 0.02 |
| `arrow_4d_dbl_spine` | 2 | 6,548 | 52,384 | at least 0.08 | 0.04878235 | 0.02 |
| Original/single/double transition plan | 2 | 6,548 | 52,384 | at least 0.08 | 0.04878235 | 0.02 |

The player lower bound is capped at four times the required margin, so `0.08`
means no styled-track triangle entered that search distance. The overall limiter
remains the known native upstop-wheel envelope beside an original outboard
support. Every car visits all five route sections. The transition surface retains
2 Original and 3 styled sections, 262 provider spine pieces, 68 ties, 69
crossbraces, 6 endpoint bridges, and 138 lowered control mounts. The reports retain the
chassis, carrier, explicit linkage, full-spin wheel, native fixture rider,
rider/rack, and neighboring-car measurements.

## Player envelope

Create seats reject `FakePlayer`, so the native fixture fills every seat with a
pig. The verifier places a vanilla player at the same seat using the exact eye
offset that remains after the common seat attachment cancels:
`(1.62 - 0.6) - (0.765 - 0) = 0.255` blocks along the live carrier up axis.
Sable rotates riders about that eye using the containing carrier pose.

The envelope reconstructs Minecraft 1.21.1 `HumanoidModel`, `PlayerModel`, and
`LivingEntityRenderer` dimensions. It includes classic and slim base skins,
sleeves, pants, jacket, and hat layer. The actual seated leg transform is used;
rotation-independent head/arm spheres conservatively contain all vanilla gaze
and arm rotations. Relative to the inferred player eye, the retained local bound
is `[-0.93666716, -1.18421303, -0.93666716]` through
`[0.93666716, 0.40935822, 0.93666716]` blocks.

## Boundary

This is source-pinned, one-game-tick sampled default-model geometry. It is not a
client-render observation and does not cover motion between ticks, armor, held
items, capes, elytra, modded cosmetics, custom resource models,
track-to-decoration self-clearance, or complete operator acceptance. The input
poses are the retained native exports from `ff7c831`; the report hashes current
geometry sources, both audit tools, all inputs, provider OBJ resources, and body
models before and after analysis. No live client or shared display was used.

Reproduction paths and dependency identities are in `config.json`. All five JSON
reports have `passes: true` and contain the exact per-run provenance.
