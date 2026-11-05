# FD401D occupied PAX sign output on selected artifact

This run closes one bounded combined-profile FD401D slice: normal Display Link configuration and rendered Ride Occupancy sign output during a normally occupied PAX BOARDING state. It used the selected clean JAR (SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`) with Track Styles and Engineered installed. The isolated input world is the retained PAX world recorded in `run-manifest.txt`.

Dev remained normally seated in a PAX seat (`RootVehicle` was `create:seat`). The winch at `(-856,144,83)` reported BOARDING, four cars, speed 16, and no fault. Observer used the normal Create Display Link source picker. The newly placed link at `(-856,144,82)` was configured as `fourthdimension:ride_occupancy` and targeted the oak sign at `(-852,146,84)`. Final server block data confirms the sign front and back both read `1 / 16`; see `rcon-final-state.txt`.

Observer’s received `FD_CLIENT_TELEMETRY` packet at 10:46:38.439 reported occupied=1, capacity=16, cars=4, BOARDING, restraints=closed, dispatch=ready, and no fault. `occupied-goggles-source.png` shows the matching 1/16, BOARDING, restraints-closed goggles overlay while targeting the winch. Dev remained mounted and the winch remained BOARDING until the sign source was applied and the world saved around 10:51, so the packet/goggles and final sign value agree within the same uninterrupted stable state interval.

The evidence does **not** show sign text and goggles together in one screenshot, and the client snapshot/goggles preceded the final sign configuration by roughly five minutes. This closes only the selected-JAR combined-profile occupied PAX sign-output slice. Base, Styles-only and Engineered-only live output agreement; other families and trailing-car occupancy; controller unavailability/re-entry and dispatch/motion; and the full source/profile matrix remain open.

The source-selection and binding screenshots, complete server/client logs, saved final world, artifact/profile identity, and hashes are retained in this directory.
