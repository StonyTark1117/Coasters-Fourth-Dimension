# FD401 candidate PAX support-repair blocker after a prolonged hold

On 2026-11-04 the isolated `81ee2e6` combined packaged candidate resumed from
the stopped [PAX fault/phase world](../fd401-candidate-pax-fault-81ee2e6/README.md).
The server and both clients used the same isolated build/JAR with SHA-256
`de64a6833fd0a4e5f7bdbc36ad40b89ff396d48941c7e6ae0ae2e52ca6ca3a70`.
All three processes exited 0 after client menu exits and server `stop`.
The [full logs](repair-server.log.gz), [Dev](repair-dev.log.gz), and
[Observer](repair-observer.log.gz), stopped [world](world.tar.gz), screenshots,
and [SHA-256 inventory](SHA256SUMS) retain the failure without editing native
car state or the foundation save.

The existing PAX demo at `(900,85,65)` had four normally coupled cars, a winch
at `(894,85,73)`, and two prior completed occupied cycles. Dev normally boarded
a fourth-car seat and closed restraints. Observer normally mined the station
curve between `(900,85,65)` and `(900,85,97)` while Dev remained mounted.
Goggles showed **1/16, four cars, Matching track support lost, alignment
required**, and dispatch denied. Observer changed the existing Display Link
to Ride fault through its normal GUI; the [same sign](repair-sign-fault.png)
showed the support fault. Stone footholds and the track item in inventory were
command fixtures; the track breaking and subsequent restoration were client
actions.

Observer used the normal PAX track item on the two surviving anchors to
restore their exact original straight section. Before Repair, Dev's
[editor](repair-editor-restored-before-repair.png) offered Repair matching
track and did **not** offer Realign. Dev used Repair; the Ride fault sign
[cleared](repair-sign-cleared-pending.png) while
[goggles](repair-cleared-pending-goggles.png) still reported 1/16, four cars,
alignment required, and dispatch denied with “Use Realign matching track after
repair before dispatch.” This is a bounded pass for fault-clear and operation
ordering through the Repair step.

The train did **not** survive Realign after the extended support-loss/repair
interval. Dev opened the [Realign editor](repair-editor-after-repair.png) at
about 06:27:51 and selected the action immediately afterward. The server log
records native coupler tension breaks at **06:27:54** (two lead links) and
**06:28:10** (final link). These followed the 06:26:56 four-car post-Repair
Observer packet and preceded the 06:28:20 one-car packet. The close timing
implicates the snap/physics interaction, but this record does not isolate
whether accumulated unsupported drift or the Realign snap is sufficient on
its own. The next
[goggles](repair-realigned-goggles.png) showed **0/4, one car, unaligned**, even
though Dev still had a Create seat `RootVehicle` and the winch block entity
still persisted its original four car IDs. The Ride fault sign remained blank.
No normal recover/capture/unload is claimed. This is a controller telemetry
defect as well as a long-hold coupling/recovery limitation: the rider and
saved train must not silently disappear from the operator's view.

The support removal was at about 06:24:03; the operator spent several minutes
setting up the sign and restoration footholds after removal. This is a longer
unsupported hold than the earlier fresh standalone repair pass. The record
requires a source fix or explicit bounded operator criterion before FD401 can
claim this support-repair case. It does not invalidate the separate candidate
PAX power-fault/resume run, which did not remove track support.
