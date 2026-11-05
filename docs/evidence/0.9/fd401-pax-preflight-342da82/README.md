# FD401 PAX support-loss preflight source change

Committed `342da82` checks the matching support of each loaded car assigned
to an active shuttle before `ShuttleRuntime.tick` advances its native guide.
When support has disappeared, the existing whole-consist fault path now runs
in that callback and the shuttle enters its stationary fault hold. The normal
vehicle loop still checks all other cars. This closes a source-order gap; it
does not establish that the packaged train survives a prolonged hold.

An isolated combined-profile `compileJava` passed offline with one Gradle
worker and a 768 MiB Gradle heap. The preceding named 4,800-tick source
GameTest on the all-body-hold candidate began one batch but was intentionally
stopped before its result when host available RAM fell to 6.7 GiB with no
swap. Its [server log](interrupted-source-test.log) is an interrupted attempt,
not a pass or fail. After shutdown, available RAM returned to about 12 GiB.

The 4,800-tick LIFTING hold and packaged normal-client acceptance remain open.

The next capped source attempt loaded the saved test world and reported
`Shuttle example requires a loaded empty corridor` before the hold began.
The [log](stale-world-test.log) is a fixture setup failure. That 11 MiB
world was moved aside in the temporary build directory so a fresh world
could generate without deleting the prior state. On the fresh-world server,
available host RAM fell from about 11 GiB to 5.5 GiB before the test was
started; the server was stopped. Its [log](fresh-world-memory-stop.log)
contains no GameTest result. The 3 GiB process-group cap reported no OOM.
