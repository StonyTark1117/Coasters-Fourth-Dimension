# Coasters: Fourth Dimension

A foundation prototype for a Create: Coasters Simulated addon. The design covers Arrow/S&S controlled 4D, legacy ZacSpin, S&S Free Spin, a revised ZacSpin-inspired variant, and PAX-style free-spin shuttles.

The current build contains development fixtures and mechanical contracts. Finished original tracks, vehicle art, survival acquisition, magnetic equipment, and shuttle operation follow the [roadmap](ROADMAP.md); this is not the 1.0 content release.

## Development

Use Java 21. Exact versions are in [build.gradle](build.gradle) and [foundation notes](docs/FOUNDATION.md).

```sh
./gradlew clean build
./gradlew runGameTestServer
./gradlew runClient
./gradlew runServer
```

On this Linux workstation, select the installed JDK with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`. The server requires Minecraft EULA acceptance in its generated run directory. Use offline mode only for a loopback development server with generated development identities.

The normal output is `build/libs/coasters-fourth-dimension-0.0.1-dev.jar`; run directories are under `build/runs`. For Unix checkout paths containing `:`, the build automatically uses `/tmp/fourthdimension-<path-hash>/build` to avoid Java classpath parsing errors. The wrapper launches from the project root for the same reason. Build logs show the resulting path. Do not store valuable worlds in build directories; `clean` removes them. GameTest runs always recreate their own test world.

A second development client is available through `./gradlew runObserver`. Optional `-PfdConnect=127.0.0.1:25578` joins a local test server; `-PfdValidation` enables client snapshot/orientation evidence logs.

## Prototype commands

Operator level 2 is required. Run these in an empty test world, near spawn:

- `/fd arrow_controlled` or `/fd zacspin_legacy`: creates a straight fixture five blocks above and eight blocks along positive world Z from the command position. Fixtures are limited to ±1024 horizontal coordinates while the documented native precision issue is investigated. The entire swept corridor must be empty.
- Other family IDs are `sns_free_spin`, `zacspin_revised`, and `pax_shuttle`. These currently select contracts and free carriers, not their later family-specific equipment.
- `/fd capture`, `/fd release`: engage or release prototype carrier motors.
- `/fd ride`: board the first available prototype seat. Capture and stop the fixture first; the production boarding interlocks are FD106.
- `/fd travel <speed>`: give the prototype chassis and carriers a common initial world-Z velocity, limited to ±5 blocks/s. This is a test impulse, not production propulsion.
- `/fd kick`: give free carrier groups opposite angular impulses for testing.
- `/fd status`: report authoritative angle, velocity, occupancy, capture, and update timing.
- `/fd clear`: remove this dimension's addon fixtures and their own rail connections. Use only in the test world.

Family identity is mechanical. Locking a carrier does not permit a different track family. An unbound family controller is rejected rather than treated as an ordinary bearing. Ordinary base bearings can use the unchanged prototype main rails; this is rail access, not a towing certification. Track Styles is neither required nor bundled.

See [PLAN.md](PLAN.md), [ROADMAP.md](ROADMAP.md), and [validation evidence](docs/FOUNDATION.md).
