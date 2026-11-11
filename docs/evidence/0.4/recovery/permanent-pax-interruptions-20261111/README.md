# FD402 permanent interruption validation (PAX, 2026-11-11)

This run used the combined styles + Engineered + FE validation build with Java 21, a PAX four-car train, and two rendered clients (`Dev` and `Observer`). The helper source is `RecoveryInterruptionValidation` (commit `df0e4b2`).

## E6 coupling removal

At tick 1589 in the first fresh run, the helper removed the exact recovery-bearing edge (`4e52e45a-2a47-4349-b1e8-2756b46794fb` ↔ `0f627947-e096-4cf5-9043-f41e71a7b796`) while the occupied four-car group was braked. The helper's summary `chassis=0642e891…` names the first car in the group, not the directly coupled end car; the native unlink log identifies the actual edge. The original four chassis IDs, eight carriers and both rider seat assignments remained present; Dev's status confirmed its mount, and no rider dismount occurred. Native links retained the original train and omitted the bearing. Subsequent status at tick 1883 reported `stopping=true`, `maintenance=true`, `recoveryCartRemoved=false`, and the production unavailable-equipment fault on all four cars. No automatic resume occurred during the removed-link hold.

The native unlink deletes the constraint; this is actual coupling removal, rather than temporary native body holding with a dormant constraint. The later explicit relink at tick 2082 creates a new constraint between the same bodies and supplies the separate restoration observation. It does not establish permanent bearing destruction. Fixture automation then started towing; that is excluded from production no-auto-resume acceptance. A later bearing-removal command was correctly rejected with `Attach and brake recovery before removing its bearing` and is not E7 evidence.

E6 closes cumulatively from this two-client combined-profile retained state and the accepted [native broken-link rejection, detach and unload](../operator-acceptance.md#native-validation), with [normal linked-train detach](../linked-train-fresh-process-656725bc-20261105/README.md) supplying unchanged cleanup mechanics. No new cleanup implementation or rider path was introduced by the validation helper. The explicit relink after the measured removed-link hold is disclosed; this record does not claim that the bearing remained uncoupled for the remainder of the fixture session.

## E7 status

The permanent-bearing action was not accepted in this run because the helper correctly requires an occupied, attached, braked recovery group and the restored fixture had auto-resumed. The raw log preserves the rejection context. A follow-up paused fixture run is required before claiming E7 closure.

The E6 raw server output is [e6-server.log.gz](e6-server.log.gz), with both real clients' logs adjacent. The originally copied `server-latest.log.gz` is the later malformed-pause setup attempt: it contains a different group and no successful removal. It is preserved as excluded setup evidence and must not be cited for E6.
