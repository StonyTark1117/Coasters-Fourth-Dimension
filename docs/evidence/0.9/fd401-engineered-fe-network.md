# FD401 real FE network transfer to an Engineered motor

This bounded check extends the [connected-client Engineered operator check](fd401-engineered-client-operator.md)
with a real FE producer and wire. The extra mod is a validation profile only;
it is not part of the advertised compatibility matrix.

| Artifact | Version and identity |
| --- | --- |
| Fourth Dimension | Release head `fcb142e`, packaged `0.9.0-dev.27` JAR SHA-256 `81626641c2689a8ddf797dd6010767304ba2ad0bb455bc546b510a586e36888b` |
| Coasters: Engineered | 1.1.1, SHA-256 `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d` |
| [Create Crafts & Additions](https://www.curseforge.com/minecraft/mc-mods/createaddition/files/8188476) | NeoForge 1.21.1 release 1.6.0, CurseForge file 8188476, SHA-256 `41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9` |

The Crafts & Additions JAR declares Create `[6.0.7,6.1.0)` and NeoForge
`[21.1.219,)`; this run used Create 6.0.10 and NeoForge 21.1.248. The
validation-only Gradle flag `-PfdFeNetwork` adds that JAR only alongside
`-PfdIntegrations=engineered`. It does not bundle the dependency in the
Fourth Dimension artifact. The packaged build passed with those flags and
`-PfdValidation`.

## Physical setup and transfer

The dedicated server reopened the saved 48-block Arrow fixture with
`requireEnergy = true`. A full copy of its world directory was backed up
before this check. Fixture commands placed a Crafts & Additions Creative Generator at
`(-654,143,-586)`, a Small Connector at `(-653,143,-586)` facing west in
`pull` mode, and a Small Connector at `(-649,143,-590)` facing west in
`push` mode against the Engineered far motor anchor `(-650,143,-590)`.
The generator was not adjacent to the motor anchor. A first connector on the
anchor's west side was left unlinked after the track model obstructed the
client's click; only the east-side connector participated in the network.
Temporary stone floor blocks let the connected client reach both endpoints.

The anchor initially held 37,001 FE from the prior bounded ride. A fixture
command cleared its `CoastersEngineeredEnergy` value to zero. The RCON query
found no nonzero energy key after several seconds with the generator and
connectors present but no wire. The connected client then used the actual
`createaddition:copper_spool` item to select the generator connector and
right-click the anchor connector. The held spool first recorded
`{node:0,x:-653,y:143,z:-586}`; after the second click, both block entities
recorded reciprocal wire nodes:

```text
generator connector: [{other:0,x:4,y:0,z:-4,id:0,type:0}]
anchor connector:    [{other:0,x:-4,y:0,z:4,id:0,type:0}]
Engineered anchor:   CoastersEngineeredEnergy = 100000
```

The client view shows the physical wire between the Crafts & Additions
generator and the Engineered anchor:
[fd401-engineered-fe-wire.png](fd401-engineered-fe-wire.png).
No direct FE fill was applied after the wire was linked.

## Cold restart

The client closed; the server ran `save-all flush` and stopped normally. A
fresh packaged server reopened the same world with the same pinned addons and
validation flag. Native availability remained `chassis=2/2 carriers=4/4
assignments=0`. Both connectors retained their reciprocal node offsets, and
the anchor retained 100,000 FE. After a second fixture command cleared the
anchor to zero, the existing network refilled it to 100,000 FE within 0.5
seconds. The server saved and stopped normally.

This establishes actual generator → wire → Engineered-anchor FE delivery and
delivery after a server restart in this pinned validation pack. The generator
was creative; no Create kinetic alternator, noncreative fuel path, or throughput
limit was measured. The previously recorded occupied ride consumed FE supplied
by a direct fixture fill, and was not repeated with this wire connected. A
complete operator/FE-network ride and broader optional-mod compatibility
remain open. No third-party functional track was used or accepted.
