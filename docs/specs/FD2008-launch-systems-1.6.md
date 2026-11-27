# FD2008 — Launch Systems interoperability for 1.6

Assigned and accepted 2026-11-27. [Implementation evidence](../evidence/1.6/README.md).

## Finite supported combinations

The moving adapter covers original Arrow (`arrow_four_rail_v1`, matching original
four-rail insert) and Wing (`wing_fixed_v1`, ordinary native Coasters insert).
It reuses their accepted 1.5 bed, train, clearance and eight-mechanism permissions;
no enlarged train, pitch, passenger or bed limits. Departure must be on a physically
locked fixed bridge and satisfy the actual Launch Systems stopping envelope.
The shared ownership path is independent of bed motion; representative translation
and occupied transition consume the already accepted moving-axis evidence.

Launch Systems local fixed controller with Tire/LSM/LIM drive and fin brakes may
own approach and departure. No equipment, controller or cable guide is mounted
on the moving bed. A configured Tire exit to another fixed LS section uses the
existing LS whole-train transfer after docking; transfer cannot overlap bed motion. Cable launch, swing programs, already-active LS section transfers,
managed 1.2 circuits, specialty vehicles, other families/providers and arbitrary
moving-host force applications remain denied by this adapter. Their previously
accepted fixed-track behavior is unchanged.

## Ownership and operation

Stop and capture the whole train under the original LS controller, close restraints,
then Capture through the moving controller. Admission requires the exact live LS
owner, a supported homogeneous consist and no competing program. Successful
physical capture rotates the old token into a suspended hold. Native moving
retention owns every body and the receiving reservation. All external traction
and brake forces are denied during physical retention; native locks own holding.

Move/Resume/Release use existing moving controls. Release preserves the old LS
identity and hold. At the locked destination explicitly arm the receiving fixed
LS controller. Its real section, modules and complete reservation must validate
before a fresh ownership token is assigned. Only one receiving controller can
accept that service handoff. A separate fresh launch input is required. Native
posture locks and receiving reservations remain until the complete train clears.

Loss of power, controller, mod, bodies or support cannot create a native fallback.
Disk restart retains owner identities but revokes motion permission; inspect and
resume the moving assembly first, then re-arm the same receiving controller and
issue a fresh launch request. Saved identities alone are never live support.

## Compatibility and remaining evidence

LS published 1.0.0 source `041e21d` / SHA-256
`02573aeeab64bfa38da2ca41b2927f46649e2a4044264350f15f13f9c8e69176`
requires FD schema 8. FD main uses schema 9. The companion change must explicitly
recognize schema 9 plus the versioned moving API; do not claim the unchanged
published LS JAR works. Existing schema-8 fixed compatibility stays available.

Required evidence: combined native real force before/after handoff, zero LS force
while retained, stale/competing requests and unsupported combinations denied,
power/controller/provider loss and persisted restart held, connected occupied
handoff/recovery, exact packaged source/JAR identity and clean owned shutdown.
No broad family/provider campaign or publication is authorized by this assignment.
