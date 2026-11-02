# FD902 two-client Arrow support recovery

This dedicated-server checkpoint uses two independent packaged clients, **Dev**
and **Observer**, on the same original Arrow carrier. The fixture places each
player in a distinct native seat, dispatches the carrier, removes its exact
matching Coasters graph connection while it is moving, restores that authored
curve, invokes the production repair action, and invokes the separate physical
realignment action.

The server reports two retained rider assignments at the fault, repair, and
realignment checkpoints. The captured client output is retained for replay, but
the current client validator can mark a healthy initial state as realigned; it
is not used as ordered post-repair client-observation proof.

## Reproduction

Run a dedicated packaged server with `-PfdCarrierFixture`,
`-PfdCarrierFixtureRider`, `-PfdCarrierFixtureRiderOnly`,
`-PfdCarrierFixtureRiderFamily=ARROW`, `-PfdCarrierFixtureRiders=2`, and the
support-loss, motion, repair, and realignment flags. Connect two separate
packaged clients with the matching carrier-validation flags. The occupied
fixture spawn is beside the selected Arrow circuit so its native chassis stays
within the clients' view-distance ticket.

## Result

The 2026-11-02 run passed the server-authoritative Arrow sequence: Dev and
Observer occupied distinct seats on one carrier and were retained through
moving support loss, exact repair, and physical realignment. The concise raw
markers are in [two-client-arrow-support-recovery.txt](two-client-arrow-support-recovery.txt).

The new [five-family server matrix](two-client-five-family-server-recovery.md)
extends this native recovery result across every family. Ordered post-repair
client observation, PAX lift dispatch after repair, and a post-repair
reconnect/cold-server restart remain open.
