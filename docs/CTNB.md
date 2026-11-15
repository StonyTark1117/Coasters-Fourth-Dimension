# CTNB tracks and controls

This adapter targets CTNB / Tracks and Balloons 1.2 (`coasters_extras`), file
8718286, with the existing Coasters 0.1.5 core. Its code and selected model
resources are fingerprinted. An unrecognized build grants no managed-car access.

The selected materials are Boost, Powered Boost, Station, Launch, Reverse,
Brake and Sensor Track. Their four rail/beam/tie OBJ meshes exactly match the
ordered native N-track vertices. Wing cars use these tracks within the existing
Wing curve, bank and clearance limits. Arrow cars can use selected materials on
their existing functional Arrow sections; repainting preserves the section ID,
family, rotation profile, geometry fingerprint and original-curve recovery data.
Other CTNB materials and other managed families remain unqualified. Bobsled's
angular drive and the passenger driver never override a managed car's pose or
ride authority.

Use CTNB's normal **sneak-use with the desired track item** on an empty curve to
repaint it. A car on that section or either adjoining endpoint prevents the
edit. Functional Arrow sections reject unqualified materials. Empty ordinary
track retains the addon's general painting behavior; that does not make an
unqualified material eligible for a managed car. A crossing without a shared
graph endpoint is not a provider join.

Bind a Boarding Station to the stopped car, approach it with the Rotation Tool,
and choose **Claim CTNB**. An existing owner must first hold the complete train;
the change transfers authority and invalidates the previous provider's lease.
Board normally, close restraints, and Dispatch. Native Chain, Engineered and
Magnetized cannot simultaneously push a CTNB-owned ride.

The native anchor dials supply target speed, launch speed, direction and signal
mode. Targets are capped at 12 blocks/s and drive acceleration at 8 blocks/s²;
braking is capped at 4 blocks/s². A zero brake target stops without reversing.
Powered Boost and signal-gated launches require endpoint redstone. Losing that
signal latches a hold: restore the signal, Capture/Stop, **Resume owner**, wait
five stopped ticks, then Dispatch. Restoring power alone never restarts a ride.

Station Track captures the complete train and counts its configured dwell only
while stopped. Dwell expiration leaves it held. Powered station anchors also
hold it. Remove that hold, close restraints, and explicitly Dispatch. Reverse
Track captures before changing direction: choose **Drive forward/reverse** while
stopped, match any explicit native direction dial, then Dispatch. The bound
Boarding Station may authorize this operation on a complete selected station or
reverse span away from the original platform. All ordinary body, restraint,
support, fault and five-stopped-tick checks still apply. Saved dwell is retained;
departure permission is not automatically restored.

Sensor Track reports actual managed-car presence to CTNB's sensor tracker.
Unpowered native sections between qualified sections coast under the same owner.
Material decisions read the live anchor curve, because the native path graph can
retain an earlier material after repainting. Exact namespace/path checks avoid
CTNB's broad `ResourceLocation.equals` alias for every `*_track` ID.

`/fd demo ctnb` supplies a ready Wing loop with all seven selected materials.
`/fd demo ctnb_track` leaves its first curve native and supplies no car: repaint
that curve as Station Track, place a Wing car and bind the station normally.
The reverse span returns the ride along the loop to the level station. These
commands are disclosed setup conveniences, not evidence of player construction.

Qualification is tracked in [the current queue](CURRENT-QUALIFICATION.md) and
[the CTNB evidence](evidence/0.8/ctnb/README.md).
