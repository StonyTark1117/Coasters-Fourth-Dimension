# 0.5 vehicle specifications — current handoff

- **Owner:** 0.5 vehicle specifications, chat `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`.
- **Assignment:** FD1401, FD1502, FD1501, FD1302, FD1301, FD1601S,
  FD1702S, FD1801S, FD1802S and FD1901S. Specifications only; Wing and runtime
  changes belong to the lead.
- **Verified state:** all ten assigned specifications complete; native interface review
  and analytic dimension/transform/link check passed. Later implementations remain open.
- **Next action:** lead selectively integrates the ready files and reconciles these ten
  specification rows for FD503. No further specification work or runtime run is pending.
- **Last failed condition / changed precondition:** design review found a Conventional
  Flying brace gap of .18445 blocks; specified relocation yields .33445 and the analytic
  check passes. No runtime failed. Axis primary product URL was 404; exact unverified
  geometry/control choices are explicitly original game design, supported by the
  indexed primary concept and current manufacturer history.
- **Workspace:** `/home/braydon/Documents/Coasters: Fourth Dimension`, `main`,
  dispatch HEAD `a4ef96d775b389ae0a5e608ac80a37533c2592ac`; lead integration HEAD observed
  `f7825405325a732f305bc72efe4f0832562dc30f`. Owned uncommitted files are the twelve
  design files listed in `check-design.py` and this evidence directory. Earlier dirty files remain
  owned by their existing authors; this owner edits only `docs/design/0.5/` package
  files and this evidence directory. No staging or commits.
- **Runtime:** none launched or owned; no client, display, world, server, port or scope.
- **Evidence:** [native interface inspection](native-interfaces.md), retained
  `native-api.txt` and `native-bytecode.txt`; [completion mapping](acceptance.md),
  [analytic check result](design-check.json). Package links follow.

## Closure semantics

These records close discovery/specification criteria only. Future A/B/C packages
retain their native/occupied implementation and content criteria. Reuse the existing
0.4 paths and new proofs when unchanged; no old family is reopened. Native API
inspection establishes representability and identifies extension seams, not a
claim that an unimplemented vehicle has passed a physics or occupied-client run.

## Ready specifications

- [FD1401 Pipeline](../../../design/0.5/FD1401-pipeline.md): complete selected reference, dimensions, state/ownership, native feasibility review and finite occupied prototype plan.
- [FD1502 Roller Ball](../../../design/0.5/FD1502-roller-ball.md): complete specification and finite native/occupied prototype plan.
- [FD1501 S1/S2 Spinning](../../../design/0.5/FD1501-spinning.md): complete specification and finite native/occupied prototype plan.
- [FD1302 Axis](../../../design/0.5/FD1302-axis.md): complete specification and finite native/occupied prototype plan.
- [FD1301 Suspended](../../../design/0.5/FD1301-suspended.md): complete specification and finite native/occupied acceptance handoff.
- [FD1601S Fixed Inverted](../../../design/0.5/FD1601S-fixed-inverted.md): complete specification and finite native/occupied acceptance handoff.
- [FD1702S Dive](../../../design/0.5/FD1702S-dive.md): complete specification and finite native/occupied acceptance handoff.
- [FD1801S Conventional Flying](../../../design/0.5/FD1801S-conventional-flying.md): complete specification; Flying/yaw uses the shared transform and winding contract.
- [FD1802S New-generation Flying](../../../design/0.5/FD1802S-new-generation-flying.md): complete specification; Flying/yaw uses the shared transform and winding contract.
- [FD1901S Powered Inverted Yaw](../../../design/0.5/FD1901S-powered-inverted-yaw.md): complete specification; Flying/yaw uses the shared transform and winding contract.
