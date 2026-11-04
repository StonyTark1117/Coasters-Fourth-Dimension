# FD401 visible coupled Styles inversion

On 2026-11-04, a Styles-only packaged FANCY client used the frozen `2c5d9a7`
candidate JAR. Its SHA-256,
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
matches the [archived candidate](../fd401-final-candidate-2c5d9a7/coasters-fourth-dimension-0.9.0-dev.27.jar).
The command used `-PfdPackaged -PfdValidation -PfdRenderValidation
-PfdRenderValidationGraphicsMode=FANCY -PfdIntegrations=styles` with an isolated
game/output directory and `-PfdWorld=FD401StylesController`. The [raw log](client.log.gz),
[30-frame second lap](second-lap-frames.tar.gz), [banked approach](banked-approach.png),
[banked inversion](banked-inversion.png), [flat approach](flat-approach.png),
[flat inversion](flat-inversion.png), [captured UI](captured-ui.png),
[unloaded view](unloaded.png), [saved world](world.tar.gz), and
[hash inventory](SHA256SUMS) retain the run. The client saved and quit normally;
Gradle exited 0.

The world was copied from an exploratory run of the
[coupled named fixture](../fd401-styles-coupled-fullturn-a23ae2d/README.md)
before that fixture gained cleanup. It contains two native linked Arrow cars,
with the first Styles candidate on the 30-degree banked full-turn section and
the second on the flat full-turn section. The styles were assigned by that
**server fixture**, not through the player UI in this run. An older optional
profile object in the copied world produced recoverable missing-registry
warnings in the Styles-only classpath.

The player boarded a physical seat on carrier body
`6e4a0f71-44f1-4b95-b7c8-c4b9191f5963`. The client logged the player's
UUID in that carrier's occupants list with `riding=true`. Its vehicle UI
recognized `1/8` riders across `2 cars`; the operator closed restraints and
dispatched there. The saved station signal was first removed so the train
could leave, then restored during each lap. A motion query on the second lap
showed chassis `635516e7-4bb6-4ada-96f7-887374031663` at about 1.75 blocks/s
with `capture=false` near the styled turns. The second-lap frames, recorded
about every three seconds, visibly show both car assemblies and the occupied
near car rotating against the styled banked and flat rails. The five-second
client diagnostics sampled the occupied carrier at `6.1948` radians during
the banked turn and another carrier at `6.0076` radians during the flat turn;
the [named native test](../fd401-styles-coupled-fullturn-a23ae2d/README.md)
measured the exact full-turn peaks for both cars. The FANCY client recorded
Flywheel indirect rendering and nonzero Styles decoration pieces.

The first lap's third-person camera clipped into the rider's head, so its
frames are not retained as visual acceptance. The second lap used Create's
contraption camera from dispatch through both turns. After return, a motion
query reported `capture=true` with velocity below 0.001 blocks/s, the vehicle
UI showed Ready with restraints closed, and opening restraints let the player
dismount. The carrier then reported an empty occupants list and `riding=false`.

This closes a client-visible **coupled, occupied** full-turn checkpoint for the
default Styles models. It does not establish component-by-component render
ownership, equipped/cosmetic player clearance, continuous between-tick sweep,
or a ride after the separate banked UI appearance change.
