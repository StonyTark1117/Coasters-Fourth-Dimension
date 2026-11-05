# FD402 two-client occupied recovery lifecycle checkpoint

This bounded dedicated-server checkpoint proves one complete occupied Arrow
recovery lifecycle with two independent rendered clients. It closes the
multiplayer recovery-lifecycle item for this supported scene; it is not an
all-family multiplayer matrix or a manual operator-input claim.

## Result

The server admitted `Observer` and `Dev` into two distinct seats on the same
original Arrow car. It attached the retained, restraints-closed car to one
ordinary Coasters bearing, started the native chain, moved and braked the
occupied group, disconnected the bearing, opened restraints, and unloaded both
players. The final server marker is:

```text
FD_CARRIER_FIXTURE completed occupied recovery cycle ... detached=true restraintsOpen=true ridersUnloaded=2 riders=[Observer, Dev]
```

Both clients independently passed after observing the rendered original car,
native ordinary bearing, live recovery link, native motion, normal release and
their own final dismount. Each recorded `recoveryLinkSeen=true`,
`recoveryMotionSeen=true`, `recoveryCycleSeen=true`, `riderMounted=false`,
`localRiderRenderCalls=true`, and more than 5.7 blocks of motion for both linked
bodies. They exited normally without an out-of-memory event.

After the clients exited, the dedicated server shut down through its normal
save path. `final-save.json` confirms schema 6, the original Arrow chassis and
two carriers, no occupants or rider-seat assignments, open restraints, retained
maintenance state, and no recovery link.

## Isolation and memory

This run used private Xvfb displays `:223` and `:224`, separate client game
directories, a separate dedicated-server directory/world, and port `25591`.
The displays and all three systemd scopes belonged only to this task and were
stopped after preservation.

Each client had a 1024 MiB Java heap inside a 3 GiB systemd hard cap. Their
recorded scope peaks were 2.4 GiB and 2.5 GiB. The server used a 1024 MiB Java
heap inside a 2.5 GiB hard cap and peaked at 1.8 GiB. `limits.txt` preserves the
live hard-limit snapshot; `memory.txt` preserves the scope commands and terminal
peak records.

## Reproduction

Start an isolated dedicated server with the recovery cycle and two-rider
fixture:

```sh
./gradlew --offline --no-daemon runServer \
  -PfdServerGameDirectory=/absolute/private/server \
  -PfdServerMaxHeap=1024M \
  -PfdCarrierFixture -PfdCarrierFixtureRecovery \
  -PfdCarrierFixtureRecoveryCycle \
  -PfdCarrierFixtureRecoveryDesign=ARROW \
  -PfdCarrierFixtureRiders=2
```

Connect one `runClient` and one `runObserver`, each on its own private display
and game directory, with:

```text
-PfdClientMaxHeap=1024M -PfdConnect=127.0.0.1:25591
-PfdCarrierValidation -PfdCarrierValidationExit
-PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion
-PfdCarrierValidationRecoveryCycle -PfdCarrierValidationRider
-PfdCarrierValidationRender -PfdCarrierFixtureRecoveryDesign=ARROW
-PfdCarrierValidationSamples=20 -PfdCarrierValidationTimeout=2400
```

The fixture now treats the configured rider count as the acceptance boundary:
it resolves every admitted rider at release, unloads all of them, and rejects
completion if any passenger or saved rider-seat assignment remains. The shared
run configuration also forwards recovery fixture/validation properties to the
dedicated server and observer, while the new game-directory properties keep
their runtime state isolated.

Raw server and client logs are preserved as `server.log.gz`, `dev.log.gz`, and
`observer.log.gz`. Run `python3 verify.py` for a launch-free replay of the
required markers and final save. `SHA256SUMS` covers every evidence artifact
except itself.

## Scope

This evidence establishes a two-player Arrow lifecycle on one dedicated server:
occupied attach, tow, brake, disconnect, restraints open and both riders
unloaded, with both clients observing the rendered transition. All-family
multiplayer coverage, fixture-free manual operation, resource-reload occupancy,
and unrelated rider-equipment/resource-pack rendering remain separate gates.
