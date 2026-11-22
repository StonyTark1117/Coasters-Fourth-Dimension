# Crossbeam assistance — source geometry and capture plan

The user's two 2026-11-17 Revised views prompted this inspection. This is a
bounded FD2101 defect investigation, not whole-family model acceptance.

## What the source proves

`VehicleFactory` places seven `wing_strut` blocks along Z for Free Spin and
Revised. Their stateless blockstates apply no rotation. Each model extends
X=0..16 but Z=4..12 in model units: adjacent Z cells have **0.5-block gaps**.
The controller-to-center-only graph in the original support audit does not
establish connectivity to those distant rows.

Run `python3 scripts/fd2101-coordinator-crossbeam-audit.py` to reconstruct the
current baked stationary assembly and regenerate `coordinator-crossbeam-audit.json`
and `coordinator-crossbeam-views.svg` / `.png`. Green elements have a 3D contact
path to the controller; red elements do not. Contact includes face-touch. The
views deliberately exclude procedural geometry, track and seats. Therefore red
does **not** mean a current fully rendered car is necessarily disconnected.
The JSON records source identities and assembly bounds; the helper reconstructs
the current factory placements explicitly and must be updated if they change.

The newly added procedural solid girder bridges the baked gaps. At inspection it
used center-relative Y=2.15, height=.85 and width=.78. Its bottom is Y=1.725,
only .025 below the lower Revised row's top (Y=1.75). Its braces end at Y=2.5,
near the girder top, rather than at the lower row centers. The long paired-row
structure also included Legacy, which has no paired-row spine in the factory.

## Coordinate convention and bounded correction design

`OriginalCarrierSupportRenderer.at` adds the parent plot center **plus .5**.
All following coordinates use that same center-relative convention:

- Upper transverse beam center: Y=2.0; full height Y=1.75..2.25.
- Revised lower transverse row centers: Y=1.5, Z=-3 and +3;
  full height Y=1.25..1.75.
- Continuous longitudinal spine should use the same I-section rotated into Z,
  not a large solid box: width=.5, height=.5, flange thickness=.09375,
  web width=.125; center Y=2, Z span -3.5..3.5.
- Revised requires explicit center risers at Z=-3 and +3 from Y=1.375..2.0,
  square section .5, overlapping both the lower row and upper spine.
- All of those parts belong to the stationary parent. Rotor hubs/cranks retain
  their existing actual child transforms. Legacy and PAX do not receive a
  paired-row longitudinal spine.

A procedural I-section preserves existing saved cars and block placements.
Changing factory block types or axis states instead requires explicitly handling
existing cars; rotating every `wing_strut` globally would break transverse beams.
The generator and runtime owner must reconcile this design before final closure.

The implementation-ready diff is `coordinator-crossbeam-repair.patch`.
It fills only the six flange gaps and the two outer end strips, retaining the
existing baked flanges as intermediate sections. This avoids duplicate coplanar
faces. A continuous Z web and the two Revised risers complete the section.
Run the helper with `--proposed-repair` for matching bounds/projections. The
proposed assembly has no disconnected cells in either paired family. These are
design/static checks, not a claim that this patch is loaded in Minecraft.
`git apply --check` passed against the inspected renderer; production edits stay
with the acknowledged file owner until a transfer is accepted.

## Reliable private-client images

Use the exact owner's private display, existing car identity and saved world.
Record artifact/source revision, body UUID, world camera position, yaw/pitch and
parent/child pose. Set daylight/clear weather for the test copy, close chat,
wait for the unverified-chat banner to disappear, and hide HUD for the image.
Keep diagnostic readbacks separately. A bright sky behind the joint is useful.

Capture the stationary car first, with fixed FOV and a complete joint in frame:

1. **Side view along X:** longitudinal spine, controller saddle and both row
   risers. This exposes the six Z-cell gaps that front projection conceals.
2. **Front view along Z:** each crossbeam and its left/right bearing roots.
   Include the controller-to-center connection without seat backs obscuring it.
3. **Underside oblique close view:** one riser-to-spine and riser-to-row joint,
   showing both intersecting surfaces. Repeat at the other row only if its
   construction differs or the first view does not resolve it.

Derive camera positions from the native parent's pose and local axes, rather
than repeated world-angle guesses. Keep camera outside beam/seat volumes;
near-plane clipping can create false holes. A screenshot only establishes
surfaces actually visible in it. Then reuse the planned occupied moving pass
to inspect the changed stationary/rotating boundary; do not infer moving fit
from these static images or rerun unrelated mechanics.
