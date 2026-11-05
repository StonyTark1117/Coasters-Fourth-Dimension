# FD401 direct stale removed-font fallback on the current worktree

**Targeted pass, 2026-11-04 local time.** A private packaged combined-profile client
removed a custom font after generation 1, performed a real resource reload, then
looked up a glyph through the closed prior `FontSet`. The current missing-font set
was returned, the chat screen rendered, and the client exited normally. The
included verifier reports `passed: true` with the ordered event lines and both
set identities in `verification.json`.

The client used the then-uncommitted PAX source changes that were subsequently
committed as `8f370d0`, plus the untracked boarding GameTest. The run and source
timing, source-file hashes, and exact packaged JAR hash are recorded in
`metadata.json`. `FontReloadTrace.java` was unchanged in that commit. This evidence
is tied to that exact worktree JAR; it does not match the selected qualification
JAR or close FD401B. The historical source-world archive and its hash are
referenced in metadata; `post-world.tar.gz` retains the client save after the
successful run. The runtime dependency manifest lists the JARs observed in the
launch log.

The decisive sequence is:

1. The client loaded eight font sets at generation 1.
2. The watcher renamed only the test pack's `transient.json` after that marker.
3. Generation 2 loaded seven sets. The direct stale lookup logged
   `missingFallback=true`, then `FD_FONT_REMOVED_STALE_PROBE passed` for
   `fd_font_probe:transient`.
4. The reload completed, chat opened, and the validation client stopped with
   exit code 0.

The run used Java 21, an isolated `xvfb-run -a` display, a 1,536 MiB game heap,
and a 4 GiB systemd process-group cap. A preceding attempt under the normal 3 GiB
cap was OOM-killed at the cap before the probe; the watcher path was also
misquoted in that attempt. The successful scope peaked at 3.8 GiB. The Linux
narrator emitted a missing `flite` library error, but the client continued through
the verified reload, chat render, and normal exit. This run does not establish
ordinary GUI retention of a removed font, Styles rendering, tracing-disabled
behavior, repeated reload/ride reliability, or the wider FD401 release gate.

Run `sha256sum -c evidence.sha256` from this directory to verify the retained
payloads.
