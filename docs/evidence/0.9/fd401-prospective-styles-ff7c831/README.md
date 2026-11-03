# Prospective Styles recovery geometry — frozen ff7c831

This is offline, one-tick sampled default-model geometry evidence using the
[isolated native exports](../fd402-isolated-base-ff7c831/README.md) from source
`ff7c831fc2e72c640958fa265486a100fd5720d6`. The frozen audit source lives at
`/tmp/fdclean-ff7c831`; each metadata file records the exact command, source,
inputs, tool hash, timestamps and process exit. No Minecraft process was launched
for these analyses. NumPy was 2.5.4.

| Proposed configuration | Result | Samples | Minimum separating bound | Required gap |
| --- | --- | --- | --- | --- |
| Single-car Arrow, single spine | PASS | 5,768 | 0.04891048 blocks | 0.02 blocks |
| Single-car Arrow, double spine | PASS | 5,768 | 0.04891048 blocks | 0.02 blocks |
| Two-car Arrow, single spine | PASS | 6,548 | 0.04878235 blocks | 0.02 blocks |
| Two-car Arrow, double spine | PASS | 6,548 | 0.04878235 blocks | 0.02 blocks |

Both completed single-car cases reconstruct five styled sections, 327 spine
pieces, 86 ties, 88 crossbraces, 10 endpoint bridges and 176 lowered control
mounts. All five sections are visited, with no uncertified body/track pairs.
The limiting pair is a native recovery-cart upstop wheel envelope against an
original outboard support. The report separately retains rider, linkage,
rider/rack and neighboring-body measurements. Rider volumes use native fixture
passengers, not player meshes.

The saved track appearance remains distinct from this proposed configuration.
A pass does not prove the Styles models were selected or rendered in a client.
This audit excludes track-to-decoration self-clearance, continuous swept motion,
player meshes, unmodeled linkage, custom resource models and full-turn ride
acceptance. It does not test the later draw-failure fallback correction and does
not close FD401, FD402 or the 0.9 release gate.

Both two-car train configurations also visit all five sections with 6,548
sampled poses and no uncertified body/track pairs. The report retains
neighboring-body measurements for the coupled consist and recovery cart.

Completed report verification matched the report/tool/source hashes, process
exit 0 and both input hashes to the committed isolated export manifest. It was
a provenance/result review, not a second independent geometry implementation.
All four uniform-style analyses exited 0. Their reports retain the exclusions above;
no mixed-style transition or rendered-client acceptance is inferred.
