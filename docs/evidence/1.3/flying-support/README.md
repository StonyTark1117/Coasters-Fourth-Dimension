# F1 Flying shaft saddles — affected client verification

**Passed 2026-11-22.** This record covers the FD2104 F1 changes in conventional
Flying and New-generation Flying: two child-fixed metal saddles per seat join
the true posture shaft to the structural pan. It does not reopen or recertify
unchanged family construction, route qualification or every graphics profile.

The [structural reconciliation](../base-structure-reconciliation.md#f1--flying-shaft-saddles)
and [source audit](source-audit.txt) establish the exact geometry. Both new
saddles remain inside the former seat bounds and below cushion top; all existing
seat elements, rider-contact surfaces, restraint/leg meshes, pivots, physics and
sweep limits remain unchanged. Their model coordinates are `[0,4,6]..[2,7.5,10]`
and `[14,4,6]..[16,7.5,10]`. The audit excludes upholstery from the load path,
reproduces the former .065-block structural separation and proves the new
shaft → saddle → pan connection in all eight exports.

## Run identity and setup

- Owner: `/root/fd13_body_design`, private helper owner `fd13-flying`.
- Primary `main`, baseline HEAD `b5e78f31b72a6c22dae25219d1eefa1f5de20a3e`
  plus FD2104 working-tree source. No source or generator edit by this verifier.
- Persistent root `/mnt/NVME4/fourthdimension-runs/fd13-flying-20261122`;
  saved dedicated world `server/FlyingSupports`.
- Private host Xvfb `:342`; loopback game/RCON ports `25723`/`25724`.
- Owned units `fd13-flying-20261122-{client,server,xvfb}.service`.
- Java 21.0.13.1 / Gradle 9.2.1; isolated build and launch scripts,
  `-PfdPackaged -PfdValidation --offline --no-daemon`; client heap1536M inside
  Minecraft-only6GiB unit, server heap2048M inside4GiB unit; render6/simulation4.
- Prelaunch host available memory43GiB, user cgroup `memory.max=max`,
  `memory.current=44404502528`; persistent XFS NVME4 had163GiB free.
- [Exact JAR and eight embedded asset hashes](artifact.json); all eight Flying
  exports in the built JAR were byte-equal to the final F1 source exports.
  [Build output](build.log) reports success.

Command-built original demo routes and explicit player positioning were disclosed
setup, not player construction evidence. Both normal boarding attempts that pass
below used an empty hand on a confirmed reachable F3 seat target. Mouse defaults
and raw input were verified. Private XTest camera turn and reachable vanilla lever
use were checked in this same connected client; `FD13_FLYING_INPUT_PASS` is in
[server events](server-events.txt). All actions and observations retain their raw
[input](input.jsonl) and [console](console.jsonl) records.

## Conventional Flying

Vehicle `901249b9-0e83-4513-95c2-7997ed34d416`, posture child
`c10e24e9-d4bc-4e09-99f8-d44d92a27123`.

Normal seat use mounted Dev. Normal Rotation Tool use opened the editor; separate
body and leg close buttons succeeded. Set prone traversed the actual native
posture joint and reached `PRONE_LOCKED`; Set seated returned to `SEATED_LOCKED`.
Opening both restraints and Shift unloaded normally. No helper boarding, pose
assignment or direct restraint mutation was used.

The [trace summary](conventional-summary.json) retains first/last records for
all six occupied phases, 2,906 occupied samples, exact rider UUID, hip/hinge
positions and raw trace location/hash. All occupied samples have an empty fault.
The stopped native mass changed from4.0 to4.8 while occupied, then the final
trace returned to no occupants. These are measured state observations, not a new
full mechanics qualification.

- [Empty seat and support target](conventional-empty-target.png)
- [Occupied upright, underside/support view](conventional-upright-occupied.png)
- [Occupied intermediate posture](conventional-transition.png)
- [Occupied prone support and rider view](conventional-prone-occupied.png)

The saddle/pan assembly follows the child posture continuously in these views.
No new detached patch, missing texture or saddle intrusion into the rider/contact
area was observed. Some rider/body portions are obscured by the existing frame;
source geometry and live occupancy complement those views rather than treating
one image as a complete structural proof.

## New-generation Flying

Vehicle `e1a2aa0c-0b0c-4561-8a36-ad27e845e129`, posture child
`564d1ee4-b737-4459-b857-feb6c019d9de`.

An initial temporary staging platform at `-205..-203,120,114..120` entered the
combined sweep; the existing guard correctly reported
`COMBINED_SWEEP_OBSTRUCTED at -203,120,114`. The unsuccessful initial seat-use
attempt is not boarding evidence. The staging blocks were removed; the original
walkway at surfaceY120 was used. Normal Retry/revalidate and Unwind + seated
cleared the setup fault and restored the boarding lock; both restraints were
then opened. No production correction or repeat launch was needed.

Normal empty-hand use then mounted Dev (authoritative `riders=1`). Both restraint
buttons succeeded; Dispatch route started actual chassis travel and nested yaw/
posture movement. [Read-only observations](ng-observations.json) include changing
physical yaw and posture, `safety=null`, and prone phi1.569814rad. The source-local
saddles remained attached through the compound transition. Normal Stop disabled
travel while retaining the occupied prone posture and closed restraints.
[Saved stopped state](stopped-state.json) records `PRONE_LOCKED`, `lock=PRONE`,
no fault, the rider and original identifiers. This check intentionally stops after
the affected support sweep; prior full-route/unload acceptance carries forward.

- [Empty seat on original walkway](ng-empty-target.png)
- [Occupied upright support view](ng-upright-occupied.png)
- [Occupied simultaneous yaw/posture transition](ng-compound-transition.png)
- [Occupied prone endpoint](ng-prone-occupied.png)
- [Normal stopped prone state](ng-prone-stopped.png)

No new saddle detachment, missing texture or rider-contact intrusion was observed.
The unchanged nested yaw/posture contract, full route and unload behavior retain
[accepted New-generation Flying evidence](../../0.7/new-generation-flying/README.md);
conventional behavior retains its [accepted family record](../../0.7/conventional-flying/README.md).

## Cleanup and scope

[Cleanup](cleanup.txt) verifies all three owned units inactive with MainPID0.
The client disconnected normally, server reported zero connected players, the
client process exited, and RCON Stop cleanly saved all dimensions/sublevels.
Xvfb stopped after its client. The world, exact build/JAR, logs, raw conventional
trace and all timestamped images remain at the persistent run root; selected
images copied here are listed in [the image map](screenshots.json).

This closes F1's changed occupied attachment/fit observation for both Flying
families when combined with the unchanged-interface source audit. It makes no
new all-roster structural certification, packaging/release or FD2102 variant claim.
