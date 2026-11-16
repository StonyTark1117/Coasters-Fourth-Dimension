# World upgrades and backup recovery

The current frozen 1.0 qualification build writes **save schema 8** and uses
**Foundation sync protocol 7** (editor protocol 8; PAX snapshot protocol 1).
It reads declared Fourth Dimension records from schemas
1–8; this is not a converter for arbitrary third-party track or archived branch
formats. Install matching Fourth Dimension builds on clients and server.
[Qualification inputs](evidence/1.0/lead/freeze.md) record the exact pinned
Minecraft 1.21.1, Java 21, NeoForge, Create, Coasters Simulated, Sable, Companion,
Veil and Flywheel versions. An addon discovered after that freeze is not an
incidental upgrade dependency.

## Preserve the original

1. Stop rides and recovery towing normally. For an attached recovery group, use
   **Brake tow**, remove propulsion and wait for the bearing and carriers to
   stop. Detach recovery or keep the complete stopped group on its matching
   loaded route. Do not upgrade a moving tow.
2. Have players disconnect, stop the server normally and wait for its process
   to exit. For singleplayer, close the world and client. Copy the **whole world**,
   including `level.dat`, `region`, `entities`, `poi`, `data`, `playerdata`,
   dimensions and Sable `sublevels`. Copying only the Foundation data file loses
   the native bodies and is not a usable backup.
3. Preserve the prior mod/dependency/configuration instance, world name and JAR
   identities. Keep the original offline world copy unchanged. A per-file
   SHA-256 manifest lets you verify that a later restore is byte-identical.
   Keep backups outside build directories: Gradle `clean` can remove run worlds.

## Upgrade a separate copy

1. Install the selected Fourth Dimension JAR and its qualified dependencies in
   a separate instance. Keep required functional providers and the same optional
   appearance providers present during the first inspection. Immersion belongs
   only on clients; do not put it on the dedicated server.
2. Open a working copy of the complete world. Let the relevant chunks and native
   bodies load before interpreting an unavailable car. `/fd status` reports
   family, occupancy and capture; inspect the actual matching track, carriers,
   station/restraint state and saved programs. Use normal Stop/revalidation when
   the documented family workflow requires it after its chunk arrives.
3. Check ordinary operation on the affected rides. Previously attached recovery
   must remain braked. Confirm its ordinary bearing, retained group, matching
   route and propulsion before explicit **Start tow**. Missing power or repaired
   support does not authorize automatic resume.
   An assigned PAX shuttle interrupted during OUTBOUND or RETURNING reopens in
   FAULT with its original carriage/train/native links retained and requires
   explicit **Recover** after inspection; **Resume** rejects those gravity phases.
   An interrupted CAPTURING recovery permits explicit **Resume** without an
   automatic distant catch. Follow the [PAX interruption workflow](RECOVERY.md#pax-shuttle-interruption-recovery),
   including the unchanged eight-block station realignment limit after track loss.
4. Save and stop the copied instance normally, then reopen it. Confirm the same
   chassis/carriers, programs and rider assignments. Logical player-to-carrier
   seat identity is persistent; Create may recreate its passenger entity UUID
   during reconnect. That is distinct from losing a saved seat assignment.
5. Keep the original backup until inspection succeeds. Apply the selected build
   to production only after the copied world is satisfactory.

## Restore if inspection fails

Stop the upgraded instance and retain its world and logs for diagnosis. Restore
an untouched **complete offline backup** into the matching preserved instance,
verify its manifest, then open it there. To roll back an upgrade, use the original
pre-upgrade backup and prior compatible mods; never load an upgraded schema-8
world in an older build as a substitute for restoring the backup.

Record the family, track coordinates, source/target build and provider versions,
server log, and whether recovery was attached. Do not replace foreign functional
blocks or rewrite saved IDs to force acceptance. Recovery controls do not bypass
track provenance, family, body, power or route guards.

## Supported data and provider boundary

Known historical records retain rail IDs and complete profiles, chassis/carrier
IDs, families and topology. Old records without recovery fields gain neither an
invented recovery partner nor a removed-bearing marker. A saved 4D-to-4D recovery
reference is not reinterpreted as ordinary recovery equipment. Unknown schemas,
unknown functional providers, invalid identities and unsupported conversions
reject rather than silently truncate or substitute geometry.

Exact intact legacy curves may acquire bounded provenance during revalidation.
Changed, missing or foreign curves must not acquire an invented fingerprint or
new track geometry. The admission limits remain 256 sections, 64 vehicles,
8192 profile keys and 512 magnetic spans per dimension, at most eight spans per
section. These storage limits are not a park-performance promise.

Appearance ownership is separate from functional eligibility. The accepted Track
Styles removal/reinstallation case preserves saved appearance references and
functional IDs/profiles, with fallback when the appearance provider is absent.
This does not permit removing a required functional provider or converting its
track. Later adopted core/adapter contracts and schema-8 dependency upgrades are
recorded below; unrelated archived schema-7/protocol-8 provider work is not
certified merely because the current loader also recognizes schema number 7.

## Evidence and limits

- [Historical loader/recovery matrix](evidence/0.9/recovery-schema-migration.md)
  and [native historical fixture](../src/main/java/dev/fourthdimension/gametest/HistoricalMigrationGameTests.java)
  retain their bounded schema/identity/rejection checks.
- [Genuine schema-3 world](evidence/0.9/native-schema3-world-replay.md), created by
  the historical 0.2.0 build, contains 24 rails, three chassis and eight carriers.
  Its accepted [Arrow ride](evidence/0.9/native-schema3-client-ride.md) is retained.
  [Final frozen-input migration record](evidence/1.0/art/migration.md) adds current
  schema-8 identity comparison, sustained Legacy/Free Spin travel, two actual
  Arrow riders and exact offline backup restoration/cold reopen.
- [Provenance backfill](evidence/0.9/create-telemetry/307ec35-legacy-provenance/README.md),
  [appearance-provider lifecycle](evidence/0.9/appearance-provider-lifecycle.md),
  [schema-8 dependency upgrade](evidence/0.9/dependencies/README.md) and
  [FD917 production disposition](evidence/fd917/README.md) retain their actual
  tested inputs. Production art did not change seat anchors or saved programs.

These records cover declared contracts and representative real historical saves.
They do not claim a packaged upgrade from every prior release, every possible
optional-addon subset, or third-party functional-block conversion. Qualification
status and any remaining gate belong to [the current queue](CURRENT-QUALIFICATION.md#current-work-queue)
and [ROADMAP](../ROADMAP.md).
