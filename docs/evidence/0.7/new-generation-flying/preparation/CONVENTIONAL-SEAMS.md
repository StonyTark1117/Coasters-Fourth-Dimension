# Conventional handoff source review

Read-only review at main `2b805fcbc954975732e1f930b6f65cfc044c15ac` on
2026-11-15. The Conventional owner reports FD1801A complete in its handoff;
the final NativeLoading correction and evidence remain pending lead integration
at this checkpoint. This review does not accept that prerequisite on the lead's
behalf or transfer production ownership.

## Reusable measured mechanics

`FlyingRuntime.createJoint(parent, child, carrier)` takes an explicit parent and
locks all translation plus angular Y/Z, with native X limits[-π/2,0].
`jointInertia` uses actual child mass, COM and hinge offsets. `gravityTorque`
projects the native gravity moment onto the supplied parent's world X axis.
These are the useful compound posture seams: its parent must be the yaw deck.

The accepted Conventional actuator uses acceleration-based native gains4000/400,
bounded reference rate.18 and acceleration.15, plus gravity compensation.
The record retains measured endpoint capture separately from motor preload,
and persists reference angle/velocity so interruption does not discard tracking
lead. New-generation must measure these gains with its two-seat compound body;
the Conventional result does not establish compound yaw/posture tracking.

`FlyingRuntime.update` also owns station-only authority, Conventional frame/seat
checks and Conventional phase semantics. It cannot be reused unchanged for
in-route posture control merely by passing a different parent. Reuse native
joint/load calculations, and implement the assigned route authority separately.

## Camera and hierarchy seam

`FlyingRiderPose.phi` currently derives relative X from chassis orientation and
posture-body orientation. That is valid for the single Conventional hinge.
For compound Flying, compute relative posture from yaw-deck orientation to
inner-body orientation; chassis-to-inner includes yaw and cannot be interpreted
as pure X. Keep native +phi head counterpitch, physical eye origin and independent
look order. Existing `FlyingVehicle.is` identifies only Conventional's
`station_changeover_v1`, so shared dispatch must explicitly select the new
`route_changeover_yaw_v1` without broadening Conventional guards.

## Restart coordinate correction

The pending NativeLoading diff refreshes mass on the first sample, and only when
that sample has no load delta and saved rotationPoint differs from rebuilt COM,
rebases pose coordinates once while preserving world block positions. The
Conventional handoff reports offline hinge gaps about.00010–.00012 and occupied
rejoin gap.000174. Consume the lead's final integrated correction; do not copy it
into another family or reopen accepted Conventional replay.

No production files, runtime or tests were changed by this review. Exact
production ownership and integrated prerequisite commit remain outstanding.
