# FD1802A qualified mechanics and occupied handoff

Qualified 2026-11-15 by owner `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da` on
primary `main`, `/home/braydon/Documents/Coasters: Fourth Dimension`.
Integration belongs to the lead; last observed HEAD `fe993053`.
Conventional prerequisite is integrated `5fe557f2`, and Conventional A–C closes
independently at `fe993053`. No 0.8 work is activated by this handoff.

## Accepted mechanics and occupied behavior

| Required behavior | Accepted evidence |
| --- | --- |
| Real chassis → unlimited-Y deck → X posture carrier; two seats; authored pivots and loading | `native/compound-pass.log`, `native/compound-summary.json`: native inner mass2, one rider+.8, full3.6; coincident hinges and bounded simultaneous yaw/posture; no renderer-owned physical rotation. |
| Canonical profiles, reverse, four-car consist, endpoint guard | `native/consist-pass.log`: native4×8-block links, fifth denied; front cannot cross posture exit before actual lock, all chassis share guard hold; actual forward79.215607→reverse45.960052 retraces yaw/posture; all return seated/zero. |
| Projected targets seated/prone, bank/inversion, winding and limits | Prior `native/target-and-turns` record's completed assertions cover banks0/90/180 and two actual unwrapped turns (its later Show failure is not claimed as a whole-file pass); `native/show-winding-pass.log` covers actual+4-turn bounded winding exhaustion. Current README preserves exact filenames/provenance. |
| Distinct fallback and explicit recovery | Missing-marker/native restore in route record; `native/failure-pass.log`: unloaded, unauthorized, degenerate, actual controller loss, unreachable winding all retain restraints and bounded actual motion, remove travel authority and require explicit retry. |
| Named stopped Show and departure | `native/loaded-show-pass.log`: one off-center prone rider, full turn, zero canonical drift, actual yaw peak.448125rad/s and acceleration.137430rad/s², prone lock preserved; unfinished/unsettled departure denied, explicit departure reaches next interval. Missing yaw deck immediately denies native propulsion. |
| Real occupied seated targeting and simultaneous changeover | `client/occupied-seated-target.png` (0.52° projected error, SEATED), `client/occupied-combined-transition-06.json`; ordinary Dispatch then changing both native angles with Dev aboard. |
| Real prone targeting through declared full roll | Artifact02 occupied views plus artifact06 `client/boundary-correction-readback.json` / `occupied-prone-after-boundary.json`: native roll exit crossed and continued, prone89.5°, projected error1.43°, no fault. |
| Two real clients, native rider alignment, independent look/comfort | Artifact02 rider Dev and late observer on owned318/319 saw same three native bodies, yaw530.1°, prone89.1°, retained target-entry blend. Views in `client/`; independent look and K comfort switch observed. |
| Saved hierarchy, winding/binding/transition and rejoin | Artifact02 actual occupied partial-blend restart preserved deck/inner IDs, marker, revision/fingerprint, entry/progress and rider; yaw difference−.00000566rad, posture−.00031655rad. Explicit retained RESTART_REVALIDATION then normal Retry/Dispatch. |
| Occupied loaded Show correction | Artifact08 `client/show-completion.json`, before/after saves and screenshot: ordinary20tps named Start reaches540°, PRONE, both restraints, no fault; canonical distance exactly8.746704059566428 before/after. Normal Dispatch resumes native travel. |
| Return/unload | Same real car returns through native seated changeover (saved `client/occupied-seated-return.json`), normal Stop on original dock, explicit Unwind to actual0°, separate Open body/Open legs, normal Shift reaches platform(−3.5,11,42.5), server occupants0; `client/normal-unload.png`. |

The shared restore-look defect subsequently observed by the lead on Roller is
closed by that owner's exact same-save reconnect: local yaw/pitch59.529404/
23.423008 restored exactly, camera difference0.0317°, ordinary look persists.
`Boarding` and carrier/seat/dimension-scoped `RestoredLookSync` supply the shared
fix; that representative changed-path proof carries to NG without a rejoin replay.
The earlier NG physical hierarchy/state evidence is unchanged.

## Corrections and reproducible boundary

The initial deck needed native collision to have mass. Conservative discrete
reference braking removed endpoint overshoot. Motion direction now updates only
during authorized travel on the same canonical edge, avoiding stopped jitter and
edge-reset sign changes; reverse test covers this path. Posture exit capture uses
the actual two-degree/five-tick endpoint window and propagates a consist hold.

NG examples use native lifted anchor endpoints from the additive FlyingExamples
four-argument overload; the former raw-base example was.5 block low at an upright
anchor and stalled the native bogie. Fresh artifact06 passed the exact boundary.
Motors sit four blocks from upright ports with only native shafts extending in.
Each scenery marker is local to its section. The Show starts eight blocks into
its span, clear of the anchor. Its loaded hold uses an actual world-to-chassis
constraint at the already-stopped measured pose, released on explicit departure;
inner COM translation is not incorrectly braked during yaw. No global collision
exemption or shared brake rewrite was used.

Artifact08 SHA256 `c6c0cc151b41483ba2a44797b920f42dd7fc6a4e7b4bc6580b454c4f101e041e`.
Earlier artifacts retain their recorded scope. All native test processes saved
and exited. Closed physics/occupied cases are not reset by new hashes, content
work, the data-only magenta recipe correction, or shared look fix.

Fixture commands, setup teleports and accelerated ticks are disclosed in raw
console/input records. Artifact06's targeting/roll ride used ordinary20tps.
Artifact07 advanced already-accepted approach via `tick sprint4000`, then verified
20tps before Show. Artifact08 return used `tick sprint2000` through the already-
accepted constant-prone segment, then verified20tps for observed seated return and
normal unload. These do not claim player construction or accelerated-motion views.

## Same-owner B/C continuation

A is qualified; FD1802B/C remain actively owned here for normal recipes, paid track,
car/marker placement, native Chain, finite track editor and final FD916 content
mapping. Normal Survival magenta-track and two-car crafting already passed;
marker/editor/construction continue. Source/resources/tests are coherent for the
lead's A integration checkpoint; no imminent mechanics edits are planned.

The exact continuing runtime is owned by this same B/C task:
`/mnt/NVME4/fourthdimension-runs/fd1802-new-generation-20261115`, world
`server/NewGenerationClearShow`, private display318, loopback25654/RCON25655,
`fd1802-new-generation-20261115-{client,server,xvfb}.service`.
Current client mainPID1004153/server mainPID1004113 (verify live ownership before
acting); Java21, client heap1536M/scope6G, server heap2048M/scope4G.
Observer319 is inactive. Ten temporary construction chunks208,0→239,79 remain
forced and will be removed at final cleanup. Earlier failed worlds and all logs
are preserved. The same owner explicitly retains this exact runtime for active
B/C, not as an idle completion handoff; all units will be saved/stopped and verified
inactive before final package closure.
