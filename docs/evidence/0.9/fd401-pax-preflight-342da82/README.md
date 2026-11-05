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
