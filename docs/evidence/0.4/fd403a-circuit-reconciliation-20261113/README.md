# FD403A Arrow circuit reconciliation — 2026-11-13

This record closes the remaining connected-client Arrow circuit claims by
combining the two compatible runs identified in the
[FD403A handoff](../../../FD403A-CIRCUIT.md). Both packaged JARs have SHA-256
`e5f65c76289a018b64c56c1d8f845fe3b09e920bac93909d783a4b8373fb3972`
(`0.9.0-dev.27`). The run logs, input records, screenshots, and state readbacks
remain in their persistent run roots, linked below.

## Normal C–D construction and traversal

The package owner's connected-client run used private display `:256` and the
intended Arrow chassis
`819ce77e-8cd5-4eaf-831a-c45de09908cb`. After the client reconnected and refreshed
anchor metadata, normal Chain selection and commit installed a 13.5 m powered
span on C–D (`TStart=0`, `TEnd=0.84375`, expected D→C direction). Preparation
removed stale prior-span tags on this copied world; that server-side setup earns
no placement credit. The span itself was selected and committed through normal
connected-client input. The server
readback, normal input sequence, and route samples are preserved in
[`console.jsonl`](/home/braydon/.local/state/fourthdimension-runs/fd403a-final-circuit-20261113/evidence/console.jsonl),
[`input.jsonl`](/home/braydon/.local/state/fourthdimension-runs/fd403a-final-circuit-20261113/evidence/input.jsonl),
and [`client.log`](/home/braydon/.local/state/fourthdimension-runs/fd403a-final-circuit-20261113/evidence/client.log).
Motion readbacks show the occupied intended car traversing C–D in the expected
direction and continuing through the remaining loop sections.

That run did **not** finish capture: severe dedicated-server tick lag timed out
the client during a later pass. It is evidence for normal C–D placement and
route traversal only, not final capture, unload, or reopen.

## Completed occupied circuit and final state

The separately owned [independent circuit run](../fd403a-independent-circuit-20261113/README.md)
completed two occupied returns on the same intended route and vehicle, then
performed normal Capture, confirmed Ready/closed, opened restraints, dismounted,
and cold-reopened the saved world. After rejoin, the controller remained
Ready/open, the player had no `RootVehicle`, and the intended chassis and carrier
identities and route records were present. That copied world retained a disclosed
16 m command-extended C–D span, so it proves circuit operation and final-state
persistence, not C–D player construction.

These records are compatible: the normal-client C–D run proves installation and
direction on the same route edge and chassis; the independent run proves the
complete occupied circuit, return/capture/unload, and reopened usability. The
earlier accepted Arrow record supplies normal recipes, paid connections,
boarding, and the controlled 50 m full turn. The accepted current-artifact
teaching review supplies the rendered Arrow instructions; this circuit produced
no conflicting behavior or caption correction. No generic item-use replay or
second teaching walkthrough is required.
