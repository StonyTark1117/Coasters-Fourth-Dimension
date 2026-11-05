# FD401D selected-JAR two-car Arrow occupancy outputs

Run date: 2026-11-05. This is one bounded **combined-profile** client observation on the selected qualification JAR. It closes an occupied two-car Arrow output slice only; it does not close FD401D's full family/profile/source matrix.

## Identity and setup

- Selected artifact: `docs/evidence/qualification/2026-11-05-ec2fcda/qualification.jar`, SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
- The launched build copy had the same SHA-256. Its combined profile loaded Track Styles 1.2.0 and Engineered 1.1.1. Java was OpenJDK 21.0.13; Gradle reported 9.2.1 with Java 21. Clients used `-Xmx1536M`, separate game directories, separate private Xvfb displays, and separate 3 GiB systemd scopes. The dedicated server ran in a separate 4 GiB service scope.
- Input world: [selected-JAR family telemetry fixture](../fd401d-selected-family-telemetry-656725bc-20261105/input-world.tar.gz), SHA-256 `7d5b707ab3a83f440efe0ba3ba70516b7f72efa970709fac578aca430154442a`. It already contained a two-car Arrow station and a Create Display Link targeting the sign at `(794,84,38)` with source `fourthdimension:ride_occupancy`; no block or source edits were made during this run.
- The remote station was at `(794,83,32)`. Dev's normal seat mount was `BlockPos{x=20481032,y=127,z=20546568}`. The sign's front and back both rendered the rider count.

## Result

1. Dev mounted the trailing Arrow seat through normal client interaction. The server logged `FD_BOARDING_EVENT ... action=mount` and accepted the rider assignment. Observer received the selected-JAR client telemetry packet for `(794,83,32)`: `family="arrow_controlled"`, `cars=2`, `occupied=1`, `capacity=8`, `phase="BOARDING"`, `restraints=0`, and dispatch denied until carrier capture and restraints are closed. The occupied Create goggles view shows `Riders: 1 / 8 (2 cars)` with the corresponding BOARDING and denial fields.
2. After the first server interval ended, Dev's saved seat assignment was restored when Dev rejoined the restarted server; the server logged `FD_BOARDING_RESTORE mounted`. In that restored state, the selected-JAR sign rendered `1 / 8` and the server sign data matched on both sides.
3. Dev used the normal Shift dismount. The server logged `FD_BOARDING_EVENT ... action=dismount` and `FD_BOARDING_DISMOUNT_CLEAR`; `fd status` returned the trailing Arrow occupancy to zero and both sign sides rendered `0 / 8`.
4. The final server shutdown saved all dimensions and chunks cleanly. The saved final world is retained here.

The first interval's server stop interrupted the observation before a normal dismount. It is retained as a separate interval, not reported as a clean lifecycle. The reconnect interval then exercised the persisted player-seat assignment and completed the normal unload. No dispatch, restraint closure, or physical ride motion was attempted; those are outside this occupancy-output slice.

## Evidence files

- `server-first-interval.log`, `dev-first-interval.log`, `observer-first-interval.log`: original normal mount, accepted assignment, and exact client telemetry/goggles interval.
- `server-reconnect-interval.log`, `dev-reconnect-interval.log`, `observer-reconnect-interval.log`: server restart/rejoin, automatic saved-seat restoration, normal Shift dismount, output reset, and clean final stop.
- `screenshots/occupied-goggles-and-world.png`: occupied two-car goggles output.
- `screenshots/normal-arrow-mount.png`: normal Arrow seat interaction and mounted UI.
- `screenshots/occupied-sign-1-of-8.png`: rendered physical sign in the restored occupied state.
- `screenshots/normal-shift-dismount.png`: client view immediately after normal Shift dismount; server log and sign capture below provide the state assertion.
- `screenshots/unloaded-sign-0-of-8.png`: rendered physical sign after unload.
- `rcon-final-state.txt`: read-only queries and their observed state values, including both sign fronts/backs and the linked Display Link source.
- `final-world.tar.gz`: cleanly saved world after normal dismount and server shutdown.
- `SHA256SUMS`: hashes for retained artifacts.

This contributes one combined-profile selected-JAR Arrow row to FD401D. Base, Styles-only, and Engineered-only client output, other-family client output, PAX unavailable/fault visuals, complete source coverage, and the remaining dispatch/re-entry output scenarios remain open.
