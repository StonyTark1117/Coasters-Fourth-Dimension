# FD401B FAST Styles reload and partial occupied ride

This bundle preserves a selected-JAR FAST-mode checkpoint on the committed
`ec2fcda` artifact. The packaged JAR SHA-256 is
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
Its classpath and generated VM argument files are byte-identical to the
companion [FABULOUS run](../fd401-styles-fabulous-validation-reload-ride-ec2fcda-20261105/README.md);
the 109-entry dependency hash manifest is retained here as well.

The game options record FAST graphics (`graphicsMode:0`) and render and
simulation distance 4. F3+T completed and Track Styles inspected all 55
decorative model entries. A disclosed `/fd validation board_locked Dev`
helper mounted Dev on the Arrow seat, and the Rotation Tool UI was used for
the capture/restraint setup. The original powered station immediately returned
the UI to Ready after Dispatch, so the ride attempt cleared its station
redstone in the scratch world. Later `/fd validation motion Dev` samples show
the occupied car moving at approximately 2 blocks/s with `capture=false`.

The client was saved and quit with Dev still riding. No station return,
recapture, restraint release, unload, or post-ride rider cleanup was verified.
This is a **partial FAST reload and occupied-motion result**, not a completed
reload/ride/capture/unload repetition. It must not be counted as one of the
completed FD401B ride repetitions. The incomplete return was preserved so a
future run can target capture/unload specifically if the mode matrix still
needs it; do not repeat the setup and outbound motion without that distinct
purpose.

The direct Java script used Java 21 with `-Xmx768M`; the client ran in an
owned `MemoryMax=3G` scope and private Xvfb `:252`. `console.log.gz` and
`client.log` retain the selected run, `final-world.tar.gz` retains its stopped
world, and `screenshots/` preserves the reload, boarding, occupied UI, and
motion samples. The supplied input-world copy was named `FD401StopCycle`; a
pre-run world archive/hash was not retained in this bundle.

## Bundle files

- `clientRunClasspath.txt`, `clientRunVmArgs.txt`,
  `clientRunProgramArgs.txt`, `run-client.sh`, and `start-client.sh` retain
  the launch identity and scope command.
- `dependencies.sha256` is the dependency manifest for the byte-identical
  classpath and VM argument files in the linked FABULOUS run.
- `bundle-artifacts.sha256` hashes this bundle's JAR, saved world, logs, and
  launch script. `SHA256SUMS` covers every file in this directory except
  itself.
- The logs show the live movement samples and clean save/quit. The screenshots
  are supporting observations; no capture or unload claim is made.
