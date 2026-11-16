# Dependency and asset provenance

All addon Java, procedural track meshes, car/seat/restraint/station models, fixtures, and test logic in this repository were authored for this project. Legacy model definitions reference installed Minecraft textures without copying those files. FD917 adds 41 independently authored pixel materials and deterministic runtime models; their editable sources and exporters are in [assets-source/fd917](assets-source/fd917/README.md). Passenger entities and seat interaction support come from Create; original seat blocks/models are ours. Coasters running gear remains provided by the installed dependency. Track contact dimensions were measured from the pinned running-gear model and render transforms; no wheel or track asset files were copied.

The Gradle wrapper comes from [NeoForge's 1.21.1 ModDevGradle MDK](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle). Its scripts retain their Apache 2.0 notice and its JAR contains the upstream license. The shell script has a project-root/relative-JAR adjustment for checkout paths containing colons. The distribution SHA-256 is pinned.

Runtime dependencies are downloaded through their Maven/CurseMaven distributions, not copied into the addon JAR. See [dependency audit](research/dependency-audit.json) for inspected artifacts and terms. Coasters Simulated APIs and the recorded Sable source/JAR signatures informed the integration; native physics, seat, camera and render implementations remain upstream. The frozen current stack uses Sable2.0.6; [the dependency manifest](docs/evidence/1.0/lead/freeze.json) preserves exact current artifacts while earlier audits retain their actual versions.

Optional third-party addon code/assets are not bundled into this release.
Independently authored adapters now cover the accepted Track Styles appearance,
Engineered operation, Additions station/selected-seat, measured CTNB track,
Magnetized drive, Skin appearance, Immersion client effects, Waterparked coexistence
and Create Coasters native-utility boundaries. They use separately installed
upstream addons. [Frozen capabilities and dependencies](docs/evidence/1.0/lead/freeze.md)
and [0.9 acceptance](docs/evidence/0.9/acceptance.md) define their actual scope;
an installed addon does not authorize arbitrary functional conversion or asset reuse.

The [historical artifact catalog](research/integration-catalog-2026-11-02.json)
and [selected dependency decisions](docs/evidence/0.5/dependencies-tooling/dependency-decisions.md)
record inspected metadata, declared licenses and provenance. Ignored research JAR
caches are not distributable project content. Track Styles' recorded project/JAR
license mismatch remains unresolved; no adapter or qualification result grants
permission to copy its code or assets. Optional-provider artwork remains external
and the addon includes its own standalone fallback content.

The packaged-JAR test run uses an isolated Gradle source set, following [ModDevGradle run/source-set configuration](https://github.com/neoforged/ModDevGradle#isolated-source-sets). It exercises our binary JAR with the dependency binaries rather than loose addon classes.

The 0.2 blue track, four-pair car, centered axles, magnetic bars/brackets, recipes and passive coupling equations are independently authored. Manufacturer [S&S 4D Free Spin information](https://www.s-s.com/ride-pages/4d-free-spin-coaster) and its [product sheet](https://assets-global.website-files.com/60be9a2c8c73a085920bb514/636c251f0271e50223f23f92_4d%20Free%20Spin%20Coaster.pdf) inform the eight-seat/magnetic concept only. No manufacturer drawings, photographs, logos or model assets are distributed. Four independent two-seat hinges, dimensions and magnetic response are original game approximations; exact real-world coupling is unverified.
