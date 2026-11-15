# FD601 — milestone 0.6 reconciliation

**Complete 2026-11-15.** All eleven 0.6 execution cards are reconciled. Lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d` owns integration
on primary `main` in `/home/braydon/Documents/Coasters: Fourth Dimension`.
The [current queue](../../CURRENT-QUALIFICATION.md#current-work-queue) owns
assignments; [FD601](../../../ROADMAP.md#fd601-milestone-06-gate) owns acceptance.
This record combines family evidence and does not require another test campaign.

| Required input | Evidence and current disposition |
| --- | --- |
| FD503 | [Accepted 0.5 baseline](../0.5/acceptance.md), integrated at `d73d43b820b725fc9648c6a2daa9348dead16b22`; remains closed |
| FD1501A | [S1 mechanics and occupied handoff](spinning/README.md), integrated at `d5565a09ac0d6230b66c9dd332c7b35fb3840f2a`; complete |
| FD1501B/C | [S1 content, construction and interrupted ride](spinning/player/README.md), integrated at `0d7f6edad3c7bb51ee36e46fbb3ae0f175ac756d`; complete |
| FD1301A | [Suspended native and four-car occupied handoff](suspended/A-handoff.md); complete, integrated at `ec27e982ece3e906bf144b25cbb5982b8d230d63` |
| FD1301B/C | [Suspended content, construction and cumulative workflow](suspended/BC-acceptance.md); complete, integrated at `bfb676d4063e903e7649203b4a20143d39af9866` |
| FD1601A | [Fixed Inverted mechanics and four-car occupied handoff](fixed-inverted/A-handoff.md); complete, integrated at `7a563e6d006fff8d80221f6e7d139b42bda452a8` |
| FD1601B/C / FD1601 | [Fixed Inverted construction, content and cleanup](fixed-inverted/BC-acceptance.md); complete |

## Evidence carried forward

Original-family and Wing outcomes remain closed. S1 contributes the generalized
carrier axis, native rider loading, bounded capture and schema7 interfaces, with
normal mount, motion, capture and unload. Its construction evidence adds normal
recipe, track and car placement, dock binding, power-loss retention, restored
power without automatic motion, explicit resume and safe unload. Its native
mechanics and connected-client records cover different claims cumulatively.

The [S1 UV correction](spinning/uv-correction/README.md) closes a specific atlas
bleed defect found in its retained placement image. Explicit face UVs preserve
geometry and mechanics; one connected saved-scene visual check passed. S1 A/C
remain closed, and B is complete with that corrected texture evidence.

The lead's review of the qualified Suspended A shared changes found that original
and S1 carriers retain the rotary joint path; only Suspended uses the bounded
generic Z joint. New overhead rendering and fit are selected by Suspended's track
design, and its keel loading, consist rules and lifecycle guards are family
specific. Additive saved fields default safely for older families. No named
original/Wing/S1 regression has been found in these changes. Suspended's native
checks establish bounded Z swing, mass/inertia, lift retention and release, pending
entry, capture/save and deliberate dock rebind. Its normal four-car occupied loop
establishes mount, dispatch, locked lift, free swing, capture/open and unload.
The journal correction connects a formerly isolated block and changes native empty
mass from 4 to 5 while preserving pivot and envelope. Its targeted native check
passed rider loading, both loaded swing bounds and capture/save; corrected visual
evidence is retained. Four-car lift and occupied controls carry forward. Reassess
later relevant changes without reopening unaffected cases for a new JAR.

Fixed Inverted's focused native case passed four-car eight-block linking, exact
four-rider loading (+3.2), dispatch, dock-only unloading, saved identity/consist
and frame-loss whole-train hold. Its runtime selects a fixed chassis before the
rotary carrier path; fractional-seat ownership and loading checks are specific to
this family. Corrected hanger selection geometry passed normal cushion boarding.
The example's banked-anchor motor obstructed the required envelope; a continuous
128-block Chain from the upright approach corrected the example without weakening
clearance. Normal Close/Dispatch, four-car full inversion, banked Stop/save/reopen,
explicit Resume, dock Ready/Open and Shift unload passed. Native pose analysis
preserved one-block seat spacing and positive car-envelope separation; the record
states its temporal sampling limits. Rider assignment cleared and the camera
returned upright on unload. B/C add normal Survival crafting, anchor/track/car/station
placement, exact eight-block Chain coupling, common-dock rebind, constructed-car
boarding and restraint views. Sixteen models have bounded explicit UVs, and their
recorded hashes match the accepted placed and occupied visual inputs.

Normal Chain coupling exposed native input's seven-block cap and six-block snap,
which the earlier direct native link fixture did not exercise. Two narrow adapters
now admit eight only for Fixed Inverted/Suspended pairs passing existing family,
variant, exact-distance, readiness and maximum-four checks. Other input delegates
to native behavior. Normal Fixed Inverted Chain input created the native8.000 link
at07:07:21UTC and cleared its selection, with eligible client preview. The same
helper closes the affected Suspended coupling case; its guide now names Minecraft
Chain. No solver, seat or ride-pose behavior changed. All earlier native, occupied
and persistence evidence remains accepted; no sibling campaign was restarted.

## Runtime disposition

All S1 client, server and Xvfb units are inactive. Saved worlds and run identities
remain in its [mechanics](spinning/README.md) and [player](spinning/player/README.md)
records. Suspended completed its named continuing B/C work and saved/stopped all owned
client/server/Xvfb units; [cleanup verification](suspended/construction/cleanup.json)
records inactive/PID0 and display312 absent. Fixed Inverted's
[cleanup verification](fixed-inverted/construction/cleanup.json) records all three
units inactive/PID0, display314 absent, dimensions saved at07:10:48UTC and RCON
exited at07:10:50UTC. The lead independently confirmed those units inactive.
All saved worlds, logs and artifact identities remain in the family records.
Source ownership is returned to the lead; no live runtime or unfinished family
work is handed off. The pre-existing root `logs/` directory remains unrelated and
unstaged. No new branch or worktree was created for 0.6.

Remaining Spinning variants belong to 0.8. This milestone does not activate 0.7,
release qualification or publication.

## Gate result

S1, Suspended and Fixed Inverted each have usable vehicles, normal construction
and controls, reviewed content and accepted occupied workflows. Their distinct
interruption/persistence claims are covered by the linked records. FD601 closes
from this cumulative evidence; no aggregate test sweep was added. Final integration
contains the accepted Chain correction, guides, evidence and central status updates.
