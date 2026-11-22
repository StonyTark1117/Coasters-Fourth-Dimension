# FD1701B connected-client attempt (2026-11-17)

This attempt used the owned private run at `/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117`:

- display `:329`, client `6G` scope, server `4G` scope;
- Java 21, raw mouse input enabled, default left/right bindings verified;
- server `127.0.0.1:25668`, RCON `25669`;
- player `Dev` connected and operator permission was granted through RCON.

The first setup attempt entered the command with a doubled prefix and therefore
showed an authoritative client-side command error. That historical input error is
retained below for traceability; it is not a wiring blocker.

Evidence:

- [command failure screenshot](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T090034.436198Z-state.png)
- [server log](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/server/logs/latest.log)
- [run manifest](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/run.json)

The initial failures were caused by the input sequence: the helper's `slash`
action already inserts `/`, while the follow-up typed another `/`, producing a
doubled prefix. After a fresh Java 21 build and reconnect, the corrected sequence
opened the command box, typed `fd demo arrow`, and captured the single-prefix box
before submission ([readback](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T090928.333382Z-after.png)).
Submitting it built the Arrow circuit and displayed the authoritative success
message ([result](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T090950.720769Z-state.png)).

The earlier doubled-prefix failure is preserved at
`20261117T090403.374034Z-state.png` for traceability; it is not a registration
defect.

## Normal operator-path readback

Using the saved world after a fresh Java 21 launch, the connected player opened
the vehicle's **Ride operator panel** through the normal Rotation Tool path. The
panel readbacks show:

- route `arrow_fixed_circuit_v1`, initially `trains 0`, block `-`, with admission
  denied until the complete train was ready ([initial panel](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T091817.600078Z-state.png));
- action 94 admitted the complete one-car train and reported `trains 1 | block 0`
  ([admission readback](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T091831.569524Z-state.png));
- closing restraints changed dispatch to ready ([closed readback](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T091845.229572Z-state.png));
- capture produced the expected braking/unresolved denial, and action 95
  **Resume route** returned `Dispatch: ready`, `route ... trains 1 | block 0`,
  and `Route status: ready` ([resume readback](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T091918.737848Z-state.png)).

This is connected-client evidence for the operator admission, occupied route,
and explicit recovery boundary. Connected three-train physical separation remains
pending; native Mechanisms covers admission, removal, and restoration only.

The client, dedicated server, and Xvfb units were stopped after this sequence;
all three units report inactive.

## Additional-car placement boundary

A follow-up owned run attempted the normal `Arrow Car` item path on the saved
route. The item was granted and selected, but the connected player was still
assigned to the prior seat by the boarding restore state. Server evidence shows
the dismount guard repeatedly cancelling exit while restraints remained closed;
teleport commands consequently left the player at the restored seat position.
The client returned the authoritative placement guard, `Aim at matching 4D track
to place this car`, rather than creating a second train. The player assignment,
seat root-vehicle readback, and final state are preserved in the run log and
snapshots (`20261117T093504.359545Z-state.png`,
`20261117T093514.894265Z-state.png`, and
`20261117T093545.376404Z-after.png`). This historical input attempt therefore does
not claim a connected three-train separation result; that criterion remains
pending, while native Mechanisms covers admission, removal, and restoration only.

## Normal additional-car placement and compact panel readback

After reopening restraints through the operator panel, the rider detached
normally (`RootVehicle` absent and the server log recorded
`FD_BOARDING_DISMOUNT_CLEAR`). With the Arrow Car item selected, three separate
right-click placements succeeded on connected route anchors. Each returned
`Carrier capture engaged. Wait for alignment before boarding.` The authoritative
availability readback was `chassis=4/4 carriers=8/8 assignments=0` before the
panel interaction, proving the original car plus three normally placed cars.
Placement snapshots are [first](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T094229.868988Z-after.png),
[second](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T094250.303163Z-after.png), and
[third](/mnt/NVME4/fd12-art-c2c6-20261117/evidence/20261117T094310.991762Z-after.png).

