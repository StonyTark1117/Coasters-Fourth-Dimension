# FD2008 combined native consumer evidence

Native consumer and dependency-loss cases accepted, 2026-11-27. Package integration is recorded in the parent record.

The original Wing case passes in `wing-consumer.log` (1/1, saved server shutdown).
It uses the real LS local controller, kinetic tire cabinets and friction fins,
with disclosed Creative construction and a fake operator. It does not prove
player input or occupancy. Original car/authority identities are asserted.

The 48-block bed moves eight metres under production moving controls. Capture
rotates the source token into a suspended hold. LS reports neither traction nor
braking permission while retained; measured propulsion impulse is exactly zero.
At the physically locked destination a fresh receiving arm accepts a new held
owner. A separate launch request produces measured impulse 64.34025017318947
and peak speed 2.674813043658976 blocks/s. The original car departs to the fixed
section, clears moving service, keeps restraints and has no vehicle fault.
Old-token dispatch and a second receiving owner are denied.

Raw run, compiled-class manifest and source patch are retained at
`/mnt/NVME4/fourthdimension-runs/fd16-20261127/native-13*`; the exact LS input is
retained there under `inputs/launch-systems.jar`. Subsequent fixture changes
must not be represented as this run's execution. The final integration record
will reconcile unchanged production classes with the selected packages.

Earlier attempts exposed a dock contact-filter bootstrap cycle and LS native
endpoint extrapolation. The contact filter now requires the native guide plus
four geometrically aligned dock locks, without requiring its own contact
transient to have already settled. Dispatch/docking still require measured
stopped state. LS confines authenticated endpoint continuation to its existing
0.6-block pad tolerance; it does not grant another rail by proximity. Fixed
cabinets belong below/outside the whole moving sweep. A cabinet obstruction
correctly stopped an earlier fixture and was corrected in setup.

## Original Arrow

`arrow-consumer.log.gz` passes 1/1 with saved clean server shutdown. It uses LS
candidate `66ec71f1990feb232715e8c692d81a778d28f7056dc2f0cf1e460218651fb723`
(socket-envelope). Actual approach propulsion impulse is 51.778615729905944;
receiving departure impulse is 64.54580795308752; retained propulsion is 0.0;
peak departure speed is 2.6497612414003515 blocks/s. Original Arrow completes
physical transfer and whole-car exit, retains closed restraints and no vehicle
fault, and rejects stale/competing authority requests. `arrow-classes.json.gz`
records compiled identities. Source fixture is committed with this package.

The initial spawned Arrow settles after interface blocks change its native mass.
Its transient matching-support fault is repaired using the existing guarded
repair/realign API before source arm; it is disclosed setup, not force-clearing
a live moving fault. Fixed source/receiving dock cabinets sit one block lower
than ordinary Wing modules, outside Arrow's full occupied sweep. LS's bounded
below-dock exception includes FD's 25 mm positional socket allowance while
retaining an exact four-block total reach and live dock authentication.

Arrow stops in its source fin zone before moving. At the receiver it can already
be READY_EXIT; this valid state was initially missed by the fixture. The final
collector samples both launch and exit stages. Earlier failures remain under
`/mnt/NVME4/fourthdimension-runs/fd16-20261127/arrow-*`; they do not weaken guards.

## Moving-specific dependency loss

`dependency-loss.log.gz` passes 1/1. A real Wing/LS moving handoff first proves
an old API1 provider without explicit moving opt-in is refused. After receiving
arm, test-only removal from the registered provider map immediately suspends the
service owner, retains external identity, denies dispatch and creates no native
fallback. Returning the provider does not resume. A fresh normal LS arm restores
held readiness; a separate queued request is allowed to execute on its next tick.
Removing the actual receiving controller then immediately holds the train. Ten
ticks later native moving service has revoked dispatch, retains closed restraints,
and measures speed below .02 blocks/s; another dispatch is refused.

Registry withdrawal is an explicit absence injection, not a cold uninstall claim.
The real two-client cold-process recovery and power interruption are recorded in
[occupied evidence](../occupied/README.md). Existing missing-body/native moving
recovery, all eight mechanism trajectories and earlier fixed-provider cases carry
forward; no family-by-mechanism campaign was added.
