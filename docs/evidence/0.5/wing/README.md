# Wing acceptance — complete, 2026-11-15 UTC

Owner `01a1dd1d-1468-7f81-8f31-5532ad7a178d`; primary main. FD1101/501A/1101A–C
and FD904C1 are complete; [FD503 reconciliation](../acceptance.md) closes0.5.
No remaining assigned runtime work or new0.6 activation.

| Required behavior | Accepted evidence |
| --- | --- |
| Fixed four-seat ordinary-track contract, native movement/two-car links, stopping, schema6, obstruction rejection | [native result](native/accepted-result.txt), [dimension survey](geometry-survey.json), WingContractTest/FoundationMathTest |
| Normal boarding, close/Dispatch, full banked lap, station stop/open/unload; fixed group angle0 | [occupied](occupied/README.md) |
| Exact seat restoration, normal Survival craft/place, dye and restored color, native Chain link | [construction](construction/README.md), normal click/server event extract |
| Two-car bend/bank geometry, eligibility, rider motion, return stop/unload | [linked bank samples](../comfort/wing/), normal coupling results |
| Selectable saved comfort view, independent look, arms/body, common rotary inversion, exit restoration | [comfort](../comfort/README.md) |
| Functional original assets, recipe, livery and playable ordinary-loop example | [player guide](../../../WING.md), construction/occupied captures, generated original assets |

Normal track authoring and the shared original-family remote/lifecycle paths retain
accepted0.4 evidence; Wing uses unmodified native anchors, curves, Chain drive/link
input and Sable seat/body replication. The Wing-specific item/recipe/placement,
fixed body eligibility and occupied outcome are verified above. Fixture track,
ingredient supply, position setup and powered station commands are disclosed;
they do not claim a newly hand-built survival park. C reuses A/B evidence rather
than repeating it. No optional adapter or later vehicle implementation is needed.

First occupied run used cefd4fae's implementation; comfort production is cd949caa.
[Final artifact](final-artifact.txt) identifies the diagnostic build, which changes
only opt-in observation. [Cleanup](final-units.txt): all client/server/Xvfb units
inactive/MainPID0/Result=success. Native GameTest had also saved and exited.
Dev disconnected20:57:05; all dimensions saved20:57:21; RCON listener exited20:57:23.

Retained world/run: `/home/braydon/.local/state/fourthdimension-runs/fd1101-wing-20261114`,
world `server/WingWorkshop`; run.json owner above, formerly :310 and loopback
25630/25631. Wing cyan/lime cars are linked, stopped/open/empty at the station;
Arrow camera fixture is stopped/open/empty. No live handoff. Client1536M/6G,
server2048M/4G, Java21; later private server sync-chunk-writes=false before launch,
with successful final flush. Preserve these worlds/evidence for a named reopen.

Resolved setup issues: below-track Chain ray selected the closer track curve;
side bearing BLOCK hits passed. Arrow backrest aim missed its half-height cushion;
measured transform/shape correction passed. No production workaround was added.
