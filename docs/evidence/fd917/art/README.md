# FD917 Art / pipeline handoff

**Assigned Art outcome complete; ready for lead integration.** Primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, branch **main**, owner
`01a1dd1d-6e99-70d2-aef3-6dcebac10d72`. Lead owns selective staging, commit and push.
No new branch/worktree. This closes Art's assigned production work; the lead
reconciles the whole FD917 package and other owners' outcomes.

## Delivered

- Editable common source and41 authored16×16 RGBA materials (committed04538f8b),
  native/style reference and [all17 finite briefs](production-briefs.md).
  [Resource disposition](resource-disposition.json) inventories489 current assets;
  source hashes are inventory, not substitutes for owner acceptance.
- Nine A/B families: Arrow, Legacy, Free Spin, Revised, PAX, Wing, Fixed Inverted,
  Suspended and Dive.124 existing owned outputs plus9 new restraint partials and
 9 articulation descriptors. Four deterministic generators preserve IDs, seats,
  pivots, mass and interaction authority. Five original controller placeholders
  are replaced with shared-material casing/webbed beam exports.
- Shared [Arrow occupied cycle](arrow-occupied.md) and [final connection fit](arrow-interface.md)
  accepted by lead with its full-range contact evidence; VP03 closes cumulatively.
  [Legacy/Revised/Free Spin fit](family-fit.md), Wing day/night open/closed,
  Fixed Inverted open/closed, Suspended open/closed/reload and restored journal,
  and [Dive occupied floorless fit](dive-fit.md) pass. PAX uses Mechanisms'
  `../mechanisms/pax-occupied-catch-corrected.png` and actual occupied cycle.
- [Items and teaching](items-teaching.md): all17 whole-car icons; representative
  hand/ground/frame/third-person transforms; Matching/Arrow/Boarding and corrected
  PAX closed-travel/open-return Ponder. Wide original6car first-person silhouettes
  normalized to.18 scale. Generator supplies a scoped language patch, merged by
  lead, without overwriting unrelated keys.
- [Appearance reconciliation](appearances.md): current Track Styles/Original join,
  retained Skin/fallback and provider ownership evidence. Native/provider art
  remains external. Accepted unchanged mechanics and adapters are carried forward.
- [Validation](export-validation.json):142 owned JSON bounds/UV/local texture/alias
  checks pass; Ponder repeat export is byte-identical. Build/packaged startup passed
  with Java21. Launch03 exposed early Sheets static initialization; lead fixed it
  and combined launch04/05 startup passed. No unrelated qualification sweep.

## Integration and saved runtime

[Exact integration paths](integration-paths.txt) include only Art-owned generators,
models/states/partials/descriptors, Ponder outputs, source README and this evidence.
Common material files already committed are excluded. `arrow-occupied.log` is an
ignored evidence log and needs explicit inclusion if committing it. `en_us.json`
and Mechanisms' shared renderers/resources remain their owners' paths.

Latest packaged candidate launch05:
`9030cc1ff5f16c2af9a8ffa1e98138eb092a2bb2e1b1605d3c2193f39f9a8d45`,
combined Skin/Additions/Track Styles, no overlay. Earlier bounded views retain
their actual launch identities. Source changes after their views only affected
named controller screw aliases and six item hand transforms; both final changes
were checked and packaged without resetting unchanged world-fit evidence.

Root `/mnt/NVME4/fourthdimension-runs/fd917-art-20261115` preserves immutable
artifacts, full logs/screenshots, SkinWorkshop and copied DiveArtWorkshop.
Private317, server25652/RCON25653; Java21 client1536M/6GiB, server2GiB/4GiB.
Final client peak3034652672bytes. [Cleanup](runtime-cleanup.txt) verifies client,
server and Xvfb inactive/MainPID0 and all dimensions saved. No process remains
owned by this completed assignment. [Bounded readbacks](owned-readbacks.txt).

Remote Dive single-car fixtures report stale velocity and were left diagnostic;
no readiness guard was bypassed. Final Dive fit used the original accepted
three-car dock with near-zero measured velocity. That fixture limitation did not
reopen unchanged Dive mechanics or block the successful distinct art check.

