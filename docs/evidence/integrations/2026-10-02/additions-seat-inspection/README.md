# Additions rotating-carrier seat investigation

A direct replacement of Fourth Dimension carrier seats with Additions 0.2.0 seats
is not a supported appearance change. Static inspection identifies separate bar and
lock authority, an incompatible player-restoration type check, and unverified seat
position/loading assumptions. No seat replacement adapter or new runtime seat
compatibility is claimed by this investigation.

## Inspected inputs

The [Additions 0.2.0 artifact](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions/files/8772429)
is `curse.maven:create-coasters-simulated-additions-1661497:8772429`, SHA-256
`398a65f7c651a098b7608c6c8d600f074e7a359f239a8bdb7b49446f3deb9883`.
Its inherited seat implementation comes from
[Interiors 0.6.1 v3](https://www.curseforge.com/minecraft/mc-mods/interiors/files/8974633),
`curse.maven:interiors-906239:8974633`, SHA-256
`d230a2960dcb0c3d5753bc1d4adb71c8c18c9491b8e3cfd4a6b56533dae7a2c6`.

Inspection used OpenJDK 21 `javap -c -p` against those local JARs and the integration
branch at `b70583ecfabf63a79475d34f23b42663da4ecc8e`. [inspection.json](inspection.json)
pins each inspected external class and local source hash. This is bytecode/source
analysis, not a native mounting, rendering, restart or multiplayer test. No external
implementation, model or texture is copied into this repository.

## Findings

| Boundary | Observed implementation | Consequence for a rotating-carrier adapter |
| --- | --- | --- |
| Block inheritance | Additions `CoasterSeatBlock` extends Interiors `DirectionalSeatBlock`, which extends Create `SeatBlock`. It does not extend our final `CarrierSeatBlock`. | Shared Create ancestry is insufficient for our restoration and visual-state contracts. |
| Restraint authority | `CoasterSeatBlockEntity.tick` computes its wireless received signal and sets its own powered state. `isBarDown` is the inverse of powered. `tickAnimation` animates that state. | A wireless bar state is independent of `FoundationData.Vehicle.restraints`; the existing adapter does not synchronize them. |
| Seat attachments | `CoasterSeatBlock.useItemOn` delegates mounting and then writes Additions `SEAT_POS` and `LOCKED` attachments on the Create seat entity. `SeatLockTicker.onPlayerTick` refreshes that lock from the seat block entity. | A replacement needs one explicit owner for the lock and the bar animation, including power changes during motion. |
| Dismount policy | Additions `SeatDismountHandler` cancels dismount for a locked seat, except creative/spectator players. Our `Boarding.mount` independently prevents living riders from exiting while vehicle restraints are closed. | Opening one system does not necessarily open the other. Additions' creative exception does not establish a bypass of our separate event handler. Actual combined event order and release behavior still need native tests. |
| Original visual state | `VehicleVisualState.tick` updates only blocks that are instances of `CarrierSeatBlock`. | Vehicle-level restraint changes do not update an Additions seat's visual bars through the existing path. |
| Player restoration | `Boarding.restore` creates a restored seat mount only if the saved seat block is a `CarrierSeatBlock`. | A player whose saved seat block has been replaced with an Additions seat cannot be restored through that branch as written. This finding is a source-level exclusion, not an observed cold-restart test. |
| Occupancy and mass | `NativeLoading.update` queries Create `SeatEntity` passengers in each declared carrier seat cell. `Boarding` also saves seat offsets from the declared carrier map. | Seat entities and offsets must be checked against the declared cells; Create ancestry alone does not establish correct occupancy, center of mass or saved assignment. |
| Offset seats | Additions `OffsetSeatEntity` extends Create `SeatEntity`; Additions' `SeatEntityMixin` preserves seats with its `SEAT_POS` attachment and removes empty ones after its grace period. | Offset-based cart seats require their own identity, position and lifecycle investigation. This review does not establish compatibility of Additions' B&M/Toutatis cart layouts with our carriers. |
| Capacity | The generic `CoasterSeatBlockEntity.seatCount` reports one seat. Our carrier layouts store two or four declared seat positions, depending on family. | Replacing one block is not a conversion of the whole carrier layout, and cannot redefine telemetry capacity implicitly. |
| Removal | Current saved carriers store local seat coordinates and rider assignments; the factory creates our original family seat blocks. No optional seat-provider restoration path exists. | Addon removal/reinstallation and explicit conversion require a defined migration/fallback design before accepting replacement seats. |

The existing station adapter remains separate: it reports our authoritative
whole-consist restraints and carrier occupancy. Its successful station, wireless
and fin tests do not imply compatibility of the Additions seat blocks themselves.

## Required adapter work before support can be claimed

1. Define an explicit, unoccupied conversion operation and a persistent seat mapping.
   Preserve functional family, carrier identity, rotation ownership and capacity.
   Do not silently swap the block registered for our original seats.
2. Route boarding, bar state and lock changes through the shared server interlocks.
   Scope any optional hooks to registered converted carriers, preserving ordinary
   Additions behavior. Do not let wireless power independently release 4D restraints.
3. Extend loading, occupancy, saved rider restoration and dismount handling for the
   verified entity/offset contract. Validate actual mass and inertia after conversion.
4. Define addon-absent and missing-block behavior before supporting saved converted
   carriers; an ordinary visual fallback is not enough to restore a missing seat block.
5. Test native item interaction with real survival players, both restraint systems,
   wireless changes, all supported carrier orientations, carrier rotation/inversion,
   wrong or missing seats, delayed entity load, reconnect, cold restart, addon removal,
   dedicated server, two clients, and normal platform exit. Review rendered restraint
   alignment and rider clearance for each advertised family/layout.

These are outstanding acceptance requirements, not implemented features. This
investigation does not modify the current release branch or its supported addon
profiles; it is retained on the six-integration research/development branch.
