# FD1701B Art — operator path audit

Prepared 2026-11-17 on `main` at assignment baseline `7147f63`.
This is an independent source/UI audit, not multi-train acceptance and not a
claim that the 1.2 contract is implemented.

## Existing 1.1 path

The current player-facing operator flow is server-authoritative:

1. `OperatorScreen` polls `ToolsNetwork.State` and renders the latest
   `RideTelemetry` snapshot.
2. `OperatorPanel.sample` resolves the complete linked consist, rider count,
   restraints, capture/retention, controller identity, power, recovery state,
   fault and dispatch denial on the server thread.
3. `EditorNetwork` sends guarded actions with the displayed vehicle revision;
   stale revisions are rejected before a state-changing operation.
4. The panel enables Dispatch only when the authoritative snapshot says it is
   ready. Server-side validation remains the final authority.

The existing panel already exposes the vocabulary needed for a ride-local
multi-train workflow: whole-consist count, occupied/capacity, phase, controller,
power, recovery, fault and a specific dispatch denial. A 1.2 contribution should
extend this vocabulary only for the lead's declared circuit/count contract.

## Existing example path

`ExampleLayouts` registers `/fd demo ...` examples and builds bounded, empty
near-origin circuits with an associated station and optional native drive. The
PAX example has a separate `ShuttleExamples` path and must not be treated as a
generic station example. Existing example setup checks clear space, coordinate
and profile/vehicle capacity before mutating the world.

## Minimum player workflow to document

The final 1.2 guide should describe only the declared fixed circuit and
homogeneous consist. The player-facing sequence is:

1. Build or invoke the usable example and inspect the named circuit/status.
2. Complete one train's consist, board it, close restraints and retain it at
   the station.
3. Admit a second train only after the server reports its reserved route blocks
   are free; an unresolved reservation or occupancy must deny entry.
4. Dispatch from the authoritative station/controller and observe the panel's
   block/reservation state. Braking and dispatch remain separate operations.
5. Recover a stopped or interrupted train through the declared explicit recovery
   action, then verify reservations and occupancy before another admission.
6. Remove or unload a train only after its reservation is released; stale or
   unresolved occupancy must remain fail-closed.

The circuit length, train limit and supported family are now fixed by the lead's
contract; the exact panel labels and action numbers are recorded above. This audit
still does not claim that a connected-client acceptance run has passed.

## Transferred 1.2 contract

The lead's declared contract is `arrow_fixed_circuit_v1`: one fixed circuit with
eight Arrow segments, six route blocks and at most three simultaneous trains.
Each train is a homogeneous consist of original Arrow cars. Admission reserves
the current and next block. The transferred helper
`Fd12ArtExample` delegates configuration and admission to the lead-owned
`RideLocalOperations` implementation and does not create another authority path.
The physical example command is `/fd demo arrow`; the operator panel exposes
`route`, `route_trains`, `route_block`, `route_denial` and `route_managed`.
**Admit route train** sends action94 and **Resume route** sends action95 through
the revision-guarded `EditorNetwork` path.

## Evidence boundary and handoff

This audit deliberately does not edit the shared panel, editor network, example
registration or central guides. The lead owns those files until a coherent bundle
is explicitly transferred. Art's unique evidence namespace is
`docs/evidence/1.2/art/`; helper scripts, if needed, use `scripts/fd12-art-*`.

The acceptance owner should consume the integrated contract and its normal client
workflow rather than this audit alone. Mechanisms owns occupied admission,
dispatch/separation and interruption/restart acceptance; this record only maps
the existing player path and the minimum documentation questions.
