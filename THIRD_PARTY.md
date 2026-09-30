# Dependency and asset provenance

All addon Java, controller model definitions, fixtures, and test logic in this repository were authored for this project. Prototype models reference Minecraft textures; the texture files are not copied into this repository. Create seats and Coasters running gear remain objects provided by installed dependencies.

The Gradle wrapper comes from [NeoForge's 1.21.1 ModDevGradle MDK](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle). Its scripts retain their Apache 2.0 notice and its JAR contains the upstream license. The shell script has a project-root/relative-JAR adjustment for checkout paths containing colons. The distribution SHA-256 is pinned.

Runtime dependencies are downloaded through their Maven/CurseMaven distributions, not copied into the addon JAR. See [dependency audit](research/dependency-audit.json) for inspected artifacts and terms. Coasters Simulated APIs and Sable 2.0.3 source/JAR signatures informed the integration; native physics, seat, camera, and render implementations remain upstream.

No Track Styles, Waterparked, Immersion, Engineered, or Magnetized code/assets are included. Track Styles integration remains optional future work, with independently authored adapters and no redistribution of its restricted assets. The source/runtime license discrepancy documented in PLAN.md remains unresolved and is not used as permission to copy anything.
