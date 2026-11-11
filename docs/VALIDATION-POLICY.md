# Validation, evidence reuse, and stopping rules

Effective 2026-11-05; release scheduling aligned 2026-11-11. This policy governs test scheduling for the current
0.4–1.0 roadmap. It supersedes blanket replay instructions, fixed repetition
counts, and stale “remaining/open” notes in older handoffs and evidence tables.
It does not declare an untested feature complete or waive a known defect.

**Closed cases stay closed across builds.** A build is an output, not a new
qualification campaign. To reopen a closed case, name the specific changed
behavior or new defect that invalidates its pass and the targeted check needed.
Do not reset unaffected siblings, parent acceptance, or earlier release stages.
If necessary, mark the release blocked by that specific regression while retaining
all other accepted results. A fix and its passing affected checks replace only
that case's result; they do not start
the artifact/requalification cycle again. If no accepted behavior is affected,
record carry-forward and continue implementation without another runtime run.

## Before a run

Read the current task status and [remaining-case queue](CURRENT-QUALIFICATION.md#current-work-queue)
before the qualification record's historical evidence. Claim or confirm the case's
owner and check for a run already covering it. An unrecorded owner is not proof
that no other agent is running the case.
Name the unresolved behavior or relevant change, the smallest check that answers
it, and the pass/stop condition. A sentence in the task update is enough; no new
manifest or audit document is required just to decide whether to test.
If existing evidence already answers it and the relevant inputs are unchanged,
link that evidence, close the case and stop.

Acceptance is cumulative by behavior and supported configuration. Construction,
normal input, motion, rendering, and persistence may have separate records; they
need not all be repeated in one uninterrupted session. Include an end-to-end
check where the interaction between those steps is itself unproven or changed.
Keep fixture limitations explicit: helper boarding cannot prove normal input,
and a command-built track cannot prove survival crafting.
Complete whole tasks when their criteria are covered. “Only a slice” describes
a particular observation's scope, not a permanent ceiling on task completion.
Combine compatible observations, close the covered criteria, and name only the
actual remaining gap. Do not add a new condition after every successful run.

## What invalidates evidence

| Change | Required response |
| --- | --- |
| Documentation, task/branch name, version label, unrelated source, or packaging timestamp | No runtime rerun. A new commit or whole-JAR hash alone does not invalidate behavior evidence. |
| Production code, assets, configuration, world setup, or dependency affecting the claim | Review the relevant diff and run the affected checks once. Expand only for a shared path or a demonstrated interaction risk. |
| Test-only fixture, assertion, logger, or collector | Check the changed test/measurement path. Identical production behavior can retain acceptance; do not rebuild the entire qualification matrix for a test overlay. If instrumentation changes behavior or an assertion was wrong, revisit the affected claim. |
| Client heap, scope, display, or run-policy change | Apply current launch policy to future runs. Earlier successful behavior evidence remains usable; memory/performance comparisons need comparable settings. Never rerun solely to impose a new cap retroactively. |
| Actual regression or unreliable evidence | Reopen the affected case, diagnose, fix, and run its targeted reproducer. Preserve the failure and its resolution. |

Record a short carry-forward note: evidence link, relevant changed/unchanged
paths or inputs, conclusion, and any affected tests. Whole-JAR equality and a
complete transitive hash inventory are not prerequisites for this review.
Use existing manifests where available; retain the actual identity of each run.
Do not relabel an old run as execution on a new artifact. Unknown historical
provenance remains a stated limitation; close a material uncertainty with a
targeted check rather than recreating every historical run.

A change to a shared runtime can affect several families; dependency, networking,
persistence, or renderer changes may justify broader checks. Name that connection.
“The class/JAR changed” without an affected behavior is not a full replay plan.

## Coverage without a Cartesian product

Preserve every advertised family, profile, rejection, and distinct interruption
case. Cover each family-specific mechanism with its appropriate native or client
check. Exercise a shared operator/lifecycle path on a representative family;
add linked Arrow and four-car PAX checks where their consist logic differs.
Combine these with profile startup/adapter checks and combined-profile interaction
checks. Do not multiply every family by every profile, graphics mode, input
method, and interruption unless the implementation or a failure requires it.
State why the chosen representative uses the same path.

Use native tests for mechanics, invariants, and failure injection. Use connected
clients for input routing, rendered output, real rider synchronization, and player
walkthroughs. Two-client acceptance still requires two real clients where claimed;
synthetic riders do not replace it. Shared server formatting plus representative
client output can establish telemetry coverage without manually displaying every
field for every family in every profile. Separate observations within a stable
state can establish agreement; a single screenshot containing every output is
not an additional gate.

One passing run per distinct case is the default. Repetitions need a named
intermittent defect or measurement-noise question and a finite trial count chosen
before running. There is no default three-per-graphics-mode or two-per-phase
requirement. A successful case is finished; another package may cite it directly.

## Freeze and stop

Keep the selected qualification baseline stable while collecting evidence.
Record a fix's artifact when needed; carry unaffected results forward. Do not
reselect and rebuild a baseline after every documentation or fixture commit.
FD404 records the original five-family development checkpoint. Freeze the
expanded feature set and deliverable at FD1000 (1.0 alpha), then reconcile the
evidence for FD901–FD905. FD901's frozen-profile native suite is one release-level
sweep, not a prerequisite for each task or development milestone. Retain the
original four-profile coverage and add only the agreed expansion profiles. An
already completed equivalent sweep counts; after a fix, rerun affected cases
and profiles. Repeat the full sweep only for a justified cross-cutting change.
FD502 improves blocked fixtures and measurement tools early; family tasks use
native checks for mechanics and connected clients for real interaction/rider/render
behavior. Schedule graphical runs as coordinated host work; a two-client scenario
is one job within current AGENTS resource rules. Final performance acceptance
follows the complete mechanism set, while useful measurements and foundational
fixes can happen earlier. Assets or documentation do not reset accepted physics.
FD1002 adds clean-install/package checks and reuses accepted rides when their
installation context and relevant inputs match.

Stop when the declared check passes and its evidence is recorded. Do not add
fresh construction, a second profile, another camera angle, extra passengers,
or another restart to a completed case without identifying an unresolved
advertised claim. An evidence note saying a run did not cover something is not,
by itself, a new release requirement.

After two inconclusive attempts with the same setup/input approach, stop that
approach and diagnose the fixture, targeting, logs, or tooling before another
launch. Record the precise blocker and continue independent work if possible.
Do not retry unchanged worlds or commands indefinitely. Real supported-case
failures remain release blockers; a failed fixture/setup is not automatically a
product failure, and a passing unrelated test cannot waive either uncertainty.

Raw evidence remains immutable. Current task status takes precedence over old
per-run “still open” prose. Update one current status/next-action entry rather
than appending another expanding checklist to every successful run.

## Shared-contract client handoff

New mechanics tasks that supply shared seat, carrier, restraint or pose interfaces
include a representative connected-client occupied cycle before dependent mechanics
consume them. Follow the [roadmap handoff](../ROADMAP.md#shared-contract-handoff):
native checks still cover invariants, while normal client input proves mount,
motion, capture/unload and camera restoration. Use a second real client for a new
or changed synchronization path; reuse accepted representative coverage otherwise.

This is an early use of the feature's required player evidence, not an extra run
at each handoff. Functional geometry and seats must be real; decorative polish
can follow. Later player acceptance cites the same proof and checks only remaining
claims. Do not apply this new scheduling rule retrospectively to closed packages.

## Gate blockers and follow-up work

Before holding a milestone or release open, record the affected case, the named
required criterion or supported invariant it violates, and the gate that consumes
it. Use the existing case entry; no separate audit document is required. The task
owner may close a covered case directly with evidence and update its package;
routine closure does not require another reviewer or permission round.

| Finding | Disposition |
| --- | --- |
| Missing promised functionality or failure of a required criterion: incorrect mechanics/eligibility, unsafe restraint or capture behavior, lost/duplicated riders, save corruption, broken supported installation, unreadable required guidance, or a failed declared performance budget | Block the consuming feature/gate and eventual release until resolved. A newly discovered supported-case failure still blocks even if the case must first be added under an existing requirement. Preserve other completed cases and milestones. |
| Fixture, input targeting or collector failure leaves a required claim unproven | Keep that specific claim open; diagnose after two inconclusive attempts. Distinguish an evidence/setup blocker from a product defect and continue independent work. |
| Cosmetic refinement, extra diagnostics or another observation that does not address an unmet required criterion | Record as nonblocking follow-up in the owning package, outside its required checklist. Do not reopen acceptance, rebuild a candidate or add a runtime campaign solely for this work. |

Severity labels alone do not change scope. A small visual defect that violates
required clearance, pose ownership or legibility is still a blocker; a preference
for another livery is not. No promised family, variant, capability or criterion
may be silently reclassified as optional polish. Resolve ambiguous requirements
explicitly instead of inventing a stricter criterion after a pass or waiving a
supported failure. Fixes reopen and recheck only the affected cases.

[FD916's model/texture checklist](../ROADMAP.md#fd916-review-and-improve-models-and-textures)
defines required visual-finish criteria for 0.9. Missing assets, unintended
placeholders and failures of that finite checklist block FD916/FD915 and the alpha
freeze; they cannot be dismissed as optional polish. Additional aesthetic preferences
outside those criteria remain follow-up work. Reuse earlier content reviews and
adequate captures. Texture-only edits need their affected visual checks, not a
mechanics replay; changed geometry, seats or pivots need only the affected
clearance, rider/render and mechanical checks. FD904A consumes the accepted visual
baseline without restarting the art review or broader qualification.