The operator panel now uses a compact layout at the 854x480 client viewport
(427x240 logical GUI): status rows, controls, and feedback stay in separate
bounds, all controls are reachable, and the full optional-controls text is
available in a tooltip ([tooltip readback](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T095209.986886Z-state.png)).
The normal **Close restraints** action remained reachable and returned the
authoritative `4D settings updated` feedback with no overlap
([action readback](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T095223.119747Z-state.png)).
The panel still showed `trains 1`; the connected client therefore does not claim
three-train separation, which remains pending a run with three admitted physical
trains. Native Mechanisms covers admission, removal, and restoration only.

## Boarding Station support-face verification

The canonical fix is the registration-side `noOcclusion().strength(3)` on the
Boarding Station block. In the same owned Java 21 client run, the station and
its smooth-stone supporting plinth were viewed from above; the plinth top face
is rendered continuously around the narrow station model
([focused view](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T095815.172535Z-state.png)).
The normal right-click interaction remained intact and opened the station editor
with its expected controls ([interaction readback](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T095825.708077Z-after.png)).
The reported pre-fix visual is preserved at
[station-support-user-report.png](station-support-user-report.png).

## Route-state telemetry follow-up

The saved world was reopened with the validation build and the connected player
was confirmed online before querying the server. The new read-only
`/fd validation route_state` command returned `route=arrow_fixed_circuit_v1`,
`trains=1`, and `reservations=2`. The sole admitted chassis was
`ea2945a0-d2c8-4a23-b152-f113b742e8c9`, with its one-car list, `block=0`,
`nextBlock=1`, `braking=true`, `unresolved=false`, and `restored=true`; blocks 0
and 1 both remained owned by that chassis. The authoritative availability query
returned `chassis=4/4 carriers=8/8 assignments=0`, confirming the four physical
chassis remained present while only one route train was admitted.

The full command responses are preserved in
[console.jsonl](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/console.jsonl)
near timestamps `1794910105` through `1794910125`, and the connected-world
snapshot is [20261117T100845.450772Z-state.png](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T100845.450772Z-state.png).
This is an authoritative guard/readback result, not a three-train separation
claim: the saved route state requires explicit recovery before another normal
admission can proceed. The other three placed chassis were not admitted during
this run because the selected restored vehicle failed the cart-center
accessibility guard.

## Resume-route retry and reach guard

The saved run was reopened again and the normal operator panel was reached from
the recorded route position. The panel identified the selected vehicle as
`Unavailable`, reported `Dispatch: Vehicle is out of reach or cannot be edited`,
and showed `Route status: ready`. One normal **Resume route** click was sent from
that panel state. The client remained on the same guard readback, and the
immediate server query returned the unchanged chassis, block pair, braking and
restored flags above. The action therefore did not reach the route recovery
handler; this run stops at the authoritative reach guard rather than attempting
additional aim guesses or unsupported admissions.

The input and result are preserved at
`20261117T102106.709289Z`/`20261117T102107.423060Z` in
[input.jsonl](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/input.jsonl),
with the unchanged panel at
[20261117T102108.267604Z-state.png](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T102108.267604Z-state.png).

A second retry kept the panel open while Dev was teleported to the live chassis
position reported by Sable (`9.5001, -58.5008, 45.8311`). After the panel's
automatic polling interval, Resume remained disabled with the same reach guard.
The enhanced route telemetry reported the logical pose but no cart-center value,
which is the exact center used by `VehicleOperations.accessible`; no recovery
click was sent from that disabled state. This points to a source-level
cart-center/accessibility defect after restore, rather than an unattempted
normal control path. The retry snapshot is
[20261117T102434.296562Z-state.png](/mnt/NVME4/fourthdimension-runs/fd12-art-c2c6-20261117/evidence/20261117T102434.296562Z-state.png).
