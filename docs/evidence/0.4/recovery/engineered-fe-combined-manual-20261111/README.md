# FD402F2 combined-profile manual recovery attempt — setup blocker

Date: 2026-11-11

This folder preserves a bounded diagnostic attempt for FD402F2. It does **not**
close FD402F2 and does not establish the requested manual recovery cycle.

## Result

- The retained world loaded with the Engineered and Track Styles profiles
  present. `client-attempt5-corrected.log.zst` records the Track Styles marker.
- Inspection established that the retained recovery archive was preparation
  state: the certified ordinary bearing was detached rather than an attached,
  braked recovery consist.
- A temporary validation-only command selected the certified ordinary bearing
  and staged it through the native teleport/reset-velocity pipeline. The client
  reported `spacing=6.0`; `06-native-pipeline-staged-spacing.png` records that
  diagnostic state.
- The normal operator editor was then used. Restraints were closed and the
  production **Attach recovery** control was selected without bypassing the
  production recovery guard.
- Production rejected the attach at 07:15:35 with `Place the recovery bearing
  6.0 blocks from the end car axle`. After a second exact restage at 07:16:12,
  the same normal control was rejected again at 07:16:13. The log and
  `07-own-editor-before-attach.png` through
  `09-own-attach-rejected-after-restage.png` preserve those observations.

The loose bearing's existing running-contact constraint resumed control on a
live tick, so a one-frame position mutation did not produce a stable native
setup accepted by the production guard. The next bounded attempt must create or
realign one fresh bearing through the existing native running-contact setup
path, verify its spacing after live ticks, and only then use the unchanged
production controls. That staging repair is a fixture issue, not new product
acceptance scope.

## Attempt and ownership notes

- Attempts 1–3 diagnosed retained-world state, bearing selection, and the
  unstable logical-position staging approach. Attempt 4 was a pre-world launch
  failure after its owned Xvfb disappeared. Attempt 5 used the corrected native
  teleport/reset-velocity diagnostic but still hit the production spacing
  guard.
- Attempt 5 ran on the task-owned Xvfb `:244`, used Java 21, a 1536 MiB game
  heap, and a 6 GiB Minecraft-only scope. Its measured peak was
  2,814,521,344 bytes, below the scope limit.
- Another task opened a window on `:244` during attempt 5. One accessibility
  **Continue** action was mistakenly sent to that window before the collision
  was identified. All subsequent input was restricted to this task's window
  (`WID 2097160`, PID 406496). No acceptance claim depends on the other window.
- The world completed its save (`All dimensions are saved`), but Xvfb was
  stopped before the client had fully exited. The final `X connection ...
  broken` means this record must not be cited as a clean client exit.
- All task-owned clients and Xvfb processes were stopped after capture. The
  temporary validation command was removed from the source tree; this attempt
  leaves no product or validation-code change.
- Persistent working data remains at
  `/home/braydon/.local/state/fourthdimension-runs/fd402f2-20261111` for the
  owning FD402 coordinator's follow-up.

## Artifacts

- `client-attempt1.log.zst` through `client-attempt3.log.zst`: diagnostic client
  logs from the retained-world and staging investigations.
- `client-attempt4-launch-failure.log.zst`: excluded pre-world Xvfb failure.
- `client-attempt5-corrected.log.zst`: corrected run, exact staging reports,
  both production guard rejections, save completion, and non-clean X loss.
- `direct-client-attempt1.sh` through `direct-client-attempt5.sh`: preserved
  direct Java 21 launch commands.
- `00-combined-world-loaded.png` through `09-own-attach-rejected-after-restage.png`:
  diagnostic screenshots. None is an FD402F2 acceptance capture.
