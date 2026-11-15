# FD403A current editor lifecycle recheck — 2026-11-14

This is a narrow carry-forward check for the current dirty checkout after the
Arrow acceptance records were collected. The current changes to
`RotationEditorScreen`, `EditorNetwork`, `RotationTool`, and `FoundationSync`
touch the shared editor response path. This recheck covers the two distinct UI
branches: an accepted non-dispatch operation refreshes the still-open editor,
and an accepted dispatch closes it. The previously accepted construction,
route, occupied circuit, capture/unload, cold-reopen and Arrow-teaching evidence
remain in the [cumulative FD403A record](../fd403a-circuit-reconciliation-20261113/README.md).

## Run identity

- Checkout: `main`, HEAD `549b130b92bf56c4bd56088560a33d4fe442d0e6`, with
  uncommitted implementation and teaching edits present; no source was changed
  for this recheck. The editor acknowledgement changes loaded by this runtime
  were committed as `b95972df` while the client remained open; the built JAR
  hash below identifies the tested artifact.
- Runtime JAR: `0.9.0-dev.27`, SHA-256
  `53c2a3ea22d7959449830859c4438efb8830f6e8920a224b764ffb4bf6fcf3f2`.
- Java 21.0.13 / NeoForge 21.1.248, packaged launch scripts prepared with
  `-PfdPackaged -PfdValidation`.
- Owner: `01a1d49d-7763-76b2-8b0c-6f5b337f0ea2`.
- Run root: `/home/braydon/.local/state/fourthdimension-runs/fd403a-editor-ack-recheck-20261114`.
- Stopped source world copied from the accepted FD403A independent run to
  `server/FD403ArrowEditorAck`; original remains untouched.
- Private Xvfb `:300`, game port `25614`, RCON port `25615`. The client used
  `-Xmx1536M` inside an owned 6 GiB scope; the server used `-Xmx2048M` inside
  an owned 4 GiB scope. Host available memory before client launch was 21 GiB.
- Client, server and display exited cleanly after the check. The server saved
  all three dimensions and all Sable sublevels.

## Results

F3 identified the targeted `fourthdimension:construction_controller` at local
block `(20481032,129,20577288)`. A normal right-click with the selected Rotation
Tool opened the editor for intended chassis
`819ce77e-8cd5-4eaf-831a-c45de09908cb`; validation logs record the tool target,
resolved body, and editor-open packet.

The editor initially read `Arrow controlled | Ready | Restraints open`. One
normal menu click on **Close restraints** received `FD_EDITOR_RESULT action=11
success=true`; the response message appeared, the editor stayed open, and its
refreshed status read `Ready | Restraints closed`.

One normal **Dispatch** click received `FD_EDITOR_RESULT action=13 success=true`.
The screen closed after the result arrived. A read-only `fd status` then showed
the intended carriers `62191fac-a0ae-441f-b94a-b1c9f7dd1109` and
`b86810c6-bd7e-4efc-8e20-46bed47ecba2` with `capture=false`, `occupants=0`;
`Dev` had no `RootVehicle`. The source changes to `VehicleOperations.capture`
and its underlying capture semantics are absent; accepted normal capture,
restraint opening, unload and persistence evidence therefore carries forward.
The new generic response/rebuild path was exercised by action 11, and its
dispatch-specific close path by action 13.

A later attempted right-click did not reach the server after the dispatched
train moved away from the last confirmed target. There is no corresponding
`FD_ROTATION_USE_ON` entry. The client remained connected until normal
disconnect; this is an uncredited target miss, not evidence of a product
regression. No repeat aim/click was sent.

## Raw evidence

- [`input.jsonl`](/home/braydon/.local/state/fourthdimension-runs/fd403a-editor-ack-recheck-20261114/evidence/input.jsonl)
- [`console.jsonl`](/home/braydon/.local/state/fourthdimension-runs/fd403a-editor-ack-recheck-20261114/evidence/console.jsonl)
- [`server.log`](/home/braydon/.local/state/fourthdimension-runs/fd403a-editor-ack-recheck-20261114/evidence/server.log)
- [`client.log`](/home/braydon/.local/state/fourthdimension-runs/fd403a-editor-ack-recheck-20261114/evidence/client.log)
- [Editor opened](/home/braydon/.local/state/fourthdimension-runs/fd403a-editor-ack-recheck-20261114/evidence/20261114T071307.552413Z-after.png)
- [Close restraints acknowledged and refreshed](/home/braydon/.local/state/fourthdimension-runs/fd403a-editor-ack-recheck-20261114/evidence/20261114T071344.712659Z-after.png)
- [Dispatch acknowledged and editor closed](/home/braydon/.local/state/fourthdimension-runs/fd403a-editor-ack-recheck-20261114/evidence/20261114T071512.074367Z-after.png)

FD403A remains complete. This scoped check carries its shared editor lifecycle
claims forward for the current checkout; it does not reopen construction,
route-motion, full-turn, persistence, or family-teaching cases.
