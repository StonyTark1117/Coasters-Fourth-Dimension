# FD403 packaged Legacy and Free Spin survival-client checkpoint

On 2026-11-02, a packaged client built from release commit `40bf4bc`
exercised Legacy ZacSpin and magnetic Free Spin in a clone of the prior
`FD403SurvivalClient` Arrow world. The packaged JAR SHA-256 was
`3cf34e8dc57b126925591c9e6240a5caa7db53ad2b386df74b6ec3a40d7e9f00`.
The saved `FD403FamilyClient` world reports game type 0 (survival) and commands
enabled. The clone retained the original Arrow section and car.

## Connected client exercise

Commands supplied only upstream ingredients and native Coasters anchors:
yellow or blue dye, Create red seats, iron bars, a Coaster Cart, and a crafting
table. No Fourth Dimension item was given. The connected player took the
crafting-table results for 16 Legacy ZacSpin Track and one Legacy ZacSpin Car,
then 16 Magnetic Free Spin Track and one Free Spin Car. The [Free Spin car
recipe](survival-client-free-spin-craft.png) shows the native cart and crafted
track in the grid.

For Legacy, the player placed two native anchors twelve blocks apart and used
the crafted track to make one matching section; the track stack fell from 15 to
12. Right-clicking the matching anchor with the crafted car consumed the car
and showed `Carrier capture engaged`. The [rendered Legacy car](survival-client-legacy-car.png)
and [car editor](survival-client-legacy-editor.png) came from this client. With
restraints open, the player boarded a native seat, closed restraints, dispatched,
captured, reopened restraints, and dismounted. The editor showed the expected
`Ready` and `Aligning / moving` states during that sequence.

For Free Spin, a ground-level placement did not accept the car, so the player
built two elevated anchor supports and a temporary boarding platform. The
crafted track connected the elevated anchors, and the crafted car was consumed
when used on that matching section. The [rendered Free Spin car](survival-client-free-spin-car.png)
shows its four carrier pairs. The player opened restraints and [boarded a real
seat](survival-client-free-spin-boarded.png); `/fd status` reported
[`occupants=1` on one of its four carriers](survival-client-free-spin-occupied.png).
After dismount, the player closed restraints, dispatched, saw
[`Aligning / moving`](survival-client-free-spin-dispatched.png), captured back to
`Ready`, and reopened restraints. The final status showed all four Free Spin
carriers captured and empty.

The integrated server saved all dimensions and sublevels, and the client exited
normally (`BUILD SUCCESSFUL in 7m 57s`). The saved schema-6
`fourthdimension_foundation.dat` has four rails (Arrow, Legacy, two Free Spin)
and three vehicles (Arrow, Legacy, Free Spin). Its Free Spin vehicle has four
captured carriers, open restraints and no fault; the Legacy vehicle has two
captured carriers, open restraints and an endpoint fault described below.
The saved data SHA-256 is
`78853c339fde1d76123a06a310ddba89814687d803eed3472088a42e76c760fd`.
[Selected runtime lines](survival-client-legacy-free-spin-events.txt) identify
the supplied ingredients, occupied Free Spin carrier, final empty carriers and
normal save. The full resumed client log SHA-256 is
`f90111a194e72fe016fd26b1aece804ef42d94ff9debda8f4232cffb1c32d088`.

## Limits

This is a connected survival client with command-supplied ingredients, supports
and position changes. It does not prove material gathering or an unassisted
build. The short sections had no propulsion, so dispatch proves the control
transition but not a powered ride. Legacy reached the end of its short section
and saved `End of compatible track; capture and inspect the route`; its endpoint
handling needs a longer-route exercise. The initial client session ended with a
native `libopenal.so` `SIGFPE` after its display session ended; the saved world
was resumed with `ALSOFT_DRIVERS=null`, completed the Free Spin exercise and
shut down normally. The initial log SHA-256 is
`d955a42aac83c0d2afe88c02dc08be9788ca747f94d8dc04205a11b8d3985b36`.
Revised, PAX, powered real-client rides and the complete five-family teaching
gate remain open.
