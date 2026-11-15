# FD1601B/C and FD1601 complete — 2026-11-15

Owner `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`; primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`.
A is integrated at `7a563e6d006fff8d80221f6e7d139b42bda452a8`.
Lead owns all Git and central status operations. This record completes the
remaining construction/content criteria and final runtime cleanup.

## Normal construction

The same private Java21 client/server used default Use/Attack bindings and Raw
Input. Supplied materials, unlocked recipes, temporary scaffolds and positioning
are disclosed in `construction/console.jsonl.gz`; they earn no normal-input credit.
The corresponding real input sequence is `construction/input.jsonl.gz`.

- In Survival, the normal crafting-table recipe book consumed six iron, two copper
  and purple dye to produce sixteen matching overhead tracks. Four Create red
  seats, three remaining iron, a native Coaster Cart and one crafted track then
  produced the four-seat car. Both outputs were taken normally; recipe/output
  captures show the consumed ingredients, car and fifteen remaining tracks.
- Normal right-clicks placed anchors at(200,104,400) and(200,104,440). Two matching
  track clicks connected them, consuming ten of the fifteen track items for the
  forty-block straight route. Temporary anchor support blocks were then removed.
- The crafted car was normally used on the rendered matching graph and consumed,
  creating chassis`881e6b0a-922e-46a3-ab5e-ff5cae0604a4` at(200.5,105.125,408.5).
  A first aim used the example's differently mounted anchor height and returned
  the expected aim rejection. Native anchor curve data established actual
  graphY104.8125; the single calculated correction placed the car normally.
- A supplied second car was placed normally at(200.5,105.125,416.5), exactly eight
  blocks away, to check the ordinary coupling input. Its identity is
  `6b6a5876-8f14-4afe-961f-3e6013be7e2e`. This item was supplied, not claimed crafted.
- Two supplied Boarding Stations were placed by right-click beside the cars and
  normally bound; both cars reached Ready. The first station's editor initially
  remained open during second-station setup; no second placement was claimed
  until that screen was closed and the correct item/world target verified.
- Minecraft Chain selected the exposed first native bearing, then the second
  bearing. The qualified adapter below made the normal second click create the
  native eight-block link. Chain is reusable and was not consumed; its pending
  target cleared. Server log at07:07:21UTC records `linked carts ... distance8.000`.
- Normal sneak-use rebound the linked train to the common station. Its editor
  showed Ready. Normal cushion Use boarded the crafted car; client readback lists
  both manually coupled chassis. Normal Close/Open changed restraints on the held
  train. Shift dismounted normally, leaving passenger=false and no native seat
  orientation. These finite content views do not replay the accepted full ride.

## Eight-block Chain input correction

The first normal link packet arrived on the correct second bearing, but native
`CoasterCartLinkPlacement.isLinkDistanceTooFar` caps input at seven blocks;
`resolveChainLinkDistance` also snaps an otherwise accepted link to at most six.
The native A fixture calls `linkCarts(...,8)` directly and therefore could not
prove this input path. Required normal construction/coupling remained open until
this precise defect was fixed. The underlying A mechanics remained accepted.

`OverheadChainCouplingMixin` adapts only the Chain handler's distance rejection
and spacing resolution. `OverheadChainOutlineMixin` adapts the corresponding
preview/deny/spacing feedback. Both delegate to the same ConsistRules helper,
which permits eight only for Fixed Inverted or Suspended pairs that also pass
existing canCouple: same family/variant, exactly eight, at most four and both
boardingReady. Ordinary and other-family calls delegate unchanged native range
and snapping behavior; final native linkCarts and its ConsistRules guard remain.
No chassis, solver, seat, recovery or ride-pose behavior changed.

`normal-chain-adapter-build.log` records Java21 package success. The real server
and client launched both required mixins, then the normal second Chain click
passed at07:07:21UTC, with target selection clearing and a native8.000 link.
`coupling-before-adapter.png` versus `coupling-after-adapter.png` shows blocked red
versus eligible blue outline. The same helper path closes the named Suspended
normal eight-block coupling case by scoped carry-forward; no Suspended ride or
native campaign is repeated. Its guide correction is owned by the lead.

## Finite FD916 asset review

Reference is the adopted four-abreast fixed under-track row: purple cushions,
iron rear hanger/foot supports, dark harnesses and orange closure bars, with native
Coasters running gear. One variant,`four_abreast_v1`, is declared. Original authored
cuboid geometry uses intentional existing Minecraft materials; no new livery or
optional seat/skin configuration is introduced.

| Criterion | Evidence and result |
| --- | --- |
| Silhouette/proportions/style | The original four distinct cushions, fixed rectangular rear hanger and overhead bogie read as the specified fixed row. `visual/occupied-open.png` shows the constructed native assembly at normal scale. |
| Assembly/motion alignment | The upper crossbar was corrected below the native spine before the accepted run; exact native seat centers and connected physical frame remain fixed. A's bank/full-inversion/return trace and banked capture show no independent swing or separated child body; the reserved envelope remains clear. `visual/occupied-closed.png` shows rider, cushion and closed harness alignment. |
| Texture finish | All16 authored model files/88 cuboids use explicit UVs within0..16 and elements within[-16,32], recorded in `visual/model-review.json` and exact source hashes. Normal placed/open/closed views show intentional purple, iron, black and orange surfaces without atlas bleed, missing texture or unintended placeholder. |
| Player-facing appearance | Recipe-book/output and held item captures show the matching track and four-seat car representations. Open/closed captures show recognizable restraints and rider placement. A supplies the occupied moving/banked view; normal controls and a usable example are documented in `docs/FIXED-INVERTED.md`. |
| Supported variants | One fixed-seat variant, unchanged native running-gear art and shared renderer. Accepted Wing/Suspended/native rendering and remote-rider ownership carry; no livery/profile matrix is added. |

The corrected playable example's continuous128-block native Chain supplies the
complete roll from its upright approach, avoiding banked-anchor motors in either
the seat or running-gear envelope. The player guide explains construction, exact
spacing, Chain coupling, station binding, restraints, return/unload and explicit
recovery. Existing native controls provide curve/bank editing; no new operator
panel beyond the shared fixed-family controls is required.

## Cumulative closure and cleanup

[A handoff](A-handoff.md) covers exact native loading, fixed topology/fit, four-car
motion and full inversion, failure retention, non-upright save/reopen, explicit
resume, complete occupied return and upright camera restoration. This record
supplies normal crafting/construction/coupling and finite content review. All
FD1601A/B/C criteria are covered; FD1601 closes from them without an extra run.
FD601 and later consumers receive the accepted outcome; 0.7 remains unassigned.

Reproducing the pose analysis from the archived trace includes 4,913 complete
four-car frames, including the final stationary records after A's initial 4,867-frame
analysis. The minimum 3.5447-block separation, inversion extrema and seat-spacing
error are unchanged; `occupied/roll-native-summary.json` reflects the retained input.

`construction/cleanup.json` verifies client/server/Xvfb inactive with MainPID0 and
display314 absent. All dimensions saved at07:10:48UTC; RCON exited at07:10:50UTC.
No runtime is handed off or left live. Final client scope peaked2,344,214,528 bytes
under6GiB; server peaked1,420,406,784 under4GiB. Both used Java21; client heap1536M.
Saved world, original logs and retained diagnostic worlds remain at
`/home/braydon/.local/state/fourthdimension-runs/fd1601-fixed-inverted-20261115/server/FixedInvertedWorkshop`.
The final adapter artifact hash is in `construction/artifact.sha256`; A's original
artifact/hash and raw evidence remain accurately labeled rather than relabeled.

Ready files: ConsistRules helper, the two new overhead Chain mixins and registration,
Fixed guide and this evidence directory. The lead's unrelated CURRENT-QUALIFICATION
and Suspended guide edits and pre-existing `logs/` are preserved. Source ownership
is returned to the lead after this handoff. No uncommitted Fixed work is deferred.
