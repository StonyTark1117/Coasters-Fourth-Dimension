# FD401 connected-client network-powered Arrow loop

This is a bounded Engineered 1.1.1 operator run on an original, closed
eight-edge Arrow circuit. The dedicated server and connected client used the
packaged `0.9.0-dev.27` JAR built from this validation branch (SHA-256
`0e0b83b630000a1223a674873f78f1ef33a70525095b4798f8c719b2d22a4c62`),
Create 6.0.10, NeoForge 21.1.248, Sable 2.0.3, and the pinned Engineered JAR
identified in the [earlier operator check](fd401-engineered-client-operator.md).
The validation-only `-PfdFeNetwork` profile also loaded Create Crafts &
Additions 1.6.0, identified in the [wire transfer check](fd401-engineered-fe-network.md).
Engineered's `requireEnergy` setting was true.

## Original circuit and power

The stock `/fd demo arrow` circuit was unsuitable for this Engineered fixture:
it installs a native chain-lift shaft at every anchor, and Engineered's client
motor selector refuses such start anchors. The new validation-only
`fd validation engineered_loop` command builds the same closed original Arrow
geometry, station and boarding platforms without native chain drives or a
preplaced car. Its normal `/fd demo arrow` behavior is unchanged. The fixture
was built with source position `(-500,143,-650)`, yielding origin
`(-500,146,-638)`. All eight original edges were connected and the loop had no
Create motor or chain-lift shaft.

The connected client fitted a motor span to the first straight from anchor
`(-500,146,-630)` toward `(-500,146,-594)`. Saved anchor NBT reported
`T0=0.0 → T1=0.38888891000111636`, roughly fourteen blocks. The client
selected that anchor with the controller item, placed its linked controller at
`(-506,146,-630)`, and saved `Launch 20 → Stop 12 → End` through the GUI.
The [controller screenshot](fd401-engineered-loop-controller.png) shows the
planned STOP position; the saved block entity recorded the three steps and
the linked anchor after confirmation.

A validation fixture placed a Crafts & Additions Creative Generator at
`(-504,146,-634)`, pull connector at `(-503,146,-634)`, and push connector at
`(-499,146,-630)` against the Engineered anchor. The client linked the
connectors with the real copper spool. Their saved wire-node offsets were
`(+4,0,+4)` and `(-4,0,-4)`. With no direct FE fill, the initially empty
Engineered anchor reached 100,000 FE through the network. The connector nodes,
controller program and 100,000 FE remained present after the server was
stopped and cold reopened for the rider phase.

## Occupied client cycle

Two client item clicks at temporary stone targets under the rail did not
create a car. A validation-only `fd validation engineered_car` probe then
called the normal `VehicleFactory.create` on the first edge at distance 12,
placed an original Arrow chassis with two carriers, and bound the station.
It returned chassis `c6cfa2bf-bad7-4af8-a92a-35cb98f208ac`; native
availability became `chassis=3/3 carriers=6/6 assignments=0`, including two
older cars in another part of the saved world. This was **fixture-spawned car
placement**, not a successful ordinary item-placement check.

The real client clicked an empty Arrow seat and mounted normally. The server
logged `FD_BOARDING_ASSIGN ... accepted=true`, availability reached one rider
assignment, and the [mounted view](fd401-engineered-loop-boarded.png) showed
the car. Through the Rotation Tool editor, the client closed restraints and
dispatched the occupied car without the validation boarding or dispatch
helpers. The first motion query showed `capture=false`, position near
`(-499.5,147.1,-614.7)` and approximately 12.09 blocks/s along the first
straight. At that moment, the anchor buffer had dipped from 100,000 to
96,202 FE while the wire remained linked; it later refilled to 100,000 FE.
This is an observed buffer dip, not a measurement of total FE consumed.

Subsequent motion samples passed the opposite return side near
`(-486.5,147.1,-637.5)`, then progressed along the first straight back to
`(-499.5,147.1,-617.5)`. On the connected closed loop, those positions and
the outward launch direction support one complete circuit return; this run
did not export a per-edge frame trace. The controller advanced to STOP step 1,
and horizontal speed near the station fell to about 0.0146 blocks/s with the
rider still assigned. Its post-run NBT reported `Pass=1`, `PassShort=1`,
`PassTarget=20`, `PassReached≈11.25`, and `PassStarved=0`: the first pass did
not reach the programmed 20-block/s target, but no FE starvation was reported.

The client used the Rotation Tool editor to capture the car after the STOP
step. A motion query then reported `capture=true`; the editor changed from
Aligning/moving to Ready with restraints closed. The client opened restraints
([ready editor view](fd401-engineered-loop-ready.png)) and Shift-dismounted.
The server logged `FD_BOARDING_DISMOUNT_CLEAR ... restraints=false`; native
availability returned to `assignments=0` with all three chassis and six
carriers loaded. The client closed, the server ran `save-all flush`, and both
processes stopped. A cold reopen of this exact completed loop was not run.

This check establishes a connected-client, occupied, network-powered full
circuit with Engineered launch/STOP and normal editor-operated capture and
unloading. It does not establish ordinary item placement on this fixture,
redstone station capture/dispatch, a noncreative FE producer, full-speed
target attainment, an exact FE-throughput figure, or broader optional-mod
compatibility. It does not close all of FD401 or the 0.9 release gate.
