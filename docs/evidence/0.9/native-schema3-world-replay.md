# FD903 complete schema-3 world and native-sublevel replay

On 2026-11-02, a packaged historical `0.2.0` dedicated server built from
commit `d97ea010e842b931dc006aad7e9002122dfdc8e1` created a complete world
under save schema 3. This is the parent of the schema-4 change. The server's
normal `/fd demo arrow`, `/fd demo legacy`, and `/fd demo free_spin` commands
created three separate native circuits. The world was saved with `save-all
flush` and then stopped normally. No player joined this source world.

The historical server was built with `./gradlew build`, then started with
`./gradlew runServer -PfdPackaged`. These commands were sent through local RCON
after startup:

```text
forceload add -160 0 -105 95
forceload add -10 0 45 95
forceload add 140 0 195 95
execute positioned -150 140 0 run fd demo arrow
execute positioned 0 140 0 run fd demo legacy
execute positioned 150 140 0 run fd demo free_spin
save-all flush
stop
```

The [archived source world](fixtures/schema3-native-world.tar.gz) contains its
`level.dat`, ordinary chunk and entity regions, Coasters path data, the
`fourthdimension_foundation.dat` record, and six Sable sublevel files. It is
12 MiB unpacked and 5.7 MiB compressed. Its archive SHA-256 is
`3f4f5d820deb9ffde91e576df7a582bd0234c8bfd2ddff2e3fe70e4dc0dbfac3`;
its source `fourthdimension_foundation.dat` SHA-256 is
`c27a35a14f5b6a3e098517c16780637d1eaa9adc49f45b4cbccd3c11e2c2aa7a`.
The source NBT has schema 3, 24 rail records, three vehicle records, and eight
carrier records. The historical packaged JAR SHA-256 is
`3605e44e7498cf36b30b2791e97289ed4a1e63e105f4f2603af352441f722923`.

## Upgrade and reopen

A separate packaged `0.9.0-dev.27` dedicated server built from release commit
`7b40731` opened that full old world. The server identified the addon version
change from `0.2.0` to `0.9.0-dev.27`. With `-PfdValidation`, its RCON query
reported:

```text
fd validation availability
Validation availability chassis=3/3 carriers=8/8 assignments=0
```

The query resolves each saved chassis and carrier UUID against the live Sable
container, so it checks the historical native bodies as well as the addon
records. After a normal `save-all flush` and `stop`, the existing on-disk
verifier reported:

```text
FD903_PACKAGED_MIGRATION sourceSchema=3 upgradedSchema=6 rails=24 vehicles=3 families=arrow_controlled,sns_free_spin,zacspin_legacy identitiesPreserved=true recoveryInvented=false profilesPreserved=true
```

The first upgraded `fourthdimension_foundation.dat` SHA-256 was
`f72dcc2e01a6bcfdff05acedd02d5159695baa5e2bc154aadbd317790460af1c`.
The current packaged server reopened the upgraded world and again reported
`chassis=3/3 carriers=8/8 assignments=0`. After its normal second save, the
same verifier passed again; the final data file SHA-256 was
`3317005efd64c75826b2ee5672c43be2d6a163196ece26c0deecfb128dd2002b`.
The upgraded world still has six Sable sublevel files. The current packaged
JAR SHA-256 was
`d871ba09d1b6536f3391bcc4d869c973fd4fad365ff915735f98e6315dfbfd59`.
[Selected server lines](native-schema3-world-replay-events.txt) record the two
addon versions, normal saves, and server exits.

## Reproduce

Extract the archived source world into an isolated `packaged-server` game
directory as its `world` folder. Build and run the current addon archive with
`./gradlew build` followed by `./gradlew runServer -PfdPackaged -PfdValidation`.
Query `/fd validation availability`, save and stop, then run:

```sh
python3 scripts/verify-packaged-historical-migration.py \
  SOURCE/world/data/fourthdimension_foundation.dat \
  UPGRADED/world/data/fourthdimension_foundation.dat
```

Keep the archived source separate from the upgraded copy. Reopen the upgraded
world once more and repeat the availability query and verifier. The script
compares original rail and chassis identities, complete rotation-profile keys,
families, and absent recovery state.

## Limits

This proves one real schema-3 world with its native sublevels survives the
schema-6 upgrade and a subsequent server restart. A later
[connected-client Arrow ride](native-schema3-client-ride.md) demonstrates
boarding, powered travel, capture, and dismount in that upgraded world. Later
[Legacy and Free Spin client operator cycles](native-schema3-legacy-free-spin-operator.md)
cover boarding, restraints, dispatch requests, capture and dismount for the
other two historical cars without measuring sustained travel. These replays do
not cover every historical world or schema, sustained travel for those two cars,
or two-client migration. Third-party functional-block conversion remains
unsupported; those broader migration cases leave FD903 and the release gate open.
