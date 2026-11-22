# P1 Pipeline fixed support repair

2026-11-22, primary `main`, coordinator-owned client check following the
source review in [base-structure-reconciliation](../base-structure-reconciliation.md).
The named defect was a 1/16-block separation between each pan and the fixed floor,
plus a disconnected pan/shell interface. The Pipeline-only generator change
extends the pan underside one model unit and adds two small fixed risers.

The [assembled geometry audit](geometry.txt) reproduces the baseline defect and
proves controller-to-floor-to-pan-to-shell contact for all four seats in both
restraint states. Native assembly, cushions, hinges and restraint meshes are
unchanged; total car bounds are unchanged. All parts share one rigid chassis,
so track inversion cannot change their relative support alignment. This is not
a new physics or route qualification.

## Affected private client check

Run root `/mnt/NVME4/fourthdimension-runs/fd13-base-20261122`, owner
`fd13-coordinator`, private display `:341`, port 25713, Java21, 1536M client heap
in a 6 GiB Minecraft-only scope and separate 2048M/4 GiB server. The
[packaged build](build.log) passed; [runtime export hashes](runtime-inputs.sha256)
identify the eight tested seat assets. Other concurrent source edits were not
used to qualify this case.

Disclosed setup: `execute as Dev positioned 0 140 -500 run fd demo pipeline`.
This created native Pipeline `886b7150-1c75-4d9e-b2e3-e0a33bbd1182`; it is not
player-construction proof. Early F3 aim readbacks selected the floor, so no use
was sent. Exact native plot coordinates and `out_sub_level` with `anchored eyes`
resolved the correct cushion target; this setup is not normal-camera evidence.
After F3 confirmed `pipeline_seat`, one empty-hand Use boarded the real seat.
The [server log](server.log) records accepted offset `(1,-2,-1)`, successful
normal editor actions 11/12, normal dismount and clean disconnect.

- [Empty assembly](empty.png): pan/floor and shell support geometry present.
- [Occupied open](occupied-open.png) and [occupied closed](occupied-closed.png):
  no new rider or restraint intrusion observed in the changed support area.

The seat contacts, restraint meshes/sweep, side trucks and vehicle envelope
retain prior acceptance. The existing schematic item does not depict these tiny
pan seams and was not changed. This bounded check closes P1, not other styles'
structural review. Raw input/console records and saved world remain at the root.
All three owned units are inactive with MainPID0 after the server saved all
dimensions ([cleanup](cleanup.txt)).
