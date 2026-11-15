# FD501F / FD912 — Immersion

Owner: Seats / Immersion, chat `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`.
Inspection on 2026-11-15 from main `f06d81607f246773e5b9624cd259c5a49e709fcc`.

## FD501F dependency disposition

The selected Immersion 1.1.1 file 9542588 was rehashed and its embedded metadata
and executable API inspected. [Artifact fingerprints](artifact-inspection.json)
and [embedded metadata](embedded-neoforge.mods.toml) retain the exact inputs.
The artifact SHA-256 is
`df3332b03763bee3f32206b6716ff3d6963869d944171bc743ad3c40c3353c8b`.

Actual mod ID is `coasters_immersion`. Required ranges are NeoForge `[21.1,)`,
Minecraft `[1.21.1,1.22)`, Create `[6.0,)`, Simulated Coasters `[0.1,)` and Sable
`[2.0,)`. The latter three dependencies are client-side. Aeronautics `[1.3,)`
and Sodium `[0.8,)` are optional client dependencies. The metadata uses
`IGNORE_SERVER_VERSION`. The supported deployment is client-only Immersion,
with the dedicated server omitting the addon. No Immersion-driven core upgrade
or world migration is required; FD501E owns the shared Skin-driven baseline
upgrade. FD912 still owns executable effects and connection qualification.

## Inspected composition seams

* Ride eligibility uses `CoasterCart.cartOf`, reflecting the native
  `hostCartFromVehicleChain(Entity)` helper. FD carrier eligibility must resolve
  the actual passenger child, not substitute a chassis pose for its rotation.
* Immersion injects at the return of Sable `getEntityOrientation`, the same seam
  as FD's neutral Flying head frame and comfort orientation. Both currently use
  default mixin priority; deterministic composition is required.
* `CameraFilter.apply` mutates its input quaternion, retains a raw orientation
  for telemetry and caches the filtered result per render frame. It must run
  once on the resolved head frame without modifying the physical carrier pose.
* Camera travel adds a translation at `Camera.setup` tail; cinematic angles,
  FOV, blur, streaks and sound are separate addon paths. Comfort must take
  precedence over camera motion while leaving eligible sound/visual effects.
* `RiderForces` consumes the detected sublevel for local and remote bodies.
  `PassengerFacing` adjusts a render pose stack, not the authoritative mount.
* Sable snapshot spline/render-pose injections affect client interpolation.
  Their compatibility belongs to the qualified shared baseline and FD912's
  affected occupied views, not a new replay of closed family acceptance.

## FD912 status

Implementation and live qualification in progress. This inspection alone does
not close camera, optional-seat, rider synchronization or effects acceptance.
No runtime has been launched by this package at the inspection checkpoint.
