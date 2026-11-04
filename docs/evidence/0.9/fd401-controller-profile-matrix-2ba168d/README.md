# FD401 visible controller availability in the three optional profiles

On 2026-11-04, isolated packaged clients ran the committed `2ba168d` source
with `-PfdPackaged -PfdValidation`. The base client had no optional integration;
the Styles client added `-PfdIntegrations=styles`; the Engineered client added
`-PfdIntegrations=engineered -PfdFeNetwork`. Each built JAR has SHA-256
`7430b4f4abdb41e248efd778f5cd9f68929d04211d7837085bbae287565cbdcf`,
the same byte identity as the [archived combined-profile artifact](../fd401-controller-native-availability-2ba168d/README.md).
The [hash inventory](SHA256SUMS), raw logs, screenshots and stopped worlds retain
the runs.

Each run copied the [combined-profile remote-controller world](../fd401-controller-live-unavailable-2ba168d/README.md)
before entering it. The loaded Arrow station at `(0,149,0)` remained bound by
command fixture to the original item-placed car near `(-500,142,-630)`. The
Create Display Link and sign at `(0,151,7)` were also command configured. The
car's chunks unloaded normally when the player stayed at the remote station;
route force-load tickets then restored it, and removing those tickets let it
unload again. The remote station remained outside dispatch range.

| Profile | Native availability during the middle phase | Client snapshots | Visible evidence |
| --- | --- | --- | --- |
| [Base](base-client.log.gz) | `1/1` chassis, `2/2` carriers | unavailable → BOARDING → unavailable | [unavailable goggles](base-unavailable.png), [available goggles](base-available.png), [return to unavailable](base-return-unavailable.png) |
| [Styles](styles-client.log.gz) | `1/1` chassis, `2/2` carriers | unavailable → BOARDING → unavailable | [unavailable goggles](styles-unavailable.png), [available goggles](styles-available.png), [return to unavailable](styles-return-unavailable.png) |
| [Engineered](engineered-client.log.gz) | `1/1` chassis, `2/2` carriers | unavailable → BOARDING → unavailable | [unavailable goggles](engineered-unavailable.png), [available goggles](engineered-available.png), [return to unavailable](engineered-return-unavailable.png) |

At either unavailable endpoint, `/fd validation availability` gave `0/1`
chassis and `0/2` carriers, and the client received `Vehicle chassis is
unavailable`. At the loaded midpoint, the client received one four-seat Arrow
car in BOARDING and the expected `Vehicle is outside station range` dispatch
denial. The base and Engineered logs also retain sign block data with the full
unavailable message. The visible sign is width limited, as in the combined run.

All three clients saved their worlds and reported `BUILD SUCCESSFUL`. Base and
Styles process exits were 0. The first Engineered terminal session returned
143 despite a normal world save and Gradle `BUILD SUCCESSFUL` in its log. A
separate [Engineered cold reopen](engineered-reopen.log.gz) of its stopped world
received a fresh unavailable snapshot, showed the [same goggles message](engineered-reopen-unavailable.png),
saved, and exited 0 with `BUILD SUCCESSFUL`.

The base and Styles world copies contained Engineered and Create: Crafts &
Additions blocks and items from their combined-profile origin. Those clients
reported unknown registry keys and substituted missing blocks with the default
block. This evidence therefore covers the functioning remote 4D controller,
telemetry packets, goggles and Create sign in each optional classpath, but is
not a clean optional-profile operator world. The Engineered-only copy omitted
Styles while retaining the Original appearance; this run does not exercise
Styles decoration, occupied cars, Engineered FE power, or normal player setup
of the command-built remote station and Display Link.
