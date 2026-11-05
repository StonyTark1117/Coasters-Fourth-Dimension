# FD401D selected-JAR family telemetry replay — partial, blocked

Run date: 2026-11-05. This was a bounded replay attempt using the selected
artifact and the existing candidate-family telemetry world. It did **not**
complete FD401D's family occupancy matrix.

## Inputs and run configuration

- Built JAR: `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
  `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
- Input `world.tar.gz` copied from `fd401-candidate-family-telemetry-81ee2e6`,
  SHA-256 `7d5b707ab3a83f440efe0ba3ba70516b7f72efa970709fac578aca430154442a`.
- Java 21. Client and observer used isolated game directories and direct launch
  wrappers with `-Xmx1536M`, each in its own systemd scope with 6 GiB cap.
  Server used an isolated world and port 25695 in a separate 4 GiB scope.
  Private displays were :341 and :342. Both clients were set through Video
  Settings to render distance 6 and simulation distance 5 before joining.
- The selected JAR loaded from the exact prepared build path. Launch arguments
  and wrappers are in `launch/`.

## Results

Observer and Dev joined successfully. The saved server log records the restored
Arrow cart link. Observer received an initial Arrow telemetry packet at station
`(794,83,32)` reporting `family:"arrow_controlled"`, `cars:2`, `capacity:8`,
`occupied:0`, and `phase:"BOARDING"`.

The Legacy normal seat interaction did not succeed. The bounded F3 aiming
captures and server log contain no `FD_BOARDING_EVENT ... action=mount` for Dev;
`RootVehicle` was absent during inspection. An incidental Create Display Link
editor interaction was captured and is not counted as a family transition.
The prior replay's Legacy seat log coordinate was
`BlockPos{x=20481032,y=127,z=20489225}`. The current attempted normal interaction
did not reach that seat. Free Spin, Revised, Legacy occupied telemetry,
goggles/snapshot transitions, and trailing Arrow occupancy were therefore not
run.

The clients were stopped, then the server received `stop`; the final server log
records all dimensions saved and the RCON listener stopped. The cleanly saved
final world is `final-saved-world.tar.gz`. Full logs and selected screenshots
are preserved here. Secrets (including the RCON password) are omitted.
