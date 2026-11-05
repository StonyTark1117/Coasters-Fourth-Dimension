# FD401D selected-JAR legacy seat target retry — target not acquired

Run date: 2026-11-05. This bounded retry used a fresh server world/game directory,
port, RCON endpoint, display, and client game directory. Earlier evidence bundles
were preserved unchanged.

## Fixture and runtime identity

- Selected artifact SHA-256:
  `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
- Original input world SHA-256:
  `7d5b707ab3a83f440efe0ba3ba70516b7f72efa970709fac578aca430154442a`.
- The server log confirms the exact selected artifact loaded from
  `/tmp/fd401d-family-selected-20261105/build/libs/coasters-fourth-dimension-0.9.0-dev.27.jar`.
- Java 21, Dev `-Xmx1536M` in its own 6 GiB scope, server in a separate 4 GiB
  scope. Dev used private display :345 and its unique absolute game directory.
  Client options are RD6 / SD5. Memory and process preflight is recorded in
  `runtime-checks.txt`. RCON credentials are omitted.

## Targeting observations

Dev joined normally and was teleported to the historical approach position
`(131.5,87,31.5)`. The read-only historical audit reports an accepted F3 target
`20481032 127 20489225 fourthdimension:legacy_seat`, with seat offset
`(0,-1,1)`, and an old normal mount event at this fixture.

This retry's bounded F3 probes at that approach did not reproduce the exact
seat target. F3 showed these ordinary world targets:

- yaw 87°, pitch 31°: `(122,82,31) minecraft:smooth_stone`.
- yaw 87°, pitch 26°: `(120,83,32) minecraft:stone`.
- yaw 85°, pitch 30°: `(122,83,32) fourthdimension:boarding_station`.
- final offset-directed probe yaw 78°, pitch 34°: `(122,82,33) minecraft:smooth_stone`.

None was the requested `20481032 127 20489225 fourthdimension:legacy_seat`.
No right-click was sent at any target. Post-probe `RootVehicle` was absent and
there is no `FD_BOARDING_EVENT ... action=mount` in the server log. The client
quit normally; the server received `stop` and logged all dimensions saved and
the RCON listener stopped.

No Observer was launched because the Legacy seat could not be targeted. The
Legacy occupied output/control transitions, Free Spin, Revised, and trailing
Arrow occupancy remain untested by this retry. This is a bounded target miss,
not a passing family matrix result.

Full logs, input and cleanly saved output worlds, options, screenshots, launch
metadata, and a checksummed manifest are included. No roadmap or unrelated
project files were changed by this evidence update.
