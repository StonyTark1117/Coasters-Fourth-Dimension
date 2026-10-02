# FD902 occupied five-family disconnect and reconnect

This dedicated-server checkpoint exercises an occupied carrier from each of the
five supported families through an actual client disconnect and a fresh
client-process reconnect. It uses Fourth Dimension `0.9.0-dev.27`, Create
`6.0.10`, Coasters Simulated `0.1.5`, Track Styles `1.2.0`, and Coasters:
Engineered `1.1.1`.

## Method

The packaged server ran on `127.0.0.1:25581` with the two audited optional
addons. Original Arrow began from its existing recovered world fixture. The
other four families used their existing `/fd demo recovery_*` fixture commands
at clear, separated locations. A local-only server command, registered only
when the `fourthdimension.validation` JVM property is true, called the real
`SeatBlock.sitDown` path on a ready unoccupied carrier seat:

```
fd validation board Dev
```

The command reported `Validation boarded Dev` for each newly created fixture.
Before every disconnect, the server's normal `fd status` reported exactly one
occupant on the intended carrier, and `data get entity Dev RootVehicle`
identified a native `create:seat` mount. No saved rider assignment, native
body, or seat entity was edited.

The client process was terminated after each boarding, producing the server
record `Dev lost connection: Disconnected`. A new client JVM joined the
unchanged live server. Every family produced `FD_BOARDING_RESTORE queued` and
then `mounted` before its deadline:

| Family | Restored carrier | Mounted game tick | Deadline tick |
| --- | --- | ---: | ---: |
| Original Arrow | `7b127e8a-ef8f-4435-a964-1d051ae712b2` | 110276 | 110473 |
| Legacy | `747965a7-abdd-48d0-839a-b2fb19b22e39` | 112025 | 112224 |
| Free-spin | `ebab2e69-604d-4cf4-b901-f978d9fbd11e` | 113089 | 113287 |
| Revised | `6becfb0c-7c48-4984-9f6d-acb911dac273` | 114066 | 114265 |
| PAX | `690ab63a-85b2-4906-b7ec-238a3976e267` | 115090 | 115289 |

After every reconnect, `data get entity Dev RootVehicle` again identified one
native `create:seat`; `fd status` continued to report one occupant on the
intended carrier. Client diagnostics reported the PAX carrier with Dev's UUID
in `occupants` and `riding=true`; the server mount and RootVehicle checks cover
the other four fresh clients.

## Result and limits

The live server retained the assigned rider through disconnect and restored a
fresh client process to the intended occupied carrier before the 200-tick
deadline for every supported family. The test covers deliberate client-process
disconnects on stationary recovery fixtures. It does not cover a server restart,
a sublevel/chunk unload, a route in motion, or concurrent riders. Those remain
FD902 acceptance work.

## Legacy server-process restart

The Legacy fixture was then rebuilt in a separate recovery corridor, boarded
through the same validation-only seat invocation, and confirmed with one
occupant on carrier `4ad9b08b-7283-48ce-93b9-c83dd2e4e151`. The client and
dedicated server were stopped. A newly launched dedicated-server process opened
the unchanged world; a new client process joined it.

The restarted server logged:

```
FD_BOARDING_RESTORE queued player=Dev assignments=1 deadlineTick=118532
FD_BOARDING_RESTORE mounted player=Dev gameTick=118334 deadlineTick=118532
```

`data get entity Dev RootVehicle` reported one native `create:seat`, and
`fd status` again showed exactly one occupant on the same Legacy carrier. This
is an occupied Legacy cold-restart result. It does not establish cold-restart
coverage for the other four families, moving routes, chunk unloads, or multiple
simultaneous riders.

## PAX server-process restart

The occupied four-car PAX shuttle from the normal-unload validation was saved
and stopped through the dedicated server's normal stop path. A fresh dedicated
server process opened that unchanged world. Before a client joined, the
read-only availability report showed the expected held state:

```text
Validation availability chassis=0/4 carriers=0/8 assignments=1
```

A fresh client then caused normal loading of all four chassis and eight
carriers. The server recorded:

