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
