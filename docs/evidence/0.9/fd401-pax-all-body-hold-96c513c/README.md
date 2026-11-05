# FD401 PAX all-body station and support hold source change

Committed `96c513c` extends the existing transient PAX world hold from the
four chassis to the twelve assigned chassis and carrier bodies. It validates
that every body is loaded before creating a hold, resets their native
velocities together when a hold must be created or rebuilt, and releases the
same handles at the existing dispatch and realignment boundaries.

This responds to the [normal-client outbound support-loss failure](../fd401-pax-longhold-client-blocker-1f13a0e/README.md),
where the affected car's chassis and both carriers dropped relative to the
other cars before its two native links broke. The edit is a mechanics
candidate, not a demonstrated fix: the failed world has not been replayed on
this source, and the intended 4,800-tick LIFTING hold is still open.

An isolated source export with this `ShuttleRuntime` edit passed
[`compileJava`](compile.txt) offline. Gradle used one worker and a 1 GiB heap;
no game server or client was launched for this check because the host had
just exceeded its available RAM during the two-client run.
