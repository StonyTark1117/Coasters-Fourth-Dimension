# Preserve native exports between profile runs

`runGameTestServer` deletes its previous world and five frame/trace exports before
starting. The historical profile runner archived logs and worlds but omitted
those exports. Use [capture-native-exports.py](../scripts/capture-native-exports.py)
for future runs, **after observing process exit and before starting another
profile**. This helper launches no game, JVM, Gradle process or network request;
it reads existing exports and creates a new evidence directory.

Do not rewrite historical bundles or claim this helper retroactively captures
their export-time hashes. A late capture of old files remains late evidence.

## Runner hook

The profile runner must record these fields in `metadata.json` before invoking
the helper: full 40-character `source`, `profile` (`base`, `styles`, `engineered`
or `combined`), timezone-qualified ISO `start_utc` and `end_utc`, and integer
`exit_code` from the completed process. Write the end time immediately after
the actual wait returns. A log line or timeout is not process completion.

Resolve the effective Gradle build directory; on Unix a checkout containing a
colon relocates outputs under `/tmp/fourthdimension-<path-hash>/build`. Let
`fd_build_dir` name that directory and `fd_profile_dir` name this run's unique
archive directory. Invoke:

```sh
python3 scripts/capture-native-exports.py \
  --run-dir "$fd_build_dir/runs/gametest" \
  --metadata "$fd_profile_dir/metadata.json" \
  --output "$fd_profile_dir/native-exports"
```

The output must not already exist and must be outside the reusable runtime
directory. Call this before lengthy packaging and before any subsequent Gradle
run. Keep the runner's original logs, JAR/dependency/source identity, copied world
and actual exit status. Never replace a failed runtime exit status with the
helper's successful status. If the runtime failed, still capture its available
exports; they are useful failure evidence.

The helper preserves exactly these outputs as deterministic gzip payloads:

- `bank-native-frames.json`
- `recovery-bank-native-frames.json`
- `recovery-bank-train-native-frames.json`
- `recovery-bank-mesh-trace.jsonl`
- `recovery-bank-train-mesh-trace.jsonl`

`manifest.json` records raw/compressed SHA-256 hashes, byte counts, source mtime,
metadata/tool hashes, capture timestamps and per-file issues. Each compressed
copy is reread and verified. Streaming keeps memory bounded for large traces.
Source size/inode/mtime/ctime changes during copying invalidate the capture;
missing, empty, symlinked, unreadable and out-of-run-interval files are also
reported. Available bytes remain preserved even when another file fails.

Exit **0** means all five exports were captured without those issues; **1** means
an incomplete/invalid capture with a diagnostic manifest; **2** means setup or
metadata was refused. An interrupted helper may leave an incomplete directory;
do not overwrite it or treat missing `manifest.json` as success. Preserve it and
use a new destination only after checking why capture stopped.

`candidate_for_separate_acceptance_review` requires complete capture and a
reported process exit of zero. Even that flag does not certify passing tests,
valid geometry, source equivalence or a release gate. Inspect logs and run the
appropriate auditors separately. The helper trusts the runner's source/exit
metadata; it cannot independently establish process ownership or prove that a
file was never changed before capture. It does not parse or modify physics data.

## Validation

Run without a JVM:

```sh
python3 -B -m unittest discover -s scripts/tests -p test_capture_native_exports.py -v
```

The seven regressions verify byte-for-byte archive round trips and deterministic
gzip output; missing/stale export refusal; an actual input append during copying;
failed-process separation; empty/symlink refusal; overwrite/runtime-directory
protection; and rejection of incomplete process metadata. Historical evidence
and production mechanics are untouched.

On 2026-11-02, the helper also captured the five retained `abc4a9a` Styles
exports: 175,887,260 raw bytes became 33,895,800 compressed bytes, with no reported
capture issues. The ignored validation copy is under
`build/evidence-capture-check/retained-styles` in the validation checkout. This
tests real trace size and archive integrity; it is a late capture, not a new
runtime or a repair of the original missing export-time hashes.


## Same-route decoration inputs

