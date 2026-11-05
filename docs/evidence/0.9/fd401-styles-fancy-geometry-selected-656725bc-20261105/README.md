# FD401B selected-JAR FANCY track geometry observation

This run adds a bounded geometry observation for the exact selected clean
`ec2fcda` artifact, SHA-256
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
It used the FANCY validation route world from the third selected-JAR FANCY
reload/ride repetition. The original saved input world is preserved as
`input-world.tar.gz`; the post-run save is `final-world.tar.gz`.

The client ran on private Xvfb display `:268` with Java 21, `-Xmx768M`, and a
3 GiB systemd scope. Graphics mode was FANCY, render distance 4, and Flywheel
reported `flywheel:indirect` enabled. Track Styles 1.2.0 loaded and logged that
it inspected 55 decorative model entries. The render validator sampled 2,509
nonempty geometry stages, 20,067 in-range rails, 71,941,528 in-range quads,
and 102,828 in-range decoration pieces before the world was saved. The
visible-track screenshot shows the styled route during the same stable scene;
it was taken before the final metric sample, so the later totals are from the
client log rather than inferred from the screenshot.

The client saved all dimensions, exited normally, and its 3 GiB scope reported
success. The earlier attempt at a 3 GiB cap while activating the Iris shader
had been OOM-killed; after a fresh memory check, that separate shader test was
retried at 4 GiB. This geometry run used the required 3 GiB cap and completed.

This establishes static styled-track geometry submission in FANCY on this
selected artifact. It does not establish geometry across dynamic motion, all
profiles or styles, provider removal/reinstallation, or the full FD401B gate.

`clientRunClasspath.txt`, `clientRunProgramArgs.txt`, `clientRunVmArgs.txt`,
`options.txt`, `run-client.sh`, and `dependencies.sha256` preserve launch and
runtime details. Screenshots and full `latest.log` and console output are
included. The JAR hash and dependency list match the adjacent selected-JAR
FANCY evidence bundle.
