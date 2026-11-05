# FD401D selected-JAR PAX Create Display Link outputs

This records one static, normal-UI Display Link scenario for the FD401D gate. It exercises the six registered ride sources on the selected `0.9.0-dev.27` JAR while the PAX winch remains at BOARDING. It does not repeat the occupied cycle. The selected JAR is the same artifact preserved in the [preceding phase telemetry bundle](../fd401-pax-selected-source-phase-telemetry-20261105/README.md), SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`. The input save is the final PAX world from that bundle (`e0d4977c55671f3d7579e2f758fb899959161e36e0a4dc76442761e96dabefc4`).

The run used the combined profile, Java 21.0.13, an integrated singleplayer client, private Xvfb `:277`, `-Xmx1536M`, and a 3 GiB systemd scope. The scope was confirmed active at the cap before use and remained below it (last sampled 2.77 GiB); it exited successfully. At launch the host reported 28 GiB available memory. The selected artifact SHA, Java launch classpath, VM arguments, program arguments, compressed integrated client/server log and saved output world are included or linked here.

The Display Link was placed on the existing `fourthdimension:shuttle_winch` using Create's normal two-click link item workflow. The target fixture was a command-placed oak standing sign at `(-856,146,84)` on a stone support at `(-856,145,84)`; its placement is disclosed because it was not part of the input route. The sign is deliberately a small one-line target, so longer source strings are visibly clipped. The source choices were changed by scrolling the Display Link source field, then applying with the UI check button. Screenshots show the dropdown and the selected UI field for each source.

| Source | Visible sign result in this run | Evidence |
| --- | --- | --- |
| Ride status | BOARDING status (matches phase in this idle, not-ready state); the first capture is at a poor angle, so the text is small | `screenshots/status-source-ui.png`, `screenshots/status-sign-crop.png` |
| Ride occupancy | `0 / 16` | `screenshots/occupancy-source-ui.png`, `screenshots/occupancy-sign.png` |
| Ride restraints | Begins `Restraints`; longer text is clipped by the sign | `screenshots/restraints-source-ui.png`, `screenshots/restraints-sign.png` |
| Ride dispatch readiness | Denial text is written to the sign and clipped; the UI identifies the dispatch-readiness source | `screenshots/dispatch-source-ui.png`, `screenshots/dispatch-sign.png` |
| Ride phase | `Boarding` | `screenshots/phase-source-ui.png`, `screenshots/phase-sign.png` |
| Ride fault | Blank because the current fault string is empty | `screenshots/fault-source-ui.png`, `screenshots/fault-sign.png` |

The initial source-choice dropdown screenshot lists all six choices together. The client log records the final saved state: the winch at `(-856,144,83)` is `BOARDING`, `shuttle_cycles=2`, speed `16`, four bound cars, empty `shuttle_fault`, no occupants and `riding=false`. The final Display Link block at `(-856,145,83)` points to the oak sign and stores `Source.Id="fourthdimension:ride_fault"`; the sign shown for this final source is blank. The final saved world and `fourthdimension_foundation.dat` checksum allow independent inspection. This is a bounded static PAX output row only; FD401D still has family/trailing-car, controller availability and re-entry, fault interruption/recovery, profile agreement, and other behavior rows open.
