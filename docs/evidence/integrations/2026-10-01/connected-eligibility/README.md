# Native family eligibility at connected endpoints

This extends the [provider/appearance matrix](../provider-eligibility/README.md)
with real native track neighbors sharing both endpoints of each family's original
lane. It isolates family rejection from the route-continuity restriction that also
rejects the earlier disjoint-lane probes. Production classes and resources are unchanged;
the change adds fixtures, assertions, a deliberate mutation control and evidence.

## What the fixture exercises

Each of the five original family lanes now has a lower and upper connected segment.
The same five cars are placed through the survival item entry point, captured and
restrained. The test temporarily replaces one neighbor's saved rail record with each
family/variant contract while keeping its actual native curve, endpoints and an
available original provider. Normal construction rejects mixed-family junctions;
these replacements model a pre-existing mixed graph that must also be rejected by
native re-engagement. They do not enable mixed-family track construction.

Every car/track pair runs at both endpoints for original appearance, Track Styles
single spine, Track Styles double spine and a missing appearance selection. Before
each probe, assertions require a shared endpoint, resolvable native geometry and an
available functional provider. This produces 200 distinct cases per profile:

- Forty matching cases pass `EligibilityHooks.allows`, the public native snap
  filter and the initial-snap API. Their saved binding advances onto the neighbor
  and the native bearing becomes engaged.
- The 160 wrong-family cases fail eligibility and the native filter. Calling the
  binding hook and initial-snap API preserves the original binding and chassis
  position and leaves the native bearing disengaged.
- Every case restores the original neighbor metadata and the car's original
  binding/native guide before the next probe. Full appearance save/load and mechanical
  preservation checks include all fifteen rail records.

The per-case `FD_CONNECTED_FAMILY` records identify car family, track family,
endpoint, appearance and outcome. The verifier checks the complete Cartesian
product, rejects duplicate/missing cases and checks actual matching/rejection counts.
The existing 100 disjoint decisions, provider-loss checks and 95 rejected/five
accepted survival item placements remain in the same registered GameTest.

These are **stationary native API checks**, not moving boundary traversals or shuttle
dispatches. In particular, PAX is never dispatched outside its winch state machine.
The separate moving Arrow boundary check remains in the suite; full dynamic and
linked-train coverage for every family is still required.

## Deliberate mutation control

After the ordinary base run, the local driver temporarily changes only the family/
variant rejection condition in `EligibilityHooks`: non-Arrow cars bypass it, while
Arrow retains it. Provider availability, consist eligibility and route continuity
remain active. This is a test mutation, not a discovered production defect or a
release candidate change.

All ten original-appearance Arrow endpoint probes pass. The next probe fails with
`Connected family eligibility mismatch ZACSPIN/ARROW`; the run reports exactly one
required failure and exits 1. That reaches a connected wrong-family case after the
old disjoint matrix and demonstrates that the new test detects the missing guard.

The driver restores the production source in `finally`, byte-for-byte, before the
optional profiles. Original/mutated source, source hashes, mutated class hash and
restoration hashes are retained. Final class/JAR checks must match the pre-existing
production `EligibilityHooks` and `VehicleFactory` classes, so the mutation cannot
be mistaken for the shipped behavior.
An entry-by-entry comparison with the prior provider-eligibility JAR also confirms
that only `FamilyEligibilityGameTests.class` and its generated helper changed: every
production class and resource is byte-identical, with no added or removed entries.

## Run status

The base and all six optional profiles exit 0, with 33 registered tests per profile
and 19 passing JUnit tests. The strict audit passes the complete 200-case Cartesian
product in every profile. The deliberate mutation exits 1 with exactly its expected
failure; its source is restored before the optional runs.

| Profile | Connected matching accepts | Connected wrong-family rejections | Absent-addon skips in suite |
| --- | ---: | ---: | ---: |
| Base | 40 | 160 | 6 |
| Engineered | 40 | 160 | 3 |
| Track Styles | 40 | 160 | 6 |
| Additions | 40 | 160 | 4 |
| Magnetized | 40 | 160 | 5 |
| Immersion | 40 | 160 | 6 |
| Combined | 40 | 160 | 0 |

Loaded versions are Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10,
Coasters Simulated 0.1.5 and Sable 2.0.3. Optional profiles select Engineered 1.1.1,
Track Styles 1.2.0, Additions 0.2.0, Magnetized 1.1.0 and Immersion 0.2.3. The complete
loaded lists are retained. Optional skips are explicit and do not certify behavior
of an absent addon. The delayed-entity passenger regression and all four occupied
PAX interruption checks remain green, with FE required in Engineered and combined.

## Reproduction and boundaries

With Java 21 and the pinned Gradle dependencies, run the native profiles sequentially:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer
```

Repeat `runGameTestServer` with `-PfdIntegrations=styles`, `engineered`, `additions`,
`magnetized`, `immersion` and `engineered,styles,additions,magnetized,immersion`.
Engineered and combined use `-PfdPaxEnergyReload` to preserve the FE-enabled lifecycle
regressions. To reproduce the negative control, use `mutation.java.txt` only in an
isolated test checkout, restore `production-original.java.txt` immediately afterward,
and rebuild before further testing. `matrix.py.txt` records that sequence with local
paths and predecessor process coordination; it is not a portable unattended runner.

Loaded versions, terminal exits, full-log hashes, selected checks/warnings, unit XML,
source/class/JAR hashes and the mutation control accompany the final results. The
native servers run from compiled classes; no new packaged-client run is claimed.

```sh
python3 scripts/verify-connected-eligibility.py docs/evidence/integrations/2026-10-01/connected-eligibility
```

This extends FD901 for fixed native straight segments and the five current family
variants. Arbitrary external materials/providers, moving crossings and boundaries,
linked/mixed consists, full multiplayer/lifecycle, rendering and performance gates
remain separate. Functional Track Styles Arrow 4D support remains FD801.
