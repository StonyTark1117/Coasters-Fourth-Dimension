# FD401 Styles-only visible banked ride with a player

On 2026-11-04, a Styles-only packaged FANCY client used the `9ab54cf` source
JAR (`2d4ecc048875af48bb21a18e5ff1179d5a49f767ed45055a4477aa3b76260fb8`)
in an isolated output/game directory. It reopened a copy of the prior styled
world, then `/execute positioned -500 207 -212 run fd demo arrow_bank` created
a separate native Arrow banked circuit. Two `/fd validation style @s` commands
selected the first Styles Arrow candidate (`arrow_4d`) on the banked and flat
full-turn sections. These are validation fixture commands through the normal
appearance transaction, not proof of selecting those styles through the player
UI. The earlier saved circuit and its old car also remained in the world;
`/fd validation board @s` initially chose that old car, so the player
dismounted and boarded the new car by right-clicking its physical seat.

The [raw client log](client.log.gz), [20-frame ride series](ride-frames.tar.gz),
[banked rotation](banked-inversion.png), [flat rotation](flat-inversion.png),
[boarded vehicle UI](boarded-ui.png), [captured vehicle UI](captured-ui.png),
[return at the station](station-return.png), [unloaded view](unloaded.png),
[saved world](world.tar.gz), and [hash inventory](SHA256SUMS) retain the pass.
The run used `-PfdPackaged -PfdValidation -PfdRenderValidation
-PfdRenderValidationGraphicsMode=FANCY -PfdIntegrations=styles` and
`-PfdWorld=FD401StopCycle`. The client saved and quit normally; Gradle exited 0.

The physical seat mounted the new carrier body
`309f0496-73a7-4c80-87ae-3e1263cd46c4`; client diagnostics show the player
UUID in its occupants list and `riding=true`. The rotation tool opened this
car's vehicle UI. The operator closed restraints and dispatched there. The
same occupied carrier then reported a `6.2716039` radian banked-turn snapshot
angle at 09:11:23 and a `6.1597832` radian flat-turn angle at 09:11:43, with
changing native angle and camera quaternion. A motion query during the flat
turn showed the new chassis `4e207381-e5e7-4007-95ab-280620e85e7d`,
`capture=false`, and about 1.99 blocks/s. Third-person frames show the styled
route and occupied car during both rotations. The FANCY render diagnostic
reported Flywheel indirect enabled, positive mesh builds, and nonzero
decoration pieces; no Styles draw-fallback marker appeared.

A redstone block powered the new station during the ride. On return, a motion
query reported `capture=true` and velocity below 0.001 blocks/s. The vehicle
UI showed Ready with restraints closed. The operator opened restraints and
dismounted; the client then reported an empty carrier occupants list and
`riding=false`.

This is one visible single-car pass with a vanilla player and one Styles
candidate on each full-turn section. It does not establish coupled train
behavior, equipped/cosmetic player clearance, continuous between-tick sweep,
component-by-component render ownership, partway-within-one-section style
changes, or repeated reliability. The old car in the copied world was a setup
confound, but the body/chassis IDs and physical boarding identify the new car
for the measured ride.
