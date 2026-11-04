# FD401 visible controller availability across native unload and cold reopen

On 2026-11-04, an isolated combined packaged client used the committed
`2ba168d` JAR, SHA-256
`7430b4f4abdb41e248efd778f5cd9f68929d04211d7837085bbae287565cbdcf`
([archived artifact](../fd401-controller-native-availability-2ba168d/README.md)).
The build used `-PfdPackaged -PfdValidation -PfdIntegrations=styles,engineered
-PfdFeNetwork`. The [first raw log](client.log.gz),
[cold raw log](cold-client.log.gz), [stopped world](world.tar.gz), screenshots,
and [hash inventory](SHA256SUMS) retain the run. Both clients saved and quit
normally; both Gradle tasks exited 0.

The run copied the saved normal STOP/cold-repeat Arrow world. Its original
item-placed car had chassis `22c713ad-ed9a-4a5f-9bea-07da6e001c8c` near
`(-500,142,-630)`. Commands built a remote Boarding Station at `(0,149,0)`,
bound that UUID through block data, put a Create Display Link above it, and
configured its Ride status source and sign target at `(0,151,7)` through block
data. These controller, binding, link and target placements were **fixtures**;
this run does not claim normal player placement or link configuration. The
remote station was outside dispatch range, so an available snapshot correctly
denied dispatch for that reason.

With Dev at the remote controller, the original car's chunks unloaded
naturally: `/fd validation availability` reported `chassis=0/1 carriers=0/2`.
The [visible sign](unavailable-sign.png) showed the width-limited prefix
`Vehicle chassis is`, while its block data held the full `Vehicle chassis is
unavailable` message. [Engineer’s Goggles](unavailable-goggles.png) showed the
full message on the loaded station, and the client received an unavailable
telemetry snapshot. This is a live rendered controller/target observation
during actual native body absence.

Forcing the original car's route chunks loaded restored native availability
to `1/1` and `2/2`, with zero rider assignments. Without rebinding, the
[goggles](restored-goggles.png) showed one available four-seat Arrow car,
BOARDING, and the expected out-of-range dispatch denial; the [same sign](restored-sign.png)
changed to `Boarding`. Removing the route force-load tickets returned native
availability to `0/1` and `0/2` and the sign's full unavailable block data.
The controller chunk's temporary force-load ticket was also removed before
saving.

An actual F3+T reload retained the [unavailable sign](reload-sign.png) and
[goggle message](reload-goggles.png). After normal shutdown and cold reopening
of the same world, the fresh client received the unavailable snapshot and
showed the same [goggles](cold-goggles.png) and [sign](cold-sign.png), with
native availability still `0/1` and `0/2`. The log contains the sign block
data and the client snapshot after reopening.

This is one integrated singleplayer client observing a remote controller,
not a late-joining second client. It does not cover reconnect, tracking
re-entry, occupied unloading, or individual optional profiles. The sign's
visible text is width-limited; the stored text and goggles carry the full
reason.
