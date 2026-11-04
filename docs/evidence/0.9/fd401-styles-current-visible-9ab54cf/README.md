# FD401 current-JAR stationary Styles rendering

On 2026-11-04, an isolated Styles-only packaged client reopened a copy of the
[earlier FANCY occupied-reload ride's stopped world](../fd401-fancy-styles-reload-ride/README.md)
with the committed `9ab54cf` source. Its built JAR SHA-256 is
`2d4ecc048875af48bb21a18e5ff1179d5a49f767ed45055a4477aa3b76260fb8`,
identical to the [archived current full-turn JAR](../fd401-styles-banked-fullturn-9ab54cf/README.md).
The command used `-PfdPackaged -PfdValidation -PfdRenderValidation
-PfdRenderValidationGraphicsMode=FANCY -PfdIntegrations=styles`, an isolated
output/game directory, and `-PfdWorld=FD401StopCycle`. The [raw log](client.log.gz),
[daylight side view](side-day.png), [closer curve view](curve-day.png),
[stopped world](world.tar.gz), and [hash inventory](SHA256SUMS) retain the run.
The client saved and quit normally; Gradle exited 0.

The saved circuit and double-spine appearance were selected by normal player
UI in the earlier run. This replay kept the same appearance; it changed only
time/weather and camera position to obtain clear screenshots. It did not
repeat selection, boarding, resource reload or the ride. The client logged
`graphicsMode=fancy`, expected `FANCY`, Flywheel `indirect` enabled, 12-chunk
render distance, eight mesh builds, and nonzero in-range decoration pieces
while the styled circuit was visible. No Styles draw-fallback marker occurred.

This is a current-byte visible Styles client checkpoint on a saved stationary
route. It is not a rendered view of the new banked full-turn fixture, a moving
inversion, a coupled train, a component-by-component ownership inspection, or
new evidence of normal operator actions on this JAR. The [class-parity review](../fd401-styles-renderer-parity-9ab54cf/README.md)
gives the narrower byte mapping to the earlier render-mode runs.
