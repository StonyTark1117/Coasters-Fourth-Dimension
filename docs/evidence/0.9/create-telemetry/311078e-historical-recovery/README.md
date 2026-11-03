# Historical occupied Arrow realignment — 311078e

Bounded result: the original occupied support-loss world now completes normal
Capture → Realign matching track → station Open restraints → rider Shift unload.
The old physical-stop rejection is no longer reproduced in this case. The
**End of compatible track** warning remains after realignment and unloading;
this is not evidence of fault-display clearance or a complete release gate.

## Exact artifact and input

- Source: `311078e8229e54514b1e3e0cbb4837bf87066ce5`.
- Packaged `coasters-fourth-dimension-0.9.0-dev.27.jar` SHA-256:
  `dc60cbdefe35cf2737573e57b1509f790cf82342bea6a81764a49066b2b72fd6`.
- Standalone profile, schema 6 / protocol 7, validation observations enabled.
  Exact dependencies are recorded in `artifact.json`. The build passed with
  compilation/unit outputs reused from cache; no fresh native suite is claimed.
- Full copy of the pristine stopped `de667f7` historical failure world, with
  matching input foundation hash
  `7b30ddbf3abf72f1002a3935992d6331d1b89ee2beed15f7ccc100d9098ca821`.
  `input-world.json` records original and copy paths. The embedded level name
  still says `telemetry-de667f7` in logs; the runtime directory is `telemetry-311078e`.
- Two normal connected clients, Dev (original rider) and Observer (operator).
  No direct vehicle-state writes, injected velocity resets, validation boarding,
  or repair shortcuts were used. RCON captured read-only state and saved the world.

## Recorded sequence

`observations.jsonl` preserves six actual observations, including the unsuccessful
attempt; labels do not override their recorded state.

1. `historical-loaded`: original chassis and both carriers restored with Dev in
   original carrier seat `274877911039`. Pending alignment and boundary warning;
   closed restraints, one assignment, boarding not ready.
2. `historical-after-capture`: normal rider Rotation Tool Capture; pending
   alignment and residual physical motion remain.
3. `historical-after-realign`: normal **Realign matching track** reports settings
   updated. Alignment-required changes 1 → 0 and boarding-ready changes 0 → 1.
   Native chassis position changes from about `(31.34,80.67,38.55)` to
   `(32.50,84.12,38.36)`. Original chassis/carrier IDs, rider assignment, seat,
   complete saved rail records and profiles are preserved. The sampled velocity
   is small afterward; this observation does not measure the atomic reset itself.
4. `historical-normal-unloaded` is a **failed unload attempt**, despite its name.
   The rider aimed the Rotation Tool at platform stone; the editor did not open.
   The subsequent attempted button click broke one platform stone, and Shift
   could not unload with restraints still closed. The `historical-ready-editor`
   screenshot is the tool's use-on-block message, not an editor. The two
   `historical-unloaded-*` screenshots still show the rider assigned / 1 of 4.
5. Observer opens the normal bound station editor. `historical-station-open-restraints`
   records successful **Open restraints**, one rider still assigned, aligned,
   and dispatch no longer ready. The station UI and goggles show the result.
6. Dev presses Shift normally. `historical-normal-unload-confirmed` records zero
   assignments, empty occupants/rider-seat mappings, open restraints and no Dev
   RootVehicle. The corresponding Dev and Observer screenshots show dismount
   and goggles **0 / 4**. Stopped foundation data confirms this state.

The historical layout has only the middle-to-B matching span available; the
original A anchor is absent. Its old lower scaffold and unused side anchor were
not removed. The endpoint warning is present in saved state, goggles, editor and
same `ride_fault` Display Link sign after realignment. Goggles show aligned and
ready while restraints are closed despite that warning; no dispatch was tested.
This bundle does not decide whether the warning is desired endpoint policy or a
remaining fault/readiness presentation problem. It must not be reported as a
cleared fault or complete restored route.

## Shutdown and retained evidence

An initial RCON invocation accidentally concatenated `save-all flush stop` and
was rejected as an invalid command. Separate `save-all flush` and `stop` commands
then completed normally. Server session 97883 exited 0 with all dimensions saved.
Observer displayed a connection-reset message during server shutdown; Dev showed
Server closed. Both clients returned through their menus and **Quit Game**, with
sessions 48140 and 48596 exiting 0. DISPLAY=:121 had zero child windows before
its owned Xvfb process received SIGTERM; its wrapper exited 0. See full compressed
logs and `shutdown-state.json`.

The full stopped world is retained locally under
`build/telemetry-local-acceptance/311078e/after-clean-stop-world`; its exact
foundation NBT and decoded data are committed here. A separate FD403 packaged
client on DISPLAY=:99 was concurrent and untouched. No timing/performance claim
is made. The successful fresh `effa1c6` world was not rerun or altered.

Run `python3 verify.py` from this directory (or pass its full path). The verifier
checks bounded saved-state invariants, shutdown records/log tails and all bundle
hashes. It does not certify renderer geometry, optional profiles, moving support
loss, cold restart after this repair, native seat-entity UUID preservation, all
families, or FD401/FD902/0.9 completion. Screenshots remain raw captures.
