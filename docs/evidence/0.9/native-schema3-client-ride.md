# FD903 connected-client ride on an upgraded schema-3 world

On 2026-11-02, a packaged `0.9.0-dev.27` dedicated server opened the complete
historical schema-3 world described in the [native-world replay](native-schema3-world-replay.md).
One packaged client joined that upgraded world. A survival player boarded its
original Arrow car, used the car editor to close restraints and dispatch,
travelled around the powered circuit, captured the car, opened restraints, and
dismounted. The client disconnected normally; the server saved and stopped
normally. This checks rider operation on a migrated native chassis, rather than
only the migrated records' ability to load.

## Inputs and commands

The archived source [world](fixtures/schema3-native-world.tar.gz) was created
by packaged historical `0.2.0` at `d97ea010e842b931dc006aad7e9002122dfdc8e1`.
The upgraded server and client used the same packaged `0.9.0-dev.27` JAR built
from release `7b40731`:

| Input | SHA-256 |
| --- | --- |
| Archived schema-3 world | `3f4f5d820deb9ffde91e576df7a582bd0234c8bfd2ddff2e3fe70e4dc0dbfac3` |
| Historical `0.2.0` JAR | `3605e44e7498cf36b30b2791e97289ed4a1e63e105f4f2603af352441f722923` |
| Current `0.9.0-dev.27` JAR | `d871ba09d1b6536f3391bcc4d869c973fd4fad365ff915735f98e6315dfbfd59` |

The current packaged server ran with `-PfdPackaged -PfdValidation`. The client
was launched with:

```sh
DISPLAY=:121 ALSOFT_DRIVERS=null JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon --console=plain \
  -PfdPackaged -PfdConnect=127.0.0.1:25565
```

Local RCON placed the survival player beside the historical Arrow platform with
`tp Dev -156.5 143 32.5 -90 -10`, and supplied one editor item with
`give Dev fourthdimension:rotation_tool 1`. The player then used actual client
mouse and keyboard interactions for mounting, closing restraints, dispatching,
capture, opening restraints, and dismounting. A temporary stone landing was
placed beside the captured car with
`fill -115 142 44 -111 142 49 minecraft:stone` before dismount.

## Observations

| Stage | Server observation | Client observation |
| --- | --- | --- |
| Before boarding | `fd validation availability` returned `chassis=3/3 carriers=8/8 assignments=0` | Arrow car visible in original circuit |
| Empty-hand seat interaction | `chassis=3/3 carriers=8/8 assignments=1`; Arrow carrier `occupants=1` | [Mounted prompt](native-schema3-arrow-boarded.png) showed “Press Left Shift to Dismount” |
| Editor and restraint control | Assignment remained present | Editor showed `arrow_controlled`, `Ready`, `Restraints open`; [Close restraints](native-schema3-arrow-closed.png) changed it to `Restraints closed` |
| Client Dispatch click | Arrow carrier `occupants=1` | [Moving editor](native-schema3-arrow-moving.png) showed `Aligning / moving` with restraints closed |
| Powered travel | Assignment stayed `1`; captured=false during movement | No player teleport after initial placement; server player position travelled from `(-153.50, 144.44, 32.50)` to `(-153.50, 144.44, 53.22)`, then the far side `(-113.50, 144.50, 32.61)`, near side again `(-153.50, 144.44, 44.50)`, and another far-side point `(-133.08, 144.44, 68.50)` |
| Client Capture click | Arrow carrier captured=true, `occupants=1` | [Settled editor](native-schema3-arrow-captured.png) showed `Ready`, restraints closed |
| Open restraints and Shift dismount | `chassis=3/3 carriers=8/8 assignments=0`; player position `(-113.5, 143.0, 46.5)` on the landing | [Open editor](native-schema3-arrow-open.png), then [dismounted view](native-schema3-arrow-dismounted.png) |

The client log records its connection at 13:25:49 and a normal stop at
13:30:26 (`BUILD SUCCESSFUL in 5m 16s`). The server log records Dev joining at
13:25:51, disconnecting at 13:30:12, and normal world and Sable sublevel saves
at 13:30:32 (`BUILD SUCCESSFUL in 5m 59s`). Complete local log SHA-256 values
are `9b2f08b12ac205e27704a501e65329a8fb93386dfa6e91d7d3ed6279e965c85b`
for the client and
`b6be93cd9c297a37b510d01070b2673d243c1bd1baa43e3679145e41360d0653`
for the server. The logs are local run artifacts; the availability and
position observations above were queried through RCON during the run.

After the final save, `scripts/verify-packaged-historical-migration.py` again
reported `sourceSchema=3 upgradedSchema=6 rails=24 vehicles=3`, all three
historical families, preserved identities and profiles, and
`recoveryInvented=false`. The saved `fourthdimension_foundation.dat` SHA-256 is
`c67f256788975459e00ea20a1850edfa0c6624841add4d7ba2f8dc87f32fcdee`.
Its three vehicle records have empty faults, open restraints, stopping state,
captured carriers, and no saved rider seats.

## Scope

This is one connected-client Arrow ride in one historical schema-3 world. Later
[Legacy and Free Spin operator cycles](native-schema3-legacy-free-spin-operator.md)
cover their client controls and unloading without sustained route travel.
Two-client migration, every historical save/schema, and third-party
functional-provider conversion remain open. The editor item, initial player
placement, and safe landing were command-supplied. The vehicle operations
themselves used the connected survival client.
