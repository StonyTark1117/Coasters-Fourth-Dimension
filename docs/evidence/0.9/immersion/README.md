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
upgrade. FD912 owns the completed executable effects and connection qualification below.

## Inspected composition seams

* Ride eligibility uses `CoasterCart.cartOf`, reflecting the native
  `hostCartFromVehicleChain(Entity)` helper. FD carrier eligibility must resolve
  the actual passenger child, not substitute a chassis pose for its rotation.
* Immersion injects at the return of Sable `getEntityOrientation`, the same seam
  as FD's neutral Flying head frame and comfort orientation. At initial inspection both used
  default mixin priority; the final composition below resolves their order.
* `CameraFilter.apply` mutates its input quaternion, retains a raw orientation
  for telemetry and caches the filtered result per render frame. It must run
  once on the resolved head frame without modifying the physical carrier pose.
* Camera travel adds a translation at `Camera.setup` tail; cinematic angles,
  FOV, blur, streaks and sound are separate addon paths. Comfort must take
  precedence over camera motion while leaving eligible sound/visual effects.
* `RiderForces` consumes the detected sublevel for local and remote bodies.
  `PassengerFacing` adjusts a render pose stack, not the authoritative mount.
  On Flying and compound Flying, its mouse-selected quarter-turn torso swivel
  yields to FD's retained physical torso; other addon effects remain eligible.
* Sable snapshot spline/render-pose injections affect client interpolation.
  Their compatibility belongs to the qualified shared baseline and FD912's
  affected occupied views, not a new replay of closed family acceptance.

## FD912 status

FD501F and FD912 complete, 2026-11-15. Dependency load, client-only connection,
effects composition and affected rider lifecycle cases passed. Source integrated
on `main` in `fd8f334a`; no remaining assigned runtime or case.

The first packaged client attempt exposed a dependency launcher defect: Immersion
was present in ModDev's legacy dependency list but absent from the direct Java
launch classpath. It did not load, so that attempt supplies no effects evidence.
The client/server/display were stopped with the world saved. Lead's build fix
`025dd6d4` adds the per-run dependency to direct launch generation. Regenerated
client/observer classpaths contain the exact addon basename, and both dedicated
server classpaths omit it. That repair alone did not make FML discover the mod.

Read-only inspection of the selected FML 4.0.45 and BootstrapLauncher 2.0.2 found
the second cause. `additionalRuntimeClasspathConfiguration` puts its entries in
the legacy **bootstrap-library** classpath. BootstrapLauncher creates modules
from those entries; LaunchContext marks their paths already located.
UserdevLocator finds the addon metadata, but DiscoveryPipeline then skips its
already-located path. Existing Create/Additions/Sable mod JARs are absent from
that bootstrap list. Standalone Java 21 probes confirmed that the exact Immersion
JAR metadata/classes are readable, its module name is `coasters.immersion`, and
there is no module collision or malformed manifest.

The required correction is a dedicated client-mod dependency on direct client
and observer launch classpaths (and Gradle client JavaExec classpaths), absent
from legacy bootstrap classpaths and every server classpath. No third identical
launch was attempted: both clients, server and owned display were stopped, with
the world and diagnostic logs preserved. Actual load verification follows the
specific build correction.

The corrected expanded profile passed actual load and client-only connection at
08:26:46 host time on 2026-11-15. The client discovered `coasters_immersion` 1.1.1,
logged its initialization, loaded its resources and exported its transformed
classes. Dev joined and authenticated UDP to the dedicated server without the
addon. Preserved records under
`/mnt/NVME4/fourthdimension-runs/fd09-seats-immersion-20261115/evidence/`:
`launch-04.json`, `client-loaded-04.log`, `server-loaded-04.log`.

Before occupied effects acceptance, exported `camera-transformed-04.txt` exposed
the native callback preceding the FD callback and cancelling it. This invalidated
the assumed injection order, not the client-only dependency result. The run was
saved and all three owned units stopped. The assigned FD camera mixin now applies
after the native mixin so its RETURN callback can precede the native filter;
the next targeted run verified the transformed order before collecting effects.

## Final composition and connected result

The Java 21 [packaged artifact](package-05.json), SHA256
`787a01bfafd96f0d407449c43c092c38f3a0978089b365fb46d95cc9f919ab07`,
ran in the expanded profile named in [FD911](../rotating-seats/README.md).
Both actual clients loaded Immersion; the dedicated server did not.
[Launch classpaths](launch-04.json) and preserved full startup logs establish
installation asymmetry; this is not merely metadata compatibility.

[Exported camera bytecode](camera-transformed-05.txt) proves that the priority 900
FD RETURN callback executes before the native Immersion callback at every
original return. For registered FD mounts it returns the composed frame and
bypasses the second filter. Its order is native physical frame → existing Flying
neutral-head adjustment when applicable → optional Immersion camera filter →
existing comfort frame. Other vehicles retain the native addon callback.
Filter call counts measure helper invocations, not distinct rendered frames;
Immersion's existing per-frame cache is retained. No second authoritative pose,
seat entity, rider assignment or physical motion controller was added.

