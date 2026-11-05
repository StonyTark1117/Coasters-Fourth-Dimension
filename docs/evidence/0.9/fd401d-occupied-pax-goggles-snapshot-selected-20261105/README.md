# FD401D selected-JAR occupied PAX goggles and client snapshot

Run date: 2026-11-05. This combined-profile run used the exact clean selected
qualification JAR and a copied PAX world. It adds a normal occupied BOARDING
observation across the server, Observer-received client telemetry, and Create's
rendered goggles overlay.

## Identity and setup

- Selected JAR SHA-256:
  `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
- Input world archive SHA-256:
  `f4557f2a4fac5a865a3bb8eb7f41fc49cbc9978a062143dc0daedb9bc582c78a`.
- Combined profile: Fourth Dimension, Create, Track Styles and Engineered. The
  run used the current qualification artifact without source or JAR changes.
- Java 21.0.13.0.3. Server and both clients used 1536 MiB maximum heaps; each
  game process ran in its own 3 GiB systemd scope. Gradle preparation was
  outside those scopes. Render/simulation distance was 6. Displays were
  private Xvfb :289 and :290, server port 25655, RCON port 25656.
- The copied PAX input world was loaded on a dedicated server. The bundled
  final world records the saved state at shutdown. Logs, launch arguments,
  prelaunch memory checks, state extracts and checksums are included.

## Observed state

Dev boarded the fourth car by normal seat interaction and closed restraints in
its normal vehicle UI. The server logged `action=mount` and an accepted seat
assignment. `occupied-board-state.txt` records one active rider assignment.

Observer received a real `FD_CLIENT_TELEMETRY` snapshot for the PAX winch at
`(-856,144,83)` with `available=1`, `phase=BOARDING`, `cars=4`, `capacity=16`,
`occupied=1`, `restraints=1`, `dispatch=1`, and no fault or denial. The packet
at 06:33:52 is eight seconds before `screenshots/occupied-goggles.png`; later
packets through 06:37:04 retain the same values. The screenshot shows the
Create goggles overlay reporting 1/16 riders, four cars, 128 su, Boarding,
restraints closed, boarding aligned and dispatch ready. This is matching state
in one uninterrupted, stable occupied interval; the screenshot itself does not
contain the packet overlay.

## Sign limitation

The existing Display Link was above the suspended winch and targeted a sign on
its support. The sign remained blank with source `fourthdimension:ride_fault`
while the fault was empty. Attempts to reach the link's source UI from the
available approach missed or were occluded by the station assembly. The run
therefore does **not** prove rendered sign agreement. The configuration attempt
and blank sign are retained so this remains a recorded failure, not an
implicit pass.

## Scope

This closes only a bounded combined-profile occupied PAX goggles/client-snapshot
agreement slice, joined to a normal mount and closed restraints. It does not
close the Display Link/sign output for this state, the all-six-source matrix
under occupancy, other families, trailing Arrow occupancy, PAX phase/fault or
controller unavailable/re-entry visuals, dispatch/motion transitions, or
base/Styles-only/Engineered-only telemetry profile coverage. See
[FD401D in the roadmap](../../../../ROADMAP.md#fd401-deliver-optional-provider-infrastructure).
