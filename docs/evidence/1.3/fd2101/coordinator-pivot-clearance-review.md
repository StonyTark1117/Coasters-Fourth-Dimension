# FD2101 pivot and support review — 2026-11-17

Read-only reviewer `/root/model_reference_review` inspected the user's close
views, factory placements, generated seats, Boarding and the support renderer.
This records a bounded defect diagnosis, not runtime acceptance.

The Revised inner seat is at child block `(0,-1,0)` and the physical hinge
reference is child-centered `(0,-.5,0)`. Rendering a bearing ring at that axis
origin puts it in the bucket's central space. The physical axis and rider need
not move: a bearing can move axially along the same X line.

For `s=sign(carrier.side)`, the proposed visual housing/journal axial offset is
`-.80*s`; the rotating radial crank plane is `-.60*s`. Preserve actual hinge
Y/Z and parent/child pose ownership. A .10-square crank at that plane stays
.05 outside the inner seat's lateral envelope `[-.5,.5]`, including NORTH and
SOUTH model facing. Use a short coaxial journal extension, a radial crank to
child Y=0, a longitudinal arm to the baked axle at Z=-.875 for Revised, and a
short axle stub to X=`-.4375*s`. Delete any old shaft back to axis-origin X=0.
These are design bounds; exact occupied anatomy and final moving fit still
need affected visual evidence.

The stationary support must also stay clear of the crank. With the originally
reviewed .28-wide housing centered at parent X=2.20, the positive-side web can
end at X<=2.05 and flanges at X<=2.30, mirrored on the negative side. Endpoint
block +2 then has model web maximum X=8.8 and flange maximum X=12.8; endpoint
block -2 has web minimum X=7.2 and flange minimum X=3.2. These are endpoint-only
design bounds, not a change to every strut, and must be reconciled with any
subsequently changed housing dimensions and existing saved cars.

The added side rails at X=+/-1.5 and four diagonals were not necessary to close
the identified six spine gaps. Removing that clutter must preserve a continuous
central support and the connection to Revised's lower rows.

## Coordinator follow-up on the next source revision

The owner adopted .80 bearing and .60 crank offsets and reduced the rings.
Two unresolved source issues were sent back immediately:

- `strutMount=side-sign*.35` gives positive parent X=2.65; its stationary beam
  to X=2.20 crosses the crank plane X=2.40. Axial relocation alone is therefore
  insufficient; both procedural and baked stationary endpoints need inspection.
- A central web at Y=2 with full height .19 has bottom Y=1.905. Revised's lower
  row ends at Y=1.75. Without endpoint risers or another actual connecting
  member, that leaves .155 vertical separation. Remove decorative clutter,
  not the required connection.

The FD2101 owner remains the sole production writer and must reconcile these
findings with the final implementation and affected occupied views. Earlier
Fixed Inverted acceptance and unrelated mechanics remain closed.