`RecoveryRouteFrames` now retains the original rail-frame grid and adds
`decorationInputs` schema 1 to each section in the single-car and train recovery
frame files. The exporter samples native position/up/forward/right directly at
`(i + .5) * length / ceil(2 * length)` for each spine and every two blocks for
ties, including the renderer's `1e-6` endpoint tolerance and clamp. It does not
interpolate the original quarter-block grid.

Each piece includes distance, native parameter, profile angle, scale and a
column-major model-to-world matrix. The matrix uses the same common
`ModelFrame` and `ArrowDecorationPlacement` methods as the renderer, including
float step rounding, vertical offset and the tie's separate model origin. Native
`right` is retained for inspection; the model's right axis is derived from
`up × forward`, as in the renderer. Each section also records its family,
variant, full profile keys, appearance/tint, validity, revision and decimal-string
geometry hash. Saved rail and resolved edge endpoints plus `canonical` expose
their direction. Historical `profile`/`profileAngle` fields keep their existing
saved-profile-at-edge-distance meaning. New `renderProfile` keys and
`renderProfileAngle` values apply the same conditional reversal as
`OriginalTrackRenderer`; use those for control-rail/support reconstruction.
A reversed profile reverses key order and negates slopes. Route chassis IDs allow pairing with the existing body/pose
trace; dimension and export game time record the observation context.

These are **potential renderer inputs**, exported even when the appearance is
Original or its optional models are unavailable. This does not activate Track
Styles, change appearance, prove model availability or show that anything was
rendered. The existing recovery trace remains a tick sample, not continuous
swept clearance or an occupied full-turn ride.

After the coordinator builds and runs the fixture, capture both files from that
same run using the helper above. On the uncompressed archived copies, run:

```sh
python3 scripts/verify-recovery-decoration-inputs.py "$fd_frames" \
  --mesh-trace "$fd_mesh_trace"
```

Pair `recovery-bank-native-frames.json` with `recovery-bank-mesh-trace.jsonl`, and
`recovery-bank-train-native-frames.json` with `recovery-bank-train-mesh-trace.jsonl`.
The verifier checks complete placement grids, matching design/variant/persisted-family
identity, recorded profile metadata, finite original and decoration frames,
original-grid distances/profile values, endpoint direction and renderer-profile
reversal, expected piece scales and model transforms, with `2e-4` numerical
matrix tolerance for float arithmetic. This tolerance is not a clearance margin.
The optional trace check requires matching chassis definitions/poses and completion
markers for each route. It does not assess collisions, rider meshes, trace timing,
gameplay success or provenance beyond those recorded IDs. A completion marker is
not a passing test. Keep the actual run logs and capture manifest.

The report hashes the input and, when supplied, trace. Its source hashes describe
the locally inspected sources, not proof of which classes produced the file.
Use the run's separately recorded source/JAR identity to establish that link.
Run the verifier from the matching source revision. Old exports intentionally
fail the new schema check; do not fill missing native samples with synthetic or
interpolated data to make them pass.

Offline validation (no JVM):

```sh
python3 -B -m unittest discover -s scripts/tests -p test_recovery_decoration_inputs.py -v
```

Twelve synthetic tests cover a non-grid section length, a quarter-turn bank,
endpoint tolerance/clamp, legacy rejection, missing/wrong sample distances,
wrong tie origin/nonfinite matrices, corrupt profile/identity, duplicate rails,
mismatched/incomplete traces, wrong persisted-family/design combinations, and
nonfinite/corrupted original frames, and reversed-edge angle/slope/direction
consistency. They validate the verifier, not native sampling.
The Java export extension still needs the coordinator's build and native run;
no new clearance or runtime acceptance is claimed by this tooling change.


## Prospective Styles recovery geometry audit

After fresh native exports pass the checks above, use
[audit-recovery-styles.py](../scripts/audit-recovery-styles.py) to evaluate a
**proposed** default Styles configuration at those recorded Arrow recovery poses.
This does not prove the client selected or rendered the configuration. Keep its
report separate from observed appearance/operator acceptance.

