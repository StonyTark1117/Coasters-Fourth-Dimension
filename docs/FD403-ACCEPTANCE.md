# FD403 connected-client acceptance procedure

This is a procedure for the remaining crafting and teaching gate, not a record
of a completed run. Use the [building guide](BUILDING.md) for player instructions
and the [FD403 roadmap entry](../ROADMAP.md#fd403-complete-crafting-and-teaching-content)
for the release criterion. Keep this acceptance on the standalone dependency
profile; optional Track Styles and Engineered behavior has separate gates.

## Evidence already recorded

| Family | Connected survival-client evidence | Still needed for this gate |
| --- | --- | --- |
| Arrow | [Craft, place, board, control, unload, save](evidence/0.4/survival-client-arrow.md) on a short unpowered section; [bounded occupied powered pass and cold reopen](evidence/0.4/survival-client-arrow-powered/acceptance.md) on a survival-built 50-block original route with a full controlled turn | Full connected Arrow circuit and family teaching walkthrough; the powered pass used a disclosed upstream motor fixture |
| Legacy | [Craft, place, board, control, unload](evidence/0.4/survival-client-legacy-free-spin.md) on a short unpowered section; [bounded occupied powered pass and cold reopen](evidence/0.4/survival-client-legacy-powered/acceptance.md) on a survival-built 50-block original route | Visible free-rotation exercise and family teaching walkthrough; the powered pass used a disclosed upstream motor fixture |
| Free Spin | [Craft, place, board, control, unload](evidence/0.4/survival-client-legacy-free-spin.md) on a short unpowered section; [bounded occupied powered magnetic pass and cold reopen](evidence/0.4/survival-client-free-spin-powered/acceptance.md) on a survival-built 50-block original route | Isolated damper-versus-capture observation and family teaching walkthrough; the powered pass used a disclosed upstream motor fixture |
| Revised | [Connected survival craft, place, board, control, unload, and reopen](evidence/0.4/survival-client-revised/acceptance.md) on a short unpowered section | Occupied powered lower-hinge exercise and teaching review |
| PAX | [Connected survival craft, route and four-car placement](evidence/0.4/survival-client-pax/acceptance.md); [player-linked occupied cycle, unload and reopen on the survival-built train](evidence/0.4/survival-client-pax/survival-cycle/acceptance.md); [shipped-demo cycle](evidence/0.4/survival-client-pax/demo/acceptance.md) | The powered run used a disclosed upstream motor fixture |

The [native survival checkpoint](evidence/0.4/survival-acceptance.md) already
checks all fifteen crafting outputs, five matching placements and twenty
wrong-family rejects. The [teaching checkpoint](evidence/0.4/teaching-acceptance.md)
and [Ponder checkpoint](evidence/0.4/ponder-acceptance.md) cover item guidance,
recipe discovery and all 53 scene registrations. The [current-artifact rendered
review](evidence/0.4/teaching-current/acceptance.md) checks all fifteen tooltips
and the captions and controls of all nine unique scenes. These checks do not
replace connected-client building and operation.

## Connected-client sequence

1. Record the exact source commit, packaged JAR digest, Minecraft/mod versions,
   installed mod list, world seed, game mode and any fixture commands. Use a
   fresh survival world or separate non-overlapping plots. Supply upstream
   materials through a documented fixture if needed, but supply no Fourth
   Dimension output items. Such a fixture proves crafting from ingredients;
   it does not prove unassisted material gathering.
2. For each family, acquire the recipe discovery ingredient listed in the
   [building guide](BUILDING.md#craft-and-discover), verify the recipe appears
   in the connected player's recipe book, and take the track and car from the
   normal crafting result slot. Craft the Rotation Tool and a Boarding Station
   for non-PAX families, an inducer and damper for Free Spin, and a Shuttle Winch
   for PAX through the same client. Record the recipe output counts and resulting
   inventory; a command-granted car or track does not count.
3. Place native Coasters anchors through ordinary item use. Connect them with
   the crafted family track, verify the survival item cost, then use the matching
   crafted car on a connected anchor. Exercise the Rotation Tool, capture and
   restraint controls. Confirm that a mismatched car is rejected without item
   consumption or a new vehicle. Keep enough clear space for the full rider and
   carrier sweep.
4. Build or extend a usable powered route with ordinary upstream components.
   For PAX, inspect the completed station curve after adding the spikes;
   equal-height station anchors alone do not prove a level tangent. Correct
   native handles and rebind the profile before placing cars if needed.
   Board an original seat through the connected client, close restraints,
   dispatch, observe measured chassis travel and the family's advertised
   carrier behavior, then capture, open restraints and dismount. A state change
   to `Aligning / moving` alone is not powered-travel evidence. A short route
   that ends in a compatible-track fault is not a completed operating example.
5. Save and reopen the world. Check that the original family, rail/profile,
   vehicle/carrier identities and normal captured/unloaded state survive. A
   normal client/server shutdown and a readable saved world are part of the
   record.

## Family mechanics and teaching review

| Family | Exercise to observe in the connected client | Tutorial points to review |
| --- | --- | --- |
| Arrow | Controlled profile, including a nonzero full turn, on matching track | Distance keys, capture, restraints, and why 360 degrees is not zero |
| Legacy | Occupied free rotation on matching track with a safe return to capture | Rider loading, damping/ballast, and no driven section-angle keys |
| Free Spin | Independent carrier response across installed inducer and damper spans | Span targets/direction, damping versus capture, and matching track |
| Revised | Occupied independent lower-hinge carriers on Revised track | Distinction from Free Spin and the need for separate clearance |
| PAX | Four matching cars linked with Minecraft Chains, bound to a powered winch; occupied lift, release, outbound/return and final catch | Shuttle route and spike requirements, platform operator, boarding readiness, and final capture |

The current-artifact teaching review above completes the common tooltip and
Ponder review. For each row, open that family's item guidance and Ponder lessons through normal
inventory controls, then follow the corresponding
[family walkthrough](BUILDING.md#family-walkthroughs). Review all fifteen item
tooltips and all nine unique scenes on the candidate artifact, checking readable
captions and controls, family matching, the certified ordinary
cart exception, controlled versus free motion, and the PAX boarding sequence.
The nine Ponder diagrams are illustrations; their playback alone does not prove
physical operation. Record any misleading instruction or clipped UI, correct
it, and repeat the affected review.

## Acceptance record

Keep one concise record per family with source/artifact identity, fixture inputs,
recipe-book and crafting observations, item-cost changes, track/car placement,
normal seat interaction, measured travel, carrier behavior, capture/unload,
save/reopen result, and selected screenshots or video plus source-log hashes.
Record failures and retries as well as the final successful run. Mark FD403
complete only when all five family paths and the teaching review above have
connected-client evidence, and any deviations from the standalone scope are
stated explicitly. Preserve the native and earlier packaged checkpoints as
bounded evidence rather than broadening their claims.
