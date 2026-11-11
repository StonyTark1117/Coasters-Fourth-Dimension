# Post-1.0 — Moving-track mechanisms

[Guide index and execution rules](README.md) · [Canonical roadmap](../../ROADMAP.md) · [Active assignments](../CURRENT-QUALIFICATION.md#current-work-queue)

FD2001 proves graph disconnection, occupied retention and reconnection once. Purpose-built mechanisms follow; all moving-track work stays outside the 1.0 gates. Assign delivery versions and finite FD2007 suffix gates after feasibility.

Card order helps navigation; actual prerequisites determine readiness. These are
procedures, not a second status tracker. Apply the index's evidence and stopping
rules to every step; already-covered steps do not require another run.

## Packages

- [FD2001 — Moving-segment contracts and prototype](#fd2001)
- [FD2002 — Transfer platform](#fd2002)
- [FD2003 — Turntable](#fd2003)
- [FD2004 — Drop table / drop track](#fd2004)
- [FD2005 — Tilt track](#fd2005)
- [FD2006A — Elevator track](#fd2006a)
- [FD2006B — Teeter-totter track](#fd2006b)
- [FD2006C — Side-drop track](#fd2006c)
- [FD2006D — Compound motion table](#fd2006d)
- [FD2006 — Additional moving-track styles](#fd2006)
- [FD2007 — Moving-track release reconciliation](#fd2007)

## FD2001

**Outcome:** A retained working moving-segment prototype and reusable graph/retention contract.

**Start conditions:** Integrated provider/eligibility, passenger and persistence contracts; [FD1701A](0.8-operations.md#fd1701a) authority interface

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2001); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Specify one short segment, two fixed endpoints, the supported vehicle envelope and graph/pose/passenger/control owners; inspect actual provider hooks.
2. Implement physical capture and retained vehicle/rider state, exclusive destination reservation and graph disconnection while moving.
3. Implement coordinate/pose synchronization and reconnect only at the reserved aligned/locked endpoint; preserve explicit authority and safe release.
4. Prove the native lifecycle and representative occupied cycle, including power loss, interrupted motion, save/restart and endpoint removal.
5. Retain the working prototype and record provider changes, limits and unresolved hook blockers; a design document alone cannot close this package.
6. Allocate delivery versions and permanent [FD2007](moving-track.md#fd2007) suffix gates with finite mechanism/profile lists; keep later unshipped styles visible without reopening accepted increments.

**Validation:** Native graph/reservation/retention and persistence checks plus connected-client occupied motion/synchronization for the changed path. Check only affected fixed-track behavior when shared graph code changes.

**Finish and handoff:** Integrate the contract and proof for [FD2002](moving-track.md#fd2002)–[FD2005](moving-track.md#fd2005). Keep the 1.0 qualification baseline separate; missing provider hooks block only their actual consumers. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2002

**Outcome:** A usable Transfer platform mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2001](moving-track.md#fd2001)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2002); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Implement bounded lateral movement between the two specified fixed endpoints with physical capture, destination reservation and controller requests.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original platform, segment and capture/lock equipment, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check lateral transfer, interrupted transfer and correct graph reconnection with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for lateral transfer, interrupted transfer and correct graph reconnection. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006C](moving-track.md#fd2006c) and its allocated [FD2007](moving-track.md#fd2007) increment gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2003

**Outcome:** A usable Turntable mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2001](moving-track.md#fd2001)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2003); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Implement full-turn yaw table geometry and a finite named exit list; reserve the chosen exit and retain the disconnected vehicle until aligned/locked.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original table, rotating segment, route-selection controls and locks, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check occupied yaw rotation, route choice and interrupted rotation/restart with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for occupied yaw rotation, route choice and interrupted rotation/restart. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006D](moving-track.md#fd2006d) and its allocated [FD2007](moving-track.md#fd2007) increment gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2004

**Outcome:** A usable Drop table / drop track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2001](moving-track.md#fd2001)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2004); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Implement upper/lower endpoints, specified drop/braking/reset motion and controlled recapture with vehicle retention.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original drop segment, supports, brakes and recapture equipment, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check occupied drop, interrupted drop, endpoint loss and recovery/reset with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for occupied drop, interrupted drop, endpoint loss and recovery/reset. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006A](moving-track.md#fd2006a) and [FD2006C](moving-track.md#fd2006c) and its allocated [FD2007](moving-track.md#fd2007) increment gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2005

**Outcome:** A usable Tilt track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2001](moving-track.md#fd2001)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2005); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Implement the specified entry/exit pitch angles, retained tilt movement, locked vehicle release and return/reset.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original tilting segment, pivot/supports and lock/release equipment, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check tilt sweep/clearance, power loss and interrupted tilt recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for tilt sweep/clearance, power loss and interrupted tilt recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006B](moving-track.md#fd2006b) and [FD2006D](moving-track.md#fd2006d) and its allocated [FD2007](moving-track.md#fd2007) increment gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006A

**Outcome:** A usable Elevator track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2004](moving-track.md#fd2004)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006a); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Extend the integrated drop contract into controlled powered upward/downward travel between fixed height stops with endpoint locks.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original lift segment, supports, powered controls and endpoint locks, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check powered lift/lower cycles, stopped retention and restart/recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for powered lift/lower cycles, stopped retention and restart/recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006](moving-track.md#fd2006) and its allocated [FD2007](moving-track.md#fd2007) increment gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006B

**Outcome:** A usable Teeter-totter track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2005](moving-track.md#fd2005)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006b); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Extend the integrated tilt contract into specified bounded repeated pitch/sway followed by a declared locked exit.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original oscillating segment, pivot/locks and cycle controls, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check occupied oscillation, retained passenger state and interrupted-oscillation recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for occupied oscillation, retained passenger state and interrupted-oscillation recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006](moving-track.md#fd2006) and its allocated [FD2007](moving-track.md#fd2007) increment gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006C

**Outcome:** A usable Side-drop track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2002](moving-track.md#fd2002), [FD2004](moving-track.md#fd2004)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006c); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Compose the integrated lateral/drop infrastructure into one explicitly defined lateral/downward trajectory, capture and graph-endpoint contract.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original side-drop segment, supports, capture equipment and trajectory controls, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check combined trajectory/swept clearance, capture and interrupted side-drop recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for combined trajectory/swept clearance, capture and interrupted side-drop recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006](moving-track.md#fd2006) and its allocated [FD2007](moving-track.md#fd2007) increment gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006D

**Outcome:** A usable Compound motion table mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2003](moving-track.md#fd2003), [FD2005](moving-track.md#fd2005)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006d); [moving-track design](../../PLAN.md#d20-post-10-moving-track-mechanisms), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Compose the integrated turntable/tilt infrastructure into one specified simultaneous yaw-plus-pitch mechanism with a finite exit set.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original compound table, supports/pivots, locks and route controls, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check simultaneous transforms/swept clearance, route selection, retention and interrupted recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for simultaneous transforms/swept clearance, route selection, retention and interrupted recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006](moving-track.md#fd2006) and its allocated [FD2007](moving-track.md#fd2007) increment gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006

**Outcome:** Additional moving-track styles reconciled from its named deliverables.

**Start conditions:** [FD2006A](moving-track.md#fd2006a)–D

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006); [canonical status and evidence](../CURRENT-QUALIFICATION.md), [gate blockers and follow-up work](../VALIDATION-POLICY.md#gate-blockers-and-follow-up-work).

**Ordered steps:**

1. Reconcile [FD2006A](moving-track.md#fd2006a) elevator, B teeter-totter, C side-drop and D compound-table outcomes against this package's existing completion criteria; cite direct acceptance or scoped carry-forward evidence.
2. Name only unmet required criteria and route each to its owning package; retain all completed cases.
3. Reconcile original content, controls/examples and distinct operating/recovery evidence; adding another axis or style needs its own scope revision.
4. Update the canonical package and current record when the criteria are covered; close the aggregate without replaying accepted child workflows.

**Validation:** Evidence review only unless a named required claim is still missing or affected. An aggregate label does not require a fresh run.

**Finish and handoff:** Feed [FD2007](moving-track.md#fd2007) while each completed style and earlier shipping increment stays closed. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2007

**Outcome:** Reconciled finite moving-track releases and eventual completion of the adopted program.

**Start conditions:** [FD2002](moving-track.md#fd2002), [FD2003](moving-track.md#fd2003), [FD2004](moving-track.md#fd2004), [FD2005](moving-track.md#fd2005), [FD2006](moving-track.md#fd2006); their allocated increment gates

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2007); [canonical status and evidence](../CURRENT-QUALIFICATION.md), [gate blockers and follow-up work](../VALIDATION-POLICY.md#gate-blockers-and-follow-up-work).

**Ordered steps:**

1. Use [FD2001](moving-track.md#fd2001)'s allocated permanent suffix gates and supported mechanism/vehicle/provider lists; reconcile each increment independently.
2. For each increment, consume its mechanism evidence and close only outstanding graph/authority conflicts, visual, persistence, measured-workload and package/documentation criteria.
3. Record an increment's completion once its finite criteria pass; publish only under separate authorization and keep later mechanisms planned.
4. Close the [FD2007](moving-track.md#fd2007) parent when [FD2002](moving-track.md#fd2002)–[FD2006](moving-track.md#fd2006) and all allocated increment gates are covered, reusing earlier accepted releases.

**Validation:** Release reconciliation and only outstanding/affected checks. No vehicle × mechanism × provider Cartesian campaign without a demonstrated implementation distinction.

**Finish and handoff:** Complete the adopted moving-track program without resetting 1.0 or accepted intermediate releases. Record evidence and closure in the canonical package/current queue as applicable, then stop.
