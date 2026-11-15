# FD601 — milestone 0.6 reconciliation

**In progress.** Lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d` owns integration
on primary `main` in `/home/braydon/Documents/Coasters: Fourth Dimension`.
The [current queue](../../CURRENT-QUALIFICATION.md#current-work-queue) owns
assignments; [FD601](../../../ROADMAP.md#fd601-milestone-06-gate) owns acceptance.
This record combines family evidence and does not require another test campaign.

| Required input | Evidence and current disposition |
| --- | --- |
| FD503 | [Accepted 0.5 baseline](../0.5/acceptance.md), integrated at `d73d43b820b725fc9648c6a2daa9348dead16b22`; remains closed |
| FD1501A | [S1 mechanics and occupied handoff](spinning/README.md), integrated at `d5565a09ac0d6230b66c9dd332c7b35fb3840f2a`; complete |
| FD1501B/C | [S1 content, construction and interrupted ride](spinning/player/README.md), integrated at `0d7f6edad3c7bb51ee36e46fbb3ae0f175ac756d`; complete |
| FD1301A | [Suspended native and four-car occupied handoff](suspended/A-handoff.md); complete, integrated with this record |
| FD1301B/C | [Suspended content, construction and cumulative workflow](suspended/BC-acceptance.md); complete |
| FD1601A–C / FD1601 | [Fixed Inverted preparation](fixed-inverted/README.md); consumes the integrated Suspended handoff and transferred shared paths |

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

## Runtime disposition

All S1 client, server and Xvfb units are inactive. Saved worlds and run identities
remain in its [mechanics](spinning/README.md) and [player](spinning/player/README.md)
records. Suspended completed its named continuing B/C work and saved/stopped all owned
client/server/Xvfb units; [cleanup verification](suspended/construction/cleanup.json)
records inactive/PID0 and display312 absent. Fixed Inverted received source
ownership and reserved its own runtime; it must complete and verify cleanup
before FD601 closes. The pre-existing root `logs/` directory remains unrelated and
unstaged. No new branch or worktree was created for 0.6.

Remaining Spinning variants belong to 0.8. This milestone does not activate 0.7,
release qualification or publication.
