# FD901 native optional-profile matrix — exact source abc4a9a

Recorded November 2, 2026 (America/Phoenix). All four profiles use source
`abc4a9a9dd2a1df4957ef5714a58eae6e150895b` and built JAR SHA-256
`74ae47b70f2e95bcd8152552f255cabba6b55ed942a1f9856fa41fef39ca2467`. Source files, compiled outputs and the built JAR hash are identical
across all runs; all 216 compiled runtime classes match the JAR byte-for-byte.
These are Gradle development GameTest runs, not runs of the packaged source set.
The JAR hash identifies the corresponding build; no packaged-client claim follows.

## Results

All runs enable `-PfdPerformanceTargets`; each JVM confirms the property is true.
Each reports all 49 required tests passed, exits 0 and saves all dimensions before
normal shutdown. Timing columns are controlled / PAX / Free Spin scene averages,
in ns. The fixed limits remain 120,000 ns per runtime invocation and 6,000 ns per
live carrier update. Host CPU, platform and Java version are retained per profile.

| Profile | Required tests | Native minutes | ns/invocation | ns/carrier update |
| --- | ---: | ---: | --- | --- |
| base | 49 | 5.339 | 57141 / 107085 / 109581 | 3206 / 2921 / 2849 |
| styles | 49 | 5.25 | 51366 / 88792 / 92425 | 3324 / 2390 / 2218 |
| engineered | 49 | 5.504 | 56725 / 98290 / 105391 | 3390 / 2576 / 2755 |
| combined | 49 | 5.585 | 58798 / 103146 / 105211 | 4208 / 2942 / 2879 |

Base and Styles each explicitly skip five Engineered-specific scenarios by
returning success when that addon is absent. Their 49-test count is not Engineered
operation coverage. Engineered and combined require actual completion markers
for four-family motors/PAX hold, ordinary-cart STOP, powered controlled train,
18-negative/4-positive interlock calls, and FE capability/starvation/supply/retention.
Styles and combined report `strokeCases=2`; base and Engineered report zero.
Every profile reports 25 family eligibility decisions and all 20 directed
wrong-family coupling rejections, preserving both vehicles' original endpoint
bindings. Appearance/mechanical separation and exact four-car PAX curve restoration
retain their bounded native assertions.

The inventory also includes valid geometry-matching foreign-material provenance
rejection and separate changed valid-Coasters and missing-curve cases. These are
synthetic historical-state tests; actual legacy-world coverage remains in its
separate artifact-specific records.

## Previous failure and the correction

The earlier [11e1a93 matrix](../profile-matrix-11e1a93/README.md) remains accurate:
base/Styles passed, Engineered hit `Vehicle capacity reached`, and combined was
not run. That Engineered run was explicitly terminated and has no normal final-save
claim. The new source moves only the family-eligibility fixture into a dedicated
batch, so it runs apart from overlapping Engineered/default fixtures. Assertions,
production mechanics and the 64-vehicle limit are unchanged. The current four-run
matrix exercises the corrected scheduling without suppressing a test or raising
production capacity. This record supersedes the earlier blocker for this native
suite, not its historical outcome.

## Evidence and reproduction

Use Java 21 with the pinned Minecraft 1.21.1 / NeoForge 21.1.248 core stack, with Create 6.0.10, Coasters Simulated 0.1.5 and Sable 2.0.3. Optional
artifacts remain Track Styles 1.2.0 and Engineered 1.1.1; no new ecosystem addon is
activated. Full compressed console/latest/debug logs, commands, launch arguments,
source/compiled-file hashes and dependency hashes are retained per profile.

Core test command: `./gradlew build runGameTestServer --no-daemon --console=plain
-PfdPerformanceTargets`, with no integration option for base,
`-PfdIntegrations=styles`, `-PfdIntegrations=engineered`, or
`-PfdIntegrations=styles,engineered`. Actual commands add the archived initialization
script and `fdRecordRuntimeClasspath` task to record dependency resolution in the
same Gradle invocation before GameTest startup. This records the configured main
runtime files; it is not live JVM process introspection. The generated legacy
classpath separately lists 67 library files and omits mod artifacts. Runtime mod
lists and profile-specific completion markers corroborate loaded addon versions.

`run-profile.py` requires an explicit source and clean checkout and refuses to
overwrite prior evidence. For reproduction, copy it and `runtime-classpath.init.gradle`
into the ignored `build/profile-matrix` directory of a checkout at the tested
source. Run profiles serially with exclusive runtime ownership; Gradle recreates
its generated GameTest world. Stop the matrix on a failed profile.

Complete stopped worlds are retained locally under
`build/profile-matrix/abc4a9a/<profile>/stopped-world` in the evidence worktree.
Each local world has an archived file-hash manifest. Worlds and third-party
binaries are not distributed in this bundle. Run
`python3 verify.py` in this directory to check payload hashes, all four profile
assertions, optional artifact identities and identical source/classes/JAR records.
Add `--world-root /path/to/build/profile-matrix/abc4a9a` to additionally rehash every
retained world file against its archived manifest. The corresponding built JAR is
retained locally under `build/profile-matrix/abc4a9a/artifact/`; add `--jar` with
its path to rehash the binary and repeat the 216-class byte comparison.

## Limits

This completes the bounded native four-profile refresh, replacing the old 41-test
optional-profile evidence for this source. It does not close all FD901 or 0.9
requirements. Dedicated production startup, real-player operator workflows,
multiplayer/lifecycle races, historical rider restoration, rendered rider/linkage
clearance, graphics/shaders and target FPS retain separate acceptance gates.
Timings are host-specific addon carrier-runtime costs, not whole-server or frame
cost. No future addon version or external functional track is certified.
