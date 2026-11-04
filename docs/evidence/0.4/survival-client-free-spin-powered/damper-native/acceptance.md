# FD403 native Free Spin damper regression on merged source

The new `MagneticGameTests.eightRidersPassiveMagnetCircuitAndCapture` assertion
ran on the merged 0.4 worktree and passed. This checks that actual Free Spin
damper impulses dissipate energy in the native powered circuit. The separate
[connected-client demo](../damper-demo/acceptance.md) observes the same
mechanic before capture from a player's seat.

## Exact run

- Source `b670499cf5e6aa6801a1d26910dd28a97d060541`, after merging the
  latest committed release branch. Base profile, Minecraft 1.21.1,
  NeoForge 21.1.248, Java 21. This is a development GameTest classpath run,
  not a packaged-client or optional-integration certification.
- Command: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx3G
  ./gradlew runGameTestServer --offline --no-daemon --console=plain`.
  The disposable GameTest directory was on `/dev/shm` to avoid disk-backed
  Sable save stalls; the prior worktree directory was preserved separately.
- The process returned exit code 0. The [complete log](gametest.log.zst)
  reports **all 50 required tests passed**, all dimensions saved, and
  `BUILD SUCCESSFUL in 4m 38s`. The Free Spin test printed
  `FD02_DAMPER impulses=483 energy=-8.571935280247345
  relativeAngularSpeedReduction=4.741178769782859`. The production audit
  increments only for accepted DAMP impulses; the test also asserts positive
  impulse count and negative energy change. Startup included optional-class
  warnings and an initial `server.properties` lookup before its file was
  created; the run continued and completed normally.
- The same source subsequently passed `./gradlew test --offline --no-daemon
  --console=plain`: 33 JUnit tests in ten suites, zero failures and zero errors.
  The [unit log](unit-tests.log.zst) and [XML results](unit-results.tar.zst) are
  preserved. Compilation reported one optional `Dist.CLIENT` class warning.
- [metadata.json](metadata.json) records the source, profile and observed exit.
  Its time window is bracketed by the console log's birth and final-modified
  timestamps. The post-exit [native export manifest](native-exports/manifest.json)
  confirms byte-verified capture of all five frame/trace outputs before another
  game run. [SHA256SUMS](SHA256SUMS) and [verify.py](verify.py) check the log,
  damper measurements, metadata and compressed exports.
- The [single-car](single-decoration-inputs.json) and
  [linked-train](train-decoration-inputs.json) input checks pass on the exact
  captured frame/trace pairs. They validate 5 routes/25 sections with 1,635
  spine and 430 tie samples, and 2 routes/10 sections with 814 spine and 212
  tie samples. Their source/input hashes and chassis pairing are recorded.
  These checks establish matching-route renderer inputs, not mesh clearance or
  proof of visible decoration in a client.

This closes execution of the native damper regression on this merged source.
It does not complete the player-built shaped Free Spin route or its family
teaching walkthrough, nor does the base profile certify Track Styles or
Engineered behavior. The captured recovery traces remain inputs for separate
FD402 clearance audits; the structural checks above do not turn them into a
clearance result.
