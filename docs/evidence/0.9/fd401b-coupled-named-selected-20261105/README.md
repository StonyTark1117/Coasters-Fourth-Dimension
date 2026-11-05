# FD401B selected-JAR coupled full-turn named test

On 2026-11-05, a Java 21 dedicated server loaded the selected clean
`ec2fcda` JAR, SHA-256
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`,
with Track Styles 1.2.0. The [server log](server.log) identifies the mod's
packaged JAR path, the enabled `fourthdimension` GameTest namespace, and one
focused RCON command: `test run styledcoupledcarscompletebankedandflatturns`.
It reported `Running 1 tests...`. The server used isolated ports 25681/25682,
an isolated game directory, and no graphical client. The copied JAR hash was
checked before and after the run. Java and Gradle both reported 21; Gradle
returned `BUILD SUCCESSFUL` after a normal RCON `stop`.

The command was:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk PATH=/usr/lib/jvm/java-21-openjdk/bin:$PATH \
  ./gradlew runServer \
  -PfdBuildDirectory=/tmp/fd401b-coupled-selected-20261105/build \
  -PfdPackaged -PfdIntegrations=styles -PfdValidation \
  -PfdNamedGameTestServer \
  -PfdServerGameDirectory=/tmp/fd401b-coupled-selected-20261105/game \
  -PfdServerMaxHeap=1536M -x jar -x compileJava \
  --offline --no-daemon --console=plain -Dorg.gradle.jvmargs=-Xmx2G
```

The selected JAR's `BankedArrowGameTests.class` is byte identical to the
[earlier tested candidate](../fd401-styles-coupled-fullturn-a23ae2d/README.md)
(`2291523a3370fc9f33956a544de8c4f33e82555ed4775c79ffd39cb1f81251ad`).
The fixture created eight native sections and two cars, linked the native
chassis at six-block spacing, assigned single spine to the banked full turn and
double spine to the flat full turn, mounted one pig per car, closed restraints,
and dispatched. It checked every tick that both riders, native link, restraints,
and original support remained valid. The logged peak carrier angles were:

| Car | Banked turn | Flat turn |
| --- | ---: | ---: |
| Lead | 6.2716189894 | 6.2811128929 |
| Second | 6.2719421203 | 6.2811175345 |

All four peaks fall within the fixture's 0.08-radian tolerance of `2π`.
The final marker reports `captured=true unloaded=true linked=true`; in the
packaged method, that marker follows the peak, station-fault, and rider-unload
assertions and precedes teardown and `helper.succeed()`. There is no separate
post-test pass summary in this dedicated-server log, so the retained evidence
is the reached final marker, absence of an error before shutdown, and the
method's inspected success path. The stopped [world](world.tar.gz), generated
[VM arguments](serverRunVmArgs.txt), [program arguments](serverRunProgramArgs.txt),
[legacy classpath](serverLegacyClasspath.txt), [dependency hashes](dependency-sha256.tsv),
and [file hashes](SHA256SUMS) preserve the bounded run.

This closes the exact selected-JAR **native coupled full-turn peak** gap for
both Arrow styles and both cars. It is a server fixture with pig riders and
fixture-assigned appearances. It does not prove normal UI selection, visible
client inversion, player or cosmetic swept clearance, or per-component pixels.
