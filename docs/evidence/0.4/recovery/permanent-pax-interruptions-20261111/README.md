# FD402 permanent interruption validation (PAX, 2026-11-11)

This run used the combined styles + Engineered + FE validation build with Java 21, a PAX four-car train, and two rendered clients (`Dev` and `Observer`). The helper source is `RecoveryInterruptionValidation` (commit `df0e4b2`).

## E6 coupling removal

At tick 1589 in the first fresh run, the helper removed the exact recovery-bearing edge (`0642e891-8ffb-4a5e-9ebe-43fc8f0bebe0` ↔ `0f627947-e096-4cf5-9043-f41e71a7b796`) while the occupied four-car group was braked. The original four chassis IDs and both rider seat assignments remained present and mounted; native links retained the original train and omitted the bearing. Subsequent status at tick 1883 reported `stopping=true`, `maintenance=true`, `recoveryCartRemoved=false`, and the production unavailable-equipment fault on all four cars. No automatic resume occurred.

The same run then restored the edge at tick 2082 to document the separate temporary-loss path. That restoration auto-started the fixture; a later bearing-removal command was correctly rejected with `Attach and brake recovery before removing its bearing`. It is not claimed as E7 evidence.

## E7 status

The permanent-bearing action was not accepted in this run because the helper correctly requires an occupied, attached, braked recovery group and the restored fixture had auto-resumed. The raw log preserves the rejection context. A follow-up paused fixture run is required before claiming E7 closure.

Raw server output: `server-latest.log` and `server-latest.log.gz`.
