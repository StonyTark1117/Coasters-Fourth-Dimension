# FD917 mechanism and articulated-family evidence

Owner: chat `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`, primary `main` checkout.
The assigned implementation, family/drive observations, transferred cue checks
and owned-runtime cleanup are complete. Lead owns final FD917
reconciliation and integration. This evidence covers changed production visuals;
it carries forward accepted controls, construction and physics qualification.

## Artifact and run identity

Persistent root: `/mnt/NVME4/fourthdimension-runs/fd917-mechanisms-20261115`.
Saved world: `server/MechanismWorkshop`, outside build output. Private display319,
loopback game25656/RCON25657, units `fd917-mechanisms-20261115-{client,server,xvfb}.service`.
Java21; client1536M heap/6GiB scope; server2048M heap/4GiB scope; llvmpipe854x480,
60fps cap, final render5/simulation5, default mouse bindings and Raw Input On.
Earlier02/03 requested simulation4 was rejected by the valid range[5:33]; its
fallback value is not claimed. Input and camera setup use the task-owned display.

| Candidate | SHA256 | Evidence |
| --- | --- | --- |
| launch03 | `1aaf423428c6138218867a391a394e6b6b39b354faad1080ee282ed4dec16506` | PAX full cycle, remote restraint/catch and late join |
| launch04 | `9030cc1ff5f16c2af9a8ffa1e98138eb092a2bb2e1b1605d3c2193f39f9a8d45` | Corrected PAX guide wheels/input coupling; S2, NG Flying; Pipeline lighting/reload; transferred C/D views |
| cue-fix | `9f2114329f246f63b95d46b99bcdfc7cb8bccb4cc517cbc706f71ee9ddcdf9a1` | Remote controller rendering independent of unloaded train body; Art affected cue check |
| indicator-final | `9c14b760e71c69295d697ccd2ef317144d817d96f944094ebb618382af2b406f` | Final exposed lens light plus full-symbol baked station/Dive UVs; all prior fit/motion carries forward |
| lens-light | `2893c2ec01a609c4af2843757f712e21b47629d6614adb95fa498e923deb9ca2` | Art client08 affected live cue proof; same runtime renderer as final |
| indicator-uv | `4c59ef4667334a393d76963039f45447e4463e7d465daf27c7c76fbd305a37c2` | Dedicated full-symbol UVs for indicator_* surfaces; structural material UVs unchanged |
| launch05 | `1cb26e8d7934235b93833c075436d1655b6629a953c7cc0ac99e7adb8bb690bf` | Same production code as cue-fix; gated fixture follows actual motor shaft path; Pipeline reverse/forward/stop |

Immutable JARs are `<root>/<candidate>/libs/coasters-fourth-dimension-0.9.0-dev.27.jar`
except cue-fix, indicator-uv, lens-light and indicator-final JARs are directly in their directories. `compile-09-cues.log` and
`compile-11-fixture.log`, `compile-12-indicator-uv.log`, `compile-13-lens-light.log`
and `compile-14-symbols.log` record successful Java21 compile/package steps. Later cue and
fixture changes do not invalidate the earlier geometry, fit or motion records.
Lead's integration-package manifest checks the final packaged resources.

## Pipeline

Actual chassis `4fc9d890-bd41-494c-b204-4b534c694404`, first edge(0,103,12)→(0,103,60).
The initial occupied route used validation boarding, normal editor Close/Dispatch,
and disclosed ordinary Capture/Open through the gated fixture. It traversed the
full curve/roll route without a safety fault after184 route chunks were loaded;
the earlier unloaded-route Dispatch rejection was preserved, not bypassed.
The occupied return is in [trace](pipeline-travel.jsonl) and
[curve view](pipeline-occupied-return-curve.png); the120 original frames remain
in the persistent run. This is not a claim that those frames isolate every bank angle.

The new paired trucks use authoritative bound-edge contacts and unambiguous graph
continuations, capped at9 candidate edges/3blocks beyond the bound edge. Nearby
disconnected rails cannot become the wheel reference. The upper, guide and upstop
wheels share actual track-frame contacts and connected brackets, with no second
native decorative wheel set. See [day](pipeline-contacts-day.png),
[night](pipeline-contacts-night.png) and [after F3+T reload](pipeline-contacts-reloaded.png).
The native chassis/track in the same view supplies the material/light reference;
steel darkens at night. Restart and returning from remote family areas also
reacquired the body. Reload clears phase caches; a >2block pose jump resets the
travel baseline in source rather than spinning through the discontinuity.

