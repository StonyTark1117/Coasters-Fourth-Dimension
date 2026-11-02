# Dependency and asset provenance

All addon Java, procedural track meshes, car/seat/restraint/station models, fixtures, and test logic in this repository were authored for this project. Original model definitions reference Minecraft textures; the texture files are not copied into this repository. Passenger entities and seat interaction support come from Create; original seat blocks/models are ours. Coasters running gear remains provided by the installed dependency. Track contact dimensions were measured from the pinned running-gear model and render transforms; no wheel or track asset files were copied.

The Gradle wrapper comes from [NeoForge's 1.21.1 ModDevGradle MDK](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle). Its scripts retain their Apache 2.0 notice and its JAR contains the upstream license. The shell script has a project-root/relative-JAR adjustment for checkout paths containing colons. The distribution SHA-256 is pinned.

Runtime dependencies are downloaded through their Maven/CurseMaven distributions, not copied into the addon JAR. See [dependency audit](research/dependency-audit.json) for inspected artifacts and terms. Coasters Simulated APIs and Sable 2.0.3 source/JAR signatures informed the integration; native physics, seat, camera, and render implementations remain upstream.

Optional third-party addon code/assets are not bundled into our release. Existing
Track Styles appearance and Engineered operating adapters are independently
authored and use separately installed addons. The [integration backlog](docs/INTEGRATION-BACKLOG.md)
also stages Additions, CTNB, Magnetized, Immersion, Coaster Skin, Waterparked and
the separate Create Coasters utility for deferred work. Its ignored local JAR
cache is not distributable mod content. The [artifact catalog](research/integration-catalog-2026-11-02.json)
records declared licenses and provenance. Track Styles' project/JAR license
mismatch remains unresolved; no staged artifact or proposed adapter authorizes
copying its code or assets.

The packaged-JAR test run uses an isolated Gradle source set, following [ModDevGradle run/source-set configuration](https://github.com/neoforged/ModDevGradle#isolated-source-sets). It exercises our binary JAR with the dependency binaries rather than loose addon classes.

The 0.2 blue track, four-pair car, centered axles, magnetic bars/brackets, recipes and passive coupling equations are independently authored. Manufacturer [S&S 4D Free Spin information](https://www.s-s.com/ride-pages/4d-free-spin-coaster) and its [product sheet](https://assets-global.website-files.com/60be9a2c8c73a085920bb514/636c251f0271e50223f23f92_4d%20Free%20Spin%20Coaster.pdf) inform the eight-seat/magnetic concept only. No manufacturer drawings, photographs, logos or model assets are distributed. Four independent two-seat hinges, dimensions and magnetic response are original game approximations; exact real-world coupling is unverified.
