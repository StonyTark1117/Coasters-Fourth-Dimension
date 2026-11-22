# FD1701C — bounded multi-train acceptance plan

Prepared 2026-11-17 on `main` at the FD1701B contract handoff. This is a
finite test plan and evidence map, not a claim that the implementation or a
runtime qualification has passed.

## Contract under test

The accepted FD1701B boundary is:

- circuit `arrow_fixed_circuit_v1`, one fixed eight-segment Arrow circuit;
- six exclusive route blocks;
- at most three simultaneous trains;
- homogeneous original Arrow consists only;
- each admitted train reserves its current and next block;
- whole-consist occupancy and readiness are required;
- dispatch and braking are separate guards;
- missing native bodies, broken links, unload, and restart-restored state fail
  closed until explicit recovery/resume.

The test fixture must identify each train and every linked car. A single-car
observation is insufficient for whole-consist acceptance.

## Accepted evidence reused

FD1701A remains closed and is reused as authority evidence. In particular,
`docs/evidence/0.8/authority/native-result.txt` records exclusive ownership,
held transfer, stale/replayed request denial, provider exclusivity,
save/load-held state, explicit resume, immediate missing-controller propulsion
denial, replacement transfer, and schema-7 upgrade. The occupied handoff in
`docs/evidence/0.8/authority/README.md` proves the player/synchronization path
for one occupied Wing car with closed restraints and a transferred hold.

That evidence does not cover route reservations, a multi-train scheduler, or
the FD1701B circuit limit, so those cases remain in this plan and are not
replayed through generic authority tests.

## Finite missing-case matrix

| Case | Required observation | Pass criterion | Planned evidence |
| --- | --- | --- | --- |
| C1 — limit and admission | Three identified trains on the declared circuit; a fourth admission attempt | Exactly three trains can hold current/next reservations; the fourth is denied without changing existing reservations | `fd1701c-c1-limit.json` plus native assertion output |
| C2 — occupied dispatch/separation | Occupied train dispatch with another train occupying or reserving the next block | Dispatch succeeds only when the complete consist is ready and its route is safe; braking/separation keeps the other train outside the reserved blocks | `fd1701c-c2-dispatch-separation.json` plus one occupied interaction record |
| C3 — removal release | Remove one identified train after stopping it | Its identity and remaining trains persist; its current/next reservations release exactly once and a waiting train may then be admitted | `fd1701c-c3-removal.json` |
| C4 — unload/reload | Unload or chunk-unload a train while its reservation/occupancy is unresolved, then restore the body | Admission and dispatch fail closed while unresolved; restored identity and reservations are either reconciled or remain denied until explicit resume | `fd1701c-c4-unload.json` |
| C5 — interruption/restart | Interrupt and restart the declared fixture with unresolved occupancy | No train enters a block from stale state; the restart path preserves identity or denies entry until explicit recovery/resume | `fd1701c-c5-restart.json` |
| C6 — player/sync boundary | One occupied-player admission or recovery interaction paired with native state readback | The player-facing result agrees with server-authoritative occupancy/reservation state; a missing or stale sync cannot grant entry | `fd1701c-c6-occupied-sync.json` and the corresponding client/server logs |

C2 and C6 are the only cases that need occupied interaction. Native state
assertions should carry the rest of the matrix. No arbitrary junction network,
mixed-family consist, automatic depot, or redundant FD1701A authority replay is
in scope.

## Execution boundary

The uniquely namespaced helper/script surface is reserved for this package:
`scripts/fd12-mechanisms-*` and any uniquely named FD12 test helper. Shared
production classes, shared GameTest registries, central queue files, and the
roadmap remain lead-owned unless an exact path transfer is acknowledged.

Dependent assertions stay deferred until the lead's reservation, occupancy,
identity, persistence, and explicit-resume APIs are present on `main`. The first
qualification run must use the lead's declared train limit and fixed circuit,
then preserve the native output and any occupied client/server readback under
the evidence names above.
