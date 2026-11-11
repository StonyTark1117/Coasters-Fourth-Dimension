# FD402 permanent interruption validation (PAX, 2026-11-11)

This run used the combined styles + Engineered + FE validation build with Java 21, a PAX four-car train, and two rendered clients (`Dev` and `Observer`). The helper source is `RecoveryInterruptionValidation` (commit `df0e4b2`).

## E6 coupling removal

At tick 1589 in the first fresh run, the helper removed the exact recovery-bearing edge (`4e52e45a-2a47-4349-b1e8-2756b46794fb` ↔ `0f627947-e096-4cf5-9043-f41e71a7b796`) while the occupied four-car group was braked. The helper's summary `chassis=0642e891…` names the first car in the group, not the directly coupled end car; the native unlink log identifies the actual edge. The original four chassis IDs, eight carriers and both rider seat assignments remained present; Dev's status confirmed its mount, and no rider dismount occurred. Native links retained the original train and omitted the bearing. Subsequent status at tick 1883 reported `stopping=true`, `maintenance=true`, `recoveryCartRemoved=false`, and the production unavailable-equipment fault on all four cars. No automatic resume occurred during the removed-link hold.

The native unlink deletes the constraint; this is actual coupling removal, rather than temporary native body holding with a dormant constraint. The later explicit relink at tick 2082 creates a new constraint between the same bodies and supplies the separate restoration observation. It does not establish permanent bearing destruction. Fixture automation then started towing; that is excluded from production no-auto-resume acceptance. A later bearing-removal command was correctly rejected with `Attach and brake recovery before removing its bearing` and is not E7 evidence.

E6 closes cumulatively from this two-client combined-profile retained state and the accepted [native broken-link rejection, detach and unload](../operator-acceptance.md#native-validation), with [normal linked-train detach](../linked-train-fresh-process-656725bc-20261105/README.md) supplying unchanged cleanup mechanics. No new cleanup implementation or rider path was introduced by the validation helper. The explicit relink after the measured removed-link hold is disclosed; this record does not claim that the bearing remained uncoupled for the remainder of the fixture session.

## E7 permanent bearing removal

A fresh paused fixture run used the same combined build and two rendered clients. At tick 883, `remove_bearing` permanently removed bearing `0a7b8957-62ca-42c9-9eee-1492e0d56372` from the attached, braked four-car group. The helper reported `recoveryCartRemoved=true`; all four chassis, eight carrier bodies, and both rider seat assignments remained present and mounted. Native links retained the original four-car train and omitted the removed bearing. Each car reported `Recovery bearing permanently removed: brake and disconnect recovery`, with `stopping=true` and no automatic resume. The raw action and status are preserved in [e7-server-latest.log.gz](e7-server-latest.log.gz).

The prior rejection after the temporary relink remains a negative control and is not used as E7 evidence.

The E6 raw server output is [e6-server.log.gz](e6-server.log.gz), with both real clients' logs adjacent. The originally copied `server-latest.log.gz` is the later malformed-pause setup attempt: it contains a different group and no successful removal. It is preserved as excluded setup evidence and must not be cited for E6.
