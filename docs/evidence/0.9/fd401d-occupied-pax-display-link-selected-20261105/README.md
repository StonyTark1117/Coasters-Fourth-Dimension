# FD401D occupied Ride Occupancy Display Link result

## Result

- Run: `fd401d-occupied-source-ui-final-20261105T1505Z`.
- Artifact: `qualification.jar`, SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7` (selected clean artifact; combined profile).
- Fixture: isolated copy of the retained occupied PAX world. Dev remained attached to a Create seat; winch at `(-856,144,83)` stayed `BOARDING`.
- Normal UI: Display Link at `(-856,145,83)` attached to oak sign at `(-856,146,84)`. The normal source selector was scrolled to Ride occupancy and applied with the GUI Done checkmark. Final RCON block data records `Source.Id=fourthdimension:ride_occupancy`; sign front and back text both read `1 / 16`.
- Occupied telemetry: Observer `FD_CLIENT_TELEMETRY` at 07:19:17.744 and 07:19:58.725 reports `occupied:1`, `phase:BOARDING`, `capacity:16`, `cars:4`, `restraints:1b`, `dispatch:1b`, and no fault. Goggles screenshot `observer-winch-target-f3.png` shows Riders 1/16, Boarding, restraints closed, and dispatch ready. The source selection and final RCON state were recorded at 07:22:45–07:22:59; the winch remained BOARDING and Dev remained mounted across this run.
- UI evidence: `observer-link-target-immediate.png` verifies the immediate F3 target was `create:display_link` at `(-856,145,83)`; `display-link-occupancy-selection-preview.png` shows Ride occupancy highlighted in the standard GUI before applying; `ride-occupancy-output-state.txt` is the final authoritative RCON output for link source, sign text, rider RootVehicle, and phase.
- Limitation: the available post-selection world screenshot `ride-occupancy-link-completed.png` does not visibly frame the sign text and goggles together. RCON independently confirms the final live sign value, while the latest received client packet and goggles capture precede source selection by about three minutes. The client snapshot and sign agree for the same rider count and BOARDING phase, but the evidence is not a simultaneous three-output capture and does not close broader FD401D profile or family coverage.
- Cleanup: server, Dev, and Observer Java processes/scopes were inactive; only task-owned Xvfb displays were stopped. No game files or external user state were changed; this evidence package and roadmap record were added afterward.

## Runtime identity

The observer and operator used the selected JAR above with Track Styles 1.2.0 and Engineered 1.1.1 in the combined profile. `qualification.jar`, `observer-latest.log`, `server-latest.log`, the RCON captures, and screenshots are retained here. The observer received the telemetry packet twice while Dev was mounted; server state remained BOARDING through final selection. This supports one bounded occupied PAX output slice, not the full FD401D acceptance matrix.
