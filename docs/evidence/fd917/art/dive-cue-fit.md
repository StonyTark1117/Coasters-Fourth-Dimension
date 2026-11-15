# Transferred Dive hold cue — passed

Owner Art `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`, primary checkout/main.
Mechanisms owns the renderer corrections; lead owns integration. This bounded
check reuses the accepted three-car Dive loop in copied `DiveArtWorkshop`.
No Dive construction, physics, occupied lifecycle, or sibling acceptance was
reopened. Normal station Transfer owner, Close and Dispatch started the cycle.

## Diagnosed observations

- The first capture used vanilla tick freeze after real HELD72. That froze
  gameTime/snapshot timing while Dive's LevelTick.Post dwell continued, so it
  cannot establish lamp state. No gameplay phase was injected. Subsequent
  captures run normally through a full client snapshot interval.
- Live HELD48 and actual pedestal power loss produced retained HELD46,
  needs_resume=true and `Dive held power/controller loss; Resume required`.
  All three real bodies were loaded and visible behind the actual controller.
  [State](dive-cue-fault-state.json). [Held before UV fix](dive-held-before-uv-fix.png)
  and [stable fault before UV fix](dive-fault-before-uv-fix.png) are failed visual
  readbacks, not passes.
- The small lamp faces sampled only the dark border of the indicator texture.
  Mechanisms changed `ProductionMesh` to sample the whole framed symbol for
  `indicator_*` only, retaining existing density UVs for other materials.
- That correction exposed a dark red center. A second specific cause was the
  light query inside the opaque controller at y+.96. Mechanisms was given the
  exact surface-light sampling correction; the same held state was preserved
  for the targeted recheck, with no new circuit.

## Run identity

Server stays on art-launch05 SHA256
`9030cc1ff5f16c2af9a8ffa1e98138eb092a2bb2e1b1605d3c2193f39f9a8d45`.
Client06 `9f2114329f246f63b95d46b99bcdfc7cb8bccb4cc517cbc706f71ee9ddcdf9a1`
adds the independently identified remote-control/body-lookup guard correction.
Client07 `4c59ef4667334a393d76963039f45447e4463e7d465daf27c7c76fbd305a37c2`
adds full indicator UVs. Client08
`2893c2ec01a609c4af2843757f712e21b47629d6614adb95fa498e923deb9ca2`
samples exposed light above the top lens, retaining ordinary world lighting. Server gameplay, packet schema and world state are
unchanged across these client replacements. Java21, client1536M/6GiB,
server2GiB/4GiB, owned display317, game25652/RCON25653. Full logs, input journals,
actual state traces and immutable candidates remain under
`/mnt/NVME4/fourthdimension-runs/fd917-art-20261115`.

The controller is (-5,152,280); the real power pedestal below it is switched
between redstone and stone as disclosed fixture input. The native train was captured by the actual Dive hold with46 ticks left before
normal Resume. The saved controller identity is retained. RELEASE_ARMED is transient
within one server invocation, so no stable green state is fabricated or claimed.

## Corrected readback and completion

Client08 shows a clear [red fault cue](dive-fault-cue.png) from the same real
held power-loss state. Power was restored, then a normal Rotation Tool click on
**Resume to dock** revalidated the physical hold. A bounded observer followed the
actual countdown, moved only the observer camera, and recorded synchronized
[amber HELD](dive-held-cue.png) followed by [off after release](dive-released-cue.png).
The [actual state trace](dive-resume-cue-trace.jsonl) records HELD45 then DROP0;
all three cars committed release epoch16, with no safety fault and measured
lead velocity approximately(0,-9.827,5.732). No phase/angle injection occurred.
This closes the transferred truthful state cue criterion. The remote body guard
was reviewed independently; this held train was visibly loaded, so these images
are not relabeled as a separate unloaded-body test.

Later station baked-symbol UV outputs do not change this dynamic Dive lens,
its texture, lighting or protocol; client08 evidence carries forward. The final
world preserves the released train for explicit-Resume continuation if reopened.
No extra return qualification was required. The original remote single-car
unavailable diagnostic remains unrelated and unchanged.

[Final cleanup](followup-runtime-cleanup.txt) confirms all three owned units
inactive/MainPID0 at20:19UTC, all dimensions saved, and client peak below6GiB.
Client/server logs, native sublevels, worlds and immutable artifacts are retained.
