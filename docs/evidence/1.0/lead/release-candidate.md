# FD905 — accepted release candidate

Complete2026-11-16 UTC. Intended stable version: **1.0.0**, unpublished.
Source `c7bb65938bfbe7e7dd2feecbedbe5d7801b10a08`; candidate17 JAR SHA-256
`77170d33e8a4a6993b3d4c2fc4f70ed3a491f19f83948fa75ced752ed485b5c5`,
currently labeled0.9.0-dev.27. The original [FD1000 freeze](freeze.md),
[exact profile/dependency record](freeze.json) and [bounded amendments](freeze-amendments.md)
remain authoritative for scope. Stable relabeling does not reset qualification.

| Required gate | Accepted evidence / carry-forward |
| --- | --- |
| FD1000 and earlier0.4–0.9/FD917 | Freeze links complete family, operation, presentation and production-art outcomes. No new feature or dependency is introduced. |
| FD901 | [Native compatibility and timing reconciliation](native/README.md), including targeted later fixes; no universal suite replay. |
| FD902 | [Mechanisms phase restarts](../mechanisms/phase-restarts.md) and [supported track repair](../mechanisms/outbound-support/README.md); bounded PAX recovery/normal lifecycle fixes retain their recorded artifacts. |
| FD903 | [Migration and backup](../art/migration.md): real historical state, exact restore and actual two-client riders. Later performance changes do not alter persistence or packets. |
| FD904A/C | [Rendering](../art/rendering.md), [camera](../art/camera-reconciliation.md), production FD917. Candidate17's renderer change passes byte-equivalence and visible reload below, with unchanged geometry/material/pose contracts. |
| FD904B | [Park acceptance](park/README.md): base11/combined13r2 initial baselines, runtime14 sustained1800/memory plateau, all17 mounted views and candidate17 base/combined later-regression checks. |

Candidate17 changes only client track-renderer class entries from runtime14;
[byte and reload proof, resource comparison and original/reconciled verdicts](park/renderer-repair.md)
retain the failures and their bounded resolutions. Existing server/resources are
identical. No continuing retained-memory path was added, so accepted sustained
coverage carries forward. All required measured limits pass under their declared
initial/later roles; current combined tick p95=50.088835ms is below the frozen
later53.871642ms, not a claim of a new initial <=50ms pass.

There are no unresolved required supported-case defects. Historical invalid
camera/helper intervals and earlier failed comparisons remain identified; none
is used as a passing substitute. Additional art preferences and post1.0 work are
outside acceptance. Supported limits remain ±1024 horizontal coordinates,
256sections/64vehicles/8192keys/512spans (8per section), declared family/track and
ordinary-recovery bounds, and the measured park hardware/settings rather than a
maximum-capacity FPS guarantee. Optional providers remain external and optional;
all frozen profiles and pinned versions remain unchanged.

[Base cleanup](park/cleanup-base17.json) and [combined cleanup](park/cleanup-combined17.json)
verify normal unloading, saved worlds/checkpoints and inactive client/server/Xvfb
with PID0. Art/Mechanisms already returned their runtimes and files to lead.
FD1001 now finalizes1.0.0 guidance; FD1002 must reproduce/inspect the stable JAR
and complete the prepared clean-install ride. FD1003 reviews readiness only;
no publication, tag or upload is authorized by this closure.
