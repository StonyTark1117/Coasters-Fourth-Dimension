# FD403A independent occupied circuit — 2026-11-13

Owner: concurrent FD403A contributor `01a1d49d-7763-76b2-8b0c-6f5b337f0ea2`.
This record supplements the [FD403A circuit handoff](../../../FD403A-CIRCUIT.md)
and the separately owned normal C–D construction pass. It does not take over
the package assignment or claim that this copied world earned construction
credit for its command-extended span.

## Run and source

The run used private Xvfb `:254`, an 854 × 480 connected client, and a dedicated
server on `127.0.0.1:25604` with RCON on `25605`. Runtime root:
`/home/braydon/.local/state/fourthdimension-runs/fd403a-independent-circuit-20261113`.
Minecraft 1.21.1 / NeoForge 21.1.248 ran on Java 21.0.13. The packaged
`0.9.0-dev.27` JAR has SHA-256
`e5f65c76289a018b64c56c1d8f845fe3b09e920bac93909d783a4b8373fb3972`, matching
the package owner's normal C–D run. The client used a 1536 MiB heap in an owned
6 GiB scope.

`FD403ArrowRetained` was copied from FD502's stopped save after verifying its
three services were inactive. The copy operation and source are recorded in
[`handoff-copy.json`](/home/braydon/.local/state/fourthdimension-runs/fd403a-independent-circuit-20261113/evidence/handoff-copy.json).
The earlier exploratory world in the same run root is separate and has no
acceptance credit.

## Connected occupied circuit

In the retained world, Dev boarded the intended Arrow seat through normal
right-click. The server logged an accepted seat assignment. The controller
read `Ready | Restraints open`; normal Close restraints changed it to
`Ready | Restraints closed`, which was read back before one normal Dispatch.
The on-screen ride was occupied and the player remained connected during the
route samples.

Read-only position samples follow the loop from the A–B straight through the
A–I–H–G side, across F–G and E–D, along D–C, around C–B and back toward A. The
first return crossed the original A–B start point between 17:11:09 and 17:11:14
UTC. A second return crossed it between 17:14:18 and 17:14:23 UTC. Samples,
timestamps and all RCON results are in the run's `evidence/console.jsonl`; the
single dispatch and timestamped before/after screenshots are in
`evidence/input.jsonl`.

After the second return, normal Capture stopped the occupied car. The controller
reopened to `Ready | Restraints closed`; normal Open restraints changed it to
`Ready | Restraints open`; normal Shift dismounted Dev and RCON reported
`RootVehicle` absent. Screenshots are `20261113T171453.887144Z-after.png`,
`20261113T171537.103588Z-after.png`, and `20261113T171609.870514Z-after.png`.

## Cold reopen

The client disconnected normally. The server completed `save-all flush`, saved
all dimensions and Sable sublevels, and stopped cleanly. The same world then
cold-started; Dev rejoined at the saved pose. F3 identified a reachable
`fourthdimension:construction_controller`, and its reopened editor read
`Ready | Restraints open`. The player had no `RootVehicle` after rejoin. After
the check, the client exited normally and the server was saved and stopped
cleanly again.

[`cold-reopen-loaded-readback.json`](/home/braydon/.local/state/fourthdimension-runs/fd403a-independent-circuit-20261113/evidence/cold-reopen-loaded-readback.json)
records the post-restart A–B 50 m span, C–D coverage/speed, F–G 26.3467 m span,
I–H 14.0841 m span, and the restored vehicle and carrier UUIDs. The intended
chassis `819ce77e-8cd5-4eaf-831a-c45de09908cb` and carriers
`62191fac-a0ae-441f-b94a-b1c9f7dd1109` / `b86810c6-bd7e-4efc-8e20-46bed47ecba2`
were present after restart. The persisted controller screenshot is
`20261113T172336.321682Z-after.png`.

## Claim boundary and teaching reconciliation

The copied C–D Chain span was extended from 14 m to 16 m by FD502 block-data
setup; its powered direction is `+32` with travel sign `+1`. Its completed
motion in this ride is therefore route evidence, not normal player installation
evidence. The package owner's separate connected run is the source for normal
C–D Chain placement and its route effect; combine that proof with this occupied
loop, capture/unload and cold-reopen record.

The accepted [current-artifact teaching review](../teaching-current/acceptance.md)
already rendered the Arrow full-turn and changed captions. This circuit showed
no conflict with the captions about controlled profiles, capture, restraints,
dispatch or the return cycle. The accepted 50 m powered Arrow record continues
to provide the nonzero full-turn observation; this circuit does not add a new
full-turn claim. No second walkthrough run is required for FD403F.

One autosave caused a client timeout before gameplay began; the same world was
reconnected and the completed circuit was run afterward. During final saves the
server reported tick delays, but it saved all dimensions and exited cleanly.
