# Occupied FE restart and native passenger restoration

An occupied packaged dedicated-server restart exposed a boarding bug. All four FE
balances, functional tracks, cars, carrier bodies and the Arrow coupling restored,
but five native pig passengers disappeared from occupancy. The pigs still existed;
their restored seat attachments had been rejected.

## Cause and correction

The retained diagnostic trace records all five native passenger mount events on
Sable's entity-deserializer thread. Each rider belonged to a known carrier, appeared
in that carrier's saved occupancy, and encountered closed restraints. The ordinary
new-boarding guard rejected those events. The native seat entities subsequently
had no passengers, leaving the pigs unattached.

`Boarding.restoringSavedPassenger` now recognizes this specific deserialization
case: a living non-player entity and Create seat entity have not yet been added to
the level, the rider UUID belongs to that carrier's saved occupancy, and the seat
position is one of the carrier's declared seats. It preserves that saved attachment
through closed restraints. Live boarding still uses the ordinary readiness and
restraint checks. Player entities continue through their separate deferred login
and view-restoration path. No seat schema, energy algorithm or addon version gate
changed. The temporary mount trace was removed from the final artifact.

The native regression serializes an occupied seat and invokes the real entity NBT
loader while restraints are closed. It requires the saved rider to reattach and
an otherwise equivalent unknown rider to be rejected; the existing live rider must
remain mounted. The subsequent normal dismount restriction still runs. This test
uses unadded deserialized entities without inserting duplicate UUIDs into the world.

## Dedicated-server cases

The validation fixture is available only with `-PfdValidation`, operator permission
and the exact Engineered 1.1.1 adapter. It requires a fresh ride-data world, creates
four 60-block tracks, and uses normal placement, native controllers/capabilities,
carrier seats and validated vehicle operations. It reconstructs every reference
from live saved data after a restart; no static Java fixture objects cross processes.

The original combined-pack baseline contains two linked Arrow cars and one each of
legacy ZacSpin, Free Spin and revised ZacSpin. Five native pigs occupy five seats
on fourteen carrier bodies, with restraints closed and capture aligned. Each
controller stores a forward launch step, and each native anchor holds a distinct
balance. The complete stopped world is backed up; save NBT is never edited.

1. The original build reproduces lost occupancy after a clean restart and 100
   simulation ticks. All five pigs remain alive but no longer ride their carriers.
   A separate replay with the temporary trace identifies the rejected attachments.
2. The fixed build loads a fresh copy of that original occupied baseline. All five
   original rider UUIDs reattach to their original carriers, with exact FE balances.
   It saves and stops again, establishing a baseline written by the fixed artifact.
3. A new process with that same fixed artifact and the combined pack restores the
   fixed baseline. After 100 ticks, all four rail records and all vehicle/carrier
   saved fields compare exactly except live carrier angle and angular velocity.
   This includes occupancy, chassis/carrier IDs, restraints, capture, family,
   provider/appearance IDs, profiles, bindings and geometry fingerprints.
4. A separate cold process uses a fresh copy of the fixed baseline with Engineered
   only. The same mechanical and rider comparisons pass. This is a combined-written
   world loaded without the other addons; the fixture contains no blocks or custom
   styles from those addons, so it does not certify removal of their active features.

Both final processes then dispatch the original occupied cars for 80 ticks, clear
remaining FE and trim-brake for 40 ticks, capture for 130 ticks, and open restraints
and unload for 10 ticks. Every car travels more than two blocks at greater than one
block/s, then brakes below 1.3 blocks/s. Passenger UUIDs, actual mount-to-carrier
relationships, loaded bodies, fault-free state and coupling are checked throughout.
Final capture requires boarding readiness. All five pigs remain alive after unloading.

| Family | Restored FE | After launch | Recovered by braking |
| --- | ---: | ---: | ---: |
| Arrow, two cars | 12,345 | 3,854 | 2,148 |
| Legacy ZacSpin | 12,346 | 6,133 | 1,412 |
| Free Spin | 12,347 | 411 | 2,889 |
| Revised ZacSpin | 12,348 | 0 | 3,126 |

The two final profiles produce identical FE results. Revised ZacSpin reports native
starvation when its buffer reaches zero; it still brakes and regenerates normally.
These measurements belong to this fixture, not a promised ride energy budget.

Native spans and full controller NBT compare exactly across final restart, excluding
only the controller's live last-cart speed/timer readout. Full anchor NBT also matches
in the combined control. The Engineered-only comparison requires exact native span
and FE plus our saved mechanical data; it does not require retention of unrelated
Additions anchor metadata. Raw records are retained for inspection.

## Validation and rejected setup

All 28 registered GameTests pass in the base, five individual-addon and combined
profiles. Explicit optional skips are respectively 5, 3 for Engineered, 5 for Styles,
3 for Additions, 4 for Magnetized, 5 for Immersion, and 0 for combined. Every profile
executes the new serialization regression and ten connected-route interruption
checks. The build also passes 19 unit tests.

Two rejected setup worlds are preserved in the command transcripts: the first was
outside the 1,024-block construction bound; the second attempted an unsupported
120-block section and failed placement reach validation. The accepted fixture uses
a fresh world inside the bounds and 60-block sections. An immediate frozen-start
report in the failing restart also found chassis loading incomplete; snapshots used
for acceptance are taken after the documented 100 ticks, not at tick zero.

All packaged server processes stopped cleanly. Original server world selection and
Engineered configuration were restored after the final run. Exact mod lists, logs,
commands, saved-data snapshots, failed occupancy records, trace, source/JAR hashes
and JUnit XML accompany the report. Existing dependency startup warnings remain.
The Additions native run also records two server-lag warnings (about 2.0 and 2.5
seconds behind); these passing functional checks do not establish timing or
performance acceptance.

## Reproduction and limits

Build with Java 21. Start a new world using the packaged JAR, `-PfdPackaged`,
`-PfdPackagedJar=/absolute/path/to/final.jar`, `-PfdValidation`, `-PfdFrozen` and the
combined integration profile. Enable `requireEnergy=true` in Engineered's server
configuration before starting. While frozen, run these commands, allowing each
`tick step` to finish before issuing the next operation:

```text
fd validation energy_restart create
tick step 40
fd validation energy_restart cars
tick step 100
fd validation energy_restart board
fd validation energy_restart charge
tick step 40
fd validation energy_restart report before
save-all flush
stop
```

Back up the stopped world. Start a new process with the same final artifact. Step
100 ticks and report/save the restored state before any dispatch. Then use
`dispatch` + 80 ticks, `brake` + 40 ticks, `capture` + 130 ticks and `unload` + 10 ticks
under the same `fd validation energy_restart` prefix, taking a `report <label>` at
each phase. Repeat on a fresh baseline copy with only `-PfdIntegrations=engineered`.
The retained command transcripts contain the complete accepted and rejected runs.

```sh
python3 scripts/verify-engineered-occupied-restart.py docs/evidence/integrations/2026-09-30/engineered-occupied-restart
```

This certifies the documented native-passenger cold restarts and subsequent straight
track operation. It does not establish human-player FE reconnect, camera recovery,
PAX FE propulsion, occupied chunk interruption, every passenger/entity type, delayed
or damaged entity storage, sloped/curved rollback, external cables or performance.
The broader FD305/FD901–FD904 lifecycle and two-client matrices remain open.
