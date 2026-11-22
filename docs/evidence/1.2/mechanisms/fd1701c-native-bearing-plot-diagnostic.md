# FD1701C native bearing plot diagnostic

This is a read-only inspection of the saved Art C2/C6 run. It does not modify
the world, production code, or the run artifacts.

Run root: `/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/server/world`

## Source artifacts

| Artifact | SHA-256 |
|---|---|
| `sublevels/r.0.0.0.slvls` | `facb244f6ae4492c58469f2a1ce55f023b1844d949930decc83cf02d97b8bc68` |
| `sublevels/r.0.0.slvlr` | `4688b7299670e78d3336a0fa3437418ce2d40a92d85f5c4edb9e0279b0f47a22` |
| `data/fourthdimension_foundation.dat` | `e02ca30fa2bdc24035bf8b30110e938003196cf2e81e24b57030236adffd8451` |

The holding-region pointers map to storage indices `0` through `11`. The
target chassis UUID `ea2945a0-d2c8-4a23-b152-f113b742e8c9` is storage index
`2`; its linked carrier UUIDs are storage indices `1` and `3`.

## Serialized plot comparison

Minecraft block coordinates use `x + 16*z + 256*y`; the decoded local XYZ
coordinates below are therefore `(x,y,z)`.

Target chassis, index `2`, plot `(0,0)`:

```text
palette: minecraft:air, fourthdimension:wing_strut
non-air: fourthdimension:wing_strut at (6,2,8), (7,2,8), (8,2,8), (9,2,8), (10,2,8)
```

There is no `simulatedcoasters:coaster_cart` or
`fourthdimension:arrow_controlled_controller` entry in this serialized plot.

Good chassis plots have the same five wing struts and also contain:

```text
simulatedcoasters:coaster_cart[snap_engaged=true] at (8,0,8)
fourthdimension:arrow_controlled_controller at (8,1,8)
fourthdimension:wing_strut at (6,2,8)..(10,2,8)
```

This explains the target's lower native mass and missing bearing center before
restore. The carrier plots are present and linked, each containing two seats
and two carrier axles. The repair target is chassis plot construction or
serialization/fixture setup: preserve or recreate the cart and controller
center blocks before native bearing lookup. A cache-only representative lookup
repair cannot recover blocks absent from the saved plot.
