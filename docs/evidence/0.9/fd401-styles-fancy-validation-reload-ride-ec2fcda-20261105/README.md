# FD401B FANCY validation reload and occupied ride

This is one additional FANCY-mode run using the selected clean `ec2fcda`
artifact, JAR SHA-256
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
The initial isolated client loaded the saved `FD401StopCycle` world and a
real F3+T reload logged Track Styles inspecting all 55 decorative model
entries with validation disabled. After saving and exiting, the same selected
JAR and world were relaunched with `-Dfourthdimension.validation=true`; a
second F3+T reload while occupied logged the same 55 entries. The full logs,
launch arguments, classpath and dependency hashes are retained here.

Normal empty-hand seat interaction did not board in this repeat. The disclosed
`/fd validation board_locked Dev` helper then succeeded and logged
`FD_BOARDING_ASSIGN ... accepted=true`. The validation client trace after the
occupied F3+T shows `riding=true` and a non-empty rider UUID on the Arrow
carrier. The single saved station block at `-356 210 -179` was removed to
start the lap and restored while the vehicle was moving. Validation motion
samples changed from approximately `(-349.5, 211.1, -162.4)` to
`(-324.6, 211.0, -147.1)` and `(-317.5, 211.1, -164.9)` with capture false;
on return near `(-349.5, 211.1, -184.1)`, speed was below 0.001 blocks/s and
capture was true. The Rotation Tool UI then showed
`arrow_controlled | Ready | Restraints closed`. Restraints were opened through
the UI, Dev dismounted with Shift, and `/data get entity Dev RootVehicle`
returned no elements. The scratch world was saved and the client exited with
`Result=success`; the private Xvfb display was stopped.

The lap began on the station redstone falling edge. A UI Dispatch attempt
while the signal was held high was not part of this validation-flagged repeat,
and no sustained UI dispatch is claimed. This is one FANCY repeat, not the
three-run mode matrix; normal seat boarding remains open under FD401C. The
validation system property was enabled only to expose the disclosed local test
command, and is recorded in `clientRunVmArgs.txt`. The 3 GiB client scope and
Java 21 launcher were used; heap was capped at 768 MiB. `final-world.tar.gz`
preserves the saved scratch world.
