# FD1001 — release documentation signoff

**Complete against the accepted FD905 candidate for intended stable 1.0.0.**
The [accepted candidate](../lead/release-candidate.md) is source
`c7bb65938bfbe7e7dd2feecbedbe5d7801b10a08`, candidate17 JAR SHA-256
`77170d33e8a4a6993b3d4c2fc4f70ed3a491f19f83948fa75ced752ed485b5c5`,
currently labeled `0.9.0-dev.27`. Documentation matches that accepted capability
set and its limits. FD1002 owns reproducible stable packaging, final stable
checksum and clean-install verification. No publication is claimed or authorized.
Lead owns staging, integration and central status on primary `main`.

| Required documentation | Concrete review/update |
| --- | --- |
| Installation and dependencies | README names matching client/server builds, Java21, pinned core and all nine optional addon versions/file IDs, support dependencies, client-only Immersion and the frozen artifact manifest. Additions' embedded0.2.2 versus distribution0.2.5 distinction is explicit. |
| Included families/variants | README indexes all 17 styles and their existing operating guides, including both Spinning and both Flying configurations. The guides retain actual seats, native motion, track, dock, coupling and recovery restrictions. Fixed Inverted/Suspended/Powered Inverted recipes describe ingredients rather than the changed grids, so the lead's pattern-only recipe fix does not require new guide prose. |
| Migration and backup | MIGRATION now describes schema8, Foundation sync7/editor8/PAX snapshot1, whole-world offline copying including native sublevels, copied upgrade, exact restoration and rollback from the original backup. FD903 supplies current actual two-client restoration evidence. |
| Recovery limits | RECOVERY retains certified bare ordinary bearing, original-five scope, four-car maximum,0.75blocks/s, stated drive/route/equipment restrictions and explicit operator resume. The accepted PAX repair now documents cold gravity-phase Recover, CAPTURING Resume, BOARDING restoration, and supported Repair → Realign → Recover with the unchanged eight-block bound. Obsolete current-schema6 and cumulative-open claims are corrected without erasing bounded historical evidence. |
| Attribution and integration terms | THIRD_PARTY distinguishes project-authored FD917 materials from referenced Minecraft assets and installed native/provider art. Accepted adapters replace stale “deferred” wording. No optional binaries/assets are bundled; the recorded Track Styles terms mismatch is not represented as resolved. |
| Known boundaries | README publishes ±1024 coordinates, dimension admission/storage limits, actual graphics profiles, independently authored inspired approximations, no guaranteed maximum park capacity, no automatic foreign-track conversion, and post1.0 builder/multi-train/moving-track scope. |
| Changelog | The intended1.0.0 entry records accepted qualification, Arrow/PAX/Roller Ball/Flying and recipe corrections, bounded performance changes, and the renderer's byte-equivalence/reload proof. Dated checkpoints retain their original limitations. Stable packaging remains explicit pending work. |

Validation: local Markdown destinations checked, edited-file `git diff --check`
passed, and both new Art Python helpers compile. No build or native test is
required for prose. The comparator itself passed four real saved-state comparisons,
including original schema3→restored8 and exact logical rider/restraint preservation.

## Accepted amendments and packaging handoff

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

The [final amendment record](../lead/freeze-amendments.md) also reconciles the
qualified Arrow travel governor, full-load Roller Ball lift and Flying example
endpoint clearance correction. Arrow preserves the3rad/s servo, full-turn program
and abrupt-overtravel capture/inspection guard. Native scan/endpoint reuse does
not cache geometry or safety decisions. The
[renderer repair](../lead/park/renderer-repair.md) preserves attribute bytes and
resource lifetime while reducing emission overhead.

[FD904B](../lead/park/README.md) accepts all17 mounted views, runtime14's
1800-second sustained workload/memory plateau and candidate17's affected base
and combined comparisons. Combined17 tick p95=50.088835ms passes the existing
later53.871642ms ceiling; it is not represented as an initial<=50ms pass. The
original classifier failure and complete-data reconciliation remain preserved;
no threshold was raised. Earlier Art evidence retains its actual frozen-JAR
identity. Final park cleanup is linked from FD905; all owned runtimes are stopped.

README and CHANGELOG now identify the accepted qualification source and intended
1.0.0, distinguish its dev.27 candidate from the pending stable artifact, and link
the completed gates. FD1001 is handed to FD1002. Record the stable checksum after
reproducible packaging and verify the documented clean client/server install.
Do not infer a universal protocol from the separate packet registrars or rerun
completed migration/render/camera cases for packaging metadata alone.

Current workspace: `/home/braydon/Documents/Coasters: Fourth Dimension`, branch
`main`, reviewed HEAD `c7bb6593`; lead may advance it during integration. Earlier
Art evidence and docs are integrated. This final handoff changes only README.md,
CHANGELOG.md and docs/evidence/1.0/art/release-documentation.md, uncommitted for
lead integration. Other owners' changes are untouched. [Runtime cleanup](runtime-cleanup.txt) confirms
no Art runtime remains; there is no live runtime awaiting a handoff.
