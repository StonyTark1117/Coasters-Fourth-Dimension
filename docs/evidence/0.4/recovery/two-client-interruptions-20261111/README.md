# FD402E two-client recovery interruptions

FD402E1–E5 passed on 2026-11-11: one-rider reconnect, braked cold server
restart, native chunk return, temporary coupling loss/restore and temporary
bearing unavailability/return. E6/E7 permanent removal remain separate.
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

## E3–E5 native interruption continuation

A server-only validation helper from commit `808a1b7a` was built in isolated
`fd402e-e6e7-build3`; the two clients and production behavior were unchanged.
[RecoveryInterruptionValidation.java](RecoveryInterruptionValidation.java) records
the exact fixture source used. It invokes native holding/unlink/link APIs and
reports state; it does not edit rider assignments or implement recovery logic.
The runtime sequence continued the E1/E2 final world with setup automation off.

| Case | Evidence and result |
| --- | --- |
| E3 chunk return and E5 temporary bearing loss/return | At tick 4471, one native `moveToUnloaded` call on the ordinary bearing unloaded its dependency group. The helper asserted all four original IDs were held: chassis, both carriers and bearing. Status reported `loadedIds=[]`, all four `heldIds`, both original rider assignments, stopping=true, and recoveryCartRemoved=false. Native chunk return restored the original link; ticks 4649–4651 showed all four loaded, none held, both real clients mounted, original seat maps, and stopping=true. Rendered Start at tick 4857 changed stopping=false and cleared the retained fault; rendered Brake restored the hold. |
| E4 temporary coupling loss/restore | Native unlink removed the exact chassis-bearing edge at tick 5163. At tick 5186 the production unavailable-equipment fault was present, both riders remained mounted, and all bodies/seat assignments were retained. Rendered Start was rejected at tick 5371 with “Restore the complete matching recovery consist…” visible. Native relink restored the same pair at six-block spacing at tick 5396. It stayed braked/faulted through tick 5620; rendered Start explicitly cleared the fault at tick 5801. Rendered Brake left both clients mounted, stopping=true, faults=[] and original links at ticks 5952–5953. |

E3/E5 share a single supported native lifecycle event: unloading this bearing
unloads the coupled chassis/carriers as a dependency group. The same measured
transition establishes the chunk-return path and the bearing's temporary
unavailability without permanent removal. This is a controlled native unload
with real clients connected, not a claim that occupied player tickets naturally
expired. The brief holding interval is established by native state assertions;
both clients establish the occupied restored result. Accepted
[five-family native recovery](../operator-acceptance.md#native-validation) and the
existing native recovery tests supply unavailable Start/detach rejection and
family breadth. Permanent removal is deliberately left to E6/E7.

The helper's generic action text says “permanently removed recovery coupling”
when the native edge is removed; E4 explicitly restores that same edge, so this
record classifies the complete event as temporary and does not close E6.
The whole group stayed retained. No new chassis, carrier, bearing or seat
assignment was allocated by either interruption action.

Server logs are [e3-e5-server.log.gz](e3-e5-server.log.gz); both clients' logs and
screenshots 026–032 are adjacent. The final world and FoundationData preserve the
same original IDs and two rider assignments. The server saved all dimensions and
exited at 08:19:45. Both clients exited normally at 08:20:55, then their private
Xvfb displays stopped. Sampled client peaks were 2,642,866,176 and 2,664,505,344
bytes, below their respective 6 GiB scopes. The saved world is intentionally
still braked/occupied for continuation; no extra detach cycle is claimed here.

`verify.py` checks original identity/seat preservation, native hold/restore and
coupling-loss markers, rendered-input state transitions and normal process exits.
