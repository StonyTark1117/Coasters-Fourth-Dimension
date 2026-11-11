# FD402E two-client recovery interruptions

FD402E1 (one-rider reconnect) and FD402E2 (braked cold server restart) passed on
2026-11-11. E3–E7 remain separately tracked until their observations are recorded.
The base-profile Arrow run uses two real client JVMs in distinct seats. It adds
only the missing multiplayer interruptions to the accepted
[two-client powered cycle](../two-client-recovery-lifecycle/acceptance.md) and
[one-player native persistence](../linked-train-fresh-process-656725bc-20261105/README.md).

## Retained identity and results

The one chassis is `af683a49-ef71-463b-b45d-b50233a83089`, its ordinary bearing is
`08f497b0-9a7f-4daf-91e4-a626935ca9a5`, and the occupied carrier is
`ee7057c2-9afc-4f3a-b3e2-5839daefbc6b`. Dev retains seat offset `(0,-1,0)` and
Observer retains `(-1,-1,0)`. The other carrier remains part of the original car.

| Case | Observed result |
| --- | --- |
| E2 | Both clients occupied attached, retained, braked recovery before normal server stop at 08:12:16. All dimensions finished saving at 08:12:27 and that JVM exited. A new JVM started with **all fixture automation disabled**. Production boarding restored Dev at tick 1026 and Observer at 1054, to the original carrier/seat offsets; the native link restored between the same chassis and bearing. The saved track and motor also restored. Both clients rendered the occupied carrier. |
| E2 hold | Before shutdown, chassis Z was 12.342404 at ticks 322 and 490. After native restoration settled, Z was 12.317206 at ticks 1197, 1345, 1803 and 2225–2227; bearing Z was 18.317205, preserving six-block spacing. The 0.0252-block restore settling is disclosed. Every sample remained stopping/maintenance/restraints=true; no automatic tow occurred. |
| E1 | Dev used normal Disconnect at 08:15:00. At tick 1803 the server listed only Observer online; Observer remained mounted, both saved assignments remained retained, and recovery stayed braked at the same position. Dev used normal Direct Connection and production restoration remounted the original seat at tick 2038. Both clients again observed the original occupied group, with two assignments and no new vehicle or bearing. |
| Explicit recovery | After both interruptions, Dev clicked rendered **Start tow** at 08:15:33; status changed to stopping=false at tick 2409 and the editor displayed Recovery towing. Dev then clicked rendered **Brake tow**, restoring stopping=true at ticks 2936–2937 with both riders mounted and native links retained. Chain power stayed off. This proves the explicit state transition; powered towing and normal release are carried forward from the accepted shared cycle rather than repeated. |

The server's final normal stop completed all-dimension saving at 08:16:19. The
retained final world is the continuation input for E3–E5. Temporary Create seat
entity UUIDs may be recreated by production boarding; stable identity here means
the same player-to-carrier/seat assignments, chassis, carrier bodies and bearing,
not the ephemeral seat entity object.

## Fixture and artifact boundary

Initial track construction, boarding and attachment used the opt-in validation
fixture. The flat fixture required a validation-only deferred chunk preload/clear
fix, commit `81403a6b`; no production recovery behavior changed. Setup automation
was disabled for the complete cold restart and reconnect. The `editor` helper
only opened the normal editor; Start and Brake were actual rendered button inputs.

[artifacts.json](artifacts.json) records client/build1 and server/build2 SHA-256.
Their only entry differences are the two validation fixture classes, as listed in
[client-server-entry-diff.json](client-server-entry-diff.json). The real clients
therefore stayed on build1 while the server used the corrected fixture build.
Java 21, 1536 MiB heaps, separate client-only 6 GiB scopes, a 4 GiB server scope,
private Xvfb displays :279/:280, and loopback port 25479 were used. Gradle ran
outside the client scopes. Persistent runtime storage is
`/home/braydon/.local/state/fourthdimension-runs/fd402e-arrow-20261111`.

Earlier attempts are excluded from acceptance: two corridor-guard setup failures
preceded the fixture fix. A later `/save-all flush` attempt hit the 60-second
watchdog while its IO worker was blocked writing chunk headers; its incomplete
world restored native bodies but lacked track chunks. The world, crash and thread
dump are preserved under `pre-restart-watchdog` and `partial-save-reopen` in the
runtime directory. The successful fresh fixture used run-local
`sync-chunk-writes=false` and `max-tick-time=180000`; its normal shutdown completed
the save before restarting. Those settings do not change recovery mechanics.

The compressed before/after server logs, both client logs, original screenshots,
launch properties, saved FoundationData and final world archive are retained here.
The client logs also include excluded setup attempts; only the timestamps and
identities above support these two closures. B and F remain closed independently.