```text
FD_BOARDING_RESTORE queued player=Dev assignments=1 deadlineTick=203007
FD_BOARDING_RESTORE mounted player=Dev gameTick=202809 deadlineTick=203007
```

Availability returned to `chassis=4/4 carriers=8/8 assignments=1`, and RCON
again identified Dev's RootVehicle as a native `create:seat`. This establishes
an occupied multi-car PAX cold restart. The later Arrow, Free Spin and Revised
checks complete stationary cold-restart coverage for all five families;
moving-route and concurrent-rider lifecycle coverage remain FD902 work.

## Arrow server-process restart

A fresh stationary original Arrow fixture was boarded through the same real
`SeatBlock.sitDown` validation path. After its client and the dedicated server
were stopped normally, a new dedicated-server process opened the unchanged
world. Before any client joined, the read-only report retained its assignment
while the native bodies remained unloaded:

```text
Validation availability chassis=0/1 carriers=0/2 assignments=1
```

The fresh client caused the chassis and both carriers to load. The server then
recorded:

```text
FD_BOARDING_RESTORE queued player=Dev assignments=1 deadlineTick=207654
FD_BOARDING_RESTORE mounted player=Dev gameTick=207456 deadlineTick=207654
```

Availability returned to `chassis=1/1 carriers=2/2 assignments=1`. RCON
identified Dev's RootVehicle as a native `create:seat`, and fresh-client
diagnostics showed Dev in carrier `3458758f-585a-44fe-9902-63739fa460a8` with
`riding=true`. This establishes an occupied original Arrow cold restart. Moving
routes and concurrent riders remain FD902 work.

## Free Spin server-process restart

A fresh stationary Free Spin recovery fixture was boarded through the same real
`SeatBlock.sitDown` validation path, then its client and dedicated server were
stopped normally. The newly launched dedicated server opened the unchanged
world. Its read-only report before a client joined retained the assignment with
the chassis and all four native carriers unloaded:

```text
Validation availability chassis=0/1 carriers=0/4 assignments=1
```

The fresh client loaded the fixture. The server recorded:

```text
FD_BOARDING_RESTORE queued player=Dev assignments=1 deadlineTick=212480
FD_BOARDING_RESTORE mounted player=Dev gameTick=212282 deadlineTick=212480
```

Availability returned to `chassis=1/1 carriers=4/4 assignments=1`. RCON
identified Dev's RootVehicle as a native `create:seat`, and fresh-client
diagnostics showed Dev in carrier `48f98802-4d82-4ebc-9823-7423cc4daed7` with
`riding=true`. This establishes an occupied Free Spin cold restart. Moving-route
and concurrent-rider lifecycle coverage remain FD902 work.

## Revised server-process restart

A fresh stationary Revised recovery fixture was boarded through the same real
`SeatBlock.sitDown` validation path and stopped normally with its client and
dedicated server. The new dedicated-server process opened the unchanged world.
Before a client joined, the read-only availability report retained the assigned
rider while the chassis and all four native carriers were unloaded:

```text
Validation availability chassis=0/1 carriers=0/4 assignments=1
```

The fresh client loaded the fixture and the server recorded:

```text
FD_BOARDING_RESTORE queued player=Dev assignments=1 deadlineTick=215956
FD_BOARDING_RESTORE mounted player=Dev gameTick=215762 deadlineTick=215956
```

Availability returned to `chassis=1/1 carriers=4/4 assignments=1`. RCON
identified Dev's RootVehicle as a native `create:seat`, and fresh-client
diagnostics showed Dev in carrier `054d1523-b1f4-458e-ad02-9487dbb3869d` with
`riding=true`. This establishes an occupied Revised cold restart. Moving-route
and concurrent-rider lifecycle coverage remain FD902 work.

## Legacy normal chunk/sublevel unload

On the same stationary Legacy fixture, `fd validation availability` first
reported `chassis=1/1 carriers=2/2 assignments=1`. The only client then
disconnected and the server was left empty for the normal chunk timeout. No
server shutdown, body-removal command, or saved-data edit occurred. The
read-only availability report became:

```
Validation availability chassis=0/1 carriers=0/2 assignments=1
```