## Shared reference and material contract

Pinned native dependency: `curse.maven:create-coasters-simulated-1601871:8655742`.
Inspected `assets/simulatedcoasters/models/block/coaster_cart.json` (one element),
`coaster_cart_bottom.json` (nine), `coaster_cart_wheel_axis.json` (ten), their
`textures/block/coaster_cart/{bearing_base_side,bearing_side,bearing_support,bearing_top,wheel}.png`,
and the native track OBJ/texture resources under `models/block/track/coaster_track`.
OBJ geometry is retained as native geometry, not counted as missing JSON elements.
The local comparison sheet is retained at
`/mnt/NVME4/fourthdimension-runs/fd917-art-20261115/reference/native-material-reference.png`;
native pixels are not redistributed in runtime assets.

Native reference: dark cool worked-metal structure, brighter narrow contact
edges, compact fasteners and exposed wheel/bearing attachments. The addon uses
family paint on shells/caps rather than replacing every surface with concrete.
All sixteen existing Wing colors remain. Material source is the authored 16×16
pixel maps/palettes in `assets-source/fd917/materials.json`; no random noise,
procedural placeholder repaint or external artwork is generated.

| Role | Shared resource | Treatment |
| --- | --- | --- |
| Structure | `block/fd917/structural` | Low-contrast dark steel, restrained edge break |
| Contact/shaft | `machined` | Directional bright bands; local to real contact/shaft surfaces |
| Fastener | `fastener` | Dedicated centered head, used only on hardware faces |
| Family enamel | `paint_<dye>` | Broad colored surface, limited edge tonal change |
| Seat cushion | `upholstery_<dye>` | Lower-value padded vinyl with intentional inset/seam |
| Restraint cushion | `restraint` | Dark smooth padding, distinct from steel and rubber |
| Rubber | `rubber` | Dark tread treatment, no metal glints |
| Indicators | `indicator_off/ready/hold/fault` | Small framed colored face; ordinary world lighting |

Density is 16 pixels/block. Larger faces are tiled without internal duplicate
faces; a full sprite is not stretched across long beams. Small faces sample a
correspondingly small UV area. Explicit hand-authored UVs remain possible.
The common helper exports PNG only when explicitly invoked with `--export`.
Family generators export only their exact owned model/blockstate set, with
recipes, language and other owners' equipment excluded.

## Component handoff

Mechanisms' articulation descriptor is
`assets/fourthdimension/fd917_articulation/<block_id>.json`, with `parts` containing
`model`, authoritative boolean `property`, model-unit `pivot`, `axis`,
`open_degrees`, `rotate_facing` and optional property `when` map. Closed mesh is
angle zero; false means open. Body `renderPose` and baked shell use the same frame.
Positive X opens north-facing shoulder bars; each chosen sweep is checked against
rider/head and frame bounds. Static shells contain fixed hinge mounts only;
animated parts have one render owner. Native optional seats bypass this renderer.

Existing seat mounts, physics shapes, mass, carrier pivots and saved IDs remain
unchanged initially. Any enlarged restraint sweep is a named new visual envelope,
not silently treated as equivalent to the former endpoint-only art.


## Transferred cue/interface follow-up — complete

Mechanisms subsequently transferred the bounded [Free Spin magnetic interface](free-spin-magnetic-fit.md)
and [Dive hold cue](dive-cue-fit.md) observations to Art. Both pass cumulatively
with the original Art handoff. The Dive check found thin-symbol UV cropping and
light sampled inside the opaque controller; Mechanisms corrected both. Client08
shows actual fault red, resumed HELD amber and committed DROP off. Free Spin's
native changing-pose views preserve the designed magnetic air gap.

[Follow-up cleanup](followup-runtime-cleanup.txt) verifies client/server/Xvfb
inactive/MainPID0 and all worlds saved. [Exact follow-up paths](followup-integration-paths.txt)
are separate from the already integrated original211-path manifest. Lead retains
staging/commit/push ownership. No Art runtime or source work remains outstanding.
