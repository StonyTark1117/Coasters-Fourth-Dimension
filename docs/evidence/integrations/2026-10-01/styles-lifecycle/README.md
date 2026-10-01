# Occupied Track Styles removal and restoration

Track Styles 1.2.0 survives a Styles-only → base pack → Styles-only save round trip
with two real players and three native pig passengers. Both Arrow spine choices
fall back to the original mesh while the addon is absent and return on reinstall.
Functional rails, profiles, car/carrier identities, restraints, and local player
seat reservations remain unchanged. This is a bounded FD903 checkpoint, not closure
of the release matrix or functional Track Styles support under FD801.

## Scene and observations

Three parallel, fixed 60-block Arrow tracks carry three captured cars and six
carriers. The tracks share the nontrivial rotation profile `(0,0), (12,0), (36,2π),
(60,0)` but use single-spine, double-spine, and original appearances respectively.
The normal profile and appearance validators apply these choices before spawning
cars. Each car has one native seated pig. Dev and Observer board the first two cars
through native Create seating with the production boarding checks active. All five
riders remain parked with closed restraints; this does not exercise moving inversion.

| Dedicated process | Input | Active optional addon | Live result |
| --- | --- | --- | --- |
| Seed | Fresh world | Styles 1.2.0 | Five passengers, both styled spines |
| Removed | Complete stopped seed world | None | Same passengers after reconnect, original appearance |
| Reinstalled | Complete stopped base-pack world | Styles 1.2.0 | Same passengers after reconnect, both saved styles restored |

Each restarted server advances 200 ticks before client startup. Players reconnect
normally under closed restraints; no reboarding command runs after the seed. The
fixture's attempted occupied edits are rejected with the entire foundation NBT
unchanged. All cars remain ready and fault-free, all six carriers load, and each
passenger is alive on the expected carrier. Original native pig seat UUIDs survive
both restarts. Player carrier and local-seat reservations are compared independently
of native human seat entity lifetime.

The six `*/third-person-{dev,observer}.png` captures were visually inspected. They
show the double spine disappearing in the base phase and returning on reinstall,
with original ties, running rails and yellow control rails retained. The original
third lane provides a control. Client `FD_STYLE_VIEW` logs separately report saved
choice, effective choice and availability of external model geometry; availability
alone is not treated as proof of a draw. Matching full server/client snapshot hashes
occur 296/296 times in seed, 43/44 in removed, and 42/39 in reinstalled.

After reinstall, a frozen, flushed occupied-world checkpoint preserves the native
data before normal unloading. The final stopped world has empty occupants and
player reservations, open restraints, all three pigs alive at health 10 and detached
from their seats, and both players grounded at platform height 116 with health 20,
flight off, no RootVehicle and gravity enabled. Pig platform landing is not claimed.

## Evidence and reproduction

`StylesLifecycleFixture` registers only with validation enabled and permission two.
It adds `fd validation styles_restart` actions `create`, `cars`, `pigs`, `close`,
`board_player <0|1> <player>`, `reject_edit`, `unload`, and `report <unique_label>`.
Read-only client diagnostics are likewise opt-in. No production compatibility or
physics behavior was changed for this checkpoint.

1. Build with Java 21 and start a fresh packaged dedicated server with
   `-PfdValidation -PfdFrozen -PfdIntegrations=styles`. Retain one immutable JAR for
   every server and client in the series.
2. Run `create`, step 100 ticks, run `cars`, step 100 ticks, then `pigs`, step 40
   ticks and unfreeze. Start two real clients with the same pack; run
   `board_player 0 Dev`, `board_player 1 Observer`, and `close`.
3. Record a unique report, reject an occupied edit, and inspect both clients' third
   person views. Disconnect clients normally, flush, stop, and copy the entire world.
4. Start that stopped copy without `fdIntegrations`, step 200 ticks, then unfreeze
   and reconnect both clients. Repeat the observations, saving and stopping the
   base-pack world before copying it for reinstall.
5. Restore `-PfdIntegrations=styles`, repeat step 4, then preserve a frozen/flushed
   occupied checkpoint. Run `unload`, step 20 ticks, record the final state, and
   disconnect/save/stop normally.

Retained drivers contain this run's local paths and exact commands. They expect
private loopback ports 25585/25586 and owned Xvfb displays. Passwords and complete
server properties are excluded. The nine game processes exit zero; six displays
are stopped; both ports close; server settings are restored byte-for-byte. The
build and nineteen unit tests pass. Native GameTests were not repeated for these
validation-only additions; the preceding acquisition checkpoint retains the
36-test base/combined regressions.

```sh
python3 scripts/verify-styles-lifecycle.py docs/evidence/integrations/2026-10-01/styles-lifecycle
```

The audit checks hashes, matches live reports to logs, decodes native foundation
and player NBT, reparses compressed entity regions, compares passenger/carrier and
seat identities, verifies appearance ownership/fallback, checks synchronized client
snapshots and validates unloading, exits and cleanup. Exact foundation comparisons
exclude only live carrier angle/angular velocity and the asserted changes from
player logout or deliberate final unloading. Source and compiled-class hashes are
retained in `artifact.json`. The JAR SHA-256 is
`d5bf815bbd5210a94016517f4014e136c49516693cdf6fef5ef6da87c35202ab`.

Two observation issues are retained rather than counted as passes. Seed overview
captures were taken before teleport packets settled and show spawn ground. An
early reinstall report contains only the three pigs, before players joined. A
reused report label then exposed a driver bug that reread that old log entry. The
reader now starts at the log byte offset preceding its command, and the unique
`occupied-verified` report confirms all five riders. No game process was restarted
or replacement passenger boarded to address these observation issues.

Pinned pack: Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Flywheel 1.0.6, Sable 2.0.3, Coasters Simulated 0.1.5,
Fourth Dimension 0.9.0-dev.1, Track Styles 1.2.0 / CurseForge file 9020601.
The known Styles dedicated-server client-class mixin errors and invalid client
resource paths remain visible in logs; successful startup is not a warning-free
startup claim. The clients use the existing default resource/render settings and
software GL. Moving removal, combined-pack removal, curved style transitions,
other render backends/shaders and older save schemas remain outside this checkpoint.
