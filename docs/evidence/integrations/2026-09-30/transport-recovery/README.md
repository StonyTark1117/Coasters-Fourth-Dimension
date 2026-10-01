# Sable transport and native carrier tracking recovery

This investigation follows the transient missing-sublevel movement messages in
the revised/PAX two-client run. One packaged client and four sequential packaged
dedicated servers used the same pinned combined pack and production JAR, with
separate complete test-world copies. No production code or default networking
configuration changed.

| Case | Missing-body messages | Native carriers restored | Matched 4D snapshots |
| --- | ---: | ---: | ---: |
| UDP, six range departures/returns | 0 | 16 on each return | 138 |
| TCP, six range departures/returns | 0 | 16 on each return | 121 |
| TCP, occupied frozen cold restart | 0 | 16 | 13 |
| UDP, identical occupied cold restart | 23 in one second | 16 after the burst | 55 |

Both cold restarts loaded exactly the same occupied foundation save before ticking.
The revised ZacSpin rider reattached to the original saved carrier/seat in each
case; native camera samples remained finite and matched the carrier pose. The UDP
case subsequently allowed normal restraint opening and dismount, with empty saved
rider maps and restored unmounted camera state. No persistent missing body or ride
fault was observed. Exact process exits, JAR/log hashes and mod lists are retained.

## Interpretation and tested fallback

The result supports a cross-transport ordering explanation, rather than persistent
loss of the saved car. Inspection of the pinned Sable 2.0.3 sources found:

- `SubLevelTrackingSystem.sendFullSync` sends body creation and plot chunks in a
  TCP bundle. `sendMovementUpdates` uses authenticated UDP when available and TCP
  otherwise.
- `SableUDPChannelHandlerClient` queues movement on Sable's separate client event
  loop; `MinecraftMixin` drains that loop during client ticks.
- `ClientboundSableSnapshotDualPacket.handleClient` logs and skips a snapshot entry
  if its body is not in the client container yet. Later snapshots can still recover.

This is source analysis plus one matched cold-restart pair, **not a packet-level
ordering trace or an upstream fix**. The comparison does not prove every similar
message has this cause. Ordinary tracking transitions alone did not reproduce the
burst. The 23 errors remain visible in `udp-cold-client-warnings.txt`; they are not
suppressed or reclassified as a clean log.

For the tested pack, setting `attempt_udp_networking = false` in the dedicated
server's `config/sable-common.toml`, then restarting the server and reconnecting
clients, used Sable's TCP fallback and avoided the burst in this occupied restart.
`disable_udp_pipeline` remained false. This is an optional troubleshooting fallback;
it can change networking latency/performance, which this run did not benchmark.
The original UDP-enabled file was restored after testing. A sustained missing-body
error or an invisible/non-restoring car is not covered by this transient recovery
result and must not be dismissed using it.

## Reproduction and evidence boundaries

1. Copy the complete stopped world from the
   [clean revised/PAX run](../revised-pax-multiplayer/README.md) after normal unloading.
   Start each server with the pinned packaged JAR, `-PfdValidation`, and
   `-PfdIntegrations=engineered,styles,additions,magnetized,immersion`.
   Use a packaged client with the same profile and validation flag. The one client
   process was reused across server connections; all four server processes were new.
2. Run the ordinary UDP and TCP cases on separate copies with only
   `attempt_udp_networking` changed between server starts. Retained TOML files show
   the exact settings. The command transcripts force-load a remote platform and
   teleport the unmounted player between it and the revised station six times,
   holding each position for eight seconds. Sable's tracking range is 320 blocks;
   the remote position is more than 5,000 blocks away. `transitions-reference.txt`
   records the loop; its RCON helper path was local to the test and credentials are
   deliberately not included. The command transcripts can be replayed with any
   authenticated server console.
3. The nearby set contains all four Free Spin carriers, four revised carriers and
   eight PAX carriers. `FD_CLIENT` reads actual native parent/child bodies, unlike
   `FD_SNAPSHOT`, which validates the separate 4D metadata packet. The verifier
   requires all sixteen original native UUIDs after each return, no native carrier
   records during the settled far interval, and ongoing metadata reception in
   every interval. It excludes one second at each transition boundary because
   native log timestamps have only whole-second precision. Screenshots show the
   returned revised car for both transports.
4. After the TCP range test, board the revised seat normally at the corrected
   returned-car position (`tp Dev -5.5 180 18.5 -90 -12`), close restraints, freeze,
   flush and stop. Make two complete copies of this stopped occupied world. Start
   one with UDP authentication disabled and one enabled, both with `-PfdFrozen`.
   Before client join, flush/decode the foundation save and compare it with
   `cold-before.json`. Join the client, wait four seconds, unfreeze, and inspect
   the rider, native bodies and saved state after twelve seconds. The same client
   rejoined both copies without forced mount or post-login teleportation.
5. Open restraints and dismount normally in the final UDP run. Stop all processes;
   restore both the original server properties and Sable configuration. All four
   servers and the client exited 0; the owned Xvfb display was stopped.

```sh
python3 scripts/verify-transport-recovery.py docs/evidence/integrations/2026-09-30/transport-recovery
```

`verification.json` records the reproduced results. Saved-data comparisons allow
only live carrier angle/omega settling after resumed ticks. Frozen comparisons
are exact. The broader world has 35 rails, eight cars and twenty carriers. Server
chunks remained force-loaded: these are **client tracking-range transitions**, not
proof of server chunk unload, moving occupied interruption, packet loss tolerance
or network performance. Only one occupied seat and one matched cold-restart pair
were tested here. Combined-pack versions are the same as the adjacent revised/PAX
report, and each server's loaded versions are retained in its `*-mods.txt` file.
