# Occupied Engineered cold restart and saved-passenger restoration

Parent: `1846f5db302212be71385bbde376669bc61fbcb7`. This checkpoint uses
Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Sable 2.0.3, Coasters 0.1.5,
and optional Engineered 1.1.1 / Track Styles 1.2.0. It runs packaged JARs on
Java 21.0.13.0.2+1. Exact artifact and source hashes accompany the results.

## Regression

A normal clean shutdown saved each native pig inside its Create seat's
`Passengers` NBT. On reopening, all five native mounting events were canceled:
Fourth Dimension recognized the carrier and saved occupant, but applied its
closed-restraint new-boarding guard during Minecraft's restoration. The pigs
remained alive, but became unmounted. The Arrow assertion stopped the scenario
before dispatch. The baseline logs include all five rejected mounts.

The fix wraps the single `Entity.startRiding` call in `EntityType`'s recursive
saved-entity loader. It permits that existing relationship only for an empty
Create seat entity on an actual Fourth Dimension seat block at a registered
carrier seat offset. The existing restoration exception is scoped to that call
and removed in `finally`. New boarding, dismount restraints, readiness, and
Engineered propulsion checks retain their original validation. Other entities
and unregistered seats pass through unchanged. No schema or network change is
needed; human-player reconnect handling remains separate.

## Scenario

The new opt-in dedicated run uses a fresh world, five original 64-block tracks,
24-block native motor spans, STOP/LAUNCH/COAST programs, and native observers.
It places one pig passenger in each family's car, closes restraints and arms
LAUNCH with real redstone. All cars remain captured before saving.

The runner stops the server normally and starts a new JVM on the same save.
It checks exact chassis and passenger UUIDs, retained closed-restraint state,
readiness, health, negligible displacement, all program steps, anchor links,
span lengths, and observer targets. Forced new boarding must still fail.
Validated dispatch then moves the four non-PAX cars across their native
observers. Their controllers advance to COAST, and normal capture/restraint
release permits unloading. PAX remains held throughout.

A third JVM reopens a byte-verified copy of the prepared save without Engineered.
It checks that the optional controller/observer block entities are gone while
all five original cars and riders remain captured, ready, healthy, and able to
unload normally. This is removal resilience; it does not preserve removed
Engineered block content for later reinstallation.

The ordinary GameTest matrix explicitly skips the cold-restart case unless its
dedicated phase is selected. A green suite alone is not restart evidence.
The dedicated runner requires the phase success marker as well as a clean
process exit; vanilla GameTest failures can otherwise coexist with exit zero.

## Reproduction

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JAVA_TOOL_OPTIONS=-Xmx2G
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build
python3 scripts/run-engineered-restart.py build/new-engineered-restart
python3 scripts/run-engineered-restart.py build/new-combined-restart --profiles styles,engineered
```

Each output directory must be new. The script uses a unique temporary runtime,
records its path, and retains the prepared world and its file hashes in the
output directory. No existing server or user save is selected. The temporary
server binds only to loopback, uses a random local RCON credential, and is
stopped normally. Do not publish its `server.properties` as evidence.

## Verified results

- Engineered-only and Engineered/Track Styles each passed all three dedicated
  phases in separate JVMs: prepare, occupied reopen, and reopen after Engineered
  removal. Each run retained the same five chassis and passenger UUIDs.
- Both installed-addon restores completed four native departures with observer
  redstone and automatic COAST advancement, followed by capture/unloading. PAX
  stayed captured. All five families rejected forced new boarding in every phase.
- All 26 unit tests passed. Base and combined packaged suites each passed all 44
  registered native tests, including explicit cold-restart skips outside the
  dedicated runner. The combined suite retained the 18 blocked / four valid
  native motor probes and the earlier five-family sequence regression.
- `python3 scripts/verify-engineered-restart.py` verifies identities, native
  operation markers, negative controls, failure evidence, unit results, and file
  hashes. New Java files pass the formatting check; the diff has no whitespace errors.

Tested JAR SHA-256:
`7c1f37fd55fb04e210b2752812f549b56049a8e0a5ce31e9a27bafeb94960b19`.

## Retained diagnostic attempts

The baseline is a required GameTest failure followed by a clean server shutdown,
so Gradle exits zero. Its failed assertion and canceled mount events are the
failure evidence. The final test adds forced-boarding rejection and addon-removal
checks to that baseline; the original test source and hash are retained.

The `slow-save-*` files retain an attempt whose runner exceeded its 90-second
shutdown wait on the checkout filesystem. Its thread dump shows the server
waiting for the chunk worker, which was writing region headers; save bytes
continued growing. The server subsequently finished saving and exited cleanly after 13m 52s. That attempt is not counted
as a completed restart. The final runner uses separate temporary runtime
folders, preserves the prepared snapshots, and allows ten minutes for shutdown.

## Limits

These are five stationary captured single cars with one native non-player rider
each at shutdown. They do not certify moving shutdown, arbitrary controller
programs, external FE networks, chunk interruption, human-player reconnect,
two-client operation, or a full PAX shuttle cycle. The remaining FD305/FD401 and
FD901–FD904 acceptance items and the complete six-integration objective stay open.
