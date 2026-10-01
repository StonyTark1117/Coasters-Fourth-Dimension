# Magnetized strength retention across occupied removal saves

Removing Magnetized 1.1.0 down to the base pack previously discarded its anchor
strength values when loaded chunks were saved. Reinstalling the addon preserved the
MAGNET/BRAKE modes but read zero strength, disabling both effects. A scoped anchor
serialization hook now retains the inactive integer field on Fourth Dimension
tracks. Two seven-process packaged-server series reproduce the loss and verify the
fix with one native seated rider in each of the five families.

The combined pack behaves differently: its remaining installed behaviours already
preserve the strength field. That control retains 37%/63% both before and after the
fix. The evidence does not generalize the base-pack loss to every addon combination.

## Change and ownership

Magnetized stores modes in `coastersmagnetized_magnetized_anchors.dat`; strength is
the Create behaviour's `ScrollValue` field in each native Coasters anchor block
entity. The base pack has no behaviour that writes this field back. Reading an
absent field after reinstall produces zero. The inspected pinned artifact bytecode
and before/fixed anchor NBT retain both halves of this finding.

`OptionalAnchorStrengthMixin` remembers only a present integer from 0 through 100.
It writes that value back only during a server save when all of these hold:

- Magnetized is not installed. Unsupported installed versions retain ownership of serialization.
- The anchor has no current scroll-value behaviour and no other writer supplied the field.
- The anchor is an endpoint of a rail stored in Fourth Dimension's foundation data.

An installed behaviour keeps ownership of its current value. The hook does not
register equipment, restore modes, enable propulsion, change readiness, alter the
field on unrelated anchors, or send inactive settings to clients. A later read with
no field clears the remembered value, including deliberate block-data removal.
It preserves an existing saved zero; it cannot infer a lost historical setting.

The opt-in `fd validation magnetized_restart` fixture adds create/cars/board/unload
and read-only report commands. It is registered only with `-PfdValidation` and
permission level two. It can report and normally unload the existing fixture with
the optional addon absent; optional classes are isolated behind the exact 1.1.0 gate.
Creation configures native anchors through Magnetized's API and Create's behaviour,
not by changing a saved file. No test-only exception bypasses the production hook.

## Occupied fixture and process matrix

A fresh Magnetized-only world contains five matching 60-block straight tracks,
five cars and fourteen carriers: Arrow, legacy ZacSpin, Free Spin, revised ZacSpin
and a parked single PAX car. Each car carries one real, no-AI pig in a native Create
seat with closed restraints. Each track's start anchor is powered MAGNET at 37%; its
end is powered BRAKE at 63%. Free Spin also retains an independent seat-magnet span.
Cars stay captured, ready and fault-free; external propulsion remains denied.

The original seed is saved after ordinary ticks and clean shutdown. Each series
uses the following actual packaged dedicated-server processes:

| Case | Input stopped world | Active optional addons |
| --- | --- | --- |
| Seed | Fresh before; original occupied seed for fixed | Magnetized |
| Control | Seed | Magnetized |
| Removed | Seed | None |
| Reinstalled | Removed, after saving without Magnetized | Magnetized |
| Combined | Seed | All five, with required dependencies |
| Combined removed | Combined, after saving | All except Magnetized |
| Combined reinstalled | Combined removed, after saving | All five |

Each restored process advances 200 ordinary ticks before readback. No riders are
reboarded after restart. The original native rider and carrier identities are checked
live; the stopped entity files independently verify original Create seat UUIDs and
health 10 while occupied. Both reinstallation cases open restraints through normal
validation and unload all five riders before their final save. The fixed series
starts from the very same pre-fix occupied world, preserving its original identities.

| Observed setting | Before | Fixed |
| --- | --- | --- |
| Normal Magnetized restart | MAGNET 37%, BRAKE 63% | MAGNET 37%, BRAKE 63% |
| Base-pack save without Magnetized | Strength field missing | Inactive 37/63 preserved |
| Reinstall after base-pack save | Modes retained; both strengths 0% | Modes and 37%/63% retained |
| Combined-pack removal/reinstall | Modes and 37%/63% retained | Modes and 37%/63% retained |

