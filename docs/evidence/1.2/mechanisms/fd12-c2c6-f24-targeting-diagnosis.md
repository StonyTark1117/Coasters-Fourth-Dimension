# FD12 C2/C6 f24 targeting diagnosis

This is a read-only targeting calculation. It does not launch or modify the
coordinator's client, server, world, or production code.

The persisted f24 chassis is `f24db323-ddfa-4a9f-a58d-267b7ad92b8a`. Its
serialized plot is storage index 7, with the controller at local `(0,1,0)` and
wing struts at local `x=-2..2,y=2,z=0` in the vehicle-relative palette. The
saved logical/cart center is approximately `(17.5002,-55.87,56.5)` in the
parent-world view. The plot's embedded block positions use the Sable plot
origin, so the corresponding exposed west/east strut click is expected around
`BlockPos{x=20481030,y=130,z=20493320}` (the same local strut used by the
successful `ea2945a0` bind, with f24's plot-Z origin).

## One reachable aim setup

Use an eye point one block west of the prior player position:

```text
eye = (16.0, -52.0, 56.5)
target face center = (17.0, -55.0, 56.5)
distance = sqrt(10) = 3.162 blocks
yaw = -90.0 degrees  (east, +X)
pitch = +71.565 degrees (downward)
```

This targets the exposed west side of the f24 body while staying within the
8-block squared reach guard. The ray descends through air above the flat grass
surface; no intervening vanilla block is expected. If using the previous F3
feet coordinate `(17.5,-53.625,56.5)`, add the normal 1.62 eye height before
applying the pitch calculation. F3's vanilla `Targeted Block` may continue to
show grass because Sable plot blocks are resolved through the embedded plot;
the authoritative readback is the server's `FD_ROTATION_USE_ON`/resolve log.

## Expected readback

For a successful use of the rotation tool, the server should report a clicked
embedded block near `(20481030,130,20493320)`, then:

```text
FD_ROTATION_RESOLVE ... body=f24db323-ddfa-4a9f-a58d-267b7ad92b8a
FD_ROTATION_BIND ... vehicle=arrow_four_rail_v1 chassis=f24db323-ddfa-4a9f-a58d-267b7ad92b8a
FD_ROTATION_DISPATCH ... target=f24db323-ddfa-4a9f-a58d-267b7ad92b8a vehicle=true
FD_EDITOR_OPEN ... target=f24db323-ddfa-4a9f-a58d-267b7ad92b8a vehicle=true
```

The exact face coordinate can vary by one block across the exposed strut or
controller. A bind resolving to `null`, a different chassis, or only a vanilla
grass target indicates the click missed the embedded Sable block, rather than
proving that the f24 native body is absent.
