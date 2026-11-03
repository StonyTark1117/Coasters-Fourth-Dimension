# FD402 isolated base native export capture

This is a fresh base-profile Gradle development GameTest-server run from exact
source `ff7c831fc2e72c640958fa265486a100fd5720d6`, executed in the isolated
`/tmp/fdclean-ff7c831` worktree. The server log reports **all 50 required tests
passed** and then shuts down normally. `native-exports/manifest.json` records a
post-exit, byte-verified capture of all five recovery exports.

The native-input and Styles audits are separate offline analyses. This bundle
does not establish optional-provider client rendering, continuous clearance, or
occupied full-turn operation.

`single-decoration-inputs.json` and `train-decoration-inputs.json` record
matching-source structural and transform checks of the uncompressed archived
single-car and train exports, paired with their respective traces. They passed
for 5 routes/25 sections and 2 routes/10 sections, respectively. These reports
are numerical input checks only; they are not a Styles rendering or clearance
result.

`single-original-mesh-audit.json` is the matching offline audit of the captured
single-car recovery trace. It passed all five Original-family routes at a
one-tick cadence, with 5,767–5,873 samples per route, every car visiting each
of the five route sections, and a lowest recorded separating bound of
0.043944863558635916 blocks against its stated 0.02-block requirement.

`train-original-mesh-audit.json` is the matching two-car train audit. Its Arrow
route passed 6,548 samples and its PAX route passed 6,974; every car visited all
five sections, neither route reported an uncertified pair, and the lowest bound
was 0.0439148282541737 blocks against the same 0.02-block requirement.

Both original-model reports are one-tick sampled, original-default-model
geometry only: sampled body/linkage, wheel-envelope and native-rider volumes.
They exclude client rendering, decorations, Styles geometry, player meshes,
arbitrary resources and continuous physics-step clearance. The train calculation
started after the old verifier checked `OriginalTrackRenderer` at startup as
`dc27216c67c5506aa472773ecaf5adb6406166fc5a09d5ab50d4832123262667`, but a
render-fallback change landed while it ran. The older verifier writes source
hashes only when emitting its report, so the train JSON names the later
`d1323d09d93e738ff01140133aa0e7fb83e6bcc7ca99884e3b265dde1c2faba4` bytes.
This is preserved as pre-fix sampled geometry evidence with that source-timing
limitation; it does not certify an immutable current source. Future auditors
reject this situation before report emission.
