# FD401C selected-JAR combined-profile Arrow operator pass

On 2026-11-04, a Java 21 packaged client loaded the clean committed-source
`8f370d0` qualification JAR (`cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`)
from an isolated source export and build directory. The launch used
`-PfdPackaged -PfdValidation -PfdIntegrations=styles,engineered -PfdFeNetwork`
and `-PfdWorld=FD401Stop12`; `-x jar -x compileJava` prevented the launcher
from replacing the selected JAR. Track Styles 1.2.0 and Engineered 1.1.1 both
appear in the client log. Crafts & Additions supplied only the disclosed FE
fixture. [dependencies.sha256](dependencies.sha256) records their actual JAR
hashes. The world copy came from the stopped wired Arrow checkpoint in
[`fd401-engineered-exact-fe-2c5d9a7`](../fd401-engineered-exact-fe-2c5d9a7/README.md).

The private task-owned Xvfb was `:217`. The first `runClient` launch used a
1.5 GiB game heap and a verified `MemoryMax=3G` systemd scope, but was OOM-killed
at world load with default video settings. Its [log](oom-default-video.log.gz)
is retained; it provides no operator acceptance. The clean world copy was
restored, graphics set to FAST and render distance to 4, and the same selected
JAR launched again under the same caps. The second scope remained active through
the run, reached its 3 GiB cap without an OOM kill, and exited `Result=success`.
Gradle reported `BUILD SUCCESSFUL`; the client saved, returned to title, and
stopped normally. The owned Xvfb was then stopped.

With `requireEnergy=true`, the saved linked controller at `(-506,142,-629)`
and anchor at `(-500,142,-630)` began at 91,309 FE. Validation availability
reported one loaded chassis, two carriers and zero assignments. The connected
player Dev boarded and closed restraints. Disclosed `/setblock` commands cleared
the rear station signal, pulsed the controller, and later restored the station
signal. During the pass, anchor FE fell to 66,059 and
`CoastersEngineeredPassStarved` remained `0b`. The controller's later snapshot
reported `CoastersEngineeredDriving=1b`, `CoastersEngineeredStep=1`, and
`CoastersEngineeredPassReached=19.399202/20`; literal 20.0 speed was not
observed. Before station capture, rider motion was about 0.01463 blocks/s;
after restoring the rear signal, `capture=true` and speed was below 0.001
blocks/s. The UI showed `Ready | Restraints closed`. Dev clicked **Open
restraints**, held Shift to dismount, and availability returned zero assignments
with no `RootVehicle`. The [client log](client.log.gz), [console log](console.log.gz),
screenshots and [final save](final-world.tar.gz) retain these observations.

This closes one bounded combined-profile Arrow operator cycle on the selected
JAR, including physical FE draw, programmed controller progression, station
capture, UI release, unload, save and clean exit. The saved route and wire were
not newly constructed. No moving-phase screenshot or all-family combined-client
matrix was captured; those claims remain open. The first OOM attempt does not
count as a ride result.
