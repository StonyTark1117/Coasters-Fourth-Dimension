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
