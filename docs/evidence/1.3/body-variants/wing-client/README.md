# FD2102 Compact Wing client acceptance — 2026-11-22

Passed the affected Wing body workflow: local world/miniature preview, cancel, authoritative Apply, independent upholstery, occupied normal boarding/restraints/ride/stop/unload, cold-process persistence, missing-resource fallback and resource restoration. No product fixes were required by this run. This is the Wing contribution; Fixed Inverted and native service guards have separate evidence.

## Artifact and isolated runtime

Primary `/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`, baseline HEAD `b5e78f31b72a6c22dae25219d1eefa1f5de20a3e` plus the shared 1.3 working changes. [artifact.json](artifact.json) records exact source hashes and all 22 body exports; all recorded source hashes still matched at handoff. Java 21.0.13.1 / Gradle 9.2.1, offline `--no-daemon` packaged validation build: [build.log](build.log).

JAR: `/mnt/NVME4/fourthdimension-runs/fd13-wing-body-20261122/build/libs/coasters-fourth-dimension-1.1.0.jar`, SHA256 `90ddc4e88d0fc280cc572e0a821dfdad5520a30e5f609c5578dab087417ca24b`. Parent independently matched this to the other body's build.

Run root `/mnt/NVME4/fourthdimension-runs/fd13-wing-body-20261122`; world `server/WingBodies`; private host Xvfb `:342`; endpoint `127.0.0.1:25723`, RCON `25724`; owner `fd13-wing-body`. Client 1536 MiB heap / 6 GiB Minecraft-only service; server 2048 MiB heap / 4 GiB service; Gradle outside both. Render distance 6, simulation 4, raw mouse enabled, default right-use/left-attack. Initial host headroom 47 GiB and target XFS free space 163 GiB; restart headroom 43 GiB. Second-process observed memory peaks: client 3,208,687,616 bytes, server 1,675,964,416 bytes.

## Setup and observations

Disclosed fixture construction: `execute as Dev positioned 100 120 100 run fd demo wing`, UUID `13320978-85f5-4b70-9afd-d94c8c659e44`, mechanical variant `wing_fixed_v1`. Commands provided safe viewing platforms, Creative inventory and initial camera positioning; F3 identified the reachable outer Wing seat. The helper independently demonstrated camera turning and reachable vanilla lever use before mod interaction. Boarding, Tools, body/palette choices, operator actions and unloading used normal input. Source-local `out_sub_level` aim was setup, not proof of normal camera operation. After the ride, loading the destination chunks before placing a viewing platform resolved an unloaded-position setup error; no game-code fix or repeated input guessing was involved.

| Required behavior | Evidence |
| --- | --- |
| Original and Compact miniatures/world preview | [Original](original-picker.png), [Compact local preview](compact-preview.png). Flushed [preview-unsaved.json](preview-unsaved.json) retains `body=base`, revision 0. |
| Cancel restores saved body | [Reopened picker after Cancel](cancel-restored.png) shows Original saved. |
| Apply persists authoritative body | [Apply](applied-body.png), action 82 success at 10:54:38.892 in [server-process1.log](server-process1.log), [applied-body.json](applied-body.json). [NBT difference](apply-body-diff.json) is only `base → wing_compact` and revision `0 → 1`. |
| Upholstery remains independent | [Crimson Apply](applied-palette.png), action 81 success 10:55:11.792. [Difference](apply-palette-diff.json) is only `ORIGINAL → CRIMSON` and revision `1 → 2`; body, mechanical variant, appearance and carrier IDs remain unchanged. |
| Normal occupied workflow | Mount accepted at 10:55:52.183, offset `(4,1,0)`; [occupied open](occupied-open.png), normal close action 11, [dispatch](occupied-dispatch.png), [banked ride](occupied-bank.png), normal Stop action 10, Open action 12, [stopped open](stopped-open.png), [normal Shift unload](normal-unload.png). Server logs mount/dismount and all four operator successes. |
| Cold process persistence | Both first processes exited after normal disconnect and complete world save: [inactive checkpoint](cold-restart-inactive.txt). New client PID 704556/server PID 704498 loaded the same world; [reopened picker](cold-restart-picker.png) and [cold-reopened.json](cold-reopened.json) retain `wing_compact`, `CRIMSON`, revision 6, mechanical `wing_fixed_v1`, appearance `original`. |

The occupied ride physically moved from approximately `(100.5,124.325,154.5)` along the straight to `(100.255,124.418,202.302)` and through the banked segment to `(150.176,124.116,225.342)`; Sable reported velocity about 4 blocks/s and the bank quaternion. Stop settled around `(195.118,124.385,209.706)` before restraints opened. No new visible detached body, rider intrusion or missing texture was observed in these affected views. This is not a full-loop or all-family campaign. [ride-unloaded.json](ride-unloaded.json) records no occupants, restraints open, stopping set and no fault. Existing mechanical/sweep acceptance and native service guards are reused, not claimed as new client observations.

## Reload and missing-resource recovery

A removable private resource pack replaced only `body_wing_compact_cyan.json` with a cube using an intentionally nonexistent texture ([exact override](intentional-missing-model.json)). It initially sat below Mod Resources, so the override did not apply; the standard pack UI confirmed that ordering. Moving it above Mod Resources caused the actual reload at 11:05:02.301 and the expected missing-texture warning in [client-process2.log](client-process2.log).

The [fallback picker](missing-resource-fallback.png) keeps Compact selected/saved, displays **Model unavailable; showing Original**, and renders the Original miniature without a missing-texture cube. The visible near-seat shell changes, but the picker partially covers it and the far seat is small; the claim that every affected world part falls back together also relies on the reviewed whole-choice availability check and model wrapper, rather than treating this screenshot as an unobstructed survey of the entire car. [fallback-state.json](fallback-state.json) still has `wing_compact`, Crimson, revision 6. Removing the single override and using standard world F3+T at 11:05:47 restored the [Compact picker/world](resources-restored.png), with no unavailable warning. [restored-state.json](restored-state.json) retains the same saved fields. The remaining edit-eligibility hint reflects the post-ride state; no new Apply was attempted there.

Vanilla F3+T while a picker is open is ignored by its keyboard handler. This run therefore performed real reloads outside the picker and reopened it. Same-picker preview-ID survival is source-review coverage, not a claimed live observation. The temporary pack was removed from the stopped client's resourcepacks directory after restoration, and its selection removed from options. Its metadata and intentional override remain in run evidence.

## Reproduction and cleanup

Raw [input.jsonl](input.jsonl) and [console.jsonl](console.jsonl) record expected readbacks and every setup command. All original before/after screenshots remain under the persistent run root; the selected images above are copied here. Screenshot/action timestamps are UTC; game logs use America/Phoenix (UTC−7). First and second client/server logs are retained separately.

Final normal disconnect, RCON `stop`, complete world/sublevel save at 11:06:21, and title-screen Quit completed before Xvfb stopped. [cleanup.txt](cleanup.txt) verifies client, server and Xvfb units `fd13-wing-body-20261122-{client,server,xvfb}.service` all inactive, MainPID 0, Result success. World and logs are preserved. No other owner's runtime or source was modified. Evidence and prior UI implementation remain uncommitted for the parent integration owner.
