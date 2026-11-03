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
geometry hash. Route chassis IDs allow pairing with the existing body/pose
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
The verifier checks complete placement grids, recorded identity/profile metadata,
finite frames, expected piece scales and model transforms, with `2e-4` numerical
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

Nine synthetic tests cover a non-grid section length, a quarter-turn bank,
endpoint tolerance/clamp, legacy rejection, missing/wrong sample distances,
wrong tie origin/nonfinite matrices, corrupt profile/identity, duplicate rails,
and mismatched/incomplete traces. They validate the verifier, not native sampling.
The Java export extension still needs the coordinator's build and native run;
no new clearance or runtime acceptance is claimed by this tooling change.
