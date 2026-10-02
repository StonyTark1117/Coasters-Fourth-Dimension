# World upgrade and recovery procedure

This procedure applies to a world that contains Coasters: Fourth Dimension
vehicles, functional track, or an attached ordinary-bearing recovery group. It
is the supported operational procedure for upgrading to the current schema-6
build. It does not convert third-party functional blocks, replace a removed
provider, or make a downgrade safe.

## Before the upgrade

1. Stop every active ride and recovery tow. For a recovery group, use **Brake
   tow**, cut ordinary propulsion, wait for the bearing and all carriers to
   stop, then either detach recovery or leave the complete stopped group in a
   loaded, matching-track corridor. Do not begin an upgrade while a tow is
   moving.
2. Stop the server normally, then make an offline copy of the *entire* world
   directory. Keep `region`, `entities`, `poi`, `data`, `playerdata`, and
   `level.dat` together; retain that copy unchanged until the upgraded world has
   been inspected. For single player, close the world before copying its save
   directory.
3. Record the prior mod JAR name, Minecraft/Create/Coasters Simulated versions,
   optional addon versions, and the world name. Preserve the prior instance or
   server directory so the copy can be reopened if restoration is necessary.

## Upgrade on a copy first

1. Install matching current Fourth Dimension JARs on both the server and every
   client that will join it. Do not mix schema-6 clients with an older server,
   or schema-6 server data with an older client build.
2. Keep the same required base dependencies. Keep any optional appearance
   provider installed for this first inspection. An appearance provider is not
   a functional-track conversion tool, and its removal must not be used as the
   first step of an upgrade.
3. Start a copy of the world. Let it finish loading before moving vehicles or
   opening a station. Use `/fd status` to identify Fourth Dimension vehicles,
   their family and capture state. Check that each 4D car remains on matching
   functional track and that its carrier topology is present.
4. For a previously attached recovery group, inspect it while stopped. Recovery
   must remain braked after a load. Confirm the ordinary bearing, matching
   family route and retained 4D group are present before choosing **Start tow**.
   Never use an upgrade to bypass the normal recovery guards.
5. Save, stop the copy normally, and reopen it once. Only after this inspection
   succeeds should the same upgrade be applied to the production world.

## If inspection fails

Stop the upgraded instance without continuing play. Restore the untouched world
directory copy into the preserved prior instance; do not downgrade the upgraded
world in place. Keep the failed copy and its `latest.log` for diagnosis. Record
the affected vehicle family, its track coordinates, installed optional addons,
and whether recovery was attached. Do not delete or replace external functional
track blocks in an attempt to repair the saved Fourth Dimension state.

## Supported data boundary

The schema-6 loader accepts prior schemas 1 through 5. Where the old record has
no recovery fields, loading leaves `recovery_cart` absent and does not invent a
removed-bearing marker. Saved 4D-to-4D recovery references are rejected rather
than being reinterpreted as ordinary recovery equipment. The current regression
proves that contract for the occupied five-family fixture; see the
[FD903 migration evidence](evidence/0.9/recovery-schema-migration.md).

This is not proof of a packaged upgrade from every historical release, a
real-client migration, provider removal, or a conversion between this addon's
functional track and another mod's functional blocks. Those FD903 cases remain
open. Restore the backup instead of attempting an unsupported conversion.
