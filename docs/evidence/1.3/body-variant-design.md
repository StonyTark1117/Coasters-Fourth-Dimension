# FD2102 finite body roster and implementation contract

Design recorded 2026-11-22 on primary `main`. Implementation follows the
accepted finite base work in [base-refinement.md](base-refinement.md).
This document defines the feature contract; runtime evidence is recorded separately.

| Body ID | Eligible vehicle | Visible geometry change | Preserved interfaces |
| --- | --- | --- | --- |
| `base` | Existing base | Current accepted original geometry | Entire current contract |
| `wing_compact` | Original fixed Wing only | Narrower stepped rear fairing and shorter arm fairings around the same cushions, with exposed fixed uprights | Four seat anchors, mounting shoe, metal pan/uprights, cushion contact, head pad, restraint hinges and complete restraint mesh; all sixteen original upholstery colors |
| `fixed_inverted_open_frame` | Original Fixed Inverted only | Replace broad painted rear shell panels with separated rails and crossmembers surrounding the same padding | Four-seat row, repaired gantry and rear spars, underpan rails, cushion/head contact, restraint hinges and mesh, outer-seat support |

Both changes are geometric choices within the refined base envelope. Neither
adds track, capacity, motion, camera behavior, recipes or a new vehicle identity.
The existing inventory car item constructs the base car. The body picker shows
a matching miniature for each choice; body selection applies to an existing car.

## Appearance and persistence

Use the existing editor's Tools appearance path. Show this picker only for
eligible vehicles. Choosing a tile previews locally; Apply sends the choice
through the server's existing editor access and revision checks. Leaving or
cancelling clears the preview. The server additionally requires every car in
the consist to be empty, stopped and captured, matching the palette guard.
Only the selected car's body ID and revision change.

Store `body` independently of mechanical `variant`, upholstery `palette` and
optional provider `appearance`. Missing saved fields mean `base`. Preserve an
unknown saved identifier for future availability, but resolve unknown or
wrong-family identifiers to the base geometry. Reject invalid new selections.
A known choice whose baked model is unavailable also renders the base geometry.
Resource reload discards baked-model availability and applied-render caches.
The open picker retains only its selected identifier and vehicle UUID, so its
preview can be rebuilt from fresh resources. Leaving the picker or disconnecting
clears that local preview; it never writes saved state.

Model replacement operates below the existing static restraint and palette
wrappers. Substitute only the fixed body geometry; dynamic restraints retain
the current renderer and ownership. Wing follows its existing block facing;
Fixed Inverted remains authored in its native chassis frame, without adding a
facing rotation. Original controls and failure messages remain authoritative.

## Finite verification

- Compare alternate exports with the refined bases: geometric difference,
  unchanged support contacts, cushions, hinges, restraint meshes and maximum
  envelope; all Wing upholstery choices retain their material mapping.
- Target actual native Wing and Fixed Inverted cars through editor apply,
  save/load and client snapshot; prove unrelated serialized state unchanged.
  Exercise stale revision, wrong-family/unknown ID, occupied or moving car,
  and permission denials through the existing entry point.
- Inspect local preview, cancel, apply and matching miniature in the private
  client, then affected occupied open/closed and ride/stop views. Retain
  unrelated mechanics acceptance rather than starting a family campaign.
- Reopen both selected choices after a clean process restart. Demonstrate
  unknown saved-choice and unavailable resource fallback without changing
  mechanical identity or rider state; include resource reload behavior.

Record actual commands, source/artifact hashes, raw results and runtime cleanup
with the completed evidence. These are compatible checks of the required
outcome, not independent stopping gates.
