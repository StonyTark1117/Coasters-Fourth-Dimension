# FD902 two-client Arrow support recovery

This dedicated-server checkpoint uses two independent packaged clients, **Dev**
and **Observer**, on the same original Arrow carrier. The fixture places each
player in a distinct native seat, dispatches the carrier, removes its exact
matching Coasters graph connection while it is moving, restores that authored
curve, invokes the production repair action, and invokes the separate physical
realignment action.

Both clients independently report the replicated support-loss, post-motion
support-loss, repaired, and physically realigned states while their local rider
remains mounted. The server reports two retained rider assignments at the fault,
repair, and realignment checkpoints.

## Reproduction

Run a dedicated packaged server with `-PfdCarrierFixture`,
`-PfdCarrierFixtureRider`, `-PfdCarrierFixtureRiderOnly`,
`-PfdCarrierFixtureRiderFamily=ARROW`, `-PfdCarrierFixtureRiders=2`, and the
support-loss, motion, repair, and realignment flags. Connect two separate
packaged clients with the matching carrier-validation flags. The occupied
fixture spawn is beside the selected Arrow circuit so its native chassis stays
within the clients' view-distance ticket.

## Result

The 2026-11-02 run passed. The server seated Dev and Observer on distinct seats
of one Arrow carrier, retained both through moving support loss, exact repair,
and physical realignment. Each client independently passed the replicated
validation with `riderMounted=true`, all four recovery observations true, and
more than two blocks of native chassis movement. The concise raw markers are in
[two-client-arrow-support-recovery.txt](two-client-arrow-support-recovery.txt).

This closes the Arrow portion of simultaneous real-client support recovery. It
does not cover concurrent recovery for Legacy, Free Spin, Revised, or PAX; PAX
lift dispatch after repair; or a post-repair reconnect/cold-server restart.
