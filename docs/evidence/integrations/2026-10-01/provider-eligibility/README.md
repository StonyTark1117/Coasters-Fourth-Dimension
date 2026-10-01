# Functional provider checks at vehicle placement

The vehicle factory now rejects unavailable or incompatible functional track
providers before allocating a chassis or carriers. The native snap path already
rejected those providers, but the factory previously returned a newly allocated
vehicle anyway. The item entry point uses this factory and now reports failure
without consuming the held item. Saved appearance IDs never grant functional access.

## Observed defect and correction

The negative control installs a saved rail record with a matching Arrow variant,
ordinary live native geometry, a missing functional provider and an Arrow appearance
selection. It calls the production factory before another vehicle occupies the
site, avoiding an unrelated axle-clearance rejection. The unmodified factory accepts
the request. The native suite records exactly one required failure:
`Factory accepted unavailable functional provider missing:functional for ARROW`.
The server exits 1. Its production factory class hash matches the previous packaged
passenger-loading artifact; source/class and artifact hashes are retained.

The correction calls the existing `TrackProviders.available` contract validation
after checking the rail's validity and variant, before geometry/clearance processing
or allocation. This covers provider absence and incompatible declared contracts.
The production change does not alter persistence, rendering, provider registration,
native engagement, optional versions or ride physics.

## Native regression scope

The existing five-family factory/snap test is extended; the suite still registers
33 tests. Each profile exercises four appearance selections: original, Track Styles
single spine, Track Styles double spine and a missing appearance provider. Only
Arrow can activate the two registered styles; other families fall back to original
appearance. Without Track Styles, both selections also fall back. Directly assigning
the saved selection isolates eligibility; the separate editor test continues to
exercise normal appearance edit requests and occupied-edit rejection.

For every profile the extended test checks:

- All 25 family pairs in each of four appearance states: 100 eligibility/native
  filter decisions, with 80 wrong-family factory rejections and 80 rejected locked
  initial snaps. Accepted/rejected binding hooks preserve the correct binding.
- Ten unavailable-provider factory and native-snap cases: each family with
  `missing:functional`, then with the cosmetic Track Styles provider ID incorrectly
  used as a functional provider. The second case also runs with Track Styles loaded.
- Five factory and native-snap rejections on ordinary geometry with no functional
  rail record. This is a native geometry test, not certification of every external
  ordinary track material or visual asset.
- The public item `useOn` entry point with a survival-mode FakePlayer: 95 rejected
  placements retain both items and allocate no vehicles/native bodies; five matching
  placements succeed and consume exactly one item each. All five resulting vehicles
  become boarding-ready and complete the locked native-snap matrix.
- Appearance save/load round trips and exact preservation of every other serialized
  rail field, including stable IDs, functional provider/family/variant, profiles,
  revision, geometry hash, validity and magnetic spans. Free Spin contains a nonempty
  damping span. The fixture uses the original placement profiles; nontrivial profile
  editing remains covered by the existing editor tests.

Native re-snapping and binding rejection remain valid when metadata is temporarily
replaced or removed and then restored. These cases emulate the eligibility state of
provider loss; actual packaged removal evidence remains in the separate
[removal matrix](../../2026-09-30/removal-matrix/README.md).

The five native family lanes are disjoint, so wrong-family snap pairs exercise family
and route-continuity restrictions together. Factory/item family rejections have no
adjacency restriction, and the unavailable-provider native cases use each car's own
matching lane. Isolating every native family restriction at a shared mixed-family
junction remains additional matrix work; this test is not sufficient by itself.
The later [connected-endpoint check](../connected-eligibility/README.md) covers those
stationary native family/filter/initial-snap/binding decisions at both ends, including
matching positive controls and a deliberate guard-mutation failure. Dynamic traversal
and linked consists remain outside that extension.

## Run status

All seven profiles exit 0 and pass the strict evidence audit:

| Profile | Registered tests | Explicit absent-addon skips |
| --- | ---: | ---: |
| Base | 33 | 6 |
| Engineered | 33 | 3 |
| Track Styles | 33 | 6 |
| Additions | 33 | 4 |
| Magnetized | 33 | 5 |
| Immersion | 33 | 6 |
| Combined | 33 | 0 |

Loaded versions are Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10,
Coasters Simulated 0.1.5 and Sable 2.0.3, with Engineered 1.1.1, Track Styles 1.2.0,
Additions 0.2.0, Magnetized 1.1.0 and Immersion 0.2.3 in their selected profiles.
Complete loaded-mod lists and build/class hashes accompany the results.

The build passes all 19 JUnit tests. Engineered uses `-PfdPaxEnergyReload`; the combined
run also enables it. The suite retains the earlier delayed-passenger-loading
regression and four occupied PAX interruption cases. Optional-mod skips are explicit
and are not runtime compatibility passes for an absent addon.

## Reproduction and retained boundaries

Use Java 21 and the pinned Gradle dependencies. Run the native servers sequentially,
since they use the same test world:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer
```

Repeat `runGameTestServer` for `-PfdIntegrations=styles`, `engineered`, `additions`,
`magnetized`, `immersion` and `engineered,styles,additions,magnetized,immersion`.
Add `-PfdPaxEnergyReload` to Engineered and combined. `matrix.py.txt` preserves the
local sequential driver. Its paths and predecessor PID/exit coordination make it an
investigation aid, not a portable one-command runner.

The negative-control test source is retained separately; run it against the archived
pre-fix factory source to reproduce the original failure. The expanded final test
adds survival item use and a nonempty magnetic span after that initial reproduction.
`runs.json` records full log paths/hashes and terminal exits; excerpts retain selected
checks, loaded versions and warnings. `classes.json` relates compiled classes to
the built JAR. These are native class-directory runs, not packaged client tests.

Audit the complete evidence with:

```sh
python3 scripts/verify-provider-eligibility.py docs/evidence/integrations/2026-10-01/provider-eligibility
```

This closes a demonstrated allocation/placement gap and extends FD901 evidence. It
does not complete the full entry-path/mixed-consist/boundary matrix, physical external
track conversions, client rendering, lifecycle, migration or performance gates.
Future functional Arrow 4D support remains separate under FD801.
