# FD1701A — ride-local authority

Qualified, 2026-11-15. Native guards and representative occupied handoff passed.
Source: main `cb46ea07` plus the current authority implementation.

`RideAuthority` binds one controller identity, saved epoch/sequence, native
consist and longitudinal provider. Hold/release never opens restraints or
advances a vehicle timer. Reload and missing controllers retain the hold and
require explicit Resume or held transfer. Consumers recheck their own physical
hold, route/exit and power predicates. Native Chain and Engineered force paths
respect provider ownership. Dive and Powered consume the interface; no
multi-train reservations are implemented.

Schema 8 accepts schemas 1–7 and prevents an older schema-7 program silently
ignoring saved ownership. Back up worlds before upgrading; downgrade is refused.

## Native case

One required `fourthdimension_fd1701a` GameTest passed on Java 21. It uses a
real native Wing chassis and two ordinary Boarding Stations. It checks exclusive
ownership, held transfer, open-restraint denial, stale/replayed requests, native
Chain versus Engineered ownership, save/load retained authority, explicit resume,
immediate missing-controller propulsion denial and held replacement transfer.
The compatible schema-7 data upgrades without inventing an owner.

Raw log: `/mnt/NVME4/fourthdimension-runs/fd08-operations-20261115/native-authority.log`.
The server saved and exited with code 0. No graphical client was used for this
case; it is not occupied-player acceptance.

## Occupied handoff and cleanup

The packaged Java 21 client/server run used the base profile, native Wing car
`9bc5f5a7-5211-4dfc-a31b-6f67ac8161b3`, real player Dev and two ordinary
Boarding Stations. Route, car, platforms, controller blocks and aiming positions
were disclosed fixture setup; this is operator/authority acceptance, not survival
construction. Default mouse bindings and camera turning plus normal stone
placement were established before the ride controls.

Normal input completed Claim → board → Close restraints → Dispatch → Stop →
bind receiving station → Transfer owner → Dispatch → Stop → Open restraints →
Shift dismount. The server accepted every control request. First dispatch produced
native velocity approximately (3.204, -0.000248, -2.306) blocks/s; after held
transfer, receiving-owner dispatch produced (-2.302, -0.000248, -3.207).
The transfer preserved the same occupied car, closed restraints and hold at owner
epoch 2. Final saved state has epoch 2, held=true, restraints=false, no fault and
empty rider assignments. The client diagnostic independently reports no passenger
and no assigned vehicle. See [request log](occupied-server-excerpt.txt),
[held transfer state](occupied-transfer-held.json), [final save](final-saved-vehicles.json)
and [unloaded client](unloaded-diagnostic.json).

Packaged JAR SHA-256: `cb5d0085657e8fed38227096dcfdfa59a6840a68d95e08b6abb11af0af627bd7`.
The UI-only layout fix in launch02 removed overlapping authority/seat-preview
buttons; native guard evidence remains applicable. Client heap1536M/scope6GiB;
server heap2048M/scope4GiB; private display316, server/RCON25650/25651.
Owner: `01a1dd1d-1468-7f81-8f31-5532ad7a178d`.
World, full logs, screenshots and input records remain under
`/mnt/NVME4/fourthdimension-runs/fd08-operations-20261115` (world
`server/AuthorityWorkshop`). Client exited, server saved all dimensions and exited,
then Xvfb stopped. All three owned units are inactive with MainPID0 and success;
see [cleanup](cleanup.txt). No runtime was handed off.

## Consumer handoff

The finite qualified shared profile is base native Chain with ordinary Boarding
Station authority. Engineered ownership rejection is covered by the native case;
optional Additions/CTNB/Magnetized gameplay belongs to its respective adapter.
`Controller` supplies stable identity, whole-consist binding/availability and a
fresh release predicate. `claim`, held `transfer`, `hold`, explicit `resume`,
`release` and `dispatch` consume fresh owner/epoch/sequence tokens. Native force
callbacks recheck provider ownership. Dive and Powered must supply their own
physical hold and route/power predicates; the shared hold does not invent a
mechanism or dwell timer. No missing shared hook remains for those consumers.
This evidence does not claim multi-train reservations or scheduler behavior.