The reverse setup initially changed0 motors: the old gated fixture searched
adjacent blocks while the native demo has two shafts before each motor. The
repair walks at most those two coaxial native shafts and stops at any other
block/unloaded position. On launch05 it changed15 motors. The [reverse trace](pipeline-reverse.jsonl)
and [60fps video](pipeline-reverse.mp4) show z18.23→Capture16.71→stopped15.42,
then restoring the15 motors and [forward trace](pipeline-forward.jsonl)/
[video](pipeline-forward.mp4) show15.42→Capture17.35→stopped18.63. Both have empty
faults. [Reverse frames](pipeline-reverse-sequence.png) and
[forward frames](pipeline-forward-sequence.png) show travel-linked wheel phase;
upper/lower wheels turn oppositely and stop with the carriage. Final Open passed,
restraints=false/riders=0. Fixture direction changes and camera positions are
explicit setup, not normal construction evidence.

## PAX drive and catch

Command origin(-400,100,400), actual origin(-400,105,465), winch(-406,105,473),
lead `e0cfa460-0d88-4620-a11a-3f9eac351bbd`, carriage `8bafc50a-85f8-47e3-a766-2a60eff1a668`.
Normal Close/Dispatch19:29:10UTC produced LIFTING19:29:27→OUTBOUND19:29:28→return
CAPTURING→BOARDING cycles1/fault empty in [cycle trace](pax-cycle.jsonl).
The lead's [observer proof](../lead/observer/README.md) supplies first-spike
carriage beyond winch watch, occupied remote restraint transition and fresh-process
late join with retained jaws. Catch authority is the actual valid constraint;
it is not inferred from saved block NBT. The04 inspect_shuttles readback confirms
caught=true/BOARDING/ready=true/fault=false/RPM128.

The rack/pinion drive follows carriage travel, while input coupling follows actual
RPM. Catch jaws close around the lead-local receiver only while caught. The04
guide-wheel yokes moved from±.75 to±1.25 to clear native train axles at±.7/r.2;
[corrected occupied closed](pax-occupied-catch-corrected.png) and
[open](pax-occupied-open-corrected.png) views verify the final coexistence.
Normal Open19:36:32 and Shift19:36:54 left the train open/empty and retained at
boarding. Remote snapshots include at most64 existing unique bound managed leads;
actual full-capacity codec roundtrip passed at10,277bytes against128KiB accounting.
Nearby unbound winches retain normal BE sync with a bounded1Hz loaded-chunk scan.

## Articulated families and shared restraints

| Family | Changed fit/motion evidence and safe ending |
| --- | --- |
| Conventional Flying | Normal reachable-seat boarding; independent normal body/leg Close; seated→prone→seated through real angle-1.45→0 in [trace](flying-posture-state.jsonl). [Sweep](flying-occupied-posture-sweep.png), [prone](flying-occupied-prone.png), [closed](flying-occupied-seated-closed.png), [open](flying-occupied-seated-open.png) show shaft/fork and actual rider. Normal opening/Shift unloaded. |
| S1 | Normal board; disclosed fixture Close for unobstructed hinge view. [Closed](s1-occupied-closed.png), [yaw](s1-occupied-yaw.png), [open](s1-occupied-open.png), [motion](s1-motion.jsonl). A late Capture window encountered the existing lift-entry guard atz37.04; ordinary Capture cleared it, angle-.0042/latched=true. Open then normal Shift unloaded. No guard bypass or physics change. |
| S2 | Normal board and live governor binding; disclosed ordinary Close/Dispatch/Capture/Open. [Full roll](s2-occupied-roll.png), [closed](s2-occupied-closed.png), [reaction interface](s2-reaction-interface.png), [motion](s2-motion.jsonl), [capture](s2-capture.txt). Actual return Capture atz-377.8 stopped-372.3, aligned~-2π/latched=true/no fault; normal Shift unloaded. Reaction annulus/magnetic span remain separate from native Chain propulsion. |
| New-generation Flying | Normal board/body+leg Close/Dispatch; real nested yaw, posture and full roll, then normal scenery show and return Dispatch. [Changeover](ngf-occupied-changeover.png), [prone](ngf-occupied-prone.png), [closed](ngf-occupied-seated-closed.png), [open](ngf-occupied-seated-open.png), [motion/show](ngf-motion.jsonl), [return](ngf-return.jsonl). Ordinary fixture Capture atz-383.7 stopped~-383; normal Unwind reached psi~.000029/phi~.00139/safety=null. Normal body/leg Open and Shift20:01UTC left open/empty. |
| Axis, Roller Ball, Powered Inverted Yaw | Accepted evidence-only transfer to lead; source ownership stayed here. Use the [lead family record](../lead/families/README.md) for actual occupied fits, adopted sweeps and cleanup. No duplicate run here. |

