# First integration slice — September 30, 2026

Development evidence, not a completed FD305 or FD901–FD904 certification.

## Versions and execution

Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Ponder
1.0.82+mc1.21.1, Sable 2.0.3, Coasters Simulated 0.1.5, Java 21.
Engineered profile adds only Engineered 1.1.1, CurseForge file 8863559.
The packaged visual artifact's SHA-256 is in `visual-artifact.sha256`.

The branch starts from `9e8ae6f975286fff2831981ce975e689ccdc7b41` in an isolated
worktree. The release chat's concurrent family lifecycle changes are not included.

## Automated results

- All 17 unit tests passed; see `unit-tests.txt`.
- Engineered profile: all 21 required native GameTests passed. The three occupied
  single-car circuits each traversed both dimensions of the loop, triggered their
  real observer, and returned aligned and captured without a fault. Early motor
  launch and station dispatch were rejected; carrier mass was included in motor
  accounting. See `engineered-native.txt`.
- Base profile: all 21 registered required tests passed, with the Engineered test
  explicitly skipped because its optional artifact was absent. The other 20 tests
  executed, including unavailable-provider persistence/migration, optional-class
  isolation, shared interlocks, distant bound-station occupancy, and an actual
  Create Display Link transfer of `1 / 4` occupancy to an oak sign. See
  `base-display-native.txt`.
- Builds passed after the final presentation changes. No external JAR is bundled.

The original Engineered experiment exposed missing independent-carrier mass in its
native rivet accounting. After correcting mass, the first trim setting still
stalled a returning free-spin car. The documented fixtures now use family-specific
launch/trim speeds. These are layout results, not universal braking guarantees.

## Packaged visual checks

A self-owned hidden Xvfb display and a disposable singleplayer save were used.
Five family scenes and the station scene were opened through `/ponder` in a
packaged client. Screenshots record model/layout and English-text checks, not every
animation frame. A missing language bundle and a bottom-clipped station overlay
were found and fixed. The final station screenshot demonstrates the corrected
placement at an 854 × 480 window size. Family screenshots precede the final shared
overlay placement/keyframe change but show unchanged models and language.

Goggles were inspected on a bound Engineered demonstration station. This is a local
client check; it does not establish two-client synchronization or occupied restart.
Client snapshots were immutable copies so concurrent builds could not replace a
loaded ZIP archive.

## Still open

FD305 reconciliation; rollback, linked-train and optional FE tests; PAX external
propulsion; every animation phase; two clients, late join, restart and chunk
interruption; real optional-mod removal/reinstallation; other individual addons
and the combined pack; measured rendering/performance. No full compatibility claim
is established by this evidence.
