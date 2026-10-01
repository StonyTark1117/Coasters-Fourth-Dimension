# Engineered FE launch, starvation and regenerative braking

Engineered 1.1.1 passed a native Forge Energy fixture on four separate 60-block
straight tracks: a linked Arrow pair, one legacy ZacSpin, one Free Spin and one
revised ZacSpin. Five native pig passengers remain mounted through forward launch,
braking, reverse launch and recapture. This adds acceptance coverage; production
adapter behavior is unchanged.

The fixture enables `requireEnergy` in its own GameTest batch and restores the
previous value afterward, including both failed setup attempts. The existing PAX
ownership test subsequently asserts `energyRequired=false`. The captured on-disk
configuration remains unchanged: 100,000 FE per anchor, 10 FE per joule and 0.3
regeneration efficiency. Supply and drain operations use the real anchor's
`Capabilities.EnergyStorage.BLOCK` API on its lower face. A simulated fill must
leave the buffer empty before actual filling. No chassis velocity or motor force
is injected.

The assertions cover:

- Full buffers cannot pull captured cars with open restraints; held cars consume
  no FE and move less than 0.15 blocks during the observed hold.
- After validated restraint closure and dispatch, empty buffers prevent launch.
  Native controller presence and its starvation flag remain active.
- Refilling permits forward launch, consuming FE and moving every car more than
  two blocks at over one block/s.
- Draining again does not prevent TRIM braking. All cars slow below 1.3 blocks/s
  and their anchor buffers gain regenerated FE.
- STOP settles the cars below 0.1 blocks/s before backward launch. Reverse launch
  consumes FE; a second empty-buffer trim brakes and regenerates in reverse.
- Validated capture returns all five cars to boarding readiness with closed
  restraints, no fault, intact Arrow coupling and all five passengers still seated.
  Restraints then open and the passengers unload normally.

Both optional profiles produced the same sampled values in this fixture. Forward
speeds were 6.80–7.51 blocks/s; reverse speeds were −2.42 to −2.08 blocks/s. Forward
braking returned 1,430–3,181 FE per anchor and reverse braking returned 188–239 FE.
These are samples from this layout, not promised speeds or energy budgets.

Two rejected setups are retained. Attempt 1 placed rails beyond the native
1,024-block boundary. Attempt 2 used a 100-block section and failed the placement
reach check. The final fixture stays inside the boundary and uses 60-block sections.
Neither rejected setup reached the operating assertions.

| Profile | Registered result | Explicit optional skips |
| --- | --- | --- |
| Engineered only | All 26 passed | 3 |
| All five addons | All 26 passed | 0 |
| Base | All 26 passed | 5 |

The build also passed 19 unit tests. Base execution verifies optional-class
isolation; its skipped energy test is not compatibility evidence. Native Gradle
runs execute compiled classes. `artifacts.json` identifies the accompanying built
JAR, fixture sources and pinned Engineered artifact. Per-profile mod lists, result
excerpts, warnings and full-log hashes are retained with the JUnit XML reports.
Dependency warnings remain present; startup and rendering performance are unmeasured.

Reproduce with Java 21, running native profiles sequentially:

```sh
./gradlew build runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
./gradlew runGameTestServer
python3 scripts/verify-engineered-energy.py docs/evidence/integrations/2026-09-30/engineered-energy
```

This covers direct FE capability supply at default energy tuning with the energy
requirement enabled. External generators/cables, partial-power limits, capacity and
regeneration extremes, FE persistence across restart/chunk interruption, linked
curves/slopes and gravity rollback remain unverified. No player clients or camera
behavior are exercised here. PAX remains winch-owned and is covered separately by
the [energy-free opposing-equipment fixture](../engineered-pax/README.md).