This is Sable's normal holding-state unload: both native carrier bodies and the
chassis were absent while Fourth Dimension retained the rider assignment. A
fresh client joined the still-running server. Sable reloaded the tracking point
and the boarding log recorded:

```
FD_BOARDING_RESTORE queued player=Dev assignments=1 deadlineTick=122280
FD_BOARDING_RESTORE mounted player=Dev gameTick=122082 deadlineTick=122280
```

Availability returned to `chassis=1/1 carriers=2/2 assignments=1`; RootVehicle
again identified one native `create:seat`, and `fd status` reported the original
Legacy carrier with one occupant. This proves one real no-player unload/reload
boundary for Legacy. The four other stationary family results follow; moving
routes and concurrent riders still need lifecycle coverage.

## Arrow, Free Spin, Revised and PAX normal chunk/sublevel unload

The same live dedicated-server procedure was repeated on fresh, stationary
original Arrow, Free Spin, revised and PAX examples. Before each client disconnect, the
validation command boarded Dev through `SeatBlock.sitDown`, closed restraints,
and reported one rider assignment. The target chunks were explicitly checked
and were not force-loaded; an earlier stale force-load at the Arrow test site
was removed before starting the normal unload wait.

| Family | Loaded before disconnect | Normal no-player state | Loaded after fresh client | Mounted game tick |
| --- | --- | --- | --- | ---: |
| Original Arrow | `chassis=1/1 carriers=2/2 assignments=1` | `chassis=0/1 carriers=0/2 assignments=1` | `chassis=1/1 carriers=2/2 assignments=1` | 187611 |
| Free Spin | `chassis=1/1 carriers=4/4 assignments=1` | `chassis=0/1 carriers=0/4 assignments=1` | `chassis=1/1 carriers=4/4 assignments=1` | 190812 |
| Revised | `chassis=1/1 carriers=4/4 assignments=1` | `chassis=0/1 carriers=0/4 assignments=1` | `chassis=1/1 carriers=4/4 assignments=1` | 195522 |
| PAX shuttle | `chassis=4/4 carriers=8/8 assignments=1` | `chassis=0/4 carriers=0/8 assignments=1` | `chassis=4/4 carriers=8/8 assignments=1` | 199967 |

The fresh Arrow client reported Dev in carrier
`322366ef-9d09-4dfa-a7fb-0c29aa060863`; the Free Spin client reported Dev in
carrier `3af37017-0607-4c8a-bc22-cc2be8db969b`; Revised reported Dev in
`78cbea91-8078-4f3d-b301-b88695f7d3d8`; and PAX reported Dev in
`b61d7d0b-a38e-4f79-aa7a-25ae975ba56d`. The PAX construction corridor was
force-loaded only until its native assembly completed, then explicitly
unmarked before the disconnect. In every case RCON confirmed a native
`create:seat` RootVehicle after reconnect and the server logged
`FD_BOARDING_RESTORE queued` followed by `mounted` before the respective
200-tick deadline. This completes stationary normal unload/reload evidence for
all five family architectures. All five families now also have stationary
occupied cold-restart evidence. Moving-route and concurrent-rider lifecycle
coverage remain open.

## Concurrent-rider investigation

A separate two-client packaged Arrow fixture was used to investigate the
remaining concurrent-rider case. The server accepted Dev on one matching seat
and Observer on a distinct matching seat in the same carrier, including the
expected relative offsets. It then received an immediate Observer dismount in
which that player entity was already removed. The fixture therefore never
reached two live native seat mounts or two retained assignments, and it is not
recorded as a passing concurrency result.

In a subsequent run, Observer was selected for an empty carrier body after
Dev's first mount. The same already-removed Observer dismount occurred after
admission to that independent body. The current evidence therefore rules out
both same-seat and same-carrier placement as the explanation; it does not
identify an upstream cause or establish a supported concurrent-rider path.

The validation-only trace now records accepted player-seat assignments before
the carrier runtime's next loading update, plus mount/dismount state. It makes
the next native-fixture investigation distinguish an admission failure from an
immediate native player removal, but it does not establish that the packaged
fixture supports two simultaneously mounted players. Concurrent rider lifecycle
recovery remains open in FD902.
