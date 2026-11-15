# 0.8 dependency baseline dispositions

2026-11-15. Reuses the approved [dated FD501 decisions](../../0.5/dependencies-tooling/dependency-decisions.md) and [catalog](../../0.5/dependencies-tooling/catalog-20261114.json). All five selected artifacts were rehashed against that catalog; see [verified files](verified-artifacts.json). No refresh or unrelated core upgrade is selected.

## FD501C — complete, no core change

CTNB file 8718286, actual `coasters_extras` 1.2, consumes the existing Create6.0.10/Coasters0.1.5 stack. Keep NeoForge21.1.248/Sable2.0.3/Veil4.1.4. Development profile `ctnb` names that exact artifact; the runtime metadata guard accepts only1.2. No dependency or saved-data migration is required by this baseline. Functional eligibility, control interception and the first supported gameplay profile belong to FD811, which remains open.

## FD501D — complete, no core change

Magnetized file8668703, `coastersmagnetized`1.1.0, has its required loader/Create/Coasters/Sable/Flywheel dependencies on the current core. Development profile `magnetized` names that exact artifact; runtime metadata guard accepts only1.1.0. No upgrade/migration is selected. Longitudinal drive, rollback, power and authority qualification remain FD812 work. Its upstream launch code requires existing motion; the consumer must implement an explicit validated start direction rather than treating boost as a stationary launch.

## FD501B — complete, no core change

Selected Additions file9243346 is published0.2.5beta and embeds `coasterfins`0.2.2. Development profile `additions` adds Aeronautics bundle1.3.2 file8763471 (including `simulated`) and Interiors0.6.1v3 file8974633. No required core upgrade. Runtime inspection now compares the four approved class fingerprints from the loaded mod container; a matching version label with different bytes is unsupported. Actual Java21 dedicated profile loaded all selected identities and the runtime fingerprint guard passed in the FD810 native case (`additions-native04.log` under the lead persistent run). No migration is required. Seat-only behavior remains FD911, outside0.8.

These baseline dispositions do not claim adapter gameplay certification. No unchanged family acceptance is reopened. Authority schema8 belongs to FD1701A, independent of optional dependency metadata.
