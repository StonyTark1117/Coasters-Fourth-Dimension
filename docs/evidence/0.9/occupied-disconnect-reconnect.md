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
