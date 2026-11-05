# FD401B tracing-disabled Styles reload and occupied ride

This run used the clean Styles-only client built from source commit `ec2fcda`.
The packaged mod JAR SHA-256 was
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
The run had no FD validation, render validation, snapshot tracing, or ride
fixture arguments enabled. The client log records Track Styles inspecting 55
decorative model entries after a real F3+T reload.

After reload, I boarded the existing Arrow carrier with an empty hand through
the normal seat interaction, used the Rotation Tool UI to capture it and close
restraints, and clicked Dispatch. The save's rear station signal was still
powered, so that UI dispatch was immediately recaptured. Following the FD401
owner's diagnosis, I located the single redstone block powering station
`-356 210 -180` at `-356 210 -179`. Removing it in this scratch world started
the occupied lap; restoring it powered the station and the car returned to
`Ready | Restraints closed`. I then opened restraints, dismounted with an
empty hand, and confirmed `/data get entity Dev RootVehicle` returned no
elements. The world was saved through the menu and the client exited cleanly.

This is bounded evidence for a tracing-disabled FANCY reload, normal seat
boarding, visible occupied motion, station capture, and unload. The lap began
on the station's redstone falling edge. The UI dispatch attempt while the
signal was held high did not produce a sustained lap; this bundle therefore
does **not** claim a sustained normal UI dispatch. It also covers only one
FANCY run and does not close the broader FD401B reload/ride repetition or
render-mode matrix.

The screenshots document startup, post-reload rendering, occupied UI,
dispatch attempt, signal transition, visible lap, captured-ready state,
restraint opening, unload, and clean title-screen exit. `client.log.gz`
contains the full game log; `console.log.gz` contains the client launch log.
`final-world.tar.gz` preserves the saved scratch world. The three
`clientRun*.txt` files and `run-client.sh` record the launch arguments. The
client ran on Java 21 in private Xvfb display `:245`, with `-Xmx768M` and an
enforced 3 GiB systemd scope; the scope exited successfully. The selected JAR
hash was unchanged after the run.

The client and Xvfb are stopped. This evidence does not modify shared
qualification summaries or close FD401 as a whole.