S2's initial remote demo retained a stale governor BE across unload/reload and
crashed during delayed binding; the world saved fully. Recovery loaded the152
route chunks and normal Use bound the live governor. Lead fixed the fixture
lifecycle and passed its targeted native test (7a32be1f); this did not weaken
controller authority. Original crash and console recovery remain in the run root.

Shared restraint presentation reads authoritative block state, applies0.35s
hinge motion, snaps initial/reloaded endpoints and suppresses the native static
bar only for descriptor-owned dynamic seats. Static placed/Ponder endpoints stay
composed; native optional seat providers remain outside this renderer. Reuse
Art's [family fit](../art/family-fit.md), [items/teaching](../art/items-teaching.md)
and [appearances](../art/appearances.md), including single-bar Suspended reload,
all17 items and combined Skin/Additions/Track Styles plus standalone fallback.

## Equipment state, exports and cost

Free Spin real induction/damping spans and actual carrier sweep are recorded in
Art's [magnetic fit](../art/free-spin-magnetic-fit.md). Ring radius1.48 and native
field placement retain the nominal1/16 radial gap; lateral inner face1.6 against
ring half-width.04 gives .08 clearance. S2 has its own annulus/launch treatment.
Capture calipers follow actual latched/liftHeld state and existing pivots.
Yaw markers remain passive identity; no invented powered-ready cue overrides guards.

Dive/S2/Powered remote controller lamps now render from authoritative snapshots
before the unloaded client-body guard (13ebb055). Art completed the affected Dive
HELD/released/unavailable observation on client08. RELEASE_ARMED is transient within the same
server advance as DROP, so no persistent green armed state is manufactured.
The first tick-frozen HELD screenshot was inconclusive because freeze prevented
snapshot timing while the event advanced dwell; it is not evidence for the source
bug or cue closure. A subsequent stable live fault exposed an independent UV defect: thin lamp faces
sampled only the texture border. ProductionMesh now gives indicator_* faces the
complete symbol UV range, leaving metal/paint/rubber density unchanged. The top
lens light sample also moved from inside the opaque housing to the exposed block
above; ordinary world lighting is preserved. Art's [final cue record](../art/dive-cue-fit.md)
passes on the preserved actual state: [red held power-loss fault](../art/dive-fault-cue.png),
[amber after normal Resume](../art/dive-held-cue.png), then
[off after committed DROP](../art/dive-released-cue.png), release epoch16. No phase
was injected. Art's [follow-up cleanup](../art/followup-runtime-cleanup.txt) confirms
its client/server/display saved and stopped as well. The same symbolic-UV issue affected baked
station windows: eight station/Dive block/item outputs now use explicit full
0..16 UVs for signal/lock/off only. [Bounded export check](indicator-export-check.json)
confirms all84 symbol faces, identical repeat export and unchanged geometry/materials.

[Final export check](export-check-final.json):237 owned outputs/10,180 faces,
repeat generation byte-identical, local textures/aliases and UV bounds valid.
Each `*-outputs.json` is an owned export list, not permission to overwrite other
owners' files. Lead packages the combined resource set.

[600-frame CPU-stage windows](render-cost-windows.txt) retain observed Pipeline
~.57–.72ms, nearby PAX four-car/rack ~.79–.90ms, joints typically~.23–.26ms and
restraints~.053–.14ms; offscreen PAX~.010ms and Pipeline~.001ms. These are bounded
presentation CPU observations, not GPU/full-park qualification. Reuse lead's
[shared baseline comparison](../lead/README.md) and the existing FD904 criteria;
final release performance remains with its owning package. [Final-run scope peaks](final-runtime-peaks.txt)
were client2,341,040,128bytes/server1,486,520,320bytes, below6GiB/4GiB limits.

## Cleanup and preserved handoff

All owned forceloads removed; query returned none. Client stopped, server flushed
and exited with all dimensions saved at20:11:26UTC, then Xvfb stopped.
[Cleanup](runtime-cleanup.txt) confirms all three units inactive/MainPID0/result=success;
no owned recorder/capture/Java launch remained. Other owners' runtimes were untouched.
The saved world, complete logs, console/input records, original frames, immutable
JARs and scripts remain under the persistent run root. No live runtime is handed off.
`CHECKPOINT.md` is historical progress only; this record supersedes its open-work notes.
