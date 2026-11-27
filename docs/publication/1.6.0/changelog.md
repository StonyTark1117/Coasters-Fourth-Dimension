# Coasters: Fourth Dimension 1.6.0

For **Minecraft 1.21.1, NeoForge 21.1.256 and Java 21**. Install matching Fourth Dimension builds on clients and servers.

## New in this release

- Launch Systems integration for original Arrow and Wing moving-track handoffs, including native force transfer, whole-train capture, movement, locked docking and downstream departure.
- Exclusive moving-track authority: LS propulsion and braking are denied while the train is retained. Stale/replayed or competing tokens cannot restart it.
- After locked Release, the receiving LS section requires a fresh arm followed by a separate fresh launch request. Missing providers/controllers, interrupted power and restart remain held until explicit recovery.
- Carries forward the 21 vehicle configurations, eight moving mechanisms and accepted features of 1.1–1.5.

**Launch Systems is optional and separately distributed. Fourth Dimension operates without it.** Only the new LS integration requires the independently released LS 1.0.1 build; LS 1.0.0 cannot read FD schema 9. This release contains no LS classes or bundled LS JAR and does not publish LS. See the [Launch Systems project](https://github.com/StonyTark1117/Coasters-Launch-Systems) for its separate availability.

## Required stack

- [Create 6.0.10](https://modrinth.com/mod/create/version/UjX6dr61)
- [Create: Coasters Simulated 0.1.5](https://modrinth.com/mod/create-coasters-simulated/version/PGhQoSBq) — 0.1.6 is not part of this qualified stack.
- [Sable 2.0.6](https://modrinth.com/mod/sable/version/fg9dTRz9), which includes Sable Companion 1.6.0 and Veil 4.3.2.
- [Veil 4.3.2](https://modrinth.com/mod/veil/version/EcZhFl38) is required on clients and already included by the pinned Sable distribution. Do not replace it with a newer standalone Veil.
- Use Create's matching Flywheel/Ponder/Registrate support stack. Fourth Dimension does not bundle upstream mods.

## Optional integrations

These are optional, never required to use standalone Fourth Dimension. Listed compatibility is finite; it does not grant every vehicle/moving-mechanism combination.

| Addon | Qualified version | Links |
| --- | --- | --- |
| Track Styles | 1.2.0 | [Modrinth](https://modrinth.com/mod/coaster-track-styles/version/dr8YaB54) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles/files/9020601) |
| Engineered | 1.1.1 | [Modrinth](https://modrinth.com/mod/coasters-engineered/version/pzBMM4ER) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/coasters-engineered/files/8863559) |
| Simulated Additions | distribution 0.2.5 / runtime 0.2.2 | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions/files/9243346) |
| CTNB | 1.2 | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/ctnb/files/8718286) |
| Magnetized | 1.1.0 | [Modrinth](https://modrinth.com/mod/coasters-magnetized/version/jVuUgvkG) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/coasters-magnetized/files/8668703) |
| Coaster Skin | 1.1.0 | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-coaster-skin/files/9409938) |
| Immersion | 1.1.1; client only | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion/files/9542588) |
| Waterparked | 0.2.8-beta-hotfix1 | [Modrinth](https://modrinth.com/mod/create-waterparked/version/stmCORbD) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-waterparked/files/9058331) |
| Create Coasters | 2.0 | [Modrinth](https://modrinth.com/mod/create-coasters/version/ssnvgDum) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-coasters/files/8110328) |

Additions also needs Aeronautics 1.3.2 and Interiors 0.6.1; Waterparked needs KotlinForForge 5.10.0. Follow those addons' dependencies. Keep Immersion off dedicated servers. Optional relations are linked directly wherever a verified platform project exists; the remaining addons retain CurseForge links here.

## Upgrade and operating limits

Back up worlds and test upgrades on a copy. Save schema 9 is unchanged; match client/server versions and do not downgrade upgraded saves in place. Repairing a fault never restarts motion automatically.

LS integration is limited to original Arrow/four-rail and Wing/native inserts, their existing train/bed limits and eight supported mechanisms. LS operates on straight, level fixed sections at physically locked docks, never on an unlocked/tilted bridge or moving bed. Tire/LSM/LIM and fin setups use the documented sequence: stop and close, source hold, Capture, Move, locked Release, fresh receiver arm, separate launch request. The controller launch input is above its top face; use a fresh low-to-high input when required. Arrow's wider swept envelope needs the documented lower fixed-cabinet mounting supported by LS 1.0.1.

Cable/swing, other/specialty configurations and managed 1.2 moving circuits are outside this new LS contract. Existing fixed-track support and other accepted 1.5 limits remain unchanged. Moving beds remain straight 16/32/48-block assemblies, at most four homogeneous whole-consist cars subject to smaller family limits. Capacity is not an FPS guarantee; maximum-size synchronous geometry validation has previously paused the server for about 3.125 seconds.

[Moving-track guide](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/blob/v1.6.0/docs/MOVING-TRACK.md#launch-systems-handoff-16) · [Finite LS contract](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/blob/v1.6.0/docs/specs/FD2008-launch-systems-1.6.md) · [Acceptance and measurements](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/blob/v1.6.0/docs/evidence/1.6/README.md)

Validated with native Arrow and Wing handoffs, dependency-loss checks, targeted FD/LS unit checks and two real clients proving occupied handoff, power interruption, cold restart, normal departure and dismount. Prior accepted cases carry forward. Java 21 package reproduction is byte-identical; all owned test runtimes were stopped cleanly.

**License:** CC0-1.0. Upstream dependencies retain their own licenses.

**JAR SHA-256:** `c9fd469c9ac7fbcdb074ac1f97153a8fd9d8676bb04712197d7105164d12d84c`
