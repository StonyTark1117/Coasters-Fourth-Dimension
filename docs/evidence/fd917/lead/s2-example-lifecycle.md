# Delayed S2 example binding

Mechanisms' remote S2 example exposed a stale block-entity reference after the
governor chunk unloaded and reloaded. The delayed bind wrote the old object,
while authority resolved the live unbound governor; its rejection escaped the
level tick and stopped the server.

`S2SpinningExamples` now resolves the live governor at its saved position and
retries ordinary readiness rejections within the existing 400-tick deadline.
Already completed native couplings survive a partial retry. Power, empty/stopped
dock and authority guards remain in place.

One Java21 native test recreated the governor, held real motor power at zero,
confirmed the live governor received the pending bind while authority remained
unclaimed, then restored power and observed successful stopped/empty binding.
The [run log](s2-example-lifecycle.txt) records all one required test passed and
BUILD SUCCESSFUL. Namespace `fourthdimension_fd917_examples`, command
`runGameTestServer`, persistent build directory
`/mnt/NVME4/fourthdimension-runs/fd917-lead-20261115/build`. Server saved and exited.
This fixes example setup only; accepted S2 ride mechanics are carried forward.