```sh
python3 scripts/audit-recovery-styles.py \
  "$fd_coasters_jar" "$fd_minecraft_resources_jar" "$fd_styles_jar" \
  "$fd_frames" "$fd_mesh_trace" --style arrow_4d
```

Repeat for `--style arrow_4d_dbl_spine`, and for both single-car and train exports.
Use uncompressed archived copies of the paired frame/trace files. For a proposed
transition layout, replace `--style` with `--style-plan path/to/plan.json`; that
JSON object must map every Arrow rail UUID to `original`, `arrow_4d` or
`arrow_4d_dbl_spine`. At least one section must be styled. Unknown styles,
non-Arrow geometry and styled profiles above the supported upper stroke are
rejected; explicitly select Original where fallback is intended.

The tool chains the exact-input verifier, trace identity check and existing
five-section native-route verifier. Do not run with Python `-O`, because the
route verifier uses assertions. It then reconstructs the proposed surface:

- Preserve running/control rails and native running supports.
- Remove the Original spine only on styled sections.
- Move those sections' control-mount attachments to the renderer's lower spine
  point, using `renderProfileAngle` for the control rail.
- Include the renderer's extra crossbrace and both endpoint spine-step bridges.
- Apply each recorded model-to-world matrix once to the pinned provider's raw
  OBJ positions, without applying the tie origin or spine scale a second time.

The existing recovery mesh audit accepts this explicit surface through an
optional argument; its default Original path is unchanged. The same audit
checks recorded chassis/carrier/linkage blocks, lower bearing, full-spin wheel
envelopes and mounted native-rider volumes, including neighboring cars and the
ordinary recovery body. Intended running-rail contact remains excluded only for
wheels. The sample-count, continuous tick sequence, five-section travel,
completion-marker, passenger and body-identity checks remain required. A zero
separating bound remains uncertified; no thresholds are relaxed.

Source and dependency hashes guard reconstruction. The new wrapper explicitly
reviews the `8a7ffdb` fixture change: its only difference from the older mesh
verifier's pinned `BankedRecoveryExamples` is the call that copies the native
curve into `rail.originalCurve`. This adds recovery provenance without changing
fixture geometry. Both current auditors now pin that reviewed provenance source.
The [post-fallback pin review](evidence/0.9/post-fallback-geometry-pin-review-2026-11-03.json)
also reconciles renderer corrections `21bfd1a` / `d5132a3`: normal Original
and Styles geometry-building methods remain byte-identical to `ff7c831`; new
code handles failed provider draws and quarantine/retry. The offline Styles
audit does not model the partial failure frame. Historical reports keep their
original source/tool/input hashes and do not become post-fix runtime evidence.
Any later source change requires reinspection, not merely replacing a hash to
obtain a pass.

Current auditors hash inputs, dependencies, source and tool before analysis, then
require those hashes to match again before emitting a result. A changed or removed
file refuses certification rather than relabeling completed calculations with its
new bytes. Custom style-plan files are included. This boundary check is not a
filesystem snapshot: still use immutable source and input copies to exclude a file
being changed and restored between checks. Earlier reports retain their actual
provenance limitations; this guard does not retroactively validate them.

Reports include the saved appearance separately from the proposed plan, component
counts, model/body resource hashes, input/source/tool hashes and NumPy version.
Only aggregate measurements and identities are written; third-party model data
stays in memory. Use the project's geometry dependency environment; the offline
unit checks here ran with NumPy 2.5.4 and do not constitute native acceptance.

Six additional synthetic tests cover raw OBJ indexing, a once-applied tie origin,
style ownership and lowered supports, Original/double-spine transitions, invalid
plans/stroke/family rejection and contained-triangle rejection. Run both suites:

```sh
python3 -B -m unittest discover -s scripts/tests -p 'test_recovery_*.py' -v
```

This remains tick-sampled body-versus-track default-model analysis. It does not
check track-to-decoration self-clearance or establish continuous physics-step clearance, player meshes/cosmetics, resource replacements, visible
client rendering, installed model availability or occupied full-turn operation.
Fresh exported inputs and an actual audit run are still pending; passing synthetic
tests or parsing the four pinned OBJ resources is not a clearance pass.
