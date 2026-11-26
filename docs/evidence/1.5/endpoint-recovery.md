# Endpoint loss and explicit recovery — 2026-11-26

Passed one required native GameTest in `/mnt/NVME4/fourthdimension-runs/fd15-root-20261122/endpoint-1.log`, Java 21; clean saved shutdown and Gradle exit 0. Source: `MovingEndpointRecoveryGameTests.lostDestinationHoldsOriginalTrainAndRequiresExplicitRepairResume`.

The fixture uses a real production controller, native Wing and Create 64 RPM input, with disclosed Creative construction and fake operator. Normal restraint/Capture/Move paths retain the original registered train. Removing the actual destination anchor during motion produces FAULT_HOLD, no live bridge, closed restraints and stopped native bodies. Resume rejects the missing endpoint. The held bed drifts less than 0.025 m before repair. Restoring the actual anchor and approach does not auto-resume; explicit Resume completes the original four-metre journey, physically authenticates the repaired dock, and Release binds the original car to that support. Thirty additional ticks retain identity, restraints and stable support.

Raw markers: assembly `5901aead-1f56-40dd-8365-9bc8089df2c8`, car `f6957660-4e51-4e12-bddf-21bfe1461de7`; STARTED, REMOVED and PASS at log lines 97, 100 and 113. This closes native endpoint-loss/recovery behavior only; the separate occupied cold-process check remains required.
