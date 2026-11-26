# 1.5 — Moving-track mechanisms

[Guide index and execution rules](README.md) · [Canonical roadmap](../../ROADMAP.md) · [Active assignments](../CURRENT-QUALIFICATION.md#current-work-queue)

All eight mechanisms belong to **1.5**, reconciled by FD2007. FD2001 proves
native graph disconnection, occupied retention, reconnection and the shared
compatibility path first. Read the [design and car/track matrix](../specs/FD2001-moving-track-1.5.md)
for real-coaster references, finite assemblies, presets, exact admitted pairs,
family retention/route adapters, dependency boundaries and player guidance.
The [2026-11-26 integration record](../evidence/1.5/integration.md) closes these
canonical requirements with bounded evidence and measured support limits.
Launch Systems moving-track compatibility is [future FD2008](external-compatibility.md#fd2008)
and is not a 1.5 prerequisite.

Card order helps navigation; actual prerequisites determine readiness. These are
procedures, not a second status tracker. Apply the index's evidence and stopping
rules to every step; already-covered steps do not require another run.

## Common delivery procedure

For each mechanism, start from its exact geometry/program and admitted car/rail
pairs in the design. Resolve the actual family handoff before implementing that
adapter; unrelated active families do not block independent work. Deliver the
capability readback, full occupied sweep preview, survival content and normal
controls with the mechanism. Confirm that the server refuses the listed
unsupported pairs, partial/oversize trains, invalid destinations and unqualified
owners. Every step below consumes this shared contract; it does not authorize an
extra mechanism or a broader vehicle combination.

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
- [FD2007 — Version 1.5 moving-track gate](#fd2007)

## FD2001

**Outcome:** A retained working moving-segment prototype and reusable graph/retention contract.

**Start conditions:** Integrated provider/eligibility, passenger and persistence contracts; [FD1701A](0.8-operations.md#fd1701a) authority; each admitted family’s accepted handoff (including its own 1.4 outcome where applicable).

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2001); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Inspect the pinned native graph/snap and Sable support hooks. Freeze the versioned capability schema, one straight segment, two docks, measured limits and graph/pose/passenger/control owners from the adopted design. Keep the 1.5 compatibility scope explicit; no blanket removal of fixed-world eligibility guards.
2. Implement physical capture, whole-train deck fit, exclusive destination reservation and transactional disconnection. Add the listed family-specific carrier/posture/route adapters and fixed-span propulsion handoffs; preserve control/magnetic/overhead/Pipeline contacts and deny unqualified owners.
3. Implement coordinate/pose synchronization and reconnect only at the reserved aligned/locked endpoint; preserve explicit authority and safe release.
4. Retain a working Wing prototype and prove the native lifecycle plus representative occupied cycle, including power loss, interruption, save/restart, endpoint loss and recovery. Use two real clients for the new synchronization path; add only distinct family adapter checks, carrying their proof to consumers.
5. Retain the working prototype and record provider changes, limits and unresolved hook blockers; a design document alone cannot close this package.
6. Integrate the single server-owned capability table and player readback used by tooltips, compatibility panel, placement preview and admission/refusal. Hand off limits, native hooks, exact supported pairs and evidence for the eight 1.5 mechanisms; FD2007 is the one release gate, and FD2008 remains future.

**Validation:** Native graph/reservation/retention and persistence checks plus connected-client occupied motion/synchronization for the changed path. Check only affected fixed-track behavior when shared graph code changes.

**Finish and handoff:** Integrate the contract and proof for [FD2002](moving-track.md#fd2002)–[FD2005](moving-track.md#fd2005). Keep the 1.0 qualification baseline separate; missing provider hooks block only their actual consumers. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2002

**Outcome:** A usable Transfer platform mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2001](moving-track.md#fd2001)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2002); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Implement bounded lateral movement between the two specified fixed endpoints with physical capture, destination reservation and controller requests.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original platform, segment and capture/lock equipment, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check lateral transfer, interrupted transfer and correct graph reconnection with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for lateral transfer, interrupted transfer and correct graph reconnection. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006C](moving-track.md#fd2006c) and the [FD2007](moving-track.md#fd2007) 1.5 gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2003

**Outcome:** A usable Turntable mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2001](moving-track.md#fd2001)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2003); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Implement full-turn yaw table geometry and a finite named exit list; reserve the chosen exit and retain the disconnected vehicle until aligned/locked.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original table, rotating segment, route-selection controls and locks, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check occupied yaw rotation, route choice and interrupted rotation/restart with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for occupied yaw rotation, route choice and interrupted rotation/restart. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006D](moving-track.md#fd2006d) and the [FD2007](moving-track.md#fd2007) 1.5 gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2004

**Outcome:** A usable Drop table / drop track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2001](moving-track.md#fd2001)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2004); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Implement upper/lower endpoints, specified drop/braking/reset motion and controlled recapture with vehicle retention.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original drop segment, supports, brakes and recapture equipment, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check occupied drop, interrupted drop, endpoint loss and recovery/reset with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for occupied drop, interrupted drop, endpoint loss and recovery/reset. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006A](moving-track.md#fd2006a) and [FD2006C](moving-track.md#fd2006c) and the [FD2007](moving-track.md#fd2007) 1.5 gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2005

**Outcome:** A usable Tilt track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2001](moving-track.md#fd2001)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2005); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Implement the specified entry/exit pitch angles, retained tilt movement, locked vehicle release and return/reset.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original tilting segment, pivot/supports and lock/release equipment, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check tilt sweep/clearance, power loss and interrupted tilt recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for tilt sweep/clearance, power loss and interrupted tilt recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006B](moving-track.md#fd2006b) and [FD2006D](moving-track.md#fd2006d) and the [FD2007](moving-track.md#fd2007) 1.5 gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006A

**Outcome:** A usable Elevator track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2004](moving-track.md#fd2004)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006a); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Extend the integrated drop contract into controlled powered upward/downward travel between fixed height stops with endpoint locks.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original lift segment, supports, powered controls and endpoint locks, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check powered lift/lower cycles, stopped retention and restart/recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for powered lift/lower cycles, stopped retention and restart/recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006](moving-track.md#fd2006) and the [FD2007](moving-track.md#fd2007) 1.5 gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006B

**Outcome:** A usable Teeter-totter track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2005](moving-track.md#fd2005)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006b); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Extend the integrated tilt contract into specified bounded repeated pitch/sway followed by a declared locked exit.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original oscillating segment, pivot/locks and cycle controls, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check occupied oscillation, retained passenger state and interrupted-oscillation recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for occupied oscillation, retained passenger state and interrupted-oscillation recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006](moving-track.md#fd2006) and the [FD2007](moving-track.md#fd2007) 1.5 gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006C

**Outcome:** A usable Side-drop track mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2002](moving-track.md#fd2002), [FD2004](moving-track.md#fd2004)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006c); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Compose the integrated lateral/drop infrastructure into one explicitly defined lateral/downward trajectory, capture and graph-endpoint contract.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original side-drop segment, supports, capture equipment and trajectory controls, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check combined trajectory/swept clearance, capture and interrupted side-drop recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for combined trajectory/swept clearance, capture and interrupted side-drop recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006](moving-track.md#fd2006) and the [FD2007](moving-track.md#fd2007) 1.5 gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2006D

**Outcome:** A usable Compound motion table mechanism with original content and accepted operating/recovery behavior.

**Start conditions:** [FD2003](moving-track.md#fd2003), [FD2005](moving-track.md#fd2005)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2006d); [1.5 design and compatibility](../specs/FD2001-moving-track-1.5.md), [TrackPlacement](../../src/main/java/dev/fourthdimension/track/TrackPlacement.java), [VehicleLifecycle](../../src/main/java/dev/fourthdimension/vehicle/VehicleLifecycle.java), [FoundationData](../../src/main/java/dev/fourthdimension/runtime/FoundationData.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java).

**Ordered steps:**

1. Consume the integrated prerequisite contract and retained prototype/evidence; fix the mechanism's finite endpoints, vehicle envelope and motion limits in its specification.
2. Compose the integrated turntable/tilt infrastructure into one specified simultaneous yaw-plus-pitch mechanism with a finite exit set.
3. Preserve reservation/disconnection, passenger/vehicle retention and pose ownership throughout motion; reconnect/release only at the valid aligned/locked endpoint.
4. Deliver original compound table, supports/pivots, locks and route controls, normal player controls, guide and a buildable example. Apply the existing [FD916](0.9-presentation.md#fd916) visual criteria to new content.
5. Check simultaneous transforms/swept clearance, route selection, retention and interrupted recovery with native state assertions and connected occupied observations; independent-axis passes cannot substitute for a combined trajectory.
6. Integrate the mechanism and reconcile its content/operating evidence; close covered cases once and record only real remaining failures.

**Validation:** Native graph/authority/motion checks and missing player/render/synchronization observations for simultaneous transforms/swept clearance, route selection, retention and interrupted recovery. Reuse the shared [FD2001](moving-track.md#fd2001) proof and unchanged content/lifecycle behavior.

**Finish and handoff:** Close this mechanism for [FD2006](moving-track.md#fd2006) and the [FD2007](moving-track.md#fd2007) 1.5 gate. No automatic depot or arbitrary player-built moving-track system is included. Record evidence and closure in the canonical package/current queue as applicable, then stop.

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

**Finish and handoff:** Feed [FD2007](moving-track.md#fd2007) while each completed style and earlier release stays closed. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2007

**Outcome:** The complete eight-mechanism 1.5 package, including declared compatibility and player guidance.

**Start conditions:** [FD2002](moving-track.md#fd2002), [FD2003](moving-track.md#fd2003), [FD2004](moving-track.md#fd2004), [FD2005](moving-track.md#fd2005), [FD2006](moving-track.md#fd2006)

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd2007); [canonical status and evidence](../CURRENT-QUALIFICATION.md), [gate blockers and follow-up work](../VALIDATION-POLICY.md#gate-blockers-and-follow-up-work).

**Ordered steps:**

1. Freeze the eight mechanisms and the adopted design’s exact car/rail/motion matrix. Consume FD2001’s native hooks, limits and shared evidence; unsupported pairs stay explicitly denied and Launch Systems stays in FD2008.
2. Reconcile child evidence against the matrix, family-specific adapters, player tooltips/panel/previews, recipes/examples, graph/authority conflicts, visuals and migration/restart. Close only uncovered claims, including optional fixed-span handoff and honest unsupported-provider feedback.
3. Reconcile maximum admitted train/workload and over-limit denial, plus two independent tables contending for one exit. Reuse accepted observations; do not multiply every family by every mechanism and addon without a distinct behavior.
4. Close FD2007 when all eight mechanisms and required compatibility/player/package criteria are covered. Keep completed children and earlier releases closed; publication is separate and no FD2008 evidence is required.

**Validation:** Release reconciliation and only outstanding/affected checks. No vehicle × mechanism × provider Cartesian campaign without a demonstrated implementation distinction.

**Finish and handoff:** Complete the 1.5 moving-track program without resetting 1.0–1.4 acceptance. Record evidence and closure in the canonical package/current queue as applicable, then stop.
