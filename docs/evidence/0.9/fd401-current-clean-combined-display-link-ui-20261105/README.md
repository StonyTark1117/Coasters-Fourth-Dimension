# FD401 refreshed-JAR combined-profile Display Link UI

On 2026-11-05, the selected clean `ec2fcda` JAR was launched in a Java 21
Styles + Engineered client. Its SHA-256 was
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`, matching
the selected artifact record. The launch used `-Xmx1536M` inside
`systemd-run --user --scope -p MemoryMax=3G`; the scope completed with
`Result=success`. Its observed memory current near the end was about 2.58 GB.
The client exited through the title screen and logged `Stopping!`; the copied
world saved all dimensions and chunks cleanly. No Gradle build ran during the
client session.

The input world was the existing combined Create telemetry fixture copied from
[`fd401-create-telemetry-combined`](../fd401-create-telemetry-combined/README.md),
whose source archive SHA-256 is
`8b65a4ac4ffeaa9c87138f3142228b4be01f43e07f87a29301249e53e6d386a2`. The
packaged mod log recorded `FD_BOARDING_RESTORE queued` and `mounted` for Dev
before the player dismounted. F3 identified the targeted block at `(-6,84,32)`
as `create:display_link`, facing up with `powered=false`; see
[targeted-display-link.png](targeted-display-link.png).

A normal right-click opened the in-game Display Link configuration screen.
[source-picker.png](source-picker.png) shows its existing source as **Ride
fault** and its target as **Line 1**. The checkmark closed the screen. This
establishes that the selected artifact can reach the Display Link UI in the
combined profile and that this saved fixture has a configured source/target.
It does not establish that a source was newly selected or changed during this
run.

This run did **not** capture changing rendered sign output, goggles output, or
a received `FD_CLIENT_TELEMETRY` snapshot. The displayed source value was not
made to transition, and the client log contains no such snapshot record. It
therefore closes no FD401D telemetry acceptance scenario; it only replaces the
previously reported source-UI navigation failure with a bounded UI reachability
observation. The saved world is a fixture, not a fresh player-built route.

The [launch script](run-client.sh), classpath, program and VM arguments,
[dependency hashes](dependencies.sha256), compressed [client log](client.log.gz),
and [checksum manifest](SHA256SUMS) retain the run inputs and evidence.