| Required behavior | Accepted evidence |
| --- | --- |
| Actual effects on managed carrier | [Pose excerpts](client-pose-evidence.txt) recognize the real Arrow child and show moving speed, physical up-vector and increasing filter calls with comfort off. Native first-person arms and visual effects are visible in the connected captures. |
| First/third person and inversion | [First-person inversion](firstperson-inverted.png), [third-person inversion](thirdperson-inverted.png). Physical up reached −.798 and −.515 in the respective records; the optional seat and rider stayed attached. Real Observer remained connected and received the same carrier/rider state. Wider combined fit is in FD911/FD916. |
| Relative mouse look | Actual XTest device turns changed mounted yaw independently: Arrow 45.00→72.15°, then comfort 170.77→185.92°. Physical carrier state and occupant count remained authoritative. These are actual pointer input, not setup teleport angles. |
| Comfort with optional effects | [Comfort pose log](comfort-motion.txt) shows physical rotation from near 0 to 4.285 rad while the first-person camera quaternion remained `(-.03725,-.4256,-.01754,.9040)` after the look input. Physical up became −.652, comfort remained true and filter calls stayed 104600. The bounded screenshot collector missed that instant; logged camera/physical pose evidence establishes it without another ride. |
| Distinct controls | Immersion's K menu collided with FD's default K. Normal Options→Controls→Key Binds changed **Fourth Dimension: Toggle world-stable rider view** to **V**, leaving Immersion on **K**. [Resolved bindings](comfort-key-conflict-resolved.png). V then toggled comfort without opening a menu, and persisted across process restart. Mouse use/attack and Raw Input stayed unchanged. |
| Exit, reconnect and full restart | Normal open-bar exit set body=null, mounted=false, speed 0 and stopped filter calls. Actual occupied disconnect/rejoin and full client/server process restart restored the same fitted seat and preference; final normal unload reset presentation again. See [FD911 lifecycle evidence](../rotating-seats/server-lifecycle-evidence.txt) and pose excerpts. |
| Changed Flying torso branch | The additional targeted real-player observation below qualifies the new guard. It is not left as inspection-only evidence. |

## Flying guard and unchanged contracts

Immersion's PassengerFacing renderer normally adds a mouse-dependent quarter-turn
posture rotation. [Transformed guard](flying-facing-transformed-05.txt) returns
false for an existing FlyingRiderPose binding so the retained physical torso is
not turned a second time. The native family still owns posture, torso/leg pose,
eye origin, head frame and restraints; Immersion keeps its other eligible effects.

On a disclosed command-built conventional Flying fixture, Dev normally boarded,
closed body and leg restraints, selected prone and reached the 88.8° PRONE lock.
The [forward first-person view](flying-prone-forward.png) retained neutral gaze
with comfort off. A real 30° pointer turn changed look 179.85→210.00°:
[local view](flying-prone-look.png), [remote before](flying-prone-remote-before.png),
[remote after](flying-prone-remote-after.png). The remote torso remained physically
prone and aligned. Normal seated return, opening both restraints and Shift
produced [ordinary unload](flying-normal-unload.png), body=null and zero telemetry.
Two temporary approach blocks initially obstructed the protected sweep; removing
those exact setup blocks and normal Stop cleared the fault before this observation.

The [C1 baseline](../../0.5/comfort/README.md),
[conventional Flying](../../0.7/conventional-flying/README.md) and
[new-generation Flying](../../0.7/new-generation-flying/README.md) remain accepted
for their unchanged physical/camera contracts. Fixed, freely rotating and other
constrained families retain their existing native poses, registered seat offsets
and rider synchronization. The generic optional carrier lookup/filter uses that
same interface; Arrow supplies actual effects/comfort/asymmetric/lifecycle proof.
Conventional Flying supplies the distinct changed torso guard and neutral-head
composition proof. The guard also uses the existing new-generation Flying binding;
its yaw-deck/posture calculation is unchanged. No new full free/fixed-family ride,
new-generation yaw mechanism replay or all-family camera matrix is claimed.

## Player setup and cleanup

Install the selected Immersion 1.1.1 addon on the client alongside the qualified
dependency profile. The server does not need it. If both actions use K, rebind
the FD comfort action to V (or another unused key) in normal Minecraft Controls.
K then opens Immersion settings; V switches between seat-following and world-stable
views while preserving mouse look. Optional Additions seat selection and its
native Wrench/Restraint Selector controls are documented in [FD911](../rotating-seats/README.md).

[Cleanup record](cleanup.json) verifies both clients, dedicated server and both
private Xvfb displays inactive with MainPID 0. Client heaps were 1536M inside separate 6 GiB
scopes; observed post-restart peaks were 3.22/3.21 GB, with the server at 1.97 GB in a 4 GiB scope.
The complete saved world and immutable full logs remain under the persistent run
root recorded above. No runtime remains active or handed off. These outcomes feed
FD915 and FD904C2 without reopening the accepted family or C1 gates.
