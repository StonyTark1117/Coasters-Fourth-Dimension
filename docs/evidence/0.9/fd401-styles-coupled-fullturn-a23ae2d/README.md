# FD401 coupled Arrow cars through both styled full turns

On 2026-11-04, an isolated Styles-only packaged client ran only the named
`styledcoupledcarscompletebankedandflatturns` GameTest from source `a23ae2d`.
The [built JAR](coasters-fourth-dimension-0.9.0-dev.27.jar) has SHA-256
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`.
The run used `-PfdPackaged -PfdValidation -PfdIntegrations=styles`, an isolated
output/game directory, `JAVA_TOOL_OPTIONS` enabling the `fourthdimension`
GameTest namespace, and `-PfdWorld=FD401StylesController`. The [raw log](client.log.gz),
[pass screenshot](pass-chat.png), [stopped world](world.tar.gz), and
[hash inventory](SHA256SUMS) retain the final run. The client saved and quit
normally; Gradle exited 0.

The focused fixture built an eight-section native Arrow banked circuit away
from the older world contents, created a second physical Arrow car six blocks
from the first, linked their chassis with the native train constraint, and
mounted one pig in each. It assigned installed `arrow_4d` to the 30-degree
banked full-turn section and `arrow_4d_dbl_spine` to the flat full-turn section,
then broadcast the appearances. Both appearances supported the stored
360-degree profiles. The styles were assigned by the **server fixture**;
this run did not use the player editor UI or view the remote circuit during
motion.

Both cars retained their riders, closed restraints, valid matching support,
and native link. The first car's banked and flat peak carrier angles were
`6.2725479` and `6.2812724` radians; the second car's were `6.2729770` and
`6.2812843`. Each was within the test's `0.08`-radian tolerance of `2π`.
The powered station captured both cars; their riders unloaded, and the test
reported `captured=true unloaded=true linked=true`. It passed in 85.519
seconds with `All required tests passed :)`. The final version also removed
its rails, cars, sublevels and blocks after the assertions so the named test
can run again in the same world. One earlier development pass before that
teardown also passed, but its JAR and world are outside this final record.

The world copy contained older optional-profile objects, which caused
recoverable missing-registry warnings when opened with Styles only. This
result establishes native coupled full-turn behavior with both Styles
appearances. It does not prove client-visible coupled inversion, normal UI
selection, vanilla-player or cosmetic swept clearance, or component-by-component
render ownership.
