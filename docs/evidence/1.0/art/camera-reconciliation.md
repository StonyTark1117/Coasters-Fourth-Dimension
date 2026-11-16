# FD904C2 / FD904C — expanded camera reconciliation

**Complete, 2026-11-16 UTC.** This is an evidence reconciliation for the
[FD1000 frozen scope](../lead/freeze.md), not a new all-family camera campaign.
FD904C1 and FD912 remain closed at their actual tested revisions. No uncovered
family-specific camera composition or affected restoration case was identified.
Lead owns the canonical roadmap and queue update.

## Shared composition and reuse boundary

[C1](../../0.5/comfort/README.md) proves the saved selectable comfort option,
world-upright captured heading, independent mouse look, first/third-person
arms/body ownership, reconnect and dismount reset on the original native carrier
path and Wing fixed seats. Its sixteen orientation combinations include upright,
vertical and inverted frames. The original five families share that carrier
binding; they do not need five copies of the same comfort test.

[FD912](../../0.9/immersion/README.md) supplies the later pinned dependency and
optional-effects composition: native Flying neutral head frame, Immersion effect
filter, then local comfort. Actual Arrow inversion, remote observer, first/third
person, mouse look, occupied reconnect and process restart retain physical body
motion while changing only the local camera. Conventional Flying supplies the
distinct prone neutral gaze and normal unload. The recorded Controls-menu binding
change separates Fourth Dimension comfort from Immersion's default K binding.

Source review on main `c13e51df` found **no changes since FD912 `fd8f334a`** to
`RiderComfort`, `RiderComfortMixin`, `ImmersionEffects`, the five Immersion mixins,
or `FlyingRiderPose`. The current camera hook accepts only CAMERA for the managed
local rider; native eye origin, body and remote transforms remain outside its
comfort override. Flying counterpitch is restricted to Conventional/NG Flying,
and NG uses its yaw deck as parent. Other families use their accepted native frame.

## Frozen family coverage

| Frozen styles | Distinct accepted pose and restoration evidence |
| --- | --- |
| Arrow, Legacy ZacSpin, Free Spin, Revised ZacSpin, PAX | C1's shared independent-carrier frame; FD912's actual Arrow inversion and effects/reconnect proof. [FD917 original-family fit](../../fd917/art/family-fit.md), [Arrow occupied cycle](../../fd917/art/arrow-occupied.md), and [PAX remote observer](../../fd917/lead/observer/README.md) retain actual rider/body ownership after production changes. |
| Wing | C1's occupied banked fixed-seat comfort and mouse look; [Wing](../../0.5/wing/README.md) supplies native fixed-seat and remote-rider transport. |
| S1 Spinning | [Occupied S1](../../0.6/spinning/README.md): fractional seats and native Y rotation, actual yaw −2.824 rad, matching client native/snapshot angles, normal captured unload with no native seat orientation. Shared transport remains unchanged. |
| S2 Spinning | [S2](../../0.8/spinning-s2/README.md): S1's unchanged native yaw/camera conventions plus occupied launched/inverting route, stopped capture and normal unload. [FD917 mechanism views](../../fd917/mechanisms/README.md) add the production occupied roll/seat fit. |
| Suspended | [Suspended acceptance](../../0.6/suspended/BC-acceptance.md) retains occupied pendular train and camera/input evidence across its targeted dock repair. [FD917 Art](../../fd917/art/README.md) supplies actual production journal/bar and dangling rider clearance. |
| Fixed Inverted | [Fixed Inverted acceptance](../../0.6/fixed-inverted/BC-acceptance.md): inverted fixed-seat train, occupied return and normal upright camera restoration; no independent camera override. |
| Axis | [Axis occupied record](../../0.7/axis/README.md) pairs the longitudinal pod's banked native seat quaternion and camera, with [completed return/unload](../../0.7/axis/BC-acceptance.md). [FD917 family views](../../fd917/lead/families/README.md) add actual occupied full roll. |
| Roller Ball | [Roller Ball](../../0.7/roller-ball/README.md) records occupied lift, summit, free descent, captured return and normal unload with nativeSeatOrientation=null; [final content/lifecycle acceptance](../../0.7/roller-ball/BC-acceptance.md) and FD917 actual swing retain the same rider frame. |
| Conventional Flying | [Flying](../../0.7/conventional-flying/README.md): seated/prone transition, bank and full inversion, physical torso rotated once, neutral head and independent look, upright unload. FD912 separately certifies this composition with Immersion and comfort. |
| New-generation Flying | [NG Flying](../../0.7/new-generation-flying/README.md): seated and prone scenery yaw, simultaneous posture/yaw through inversion, real Observer agreement at yaw530.1° and posture89.1°, local comfort toggle/mouse look, retained occupied restart and final normal return. The same Flying neutral-head guard handles both posture variants. |
| Pipeline | [Pipeline](../../0.8/pipeline/README.md): actual rolled first person, third-person rider, forward/reverse full native chassis roll, occupied restart and normal unload with camera/assignment cleared. Fixed-seat networking is reused. |
| Dive | [Dive](../../0.8/dive/README.md): fixed incline/hold/drop, actual restored DROP, returned dock and unloaded walking camera. FD1701A fixed-seat ownership and shared remote path are unchanged. |
| Powered Inverted Yaw | [Powered Inverted](../../0.8/powered-inverted-yaw/README.md): distinct upright seat below overhead support, independent native yaw/show, normal captured unload. It explicitly reuses accepted FD1802A yaw/comfort and fixed-inverted support/transport. FD917 supplies actual occupied yaw/show fit. |

The table accounts for all 17 frozen styles. Original operating variants retain
their family record; it does not create a family × profile × camera cross product.
Base and optional-effects composition are represented by C1 and FD912 respectively.
The frozen combined-expanded profile keeps Immersion client/observer-only, absent
from the dedicated server. Provider cosmetics do not acquire camera authority.

## Production changes and conclusion

[FD917](../../fd917/README.md) preserves native seat anchors, pivot/mass, saved
programs and optional-seat authority. Its actual-pose models, client restraint
interpolation, Arrow spring/clutch and bounded PAX snapshots have their own
affected occupied and remote observations. None changes the reviewed camera
composition. Those accepted checks supply the final production rider/body views;
they are not relabeled as FD912's original artifact.

FD904C2 therefore closes by scoped reuse; FD904C closes from C1 + C2. Whole-frame
performance remains FD904B, technical mode/reload/shutdown evidence remains
FD904A, and lifecycle/migration records retain their separate acceptance scope.
This review launched no runtime and requires no additional camera implementation.
