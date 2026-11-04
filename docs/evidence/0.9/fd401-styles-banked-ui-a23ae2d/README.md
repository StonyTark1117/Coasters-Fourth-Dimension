# FD401 normal UI style selection on a banked full-turn rail

On 2026-11-04, a Styles-only packaged FANCY client opened a copy of the
[visible banked ride's saved world](../fd401-styles-visible-bank-player-9ab54cf/README.md)
with the `a23ae2d` source JAR. Its SHA-256 is
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
identical to the [archived coupled-test JAR](../fd401-styles-coupled-fullturn-a23ae2d/coasters-fourth-dimension-0.9.0-dev.27.jar).
The run used `-PfdPackaged -PfdValidation -PfdRenderValidation
-PfdRenderValidationGraphicsMode=FANCY -PfdIntegrations=styles`, an isolated
output/game directory, and `-PfdWorld=FD401StopCycle`. The [raw log](client.log.gz),
[initial rail editor](full-turn-before.png), [initial appearance](appearance-before.png),
[new choice](double-spine-choice.png), [apply response](applied.png),
[reopened rail editor](full-turn-after.png), [saved choice readback](appearance-after.png),
[banked rail view](styled-bank.png), [saved world](world.tar.gz), and
[hash inventory](SHA256SUMS) retain the run. The client saved and quit normally;
Gradle exited 0.

The player used the rotation tool on the physical anchor at the beginning of
the 30-degree banked full-turn section of the saved Arrow circuit. The first
editor view showed a bound native rail at revision 1 and the 0→360→0 degree
profile. Its appearance screen showed `Track Styles: Arrow spine`, originally
assigned by a validation command in the earlier ride. The player clicked the
choice control to `Track Styles: Arrow double spine` and clicked Apply. The
client displayed `4D settings updated`. Reopening the same rail showed revision
2 with the same 0→360→0 profile, and reopening its appearance screen showed
`Track Styles: Arrow double spine`. The render diagnostic recorded FANCY and
Flywheel indirect, with nonzero decoration pieces near the styled route.

This establishes a normal client UI appearance edit and same-session readback
on a native banked full-turn section. It does not demonstrate a ride after
this particular edit, a cold reopen readback, a change partway within one
section, or visual ownership of every component during inversion. The saved
world also contains the earlier circuit and cars, so older optional-profile
registry warnings may appear in this Styles-only log.
