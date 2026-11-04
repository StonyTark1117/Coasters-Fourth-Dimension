# FD401 broken-consist telemetry replay

On 2026-11-04, the stopped [failed PAX realignment
world](../fd401-candidate-pax-realignment-blocker-81ee2e6/README.md) was
reopened under the isolated combined packaged `2e12e9d` JAR, SHA-256
`1afa7066607313898a11ff628b762be86369a82a62423c3fcce4ffd9008d62f`.
The artifact and build record are in the [successful short-hold
retest](../fd401-candidate-pax-realignment-2e12e9d/README.md). Only the
dedicated server and Observer client ran for this read-only replay; both
exited 0 after normal client menu exit and server `stop`. The [server
log](server.log.gz), [Observer log](observer.log.gz), [stopped world](world.tar.gz),
screenshots, and [SHA-256 inventory](SHA256SUMS) retain the result.

The winch at `(894,85,73)` still saved four PAX car IDs in block data, while
the earlier coupler failure had left one native linked car at the lead. With
Observer facing the same winch, [goggles](goggles-broken-replay.png) now said
**“Shuttle consist changed; recover and rebind before operating”** instead
of the earlier misleading `0/4 (1 car)`. The 06:44:53 Observer telemetry
packet records `available:0`, `dispatch:0`, and this text in both denial and
fault. The existing Display Link at `(894,86,73)` retained Ride fault source
and targeted the same sign at `(894,86,80)`. [The sign](sign-broken-replay.png)
showed the beginning of that message; read-only block data showed its full
front and back text as “Shuttle consist changed; recover and rebind before
operating”. The physical sign clips this long line.

No new car, link, track, rider or controller state was created in the replay.
The telemetry guard is therefore runtime-proven for this saved broken state.
Normal operator recovery and rebind from a broken consist remain unproven.
