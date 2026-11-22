# FD12 C2/C6 staged Arrow boarding target

Read-only diagnosis for the coordinator's staged chassis
`2b1ec00a-cc1a-488b-99be-98ab18dc6d67` in `server-repair-copy`. No runtime or
world files were modified.

## Saved native layout

The chassis center is `(41.5,-55.875,41.213)`. Its carrier bodies are:

| side | body UUID | parent-world center | seat block in embedded plot |
|---:|---|---|---|
| -3 | `1ae0e004-281b-4223-8556-52217fb7ce52` | `(38.0,-54.532,40.901)` | `(20481032,127,20507656)` |
| +3 | `d7a2cef0-ea6f-4d8a-ae44-4c233ead6d53` | `(45.0,-54.531,40.901)` | `(20481032,127,20509704)` |

The selected left carrier has seat blocks at embedded local X `7,8`, Y `127`,
Z `20507656`; use X `20481032` (local X `8`) for the stable seat hit. This is
distinct from the chassis/strut click at Y `130`.

## Recommended eye and aim

From a clean, dismounted player state, use an eye point near
`(39.0,-51.38,42.5)` (feet approximately `(39.0,-53.0,42.5)`) and aim at the
visible left-carrier seat center near `(38.0,-54.5,40.9)`:

```text
delta = (-1.0, -3.12, -1.6)
horizontal distance = 1.887
distance = 3.65 blocks
yaw = +148.0 degrees (northwest)
pitch = +58.8 degrees (downward)
```

The aim is within normal interaction reach and descends through open air above
the flat grass. A one-block adjustment across the two left-carrier seats is
expected; keep the Y `127` seat layer so the click cannot be mistaken for a
Y `130` chassis strut.

## Expected server readback

For the selected seat, expect:

```text
FD_BOARDING_EVENT player=Dev action=mount seat=BlockPos{x=20481032,y=127,z=20507656} restraints=false restoring=false
FD_BOARDING_ASSIGN player=Dev seat=BlockPos{x=20481032,y=127,z=20507656} offset=BlockPos{x=0,y=-1,z=0} ... accepted=true
```

The assignment should identify body
`1ae0e004-281b-4223-8556-52217fb7ce52` in the subsequent state/telemetry. A
click at Y `130` or at Z `20505608` is the chassis/strut path and tests
rotation binding, not normal carrier boarding. The exact parent-world seat
center has sub-block pose drift; the embedded seat block coordinate and
`FD_BOARDING_ASSIGN` result are authoritative.
