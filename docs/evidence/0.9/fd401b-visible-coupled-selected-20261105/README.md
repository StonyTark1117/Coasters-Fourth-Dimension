# FD401B selected-JAR visible coupled full turns

On 2026-11-05 the selected `ec2fcda` clean JAR (SHA-256
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`)
ran with Track Styles 1.2.0 in a Java 21 FANCY client on task-owned Xvfb
`:306`. The direct Java launch used `-Xmx1536M` inside an active
`fd401b-visual-client-306.scope` with `MemoryMax=6G`; Gradle prepared the
launch outside that scope. Its peak observed `MemoryCurrent` was about 3.55
GiB, and the host had about 17 GiB available during the run. The client used
an isolated game directory and `FD401VisualClean` save. Java and Gradle
reported JVM 21. The [classpath](clientRunClasspath.txt),
[VM arguments](clientRunVmArgs.txt), [program arguments](clientRunProgramArgs.txt),
[direct launch script](run-client-capped.sh), [options](options.txt), and
[dependency hashes](dependency-sha256.tsv) retain the run identity.

The clean save was copied from the stopped
[`fd401b-coupled-named-selected-20261105` world](../fd401b-coupled-named-selected-20261105/README.md).
Its foundation data had no rails or vehicles. In this scratch copy only,
`level.dat`'s `allowCommands` byte was changed from zero to one so the
packaged named GameTest could be invoked from the client. The initial
`FD401StopCycle` save was exited without running the test because it contained
an overlapping route. The spectator was placed at
`-504.5 216 -128.5`, yaw `180`, pitch `20`, facing the fixture. We expanded
the client window to 1280×720 before the second test invocation to resolve
the two cars clearly. No host desktop or another agent's display was used.

The [client log](client-console.log.gz) records two invocations of
`/test run styledcoupledcarscompletebankedandflatturns`. Both reported
`passed!` and `All required tests passed :)`. The fixture applied single
spine to the banked turn and double spine to the flat turn, coupled two native
Arrow cars, and mounted one pig per car. Its last marker on each invocation
reported `captured=true unloaded=true linked=true`; all four measured turn
peaks were within 0.08 radians of `2π`:

| Car | Banked single-spine | Flat double-spine |
| --- | ---: | ---: |
| Lead | 6.2716189894 | 6.2811128929 |
| Second | 6.2719421203 | 6.2811175345 |

The consecutive unedited client frames [05](frame-05.png),
[06](frame-06.png), [08](frame-08.png), [10](frame-10.png),
[12](frame-12.png), and [16](frame-16.png) show the coupled cars and riders
rotating through the visible styled full-turn section. [Frame 43](frame-43.png)
shows them later on the route. These frames close the previously unisolated
**visible moving inversion** on the selected JAR, paired with the exact-angle
and successful capture/unload assertions. The [world](world.tar.gz) was saved
with the normal Save and Quit control; the client exited normally at 08:36:33
and the task-owned Xvfb service was stopped after it exited.

This is a FANCY spectator view of a packaged GameTest with pig riders and
fixture-assigned styles. Separate selected-JAR records cover normal UI style
selection and normally boarded occupied client cycles. These screenshots do
not prove arbitrary rider cosmetics or every individual rendered pixel.
