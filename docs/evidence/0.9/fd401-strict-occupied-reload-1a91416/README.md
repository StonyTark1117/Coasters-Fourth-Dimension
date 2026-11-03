# Strict occupied resource reload, packaged combined profile

Source `1a91416aefc36abbcd74b46d00473e9d4a9861eb` was checked out at
`/tmp/fdoccupied-reload-1a91416` and built as the distributable
`coasters-fourth-dimension-0.9.0-dev.27.jar` (SHA-256
`bb8fb57f8babaf090c52c460739ddd6367d5f8235a90e42fb5bfe01f9a833526`).
The Gradle packaged-client classpath loaded the pinned standalone addon, Track
Styles 1.2.0 and Engineered 1.1.1 as optional runtime dependencies. The exact
command, dependency hashes, source/JAR identity and world hashes are in
`strict-metadata.json`. No GitHub Actions minutes were used.

The first client run was **setup only**. Quick Play reported that its named world
did not exist. I created `FD401OccupiedReloadStrict1a91416` through the Minecraft
world-creation screen without carrier-fixture flags, then let the client complete
its one requested resource reload, render chat and shut down normally. The
pre-run save was captured before the strict client launch as `pre-world.tar.gz`
with per-file hashes in `pre-world.sha256`; it contained no
`fourthdimension_foundation.dat`. `setup-client.log` and `setup-metadata.json`
preserve this separate preparation run. The saved `level.dat` SHA-256 was
`e10b486e04b383fa57d0443d2e0a7bb508c9799eb112bc483797c079566bb390`.

The second client used that already-existing world with opt-in Arrow
carrier/rider/motion fixture flags, one font reload, and the strict occupied
reload gate. Its Gradle process exited **0** (`BUILD SUCCESSFUL`); Minecraft
stopped normally and saved all dimensions. The key events in `strict-client.log`
are:

| Local time, 2026-11-03 | Observed event |
| --- | --- |
| 03:14:10–11 | One Arrow vehicle, two carriers and eight rails created; Dev boarded the FD carrier. |
| 03:14:36 | The fixture dispatched the identified Arrow chassis. |
| 03:14:37 | Strict gate armed player `380df991-f603-344c-a090-369bad2a924a`, mount `8000181c-e384-412e-b9e9-005c978ee63a`, chassis `d8770b9c-246a-4b05-a8c2-a2d54603d253`, carrier `4d26548e-ecb7-4d98-9b97-f8f3c7ba5360` after 501 mounted ticks and 1.08314 blocks of chassis displacement. It then requested reload. |
| 03:14:38 | Font generation advanced from 1 to 2 and the requested reload completed successfully. |
| 03:14:40–41 | Gate observed completed reload, then passed after 20 occupied ticks, 109 geometry stages and 1.84616 blocks further chassis displacement. |
| 03:14:41–42 | Client stopped; integrated server saved all dimensions. |

The strict source checks that the **same player, seat mount, chassis and carrier**
remain bound on every client tick from request through reload and the post-reload
sample. It fails on a level or identity change. The gate counted Original FD
track geometry stages while the identified Arrow chassis moved. That metric is
global to Original rails in the camera's 160-block range; it does not identify
the rendered rail as the ridden route. The save used Original appearance, so the
run has zero styled decoration pieces and does **not** certify Styles rendering.
It is a fixture-created occupied client result, not a normal player/operator
workflow, full-turn ride, multiplayer result, tracing-disabled result or the
remaining FD401/FD402/FD901–FD905 acceptance.

`post-world.tar.gz` and `post-world.sha256` capture the stopped save, including
the fixture's foundation and path data. `evidence.sha256` verifies every retained
file in this bundle. The only excluded save file is `session.lock`, whose bytes
do not describe world content.
