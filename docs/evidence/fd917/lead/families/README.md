# Axis and Roller Ball production fit

Lead evidence-only transfer from Mechanisms; generator/source ownership remains
with Mechanisms. Axis fit and representative motion passed. Roller Ball occupied
open/closed fit passed; motion and owned-runtime cleanup are in progress.

Run `/mnt/NVME4/fourthdimension-runs/fd917-lead-families-20261115`, owner chat
`01a1dd1d-1468-7f81-8f31-5532ad7a178d`, private display318, loopback25658/RCON25659,
world `server/FamilyWorkshop`, units `fd917-lead-families-20261115-{client,server,xvfb}.service`.
Java21, client1536M heap/6GiB scope, server2048M/4GiB,854x480/60fps,
render6/simulation5, Raw Input On and default mouse bindings. Frozen candidate04
SHA256 `9030cc1ff5f16c2af9a8ffa1e98138eb092a2bb2e1b1605d3c2193f39f9a8d45`.
The later S2 example setup fix does not alter either family's geometry or runtime.

This live client first proved relative XTest camera motion (-180 to-176.85 degrees)
and a reachable vanilla lever false→true through normal Use. Input uses the owned
display/window via `scripts/fd-private-client.py`. Demos, forceload and calculated
eye/target placement are disclosed setup. Each boarding input followed an F3-named
seat within reach; no synthetic rider or seat-pose override was used.

## Axis

Demo origin(0,100,0), chassis8297ca8d-d73e-49f9-8222-467efc4975f5,
carrier b747ab1f-4c66-422e-8192-931809af3d6a. Normal empty-hand Use mounted at19:44:09UTC.
Normal mounted-editor Close/Open shows the actual rider in the two-seat pod,
moving shoulder bars, head/leg clearance, fork and longitudinal roll hub:
[closed](axis-occupied-closed.png), [open](axis-occupied-open.png).

Normal Dispatch at19:50:18 traversed the banked DRIVE full turn and DAMP/FREE
return. [Occupied roll frame](axis-occupied-roll.png) at40s into the retained
180s video shows the actual carrier/hub and parent fork remaining connected.
[470 readbacks](axis-motion.jsonl) span actual angle-.61445 to6.27295rad with
one real rider retained. This is distinct production fit/motion evidence, not
a new qualification of the unchanged Axis program/physics contract.

Return capture was late: the normal lift-entry guard stopped the car atz36.77,
before the two-block unloading inset. Ordinary Capture cleared the fault and
latched upright; Open correctly remained rejected outside the inset. Normal
Resume to dock moved it forward, and the disclosed watcher called ordinary
Capture atz45.55. It settled Ready atz45.88. An editor command using the old
revision was correctly rejected; reopening the editor refreshed it. Normal Open
then Shift unloaded alive at19:54:44, restraints=false/riders=0/fault empty.
No guard was bypassed or runtime source changed for this setup recovery.

## Roller Ball

Demo command origin(-300,100,300), actual dock anchor(-300,128,300),
chassis35dd29d6-056c-4b22-b194-0b90aa6d2960, shared carrier
773ad945-1895-4f15-98b4-476de2f3237b. Normal Use mounted the F3-named outer seat
at19:55:18. The [open](roller-ball-occupied-open.png) and
[closed](roller-ball-occupied-closed.png) views show the raised/closed bar, actual
rider clearance, enclosing back-to-back shell and transverse axle connection.
Normal Dispatch began at19:57:10. Motion record is in progress.

Full videos, input before/after images, connection/readback logs and saved world
remain in the persistent run root. Runtime is still lead-owned and active until
the Roller Ball case and save/cleanup complete.
