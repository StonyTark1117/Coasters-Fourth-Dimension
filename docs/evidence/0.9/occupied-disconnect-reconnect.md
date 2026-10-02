# FD902 occupied Arrow disconnect and reconnect

This dedicated-server checkpoint exercises one occupied original Arrow carrier
through an actual client disconnect and a fresh client-process reconnect. It
uses Fourth Dimension `0.9.0-dev.27`, Create `6.0.10`, Coasters Simulated
`0.1.5`, Track Styles `1.2.0`, and Coasters: Engineered `1.1.1`.

## Method

The packaged recovery world ran on `127.0.0.1:25581` with the two audited
optional addons. A local-only server command, registered only when the
`fourthdimension.validation` JVM property is true, was used to call the real
`SeatBlock.sitDown` path on the first ready unoccupied carrier seat:

```
fd validation board Dev
```

The command reported `Validation boarded Dev`. The server's normal `fd status`
then reported one occupant on original Arrow carrier
`7b127e8a-ef8f-4435-a964-1d051ae712b2`; `data get entity Dev RootVehicle`
identified a native `create:seat` mount. No saved rider assignment, native
body, or seat entity was edited.

The client process was terminated, producing the server record `Dev lost
connection: Disconnected`. A new client JVM joined the unchanged live server.
The server logged:

```
FD_BOARDING_RESTORE queued player=Dev assignments=1 deadlineTick=107689
FD_BOARDING_RESTORE mounted player=Dev gameTick=107490 deadlineTick=107689
```

After reconnect, `data get entity Dev RootVehicle` again identified one native
`create:seat`; `fd status` still reported one occupant on the same Arrow carrier.
Client diagnostics reported that carrier with Dev's UUID in `occupants` and
`riding=true`.

## Result and limits

The live server retained the assigned rider through disconnect and restored the
fresh client process to the original occupied Arrow carrier before the 200-tick
deadline. The test covers a deliberate client-process disconnect, not a server
restart, a sublevel/chunk unload, a route in motion, an all-family matrix, or a
second concurrent rider. Those remain FD902 acceptance work.
