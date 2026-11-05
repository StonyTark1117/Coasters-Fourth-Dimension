# FD401B retained-input geometry re-audit

This bundle re-runs the current mesh, Styles-surface, and swept-interpolation
auditors against the retained native recovery exports in
[`fd402-isolated-base-ff7c831/native-exports`](../fd402-isolated-base-ff7c831/native-exports/).
The retained exports came from source `ff7c831fc2e72c640958fa265486a100fd5720d6`;
the re-audit uses the geometry tools and repository source hashes present when
this bundle was produced on `main` at `8b0133e8004d1dad8a07f0e76fcbedb67c1561c8`.

The five sampled Styles rows pass: single-car Arrow with each 4D style, coupled
Arrow with each style, and the mixed Original/4D/double-spine transition. The
minimum sampled separation is `0.0489104813` blocks for single Arrow and
`0.0487823462` blocks for coupled Arrow and the transition. Each has a `0.08`
block minimum for both the vanilla player envelope and fixture rider, with no
uncertified pairs. Both refreshed Original-model Arrow audits also pass, with
minimum sampled bounds of `0.0489461525` single-car and `0.0487823462` coupled.

The swept-interpolation audit passes all five Styles rows. It locally checks 160
single-car intervals and 307 coupled intervals; each row's minimum checked swept
lower bound is `0.03` blocks. The refreshed resource hashes include the current
`carrier_axle` model (`dc5f35bb…`) and current `free_spin_axle` and
`revised_axle` models (`62e8b36f…`). The dependency identities remain the
retained base (`f80b4c3c…`), Minecraft (`499f6897…`), Styles (`42535a83…`),
Sable (`da6c3b66…`), and Sable Companion (`873633e3…`) hashes.

This is an **offline re-audit of retained 2026-11-02 frames and traces**. It
updates calculator and resource coverage for those recorded poses; it does not
re-export current runtime poses, qualify a client renderer, or close the other
FD401B graphics, fallback, lifecycle, or repetition rows. In particular, the
current axle mesh checks do not turn historical frames into a fresh runtime
result. The native export manifest and each JSON report retain exact payload,
tool, source, and dependency hashes.

Run `python3 replay.py` from the project root to regenerate the reports. The
replay needs the retained native exports, the pinned dependency JARs recorded
in `config.json`, Python with NumPy, and the source revision whose hashes are
in the report `sourceHashes`. Run `python3 replay.py --verify-only` to check
the saved bundle hashes, source hashes, dependency identities, and retained
input payload identities without recalculating geometry.
