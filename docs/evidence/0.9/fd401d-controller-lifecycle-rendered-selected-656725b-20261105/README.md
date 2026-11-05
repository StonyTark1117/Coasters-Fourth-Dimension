# FD401D selected-JAR Arrow controller lifecycle rendered outputs

Run date: 2026-11-05. This is one bounded combined-profile client observation
on the selected clean qualification JAR. It extends the prior selected-JAR
controller lifecycle fixture with a normally configured Ride Fault Display
Link, physical sign output, and matching goggles/client telemetry across
loaded → unavailable → re-entry → unavailable.

## Identity and setup

- Artifact: `docs/evidence/qualification/2026-11-05-ec2fcda/qualification.jar`,
  SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
- Profile: Track Styles 1.2.0 + Engineered 1.1.1 (combined). Java 21.0.13;
  Observer used `-Xmx1536M` inside a 3 GiB systemd scope. Server used its own
  4 GiB scope. The client and server exited successfully. No Gradle build ran
  during this client session.
- Input: the saved selected-JAR lifecycle fixture from
  [`fd401d-controller-lifecycle-selected-656725b`](../fd401d-controller-lifecycle-selected-656725b-20261105/README.md),
  archive SHA-256 `2dfaf65d8ef40f240d0e02c18f10bfa05fd5997c2ff0e8ba126d137a6657a11e`.
- Remote Boarding Station: `(1000,149,1000)`, bound to the saved one-car
  Arrow chassis. The Create Display Link was normally placed adjacent to the
  station with the Display Link item after targeting the oak sign at
  `(1000,151,1007)`. Its normal UI source selection was `Ride fault` and its
  output was `Line 1`. The observer wore Create goggles and targeted the
  station for the client telemetry snapshots.
- The route chunks `[0,0]`–`[0,3]` were force-loaded for the available states
  with `/forceload add 0 12 0 56`, then released with the matching
  `/forceload remove`. The observer remained at the remote station. No rider,
  dispatch, restraint change, or vehicle motion was introduced.

## Results

The same station and Display Link showed these ordered states:

| State | Server availability | Received client snapshot and output |
| --- | --- | --- |
| Loaded baseline | chassis `1/8`, carriers `2/20`, assignments `0` | `arrow_controlled`, one car, capacity 4, unoccupied `BOARDING`, captured, with the existing `End of compatible track; capture and inspect the route` fault. Goggles and sign output agree. |
| Unavailable | chassis `0/8`, carriers `0/20`, assignments `0` | Snapshot becomes unavailable with zero cars/capacity and fault/denial `Vehicle chassis is unavailable`. Goggles and both sign sides show the unavailable result. |
| Re-entry | chassis `1/8`, carriers `2/20`, assignments `0` | Same loaded Arrow snapshot and compatible-track fault return; the sign and goggles return to that fault output. |
| Returned unavailable | chassis `0/8`, carriers `0/20`, assignments `0` | Unavailable snapshot and sign output return. `forceload query` reports no force-loaded chunks. |

`observer-latest.log` retains the four matching `FD_CLIENT_TELEMETRY` snapshots.
`state-observations.txt` records the server availability and both sign-side
values. The physical sign display clips the longer strings to its available
width; the complete source text is confirmed by its block data. Screenshots
retain the rendered goggles and sign views at each relevant state.

## Evidence boundary

This closes one combined-profile selected-JAR **unoccupied one-car Arrow
controller lifecycle output** slice, including linked sign rendering,
goggles, client packet agreement, and repeated route unload/re-entry. The
loaded controller retained an existing compatible-track fault, so this does
not establish a fault-free ride, dispatch authority under motion, or
occupancy behavior. It does not close other families, other profiles, all six
sources, PAX phase/fault output, or the FD401D matrix.

The final world is saved in the returned-unavailable state. `SHA256SUMS`
covers all retained files in this bundle.
