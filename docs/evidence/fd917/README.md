# FD917 production baseline reconciliation

**Complete — 2026-11-15.** All 17 adopted styles and the finite shared visual,
mechanical, state, item and appearance criteria are accepted cumulatively.
Implementation and final owner evidence are integrated on `main` through
`c3ff5b9c`; the [package identity](lead/integration-package.json) records the final
Java21 artifact. No required FD917 case remains open. FD1000 freeze and release
qualification/publication remain separate and have not started.

## Integrated result

- [Finite 17-style briefs](art/production-briefs.md) and
  [resource disposition](art/resource-disposition.json) preserve native seats,
  pivots, mass, running gear and provider ownership. Production shells, suspension,
  materials and items give each adopted style its own silhouette.
- [Authoritative sources/export instructions](../../../assets-source/fd917/README.md)
  define deterministic family geometry, bounded UVs and41 authored pixel materials.
  Art's142 resource checks and Mechanisms'237-resource export checks cover their
  separate outputs. Native and optional-provider artwork remains external.
- [Arrow and shared track](lead/README.md) provide actual-pose rail/follower/rack/
  transmission/axle continuity, bounded spring/clutch behavior and textured,
  world-lit/cached/culled track. VP03 is accepted cumulatively, including actual
  occupied interface fit, joins/banks, forward/reverse, multi-turn and capture.
- [Art handoff](art/README.md) covers nine A/B families, all17 whole-car icons,
  held/world transforms, affected Ponder and combined optional appearances.
  [Mechanisms](mechanisms/README.md) supplies articulated restraint endpoints,
  physical pivots/locks, Pipeline wheels, PAX drive/catch and truthful state cues.
- [Integrated package](lead/integration-package.json) built under Java21 and
  contains429 verified byte-identical selected resources. Bulk source integrated in
  `b07574fa`; final source/evidence in `c3ff5b9c`. The targeted S2 example repair
  is `7a32be1f`.

## Cumulative visual evidence

| Styles / shared claim | Evidence and disposition |
| --- | --- |
| Arrow | [Transmission/range/contact](lead/README.md), [occupied normal cycle](art/arrow-occupied.md), [neutral/nonzero interface](art/arrow-interface.md): accepted. |
| Legacy, Revised, Free Spin shells | [Distinct occupied open/closed fit](art/family-fit.md): accepted. [Actual field/reaction gap](art/free-spin-magnetic-fit.md) also passed. |
| Wing, Fixed Inverted, Suspended | [Art handoff and views](art/README.md): accepted, including preserved16 Wing colors, dangling-leg space and Suspended journal/single-bar reload. |
| Dive shell | [Occupied six-abreast floorless fit](art/dive-fit.md): accepted. [Actual fault/held/released cue](art/dive-cue-fit.md): accepted. |
| PAX | [Corrected guide/catch and actual occupied cycle](mechanisms/README.md), [remote close/far-spike/late-join](lead/observer/README.md): accepted within the recorded bounds. |
| Axis | [Occupied open/closed, banked actual full roll and normal unload](lead/families/README.md): accepted. |
| Roller Ball, Powered Inverted | [Occupied fit, actual swing/yaw/show and normal unload](lead/families/README.md): accepted. |
| Conventional Flying, New-generation Flying, S1, S2 | [Mechanism evidence](mechanisms/README.md): accepted distinct occupied posture/yaw/roll views and verified final cleanup. |
| Pipeline | [Wheel/occupied route record](mechanisms/README.md): forward/reload plus targeted reverse/forward restoration and contact/lighting passed; final handoff and cleanup accepted. |
| Items, teaching and optional appearances | [All17 icons and shared transforms/Ponder](art/items-teaching.md), [Skin/Track Styles/provider reconciliation](art/appearances.md): accepted; unchanged adapter acceptance carries forward. |

## Affected behavior and limits

New visuals use actual interpolated parent/child poses and authoritative state.
Client restraint interpolation changes presentation only. No family capacity,
saved program, native seat anchor, pivot, mass or optional-seat authority changed.
Arrow's finite spring/clutch overtravel response has its own passing native and
connected evidence. PAX's bounded snapshot has its packet-size and actual remote
observer checks. The [S2 example lifecycle repair](lead/s2-example-lifecycle.md)
passed one targeted native test without weakening ordinary power/authority guards.
The early Sheets initialization failure is fixed and combined-profile startup
passed; those are the named affected behavior checks, not a new profile sweep.

Shared track's comparable scene costs3.4069ms versus the retained6.5595ms CPU
stage; Arrow's close/moving assembly averages about1.6ms within its3ms working
allowance. Mechanisms records bounded per-stage measurements for its scenes.
These are component CPU observations, not GPU, whole-frame or representative-park
qualification. FD904B retains its final performance workload and budget ownership.

Earlier FD404/FD503/FD601/FD701/FD813/FD915/FD916 and unchanged provider/physics/
persistence acceptance remain closed. This package hands its integrated resources,
actual identities and accumulated evidence to FD1000 and FD901–FD904; no second
art campaign or Cartesian family×provider replay is implied.

## Final cue correction

The real held Dive fault exposed two presentation defects: density UVs cropped
small indicator faces to the dark border, and the lens sampled light inside its
opaque housing. The renderer now samples the whole indicator and exposed light
above the controller, preserving world lighting. Art's [same-state live proof](art/dive-cue-fit.md)
shows red fault, normal Resume to amber HELD and committed DROP/off with release
epoch16. Earlier failed views remain labeled. The final baked station/Dive symbol
change is UV-only: [84 faces and deterministic exports](mechanisms/indicator-export-check.json)
passed; all other geometry/materials are unchanged. No extra family replay is needed.

## Runtime disposition

All owned clients, servers, private displays and collectors are stopped. Saved
worlds and full logs remain under their recorded persistent run roots. The
[lead family cleanup](lead/families/runtime-cleanup.txt),
[Mechanisms cleanup](mechanisms/runtime-cleanup.txt), and
[Art follow-up cleanup](art/followup-runtime-cleanup.txt) verify inactive units
and MainPID0; server logs confirm saved worlds. Earlier lead/observer and Art
baseline cleanup remains linked in their handoffs. No runtime is left awaiting
an owner, and no process failed cleanup.
