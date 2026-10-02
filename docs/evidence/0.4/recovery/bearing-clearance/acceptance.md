# FD402 native lower-bearing clearance truss

Dev.22 continues FD402 in the 0.4 stage. It addresses a **candidate** visual
overlap found while reconstructing the pinned Coasters lower-bearing renderer:
the former Original crossbar/mount topology passed through the bearing's
centerline. A zero separating bound is not a proof of a live client collision,
but it is not adequate clearance evidence.

## Implementation

Original appearance now uses two 0.06-block-radius outboard supports at local
right `-1.12` and `+1.12`, joined to a local-up `-0.70` underbrace. When Track
Styles owns the spine, its transition crossbrace follows that lower path. The
former centerline running-rail crossbar and diagonal mounts are no longer
rendered. Functional rails, track-family rules, native coupling and vehicle
physics are unchanged.

The independent sampled recovery audit also loads the pinned native
`coaster_cart_bottom` model from the installed Coasters JAR. It transforms each
of its unrotated cuboids using the recorded chassis frame, then checks it with
the original track mesh and neighboring original cars. This is source- and
resource-pinned evidence; no external mesh or texture is copied into the
repository.

## Local validation

Fresh dev.22 standalone and combined Track Styles/Engineered GameTest exports
reported the same conservative minimum separations:

| Route set | Minimum lower-bound separation |
| --- | ---: |
| Five single-car family routes | 0.06356753 blocks |
| Original Arrow/PAX train routes | 0.06355955 blocks |

The required sampled margin is 0.02 blocks. Both profiles passed all 37 native
GameTests and the five-family/two-train native route-frame checks. The
level-straight wheel audit also measures every revised support component and
rejects a support projection with no nondegenerate triangles. CI repeats the
same audits in its standalone, Styles, Engineered and combined profiles.

## Scope still open

This does not certify rotating native wheel sweeps through banks or curves,
rendered players, mechanical linkage, optional decoration/resource overrides,
continuous motion, real-client operator operation, Engineered recovery power,
general strength or the full recovery lifecycle. FD402, FD404 and the 0.9 gate
remain in progress.
