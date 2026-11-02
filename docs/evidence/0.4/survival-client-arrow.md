# FD403 packaged Arrow survival-client checkpoint

On 2026-11-02 a packaged `0.9.0-dev.27` client built from release commit
`72854b2` completed a bounded Arrow construction and control sequence in a
fresh superflat single-player world.
The packaged JAR SHA-256 was
`93a24c6804629b90896a8225d1a93994137865a82b13ad9e02d48bc8b283ff67`.
The saved `FD403SurvivalClient` world reports game type 0 (survival), commands
enabled, and peaceful difficulty. The client ran in an isolated Xvfb display,
then saved and shut down normally (`BUILD SUCCESSFUL in 16m 38s`).

## Client exercise

Commands supplied upstream ingredients, a crafting table, and two native
Coasters anchors; they supplied no Fourth Dimension items. The connected player
used the crafting-table GUI result slot to make 16 Controlled 4D Track items
from six iron ingots, two copper ingots, and red dye. The player then used that
table to craft a Controlled 4D Car from Create red seats, iron bars, iron,
one crafted track item, and a native Coasters cart. The player used the 2×2
inventory crafting grid to make a Rotation Tool from a compass, Create brass
ingot, and stick.

The player placed both anchors by right click, twelve blocks apart, selected
them with the crafted track item, and created one original Arrow section. The
track stack fell from 15 to 12, matching the three-item cost for that section.
A right click with the crafted car on the matching anchor consumed the car and
showed `Carrier capture engaged`. The [rendered car and track](survival-client-arrow-car.png)
and [bound section editor](survival-client-arrow-track-editor.png) were captured
from the same connected client.

After the car settled, the player boarded a native `create:seat` with an empty
hand. The crafted Rotation Tool opened the car editor while mounted. The editor
showed `Ready | Restraints open`; closing restraints changed it to
`Ready | Restraints closed`. Dispatch was accepted and the editor then showed
`Aligning / moving | Restraints closed`. The player requested capture, waited
for `Ready | Restraints closed`, opened restraints, and dismounted normally.
The subsequent `/fd status` listed both Arrow carriers with `capture=true`
and `occupants=0`.

The saved schema-6 `fourthdimension_foundation.dat` contains one valid
`arrow_four_rail_v1` section with two profile keys and one
`arrow_controlled` vehicle with two carriers, `stopping=true`,
`restraints=false`, and an empty fault. The saved player inventory retains
12 Arrow track items and the Rotation Tool, alongside 54 iron, 14 copper and
seven red dye; neither anchor nor the car item remains. The integrated server
saved all chunks and sublevels before normal client exit. The saved
`fourthdimension_foundation.dat` SHA-256 was
`89aebad7a2d6204e517ca69ac3ef40e17572930fa915fe42ce97bd6a7d7be72c`;
the full client log SHA-256 was
`5945a0c5c639aec2cba1e81f5b9fb15cea446d910829823b95240116b5134669`.
[Selected runtime lines](survival-client-arrow-events.txt) record the supplied
ingredients, two captured empty carriers after dismount, and the final save.

## Limits

This is a real connected survival client using command-supplied ingredients
and position changes. It does not prove gathering materials or an unassisted
build. The short flat section had no propulsion: dispatch changed the operating
state, but no powered ride or measured chassis travel is claimed. Legacy,
Free Spin, Revised, PAX, full station/winch operation, and the complete
five-family teaching gate remain FD403 work.
