# FD403C Free Spin shaped occupied route — 2026-11-15

This record closes the remaining connected-client Free Spin route, occupied
damper, final-state persistence, and family teaching claims. It combines the
owner's retained normal-input observations with the pre-capture damper and
cold-reopen readbacks. The full FD403 execution procedure did not require a
second uninterrupted ride or a second teaching ride.

## Run and construction provenance

- Owner: `Complete FD403C Free Spin`, thread
  `01a1d49d-7763-76b2-8b0c-6f5b337f0ea2`.
- Persistent run root:
  `/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113`.
- Connected dedicated server: `127.0.0.1:25612`; private display `:260`.
- World: `FD403FreeSpinRiderRepair20261114`.
- Chassis: `49305388-c9f5-4125-aaa1-12dbfc7c17a5`.
- Intended DAMP carrier: `a79f9650-1b2c-433c-8428-b94eb0ed765f`.
- Runtime artifact: `0.9.0-dev.27`, SHA-256
  `a0f776fbf7de28fd3a9308682012904eaec6a7234f347a036a115951fc647413`;
  Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Coasters 0.1.5,
  Sable 2.0.3, Java 21.0.13. This was the run-specific diagnostic snapshot
  build; its diagnostic hooks were removed from the working source after the
  evidence was collected. No unrelated Arrow source change affects this
  Free Spin behavior, and the teaching screenshots/resources reviewed below
  are unchanged for Free Spin.
- Runtime, server/client logs, normal input history and command readbacks remain
  in the run root's `run.json` and `evidence/` directory.

Previously accepted Free Spin recipe discovery/crafting, track cost, ordinary
anchors, car placement, boarding and short-route save/reopen evidence carries
forward from [the shared family record](../survival-client-legacy-free-spin.md).
In this retained connected-client world the player crafted 58 track items for
the new loop, placed the shaped native-anchor route and its Free Spin track,
placed the car, and normally installed the magnetic and Chain spans. Upstream
motor power was supplied as a disclosed fixture; no Fourth Dimension output
item or route section was command-placed. The accepted bounded 50-block pass
and command-authored demo remain supporting evidence rather than substitutes
for this player-built shaped route.

The retained `input.jsonl` shows normal placement of the 36-block i1–i0 Chain
span, normal Inducer use at `20261114T040637Z` and `20261114T040717Z`, normal
Damper use at `20261114T041054Z`, and normal i6 Chain selection/commit at
`20261114T052907Z` / `20261114T052943Z`. The Damper editor readback followed at
`20261114T041141Z`. Before the successful occupied pass, removal of the one
disclosed temporary stone support at `(362,-57,164)` restored travel. The i2
motor/span direction was corrected in the retained setup; neither correction
is credited as player construction. The corrected route was traversed with a
normal occupied dispatch.

## Occupied DAMP before capture

The compact [damper readback](occupied-damper-pass.json) records the same
assigned carrier before and after the occupied pass:

| Sample | Occupied / released | DAMP impulses | DAMP energy | DAMP speed reduction |
| --- | --- | ---: | ---: | ---: |
| Baseline, 01:28:19 UTC | `occupants=1`, `capture=false` | 0 | 0.0 | 0.0 |
| After crossing the installed span, 01:28:51 UTC | `occupants=1`, `capture=false` | 78 | −0.17315709154037506 | 0.47375498768252844 |

The chassis motion readback at 01:28:46 confirms the occupied car remained
moving on the route. The damper counters establish an actual DAMP response
while released and occupied, before the later normal Capture. The reported
`damperSpeedReduction` is the mechanism counter; the chassis also crossed
Inducer spans, so the net angular velocity samples are not presented as a
standalone before/after damper comparison.

## Normal capture, unload and cold reopen

After the route pass, normal Rotation Tool **Capture** was acknowledged at
`20261115T013939Z`; the editor showed `Ready | Restraints closed`. Normal **Open
restraints** was acknowledged at `20261115T014038Z`, and the retained
[screenshot](restraints-open.png)
shows `Ready | Restraints open`. Normal **Shift** dismount at
`20261115T014106Z` produced `FD_BOARDING_DISMOUNT_CLEAR` in `server.log`.
Readbacks at 01:41:20 showed the four assigned carriers captured and empty,
with no player `RootVehicle`.

The final world was cleanly saved; the first stop recorded
`ThreadedAnvilChunkStorage: All dimensions are saved` at 01:45:13 UTC. It was
then reopened in a fresh dedicated-server/client process and Dev rejoined at
01:46:37. The [cold-reopen readback](final-state-reopen.json)
and [post-reopen screenshot](cold-reopen.png)
show all four assigned carriers captured and empty and no `RootVehicle`. The
runtime DAMP counters are zero after restart because those counters reset with
the process; the pre-capture DAMP pass is preserved separately above. The
post-reopen server was then stopped cleanly and again recorded all dimensions
saved at 18:53:15 server-local time (01:53:15 UTC).

## Free Spin teaching reconciliation

The accepted current-artifact review already covers the Free Spin, magnets and
boarding scenes at
[`free_spin-captions.png`](../teaching-current/ponder/free_spin-captions.png),
[`magnets-captions.png`](../teaching-current/ponder/magnets-captions.png), and
[`boarding-captions.png`](../teaching-current/ponder/boarding-captions.png),
along with the Free Spin car, track, Rotation Tool, Inducer and Damper tooltip
screenshots (`tooltips/03.png`, `04.png`, `09.png`, `10.png`, `11.png`). Those
Free Spin resources are unchanged by the reviewed Arrow caption edit.

The observed route agrees with the instructions: matching magnetic track
accepts spans; Inducers add torque; Dampers oppose angular velocity; target
masks select the intended carrier groups; and DAMP is independent from the
capture/boarding lock. The diagrams are explicitly labeled as illustrations,
not exact ride coupling. The normal Capture, restraint opening and unload
followed the documented separate readiness flow. No misleading Free Spin
control, caption or tooltip was found, so no resource edit or second walkthrough
ride was warranted. This closes only Free Spin's family contribution to FD403F;
the aggregate owner retains aggregate reconciliation.

## Source readbacks

- [Normal connected-client input history](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/input.jsonl)
- [Normal command/readback history](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/console.jsonl)
- [Dedicated-server log](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/server.log)
- [Client log](/home/braydon/.local/state/fourthdimension-runs/fd403c-free-spin-20261113/evidence/client.log)
- [Compact occupied-DAMP record](occupied-damper-pass.json)
- [Compact final cold-reopen record](final-state-reopen.json)
