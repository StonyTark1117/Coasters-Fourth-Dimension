# FD401D selected-JAR controller lifecycle: Arrow

Run date: 2026-11-05. This package records one combined-profile client/server run using the frozen selected artifact. The run exercised the controller's unavailable → re-entry → unavailable lifecycle and received client telemetry at each state.

## Run identity

- Selected JAR: `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`. `artifact-and-world-hashes.txt` records the copied JAR and final save hashes.
- Profile: packaged + validation + FE networking, integrations `styles,engineered`; Create and Create Crafts & Additions were loaded. No source or JAR was changed for the run.
- Java: OpenJDK 21.0.13. Server and clients used `-Xmx1536M`. Dev and Observer each ran in a task-owned 3 GiB systemd scope; server used its separate 4 GiB scope. Client view/simulation distance was 6/5; server view/simulation distance was 6/6.
- Private Xvfb displays: Dev `:301`, Observer `:302`. Isolated directories and ports: server `25695`, RCON `25696`; separate Dev and Observer game directories.
- Fixture source: the current selected-JAR combined Arrow save retained at `/tmp/fd401d-clean-telemetry-20261105/game/saves/world`. The final copied world is `final-world.tar.gz`; the initial copied-world manifest SHA-256 was `c6e1ca8d123160f7a599ef0c23dbd3736c323c368d33ace76dc81bcb013d671b`. The saved final `level.dat` SHA-256 is recorded in the hash file.

## Fixture and transitions

The test kept the original Arrow ride and added a remote Boarding Station at `(1000,149,1000)`, bound to chassis UUID `efb70f72-5fc2-44b9-8f49-4657b64c1ece`. A stone floor and Create Display Link/sign were placed as remote observation fixtures. The remote station remained loaded while the default world spawn was moved from the ride area to the remote station, allowing the route to unload. Fixture commands and RCON responses are retained in this directory.

| State | Authoritative server evidence | Observer packet |
|---|---|---|
| Initial, before moving spawn | `baseline-rcon.txt`: `chassis=1/8 carriers=2/20`; original station BE is bound to the Arrow chassis. | `observer-console.log` at 08:16:57: available, captured, boarding; `arrow_controlled`, phase `BOARDING`, 1 car, capacity 4, fault `End of compatible track; capture and inspect the route`. |
| Unavailable | After moving spawn to the remote station and waiting, `unavailable-attempt-rcon.txt`: `0/8`, `0/20`; no force-load ticket. | `observer-console.log` at 08:22:43: `available=0`, `cars=0`, empty family/phase, fault and denial `Vehicle chassis is unavailable`. `unavailable-observer-f3.png` confirms the Observer targets the remote `fourthdimension:boarding_station` BE at `(1000,149,1000)`. |
| Re-entry | First ticket attempt marked only chunk `[-1,2]`, the remote/original station chunk, and stayed at `0/8`, `0/20`. Inspection of `selected-foundation.json` showed the saved rail footprint at `(8,83,12)`, `(0,83,20)`, and `(0,83,56)`. A single corrected ticket covered the four route chunks `[0,0]`–`[0,3]`; after five seconds, `reentry-settled-rcon.txt` reports `1/8`, `2/20`. | `observer-console.log` at 08:26:09: `available=1`, `boarding=1`, `captured=1`, `cars=1`, capacity 4, family `arrow_controlled`, phase `BOARDING`, with the same end-of-track fault. Extracted in `reentry-observer-telemetry.txt`. |
| Returned unavailable | `return-unavailable-settled-rcon.txt`: all force-load tickets removed and `0/8`, `0/20` after five seconds. | `observer-console.log` at 08:26:24 returns to `available=0`, empty family/phase, and `Vehicle chassis is unavailable`. Final screenshot: `return-unavailable-observer.png`. |

The first force-load was a setup miss: it targeted the station chunk, not the saved route footprint. The corrected four-chunk ticket produced the intended re-entry. The ticket was then removed; no ticket remained for the returned unavailable state.

## Limits

The Display Link block and sign were present, but the link was not configured to target the station or a source. Final RCON reads show `LinkedGauges: []` and blank sign text. Therefore this run closes the ordered server/client controller availability lifecycle, but does not establish linked-sign output. Goggles were given to the clients, but this lifecycle bundle does not claim a matched goggles reading. The ride was unoccupied throughout; this run does not cover rider occupancy, dispatch, or a moving phase.

## Evidence files

- `server-console.log`, `dev-console.log`, `observer-console.log`: startup/runtime/shutdown logs; observer log contains all four received state snapshots.
- `baseline-rcon.txt`, `unavailable-attempt-rcon.txt`, `reentry-rcon.txt`, `route-forceload-attempt.txt`, `reentry-settled-rcon.txt`, `return-unavailable-rcon.txt`, `return-unavailable-settled-rcon.txt`, `final-state-rcon.txt`: command inputs and authoritative output.
- `selected-foundation.json`: readout of the saved route coordinates used to correct the ticket.
- `unavailable-observer-f3.png`, `return-unavailable-observer.png`, `observer-at-remote-controller-live.png`, `dev-at-remote-controller-live.png`: screenshots retained from this run.
- `final-world.tar.gz`: server world after save and shutdown.
- `artifact-and-world-hashes.txt`, `SHA256SUMS`: integrity manifests.
