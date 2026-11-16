# FD1001 — release documentation preparation

**Prepared; final signoff waits for FD905 and the intended release artifact.**
This does not claim publication or close an unmet prerequisite. Art owns the
following exact transferred files; lead owns staging, integration and central
status on primary `main`.

| Required documentation | Concrete review/update |
| --- | --- |
| Installation and dependencies | README names matching client/server builds, Java21, pinned core and all nine optional addon versions/file IDs, support dependencies, client-only Immersion and the frozen artifact manifest. Additions' embedded0.2.2 versus distribution0.2.5 distinction is explicit. |
| Included families/variants | README indexes all 17 styles and their existing operating guides, including both Spinning and both Flying configurations. The guides retain actual seats, native motion, track, dock, coupling and recovery restrictions. Fixed Inverted/Suspended/Powered Inverted recipes describe ingredients rather than the changed grids, so the lead's pattern-only recipe fix does not require new guide prose. |
| Migration and backup | MIGRATION now describes schema8, Foundation sync7/editor8/PAX snapshot1, whole-world offline copying including native sublevels, copied upgrade, exact restoration and rollback from the original backup. FD903 supplies current actual two-client restoration evidence. |
| Recovery limits | RECOVERY retains certified bare ordinary bearing, original-five scope, four-car maximum,0.75blocks/s, stated drive/route/equipment restrictions and explicit operator resume. The accepted PAX repair now documents cold gravity-phase Recover, CAPTURING Resume, BOARDING restoration, and supported Repair → Realign → Recover with the unchanged eight-block bound. Obsolete current-schema6 and cumulative-open claims are corrected without erasing bounded historical evidence. |
| Attribution and integration terms | THIRD_PARTY distinguishes project-authored FD917 materials from referenced Minecraft assets and installed native/provider art. Accepted adapters replace stale “deferred” wording. No optional binaries/assets are bundled; the recorded Track Styles terms mismatch is not represented as resolved. |
| Known boundaries | README publishes ±1024 coordinates, dimension admission/storage limits, actual graphics profiles, independently authored inspired approximations, no guaranteed maximum park capacity, no automatic foreign-track conversion, and post1.0 builder/multi-train/moving-track scope. |
| Changelog | Current expanded qualification entry links the frozen input and actual results, including Mechanisms' accepted PAX repair; dated dev checkpoints retain their original limitations. No published1.0 artifact is claimed. |

Validation: local Markdown destinations checked, edited-file `git diff --check`
passed, and both new Art Python helpers compile. No build or native test is
required for prose. The comparator itself passed four real saved-state comparisons,
including original schema3→restored8 and exact logical rider/restraint preservation.

## Final dependency handoff

Mechanisms confirmed the final compile06/launch04 wording against its
[phase restarts](../mechanisms/phase-restarts.md) and
[supported track repair](../mechanisms/outbound-support/README.md). Candidate
SHA256 `8e441483af8a794ff0a4aa671d45d99f75c3c0de1138620aa7b3aaf8e82453e4`
changes only ShuttleRuntime/ShuttleWinchEntity production classes from the frozen
input and adds an opt-in lifecycle fixture. README, CHANGELOG, MIGRATION and
RECOVERY now incorporate that accepted behavior. The out-of-range straddled-spike
case remains a rejected boundary; no wider recovery range is promised.
[Mechanisms cleanup](../mechanisms/cleanup.json) records saved worlds and all
three owned units inactive. This evidence does not relabel Art's earlier frozen-JAR
migration/rendering checks as runs of the repaired candidate.

After lead closes FD905, reconcile the final selected artifact/version/checksums. Update
README/CHANGELOG's qualification status and this record, then hand FD1001 to
FD1002. Do not infer a new universal protocol from the separate packet registrars,
or rerun completed migration/render/camera cases for packaging metadata alone.

Current workspace: `/home/braydon/Documents/Coasters: Fourth Dimension`, branch
`main`, reviewed HEAD `c13e51df`; lead may advance it during integration. Art's
uncommitted files are README.md, CHANGELOG.md, docs/MIGRATION.md, docs/RECOVERY.md,
THIRD_PARTY.md, new docs/evidence/1.0/art/ and scripts/fd10-art-{migration,private}.py.
Other owners' changes are untouched. [Runtime cleanup](runtime-cleanup.txt) confirms
no Art runtime remains; there is no live runtime awaiting a handoff.
