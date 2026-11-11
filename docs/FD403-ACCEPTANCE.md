# FD403 connected-client acceptance procedure

This is a procedure for the remaining crafting and teaching gate, not a record
of a completed run. Use the [building guide](BUILDING.md) for player instructions
and the [FD403 roadmap entry](../ROADMAP.md#fd403-complete-crafting-and-teaching-content)
for the release criterion. Keep this acceptance on the standalone dependency
profile; optional Track Styles and Engineered behavior has separate gates.

Apply [VALIDATION-POLICY.md](VALIDATION-POLICY.md): the sequence below describes
the required player capabilities, not a command to redo every step. Reuse already
accepted crafting, input, mechanics, teaching and save/reopen records after a
relevant-change review. Extend saved player-built routes when only the shaped or
powered portion is missing. A fresh world and one uninterrupted all-steps session
are not requirements; preserve enough provenance to connect the observations.
Use the [current 0.4 queue](CURRENT-QUALIFICATION.md#current-work-queue) for
case ownership and next actions. The matrix below records the gaps at queue
adoption; later case closures supersede its “still needed” wording. A family
walkthrough record may close both its family case and the matching FD403F teaching
claim without a second exercise.

## Evidence already recorded

| Family | Connected survival-client evidence | Still needed for this gate |
| --- | --- | --- |
| Arrow | [Craft, place, board, control, unload, save](evidence/0.4/survival-client-arrow.md) on a short unpowered section; [connected track crafting](evidence/0.4/survival-client-arrow-loop-connected-attempt1/README.md) from upstream ingredients; [bounded occupied powered pass and cold reopen](evidence/0.4/survival-client-arrow-powered/acceptance.md) on a survival-built 50-block original route with a full controlled turn | Full connected Arrow circuit and family teaching walkthrough; the powered pass used a disclosed upstream motor fixture |
| Legacy | [Craft, place, board, control, unload](evidence/0.4/survival-client-legacy-free-spin.md) on a short unpowered section; [bounded occupied powered pass and cold reopen](evidence/0.4/survival-client-legacy-powered/acceptance.md) on a survival-built 50-block original route; [visible free rotation on a shaped powered demo circuit](evidence/0.4/survival-client-legacy-powered/demo/acceptance.md) | Player-built shaped powered route and family teaching walkthrough; the visible-rotation demo was command-authored |
| Free Spin | [Craft, place, board, control, unload](evidence/0.4/survival-client-legacy-free-spin.md) on a short unpowered section; [bounded occupied powered magnetic pass and cold reopen](evidence/0.4/survival-client-free-spin-powered/acceptance.md) on a survival-built 50-block original route; [released-carrier damper comparison](evidence/0.4/survival-client-free-spin-powered/damper-demo/acceptance.md) on the command-authored circuit; [exact-source native regression](evidence/0.4/survival-client-free-spin-powered/damper-native/acceptance.md) | Complete player-built shaped route and family teaching walkthrough; the survival-built powered pass used a disclosed upstream motor fixture |
| Revised | [Connected survival craft, place, board, control, unload, and reopen](evidence/0.4/survival-client-revised/acceptance.md) on a short unpowered section; [occupied powered demo circuit and cold reopen](evidence/0.4/survival-client-revised/powered-demo/acceptance.md) with independent lower-hinge response | Player-built powered route and family teaching walkthrough; the powered demo was command-authored |
| PAX | [Connected survival craft, route and four-car placement](evidence/0.4/survival-client-pax/acceptance.md); [player-linked occupied cycle, unload and reopen on the survival-built train](evidence/0.4/survival-client-pax/survival-cycle/acceptance.md); [shipped-demo cycle](evidence/0.4/survival-client-pax/demo/acceptance.md) | **Accepted 2026-11-11:** [PAX walkthrough reconciliation](evidence/0.4/fd403-workflows-20261111/README.md#pax--fd403e-and-its-fd403f-walkthrough-accepted) closes E and its F claim; upstream materials/supports and creative motor are disclosed fixtures. |

The [native survival checkpoint](evidence/0.4/survival-acceptance.md) already
checks all fifteen crafting outputs, five matching placements and twenty
wrong-family rejects. The [teaching checkpoint](evidence/0.4/teaching-acceptance.md)
and [Ponder checkpoint](evidence/0.4/ponder-acceptance.md) cover item guidance,
recipe discovery and all 53 scene registrations. The [current-artifact rendered
review](evidence/0.4/teaching-current/acceptance.md) checks all fifteen tooltips
and the captions and controls of all nine unique scenes. These checks do not
replace connected-client building and operation.
The [player-crafted Arrow loop native test](evidence/0.4/survival-client-arrow-loop-native/README.md)
also passes a full eight-section powered circuit and station recapture with
ordinary survival crafting and placement through a synthetic server player.
Its upstream Chain/motor fixtures and absent connected client leave the Arrow
row's connected-client walkthrough and save/reopen work open.
In pinned Coasters 0.1.5, normal Chain Lift selection uses the client
interaction-key path (`ChainLiftAnchorOutlineClient.onUseItemKey`), which sends
`ChainLiftCommitPayload` to the server. A synthetic server-player right click or
direct `commitChainLift` call cannot certify this input path. Record actual
connected-client Chain selection, the installed span and its powered direction.

## Connected-client sequence

1. For a missing or affected case, record the source commit, packaged JAR digest, Minecraft/mod versions,
   installed mod list, world seed, game mode and any fixture commands. Use a
   retained survival world, a fresh world, or separate non-overlapping plots. Supply upstream
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

For the Free Spin damper comparison, sample the same carrier before and after
its damper span while `capture=false` and the car remains dispatched. Record
`damperImpulses`, `damperEnergy` and `damperSpeedReduction` from `/fd status`
alongside position, angle and angular speed. The audit counts only actual
damper impulses and resets with the live client; an increase before capture
separates the damper action from the boarding lock. Capture only after the
downstream sample, with enough remaining route to stop safely. A before/after
angular-speed comparison alone can also include gravity and preceding inducers,
so identify those spans and the time between samples.

The recorded teaching review above completes the common tooltip and Ponder
review. Carry it forward if the relevant resources and input paths are unchanged;
do not reopen all fifteen tooltips or all nine scenes for a new JAR hash. For
missing family walkthrough steps, use normal inventory controls and the
[family walkthrough](BUILDING.md#family-walkthroughs). Review changed captions,
controls or instructions for family matching, the ordinary cart exception,
controlled versus free motion, and PAX boarding as applicable.
The nine Ponder diagrams are illustrations; their playback alone does not prove
physical operation. Record any misleading instruction or clipped UI, correct
it if it violates a required teaching/legibility criterion, and repeat only the
affected review. Optional presentation refinements go to follow-up notes; apply
the [blocker classification](VALIDATION-POLICY.md#gate-blockers-and-follow-up-work).

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