All fourteen processes exit zero. All five functional rails, profiles, magnetic
span, vehicle/carrier identities, seat definitions and operating data survive.
Exact saved-data comparisons exclude only live carrier angle/angular velocity, plus
the explicitly verified restraint opening, revision increment and cleared occupants
in final unloaded saves. The ten addon mode records remain identical in every case,
including when their file is opaque to the running base pack.

This is occupied parked restart/removal evidence. It is not moving removal,
automatic chunk eviction, client rendering, camera removal, simultaneous passenger
capacity, a coupled PAX shuttle lifecycle or proof that braking distances remain
suitable for an arbitrary ride. The separate coupled-Arrow native checkpoint covers
active Magnetized propulsion and braking.

## Regression results

The fixed source passes nineteen unit tests and three fresh native regression runs:
34 registered checks each, with seven absent-addon skips in base, five in Magnetized
and none in the combined pack. All three exit zero. The combined run retains
`-PfdPaxEnergyReload` so the existing occupied PAX FE restart cases execute. These
are development GameTests alongside the fourteen separate packaged-server runs.

## Recovery and reproduction

The fix must run before a missing-addon save discards the field. It does not repair
a world already saved with missing or zeroed strengths. Keep the complete stopped
world backup when changing a pack. For an already affected world, restore the
pre-removal world with its original pack, or reconfigure and verify every affected
anchor before dispatching riders. Reinstalling the addon alone does not reconstruct
lost percentages. Other addons' missing blocks and settings have their own removal
behaviour; this fix makes no claim to recover them.

Reproduce with Java 21 and the pinned packaged JAR in a fresh disposable world:

1. Start `runServer -PfdPackaged -PfdPackagedJar=/absolute/path/to/jar
   -PfdValidation -PfdFrozen -PfdIntegrations=magnetized`.
2. Run `fd validation magnetized_restart create`, step 100 ticks, run `cars`, step
   100 ticks, then `board` and step five ticks. Record `report occupied` and stop
   after `save-all flush`. The retained driver prefixes each action as in the first
   command. This fixture requires a fresh world and uses fixed coordinates.
3. Make separate complete stopped-world copies and follow the process matrix above.
   Use no `fdIntegrations` property for the removed/base case. Combined uses
   `engineered,styles,additions,magnetized,immersion`; omit `magnetized` for its removal
   case. Let each process advance 200 ticks, `report restored`, save and stop.
4. In each reinstalled case, run `unload`, advance ten ticks, and `report unloaded`.
   Do not board fresh riders to replace missing passengers.

The retained drivers include exact flags, world-copy sequence, tick stepping and
commands. They contain this run's absolute workspace paths and expect private
loopback ports 25585/25586. Original server settings are restored byte-for-byte.
Runtime passwords and complete server properties are excluded from evidence.

```sh
python3 scripts/verify-magnetized-lifecycle.py docs/evidence/integrations/2026-10-01/magnetized-lifecycle
```

The audit matches reports to raw logs, reparses saved NBT and native entity-region
files, compares track/mechanical
and mode data, checks live and saved passenger identities, verifies the before/fixed
strength differences, normal unloading, exit codes, regressions and hashes.
`verification.json` retains the audited per-case results. Full logs are compressed;
command text trims trailing whitespace with original bytes retained as gzip.
Artifact and compiled-class hashes identify the two immutable JARs.

Pinned versions: Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Sable 2.0.3, Coasters Simulated 0.1.5,
Fourth Dimension 0.9.0-dev.1, Magnetized 1.1.0 / CurseForge file 8668703.
Combined adds Engineered 1.1.1, Track Styles 1.2.0, Additions 0.2.0,
Immersion 0.2.3, Aeronautics 1.3.2 and Interiors 0.6.1v3.
Known dependency loader warnings are preserved. Broader FD901–FD904 acceptance
remains open; this result does not certify other addon versions or arbitrary packs.
